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
package org.jboss.ejb.plugins.lock;

import org.jboss.util.deadlock.ApplicationDeadlockException;
import org.jboss.util.deadlock.Resource;
import org.jboss.util.deadlock.DeadlockDetector;

import javax.transaction.Transaction;

/**
 * Implementents a non reentrant lock with deadlock detection
 * <p/>
 * It will throw a ReentranceException if the same thread tries to acquire twice
 * or the same transaction tries to acquire twice
 *
 * @author <a href="bill@jboss.org">Bill Burke</a>
 * @author <a href="alex@jboss.org">Alexey Loubyansky</a>
 * @version $Revision: 81030 $
 */
public class NonReentrantLock implements Resource
{
   public static class ReentranceException extends Exception
   {
      public ReentranceException()
      {
      }

      public ReentranceException(String message)
      {
         super(message);
      }
   }

   //private static final Logger log = Logger.getLogger(NonReentrantLock.class);

   protected Thread lockHolder;
   protected Object lock = new Object();
   protected volatile int held = 0;
   protected Transaction holdingTx = null;
   private boolean inNonReentrant;

   public Object getResourceHolder()
   {
      if(holdingTx != null) return holdingTx;
      return lockHolder;
   }

   protected boolean acquireNonReentrant(long waitTime, Transaction miTx)
      throws ApplicationDeadlockException, InterruptedException, ReentranceException
   {
      synchronized(lock)
      {
         final Thread curThread = Thread.currentThread();
         if(lockHolder != null)
         {
            if(lockHolder == curThread)
            {
               if(inNonReentrant)
               {
                  throw new ReentranceException("The same thread reentered: thread-holder=" +
                     lockHolder +
                     ", holding tx=" +
                     holdingTx +
                     ", current tx=" + miTx);
               }
            }
            else if(miTx != null && miTx.equals(holdingTx))
            {
               if(inNonReentrant)
               {
                  throw new ReentranceException("The same tx reentered: tx=" +
                     miTx +
                     ", holding thread=" +
                     lockHolder +
                     ", current thread=" + curThread);
               }
            }
            else
            {
               // Always upgrade deadlock holder to Tx so that we can detect lock properly
               Object deadlocker = curThread;
               if(miTx != null) deadlocker = miTx;
               try
               {
                  DeadlockDetector.singleton.deadlockDetection(deadlocker, this);
                  while(lockHolder != null)
                  {
                     if(waitTime < 1)
                     {
                        lock.wait();
                     }
                     else
                     {
                        lock.wait(waitTime);
                     }
                     // If we waited and never got lock, abort
                     if(waitTime > 0 && lockHolder != null) return false;
                  }
               }
               finally
               {
                  DeadlockDetector.singleton.removeWaiting(deadlocker);
               }
            }
         }

         ++held;
         lockHolder = curThread;
         holdingTx = miTx;
         inNonReentrant = true;
      }
      return true;
   }

   protected boolean acquireReentrant(long waitTime, Transaction miTx)
      throws ApplicationDeadlockException, InterruptedException, ReentranceException
   {
      synchronized(lock)
      {
         final Thread curThread = Thread.currentThread();
         if(lockHolder != null)
         {
            if(lockHolder != curThread && (miTx == null || miTx.equals(holdingTx)))
            {
               // Always upgrade deadlock holder to Tx so that we can detect lock properly
               Object deadlocker = curThread;
               if(miTx != null) deadlocker = miTx;
               try
               {
                  DeadlockDetector.singleton.deadlockDetection(deadlocker, this);
                  while(lockHolder != null)
                  {
                     if(waitTime < 1)
                     {
                        lock.wait();
                     }
                     else
                     {
                        lock.wait(waitTime);
                     }
                     // If we waited and never got lock, abort
                     if(waitTime > 0 && lockHolder != null) return false;
                  }
               }
               finally
               {
                  DeadlockDetector.singleton.removeWaiting(deadlocker);
               }
            }
         }

         ++held;
         lockHolder = curThread;
         holdingTx = miTx;
      }
      return true;
   }

   public boolean attempt(long waitTime, Transaction miTx, boolean nonReentrant)
      throws ApplicationDeadlockException, InterruptedException, ReentranceException
   {
      return nonReentrant ? acquireNonReentrant(waitTime, miTx) : acquireReentrant(waitTime, miTx);
   }

   public void release(boolean nonReentrant)
   {
      synchronized(lock)
      {
         held--;
         if(held < 0)
         {
            throw new IllegalStateException("Released lock too many times");
         }
         else if(held == 0)
         {
            lockHolder = null;
            holdingTx = null;
            lock.notify();
         }

         if(nonReentrant)
         {
            inNonReentrant = false;
         }
      }
   }
}
