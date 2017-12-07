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
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.Xid;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.tm.JBossXATerminator;
import org.jboss.util.threadpool.Task;
import org.jboss.util.threadpool.ThreadPool;

/**
 * The work manager implementation
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 80054 $
 */
public class JBossWorkManager extends ServiceMBeanSupport implements WorkManager, JBossWorkManagerMBean
{
   /** Whether trace is enabled */
   private boolean trace = log.isTraceEnabled();
   
   /** The thread pool */
   private ThreadPool threadPool;

   /** The xa terminator */
   private JBossXATerminator xaTerminator;

   /**
    * Retrieve the thread pool
    *
    * @return the thread pool
    */
   public ThreadPool getThreadPool()
   {
      return threadPool;
   }

   /**
    * Set the thread pool
    *
    * @param threadPool the thread pool
    */
   public void setThreadPool(ThreadPool threadPool)
   {
      this.threadPool = threadPool;
   }

   /**
    * Get the XATerminator
    * 
    * @return the xa terminator
    */
   public JBossXATerminator getXATerminator()
   {
      return xaTerminator;
   }

   /**
    * Set the XATerminator
    * 
    * @param xaTerminator the xa terminator
    */
   public void setXATerminator(JBossXATerminator xaTerminator)
   {
      this.xaTerminator = xaTerminator;
   }

   public WorkManager getInstance()
   {
      return this;
   }

   public void doWork(Work work, long startTimeout, ExecutionContext ctx, WorkListener listener) throws WorkException
   {
      if (ctx == null)
         ctx = new ExecutionContext();
      WorkWrapper wrapper = new WorkWrapper(this, work, Task.WAIT_FOR_COMPLETE, startTimeout, ctx, listener);
      importWork(wrapper);
      executeWork(wrapper);
      if (wrapper.getWorkException() != null)
         throw wrapper.getWorkException();
   }

   public void doWork(Work work) throws WorkException
   {
      doWork(work, WorkManager.INDEFINITE, null, null);
   }

   public long startWork(Work work, long startTimeout, ExecutionContext ctx, WorkListener listener) throws WorkException
   {
      if (ctx == null)
         ctx = new ExecutionContext();
      WorkWrapper wrapper = new WorkWrapper(this, work, Task.WAIT_FOR_START, startTimeout, ctx, listener);
      importWork(wrapper);
      executeWork(wrapper);
      if (wrapper.getWorkException() != null)
         throw wrapper.getWorkException();
      return wrapper.getBlockedElapsed();
   }

   public long startWork(Work work) throws WorkException
   {
      return startWork(work, WorkManager.INDEFINITE, null, null);
   }

   public void scheduleWork(Work work, long startTimeout, ExecutionContext ctx, WorkListener listener) throws WorkException
   {
      if (ctx == null)
         ctx = new ExecutionContext();
      WorkWrapper wrapper = new WorkWrapper(this, work, Task.WAIT_NONE, startTimeout, ctx, listener);
      importWork(wrapper);
      executeWork(wrapper);
      if (wrapper.getWorkException() != null)
         throw wrapper.getWorkException();
   }

   public void scheduleWork(Work work) throws WorkException
   {
      scheduleWork(work, WorkManager.INDEFINITE, null, null);
   }

   /**
    * Import any work
    * 
    * @param wrapper the work wrapper
    * @throws WorkException for any error 
    */
   protected void importWork(WorkWrapper wrapper) throws WorkException
   {
      trace = log.isTraceEnabled();
      if (trace)
         log.trace("Importing work " + wrapper);
      
      ExecutionContext ctx = wrapper.getExecutionContext();
      if (ctx != null)
      {
         Xid xid = ctx.getXid();
         if (xid != null)
         {
            //JBAS-4002 base value is in seconds as per the API, here we convert to millis
            long timeout = (ctx.getTransactionTimeout() * 1000);
            xaTerminator.registerWork(wrapper.getWork(), xid, timeout);
         }
      }
      if (trace)
         log.trace("Imported work " + wrapper);
   }
   
   /**
    * Execute the work
    * 
    * @param wrapper the work wrapper
    * @throws WorkException for any error 
    */
   protected void executeWork(WorkWrapper wrapper) throws WorkException
   {
      if (trace)
         log.trace("Submitting work to thread pool " + wrapper);

      threadPool.runTaskWrapper(wrapper);

      if (trace)
         log.trace("Submitted work to thread pool " + wrapper);
   }

   /**
    * Start work
    * 
    * @param wrapper the work wrapper
    * @throws WorkException for any error 
    */
   protected void startWork(WorkWrapper wrapper) throws WorkException
   {
      if (trace)
         log.trace("Starting work " + wrapper);

      ExecutionContext ctx = wrapper.getExecutionContext();
      if (ctx != null)
      {
         Xid xid = ctx.getXid();
         if (xid != null)
         {
            xaTerminator.startWork(wrapper.getWork(), xid);
         }
      }
      if (trace)
         log.trace("Started work " + wrapper);
   }

   /**
    * End work
    * 
    * @param wrapper the work wrapper
    */
   protected void endWork(WorkWrapper wrapper)
   {
      if (trace)
         log.trace("Ending work " + wrapper);

      ExecutionContext ctx = wrapper.getExecutionContext();
      if (ctx != null)
      {
         Xid xid = ctx.getXid();
         if (xid != null)
         {
            xaTerminator.endWork(wrapper.getWork(), xid);
         }
      }
      if (trace)
         log.trace("Ended work " + wrapper);
   }

   /**
    * Cancel work
    * 
    * @param wrapper the work wrapper
    */
   protected void cancelWork(WorkWrapper wrapper)
   {
      if (trace)
         log.trace("Cancel work " + wrapper);

      ExecutionContext ctx = wrapper.getExecutionContext();
      if (ctx != null)
      {
         Xid xid = ctx.getXid();
         if (xid != null)
         {
            xaTerminator.cancelWork(wrapper.getWork(), xid);
         }
      }
      if (trace)
         log.trace("Canceled work " + wrapper);
   }
}
