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

import org.jboss.logging.DynamicLogger;
import org.jboss.monitor.alarm.Alarm;
import org.jboss.monitor.alarm.AlarmHelper;
import org.jboss.monitor.alarm.AlarmNotification;
import org.jboss.monitor.alarm.AlarmTable;
import org.jboss.monitor.alarm.AlarmTableNotification;
import org.jboss.system.ListenerServiceMBeanSupport;
import org.jboss.util.Strings;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedLong;

/**
 * ActiveAlarmTable
 *
 * @jmx:mbean
 *    extends="org.jboss.system.ListenerServiceMBean"
 * 
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public class ActiveAlarmTable extends ListenerServiceMBeanSupport
   implements ActiveAlarmTableMBean
{
   /** DynamicLogger */
   protected static final DynamicLogger log  = DynamicLogger.getDynamicLogger(ActiveAlarmTable.class);
   
   // Private Data --------------------------------------------------
    
   /** Number of processed JMX notifications */
   private SynchronizedLong notificationCount;
   
   /** alarm table */
   AlarmTable almtab = new AlarmTable(this);
      
   // Constructors --------------------------------------------------
   
   /**
    * CTOR
    */
   public ActiveAlarmTable()
   {
      notificationCount = new SynchronizedLong(0);
      almtab.setServerId(Alarm.DEFAULT_SERVER_ID);
      almtab.setMaxSize(1000);
   }
   
   // Attributes ----------------------------------------------------
   
   
   /**
    * @jmx:managed-attribute
    */
   public int getActiveAlarmCount()
   {
      return almtab.getAlarmSize();
   }
   
   /**
    * @jmx:managed-attribute
    */
   public long getNotificationsReceived()
   {
      return notificationCount.get();
   }

   /**
    * @jmx:managed-attribute
    */
   public void setServerId(String serverId)
   {
      if (serverId != null)
      {
         almtab.setServerId(serverId);
      }
   }
   
   /**
    * @jmx:managed-attribute
    */   
   public void setMaxTableSize(int maxSize)
   {
      almtab.setMaxSize(maxSize);
   }
   
   /**
    * @jmx:managed-attribute
    */   
   public int getMaxTableSize()
   {
      return almtab.getMaxSize();
   }
   
   /**
    * @jmx:managed-attribute    
    */
   public String getServerId()
   {
      return almtab.getServerId();
   }
   
   /**
    * @jmx:managed-attribute    
    */
   public void setLogLevel(String logLevel)
   {
      log.setLogLevelAsString(logLevel);
   }
   
   /**
    * @jmx:managed-attribute    
    */
   public String getLogLevel()
   {
      return log.getLogLevelAsString();
   }
   
   // Operations ----------------------------------------------------
   
   /**
    * Acknowledge all
    *
    * @jmx:managed-operation
    *
    * @return number of acknowledged alarms       
    */
   public int acknowledgeAll(String user, String system)
   {
      AlarmTableNotification[] atns = almtab.getAlarmTable();
      int acked = 0;
      
      for (int i = 0; i < atns.length; ++i)
      {
         AlarmTableNotification atn = atns[i];
         String alarmId = atn.getAlarmId();
         if (almtab.acknowledge(alarmId, user, system))
         {
            ++acked;
         }
      }
      return acked;
   }

   /**
    * Uncknowledge all
    *
    * @jmx:managed-operation
    *
    * @return number of unacknowledged alarms       
    */
   public int unacknowledgeAll(String user, String system)
   {
      AlarmTableNotification[] atns = almtab.getAlarmTable();
      int unacked = 0;
      
      for (int i = 0; i < atns.length; ++i)
      {
         AlarmTableNotification atn = atns[i];
         String alarmId = atn.getAlarmId();         
         if (almtab.unacknowledge(alarmId, user, system))
         {
            ++unacked;
         }
      }
      return unacked;
   }
   
   /**
    * Acknowledge an Alarm
    *
    * @jmx:managed-operation   
    *
    * @return true if ack was succesful, false otherwise
    *         (not in table or acked already)
    */
   public boolean acknowledge(String alarmId, String user, String system)
   {
      return almtab.acknowledge(alarmId, user, system);
   }

   /**
    * Unacknowledge an Alarm
    *
    * @jmx:managed-operation   
    *
    * @return true if unack was succesful, false otherwise
    *         (not in table or unacked already)
    */
   public boolean unacknowledge(String alarmId, String user, String system)
   {
      return almtab.unacknowledge(alarmId, user, system);
   }
   
   /**
    * Gets the ActiveAlarmTable
    *
    * @jmx:managed-operation    
    */
   public AlarmTableNotification[] fetchAlarmTable()
   {
      return this.almtab.getAlarmTable();
   }
   
   /**
    * Gets the ActiveAlarmTable as Html
    *
    * @jmx:managed-operation    
    */
   public String fetchAlarmTableAsHtml()
   {
      AlarmTableNotification[] tab = almtab.getAlarmTable();
      
      StringBuffer sbuf = new StringBuffer(8192);
      
      sbuf.append("<p>Number of Alarms: ").append(tab.length).append("</p>").append("\n");
      sbuf.append("<table border=\"1\">").append("\n");
      sbuf.append("<tr>");
      sbuf.append("<th width=\"20%\">AlarmInfo</th>");
      sbuf.append("<th>NotificationInfo</th>");
      sbuf.append("</tr>").append("\n");
      
      for (int i = 0; i <  tab.length; i++)
      {
         AlarmTableNotification atn = tab[i];
         Notification n = (Notification)atn.getUserData();
         Object source = AlarmNotification.getEffectiveSource(n);
         
         sbuf.append("<tr>");
         sbuf.append("<td>")
            .append("alarmId: ").append(atn.getAlarmId()).append("<br><br>")
            .append("severity: ").append(AlarmHelper.getSeverityAsString(atn.getSeverity())).append("<br>")
            .append("alarmState: ").append(AlarmHelper.getStateAsString(atn.getAlarmState())).append("<br><br>")
            .append("ackState: ").append(atn.getAckState()).append("<br><br>")
            .append("ackTime: ").append(atn.getAckTime()).append("<br>")
            .append("ackUser: ").append(atn.getAckUser()).append("<br>")
            .append("ackSystem: ").append(atn.getAckSystem()).append("</td>");         
         sbuf.append("<td>")
            .append("source: ").append(source).append("<br>")
            .append("type: ").append(n.getType()).append("<br>")
            .append("timeStamp: ").append(n.getTimeStamp()).append("<br>")
            .append("sequenceNumber: ").append(n.getSequenceNumber()).append("<br><br>")
            .append("message: ").append(substNewLines(n.getMessage())).append("<br><br>")
            .append("userData: ").append(substNewLines(n.getUserData())).append("</td>");
         sbuf.append("</tr>").append("\n");
      }
      sbuf.append("</table>").append("\n");
      
      return sbuf.toString(); 
   }
   
   // Lifecycle control (ServiceMBeanSupport) -----------------------
   
   /**
    * Start 
    */
   public void startService() throws Exception
   {
      // subsbscribe myself for notifications
      super.subscribe(true);
   }
   
   /**
    * Stop
    */
   public void stopService() throws Exception
   {
      // unsubscribe for notifications
      super.unsubscribe();
   }
   
   // ListenerServiceMBeanSupport -----------------------------------
   
   /**
    * Overriden to add handling!
    */
   public void handleNotification2(Notification notification, Object handback)
   {
      log.log("Got notification (#" + Long.toString(this.notificationCount.increment())
             + "): " + notification + ", handback: " + handback);
      
      almtab.update(notification);
   }
   
   // Protected -----------------------------------------------------
   
   /**
    * Convert every occurence of "\n" to "<br>"
    */
   protected String substNewLines(Object input)
   {
      if (input == null)
      {
         return "null";
      }
      else
      {
         return Strings.subst("\n", "<br>", input.toString());
      }
   }
}
