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

import javax.management.ObjectName;

/**
 * AlarmTableNotification
 *
 * userData field, holds a reference to the source Notification
 *
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public class AlarmTableNotification extends AlarmNotification
{
   // Constants -----------------------------------------------------
   
   /** The type of AlarmTableNotification */
   public static final String ALARM_TABLE_UPDATE = "jboss.alarm.table.update";

   // Private Data --------------------------------------------------

   /** @since 4.0.4 */
   private static final long serialVersionUID = -2307598716282112101L;
   
   /** A unique id for the alarm */
   private String alarmId;
   
   // AckStuff
   /** the acked/unacked status of the alarm */
   private boolean ackState;

   /** the time the ack/unack happened */
   private long ackTime;
   
   /** the user that performed the ack/unack */
   private String ackUser;
   
   /** the system ack/unack came from */
   private String ackSystem;   
   
   // CTORS ---------------------------------------------------------
   
   /**
    * CTOR, creates an AlarmTableNotification object
    *
    * Same restrictions with AlarmNotification apply
    */
   public AlarmTableNotification(
      String alarmId,
      String type, Object source,
      ObjectName target, int severity, int alarmState, 
      long sequenceNumber, long timeStamp, String message)
   {
      super(type, source, target, severity, alarmState, sequenceNumber, timeStamp, message);
      
      this.alarmId = alarmId;
   }
   
   /**
    * Copy Constructor.
    *
    * Note, userData is not deep copied!
    */
   public AlarmTableNotification(AlarmTableNotification atn)
   {
      super(
         atn.getType(), atn.getSource(),
         atn.getTarget(), atn.getSeverity(), atn.getAlarmState(),
         atn.getSequenceNumber(), atn.getTimeStamp(), atn.getMessage()
         );
      
      // this is not a deep copy!
      this.setUserData(atn.getUserData());
      
      this.alarmId = atn.alarmId;
      this.ackState = atn.ackState;
      this.ackTime = atn.ackTime;
      this.ackUser = atn.ackUser;
      this.ackSystem = atn.ackSystem;
   }
   
   // Accessors/Mutators --------------------------------------------
   
   /**
    * Gets alarmId
    */
   public String getAlarmId()
   {
      return alarmId;
   }

   /**
    * Gets the acked/unacked status of the alarm
    */
   public boolean getAckState()
   {
      return ackState;
   }
   
   /**
    * Gets the last time the alarm was acked/unacked
    */
   public long getAckTime()
   {
      return ackTime;
   }
   
   /**
    * Gets the user that performed the ack/unack
    */
   public String getAckUser()
   {
      return ackUser;
   }
   
   /**
    * Gets the system that performed the ack/unack
    */
   public String getAckSystem()
   {
      return ackSystem;
   }
   
   /**
    * Sets all ack parameters
    */
   public void setAckParams(boolean ackState, long ackTime, String ackUser, String ackSystem)
   {
      this.ackState = ackState;
      this.ackTime = ackTime;
      this.ackUser = ackUser;
      this.ackSystem = ackSystem;
   }
   
   // Object stuff --------------------------------------------------
   
   /**
    * toString()
    */
   public String toString()
   {
      StringBuffer sbuf = new StringBuffer(256);
      
      sbuf.append(AlarmTableNotification.class.getName());
      sbuf.append(" [ alarmId=").append(alarmId);      
      sbuf.append(", type=").append(getType());      
      sbuf.append(", source=").append(getSource());
      sbuf.append(", target=").append(getTarget());
      sbuf.append(", severity=").append(Alarm.SEVERITY_STRINGS[getSeverity()]);
      sbuf.append(", alarmState=").append(Alarm.STATE_STRINGS[getAlarmState()]);      
      sbuf.append(", sequenceNumber=").append(getSequenceNumber());
      sbuf.append(", timeStamp=").append(getTimeStamp());
      sbuf.append(", message=").append(getMessage());
      sbuf.append(", userData={").append(getUserData());
      sbuf.append("}, ackState=").append(ackState);
      sbuf.append(", ackTime=").append(ackTime);
      sbuf.append(", ackUser=").append(ackUser);
      sbuf.append(", ackSystem=").append(ackSystem);
      sbuf.append(" ]");
      
      return sbuf.toString();
   }
}
