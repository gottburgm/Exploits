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
package org.jboss.test.timer.test;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import javax.ejb.EJBHome;
import javax.ejb.NoSuchObjectLocalException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginContext;

import junit.framework.Test;

import org.jboss.security.auth.callback.AppCallbackHandler;
import org.jboss.test.JBossTestCase;
import org.jboss.test.timer.interfaces.TimerEntity;
import org.jboss.test.timer.interfaces.TimerEntityHome;
import org.jboss.test.timer.interfaces.TimerSFSB;
import org.jboss.test.timer.interfaces.TimerSFSBHome;
import org.jboss.test.timer.interfaces.TimerSLSB;
import org.jboss.test.timer.interfaces.TimerSLSBHome; 

/**
 * Simple unit tests for the EJB Timer service that uses secured ejbs.
 */
public class SecureTimerUnitTestCase extends JBossTestCase
{

   private static final String EJB_TIMER_XAR = "ejb-timer.ear";

   private static final int SHORT_PERIOD = 1 * 1000; // 1s
   private LoginContext lc;

   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(SecureTimerUnitTestCase.class, EJB_TIMER_XAR);
   }

   /**
    * Constructor for the BasicTimerUnitTest object
    */
   public SecureTimerUnitTestCase(String pName)
   {
      super(pName);
   }

   public void tearDown()
   {
   }

   /**
    * Test that a Stafeful Session Bean cannot access a Timer Service
    *
    * @throws Exception Unexpected Exception indicating an error
    */
   public void testSecuredStatefulSessionBeanTimer()
      throws Exception
   {
      login();
      TimerSFSBHome home = (TimerSFSBHome) getEJBHome(TimerSFSBHome.SECURED_JNDI_NAME);
      TimerSFSB lBean = home.create();
      try
      {
         lBean.checkTimerService();
         fail("Stateful Session Bean is not allowed to get a Timer Service");
      }
      catch (RemoteException re)
      {
         Throwable lCause = re.detail;
         if (lCause instanceof ServerException)
         {
            lCause = ((ServerException) lCause).detail;
            if (lCause instanceof IllegalStateException)
            {
               // This exception is expected -> ignore
            }
            else
            {
               throw re;
            }
         }
      }
      logout();
   }

   /**
    * Test that a repetitive Stafeless Session Bean Timer
    *
    * @throws Exception Unexpected Exception indicating an error
    */
   public void testSecuredStatelessSessionBeanTimer()
      throws Exception
   {
      login();
      TimerSLSBHome home = (TimerSLSBHome) getEJBHome(TimerSLSBHome.SECURED_JNDI_NAME);
      TimerSLSB bean = home.create();
      String timerName = "testSecuredStatelessSessionBeanTimer";
      bean.startTimer(timerName, SHORT_PERIOD);
      // Sleep for 20x the timer interval and expect at least 10 events
      Thread.sleep(20 * SHORT_PERIOD);
      int count = bean.getTimeoutCount(timerName);
      bean.stopTimer(timerName);
      assertTrue("Timeout was expected to be called at least 10 times but was "
         + "only called: " + count + " times",
         count >= 10);
      Thread.sleep(5 * SHORT_PERIOD);
      int count2 = bean.getTimeoutCount(timerName);
      assertTrue("After the timer was stopped no timeout should happen but "
         + "it was called " + count2 + " more times",
         count2 == 0);
      bean.remove();
      logout();
   }

   /**
    * Test that a single Stafeless Session Bean Timer
    *
    * @throws Exception Unexpected Exception indicating an error
    */
   public void testSecuredStatelessSessionBeanSingleTimer()
      throws Exception
   {
      login();
      TimerSLSBHome home = (TimerSLSBHome) getEJBHome(TimerSLSBHome.SECURED_JNDI_NAME);
      TimerSLSB bean = home.create();
      String timerName = "testSecuredStatelessSessionBeanSingleTimer";
      bean.startSingleTimer(timerName, SHORT_PERIOD);
      Thread.sleep(5 * SHORT_PERIOD);
      int count = bean.getTimeoutCount(timerName);
      assertTrue("Timeout was expected to be called only once but was called: "
         + count + " times",
         count == 1);
      try
      {
         bean.stopTimer(timerName);
         fail("A single timer should expire after the first event and therefore this "
            + "has to throw an NoSuchObjectLocalException");
      }
      catch (RemoteException re)
      {
         Throwable lCause = re.detail;
         if (lCause instanceof ServerException)
         {
            lCause = ((ServerException) lCause).detail;
            if (lCause instanceof NoSuchObjectLocalException)
            {
               // This exception is expected -> ignore
            }
            else
            {
               throw re;
            }
         }
      }
      bean.remove();
      logout();
   }

   /**
    * Test that a repetitive Entity Bean Timer
    *
    * @throws Exception Unexpected Exception indicating an error
    */
   public void testSecuredEntityBeanTimer()
      throws Exception
   {
      login();
      TimerEntityHome home = (TimerEntityHome) getEJBHome(TimerEntityHome.SECURED_JNDI_NAME);
      TimerEntity entity = home.create(new Integer(111));
      entity.startTimer(SHORT_PERIOD);
      Thread.sleep(12 * SHORT_PERIOD);
      entity.stopTimer();
      int count = entity.getTimeoutCount();
      assertTrue("Timeout was expected to be called at least 10 times but was "
         + "only called: " + count + " times",
         count >= 10);
      Thread.sleep(5 * SHORT_PERIOD);
      int count2 = entity.getTimeoutCount();
      assertTrue("After the timer was stopped no timeout should happen but "
         + "it was called " + (count2 - count) + " more times",
         count == count2);
      entity.remove();
      logout();
   }

   /**
    * Test that a single Entity Bean Timer
    *
    * @throws Exception Unexpected Exception indicating an error
    */
   public void testSecuredEntityBeanSingleTimer()
      throws Exception
   {
      login();
      TimerEntityHome home = (TimerEntityHome) getEJBHome(TimerEntityHome.SECURED_JNDI_NAME);
      TimerEntity entity = home.create(new Integer(222));
      entity.startSingleTimer(SHORT_PERIOD);
      Thread.sleep(5 * SHORT_PERIOD);
      int count = entity.getTimeoutCount();
      assertTrue("Timeout was expected to be called only once but was called: "
         + count + " times",
         count == 1);
      try
      {
         entity.stopTimer();
         fail("A single timer should expire after the first event and therefore this "
            + "has to throw an NoSuchObjectLocalException");
      }
      catch (RemoteException re)
      {
         Throwable lCause = re.detail;
         if (lCause instanceof ServerException)
         {
            lCause = ((ServerException) lCause).detail;
            if (lCause instanceof NoSuchObjectLocalException)
            {
               // This exception is expected -> ignore
            }
            else
            {
               throw re;
            }
         }
      }
      entity.remove();
      logout();
   }

   private EJBHome getEJBHome(String pJNDIName)
      throws NamingException
   {
      InitialContext lContext = new InitialContext();
      return (EJBHome) lContext.lookup(pJNDIName);
   }

   private void login() throws Exception
   {
      lc = null;
      String username = "jduke";
      char[] password = "theduke".toCharArray();
      AppCallbackHandler handler = new AppCallbackHandler(username, password);
      log.debug("Creating LoginContext(ejb-timers)");
      lc = new LoginContext("ejb-timers", handler);
      lc.login();
      log.debug("Created LoginContext, subject="+lc.getSubject());
   }
   private void logout() throws Exception
   {
      if( lc != null )
      {
         lc.logout();
         lc = null;
      }
   }
}
