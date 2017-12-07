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

import javax.management.Notification;
import javax.management.ObjectName;
import java.util.Map;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81038 $
 *
 **/
public class StringThresholdNotification
        extends JBossMonitorNotification
{
   public static final String TRIGGERED_ATTRIBUTE_VALUE="TRIGGERED_ATTRIBUTE_VALUE";
   public static final String THRESHOLD = "THRESHOLD";

   private final String value;
   private final String threshold;
   private final boolean equality;

   public StringThresholdNotification(String monitorName, ObjectName monitorObjectName, ObjectName observedObject,
                                String attribute, String value, String threshold, boolean equality, long sequenceNumber)
   {
      super(monitorName, monitorObjectName, observedObject, attribute, sequenceNumber);
      this.value = value;
      this.threshold = threshold;
      this.equality = equality;
   }

   public String getValue()
   {
      return value;
   }

   public String getThreshold()
   {
      return threshold;
   }


   public boolean getEquality()
   {
      return equality;
   }

   /**
    * Return a substitution map that can be used by org.jboss.util.Strings.subst
    * to create a printable alert message.
    *
    * @return
    */
   public Map substitutionMap()
   {
      Map map = super.substitutionMap();
      map.put(TRIGGERED_ATTRIBUTE_VALUE, value);
      map.put(THRESHOLD, threshold);
      return map;
   }
}
