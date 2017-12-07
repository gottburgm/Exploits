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
package org.jboss.monitor;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.logging.Logger;

import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.ReflectionException;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81038 $
 *
 **/
public class StringThresholdMonitor extends JBossMonitor implements StringThresholdMonitorMBean
{
   protected String thresholdString;
   protected boolean equalityTriggersAlert;

   protected void testThreshold()
   {
      if (alertSent) return;
      String value = null;
      try
      {
         value = (String)getServer().getAttribute(observedObject, attribute);
         if (equalityTriggersAlert) // alert trigger when value == threshold
         {
            if (!thresholdString.equals(value))
            {
               return;
            }
         }
         else // alert triggered when value != threshold
         {
            if (thresholdString.equals(value))
            {
               return;
            }
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Failed to compare threshold, mbean failure");
      }

      alertSent = true;
      triggerTime = System.currentTimeMillis();
      triggeredAttributeValue = value;

      StringThresholdNotification notification = new StringThresholdNotification(monitorName, getServiceName(), observedObject,
              attribute, value, thresholdString, equalityTriggersAlert,
              getNextNotificationSequenceNumber());
      this.sendNotification(notification);
   }

   public String getThreshold()
   {
      return thresholdString;
   }

   public void setThreshold(String val)
   {
      thresholdString = val;
   }

   public boolean getEqualityTriggersAlert()
   {
      return equalityTriggersAlert;
   }

   public void setEqualityTriggersAlert(boolean compareEqual)
   {
      this.equalityTriggersAlert = compareEqual;
   }
}
