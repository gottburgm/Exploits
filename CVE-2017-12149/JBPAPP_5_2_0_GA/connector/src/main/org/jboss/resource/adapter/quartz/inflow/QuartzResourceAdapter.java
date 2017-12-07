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

import org.jboss.logging.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.ResourceException;
import javax.transaction.xa.XAResource;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 102427 $
 */
public class QuartzResourceAdapter implements ResourceAdapter
{
   private static Logger log = Logger.getLogger(QuartzResourceAdapter.class);

   private static final ThreadLocal holder = new ThreadLocal();

   private Scheduler sched;

   public static WorkManager getConfigTimeWorkManager() {
      return (WorkManager) holder.get();
   }

   public void start(BootstrapContext ctx) throws ResourceAdapterInternalException
   {
      log.debug("start quartz!!!");
      // First we must get a reference to a scheduler
      SchedulerFactory sf = new StdSchedulerFactory();
      try
      {
         holder.set(ctx.getWorkManager());
         sched = sf.getScheduler();
         SecurityActions.startScheduler(sched);
      }
      catch (SchedulerException e)
      {
         throw new ResourceAdapterInternalException(e);
      }
      finally
      {
         holder.set(null);
      }
   }

   public void stop()
   {
      log.debug("stop");
      try
      {
         SecurityActions.shutdownScheduler(sched);
      }
      catch (SchedulerException e)
      {
         throw new RuntimeException(e);
      }
   }

   public void endpointActivation(MessageEndpointFactory endpointFactory,
                                  ActivationSpec spec)
      throws ResourceException
   {
      log.debug("endpointActivation, spec="+spec);
      QuartzActivationSpec quartzSpec = (QuartzActivationSpec) spec;

      // allocate instance of endpoint to figure out its endpoint interface
      Class clazz = QuartzJob.class;
      MessageEndpoint tmpMe = endpointFactory.createEndpoint(null);
      if (tmpMe instanceof StatefulJob) clazz = StatefulQuartzJob.class;
      tmpMe.release();

      try
      {
         JobDetail jobDetail = new JobDetail(quartzSpec.getJobName(), quartzSpec.getJobGroup(), clazz, true, false, false);
         jobDetail.getJobDataMap().setAllowsTransientData(true);
         jobDetail.getJobDataMap().put("endpointFactory", endpointFactory);
         log.debug("adding job: " + quartzSpec);
         CronTrigger trigger = new CronTrigger(quartzSpec.getTriggerName(), quartzSpec.getTriggerGroup(), quartzSpec.getCronTrigger());
         sched.scheduleJob(jobDetail, trigger);
      }
      catch (Exception e)
      {
         log.error(e);
         throw new ResourceException(e);
      }
   }

   public void endpointDeactivation(MessageEndpointFactory endpointFactory,
                                    ActivationSpec spec)
   {
      QuartzActivationSpec quartzSpec = (QuartzActivationSpec) spec;
      try
      {
         log.debug("****endpointDeactivation: " + quartzSpec);
         sched.deleteJob(quartzSpec.getJobName(), quartzSpec.getJobGroup());
      }
      catch (SchedulerException e)
      {
         throw new RuntimeException(e);
      }
   }

   public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException
   {
      return new XAResource[0];
   }

}
