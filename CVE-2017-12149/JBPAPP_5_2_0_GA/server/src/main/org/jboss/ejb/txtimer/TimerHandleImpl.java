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

// $Id: TimerHandleImpl.java 107116 2010-07-27 15:41:59Z jaikiran $

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.management.ObjectName;

/**
 * An implementation of the TimerHandle
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 107116 $
 * @since 07-Apr-2004
 */
public class TimerHandleImpl implements TimerHandle
{
   /**
    * The date pattern used by this handle
    */
   public static final String DATE_PATTERN = "dd-MMM-yyyy HH:mm:ss.SSS";

   // The initial txtimer properties
   private String timerId;
   private TimedObjectId timedObjectId;
   private Date firstTime;
   private Date nextTimeout;
   private long periode;
   private Serializable info;
   private int hashCode;

   /**
    * Construct a handle from a timer
    */
   TimerHandleImpl(TimerImpl timer)
   {
      timerId = timer.getTimerId();
      timedObjectId = timer.getTimedObjectId();
      firstTime = timer.getFirstTime();
      nextTimeout = new Date(timer.getNextExpire());
      periode = timer.getPeriode();
      info = timer.getInfoInternal();
   }

   /**
    * Construct a handle from individual parameters
    * @deprecated Use {@link #TimerHandleImpl(String, TimedObjectId, Date, Date, long, Serializable)}
    */
   @Deprecated
   TimerHandleImpl(String timerId, TimedObjectId timedObjectId, Date firstTime, long periode, Serializable info)
   {
      this.timerId = timerId;
      this.timedObjectId = timedObjectId;
      this.firstTime = firstTime;
      this.periode = periode;
      this.info = info;
   }
   
   /**
    * Constructs an timer handle from the passed parameters
    * @param timerId
    * @param timedObjectId
    * @param firstTime
    * @param nextTimeout
    * @param periode
    * @param info
    */
   TimerHandleImpl(String timerId, TimedObjectId timedObjectId, Date firstTime, Date nextTimeout, long periode, Serializable info)
   {
      this.timerId = timerId;
      this.timedObjectId = timedObjectId;
      this.firstTime = firstTime;
      this.nextTimeout = nextTimeout;
      this.periode = periode;
      this.info = info;
   }

   /**
    * Construct a handle from external form
    */
   private TimerHandleImpl(String externalForm)
   {
      if (externalForm.startsWith("[") == false || externalForm.endsWith("]") == false)
         throw new IllegalArgumentException("Square brackets expected arround: " + externalForm);

      try
      {
         // take first and last char off
         String inStr = externalForm.substring(1, externalForm.length() - 1);

         if (inStr.startsWith("id=") == false)
            throw new IllegalArgumentException("Cannot parse: " + externalForm);

         int targetIndex = inStr.indexOf(",target=");
         int firstIndex = inStr.indexOf(",first=");

         String idStr = inStr.substring(3, targetIndex);
         String targetStr = inStr.substring(targetIndex + 8, firstIndex);
         String restStr = inStr.substring(firstIndex + 1);

         timerId = idStr;
         timedObjectId = TimedObjectId.parse(targetStr);

         StringTokenizer st = new StringTokenizer(restStr, ",=");
         if (st.countTokens() % 2 != 0)
            throw new IllegalArgumentException("Cannot parse: " + externalForm);

         SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

         periode = -1;

         while (st.hasMoreTokens())
         {
            String key = st.nextToken();
            String value = st.nextToken();
            if (key.equals("first"))
               firstTime = sdf.parse(value);
            if (key.equals("periode"))
               periode = new Long(value).longValue();
         }

         if (firstTime == null || periode < 0)
            throw new IllegalArgumentException("Cannot parse: " + externalForm);
      }
      catch (ParseException e)
      {
         throw new IllegalArgumentException("Cannot parse date/time in: " + externalForm);
      }
   }

   /**
    * Parse the handle from external form.
    * "[toid=timedObjectId,first=firstTime,periode=periode]"
    */
   public static TimerHandleImpl parse(String externalForm)
   {
      return new TimerHandleImpl(externalForm);
   }

   /**
    * Returns the external representation of the handle.
    * "[toid=timedObjectId,first=firstTime,periode=periode]"
    */
   public String toExternalForm()
   {
      SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
      String firstEvent = sdf.format(firstTime);
      return "[id=" + timerId + ",target=" + timedObjectId + ",first=" + firstEvent + ",periode=" + periode + "]";
   }

   public String getTimerId()
   {
      return timerId;
   }

   public TimedObjectId getTimedObjectId()
   {
      return timedObjectId;
   }

   public Date getFirstTime()
   {
      return firstTime;
   }

   public long getPeriode()
   {
      return periode;
   }

   public Serializable getInfo()
   {
      return info;
   }
   
   public Date getNextTimeout()
   {
      return this.nextTimeout;
   }

   /**
    * Obtain a reference to the txtimer represented by this handle.
    *
    * @return Timer which this handle represents
    * @throws IllegalStateException  If this method is invoked while the instance is in
    *                                a state that does not allow access to this method.
    * @throws javax.ejb.NoSuchObjectLocalException
    *                                If invoked on a txtimer that has expired or has been cancelled.
    * @throws javax.ejb.EJBException If this method could not complete due to a system-level failure.
    */
   public Timer getTimer() throws IllegalStateException, NoSuchObjectLocalException, EJBException
   {

      EJBTimerService ejbTimerService = EJBTimerServiceLocator.getEjbTimerService();
      ObjectName containerId = timedObjectId.getContainerId();
      Object instancePk = timedObjectId.getInstancePk();
      TimerServiceImpl timerService = (TimerServiceImpl)ejbTimerService.getTimerService(containerId, instancePk);
      if (timerService == null)
         throw new NoSuchObjectLocalException("TimerService not available: " + timedObjectId);

      TimerImpl timer = (TimerImpl)timerService.getTimer(this);
      if (timer == null || timer.isActive() == false)
         throw new NoSuchObjectLocalException("Timer not available: " + timedObjectId);

      return timer;
   }

   /**
    * Return true if objectId, createDate, periode are equal
    */
   public boolean equals(Object obj)
   {
      if (obj == this) return true;
      if (obj instanceof TimerHandleImpl)
      {
         TimerHandleImpl other = (TimerHandleImpl)obj;
         return hashCode() == other.hashCode();
      }
      return false;
   }

   /**
    * Hash code based on objectId, createDate, periode
    */
   public int hashCode()
   {
      if (hashCode == 0)
      {
         hashCode = toExternalForm().hashCode();
      }
      return hashCode;
   }

   /**
    * Returns a string representation of the object.
    */
   public String toString()
   {
      return toExternalForm();
   }
}
