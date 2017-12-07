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
package org.jboss.test.txtimer.test;

import javax.ejb.TimedObject;
import javax.ejb.TimerService;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.jboss.ejb.AllowedOperationsAssociation;
import org.jboss.ejb.txtimer.EJBTimerService;
import org.jboss.ejb.txtimer.EJBTimerServiceLocator;
import org.jboss.ejb.txtimer.TimedObjectId;

/**
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public abstract class TimerTestBase extends TestCase
{
   protected EJBTimerService ejbTimerService;

   protected TimerTestBase(String name)
   {
      super(name);
   }

   /**
    * Sets up the fixture, for example, open a network connection.
    * This method is called before a test is executed.
    */
   protected void setUp() throws Exception
   {
      super.setUp();
      ejbTimerService = EJBTimerServiceLocator.getEjbTimerService();

      // when the timer runs inside JBoss, this is taken care of by the container
      // is standalone mode, we fake the context for timer operations, so the timer thinks we are inside a EJB
      // business method, and does not refuse the operations
      AllowedOperationsAssociation.pushInMethodFlag(AllowedOperationsAssociation.IN_BUSINESS_METHOD);
   }

   /**
    * Tears down the fixture, for example, close a network connection.
    * This method is called after a test is executed.
    */
   protected void tearDown() throws Exception
   {
      super.tearDown();
      AllowedOperationsAssociation.popInMethodFlag();
   }

   protected TimerService createTimerService(TimedObject timedObject)
   {
      SimpleTimedObjectInvoker invoker = new SimpleTimedObjectInvoker();
      TimedObjectId timedObjectId = invoker.addTimedObject(timedObject);
      ObjectName containerId = timedObjectId.getContainerId();
      Object instancePk = timedObjectId.getInstancePk();
      return ejbTimerService.createTimerService(containerId, instancePk, invoker);
   }

   protected void sleep(long interval) throws InterruptedException
   {
      synchronized (this)
      {
         wait(interval);
      }
   }
}
