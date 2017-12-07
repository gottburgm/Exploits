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
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 71554 $
 */
public class QuartzJob implements Job
{
   private static Logger log = Logger.getLogger(QuartzJob.class);

   public QuartzJob()
   {
   }

   public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
   {
      MessageEndpoint endpoint = null;
      try
      {
         MessageEndpointFactory endpointFactory = (MessageEndpointFactory)jobExecutionContext.getJobDetail().getJobDataMap().get("endpointFactory");
         endpoint = endpointFactory.createEndpoint(null);
         if (endpoint != null)
         {
            Job job = (Job) endpoint;
            job.execute(jobExecutionContext);
         }
         else
         {
            log.error("ENDPOINT IS NULL!!!!");
         }
      }
      catch (UnavailableException e)
      {
         throw new JobExecutionException(e);
      }
      finally
      {
         if (endpoint != null)
         {
            endpoint.release();
         }
      }

   }
}
