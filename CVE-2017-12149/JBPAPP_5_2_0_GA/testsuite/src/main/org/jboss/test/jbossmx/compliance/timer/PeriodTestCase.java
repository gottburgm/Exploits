/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.jbossmx.compliance.timer;

import java.util.ArrayList;
import java.util.Date;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerInvocationHandler;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.timer.TimerMBean;

import org.jboss.test.jbossmx.compliance.TestCase;

/**
 * Test fixed-delay/fixed-rate timer execution modes.
 * 
 * Program a JMX timer to produce TIMES notifications, every PERIOD msces,
 * with the initial notification after PERIOD msecs.
 * 
 * Introduce a fixed DELAY (<PERIOD) in the reception of the timer notification
 * to slow it down. Measure the total time in both fixed-rate & fixed-delay
 * scenarios and compare it with an expected value +/- an allowed percentage
 * difference.
 * 
 * In fixed-rate mode the delay does not affect the periodic execution (because
 * it's less than the period), so the expected total time is the number of repeatitions
 * times the period, plus the final delay (because that one doesn't overlap with a period).
 * 
 * In fixed-delay mode things are simpler. The total execution time is prolonged because
 * the period doesn't overlap with the execution/delay time, so the total time is
 * period plus delay times the number of repeatitions.
 * 
 * The choice of numbers below makes sure that even with a 15% allowed difference
 * there won't be any overlap in the fixed-rate/fixed-delay execution modes
 * (i.e. one cannot be confused with the other)
 * 
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 62064 $
 */
public class PeriodTestCase extends TestCase
   implements NotificationListener
{
   private final long PERIOD = 300;
   private final long DELAY  = 200;
   private final long TIMES  = 5;
   private final long FIXED_RATE_TOTAL = PERIOD * TIMES + DELAY;
   private final long FIXED_DELAY_TOTAL = (PERIOD + DELAY) * TIMES;
   private final long ALLOWED_DIFFERENCE = 15;
   
   /** The object name of the timer service */
   private ObjectName timerName;

   /** The MBean server */
   private MBeanServer server;
   
   /** Test start time */
   private long startTime;
   
   /** The received notifications */
   private ArrayList receivedNotifications = new ArrayList();
   
   // Constructor ---------------------------------------------------------------

   public PeriodTestCase(String s)
   {
      super(s);
   }

   // Overrides -----------------------------------------------------------------
   
   /**
    * The timer class to test
    */
   protected String getTestedTimerClass()
   {
      // the standard JMX timer
      return "javax.management.timer.Timer";
   }
   
   // Tests ---------------------------------------------------------------------

   /**
    * Test the (default) fixed-delay timer execution mode
    */
   public void testFixedDelay() throws Exception
   {
      try
      {
         startTimerService();
         TimerMBean timer = (TimerMBean)MBeanServerInvocationHandler.newProxyInstance(server, timerName, TimerMBean.class, false);

         // calculate all times from now
         startTime = System.currentTimeMillis();
         
         // This must cause a fixed-delay timer notification production
         // with TIMES notification produced, spaced at PERIOD msecs, starting in now+PERIOD 
         timer.addNotification("timer.notification", null, null, new Date(startTime + PERIOD), PERIOD, TIMES);
         
         long expectedDuration = FIXED_DELAY_TOTAL;          
         waitForNotifications(TIMES, expectedDuration * 2);
         
         long testDuration = System.currentTimeMillis() - startTime;
         checkTimeDifference(expectedDuration, testDuration, ALLOWED_DIFFERENCE);
      }
      finally
      {
         stopTimerService();
      }
   }

   /**
    * Test the fixed-rate timer execution mode
    */
   public void testFixedRate() throws Exception
   {
      try
      {
         startTimerService();
         TimerMBean timer = (TimerMBean)MBeanServerInvocationHandler.newProxyInstance(server, timerName, TimerMBean.class, false);

         // calculate all times from now
         startTime = System.currentTimeMillis();
         
         // This must cause a fixed-rate timer notification production
         // with TIMES notification produced, spaced at PERIOD msecs, starting in now+PERIOD 
         timer.addNotification("timer.notification", null, null, new Date(startTime + PERIOD), PERIOD, TIMES, true);
         
         long expectedDuration = FIXED_RATE_TOTAL;            
         waitForNotifications(TIMES, expectedDuration * 2);
         
         long testDuration = System.currentTimeMillis() - startTime;
         checkTimeDifference(expectedDuration, testDuration, ALLOWED_DIFFERENCE);
      }
      finally
      {
         stopTimerService();
      }
   }
   
   public void handleNotification(Notification notification, Object handback)
   {
      try
      {
         long time = notification.getTimeStamp() - startTime;
         long seqNo = notification.getSequenceNumber();
         log.debug("#" + seqNo + " (" + time + "ms) - " + notification);
         
         // cause an artifical delay
         Thread.sleep(DELAY);
      }
      catch (InterruptedException ignore) {}
      
      synchronized (receivedNotifications)
      {
        receivedNotifications.add(notification);
        
        // Notify test completion
        if (receivedNotifications.size() >= TIMES)
           receivedNotifications.notifyAll();
      }      
   }
   
   // Support functions ---------------------------------------------------------

   private void checkTimeDifference(long expected, long actual, long percentage)
   {
      long actualDiff = (actual - expected) * 100 / expected;
      log.debug("Actual time: " + actual + " msec, expected time: " + expected + " msecs");
      log.debug("Actual difference: " + actualDiff + "%, allowed: +/-" + percentage + "%");

      long diff = Math.abs(expected - actual);
      long maxDeviation = expected * percentage / 100;
      
      if (diff > maxDeviation)
         fail("Time difference larger than " + percentage + "%");
   }
   
   private void waitForNotifications(long totalExpected, long wait) throws Exception
   {
      synchronized (receivedNotifications)
      {
         if (receivedNotifications.size() > totalExpected)
            fail("too many notifications " + receivedNotifications.size());
      
         if (receivedNotifications.size() < totalExpected)
            receivedNotifications.wait(wait);
      }
      assertEquals(totalExpected, receivedNotifications.size());
   }
   
   /**
    * Get an MBeanServer, install the timer service and a notification
    * listener.
    */
   private void startTimerService() throws Exception
   {
     server = MBeanServerFactory.createMBeanServer("Timer");
     timerName = new ObjectName("Timer:type=TimerService");
     server.createMBean(getTestedTimerClass(), timerName, new Object[0], new String[0]);
     server.invoke(timerName, "start", new Object[0], new String[0]);
     server.addNotificationListener(timerName, this, null, null);
     receivedNotifications.clear();
   }

   /**
    * Remove everything used by this test.
    */
   private void stopTimerService()
   {
     try
     {
       server.invoke(timerName, "removeAllNotifications", new Object[0], new String[0]);
       server.invoke(timerName, "stop", new Object[0], new String[0]);
       server.unregisterMBean(timerName);
       MBeanServerFactory.releaseMBeanServer(server);
       receivedNotifications.clear();       
     }
     catch (Exception ignored) {}
   }   
      
}
