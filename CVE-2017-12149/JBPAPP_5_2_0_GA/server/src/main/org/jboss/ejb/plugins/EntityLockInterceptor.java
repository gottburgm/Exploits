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

import org.jboss.ejb.BeanLock;
import org.jboss.invocation.Invocation;

/**
 * The lock interceptors role is to schedule thread wanting to invoke method on a target bean
 *
* <p>The policies for implementing scheduling (pessimistic locking etc) is implemented by pluggable
*    locks
*
* <p>We also implement serialization of calls in here (this is a spec
*    requirement). This is a fine grained notify, notifyAll mechanism. We
*    notify on ctx serialization locks and notifyAll on global transactional
*    locks.
*   
* <p><b>WARNING: critical code</b>, get approval from senior developers
*    before changing.
*    
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
* @version $Revision: 81030 $
*
* <p><b>Revisions:</b><br>
* <p><b>2001/07/30: marcf</b>
* <ol>
*   <li>Initial revision
*   <li>Factorization of the lock out of the context in cache
*   <li>The new locking is implement as "scheduling" in the lock which allows for pluggable locks
* </ol>
* <p><b>2001/08/07: billb</b>
* <ol>
*   <li>Removed while loop and moved it to SimplePessimisticEJBLock where it belongs.
* </ol>
*/
public class EntityLockInterceptor
   extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
 
   // Attributes ----------------------------------------------------
 
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
 
   // Public --------------------------------------------------------
 
   // Interceptor implementation --------------------------------------
 
   public Object invokeHome(Invocation mi)
      throws Exception
   {  
      // Invoke through interceptors
      return getNext().invokeHome(mi);
  
   }
 
   public Object invoke(Invocation mi)
      throws Exception
   {
  
      // The key.
      Object key = mi.getId();

      // The lock.
      BeanLock lock ;
  
      boolean threadIsScheduled = false;
      boolean trace = log.isTraceEnabled();
  
      if( trace ) log.trace("Begin invoke, key="+key);
   
  
      lock = container.getLockManager().getLock(key);
      try 
      {
   
         lock.schedule(mi);
   
         try {
    
            return getNext().invoke(mi); 
         }
   
         finally 
         {
    
            // we are done with the method, decrease the count, if it reaches 0 it will wake up 
            // the next thread 
            lock.sync();
            lock.endInvocation(mi);
            lock.releaseSync(); 
         }
      }
      finally
      {
   
         // We are done with the lock in general
         container.getLockManager().removeLockRef(key);
   
         if( trace ) log.trace("End invoke, key="+key);
      }
   }
}

