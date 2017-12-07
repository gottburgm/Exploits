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

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.Strings;

import javax.management.ObjectName;
import javax.management.Notification;

import org.jboss.monitor.alarm.Alarm;
import org.jboss.monitor.alarm.AlarmManager;
import org.jboss.monitor.alarm.MBeanImplAccess;

/**
 * MemoryMonitor class.
 *
 * @jmx:mbean
 *    extends="org.jboss.system.ServiceMBean"
 *
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public class MemoryMonitor extends ServiceMBeanSupport
   implements MemoryMonitorMBean
{
   // Constants -----------------------------------------------------
   
   /** Notification type which indicates a memory low alarm */
   public static final String MEMORY_LOW = "jboss.alarm.memory.low";
   
   /** Free memory key to use in AlarmNotification/userData map */
   public static final String FREE_MEMORY_KEY = "freeMemory";
   
   /** default warning threshold */
   public static final String DEFAULT_WARNING_THRESHOLD = "5m";
   
   /** default number of measurements to trigger warning */
   public static final int DEFAULT_WARNING_MEASUREMENTS = 3;
   
   /** default critical threshold */
   public static final String DEFAULT_CRITICAL_THRESHOLD = "2m";
   
   /** default sampling period */
   public static final String DEFAULT_SAMPLING_PERIOD = "5sec";
   
   /** conversion constants */
   public static final long KILO = 1024;
   public static final long MEGA = 1024 * 1024;
   public static final long GIGA = 1024 * 1024 * 1024;
   
   // Private -------------------------------------------------------
   
   /** warning threshold */
   private long wThreshold;
   
   /** warning threshold stringfied */
   private String wThresholdString;

   /** number of measurements in the warning area before warning is sent */
   private int wMeasurements;
   
   /** critical threshold */
   private long cThreshold;
   
   /** critical threshold stringfied */
   private String cThresholdString;
   
   /** memory sampling period */
   private long samplingPeriod;
   
   /** sampling period stringfied */
   private String samplingPeriodString;
   
   /** control sampling thread */
   private boolean isStopRequested;
   
   /** last measurement of free memory */
   private long freeMemory;
   
   /** number of samples in the warning area */
   private int warningSamples;
   
   /** alarm manager */
   AlarmManager alm =
      new AlarmManager(
         new MBeanImplAccess() {
            public ObjectName getMBeanName() { return getServiceName(); }
            public long getSequenceNumber() { return getNextNotificationSequenceNumber(); }
            public void emitNotification(Notification n) { sendNotification(n); }
      });
   
   // Constructors --------------------------------------------------

   /**
    * CTOR
    */
   public MemoryMonitor()
   {
      // setup default values
      setFreeMemoryWarningThreshold(DEFAULT_WARNING_THRESHOLD);
      setFreeMemoryCriticalThreshold(DEFAULT_CRITICAL_THRESHOLD);
      setSamplingPeriod(DEFAULT_SAMPLING_PERIOD);
      this.wMeasurements = DEFAULT_WARNING_MEASUREMENTS;
   }
                
   // Attributes --------------------------------------------------

   /**
    * @jmx:managed-attribute
    */
   public void setTriggeringWarningMeasurements(int measurements)
   {
      if (measurements > 0)
      {
         this.wMeasurements = measurements;
      }
   }

   /**
    * @jmx:managed-attribute
    */
   public int getTriggeringWarningMeasurements()
   {
      return this.wMeasurements;
   }
   
   /**
    * @jmx:managed-attribute
    */
   public void setFreeMemoryWarningThreshold(String s)
   {
      synchronized (this)
      {
         this.wThreshold = parseMemorySpec(s);
         this.wThresholdString = s;
      }
   }

   /**
    * @jmx:managed-attribute
    */
   public String getFreeMemoryWarningThreshold()
   {
      return this.wThresholdString;
   }
   
   /**
    * @jmx:managed-attribute
    */
   public void setFreeMemoryCriticalThreshold(String s)
   {
      synchronized (this)
      {
         this.cThreshold = parseMemorySpec(s);
         this.cThresholdString = s;
      }
   }   
   
   /**
    * @jmx:managed-attribute
    */
   public String getFreeMemoryCriticalThreshold()
   {
      return this.cThresholdString;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setSamplingPeriod(String s)
   {
      synchronized (this)
      {
         this.samplingPeriod = Strings.parsePositiveTimePeriod(s);
         this.samplingPeriodString = s;
      }
   }   
   
   /**
    * @jmx:managed-attribute
    */
   public String getSamplingPeriod()
   {
      return this.samplingPeriodString;
   }   

   /**
    * @jmx:managed-attribute
    */
   public long getFreeMemorySample()
   {
      synchronized (this)
      {
         return this.freeMemory;
      }
   }

   /**
    * @jmx:managed-attribute
    */
   public String getSeverity()
   {
      return alm.getSeverityAsString(MEMORY_LOW);
   }
   
   // Service Lifecycle ---------------------------------------------
   
   public void startService() throws Exception
   {
      // Annonymous class
      Runnable r = new Runnable()
      {
         public void run()
         {
            log.debug("Started memory monitor thread" +
                     ", samplingPeriod=" + MemoryMonitor.this.samplingPeriodString +
                     ", warningThreshold=" + MemoryMonitor.this.wThresholdString +
                     ", criticalThreshold=" + MemoryMonitor.this.cThresholdString);

            // make copies of config params
            long wThreshold;
            long cThreshold;
            long samplingPeriod;
            
            synchronized(MemoryMonitor.this)
            {
               wThreshold = MemoryMonitor.this.wThreshold;
               cThreshold = MemoryMonitor.this.cThreshold;
               samplingPeriod = MemoryMonitor.this.samplingPeriod;               
            }
            
            // initialise warningSamples countdown
            warningSamples = wMeasurements;
            
            while (!isStopRequested)
            {
               sampleMemory(wThreshold, cThreshold);
         
               if (!isStopRequested)
               {
                  try
                  {
                     Thread.sleep(samplingPeriod);
                  }
                  catch (InterruptedException e)
                  {
                     // ignored
                  }
               }
            }
            log.debug("Stopped memory monitor thread");
         }
      };
      
      // check for validity
      if (this.cThreshold > this.wThreshold)
      {
         throw new Exception(
            "FreeMemoryWarningThreshold (" + this.wThreshold +
            ") set lower than FreeMemoryCriticalThreshold (" + this.cThreshold + ")");
      }                      
      isStopRequested = false;
      Thread t = new Thread(r, "Memory monitor thread of \"" + getServiceName() + "\"");
      t.start();
   }
   
   public void stopService()
   {
      // signal thread to stop
      this.isStopRequested = true;
   }
   
   // Private Methods -----------------------------------------------
   
   /**
    * The real stuff
    */
   private void sampleMemory(long wThreshold, long cThreshold)
   {
      long freeMemory = Runtime.getRuntime().freeMemory();
      
      synchronized (this)
      {
         this.freeMemory = freeMemory;
      };
      
      if (freeMemory <= cThreshold)
      { // critical
         alm.setAlarm(
            MEMORY_LOW,
            Alarm.SEVERITY_CRITICAL,
            "Free memory in critical state",
            FREE_MEMORY_KEY,
            new Long(freeMemory)
         );
         // reset warning countdown
         warningSamples = wMeasurements;         
      }
      else if (freeMemory <= wThreshold)
      {
         if (warningSamples > 0)
         {
            --warningSamples;
         }  
         if (warningSamples == 0
            || alm.getSeverity(MEMORY_LOW) == Alarm.SEVERITY_CRITICAL)
         {
            alm.setAlarm(
               MEMORY_LOW,
               Alarm.SEVERITY_WARNING,
               "Free memory getting low",
               FREE_MEMORY_KEY,
               new Long(freeMemory)
            );
         }
      }
      else
      {
         alm.setAlarm(
            MEMORY_LOW,
            Alarm.SEVERITY_NORMAL,
            "Free memory at normal levels",
            FREE_MEMORY_KEY,
            new Long(freeMemory)
         );
         // reset warning countdown
         warningSamples = wMeasurements;            
      }
   }
   
   /**
    * Parses a memory specification into a long.
    *
    * Translates the [kK|mM|gG] suffixes
    *
    * For example:
    *   "10"   ->  10 (bytes)
    *   "10k"  ->  10240 (bytes)
    *   "10m"  ->  10485760 (bytes)
    *   "10g"  ->  10737418240 (bytes)
    */
   private static long parseMemorySpec(String s)
   {
      try
      {
         int len = s.length();
         long factor = 0;
         
         switch (s.charAt(len - 1))
         {
            case 'k':
            case 'K':
               factor = KILO;
               s = s.substring(0, len - 1);
               break;
               
            case 'm':
            case 'M':
               factor = MEGA;
               s = s.substring(0, len - 1);
               break;
               
            case 'g':
            case 'G':
               factor = GIGA;
               s = s.substring(0, len - 1);
               break;
            
            default:
               factor = 1;
               break;
         }
         long retval = Long.parseLong(s) * factor;
         
         if (retval < 0)
         {
            throw new NumberFormatException();
         }
         return retval;
      }
      catch (RuntimeException e)
      {
         throw new NumberFormatException("Not a valid memory specification: " + s);
      }
   }
   
}
