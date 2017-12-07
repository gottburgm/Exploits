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

import org.jboss.invocation.Invocation;

/**
 * No locking what-so-ever
 * 
 * 
 * Holds all locks for entity beans, not used for stateful. <p>
 *
 * All BeanLocks have a reference count.
 * When the reference count goes to 0, the lock is released from the
 * id -> lock mapping.
 *
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 *
 * @version $Revision: 81030 $
 *
 * <p><b>Revisions:</b><br>
 * <p><b>2001/08/08: billb</b>
 *  <ol>
 *  <li>Initial revision
 *  </ol>
 */
public class NoLock extends BeanLockSupport
{
   /**
    * Schedule(Invocation)
    * 
    * Schedule implements a particular policy for scheduling the threads coming in. 
    * There is always the spec required "serialization" but we can add custom scheduling in here
    *
    * Synchronizing on lock: a failure to get scheduled must result in a wait() call and a 
    * release of the lock.  Schedulation must return with lock.
    * 
    */
   public void schedule(Invocation mi) 
      throws Exception
   {
      return;
   } 

   public void endTransaction(Transaction transaction)
   {
      // complete
   }

   public void wontSynchronize(Transaction trasaction)
   {
      // complete
   }

   public void endInvocation(Invocation mi) 
   { 
      // complete
   }
   
}

