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

import org.jboss.util.NestedRuntimeException;
import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

import java.util.List;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanRegistration;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81038 $
 *
 **/
public class SnapshotRecordingMonitor implements Runnable, SnapshotRecordingMonitorMBean, MBeanRegistration
{
   protected Logger log;
   protected String monitorName;
   protected ObjectName observedObject;
   protected String attribute;
   protected boolean recording;
   protected long period;
   protected ArrayList history;
   protected long startTime;
   protected long endTime;
   protected MBeanServer mbeanServer;

   public SnapshotRecordingMonitor()
   {
      log = Logger.getLogger(monitorName);
      history = new ArrayList(100);
   }

   protected void startMonitorThread()
   {
      Thread t = new Thread(this, "JBoss JMX Attribute Snapshot " + monitorName);
      t.start();
   }

   public String getMonitorName()
   {
      return monitorName;
   }

   public void setMonitorName(String name)
   {
      monitorName = name;
   }

   public ObjectName getObservedObject()
   {
      return observedObject;
   }

   public void setObservedObject(ObjectName oname)
   {
      this.observedObject = oname;
   }

   public String getObservedAttribute()
   {
      return attribute;
   }

   public void setObservedAttribute(String attr)
   {
      attribute = attr;
   }

   public boolean isRecording() { return recording; }
   public void setRecording(boolean start)
   {
      if (start == recording) return;
      recording = start;

      if (start)
      {
         startMonitorThread();
      }
   }

   public long getPeriod()
   {
      return period;
   }

   public void setPeriod(long period)
   {
      this.period = period;
   }

   public ArrayList getData()
   {
      return history;
   }

   public void clearData()
   {
      history.clear();
   }

   public void startSnapshot()
   {
      history.clear();
      setRecording(true);
   }

   public void endSnapshot()
   {
      recording = false;
   }

   public long getStartTime()
   {
      return startTime;
   }

   public long getEndTime()
   {
      return endTime;
   }

   public void run()
   {
      startTime = System.currentTimeMillis();
      while (recording)
      {
         try
         {
            Object value = mbeanServer.getAttribute(observedObject, attribute);
            history.add(value);
            endTime = System.currentTimeMillis();
         }
         catch (Exception ex)
         {
            log.error(monitorName + " had error while monitoring", ex);
         }
         if (recording)
         {
            try
            {
               Thread.sleep(period);
            }
            catch (InterruptedException ignored)
            {
            }
         }
      }
   }

   // MBeanRegistrationImplementation overrides ---------------------

   public ObjectName preRegister(MBeanServer server, ObjectName objectName)
     throws Exception
   {
      mbeanServer = server;
      return objectName;
   }

   public void postRegister(Boolean registrationDone)
   {
   }

   public void preDeregister()
     throws Exception
   {
   }

   public void postDeregister()
   {
   }


}
