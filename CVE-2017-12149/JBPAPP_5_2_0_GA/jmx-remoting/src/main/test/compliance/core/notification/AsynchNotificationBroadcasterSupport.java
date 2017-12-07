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
package test.compliance.core.notification;

import javax.management.Notification;
import javax.management.NotificationListener;
import org.jboss.logging.Logger;
import org.jboss.mx.util.JBossNotificationBroadcasterSupport;
import org.jboss.util.threadpool.BasicThreadPool;
import org.jboss.util.threadpool.ThreadPool;

/**
 * A notification broadcaster with asynch notifications
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81023 $
 */
public class AsynchNotificationBroadcasterSupport
      extends JBossNotificationBroadcasterSupport
{
   // Attributes ----------------------------------------------------
   private static Logger log = Logger.getLogger(AsynchNotifier.class);
   /**
    * The default pool used in the absence of on instance specific one
    */
   private static ThreadPool defaultPool = new BasicThreadPool("AsynchNotificationBroadcasterSupport");
   /**
    * The default comp
    */
   private static long defaultNotificationTimeout;

   /**
    * The instance
    */
   private long notificationTimeout;
   private ThreadPool pool;

   public static synchronized void setDefaultThreadPool(ThreadPool tp)
   {
      defaultPool = tp;
   }

   public static long getDefaultNotificationTimeout()
   {
      return defaultNotificationTimeout;
   }

   public static void setDefaultNotificationTimeout(long defaultNotificationTimeout)
   {
      AsynchNotificationBroadcasterSupport.defaultNotificationTimeout = defaultNotificationTimeout;
   }

   // Constructor ---------------------------------------------------

   /**
    * Construct a new Asyncrhonous broadcaster
    * Calls this(defaultNotificationTimeout, defaultPool)
    */
   public AsynchNotificationBroadcasterSupport()
   {
      this(defaultNotificationTimeout, defaultPool);
   }

   /**
    * Construct a new Asyncrhonous broadcaster. Calls
    * this(notificationTimeout, defaultPool)
    *
    * @param notificationTimeout the notification completion timeout in MS. A
    *                            0 value means no timeout.
    */
   public AsynchNotificationBroadcasterSupport(long notificationTimeout)
   {
      this(notificationTimeout, defaultPool);
   }

   /**
    * Construct a new Asyncrhonous broadcaster
    *
    * @param notificationTimeout - the notification completion timeout in MS. A
    *                            0 value means no timeout.
    * @param pool                - the thread pool to use for the asynchronous notifcations
    */
   public AsynchNotificationBroadcasterSupport(long notificationTimeout,
                                               ThreadPool pool)
   {
      this.notificationTimeout = notificationTimeout;
      this.pool = pool;
   }

   // Public --------------------------------------------------------

   public long getNotificationTimeout()
   {
      return notificationTimeout;
   }

   public void setNotificationTimeout(long notificationTimeout)
   {
      this.notificationTimeout = notificationTimeout;
   }

   public ThreadPool getThreadPool()
   {
      return pool;
   }

   public void setThreadPool(ThreadPool pool)
   {
      this.pool = pool;
   }

   // NotificationBroadcasterSupport overrides ----------------------

   /**
    * Handle the notification, asynchronously invoke the listener.
    *
    * @param listener     the listener to notify
    * @param notification the notification
    * @param handback     the handback object
    */
   public void handleNotification(NotificationListener listener,
                                  Notification notification,
                                  Object handback)
   {
      AsynchNotifier notifier = new AsynchNotifier(listener, notification, handback);
      pool.run(notifier, 0, notificationTimeout);
   }

   /**
    * Invoke stop on the thread pool if its not the class default pool.
    *
    * @param immeadiate the immeadiate flag passed to the TheadPool#stop
    */
   protected void stopThreadPool(boolean immeadiate)
   {
      if(pool != defaultPool)
      {
         pool.stop(immeadiate);
      }
   }

   // Inner classes -------------------------------------------------

   public class AsynchNotifier
         implements Runnable
   {
      NotificationListener listener;
      Notification notification;
      Object handback;

      public AsynchNotifier(NotificationListener listener,
                            Notification notification,
                            Object handback)
      {
         this.listener = listener;
         this.notification = notification;
         this.handback = handback;
      }

      public void run()
      {
         try
         {
            listener.handleNotification(notification, handback);
         }
         catch(Throwable throwable)
         {
            log.error("Error processing notification=" + notification +
                      " listener=" + listener +
                      " handback=" + handback,
                      throwable);
         }
      }
   }
}
