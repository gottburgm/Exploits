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
package org.jboss.ejb.txtimer;

// $Id: FixedDelayRetryPolicy.java 81030 2008-11-14 12:59:42Z dimitris@jboss.org $

import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

import javax.ejb.Timer;

/**
 * This service implements a RetryPolicy that retries
 * the call to ejbTimeout after a fixed delay.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 81030 $
 * @since 07-Apr-2004
 */
public class FixedDelayRetryPolicy extends ServiceMBeanSupport implements FixedDelayRetryPolicyMBean
{
   // logging support
   private static Logger log = Logger.getLogger(FixedDelayRetryPolicy.class);

   // the delay before retry
   private long delay = 100;

   /**
    * Get the delay for retry
    *
    * @return delay in ms
    * @jmx.managed-attribute
    */
   public long getDelay()
   {
      return this.delay;
   }

   /**
    * Set the delay for retry
    *
    * @param delay in ms
    * @jmx.managed-attribute
    */
   public void setDelay(long delay)
   {
      this.delay = delay;
   }

   /**
    * Invokes the ejbTimeout method on the TimedObject with the given id.
    *
    * @param invoker The invoker for the TimedObject
    * @param timer   the Timer that is passed to ejbTimeout
    * @jmx.managed-operation
    */
   public void retryTimeout(TimedObjectInvoker invoker, Timer timer)
   {
      // check if the delay is appropriate
      if (timer instanceof TimerImpl)
      {
         TimerImpl txTimer = (TimerImpl)timer;

         long periode = txTimer.getPeriode();
         if (0 < periode && periode / 2 < delay)
            log.warn("A delay of " + delay + " ms might not be appropriate for a timer periode of " + periode + " ms");
      }

      new RetryThread(invoker, timer).start();
   }

   /**
    * The thread that does the actual invocation,
    * after a short delay.
    */
   private class RetryThread extends Thread
   {
      private TimedObjectInvoker invoker;
      private Timer timer;

      public RetryThread(TimedObjectInvoker invoker, Timer timer)
      {
         this.invoker = invoker;
         this.timer = timer;
      }

      public void run()
      {
         try
         {
            Thread.sleep(delay);
            log.debug("Retry ejbTimeout: " + timer);
            invoker.callTimeout(timer);
         }
         catch (Exception ignore)
         {
            ignore.printStackTrace();
         }
      }
   }
}
