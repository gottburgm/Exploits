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
package org.jboss.ejb;

// $Id: SessionContainer.java 81030 2008-11-14 12:59:42Z dimitris@jboss.org $

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.RemoveException;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.management.ObjectName;
 
import org.jboss.invocation.Invocation;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.metadata.SessionMetaData;

/**
 * <p>
 * Container dedicated to session beans. Contains factored out
 * redundancies between stateless and stateful treatments, because
 * (extending the spec) we would like to also support stateful
 * web services.
 * </p>
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @version $Revision: 81030 $
 * @since 30.10.2003
 */
public abstract class SessionContainer extends Container
{
   /**
    * These are the mappings between the home interface methods and the
    * container methods.
    */
   protected Map homeMapping;

   /**
    * These are the mappings between the remote interface methods and the
    * bean methods.
    */
   protected Map beanMapping;

   /**
    * This is the first interceptor in the chain. The last interceptor must
    * be provided by the container itself
    */
   protected Interceptor interceptor;

   /** this is the service endpoint class */
   protected Class serviceEndpoint;

   /** This is the instancepool that is to be used */
   protected InstancePool instancePool;

   /** set the instance pool */
   public void setInstancePool(InstancePool ip)
   {
      if (ip == null)
         throw new IllegalArgumentException("Null pool");

      this.instancePool = ip;
   }

   /** return instance pool */
   public InstancePool getInstancePool()
   {
      return instancePool;
   }

   /** return local proxy factory */
   public LocalProxyFactory getLocalProxyFactory()
   {
      return localProxyFactory;
   }

   /** add an additional interceptor to the chain */
   public void addInterceptor(Interceptor in)
   {
      if (interceptor == null)
      {
         interceptor = in;
      }
      else
      {
         Interceptor current = interceptor;
         while (current.getNext() != null)
         {
            current = current.getNext();
         }

         current.setNext(in);
      }
   }

   /** return first interceptor */
   public Interceptor getInterceptor()
   {
      return interceptor;
   }

   /** return service endpoint */
   public Class getServiceEndpoint()
   {
      return serviceEndpoint;
   }

   // Container stuff

   protected void createService() throws Exception
   {
      // Associate thread with classloader
      ClassLoader oldCl = SecurityActions.getContextClassLoader();
      SecurityActions.setContextClassLoader(getClassLoader());
      pushENC();
      try
      {
         // Acquire classes from CL
         if (metaData.getHome() != null)
            homeInterface = classLoader.loadClass(metaData.getHome());
         if (metaData.getRemote() != null)
            remoteInterface = classLoader.loadClass(metaData.getRemote());
         if (((SessionMetaData) metaData).getServiceEndpoint() != null)
         {
            serviceEndpoint =
                    classLoader.loadClass(((SessionMetaData) metaData).getServiceEndpoint());
         }

         // Call default init
         super.createService();

         // Make some additional validity checks with regards to the container configuration
         checkCoherency();

         // Map the bean methods
         setupBeanMapping();

         // Map the home methods
         setupHomeMapping();

         // Map the interfaces to Long
         setupMarshalledInvocationMapping();

         createInvokers();

         createInstanceCache();

         createInstancePool();

         createPersistenceManager();

         createInterceptors();
      }
      finally
      {
         popENC();
         // Reset classloader
         SecurityActions.setContextClassLoader(oldCl);
      }
   }

   /**
    * how home methods are treated by container
    */
   protected abstract void setupHomeMapping() throws Exception;

   /** loop through methods and setup mapping */
   protected void setUpBeanMappingImpl(Map map, Method[] methods, String declaringClass)
           throws NoSuchMethodException
   {
      for (int i = 0; i < methods.length; i++)
      {
         Method m = methods[i];
         if (m.getDeclaringClass().getName().equals(declaringClass) == false)
         {
            // Implemented by bean
            try
            {
               Method beanMethod = beanClass.getMethod(m.getName(), m.getParameterTypes());
               map.put(m, beanMethod);
            }
            catch (NoSuchMethodException ex)
            {
               throw new NoSuchMethodException("Not found in bean class: " + m);
            }

            log.debug("Mapped " + m.getName() + " HASH " + m.hashCode() + "to " + map.get(m));
         }
         else
         {
            // Implemented by container
            try
            {
               Method containerMethod = getClass().getMethod(m.getName(), new Class[]{Invocation.class});
               map.put(m, containerMethod);
            }
            catch (NoSuchMethodException e)
            {
               throw new NoSuchMethodException("Not found in container class: " + m);
            }

            log.debug("Mapped Container method " + m.getName() + " HASH " + m.hashCode());
         }
      }
   }

   /** build bean mappings for application logic */
   protected void setupBeanMapping() throws NoSuchMethodException
   {
      Map map = new HashMap();

      if (remoteInterface != null)
      {
         Method[] m = remoteInterface.getMethods();
         setUpBeanMappingImpl(map, m, "javax.ejb.EJBObject");
      }

      if (localInterface != null)
      {
         Method[] m = localInterface.getMethods();
         setUpBeanMappingImpl(map, m, "javax.ejb.EJBLocalObject");
      }

      if (TimedObject.class.isAssignableFrom(beanClass))
      {
         Method[] m = new Method[]{TimedObject.class.getMethod("ejbTimeout", new Class[]{Timer.class})};
         setUpBeanMappingImpl(map, m, "javax.ejb.Timer");
      }

      if (serviceEndpoint != null)
      {
         Method[] m = serviceEndpoint.getMethods();
         setUpBeanMappingImpl(map, m, "java.rmi.Remote");
      }

      beanMapping = map;
   }

   /**
    * sets up marshalled invocation mappings
    * @throws Exception
    */

   protected void setupMarshalledInvocationMapping() throws Exception
   {
      // Create method mappings for container invoker
      if (homeInterface != null)
      {
         Method[] m = homeInterface.getMethods();
         for (int i = 0; i < m.length; i++)
         {
            marshalledInvocationMapping.put(new Long(MarshalledInvocation.calculateHash(m[i])), m[i]);
         }
      }

      if (remoteInterface != null)
      {
         Method[] m = remoteInterface.getMethods();
         for (int j = 0; j < m.length; j++)
         {
            marshalledInvocationMapping.put(new Long(MarshalledInvocation.calculateHash(m[j])), m[j]);
         }
      }
      // Get the getEJBObjectMethod
      Method getEJBObjectMethod =
              Class.forName("javax.ejb.Handle").getMethod("getEJBObject",
                      new Class[0]);

      // Hash it
      marshalledInvocationMapping.put(new Long(MarshalledInvocation.calculateHash(getEJBObjectMethod)), getEJBObjectMethod);
   }

   protected void checkCoherency() throws Exception
   {
      // Check clustering cohrency wrt metadata
      //
      if (metaData.isClustered())
      {
         boolean clusteredProxyFactoryFound = false;
         Iterator it = proxyFactories.keySet().iterator();
         while (it.hasNext())
         {
            String invokerBinding = (String) it.next();
            EJBProxyFactory ci = (EJBProxyFactory) proxyFactories.get(invokerBinding);
            if (ci instanceof org.jboss.proxy.ejb.ClusterProxyFactory)
               clusteredProxyFactoryFound = true;
         }

         if (!clusteredProxyFactoryFound)
         {
            log.warn("*** EJB '"
                    + this.metaData.getEjbName()
                    + "' deployed as CLUSTERED but not a single clustered-invoker is bound to container ***");
         }
      }
   }

   /** creates a new instance pool */
   protected void createInstancePool() throws Exception
   {

      // Try to register the instance pool as an MBean
      try
      {
         ObjectName containerName = super.getJmxName();
         Hashtable props = containerName.getKeyPropertyList();
         props.put("plugin", "pool");
         ObjectName poolName = new ObjectName(containerName.getDomain(), props);
         server.registerMBean(instancePool, poolName);
      }
      catch (Throwable t)
      {
         log.debug("Failed to register pool as mbean", t);
      }
      // Initialize pool
      instancePool.setContainer(this);
      instancePool.create();
   }

   /**
    * no instance cache per default
    */
   protected void createInstanceCache() throws Exception
   {
   }

   /** creates the invokers */
   protected void createInvokers() throws Exception
   {
      // Init container invoker
      for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext();)
      {
         String invokerBinding = (String) it.next();
         EJBProxyFactory ci = (EJBProxyFactory) proxyFactories.get(invokerBinding);
         ci.create();
      }
   }

   /** Initialize the interceptors by calling the chain */
   protected void createInterceptors() throws Exception
   {
      Interceptor in = interceptor;
      while (in != null)
      {
         in.setContainer(this);
         in.create();
         in = in.getNext();
      }
   }

   /**
    * no persistence manager per default
    */
   protected void createPersistenceManager() throws Exception
   {
   }

   protected void startService() throws Exception
   {
      // Associate thread with classloader
      ClassLoader oldCl = SecurityActions.getContextClassLoader();
      SecurityActions.setContextClassLoader(getClassLoader());
      pushENC();
      try
      {
         // Call default start
         super.startService();

         startInvokers();

         startInstanceCache();

         startInstancePool();

         startPersistenceManager();

         startInterceptors();
         
         // Restore persisted ejb timers
         restoreTimers();         
      }
      finally
      {
         popENC();
         // Reset classloader
         SecurityActions.setContextClassLoader(oldCl);
      }
   }

   /**
    * no persistence manager per default
    */
   protected void startPersistenceManager() throws Exception
   {
   }

   /**
    * no instance cache per default
    */
   protected void startInstanceCache() throws Exception
   {
   }

   /** Start container invokers */
   protected void startInvokers() throws Exception
   {
      for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext();)
      {
         String invokerBinding = (String) it.next();
         EJBProxyFactory ci = (EJBProxyFactory) proxyFactories.get(invokerBinding);
         ci.start();
      }
   }

   /** Start pool */
   protected void startInstancePool() throws Exception
   {
      instancePool.start();
   }

   /** Start all interceptors in the chain **/
   protected void startInterceptors() throws Exception
   {
      Interceptor in = interceptor;
      while (in != null)
      {
         in.start();
         in = in.getNext();
      }
   }

   protected void stopService() throws Exception
   {
      // Associate thread with classloader
      ClassLoader oldCl = SecurityActions.getContextClassLoader();
      SecurityActions.setContextClassLoader(getClassLoader());
      pushENC();
      try
      {
         // Call default stop
         super.stopService();

         stopInvokers();

         stopInstanceCache();

         stopInstancePool();

         stopPersistenceManager();

         stopInterceptors();
      }
      finally
      {
         popENC();
         // Reset classloader
         SecurityActions.setContextClassLoader(oldCl);
      }
   }

   /** Stop all interceptors in the chain */
   protected void stopInterceptors()
   {
      Interceptor in = interceptor;
      while (in != null)
      {
         in.stop();
         in = in.getNext();
      }
   }

   /** no persistence */
   protected void stopPersistenceManager()
   {
   }

   /** Stop pool */
   protected void stopInstancePool()
   {
      instancePool.stop();
   }

   /** no instance cache */
   protected void stopInstanceCache()
   {
   }

   /** Stop container invoker */
   protected void stopInvokers()
   {
      for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext();)
      {
         String invokerBinding = (String) it.next();
         EJBProxyFactory ci = (EJBProxyFactory) proxyFactories.get(invokerBinding);
         ci.stop();
      }
   }

   protected void destroyService() throws Exception
   {
      // Associate thread with classloader
      ClassLoader oldCl = SecurityActions.getContextClassLoader();
      SecurityActions.setContextClassLoader(getClassLoader());
      pushENC();
      try
      {
         destroyInvokers();

         destroyInstanceCache();

         destroyInstancePool();

         destroyPersistenceManager();

         destroyInterceptors();

         destroyMarshalledInvocationMapping();

         homeInterface = null;
         remoteInterface = null;
         serviceEndpoint = null;
         beanMapping.clear();
         
         // Call default destroy
         super.destroyService();
      }
      finally
      {
         popENC();
         // Reset classloader
         SecurityActions.setContextClassLoader(oldCl);
      }
   }

   protected void destroyMarshalledInvocationMapping()
   {
      MarshalledInvocation.removeHashes(homeInterface);
      MarshalledInvocation.removeHashes(remoteInterface);
   }

   protected void destroyInterceptors()
   {
      // Destroy all the interceptors in the chain
      Interceptor in = interceptor;
      while (in != null)
      {
         in.destroy();
         in.setContainer(null);
         in = in.getNext();
      }
   }

   protected void destroyPersistenceManager()
   {
   }

   protected void destroyInstancePool()
   {
      // Destroy pool
      instancePool.destroy();
      instancePool.setContainer(null);
      try
      {
         ObjectName containerName = super.getJmxName();
         Hashtable props = containerName.getKeyPropertyList();
         props.put("plugin", "pool");
         ObjectName poolName = new ObjectName(containerName.getDomain(), props);
         server.unregisterMBean(poolName);
      }
      catch (Throwable ignore)
      {
      }
   }

   protected void destroyInstanceCache()
   {
   }

   protected void destroyInvokers()
   {
      // Destroy container invoker
      for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext();)
      {
         String invokerBinding = (String) it.next();
         EJBProxyFactory ci = (EJBProxyFactory) proxyFactories.get(invokerBinding);
         ci.destroy();
      }
   }

   public Object internalInvokeHome(Invocation mi) throws Exception
   {
      Method method = mi.getMethod();
      if (method != null && method.getName().equals("remove"))
      {
         // Handle or primary key?
         Object arg = mi.getArguments()[0];
         if (arg instanceof Handle)
         {
            if (arg == null)
               throw new RemoteException("Null handle");
            Handle handle = (Handle) arg;
            EJBObject ejbObject = handle.getEJBObject();
            ejbObject.remove();
            return null;
         }
         else
            throw new RemoveException("EJBHome.remove(Object) not allowed for session beans");
      } 
      // Invoke through interceptors
      return getInterceptor().invokeHome(mi);
   }

   /**
    * This method does invocation interpositioning of tx and security,
    * retrieves the instance from an object table, and invokes the method
    * on the particular instance
    */
   public Object internalInvoke(Invocation mi) throws Exception
   { 
      // Invoke through interceptors
      return getInterceptor().invoke(mi);
   }

   // EJBObject implementation --------------------------------------

   /**
    * While the following methods are implemented in the client in the case
    * of JRMP we would need to implement them to fully support other transport
    * protocols
    *
    * @return  Always null
    */
   public Handle getHandle(Invocation mi) throws RemoteException
   {
      
      // TODO
      return null;
   }

   public Object getPrimaryKey(Invocation mi) throws RemoteException
   {
      return getPrimaryKey();
   }

   public Object getPrimaryKey() throws RemoteException
   {
      throw new RemoteException("Call to getPrimaryKey not allowed on session bean");
   }
   
   public EJBHome getEJBHome(Invocation mi) throws RemoteException
   {
      EJBProxyFactory ci = getProxyFactory();
      if (ci == null)
      {
         String msg = "No ProxyFactory, check for ProxyFactoryFinderInterceptor";
         throw new IllegalStateException(msg);
      }

      return (EJBHome) ci.getEJBHome();
   }

   /**
    * @return   Always false
    */
   public boolean isIdentical(Invocation mi) throws RemoteException
   {
      EJBProxyFactory ci = getProxyFactory();
      if (ci == null)
      {
         String msg = "No ProxyFactory, check for ProxyFactoryFinderInterceptor";
         throw new IllegalStateException(msg);
      }

      return ci.isIdentical(this, mi);
   }

   public EJBMetaData getEJBMetaDataHome(Invocation mi) throws RemoteException
   {
      return getEJBMetaDataHome();
   }
   
   public EJBMetaData getEJBMetaDataHome() throws RemoteException
   {
      EJBProxyFactory ci = getProxyFactory();
      if (ci == null)
      {
         String msg = "No ProxyFactory, check for ProxyFactoryFinderInterceptor";
         throw new IllegalStateException(msg);
      }

      return ci.getEJBMetaData();
   }
   
   public HomeHandle getHomeHandleHome(Invocation mi) throws RemoteException
   {
      return getHomeHandleHome();
   }
   
   public HomeHandle getHomeHandleHome() throws RemoteException
   {
      EJBProxyFactory ci = getProxyFactory();
      if (ci == null)
      {
         String msg = "No ProxyFactory, check for ProxyFactoryFinderInterceptor";
         throw new IllegalStateException(msg);
      }

      EJBHome home = (EJBHome) ci.getEJBHome();
      return home.getHomeHandle();
   }

   // Home interface implementation ---------------------------------

   // local object interface implementation

   public EJBLocalHome getEJBLocalHome(Invocation mi)
   {
      return localProxyFactory.getEJBLocalHome();
   }

   /**
    * needed for sub-inner-class access (old jdk compiler bug)
    * @return
    */
   protected Map getHomeMapping()
   {
      return homeMapping;
   }

   /**
    * needed for sub-inner-class access (old jdk compiler bug)
    * @return
    */
   protected Map getBeanMapping()
   {
      return beanMapping;
   }

}
