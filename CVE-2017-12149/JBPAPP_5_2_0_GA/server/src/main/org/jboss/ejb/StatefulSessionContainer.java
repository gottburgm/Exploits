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
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import java.rmi.RemoteException;
import java.security.Principal;

import javax.ejb.EJBObject;
import javax.ejb.EJBLocalObject;
import javax.ejb.RemoveException;
import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.management.ObjectName;

import org.jboss.ejb.plugins.StatefulSessionInstanceCache;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;
import org.jboss.security.SimplePrincipal;
import org.jboss.util.UnreachableStatementException;

/**
 * The container for <em>stateful</em> session beans.
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @version <tt>$Revision: 90278 $</tt>
 * @jmx:mbean extends="org.jboss.ejb.ContainerMBean"
 */

public class StatefulSessionContainer
   extends SessionContainer
   implements EJBProxyFactoryContainer, InstancePoolContainer, StatefulSessionContainerMBean
{
   /**
    * This is the persistence manager for this container
    */
   protected StatefulSessionPersistenceManager persistenceManager;

   /**
    * The instance cache.
    */
   protected InstanceCache instanceCache;
   protected Method getEJBObject;
   protected Method ejbObjectRemove;
   protected Method ejbLocalObjectRemove;

   public void setInstanceCache(InstanceCache ic)
   {
      this.instanceCache = ic;
   }

   public InstanceCache getInstanceCache()
   {
      return instanceCache;
   }

   public long getPassivatedCount()
   {
      if (! (instanceCache instanceof StatefulSessionInstanceCache))
         throw new UnsupportedOperationException();

      return ((StatefulSessionInstanceCache)instanceCache).getPassivatedCount();
   }

   public StatefulSessionPersistenceManager getPersistenceManager()
   {
      return persistenceManager;
   }

   public void setPersistenceManager(StatefulSessionPersistenceManager pm)
   {
      persistenceManager = pm;
   }

   /**
    * Override getMethodPermissions to work around the fact that stateful
    * session handles obtain their ejb objects by doing an invocation on the
    * container as a home method invocation using the Handle.getEJBObject
    * method.
    * @param m
    * @param iface
    * @return
    */
   public Set<Principal> getMethodPermissions(Method m, InvocationType iface)
   {
      if (m.equals(getEJBObject) == false)
         return super.getMethodPermissions(m, iface);

      Class[] sig = {};
      Set<String> permissions = getBeanMetaData().getMethodPermissions("create",
         sig, iface);
      //Convert this into Set of Principals
      Set<Principal> principalSet = new HashSet<Principal>();
      for(String perm: permissions)
      {
         principalSet.add(new SimplePrincipal(perm));
      }
      return principalSet;
   }

   // Container implementation --------------------------------------
   protected void createService() throws Exception
   {
      super.createService();
      // Get the Handle.getEJBObject method for permission checks
      try
      {
         getEJBObject = Handle.class.getMethod("getEJBObject", new Class[0]);
      }
      catch (Exception e)
      {
         log.warn("Failed to grant access to the Handle.getEJBObject method");
      }

      ejbObjectRemove = EJBObject.class.getMethod("remove", null);
      ejbLocalObjectRemove = EJBLocalObject.class.getMethod("remove", null);
   }

   /**
    * creates and registers the instance cache
    */
   protected void createInstanceCache() throws Exception
   {
      // Try to register the instance cache as an MBean
      try
      {
         ObjectName containerName = super.getJmxName();
         Hashtable props = containerName.getKeyPropertyList();
         props.put("plugin", "cache");
         ObjectName cacheName = new ObjectName(containerName.getDomain(), props);
         server.registerMBean(instanceCache, cacheName);
      }
      catch (Throwable t)
      {
         log.debug("Failed to register cache as mbean", t);
      }
      // Init instance cache
      instanceCache.setContainer(this);
      instanceCache.create();
   }

   /**
    * create persistence manager
    */
   protected void createPersistenceManager() throws Exception
   {
      persistenceManager.setContainer(this);
      persistenceManager.create();
   }

   /**
    * Start persistence
    */
   protected void startPersistenceManager() throws Exception
   {
      persistenceManager.start();
   }

   /**
    * Start instance cache
    */
   protected void startInstanceCache() throws Exception
   {
      instanceCache.start();
   }

   /**
    * Stop persistence
    */
   protected void stopPersistenceManager()
   {
      persistenceManager.stop();
   }

   /**
    * Stop instance cache
    */
   protected void stopInstanceCache()
   {
      instanceCache.stop();
   }

   protected void destroyPersistenceManager()
   {
      // Destroy persistence
      persistenceManager.destroy();
      persistenceManager.setContainer(null);
   }

   protected void destroyInstanceCache()
   {
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
      catch (Throwable ignore)
      {
      }
   }

   // EJBObject implementation --------------------------------------

   public void remove(Invocation mi)
      throws RemoteException, RemoveException
   {
      // if the session is removed already then let the user know they have a problem
      StatefulSessionEnterpriseContext ctx = (StatefulSessionEnterpriseContext) mi.getEnterpriseContext();
      if (ctx.getId() == null)
      {
         throw new RemoveException("SFSB has been removed already");
      }

      // Remove from storage
      try
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_REMOVE);
         getPersistenceManager().removeSession(ctx);
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }

      // We signify "removed" with a null id
      ctx.setId(null);
      removeCount++;
   }

   // Home interface implementation ---------------------------------

   private void createSession(final Method m,
      final Object[] args,
      final StatefulSessionEnterpriseContext ctx)
      throws Exception
   {
      // Create a new ID and set it
      Object id = getPersistenceManager().createId(ctx);
      log.debug("Created new session ID: " + id);
      ctx.setId(id);

      // Invoke ejbCreate<METHOD>()
      try
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_CREATE);

         // Build the ejbCreate<METHOD> from the home create<METHOD> sig
         String createName = m.getName();
         Object instance = ctx.getInstance();
         String ejbCreateName = "ejbC" + createName.substring(1);
         Method createMethod = instance.getClass().getMethod(ejbCreateName, m.getParameterTypes());
         log.debug("Using create method for session: " + createMethod);
         createMethod.invoke(instance, args);
         createCount++;
      }
      catch (IllegalAccessException e)
      {
         ctx.setId(null);

         throw new EJBException(e);
      }
      catch (InvocationTargetException e)
      {
         ctx.setId(null);

         Throwable t = e.getTargetException();
         if (t instanceof RuntimeException)
         {
            if (t instanceof EJBException)
               throw (EJBException) t;
            // Wrap runtime exceptions
            throw new EJBException((Exception) t);
         }
         else if (t instanceof Exception)
         {
            // Remote, Create, or custom app. exception
            throw (Exception) t;
         }
         else if (t instanceof Error)
         {
            throw (Error) t;
         }
         else
         {
            throw new org.jboss.util.UnexpectedThrowable(t);
         }
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }

      // call back to the PM to let it know that ejbCreate has been called with success
      getPersistenceManager().createdSession(ctx);

      // Insert in cache
      getInstanceCache().insert(ctx);

      // Create EJBObject
      if (getProxyFactory() != null)
         ctx.setEJBObject((EJBObject) getProxyFactory().getStatefulSessionEJBObject(id));

      // Create EJBLocalObject
      if (getLocalHomeClass() != null)
         ctx.setEJBLocalObject(getLocalProxyFactory().getStatefulSessionEJBLocalObject(id));
   }

   public EJBObject createHome(Invocation mi)
      throws Exception
   {
      StatefulSessionEnterpriseContext ctx = (StatefulSessionEnterpriseContext) mi.getEnterpriseContext();
      createSession(mi.getMethod(), mi.getArguments(), ctx);
      return ctx.getEJBObject();
   }


   // local home interface implementation

   /**
    * @throws Error Not yet implemented
    */
   public void removeLocalHome(Invocation mi)
      throws RemoteException, RemoveException
   {
      throw new UnreachableStatementException();
   }

   public EJBLocalObject createLocalHome(Invocation mi)
      throws Exception
   {
      StatefulSessionEnterpriseContext ctx = (StatefulSessionEnterpriseContext) mi.getEnterpriseContext();
      createSession(mi.getMethod(), mi.getArguments(), ctx);
      return ctx.getEJBLocalObject();
   }

   /**
    * A method for the getEJBObject from the handle
    */
   public EJBObject getEJBObject(Invocation mi) throws RemoteException
   {
      // All we need is an EJBObject for this Id, the first argument is the Id
      EJBProxyFactory ci = getProxyFactory();
      if (ci == null)
      {
         String msg = "No ProxyFactory, check for ProxyFactoryFinderInterceptor";
         throw new IllegalStateException(msg);
      }

      Object id = mi.getArguments()[0];
      if (id == null)
         throw new IllegalStateException("Cannot get a session interface with a null id");

      // Does the session still exist?
      InstanceCache cache = getInstanceCache();
      BeanLock lock = getLockManager().getLock(id);
      lock.sync();
      try
      {
         if (cache.get(id) == null)
            throw new RemoteException("Session no longer exists: " + id);
      }
      finally
      {
         lock.releaseSync();
         getLockManager().removeLockRef(id);
      }

      // Ok lets create the proxy
      return (EJBObject) ci.getStatefulSessionEJBObject(id);
   }


   // EJBHome implementation ----------------------------------------

   //
   // These are implemented in the local proxy
   //

   /**
    * @throws Error Not yet implemented
    */
   public void removeHome(Invocation mi)
      throws RemoteException, RemoveException
   {
      throw new Error("Not Yet Implemented");
   }

   public String getBeanTypeName()
   {
      return "StatefulSession";
   }

   // Private -------------------------------------------------------

   protected void setupHomeMapping() throws Exception
   {
      // Adrian Brock: This should go away when we don't support EJB1x
      boolean isEJB1x = metaData.getApplicationMetaData().isEJB1x();

      Map map = new HashMap();

      if (homeInterface != null)
      {

         Method[] m = homeInterface.getMethods();
         for (int i = 0; i < m.length; i++)
         {
            try
            {
               // Implemented by container
               if (isEJB1x == false && m[i].getName().startsWith("create"))
               {
                  map.put(m[i], getClass().getMethod("createHome",
                     new Class[]{Invocation.class}));
               }
               else
               {
                  map.put(m[i], getClass().getMethod(m[i].getName() + "Home",
                     new Class[]{Invocation.class}));
               }
            }
            catch (NoSuchMethodException e)
            {
               log.info(m[i].getName() + " in bean has not been mapped");
            }
         }
      }

      if (localHomeInterface != null)
      {
         Method[] m = localHomeInterface.getMethods();
         for (int i = 0; i < m.length; i++)
         {
            try
            {
               // Implemented by container
               if (isEJB1x == false && m[i].getName().startsWith("create"))
               {
                  map.put(m[i], getClass().getMethod("createLocalHome",
                     new Class[]{Invocation.class}));
               }
               else
               {
                  map.put(m[i], getClass().getMethod(m[i].getName() + "LocalHome",
                     new Class[]{Invocation.class}));
               }
            }
            catch (NoSuchMethodException e)
            {
               log.info(m[i].getName() + " in bean has not been mapped");
            }
         }
      }

      try
      {
         // Get getEJBObject from on Handle, first get the class
         Class handleClass = Class.forName("javax.ejb.Handle");

         //Get only the one called handle.getEJBObject
         Method getEJBObjectMethod = handleClass.getMethod("getEJBObject", new Class[0]);

         //Map it in the home stuff
         map.put(getEJBObjectMethod, getClass().getMethod("getEJBObject",
            new Class[]{Invocation.class}));
      }
      catch (NoSuchMethodException e)
      {
         log.debug("Couldn't find getEJBObject method on container");
      }

      homeMapping = map;
   }

   protected Interceptor createContainerInterceptor()
   {
      return new ContainerInterceptor();
   }

   /**
    * This is the last step before invocation - all interceptors are done
    */
   class ContainerInterceptor
      extends AbstractContainerInterceptor
   {
      public Object invokeHome(Invocation mi) throws Exception
      {
         boolean trace = log.isTraceEnabled();

         if (trace)
         {
            log.trace("HOMEMETHOD coming in ");
            log.trace("" + mi.getMethod());
            log.trace("HOMEMETHOD coming in hashcode" + mi.getMethod().hashCode());
            log.trace("HOMEMETHOD coming in classloader" + mi.getMethod().getDeclaringClass().getClassLoader().hashCode());
            log.trace("CONTAINS " + getHomeMapping().containsKey(mi.getMethod()));
         }

         Method miMethod = mi.getMethod();
         Method m = (Method) getHomeMapping().get(miMethod);
         if (m == null)
         {
            String msg = "Invalid invocation, check your deployment packaging"
               + ", method=" + miMethod;
            throw new EJBException(msg);
         }

         // Invoke and handle exceptions
         if (trace)
         {
            log.trace("HOMEMETHOD m " + m);
            java.util.Iterator iterator = getHomeMapping().keySet().iterator();
            while (iterator.hasNext())
            {
               Method me = (Method) iterator.next();

               if (me.getName().endsWith("create"))
               {
                  log.trace(me.toString());
                  log.trace("" + me.hashCode());
                  log.trace("" + me.getDeclaringClass().getClassLoader().hashCode());
                  log.trace("equals " + me.equals(mi.getMethod()) + " " + mi.getMethod().equals(me));
               }
            }
         }

         try
         {
            return mi.performCall(StatefulSessionContainer.this, m, new Object[]{mi});
         }
         catch (Exception e)
         {
            rethrow(e);
         }

         // We will never get this far, but the compiler does not know that
         throw new org.jboss.util.UnreachableStatementException();
      }

      public Object invoke(Invocation mi) throws Exception
      {
         // Get method
         Method miMethod = mi.getMethod();
         Method m = (Method) getBeanMapping().get(miMethod);
         if (m == null)
         {
            String msg = "Invalid invocation, check your deployment packaging"
               + ", method=" + miMethod;
            throw new EJBException(msg);
         }

         // wire the transaction on the context, this is how the instance remember the tx
         // Unlike Entity beans we can't do that in the previous interceptors (ordering)
         EnterpriseContext ctx = (EnterpriseContext) mi.getEnterpriseContext();
         if (ctx.getTransaction() == null)
            ctx.setTransaction(mi.getTransaction());
         else if(ejbObjectRemove.equals(miMethod) || ejbLocalObjectRemove.equals(miMethod))
         {
            throw new RemoveException("An attempt to remove a session " +
               "object while the object is in a transaction " +
               "(EJB2.1, 7.6.4 Restrictions for Transactions): ejb-name=" +
               metaData.getEjbName() + ", method=" + mi.getMethod() + ", tx=" + ctx.getTransaction());
         }

         // Select instance to invoke (container or bean)
         if (m.getDeclaringClass().equals(StatefulSessionContainer.class)
            || m.getDeclaringClass().equals(SessionContainer.class))
         {
            // Invoke and handle exceptions
            try
            {
               return mi.performCall(StatefulSessionContainer.this, m, new Object[]{mi});
            }
            catch (Exception e)
            {
               rethrow(e);
            }
         }
         else
         {
            // Invoke and handle exceptions
            try
            {
               Object bean = ctx.getInstance();
               return mi.performCall(bean, m, mi.getArguments());
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
