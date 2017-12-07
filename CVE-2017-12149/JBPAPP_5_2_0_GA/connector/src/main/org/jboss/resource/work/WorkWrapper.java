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
package org.jboss.resource.work;

import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.work.WorkRejectedException;

import org.jboss.logging.Logger;
import org.jboss.util.JBossStringBuilder;
import org.jboss.util.NestedRuntimeException;
import org.jboss.util.threadpool.BasicTaskWrapper;
import org.jboss.util.threadpool.StartTimeoutException;
import org.jboss.util.threadpool.Task;

/**
 * Wraps the resource adapter's work.
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 108436 $
 */
public class WorkWrapper extends BasicTaskWrapper implements Task
{
   /** The log */
   private static final Logger log = Logger.getLogger(WorkWrapper.class);

   /** Whether we are tracing */
   private boolean trace = log.isTraceEnabled();
   
   /** The work */
   private Work work;

   /** The execution context */
   private ExecutionContext executionContext;

   /** the work listener */
   private WorkListener workListener;

   /** The start timeout */
   private long startTimeout;

   /** The work manager */
   private JBossWorkManager workManager;

   /** The wait type */
   private int waitType;

   /** The blocked time */
   private long blockedTime;

   /** Any exception */
   private WorkException exception;

   /**
    * Create a new WorkWrapper
    *
    * @param workManager the work manager
    * @param work the work
    * @param waitType the waitType
    * @param executionContext the execution context
    * @param workListener the WorkListener
    * @throws IllegalArgumentException for null work, execution context or a negative start timeout
    */
   public WorkWrapper(JBossWorkManager workManager, Work work, int waitType, long startTimeout, ExecutionContext executionContext, WorkListener workListener)
   {
      super();

      if (work == null)
         throw new IllegalArgumentException("Null work");
      if (executionContext == null)
         throw new IllegalArgumentException("Null execution context");
      if (startTimeout < 0)
         throw new IllegalArgumentException("Illegal start timeout: " + startTimeout);

      this.workManager = workManager;
      this.work = work;
      this.waitType = waitType;
      this.startTimeout = startTimeout;
      this.executionContext = executionContext;
      this.workListener = workListener;

      setTask(this);
   }

   /**
    * Get the work manager
    *
    * @return the work manager
    */
   public JBossWorkManager getWorkManager()
   {
      return workManager;
   }

   /**
    * Retrieve the work
    *
    * @return the work
    */
   public Work getWork()
   {
      return work;
   }

   /**
    * Retrieve the work listener
    *
    * @return the WorkListener
    */
   public WorkListener getWorkListener()
   {
      return workListener;
   }

   /**
    * Retrieve the exection context
    *
    * @return the execution context
    */
   public ExecutionContext getExecutionContext()
   {
      return executionContext;
   }

   /**
    * Retrieve the time blocked
    *
    * @return the blocked time
    */
   public long getBlockedElapsed()
   {
      return blockedTime;
   }
   
   /**
    * Get any exception
    * 
    * @return the exception or null if there is none
    */
   public WorkException getWorkException()
   {
      return exception;
   }

   public int getWaitType()
   {
      return waitType;
   }

   public int getPriority()
   {
      return Thread.NORM_PRIORITY;
   }

   public long getStartTimeout()
   {
      return startTimeout;
   }

   public long getCompletionTimeout()
   {
      // [JBAS-6400] Conversion of seconds to milliseconds
      return executionContext.getTransactionTimeout() * 1000L;
   }

   public void execute()
   {
      if (trace)
         log.trace("Executing work " + this);
      try
      {
         workManager.startWork(this);
      }
      catch (WorkException e)
      {
         taskRejected(new NestedRuntimeException(e));
         return;
      }
      try
      {
         work.run();
      }
      finally
      {
         workManager.endWork(this);
      }
      if (trace)
         log.trace("Executed work " + this);
   }

   public void stop()
   {
      if (trace)
         log.trace("Stopping work " + this);

      work.release();
   }

   public void accepted(long time)
   {
      blockedTime = time;

      if (trace)
         log.trace("Accepted work " + this);

      if (workListener != null)
      {
         WorkEvent event = new WorkEvent(workManager, WorkEvent.WORK_ACCEPTED, work, null);
         workListener.workAccepted(event);
      }
   }

   public void rejected(long time, Throwable throwable)
   {
      blockedTime = time;

      if (trace)
      {
         if (throwable != null)
            log.trace("Rejecting work " + this, throwable);
         else
            log.trace("Rejecting work " + this);
      }

      if (throwable != null)
      {
         exception = new WorkRejectedException(throwable);
         if (throwable instanceof StartTimeoutException)
            exception.setErrorCode(WorkRejectedException.START_TIMED_OUT);
      }
      
      workManager.cancelWork(this);
      
      if (workListener != null)
      {
         WorkEvent event = new WorkEvent(workManager, WorkEvent.WORK_REJECTED, work, exception);
         workListener.workRejected(event);
      }
   }

   public void started(long time)
   {
      if (waitType != WAIT_NONE)
         blockedTime = time;

      if (workListener != null)
      {
         WorkEvent event = new WorkEvent(workManager, WorkEvent.WORK_STARTED, work, null);
         workListener.workStarted(event);
      }
   }

   public void completed(long time, Throwable throwable)
   {
      if (waitType == WAIT_FOR_COMPLETE)
         blockedTime = time;

      if (throwable != null)
         exception = new WorkCompletedException(throwable);

      if (trace)
         log.trace("Completed work " + this);

      if (workListener != null)
      {
         WorkEvent event = new WorkEvent(workManager, WorkEvent.WORK_COMPLETED, work, exception);
         workListener.workCompleted(event);
      }
   }
   
   public String toString()
   {
      JBossStringBuilder buffer = new JBossStringBuilder(100);
      buffer.append("WorkWrapper@").append(Integer.toHexString(System.identityHashCode(this)));
      buffer.append("[workManger=").append(workManager);
      buffer.append(" work=").append(work);
      buffer.append(" state=").append(getStateString());
      if (executionContext != null && executionContext.getXid() != null)
      {
         buffer.append(" xid=").append(executionContext.getXid());
         buffer.append(" txTimeout=").append(executionContext.getTransactionTimeout());
      }
      buffer.append(" waitType=");
      switch (waitType)
      {
         case WAIT_NONE:
         {
            buffer.append("WAIT_NONE");
            break;
         }
         case WAIT_FOR_START:
         {
            buffer.append("WAIT_FOR_START");
            break;
         }
         case WAIT_FOR_COMPLETE:
         {
            buffer.append("WAIT_FOR_COMPLETE");
            break;
         }
         default:
            buffer.append("???");
      }
      if (startTimeout != WorkManager.INDEFINITE)
         buffer.append(" startTimeout=").append(startTimeout);
      long completionTimeout = getCompletionTimeout();
      if (completionTimeout != -1)
         buffer.append(" completionTimeout=").append(completionTimeout);
      if (blockedTime != 0)
         buffer.append(" blockTime=").append(blockedTime);
      buffer.append(" elapsedTime=").append(getElapsedTime());
      if (workListener != null)
         buffer.append(" workListener=").append(workListener);
      if (exception != null)
         buffer.append(" exception=").append(exception);
      buffer.append("]");
      return buffer.toString();
   }
}
