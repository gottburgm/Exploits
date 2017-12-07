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
package org.jboss.varia.scheduler;

import java.util.Date;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;

/**
 * ScheduleManagerMBean interface.
 * 
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public interface ScheduleManagerMBean extends ServiceMBean
{
   /** The default ObjectName */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss:service=ScheduleMBean");

   // Attributes ----------------------------------------------------
   
   /** Whether the scheduler should be started upon MBean start or not */
   void setStartAtStartup(boolean startAtStartup);
   boolean isStartAtStartup();

   /** The JMX timer to use for the actual scheduling */
   void setTimerName(String timerObjectName);
   String getTimerName();
   
   /** The default scheduling to use, fixed-rate or fixed-delay (false, default) */
   void setFixedRate(boolean fixedRate);
   boolean getFixedRate();
   
   // Operations ----------------------------------------------------
   
   /**
    * Starts all the registered Schedules
    */
   void startSchedules();

   /**
    * Stops all the registered Schedules
    * 
    * @param doItNow currently ignored
    */
   void stopSchedules(boolean doItNow);

   /**
    * Stops and restarts the service
    */
   void restartSchedule();

   /**
    * Register a Schedule Provider to make him available. This method
    * calls startProviding() on the Provider to indicate that the
    * Provider can start adding Schedules.
    * 
    * @param providerObjectName Object Name of the Provider
    */
   void registerProvider(String providerObjectName);

   /**
    * Unregister a Schedule Provider which in turn calls back stopProviding()
    * to indicate to the Provider that it should remove all the Schedules.
    * 
    * @param providerObjectName Object Name of the Provider
    */
   void unregisterProvider(String providerObjectName);

   /**
    * Adds a new Schedule to the Scheduler
    * @param target Object Name of the Target MBean
    * @param methodName Name of the method to be called
    * @param methodSignature List of Attributes of the method to be called where ...
    * @param startDate Date when the schedule is started
    * @param repetitions Initial Number of repetitions
    * @return identification of the Schedule used later to remove it if necessary
    */
   int addSchedule(ObjectName provider, ObjectName target, String methodName, String[] methodSignature,
         Date startDate, long period, int repetitions);

   /**
    * Removes a Schedule so that no notification is sent anymore
    * @param identification id returned by {@link #addSchedule addSchedule()}.
    */
   void removeSchedule(int identification);

}
