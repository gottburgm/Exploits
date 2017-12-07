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
package org.jboss.ejb.plugins.cmp.jdbc2.schema;

import org.jboss.cache.invalidation.Invalidatable;
import org.jboss.cache.invalidation.InvalidationGroup;
import org.jboss.logging.Logger;

import javax.transaction.TransactionManager;
import javax.transaction.Transaction;
import javax.transaction.SystemException;
import java.io.Serializable;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81030 $</tt>
 */
public class CacheInvalidator
   implements Invalidatable
{
   private static final Logger log = Logger.getLogger(CacheInvalidator.class);

   private final Cache cache;
   private final TransactionManager tm;
   private final InvalidationGroup group;

   public CacheInvalidator(Cache cache, TransactionManager tm, InvalidationGroup group)
   {
      this.cache = cache;
      this.tm = tm;
      this.group = group;
      group.register(this);
      log.debug("registered to group " + group.getGroupName());
   }

   public void unregister()
   {
      group.unregister(this);
      log.debug("unregistered from group " + group.getGroupName());
   }

   public void isInvalid(Serializable key)
   {
      Transaction tx = null;
      try
      {
         tx = tm.getTransaction();
      }
      catch(SystemException e)
      {
         log.error("Failed to obtain the current transaction", e);
         throw new IllegalStateException("Failed to obtain the current transaction: " + e.getMessage());
      }

      if(log.isTraceEnabled())
      {
         log.trace("invalidating key=" + key);
      }

      cache.lock(key);
      try
      {
         cache.remove(tx, key);
      }
      catch(Cache.RemoveException e)
      {
         if(log.isTraceEnabled())
         {
            log.trace(e.getMessage());
         }
      }
      finally
      {
         cache.unlock(key);
      }
   }

   public void areInvalid(Serializable[] keys)
   {
      Transaction tx = null;
      try
      {
         tx = tm.getTransaction();
      }
      catch(SystemException e)
      {
         log.error("Failed to obtain the current transaction", e);
         throw new IllegalStateException("Failed to obtain the current transaction: " + e.getMessage());
      }

      boolean trace = log.isTraceEnabled();
      for(int i = 0; i < keys.length; ++i)
      {
         if(trace)
         {
            log.trace("invalidating key[" + i + "]=" + keys[i]);
         }

         cache.lock(keys[i]);
         try
         {
            cache.remove(tx, keys[i]);
         }
         catch(Cache.RemoveException e)
         {
            if(trace)
            {
               log.trace(e.getMessage());
            }
         }
         finally
         {
            cache.unlock(keys[i]);
         }
      }
   }

   public void invalidateAll()
   {
      cache.lock();
      try
      {
         cache.flush();
      }
      finally
      {
         cache.unlock();
      }
   }
}
