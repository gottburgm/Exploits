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
package org.jboss.test.jmx.compliance.timer;

import java.util.ArrayList;
import java.util.Date;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.timer.Timer;
import javax.management.timer.TimerNotification;

import junit.framework.TestCase;

/**
 * Timer Notification Tests
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */
public class TimerNotificationTestCase
   extends TestCase
   implements NotificationListener
{
   // Constants ---------------------------------------------------------------

   String TIMER_TYPE = "TimerType";
   String MESSAGE = "Message";
   String USER_DATA = "UserData";

   // Attributes --------------------------------------------------------------

   /**
    * The MBeanServer
    */
   MBeanServer server;

   /**
    * The object name of the timer
    */
   ObjectName timerName;

   /**
    * The timer
    */
   Timer timer;

   /**
    * The timer notification id
    */
   Integer id;

   /**
    * The notifications
    */
   ArrayList notifications = new ArrayList();

   // Constructor -------------------------------------------------------------

   /**
    * Construct the test
    */
   public TimerNotificationTestCase(String s)
   {
      super(s);
   }

   // Tests -------------------------------------------------------------------

   /**
    * Test a notification gives reasonable values
    */
   public void testReasonableValues()
   {
      initTest();
      try
      {
         initTimer();
         startTimer();
         long startTime = timeOffset(0).getTime();
         addNotification(TimerSUITE.ZERO_TIME);
         sync();
         stopTimer();
         long endTime = timeOffset(0).getTime();

         assertEquals(1, notifications.size());
         TimerNotification tn = (TimerNotification) notifications.get(0);
         assertEquals(MESSAGE, tn.getMessage());
         assertEquals(1, tn.getSequenceNumber());
         assertEquals(timerName, tn.getSource());
         assertEquals(TIMER_TYPE, tn.getType());
         assertEquals(USER_DATA, tn.getUserData());
         assertEquals(id, tn.getNotificationID());
         if (tn.getTimeStamp() < startTime + TimerSUITE.ZERO_TIME)
           fail("Timer notification before start?");
         if (tn.getTimeStamp() > endTime)
           fail("Timer notification after end?");
      }
      finally
      {
         MBeanServerFactory.releaseMBeanServer(server);
      }
   }

   // Support -----------------------------------------------------------------

   /**
    * Start a new test
    */
   private void initTest()
   {
      notifications.clear();
      server = MBeanServerFactory.createMBeanServer();
   }

   /**
    * Create a timer and register ourselves as a listener
    */
   private void initTimer()
   {
      try
      {
         timer = new Timer();
         timerName = new ObjectName("test:type=timer");
         server.registerMBean(timer, timerName);
         server.addNotificationListener(timerName, this, null, null);
      }
      catch (Exception e)
      {
         fail(e.toString());
      }
   }

   /**
    * Start the timer
    */
   private void startTimer()
   {
      timer.start();
   }

   /**
    * Stop the timer, does a small wait to avoid problems with the RI
    */
   private void stopTimer()
   {
      sleep();
      timer.stop();
   }

   /**
    * Add a notification
    */
   private void addNotification(long offset)
   {
      id = timer.addNotification(TIMER_TYPE, MESSAGE, USER_DATA,
                                 timeOffset(TimerSUITE.ZERO_TIME));  
   }

   /**
    * Handle the notification, just add it to the list
    */
   public void handleNotification(Notification n, Object ignored)
   {
      notifications.add(n);
      synchronized(notifications)
      {
         notifications.notifyAll();
      }
   }

   /**
    * Sync with the notification handler
    */
   private void sync()
   {
      synchronized(notifications)
      {
         try
         {
            notifications.wait(TimerSUITE.MAX_WAIT);
         }
         catch (InterruptedException ignored)
         {
         }
      }
   }

   /**
    * Get the time using and offset
    */
   private Date timeOffset(long offset)
   {
      return new Date(System.currentTimeMillis() + offset);
   }

   /**
    * Sleep for the default time
    */
   private void sleep()
   {
      sleep(TimerSUITE.ZERO_TIME);
   }

   /**
    * Sleep for a bit
    */
   private void sleep(long time)
   {
      try
      {
         Thread.sleep(time);
      }
      catch (InterruptedException ignored)
      {
      }
   }
}
