/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.connectionmanager;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.ejb.EJBException;
import javax.ejb.RemoveException;
import javax.management.MBeanServer;
import javax.resource.ResourceException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.GenericEntityObjectFactory;
import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;
import org.jboss.logging.Logger;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * CachedConnectionInterceptor
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:E.Guib@ceyoniq.com">Erwin Guib</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 76129 $
 */
public class CachedConnectionInterceptor extends AbstractInterceptor implements EntityPersistenceManager
{
   private final CachedConnectionManager ccm;

   private final Logger log = Logger.getLogger(getClass());

   private Container container;

   private EntityPersistenceManager pm;

   // contains the JNDI names of unshareable resources
   private Set unsharableResources = new HashSet();

   public CachedConnectionInterceptor() throws Exception
   {
      try
      {
         MBeanServer server = MBeanServerLocator.locateJBoss();
         ccm = (CachedConnectionManager) server.getAttribute(CachedConnectionManagerMBean.OBJECT_NAME, "Instance");
      }
      catch (Exception e)
      {
         JMXExceptionDecoder.rethrow(e);
         throw e;
      }
   }

   @SuppressWarnings("deprecation")
   public void start() throws Exception
   {
      log.debug("start called in CachedConnectionInterceptor");
      if (container == null)
      {
         log.warn("container is null, can't steal persistence manager");
         return;
      }
      if (container instanceof EntityContainer)
      {
         EntityContainer ec = (EntityContainer) container;

         if (ec.getPersistenceManager() == null)
         {
            log.info("no persistence manager in container!");
            return;
         }
         if (ec.getPersistenceManager() == this)
         {
            log.info(" persistence manager in container already set!");
            return;
         }
         pm = ec.getPersistenceManager();
         ec.setPersistenceManager(this);
      }

      // get the JNDI names for all resources that are referenced "Unshareable"
      org.jboss.metadata.BeanMetaData bmd = container.getBeanMetaData();
      org.jboss.metadata.ApplicationMetaData appMetaData = bmd.getApplicationMetaData();
      org.jboss.metadata.ResourceRefMetaData resRefMetaData;
      String jndiName;

      for (Iterator iter = bmd.getResourceReferences(); iter.hasNext();)
      {
         resRefMetaData = (org.jboss.metadata.ResourceRefMetaData) iter.next();
         jndiName = resRefMetaData.getJndiName();
         if (jndiName == null)
         {
            jndiName = appMetaData.getResourceByName(resRefMetaData.getResourceName());
         }
         if (jndiName != null && resRefMetaData.isShareable() == false)
         {
            int i = jndiName.indexOf(':');
            if (jndiName.charAt(i + 1) == '/')
            {
               i++;
            }
            unsharableResources.add(jndiName.substring(i + 1));
         }
      }

   }

   public void stop()
   {
      if (container != null && pm != null && ((EntityContainer) container).getPersistenceManager() == this)
      {
         ((EntityContainer) container).setPersistenceManager(pm);
         pm = null;
      }
      unsharableResources.clear();
   }

   public Object invoke(Invocation mi) throws Exception
   {
      Object key = ((EnterpriseContext) mi.getEnterpriseContext()).getInstance();
      try
      {
         ccm.pushMetaAwareObject(key, unsharableResources);
         try
         {
            return getNext().invoke(mi);
         }
         finally
         {
            ccm.popMetaAwareObject(unsharableResources);
         }
      }
      catch (ResourceException e)
      {
         InvocationType type = mi.getType();
         boolean isLocal = (type == InvocationType.LOCAL || type == InvocationType.LOCALHOME);
         if (isLocal)
            throw new EJBException("Resource problem during invoke", e);
         else
            throw new RemoteException("Resource problem during invoke", e);
      }
   }

   public Object invokeHome(Invocation mi) throws Exception
   {
      EnterpriseContext ctx = (EnterpriseContext) mi.getEnterpriseContext();
      if (ctx == null)
         return getNext().invokeHome(mi);
      else
      {
         Object key = ctx.getInstance();
         try
         {
            ccm.pushMetaAwareObject(key, unsharableResources);
            try
            {
               return getNext().invokeHome(mi);
            }
            finally
            {
               ccm.popMetaAwareObject(unsharableResources);
            }
         }
         catch (ResourceException e)
         {
            InvocationType type = mi.getType();
            boolean isLocal = (type == InvocationType.LOCAL || type == InvocationType.LOCALHOME);
            if (isLocal)
               throw new EJBException("Resource problem during invokeHome", e);
            else
               throw new RemoteException("Resource problem during invokeHome", e);
         }
      }
   }

   public void setContainer(Container container)
   {
      this.container = container;
   }

   public Container getContainer()
   {
      return container;
   }

   public Object createBeanClassInstance() throws Exception
   {
      return pm.createBeanClassInstance();
   }

   public void createEntity(Method m, Object[] args, EntityEnterpriseContext instance) throws Exception
   {
      pm.createEntity(m, args, instance);
   }

   public void postCreateEntity(Method m, Object[] args, EntityEnterpriseContext instance) throws Exception
   {
      pm.postCreateEntity(m, args, instance);
   }

   public Object findEntity(Method finderMethod, Object[] args, EntityEnterpriseContext instance,
         GenericEntityObjectFactory factory) throws Exception
   {
      return pm.findEntity(finderMethod, args, instance, factory);
   }

   public Collection findEntities(Method finderMethod, Object[] args, EntityEnterpriseContext instance,
         GenericEntityObjectFactory factory) throws Exception
   {
      return pm.findEntities(finderMethod, args, instance, factory);
   }

   public void activateEntity(EntityEnterpriseContext instance) throws RemoteException
   {
      pm.activateEntity(instance);
   }

   public void loadEntity(EntityEnterpriseContext instance) throws RemoteException
   {
      pm.loadEntity(instance);
   }

   public boolean isStoreRequired(EntityEnterpriseContext instance) throws Exception
   {
      return pm.isStoreRequired(instance);
   }

   public boolean isModified(EntityEnterpriseContext ctx) throws Exception
   {
      return pm.isModified(ctx);
   }

   public void storeEntity(EntityEnterpriseContext ctx) throws RemoteException
   {
      Object key = ctx.getInstance();
      try
      {
         ccm.pushMetaAwareObject(key, unsharableResources);
         try
         {
            pm.storeEntity(ctx);
         }
         finally
         {
            ccm.popMetaAwareObject(unsharableResources);
         }
      }
      catch (ResourceException e)
      {
         throw new RemoteException("Could not store!: ", e);
      }
   }

   public void invokeEjbStore(EntityEnterpriseContext ctx) throws RemoteException
   {
      Object key = ctx.getInstance();
      try
      {
         ccm.pushMetaAwareObject(key, unsharableResources);
         try
         {
            pm.invokeEjbStore(ctx);
         }
         finally
         {
            ccm.popMetaAwareObject(unsharableResources);
         }
      }
      catch (ResourceException e)
      {
         throw new RemoteException("Could not store!: ", e);
      }
   }

   public void passivateEntity(EntityEnterpriseContext instance) throws RemoteException
   {
      pm.passivateEntity(instance);
   }

   public void removeEntity(EntityEnterpriseContext instance) throws RemoteException, RemoveException
   {
      pm.removeEntity(instance);
   }

   /**
    Return the real EntityPersistenceManager to which this interceptor delegates.
    @return the real EntityPersistenceManager
    */
   public EntityPersistenceManager getDelegatePersistenceManager()
   {
      return pm;
   }
}
