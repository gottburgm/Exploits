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

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.RemoveException;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.management.ObjectName;
import javax.transaction.Transaction;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.monitor.StatisticsProvider;
import org.jboss.util.collection.SerializableEnumeration;

/**
 * This is a Container for EntityBeans (both BMP and CMP).
 *
 * @see Container
 * @see EntityEnterpriseContext
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:anil.saldhana@redhat.com">Anil Saldhana</a>
 * @version $Revision: 89854 $
 *
 * @jmx.mbean extends="org.jboss.ejb.ContainerMBean"
 */
public class EntityContainer
   extends Container
   implements EJBProxyFactoryContainer, InstancePoolContainer,
      EntityContainerMBean
{
   /**
    * These are the mappings between the home interface methods and the
    * container methods.
    */
   protected Map homeMapping = new HashMap();

   /**
    * These are the mappings between the remote/local interface methods and the
    * bean methods.
    */
   protected Map beanMapping = new HashMap();

   /** This is the persistence manager for this container */
   protected EntityPersistenceManager persistenceManager;

   /** This is the instance cache for this container */
   protected InstanceCache instanceCache;

   /** This is the instancepool that is to be used */
   protected InstancePool instancePool;

   /**
    * This is the first interceptor in the chain. The last interceptor must
    * be provided by the container itself.
    */
   protected Interceptor interceptor;

   /**
    * <code>readOnly</code> determines if state can be written to resource manager.
    */
   protected boolean readOnly = false;

   /**
    * This provides a way to find the entities that are part of a given
    * transaction EntitySynchronizationInterceptor and InstanceSynchronization
    * manage this instance.
    */
   protected static GlobalTxEntityMap globalTxEntityMap = new GlobalTxEntityMap();

   public static GlobalTxEntityMap getGlobalTxEntityMap()
   {
      return globalTxEntityMap;
   }

   /**
    * Stores all of the entities associated with the specified transaction.
    * As per the spec 9.6.4, entities must be synchronized with the datastore
    * when an ejbFind<METHOD> is called.
    * Also, all entities within entire transaction should be synchronized before
    * a remove, otherwise there may be problems with 'cascade delete'.
    *
    * @param tx the transaction that associated entites will be stored
    */
   public static void synchronizeEntitiesWithinTransaction(Transaction tx)
   {
      // If there is no transaction, there is nothing to synchronize.
      if(tx != null)
      {
         getGlobalTxEntityMap().synchronizeEntities(tx);
      }
   }

   // Public --------------------------------------------------------

   public boolean isReadOnly()
   {
      return readOnly;
   }

   public LocalProxyFactory getLocalProxyFactory()
   {
      return localProxyFactory;
   }

   public void setInstancePool(InstancePool ip)
   {
      if (ip == null)
         throw new IllegalArgumentException("Null pool");

      this.instancePool = ip;
   }

   public InstancePool getInstancePool()
   {
      return instancePool;
   }

   public void setInstanceCache(InstanceCache ic)
   {
      if (ic == null)
         throw new IllegalArgumentException("Null cache");

      this.instanceCache = ic;
   }

   public InstanceCache getInstanceCache()
   {
      return instanceCache;
   }

   public EntityPersistenceManager getPersistenceManager()
   {
      return persistenceManager;
   }

   public void setPersistenceManager(EntityPersistenceManager pm)
   {
      if (pm == null)
         throw new IllegalArgumentException("Null persistence manager");

      persistenceManager = pm;
   }

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

   public Interceptor getInterceptor()
   {
      return interceptor;
   }

   public Class getHomeClass()
   {
      return homeInterface;
   }

   public Class getRemoteClass()
   {
      return remoteInterface;
   }

   /**
    * Returns a new instance of the bean class or a subclass of the bean class.
    * If this is 1.x cmp, simply return a new instance of the bean class.
    * If this is 2.x cmp, return a subclass that provides an implementation
    * of the abstract accessors.
    *
    * @see java.lang.Class#newInstance
    *
    * @return   The new instance.
    */
   public Object createBeanClassInstance() throws Exception {
      return persistenceManager.createBeanClassInstance();
   }

   // Container implementation --------------------------------------

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

         // Call default init
         super.createService();

         // Make some additional validity checks with regards to the container configuration
         checkCoherency ();

         // Map the bean methods
         setupBeanMapping();

         // Map the home methods
         setupHomeMapping();

         // Map the interfaces to Long
         setupMarshalledInvocationMapping();

         // Try to register the instance pool as an MBean
         try
         {
            ObjectName containerName = super.getJmxName();
            Hashtable props = containerName.getKeyPropertyList();
            props.put("plugin", "pool");
            ObjectName poolName = new ObjectName(containerName.getDomain(), props);
            server.registerMBean(instancePool, poolName);
         }
         catch(Throwable t)
         {
            log.debug("Failed to register cache as mbean", t);
         }
         // Initialize pool
         instancePool.setContainer(this);
         instancePool.create();

         for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext(); )
         {
            String invokerBinding = (String)it.next();
            EJBProxyFactory ci = (EJBProxyFactory)proxyFactories.get(invokerBinding);
            ci.create();
         }

         // Try to register the instance cache as an MBean
         try
         {
            ObjectName containerName = super.getJmxName();
            Hashtable props = containerName.getKeyPropertyList();
            props.put("plugin", "cache");
            ObjectName cacheName = new ObjectName(containerName.getDomain(), props);
            server.registerMBean(instanceCache, cacheName);
         }
         catch(Throwable t)
         {
            log.debug("Failed to register cache as mbean", t);
         }
         // Init instance cache
         instanceCache.setContainer(this);
         instanceCache.create();

         // Init persistence
         persistenceManager.setContainer(this);
         persistenceManager.create();

         // Initialize the interceptor by calling the chain
         Interceptor in = interceptor;
         while (in != null)
         {
            in.setContainer(this);
            in.create();
            in = in.getNext();
         }
         readOnly = ((EntityMetaData)metaData).isReadOnly();
      }
      finally
      {
         popENC();
         // Reset classloader
         SecurityActions.setContextClassLoader(oldCl);
      }
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

         // Start container invokers
         for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext(); )
         {
            String invokerBinding = (String)it.next();
            EJBProxyFactory ci = (EJBProxyFactory)proxyFactories.get(invokerBinding);
            ci.start();
         }

         // Start instance cache
         instanceCache.start();

         // Start the instance pool
         instancePool.start();

         Interceptor i = interceptor;
         while(i != null)
         {
            i.start();
            i = i.getNext();
         }

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

   protected void stopService() throws Exception
   {
      // Associate thread with classloader
      ClassLoader oldCl = SecurityActions.getContextClassLoader();
      SecurityActions.setContextClassLoader(getClassLoader());
      pushENC();
      try
      {
         //Stop items in reverse order from start
         //This assures that CachedConnectionInterceptor will get removed
         //from in between this and the pm before the pm is stopped.
         // Stop all interceptors in the chain
         Interceptor in = interceptor;
         while (in != null)
         {
            in.stop();
            in = in.getNext();
         }

         // Stop the instance pool
         instancePool.stop();

         // Stop persistence
         persistenceManager.stop();

         // Stop instance cache
         instanceCache.stop();

         // Stop container invoker
         for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext(); )
         {
            String invokerBinding = (String)it.next();
            EJBProxyFactory ci = (EJBProxyFactory)proxyFactories.get(invokerBinding);
            ci.stop();
         }

         // Call default stop
         super.stopService();
      }
      finally
      {
         popENC();
         // Reset classloader
         SecurityActions.setContextClassLoader(oldCl);
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
         // Destroy container invoker
         for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext(); )
         {
            String invokerBinding = (String)it.next();
            EJBProxyFactory ci = (EJBProxyFactory)proxyFactories.get(invokerBinding);
            ci.destroy();
         }

         // Destroy instance cache
         instanceCache.destroy();
         instanceCache.setContainer(null);
         try
         {
            ObjectName containerName = super.getJmxName();
            Hashtable props = containerName.getKeyPropertyList();
            props.put("plugin", "cache");
            ObjectName cacheName = new ObjectName(containerName.getDomain(), props);
            server.unregisterMBean(cacheName);
         }
         catch(Throwable ignore)
         {
         }

         // Destroy persistence
         persistenceManager.destroy();
         persistenceManager.setContainer(null);

         // Destroy the pool
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
         catch(Throwable ignore)
         {
         }

         // Destroy all the interceptors in the chain
         Interceptor in = interceptor;
         while (in != null)
         {
            in.destroy();
            in.setContainer(null);
            in = in.getNext();
         }

         MarshalledInvocation.removeHashes(homeInterface);
         MarshalledInvocation.removeHashes(remoteInterface);

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

   public Object internalInvokeHome(Invocation mi) throws Exception
   {
      Method method = mi.getMethod();
      if (method != null && method.getName().equals("remove"))
      {
         // Map to EJBHome.remove(Object) to EJBObject.remove()
         InvocationType type = mi.getType();
         if (type == InvocationType.HOME)
            mi.setType(InvocationType.REMOTE);
         else if (type == InvocationType.LOCALHOME)
            mi.setType(InvocationType.LOCAL);
         mi.setMethod(EJBOBJECT_REMOVE);

         // Handle or primary key?
         Object arg = mi.getArguments()[0];
         if (arg instanceof Handle)
         {
            if (arg == null)
               throw new RemoteException("Null handle");
            Handle handle = (Handle) arg;
            EJBObject ejbObject = handle.getEJBObject();
            mi.setId(ejbObject.getPrimaryKey());
         }
         else
            mi.setId(arg);

         mi.setArguments(new Object[0]);
         return getInterceptor().invoke(mi);
      }
      // Invoke through interceptors
      return getInterceptor().invokeHome(mi);
   }

   public Object internalInvoke(Invocation mi) throws Exception
   {
      // Invoke through interceptors
      return getInterceptor().invoke(mi);
   }

   // EJBObject implementation --------------------------------------

   public void remove(Invocation mi)
      throws RemoteException, RemoveException
   {
      // synchronize entities with the datastore before the bean is removed
      // this will write queued updates so datastore will be consistent before removal
      Transaction tx = mi.getTransaction();
      if (!getBeanMetaData().getContainerConfiguration().getSyncOnCommitOnly())
         synchronizeEntitiesWithinTransaction(tx);

      // Get the persistence manager to do the dirty work
      EntityEnterpriseContext ctx = (EntityEnterpriseContext)mi.getEnterpriseContext();
      getPersistenceManager().removeEntity(ctx);

      final Object pk = ctx.getId();
      AccessController.doPrivileged(new PrivilegedAction<Object>()
      {
         public Object run()
         {
            removeTimerService(pk);
            return null;
         }
      });

      // We signify "removed" with a null id
      // There is no need to synchronize on the context since all the threads reaching here have
      // gone through the InstanceInterceptor so the instance is locked and we only have one thread
      // the case of reentrant threads is unclear (would you want to delete an instance in reentrancy)
      ctx.setId(null);
      removeCount++;
   }

   /**
    * @throws Error    Not yet implemented.
    */
   public Handle getHandle(Invocation mi)
      throws RemoteException
   {
      // TODO
      throw new Error("Not yet implemented");
   }

   public Object getPrimaryKey(Invocation mi)
      throws RemoteException
   {
      return mi.getId();
   }

   /**
    * @throws IllegalStateException     If container invoker is null.
    */
   public EJBHome getEJBHome(Invocation mi)
      throws RemoteException
   {
      EJBProxyFactory ci = getProxyFactory();
      if (ci == null)
      {
         String msg = "No ProxyFactory, check for ProxyFactoryFinderInterceptor";
         throw new IllegalStateException(msg);
      }
      return (EJBHome) ci.getEJBHome();
   }

   public boolean isIdentical(Invocation mi)
      throws RemoteException
   {
      EJBProxyFactory ci = getProxyFactory();
      if (ci == null)
      {
         String msg = "No ProxyFactory, check for ProxyFactoryFinderInterceptor";
         throw new IllegalStateException(msg);
      }

      return ci.isIdentical(this, mi);
   }

   /**
    * MF FIXME these are implemented on the client
    */
   public EJBLocalHome getEJBLocalHome(Invocation mi)
   {
      return localProxyFactory.getEJBLocalHome();
   }

   /**
    * @throws Error    Not yet implemented.
    */
   public void removeLocalHome(Invocation mi)
      throws RemoteException, RemoveException
   {
      throw new Error("Not Yet Implemented");
   }

   /**
    * Local home interface implementation
    */
   public EJBLocalObject createLocalHome(Invocation mi)
      throws Exception
   {
      // The persistence manager takes care of the wiring and creating the EJBLocalObject
      final EntityEnterpriseContext ctx = (EntityEnterpriseContext)mi.getEnterpriseContext();
      getPersistenceManager().createEntity(mi.getMethod(), mi.getArguments(), ctx);

      // The context implicitely carries the EJBObject
      createCount++;
      return localProxyFactory.getEntityEJBLocalObject(ctx.getId(), true);
   }

   /**
    * Delegates to the persistence manager postCreateEntityMethod.
    */
   public void postCreateLocalHome(Invocation mi) throws Exception
   {
      // The persistence manager takes care of the post create step
      getPersistenceManager().postCreateEntity(mi.getMethod(),mi.getArguments(),
         (EntityEnterpriseContext) mi.getEnterpriseContext());
   }

   public Object findLocal(Invocation mi)
      throws Exception
   {
      Method method = mi.getMethod();
      Object[] args = mi.getArguments();
      EntityEnterpriseContext instance = (EntityEnterpriseContext)mi.getEnterpriseContext();

      boolean syncOnCommitOnly = metaData.getContainerConfiguration().getSyncOnCommitOnly();
      Transaction tx = mi.getTransaction();

      Class returnType = method.getReturnType();
      if (Collection.class.isAssignableFrom(returnType) || returnType == Enumeration.class)
      {
         // as per the spec 9.6.4, entities must be synchronized with the datastore when an ejbFind<METHOD> is called.
         if (!syncOnCommitOnly)
         {
            synchronizeEntitiesWithinTransaction(tx);
         }

         // Iterator finder
         Collection c = getPersistenceManager().findEntities(method, args, instance, localProxyFactory);

         // BMP entity finder methods are allowed to return java.util.Enumeration.
         if (returnType == Enumeration.class)
         {
            return java.util.Collections.enumeration(c);
         }
         else
         {
            return c;
         }
      }
      else
      {
         return findSingleObject(tx, method, args, instance, localProxyFactory);
      }
   }

   // Home interface implementation ---------------------------------

   /**
    * This methods finds the target instances by delegating to the persistence
    * manager It then manufactures EJBObject for all the involved instances
    * found.
    */
   public Object find(Invocation mi) throws Exception
   {
      EJBProxyFactory ci = getProxyFactory();
      if (ci == null)
      {
         String msg = "No ProxyFactory, check for ProxyFactoryFinderInterceptor";
         throw new IllegalStateException(msg);
      }

      Method method = mi.getMethod();
      Object[] args = mi.getArguments();
      EntityEnterpriseContext instance = (EntityEnterpriseContext)mi.getEnterpriseContext();

      boolean syncOnCommitOnly = metaData.getContainerConfiguration().getSyncOnCommitOnly();
      Transaction tx = mi.getTransaction();

      Class returnType = method.getReturnType();
      if (Collection.class.isAssignableFrom(returnType) || returnType == Enumeration.class)
      {
         // as per the spec 9.6.4, entities must be synchronized with the datastore when an ejbFind<METHOD> is called.
         if (!syncOnCommitOnly)
         {
            synchronizeEntitiesWithinTransaction(tx);
         }

         // Iterator finder
         Collection c = getPersistenceManager().findEntities(method, args, instance, ci);

         // BMP entity finder methods are allowed to return java.util.Enumeration.
         // We need a serializable Enumeration, so we can't use Collections.enumeration()
         if (returnType == Enumeration.class)
         {
            return new SerializableEnumeration(c);
         }
         else
         {
            return c;
         }
      }
      else
      {
         return findSingleObject(tx, method, args, instance, ci);
      }
   }

   /**
    * Invokes ejbStore method on the instance
    * @param ctx  the instance to invoke ejbStore on
    * @throws Exception
    */
   public void invokeEjbStore(EntityEnterpriseContext ctx) throws Exception
   {
      if (ctx.getId() != null)
      {
         final EntityPersistenceManager pm = getPersistenceManager();
         pm.invokeEjbStore(ctx);
      }
   }

   /**
    * For CMP actually stores the instance
    */
   public void storeEntity(EntityEnterpriseContext ctx) throws Exception
   {
      if (ctx.getId() != null)
      {
         final EntityPersistenceManager pm = getPersistenceManager();
         if(pm.isStoreRequired(ctx))
         {
            pm.storeEntity(ctx);
         }
      }
   }

   /**
    * Delegates to the persistence manager postCreateEntityMethod.
    */
   public void postCreateHome(Invocation mi) throws Exception
   {
      // The persistence manager takes care of the post create step
      getPersistenceManager().postCreateEntity(mi.getMethod(),mi.getArguments(),
         (EntityEnterpriseContext) mi.getEnterpriseContext());
   }

   /**
    * This method takes care of the wiring of the "EJBObject" trio
    * (target, context, proxy).  It delegates to the persistence manager.
    */
   public EJBObject createHome(Invocation mi)
      throws Exception
   {
      // The persistence manager takes care of the wiring and creating the EJBObject
      getPersistenceManager().createEntity(mi.getMethod(),mi.getArguments(),
         (EntityEnterpriseContext) mi.getEnterpriseContext());

      // The context implicitely carries the EJBObject
      createCount++;
      return ((EntityEnterpriseContext)mi.getEnterpriseContext()).getEJBObject();
   }

   /**
    * A method for the getEJBObject from the handle
    */
   public EJBObject getEJBObject(Invocation mi)
      throws RemoteException
   {
      EJBProxyFactory ci = getProxyFactory();
      if (ci == null)
      {
         String msg = "No ProxyFactory, check for ProxyFactoryFinderInterceptor";
         throw new IllegalStateException(msg);
      }
      // All we need is an EJBObject for this Id;
      return (EJBObject)ci.getEntityEJBObject(((EntityCache) instanceCache).createCacheKey(mi.getId()));
   }

   // EJBHome implementation ----------------------------------------

   /**
    * @throws Error    Not yet implemented.
    */
   public void removeHome(Invocation mi)
      throws RemoteException, RemoveException
   {
      throw new Error("Not yet implemented");
   }

   public EJBMetaData getEJBMetaDataHome(Invocation mi)
      throws RemoteException
   {
      EJBProxyFactory ci = getProxyFactory();
      if (ci == null)
      {
         String msg = "No ProxyFactory, check for ProxyFactoryFinderInterceptor";
         throw new IllegalStateException(msg);
      }
      return ci.getEJBMetaData();
   }

   /**
    * @throws Error    Not yet implemented.
    */
   public HomeHandle getHomeHandleHome(Invocation mi)
      throws RemoteException
   {
      // TODO
      throw new Error("Not yet implemented");
   }

   /**
    * @jmx.managed-attribute
    * @return the current cache size
    */
   public long getCacheSize()
   {
      return instanceCache.getCacheSize();
   }

   /** Flush the cache
    * @jmx.managed-operation
    */
   public void flushCache()
   {
      instanceCache.flush();
   }

   // StatisticsProvider implementation ------------------------------------

   public Map retrieveStatistic()
   {
      // Loop through all Interceptors and add statistics
      Map lStatistics = new HashMap();
      StatisticsProvider lProvider = (StatisticsProvider) getPersistenceManager();
      lStatistics.putAll( lProvider.retrieveStatistic() );
      lProvider = (StatisticsProvider) getInstancePool();
      lStatistics.putAll( lProvider.retrieveStatistic() );
      return lStatistics;
   }

   public void resetStatistic()
   {
   }

   public String getBeanTypeName()
   {
      return "Entity";
   }

   // Private -------------------------------------------------------

   private void setupHomeMappingImpl(Method[] m,
                                     String finderName,
                                     String append)
      throws Exception
   {
      // Adrian Brock: This should go away when we don't support EJB1x
      boolean isEJB1x = metaData.getApplicationMetaData().isEJB1x();

      for (int i = 0; i < m.length; i++)
      {
         String methodName = m[i].getName();
         try
         {
            try // Try home method
            {
               String ejbHomeMethodName = "ejbHome" + methodName.substring(0,1).toUpperCase() + methodName.substring(1);
               homeMapping.put(m[i], beanClass.getMethod(ejbHomeMethodName, m[i].getParameterTypes()));

               continue;
            }
            catch (NoSuchMethodException ignore) {} // just go on with other types of methods


            // Implemented by container (in both cases)
            if (methodName.startsWith("find"))
            {
               homeMapping.put(m[i], this.getClass().getMethod(finderName, new Class[] { Invocation.class }));
            }
            else if (methodName.equals("create") ||
                  (isEJB1x == false && methodName.startsWith("create")))
            {
               homeMapping.put(m[i], this.getClass().getMethod("create"+append, new Class[] { Invocation.class }));
               beanMapping.put(m[i], this.getClass().getMethod("postCreate"+append, new Class[] { Invocation.class }));
            }
            else
            {
               homeMapping.put(m[i], this.getClass().getMethod(methodName+append, new Class[] { Invocation.class }));
            }
         }
         catch (NoSuchMethodException e)
         {
            throw new NoSuchMethodException("Could not find matching method for "+m[i]);
         }
      }
   }

   protected void setupHomeMapping() throws Exception
   {
      try {
         if (homeInterface != null)
         {
            Method[] m = homeInterface.getMethods();
            setupHomeMappingImpl( m, "find", "Home" );
         }
         if (localHomeInterface != null)
         {
            Method[] m = localHomeInterface.getMethods();
            setupHomeMappingImpl( m, "findLocal", "LocalHome" );
         }

         // Special methods

         // Get the One on Handle (getEJBObject), get the class
         Class handleClass = Class.forName("javax.ejb.Handle");

         // Get the methods (there is only one)
         Method[] handleMethods = handleClass.getMethods();

         //Just to make sure let's iterate
         for (int j=0; j<handleMethods.length ;j++)
         {
            //Get only the one called handle.getEJBObject
            if (handleMethods[j].getName().equals("getEJBObject"))
            {
               //Map it in the home stuff
               homeMapping.put(handleMethods[j],
                               this.getClass().getMethod("getEJBObject",
                                                         new Class[] {Invocation.class}));
            }
         }
      }
      catch (Exception e)
      {
         // ditch the half built mappings
         homeMapping.clear();
         beanMapping.clear();

         throw e;
      }
   }

   private void setupBeanMappingImpl( Method[] m, String intfName )
      throws Exception
   {
      for (int i = 0; i < m.length; i++)
      {
         if (!m[i].getDeclaringClass().getName().equals(intfName))
         {
            // Implemented by bean
            beanMapping.put(m[i], beanClass.getMethod(m[i].getName(), m[i].getParameterTypes()));
         }
         else
         {
            // Implemented by container
            beanMapping.put(m[i], getClass().getMethod(m[i].getName(),
                                                       new Class[] { Invocation.class }));
         }
      }
   }

   protected void setupBeanMapping() throws Exception
   {
      try {
         if (remoteInterface != null)
         {
            Method[] m = remoteInterface.getMethods();
            setupBeanMappingImpl( m, "javax.ejb.EJBObject" );
         }
         if (localInterface != null)
         {
            Method[] m = localInterface.getMethods();
            setupBeanMappingImpl( m, "javax.ejb.EJBLocalObject" );
         }
         if( TimedObject.class.isAssignableFrom( beanClass ) ) {
             // Map ejbTimeout
             beanMapping.put(
                TimedObject.class.getMethod( "ejbTimeout", new Class[] { Timer.class } ),
                beanClass.getMethod( "ejbTimeout", new Class[] { Timer.class } )
             );
         }
      }
      catch (Exception e)
      {
         // ditch the half built mappings
         homeMapping.clear();
         beanMapping.clear();

         throw e;
      }
   }

   protected void setupMarshalledInvocationMapping() throws Exception
   {
      // Create method mappings for container invoker
      if (homeInterface != null)
      {
         Method [] m = homeInterface.getMethods();
         for (int i = 0 ; i<m.length ; i++)
         {
            marshalledInvocationMapping.put( new Long(MarshalledInvocation.calculateHash(m[i])), m[i]);
         }
      }

      if (remoteInterface != null)
      {
         Method [] m = remoteInterface.getMethods();
         for (int j = 0 ; j<m.length ; j++)
         {
            marshalledInvocationMapping.put( new Long(MarshalledInvocation.calculateHash(m[j])), m[j]);
         }
      }

      // Get the getEJBObjectMethod
      Method getEJBObjectMethod = Class.forName("javax.ejb.Handle").getMethod("getEJBObject", new Class[0]);

      // Hash it
      marshalledInvocationMapping.put(new Long(MarshalledInvocation.calculateHash(getEJBObjectMethod)),getEJBObjectMethod);
   }

   Interceptor createContainerInterceptor()
   {
      return new ContainerInterceptor();
   }

   protected void checkCoherency () throws Exception
   {
      // Check clustering cohrency wrt metadata
      //
      if (metaData.isClustered())
      {
         boolean clusteredProxyFactoryFound = false;
         for (Iterator it = proxyFactories.keySet().iterator(); it.hasNext(); )
         {
            String invokerBinding = (String)it.next();
            EJBProxyFactory ci = (EJBProxyFactory)proxyFactories.get(invokerBinding);
            if (ci instanceof org.jboss.proxy.ejb.ClusterProxyFactory)
               clusteredProxyFactoryFound = true;
         }

         if (!clusteredProxyFactoryFound)
         {
            log.warn("*** EJB '" + this.metaData.getEjbName() + "' deployed as CLUSTERED but not a single clustered-invoker is bound to container ***");
         }
      }
   }

   private Object findSingleObject(Transaction tx,
                                   Method method,
                                   Object[] args,
                                   EntityEnterpriseContext instance,
                                   GenericEntityObjectFactory factory)
      throws Exception
   {
      if(method.getName().equals("findByPrimaryKey"))
      {
         if(args[0] == null)
            throw new IllegalArgumentException("findByPrimaryKey called with null argument.");

         if(metaData.getContainerConfiguration().getCommitOption() != ConfigurationMetaData.B_COMMIT_OPTION)
         {
            Object key = instance.getCacheKey();
            if(key == null)
            {
               key = ((EntityCache)instanceCache).createCacheKey(args[0]);
            }

            if(instanceCache.isActive(key))
            {
               return factory.getEntityEJBObject(key);
            }
         }
      }
      else if(!metaData.getContainerConfiguration().getSyncOnCommitOnly())
      {
         EntityContainer.synchronizeEntitiesWithinTransaction(tx);
      }

      return getPersistenceManager().findEntity(method, args, instance, factory);
   }

   // Inner classes -------------------------------------------------

   /**
    * This is the last step before invocation - all interceptors are done
    */
   class ContainerInterceptor
      extends AbstractContainerInterceptor
   {
      public Object invokeHome(Invocation mi) throws Exception
      {
         // Invoke and handle exceptions
         Method miMethod = mi.getMethod();
         Method m = (Method) homeMapping.get(miMethod);
         if( m == null )
         {
            String msg = "Invalid invocation, check your deployment packaging"
               +", method="+miMethod;
            throw new EJBException(msg);
         }

         if (m.getDeclaringClass().equals(EntityContainer.class))
         {
            try
            {
               return mi.performCall(EntityContainer.this, m, new Object[] { mi });
            }
            catch (Exception e)
            {
               rethrow(e);
            }
         }
         else // Home method
         {
            EnterpriseContext ctx = (EnterpriseContext) mi.getEnterpriseContext();
            try
            {
               AllowedOperationsAssociation.pushInMethodFlag(AllowedOperationsAssociation.IN_EJB_HOME);
               return mi.performCall(ctx.getInstance(), m, mi.getArguments());
            }
            catch (Exception e)
            {
               rethrow(e);
            }
            finally{
               AllowedOperationsAssociation.popInMethodFlag();
            }
         }

         // We will never get this far, but the compiler does not know that
         throw new org.jboss.util.UnreachableStatementException();
      }

      public Object invoke(Invocation mi) throws Exception
      {
         // Get method
         Method miMethod = mi.getMethod();
         Method m = (Method) beanMapping.get(miMethod);
         if( m == null )
         {
            String msg = "Invalid invocation, check your deployment packaging"
               +", method="+miMethod;
            throw new EJBException(msg);
         }

         // Select instance to invoke (container or bean)
         if (m.getDeclaringClass().equals(EntityContainer.class))
         {
            // Invoke container
            try
            {
               return mi.performCall(EntityContainer.this, m, new Object[]{ mi });
            }
            catch (Exception e)
            {
               rethrow(e);
            }
         }
         else
         {
            // Invoke bean instance
            try
            {
               EnterpriseContext ctx = (EnterpriseContext) mi.getEnterpriseContext();
               Object instance = ctx.getInstance();

               return mi.performCall(instance, m, mi.getArguments());
            }
            catch (Exception e)
            {
               rethrow(e);
            }
         }

         // We will never get this far, but the compiler does not know that
         throw new org.jboss.util.UnreachableStatementException();
      }
   }
}
