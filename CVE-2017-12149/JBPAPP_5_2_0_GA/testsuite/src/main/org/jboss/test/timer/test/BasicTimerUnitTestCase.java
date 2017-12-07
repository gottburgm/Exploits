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
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import javax.ejb.EJBHome;
import javax.ejb.NoSuchObjectLocalException;
import javax.jms.Message;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginContext;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.util.AppCallbackHandler;
import org.jboss.test.util.jms.JMSDestinationsUtil;
import org.jboss.test.timer.interfaces.TimerEntity;
import org.jboss.test.timer.interfaces.TimerEntityHome;
import org.jboss.test.timer.interfaces.TimerSFSB;
import org.jboss.test.timer.interfaces.TimerSFSBHome;
import org.jboss.test.timer.interfaces.TimerSLSBHome;
import org.jboss.test.timer.interfaces.TimerSLSB;

/**
 * Simple unit tests for the EJB Timer service
 */
public class BasicTimerUnitTestCase extends JBossTestCase
{

   private static final String EJB_TIMER_XAR = "ejb-timer.ear";

   private static final int SHORT_PERIOD = 1 * 1000; // 1s
   private static final int LONG_PERIOD = 20 * 1000; // 20s
   private LoginContext lc;

   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      TestSetup wrapper = new JBossTestSetup(new TestSuite(BasicTimerUnitTestCase.class))
      {
         @Override
         protected void setUp() throws Exception
         {
            super.setUp();
            JMSDestinationsUtil.setupBasicDestinations();
            JMSDestinationsUtil.deployQueue("QueueA");
            JMSDestinationsUtil.deployQueue("QueueB");
            JMSDestinationsUtil.deployQueue("QueueC");
            JMSDestinationsUtil.deployQueue("QueueD");
            redeploy(EJB_TIMER_XAR);
            flushAuthCache();
         }

         @Override
         protected void tearDown() throws Exception
         {
            undeploy(EJB_TIMER_XAR);
            JMSDestinationsUtil.destroyDestinations();
            super.tearDown();

         }
      };
      return wrapper;

   }

   /**
    * Constructor for the BasicTimerUnitTest object
    */
   public BasicTimerUnitTestCase(String pName)
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
   public void testStatefulSessionBeanTimer()
      throws Exception
   {
      TimerSFSBHome lHome = (TimerSFSBHome) getEJBHome(TimerSFSBHome.JNDI_NAME);
      TimerSFSB lBean = lHome.create();
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
   }

   /**
    * Test that a repetitive Stafeless Session Bean Timer
    *
    * @throws Exception Unexpected Exception indicating an error
    */
   public void testStatelessSessionBeanTimer()
      throws Exception
   {
      TimerSLSBHome home = (TimerSLSBHome) getEJBHome(TimerSLSBHome.JNDI_NAME);
      TimerSLSB bean = home.create();
      String timerName = "testStatelessSessionBeanTimer";
      bean.startTimer(timerName, SHORT_PERIOD);
      Thread.sleep(12 * SHORT_PERIOD + SHORT_PERIOD);
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
   }

   /**
    * Test that a repetitive Stateless Session Bean Timer with a retry.
    * NOTE: This test was added to test JIRA issue JBAS-1926.
    * Since the same timer mechanism is used (with respects to what this test actualy tests)
    * with entity and message beans, testing on those would probably be redundant, so a test
    * is only run on stateless session bean.
    *
    *
    * @throws Exception Unexpected Exception indicating an error
    */
   public void testStatelessSessionBeanTimerRetry()
      throws Exception
   {
      log.info("testStatelessSessionBeanTimerRetry(): start");
      TimerSLSBHome home = (TimerSLSBHome) getEJBHome(TimerSLSBHome.JNDI_NAME);
      TimerSLSB bean = home.create();

      // We need to make sure that the next timer interval occurs
      // while the retry timeout is STILL running in order to test JBAS-1926
      final long retryMs = bean.getRetryTimeoutPeriod();
      String timerName = "testStatelessSessionBeanTimerRetry";
      log.info("testStatelessSessionBeanTimerRetry():GOT RETRY TIME:" + retryMs);
      assertFalse("Failed to get valid retry timeout!", retryMs == -1);
      final HashMap info = new HashMap();
      info.put(TimerSLSB.INFO_EXEC_FAIL_COUNT,new Integer(1)); // fail only once
      // RE: JIRA Issue JBAS-1926
      // This is the amount of time the task will take to execute
      // This is intentionlly more than the time of the interval
      // so that the we can be sure that the interval will fire again
      // WHILE the retry is still in progress.
      final int taskTime = SHORT_PERIOD * 2;
      info.put(TimerSLSB.INFO_TASK_RUNTIME,new Integer(taskTime)); // the time is takes to execute the task

      bean.startTimer(timerName, SHORT_PERIOD,info);
      // Wait for 1 SHORT_PERIOD for the first firing
      // Another retryMs for the amount of time it takes for the retry to happen
      // and finally the amount of time that it takes to execute the task and 200ms to be safe.
      Thread.sleep(SHORT_PERIOD  + retryMs + taskTime + 200);
      int count = bean.getTimeoutCount(timerName);
      bean.stopTimer(timerName);
      assertEquals("Timeout was called too many times. Should have been once for the initial" +
            ", and once for the retry during the time allotted.",2,count);

      bean.remove();
   }
   

   /**
    * Test that a single Stafeless Session Bean Timer
    *
    * @throws Exception Unexpected Exception indicating an error
    */
   public void testStatelessSessionBeanSingleTimer()
      throws Exception
   {
      TimerSLSBHome home = (TimerSLSBHome) getEJBHome(TimerSLSBHome.JNDI_NAME);
      TimerSLSB bean = home.create();
      final String timerName = "testStatelessSessionBeanSingleTimer";
      bean.startSingleTimer(timerName, SHORT_PERIOD);
      Thread.sleep(5 * SHORT_PERIOD);
      int lCount = bean.getTimeoutCount(timerName);
      assertTrue("Timeout was expected to be called only once but was called: "
         + lCount + " times",
         lCount == 1);
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

      // Test for the case where a transaction fails, the time should be retried once
      // This test assumes the FixedRetryPolicy is left at the default of 200ms.
      // The "fail-once" data in the timer will be used by the bean to fail the
      // transaction once, to make sure that it is automatically retried.
      log.info("testStatelessSessionBeanSingleTimer(): Testing retry on timer.");
      final HashMap info = new HashMap(1);
      info.put(TimerSLSB.INFO_EXEC_FAIL_COUNT,new Integer(1));
      bean.startSingleTimer(timerName, SHORT_PERIOD,info);
      Thread.sleep(5 * SHORT_PERIOD);
      assertEquals("Timeout was expected to be called twice, once inititially, one once for the retry.",
               2,bean.getTimeoutCount(timerName));


   }

   /**
    * Test that a repetitive Entity Bean Timer
    *
    * @throws Exception Unexpected Exception indicating an error
    */
   public void testEntityBeanTimer()
      throws Exception
   {
      TimerEntityHome home = (TimerEntityHome) getEJBHome(TimerEntityHome.JNDI_NAME);
      TimerEntity entity = home.create(new Integer(111));
      entity.startTimer(SHORT_PERIOD);
      Thread.sleep(12 * SHORT_PERIOD);
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
      entity.remove();
   }

   /**
    * Test that a single Entity Bean Timer
    *
    * @throws Exception Unexpected Exception indicating an error
    */
   public void testEntityBeanSingleTimer()
      throws Exception
   {
      TimerEntityHome home = (TimerEntityHome) getEJBHome(TimerEntityHome.JNDI_NAME);
      TimerEntity entity = home.create(new Integer(222));
      entity.startSingleTimer(SHORT_PERIOD);
      Thread.sleep(5 * SHORT_PERIOD);
      int lCount = entity.getTimeoutCount();
      assertTrue("Timeout was expected to be called only once but was called: "
         + lCount + " times",
         lCount == 1);
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
   }

   /** Test an mdb that creates a timer for each onMessage
    * @throws Exception
    */ 
   public void testMDBTimer() throws Exception
   {
      log.info("+++ testMDBTimer");
      InitialContext ctx = new InitialContext();
      QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");

      QueueConnection queConn = null;
      QueueSession session = null;
      QueueSender sender = null;
      QueueReceiver receiver = null;

      try
      {
         queConn = factory.createQueueConnection();
         queConn.start();

         Queue queueA = (Queue) ctx.lookup("queue/QueueA");
         Queue queueB = (Queue) ctx.lookup("queue/QueueB");
         
         session = queConn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
         sender = session.createSender(queueA);
         receiver = session.createReceiver(queueB);

         while (receiver.receive(1000) != null)
         {
            // Empty the queue
         }

         TextMessage message = session.createTextMessage();
         message.setText("testMDBTimer");
         message.setIntProperty("UNIQUE_ID", 123456789);
         message.setJMSReplyTo(queueB);
         sender.send(message);

         // Get the initial onMessage ack
         Message reply = receiver.receive(30000);
         log.info("onMessage reply: " + reply);
         assertTrue("onMessage reply != null", reply != null);

         if (log.isDebugEnabled())
         {
            log.debug("Properties"); 
            Enumeration e = reply.getPropertyNames();
            while (e.hasMoreElements())
            {
               log.debug(e.nextElement()); 
            }
         }

         int id = reply.getIntProperty("UNIQUE_ID");
         log.debug("id=" + id); 
         assertTrue("onMessage reply.id = 123456789", id == 123456789);

         // Get the initial timer reply
         reply = receiver.receive(30000);
         log.info("ejbTimeout reply: " + reply);
         assertTrue("ejbTimeout reply != null", reply != null);

         if (log.isDebugEnabled())
         {
            log.debug("Properties"); 
            Enumeration e = reply.getPropertyNames();
            while (e.hasMoreElements())
            {
               log.debug(e.nextElement()); 
            }
         }

         id = reply.getIntProperty("UNIQUE_ID");
         log.debug("id=" + id); 
         assertTrue("onMessage reply.id = 123456789", id == 123456789);
      }
      finally
      {
         if (receiver != null)
         {
            try
            {
               receiver.close();
            } catch (JMSException ignore)
            {
               //
            }
         }
         if (sender != null)
         {
            try
            {
               sender.close();
            } catch (JMSException ignore)
            {
               //
            }
         }
         if (session != null)
         {
            try
            {
               session.close();
            } catch (JMSException ignore)
            {
               //
            }
         }
         if (queConn != null)
         {
            try
            {
               queConn.close();
            } catch (JMSException ignore)
            {
               //
            }
         }
      }
   }

   /** Test an mdb that creates a timer in its ejbCreate method
    * @throws Exception
    */ 
   public void testOnCreateMDBTimer() throws Exception
   {
      log.info("+++ testOnCreateMDBTimer");
      InitialContext ctx = new InitialContext();
      QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");

      QueueConnection queConn = null;
      QueueSession session = null;
      QueueSender sender = null;
      QueueReceiver receiver = null;

      try
      {
         queConn = factory.createQueueConnection();
         queConn.start();

         Queue queueA = (Queue) ctx.lookup("queue/QueueC");
         Queue queueB = (Queue) ctx.lookup("queue/QueueD");

         session = queConn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
         sender = session.createSender(queueA);
         receiver = session.createReceiver(queueB);

         while (receiver.receive(1000) != null)
         {
            // Empty the queue
         }

         TextMessage message = session.createTextMessage();
         message.setText("testOnCreateMDBTimer");
         message.setIntProperty("UNIQUE_ID", 123456788);
         message.setJMSReplyTo(queueB);
         sender.send(message);

         // Get the initial onMessage ack
         Message reply = receiver.receive(15000);
         log.info("onMessage reply: " + reply);
         assertTrue("onMessage reply != null", reply != null);
         int id = reply.getIntProperty("UNIQUE_ID");
         assertTrue("onMessage reply.id = 123456788", id == 123456788);

         // Get the 10 ejbCreate timer replys
         for(int n = 0; n < 10; n ++)
         {
            reply = receiver.receive(15000);
            log.info("ejbTimeout reply: " + reply);
            assertTrue("ejbTimeout reply != null", reply != null);
            id = reply.getIntProperty("UNIQUE_ID");
            assertTrue("onMessage reply.id = 123456788", id == 123456788);
            long elapsed = reply.getLongProperty("Elapsed");
            log.info("Elapsed: "+elapsed);
         }
      }
      finally
      {
         if (receiver != null)
         {
            try
            {
               receiver.close();
            } catch (JMSException ignore)
            {
               //
            }
         }
         if (sender != null)
         {
            try
            {
               sender.close();
            } catch (JMSException ignore)
            {
               //
            }
         }
         if (session != null)
         {
            try
            {
               session.close();
            } catch (JMSException ignore)
            {
               //
            }
         }
         if (queConn != null)
         {
            try
            {
               queConn.close();
            } catch (JMSException ignore)
            {
               //
            }
         }
      }
   }

   /**
    * Test with a repetitive timer the timer interface
    *
    * @throws Exception Unexpected Exception indicating an error
    */
   public void testTimerImplementation()
      throws Exception
   {
      TimerSLSBHome home = (TimerSLSBHome) getEJBHome(TimerSLSBHome.JNDI_NAME);
      TimerSLSB bean = home.create();
      String timerName = "testTimerImplementation";
      bean.startTimer(timerName, LONG_PERIOD);
      Date lNextEvent = bean.getNextTimeout(timerName);
      long lUntilNextEvent = lNextEvent.getTime() - new Date().getTime();
      Thread.sleep(SHORT_PERIOD);
      long lTimeRemaining = bean.getTimeRemaining(timerName);
      assertTrue("Date of the next event must be greater than 0", lUntilNextEvent > 0);
      assertTrue("Period until next event must be greater than 0", lTimeRemaining > 0);
      assertTrue("Period until next event must be smaller than time until next even because it "
         + "it is called later", lUntilNextEvent > lTimeRemaining);
      bean.stopTimer(timerName);
   }

   /**
    * Test that a session that does not implement TimedObject cannot obtain
    * the TimerService from its EJBContext
    *
    * @throws Exception Unexpected Exception indicating an error
    */
   public void testBadStatelessSessionBeanTimer()
      throws Exception
   {
      TimerSLSBHome home = (TimerSLSBHome) getEJBHome("ejb/test/timer/NoTimedObjectBean");
      TimerSLSB bean = home.create();
      try
      {
         bean.startTimer("testBadStatelessSessionBeanTimer", SHORT_PERIOD);
         fail("Was able to call NoTimedObjectBean.startTimer");
      }
      catch(RemoteException e)
      {
         log.info("Saw exception as expected", e);
      }
      bean.remove();
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
