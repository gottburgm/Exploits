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

import javax.transaction.Transaction;

import org.jboss.ejb.BeanLock;
import org.jboss.ejb.Container;
import org.jboss.ejb.BeanLockExt;
import org.jboss.invocation.Invocation;
import org.jboss.logging.Logger;
import org.jboss.util.deadlock.Resource;


/**
 * Support for the BeanLock
 *
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 81030 $
 */
public abstract class BeanLockSupport implements Resource, BeanLockExt
{
   protected Container container = null;
   
   /**
    * Number of threads that retrieved this lock from the manager
    * (0 means removing)
    */ 
   protected int refs = 0;
   
   /** The Cachekey corresponding to this Bean */
   protected Object id = null;
 
   /** Logger instance */
   static Logger log = Logger.getLogger(BeanLock.class);
 
   /** Transaction holding lock on bean */
   protected Transaction tx = null;
 
   protected Thread synched = null;
   protected int synchedDepth = 0;

   protected int txTimeout;

	
   public void setId(Object id) { this.id = id;}
   public Object getId() { return id;}
   public void setTimeout(int timeout) {txTimeout = timeout;}
   public void setContainer(Container container) { this.container = container; }
   public Object getResourceHolder() { return tx; }

   /**
    * A non-blocking method that checks if the calling thread will be able to acquire
    * the sync lock based on the calling thread.
    *
    * @return true if the calling thread can obtain the sync lock in which
    * case it will, false if another thread already has the lock.
    */
   public boolean attemptSync()
   {
      boolean didSync = false;
      synchronized(this)
      {
         Thread thread = Thread.currentThread();
         if(synched == null || synched.equals(thread) == true)
         {
            synched = thread;
            ++ synchedDepth;
            didSync = true;
         }
      }
      return didSync;
   }

   /**
    * A method that checks if the calling thread has the lock, and if it
    * does not blocks until the lock is available. If there is no current owner
    * of the lock, or the calling thread already owns the lock then the
    * calling thread will immeadiately acquire the lock.
    */ 
   public void sync()
   {
      synchronized(this)
      {
         Thread thread = Thread.currentThread();
         while(synched != null && synched.equals(thread) == false)
         {
            try
            {
               this.wait();
            }
            catch (InterruptedException ex) { /* ignore */ }
         }
         synched = thread;
         ++synchedDepth;
      }
   }
 
   public void releaseSync()
   {
      synchronized(this)
      {
         if (--synchedDepth == 0)
            synched = null;
         this.notify();
      }
   }
 
   public abstract void schedule(Invocation mi) throws Exception;
	
   /**
    * The setTransaction associates a transaction with the lock.
    * The current transaction is associated by the schedule call.
    */
   public void setTransaction(Transaction tx){this.tx = tx;}
   public Transaction getTransaction(){return tx;}
   
   public abstract void endTransaction(Transaction tx);
   public abstract void wontSynchronize(Transaction tx);
	
   public abstract void endInvocation(Invocation mi);
   
   public void addRef() { refs++;}
   public void removeRef() { refs--;}
   public int getRefs() { return refs;}
   
   // Private --------------------------------------------------------
   
}
