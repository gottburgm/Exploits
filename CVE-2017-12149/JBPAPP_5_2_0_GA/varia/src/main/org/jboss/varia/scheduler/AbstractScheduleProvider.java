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

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.MBeanServerInvocationHandler;

import org.jboss.system.ServiceMBeanSupport;

/**
 * Abstract Base Class for Schedule Providers.
 *
 * The class used to extend HASingletonSupport, but not anymore.
 * We can achieve the same effect without a dependency on HA jars
 * using just a depends clause that starts/stops the schedule provide
 * using the notifications produced by another HASingleton (see JBAS-3082) 
 *
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author <a href="mailto:ivelin@apache.org">Ivelin Ivanov</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public abstract class AbstractScheduleProvider extends ServiceMBeanSupport
   implements AbstractScheduleProviderMBean
{
   /** The schedule manager ObjectName */
   private ObjectName scheduleManagerName = ScheduleManagerMBean.OBJECT_NAME;
   private ScheduleManagerMBean manager;

   // ------------------------------------------------------------------------
   // Constructors
   //-------------------------------------------------------------------------

   /**
    * Default (no-args) Constructor
    */
   public AbstractScheduleProvider()
   {   
   }

   // -------------------------------------------------------------------------
   // SchedulerMBean Attributes
   // -------------------------------------------------------------------------

   /**
    * Get the Schedule Manager Name
    *
    * @jmx:managed-operation
    */
   public ObjectName getScheduleManagerName()
   {
      return scheduleManagerName;
   }

   /**
    * Set the Schedule Manager Name
    *
    * @jmx:managed-operation
    */
   public void setScheduleManagerName(ObjectName scheduleManagerName)
   {
      this.scheduleManagerName = scheduleManagerName;
   }

   // -------------------------------------------------------------------------
   // ScheduleMBean Operations - Override
   // -------------------------------------------------------------------------
   
   /**
    * Add the Schedules to the Schedule Manager
    *
    * @jmx:managed-operation
    */
   public abstract void startProviding() throws Exception;

   /**
    * Stops the Provider from providing and
    * causing him to remove all Schedules
    *
    * @jmx:managed-operation
    */
   public abstract void stopProviding();

   // -------------------------------------------------------------------------
   // Helper Methods
   // -------------------------------------------------------------------------
   
   /**
    * Add a single Schedule to the Schedule Manager
    *
    * @param pTarget Object Name of the target MBean (receiver
    *                of the time notification)
    * @param pMethodName Name of the Method to be called on the
    *                    target
    * @param pMethodSignature Signature of the Method
    * @param pStart Date when the Schedule has to start
    * @param pPeriod Time between two notifications
    * @param pRepetitions Number of repetitions (-1 for unlimited)
    *
    * @return Identification of the Schedule which is used
    *         to remove it later
    */
   protected int addSchedule(
         ObjectName pTarget, String pMethodName, String[] pMethodSignature,
         Date pStart, long pPeriod, int pRepetitions) throws JMException
   {
	  return manager.addSchedule(serviceName,
                        pTarget,
                        pMethodName,
                        pMethodSignature,
                        pStart,
                        pPeriod,
                        pRepetitions);
   }

   /**
    * Remove a Schedule from the Schedule Manager
    *
    * @param pID Identification of the Schedule
    */
   protected void removeSchedule(int pID) throws JMException
   {
	  manager.removeSchedule(pID);
   }

   // -------------------------------------------------------------------------
   // ServiceMBean Overrides and Lifecycle Methods
   // -------------------------------------------------------------------------

   /**
    * When the Service is started it will register itself at the
    * Schedule Manager which makes it necessary that the Schedule Manager
    * is already running.
    * This allows the Schedule Manager to call {@link #startProviding
    * startProviding()} which is the point for the Provider to add
    * the Schedules on the Schedule Manager.
    * ATTENTION: If you overwrite this method in a subclass you have
    * to call this method (super.startService())
    */   
   protected void startService() throws Exception
   {
	  this.manager = (ScheduleManagerMBean)MBeanServerInvocationHandler.newProxyInstance(
			getServer(), scheduleManagerName, ScheduleManagerMBean.class, false);
      startScheduleProviderService();
   }

   /**
    * When the Service is stopped it will unregister itself at the
    * Schedule Manager.
    * This allows the Schedule Manager to remove the Provider from its
    * list and then call {@link #stopProviding stopProviding()} which
    * is the point for the Provider to remove the Schedules from the
    * Schedule Manager.
    * ATTENTION: If you overwrite this method in a subclass you have
    * to call this method (super.stopService())
    */   
   protected void stopService() throws Exception
   {
      stopScheduleProviderService();
   }
   
   /**
    * Registers this schedule provider to the schedule manager
    */
   protected void startScheduleProviderService()
      throws InstanceNotFoundException, MBeanException, ReflectionException
   {
	  manager.registerProvider(serviceName.toString());
   }

   /**
    * Unregisters this schedule provider to the schedule manager
    */
   protected void stopScheduleProviderService()
      throws InstanceNotFoundException, MBeanException, ReflectionException
   {
	  manager.unregisterProvider(serviceName.toString());
   }
   
}
