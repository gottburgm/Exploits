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

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RemoteStub;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.NoSuchObjectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import javax.naming.NoPermissionException;

import org.jboss.ha.client.loadbalance.LoadBalancePolicy;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.interfaces.HARMIClient;
import org.jboss.ha.framework.interfaces.HARMIProxy;
import org.jboss.ha.framework.interfaces.HARMIResponse;
import org.jboss.ha.framework.interfaces.HARMIServer;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.logging.Logger;
import org.jboss.net.sockets.DefaultSocketFactory;
import org.jboss.ha.jndi.HARMIServerGuard;
import org.jnp.server.NamingServerGuard;

/**
 * This class is a <em>server-side</em> proxy for replicated RMI objects.
 *
 * @author bill@jboss.org
 * @author sacha.labourey@jboss.org
 * @author Scott.Stark@jboss.org
 * @version $Revision: 113132 $
 */
public class HARMIServerImpl
   implements HARMIServer
{
   protected Object handler;
   protected Map invokerMap = new HashMap();
   protected org.jboss.logging.Logger log;
   protected RemoteStub rmistub;
   protected Object stub;
   protected String key;
   protected Class intf;
   protected RefreshProxiesHATarget target;

   protected static final int MAX_CONCURRENT_REQUESTS = Integer.MAX_VALUE;
   protected Semaphore startStopSemaphore = new Semaphore(0, true);

   public HARMIServerImpl(HAPartition partition,
                          String replicantName,
                          Class intf,
                          Object handler,
                          int port,
                          RMIClientSocketFactory csf,
                          RMIServerSocketFactory ssf)
      throws Exception
   {
      this(partition,
                      replicantName,
                      intf,
                      handler,
                      port,
                      csf,
                      ssf,
                      null);

   }

   public HARMIServerImpl(HAPartition partition,
                          String replicantName,
                          Class intf,
                          Object handler,
                          int port,
                          RMIClientSocketFactory clientSocketFactory,
                          RMIServerSocketFactory serverSocketFactory,
                          InetAddress bindAddress)
      throws Exception
   {
      this.handler = handler;
      this.log = Logger.getLogger(this.getClass());
      this.intf = intf;
      this.key = partition.getPartitionName() + "/" + replicantName;

      // Obtain the hashes for the supported handler interfaces
      Class[] ifaces = handler.getClass().getInterfaces();
      for (int i = 0; i < ifaces.length; i++)
      {
         Map tmp = MarshalledInvocation.methodToHashesMap(ifaces[i]);
         invokerMap.putAll(tmp);
      }

      if( bindAddress != null )
      {
         // If there is no serverSocketFactory use a default
         if( serverSocketFactory == null )
            serverSocketFactory = new DefaultSocketFactory(bindAddress);
         else
         {
            // See if the server socket supports setBindAddress(String)
            try
            {
               Class[] parameterTypes = {String.class};
               Class ssfClass = serverSocketFactory.getClass();
               Method m = ssfClass.getMethod("setBindAddress", parameterTypes);
               Object[] args = {bindAddress.getHostAddress()};
               m.invoke(serverSocketFactory, args);
            }
            catch (NoSuchMethodException e)
            {
               log.warn("Socket factory does not support setBindAddress(String)");
               // Go with default address
            }
            catch (Exception e)
            {
               log.warn("Failed to setBindAddress="+bindAddress+" on socket factory", e);
               // Go with default address
            }
         }
      }

      this.rmistub = (RemoteStub)UnicastRemoteObject.exportObject(this, port, clientSocketFactory, serverSocketFactory);// casting is necessary because interface has changed in JDK>=1.2
      this.target = new RefreshProxiesHATarget(partition, replicantName, rmistub, HATarget.ENABLE_INVOCATIONS);

      HARMIServer.rmiServers.put(key, this);

      // Start accepting requests
      startStopSemaphore.release(MAX_CONCURRENT_REQUESTS);
   }

   /**
    * Create a new HARMIServer implementation that will act as a RMI end-point for a specific server.
    *
    * @param partition {@link HAPartition} that will determine the cluster member
    * @param replicantName Name of the service using this HARMIServer
    * @param intf Class type under which should appear the RMI server dynamically built
    * @param handler Target object to which calls will be forwarded
    * @throws Exception Thrown if any exception occurs during call forwarding
    */
   public HARMIServerImpl(HAPartition partition, String replicantName, Class intf, Object handler) throws Exception
   {
      this(partition, replicantName, intf, handler, 0, null, null);
   }

   /**
    * Once a HARMIServer implementation exists, it is possible to ask for a stub that can, for example,
    * be bound in JNDI for client use. Each client stub may incorporate a specific load-balancing
    * policy.
    *
    * @param policy {@link org.jboss.ha.client.loadbalance.LoadBalancePolicy} implementation to ues on the client.
    * @return proxy instance object
    */
   public Object createHAStub(LoadBalancePolicy policy)
   {
      HARMIClient client = new HARMIClient(target.getReplicants(),
         target.getCurrentViewId (), policy, key, handler);
      this.target.addProxy (client);
      return Proxy.newProxyInstance(
      intf.getClassLoader(),
      new Class[]{ intf, HARMIProxy.class },
      client);
   }

   public void destroy()
   {
      try
      {
         HARMIServer.rmiServers.remove(key);
         UnicastRemoteObject.unexportObject(this, true);

         // wait for current requests to finish
         startStopSemaphore.acquire(MAX_CONCURRENT_REQUESTS);

	     target.destroy();
      } catch (Exception e)
      {
         log.error("failed to destroy", e);
      }
   }

   // HARMIServer implementation ----------------------------------------------

   public HARMIResponse invoke(long clientViewId, MarshalledInvocation mi)
      throws Exception
   {
      mi.setMethodMap(invokerMap);
      Method method = mi.getMethod();

      // NoSuchObjectException is used because it's the exception that the client would 
      // get during startup or shutdown if the RMI object was exported in the right sequence
      if (! startStopSemaphore.tryAcquire())
         throw new NoSuchObjectException ( "HARMIServer is not running" );


      try
      {
         log.info("RMI local invocation =" + mi.isLocal());
         if (NamingServerGuard.GUARDED_JNDI_METHOD_NAMES.indexOf(method.getName()) != -1) 
         {
            throw new NoPermissionException(method.getName() + 
                  " JNDI operation not allowed when on non-local invocation.");
         }

         HARMIResponse rsp = new HARMIResponse();
         if (clientViewId != target.getCurrentViewId())
         {
            rsp.newReplicants = new ArrayList(target.getReplicants());
            rsp.currentViewId = target.getCurrentViewId();
         }

         rsp.response = method.invoke(handler, mi.getArguments());
         return rsp;
      }
      catch (IllegalAccessException iae)
      {
         throw iae;
      }
      catch (IllegalArgumentException iae)
      {
         throw iae;
      }
      catch (java.lang.reflect.InvocationTargetException ite)
      {
         throw (Exception)ite.getTargetException();
      }
      finally
      {
         startStopSemaphore.release();
      }
   }

   public List getReplicants() throws Exception
   {
      return target.getReplicants();
   }

   public Object getLocal() throws Exception
   {
      return handler;
   }

   public class RefreshProxiesHATarget extends HATarget
   {
      protected ArrayList generatedProxies;

      public RefreshProxiesHATarget(HAPartition partition,
            String replicantName,
            java.io.Serializable target,
            int allowInvocations)
         throws Exception
      {
         super (partition, replicantName, target, allowInvocations);
      }

      public void init() throws Exception
      {
         super.init ();
         generatedProxies = new ArrayList ();
      }


      public synchronized void addProxy (HARMIClient client)
      {
         SoftReference ref = new SoftReference(client);
         generatedProxies.add (ref);
      }

      public synchronized void replicantsChanged(String key, List newReplicants, int newReplicantsViewId, boolean merge)
      {
         super.replicantsChanged (key, newReplicants, newReplicantsViewId, merge);

         // we now update all generated proxies
         //
         int max = generatedProxies.size ();
         ArrayList trash = new ArrayList();
         for (int i=0; i<max; i++)
         {
            SoftReference ref = (SoftReference)generatedProxies.get (i);
            HARMIClient proxy = (HARMIClient)ref.get ();
            if (proxy == null)
            {
               trash.add (ref);
            }
            else
            {
               proxy.updateClusterInfo (this.replicants, this.clusterViewId);
            }
         }

         if (trash.size () > 0)
            generatedProxies.removeAll (trash);

      }
   }
}
