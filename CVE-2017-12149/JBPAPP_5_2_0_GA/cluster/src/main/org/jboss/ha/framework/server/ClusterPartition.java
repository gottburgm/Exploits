/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ha.framework.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jboss.bootstrap.spi.util.ServerConfigUtil;
import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.interfaces.ResponseFilter;
import org.jboss.ha.framework.server.deployers.DefaultHAPartitionDependencyCreator;
import org.jboss.ha.framework.server.deployers.HAPartitionDependencyCreator;
import org.jboss.ha.framework.server.spi.HAPartitionCacheHandler;
import org.jboss.ha.framework.server.spi.ManagedDistributedState;
import org.jboss.invocation.MarshalledValueInputStream;
import org.jboss.invocation.MarshalledValueOutputStream;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedOperation.Impact;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementObjectID;
import org.jboss.managed.api.annotation.ManagementOperation;
import org.jboss.managed.api.annotation.ManagementParameter;
import org.jboss.managed.api.annotation.ManagementProperties;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.naming.NonSerializableFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.threadpool.ThreadPool;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelFactory;
import org.jgroups.ExtendedMembershipListener;
import org.jgroups.ExtendedMessageListener;
import org.jgroups.MembershipListener;
import org.jgroups.MergeView;
import org.jgroups.Message;
import org.jgroups.MessageListener;
import org.jgroups.Version;
import org.jgroups.View;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;

/**
 * {@link HAPartition} implementation based on a
 * <a href="http://www.jgroups.com/">JGroups</a> <code>RpcDispatcher</code>
 * and a multiplexed <code>JChannel</code>.
 *
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>.
 * @author Scott.Stark@jboss.org
 * @author brian.stansberry@jboss.com
 * @author Galder Zamarreño
 * @version $Revision: 91481 $
 */
@ManagementObject(componentType=@ManagementComponent(type="MCBean", subtype="HAPartition"),
                  properties=ManagementProperties.CLASS_AND_EXPLICIT,
                  classProperties={@ManagementProperty(name="stateString",use={ViewUse.STATISTIC})},
                  isRuntime=true)
public class ClusterPartition
   extends ServiceMBeanSupport
   implements ExtendedMembershipListener, HAPartition,
              AsynchEventHandler.AsynchEventProcessor,
              ClusterPartitionMBean
{
   public static final String DEFAULT_CACHE_CONFIG = "ha-partition";
   
   private static final byte EOF_VALUE   = -1;
   private static final byte NULL_VALUE   = 0;
   private static final byte SERIALIZABLE_VALUE = 1;
   // TODO add Streamable support
   // private static final byte STREAMABLE_VALUE = 2;
   
   /**
    * Returned when an RPC call arrives for a service that isn't registered.
    */
   public static class NoHandlerForRPC implements Serializable
   {
      static final long serialVersionUID = -1263095408483622838L;
   }
   
   private static class StateStreamEnd implements Serializable
   {
      /** The serialVersionUID */
      private static final long serialVersionUID = -3705345735451504946L;
   }
   
   /**
    * Used internally when an RPC call requires a custom classloader for unmarshalling
    */
   private static class HAServiceResponse implements Serializable
   {
      private static final long serialVersionUID = -6485594652749906437L;
      private final String serviceName;
      private final byte[] payload;
           
      public HAServiceResponse(String serviceName, byte[] payload)
      {
         this.serviceName = serviceName;
         this.payload = payload;
      }
           
      public String getServiceName()
      {
         return this.serviceName;
      }
           
      public byte[] getPayload()
      {
         return this.payload;
      }
   }
   
   /**
    * Used to connect the channel asynchronously from the thread that calls start().
    */
   private class ChannelConnectTask implements Runnable
   {
      private final CountDownLatch latch;
      
      private ChannelConnectTask(CountDownLatch latch)
      {
         this.latch = latch;
      }
      
      public void run()
      {
         try
         {
            ClusterPartition.this.channel.connect(ClusterPartition.this.getPartitionName());
         }
         catch (Exception e)
         {
            synchronized (ClusterPartition.this.channelLock)
            {
               ClusterPartition.this.connectException = e;
            }
         }
         finally
         {
            this.latch.countDown();
         }
      }
   }

   // Constants -----------------------------------------------------

   // final MethodLookup method_lookup_clos = new MethodLookupClos();

   // Attributes ----------------------------------------------------

   private   HAPartitionCacheHandler cacheHandler;
   private   String cacheConfigName;
   private   ChannelFactory channelFactory;
   private   String stackName;
   private   String partitionName = ServerConfigUtil.getDefaultPartitionName();
   private   InetAddress nodeAddress = null;
   private   long state_transfer_timeout=60000;
   private   long method_call_timeout=60000;
   
   /** Thread pool used to asynchronously start our channel */
   private   ThreadPool threadPool;
   
   private final Map<String, Object> rpcHandlers = new ConcurrentHashMap<String, Object>();
   private final Map<String, HAPartitionStateTransfer> stateHandlers = new HashMap<String, HAPartitionStateTransfer>();
   /** Do we send any membership change notifications synchronously? */
   private boolean allowSyncListeners = false;
   /** The HAMembershipListener and HAMembershipExtendedListeners */
   private final ArrayList<HAMembershipListener> synchListeners = new ArrayList<HAMembershipListener>();
   /** The asynch HAMembershipListener and HAMembershipExtendedListeners */
   private final ArrayList<HAMembershipListener> asynchListeners = new ArrayList<HAMembershipListener>();
   /** The handler used to send membership change notifications asynchronously */
   private AsynchEventHandler asynchHandler;
   /** The current cluster partition members */
   private Vector<ClusterNode> members = null;
   private Vector<Address> jgmembers = null;
   private final Map<String, WeakReference<ClassLoader>> clmap = new ConcurrentHashMap<String, WeakReference<ClassLoader>>();

   private final Vector<String> history = new Vector<String>();

   /** The partition members other than this node */
   private Vector<ClusterNode> otherMembers = null;
   private Vector<Address> jgotherMembers = null;
   /** the local JG IP Address */
   private Address localJGAddress = null;
   /** The cluster transport protocol address string */
   private String nodeName;
   /** me as a ClusterNode */
   private ClusterNode me = null;
   /** The JGroups partition channel */
   private Channel channel;
   /** The cluster replicant manager */
   private DistributedReplicantManagerImpl replicantManager;
   /** The DistributedState service we manage */
   @SuppressWarnings("deprecation")
   private org.jboss.ha.framework.interfaces.DistributedState distributedState;
   /** The cluster instance log category */
   private Logger log = Logger.getLogger(HAPartition.class.getName());;
   private Logger clusterLifeCycleLog = Logger.getLogger(HAPartition.class.getName() + ".lifecycle");
   /** The current cluster view id */
   private long currentViewId = -1;
   /** Whether to bind the partition into JNDI */
   private boolean bindIntoJndi = true;
   
   private final ThreadGate flushBlockGate = new ThreadGate();
   
   private RpcDispatcher dispatcher = null;

   /**
    * True if serviceState was initialized during start-up.
    */
   protected boolean isStateSet = false;

   /**
    * An exception occuring upon fetch serviceState.
    */
   private Exception setStateException;
   /**
    * An exception occuring during channel connect
    */
   private Exception connectException;
   private final Object channelLock = new Object();
   private final MessageListenerAdapter messageListener = new MessageListenerAdapter();
   
   private HAPartitionDependencyCreator  haPartitionDependencyCreator;
   private KernelControllerContext kernelControllerContext;

   // Static --------------------------------------------------------
   
   private Channel createChannel()
   {
      ChannelFactory factory = this.getChannelFactory();
      if (factory == null)
      {
         throw new IllegalStateException("HAPartitionConfig has no JChannelFactory");
      }
      String stack = this.getChannelStackName();
      if (stack == null)
      {
         throw new IllegalStateException("HAPartitionConfig has no multiplexer stack");
      }
      try
      {
         return factory.createMultiplexerChannel(stack, this.getPartitionName());
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Failure creating multiplexed Channel", e);
      }
   }

    // Constructors --------------------------------------------------
   
   public ClusterPartition()
   {
      this.logHistory("Partition object created");
   }

   // ------------------------------------------------------------ ServiceMBean
   
   // ----------------------------------------------------------------- Service
   
   protected void createService() throws Exception
   {
      if (this.replicantManager == null)
      {
         this.replicantManager = new DistributedReplicantManagerImpl(this);
      }

//      registerDRM();
      
      this.setupLoggers(this.getPartitionName());
      
      this.replicantManager.createService();
      
      if (this.distributedState instanceof ManagedDistributedState)
      {
         ((ManagedDistributedState) this.distributedState).createService();
      }
      
      // Create the asynchronous handler for view changes
      this.asynchHandler = new AsynchEventHandler(this, "AsynchViewChangeHandler");      

      // Add a well-known MC alias that other beans can depend on
      addCanonicalAlias();
      
      this.log.debug("done initializing partition");
   }
   
   protected void startService() throws Exception
   {
      this.logHistory ("Starting partition");
      
      // Have the handler get the cache
      this.cacheHandler.acquireCache();
      this.channelFactory = this.cacheHandler.getCacheChannelFactory();
      this.stackName = this.cacheHandler.getChannelStackName();
      
      if (this.channel == null || !this.channel.isOpen())
      {
         this.log.debug("Creating Channel for partition " + this.getPartitionName() +
               " using stack " + this.getChannelStackName());
   
         this.channel = this.createChannel();
         
         this.channel.setOpt(Channel.AUTO_RECONNECT, Boolean.TRUE);
         this.channel.setOpt(Channel.AUTO_GETSTATE, Boolean.TRUE);
      }
      
      this.log.info("Initializing partition " + this.getPartitionName());
      this.logHistory ("Initializing partition " + this.getPartitionName());
      
      this.dispatcher = new RpcHandler(this.channel, null, null, new Object(), false);
      
      // Subscribe to events generated by the channel
      this.log.debug("setMembershipListener");
      this.dispatcher.setMembershipListener(this);
      this.log.debug("setMessageListener");
      this.dispatcher.setMessageListener(this.messageListener);
      this.dispatcher.setRequestMarshaller(new RequestMarshallerImpl());
      this.dispatcher.setResponseMarshaller(new ResponseMarshallerImpl());
      
      // Clear any old connectException
      this.connectException = null;
      CountDownLatch connectLatch = new CountDownLatch(1);
      
      if (this.threadPool == null)
      {
         this.channel.connect(this.getPartitionName());
         connectLatch.countDown();
      }
      else
      {
         // Do the channel connect in another thread while this
         // thread starts the cache and does that channel connect
         ChannelConnectTask task = new ChannelConnectTask(connectLatch);
         this.threadPool.run(task);
      }
      
      this.cacheHandler.startCache();
      
      try
      {
         // This will block waiting for any async channel connect above
         connectLatch.await();
         
         if (this.connectException != null)
         {
            throw this.connectException;
         }
         
         this.log.debug("Get current members");
         this.waitForView();
         
         // get current JG group properties
         this.log.debug("get nodeName");
         this.localJGAddress = this.channel.getLocalAddress();
         this.me = new ClusterNodeImpl((IpAddress) this.localJGAddress);
         this.nodeName = this.me.getName();

         this.verifyNodeIsUnique();

         this.fetchState();
         
         this.replicantManager.startService();
         
         if (this.distributedState instanceof ManagedDistributedState)
         {
            ((ManagedDistributedState) this.distributedState).startService();
         }
         
         // Start the asynch listener handler thread
         this.asynchHandler.start();
         
         // Register with the service locator
         HAPartitionLocator.getHAPartitionLocator().registerHAPartition(this);
         
         // Bind ourself in the public JNDI space if configured to do so
         if (this.bindIntoJndi)
         {
            Context ctx = new InitialContext();
            this.bind(HAPartitionLocator.getStandardJndiBinding(this.getPartitionName()),
                      this, ClusterPartition.class, ctx);
            this.log.debug("Bound in JNDI under /HAPartition/" + this.getPartitionName());
         }
      }
      catch (Throwable t)
      {
         this.log.debug("Caught exception after channel connected; closing channel -- " + t.getLocalizedMessage());
         this.channel.close();
         this.channel = null;
         throw (t instanceof Exception) ? (Exception) t : new RuntimeException(t);
      }
      
   }

   protected void stopService() throws Exception
   {
      this.logHistory ("Stopping partition");
      this.log.info("Stopping partition " + this.getPartitionName());

      try
      {
         this.asynchHandler.stop();
      }
      catch( Exception e)
      {
         this.log.warn("Failed to stop asynchHandler", e);
      }
      
      if (this.distributedState instanceof ManagedDistributedState)
      {
         ((ManagedDistributedState) this.distributedState).stopService();
      }

      this.replicantManager.stopService();
      
      try
      {
         this.cacheHandler.releaseCache();
      }
      catch (Exception e)
      {
         this.log.error("cache release failed", e);
      }
      
//    NR 200505 : [JBCLUSTER-38] replace channel.close() by a disconnect and
//    add the destroyPartition() step
      try
      {
         if (this.channel != null && this.channel.isConnected())
         {
            this.channel.disconnect();
         }
      }
      catch (Exception e)
      {
         this.log.error("channel disconnection failed", e);
      }

      if (this.bindIntoJndi)
      {
         String boundName = HAPartitionLocator.getStandardJndiBinding(this.getPartitionName());
         InitialContext ctx = null;
         try
         {
            // the following statement fails when the server is being shut down (07/19/2007)
            ctx = new InitialContext();
            ctx.unbind(boundName);
         }
         catch (Exception e) {
            this.log.error("partition unbind operation failed", e);
         }
         finally
         {
            if (ctx != null)
            {
               ctx.close();
            }
         }
         NonSerializableFactory.unbind(boundName);
      }
      
      HAPartitionLocator.getHAPartitionLocator().deregisterHAPartition(this);

      this.log.info("Partition " + this.getPartitionName() + " stopped.");
   }
   
   protected void destroyService()  throws Exception
   {
      this.log.debug("Destroying HAPartition: " + this.getPartitionName());
      
      removeCanonicalAlias();
      
      if (this.distributedState instanceof ManagedDistributedState)
      {
         ((ManagedDistributedState) this.distributedState).destroyService();
      }

      this.replicantManager.destroyService();
      
//      unregisterDRM();

      try
      {
         if (this.channel != null && this.channel.isOpen())
         {
            this.channel.close();
         }
      }
      catch (Exception e)
      {
         this.log.error("Closing channel failed", e);
      }

      this.log.info("Partition " + this.getPartitionName() + " destroyed.");
   }

   /**
    * Adds an alias to our controller context -- the concatenation of
    * {@link #getAliasPrefix()} and {@link #getPartitionName()}.
    * This mechanism allows Ejb2HAPartitionDependencyDeployer to add
    * dependencies to deployments based on the partition name specified in
    * their metadata, without needing to know the bean name of this partition.
    */
   private void addCanonicalAlias()
   {
      if (kernelControllerContext != null)
      {
         KernelController kc = (KernelController) kernelControllerContext.getController();
         String aliasName = getHaPartitionDependencyCreator().getHAPartitionDependencyName(this.partitionName);
         try
         {
            kc.addAlias(aliasName, kernelControllerContext.getName());
         }
         catch (Throwable t)
         {
            log.error("Failed adding alias " + aliasName + " to context " + kernelControllerContext.getName(), t);
         }
      }
   }

   /**
    * Removes the alias created in {@link #addCanonicalAlias()}
    */
   private void removeCanonicalAlias()
   {
      if (kernelControllerContext != null)
      {
         KernelController kc = (KernelController) kernelControllerContext.getController();
         String aliasName = getHaPartitionDependencyCreator().getHAPartitionDependencyName(this.partitionName);
         Set<Object> aliases = kernelControllerContext.getAliases();
         if (aliases != null && aliases.contains(aliasName))
         {
            try
            {
               kc.removeAlias(aliasName);
            }
            catch (Throwable t)
            {
               log.error("Failed removing alias " + aliasName + " from context " + kernelControllerContext.getName(), t);
            }
         }
      }
   }
   
   // ---------------------------------------------------------- State Transfer


   protected void fetchState() throws Exception
   {
      this.log.info("Fetching serviceState (will wait for " + this.getStateTransferTimeout() +
            " milliseconds):");
      long start, stop;
      this.isStateSet = false;
      start = System.currentTimeMillis();
      boolean rc = this.channel.getState(null, this.getStateTransferTimeout());
      if (rc)
      {
         synchronized (this.channelLock)
         {
            while (!this.isStateSet)
            {
               if (this.setStateException != null)
               {
                  throw this.setStateException;
               }

               try
               {
                  this.channelLock.wait();
               }
               catch (InterruptedException iex)
               {
               }
            }
         }
         stop = System.currentTimeMillis();
         this.log.info("serviceState was retrieved successfully (in " + (stop - start) + " milliseconds)");
      }
      else
      {
         // No one provided us with serviceState.
         // We need to find out if we are the coordinator, so we must
         // block until viewAccepted() is called at least once

         synchronized (this.members)
         {
            while (this.members.size() == 0)
            {
               this.log.debug("waiting on viewAccepted()");
               try
               {
                  this.members.wait();
               }
               catch (InterruptedException iex)
               {
               }
            }
         }

         if (this.isCurrentNodeCoordinator())
         {
            this.log.info("State could not be retrieved (we are the first member in group)");
         }
         else
         {
            throw new IllegalStateException("Initial serviceState transfer failed: " +
               "Channel.getState() returned false");
         }
      }
   }

   private void getStateInternal(OutputStream stream) throws IOException
   {
      MarshalledValueOutputStream mvos = null; // don't create until we know we need it
      
      for (Map.Entry<String, HAPartitionStateTransfer> entry: this.stateHandlers.entrySet())
      {
         HAPartitionStateTransfer subscriber = entry.getValue();
         this.log.debug("getState for " + entry.getKey());
         Object state = subscriber.getCurrentState();
         if (state != null)
         {
            if (mvos == null)
            {
               // This is our first write, so need to write the header first
               stream.write(SERIALIZABLE_VALUE);
               
               mvos = new MarshalledValueOutputStream(stream);
            }
            
            mvos.writeObject(entry.getKey());
            mvos.writeObject(state);
         }
      }
      
      if (mvos == null)
      {
         // We never wrote any serviceState, so write the NULL header
         stream.write(NULL_VALUE);
      }
      else
      {
         mvos.writeObject(new StateStreamEnd());
         mvos.flush();
         mvos.close();
      }
      
   }
   
   private void setStateInternal(InputStream stream) throws IOException, ClassNotFoundException
   {
      byte type = (byte) stream.read();
      
      if (type == EOF_VALUE)
      {
         this.log.debug("serviceState stream is empty");
         return;
      }
      else if (type == NULL_VALUE)
      {
         this.log.debug("serviceState is null");
         return;
      }
      
      long used_mem_before, used_mem_after;
      Runtime rt=Runtime.getRuntime();
      used_mem_before=rt.totalMemory() - rt.freeMemory();
      
      MarshalledValueInputStream mvis = new MarshalledValueInputStream(stream);
      
      while (true)
      {
         Object obj = mvis.readObject();
         if (obj instanceof StateStreamEnd)
         {
            break;
         }
         
         String key = (String) obj;
         this.log.debug("setState for " + key);
         Object someState = mvis.readObject();
         HAPartitionStateTransfer subscriber = this.stateHandlers.get(key);
         if (subscriber != null)
         {
            try
            {
               subscriber.setCurrentState((Serializable)someState);
            }
            catch (Exception e)
            {
               // Don't let issues with one subscriber affect others
               // unless it is DRM, which is really an internal function
               // of the HAPartition
               // FIXME remove this once DRM is JBC-based
               if (DistributedReplicantManagerImpl.SERVICE_NAME.equals(key))
               {
                  if (e instanceof RuntimeException)
                  {
                     throw (RuntimeException) e;
                  }

                  throw new RuntimeException(e);
               }

               this.log.error("Caught exception setting serviceState to " + subscriber, e);
            }
         }
         else
         {
            this.log.debug("There is no stateHandler for: " + key);
         }
      }
      
      try
      {
         stream.close();
      }
      catch(Exception e)
      {
         this.log.error("Caught exception closing serviceState stream", e);
      }

      used_mem_after=rt.totalMemory() - rt.freeMemory();
      this.log.debug("received serviceState; expanded memory by " +
            (used_mem_after - used_mem_before) + " bytes (used memory before: " + used_mem_before +
            ", used memory after: " + used_mem_after + ")");
   }

   private void recordSetStateFailure(Throwable t)
   {
      this.log.error("failed setting serviceState", t);
      if (t instanceof Exception)
      {
         this.setStateException = (Exception) t;
      }
      else
      {
         this.setStateException = new Exception(t);
      }
   }

   private void notifyChannelLock()
   {
      synchronized (this.channelLock)
      {
         this.channelLock.notifyAll();
      }
   }
   
   // org.jgroups.MembershipListener implementation ----------------------------------------------
   
   public void suspect(org.jgroups.Address suspected_mbr)
   {
      this.logHistory ("Node suspected: " + (suspected_mbr==null?"null":suspected_mbr.toString()));
      if (this.isCurrentNodeCoordinator ())
      {
         this.clusterLifeCycleLog.info ("Suspected member: " + suspected_mbr);
      }
      else
      {
         this.log.info("Suspected member: " + suspected_mbr);
      }
   }

   public void block()
   {
       this.flushBlockGate.close();
       this.log.debug("Block processed at " + this.me);
   }
   
   public void unblock()
   {
       this.flushBlockGate.open();
       this.log.debug("Unblock processed at " + this.me);
   }
   
   /** Notification of a cluster view change. This is done from the JG protocol
    * handlder thread and we must be careful to not unduly block this thread.
    * Because of this there are two types of listeners, synchronous and
    * asynchronous. The synchronous listeners are messaged with the view change
    * event using the calling thread while the asynchronous listeners are
    * messaged using a seperate thread.
    *
    * @param newView
    */
   public void viewAccepted(View newView)
   {
      try
      {
         // we update the view id
         this.currentViewId = newView.getVid().getId();

         // Keep a list of other members only for "exclude-self" RPC calls
         this.jgotherMembers = (Vector<Address>)newView.getMembers().clone();
         this.jgotherMembers.remove (this.channel.getLocalAddress());
         this.otherMembers = this.translateAddresses (this.jgotherMembers); // TRANSLATE!
         Vector<ClusterNode> translatedNewView = this.translateAddresses ((Vector<Address>)newView.getMembers().clone());
         this.logHistory ("New view: " + translatedNewView + " with viewId: " + this.currentViewId +
                     " (old view: " + this.members + " )");


         // Save the previous view and make a copy of the new view
         Vector<ClusterNode> oldMembers = this.members;

         Vector<Address> newjgMembers = (Vector<Address>)newView.getMembers().clone();
         Vector<ClusterNode> newMembers = this.translateAddresses(newjgMembers); // TRANSLATE
         this.members = newMembers;
         this.jgmembers = newjgMembers;
         
         if (oldMembers == null)
         {
            // Initial viewAccepted
            this.log.debug("ViewAccepted: initial members set for partition " + this.getPartitionName() + ": " +
                     this.currentViewId + " (" + this.members + ")");
            
            this.log.info("Number of cluster members: " + this.members.size());
            for(int m = 0; m > this.members.size(); m ++)
            {
               Object node = this.members.get(m);
               this.log.debug(node);
            }
            this.log.info ("Other members: " + this.otherMembers.size ());
            
            // Wake up the deployer thread blocking in waitForView
            this.notifyChannelLock();
            return;
         }
         
         int difference = newMembers.size() - oldMembers.size();
         
         if (this.isCurrentNodeCoordinator ())
         {
            this.clusterLifeCycleLog.info ("New cluster view for partition " + this.getPartitionName() + " (id: " +
                                      this.currentViewId + ", delta: " + difference + ") : " + this.members);
         }
         else
         {
            this.log.info("New cluster view for partition " + this.getPartitionName() + ": " +
                     this.currentViewId + " (" + this.members + " delta: " + difference + ")");
         }

         // Build a ViewChangeEvent for the asynch listeners
         ViewChangeEvent event = new ViewChangeEvent();
         event.viewId = this.currentViewId;
         event.allMembers = translatedNewView;
         event.deadMembers = this.getDeadMembers(oldMembers, event.allMembers);
         event.newMembers = this.getNewMembers(oldMembers, event.allMembers);
         event.originatingGroups = null;
         // if the new view occurs because of a merge, we first inform listeners of the merge
         if(newView instanceof MergeView)
         {
            MergeView mergeView = (MergeView) newView;
            event.originatingGroups = mergeView.getSubgroups();
         }

         this.log.debug("membership changed from " + oldMembers.size() + " to " + event.allMembers.size());
         // Put the view change to the asynch queue
         this.asynchHandler.queueEvent(event);

         // Broadcast the new view to the synchronous view change listeners
         if (this.allowSyncListeners)
         {
            this.notifyListeners(this.synchListeners, event.viewId, event.allMembers,
                  event.deadMembers, event.newMembers, event.originatingGroups);
         }
      }
      catch (Exception ex)
      {
         this.log.error("ViewAccepted failed", ex);
      }
   }

   private void waitForView() throws Exception
   {
      synchronized (this.channelLock)
      {
         if (this.members == null)
         {
            if (this.connectException != null)
            {
               throw this.connectException;
            }
            
            try
            {
               this.channelLock.wait(this.getMethodCallTimeout());
            }
            catch (InterruptedException iex)
            {
            }
            
            if (this.connectException != null)
            {
               throw this.connectException;
            }
            
            if (this.members == null)
            {
               throw new IllegalStateException("No view received from Channel");
            }
         }
      }
   }

   // HAPartition implementation ----------------------------------------------
   
   @ManagementProperty(use={ViewUse.STATISTIC}, description="The identifier for this node in cluster topology views")
   public String getNodeName()
   {
      return this.nodeName;
   }
   
   @ManagementProperty(use={ViewUse.CONFIGURATION}, description="The partition's name")
   @ManagementObjectID(type="HAPartition")
   public String getPartitionName()
   {
      return this.partitionName;
   }

   public void setPartitionName(String newName)
   {
      this.partitionName = newName;
   }
   
   public DistributedReplicantManager getDistributedReplicantManager()
   {
      return this.replicantManager;
   }
   
   @SuppressWarnings("deprecation")
   public org.jboss.ha.framework.interfaces.DistributedState getDistributedStateService()
   {
      return this.distributedState;
   }

   @ManagementProperty(use={ViewUse.STATISTIC}, description="Identifier for the current topology view")
   public long getCurrentViewId()
   {
      return this.currentViewId;
   }
   
   @ManagementProperty(use={ViewUse.STATISTIC}, description="The current cluster topology view")
   public Vector<String> getCurrentView()
   {
      Vector<String> result = new Vector<String>(this.members.size());
      for (ClusterNode member: this.members)
      {
         result.add(member.getName());
      }
      return result;
   }

   public ClusterNode[] getClusterNodes ()
   {
      synchronized (this.members)
      {
         return this.members.toArray(new ClusterNode[this.members.size()]);
      }
   }

   public ClusterNode getClusterNode ()
   {
      return this.me;
   }

   @ManagementProperty(use={ViewUse.STATISTIC}, description="Whether this node is acting as the group coordinator for the partition")
   public boolean isCurrentNodeCoordinator ()
   {
      if(this.members == null || this.members.size() == 0 || this.me == null)
      {
         return false;
      }
     return this.members.elementAt (0).equals (this.me);
   }

   // ***************************
   // ***************************
   // RPC multicast communication
   // ***************************
   // ***************************
   
   public void registerRPCHandler(String objName, Object subscriber)
   {
      this.rpcHandlers.put(objName, subscriber);
   }
   
   public void registerRPCHandler(String objName, Object subscriber, ClassLoader classloader)
   {
      this.registerRPCHandler(objName, subscriber);
      this.clmap.put(objName, new WeakReference<ClassLoader>(classloader));
   }
   
   public void unregisterRPCHandler(String objName, Object subscriber)
   {
      this.rpcHandlers.remove(objName);
      this.clmap.remove(objName);
   }

   /**
    * This function is an abstraction of RpcDispatcher.
    */
   @SuppressWarnings("unchecked")
   public ArrayList callMethodOnCluster(String objName, String methodName,
      Object[] args, Class[] types, boolean excludeSelf) throws Exception
   {
      return this.callMethodOnCluster(objName, methodName, args, types, excludeSelf, null);
   }
   
   @SuppressWarnings("unchecked")
   public ArrayList callMethodOnCluster(String objName, String methodName, 
      Object[] args, Class[] types, boolean excludeSelf, ResponseFilter filter) throws Exception
   {
      return this.callMethodOnCluster(objName, methodName, args, types, excludeSelf, this.getMethodCallTimeout(), filter);
   }   

   @SuppressWarnings("unchecked")
   public ArrayList callMethodOnCluster(String objName, String methodName,
       Object[] args, Class[] types, boolean excludeSelf, long methodTimeout, ResponseFilter filter) throws Exception
   {
      RspList rsp = null;
      boolean trace = this.log.isTraceEnabled();

      MethodCall m = new MethodCall(objName + "." + methodName, args, types);
      RspFilterAdapter rspFilter = filter == null ? null : new RspFilterAdapter(filter);
      
      if(this.channel.flushSupported())
      {
     	 this.flushBlockGate.await(this.getStateTransferTimeout());
      }
      if (excludeSelf)
      {
         if( trace )
         {
            this.log.trace("callMethodOnCluster(true), objName="+objName
               +", methodName="+methodName+", members="+this.jgotherMembers);
         }
         rsp = this.dispatcher.callRemoteMethods(this.jgotherMembers, m, GroupRequest.GET_ALL, methodTimeout, false, false, rspFilter);
      }
      else
      {
         if( trace )
         {
            this.log.trace("callMethodOnCluster(false), objName="+objName
               +", methodName="+methodName+", members="+this.members);
         }
         rsp = this.dispatcher.callRemoteMethods(null, m, GroupRequest.GET_ALL, methodTimeout, false, false, rspFilter);
      }

      return this.processResponseList(rsp, trace);
    }

   /**
    * Calls method on Cluster coordinator node only.  The cluster coordinator node is the first node to join the
    * cluster.
    * and is replaced
    * @param objName
    * @param methodName
    * @param args
    * @param types
    * @param excludeSelf
    * @return an array of responses from remote nodes
    * @throws Exception
    */
   @SuppressWarnings("unchecked")
   public ArrayList callMethodOnCoordinatorNode(String objName, String methodName,
          Object[] args, Class[] types,boolean excludeSelf) throws Exception
   {
      return this.callMethodOnCoordinatorNode(objName,methodName,args,types,excludeSelf, this.getMethodCallTimeout());
   }

   /**
    * Calls method on Cluster coordinator node only.  The cluster coordinator node is the first node to join the
    * cluster.
    * and is replaced
    * @param objName
    * @param methodName
    * @param args
    * @param types
    * @param excludeSelf
    * @param methodTimeout
    * @return an array of responses from remote nodes
    * @throws Exception
    */
   @SuppressWarnings("unchecked")
   public ArrayList callMethodOnCoordinatorNode(String objName, String methodName,
          Object[] args, Class[] types,boolean excludeSelf, long methodTimeout) throws Exception
   {
      boolean trace = this.log.isTraceEnabled();

      MethodCall m = new MethodCall(objName + "." + methodName, args, types);
      
      if( trace )
      {
         this.log.trace("callMethodOnCoordinatorNode(false), objName="+objName
            +", methodName="+methodName);
      }

      // the first cluster view member is the coordinator
      Vector<Address> coordinatorOnly = new Vector<Address>();
      // If we are the coordinator, only call ourself if 'excludeSelf' is false
      if (false == this.isCurrentNodeCoordinator () ||
          false == excludeSelf)
      {
         coordinatorOnly.addElement(this.jgmembers.elementAt(0));
      }
      
      RspList rsp = this.dispatcher.callRemoteMethods(coordinatorOnly, m, GroupRequest.GET_ALL, methodTimeout);

      return this.processResponseList(rsp, trace);
   }

    /**
     * Calls method synchrounously on target node only.
     * @param serviceName Name of the target service name on which calls are de-multiplexed
     * @param methodName name of the Java method to be called on remote services
     * @param args array of Java Object representing the set of parameters to be
     * given to the remote method
     * @param types The types of the parameters
     * node of the partition or only on remote nodes
     * @param targetNode is the target of the call
     * @return the value returned by the target method
     * @throws Exception Throws if a communication exception occurs
     */
   @SuppressWarnings("unchecked")
   public Object callMethodOnNode(String serviceName, String methodName,
           Object[] args, Class[] types, long methodTimeout, ClusterNode targetNode) throws Throwable
    {
       if (!(targetNode instanceof ClusterNodeImpl))
      {
         throw new IllegalArgumentException("targetNode " + targetNode + " is not an instance of " +
                                          ClusterNodeImpl.class + " -- only targetNodes provided by this HAPartition should be used");
      }
       boolean trace = this.log.isTraceEnabled();
       
       MethodCall m = new MethodCall(serviceName + "." + methodName, args, types);

       if( trace )
       {
          this.log.trace("callMethodOnNode( objName="+serviceName
             +", methodName="+methodName);
       }
       Object rc = this.dispatcher.callRemoteMethod(((ClusterNodeImpl)targetNode).getOriginalJGAddress(), m, GroupRequest.GET_FIRST, methodTimeout);
       if (rc != null)
       {
          Object item = rc;
          if (item instanceof Rsp)
          {
             Rsp response = (Rsp) item;
             // Only include received responses
             boolean wasReceived = response.wasReceived();
             if( wasReceived == true )
             {
                item = response.getValue();
                if (!(item instanceof NoHandlerForRPC))
               {
                  rc = item;
               }
                }
                else if( trace )
               {
                  this.log.trace("Ignoring non-received response: "+response);
               }
             }
             else
             {
                if (!(item instanceof NoHandlerForRPC))
               {
                  rc = item;
               }
               else if( trace )
               {
                  this.log.trace("Ignoring NoHandlerForRPC");
               }
             }
          }
       return rc;
     }


   /**
     * Calls method on target node only.
     * @param serviceName Name of the target service name on which calls are de-multiplexed
     * @param methodName name of the Java method to be called on remote services
     * @param args array of Java Object representing the set of parameters to be
     * given to the remote method
     * @param types The types of the parameters
     * node of the partition or only on remote nodes
     * @param targetNode is the target of the call
     * @return none
     * @throws Exception Throws if a communication exception occurs
     */
   @SuppressWarnings("unchecked")
   public void callAsyncMethodOnNode(String serviceName, String methodName,
           Object[] args, Class[] types, long methodTimeout, ClusterNode targetNode) throws Throwable
   {
      if (!(targetNode instanceof ClusterNodeImpl))
      {
         throw new IllegalArgumentException("targetNode " + targetNode + " is not an instance of " +
                                         ClusterNodeImpl.class + " -- only targetNodes provided by this HAPartition should be used");
      }
       boolean trace = this.log.isTraceEnabled();

       MethodCall m = new MethodCall(serviceName + "." + methodName, args, types);

       if( trace )
       {
          this.log.trace("callAsyncMethodOnNode( objName="+serviceName
             +", methodName="+methodName);
       }
       this.dispatcher.callRemoteMethod(((ClusterNodeImpl)targetNode).getOriginalJGAddress(), m, GroupRequest.GET_NONE, methodTimeout);
   }

   private ArrayList<Object> processResponseList(RspList rsp, boolean trace)
   {
      ArrayList<Object> rtn = new ArrayList<Object>();
      if (rsp != null)
      {
         for (Object item : rsp.values())
         {
            if (item instanceof Rsp)
            {
               Rsp response = (Rsp) item;
               // Only include received responses
               boolean wasReceived = response.wasReceived();
               if( wasReceived == true )
               {
                  item = response.getValue();
                  if (!(item instanceof NoHandlerForRPC))
                  {
                     rtn.add(item);
                  }
               }
               else if( trace )
               {
                  this.log.trace("Ignoring non-received response: "+response);
               }
            }
            else
            {
               if (!(item instanceof NoHandlerForRPC))
               {
                  rtn.add(item);
               }
               else if( trace )
               {
                  this.log.trace("Ignoring NoHandlerForRPC");
               }
            }
         }
         
      }
      return rtn;
   }

   /**
    * This function is an abstraction of RpcDispatcher for asynchronous messages
    */
   @SuppressWarnings("unchecked")
   public void callAsynchMethodOnCluster(String objName, String methodName,
      Object[] args, Class[] types, boolean excludeSelf) throws Exception
   {
      boolean trace = this.log.isTraceEnabled();

      MethodCall m = new MethodCall(objName + "." + methodName, args, types);

      if(this.channel.flushSupported())
      {
     	 this.flushBlockGate.await(this.getStateTransferTimeout());
      }
      if (excludeSelf)
      {
         if( trace )
         {
            this.log.trace("callAsynchMethodOnCluster(true), objName="+objName
               +", methodName="+methodName+", members="+this.jgotherMembers);
         }
         this.dispatcher.callRemoteMethods(this.jgotherMembers, m, GroupRequest.GET_NONE, this.getMethodCallTimeout());
      }
      else
      {
         if( trace )
         {
            this.log.trace("callAsynchMethodOnCluster(false), objName="+objName
               +", methodName="+methodName+", members="+this.members);
         }
         this.dispatcher.callRemoteMethods(null, m, GroupRequest.GET_NONE, this.getMethodCallTimeout());
      }
   }
   
   // *************************
   // *************************
   // State transfer management
   // *************************
   // *************************
   
   public void subscribeToStateTransferEvents(String objectName, HAPartitionStateTransfer subscriber)
   {
      this.stateHandlers.put(objectName, subscriber);
   }
   
   public void unsubscribeFromStateTransferEvents(String objectName, HAPartitionStateTransfer subscriber)
   {
      this.stateHandlers.remove(objectName);
   }
   
   // *************************
   // *************************
   // Group Membership listeners
   // *************************
   // *************************
   
   public void registerMembershipListener(HAMembershipListener listener)
   {
      boolean isAsynch = (this.allowSyncListeners == false)
            || (listener instanceof AsynchHAMembershipListener)
            || (listener instanceof AsynchHAMembershipExtendedListener);
      if( isAsynch ) {
         synchronized(this.asynchListeners) {
            this.asynchListeners.add(listener);
         }
      }
      else  {
         synchronized(this.synchListeners) {
            this.synchListeners.add(listener);
         }
      }
   }
   
   public void unregisterMembershipListener(HAMembershipListener listener)
   {
      boolean isAsynch = (this.allowSyncListeners == false)
            || (listener instanceof AsynchHAMembershipListener)
            || (listener instanceof AsynchHAMembershipExtendedListener);
      if( isAsynch ) {
         synchronized(this.asynchListeners) {
            this.asynchListeners.remove(listener);
         }
      }
      else  {
         synchronized(this.synchListeners) {
            this.synchListeners.remove(listener);
         }
      }
   }
   
   @ManagementProperty(use={ViewUse.CONFIGURATION, ViewUse.RUNTIME}, 
         description="Whether to allow synchronous notifications of topology changes")
   public boolean getAllowSynchronousMembershipNotifications()
   {
      return this.allowSyncListeners;
   }

   /**
    * Sets whether this partition will synchronously notify any 
    * HAPartition.HAMembershipListener of membership changes using the  
    * calling thread from the underlying group communications layer
    * (e.g. JGroups).
    * 
    * @param allowSync  <code>true</code> if registered listeners that don't 
    *         implement <code>AsynchHAMembershipExtendedListener</code> or
    *         <code>AsynchHAMembershipListener</code> should be notified
    *         synchronously of membership changes; <code>false</code> if
    *         those listeners can be notified asynchronously.  Default
    *         is <code>false</code>.
    */
   public void setAllowSynchronousMembershipNotifications(boolean allowSync)
   {
      this.allowSyncListeners = allowSync;
   }
   
   // AsynchEventHandler.AsynchEventProcessor -----------------------

   public void processEvent(Object event)
   {
      ViewChangeEvent vce = (ViewChangeEvent) event;
      this.notifyListeners(this.asynchListeners, vce.viewId, vce.allMembers,
            vce.deadMembers, vce.newMembers, vce.originatingGroups);
      
   }
   
   
   // Public ------------------------------------------------------------------
   
   @SuppressWarnings("deprecation")
   public void setDistributedStateImpl(org.jboss.ha.framework.interfaces.DistributedState distributedState)
   {
      this.distributedState = distributedState;
   }   
   
   // Protected -----------------------------------------------------

   protected void verifyNodeIsUnique () throws IllegalStateException
   {
      ClusterNodeImpl matched = null;
      for (ClusterNode member : this.getClusterNodes())
      {
         if (member.equals(this.me))
         {
            if (matched == null)
            {
               // We of course are in the view, so we expect one match
               // Just track that we've had one
               matched = (ClusterNodeImpl) member;
            }
            else
            {
               // Two nodes in view match us; try to figure out which one isn't us
               ClusterNodeImpl other = matched;
               if (other.getOriginalJGAddress().equals(((ClusterNodeImpl)this.me).getOriginalJGAddress()))
               {
                  other = (ClusterNodeImpl) member;
               }
               throw new IllegalStateException("Found member " + other +
                     " in current view that duplicates us (" + this.me + "). This" +
                     " node cannot join partition until duplicate member has" +
                     " been removed");
            }
         }
      }
   }

   /**
    * Helper method that binds the partition in the JNDI tree.
    * @param jndiName Name under which the object must be bound
    * @param who Object to bind in JNDI
    * @param classType Class type under which should appear the bound object
    * @param ctx Naming context under which we bind the object
    * @throws Exception Thrown if a naming exception occurs during binding
    */
   protected void bind(String jndiName, Object who, Class<?> classType, Context ctx) throws Exception
   {
      // Ah ! This service isn't serializable, so we use a helper class
      //
      NonSerializableFactory.bind(jndiName, who);
      Name n = ctx.getNameParser("").parse(jndiName);
      while (n.size () > 1)
      {
         String ctxName = n.get (0);
         try
         {
            ctx = (Context)ctx.lookup (ctxName);
         }
         catch (NameNotFoundException e)
         {
            this.log.debug ("creating Subcontext " + ctxName);
            ctx = ctx.createSubcontext (ctxName);
         }
         n = n.getSuffix (1);
      }

      // The helper class NonSerializableFactory uses address type nns, we go on to
      // use the helper class to bind the service object in JNDI
      //
      StringRefAddr addr = new StringRefAddr("nns", jndiName);
      Reference ref = new Reference(classType.getName (), addr, NonSerializableFactory.class.getName (), null);
      ctx.rebind (n.get (0), ref);
   }
   
   /**
    * Helper method that returns a vector of dead members from two input vectors: new and old vectors of two views.
    * Dead members are old - new members.
    * @param oldMembers Vector of old members
    * @param newMembers Vector of new members
    * @return Vector of members that have died between the two views, can be empty.
    */
   protected Vector<ClusterNode> getDeadMembers(Vector<ClusterNode> oldMembers, Vector<ClusterNode> newMembers)
   {
      if(oldMembers == null)
      {
         oldMembers=new Vector<ClusterNode>();
      }
      if(newMembers == null)
      {
         newMembers=new Vector<ClusterNode>();
      }
      Vector<ClusterNode> dead=(Vector<ClusterNode>)oldMembers.clone();
      dead.removeAll(newMembers);
      this.log.debug("dead members: " + dead);
      return dead;
   }
   
   /**
    * Helper method that returns a vector of new members from two input vectors: new and old vectors of two views.
    * @param oldMembers Vector of old members
    * @param allMembers Vector of new members
    * @return Vector of members that have joined the partition between the two views
    */
   protected Vector<ClusterNode> getNewMembers(Vector<ClusterNode> oldMembers, Vector<ClusterNode> allMembers)
   {
      if(oldMembers == null)
      {
         oldMembers=new Vector<ClusterNode>();
      }
      if(allMembers == null)
      {
         allMembers=new Vector<ClusterNode>();
      }
      Vector<ClusterNode> newMembers=(Vector<ClusterNode>)allMembers.clone();
      newMembers.removeAll(oldMembers);
      return newMembers;
   }

   protected void notifyListeners(ArrayList<HAMembershipListener> theListeners, long viewID,
      Vector<ClusterNode> allMembers, Vector<ClusterNode> deadMembers, Vector<ClusterNode> newMembers,
      Vector<View> originatingGroups)
   {
      this.log.debug("Begin notifyListeners, viewID: "+viewID);
      synchronized(theListeners)
      {
         // JBAS-3619 -- don't hold synch lock while notifying
         theListeners = (ArrayList<HAMembershipListener>) theListeners.clone();
      }
      
      for (int i = 0; i < theListeners.size(); i++)
      {
         HAMembershipListener aListener = null;
         try
         {
            aListener = theListeners.get(i);
            if(originatingGroups != null && (aListener instanceof HAMembershipExtendedListener))
            {
               HAMembershipExtendedListener exListener = (HAMembershipExtendedListener) aListener;
               exListener.membershipChangedDuringMerge (deadMembers, newMembers,
                  allMembers, originatingGroups);
            }
            else
            {
               aListener.membershipChanged(deadMembers, newMembers, allMembers);
            }
         }
         catch (Throwable e)
         {
            // a problem in a listener should not prevent other members to receive the new view
            this.log.warn("HAMembershipListener callback failure: "+aListener, e);
         }
      }
      
      this.log.debug("End notifyListeners, viewID: "+viewID);
   }
   
   /*
    * Allows caller to specify whether the partition instance should be bound into JNDI.  Default value is true.
    * This method must be called before the partition is started as the binding occurs during startup.
    * 
    * @param bind  Whether to bind the partition into JNDI.
    */
   public void setBindIntoJndi(boolean bind)
   {
       this.bindIntoJndi = bind;
   }
   
   /*
    * Allows caller to determine whether the partition instance has been bound into JNDI.
    * 
    * @return true if the partition has been bound into JNDI.
    */
   @ManagementProperty(description="Whether this HAPartition should bind itself into JNDI")
   public boolean getBindIntoJndi()
   {
       return this.bindIntoJndi;
   }

   public ThreadPool getThreadPool()
   {
      return this.threadPool;
   }

   public void setThreadPool(ThreadPool threadPool)
   {
      this.threadPool = threadPool;
   }

   public synchronized HAPartitionDependencyCreator getHaPartitionDependencyCreator()
   {
      if (haPartitionDependencyCreator == null)
      {
         haPartitionDependencyCreator = DefaultHAPartitionDependencyCreator.INSTANCE;
      }
      return haPartitionDependencyCreator;
   }

   public synchronized void setHaPartitionDependencyCreator(HAPartitionDependencyCreator haPartitionDependencyCreator)
   {
      this.haPartitionDependencyCreator = haPartitionDependencyCreator;
   }

   protected Vector<ClusterNode> translateAddresses(Vector<Address> addresses)
   {
      if (addresses == null)
      {
         return null;
      }

      Vector<ClusterNode> result = new Vector<ClusterNode>(addresses.size());
      for (Address address: addresses)
      {
         result.add(new ClusterNodeImpl((IpAddress) address));
      }

      return result;
   }

   public void logHistory (String message)
   {
      try
      {
         this.history.add(new SimpleDateFormat().format (new Date()) + " : " + message);
      }
      catch (Exception ignored){}
   }

   // --------------------------------------------------- ClusterPartitionMBean
   
   @ManagementOperation(description="Gets a listing of significant events since " +
   		                            "the instantiation of this service",
                        impact=Impact.ReadOnly)
   public String showHistory()
   {
      StringBuffer buff = new StringBuffer();
      Vector<String> data = new Vector<String>(this.history);
      for (java.util.Iterator<String> row = data.iterator(); row.hasNext();)
      {
         String info = row.next();
         buff.append(info).append("\n");
      }
      return buff.toString();
   }

   @ManagementOperation(description="Gets an XML format listing of significant events since " +
                                    "the instantiation of this service",
                        impact=Impact.ReadOnly)
   public String showHistoryAsXML()
   {
      StringBuffer buff = new StringBuffer();
      buff.append("<events>\n");
      Vector<String> data = new Vector<String>(this.history);
      for (java.util.Iterator<String> row = data.iterator(); row.hasNext();)
      {
         buff.append("   <event>\n      ");
         String info = row.next();
         buff.append(info);
         buff.append("\n   </event>\n");
      }
      buff.append("</events>\n");
      return buff.toString();
   }

   /**
    * Deprecated; always returns <code>false</code>.
    * 
    * @return <code>false</code>
    * 
    * @deprecated will be removed in next major release
    */
   @Deprecated
   public boolean getDeadlockDetection()
   {
      return false;
   }

   /**
    * Deprecated; logs a WARN message if invoked.
    * 
    * @param doIt ignored
    * 
    * @deprecated will be removed in next major release
    */
   @Deprecated
   public void setDeadlockDetection(boolean doit)
   {
      log.warn("Property deadlockDetection has been deprecated; setting it has no effect");
   }

   @Deprecated
   public HAPartition getHAPartition()
   {
      return this;
   }

   @ManagementProperty(use={ViewUse.STATISTIC}, description="The release version of JGroups")
   public String getJGroupsVersion()
   {
      return Version.description + "( " + Version.cvs + ")";
   }
   
//   @ManagementProperty(name="distributedReplicantManager", use={ViewUse.STATISTIC}, description="The DistributedReplicantManager")
//   @ManagementObjectRef(type="DistributedReplicantManager")
//   public String getDRMName()
//   {
//      return getPartitionName();
//   }
   
   public DistributedReplicantManagerImpl getDistributedReplicantManagerImpl()
   {
      return this.replicantManager;
   }

   public ChannelFactory getChannelFactory()
   {
      return this.channelFactory;
   }

   public HAPartitionCacheHandler getCacheHandler()
   {
      return this.cacheHandler;
   }

   public void setCacheHandler(HAPartitionCacheHandler cacheHandler)
   {
      this.cacheHandler = cacheHandler;
      this.cacheConfigName = cacheHandler == null ? null : cacheHandler.getCacheConfigName();
   }

   @ManagementProperty(use={ViewUse.STATISTIC}, 
         description="Name of the CacheManager configuration used for deriving the JGroups channel stack name")
   public String getCacheConfigName()
   {
      return this.cacheConfigName;
   }
   
   @ManagementProperty(use={ViewUse.STATISTIC}, description="Name of the JGroups protocol stack configuration")
   public String getChannelStackName()
   {
      return this.stackName;
   }

   public InetAddress getNodeAddress()
   {
      return this.nodeAddress;
   }

   public void setNodeAddress(InetAddress address)
   {
      this.nodeAddress = address;
   }

   @ManagementProperty(description="Time (in ms) to allow for state transfer to finish")
   public long getStateTransferTimeout() {
      return this.state_transfer_timeout;
   }

   public void setStateTransferTimeout(long timeout)
   {
      this.state_transfer_timeout = timeout;
   }

   @ManagementProperty(use={ViewUse.CONFIGURATION, ViewUse.RUNTIME},
         description="Time (in ms) to allow for group RPCs to return")
   public long getMethodCallTimeout() {
      return this.method_call_timeout;
   }

   public void setMethodCallTimeout(long timeout)
   {
      this.method_call_timeout = timeout;
   }
   
   // KernelControllerContextAware --------------------------------------------
   
   @Override
   public void setKernelControllerContext(KernelControllerContext controllerContext) throws Exception
   {
      super.setKernelControllerContext(controllerContext);
      this.kernelControllerContext = controllerContext;
   }

   @Override
   public void unsetKernelControllerContext(KernelControllerContext controllerContext) throws Exception
   {
      super.unsetKernelControllerContext(controllerContext);
      this.kernelControllerContext = null;
   }
   
   // ManagedObject interface for DRM ---------------------------------------
   
   @ManagementOperation(description="List all known DistributedReplicantManager keys and the nodes that have registered bindings",
         impact=Impact.ReadOnly)
   public String listDRMContent() throws Exception
   {
      return this.replicantManager == null ? null : this.replicantManager.listContent();
   }
   
   @ManagementOperation(description="List in XML format all known DistributedReplicantManager keys and the nodes that have registered bindings",
         impact=Impact.ReadOnly)
   public String listDRMContentAsXml() throws Exception
   {
      return this.replicantManager == null ? null : this.replicantManager.listXmlContent();
   }
   
   @ManagementOperation(description="Returns the names of the nodes that have registered objects with the DistributedReplicantManager under the given key",
                        impact=Impact.ReadOnly,
                        params={@ManagementParameter(name="key",
                                                     description="The name of the service")})
   @SuppressWarnings("deprecation")
   public List<String> lookupDRMNodeNames(String key)
   {
      return this.replicantManager == null ? null : this.replicantManager.lookupReplicantsNodeNames(key);
   }
   
   @ManagementOperation(description="Returns a hash of the list of nodes that " +
                                    "have registered an object with the DistributedReplicantManager under  the given key",
                        impact=Impact.ReadOnly,
                        params={@ManagementParameter(name="key",
                                                     description="The name of the service")})
   public int getDRMServiceViewId(String key)
   {
      return this.replicantManager == null ? null : this.replicantManager.getReplicantsViewId(key);
   }
   
   @ManagementOperation(description="Returns whether the DistributedReplicantManager considers this node to be the master for the given service",
         impact=Impact.ReadOnly,
         params={@ManagementParameter(name="key", description="The name of the service")})
   public boolean isDRMMasterForService(String key)
   {
      return this.replicantManager == null ? null : this.replicantManager.isMasterReplica(key);
   }
   
   @ManagementOperation(description="Get a collection of the names of all keys for which the DistributedReplicantManager has bindings",
         impact=Impact.ReadOnly)
   public Collection<String> getDRMServiceNames()
   {
      return this.replicantManager == null ? null : this.replicantManager.getAllServices();
   }

   // Protected --------------------------------------------------------------
      
   /**
    * Creates an object from a byte buffer
    */
   protected Object objectFromByteBufferInternal (byte[] buffer) throws Exception
   {
      if(buffer == null)
      {
         return null;
      }

      ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
      MarshalledValueInputStream mvis = new MarshalledValueInputStream(bais);
      return mvis.readObject();
   }
   
   /**
    * Serializes an object into a byte buffer.
    * The object has to implement interface Serializable or Externalizable
    */
   protected byte[] objectToByteBufferInternal (Object obj) throws Exception
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      MarshalledValueOutputStream mvos = new MarshalledValueOutputStream(baos);
      mvos.writeObject(obj);
      mvos.flush();
      return baos.toByteArray();
   }
   
   /**
    * Creates a response object from a byte buffer - optimized for response marshalling
    */
   protected Object objectFromByteBufferResponseInternal (byte[] buffer) throws Exception
   {
      if(buffer == null)
      {
         return null;
      }

      if (buffer[0] == NULL_VALUE)
      {
         return null;
      }

      ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
      // read past the null/serializable byte
      bais.read();
      MarshalledValueInputStream mvis = new MarshalledValueInputStream(bais);
      return mvis.readObject();
   }
   
   /**
    * Serializes a response object into a byte buffer, optimized for response marshalling.
    * The object has to implement interface Serializable or Externalizable
    */
   protected byte[] objectToByteBufferResponseInternal (Object obj) throws Exception
   {
      if (obj == null)
      {
         return new byte[]{NULL_VALUE};
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      // write a marker to stream to distinguish from null value stream
      baos.write(SERIALIZABLE_VALUE);
      MarshalledValueOutputStream mvos = new MarshalledValueOutputStream(baos);
      mvos.writeObject(obj);
      mvos.flush();
      return baos.toByteArray();
   }

//   @Override
//   public void postRegister(Boolean registrationDone)
//   {
//      super.postRegister(registrationDone);
//      registerDRM();
//   }
   
   
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------

   private class MessageListenerAdapter
         implements ExtendedMessageListener
   {
      
      public void getState(OutputStream stream)
      {
         ClusterPartition.this.logHistory ("getState called on partition");
         
         ClusterPartition.this.log.debug("getState called.");
         try
         {
            ClusterPartition.this.getStateInternal(stream);
         }
         catch (Exception ex)
         {
            ClusterPartition.this.log.error("getState failed", ex);
         }
         
      }
      
      public void getState(String state_id, OutputStream ostream)
      {
         throw new UnsupportedOperationException("Not implemented; see http://jira.jboss.com/jira/browse/JBAS-3594");
      }

      public byte[] getState(String state_id)
      {
         throw new UnsupportedOperationException("Not implemented; see http://jira.jboss.com/jira/browse/JBAS-3594");
      }
      
      public void setState(InputStream stream)
      {
         ClusterPartition.this.logHistory ("setState called on partition");
         try
         {
            if (stream == null)
            {
               ClusterPartition.this.log.debug("transferred serviceState is null (may be first member in cluster)");
            }
            else
            {
               ClusterPartition.this.setStateInternal(stream);
            }
            
            ClusterPartition.this.isStateSet = true;
         }
         catch (Throwable t)
         {
            ClusterPartition.this.recordSetStateFailure(t);
         }
         finally
         {
            // Notify waiting thread that serviceState has been set.
            ClusterPartition.this.notifyChannelLock();
         }
      }

      public byte[] getState()
      {
         ClusterPartition.this.logHistory ("getState called on partition");
         
         ClusterPartition.this.log.debug("getState called.");
         try
         {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            ClusterPartition.this.getStateInternal(baos);
            return baos.toByteArray();
         }
         catch (Exception ex)
         {
            ClusterPartition.this.log.error("getState failed", ex);
         }
         return null; // This will cause the receiver to get a "false" on the channel.getState() call
      }

      public void setState(String state_id, byte[] state)
      {
         throw new UnsupportedOperationException("Not implemented; see http://jira.jboss.com/jira/browse/JBAS-3594");
      }

      public void setState(String state_id, InputStream istream)
      {
         throw new UnsupportedOperationException("Not implemented; see http://jira.jboss.com/jira/browse/JBAS-3594");
      }

      public void receive(org.jgroups.Message msg)
      { /* complete */}
      
      public void setState(byte[] obj)
      {
         ClusterPartition.this.logHistory ("setState called on partition");
         try
         {
            if (obj == null)
            {
               ClusterPartition.this.log.debug("transferred serviceState is null (may be first member in cluster)");
            }
            else
            {
               ByteArrayInputStream bais = new ByteArrayInputStream(obj);
               ClusterPartition.this.setStateInternal(bais);
               bais.close();
            }
            
            ClusterPartition.this.isStateSet = true;
         }
         catch (Throwable t)
         {
            ClusterPartition.this.recordSetStateFailure(t);
         }
         finally
         {
            // Notify waiting thread that serviceState has been set.
            ClusterPartition.this.notifyChannelLock();
         }
      }
      
   }

   /**
    * A simple data class containing the view change event needed to
    * notify the HAMembershipListeners
    */
   private static class ViewChangeEvent
   {
      long viewId;
      Vector<ClusterNode> deadMembers;
      Vector<ClusterNode> newMembers;
      Vector<ClusterNode> allMembers;
      Vector<View> originatingGroups;
   }
   
   private class RequestMarshallerImpl implements org.jgroups.blocks.RpcDispatcher.Marshaller
   {

      public Object objectFromByteBuffer(byte[] buf) throws Exception
      {
         return ClusterPartition.this.objectFromByteBufferInternal(buf);
      }

      public byte[] objectToByteBuffer(Object obj) throws Exception
      {
         // wrap MethodCall in Object[service_name, byte[]] so that service name is available during demarshalling
         if (obj instanceof MethodCall)
         {
            String name = ((MethodCall)obj).getName();
            int idx = name.lastIndexOf('.');
            String serviceName = name.substring(0, idx);
            return ClusterPartition.this.objectToByteBufferInternal(new Object[]{serviceName, ClusterPartition.this.objectToByteBufferInternal(obj)});
         }

         return ClusterPartition.this.objectToByteBufferInternal(obj);
      }
   }
   
   private class ResponseMarshallerImpl implements org.jgroups.blocks.RpcDispatcher.Marshaller
   {
      
      public Object objectFromByteBuffer(byte[] buf) throws Exception
      {
         boolean trace = ClusterPartition.this.log.isTraceEnabled();
         Object retval = ClusterPartition.this.objectFromByteBufferResponseInternal(buf);
         // HAServiceResponse is only received when a scoped classloader is required for unmarshalling
         if (!(retval instanceof HAServiceResponse))
         {
            return retval;
         }
          
         String serviceName = ((HAServiceResponse)retval).getServiceName();
         byte[] payload = ((HAServiceResponse)retval).getPayload();

         ClassLoader previousCL = null;
         boolean overrideCL = false;
         try
         {
            WeakReference<ClassLoader> weak = ClusterPartition.this.clmap.get(serviceName);
            if (weak != null) // this should always be true since we only use HAServiceResponse when classloader is specified
            {
               previousCL = Thread.currentThread().getContextClassLoader();
               ClassLoader loader = weak.get();
               if( trace )
               {
                  ClusterPartition.this.log.trace("overriding response Thread ContextClassLoader for service " + serviceName);
               }
               overrideCL = true;
               Thread.currentThread().setContextClassLoader(loader);
            }
            retval = ClusterPartition.this.objectFromByteBufferResponseInternal(payload);
   
            return retval;
         }
         finally
         {
            if (overrideCL == true)
            {
               ClusterPartition.this.log.trace("resetting response classloader");
               Thread.currentThread().setContextClassLoader(previousCL);
            }
         }
      }

      public byte[] objectToByteBuffer(Object obj) throws Exception
      {
         return ClusterPartition.this.objectToByteBufferResponseInternal(obj);
      }
   }
   
   /**
    * Overrides RpcDispatcher.Handle so that we can dispatch to many
    * different objects.
    */
   private class RpcHandler extends RpcDispatcher
   {
      private RpcHandler(Channel channel, MessageListener l, MembershipListener l2, Object server_obj,
            boolean deadlock_detection)
      {
         super(channel, l, l2, server_obj, deadlock_detection);
      }
      
      /**
       * Analyze the MethodCall contained in <code>req</code> to find the
       * registered service object to invoke against, and then execute it
       * against *that* object and return result.
       *
       * This overrides RpcDispatcher.Handle so that we can dispatch to many different objects.
       * @param req The org.jgroups. representation of the method invocation
       * @return The serializable return value from the invocation
       */
      public Object handle(Message req)
      {
         Object body = null;
         Object retval = null;
         Object handler = null;
         boolean trace = this.log.isTraceEnabled();
         boolean overrideCL = false;
         ClassLoader previousCL = null;
         String service = null;
         byte[] request_bytes = null;
         
         if( trace )
         {
            this.log.trace("Partition " + ClusterPartition.this.getPartitionName() + " received msg");
         }
         if(req == null || req.getBuffer() == null)
         {
            this.log.warn("Partition " + ClusterPartition.this.getPartitionName() + " message or message buffer is null!");
            return null;
         }
         
         try
         {
            Object wrapper = ClusterPartition.this.objectFromByteBufferInternal(req.getBuffer());
            if(wrapper == null || !(wrapper instanceof Object[]))
            {
               this.log.warn("Partition " + ClusterPartition.this.getPartitionName() + " message wrapper does not contain Object[] object!");
               return null;
            }

            // wrapper should be Object[]{service_name, byte[]}
            Object[] temp = (Object[])wrapper;
            service = (String)temp[0];
            request_bytes = (byte[])temp[1];

            // see if this node has registered to handle this service
            handler = ClusterPartition.this.rpcHandlers.get(service);
            if (handler == null)
            {
               if( trace )
               {
                  this.log.trace("Partition " + ClusterPartition.this.getPartitionName() + " no rpc handler registered under service " + service);
               }
               return new NoHandlerForRPC();
            }
         }
         catch(Exception e)
         {
            this.log.warn("Partition " + ClusterPartition.this.getPartitionName() + " failed unserializing message buffer (msg=" + req + ")", e);
            return null;
         }
         
         try
         {
            // If client registered the service with a classloader, override the thread classloader here
            WeakReference<ClassLoader> weak = ClusterPartition.this.clmap.get(service);
            if (weak != null)
            {
               if( trace )
               {
                  this.log.trace("overriding Thread ContextClassLoader for RPC service " + service);
               }
               previousCL = Thread.currentThread().getContextClassLoader();
               ClassLoader loader = weak.get();
               overrideCL = true;
               Thread.currentThread().setContextClassLoader(loader);
            }
            body = ClusterPartition.this.objectFromByteBufferInternal(request_bytes);
         }
         catch (Exception e)
         {
            this.log.warn("Partition " + ClusterPartition.this.getPartitionName() + " failed extracting message body from request bytes", e);
            return null;
         }
         finally
         {
            if (overrideCL)
            {
               this.log.trace("resetting Thread ContextClassLoader");
               Thread.currentThread().setContextClassLoader(previousCL);
            }
         }
         
         if(body == null || !(body instanceof MethodCall))
         {
            this.log.warn("Partition " + ClusterPartition.this.getPartitionName() + " message does not contain a MethodCall object!");
            return null;
         }
         
         // get method call information
         MethodCall method_call = (MethodCall)body;
         String methodName = method_call.getName();
         
         if( trace )
         {
            this.log.trace("full methodName: " + methodName);
         }
         
         int idx = methodName.lastIndexOf('.');
         String handlerName = methodName.substring(0, idx);
         String newMethodName = methodName.substring(idx + 1);
         if( trace )
         {
            this.log.trace("handlerName: " + handlerName + " methodName: " + newMethodName);
            this.log.trace("Handle: " + methodName);
         }
         
         // prepare method call
         method_call.setName(newMethodName);

         /* Invoke it and just return any exception with trace level logging of
         the exception. The exception semantics of a group rpc call are weak as
         the return value may be a normal return value or the exception thrown.
         */
         try
         {
            retval = method_call.invoke(handler);
            if (overrideCL)
            {
               // wrap the response so that the service name can be accessed during unmarshalling of the response
               byte[] retbytes = ClusterPartition.this.objectToByteBufferResponseInternal(retval);
               retval = new HAServiceResponse(handlerName, retbytes);
            }
            if( trace )
            {
               this.log.trace("rpc call return value: " + retval);
            }
         }
         catch (Throwable t)
         {
            if( trace )
            {
               this.log.trace("Partition " + ClusterPartition.this.getPartitionName() + " rpc call threw exception", t);
            }
            retval = t;
         }

         return retval;
      }
      
   }
   
   /**
    * Adapted from org.jboss.cache.util.concurrent.ReclosableLatch.
    * @author Manik Surtani
    */
   private static class ThreadGate
   {
      private static final int OPEN = 1;
      private static final int CLOSED = -1;
      
      private static class Sync extends AbstractQueuedSynchronizer
      {
         Sync(int state)
         {
            this.setState(state);
         }
         
         @Override
         protected int tryAcquireShared(int ingored)
         {
            return this.getState();
         }

         @Override
         protected boolean tryReleaseShared(int state)
         {
            this.setState(state);
            return true;
         }
      }

      private final Sync sync = new Sync(CLOSED);
      
      public void open()
      {
         this.sync.releaseShared(OPEN);
      }
      
      public void close()
      {
         this.sync.releaseShared(CLOSED);
      }
      
      public void await() throws InterruptedException
      {
         this.sync.acquireSharedInterruptibly(0);
      }
      
      public boolean await(long timeout) throws InterruptedException
      {
         return this.sync.tryAcquireSharedNanos(0, TimeUnit.MILLISECONDS.toNanos(timeout));
      }
   }
   
   private void setupLoggers(String partitionName)
   {
      if (partitionName == null)
      {
         this.log = Logger.getLogger(HAPartition.class.getName());
         this.clusterLifeCycleLog = Logger.getLogger(HAPartition.class.getName() + ".lifecycle");
      }
      else
      {
         this.log = Logger.getLogger(HAPartition.class.getName() + "." + partitionName);
         this.clusterLifeCycleLog = Logger.getLogger(HAPartition.class.getName() + ".lifecycle." + partitionName);
      }
   }
   
//   private synchronized void registerDRM()
//   {
//      MBeanServer mbs = getServer();
//      if (this.replicantManager != null && mbs != null)
//      {
//         try
//         {
//            ObjectName oname = getDRMObjectName();
//            mbs.registerMBean(this.replicantManager, oname);
//         }
//         catch (JMException e)
//         {
//            log.error("Unable to register DRM in JMX", e);
//         }
//      }
//   }
   
//   private void unregisterDRM()
//   {
//      MBeanServer mbs = getServer();
//      if (this.replicantManager != null && mbs != null)
//      {
//         try
//         {
//            ObjectName oname = getDRMObjectName();
//            if (mbs.isRegistered(oname))
//            {
//               mbs.unregisterMBean(oname);
//            }
//         }
//         catch (JMException e)
//         {
//            log.error("Unable to register DRM in JMX", e);
//         }
//      }
//      
//   }

//   private ObjectName getDRMObjectName() throws MalformedObjectNameException
//   {
//      ObjectName oname = new ObjectName(DistributedReplicantManagerImpl.OBJECT_NAME_BASE + ",partitionName=" + getPartitionName());
//      return oname;
//   }
   
}
