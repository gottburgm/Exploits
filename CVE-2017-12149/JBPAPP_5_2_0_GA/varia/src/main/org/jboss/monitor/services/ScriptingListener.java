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

import java.util.LinkedList;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.ObjectName;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.jboss.logging.Logger;
import org.jboss.monitor.alarm.AlarmManager;
import org.jboss.system.ListenerServiceMBeanSupport;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedLong;

/**
 * A simple listener that can subscribe for any combination
 * of notifications, and asynchronously process them using
 * a script written using any of the languages supported by
 * the apache Bean Scripting Framework (BSF).
 * 
 * The following variables are setup for the script to use:
 * 
 * "log"          - service Logger
 * "server"       - the MBeanServer
 * "manager"      - alarm manager helper
 *
 * "notification" - the Notification to be processed
 * "handback"     - the Object sent with the notification
 * 
 * By setting up a Timer using the TimerService to periodicaly
 * emit notifications, we can use those notifications as triggers
 * for performing any sort of polling operation.
 * 
 * One of the intented uses of this service is to use the "manager"
 * (see org.jboss.monitor.alarm.AlarmManager) in the script,
 * help maintain a list of active system alarms in the
 * ActiveAlarmTable service.
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public class ScriptingListener  extends ListenerServiceMBeanSupport
    implements ScriptingListenerMBean
{
   // Private Data --------------------------------------------------
    
   /** The Script */
   private String script;
   
   /** The language the script is written into */
   private String language;

   /** Dynamic subscriptions flag */
   private boolean dynamicSubscriptions;

   /** Set to deliver notification directly */
   private ObjectName targetListener;
   
   /** The number of notifications received/enqueued */
   private SynchronizedLong notificationsReceived;
   
   /** The number of notifications processed/dequeued by the script */
   private SynchronizedLong notificationsProcessed;
   
   /** The total time (msecs) spent executing the script */
   private SynchronizedLong totalProcessingTime;
   
   /** Bean Scripting Framework entry point */
   private BSFManager manager;
   
   /** Enqueued notifications */
   private List queue;
   
   /** Signals stop processing */
   private boolean stopRequested;

   /** The thread running the script */
   private Thread processorThread;
   
   /** The alarm manager helper */
   private AlarmManager alm = new AlarmManager(this);
   
   // Constructors --------------------------------------------------
   
   /**
    * CTOR
    */
   public ScriptingListener()
   {
      queue = new LinkedList();
      
      notificationsReceived = new SynchronizedLong(0);
      notificationsProcessed = new SynchronizedLong(0);
      totalProcessingTime = new SynchronizedLong(0);
   }
   
   // ScriptNotificationListenerMBean Implementation ----------------
   
   /**
    * @jmx:managed-attribute
    */
   public void setScript(String script)
   {
      this.script = script;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getScript()
   {
      return script;
   }
   
   /**
    * @jmx:managed-attribute
    */
   public void setScriptLanguage(String language)
   {
      this.language = language;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getScriptLanguage()
   {
      return language;
   }
   
   /**
    * @jmx:managed-attribute
    */
   public void setDynamicSubscriptions(boolean dynamicSubscriptions)
   {
      this.dynamicSubscriptions = dynamicSubscriptions;
   }

   /**
    * @jmx:managed-attribute
    */
   public boolean getDynamicSubscriptions()
   {
      return this.dynamicSubscriptions;
   }
   
   /**
    * @jmx:managed-attribute
    */
   public long getNotificationsReceived()
   {
      return notificationsReceived.get();
   }

   /**
    * @jmx:managed-attribute
    */
   public long getNotificationsProcessed()
   {
      return notificationsProcessed.get();
   }
   
   /**
    * @jmx:managed-attribute
    */
   public long getTotalProcessingTime()
   {
      return totalProcessingTime.get();
   }
   
   /**
    * @jmx:managed-attribute
    */
   public long getAverageProcessingTime()
   {
      long processed = notificationsProcessed.get();
      
      return (processed == 0) ? 0 : totalProcessingTime.get() / processed;
   }
   
   // Lifecycle control (ServiceMBeanSupport) -----------------------
   
   /**
    * Start 
    */
   public void startService() throws Exception
   {
      log.debug("Initializing BSFManager for language '" + language + "'");
      
      // This is needed until BSF adds it
      BSFManager.registerScriptingEngine(
            "groovy", 
            "org.codehaus.groovy.bsf.GroovyEngine", 
            new String[] { "groovy", "gy" }
           ); 
      
      // I suppose we need one BSFManager per processing thread
      manager = new BSFManager();
     
      manager.setClassLoader(Thread.currentThread().getContextClassLoader());
      manager.loadScriptingEngine(language);
      manager.declareBean("log", log, Logger.class);
      manager.declareBean("server", server, MBeanServer.class);
      manager.declareBean("manager", alm, AlarmManager.class);
      
      // test with a dummy notification first, to see if the script is valid
      Notification testNotification = new Notification("jboss.script.test", serviceName, 0);
      manager.declareBean("notification", testNotification, Notification.class);
      manager.declareBean("handback", "", Object.class);
      
      manager.exec(language, "in-memory-script", 0, 0, script);
      
      // Start the ScriptProcessor in its own thread
      processorThread = new Thread(new ScriptProcessor(), "ScriptProcessor[" + serviceName + "]");
      processorThread.start();
      
      // subscribe for notifications
      super.subscribe(dynamicSubscriptions);
   }
   
   /**
    * Stop
    */
   public void stopService() throws Exception
   {
      // unsubscribe for notifications
      super.unsubscribe();
      
      log.debug("Stopping " + processorThread.getName());
      
      // tell the ScriptProcessor to stop
      stopRequested = true;
      
      // notify the processing thread in case it is waiting on the queue
      synchronized (queue)
      {
         queue.notify();         
      }
     
      try
      {
         // wait for the processor to finish, but not for too long
         processorThread.join(5000);
      }
      catch (InterruptedException e)
      {
         // set interrupted status
         Thread.currentThread().interrupt();
      }
      
      // cleanup
      queue.clear();
      manager.terminate();
   }
   
   // ListenerServiceMBeanSupport overrides -------------------------
   
   /**
    * Overriden to add handling!
    */
   public void handleNotification2(Notification notification, Object handback)
   {
      // count the received notifications
      notificationsReceived.increment();
      
      // append the received notification to the end of the list,
      // for processing from a different thread      
      synchronized (queue)
      {
         queue.add(new QueueEntry(notification, handback));
         
         // hint to the processing thread to kick-in
         queue.notify();
      }
   }
   
   // Inner ---------------------------------------------------------
   
   /**
    * Simple data holder
    */
   private static class QueueEntry
   {
      public Notification notification;
      public Object handback;
      
      public QueueEntry(Notification notification, Object handback)
      {
         this.notification = notification;
         this.handback = handback;
      }
   }
   
   /**
    * Inner class to encapsulate script execution logic
    */
   private class ScriptProcessor implements Runnable
   {
      public void run()
      {
         String name = Thread.currentThread().getName();
         log.debug("Started thread: " + name);
         
         while (!stopRequested)
         {
            QueueEntry entry;
            
            synchronized (queue)
            {
               while (queue.isEmpty() && !stopRequested)
               {
                  try
                  {
                     queue.wait();
                  }
                  catch (InterruptedException e)
                  {
                     // ignore
                  }
               }
               
               if (stopRequested)
               {
                  // done
                  break;
               }
               else
               {
                  // extract the first entry for processing
                  entry = (QueueEntry)queue.remove(0);
               }
            }
            
            // use this for measurement
            long start = System.currentTimeMillis();
            
            // we have a notification to process
            try
            {
               manager.declareBean("notification", entry.notification, Notification.class);
               manager.declareBean("handback", entry.handback == null ? "" : entry.handback, Object.class);
               
               manager.exec(language, "in-memory-script", 0, 0, script);
            }
            catch (BSFException e)
            {
               log.warn("Caught exception", e);
            }
            
            // measure time spent processing the script
            long stop = System.currentTimeMillis();
            totalProcessingTime.add(stop - start);
            notificationsProcessed.increment();
         }
         log.debug("Stopped thread: " + name);
      }
   }
   
}
