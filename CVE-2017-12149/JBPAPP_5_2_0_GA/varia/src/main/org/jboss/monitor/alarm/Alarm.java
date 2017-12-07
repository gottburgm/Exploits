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

/**
 * Alarm Constants.
 *
 * An alarm can be of two types:
 *
 * Stateless, when the producing MBean keeps no state about the occurence
 * of the alarm. The produced alarm notification must have alarmState STATE_NONE
 * and the valid severities are SEVERITY_NORMAL -> SEVERITY_UNKNOWN
 *
 * Stateful, when the producing MBean keeps state about the occurence
 * of the alarm. The first notification must carry alarmState STATE_CREATED,
 * with valid severities SEVERITY_WARNING -> SEVERITY_UNKNOWN. Any change
 * in the alarm (severity) must generate an alarm notification with alarmState
 * STATE_CHANGED and valid severities SEVERITY_WARNING -> SEVERITY_UNKNOWN.
 * The clearance of the alarm must be indicates with an alarm notification
 * with alarmState STATE_CLEARED and a severity of SEVERITY_NORMAL.
 *
 * This complexity is required in order to be able to easily correlate
 * alarms and associaty the generation and clearence of system faults.
 *
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public interface Alarm
{
   // Constants -----------------------------------------------------

   /** default server id */
   public static final String DEFAULT_SERVER_ID = "jboss";
   
   /** the possible states of an alarm */
   public static final int STATE_CLEARED   = 0;
   public static final int STATE_CHANGED   = 1;
   public static final int STATE_CREATED   = 2;
   public static final int STATE_NONE      = 3;
   
   /** stringfied alarm states */
   public static final String[] STATE_STRINGS = {
      "CLEARED", "CHANGED", "CREATED", "NONE"
   };
   
   /** the possible severities of an alarm */
   public static final int SEVERITY_NORMAL   = 0;
   public static final int SEVERITY_WARNING  = 1;
   public static final int SEVERITY_MINOR    = 2;
   public static final int SEVERITY_MAJOR    = 3;
   public static final int SEVERITY_CRITICAL = 4;   
   public static final int SEVERITY_UNKNOWN  = 5;

   /** stringfied severities */
   public static final String[] SEVERITY_STRINGS = {
      "NORMAL", "WARNING", "MINOR", "MAJOR", "CRITICAL", "UNKNOWN"
   };
}
