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

import java.lang.reflect.Constructor;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Arrays;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.MBeanServerInvocationHandler;
import javax.management.timer.Timer;
import javax.management.timer.TimerMBean;
import javax.management.timer.TimerNotification;

import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.Classes;

/**
 * Schedules a timer task that calls an MBean or Object instance.
 * Any MBean operation can be called.  Object instances must implement the
 * {@link Schedulable} interface.
 * <p />
 * Create a separate Scheduler MBean for every MBean or Object you wish to call.
 * One example naming strategy for calling an MBean named:
 <code>example:type=HelloWorld</code>
 * is to create a similarly named:
 <code>example:type=Scheduler,call=HelloWorld</code> MBean.
 * This way you should not run into a name conflict.
 * <p>
 * This MBean registers a notification listener with an
 * javax.management.timer.Timer MBean. If the Timer does not exist, this MBean
 * will create it.  Each Timer can handle multiple Scheduler instances.
 * </p>
 *
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author Cameron (camtabor)
 * @version $Revision: 81038 $
 */
public class Scheduler extends ServiceMBeanSupport
   implements SchedulerMBean
{

   // -------------------------------------------------------------------------
   // Constants
   // -------------------------------------------------------------------------

   public static String JNDI_NAME = "scheduler:domain";
   public static String JMX_NAME = "scheduler";
   public static String DEFAULT_TIMER_NAME = ScheduleManager.DEFAULT_TIMER_NAME;

   private static final int NOTIFICATION = 0;
   private static final int DATE = 1;
   private static final int REPETITIONS = 2;
   private static final int SCHEDULER_NAME = 3;
   private static final int NULL = 4;

   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------

   private long mActualSchedulePeriod;
   private long mRemainingRepetitions = 0;
   private int mNotificationID = -1;
   private String mTimerName = DEFAULT_TIMER_NAME;
   private ObjectName mTimerObjectName;
   private TimerMBean mTimer;
   private NotificationEmitter mTimerEmitter;
   private Schedulable mSchedulable;

   private boolean mScheduleIsStarted = false;
   private boolean mWaitForNextCallToStop = false;
   private boolean mStartOnStart = false;
   private boolean mIsRestartPending = true;

   // Pending values which can be different to the actual ones
   private boolean mUseMBean = false;

   private Class mSchedulableClass;
   private String mSchedulableArguments;
   private String[] mSchedulableArgumentList = new String[0];
   private String mSchedulableArgumentTypes;
   private Class[] mSchedulableArgumentTypeList = new Class[0];

   private ObjectName mSchedulableMBean;
   private String mSchedulableMBeanMethod;
   private String mSchedulableMBeanMethodName;
   private int[] mSchedulableMBeanArguments = new int[0];
   private String[] mSchedulableMBeanArgumentTypes = new String[0];

   private SimpleDateFormat mDateFormatter;
   private Date mStartDate;
   private String mStartDateString;
   private boolean mStartDateIsNow;
   private long mSchedulePeriod;
   private long mInitialRepetitions;
   private boolean mFixedRate = false;
   
   private NotificationListener mListener;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * Constructs a new Scheduler instance.
    **/
   public Scheduler()
   {
   }

   /**
    * Constructs a new Scheduler instance.
    * @param pSchedulableClass
    * @param pSchedulePeriod
    */ 
   public Scheduler(String pSchedulableClass,
      long pSchedulePeriod)
   {
      setStartAtStartup(true);
      setSchedulableClass(pSchedulableClass);
      setSchedulePeriod(pSchedulePeriod);
   }

   /**
    * Constructs a new Scheduler instance.
    * @param pSchedulableClass
    * @param pInitArguments
    * @param pInitTypes
    * @param pInitialStartDate
    * @param pSchedulePeriod
    * @param pNumberOfRepetitions
    */ 
   public Scheduler(String pSchedulableClass,
      String pInitArguments,
      String pInitTypes,
      String pInitialStartDate,
      long pSchedulePeriod,
      long pNumberOfRepetitions
      )
   {
      setStartAtStartup(true);
      setSchedulableClass(pSchedulableClass);
      setSchedulableArguments(pInitArguments);
      setSchedulableArgumentTypes(pInitTypes);
      setInitialStartDate(pInitialStartDate);
      setSchedulePeriod(pSchedulePeriod);
      setInitialRepetitions(pNumberOfRepetitions);
   }

   /**
    * Constructs a new Scheduler instance.
    * @param pSchedulableClass
    * @param pInitArguments
    * @param pInitTypes
    * @param pDateFormat
    * @param pInitialStartDate
    * @param pSchedulePeriod
    * @param pNumberOfRepetitions
    */ 
   public Scheduler(
      String pSchedulableClass,
      String pInitArguments,
      String pInitTypes,
      String pDateFormat,
      String pInitialStartDate,
      long pSchedulePeriod,
      long pNumberOfRepetitions
      )
   {
      setStartAtStartup(true);
      setSchedulableClass(pSchedulableClass);
      setSchedulableArguments(pInitArguments);
      setSchedulableArgumentTypes(pInitTypes);
      setDateFormat(pDateFormat);
      setInitialStartDate(pInitialStartDate);
      setSchedulePeriod(pSchedulePeriod);
      setInitialRepetitions(pNumberOfRepetitions);
   }

   // -------------------------------------------------------------------------
   // SchedulerMBean Methods
   // -------------------------------------------------------------------------
   //

   private void checkMBean() {
	  if (mSchedulableMBean == null)
	  {
		 log.debug("Schedulable MBean Object Name is not set");
		 throw new InvalidParameterException(
			"Schedulable MBean must be set"
		 );
	  }
	  if (mSchedulableMBeanMethodName == null)
	  {
		 mSchedulableMBeanMethodName = "perform";
		 mSchedulableMBeanArguments = new int[]{DATE, REPETITIONS};
		 mSchedulableMBeanArgumentTypes = new String[]{
			Date.class.getName(),
			Integer.TYPE.getName()
		 };
	  }
   }

   private void createSchedulable() {
	  if (mSchedulableClass == null)
	  {
		 throw new InvalidParameterException("Schedulable Class not set");
	  }
	  if (mSchedulableArgumentList.length != mSchedulableArgumentTypeList.length)
	  {
		 throw new InvalidParameterException(
			"Schedulable Class Arguments and Types do not match in length"
		 );
	  }
	  // Create all the Objects for the Constructor to be called
	  Object[] lArgumentList = new Object[mSchedulableArgumentTypeList.length];
	  try
	  {
		 for (int i = 0; i < mSchedulableArgumentTypeList.length; i++)
		 {
			Class lClass = mSchedulableArgumentTypeList[i];
			if (lClass == Boolean.TYPE)
			{
			   lArgumentList[i] = new Boolean(mSchedulableArgumentList[i]);
			}
			else if (lClass == Integer.TYPE)
			{
			   lArgumentList[i] = new Integer(mSchedulableArgumentList[i]);
			}
			else if (lClass == Long.TYPE)
			{
			   lArgumentList[i] = new Long(mSchedulableArgumentList[i]);
			}
			else if (lClass == Short.TYPE)
			{
			   lArgumentList[i] = new Short(mSchedulableArgumentList[i]);
			}
			else if (lClass == Float.TYPE)
			{
			   lArgumentList[i] = new Float(mSchedulableArgumentList[i]);
			}
			else if (lClass == Double.TYPE)
			{
			   lArgumentList[i] = new Double(mSchedulableArgumentList[i]);
			}
			else if (lClass == Byte.TYPE)
			{
			   lArgumentList[i] = new Byte(mSchedulableArgumentList[i]);
			}
			else if (lClass == Character.TYPE)
			{
			   lArgumentList[i] = new Character(mSchedulableArgumentList[i].charAt(0));
			}
			else
			{
			   Constructor lConstructor = lClass.getConstructor(new Class[]{String.class});
			   lArgumentList[i] = lConstructor.newInstance(new Object[]{mSchedulableArgumentList[i]});
			}
		 }
	  }
	  catch (Exception e)
	  {
		 log.error("Could not load or create constructor argument", e);
		 throw new InvalidParameterException("Could not load or create a constructor argument");
	  }
	  try
	  {
		 // Check if constructor is found
		 Constructor lSchedulableConstructor = mSchedulableClass.getConstructor(mSchedulableArgumentTypeList);
		 // Create an instance of it
		 mSchedulable = (Schedulable) lSchedulableConstructor.newInstance(lArgumentList);
	  }
	  catch (Exception e)
	  {
		 log.error("Could not find the constructor or create Schedulable instance", e);
		 throw new InvalidParameterException("Could not find the constructor or create the Schedulable Instance");
	  }
   }

   private Date getNow() {
	  long now = System.currentTimeMillis();
	  return new Date(now + 1000);
   }

   private void initStartDate() {
	  // Register the Schedule at the Timer
	  // If start date is NOW then take the current date
	  if (mStartDateIsNow)
	  {
		 mStartDate = getNow();
	  }
	  else
	  {
		 // Check if initial start date is in the past
		 if (mStartDate.before(new Date()))
		 {
			// If then first check if a repetition is in the future
			long lNow = new Date().getTime() + 100;
			long lSkipRepeats = ((lNow - mStartDate.getTime()) / mActualSchedulePeriod) + 1;
			log.debug("Old start date: " + mStartDate + ", now: " + new Date(lNow) + ", Skip repeats: " + lSkipRepeats);
			if (mRemainingRepetitions > 0)
			{
			   // If not infinit loop
			   if (lSkipRepeats >= mRemainingRepetitions)
			   {
				  // No repetition left -> exit
				  log.info("No repetitions left because start date is in the past and could " +
					 "not be reached by Initial Repetitions * Schedule Period");
				  return;
			   }
			   else
			   {
				  // Reduce the missed hits
				  mRemainingRepetitions -= lSkipRepeats;
			   }
			}
			mStartDate = new Date(mStartDate.getTime() + (lSkipRepeats * mActualSchedulePeriod));
		 }
	  }
   }

   /**
    * Starts the schedule if the schedule is stopped otherwise nothing will happen.
    * The Schedule is immediately set to started even the first call is in the
    * future.
    *
    * @jmx:managed-operation
    *
    * @throws InvalidParameterException If any of the necessary values are not set
    *                                   or invalid (especially for the Schedulable
    *                                   class attributes).
    */
   public void startSchedule()
   {
      if (isStarted())
      {
		 log.debug("already started");
		 return;
	  }

	  if (mUseMBean)
	  {
		 checkMBean();
	  }
	  else
	  {
		 createSchedulable();
	  }

	  mRemainingRepetitions = mInitialRepetitions;
	  mActualSchedulePeriod = mSchedulePeriod;
	  initStartDate();

	  log.debug("Schedule initial call to: " + mStartDate + ", remaining repetitions: " + mRemainingRepetitions);
	  mNotificationID = mTimer.addNotification(
			"Schedule", "Scheduler Notification",
			null, // new Integer(getID()), // User Object
			mStartDate,
			new Long(mActualSchedulePeriod),
			mRemainingRepetitions < 0 ? new Long(0) : new Long(mRemainingRepetitions),
			Boolean.valueOf(mFixedRate)
	  );
	  mListener = mUseMBean ? new MBeanListener() : new PojoScheduler();
	  mTimerEmitter.addNotificationListener(
			mListener,
			new ScheduleManager.IdNotificationFilter(mNotificationID),
			null
	  );
	  mScheduleIsStarted = true;
	  mIsRestartPending = false;
   }

   /**
    * Stops the schedule immediately.
    * @jmx:managed-operation
    */
   public void stopSchedule()
   {
	  stopSchedule(true);
   }

   /**
    * Stops the schedule because it is either not used anymore or to restart it with
    * new values.
    *
    * @jmx:managed-operation
    *
    * @param pDoItNow If true the schedule will be stopped without waiting for the next
    *                 scheduled call otherwise the next call will be performed before
    *                 the schedule is stopped.
    */
   public void stopSchedule(boolean pDoItNow)
   {
	  log.debug("stopSchedule(" + pDoItNow + ")");
      try
      {
         if (mNotificationID < 0)
         {
            mScheduleIsStarted = false;
            mWaitForNextCallToStop = false;
            return;
         }
         if (pDoItNow)
         {
            log.debug("stopSchedule(), removing schedule id: " + mNotificationID);
            mWaitForNextCallToStop = false;
            if (mListener != null)
            {
			   mTimerEmitter.removeNotificationListener(mListener);
			   try
			   {
				  mTimer.removeNotification(mNotificationID);
			   }
			   catch (InstanceNotFoundException e)
			   {
			      log.trace(e);
			   }
               mListener = null;
            }
			mNotificationID = -1;
            mScheduleIsStarted = false;
         }
         else
         {
            mWaitForNextCallToStop = true;
         }
      }
      catch (Exception e)
      {
         log.error("stopSchedule failed", e);
      }
   }

   /**
    * Stops the server right now and starts it right now.
    *
    * @jmx:managed-operation
    */
   public void restartSchedule()
   {
      stopSchedule();
      startSchedule();
   }

   /**
    * @jmx:managed-attribute
    *
    * @return Full qualified Class name of the schedulable class called by the schedule or
    *         null if not set.
    */
   public String getSchedulableClass()
   {
      if (mSchedulableClass == null)
      {
         return null;
      }
      return mSchedulableClass.getName();
   }

   /**
    * Sets the fully qualified Class name of the Schedulable Class being called by the
    * Scheduler. Must be set before the Schedule is started. Please also set the
    * {@link #setSchedulableArguments} and {@link #setSchedulableArgumentTypes}.
    *
    * @jmx:managed-attribute
    *
    * @param pSchedulableClass Fully Qualified Schedulable Class.
    *
    * @throws InvalidParameterException If the given value is not a valid class or cannot
    *                                   be loaded by the Scheduler or is not of instance
    *                                   Schedulable.
    */
   public void setSchedulableClass(String pSchedulableClass)
      throws InvalidParameterException
   {
      if (pSchedulableClass == null || pSchedulableClass.equals(""))
      {
         throw new InvalidParameterException("Schedulable Class cannot be empty or undefined");
      }
      try
      {
         // Try to load the Schedulable Class
         ClassLoader loader = TCLActions.getContextClassLoader();
         mSchedulableClass = loader.loadClass(pSchedulableClass);
         // Check if instance of Schedulable
         if (!isSchedulable(mSchedulableClass))
         {
            String msg = "Given class " + pSchedulableClass + " is not instance of Schedulable";
            StringBuffer info = new StringBuffer(msg);
            info.append("\nThe SchedulableClass info:");
            Classes.displayClassInfo(mSchedulableClass, info);
            info.append("\nSchedulable.class info:");
            Classes.displayClassInfo(Schedulable.class, info);
            log.debug(info.toString());
            throw new InvalidParameterException(msg);
         }
      }
      catch (ClassNotFoundException e)
      {
         log.info("Failed to find: "+pSchedulableClass, e);
         throw new InvalidParameterException(
            "Given class " + pSchedulableClass + " is not  not found"
         );
      }
      mIsRestartPending = true;
      mUseMBean = false;
   }

   /**
    * @jmx:managed-attribute
    *
    * @return Comma seperated list of Constructor Arguments used to instantiate the
    *         Schedulable class instance. Right now only basic data types, String and
    *         Classes with a Constructor with a String as only argument are supported.
    */
   public String getSchedulableArguments()
   {
      return mSchedulableArguments;
   }

   /**
    * @jmx:managed-attribute
    *
    * Sets the comma seperated list of arguments for the Schedulable class. Note that
    * this list must have as many elements as the Schedulable Argument Type list otherwise
    * the start of the Scheduler will fail. Right now only basic data types, String and
    * Classes with a Constructor with a String as only argument are supported.
    *
    * @param pArgumentList List of arguments used to create the Schedulable intance. If
    *                      the list is null or empty then the no-args constructor is used.
    */
   public void setSchedulableArguments(String pArgumentList)
   {
      if (pArgumentList == null || pArgumentList.equals(""))
      {
         mSchedulableArgumentList = new String[0];
      }
      else
      {
         StringTokenizer lTokenizer = new StringTokenizer(pArgumentList, ",");
         Vector lList = new Vector();
         while (lTokenizer.hasMoreTokens())
         {
            String lToken = lTokenizer.nextToken().trim();
            if (lToken.equals(""))
            {
               lList.add("null");
            }
            else
            {
               lList.add(lToken);
            }
         }
         mSchedulableArgumentList = (String[]) lList.toArray(new String[0]);
      }
      mSchedulableArguments = pArgumentList;
      mIsRestartPending = true;
   }

   /**
    * @jmx:managed-attribute
    *
    * @return A comma seperated list of Argument Types which should match the list of
    *         arguments.
    */
   public String getSchedulableArgumentTypes()
   {
      return mSchedulableArgumentTypes;
   }

   /**
    * Sets the comma seperated list of argument types for the Schedulable class. This will
    * be used to find the right constructor and to created the right instances to call the
    * constructor with. This list must have as many elements as the Schedulable Arguments
    * list otherwise the start of the Scheduler will fail. Right now only basic data types,
    * String and Classes with a Constructor with a String as only argument are supported.
    *
    * @jmx:managed-attribute
    *
    * @param pTypeList List of arguments used to create the Schedulable intance. If
    *                  the list is null or empty then the no-args constructor is used.
    *
    * @throws InvalidParameterException If the given list contains a unknow datat type.
    */
   public void setSchedulableArgumentTypes(String pTypeList)
      throws InvalidParameterException
   {
      if (pTypeList == null || pTypeList.equals(""))
      {
         mSchedulableArgumentTypeList = new Class[0];
      }
      else
      {
         StringTokenizer lTokenizer = new StringTokenizer(pTypeList, ",");
         Vector lList = new Vector();
         while (lTokenizer.hasMoreTokens())
         {
            String lToken = lTokenizer.nextToken().trim();
            // Get the class
            Class lClass = null;
            if (lToken.equals("short"))
            {
               lClass = Short.TYPE;
            }
            else if (lToken.equals("int"))
            {
               lClass = Integer.TYPE;
            }
            else if (lToken.equals("long"))
            {
               lClass = Long.TYPE;
            }
            else if (lToken.equals("byte"))
            {
               lClass = Byte.TYPE;
            }
            else if (lToken.equals("char"))
            {
               lClass = Character.TYPE;
            }
            else if (lToken.equals("float"))
            {
               lClass = Float.TYPE;
            }
            else if (lToken.equals("double"))
            {
               lClass = Double.TYPE;
            }
            else if (lToken.equals("boolean"))
            {
               lClass = Boolean.TYPE;
            }
            if (lClass == null)
            {
               try
               {
                  // Load class to check if available
                  ClassLoader loader = TCLActions.getContextClassLoader();
                  lClass = loader.loadClass(lToken);
               }
               catch (ClassNotFoundException cnfe)
               {
                  throw new InvalidParameterException(
                     "The argument type: " + lToken + " is not a valid class or could not be found"
                  );
               }
            }
            lList.add(lClass);
         }
         mSchedulableArgumentTypeList = (Class[]) lList.toArray(new Class[0]);
      }
      mSchedulableArgumentTypes = pTypeList;
      mIsRestartPending = true;
   }

   /**
    * @jmx:managed-attribute
    *
    * @return Object Name if a Schedulable MBean is set
    */
   public String getSchedulableMBean()
   {
      return mSchedulableMBean == null ?
         null :
         mSchedulableMBean.toString();
   }

   /**
    * Sets the fully qualified JMX MBean name of the Schedulable MBean to be called.
    * <b>Attention: </b>if set the all values set by {@link #setSchedulableClass},
    * {@link #setSchedulableArguments} and {@link #setSchedulableArgumentTypes} are
    * cleared and not used anymore. Therefore only use either Schedulable Class or
    * Schedulable MBean. If {@link #setSchedulableMBeanMethod} is not set then the
    * schedule method as in the {@link Schedulable#perform} will be called with the
    * same arguments. Also note that the Object Name will not be checked if the
    * MBean is available. If the MBean is not available it will not be called but
    * the remaining repetitions will be decreased.
    *
    * @jmx:managed-attribute
    *
    * @param pSchedulableMBean JMX MBean Object Name which should be called.
    *
    * @throws InvalidParameterException If the given value is an valid Object Name.
    */
   public void setSchedulableMBean(String pSchedulableMBean)
      throws InvalidParameterException
   {
      if (pSchedulableMBean == null)
      {
         throw new InvalidParameterException("Schedulable MBean must be specified");
      }
      try
      {
         mSchedulableMBean = new ObjectName(pSchedulableMBean);
         mUseMBean = true;
      }
      catch (MalformedObjectNameException e)
      {
         throw new InvalidParameterException("Schedulable MBean name invalid " + pSchedulableMBean);
      }
   }

   /**
    * @return Schedulable MBean Method description if set
    **/
   public String getSchedulableMBeanMethod()
   {
      return mSchedulableMBeanMethod;
   }

   /**
    * Sets the method name to be called on the Schedulable MBean. It can optionally be
    * followed by an opening bracket, list of attributes (see below) and a closing bracket.
    * The list of attributes can contain:
    * <ul>
    * <li>NOTIFICATION which will be replaced by the timers notification instance
    *     (javax.management.Notification)</li>
    * <li>DATE which will be replaced by the date of the notification call
    *     (java.util.Date)</li>
    * <li>REPETITIONS which will be replaced by the number of remaining repetitions
    *     (long)</li>
    * <li>SCHEDULER_NAME which will be replaced by the Object Name of the Scheduler
    *     (javax.management.ObjectName)</li>
    * <li>any full qualified Class name which the Scheduler will be set a "null" value
    *     for it</li>
    * </ul>
    * <br>
    * An example could be: "doSomething( NOTIFICATION, REPETITIONS, java.lang.String )"
    * where the Scheduler will pass the timer's notification instance, the remaining
    * repetitions as int and a null to the MBean's doSomething() method which must
    * have the following signature: doSomething( javax.management.Notification, long,
    * java.lang.String ).
    *
    * @jmx:managed-attribute
    *
    * @param pSchedulableMBeanMethod Name of the method to be called optional followed
    *                                by method arguments (see above).
    *
    * @throws InvalidParameterException If the given value is not of the right
    *                                   format
    */
   public void setSchedulableMBeanMethod(String pSchedulableMBeanMethod)
      throws InvalidParameterException
   {
      if (pSchedulableMBeanMethod == null)
      {
         mSchedulableMBeanMethod = null;
         return;
      }
      int lIndex = pSchedulableMBeanMethod.indexOf('(');
	  String lMethodName;
      if (lIndex == -1)
      {
         lMethodName = pSchedulableMBeanMethod.trim();
         mSchedulableMBeanArguments = new int[0];
         mSchedulableMBeanArgumentTypes = new String[0];
      }
      else
      {
         lMethodName = pSchedulableMBeanMethod.substring(0, lIndex).trim();
      }
      if (lMethodName.equals(""))
      {
         lMethodName = "perform";
      }
      if (lIndex >= 0)
      {
         int lIndex2 = pSchedulableMBeanMethod.indexOf(')');
         if (lIndex2 < lIndex)
         {
            throw new InvalidParameterException("Schedulable MBean Method: closing bracket must be after opening bracket");
         }
         if (lIndex2 < pSchedulableMBeanMethod.length() - 1)
         {
            String lRest = pSchedulableMBeanMethod.substring(lIndex2 + 1).trim();
            if (lRest.length() > 0)
            {
               throw new InvalidParameterException("Schedulable MBean Method: nothing should be after closing bracket");
            }
         }
         String lArguments = pSchedulableMBeanMethod.substring(lIndex + 1, lIndex2).trim();
         if (lArguments.equals(""))
         {
            mSchedulableMBeanArguments = new int[0];
            mSchedulableMBeanArgumentTypes = new String[0];
         }
         else
         {
            StringTokenizer lTokenizer = new StringTokenizer(lArguments, ", ");
            mSchedulableMBeanArguments = new int[lTokenizer.countTokens()];
            mSchedulableMBeanArgumentTypes = new String[lTokenizer.countTokens()];
            for (int i = 0; lTokenizer.hasMoreTokens(); i++)
            {
               String lToken = lTokenizer.nextToken().trim();
               if (lToken.equals("NOTIFICATION"))
               {
                  mSchedulableMBeanArguments[i] = NOTIFICATION;
                  mSchedulableMBeanArgumentTypes[i] = Notification.class.getName();
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
         }
      }
      mSchedulableMBeanMethodName = lMethodName;
      mSchedulableMBeanMethod = pSchedulableMBeanMethod;
   }

   /**
    * @jmx:managed-attribute
    *
    * @return True if the Scheduler uses a Schedulable MBean, false if it uses a
    *         Schedulable class
    */
   public boolean isUsingMBean()
   {
      return mUseMBean;
   }

   /**
    * @jmx:managed-attribute
    *
    * @return Schedule Period between two scheduled calls in Milliseconds. It will always
    *         be bigger than 0 except it returns -1 then the schedule is stopped.
    */
   public long getSchedulePeriod()
   {
      return mSchedulePeriod;
   }

   /**
    * Sets the Schedule Period between two scheduled call.
    *
    * @jmx:managed-attribute
    *
    * @param pPeriod Time between to scheduled calls (after the initial call) in Milliseconds.
    *                This value must be bigger than 0.
    *
    * @throws InvalidParameterException If the given value is less or equal than 0
    */
   public void setSchedulePeriod(long pPeriod)
   {
      if (pPeriod <= 0)
      {
         throw new InvalidParameterException("Schedulable Period may be not less or equals than 0");
      }
      mSchedulePeriod = pPeriod;
      mIsRestartPending = true;
   }

   /**
    * @jmx:managed-attribute
    *
    * @return the date format
    */
   public String getDateFormat()
   {
      if (mDateFormatter == null)
         mDateFormatter = new SimpleDateFormat();
      return mDateFormatter.toPattern();
   }

   /**
    * Sets the date format used to parse date/times
    *
    * @jmx:managed-attribute
    *
    * @param dateFormat The date format when empty or null the locale is used to parse dates
    */
   public void setDateFormat(String dateFormat)
   {
      if (dateFormat == null || dateFormat.trim().length() == 0)
         mDateFormatter = new SimpleDateFormat();
      else
         mDateFormatter = new SimpleDateFormat(dateFormat);
   }

   /**
    * @jmx:managed-attribute
    *
    * @return Date (and time) of the first scheduled. For value see {@link #setInitialStartDate}
    *         method.
    */
   public String getInitialStartDate()
   {
      return mStartDateString;
   }

   /**
    * Sets the first scheduled call. If the date is in the past the scheduler tries to find the
    * next available start date.
    *
    * @jmx:managed-attribute
    *
    * @param pStartDate Date when the initial call is scheduled. It can be either:
    *                   <ul>
    *                      <li>
    *                         NOW: date will be the current date (new Date()) plus 1 seconds
    *                      </li><li>
    *                         Date as String able to be parsed by SimpleDateFormat with default format
    *                      </li><li>
    *                         Date as String parsed using the date format attribute
    *                      </li><li>
    *                         Milliseconds since 1/1/1970
    *                      </li>
    *                   </ul>
    *                   If the date is in the past the Scheduler
    *                   will search a start date in the future with respect to the initial repe-
    *                   titions and the period between calls. This means that when you restart
    *                   the MBean (restarting JBoss etc.) it will start at the next scheduled
    *                   time. When no start date is available in the future the Scheduler will
    *                   not start.<br>
    *                   Example: if you start your Schedulable everyday at Noon and you restart
    *                   your JBoss server then it will start at the next Noon (the same if started
    *                   before Noon or the next day if start after Noon).
    */
   public void setInitialStartDate(String pStartDate)
   {
      mStartDateString = pStartDate == null ? "" : pStartDate.trim();
      if (mStartDateString.equals(""))
      {
         mStartDate = new Date(0);
      }
      else if (mStartDateString.equals("NOW"))
      {
         mStartDate = getNow();
         mStartDateIsNow = true;
      }
      else
      {
         try
         {
            long lDate = new Long(pStartDate).longValue();
            mStartDate = new Date(lDate);
            mStartDateIsNow = false;
         }
         catch (NumberFormatException e)
         {
            try
            {
               if (mDateFormatter == null)
               {
                  mDateFormatter = new SimpleDateFormat();
               }
               mStartDate = mDateFormatter.parse(mStartDateString);
               mStartDateIsNow = false;
            }
            catch (Exception e2)
            {
               log.error("Could not parse given date string: " + mStartDateString, e2);
               throw new InvalidParameterException("Schedulable Date is not of correct format: " + mStartDateString);
            }
         }
      }
      log.debug("Initial Start Date is set to: " + mStartDate);
   }

   /**
    * @jmx:managed-attribute
    *
    * @return Number of scheduled calls initially. If -1 then there is not limit.
    */
   public long getInitialRepetitions()
   {
      return mInitialRepetitions;
   }

   /**
    * Sets the initial number of scheduled calls.
    *
    * @jmx:managed-attribute
    *
    * @param pNumberOfCalls Initial Number of scheduled calls. If -1 then the number
    *                       is infinite
    *
    * @throws InvalidParameterException If the given value is less or equal than 0
    */
   public void setInitialRepetitions(long pNumberOfCalls)
   {
      if (pNumberOfCalls <= 0)
      {
         pNumberOfCalls = -1;
      }
      mInitialRepetitions = pNumberOfCalls;
      mIsRestartPending = true;
   }

   /**
    * @jmx:managed-attribute
    *
    * @return Number of remaining repetitions. If -1 then there is no limit.
    */
   public long getRemainingRepetitions()
   {
      return mRemainingRepetitions;
   }

   /**
    * @jmx:managed-attribute
    *
    * @return True if the schedule is up and running. If you want to start the schedule
    *         with another values by using {@ #startSchedule} you have to stop the schedule
    *         first with {@ #stopSchedule} and wait until this method returns false.
    */
   public boolean isStarted()
   {
      return mScheduleIsStarted;
   }

   /**
    * @jmx:managed-attribute
    *
    * @return True if any attributes are changed but the Schedule is not restarted yet.
    */
   public boolean isRestartPending()
   {
      return mIsRestartPending;
   }

   /**
    * @jmx:managed-attribute
    *
    * @return True if the Schedule when the Scheduler is started
    */
   public boolean isStartAtStartup()
   {
      return mStartOnStart;
   }

   /**
    * Set the scheduler to start when MBean started or not. Note that this method only
    * affects when the {@link #startService startService()} gets called (normally at
    * startup time.
    *
    * @jmx:managed-attribute
    *
    * @param pStartAtStartup True if Schedule has to be started at startup time
    */
   public void setStartAtStartup(boolean pStartAtStartup)
   {
      mStartOnStart = pStartAtStartup;
   }

   /**
    * @jmx:managed-attribute
    *
    * @return True if this Scheduler is active and will send notifications in the future
    */
   public boolean isActive()
   {
      return isStarted() && mRemainingRepetitions != 0;
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
   // Methods
   // -------------------------------------------------------------------------

   // -------------------------------------------------------------------------
   // ServiceMBean - Methods
   // -------------------------------------------------------------------------

   protected void startService()
      throws Exception
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
      if (mStartOnStart)
      {
         log.debug("Start Scheduler on start up time");
         startSchedule();
      }
   }

   protected void stopService()
   {
      stopSchedule();
   }

   private static boolean isSchedulable(Class c)
   {
      boolean lFound = false;
      do
      {
         Class[] lInterfaces = c.getInterfaces();
         for (int i = 0; i < lInterfaces.length; i++)
         {
            if (lInterfaces[i] == Schedulable.class)
            {
               lFound = true;
               break;
            }
         }
         c = c.getSuperclass();
      } 
      while (c != null && !lFound);
      return lFound;
   }

   /**
	* Base class for listeners.
	*/
   public abstract class BaseListener
      implements NotificationListener
   {
      final Logger log = Logger.getLogger(BaseListener.class);

      public void handleNotification(
         Notification notification,
         Object handback
         )
      {
		 boolean trace = log.isTraceEnabled();
		 if (trace)
		 {
			log.trace("handleNotification: " + notification);
		 }
		 if (!isStarted())
		 {
			log.trace("Scheduler not started");
			stopSchedule();
			return;
		 }
		 if (mRemainingRepetitions == 0)
		 {
			log.trace("No more repetitions");
			stopSchedule();
			return;
		 }
		 if (mRemainingRepetitions > 0)
		 {
			mRemainingRepetitions--;
			if (trace)
			   log.trace("Remaining repetitions: " + mRemainingRepetitions);
		 }
		 invoke(notification);
		 if (mWaitForNextCallToStop)
		 {
			stopSchedule();
		 }
      }

	  /**
	   * Invokes the scheduler method.
	   */
	  protected abstract void invoke(Notification notification);

   }

   // -------------------------------------------------------------------------
   // Inner Classes
   // -------------------------------------------------------------------------

   /**
	* Calls {@link Schedulable#perform} on a plain Java Object.
	*/
   public class PojoScheduler extends BaseListener
   {

	  protected void invoke(Notification notification)
      {
         ClassLoader currentTCL = TCLActions.getContextClassLoader();
         try
         {
            ClassLoader loader = TCLActions.getClassLoader(mSchedulable.getClass());
            TCLActions.setContextClassLoader(loader);
            Date lTimeStamp = new Date(notification.getTimeStamp());
			mSchedulable.perform(lTimeStamp, getRemainingRepetitions());
         }
         catch (Exception e)
         {
            log.error("Scheduler.perform call failed", e);
         }
         finally
         {
            TCLActions.setContextClassLoader(currentTCL);
         }
      }
   }

   /**
	* Invokes an operation on an MBean.
	*/
   public class MBeanListener extends BaseListener
   {
	  protected void invoke(Notification notification)
      {
		 Object[] lArguments = new Object[mSchedulableMBeanArguments.length];
		 for (int i = 0; i < lArguments.length; i++)
		 {
			switch (mSchedulableMBeanArguments[i])
			{
			   case NOTIFICATION:
				  lArguments[i] = notification;
				  break;
			   case DATE:
				  lArguments[i] = new Date(notification.getTimeStamp());
				  break;
			   case REPETITIONS:
				  lArguments[i] = new Long(mRemainingRepetitions);
				  break;
			   case SCHEDULER_NAME:
				  lArguments[i] = getServiceName();
				  break;
			   default:
				  lArguments[i] = null;
			}
		 }
		 if (log.isTraceEnabled())
		 {
			log.debug("invoke " + mSchedulableMBean + " " + mSchedulableMBeanMethodName);
			log.debug("arguments: " + Arrays.asList(lArguments));
			log.debug("argument types: " + Arrays.asList(mSchedulableMBeanArgumentTypes));
		 }
		 try
		 {
			getServer().invoke(
			   mSchedulableMBean,
			   mSchedulableMBeanMethodName,
			   lArguments,
			   mSchedulableMBeanArgumentTypes
			);
		 }
		 catch (Exception e)
		 {
			log.error("Invoke failed for " + mSchedulableMBean + " " + mSchedulableMBeanMethodName, e);
		 }
      }
   }

}
