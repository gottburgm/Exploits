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

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.logging.Logger;

import javax.management.ObjectName;
import java.util.ArrayList;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81038 $
 *
 **/
public abstract class JBossMonitor extends ServiceMBeanSupport implements Runnable, JBossMonitorMBean
{
   protected Logger log;
   protected String monitorName;
   protected ObjectName observedObject;
   protected String attribute;
   protected boolean enabled;
   protected boolean alertSent = false;
   protected long period;
   protected ArrayList alertListeners = null;
   protected String thresholdString;
   protected Object triggeredAttributeValue;
   protected long triggerTime;

   protected void startService
           () throws Exception
   {
      super.startService();
      log = Logger.getLogger(monitorName);
      if (alertListeners != null)
      {
         for (int i = 0; i < alertListeners.size(); i++)
         {
            ObjectName aname = (ObjectName)alertListeners.get(i);
            getServer().addNotificationListener(getServiceName(), aname, null, null);
         }
      }
      if (enabled)
      {
         startMonitorThread();
      }
   }

   protected void stopService()
   {
      enabled = false; // to shutdown monitor thread
   }

   protected void startMonitorThread()
   {
      Thread t = new Thread(this, "JBoss JMX Attribute Monitor " + monitorName);
      t.start();
   }

   protected abstract void testThreshold();

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

   public boolean alerted()
   {
      return alertSent;
   }

   public void clearAlert()
   {
      alertSent = false;
      triggeredAttributeValue = null;
      triggerTime = 0;
   }

   public boolean getEnabled()
   {
      return enabled;
   }

   public void setEnabled(boolean start)
   {
      if (start == enabled) return;
      enabled = start;

      // only start monitor thread if mbean is started and
      // we have a state change from enabled == false to enabled == true
      if (start && getState() == STARTED)
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

   public ArrayList getAlertListeners()
   {
      return alertListeners;
   }

   public void setAlertListeners(ArrayList listeners)
   {
      if (alertListeners != null && getState() == STARTED)
      {
         // remove old listeners
         ArrayList copy = new ArrayList(listeners);
         for (int i = 0; i < alertListeners.size(); i++)
         {
            ObjectName oname = (ObjectName)alertListeners.get(i);
            int idx = copy.indexOf(oname);
            if (idx == -1)
            {
               try
               {
                  getServer().removeNotificationListener(getServiceName(), oname);
               }
               catch (Exception ex)
               {
                  getLog().warn("failed to remove listener", ex);
               }
            }
            else
            {
               copy.remove(idx);
            }
         }
         // copy has all the new listeners
         for (int i = 0; i < copy.size(); i++)
         {
            ObjectName aname = (ObjectName)copy.get(i);
            try
            {
               getServer().addNotificationListener(getServiceName(), aname, null, null);
            }
            catch (Exception ex)
            {
               getLog().warn("failed to remove listener", ex);
            }
         }
      }
      alertListeners = listeners;
   }

   public Object getTriggeredAttributeValue()
   {
      return triggeredAttributeValue;
   }

   public long getTriggerTime()
   {
      return triggerTime;
   }

   public void run()
   {
      while (this.getState() == STARTED || this.getState() == STARTING)
      {
         if (enabled)
         {
            try
            {
               testThreshold();
            }
            catch (Exception ex)
            {
               log.error(monitorName + " had error while monitoring", ex);
            }
         }
         try
         {
            Thread.sleep(period);
         }
         catch (InterruptedException ignored)
         {
         }
      }
   }

   public String getThreshold()
   {
      return thresholdString;
   }

   public void setThreshold(String val)
   {
      thresholdString = val;
   }
}
