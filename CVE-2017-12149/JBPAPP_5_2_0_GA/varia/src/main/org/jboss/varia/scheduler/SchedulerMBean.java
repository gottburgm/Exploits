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

import java.security.InvalidParameterException;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;

/**
 * ScheduleMBean interface.
 * 
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public interface SchedulerMBean extends ServiceMBean
{
   /** The default ObjectName */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss:service=Scheduler");

   // Attributes ----------------------------------------------------

   /** 
    * The first scheduled call
    * 
    * - NOW: date will be the current date (new Date()) plus 1 seconds
    * - Date as String able to be parsed by SimpleDateFormat with default format
    * - Date as String parsed using the date format attribute
    * - Milliseconds since 1/1/1970
    * 
    * If the date is in the past the Scheduler will search a start date in the future
    * with respect to the initial repetitions and the period between calls.
    * This means that when you restart the MBean (restarting JBoss etc.) it will start
    * at the next scheduled time. When no start date is available in the future the
    * Scheduler will not start.
    * 
    * Example: if you start your Schedulable everyday at Noon and you restart your JBoss
    * server then it will start at the next Noon (the same if started before Noon or the
    * next day if start after Noon).
    */
   void setInitialStartDate(String startDate);
   String getInitialStartDate();
   
   /** The date format used to parse date/times - when empty or null the locale is used to parse dates */
   void setDateFormat(String dateFormat);
   String getDateFormat();
   
   /** The Schedule Period between two scheduled call (in msecs) */
   void setSchedulePeriod(long period);
   long getSchedulePeriod();
   
   /** The number of scheduled calls */
   void setInitialRepetitions(long numberOfCalls);
   long getInitialRepetitions();
   
   /** The fully qualified Class name of the Schedulable Class being called by the Scheduler. */
   void setSchedulableClass(String schedulableClass) throws java.security.InvalidParameterException;
   String getSchedulableClass(); 
   
   /** The arguments to pass to the schedule */
   void setSchedulableArguments(String argumentList);
   String getSchedulableArguments();   
   
   /**
    * The comma seperated list of argument types for the Schedulable class.
    * This will be used to find the right constructor and to created the right
    * instances to call the constructor with. This list must have as many elements
    * as the Schedulable Arguments list otherwise the start of the Scheduler will fail.
    * Right now only basic data types, String and Classes with a Constructor with a
    * String as only argument are supported.
    * 
    * If the list is null or empty then the no-args constructor is used.
    */
   void setSchedulableArgumentTypes(String typeList) throws java.security.InvalidParameterException;
   String getSchedulableArgumentTypes();
   
   /** The fully qualified JMX MBean name of the Schedulable MBean to be called.
    * Attention: if set the values set by {@link #setSchedulableClass},
    * {@link #setSchedulableArguments} and {@link #setSchedulableArgumentTypes}
    * are cleared and not used anymore. Therefore only use either Schedulable Class
    * or Schedulable MBean. If {@link #setSchedulableMBeanMethod} is not set then
    * the schedule method as in the {@link Schedulable#perform} will be called with
    * the same arguments. Also note that the Object Name will not be checked if the
    * MBean is available. If the MBean is not available it will not be called but the
    * remaining repetitions will be decreased. */
   void setSchedulableMBean(String schedulableMBean) throws java.security.InvalidParameterException;
   String getSchedulableMBean();
   
   /**
    * The method name to be called on the Schedulable MBean. It can optionally be
    * followed by an opening bracket, list of attributes (see below) and a closing bracket.
    * The list of attributes can contain:
    * - NOTIFICATION which will be replaced by the timers notification instance (javax.management.Notification)
    * - DATE which will be replaced by the date of the notification call (java.util.Date)
    * - REPETITIONS which will be replaced by the number of remaining repetitions (long)
    * - SCHEDULER_NAME which will be replaced by the Object Name of the Scheduler (javax.management.ObjectName)
    * - any full qualified Class name which the Scheduler will be set a "null" value for it
    * 
    * An example could be: "doSomething( NOTIFICATION, REPETITIONS, String )" where the Scheduler
    * will pass the timer's notification instance, the remaining repetitions as int and a null to
    * the MBean's doSomething() method which must have the following signature:
    * doSomething( javax.management.Notification, long, String ).
    */
   void setSchedulableMBeanMethod(String schedulableMBeanMethod) throws java.security.InvalidParameterException;
   String getSchedulableMBeanMethod();
   
   /** The default scheduling to use, fixed-rate or fixed-delay (false, default) */
   void setFixedRate(boolean fixedRate);
   boolean getFixedRate();

   /** Start the scheduler when the MBean started or not. */
   void setStartAtStartup(boolean startAtStartup);
   boolean isStartAtStartup();
   
   /** The JMX Timer to use (or create if not there) */
   void setTimerName(String timerName);    
   String getTimerName();
   
   // Informative Attributes ----------------------------------------
   
   long getRemainingRepetitions();   
   boolean isActive();   
   boolean isStarted();
   boolean isRestartPending();
   boolean isUsingMBean();
   
   // Operations ----------------------------------------------------
   
   /**
    * Starts the schedule if the schedule is stopped otherwise nothing will happen.
    * The Schedule is immediately set to started even the first call is in the future.
    * @throws InvalidParameterException If any of the necessary values are not set or invalid
    * (especially for the Schedulable class attributes).
    */
   void startSchedule();

   /**
    * Stops the schedule because it is either not used anymore or to restart it with new values.
    * @param doItNow If true the schedule will be stopped without waiting for the next scheduled
    * call otherwise the next call will be performed before the schedule is stopped.
    */
   void stopSchedule(boolean doItNow);

   /**
    * Stops the schedule immediately.
    */
   void stopSchedule();

   /**
    * Stops the server right now and starts it right now.
    */
   void restartSchedule();
}
