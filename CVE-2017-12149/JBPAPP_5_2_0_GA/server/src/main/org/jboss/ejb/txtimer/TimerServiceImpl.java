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
package org.jboss.ejb.txtimer;

// $Id: TimerServiceImpl.java 112630 2012-02-09 12:35:24Z wolfc $

import org.jboss.logging.Logger;

import javax.ejb.EJBException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The TimerService provides enterprise bean components with access to the
 * container-provided Timer Service. The EJB Timer Service allows entity beans, stateless
 * session beans, and message-driven beans to be registered for timer callback events at
 * a specified time, after a specified elapsed time, or after a specified interval.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @author miclark@redhat.com
 * @version $Revision: 112630 $
 * @since 07-Apr-2004
 */
public class TimerServiceImpl implements TimerRestoringTimerService
{
   // logging support
   private static Logger log = Logger.getLogger(TimerServiceImpl.class);

   // The tx manager
   private TransactionManager transactionManager;
   // The persistence policy plug-in
   private PersistencePolicy persistencePolicy;
   // The timerId generator
   private TimerIdGenerator timerIdGenerator;
   // The retry policy
   private RetryPolicy retryPolicy;
   
   // The timed object id
   private TimedObjectId timedObjectId;
   // The invoker for the timed object
   private TimedObjectInvoker timedObjectInvoker;

   // Map<TimerHandleImpl,TimerImpl>
   private Map timers = new HashMap();

   private final ScheduledExecutorService scheduledExecutorService;

   // Constructors --------------------------------------------------
   
   /**
    * CTOR
    * 
    * All the dependencies are supplied by the caller
    */
   public TimerServiceImpl(
         final TimedObjectId timedObjectId, TimedObjectInvoker timedObjectInvoker,
         TransactionManager transactionManager, PersistencePolicy persistencePolicy,
         RetryPolicy retryPolicy, TimerIdGenerator timerIdGenerator, int corePoolSize)
   {
      this.timedObjectId = timedObjectId;
      this.timedObjectInvoker = timedObjectInvoker;
      this.transactionManager = transactionManager;
      this.persistencePolicy = persistencePolicy;
      this.timerIdGenerator = timerIdGenerator;
      this.retryPolicy = retryPolicy;
      final AtomicInteger numThread = new AtomicInteger(0);
      final ThreadFactory threadFactory = new ThreadFactory()
      {
         @Override
         public Thread newThread(Runnable r)
         {
            // JBAS-4330, provide a meaningful name to the timer thread, needs jdk5+
            return new Thread(r, "EJB-Timer-" + numThread.incrementAndGet() + " " + timedObjectId);
         }
      };
      this.scheduledExecutorService = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
   }

   // Public --------------------------------------------------------
   
   /**
    * Get the list of all registerd timers, both active and inactive
    */
   public Collection getAllTimers()
   {
      synchronized (timers)
      {
         return new ArrayList(timers.values());
      }
   }

   ScheduledExecutorService getScheduledExecutorService()
   {
      return scheduledExecutorService;
   }

   /**
    * Get the Timer for the given timedObjectId
    */
   public Timer getTimer(TimerHandle handle)
   {
      TimerImpl timer = (TimerImpl)timers.get(handle);
      if (timer != null && timer.isActive())
         return timer;
      else
         return null;
   }
   
   /**
    * Kill all timers
    * 
    * @param keepState Whether to maintain  or remove timer persistent state
    */
   public void shutdown(boolean keepState)
   {
      scheduledExecutorService.shutdown();
      // TODO: should we await termination? If so, how long?
      synchronized (timers)
      {
         Iterator it = timers.values().iterator();
         while (it.hasNext())
         {
            TimerImpl timer = (TimerImpl)it.next();
            timer.stopTimer();
            
            if (keepState == false)
               persistencePolicy.deleteTimer(timer.getTimerId(), timer.getTimedObjectId());
         }
         timers.clear();
      }
   }

   /**
    * Get the TimedObjectInvoker associated with this TimerService
    */
   public TimedObjectInvoker getTimedObjectInvoker()
   {
      return timedObjectInvoker;
   }

   // javax.ejb.TimerService ----------------------------------------
   
   /**
    * Create a single-action txtimer that expires after a specified duration.
    *
    * @param duration The number of milliseconds that must elapse before the txtimer expires.
    * @param info     Application information to be delivered along with the txtimer expiration
    *                 notification. This can be null.
    * @return The newly created Timer.
    * @throws IllegalArgumentException If duration is negative
    * @throws IllegalStateException    If this method is invoked while the instance is in
    *                                  a state that does not allow access to this method.
    * @throws javax.ejb.EJBException   If this method could not complete due to a system-level failure.
    */
   public Timer createTimer(long duration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
   {
      if (duration < 0)
         throw new IllegalArgumentException("duration is negative");

      return createTimer(new Date(System.currentTimeMillis() + duration), 0, info);
   }

   /**
    * Create an interval txtimer whose first expiration occurs after a specified duration,
    * and whose subsequent expirations occur after a specified interval.
    *
    * @param initialDuration  The number of milliseconds that must elapse before the first
    *                         txtimer expiration notification.
    * @param intervalDuration The number of milliseconds that must elapse between txtimer
    *                         expiration notifications. Expiration notifications are
    *                         scheduled relative to the time of the first expiration. If
    *                         expiration is delayed(e.g. due to the interleaving of other
    *                         method calls on the bean) two or more expiration notifications
    *                         may occur in close succession to "catch up".
    * @param info             Application information to be delivered along with the txtimer expiration
    *                         notification. This can be null.
    * @return The newly created Timer.
    * @throws IllegalArgumentException If initialDuration is negative, or intervalDuration
    *                                  is negative.
    * @throws IllegalStateException    If this method is invoked while the instance is in
    *                                  a state that does not allow access to this method.
    * @throws javax.ejb.EJBException   If this method could not complete due to a system-level failure.
    */
   public Timer createTimer(long initialDuration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
   {
      if (initialDuration < 0)
         throw new IllegalArgumentException("initial duration is negative");
      if (intervalDuration < 0)
         throw new IllegalArgumentException("interval duration is negative");

      return createTimer(new Date(System.currentTimeMillis() + initialDuration), intervalDuration, info);
   }

   /**
    * Create a single-action txtimer that expires at a given point in time.
    *
    * @param expiration The point in time at which the txtimer must expire.
    * @param info       Application information to be delivered along with the txtimer expiration
    *                   notification. This can be null.
    * @return The newly created Timer.
    * @throws IllegalArgumentException If expiration is null, or expiration.getTime() is negative.
    * @throws IllegalStateException    If this method is invoked while the instance is in
    *                                  a state that does not allow access to this method.
    * @throws javax.ejb.EJBException   If this method could not complete due to a system-level failure.
    */
   public Timer createTimer(Date expiration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
   {
      if (expiration == null)
         throw new IllegalArgumentException("expiration is null");

      return createTimer(expiration, 0, info);
   }

   /**
    * Create an interval txtimer whose first expiration occurs at a given point in time and
    * whose subsequent expirations occur after a specified interval.
    *
    * @param initialExpiration The point in time at which the first txtimer expiration must occur.
    * @param intervalDuration  The number of milliseconds that must elapse between txtimer
    *                          expiration notifications. Expiration notifications are
    *                          scheduled relative to the time of the first expiration. If
    *                          expiration is delayed(e.g. due to the interleaving of other
    *                          method calls on the bean) two or more expiration notifications
    *                          may occur in close succession to "catch up".
    * @param info              Application information to be delivered along with the txtimer expiration
    *                          notification. This can be null.
    * @param timerId           TimerId used for persistence of the timer.
    * @return The newly created Timer.
    * @throws IllegalArgumentException If initialExpiration is null, or initialExpiration.getTime()
    *                                  is negative, or intervalDuration is negative.
    * @throws IllegalStateException    If this method is invoked while the instance is in
    *                                  a state that does not allow access to this method.
    * @throws javax.ejb.EJBException   If this method could not complete due to a system-level failure.
    */
   public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info, String timerId)
      throws IllegalArgumentException, IllegalStateException, EJBException
   {
      if (initialExpiration == null)
         throw new IllegalArgumentException("initial expiration is null");
      if (intervalDuration < 0)
         throw new IllegalArgumentException("interval duration is negative");
      if (timerId == null)
         throw new IllegalArgumentException("timerId is null");

      try
      {
         TimerImpl timer = new TimerImpl(this, timerId, timedObjectId, timedObjectInvoker, info);
         persistencePolicy.insertTimer(timerId, timedObjectId, initialExpiration, intervalDuration, info);
         timer.startTimer(initialExpiration, intervalDuration);
         return timer;
      }
      catch (Exception e)
      {
         throw new EJBException("Failed to create timer", e);
      }
   }

   /**
    * Create an interval txtimer whose first expiration occurs at a given point in time and
    * whose subsequent expirations occur after a specified interval.
    *
    * @param initialExpiration The point in time at which the first txtimer expiration must occur.
    * @param intervalDuration  The number of milliseconds that must elapse between txtimer
    *                          expiration notifications. Expiration notifications are
    *                          scheduled relative to the time of the first expiration. If
    *                          expiration is delayed(e.g. due to the interleaving of other
    *                          method calls on the bean) two or more expiration notifications
    *                          may occur in close succession to "catch up".
    * @param info              Application information to be delivered along with the txtimer expiration
    *                          notification. This can be null.
    * @return The newly created Timer.
    * @throws IllegalArgumentException If initialExpiration is null, or initialExpiration.getTime()
    *                                  is negative, or intervalDuration is negative.
    * @throws IllegalStateException    If this method is invoked while the instance is in
    *                                  a state that does not allow access to this method.
    * @throws javax.ejb.EJBException   If this method could not complete due to a system-level failure.
    */
   public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
   {
      if (initialExpiration == null)
         throw new IllegalArgumentException("initial expiration is null");
      if (intervalDuration < 0)
         throw new IllegalArgumentException("interval duration is negative");

      String timerId = timerIdGenerator.nextTimerId();
      return createTimer(initialExpiration, intervalDuration, info, timerId);
   }

   /**
    * Get all the active timers associated with this bean.
    *
    * @return A collection of javax.ejb.Timer objects.
    * @throws IllegalStateException  If this method is invoked while the instance is in
    *                                a state that does not allow access to this method.
    * @throws javax.ejb.EJBException If this method could not complete due to a system-level failure.
    */
   public Collection getTimers() throws IllegalStateException, EJBException
   {
      ArrayList activeTimers = new ArrayList();
      synchronized (timers)
      {
         Iterator it = timers.values().iterator();
         while (it.hasNext())
         {
            TimerImpl timer = (TimerImpl)it.next();
            if (timer.isActive())
               activeTimers.add(timer);
         }
      }
      return activeTimers;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public Timer restoreTimer(Date initialExpiration, long intervalDuration, Date nextExpiry, Serializable info,
         String timerId) throws IllegalArgumentException, IllegalStateException, EJBException
   {
      if (initialExpiration == null)
         throw new IllegalArgumentException("initial expiration is null");
      if (intervalDuration < 0)
         throw new IllegalArgumentException("interval duration is negative");
      if (timerId == null)
         throw new IllegalArgumentException("timerId is null");

      try
      {
         TimerImpl timer = new TimerImpl(this, timerId, timedObjectId, timedObjectInvoker, info);
         // store the timer info
         persistencePolicy.insertTimer(timerId, timedObjectId, initialExpiration, intervalDuration, info);
         // additionally, persist the next timeout too (can't store this next timeout date
         // through the insert API, because it doesn't allow a way to pass the next timeout date). So
         // this additional update
         if (persistencePolicy instanceof PersistencePolicyExt)
         {
            ((PersistencePolicyExt) persistencePolicy).updateNextTimeout(timerId, timedObjectId, nextExpiry);
         }
         // now start the timer
         timer.startTimer(initialExpiration, nextExpiry, intervalDuration);
         return timer;
      }
      catch (Exception e)
      {
         throw new EJBException("Failed to restore timer", e);
      }
   }
   
   // Package protected ---------------------------------------------
   
   /**
    * Get the current transaction
    */
   Transaction getTransaction()
   {
      try
      {
         return transactionManager.getTransaction();
      }
      catch (SystemException e)
      {
         return null;
      }
   }

   /**
    * Add a txtimer to the list of active timers
    */
   void addTimer(TimerImpl txtimer)
   {
      synchronized (timers)
      {
         TimerHandle handle = new TimerHandleImpl(txtimer);
         timers.put(handle, txtimer);
      }
   }

   /**
    * Remove a txtimer from the list of active timers
    */
   void removeTimer(TimerImpl txtimer)
   {
      synchronized (timers)
      {
         persistencePolicy.deleteTimer(txtimer.getTimerId(), txtimer.getTimedObjectId());
         timers.remove(new TimerHandleImpl(txtimer));
      }
   }
   
   void retryTimeout(TimerImpl txtimer)
   {
      try
      {
         retryPolicy.retryTimeout(timedObjectInvoker, txtimer);
      }
      catch (Exception e)
      {
         log.error("Retry timeout failed for timer: " + txtimer, e);
      }
   }
   
   PersistencePolicy getPersistencePolicy()
   {
      return this.persistencePolicy;
   }
}
