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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.management.Notification;
import javax.management.ObjectName;

import org.jboss.system.ServiceMBeanSupport;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedLong;

/**
 * AlarmTable
 *
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public class AlarmTable
{
   // Private/Protected Data ----------------------------------------

   /** Mediates the related MBean */
   protected MBeanImplAccess mbeanImpl;
   
   /** The serverId to use when producing AlarmTableNotification alarmIds */
   private String serverId;
   
   /** Counter the help produce unique ids: serverId-alarmIdCount */
   private SynchronizedLong alarmIdCount;   
   
   /** The active alarm table, maps AlarmId(String) -> AlarmTableNotification */
   private Map alarmMap;
   
   /** Maps AlarmKey -> AlarmId(String), for stateful alarms */
   private Map statefulMap;

   /** Maximum number of entries to keep */
   private int maxSize = -1;
   
   // Constructors --------------------------------------------------
   
   /**
    * CTOR
    */
   public AlarmTable(MBeanImplAccess mbeanImpl)
   {
      this.mbeanImpl = mbeanImpl;
      this.alarmIdCount = new SynchronizedLong(0);
      this.alarmMap = new LinkedHashMap();
      this.statefulMap = new HashMap();
   }
   
   /**
    * CTOR
    * 
    * @param service hosting the AlarmManager
    */
   public AlarmTable(final ServiceMBeanSupport service)
   {
      this(new MBeanImplAccess() {
         public ObjectName getMBeanName() { return service.getServiceName(); }
         public long getSequenceNumber() { return service.nextNotificationSequenceNumber(); }
         public void emitNotification(Notification n) { service.sendNotification(n); }
      });
   }
   
   // AlarmTable Implementation -------------------------------------

   /**
    * Sets the serverId
    */   
   public void setServerId(String serverId)
   {
      this.serverId = serverId;
   }
   
   /**
    * Gets the serverId
    */   
   public String getServerId()
   {
      return serverId;
   }

   /**
    * Sets the maximum number of entries to keep
    * -1 equals to no limit.
    */
   public void setMaxSize(int maxSize)
   {
      this.maxSize = maxSize;
   }

   /**
    * Gets the maximum number of entries to keep
    */
   public int getMaxSize()
   {
      return maxSize;
   }
   
   /**
    * Update the AlarmTable based on the incoming Notification
    */   
   public void update(Notification n)
   {
      if (n instanceof AlarmTableNotification)
      {
         // ignore - those notification are
         // meant to be produced only by me
      }
      else if (n instanceof AlarmNotification)
      {
         AlarmNotification an = (AlarmNotification)n;
         
         if (an.getAlarmState() == Alarm.STATE_NONE)
         {
            updateNotificationStateless(n, an.getSeverity());
         }
         else
         {
            updateNotificationStatefull(an);
         }
      }
      else
      {
         updateNotificationStateless(n, Alarm.SEVERITY_UNKNOWN);
      }
   }

   /**
    * Acknowledge an Alarm
    *
    * @return true if ack was succesful, false otherwise
    *         (not in table or acked already)
    */
   public boolean acknowledge(String alarmId, String user, String system)
   {
      AlarmTableNotification atn;
      
      synchronized (this)
      {
         AlarmTableNotification entry =
            (AlarmTableNotification)alarmMap.get(alarmId);
         
         if (entry == null || entry.getAckState() == true)
         {
            return false; // ack failed
         }  
         // ack the alarm
         entry.setAckParams(true, System.currentTimeMillis(), user, system);
         
         // prepare the AlarmTableNotification to send
         atn =  new AlarmTableNotification(entry);
         
         // this is a new notification
         atn.setSequenceNumber(mbeanImpl.getSequenceNumber());
         atn.setTimeStamp(System.currentTimeMillis());
         
         // if alarm Stateless or Statefull but Cleared, remove from table
         int alarmState = entry.getAlarmState();
         if (alarmState == Alarm.STATE_NONE || alarmState == Alarm.STATE_CLEARED)
         {
            alarmMap.remove(alarmId);
         }
      }
      // send the AlarmTableNotification
      mbeanImpl.emitNotification(atn);
      
      return true; // ok
   }

   /**
    * Unacknowledge an Alarm
    *
    * @return true if unack was succesful, false otherwise
    *         (not in table or unacked already)
    */
   public boolean unacknowledge(String alarmId, String user, String system)
   {
      AlarmTableNotification atn;
      
      synchronized (this)
      {
         AlarmTableNotification entry =
            (AlarmTableNotification)alarmMap.get(alarmId);
         
         if (entry == null || entry.getAckState() == false)
         {
            return false; // unack failed
         }  
         // unack the alarm
         entry.setAckParams(false, System.currentTimeMillis(), user, system);
         
         // prepare the AlarmTableNotification to send
         atn =  new AlarmTableNotification(entry);
         
         // this is a new notification
         atn.setSequenceNumber(mbeanImpl.getSequenceNumber());
         atn.setTimeStamp(System.currentTimeMillis());
      }
      // send the AlarmTableNotification
      mbeanImpl.emitNotification(atn);
      
      return true; // ok
   }
   
   /**
    * Gets a copy of the AlarmTable
    */   
   public AlarmTableNotification[] getAlarmTable()
   {
      // this syncronized deep copy is quite expensive
      synchronized (this)
      {
         Collection alarms = alarmMap.values();
         AlarmTableNotification[] array = new AlarmTableNotification[alarms.size()];
         return (AlarmTableNotification[])alarms.toArray(array);
      }
   }
   
   /**
    * Gets the number of entries in the table
    */
   public int getAlarmSize()
   {
      synchronized(this)
      {
         return alarmMap.size();
      }
   }
   
   // Private Methods -----------------------------------------------
   
   /**
    * Since this is stateful, first check if there is already
    * an entry in the stateful alarm map, then update both maps.
    */
   private void updateNotificationStatefull(AlarmNotification an)
   {
      int alarmState = an.getAlarmState();
      int severity = an.getSeverity();
      
      // Create a key based on source+type
      Object alarmKey = AlarmNotification.createKey(an);
      
      AlarmTableNotification atn;
      
      // Check if this stateful alarm is already stored
      synchronized (this)
      {
         String alarmId = (String)statefulMap.get(alarmKey);
         if (alarmId == null)
         {
            // the stateful alarm is not known
            if (isMaxSizeReached())
            {
               // return if table is full
               return;
            }
            else
            {
               // generate a new Id, if not found
               alarmId = generateAlarmId();
            }
         }
         // create an AlarmTableNotification
         atn = new AlarmTableNotification(
                  alarmId,
                  AlarmTableNotification.ALARM_TABLE_UPDATE,
                  this.mbeanImpl.getMBeanName(),
                  null,
                  severity,
                  alarmState,
                  this.mbeanImpl.getSequenceNumber(),
                  System.currentTimeMillis(),
                  null
            );
         
         // store a reference to the original notification
         atn.setUserData(an);
      
         // need to check if acked already, in which case
         // we must copy the ack data to the new AlarmTableNotification
         // and remove the entry from the table         
         if (alarmState == Alarm.STATE_CLEARED)
         {
            AlarmTableNotification entry =
               (AlarmTableNotification)alarmMap.get(alarmId);
            
            if (entry != null && entry.getAckState() == true)
            {
               statefulMap.remove(alarmKey);
               alarmMap.remove(alarmId);
               
               atn.setAckParams(true, entry.getAckTime(),
                                entry.getAckUser(), entry.getAckSystem());
            }
            else
            {
               // just add it
               statefulMap.put(alarmKey, alarmId);
               alarmMap.put(alarmId, atn);
            }
         }
         else
         {
            // just add it
            statefulMap.put(alarmKey, alarmId);
            alarmMap.put(alarmId, atn);
         }
      }
      // the only case to be acked is when it is not stored in table
      // in which case send the new AlarmTableNotification itself
      if (atn.getAckState() == true)
      {
         mbeanImpl.emitNotification(atn);
      }
      else // send a copy away
      {
         mbeanImpl.emitNotification(new AlarmTableNotification(atn));
      }
   }
   
   /**
    * Store the notification in the active alarm map.
    */
   private void updateNotificationStateless(Notification n, int severity)
   {
      synchronized (this)
      {
         if (isMaxSizeReached())
         {
            // can't hold no more alarms
            return;
         }
      }
      // create an AlarmTableNotification
      AlarmTableNotification atn =
         new AlarmTableNotification(
            generateAlarmId(),
            AlarmTableNotification.ALARM_TABLE_UPDATE,
            this.mbeanImpl.getMBeanName(),
            null,
            severity,
            Alarm.STATE_NONE,
            this.mbeanImpl.getSequenceNumber(),
            System.currentTimeMillis(),
            null
         );
      // store a reference to the original notification
      atn.setUserData(n);
      
      // store the AlarmTableNotification - this is always a new entry
      synchronized (this)
      {
         alarmMap.put(atn.getAlarmId(), atn);
      }
      
      // send a copy away
      mbeanImpl.emitNotification(new AlarmTableNotification(atn));
   }
   
   /**
    * Generate a (hopefully) unique alarmId
    */
   private String generateAlarmId()
   {
      return serverId + '-' + alarmIdCount.increment();
   }
   
   /**
    * Check if table is full
    */
   private boolean isMaxSizeReached()
   {
      if (maxSize != -1)
      {
         return (alarmMap.size() >= maxSize) ? true : false;
      }
      else
      {
         return false;
      }
   }
}
