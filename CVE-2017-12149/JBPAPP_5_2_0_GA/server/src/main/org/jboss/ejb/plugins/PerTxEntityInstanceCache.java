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

import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.BeanLock;
import org.jboss.ejb.EntityCache;
import org.jboss.ejb.txtimer.EJBTimerService;
import org.jboss.tm.TransactionLocal;
import org.jboss.logging.Logger;

import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.TimerService;
import javax.management.ObjectName;

/**
 * Per transaction instance cache.
 *
 * @jmx:mbean
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @author Galder Zamarreño
 * @version <tt>$Revision: 88746 $</tt>
 */
public class PerTxEntityInstanceCache
   implements EntityCache, PerTxEntityInstanceCacheMBean
{
   private static final Logger log = Logger.getLogger(PerTxEntityInstanceCache.class);

   // per container tx instance cache
   private final TransactionLocal txLocalCache = new TransactionLocal()
   {
      protected Object initialValue()
      {
         return new HashMap();
      }
   };

   private EntityContainer container;

   // EntityCache implementation

   public Object createCacheKey(Object id)
   {
      return id;
   }

   public EnterpriseContext get(Object id) throws RemoteException, NoSuchObjectException
   {
      if(id == null) throw new IllegalArgumentException("Can't get an object with a null key");

      Map cache = getLocalCache();
      EntityEnterpriseContext instance = (EntityEnterpriseContext) cache.get(id);
      if(instance == null)
      {
         try
         {
            // acquire
            instance = (EntityEnterpriseContext) container.getInstancePool().get();
            // set key
            instance.setId(id);
            instance.setCacheKey(id);
            // activate
            container.getPersistenceManager().activateEntity(instance);
            // insert
            cache.put(id, instance);
         }
         catch(Throwable x)
         {
            throw new NoSuchObjectException(x.getMessage());
         }
      }
      return instance;
   }

   public void insert(EnterpriseContext instance)
   {
      if(instance == null) throw new IllegalArgumentException("Can't insert a null object in the cache");

      EntityEnterpriseContext entity = (EntityEnterpriseContext) instance;
      getLocalCache().put(entity.getCacheKey(), instance);
   }

   public void release(EnterpriseContext instance)
   {
      if(instance == null) throw new IllegalArgumentException("Can't release a null object");

      tryToPassivate(instance);
   }

   public void remove(Object id)
   {
      // By default, commit option C is used with this cache which means that cache instances are 
      // removed immediately without trying to passivate, hence, remove associated timer if empty.
      removeTimerServiceIfAllCancelledOrExpired(id);
      getLocalCache().remove(id);
   }

   public boolean isActive(Object id)
   {
      return getLocalCache().containsKey(id);
   }

   public long getCacheSize()
   {
      return 0;
   }

   public void flush()
   {
   }

   // ContainerPlugin implementation

   public void setContainer(Container con)
   {
      this.container = (EntityContainer) con;
   }

   // Service implementation

   public void create() throws Exception
   {
   }

   public void start() throws Exception
   {
   }

   public void stop()
   {
   }

   public void destroy()
   {
   }

   // Protected

   protected void tryToPassivate(EnterpriseContext instance)
   {
      Object id = instance.getId();
      if(id != null)
      {
         BeanLock lock = container.getLockManager().getLock(id);
         try
         {
            lock.sync();
            if(canPassivate(instance))
            {
               try
               {
                  remove(id);
                  EntityEnterpriseContext entity = (EntityEnterpriseContext) instance;
                  container.getPersistenceManager().passivateEntity(entity);
                  container.getInstancePool().free(instance);
               }
               catch(Exception ignored)
               {
                  log.warn("failed to passivate, id=" + id, ignored);
               }
            }
            else
            {
               log.warn("Unable to passivate due to ctx lock, id=" + id);
            }
         }
         finally
         {
            lock.releaseSync();
            container.getLockManager().removeLockRef(id);
         }
      }
   }

   protected boolean canPassivate(EnterpriseContext ctx)
   {
      if(ctx.isLocked())
      {
         // The context is in the interceptor chain
         return false;
      }

      if(ctx.getTransaction() != null)
      {
         return false;
      }

      Object key = ((EntityEnterpriseContext) ctx).getCacheKey();
      return container.getLockManager().canPassivate(key);
   }

   protected void removeTimerServiceIfAllCancelledOrExpired(final Object id)
   {
      final boolean trace = log.isTraceEnabled();
      if (trace)
      {
         log.trace("Check whether all timers are cancelled or expired for this entity: " + id);
      }
      final EJBTimerService service = container.getTimerService();
      final ObjectName containerId = container.getJmxName();
      
      AccessController.doPrivileged(new PrivilegedAction<Object>()
      {
         public Object run()
         {
            TimerService timerService = service.getTimerService(containerId, id);
            if (timerService != null && timerService.getTimers().isEmpty())
            {
               // Assuming that active timers do not include cancelled or expired ones.
               if (trace)
               {
                  log.trace("No active timers available for " + containerId + " and primary key " + id);
               }
               service.removeTimerService(containerId, id);
            }
            return null;
         }
      });
   }

   // Private

   private Map getLocalCache()
   {
      return (Map) txLocalCache.get();
   }
}
