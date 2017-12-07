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

import javax.management.ObjectName;

import org.jboss.monitor.alarm.AlarmTableNotification;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ListenerServiceMBean;

/**
 * MBean interface.
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public interface ActiveAlarmTableMBean extends ListenerServiceMBean
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.monitor:service=ActiveAlarmTable");
   
   // Attributes ----------------------------------------------------
   
   /**
    * The unique serverId
    */
   void setServerId(String serverId);
   String getServerId();

   /**
    * The dynamic log level
    */
   void setLogLevel(String logLevel);
   String getLogLevel();
   
   /**
    * The maximum number of alarms to keep, use -1 to disable
    */
   void setMaxTableSize(int maxSize);
   int getMaxTableSize();
   
   /**
    * Number of notifications received.
    */
   long getNotificationsReceived();

   /**
    * Number of active alarms in the table
    */
   int getActiveAlarmCount();
   
   // Operations ----------------------------------------------------
   
   /**
    * Acknowledge all alarms
    * @return number of acknowledged alarms
    */
   int acknowledgeAll(String user, String system);

   /**
    * Uncknowledge all alarms
    * @return number of unacknowledged alarms
    */
   int unacknowledgeAll(String user, String system);

   /**
    * Acknowledge an Alarm
    * @return true if ack was succesful, false otherwise (not in table or acked already)
    */
   boolean acknowledge(String alarmId, String user, String system);

   /**
    * Unacknowledge an Alarm
    * @return true if unack was succesful, false otherwise (not in table or unacked already)
    */
   boolean unacknowledge(String alarmId, String user, String system);

   /**
    * Gets the ActiveAlarmTable
    */
   AlarmTableNotification[] fetchAlarmTable();

   /**
    * Gets the ActiveAlarmTable as Html
    */
   String fetchAlarmTableAsHtml();

}
