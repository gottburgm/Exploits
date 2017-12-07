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
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;

import javax.ejb.TimerService;
import javax.management.ObjectName;

import org.jboss.common.beans.property.BeanUtils;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.GlobalTxEntityMap;
import org.jboss.ejb.txtimer.EJBTimerService;
import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.ConfigurationMetaData;

/**
 * Cache subclass for entity beans.
 * 
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author Galder Zamarreño
 * @version $Revision: 113110 $
 * @jmx:mbean extends="org.jboss.ejb.plugins.AbstractInstanceCacheMBean"
 */
public class EntityInstanceCache
   extends AbstractInstanceCache 
   implements org.jboss.ejb.EntityCache, EntityInstanceCacheMBean
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   /* The container */
   private EntityContainer m_container;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   /* From ContainerPlugin interface */
   public void setContainer(Container c) 
   {
      m_container = (EntityContainer)c;
   }

   // Z implementation ----------------------------------------------
   public Object createCacheKey(Object id)
   {
      return id;
   }

   // Y overrides ---------------------------------------------------
   public EnterpriseContext get(Object id) 
      throws RemoteException, NoSuchObjectException 
   {
      EnterpriseContext rtn = null;
      rtn = super.get(id);
      return rtn;
   }

   /**
    * @jmx:managed-operation
    */
   public void remove(String id)
      throws Exception
   {
      EntityMetaData metaData = (EntityMetaData) m_container.getBeanMetaData();
      String primKeyClass = metaData.getPrimaryKeyClass();
      Object key = BeanUtils.convertValue(id, primKeyClass);
      remove(key);
   }

   public void destroy()
   {
      synchronized( this )
      {
         this.m_container = null;
      }
      super.destroy();
   }

   protected Object getKey(EnterpriseContext ctx) 
   {
      return ((EntityEnterpriseContext)ctx).getCacheKey();
   }

   protected void setKey(Object id, EnterpriseContext ctx) 
   {
      ((EntityEnterpriseContext)ctx).setCacheKey(id);
      ctx.setId(id);
   }

   protected synchronized Container getContainer()
   {
      return m_container;
   }

   protected void passivate(EnterpriseContext ctx) throws RemoteException
   {
      removeTimerServiceIfAllCancelledOrExpired(ctx);
      m_container.getPersistenceManager().passivateEntity((EntityEnterpriseContext)ctx);
   }

   protected void activate(EnterpriseContext ctx) throws RemoteException
   {
      m_container.getPersistenceManager().activateEntity((EntityEnterpriseContext)ctx);
   }

   protected EnterpriseContext acquireContext() throws Exception
   {
      return m_container.getInstancePool().get();
   }

   protected void freeContext(EnterpriseContext ctx)
   {
      m_container.getInstancePool().free(ctx);
   }

   protected boolean canPassivate(EnterpriseContext ctx) 
   {
      if (ctx.isLocked()) 
      {
         // The context is in the interceptor chain
         return false;
      }

      if (ctx.getTransaction() != null)
      {
         return false;
      }

      Object key = ((EntityEnterpriseContext)ctx).getCacheKey();
      return m_container.getLockManager().canPassivate(key);
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   protected void unableToPassivateDueToCtxLock(EnterpriseContext ctx, boolean passivateAfterCommit)
   {
      EntityEnterpriseContext ectx = (EntityEnterpriseContext)ctx;
      ectx.setPassivateAfterCommit(passivateAfterCommit);
      ConfigurationMetaData config = m_container.getBeanMetaData().getContainerConfiguration();
      if(!config.isStoreNotFlushed() && ectx.hasTxSynchronization())
      {
         ectx.setTxAssociation(GlobalTxEntityMap.PREVENT_SYNC);
      }
   }

   protected void removeTimerServiceIfAllCancelledOrExpired(EnterpriseContext ctx)
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
      {
         log.trace("Check whether all timers are cancelled or expired for this entity: " + ctx);
      }
      EJBTimerService service = m_container.getTimerService();
      ObjectName containerId = m_container.getJmxName();
      Object pKey = ctx.getId();
      TimerService timerService = service.getTimerService(containerId, pKey);
      if (timerService != null && timerService.getTimers().isEmpty())
      {
         // Assuming that active timers do not include cancelled or expired ones.
         if (trace)
         {
            log.trace("No active timers available for " + containerId + " and primary key " + pKey);
         }
         service.removeTimerService(containerId, pKey);
      }
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
