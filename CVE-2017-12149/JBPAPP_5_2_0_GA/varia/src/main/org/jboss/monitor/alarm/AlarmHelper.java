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
import java.util.Map;

/**
 * Misc utilities
 *
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public class AlarmHelper implements Alarm
{
   // Constructors --------------------------------------------------
   
   /**
    * Do not allow object creation
    */
   private AlarmHelper()
   {
      // empty
   }

   // Static --------------------------------------------------------

   /**
    * Return the severity in String form
    */
   public static String getSeverityAsString(int severity)
   {
      if (severity < SEVERITY_NORMAL || severity > SEVERITY_UNKNOWN)
      {  
         severity = SEVERITY_UNKNOWN;
      }
      return SEVERITY_STRINGS[severity];
   }
   
   /**
    * Return the alarm state in String form
    */
   public static String getStateAsString(int alarmState)
   {
      if (alarmState < STATE_CLEARED || alarmState > STATE_NONE)
      {
         alarmState = STATE_NONE;
      }
      return STATE_STRINGS[alarmState];
   }
   
   public static Map getAlarmTableNotificationStats(AlarmTableNotification[] almtab)
   {
      // counters
      int stateCleared = 0;
      int stateChanged = 0;
      int stateCreated = 0;
      int stateNone = 0;
      
      int severityNormal = 0;
      int severityWarning = 0;
      int severityMinor = 0;
      int severityMajor = 0;
      int severityCritical = 0;
      int severityUnknown = 0;
      
      if (almtab != null)
      {
         for (int i = 0; i < almtab.length; i++)
         {
            AlarmTableNotification atn = almtab[i];
            switch (atn.getAlarmState())
            {
               case STATE_CLEARED:
                  ++stateCleared;
                  break;
               case STATE_CHANGED:
                  ++stateChanged;
                  break;
               case STATE_CREATED:
                  ++stateCreated;
                  break;
               case STATE_NONE:
               default:
                  ++stateNone;
                  break;
            }
            switch (atn.getSeverity())
            {
               case SEVERITY_NORMAL:
                  ++severityNormal;
                  break;
               case SEVERITY_WARNING:
                  ++severityWarning;
                  break;
               case SEVERITY_MINOR:
                  ++severityMinor;
                  break;
               case SEVERITY_MAJOR:
                  ++severityMajor;
                  break;
               case SEVERITY_CRITICAL:
                  ++severityCritical;
                  break;
               case SEVERITY_UNKNOWN:
               default:
                  ++severityUnknown;
                  break;
            }
         }
         Map stats = new HashMap();
         
         stats.put(getSeverityAsString(SEVERITY_NORMAL), new Integer(severityNormal));
         stats.put(getSeverityAsString(SEVERITY_WARNING), new Integer(severityWarning));
         stats.put(getSeverityAsString(SEVERITY_MINOR), new Integer(severityMinor));
         stats.put(getSeverityAsString(SEVERITY_MAJOR), new Integer(severityMajor));
         stats.put(getSeverityAsString(SEVERITY_CRITICAL), new Integer(severityCritical));
         stats.put(getSeverityAsString(SEVERITY_UNKNOWN), new Integer(severityUnknown));
         
         stats.put(getStateAsString(STATE_CLEARED), new Integer(stateCleared));
         stats.put(getStateAsString(STATE_CHANGED), new Integer(stateChanged));
         stats.put(getStateAsString(STATE_CREATED), new Integer(stateCreated));
         stats.put(getStateAsString(STATE_NONE), new Integer(stateNone));
         
         return stats;
      }
      else
      {
         return null;
      }
   }
}
