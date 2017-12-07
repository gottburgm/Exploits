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

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.ResourceException;
import java.io.Serializable;

/** The encapsulation of the mail folder endpoint specification
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 71554 $
 */
public class QuartzActivationSpec
   implements ActivationSpec, Serializable
{
   /** The resource adapter */
   private transient ResourceAdapter ra;

   private static long counter;

   private static synchronized long getCounter()
   {
      return counter++;
   }

   public QuartzActivationSpec()
   {
      
   }

   private String jobName = "job." + getCounter() + "." + System.currentTimeMillis();
   private String jobGroup = "default";
   private String triggerName = "trigger." + getCounter() + "." + System.currentTimeMillis();
   private String triggerGroup = "default";
   private String cronTrigger;
   private boolean stateful;

   //---- required ActivationSpec methods

   public void validate() throws InvalidPropertyException
   {
   }

   public ResourceAdapter getResourceAdapter()
   {
      return ra;
   }

   public void setResourceAdapter(ResourceAdapter ra) throws ResourceException
   {
      this.ra = ra;
   }

   //-- Java bean methods

   public boolean isStateful()
   {
      return stateful;
   }

   public void setStateful(boolean stateful)
   {
      this.stateful = stateful;
   }

   public String getJobName()
   {
      return jobName;
   }

   public void setJobName(String jobName)
   {
      this.jobName = jobName;
   }

   public String getJobGroup()
   {
      return jobGroup;
   }

   public void setJobGroup(String jobGroup)
   {
      this.jobGroup = jobGroup;
   }

   public String getTriggerName()
   {
      return triggerName;
   }

   public void setTriggerName(String triggerName)
   {
      this.triggerName = triggerName;
   }

   public String getTriggerGroup()
   {
      return triggerGroup;
   }

   public void setTriggerGroup(String triggerGroup)
   {
      this.triggerGroup = triggerGroup;
   }

   public String getCronTrigger()
   {
      return cronTrigger;
   }

   public void setCronTrigger(String cronTrigger)
   {
      this.cronTrigger = cronTrigger;
   }

   public String toString()
   {
      return "jobName=" + jobName + ",jobGroup="+jobGroup+",triggerName="+triggerName+",triggerGroup="+triggerGroup+",cronTrigger="+cronTrigger;
   }


}
