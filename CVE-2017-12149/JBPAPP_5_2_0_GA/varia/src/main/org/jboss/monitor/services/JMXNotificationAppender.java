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

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.jboss.monitor.alarm.Alarm;
import org.jboss.monitor.alarm.AlarmNotification;
import org.jboss.mx.util.JBossNotificationBroadcasterSupport;
import org.jboss.mx.util.MBeanServerLocator;


/**
 * A log4j Appender that emits the received log events as JMX Notifications
 * 
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public class JMXNotificationAppender extends AppenderSkeleton
   implements JMXNotificationAppenderMBean, NotificationEmitter
{
   public static final String DEFAULT_TYPE = "jboss.alarm.logging";
   
   // Private Data --------------------------------------------------

   private MBeanServer server;
   
   private ObjectName objectName;
   
   private String objectNameString;
   
   private String notificationType;
   
   private JBossNotificationBroadcasterSupport emitter;
   
   // Protected Data ------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
    * CTOR
    */
   public JMXNotificationAppender()
   {
      notificationType = DEFAULT_TYPE;
      server = MBeanServerLocator.locateJBoss();
      emitter = new JBossNotificationBroadcasterSupport();
      
      // Default Threadshold
      setThreshold(Level.WARN);
   }

   // Attributes ----------------------------------------------------
   
   public void setObjectName(String objectNameString) throws Exception
   {
      // in case we've registered before
      unregister();

      if (server != null)
      {
         objectName = new ObjectName(objectNameString);
         server.registerMBean(this, objectName);
      }
      
      this.objectNameString = objectNameString;
   }
   
   public String getObjectName()
   {
      return objectNameString;
   }
   
   public void setNotificationType(String notificationType)
   {
      this.notificationType = notificationType;
   }
   
   public String getNotificationType()
   {
      return notificationType;
   }
   
   // NotificationEmitter implementation ----------------------------
   
   public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
   {
      // delegate
      emitter.addNotificationListener(listener, filter, handback);
   }

   public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException
   {
      // delegate
      emitter.removeNotificationListener(listener);
   }
   
   public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
      throws ListenerNotFoundException
   {
      // delegate
      emitter.removeNotificationListener(listener, filter, handback);
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      // delegate
      return emitter.getNotificationInfo();
   }

   // Appender implementation ---------------------------------------
   
   public void close()
   {
      unregister();
      
      server = null;
      emitter = null;
      objectName = null;
      objectNameString = null;
      notificationType = null;
   }
   
   public boolean requiresLayout()
   {
     return true;
   }
   
   // AppenderSkeleton overrides ------------------------------------
   
   protected void append(LoggingEvent event)
   {
      String msg = super.layout.format(event);

      if (super.layout.ignoresThrowable())
      {
         String[] ts = event.getThrowableStrRep();
         if (ts != null)
         {
            StringBuffer sbuf = new StringBuffer(msg);

            int len = ts.length;
            for (int i = 0; i < len; i++)
            {
               sbuf.append(Layout.LINE_SEP).append(ts[i]);
            }
            
            msg = sbuf.toString();
         }
      }

      // Map level to severity
      Level level = event.getLevel();
      int severity;
      
      if (level == Level.WARN)
      {
         severity = Alarm.SEVERITY_WARNING;
      }
      else if (level == Level.ERROR)
      {
         severity = Alarm.SEVERITY_MAJOR;
      }
      else if (level == Level.FATAL)
      {
         severity = Alarm.SEVERITY_CRITICAL;
      }
      else
      {
         severity = Alarm.SEVERITY_UNKNOWN;
      }

      // create the alarm
      AlarmNotification alarm = new AlarmNotification(
            notificationType, this, null, severity, Alarm.STATE_NONE,
            emitter.nextNotificationSequenceNumber(), event.timeStamp, msg);
      
      emitter.sendNotification(alarm);
   }      
   
   // Private -------------------------------------------------------
   
   private void unregister()
   {
      if (server != null && objectName != null)
      {
         try
         {
            server.unregisterMBean(objectName);
         }
         catch (Exception ignored)
         {
            // ignored
         }
      }      
   }
}
