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
package org.jboss.test.jbossmessaging.test;

import junit.framework.Assert;
import org.jboss.test.jbossmessaging.JMSBase;
import org.jboss.test.util.jms.JMSDestinationsUtil;

import java.util.concurrent.Semaphore;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Test of security features in JMS providers
 *
 * @author <a href="mailto:richard.achmatowicz">Richard Achmatowicz</a>
 * @author     <a href="pra@tim.se">Peter Antman</a>
 * @version $Revision: 113095 $
 */
public class SecurityUnitTestCase extends JMSBase
{
   private static final String PRECONF_CLIENT_IDCONNECTIONFACTORY = "PreconfClientIDConnectionfactory";

   public SecurityUnitTestCase(String name)
   {
      super(name);
   }
   
   protected void setUp() throws Exception
   {
      super.setUp();
      JMSDestinationsUtil.setupBasicDestinations();
   }
   
   protected void tearDown() throws Exception
   {
      JMSDestinationsUtil.destroyDestinations();
      super.tearDown();
   }

   public void testLoginTest() throws Exception
   {
      TopicWorker sub1 = null;
      TopicWorker pub1 = null;
      try
      {
         drainTopic();
         int ic = 5;
         IntRangeMessageFilter f1 = new IntRangeMessageFilter(javax.jms.Message.class, "USER_NR", 0, ic);
         final Semaphore semaphore = new Semaphore(0);
         sub1 = new TopicWorker(SUBSCRIBER, TRANS_NONE, f1, semaphore);
         sub1.setUser("john", "needle");
         sub1.connect();
         sub1.subscribe();
         // Publish
         IntRangeMessageCreator c1 = new IntRangeMessageCreator("USER_NR", 0);
         pub1 = new TopicWorker(PUBLISHER, TRANS_NONE, c1, ic);
         pub1.connect();
         pub1.publish();
         Assert.assertEquals("Publisher did not publish correct number of messages " + pub1.getMessageHandled(), ic,
               pub1.getMessageHandled());
         // let sub1 have some time to handle the messages.
         assertTrue(semaphore.tryAcquire(ic, 5, SECONDS));
         Assert.assertEquals("Subscriber did not get correct number of messages " + sub1.getMessageHandled(), ic, sub1
               .getMessageHandled());
         sub1.close();
         pub1.close();
      }
      catch (Throwable t)
      {
         if (t instanceof junit.framework.AssertionFailedError)
            throw (junit.framework.AssertionFailedError) t;
         log.error("Error in test: " + t, t);
         throw new Exception(t.getMessage());
      }
      finally
      {
         try
         {
            if (sub1 != null)
               sub1.close();
         }
         catch (Exception ex)
         {
         }
         try
         {
            if (pub1 != null)
               pub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   /**
    Tests that check authentication
    1. Login without cred
    2. Login with valid usedid,pwd
    3. Login with valid user, unvalid pwd
    4. Login with unvalid user.
    */
   public void testLoginNoCred() throws Exception
   {
      TopicWorker pub1 = null;
      try
      {
         drainTopic();
         pub1 = new TopicWorker(PUBLISHER, TRANS_NONE, null, 0);
         pub1.connect();
      }
      catch (Exception ex)
      {
         Assert.fail("Could lot login without any cred");
      }
      finally
      {
         try
         {
            pub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testLoginValidCred() throws Exception
   {
      TopicWorker pub1 = null;
      try
      {
         drainTopic();
         pub1 = new TopicWorker(PUBLISHER, TRANS_NONE, null, 0);
         pub1.setUser("john", "needle");
         pub1.connect();
      }
      catch (Exception ex)
      {
         Assert.fail("Could lot login with valid cred");
      }
      finally
      {
         try
         {
            pub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testLoginInvalidPwd() throws Exception
   {
      TopicWorker pub1 = null;
      try
      {
         drainTopic();
         pub1 = new TopicWorker(PUBLISHER, TRANS_NONE, null, 0);
         pub1.setUser("john", "bogus");
         Exception e = null;
         try
         {
            pub1.connect();
         }
         catch (Exception ex)
         {
            e = ex;
         }
         log.debug(e);
         Assert.assertTrue("Loggin in with invalid password did not throw correct exception",
               e instanceof javax.jms.JMSSecurityException);
      }
      finally
      {
         try
         {
            pub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testLoginInvalidCred() throws Exception
   {
      TopicWorker pub1 = null;
      try
      {
         drainTopic();
         pub1 = new TopicWorker(PUBLISHER, TRANS_NONE, null, 0);
         pub1.setUser("bogus", "bogus");
         Exception e = null;
         try
         {
            pub1.connect();
         }
         catch (Exception ex)
         {
            e = ex;
         }
         log.debug(e);
         Assert.assertTrue("Loggin in with invalid user did not throw correct exception",
               e instanceof javax.jms.JMSSecurityException);
      }
      finally
      {
         try
         {
            pub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   /**
    An number of tests to verrify that clientID works as expected:
    
    This tests a specific a specific behavior from JBossMQ.
    This tests creates a TopicSession and expects the clientID to be set. There is no requirement for this on JMS Spec.
    */
   /*public void testClientIDNormalTest() throws Exception
   {
      TopicWorker pub1 = null;
      try
      {
         drainTopic();
         int ic = 5;
         // Publish
         IntRangeMessageCreator c1 = new IntRangeMessageCreator("USER_NR", 0);
         pub1 = new TopicWorker(PUBLISHER, TRANS_NONE, c1, ic);
         pub1.connect();
         pub1.publish();
         Assert.assertTrue("Client did not get a valid clientID", pub1.connection.getClientID().startsWith("ID"));
         pub1.close();
      }
      catch (Throwable t)
      {
         if (t instanceof junit.framework.AssertionFailedError)
            throw (junit.framework.AssertionFailedError) t;
         log.error("Error in test: " + t, t);
         throw new Exception(t.getMessage());
      }
      finally
      {
         try
         {
            pub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   } */

   public void testClientIDPreconfTest() throws Exception
   {
      TopicWorker pub1 = null;
      try
      {
         drainTopic();
         int ic = 5;
         // Publish
         IntRangeMessageCreator c1 = new IntRangeMessageCreator("USER_NR", 0);
  	     if (JMSDestinationsUtil.isHornetQ())
  	     {
  	        pub1 = new TopicWorker(PUBLISHER, TRANS_NONE, c1, ic, PRECONF_CLIENT_IDCONNECTIONFACTORY);
  	     }
  	     else
	     {
            pub1 = new TopicWorker(PUBLISHER, TRANS_NONE, c1, ic);
         }
         pub1.setUser("john", "needle");
         pub1.connect();
         pub1.publish();
         Assert.assertEquals("Client did not get a valid clientID", "DurableSubscriberExample", pub1.connection
               .getClientID());
         pub1.close();
      }
      catch (Throwable t)
      {
         if (t instanceof junit.framework.AssertionFailedError)
            throw (junit.framework.AssertionFailedError) t;
         log.error("Error in test: " + t, t);
         throw new Exception(t.getMessage());
      }
      finally
      {
         try
         {
            pub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testClientIDSetTest() throws Exception
   {
      TopicWorker pub1 = null;
      try
      {
         drainTopic();
         int ic = 5;
         // Publish
         IntRangeMessageCreator c1 = new IntRangeMessageCreator("USER_NR", 0);
         pub1 = new TopicWorker(PUBLISHER, TRANS_NONE, c1, ic);
         pub1.setClientID("myId");
         pub1.connect();
         pub1.publish();
         Assert.assertEquals("Client did not get a valid clientID", "myId", pub1.connection.getClientID());
         pub1.close();
      }
      finally
      {
         try
         {
            pub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   /*
    * Test only valid on JBossMQ - commented out.
    * public void testClientIDSetSteelPreconf() throws Exception
   {
      TopicWorker pub1 = null;
      try
      {
         drainTopic();
         int ic = 5;
         // Publish
         IntRangeMessageCreator c1 = new IntRangeMessageCreator("USER_NR", 0);
         pub1 = new TopicWorker(PUBLISHER, TRANS_NONE, c1, ic);
         pub1.setClientID("DurableSubscriberExample");
         Exception e = null;
         try
         {
            pub1.connect();
         }
         catch (Exception ex)
         {
            e = ex;
         }
         log.debug(e);
         Assert.assertTrue("Setting a clientID wich is preconfigured did not throw correct exception",
               e instanceof javax.jms.InvalidClientIDException);
         pub1.close();
      }
      finally
      {
         try
         {
            pub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   } */

   public void testClientIDSetAfterInvoke() throws Exception
   {
      TopicWorker pub1 = null;
      try
      {
         drainTopic();
         int ic = 5;
         // Publish
         IntRangeMessageCreator c1 = new IntRangeMessageCreator("USER_NR", 0);
         pub1 = new TopicWorker(PUBLISHER, TRANS_NONE, c1, ic);
         pub1.connect();
         pub1.publish();
         Exception e = null;
         try
         {
            pub1.connection.setClientID("myID");
         }
         catch (Exception ex)
         {
            e = ex;
         }
         log.debug(e);
         Assert.assertTrue("Setting a clientID after connection is used did not throw correct exception: " + e,
               e instanceof javax.jms.IllegalStateException);
         pub1.close();
      }
      finally
      {
         try
         {
            pub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   /**
    Tests to check autorization.
    
    Remember there are actuallt two types of fails:
    a) You are a user, but do no belong to a group that has acl.
    b) You belong to a group, but that group does not have acl.
    we test the first for topics and the second for queues, by
    configuration in jbossmq-testsuite-service.xml
    
    Tests that check autorization.
    1. test valid topic publisher
    2. test invalid topic publisher
    3. test valid topic subscriber
    4. test invalid topic subscriber
    5. test valid queue sender
    6. test invalid queue sender
    7. test valid queue receiver
    8. test invalid queue receiver
    9. test valid queue browser.
    10. test invalid queue browser
    11. test preconf dur sub, to valid dest.
    12. test preconf dur sub, to invalid dest.
    13. test dyn dur sub, to valid dest.
    14. test  dyn dur sub, to valid dest.
    */
   public void testAuzValidTopicPublisher() throws Exception
   {
      TopicWorker pub1 = null;
      try
      {
         IntRangeMessageCreator c1 = new IntRangeMessageCreator("USER_NR", 0);
         pub1 = new TopicWorker(PUBLISHER, TRANS_NONE, c1, 1);
         pub1.setUser("john", "needle");
         pub1.connect();
         pub1.publish();
      }
      catch (Exception ex)
      {
         Assert.fail("Could not publish to valid destination");
      }
      finally
      {
         try
         {
            pub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testAuzValidTopicPublisherTransaction() throws Exception
   {
      TopicWorker pub1 = null;
      try
      {
         IntRangeMessageCreator c1 = new IntRangeMessageCreator("USER_NR", 0);
         pub1 = new TopicWorker(PUBLISHER, TRANS_INDIVIDUAL, c1, 1);
         pub1.setUser("john", "needle");
         pub1.connect();
         pub1.publish();
      }
      catch (Exception ex)
      {
         Assert.fail("Could not publish to valid destination");
      }
      finally
      {
         try
         {
            pub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testAuzInvalidTopicPublisher() throws Exception
   {
      TopicWorker pub1 = null;
      try
      {
         IntRangeMessageCreator c1 = new IntRangeMessageCreator("USER_NR", 0);
         pub1 = new TopicWorker(PUBLISHER, TRANS_NONE, c1, 1);
         pub1.setUser("nobody", "nobody");
         pub1.connect();
         Exception e = null;
         try
         {
            pub1.publish();
         }
         catch (Exception ex)
         {
            e = ex;
            e.printStackTrace(System.out);
         }
         log.debug(e);
         Assert.assertTrue("Unauz topic publishing throw wrong exception: " + e,
               e instanceof javax.jms.JMSSecurityException);
      }
      finally
      {
         try
         {
            pub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testAuzInvalidTopicPublisherTransaction() throws Exception
   {
      TopicWorker pub1 = null;
      try
      {
         IntRangeMessageCreator c1 = new IntRangeMessageCreator("USER_NR", 0);
         pub1 = new TopicWorker(PUBLISHER, TRANS_INDIVIDUAL, c1, 1);
         pub1.setUser("nobody", "nobody");
         pub1.connect();
         Exception e = null;
         try
         {
            pub1.publish();
         }
         catch (Exception ex)
         {
            e = ex;
         }
         log.debug(e);
         Assert.assertTrue("Unauz topic publishing throw wrong exception: " + e,
               e instanceof javax.jms.JMSSecurityException);
      }
      finally
      {
         try
         {
            pub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testAuzValidTopicSubscriber() throws Exception
   {
      TopicWorker sub1 = null;
      try
      {
         drainTopic();
         IntRangeMessageFilter f1 = new IntRangeMessageFilter(javax.jms.Message.class, "USER_NR", 0, 1);
         sub1 = new TopicWorker(SUBSCRIBER, TRANS_NONE, f1, null);
         sub1.setUser("john", "needle");
         sub1.setStoped();
         sub1.run();
         Exception ex = sub1.getException();
         Assert.assertTrue("Autz topic subscriber did not work", ex == null);
      }
      finally
      {
         try
         {
            sub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testAuzValidTopicSubscriberTransaction() throws Exception
   {
      TopicWorker sub1 = null;
      try
      {
         drainTopic();
         IntRangeMessageFilter f1 = new IntRangeMessageFilter(javax.jms.Message.class, "USER_NR", 0, 1);
         sub1 = new TopicWorker(SUBSCRIBER, TRANS_INDIVIDUAL, f1, null);
         sub1.setUser("john", "needle");
         sub1.setStoped();
         sub1.run();
         Exception ex = sub1.getException();
         Assert.assertTrue("Autz topic subscriber did not work", ex == null);
      }
      finally
      {
         try
         {
            sub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testAuzInvalidTopicSubscriber() throws Exception
   {
      TopicWorker sub1 = null;
      try
      {
         drainTopic();
         IntRangeMessageFilter f1 = new IntRangeMessageFilter(javax.jms.Message.class, "USER_NR", 0, 1);
         sub1 = new TopicWorker(SUBSCRIBER, TRANS_NONE, f1, null);
         sub1.setUser("nobody", "nobody");
         sub1.setStoped();
         sub1.run();
         Exception ex = sub1.getException();
         Assert.assertTrue("Unautz topic subscriber throw wrong exception: " + ex,
               ex instanceof javax.jms.JMSSecurityException);
      }
      finally
      {
         try
         {
            sub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testAuzInvalidTopicSubscriberTransaction() throws Exception
   {
      TopicWorker sub1 = null;
      try
      {
         drainTopic();
         IntRangeMessageFilter f1 = new IntRangeMessageFilter(javax.jms.Message.class, "USER_NR", 0, 1);
         sub1 = new TopicWorker(SUBSCRIBER, TRANS_INDIVIDUAL, f1, null);
         sub1.setUser("nobody", "nobody");
         sub1.setStoped();
         sub1.run();
         Exception ex = sub1.getException();
         assertNotNull("No exception received", ex);
         Assert.assertTrue("Unautz topic subscriber throw wrong exception: " + ex,
               ex instanceof javax.jms.JMSSecurityException);
      }
      finally
      {
         try
         {
            sub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testAuzValidQueueSender() throws Exception
   {
      QueueWorker pub1 = null;
      try
      {
         IntRangeMessageCreator c1 = new IntRangeMessageCreator("USER_NR", 0);
         pub1 = new QueueWorker(PUBLISHER, TRANS_NONE, c1, 1);
         pub1.setUser("john", "needle");
         pub1.connect();
         pub1.publish();
      }
      catch (Exception ex)
      {
         Assert.fail("Could not publish to valid destination");
      }
      finally
      {
         try
         {
            pub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testAuzValidQueueSenderTransaction() throws Exception
   {
      QueueWorker pub1 = null;
      try
      {
         IntRangeMessageCreator c1 = new IntRangeMessageCreator("USER_NR", 0);
         pub1 = new QueueWorker(PUBLISHER, TRANS_INDIVIDUAL, c1, 1);
         pub1.setUser("john", "needle");
         pub1.connect();
         pub1.publish();
      }
      catch (Exception ex)
      {
         Assert.fail("Could not publish to valid destination");
      }
      finally
      {
         try
         {
            pub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testAuzInvalidQueueSender() throws Exception
   {
      QueueWorker pub1 = null;
      try
      {
         IntRangeMessageCreator c1 = new IntRangeMessageCreator("USER_NR", 0);
         pub1 = new QueueWorker(PUBLISHER, TRANS_NONE, c1, 1);
         pub1.setUser("nobody", "nobody");
         pub1.connect();
         Exception e = null;
         try
         {
            pub1.publish();
         }
         catch (Exception ex)
         {
            e = ex;
         }
         log.debug(e);
         Assert.assertTrue("Unauz queue publishing throw wrong exception: " + e,
               e instanceof javax.jms.JMSSecurityException);
      }
      finally
      {
         try
         {
            pub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testAuzInvalidQueueSenderTransaction() throws Exception
   {
      QueueWorker pub1 = null;
      try
      {
         IntRangeMessageCreator c1 = new IntRangeMessageCreator("USER_NR", 0);
         pub1 = new QueueWorker(PUBLISHER, TRANS_INDIVIDUAL, c1, 1);
         pub1.setUser("nobody", "nobody");
         pub1.connect();
         Exception e = null;
         try
         {
            pub1.publish();
         }
         catch (Exception ex)
         {
            e = ex;
         }
         log.debug(e);
         Assert.assertTrue("Unauz queue publishing throw wrong exception: " + e,
               e instanceof javax.jms.JMSSecurityException);
      }
      finally
      {
         try
         {
            pub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testAuzValidQueueReceiver() throws Exception
   {
      QueueWorker sub1 = null;
      try
      {
         IntRangeMessageFilter f1 = new IntRangeMessageFilter(javax.jms.Message.class, "USER_NR", 0, 1);
         sub1 = new QueueWorker(GETTER, TRANS_NONE, f1, null);
         sub1.setUser("john", "needle");
         sub1.connect();
         Exception ex = null;
         try
         {
            sub1.get();
         }
         catch (Exception e)
         {
            ex = e;
            log.error("ValidQueueReceiver got an exception: " + e, e);
         }
         Assert.assertTrue("Autz queue receiver did not work", ex == null);
      }
      finally
      {
         try
         {
            sub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testAuzValidQueueReceiverTransaction() throws Exception
   {
      QueueWorker sub1 = null;
      try
      {
         IntRangeMessageFilter f1 = new IntRangeMessageFilter(javax.jms.Message.class, "USER_NR", 0, 1);
         sub1 = new QueueWorker(GETTER, TRANS_INDIVIDUAL, f1, null);
         sub1.setUser("john", "needle");
         sub1.connect();
         Exception ex = null;
         try
         {
            sub1.get();
         }
         catch (Exception e)
         {
            ex = e;
            log.error("ValidQueueReceiver got an exception: " + e, e);
         }
         Assert.assertTrue("Autz queue receiver did not work", ex == null);
      }
      finally
      {
         try
         {
            sub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testAuzInvalidQueueReceiver() throws Exception
   {
      QueueWorker sub1 = null;
      try
      {
         IntRangeMessageFilter f1 = new IntRangeMessageFilter(javax.jms.Message.class, "USER_NR", 0, 1);
         sub1 = new QueueWorker(GETTER, TRANS_NONE, f1, null);
         sub1.setUser("nobody", "nobody");
         sub1.connect();
         Exception ex = null;
         try
         {
            sub1.get();
         }
         catch (Exception e)
         {
            ex = e;
         }
         Assert.assertTrue("Unautz queue receiver throw wrong exception: " + ex,
               ex instanceof javax.jms.JMSSecurityException);
      }
      finally
      {
         try
         {
            sub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testAuzInvalidQueueReceiverTransaction() throws Exception
   {
      QueueWorker sub1 = null;
      try
      {
         IntRangeMessageFilter f1 = new IntRangeMessageFilter(javax.jms.Message.class, "USER_NR", 0, 1);
         sub1 = new QueueWorker(GETTER, TRANS_INDIVIDUAL, f1, null);
         sub1.setUser("nobody", "nobody");
         sub1.connect();
         Exception ex = null;
         try
         {
            sub1.get();
         }
         catch (Exception e)
         {
            ex = e;
         }
         Assert.assertTrue("Unautz queue receiver throw wrong exception: " + ex,
               ex instanceof javax.jms.JMSSecurityException);
      }
      finally
      {
         try
         {
            sub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testAuzValidQueueBrowser() throws Exception
   {
      QueueWorker sub1 = null;
      try
      {
         IntRangeMessageFilter f1 = new IntRangeMessageFilter(javax.jms.Message.class, "USER_NR", 0, 1);
         sub1 = new QueueWorker(GETTER, TRANS_NONE, f1, null);
         sub1.setUser("john", "needle");
         sub1.connect();
         Exception ex = null;
         try
         {
            sub1.browse();
         }
         catch (Exception e)
         {
            ex = e;
            log.error("ValidQueueBrowser throw exception: " + e, e);
         }
         Assert.assertTrue("Autz queue receiver did not work", ex == null);
      }
      finally
      {
         try
         {
            sub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testAuzInvalidQueueBrowser() throws Exception
   {
      QueueWorker sub1 = null;
      try
      {
         IntRangeMessageFilter f1 = new IntRangeMessageFilter(javax.jms.Message.class, "USER_NR", 0, 1);
         sub1 = new QueueWorker(GETTER, TRANS_NONE, f1, null);
         sub1.setUser("nobody", "nobody");
         sub1.connect();
         Exception ex = null;
         try
         {
            sub1.browse();
         }
         catch (Exception e)
         {
            ex = e;
         }
         Assert.assertTrue("Unautz queue receiver throw wrong exception: " + ex,
               ex instanceof javax.jms.JMSSecurityException);
      }
      finally
      {
         try
         {
            sub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testValidPreconfDurSub() throws Exception
   {
      TopicWorker sub1 = null;
      TopicWorker pub1 = null;
      try
      {
         // Clean testarea up
         drainTopic();
         int ic = 5;
         // Set up a durable subscriber
         IntRangeMessageFilter f1 = new IntRangeMessageFilter(javax.jms.Message.class, "DURABLE_NR", 0, ic);
         final Semaphore semaphore = new Semaphore(0);
         if (JMSDestinationsUtil.isHornetQ())
         {
        	   sub1 = new TopicWorker(SUBSCRIBER, TRANS_NONE, f1, PRECONF_CLIENT_IDCONNECTIONFACTORY, semaphore);
         }
         else
         {
        	   sub1 = new TopicWorker(SUBSCRIBER, TRANS_NONE, f1, semaphore);
         }
         sub1.setDurable("john", "needle", "sub2");
         sub1.connect();
         sub1.subscribe();
         // Publish
         IntRangeMessageCreator c1 = new IntRangeMessageCreator("DURABLE_NR", 0);
         pub1 = new TopicWorker(PUBLISHER, TRANS_NONE, c1, ic);
         pub1.connect();
         pub1.publish();
         Assert.assertEquals("Publisher did not publish correct number of messages " + pub1.getMessageHandled(), ic,
               pub1.getMessageHandled());
         // let sub1 have some time to handle the messages.
         assertTrue(semaphore.tryAcquire(ic, 5, SECONDS));
         Exception ex = sub1.getException();
         if (ex != null)
            log.error("ValidPreconfDurSub got an exception: " + ex, ex);
         Assert.assertTrue("ValidPreconfDurSub did not work", ex == null);
         Assert.assertEquals("Subscriber did not get correct number of messages " + sub1.getMessageHandled(), ic, sub1
               .getMessageHandled());
         sub1.setStoped();
      }
      finally
      {
         try
         {
            pub1.close();
         }
         catch (Exception ex)
         {
         }
         try
         {
            // if this stops working it might be that we have become spec
            // compliant an do not allow unsubscribe with an open consumer.
            sub1.unsubscribe();
            sub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testInvalidPreconfDurSub() throws Exception
   {
      TopicWorker sub1 = null;
      try
      {
         // Clean testarea up
         TEST_TOPIC = "topic/securedTopic";
         //drainTopic();
         int ic = 5;
         // Set up a durable subscriber
         IntRangeMessageFilter f1 = new IntRangeMessageFilter(javax.jms.Message.class, "DURABLE_NR", 0, ic);
         if (JMSDestinationsUtil.isHornetQ())
         {
            sub1 = new TopicWorker(SUBSCRIBER, TRANS_NONE, f1, PRECONF_CLIENT_IDCONNECTIONFACTORY, null);
         }
         else
         {
            sub1 = new TopicWorker(SUBSCRIBER, TRANS_NONE, f1, null);
         }
         
         sub1.setDurable("john", "needle", "sub3");
         sub1.setStoped();
         sub1.run();
         Exception ex = sub1.getException();
         Assert.assertTrue("InvalidPreconfDurSub did not get correct exception:" + ex,
                 ex instanceof javax.jms.JMSSecurityException);
      }
      finally
      {
         try
         {
            sub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testValidDynDurSub() throws Exception
   {
      TopicWorker sub1 = null;
      TopicWorker pub1 = null;
      try
      {
         // Clean testarea up
         drainTopic();
         int ic = 5;
         // Set up a durable subscriber
         IntRangeMessageFilter f1 = new IntRangeMessageFilter(javax.jms.Message.class, "DURABLE_NR", 0, ic);
         final Semaphore semaphore = new Semaphore(0);
         sub1 = new TopicWorker(SUBSCRIBER, TRANS_NONE, f1, semaphore);
         sub1.setDurable("dynsub", "dynsub", "sub4");
         sub1.setClientID("myId");
         sub1.connect();
         sub1.subscribe();
         // Publish
         IntRangeMessageCreator c1 = new IntRangeMessageCreator("DURABLE_NR", 0);
         pub1 = new TopicWorker(PUBLISHER, TRANS_NONE, c1, ic);
         pub1.connect();
         pub1.publish();
         Assert.assertEquals("Publisher did not publish correct number of messages " + pub1.getMessageHandled(), ic,
               pub1.getMessageHandled());
         // let sub1 have some time to handle the messages.
         assertTrue(semaphore.tryAcquire(ic, 5, SECONDS));
         Exception ex = sub1.getException();
         if (ex != null)
         {
             throw ex;
         }
         Assert.assertTrue("ValidDynDurSub did not work", ex == null);
         Assert.assertEquals("Subscriber did not get correct number of messages " + sub1.getMessageHandled(), ic, sub1
               .getMessageHandled());
      }
      finally
      {
         try
         {
            pub1.close();
         }
         catch (Exception ex)
         {
         }
         try
         {
            // if this stops working it might be that we have become spec
            // compliant an do not allow unsubscribe with an open consumer.
            sub1.unsubscribe();
            sub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }

   public void testInvalidDynDurSub() throws Exception
   {
      TopicWorker sub1 = null;
      try
      {
         // Clean testarea up
         TEST_TOPIC = "topic/securedTopic";
         //drainTopic();
         int ic = 5;
         // Set up a durable subscriber
         IntRangeMessageFilter f1 = new IntRangeMessageFilter(javax.jms.Message.class, "DURABLE_NR", 0, ic);
         sub1 = new TopicWorker(SUBSCRIBER, TRANS_NONE, f1, null);
         sub1.setDurable("dynsub", "dynsub", "sub5");
         sub1.setClientID("myId2");
         sub1.setStoped();
         sub1.run();
         Exception ex = sub1.getException();
         Assert.assertTrue("InvalidDynDurSub did not get correct exception:" + ex,
                 ex instanceof javax.jms.JMSSecurityException);
      }
      finally
      {
         try
         {
            sub1.close();
         }
         catch (Exception ex)
         {
         }
      }
   }
}
