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
package org.jboss.monitor.services;

import javax.management.Notification;
import javax.management.ObjectName;

import org.jboss.logging.DynamicLogger;
import org.jboss.system.ListenerServiceMBeanSupport;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedLong;

/**
 * A simple JMX notification listener that outputs notifications as log.INFO
 * messages, and demonstrates the usefulness of ListenerServiceMBeanSupport.
 *
 * @jmx:mbean
 *    extends="org.jboss.system.ListenerServiceMBean"
 * 
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public class NotificationListener extends ListenerServiceMBeanSupport
   implements NotificationListenerMBean
{
   
   // Private Data --------------------------------------------------
    
   /** Number of processed JMX notifications */
   private SynchronizedLong notificationCount;
   
   /** Dynamic subscriptions flag */
   private boolean dynamicSubscriptions;
   
   /** Listener MBean */
   private ObjectName notificationListener;
   
   // Protected Data ------------------------------------------------
   
   /** The dynamic logger */
   protected DynamicLogger log = DynamicLogger.getDynamicLogger(super.log.getName());
   
   // Constructors --------------------------------------------------
   
   /**
    * CTOR
    */
   public NotificationListener()
   {
      notificationCount = new SynchronizedLong(0);
      dynamicSubscriptions = true;
      notificationListener = null;
      log.setLogLevel(DynamicLogger.LOG_LEVEL_INFO);
   }
   
   // NotificationListenerMBean Implementation -----------------------
   
   /**
    * Number of notifications received.
    *
    * @jmx:managed-attribute
    */
   public long getNotificationCount()
   {
      return this.notificationCount.get();
   }

   /**
    * Enables/disables dynamic subscriptions
    *
    * @jmx:managed-attribute
    */
   public void setDynamicSubscriptions(boolean dynamicSubscriptions)
   {
      this.dynamicSubscriptions = dynamicSubscriptions;
   }

   /**
    * Gets the dynamic subscriptions status
    *
    * @jmx:managed-attribute
    */
   public boolean getDynamicSubscriptions()
   {
      return this.dynamicSubscriptions;
   }
   
   /**
    * Sets listener of notifications
    *
    * @jmx:managed-attribute
    */
   public void setNotificationListener(ObjectName notificationListener)
   {
      this.notificationListener = notificationListener;
   }
   
   /**
    * Gets listener of notifications
    *
    * @jmx:managed-attribute
    */
   public ObjectName getNotificationListener()
   {
      return this.notificationListener;
   }   
   
   /**
    * Sets the dynamic log level
    * 
    * @jmx:managed-attribute    
    */
   public void setLogLevel(String logLevel)
   {
      log.setLogLevelAsString(logLevel);
   }
   
   /**
    * Gets the dynamic log level
    * 
    * @jmx:managed-attribute    
    */
   public String getLogLevel()
   {
      return log.getLogLevelAsString();
   }
   
   // Lifecycle control (ServiceMBeanSupport) -----------------------
   
   /**
    * Start 
    */
   public void startService() throws Exception
   {
      if (this.notificationListener == null)
      {
         super.subscribe(this.dynamicSubscriptions); // listener is me!
      }
      else
      {
         super.subscribe(this.dynamicSubscriptions, this.notificationListener);
      }
   }
   
   /**
    * Stop
    */
   public void stopService() throws Exception
   {
      // unsubscribe for notifications
      super.unsubscribe();
   }
   
   /**
    * Overriden to add handling!
    */
   public void handleNotification2(Notification notification, Object handback)
   {
      log.log("Got notification (#" + Long.toString(this.notificationCount.increment())
             + "): " + notification + ", handback: " + handback);
   }
}
