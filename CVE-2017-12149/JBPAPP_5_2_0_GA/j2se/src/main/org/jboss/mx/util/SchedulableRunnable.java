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
package org.jboss.mx.util;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedLong;
import org.jboss.util.timeout.Timeout;

/**
 * A schedulable runnable.<p>
 *
 * Subclasses should implement doRun() to do some real work.<p>
 *
 * setScheduler(RunnableScheduler) has to be invoked with a RunnableScheduler
 * that has been started for the work to be performed. If the doRun() does
 * not invoke setNextRun(), the link to the scheduler is removed.
 *
 * @see RunnableScheduler
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81019 $
 */
public abstract class SchedulableRunnable
   implements Runnable
{
   // Attributes ----------------------------------------------------

   /**
    * The next run timestamp
    */
   private SynchronizedLong nextRun = new SynchronizedLong(0);

   /**
    * The current scheduler
    */
   private RunnableScheduler scheduler;

   /**
    * Whether we are running
    */
   private boolean running;

   /**
    * Whether we should reschedule after the run
    */
   private boolean reschedule;

   /**
    * Handle for canceling ourselves.
    */
   private Timeout timeout;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Constructs a new schedulable runnable.
    */
   public SchedulableRunnable()
   {
   }

   // Public --------------------------------------------------------

   /**
    * Gets the next run timestamp
    *
    * @return the next run
    */
   public long getNextRun()
   {
      return nextRun.get();
   }

   /**
    * Sets the next run date<p>
    *
    * If it is linked to a scheduler, it is temporarily removed while the date
    * is modified.
    *
    * @param nextRun the next run date
    */
   public synchronized void setNextRun(long nextRun)
   {
      // Remove from scheduler
      if (timeout != null)
         timeout.cancel();

      // Set the new run time
      this.nextRun.set(nextRun);

      // If we are not running, add it to the scheduler otherwise
      // remember we want adding back
      if (running == false && scheduler != null)
      {
         //log.debug("add to scheduler: " + this);
         timeout = scheduler.add(this);
      }
      else
      {
         //log.debug("reschedule: " + this);
         reschedule = true;
      }
   }

   /**
    * Set the scheduler for this runnable
    *
    * @param scheduler pass null to remove the runnable from any scheduler
    * @return the previous scheduler or null if there was no previous scheduler
    */
   public synchronized RunnableScheduler setScheduler(RunnableScheduler scheduler)
   {
      // Null operation
      if (this.scheduler == scheduler)
         return this.scheduler;

      // Remember the result
      RunnableScheduler result = this.scheduler;

      // Remove from previous scheduler
      if (this.timeout != null)
         timeout.cancel();

      // Set the new state
      this.scheduler = scheduler;

      // This is a remove operation
      if (scheduler == null)
         reschedule = false;

      // If we are not running, add it to the scheduler otherwise
      // remember we want adding
      else if (running == false)
         timeout = scheduler.add(this);
      else
         reschedule = true;

      // We are done
      return result;
   }

   /**
    * Do the work, the scheduled runnable should do its work here
    */
   public abstract void doRun();

   // Runnable Implementation ---------------------------------------

   /**
    * Runs doRun()<p>
    *
    * If setNextRun() is not invoked during the doRun(), the link to the 
    * scheduler is removed
    */
   public final void run()
   {
      startRun();
      try
      {
         doRun();
      }
      finally
      {
         endRun();
      }
   }

   // Object Overrides ----------------------------------------------

   // Protected -----------------------------------------------------

   // Package -------------------------------------------------------

   // Private -------------------------------------------------------

   /**
    * Start the run
    */
   private synchronized void startRun()
   {
      running = true;
   }

   /**
    * Check whether the work got rescheduled
    */
   private synchronized void endRun()
   {
      running = false;
      if (reschedule == true)
         timeout = scheduler.add(this);
      reschedule = false;
   }

   // Inner Classes -------------------------------------------------
}

