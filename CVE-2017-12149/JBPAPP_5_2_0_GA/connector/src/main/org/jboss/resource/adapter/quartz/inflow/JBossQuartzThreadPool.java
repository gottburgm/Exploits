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
package org.jboss.resource.adapter.quartz.inflow;

import org.quartz.SchedulerConfigException;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;

/**
 * Thread pool used to fire Quartz Jobs.
 * <p/>
 * Using BootstrapContext's workManager thread pool.
 * No dependency outside this rar or JCA.
 *
 * @see http://jira.jboss.com/jira/browse/JBAS-1792
 * @see quartz.properties
 *
 * @author <a href="mailto:ales.justin@gmail.com">Ales Justin</a>
 */
public class JBossQuartzThreadPool implements org.quartz.spi.ThreadPool
{
   private int poolSize = Integer.MAX_VALUE;

   private WorkManager workManager;

   public void initialize() throws SchedulerConfigException
   {
      workManager = QuartzResourceAdapter.getConfigTimeWorkManager();
   }

   /**
    * Currently this method is only used in metadata lookup.
    * Which is has no further use.
    * How to provide better estimate?
    */
   public int getPoolSize()
   {
      return poolSize;
   }

   public boolean runInThread(Runnable runnable)
   {
      try
      {
         WorkWrapper workWrapper = new WorkWrapper(runnable);
         workManager.doWork(workWrapper);
         return true;
      }
      catch (WorkException e)
      {
         return false;
      }
   }

   /**
    * No shutdown impl - workManager is shutdown by itself.
    */
   public void shutdown(boolean waitForJobsToComplete)
   {
   }

   /**
    * Just in case we want to set pool size.
    */
   public void setPoolSize(int poolSize)
   {
      this.poolSize = poolSize;
   }

   private class WorkWrapper implements Work
   {

      private Runnable delegate;

      public WorkWrapper(Runnable delegate)
      {
         this.delegate = delegate;
      }

      public void run()
      {
         delegate.run();
      }

      public void release()
      {
      }

   }

}
