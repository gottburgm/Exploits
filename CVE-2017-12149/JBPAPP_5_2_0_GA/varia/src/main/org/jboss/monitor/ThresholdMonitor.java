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

import org.jboss.util.NestedRuntimeException;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81038 $
 *
 **/
public class ThresholdMonitor extends JBossMonitor
   implements ThresholdMonitorMBean, Runnable
{
   protected Number thresholdValue;
   protected int compareTo;
   protected Class attributeClass;

   public ThresholdMonitor() {}

   protected void parseThresholdValue()
   {
      if (attributeClass.equals(Long.class))
      {
         thresholdValue = new Long(Long.parseLong(thresholdString == null ? "0" : thresholdString));
         return;
      }
      else if (attributeClass.equals(Integer.class))
      {
         thresholdValue = new Integer(Integer.parseInt(thresholdString == null ? "0" : thresholdString));
         return;
      }
      else if (attributeClass.equals(Double.class))
      {
         thresholdValue = new Double(Double.parseDouble(thresholdString == null ? "0" : thresholdString));
         return;
      }
      else if (attributeClass.equals(Float.class))
      {
         thresholdValue = new Float(Float.parseFloat(thresholdString == null ? "0" : thresholdString));
         return;
      }
      else if (attributeClass.equals(Short.class))
      {
         thresholdValue = new Short(Short.parseShort(thresholdString == null ? "0" : thresholdString));
         return;
      }
      else if (attributeClass.equals(Byte.class))
      {
         thresholdValue = new Byte(Byte.parseByte(thresholdString == null ? "0" : thresholdString));
         return;
      }
      throw new RuntimeException("Failed to parse threshold string: " + thresholdString + " attributeClass: " + attributeClass);

   }

   protected int compare(Object value)
   {
      parseThresholdValue();
      if (attributeClass.equals(Long.class))
      {
         return ((Long) thresholdValue).compareTo((Long)value);
      }
      else if (attributeClass.equals(Integer.class))
      {
         return ((Integer) thresholdValue).compareTo((Integer)value);
      }
      else if (attributeClass.equals(Double.class))
      {
         return ((Double) thresholdValue).compareTo((Double) value);
      }
      else if (attributeClass.equals(Float.class))
      {
         return ((Float) thresholdValue).compareTo((Float) value);
      }
      else if (attributeClass.equals(Short.class))
      {
         return ((Short) thresholdValue).compareTo((Short) value);
      }
      else if (attributeClass.equals(Byte.class))
      {
         return ((Byte) thresholdValue).compareTo((Byte) value);
      }
      throw new RuntimeException("Failed to compare threshold, unknown type");
   }

   protected void startService() throws Exception
   {
      Object val = this.getServer().getAttribute(observedObject, attribute);
      attributeClass = val.getClass();
      super.startService();
   }

   protected void testThreshold()
   {
      if (alertSent) return;
      Object value = null;
      try
      {
         value = getServer().getAttribute(observedObject, attribute);
         if (compare(value) != compareTo) return;
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException("Failed to compare threshold, mbean failure", e);
      }

      alertSent = true;
      triggerTime = System.currentTimeMillis();
      triggeredAttributeValue = value;

      ThresholdNotification notification = new ThresholdNotification(monitorName, getServiceName(), observedObject,
              attribute, (Number) value, thresholdValue,
              getNextNotificationSequenceNumber());
      this.sendNotification(notification);
   }

   public int getCompareTo()
   {
      return compareTo;
   }

   public void setCompareTo(int compare)
   {
      compareTo = compare;
   }

   public Number getThresholdValue()
   {
      return thresholdValue;
   }

   public void setThreshold(String val)
   {
      thresholdString = val;
   }
}
