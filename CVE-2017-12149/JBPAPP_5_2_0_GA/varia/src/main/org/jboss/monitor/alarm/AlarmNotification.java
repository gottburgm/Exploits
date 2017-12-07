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

import javax.management.Notification;
import javax.management.ObjectName;

/**
 * AlarmNotification
 *
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public class AlarmNotification extends Notification
{
   // Private Data --------------------------------------------------

   /** @since 4.0.4 */
   private static final long serialVersionUID = -7041616127511632675L;
   
   /** Set when the alarm refers to some other MBean */
   private ObjectName target;
   
   /** The alarm severity */
   private int severity;
   
   /** The alarm state */
   private int alarmState;
   
   // CTORS ---------------------------------------------------------
   
   /**
    * Complete CTOR, creates an AlarmNotification object
    *
    * Note:
    *   STATE_CLEARED forces severity to SEVERITY_NORMAL
    *   STATE_CREATED/CHANGED have valid severities WARNING to UNKNOWN
    *   STATE_NONE has valid severities NORMAL to UNKNOWN
    * Also:
    *   Out-of-range states are mapped to STATE_NONE
    *   Out-of-range severities are mapped to SEVERITY_UNKNOWN
    */
   public AlarmNotification(
      String type, Object source,
      ObjectName target,  int severity, int alarmState,
      long sequenceNumber, long timeStamp, String message
   )
   {
      super(type, source, sequenceNumber, timeStamp, message);
      
      this.target = target;
      
      switch (alarmState)
      {
         case Alarm.STATE_CLEARED:
            this.alarmState = Alarm.STATE_CLEARED;
            // forces severity=SEVERITY_NORMAL
            this.severity = Alarm.SEVERITY_NORMAL;
            break;
            
         case Alarm.STATE_CREATED:
         case Alarm.STATE_CHANGED:
            this.alarmState = alarmState;
            // can't have SEVERITY_NORMAL!
            if (severity > Alarm.SEVERITY_NORMAL && severity <= Alarm.SEVERITY_UNKNOWN)
            {
               this.severity = severity;
            }
            else // handle out of range severity as SEVERITY_UNKNOWN
            {
               this.severity = Alarm.SEVERITY_UNKNOWN;
            }
            break;            

         case Alarm.STATE_NONE:
         default: // handle out of range alarmState as STATE_NONE
            this.alarmState = Alarm.STATE_NONE;
            if (severity >= Alarm.SEVERITY_NORMAL && severity <= Alarm.SEVERITY_UNKNOWN)
            {
               this.severity = severity;
            }
            else // handle out of range severity as SEVERITY_UNKNOWN
            {
               this.severity = Alarm.SEVERITY_UNKNOWN;
            }
            break;            
      }
   }
   
   // Static --------------------------------------------------------
   
   /**
    * Returns a key that can be used in AlarmTables (maps)
    */
   public static Object createKey(Notification n)
   {
      Object source = getEffectiveSource(n);
      return AlarmKey.createKey(source, n.getType());
   }
   
   /**
    * Returns the effective source for the notification.
    * In case of a AlarmNotification with a non-null target
    * the target becomes the source.
    */
   public static Object getEffectiveSource(Notification n)
   {
      Object source = n.getSource();
      if (n instanceof AlarmNotification)
      {
         ObjectName target = ((AlarmNotification)n).getTarget();
         if (target != null)
         {
            source = target;
         }
      }
      return source;
   }
   
   // Accessors -----------------------------------------------------
   
   /**
    * Gets the target MBean name, when the alarm is produced
    * by 'source' on behalf of the 'target', or null. 
    */
   public ObjectName getTarget()
   {
      return target;
   }
   
   /**
    * Gets alarm severity
    */
   public int getSeverity()
   {
      return this.severity;
   }
   
   /**
    * Gets alarm state
    */
   public int getAlarmState()
   {
      return this.alarmState;
   }
   
   // Object stuff --------------------------------------------------
   
   /**
    * toString()
    */
   public String toString()
   {
      StringBuffer sbuf = new StringBuffer(256);
      
      sbuf.append(AlarmNotification.class.getName());
      sbuf.append(" [ type=").append(getType());      
      sbuf.append(", source=").append(getSource());
      sbuf.append(", target=").append(target);
      sbuf.append(", severity=").append(Alarm.SEVERITY_STRINGS[severity]);
      sbuf.append(", alarmState=").append(Alarm.STATE_STRINGS[alarmState]);      
      sbuf.append(", sequenceNumber=").append(getSequenceNumber());
      sbuf.append(", timeStamp=").append(getTimeStamp());
      sbuf.append(", message=").append(getMessage());
      sbuf.append(", userData={").append(getUserData());
      sbuf.append("} ]");
      
      return sbuf.toString();
   }
}
