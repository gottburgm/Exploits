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
package org.jboss.test.timer.interfaces;

import java.util.HashMap;

/**
 * Remote interface for test/timer/TimerSLSB.
 * @author Thomas Diesler
 * @author Scott.Stark@jboss.org
 * @version $Revision: 107174 $
 */
public interface TimerSLSB
   extends javax.ejb.EJBObject
{
   /**
    * Used in the unit tests. This is a key in a Map that can be used as the timer's
    * info (timer.getInfo()). If this key maps to an Integer value in the Map,it will cause the
    * ejbTimeout method to rollback the transaction that many times.
    */
   String INFO_EXEC_FAIL_COUNT = "ExecFailCount";


   /**
    * Used in the unit tests. This is a key in a Map that can be used as the timer's
    * info (timer.getInfo()). If this key maps to an Integer value in the Map,it will cause the
    * ejbTimeout method to sleep for number of milliseconds specified in the Integer.
    * This is used to simulate a long running task, and helps to test JBAS-1926.
    */
   String INFO_TASK_RUNTIME = "TaskRuntime";

   /**
    * Start a single timer (if not already set) with the start date plus the period
    * This method uses an implementation defined default value for the timer info.
    * @param pPeriod Time that will elapse between now and the timed event in milliseconds
    */
   public void startSingleTimer( String timerName, long pPeriod )
      throws java.rmi.RemoteException;

   /**
   * Start a single timer (if not already set) with the start date plus the period and specified info.
   *
   * @param pPeriod Time that will elapse between now and the timed event in milliseconds
   * @param info an object to be used as the info for the timer.
   **/
   public void startSingleTimer(String timerName, long pPeriod, HashMap info)
      throws java.rmi.RemoteException;

   /**
    * Start a timer (if not already set) with the start date plus the period and an interval of the given period.
    * This method uses an implementation defined default value for the timer info.
    * @param pPeriod Time that will elapse between two events in milliseconds
    */
   public void startTimer(String timerName, long pPeriod )
      throws java.rmi.RemoteException;

   /**
    * Start a timer (if not already set) with the start date plus the period and an interval of the given
    * period and the specified timer info
    * @param pPeriod Time that will elapse between two events in milliseconds
    * @param info an object to be used as the info for the timer.
    */
   public void startTimer(String timerName, long pPeriod, HashMap info )
      throws java.rmi.RemoteException;

   public void stopTimer(String timerName)
      throws java.rmi.RemoteException;

   public int getTimeoutCount(String timerName)
      throws java.rmi.RemoteException;

   public java.util.Date getNextTimeout(String timerName)
      throws java.rmi.RemoteException;

   public long getTimeRemaining(String timerName)
      throws java.rmi.RemoteException;

   public Object getInfo(String timerName)
      throws java.rmi.RemoteException;

   /**
    * Returns the value from the RetryPolicyMBean. This is used by unit tests to help determine timing
    * for some of the tests, specifically, those that test the fix for JBAS-1926.
    */
   public long getRetryTimeoutPeriod()
      throws java.rmi.RemoteException;

}
