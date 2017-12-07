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
package org.jboss.monitor.alarm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.ObjectName;

import org.jboss.system.ServiceMBeanSupport;

/**
 * AlarmManager
 *
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public class AlarmManager
{
   // Private/Protected Data ----------------------------------------
   
   /** Mediates the related MBean */
   protected MBeanImplAccess mbeanImpl;
   
   /** Holds map of maps, each one containing type --> severity mappings */
   private Map nameMap;
   
   // Constructors --------------------------------------------------
   
   /**
    * CTOR
    * 
    * @param mbeanImpl providing access to notification broadcasting
    */
   public AlarmManager(MBeanImplAccess mbeanImpl)
   {
      this.mbeanImpl = mbeanImpl;
      this.nameMap = new HashMap();
   }
   
   /**
    * CTOR
    * 
    * @param service hosting the AlarmManager
    */
   public AlarmManager(final ServiceMBeanSupport service)
   {
      this(new MBeanImplAccess() {
         public ObjectName getMBeanName() { return service.getServiceName(); }
         public long getSequenceNumber() { return service.nextNotificationSequenceNumber(); }
         public void emitNotification(Notification n) { service.sendNotification(n); }
      });
   }

   // High-level part of the interface used to support Statefull Alarms.
   // The sending of the actual AlarmNotifications is done through the
   // sendAlarmNotification() method.
   
   /**
    * Sets the severity of an Alarm, keyed by its type, without producing
    * an AlarmNotification, for the current mbean.
    */
   public void setSeverity(String type, int severity)
   {
      setSeverity(mbeanImpl.getMBeanName(), type, severity);
   }

   /**
    * Sets the severity of an Alarm, keyed by its type, without producing
    * an AlarmNotification, for the specified mbean.
    */   
   public void setSeverity(ObjectName name, String type, int severity)
   {
      synchronized (this)
      {
         // find or add TypeMap
         Map typeMap = getTypeMap(name);
         
         Severity s = (Severity)typeMap.get(type);
         if (s == null)
         {
            typeMap.put(type, new Severity(severity));
         }
         else
         {
            s.severity = severity;
         }
      }
   }
   
   /**
    * Gets the severity of an alarm, keyed by its type, for the current mbean.
    */
   public int getSeverity(String type)
   {
      return getSeverity(mbeanImpl.getMBeanName(), type);
   }
   
   /**
    * Gets the severity of an alarm, keyed by its type, for the specified mbean.
    */   
   public int getSeverity(ObjectName name, String type)
   {
      synchronized (this)
      {
         Map typeMap = (Map)nameMap.get(name);
         if (typeMap == null)
         {
            return Alarm.SEVERITY_NORMAL;
         }
         else
         {
            Severity s = (Severity)typeMap.get(type);
            if (s == null)
            {
               return Alarm.SEVERITY_NORMAL;
            }
            else
            {
               return s.severity;
            }
         }
      }
   }   
   
   /**
    * Gets the severity of an alarm as a String,
    * keyed by its type for the current mbean
    */
   public String getSeverityAsString(String type)
   {
      return getSeverityAsString(mbeanImpl.getMBeanName(), type);
   }
   
   /**
    * Gets the severity of an alarm as a String,
    * keyed by its type for the specified mbean
    */
   public String getSeverityAsString(ObjectName name, String type)
   {
      return Alarm.SEVERITY_STRINGS[getSeverity(name, type)];      
   }

   /**
    * Sets the alarm for the current mbean, keyed by its type.
    * If severity has changed an AlarmNotification will be thrown.
    * The alarmState of the AlarmNotification will be either
    * Alarm.STATE_CREATED, Alarm.STATE_CHANGED or Alarm.STATE_CLEARED.
    */   
   public void setAlarm(String type, int severity, String message, Object userData)
   {
      setAlarm(mbeanImpl.getMBeanName(), type, severity, message, userData);
   }
   
   /**
    * Sets the alarm for the specified target mbean, keyed by its type.
    * If severity has changed an AlarmNotification will be thrown.
    * The alarmState of the AlarmNotification will be either
    * Alarm.STATE_CREATED, Alarm.STATE_CHANGED or Alarm.STATE_CLEARED.
    */   
   public void setAlarm(ObjectName target, String type, int severity, String message, Object userData)
   {
      Severity s;
      synchronized (this)
      {
         Map typeMap = getTypeMap(target);
         s = (Severity)typeMap.get(type);
         
         // if alarm does not exist, add it with a default severity
         if (s == null)
         {
            s = new Severity(Alarm.SEVERITY_NORMAL);
            typeMap.put(type, s);
         }
      }
      
      // There must be a small race condition here if 2 threads
      // set the same severity, thus producing duplicate notifications
      // Not a big deal...
      int oldSeverity = s.severity;
      
      // if the severity has changed, send an AlarmNotification
      if (severity != oldSeverity)
      {
         // store the new severity
         s.severity = severity;
         
         if (severity == Alarm.SEVERITY_NORMAL)
         {
            sendAlarmNotification(
                  target, type, severity, Alarm.STATE_CLEARED, message, userData);
         }
         else if (oldSeverity == Alarm.SEVERITY_NORMAL)
         {
            sendAlarmNotification(
               target, type, severity, Alarm.STATE_CREATED, message, userData);
         }
         else
         {
            sendAlarmNotification(
               target, type, severity, Alarm.STATE_CHANGED, message, userData);
         }
      }
   }
   
   /**
    * See set Alarm above
    *
    * Essentially a helper method that will populate the userData field
    * of the Notification with a HashMap, containing a single key/value pair.
    *
    * Note, that an AlarmNotification will not be emitted if there is no
    * severity change.
    */
   public void setAlarm(String type, int severity, String message, String key, Object value)
   {
      setAlarm(mbeanImpl.getMBeanName(), type, severity, message, key, value);
   }
   
   /**
    * See set Alarm above
    *
    * Essentially a helper method that will populate the userData field
    * of the Notification with a HashMap, containing a single key/value pair.
    *
    * Note, that an AlarmNotification will not be thrown if there is no
    * severity change.
    */
   public void setAlarm(ObjectName target, String type, int severity, String message, String key, Object value)
   {
      HashMap map = new HashMap();
      map.put(key, value);
      setAlarm(target, type, severity, message, map);      
   }
   
   // Low-level part of the interface used to generate and send
   // various types of notifications, including AlarmNotifications
   // corresponding to Stateless Alarms.

   /**
    * Generates and sends an AlarmNotification.
    *
    * source, sequenceNumber, timeStamp
    * will be automatically filled.
    */   
   public void sendAlarm(String type, int severity, String message, String key, Object value)
   {
      sendAlarm(null, type, severity, message, key, value);
   }
   
   /**
    * Generates and sends an AlarmNotification.
    *
    * source, sequenceNumber, timeStamp
    * will be automatically filled.
    */   
   public void sendAlarm(ObjectName target, String type, int severity, String message, String key, Object value)
   {
      HashMap map = new HashMap();
      map.put(key, value);      
      sendAlarm(target, type, severity, message, map);
   }
   
   /**
    * Generates and sends an AlarmNotification.
    *
    * source, sequenceNumber, timeStamp
    * will be automatically filled.
    */   
   public void sendAlarm(String type, int severity, String message, Object userData)
   {
      sendAlarm(null, type, severity, message, userData);
   }
   
   /**
    * Generates and sends an AlarmNotification.
    *
    * source, sequenceNumber, timeStamp
    * will be automatically filled.
    */   
   public void sendAlarm(
      ObjectName target, String type, int severity, String message, Object userData)
   {
      sendAlarmNotification(target, type, severity, Alarm.STATE_NONE, message, userData);
   }
   
   /**
    * Generates and sends an AlarmNotification.
    *
    * An alarmState of Alarm.STATE_CLEARED forces severity to SEVERITY_NORMAL
    *
    * source, sequenceNumber, timeStamp
    * will be automatically filled.
    */   
   protected void sendAlarmNotification(
      ObjectName target, String type, int severity, int alarmState, String message, Object userData)
   {
      Notification n = new AlarmNotification(
         type,
         mbeanImpl.getMBeanName(), // source
         target,
         severity,           
         alarmState,         
         this.mbeanImpl.getSequenceNumber(),
         System.currentTimeMillis(),
         message
      );
      n.setUserData(userData);
      
      // send it away
      mbeanImpl.emitNotification(n);
   }
   
   /**
    * Generates and sends an AttributeChangeNotification.
    *
    * source, sequenceNumber, timeStamp
    * will be automatically filled in.
    */   
   public void sendAttributeChangeNotification(
      String type, String message, Object userData,
      String attributeName, String attributeType,
      Object oldValue, Object newValue)
   {
      Notification n = new AttributeChangeNotification(
         mbeanImpl.getMBeanName(), // source
         mbeanImpl.getSequenceNumber(),
         System.currentTimeMillis(),
         message,
         attributeName,
         attributeType,
         oldValue,
         newValue
      );
      n.setUserData(userData);
      
      // send it away
      mbeanImpl.emitNotification(n);
   }
   
   /**
    * Generates and sends a simple Notification.
    *
    * source, sequenceNumber, timeStamp
    * will be automatically filled in.
    */   
   public void sendNotification(String type, String message, Object userData)
   {
      Notification n = new Notification(
         type,
         mbeanImpl.getMBeanName(), // source
         mbeanImpl.getSequenceNumber(),
         System.currentTimeMillis(),
         message
      );
      n.setUserData(userData);
      
      // send it away
      mbeanImpl.emitNotification(n);
   }
   
   /**
    * Clear all the stored severities
    *
    */
   public void clear()
   {
      synchronized (this)
      {
         for (Iterator i = nameMap.entrySet().iterator(); i.hasNext(); )
         {
            Map.Entry entry = (Map.Entry)i.next();
            Map typeMap = (Map)entry.getValue();
            typeMap.clear();
         }
         nameMap.clear();
      }
   }
   
   // Private -------------------------------------------------------
   
   /**
    * Return the typeMap for an ObjectName, create it if needed
    */
   private Map getTypeMap(ObjectName name)
   {
      Map typeMap = (Map)nameMap.get(name);
      if (typeMap == null)
      {
         typeMap = new HashMap();
         nameMap.put(name, typeMap);
      }
      return typeMap;
   }
   
   // Inner Class ---------------------------------------------------
   
   /**
    * Simple Data Holder
    */
   private static class Severity
   {
      public int severity;
      
      public Severity(int severity)
      {
         this.severity = severity;
      }
   }
   
}
