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

import java.util.Date;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.timer.TimerMBean;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.Strings;

/**
 * A simple service used to configure the periodic emition of notifications
 * by a standard JMX Timer.
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public class TimerService extends ServiceMBeanSupport
   implements TimerServiceMBean
{
   // Private Data --------------------------------------------------
   
   /** Notification type */
   private String notificationType;
   
   /** Notification message */
   private String notificationMessage;
   
   /** Timer period string */
   private String timerPeriodString;
   
   /** Number of occurences */
   private long repeatitions;
   
   /** Periodic execution mode */
   private boolean fixedRate;
   
   /** TimerMBean name */
   private ObjectName timerObjectName;
   
   /** Timer period as long */
   private long timerPeriod;
   
   /** Proxy to the TimerMBean */
   private TimerMBean timerProxy;

   /** The timer subscription id */
   private Integer id;
   
   // Constructors --------------------------------------------------
   
   /**
    * CTOR
    */
   public TimerService()
   {
      // empty
   }
   
   // Attributes -----------------------------------------------------
   
   /**
    * @jmx:managed-attribute
    */
   public void setNotificationType(String type)
   {
      this.notificationType = type;
   }
   
   /**
    * @jmx:managed-attribute
    */   
   public String getNotificationType()
   {
      return notificationType;
   }

   /**
    * @jmx:managed-attribute
    */ 
   public void setNotificationMessage(String message)
   {
      this.notificationMessage = message;
   }
   
   /**
    * @jmx:managed-attribute
    */ 
   public String getNotificationMessage()
   {
      return notificationMessage;
   }
   
   /**
    * @jmx:managed-attribute
    */ 
   public void setTimerPeriod(String timerPeriod)
   {
      this.timerPeriod = Strings.parsePositiveTimePeriod(timerPeriod);
      this.timerPeriodString = timerPeriod;
   }
   
   /**
    * @jmx:managed-attribute
    */ 
   public String getTimerPeriod()
   {
      return this.timerPeriodString;
   }
   
   /**
    * @jmx:managed-attribute
    */    
   public void setRepeatitions(long repeatitions)
   {
      this.repeatitions = repeatitions;
   }
   
   /**
    * @jmx:managed-attribute
    */    
   public long getRepeatitions()
   {
      return repeatitions;
   }
   
   /**
    * @jmx:managed-attribute
    */     
   public void setFixedRate(boolean fixedRate)
   {
      this.fixedRate = fixedRate;
   }
   
   /**
    * @jmx:managed-attribute
    */     
   public boolean getFixedRate()
   {
      return fixedRate;
   }
   
   /**
    * @jmx:managed-attribute
    */ 
   public void setTimerMBean(ObjectName timerMBean)
   {
      this.timerProxy = (TimerMBean)MBeanServerInvocationHandler
         .newProxyInstance(getServer(), timerMBean, TimerMBean.class, false);
      this.timerObjectName = timerMBean;
   }
   
   /**
    * @jmx:managed-attribute
    */ 
   public ObjectName getTimerMBean()
   {
      return timerObjectName;
   }
   
   // Lifecycle control (ServiceMBeanSupport) -----------------------
   
   /**
    * Start 
    */
   public void startService() throws Exception
   {
      if (timerProxy != null)
      {
         id = timerProxy.addNotification(
               notificationType,
               notificationMessage,
               null, // UserData
               new Date(), // now
               timerPeriod,
               repeatitions,
               fixedRate);
         
         log.debug("Added timer notification(" + id
               + ") : type=" + notificationType
               + ", message=" + notificationMessage
               + ", period=" + timerPeriodString
               + ", repeatitions=" + repeatitions
               + ", fixedRate=" + fixedRate
               + ", to timer '" + timerObjectName + "'");
      }
      else
      {
         log.warn("TimerMBean not set");
      }
   }
   
   /**
    * Stop
    */
   public void stopService()
   {
      if (id != null)
      {
         try
         {
            timerProxy.removeNotification(id);
            log.debug("Removed timer notification(" + id + ") from timer '" + timerObjectName + "'");
         }
         catch (InstanceNotFoundException ignore)
         {
            // after a fixed number of repeatitions
            // the notification is removed from the timer
         }
         id = null;
      }
   }
   
}
