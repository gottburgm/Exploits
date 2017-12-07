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
package org.jboss.ha.jndi;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.rmi.MarshalledObject;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.naming.NoPermissionException;

import javax.management.ObjectName;
import javax.net.ServerSocketFactory;

import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.jndi.spi.DistributedTreeManager;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.threadpool.BasicThreadPool;
import org.jboss.util.threadpool.BasicThreadPoolMBean;
import org.jboss.util.threadpool.ThreadPool;
import org.jnp.interfaces.Naming;
import org.jnp.interfaces.NamingContext;
import org.jnp.server.NamingServerGuard;

/**
 * Management Bean for the protocol independent HA-JNDI service. This allows the
 * naming service transport layer to be provided by a detached invoker service
 * like JRMPInvokerHA + ProxyFactoryHA.
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 113129 $
 */
public class DetachedHANamingService
   extends ServiceMBeanSupport
   implements DetachedHANamingServiceMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   /**
    * The jnp server socket through which the HAJNDI stub is vended
    */
   ServerSocket bootstrapSocket;

   /**
    * The Naming interface server implementation
    */
   HAJNDI theServer;
   /**
    * The mapping from the long method hash to the Naming Method
    */
   private Map<Long, Method> marshalledInvocationMapping;
   /**
    * The protocol stub returned to clients by the bootstrap lookup
    */
   Naming stub;
   /**
    * The HAPartition
    */
   protected HAPartition clusterPartition;
   /**
    * The manager of our distributed bindings
    */
   private DistributedTreeManager  distributedTreeManager;

   /**
    * The proxy factory service that generates the Naming stub
    */
   private ObjectName proxyFactory;
   
   /**
    * The local (non-HA) Naming instance. 
    */
   private Naming localNamingInstance;

   /**
    * The interface to bind to. This is useful for multi-homed hosts that want
    * control over which interfaces accept connections.
    */
   InetAddress bindAddress;
   /**
    * The bootstrapSocket listen queue depth
    */
   private int backlog = 50;
   /**
    * The jnp protocol listening port. The default is 1100, the same as the RMI
    * registry default port.
    */
   int port = 1100;

   /**
    * The autodiscovery multicast group
    */
   String adGroupAddress = NamingContext.DEFAULT_DISCOVERY_GROUP_ADDRESS;
   /**
    * The autodiscovery port
    */
   int adGroupPort = NamingContext.DEFAULT_DISCOVERY_GROUP_PORT;
   /**
    * The interface to bind the Multicast socket for autodiscovery to
    */
   InetAddress discoveryBindAddress;
   /** The runable task for discovery request packets */
   private AutomaticDiscovery autoDiscovery = null;
   /** A flag indicating if autodiscovery should be disabled */
   private boolean discoveryDisabled = false;
   /** The autodiscovery Multicast reply TTL */
   int autoDiscoveryTTL = 16;
   /**
    * An optional custom server socket factory for the bootstrap lookup
    */
   private ServerSocketFactory jnpServerSocketFactory;
   /**
    * The class name of the optional custom JNP server socket factory
    */
   private String jnpServerSocketFactoryName;

   /**
    * The thread pool used to handle jnp stub lookup requests
    */
   ThreadPool lookupPool;

   // Public --------------------------------------------------------

   public DetachedHANamingService()
   {
      // for JMX
   }

   /**
    * Expose the Naming service interface mapping as a read-only attribute
    * @return A Map<Long hash, Method> of the Naming interface
    * @jmx:managed-attribute
    */
   public Map<Long, Method> getMethodMap()
   {
      return this.marshalledInvocationMapping;
   }

   public String getPartitionName()
   {
      return this.clusterPartition.getPartitionName();
   }

   public HAPartition getHAPartition()
   {
      return this.clusterPartition;
   }

   public void setHAPartition(HAPartition clusterPartition)
   {
      this.clusterPartition = clusterPartition;
   }
   
   public DistributedTreeManager  getDistributedTreeManager ()
   {
      return this.distributedTreeManager;
   }

   public void setDistributedTreeManager (DistributedTreeManager  distributedTreeManager )
   {
      this.distributedTreeManager = distributedTreeManager;
   }

   public Naming getLocalNamingInstance()
   {
      return localNamingInstance;
   }

   public void setLocalNamingInstance(Naming localNamingInstance)
   {
      this.localNamingInstance = localNamingInstance;
   }

   public ObjectName getProxyFactoryObjectName()
   {
      return this.proxyFactory;
   }

   public void setProxyFactoryObjectName(ObjectName proxyFactory)
   {
      this.proxyFactory = proxyFactory;
   }

   public void setPort(int p)
   {
      this.port = p;
   }

   public int getPort()
   {
      return this.port;
   }

   public String getBindAddress()
   {
      String address = null;
      if (this.bindAddress != null)
      {
         address = this.bindAddress.getHostAddress();
      }
      return address;
   }

   public void setBindAddress(String host) throws java.net.UnknownHostException
   {
      this.bindAddress = InetAddress.getByName(host);
   }

   public int getBacklog()
   {
      return this.backlog;
   }

   public void setBacklog(int backlog)
   {
      if (backlog <= 0)
      {
         backlog = 50;
      }
      this.backlog = backlog;
   }

   public void setDiscoveryDisabled(boolean disable)
   {
      this.discoveryDisabled = disable;
   }

   public boolean getDiscoveryDisabled()
   {
      return this.discoveryDisabled;
   }

   public String getAutoDiscoveryAddress()
   {
      return this.adGroupAddress;
   }

   public void setAutoDiscoveryAddress(String adAddress)
   {
      this.adGroupAddress = adAddress;
   }

   public int getAutoDiscoveryGroup()
   {
      return this.adGroupPort;
   }
   
   public void setAutoDiscoveryGroup(int adGroup)
   {
      this.adGroupPort = adGroup;
   }

   public String getAutoDiscoveryBindAddress()
   {
      return (this.discoveryBindAddress != null) ? this.discoveryBindAddress.getHostAddress() : null;
   }
   
   public void setAutoDiscoveryBindAddress(String address) throws UnknownHostException
   {
      this.discoveryBindAddress = InetAddress.getByName(address);
   }

   public int getAutoDiscoveryTTL()
   {
      return this.autoDiscoveryTTL;
   }

   public void setAutoDiscoveryTTL(int ttl)
   {
      this.autoDiscoveryTTL = ttl;
   }

   public void setJNPServerSocketFactory(String factoryClassName) throws ClassNotFoundException, InstantiationException, IllegalAccessException
   {
      this.jnpServerSocketFactoryName = factoryClassName;
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<?> clazz = loader.loadClass(this.jnpServerSocketFactoryName);
      this.jnpServerSocketFactory = (ServerSocketFactory) clazz.newInstance();
   }

   public void setLookupPool(BasicThreadPoolMBean poolMBean)
   {
      this.lookupPool = poolMBean.getInstance();
   }
/*
   public void startService(HAPartition haPartition)
      throws Exception
   {
      this.startService();
   }
*/
   @Override
   protected void createService() throws Exception
   {
      if (this.clusterPartition == null)
      {
         throw new IllegalStateException("HAPartition property must be set before starting HAJNDI service");
      }
      
      if (this.distributedTreeManager == null)
      {
         throw new IllegalStateException("DistributedTreeManager property must be set before starting HAJNDI service");
      }
      
      this.log.debug("Initializing HAJNDI server on partition: " + this.clusterPartition.getPartitionName());
      
      // Start HAJNDI service
      this.theServer = new HAJNDI(this.clusterPartition, this.distributedTreeManager, localNamingInstance);

      // Build the Naming interface method map
      Map<Long, Method> map = new HashMap<Long, Method>(13);
      Method[] methods = Naming.class.getMethods();
      for (Method method: methods)
      {
         Long hash = new Long(MarshalledInvocation.calculateHash(method));
         map.put(hash, method);
      }
      this.marshalledInvocationMapping = Collections.unmodifiableMap(map);
      
      // share instance for in-vm discovery
      NamingContext.setHANamingServerForPartition(this.clusterPartition.getPartitionName(), this.theServer);
   }

   @Override
   protected void startService()
      throws Exception
   {
      this.log.debug("Obtaining the HAJNDI transport proxy");
      this.stub = this.getNamingProxy();
      this.distributedTreeManager.setHAStub(this.stub);

      this.log.debug("initializing HAJNDI");
      this.theServer.init();

      if (this.port >= 0)
      {
         this.log.debug("Starting HAJNDI bootstrap listener");
         this.initBootstrapListener();
      }

      // Automatic Discovery for unconfigured clients
      if (this.adGroupAddress != null && this.discoveryDisabled == false)
      {
         try
         {
            this.autoDiscovery = new AutomaticDiscovery();
            this.autoDiscovery.start();
            this.lookupPool.run(this.autoDiscovery);
         }
         catch (Exception e)
         {
            this.log.warn("Failed to start AutomaticDiscovery", e);
         }
      }
   }

   @Override
   protected void stopService() throws Exception
   {
      // un-share instance for in-vm discovery
      NamingContext.removeHANamingServerForPartition(this.clusterPartition.getPartitionName());

      // Stop listener
      ServerSocket s = this.bootstrapSocket;
      this.bootstrapSocket = null;
      if (s != null)
      {
         this.log.debug("Closing the HAJNDI bootstrap listener");
         s.close();
      }

      // Stop HAJNDI service
      this.log.debug("Stopping the HAJNDI service");
      this.theServer.shutdown();

      this.log.debug("Stopping AutomaticDiscovery");
      if (this.autoDiscovery != null && this.discoveryDisabled == false)
      {
         this.autoDiscovery.stop();
      }
   }
   
   @Override
   protected void destroyService() throws Exception
   {
      this.log.debug("Destroying the HAJNDI service");      
   }

   /**
    * Expose the Naming service via JMX to invokers.
    * @param invocation A pointer to the invocation object
    * @return Return value of method invocation.
    * @throws Exception Failed to invoke method.
    * @jmx:managed-operation
    */
   public Object invoke(Invocation invocation) throws Exception
   {
      // Set the method hash to Method mapping
      if (invocation instanceof MarshalledInvocation)
      {
         MarshalledInvocation mi = (MarshalledInvocation) invocation;
         mi.setMethodMap(this.marshalledInvocationMapping);
      }
      // Invoke the Naming method via reflection
      Method method = invocation.getMethod();
      Object[] args = invocation.getArguments();
      Object value = null;
      
      log.info("DETACHED local invocation =" + invocation.isLocal());
      if (!invocation.isLocal() 
            && NamingServerGuard.GUARDED_JNDI_METHOD_NAMES.indexOf(method.getName()) != -1) {
         throw new NoPermissionException(method.getName() + 
               " JNDI operation not allowed when on non-local invocation.");
      }
      
      try
      {
         value = method.invoke(this.theServer, args);
      }
      catch (InvocationTargetException e)
      {
         Throwable t = e.getTargetException();
         if (t instanceof Exception)
         {
            throw (Exception) t;
         }

         throw new UndeclaredThrowableException(t, method.toString());
      }

      return value;
   }

   /**
    * Bring up the bootstrap lookup port for obtaining the naming service proxy
    */
   protected void initBootstrapListener()
   {
      // Start listener
      try
      {
         // Get the default ServerSocketFactory is one was not specified
         if (this.jnpServerSocketFactory == null)
         {
            this.jnpServerSocketFactory = ServerSocketFactory.getDefault();
         }
         this.bootstrapSocket = this.jnpServerSocketFactory.createServerSocket(this.port, this.backlog, this.bindAddress);
         // If an anonymous port was specified get the actual port used
         if (this.port == 0)
         {
            this.port = this.bootstrapSocket.getLocalPort();
         }
         String msg = "Started HAJNDI bootstrap; jnpPort=" + this.port
            + ", backlog=" + this.backlog + ", bindAddress=" + this.bindAddress;
         this.log.info(msg);
      }
      catch (IOException e)
      {
         this.log.error("Could not start HAJNDI bootstrap listener on port " + this.port, e);
      }

      if (this.lookupPool == null)
      {
         this.lookupPool = new BasicThreadPool("HANamingBootstrap Pool");
      }
      AcceptHandler handler = new AcceptHandler();
      this.lookupPool.run(handler);
   }

   // Protected -----------------------------------------------------

   /**
    * Get the Naming proxy for the transport. This version looks  up the
    * proxyFactory service Proxy attribute. Subclasses can override this to set
    * the proxy another way.
    * @return The Naming proxy for the protocol used with the HAJNDI service
    */
   protected Naming getNamingProxy() throws Exception
   {
      return (Naming) this.server.getAttribute(this.proxyFactory, "Proxy");
   }

   // Private -------------------------------------------------------

   private class AutomaticDiscovery
      implements Runnable
   {
      private Logger log = Logger.getLogger(AutomaticDiscovery.class);
      /** The socket for auto discovery requests */
      private MulticastSocket socket = null;
      /** The ha-jndi addres + ':' + port string */
      private byte[] ipAddress = null;
      /** The multicast group address */
      private InetAddress group = null;
      private volatile boolean stopping = false;
      // Thread that is executing the run() method
      private volatile Thread receiverThread = null;
      private volatile boolean receiverStopped = true;

      public AutomaticDiscovery() throws Exception
      {
      }

      public void start() throws Exception
      {
         this.stopping = false;
         
         // Set up the multicast socket on which we listen for discovery requests
         
         this.group = InetAddress.getByName(DetachedHANamingService.this.adGroupAddress);
         
         // On Linux, we avoid cross-talk problem by binding the MulticastSocket
         // to the multicast address. See https://jira.jboss.org/jira/browse/JGRP-777
         if(checkForPresence("os.name", "linux"))
         {             
            this.socket = createMulticastSocket(group, DetachedHANamingService.this.adGroupPort);
         }
         else
         {
            this.socket = new MulticastSocket(DetachedHANamingService.this.adGroupPort);
         }
         
         // If there is a valid bind address, set the socket interface to it         
         // Use the jndi bind address if there is no discovery address
         if (DetachedHANamingService.this.discoveryBindAddress == null)
         {
            DetachedHANamingService.this.discoveryBindAddress = DetachedHANamingService.this.bindAddress;
         }
         
         if (DetachedHANamingService.this.discoveryBindAddress != null 
               && DetachedHANamingService.this.discoveryBindAddress.isAnyLocalAddress() == false)
         {
            this.socket.setInterface(DetachedHANamingService.this.discoveryBindAddress);
         }
         
         this.socket.setTimeToLive(DetachedHANamingService.this.autoDiscoveryTTL);
         this.socket.joinGroup(this.group);

         
         // Determine the hostname:port string we will return to discovery requests
         String address = DetachedHANamingService.this.getBindAddress();
         /* An INADDR_ANY (0.0.0.0 || null) address is useless as the value
            sent to a remote client so check for this and use the local host
            address instead.
          */
         if (address == null || address.equals("0.0.0.0"))
         {
            address = InetAddress.getLocalHost().getHostAddress();
         }
         this.ipAddress = (address + ":" + DetachedHANamingService.this.port).getBytes();

         this.log.info("Listening on " + this.socket.getInterface() + ":" + this.socket.getLocalPort()
            + ", group=" + DetachedHANamingService.this.adGroupAddress
            + ", HA-JNDI address=" + new String(this.ipAddress));
      }

      public void stop()
      {
         try
         {
            this.stopping = true;
            
            // JBAS-2834 -- try to stop the receiverThread
            if (this.receiverThread != null
                  && this.receiverThread != Thread.currentThread()
                  && this.receiverThread.isInterrupted() == false)
            {
               // Give it a moment to die on its own (unlikely)
               this.receiverThread.join(5);
               if (!this.receiverStopped)
               {
                  this.receiverThread.interrupt(); // kill it
               }
            }
            
            this.socket.leaveGroup(this.group);
            this.socket.close();
         }
         catch (Exception ex)
         {
            this.log.error("Stopping AutomaticDiscovery failed", ex);
         }
      }

      public void run()
      {
         boolean trace = this.log.isTraceEnabled();
         this.log.debug("Discovery request thread begin");
         
         // JBAS-2834 Cache a reference to this thread so stop()
         // can interrupt it if necessary
         this.receiverThread = Thread.currentThread();

         this.receiverStopped = false;
         
         // Wait for a datagram
         while (true)
         {
            // Stopped by normal means
            if (this.stopping)
            {
               break;
            }
            try
            {
               if (trace)
               {
                  this.log.trace("HA-JNDI AutomaticDiscovery waiting for queries...");
               }
               byte[] buf = new byte[256];
               DatagramPacket packet = new DatagramPacket(buf, buf.length);
               this.socket.receive(packet);
               if (trace)
               {
                  this.log.trace("HA-JNDI AutomaticDiscovery Packet received.");
               }

               // Queue the response to the thread pool
               DiscoveryRequestHandler handler = new DiscoveryRequestHandler(this.log,
                  packet, this.socket, this.ipAddress);
               DetachedHANamingService.this.lookupPool.run(handler);
               if (trace)
               {
                  this.log.trace("Queued DiscoveryRequestHandler");
               }
            }
            catch (Throwable t)
            {
               if (this.stopping == false)
               {
                  this.log.warn("Ignored error while processing HAJNDI discovery request:", t);
               }
            }
         }
         this.receiverStopped = true;
         this.log.debug("Discovery request thread end");
      }

      private boolean checkForPresence(final String key, String value)
      {
         try
         {
            String tmp = null;
            if (System.getSecurityManager() == null)
            {
               tmp = System.getProperty(key);
            }
            else
            {
               // Use a different local var to limit scope of @SuppressWarnings
               @SuppressWarnings("unchecked")
               String prop = (String) AccessController.doPrivileged(new PrivilegedAction()
                  {
                     public Object run()
                     {
                        return System.getProperty(key);
                     }
   
                  });
               tmp = prop;
            }

            return tmp != null && tmp.trim().toLowerCase().startsWith(value);
         }
         catch (Throwable t)
         {
            return false;
         }
      }
      
      private MulticastSocket createMulticastSocket(InetAddress mcast_addr, int port) throws IOException 
      {
        if(mcast_addr != null && !mcast_addr.isMulticastAddress()) {
            log.warn("mcast_addr (" + mcast_addr + ") is not a multicast address, will be ignored");
            return new MulticastSocket(port);
        }

        SocketAddress saddr=new InetSocketAddress(mcast_addr, port);
        MulticastSocket retval=null;

        try {
            retval=new MulticastSocket(saddr);
        }
        catch(IOException ex) {
            StringBuilder sb=new StringBuilder();
             String type=mcast_addr != null ? mcast_addr instanceof Inet4Address? "IPv4" : "IPv6" : "n/a";
             sb.append("could not bind to " + mcast_addr + " (" + type + " address)");
             sb.append("; make sure your mcast_addr is of the same type as the IP stack (IPv4 or IPv6).");
             sb.append("\nWill ignore mcast_addr, but this may lead to cross talking " +
                     "(see http://community.jboss.org/wiki/CrossTalking for details). ");
             sb.append("\nException was: " + ex);
             log.warn(sb);
        }
        if(retval == null)
        {
            retval=new MulticastSocket(port);
        }
        return retval;
    }
   }

   /**
    * The class used as the runnable for writing the bootstrap stub
    */
   private class DiscoveryRequestHandler implements Runnable
   {
      private Logger log;
      private MulticastSocket socket;
      private DatagramPacket packet;
      private byte[] ipAddress;

      DiscoveryRequestHandler(Logger log, DatagramPacket packet,
         MulticastSocket socket, byte[] ipAddress)
      {
         this.log = log;
         this.packet = packet;
         this.socket = socket;
         this.ipAddress = ipAddress;
      }
      public void run()
      {
         boolean trace = this.log.isTraceEnabled();
         if( trace )
         {
            this.log.trace("DiscoveryRequestHandler begin");
         }
         // Return the naming server IP address and port to the client
         try
         {
            // See if the discovery is restricted to a particular parition
            String requestData = new String(this.packet.getData()).trim();
            if( trace )
            {
               this.log.trace("RequestData: "+requestData);
            }
            int colon = requestData.indexOf(':');
            if (colon > 0)
            {
               // Check the partition name
               String name = requestData.substring(colon + 1);
               if (name.equals(DetachedHANamingService.this.clusterPartition.getPartitionName()) == false)
               {
                  this.log.debug("Ignoring discovery request for partition: " + name);
                  if( trace )
                  {
                     this.log.trace("DiscoveryRequestHandler end");
                  }
                  return;
               }
            }
            DatagramPacket p = new DatagramPacket(this.ipAddress, this.ipAddress.length,
               this.packet.getAddress(), this.packet.getPort());
            if (trace)
            {
               this.log.trace("Sending AutomaticDiscovery answer: " + new String(this.ipAddress) +
                         " to " + this.packet.getAddress() + ":" + this.packet.getPort());
            }
            this.socket.send(p);
            if (trace)
            {
               this.log.trace("AutomaticDiscovery answer sent.");
            }
         }
         catch (IOException ex)
         {
            this.log.error("Error writing response", ex);
         }
         if( trace )
         {
            this.log.trace("DiscoveryRequestHandler end");
         }
      }
   }

   /**
    * The class used as the runnable for the bootstrap lookup thread pool.
    */
   private class AcceptHandler implements Runnable
   {
      AcceptHandler()
      {
      }
      
      public void run()
      {
         boolean trace = DetachedHANamingService.this.log.isTraceEnabled();
         while (DetachedHANamingService.this.bootstrapSocket != null)
         {
            Socket socket = null;
            // Accept a connection
            try
            {
               socket = DetachedHANamingService.this.bootstrapSocket.accept();
               if (trace)
               {
                  DetachedHANamingService.this.log.trace("Accepted bootstrap client: "+socket);
               }
               BootstrapRequestHandler handler = new BootstrapRequestHandler(socket);
               DetachedHANamingService.this.lookupPool.run(handler);
            }
            catch (IOException e)
            {
               // Stopped by normal means
               if (DetachedHANamingService.this.bootstrapSocket == null)
               {
                  return;
               }
               DetachedHANamingService.this.log.error("Naming accept handler stopping", e);
            }
            catch(Throwable e)
            {
               DetachedHANamingService.this.log.error("Unexpected exception during accept", e);
            }
         }
      }
   }

   /**
    * The class used as the runnable for writing the bootstrap stub
    */
   private class BootstrapRequestHandler implements Runnable
   {
      private Socket socket;
      BootstrapRequestHandler(Socket socket)
      {
         this.socket = socket;
      }
      public void run()
      {
         // Return the naming server stub
         try
         {
            OutputStream os = this.socket.getOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(os);
            MarshalledObject replyStub = new MarshalledObject(DetachedHANamingService.this.stub);
            out.writeObject(replyStub);
            out.close();
         }
         catch (IOException ex)
         {
            DetachedHANamingService.this.log.debug("Error writing response to " + this.socket, ex);
         }
         finally
         {
            try
            {
               this.socket.close();
            }
            catch (IOException e)
            {
            }
         }
      }
   }
}
