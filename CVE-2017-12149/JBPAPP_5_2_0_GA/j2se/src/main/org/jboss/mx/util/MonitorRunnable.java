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
import java.util.Map;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.monitor.Monitor;
import javax.management.monitor.MonitorNotification;

import org.jboss.logging.Logger;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81019 $
 */
public class MonitorRunnable
   extends SchedulableRunnable
{
   private static final Logger log = Logger.getLogger(MonitorRunnable.class);
   
   /**
    * The scheduler.
    */
   static RunnableScheduler scheduler;

   /**
    * Start the scheduler
    */
   static
   {
      scheduler = new RunnableScheduler();
      scheduler.start();
   }

   // Attributes ----------------------------------------------------

   // The monitoring to perform
   private Monitor monitor;
   private ObjectName monitorName;
   private MonitorCallback callback;
   private Map observedObjects;
   private MBeanServer server;

   // Constructors --------------------------------------------------

   /**
    * Create a monitor runnable to periodically perform monitoring.
    *
    * @param monitor the monitoring to perform.
    */
   public MonitorRunnable(Monitor monitor, ObjectName monitorName,
      MonitorCallback callback, Map observedObjects, MBeanServer server)
   {
      this.monitor = monitor;
      this.monitorName = monitorName;
      this.callback = callback;
      this.observedObjects = observedObjects;
      this.server = server;
      setScheduler(scheduler);
   }

   // Public --------------------------------------------------------

   /**
    * Run the monitor.<p>
    *
    * Retrieves the monitored attribute and passes it to each service.<p>
    *
    * Peforms the common error processing.
    *
    * @param object the mbean to run the monitor on
    */
   void runMonitor(ObservedObject object)
   {
      // Monitor for uncaught errors
      try
      {
         MBeanInfo mbeanInfo = null;
         try
         {
            mbeanInfo = server.getMBeanInfo(object.getObjectName());
         }
         catch (InstanceNotFoundException e)
         {
            sendObjectErrorNotification(object, "The observed object is not registered.");
            return;
         }

         // Get the attribute information
         MBeanAttributeInfo[] mbeanAttributeInfo = mbeanInfo.getAttributes();
         MBeanAttributeInfo attributeInfo = null;
         for (int i = 0; i < mbeanAttributeInfo.length; i++)
         {
            if (mbeanAttributeInfo[i].getName().equals(monitor.getObservedAttribute()))
            {
               attributeInfo = mbeanAttributeInfo[i];
               break;
            }
         }

         // The attribute must exist
         if (attributeInfo == null)
         {
            sendAttributeErrorNotification(object,
               "The observed attribute does not exist");
            return;
         }

         // The attribute must exist
         if (!attributeInfo.isReadable())
         {
            sendAttributeErrorNotification(object, "Attribute not readable.");
            return;
         }

         // Get the value
         Object value = null;
         try
         {
            value = server.getAttribute(object.getObjectName(), monitor.getObservedAttribute());
         }
         catch (InstanceNotFoundException e)
         {
            sendObjectErrorNotification(object, "The observed object is not registered.");
            return;
         }

         // Check for null value
         if (value == null)
         {
            sendAttributeTypeErrorNotification(object, "Attribute is null");
            return;
         }

         // Now pass the value to the respective monitor.
         callback.monitorCallback(object, attributeInfo, value);
      }
      // Notify an unexcepted error
      catch (Throwable e)
      {
         log.debug("Error in monitor ", e);
         sendRuntimeErrorNotification(object, "General error: " + e.toString());
      }
   }

   /**
    * Run the montior
    */
   public void doRun()
   {
      // Perform the monitoring
      runMonitor();
 
      // Reschedule
      setNextRun(System.currentTimeMillis() + monitor.getGranularityPeriod());
   }

   /**
    * Run the monitor on each observed object
    */
   void runMonitor()
   {
      // Loop through each mbean
      boolean isActive = monitor.isActive();
      for (Iterator i = observedObjects.values().iterator(); i.hasNext() && isActive;)
         runMonitor((ObservedObject) i.next());
   }

   /**
    * Sends the notification
    *
    * @param object the observedObject.
    * @param type the notification type.
    * @param timestamp the time of the notification.
    * @param message the human readable message to send.
    * @param attribute the attribute name.
    * @param gauge the derived gauge.
    * @param trigger the trigger value.
    */
   void sendNotification(ObservedObject object, String type, long timestamp, String message,
      String attribute, Object gauge, Object trigger)
   {
      MonitorNotification n = callback.createNotification(type,
         monitorName, timestamp, message, gauge, attribute,
         object.getObjectName(), trigger);
      monitor.sendNotification(n);
   }

   /**
    * Send a runtime error notification.
    *
    * @param object the observedObject.
    * @param message the human readable message to send.
    */
   void sendRuntimeErrorNotification(ObservedObject object, String message)
   {
      if (object.notAlreadyNotified(ObservedObject.RUNTIME_ERROR_NOTIFIED))
         sendNotification(object, MonitorNotification.RUNTIME_ERROR, 0,
            message, monitor.getObservedAttribute(), null, null);
   }

   /**
    * Send an object error notification.
    *
    * @param object the observedObject.
    * @param message the human readable message to send.
    */
   void sendObjectErrorNotification(ObservedObject object, String message)
   {
      if (object.notAlreadyNotified(ObservedObject.OBSERVED_OBJECT_ERROR_NOTIFIED))
         sendNotification(object, MonitorNotification.OBSERVED_OBJECT_ERROR, 0,
            message, monitor.getObservedAttribute(), null, null);
   }

   /**
    * Send an attribute error notification.
    *
    * @param object the observedObject.
    * @param message the human readable message to send.
    */
   void sendAttributeErrorNotification(ObservedObject object, String message)
   {
      if (object.notAlreadyNotified(ObservedObject.OBSERVED_ATTRIBUTE_ERROR_NOTIFIED))
         sendNotification(object, MonitorNotification.OBSERVED_ATTRIBUTE_ERROR, 0,
            message, monitor.getObservedAttribute(), null, null);
   }

   /**
    * Send an attribute type error notification.
    *
    * @param object the observedObject.
    * @param message the human readable message to send.
    */
   void sendAttributeTypeErrorNotification(ObservedObject object, String message)
   {
      if (object.notAlreadyNotified(ObservedObject.OBSERVED_ATTRIBUTE_TYPE_ERROR_NOTIFIED))
         sendNotification(object, MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR, 0,
            message, monitor.getObservedAttribute(), null, null);
   }

}
