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
package org.jboss.mx.timer;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.management.timer.TimerNotification;

import org.jboss.logging.Logger;
import org.jboss.mx.util.RunnableScheduler;
import org.jboss.mx.util.SchedulableRunnable;

/**
 * A clone of the JBossMX javax.management.timer.Timer service.
 * 
 * There are indications that the jdk5 javax.management.timer.Timer 
 * uses internally a single-threaded implementation for executing
 * scheduled tasks, so scheduling of multiple tasks is affected
 * when moving from jdk1.4 and the jboss implementation of Timer,
 * to a jdk5 runtime.
 * 
 * The JBossMX Timer implementation in contrast uses a dynamically
 * extensible thread pool to execute scheduled tasks. Since we don't
 * control the jdk5 implementation, we've cloned the jboss timer
 * so it can be used as a drop-in replacement of the jdk JMX Timer.
 * 
 * The two classes *should* be kept in sync, or instead change our
 * javax.management.timer.Timer to delegate to this class.
 * 
 * @see javax.management.timer.Timer
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 85945 $
 */
public class JBossTimer extends NotificationBroadcasterSupport
   implements JBossTimerMBean, MBeanRegistration
{
   // logging support
   private static Logger log = Logger.getLogger(JBossTimer.class);

   // Constants -----------------------------------------------------

   /** The number of milliseconds in one second. */
   public static final long ONE_SECOND = 1000;

   /** The number of milliseconds in one minute. */
   public static final long ONE_MINUTE = ONE_SECOND * 60;

   /** The number of milliseconds in one hour. */
   public static final long ONE_HOUR = ONE_MINUTE * 60;

   /** The number of milliseconds in one day. */
   public static final long ONE_DAY = ONE_HOUR * 24;

   /** The number of milliseconds in one week. */
   public static final long ONE_WEEK = ONE_DAY * 7;

   /** Don't send notifications at initial start up. */
   private static final int SEND_NO = 0;

   /** Send all past notifications at initial start up. */
   private static final int SEND_START = 1;

   /** Normal operation sending */
   private static final int SEND_NORMAL = 2;

   // Attributes ----------------------------------------------------

   /** The next notification id. */
   int nextId = 0;

   /** The next notification sequence number. */
   long sequenceNumber = 0;

   /** The send past events attribute. */
   boolean sendPastNotifications = false;

   /** Whether the service is active. */
   boolean active = false;

   /** Our object name. */
   ObjectName objectName;

   /** The registered notifications. */
   HashMap notifications = new HashMap();

   /** The scheduler */
   private RunnableScheduler scheduler = new RunnableScheduler();

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // TimerMBean implementation -------------------------------------

   public Integer addNotification(String type, String message, Object userData, Date date)
      throws IllegalArgumentException
   {
      return addNotification(type, message, userData, date, 0);
   }

   public Integer addNotification(String type, String message, Object userData, Date date, long period)
      throws IllegalArgumentException
   {
      return addNotification(type, message, userData, date, period, 0);
   }

   public Integer addNotification(String type, String message, Object userData, Date date, long period, long occurences)
      throws IllegalArgumentException
   {
      return addNotification(type, message, userData, date, period, occurences, false);
   }

   /**
    * Creates a new timer notification with the specified type, message and userData and inserts it into the list of notifications with a given date, period and number of occurences.
    * <p/>
    * If the timer notification to be inserted has a date that is before the current date, the method behaves as if the specified date were the current date.
    * For once-off notifications, the notification is delivered immediately.
    * For periodic notifications, the first notification is delivered immediately and the subsequent ones are spaced as specified by the period parameter.
    * <p/>
    * Note that once the timer notification has been added into the list of notifications, its associated date, period and number of occurences cannot be updated.
    * <p/>
    * In the case of a periodic notification, the value of parameter fixedRate is used to specify the execution scheme, as specified in Timer.
    *
    * @param type         The timer notification type.
    * @param message      The timer notification detailed message.
    * @param userData     The timer notification user data object.
    * @param date         The date when the notification occurs.
    * @param period       The period of the timer notification (in milliseconds).
    * @param nbOccurences The total number the timer notification will be emitted.
    * @param fixedRate    If true and if the notification is periodic, the notification is scheduled with a fixed-rate execution scheme. If false and if the notification is periodic, the notification is scheduled with a fixed-delay execution scheme. Ignored if the notification is not periodic.
    * @return The identifier of the new created timer notification.
    * @throws IllegalArgumentException The period or the number of occurences is negative
    */
   public Integer addNotification(String type, String message, Object userData, Date date, long period, long nbOccurences, boolean fixedRate)
      throws IllegalArgumentException
   {
      // Generate the next id.
      int newId = 0;
      newId = ++nextId;
      Integer id = new Integer(newId);

      // Validate and create the registration.
      RegisteredNotification rn =
         new RegisteredNotification(id, type, message, userData, date, period, nbOccurences, fixedRate);

      // Add the registration.
      synchronized(notifications)
      {
         notifications.put(id, rn);
         rn.setNextRun(rn.nextDate);
         rn.setScheduler(scheduler);
      }

      return id;
   }
  
   public Vector getAllNotificationIDs()
   {
      synchronized(notifications)
      {
         return new Vector(notifications.keySet());
      }
   }

   public Date getDate(Integer id)
   {
      // Make sure there is a registration
      RegisteredNotification rn = (RegisteredNotification) notifications.get(id);
      if (rn == null)
         return null;

      // Return a copy of the date.
      return new Date(rn.startDate);
   }

   public int getNbNotifications()
   {
      return notifications.size();
   }

   public Long getNbOccurences(Integer id)
   {
      // Make sure there is a registration
      RegisteredNotification rn = (RegisteredNotification) notifications.get(id);
      if (rn == null)
         return null;

      // Return a copy of the occurences.
      return new Long(rn.occurences);
   }

   /**
    * Gets a copy of the flag indicating whether a peridic notification is executed at fixed-delay or at fixed-rate.
    *
    * @param id The timer notification identifier.
    * @return A copy of the flag indicating whether a peridic notification is executed at fixed-delay or at fixed-rate.
    */
   public Boolean getFixedRate(Integer id)
   {
      // Make sure there is a registration
      RegisteredNotification rn = (RegisteredNotification) notifications.get(id);
      if (rn == null)
         return null;

      // Return a copy of the fixedRate
      return new Boolean(rn.fixedRate);
   }

   public Vector getNotificationIDs(String type)
   {
      Vector result = new Vector();

      // Loop through the notifications looking for the passed type.
      synchronized (notifications)
      {
         Iterator iterator = notifications.values().iterator();
         while (iterator.hasNext())
         {
            RegisteredNotification rn = (RegisteredNotification) iterator.next();
            if (rn.type.equals(type))
               result.add(rn.id);
         }
      }
      
      return result;
   }

  public String getNotificationMessage(Integer id)
  {
    // Make sure there is a registration
    RegisteredNotification rn = (RegisteredNotification) notifications.get(id);
    if (rn == null)
      return null;

    // Return the message
    return rn.message;
  }

  public String getNotificationType(Integer id)
  {
    // Make sure there is a registration
    RegisteredNotification rn = (RegisteredNotification) notifications.get(id);
    if (rn == null)
      return null;

    // Return the type.
    return rn.type;
  }

  public Object getNotificationUserData(Integer id)
  {
    // Make sure there is a registration
    RegisteredNotification rn = (RegisteredNotification) notifications.get(id);
    if (rn == null)
      return null;

    // Return the user data.
    return rn.userData;
  }

  public Long getPeriod(Integer id)
  {
    // Make sure there is a registration
    RegisteredNotification rn = (RegisteredNotification) notifications.get(id);
    if (rn == null)
      return null;

    // Return a copy of the period
    return new Long(rn.period);
  }

  public boolean getSendPastNotifications()
  {
    return sendPastNotifications;
  }

  public boolean isActive()
  {
    return active;
  }

  public boolean isEmpty()
  {
    return notifications.isEmpty();
  }

  public void removeAllNotifications()
  {
    // Remove the notifications
    synchronized(notifications)
    {
       Iterator iterator = notifications.values().iterator();
       while (iterator.hasNext())
       {
          RegisteredNotification rn = (RegisteredNotification) iterator.next();
          rn.setScheduler(null);
          iterator.remove();
       }
    }

    // The spec says to reset the identifiers, seems like a bad idea to me
    synchronized (this)
    {
       nextId = 0;
    }
  }

  public void removeNotification(Integer id)
    throws InstanceNotFoundException
  {

     log.debug("removeNotification: " + objectName + ",id=" + id);

    // Check if there is a notification.
    synchronized(notifications)
    {
       RegisteredNotification rn = (RegisteredNotification) notifications.get(id);
       if (rn == null)
         throw new InstanceNotFoundException("No notification id : " +
                                          id.toString());

       // Remove the notification
       rn.setScheduler(null);
       notifications.remove(id);
    }
  }

  public void removeNotifications(String type)
    throws InstanceNotFoundException
  {
    boolean found = false;

     log.debug("removeNotifications: " + objectName + ",type=" + type);

    // Loop through the notifications removing the passed type.
    synchronized(notifications)
    {
       Iterator iterator = notifications.values().iterator();
       while (iterator.hasNext())
       {
          RegisteredNotification rn = (RegisteredNotification) iterator.next();
          if (rn.type.equals(type))
          {
             rn.setScheduler(null);
             iterator.remove();
             found = true;
          }
       }
    }

    // The spec says to through an exception when nothing removed.
    if (found == false)
      throw new InstanceNotFoundException("Nothing registered for type: " +
                                          type);
  }

   public void setSendPastNotifications(boolean value)
   {
      log.debug("setSendPastNotifications: " + objectName + ",value=" + value);
      sendPastNotifications = value;
   }

   public synchronized void start()
   {
      // Ignore if already active
      if (active == true)
         return;
      active = true;

      log.debug("start: " + objectName + " at " + new Date());

      // Perform the initial sends, for past notifications send missed events
      // otherwise ignore them
      synchronized (notifications)
      {
         Iterator iterator = notifications.values().iterator();
         while (iterator.hasNext())
         {
            RegisteredNotification rn = (RegisteredNotification) iterator.next();
            if (sendPastNotifications)
               rn.sendType = SEND_START;
            else
               rn.sendType = SEND_NO;
            sendNotifications(rn);
            rn.sendType = SEND_NORMAL;
         }
      }

      // Start 'em up
      scheduler.start();
   }

  public synchronized void stop()
  {
    // Ignore if not active
    if (active == false)
      return;

     log.debug("stop: " + objectName + ",now=" + new Date());

    // Stop the threads
    active = false;
    scheduler.stop();
  }

   // MBeanRegistrationImplementation overrides ---------------------

  public ObjectName preRegister(MBeanServer server, ObjectName objectName)
    throws Exception
  {
    // Save the object name
    this.objectName = objectName;

    // Use the passed object name.
    return objectName;
  }

  public void postRegister(Boolean registrationDone)
  {
  }

  public void preDeregister()
    throws Exception
  {
    // Stop the timer before deregistration.
    stop();
  }

  public void postDeregister()
  {
  }

  // Package protected ---------------------------------------------

  // Protected -----------------------------------------------------

  // Private -------------------------------------------------------

  /**
   * Send any outstanding notifications.
   *
   * @param rn the registered notification to send.
   */
  private void sendNotifications(RegisteredNotification rn)
  {
     // Keep going until we have done all outstanding notifications.
     // The loop ends when not active, or there are no outstanding
     // notifications.
     // REVIEW: In practice for normal operation it never loops. We
     // ignore sends that we have missed. This avoids problems where
     // the notification takes longer than the period. Correct???
     while (isActive() && rn.nextDate != 0
             && rn.nextDate <= System.currentTimeMillis())
     {
        // Do we actually send it?
        // Yes, unless start and not sending past notifications.
        if (rn.sendType != SEND_NO)
        {
           long seq = 0;
           synchronized (this)
           {
              seq = ++sequenceNumber;
           }

           log.debug("sendNotification: " + rn);
           TimerNotification tn = new TimerNotification(rn.type, objectName,
              seq, rn.nextDate, rn.message, rn.id);
           tn.setUserData(rn.userData);
           sendNotification(tn);
        }
        // Calculate the next date.
        // Except for when we are sending past notifications at start up,
        // it cannot be in the future.
        do
        {
           // If no next run, remove it sets the next date to zero.
           if (rn.calcNextDate() == false)
           {
              synchronized (notifications)
              {
                 log.debug("remove: " + rn);
                 notifications.remove(rn.id);
              }
           }
        }
        while (isActive() && rn.sendType != SEND_START && rn.nextDate != 0
                && rn.occurences == 0 && rn.nextDate < System.currentTimeMillis());
     }

     if (rn.nextDate != 0)
        rn.setNextRun(rn.nextDate);
  }

   // Inner classes -------------------------------------------------

   /**
    * A registered notification. These run as separate threads.
    */
   private class RegisteredNotification extends SchedulableRunnable
   {
      // Attributes ----------------------------------------------------

      /** The notification id. */
      public Integer id;

      /** The notification type. */
      public String type;

      /** The message. */
      public String message;

      /** The user data. */
      public Object userData;

      /** The start date. */
      public long startDate;

      /** The period. */
      public long period;

      /** The maximum number of occurences. */
      public long occurences;

      /** The flag to indicate fixedRate notifications, or fixedDelay (default) */
      public boolean fixedRate;
      
      /** The send type, no send, past notifications or normal */
      public int sendType = SEND_NORMAL;

      /** The next run date */
      public long nextDate = 0;

      // Constructors --------------------------------------------------

      /**
       * The default constructor.
       *
       * @param id the notification id.
       * @param type the notification type.
       * @param message the notification's message string.
       * @param userData the notification's user data.
       * @param startDate the date/time the notification will occur.
       * @param period the repeat period in milli-seconds. Passing zero means
       *        no repeat.
       * @param occurences the maximum number of repeats. When the period is not
       *        zero and this parameter is zero, it will repeat indefinitely.
       * @param fixedRate If true and if the notification is periodic, the notification
       *        is scheduled with a fixed-rate execution scheme. If false and if the notification
       *        is periodic, the notification is scheduled with a fixed-delay execution scheme.
       *        Ignored if the notification is not periodic.
       *
       * @exception IllegalArgumentException when the date is before the current
       *        date, the period is negative or the number of repeats is
       *        negative.
       */
      public RegisteredNotification(Integer id, String type, String message, Object userData,
            Date startDate, long period, long occurences, boolean fixedRate)
         throws IllegalArgumentException
      {
         // Basic validation
         if (startDate == null)
            throw new IllegalArgumentException("Null Date");
         if (period < 0)
            throw new IllegalArgumentException("Negative Period");
         if (occurences < 0)
            throw new IllegalArgumentException("Negative Occurences");

         this.startDate = startDate.getTime();
         if (startDate.getTime() < System.currentTimeMillis())
         {
            log.debug("startDate [" + startDate + "] in the past, set to now");
            this.startDate = System.currentTimeMillis();
         }

         // Remember the values
         this.id = id;
         this.type = type;
         this.message = message;
         this.userData = userData;
         this.period = period;
         this.occurences = occurences;
         this.fixedRate = fixedRate;

         this.nextDate = this.startDate;

         String msgStr = "new " + this.toString();
         log.debug(msgStr);
      }

      // Public --------------------------------------------------------

      /**
       * Calculate the next notification date. Add on the period until
       * the number of occurences is exhausted.
       *
       * @return false when there are no more occurences, true otherwise.
       */
      boolean calcNextDate()
      {
         // No period, we've finished
         if (period == 0)
         {
            nextDate = 0;
            return false;
         }

         // Limited number of repeats have we finished?
         if (occurences != 0 && --occurences == 0)
         {
            nextDate = 0;
            return false;
         }

         // Calculate the next occurence
         if (fixedRate)
            nextDate += period;
         else // fixed delay
            nextDate = System.currentTimeMillis() + period;

         return true;
      }

      // SchedulableRunnable overrides ---------------------------------

      /**
       * Send the notifications.
       */
      public void doRun()
      {
         // Send any notifications
         sendNotifications(this);
      }

      public String toString()
      {
         return " RegisteredNotification: [timer=" + objectName + ",id=" + id + ",startDate=" + new Date(startDate) +
                ",period=" + period + ",occurences=" + occurences + ",fixedRate=" + fixedRate +
                ",nextDate=" + new Date(nextDate) + "]";
      }
   }
}
