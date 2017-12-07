/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.ha.framework.server.lock;

import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jboss.ha.framework.interfaces.HAPartition;

/**
 * @author Brian Stansberry
 * 
 * @version $Revision:$
 */
public class LocalAndClusterLockManager
{
   
   private class LocalLock
   {
      private volatile ClusterNode holder;
      private final AtomicBoolean locked = new AtomicBoolean(false);
      private final Queue<Thread> waiters = new ConcurrentLinkedQueue<Thread>();


      private void lock(ClusterNode caller, long timeout) throws TimeoutException
      {
         long deadline = System.currentTimeMillis() + timeout;
         boolean wasInterrupted = false;
         Thread current = Thread.currentThread();
         waiters.add(current);
         
         try
         {
            // Block while not first in queue or cannot acquire lock
            while (waiters.peek() != current || 
                   !locked.compareAndSet(false, true)) 
            { 
               LockSupport.parkUntil(deadline);
               if (Thread.interrupted()) // ignore interrupts while waiting
                  wasInterrupted = true;
               if (System.currentTimeMillis() >= deadline)
               {
                  if (waiters.peek() != current || 
                        !locked.compareAndSet(false, true))
                  {
                     throw new TimeoutException(this.holder);
                  }
                  break;
               }
            }
            
            if (locked.get())
            {
               holder = caller;
            }
            else
            {
               throw new TimeoutException(this.holder);
            }
         }
         finally
         {
            waiters.remove();
            if (wasInterrupted)          // reassert interrupt status on exit
               current.interrupt();
         }
      }
      
      private void unlock(ClusterNode caller)
      {
         if (caller.equals(holder))              
         {
            locked.set(false);
            holder = null;
            LockSupport.unpark(waiters.peek());
         }
       } 

      
   }
   
   /** Handles callbacks from the cluster lock support object */
   private class ClusterHandler implements LocalLockHandler
   {      
      // ----------------------------------------------------- LocalLockHandler
      
      public ClusterNode getLocalNode(ClusterNode localNode)
      {
         return LocalAndClusterLockManager.this.localNode;
      }

      public void setLocalNode(ClusterNode localNode)
      {
         LocalAndClusterLockManager.this.localNode = localNode;
      }

      public void lockFromCluster(Serializable lockName, ClusterNode caller, long timeout) throws TimeoutException,
            InterruptedException
      {
         LocalAndClusterLockManager.this.doLock(lockName, caller, timeout);
      }

      public ClusterNode getLockHolder(Serializable lockName)
      {
         LocalLock lock = LocalAndClusterLockManager.this.getLocalLock(lockName, false);
         return lock == null ? null : lock.holder;
      }

      public void unlockFromCluster(Serializable lockName, ClusterNode caller)
      {
         LocalAndClusterLockManager.this.doUnlock(lockName, caller);
      }
      
   }
   
   private ClusterNode localNode;
   private ConcurrentMap<Serializable, LocalLock> localLocks = new ConcurrentHashMap<Serializable, LocalLock>();
   private final NonGloballyExclusiveClusterLockSupport clusterSupport;
   
   public LocalAndClusterLockManager(String serviceHAName, HAPartition partition)
   {
      ClusterHandler handler = new ClusterHandler();
      clusterSupport = new NonGloballyExclusiveClusterLockSupport(serviceHAName, partition, handler);
   }
   
   // ----------------------------------------------------------------- Public
   
   public void lockLocally(Serializable lockName, long timeout)
         throws TimeoutException, InterruptedException
   {
      if (this.localNode == null)
      {
         throw new IllegalStateException("Null localNode");
      }
      
      doLock(lockName, this.localNode, timeout);
   }
   
   public void unlockLocally(Serializable lockName)
   {
      if (this.localNode == null)
      {
         throw new IllegalStateException("Null localNode");
      }
      
      doUnlock(lockName, this.localNode);   
   }
   
   public void lockGlobally(Serializable lockName, long timeout)
         throws TimeoutException, InterruptedException
   {
      this.clusterSupport.lock(lockName, timeout);
   }
   
   public void unlockGlobally(Serializable lockName)
   {
      this.clusterSupport.unlock(lockName);
   }
   
   public void start() throws Exception
   {
      this.clusterSupport.start();
   }
   
   public void stop() throws Exception
   {
      this.clusterSupport.stop();
   }
   
   // ----------------------------------------------------------------- Private
   
   private LocalLock getLocalLock(Serializable categoryName, boolean create)
   {
      LocalLock category = localLocks.get(categoryName);
      if (category == null && create)
      {
         category = new LocalLock();
         LocalLock existing = localLocks.putIfAbsent(categoryName, category);
         if (existing != null)
         {
            category = existing;
         }         
      }
      return category;
   }
   
   private void doLock(Serializable lockName, ClusterNode caller, long timeout) throws TimeoutException,
         InterruptedException
   {
      LocalLock lock = getLocalLock(lockName, true);
      lock.lock(caller, timeout);
   }
   
   private void doUnlock(Serializable lockName, ClusterNode caller)
   {
      LocalLock lock = getLocalLock(lockName, false);
      if (lock != null)
      {
         lock.unlock(caller);
      }
   }

}
