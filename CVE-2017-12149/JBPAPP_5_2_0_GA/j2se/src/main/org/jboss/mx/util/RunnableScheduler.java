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

import java.util.Iterator;
import java.util.TreeSet;

import org.jboss.util.threadpool.ThreadPool;
import org.jboss.util.timeout.TimeoutFactory;
import org.jboss.util.timeout.Timeout;

/**
 * A runnable scheduler.<p>
 * 
 * The scheduler needs to be started to do real work. To add work to the
 * scheduler, create a SchedulableRunnable and set the scheduler. When
 * the next run has passed the work is performed.
 * 
 * @see SchedulableRunnable
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 81019 $
 */
public class RunnableScheduler
{

   // Attributes ----------------------------------------------------

   /**
    * The runnables to schedule
    */
   private TimeoutFactory factory;

   /**
    * Constructs a new runnable scheduler.
    */
   public RunnableScheduler()
   {
	   this.factory = new TimeoutFactory();
   }

   /**
    * Constructs a new runnable scheduler.
    */
   public RunnableScheduler(ThreadPool threadPool)
   {
	   this.factory = new TimeoutFactory(threadPool);
   }

   /**
    * Starts the scheduler.
    */
   public void start()
   {
   }

   /**
    * Stops the scheduler, cancels all submitted jobs.
    */
   public synchronized void stop()
   {
	  factory.cancel();
   }

   /**
    * Runs the scheduler.
    */
   public void run()
   {
   }

   // Public --------------------------------------------------------

   // X Implementation ----------------------------------------------

   // Y Overrides ---------------------------------------------------

   // Protected -----------------------------------------------------

   // Package -------------------------------------------------------

   /**
    * Add a schedulable runnable
    *
    * @param runnable the runnable to add
    */
   Timeout add(SchedulableRunnable runnable)
   {
      return factory.schedule(runnable.getNextRun(), runnable);
   }

   // Private -------------------------------------------------------

   // Inner Classes -------------------------------------------------
}

