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
package org.jboss.varia.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.NotificationEmitter;
import javax.management.ObjectName;
import javax.management.timer.TimerNotification;
import javax.management.timer.TimerMBean;
import javax.management.timer.Timer;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;
import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;
import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

/**
 * ScheduleManager manages multiple scheduled timer listeners.
 * These are registered using the {@link #addSchedule} operation.
 * Providers (basically MBean lifecycle listeners) can be registered using the
 * {@link #registerProvider} operation.
 *
 * Because of the way the JBoss deployment model works (no way to readily
 * invoke operations at deployment time), prefer to use the {@link Scheduler}
 * MBean instead.
 *
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 109190 $
 */
public class ScheduleManager extends ServiceMBeanSupport
   implements ScheduleManagerMBean
{

   // -------------------------------------------------------------------------
   // Constants
   // -------------------------------------------------------------------------

   /** 
	* Default Timer Object Name
	*/
   public static String DEFAULT_TIMER_NAME = "jboss:service=Timer";

   /** 
	* Counter for the number of scheduled instances.
	*/
   private static SynchronizedInt sCounter = new SynchronizedInt(0);

   private static final int NOTIFICATION = 0;
   private static final int DATE = 1;
   private static final int REPETITIONS = 2;
   private static final int SCHEDULER_NAME = 3;
   private static final int NULL = 4;
   private static final int ID = 5;
   private static final int NEXT_DATE = 6;

   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------

   private String mTimerName = DEFAULT_TIMER_NAME;
   private ObjectName mTimerObjectName;
   private TimerMBean mTimer;
   private NotificationEmitter mTimerEmitter;

   private boolean mStartOnStart = true;
   private boolean mFixedRate = false;
   private SynchronizedBoolean mIsPaused = new SynchronizedBoolean(false);

   /**
	* List of registered AbstractScheduleProvider ObjectNames to inform 
	* when the this manager is stop or started.
    */
   private List mProviders = Collections.synchronizedList(new ArrayList());
   
   /** 
	* Maps Integer to registered ScheduleInstance.
	*/
   private Map mSchedules = new ConcurrentReaderHashMap();

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * Default (no-args) Constructor
    */
   public ScheduleManager()
   {
   }

   // -------------------------------------------------------------------------
   // SchedulerMBean Methods
   // -------------------------------------------------------------------------

   /**
    * Starts all the registered Schedules
    *
    * @jmx:managed-operation
    */
   public void startSchedules()
   {
      log.debug("startSchedules()");
      // Check if not already started
      if (!isStarted())
      {
         // Loop over all available Schedule Instance and start them now
         Iterator i = mSchedules.values().iterator();
         while (i.hasNext())
         {
            ScheduleInstance lInstance = (ScheduleInstance) i.next();
            try
            {
               lInstance.start();
            }
            catch (JMException e)
            {
               log.error("Could not start: " + lInstance, e);
            }
         }
      }
   }

   /**
    * Stops all the registered Schedules
    *
    * @jmx:managed-operation
    *
    * @param pDoItNow If true the schedule will be stopped without waiting for the next
    *                 scheduled call otherwise the next call will be performed before
    *                 the schedule is stopped.
    */
   public void stopSchedules(boolean pDoItNow)
   {
      // Check if it is already started
      if (isStarted())
      {
         // Loop over all available Schedule Instance and start them now
         Iterator i = mSchedules.values().iterator();
         while (i.hasNext())
         {
            ScheduleInstance lInstance = (ScheduleInstance) i.next();
            try
            {
               lInstance.stop();
            }
            catch (JMException e)
            {
               log.error("Could not stop: " + lInstance, e);
            }
         }
      }
   }

   /**
    * Stops existing schedules immediately and restarts them right away.
    *
    * @jmx:managed-operation
    */
   public void restartSchedule()
   {
      stopSchedules(true);
      startSchedules();
   }

   /**
    * Register a Provider to make it available. In turn this
    * method calls "startProviding()" method on the Provider
    * to indicate that the Provider can start adding Schedules.
    *
    * @param pProviderObjectName Object Name of the Provider
    *
    * @jmx:managed-operation
    */
   public void registerProvider(String pProviderObjectName)
   {
	  try
	  {
		 registerProvider(new ObjectName(pProviderObjectName));
	  }
      catch (JMException jme)
      {
         log.error("Could not call startProviding() on " + pProviderObjectName, jme);
      }
   }

   /**
    * Register a Provider to make it available. In turn this
    * method calls "startProviding()" method on the Provider
    * to indicate that the Provider can start adding Schedules.
    *
    * @param pProviderObjectName Object Name of the Provider
    *
    * @jmx:managed-operation
    */
   public void registerProvider(ObjectName pProviderObjectName)
	  throws JMException
   {
      if (pProviderObjectName == null)
      {
         throw new MalformedObjectNameException("Provider must not be null");
      }
	  synchronized (mProviders) {
		 if (mProviders.contains(pProviderObjectName))
			throw new JMException("Already registered: " + pProviderObjectName);
		 mProviders.add(pProviderObjectName);
	  }
	  server.invoke(
			  pProviderObjectName,
			  "startProviding",
			  new Object[]{},
			  new String[]{}
	  );
   }

   /**
    * Unregister a Provider which in turn calls "stopProviding()"
    * indicating to the Provider to remove all the Schedules.
    *
    * @param pProviderObjectName Object Name of the Provider
    *
    * @jmx:managed-operation
    */
   public void unregisterProvider(String pProviderObjectName)
   {
      try
      {
		 unregisterProvider(new ObjectName(pProviderObjectName));
      }
      catch (JMException jme)
      {
         log.error("Could not call stopProviding() on " + pProviderObjectName, jme);
      }
   }

   /**
    * Unregister a Provider which in turn calls "stopProviding()"
    * indicating to the Provider to remove all the Schedules.
    *
    * @param pProviderObjectName Object Name of the Provider
    *
    * @jmx:managed-operation
    */
   public void unregisterProvider(ObjectName pProviderObjectName)
	  throws JMException
   {
      if (!mProviders.remove(pProviderObjectName))
		 return;
	  server.invoke(
			  pProviderObjectName,
			  "stopProviding",
			  new Object[]{},
			  new String[]{}
	  );
   }

   /**
    * Adds a new Schedule to the Scheduler
    *
    * @param pTarget Object Name of the Target MBean
    * @param pMethodName Name of the method to be called
    * @param pMethodSignature List of Attributes of the method to be called
    *                         where ...
    * @param pStartDate Date when the schedule is started
    * @param pRepetitions Initial Number of repetitions
    *
    * @return Identification of the Schedule used later to remove it
    *         if necessary
    *
    * @jmx:managed-operation
    */
   public int addSchedule(
           ObjectName pProvider,
           ObjectName pTarget,
           String pMethodName,
           String[] pMethodSignature,
           Date pStartDate,
           long pPeriod,
           int pRepetitions
           )
   {
      ScheduleInstance lInstance = new ScheduleInstance(
              pProvider,
              pTarget,
              pMethodName,
              pMethodSignature,
              pStartDate,
              pRepetitions,
              pPeriod
      );
      if (isStarted())
      {
         try
         {
            lInstance.start();
         }
         catch (JMException jme)
         {
            log.error("Could not start " + lInstance, jme);
         }
      }
      int lID = lInstance.getID();
      mSchedules.put(new Integer(lID), lInstance);
      return lID;
   }

   /**
    * Removes a Schedule so that no notification is sent anymore
    *
    * @param pIdentification Identification returned by {@link #addSchedule
    *                        addSchedule()} or {@link #getSchedules
    *                        getSchedules()}.
    *
    * @jmx:managed-operation
    */
   public void removeSchedule(int pIdentification)
   {
      ScheduleInstance lInstance = (ScheduleInstance) mSchedules.get(new Integer(pIdentification));
      try
      {
		 if (lInstance == null)
			throw new InstanceNotFoundException();
         lInstance.stop();
      }
      catch (JMException e)
      {
         log.error("Could not stop " + lInstance, e);
      }
      mSchedules.remove(new Integer(pIdentification));
   }

   /**
    * Returns a list of the ids of all registered schedules
    *
    * @return List of Ids separated by a ","
    */
   public String getSchedules()
   {
      Iterator i = mSchedules.values().iterator();
      StringBuffer lReturn = new StringBuffer();
      boolean lFirst = true;
      while (i.hasNext())
      {
         ScheduleInstance lInstance = (ScheduleInstance) i.next();
         if (lFirst)
         {
            lReturn.append(lInstance.mIdentification);
            lFirst = false;
         }
         else
         {
            lReturn.append(",").append(lInstance.mIdentification);
         }
      }
      return lReturn.toString();
   }

   /**
    * @return True if all the Schedules are paused meaning that even when the notifications
    *   are sent to the listener they are ignored. ATTENTION: this applies to all registered
    *   Schedules and any notifications are lost during pausing
    */
   public boolean isPaused()
   {
      return mIsPaused.get();
   }

   /**
    * Pauses or restarts the Schedules which either suspends the
    * notifications or start transfering them to the target
    *
    * @param pIsPaused True when the Schedules are paused or false when they resume
    */
   public void setPaused(boolean pIsPaused)
   {
      mIsPaused.set(pIsPaused);
   }

   /**
    * @return true if the Schedule Manager is started
    */
   public boolean isStarted()
   {
      return getState() == STARTED;
   }

   // -------------------------------------------------------------------------
   // SchedulerManagerMBean Attributes
   // -------------------------------------------------------------------------

   /**
    * Set the scheduler to start when MBean started or not. Note that this method only
    * affects when the {@link #startService startService()} gets called (normally at
    * startup time.
    *
    * @jmx:managed-attribute
    *
    * @param pStartAtStartup if the scheduler should be started upon MBean start or not
    */
   public void setStartAtStartup(boolean pStartAtStartup)
   {
      mStartOnStart = pStartAtStartup;
   }

   /**
    * @jmx:managed-attribute
    *
    * @return true if the scheduler should be started upon MBean start or not
    */
   public boolean isStartAtStartup()
   {
      return mStartOnStart;
   }
   
   /**
    * @jmx:managed-attribute
    *
    * @param pTimerName Object Name of the Timer MBean to
    *                   be used. If null or not a valid ObjectName
    *                   the default will be used
    */
   public void setTimerName(String pTimerName)
   {
      mTimerName = pTimerName;
   }
   
   /**
    * @jmx:managed-attribute
    *
    * @return Name of the Timer MBean used in here
    */
   public String getTimerName()
   {
      return mTimerName;
   }

   /**
    * @jmx:managed-attribute
    * 
    * @param fixedRate the default scheduling to use, fixed-rate or fixed-delay (false, default)
    */
   public void setFixedRate(boolean fixedRate)
   {
      mFixedRate = fixedRate;
   }
   
   /**
    * @jmx:managed-attribute
    * 
    * @return the default scheduling to use
    */   
   public boolean getFixedRate()
   {
      return mFixedRate;
   }
   
   // -------------------------------------------------------------------------
   // ServiceMBeanSupport overrides
   // -------------------------------------------------------------------------

   public ObjectName getObjectName(MBeanServer pServer, ObjectName pName)
      throws MalformedObjectNameException
   {
      return pName == null ? OBJECT_NAME : pName;
   }

   /**
    * Creates the requested Timer if not already available
    * and start all added Schedules.
    * ATTENTION: the start of the schedules is not necessary when
    * the service is started but this method is also called when
    * the service is restarted and therefore schedules can be
    * available.
    */
   protected void startService() throws Exception
   {
	  mTimerObjectName = new ObjectName(mTimerName);
      if (!getServer().isRegistered(mTimerObjectName))
      {
         getServer().createMBean(Timer.class.getName(), mTimerObjectName);
      }
	  mTimer = (TimerMBean)MBeanServerInvocationHandler.newProxyInstance(getServer(),
			mTimerObjectName, TimerMBean.class, true);
	  mTimerEmitter = (NotificationEmitter)mTimer;
      if (!mTimer.isActive())
      {
		 mTimer.start();
      }
      startSchedules();
   }

   /**
    * Stops all Schedules.
    */
   protected void stopService()
   {
      stopSchedules(true);
   }

   /**
    * When Service is destroyed it will call the "unregisterProvider()"
    * on all register Providers to let them remove their Schedules and
    * being notified that they should stop providing.
    */
   protected void destroyService()
   {
      // Unregister all providers
      Iterator i = mSchedules.values().iterator();
      while (i.hasNext())
      {
         ScheduleInstance lInstance = (ScheduleInstance) i.next();
         unregisterProvider(lInstance.mProvider.toString());
      }
   }
   
   // -------------------------------------------------------------------------
   // Inner Classes
   // -------------------------------------------------------------------------

   /**
    * This listener is waiting for its Timer Notification and call the
    * appropriate method on the given Target (MBean) and count down the
    * number of remaining repetitions.
    */
   public class MBeanListener implements NotificationListener
   {
      private final Logger log = Logger.getLogger(MBeanListener.class);

      private ScheduleInstance mSchedule;

      public MBeanListener(ScheduleInstance pSchedule)
      {
         mSchedule = pSchedule;
      }

      public void handleNotification(Notification pNotification, Object pHandback)
      {
		 boolean trace = log.isTraceEnabled();
		 if (trace) {
			log.trace("MBeanListener.handleNotification: " + pNotification);
		 }

		 try
		 {
			if (!isStarted()) {
			   log.trace("Scheduler not started");
			   mSchedule.stop();
			   return;
			}
			if (mSchedule.mRemainingRepetitions == 0)
			{
			   log.trace("No more repetitions");
			   mSchedule.stop();
			   return;
			}
			if (mIsPaused.get())
			{
			   log.trace("Paused");
			   return;
			}
			if (mSchedule.mRemainingRepetitions > 0)
			{
			   mSchedule.mRemainingRepetitions--;
			   if (trace)
				  log.trace("Remaining repetitions: " + mSchedule.mRemainingRepetitions);
			}
			Object[] lArguments = getArguments(pNotification);
			if (trace)
			{
			   log.trace("invoke " + mSchedule);
			   log.trace("arguments are: " + Arrays.asList(lArguments));
			}
			
			ObjectName on = mSchedule.mTarget;
			String mn = mSchedule.mMethodName;
			getServer().invoke(on, mn, lArguments,
					mSchedule.mSchedulableMBeanArgumentTypes
			);
		 }
		 catch (Exception e)
		 {
			log.error("Invoke failed: " + mSchedule.getTargetString(), e);
		 }
      }

	  private Object[] getArguments(Notification pNotification)
	  {
		 Object[] lArguments = new Object[mSchedule.mSchedulableMBeanArguments.length];
		 Date lTimeStamp = new Date(pNotification.getTimeStamp());
		 for (int i = 0; i < lArguments.length; i++)
		 {
			switch (mSchedule.mSchedulableMBeanArguments[i])
			{
			   case ID:
				  lArguments[i] = pNotification.getUserData();
				  break;
			   case NOTIFICATION:
				  lArguments[i] = pNotification;
				  break;
			   case DATE:
				  lArguments[i] = lTimeStamp;
				  break;
			   case REPETITIONS:
				  lArguments[i] = new Long(mSchedule.mRemainingRepetitions);
				  break;
			   case SCHEDULER_NAME:
				  lArguments[i] = getServiceName();
				  break;
			   case NEXT_DATE:
				  lArguments[i] = new Date(lTimeStamp.getTime() + mSchedule.mPeriod);
				  break;
			   default:
				  lArguments[i] = null;
			}
		 }
		 return lArguments;
	  }

   }

   /**
    * Filter to ensure that each Scheduler only gets notified when it is supposed to.
    */
   static class IdNotificationFilter implements javax.management.NotificationFilter
   {
      private static final Logger log = Logger.getLogger(IdNotificationFilter.class);

      private Integer filterId;

      /**
       * Create a Filter.
       * @param pId the Scheduler id
       */
      public IdNotificationFilter(int filterId)
      {
         this.filterId = new Integer(filterId);
      }

      /**
       * Determine if the notification should be sent to this Scheduler
       */
      public boolean isNotificationEnabled(Notification pNotification)
      {
         if (!(pNotification instanceof TimerNotification))
			return false;
		 TimerNotification lTimerNotification = (TimerNotification) pNotification;
		 if (log.isTraceEnabled())
			log.trace("isNotificationEnabled(), filterId=" + filterId +
			   ", notification=" + pNotification +
			   ", notificationId=" + lTimerNotification.getNotificationID() +
			   ", timestamp=" + lTimerNotification.getTimeStamp() +
			   ", message=" + lTimerNotification.getMessage()
			);
		 return lTimerNotification.getNotificationID().equals(filterId);
      }
   }

   /**
    * Represents a single Schedule which can be started and stopped
    * if necessary.
    */
   private class ScheduleInstance
   {

      private final Logger log = Logger.getLogger(ScheduleInstance.class);
      private int mIdentification;
      private MBeanListener mListener;

      public int mNotificationID;
      public ObjectName mProvider;
      public ObjectName mTarget;
      public long mInitialRepetitions;
      public long mRemainingRepetitions = 0;
      public Date mStartDate;
      public long mPeriod;
      public String mMethodName;
      public int[] mSchedulableMBeanArguments;
      public String[] mSchedulableMBeanArgumentTypes;

      public ScheduleInstance(
              ObjectName pProvider,
              ObjectName pTarget,
              String pMethodName,
              String[] pMethodArguments,
              Date pStartDate,
              int pRepetitions,
              long pPeriod
              )
      {
         mProvider = pProvider;
         mTarget = pTarget;
         mInitialRepetitions = pRepetitions;
         mStartDate = pStartDate;
         mPeriod = pPeriod;
         mMethodName = pMethodName;
         mSchedulableMBeanArguments = new int[pMethodArguments.length];
         mSchedulableMBeanArgumentTypes = new String[pMethodArguments.length];
         for (int i = 0; i < pMethodArguments.length; i++)
         {
            String lToken = pMethodArguments[i];
            if (lToken.equals("ID"))
            {
               mSchedulableMBeanArguments[i] = ID;
               mSchedulableMBeanArgumentTypes[i] = Integer.class.getName();
            }
            else if (lToken.equals("NOTIFICATION"))
            {
               mSchedulableMBeanArguments[i] = NOTIFICATION;
               mSchedulableMBeanArgumentTypes[i] = Notification.class.getName();
            }
            else if (lToken.equals("NEXT_DATE"))
            {
               mSchedulableMBeanArguments[i] = NEXT_DATE;
               mSchedulableMBeanArgumentTypes[i] = Date.class.getName();
            }
            else if (lToken.equals("DATE"))
            {
               mSchedulableMBeanArguments[i] = DATE;
               mSchedulableMBeanArgumentTypes[i] = Date.class.getName();
            }
            else if (lToken.equals("REPETITIONS"))
            {
               mSchedulableMBeanArguments[i] = REPETITIONS;
               mSchedulableMBeanArgumentTypes[i] = Long.TYPE.getName();
            }
            else if (lToken.equals("SCHEDULER_NAME"))
            {
               mSchedulableMBeanArguments[i] = SCHEDULER_NAME;
               mSchedulableMBeanArgumentTypes[i] = ObjectName.class.getName();
            }
            else
            {
               mSchedulableMBeanArguments[i] = NULL;
               //AS ToDo: maybe later to check if this class exists !
               mSchedulableMBeanArgumentTypes[i] = lToken;
            }
         }
         mIdentification = sCounter.increment();
      }

      /**
       * Starts the Schedule by adding itself to the timer
       * and registering its listener to get the notifications
       * and hand over to the target
       **/
      public void start() throws JMException
      {
         Date lStartDate = null;
         // Check if initial start date is in the past
         if (mStartDate.getTime() < new Date().getTime() && mPeriod > 0)
         {
            // If then first check if a repetition is in the future
            long lNow = new Date().getTime() + 100;
            long lSkipRepeats = ((lNow - mStartDate.getTime()) / mPeriod) + 1;
            log.debug("Old start date: " + mStartDate + ", now: " + new Date(lNow) + ", Skip repeats: " + lSkipRepeats);
            if (mInitialRepetitions > 0)
            {
               // If not infinit loop
               if (lSkipRepeats >= mInitialRepetitions)
               {
                  // No repetition left -> exit
                  log.warn("No repetitions left because start date is in the past and could " +
                          "not be reached by Initial Repetitions * Schedule Period");
                  return;
               }
               else
               {
                  // Reduce the missed hits
                  mRemainingRepetitions = mInitialRepetitions - lSkipRepeats;
               }
            }
            else
            {
               if (mInitialRepetitions == 0)
               {
                  mRemainingRepetitions = 0;
               }
               else
               {
                  mRemainingRepetitions = -1;
               }
            }
            lStartDate = new Date(mStartDate.getTime() + (lSkipRepeats * mPeriod));
         }
         else
         {
            lStartDate = mStartDate;
            mRemainingRepetitions = mInitialRepetitions;
         }
         mNotificationID = mTimer.addNotification(
			   "Schedule", "Scheduler Notification",
			   new Integer(getID()), // User Object
			   lStartDate,
			   new Long(mPeriod),
			   mRemainingRepetitions < 0 ? new Long(0) : new Long(mRemainingRepetitions),
			   Boolean.valueOf(mFixedRate)
		 );
         mListener = new MBeanListener(this);
		 mTimerEmitter.addNotificationListener(
			   mListener,
               new IdNotificationFilter(mNotificationID),
               // No object handback necessary
               null
         );
         log.debug("start(), add Notification to Timer with ID: " + mNotificationID);
      }

      /**
       * Stops the Schedule by remove itself from the timer
       * and removing the listener
       **/
      public void stop()
              throws JMException
      {
         log.debug("stopSchedule(), notification id: " + mNotificationID);
		 mTimerEmitter.removeNotificationListener(mListener);
		 try
		 {
			mTimer.removeNotification(mNotificationID);
		 }
		 catch (InstanceNotFoundException e)
		 {
		     log.trace(e);
		 }
      }

      public int getID()
      {
         return mIdentification;
      }

	  public String toString()
	  {
		 return "Schedule target=" + getTargetString();
	  }

	  public String getTargetString()
	  {
		 return mTarget + " " + mMethodName + "" + Arrays.asList(mSchedulableMBeanArgumentTypes);
	  }

   }
}
