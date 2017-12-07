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
import java.util.HashMap;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81038 $
 *
 **/
public class JBossMonitorNotification extends Notification
{
   public static final String NOTIFICATION_TYPE = "JBOSS_MONITOR_NOTIFICATION";
   public static final String OBSERVED_OBJECT = "OBSERVED_OBJECT";
   public static final String MONITOR_OBJECT_NAME = "MONITOR_OBJECT_NAME";
   public static final String MONITOR_NAME = "MONITOR_NAME";
   public static final String ATTRIBUTE = "ATTRIBUTE";


   protected final ObjectName observedObject;
   protected final ObjectName monitorObjectName;
   protected final String monitorName;
   protected final String attribute;

   public JBossMonitorNotification(String monitorName, ObjectName monitorObjectName, ObjectName observedObject,
                                String attribute, long sequenceNumber)
   {
      super(NOTIFICATION_TYPE, monitorObjectName, sequenceNumber);
      this.observedObject = observedObject;
      this.attribute = attribute;
      this.monitorName = monitorName;
      this.monitorObjectName = monitorObjectName;
   }

   public ObjectName getObservedObject()
   {
      return observedObject;
   }

   public ObjectName getMonitorObjectName()
   {
      return monitorObjectName;
   }

   public String getMonitorName()
   {
      return monitorName;
   }

   public String getAttribute()
   {
      return attribute;
   }

   /**
    * Return a substitution map that can be used by org.jboss.util.Strings.subst
    * to create a printable alert message.
    *
    * @return
    */
   public Map substitutionMap()
   {
      HashMap map = new HashMap();
      map.put(OBSERVED_OBJECT, observedObject.toString());
      map.put(MONITOR_OBJECT_NAME, monitorObjectName.toString());
      map.put(MONITOR_NAME, monitorName);
      map.put(ATTRIBUTE, attribute);
      return map;
   }
}
