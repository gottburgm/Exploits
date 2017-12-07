/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.adapter.mail.inflow;

import java.util.concurrent.PriorityBlockingQueue;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkListener;

import org.jboss.logging.Logger;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 110243 $
 */
public class NewMsgsWorker
   implements Work, WorkListener
{
   private static Logger log = Logger.getLogger(NewMsgsWorker.class);
   private boolean released;
   private WorkManager mgr;
   private PriorityBlockingQueue pollQueue;
   private boolean trace;

   public NewMsgsWorker(WorkManager mgr)
   {
      this.mgr = mgr;
      // The capacity needs to be externalized
      this.pollQueue = new PriorityBlockingQueue(1024);
      this.trace = log.isTraceEnabled();
   }

   public void watch(MailActivation activation) throws InterruptedException
   {
      long now = System.currentTimeMillis();
      activation.updateNextNewMsgCheckTime(now);
      pollQueue.put(activation);
   }

   public void release()
   {
      released = true;
      if( trace )
         log.trace("released");
   }
   public void run()
   {
      if( trace )
         log.trace("Begin run");
      while( released == false )
      {
         try
         {
            MailActivation ma = (MailActivation) pollQueue.take();
            // Wait until its time to check for new msgs
            long now = System.currentTimeMillis();
            long nextTime = ma.getNextNewMsgCheckTime();
            long sleepMS = nextTime - now;
            if (sleepMS > 0)
               Thread.sleep(sleepMS);
            if( released )
               break;
            // This has to go after the sleep otherwise we can get into an inconsistent state
            if( ma.isReleased() )
                continue;
            // Now schedule excecution of the new msg check
            mgr.scheduleWork(ma, WorkManager.INDEFINITE, null, this);
         }
         catch(InterruptedException e)
         {
            log.warn("Interrupted waiting for new msg check", e);
         }
         catch (WorkException e)
         {
            log.warn("Failed to schedule new msg check", e);            
         }
      }
      if( trace )
         log.trace("End run");
   }

   // --- Begin WorkListener interface methods
   public void workAccepted(WorkEvent e)
   {
      if( trace )
         log.trace("workAccepted, e="+e);
   }

   public void workRejected(WorkEvent e)
   {
      if( trace )
         log.trace("workRejected, e="+e);
   }

   public void workStarted(WorkEvent e)
   {
      if( trace )
         log.trace("workStarted, e="+e);
   }

   public void workCompleted(WorkEvent e)
   {
      if( trace )
         log.trace("workCompleted, e="+e);
      MailActivation activation = (MailActivation) e.getWork();
      try
      {
         watch(activation);
      }
      catch(InterruptedException ex)
      {
         log.warn("Failed to reschedule new msg check", ex);
      }
   }
   // --- End WorkListener interface methods

}
