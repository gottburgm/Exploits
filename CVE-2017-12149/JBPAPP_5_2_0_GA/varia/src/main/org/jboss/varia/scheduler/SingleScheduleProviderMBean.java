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

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;

/**
 * SingleScheduleProvider MBean interface.
 * 
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $ 
 */
public interface SingleScheduleProviderMBean extends AbstractScheduleProviderMBean
{
   /** The default ObjectName */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss:service=SingleScheduleProvider");

   // Attributes ----------------------------------------------------
   
   /**
    * The ObjectName of the Schedulable MBean to call
    */
   void setTargetName(ObjectName objectName);   
   ObjectName getTargetName();

   /** 
    * The method name to call on the Schedulable MBean. It can optionally
    * be followed by an opening bracket, list of attributes (see below) and a closing bracket.
    * 
    * The list of attributes can contain:
    * <ul>
    *   <li>NOTIFICATION which will be replaced by the timers notification instance (javax.management.Notification)</li>
    *   <li>DATE which will be replaced by the date of the notification call (java.util.Date)</li>
    *   <li>REPETITIONS which will be replaced by the number of remaining repetitions (long)</li>
    *   <li>SCHEDULER_NAME which will be replaced by the Object Name of the Scheduler (javax.management.ObjectName)</li>
    *   <li>any full qualified Class name which the Scheduler will be set a "null" value for it</li>
    * </ul
    *  <br>
    * An example could be: "doSomething( NOTIFICATION, REPETITIONS, java.lang.String )"
    * where the Scheduler will pass the timer's notification instance, the remaining
    * repetitions as int and a null to the MBean's doSomething() method which must
    * have the following signature: doSomething(javax.management.Notification, long, java.lang.String).
    * 
    * @param method Name of the method to be called optional followed by method arguments (see above).
    * @throws IllegalArgumentException If the given value is not of the right format
    */
   void setTargetMethod(String method) throws IllegalArgumentException;
   String getTargetMethod();

   /**
    * The Schedule Period between two scheduled call.
    * 
    * @param period Time between to scheduled calls (after the initial call)
    *               in Milliseconds. This value must be bigger than 0.
    * @throws IllegalArgumentException If the given value is less or equal than 0
    */
   void setPeriod(long period);
   long getPeriod();

   /**
    * The date format used to parse date/times
    * @param dateFormat The date format when empty or null the locale is used to parse dates
    */
   void setDateFormat(String dateFormat);
   String getDateFormat();

   /**
    * The date/time of the first scheduled call. If the date is in the past
    * the scheduler tries to find the next available start date.
    * @param startDate Date when the initial call is scheduled. It can be either:
    * <ul>
    *   <li> NOW: date will be the current date (new Date()) plus 1 seconds </li>
    *   <li> Date as String able to be parsed by SimpleDateFormat with default format </li>
    *   <li> Date as String parsed using the date format attribute </li>
    *   <li> Milliseconds since 1/1/1970 </li>
    * </ul>
    * If the date is in the past the Scheduler will search a start date in the future
    * with respect to the initial repe- titions and the period between calls. This means
    * that when you restart the MBean (restarting JBoss etc.) it will start at the next
    * scheduled time. When no start date is available in the future the Scheduler will not start.
    * <br>
    * Example: if you start your Schedulable everyday at Noon and you restart your JBoss server
    * then it will start at the next Noon (the same if started before Noon or the next day if
    * start after Noon).
    */
   void setStartDate(String startDate);
   String getStartDate();

   /**
    * Sets the initial number of scheduled calls.
    * 
    * @param numberOfCalls Initial Number of scheduled calls. If -1 then the number is unlimited.
    * @throws IllegalArgumentException If the given value is less or equal than 0
    */
   void setRepetitions(long numberOfCalls);
   long getRepetitions();

}
