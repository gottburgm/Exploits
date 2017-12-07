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
package org.jboss.aspects.versioned;
import org.jboss.util.LRUCachePolicy;

/**
 * This is a LRU cache.  The TxCache itself is not transactional
 * but any accesses to objects within the cache ARE transactional.
 */
public class TxCache extends LRUCachePolicy
{
   protected long lockTimeout;
   protected LocalSynchronizationManager synchManager;
   protected DistributedVersionManager versionManager;
   public TxCache(int maxSize, long lockTimeout)
   {
      super(2, maxSize);
      this.lockTimeout = lockTimeout;
      synchManager = new LocalSynchronizationManager(null);
      versionManager = new DistributedVersionManager(lockTimeout, synchManager);
      this.create();
      this.start();
   }

   public void insert(Object key, Object obj)
   {
      try
      {
         Object versioned = versionManager.makeVersioned(obj);
         super.insert(key, versioned);
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
   }
}
