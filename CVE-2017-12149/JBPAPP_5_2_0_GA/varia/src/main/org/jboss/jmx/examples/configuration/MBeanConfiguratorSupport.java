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
package org.jboss.jmx.examples.configuration;

import java.io.Serializable;

import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.services.binding.ServiceBindingManager;
import org.jboss.services.binding.ServiceBindingManagerMBean;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 * @author Brian Stansberry
 */
public abstract class MBeanConfiguratorSupport extends ServiceMBeanSupport 
   implements MBeanConfiguratorSupportMBean, NotificationListener
{

   private static final Logger log = Logger.getLogger(MBeanConfiguratorSupport.class.getName());

   private ServiceBindingManager serviceBindingManager;
   
   /**
    * Use the short class name as the default for the service name.
    */
   public String getName()
   {
      return "MBeanConfiguratorSupport";
   }

   public void startService()
   {
      try
      {
         log.debug("Starting MBeanConfiguratorSupport service.");
         
         ObjectName mbeanserver = new ObjectName("JMImplementation:type=MBeanServerDelegate");
         RegistrationNotificationFilter mbeanServerNotificationFilter = new RegistrationNotificationFilter();
         getServer().addNotificationListener(mbeanserver, this, mbeanServerNotificationFilter, null);
      }
      catch (Exception e)
      {
         log.error("Could not start MBeanConfiguratorSupport service.", e);
      }
   }

   public ServiceBindingManager getServiceBindingManager()
   {
      return serviceBindingManager;
   }

   public void setServiceBindingManager(ServiceBindingManager serviceBindingManager)
   {
      this.serviceBindingManager = serviceBindingManager;
   }

   public abstract void mbeanRegistered(ObjectName objectName)
         throws Exception;

   /**
    * Callback method from the broadcaster MBean this listener implementation
    * is registered to.
    *
    * @param notification the notification object
    * @param handback     the handback object given to the broadcaster
    *                     upon listener registration
    */
   public void handleNotification(Notification notification, Object handback)
   {
      //Should be a mbean registration notification due to RegistrationNotificationFilter being used.
      if (notification instanceof MBeanServerNotification)
      {
         if (notification.getType().equals(MBeanServerNotification.REGISTRATION_NOTIFICATION))
         {
            MBeanServerNotification serverNotification = (MBeanServerNotification) notification;
            try
            {
               mbeanRegistered(serverNotification.getMBeanName());
            }
            catch (Exception e)
            {
               log.error("Error configuring mbean " + serverNotification.getMBeanName(), e);
            }
         }
      }

   }

   public class RegistrationNotificationFilter implements NotificationFilter, Serializable
   {

      /**
       * This method is called before a notification is sent to see whether
       * the listener wants the notification.
       *
       * @param notification the notification to be sent.
       * @return true if the listener wants the notification, false otherwise
       */
      public boolean isNotificationEnabled(Notification notification)
      {
         boolean processNotification = false;
         if (notification instanceof MBeanServerNotification)
         {
            if (notification.getType().equals(MBeanServerNotification.REGISTRATION_NOTIFICATION))
            {
               processNotification = true;
            }
         }
         return processNotification;
      }
   }
}