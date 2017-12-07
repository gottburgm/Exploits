/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.timer.test;

import javax.ejb.EJBHome;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.timer.interfaces.TimerEntityExt;
import org.jboss.test.timer.interfaces.TimerEntityExtHome;

/**
 * RemovalAfterPassivationTimerUnitTestCase.
 * 
 * @author Galder Zamarreño
 */
public class TimerCleanUpUnitTestCase extends JBossTestCase
{
   private static final String EJB_TIMER_XAR = "ejb-timer.ear";

   private static final int SHORT_PERIOD = 1 * 1000; // 1s

   public TimerCleanUpUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(TimerCleanUpUnitTestCase.class, EJB_TIMER_XAR);
   }
   
   public void testEntityBeanTimerHasTimerServiceAfterCancellationStd() throws Exception
   {
      String jndi = "ejb/test/timer/TimerEntityExtStd";
      TimerEntityExtHome home = (TimerEntityExtHome) getEJBHome(jndi);
      TimerEntityExt entity = home.create(new Integer(333));
      try{
          entity.startTimer(SHORT_PERIOD);
          Thread.sleep(12 * SHORT_PERIOD);
          assertTrue("Timer service should be associated with bean." , entity.hasTimerService(jndi));
          entity.stopTimer();
          int lCount = entity.getTimeoutCount();
          assertTrue("Timeout was expected to be called at least 10 times but was "
             + "only called: " + lCount + " times",
             lCount >= 10);
          Thread.sleep(5 * SHORT_PERIOD);
          int lCount2 = entity.getTimeoutCount();
          assertTrue("After the timer was stopped no timeout should happen but "
             + "it was called " + (lCount2 - lCount) + " more times",
             lCount == lCount2);
          Thread.sleep(40 * SHORT_PERIOD);
          assertFalse("Timer service should have been passivated and timer service association removed." 
                , entity.hasTimerService(jndi));
       }finally{
           entity.remove();
       }
   }
   
   public void testEntityBeanSingleTimerHasTimerServiceAfterExpirationStd() throws Exception
   {
      String jndi = "ejb/test/timer/TimerEntityExtStd";
      TimerEntityExtHome home = (TimerEntityExtHome) getEJBHome(jndi);
      TimerEntityExt entity = home.create(new Integer(444));
      try{
          entity.startSingleTimer(SHORT_PERIOD);
          Thread.sleep(5 * SHORT_PERIOD);
          int lCount = entity.getTimeoutCount();
          assertTrue("Timeout was expected to be called only once but was called: "
             + lCount + " times",
             lCount == 1);
          Thread.sleep(40 * SHORT_PERIOD);
          assertFalse("Timer service should have been passivated and timer service association removed."
                , entity.hasTimerService(jndi));
       }finally{
           entity.remove();
       }
   }

   public void testEntityBeanTimerHasTimerServiceAfterCancellationInstPerTx() throws Exception
   {
      String jndi = "ejb/test/timer/TimerEntityExtInstPerTx";
      TimerEntityExtHome home = (TimerEntityExtHome) getEJBHome(jndi);
      TimerEntityExt entity = home.create(new Integer(555));
      try{
          entity.startTimer(SHORT_PERIOD);
          Thread.sleep(12 * SHORT_PERIOD);
          assertTrue("Timer service should be associated with bean." , entity.hasTimerService(jndi));
          entity.stopTimer();
          int lCount = entity.getTimeoutCount();
          assertTrue("Timeout was expected to be called at least 10 times but was "
             + "only called: " + lCount + " times",
             lCount >= 10);
          Thread.sleep(5 * SHORT_PERIOD);
          int lCount2 = entity.getTimeoutCount();
          assertTrue("After the timer was stopped no timeout should happen but "
             + "it was called " + (lCount2 - lCount) + " more times",
             lCount == lCount2);
          assertFalse("Timer service should have been passivated and timer service association removed." 
                , entity.hasTimerService(jndi));
      }finally{
          entity.remove();
      }
   }
   
   public void testEntityBeanSingleTimerHasTimerServiceAfterExpirationInstPerTx() throws Exception
   {
      String jndi = "ejb/test/timer/TimerEntityExtInstPerTx";
      TimerEntityExtHome home = (TimerEntityExtHome) getEJBHome(jndi);
      TimerEntityExt entity = home.create(new Integer(666));
      try{
          entity.startSingleTimer(SHORT_PERIOD);
          Thread.sleep(5 * SHORT_PERIOD);
          assertFalse("Timer service should be associated with bean." , entity.hasTimerService(jndi));
          int lCount = entity.getTimeoutCount();
          assertTrue("Timeout was expected to be called only once but was called: "
             + lCount + " times",
             lCount == 1);
          assertFalse("Timer service should have been passivated and timer service association removed." 
                , entity.hasTimerService(jndi));
      }finally{
          entity.remove();
      }
   }   
   
   private EJBHome getEJBHome(String pJNDIName) throws NamingException
   {
      InitialContext lContext = new InitialContext();
      return (EJBHome) lContext.lookup(pJNDIName);
   }
}
