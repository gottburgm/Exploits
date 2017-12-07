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

/**
 * @author Thomas Diesler
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public interface TimerEntity
   extends javax.ejb.EJBObject
{
   /**
    * Start a single timer (if not already set) with the start date plus the period
    * @param pPeriod Time that will elapse between now and the timed event in milliseconds
    */
   public void startSingleTimer( long pPeriod )
      throws java.rmi.RemoteException;

   /**
    * Start a timer (if not already set) with the start date plus the period and an interval of the given period
    * @param pPeriod Time that will elapse between two events in milliseconds
    */
   public void startTimer( long pPeriod )
      throws java.rmi.RemoteException;

   public void stopTimer(  )
      throws java.rmi.RemoteException;

   public int getTimeoutCount(  )
      throws java.rmi.RemoteException;

   public java.util.Date getNextTimeout(  )
      throws java.rmi.RemoteException;

   public long getTimeRemaining(  )
      throws java.rmi.RemoteException;

   public java.lang.Object getInfo(  )
      throws java.rmi.RemoteException;

   public javax.ejb.TimerHandle getTimerHandle(  )
      throws java.rmi.RemoteException;

}
