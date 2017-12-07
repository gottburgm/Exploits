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
 * AlarmKey
 *
 * Used to correlate stateful alarms based on mbean+type match.
 *
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
class AlarmKey
{
   // Private Data --------------------------------------------------
   
   private String alarmId;

   // Static Helper -------------------------------------------------  
   
   public static AlarmKey createKey(Object mbean, String type)
   {
      if (mbean instanceof ObjectName)
      {
         return new AlarmKey(mbean.toString(), type);
      }
      else if (mbean instanceof String)
      {
         return new AlarmKey((String)mbean, type);
      }
      else
      {
         throw new IllegalArgumentException("Bad non-ObjectName 'mbean' parameter: " + mbean);
      }
   }
   
   // Constructor ---------------------------------------------------
   
   public AlarmKey(String mbean, String type)
   {
      StringBuffer sbuf = new StringBuffer(256);
      
      sbuf.append(mbean).append('+').append(type);
      
      alarmId = sbuf.toString();
   }
   
   // Object stuff --------------------------------------------------
   
   /**
    * Bases equality on alarmId equality
    */
   public boolean equals(Object obj)
   {
      if (obj instanceof AlarmKey)
      {
         return ((AlarmKey)obj).alarmId.equals(this.alarmId);
      }
      else
      {
         return false;
      }
   }
   
   /**
    * Makes use of alarmId's hashCode
    */
   public int hashCode()
   {
      return this.alarmId.hashCode();
   }
   
   /**
    * Pretty prints
    */
   public String toString()
   {
      StringBuffer sbuf = new StringBuffer(256);
      
      sbuf.append("AlarmKey[").append(this.alarmId).append("]");
      
      return sbuf.toString();
   }
}
