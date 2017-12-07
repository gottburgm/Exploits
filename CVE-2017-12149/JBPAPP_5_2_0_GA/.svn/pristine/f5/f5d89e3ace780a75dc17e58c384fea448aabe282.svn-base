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

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.jms.Queue;
import javax.naming.Context;

import junit.framework.Test;

import org.jboss.logging.Logger;


import org.jboss.test.JBossJMSTestCase;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/**
 * Rollback tests
 *
 * @author <a href="mailto:richard.achmatowicz@jboss.com">Richard Achmatowicz</a>
 * @author
 * @version
 */
public class RollBackUnitTestCase extends JBossJMSTestCase
{

   // Provider specific
   static String TOPIC_FACTORY = "ConnectionFactory";

   static String QUEUE_FACTORY = "ConnectionFactory";

   static String TEST_QUEUE = "queue/testQueue";

   static String TEST_TOPIC = "topic/testTopic";

   static String TEST_DURABLE_TOPIC = "topic/testDurableTopic";

   static byte[] PAYLOAD = new byte[10];

   static Context context;

   static QueueConnection queueConnection;

   static TopicConnection topicConnection;

   static TopicConnection topicDurableConnection;

   /**
    * Constructor the test
    *
    * @param name           Description of Parameter
    * @exception Exception  Description of Exception
    */
   public RollBackUnitTestCase(String name) throws Exception
   {
      super(name);
   }

   /**
    * #Description of the Method
    *
    * @param persistence    Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void runQueueSendRollBack(final int persistence, final boolean explicit) throws Exception
   {
      drainQueue();
      final int iterationCount = getIterationCount();
      final Logger log = getLog();

      Thread sendThread = new Thread()
      {
         public void run()
         {
            try
            {
               QueueSession session = queueConnection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
               Queue queue = (Queue) context.lookup(TEST_QUEUE);

               QueueSender sender = session.createSender(queue);

               BytesMessage message = session.createBytesMessage();
               message.writeBytes(PAYLOAD);
               message.setStringProperty("TEST_NAME", "runQueueSendRollback");
               message.setIntProperty("TEST_PERSISTENCE", persistence);
               message.setBooleanProperty("TEST_EXPLICIT", explicit);
               
               for (int i = 0; i < iterationCount; i++)
               {
                  sender.send(message, persistence, 4, 0);
               }

               if (explicit)
                  session.rollback();
               session.close();
            }
            catch (Exception e)
            {
               log.error("error", e);
            }
         }
      };

      sendThread.start();
      sendThread.join();
      assertTrue("Queue should be empty", drainQueue() == 0);
   }

   /**
    * #Description of the Method
    *
    * @param persistence    Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void runTopicSendRollBack(final int persistence, final boolean explicit) throws Exception
   {
      drainQueue();
      drainTopic();

      final int iterationCount = getIterationCount();
      final Logger log = getLog();

      Thread sendThread = new Thread()
      {
         public void run()
         {
            try
            {

               TopicSession session = topicConnection.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
               Topic topic = (Topic) context.lookup(TEST_TOPIC);

               TopicPublisher publisher = session.createPublisher(topic);

               BytesMessage message = session.createBytesMessage();
               message.writeBytes(PAYLOAD);
               message.setStringProperty("TEST_NAME", "runTopicSendRollback");
               message.setIntProperty("TEST_PERSISTENCE", persistence);
               message.setBooleanProperty("TEST_EXPLICIT", explicit);

               for (int i = 0; i < iterationCount; i++)
               {
                  publisher.publish(message, persistence, 4, 0);
               }

               session.close();
            }
            catch (Exception e)
            {
               log.error("error", e);
            }
         }
      };

      sendThread.start();
      sendThread.join();
      assertTrue("Topic should be empty", drainTopic() == 0);
   }

   /**
    * #Description of the Method
    *
    * @param persistence    Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void runAsynchQueueReceiveRollBack(final int persistence, final boolean explicit) throws Exception
   {
      drainQueue();

      final int iterationCount = getIterationCount();
      final Logger log = getLog();

      Thread sendThread = new Thread()
      {
         public void run()
         {
            try
            {
               QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
               Queue queue = (Queue) context.lookup(TEST_QUEUE);

               QueueSender sender = session.createSender(queue);

               BytesMessage message = session.createBytesMessage();
               message.writeBytes(PAYLOAD);
               message.setStringProperty("TEST_NAME", "runAsynchQueueReceiveRollback");
               message.setIntProperty("TEST_PERSISTENCE", persistence);
               message.setBooleanProperty("TEST_EXPLICIT", explicit);

               for (int i = 0; i < iterationCount; i++)
               {
                  sender.send(message, persistence, 4, 0);
               }

               session.close();
            }
            catch (Exception e)
            {
               log.error("error", e);
            }
         }
      };

      QueueSession session = queueConnection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue) context.lookup(TEST_QUEUE);
      QueueReceiver receiver = session.createReceiver(queue);

      MyMessageListener listener = new MyMessageListener(iterationCount, log);

      sendThread.start();
      receiver.setMessageListener(listener);
      queueConnection.start();
      synchronized (listener)
      {
         if (listener.i < iterationCount)
            listener.wait();
      }
      receiver.setMessageListener(null);

      if (explicit)
         session.rollback();
      session.close();

      queueConnection.stop();

      sendThread.join();

      assertTrue("Queue should be full", drainQueue() == iterationCount);

   }

   /**
    * #Description of the Method
    *
    * @param persistence    Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void runAsynchTopicReceiveRollBack(final int persistence, final boolean explicit) throws Exception
   {
      drainQueue();
      drainTopic();

      final int iterationCount = getIterationCount();
      final Logger log = getLog();

      Thread sendThread = new Thread()
      {
         public void run()
         {
            try
            {

               TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
               Topic topic = (Topic) context.lookup(TEST_TOPIC);

               TopicPublisher publisher = session.createPublisher(topic);

               waitForSynchMessage();

               BytesMessage message = session.createBytesMessage();
               message.writeBytes(PAYLOAD);
               message.setStringProperty("TEST_NAME", "runAsynchTopicReceiveRollback");
               message.setIntProperty("TEST_PERSISTENCE", persistence);
               message.setBooleanProperty("TEST_EXPLICIT", explicit);

               for (int i = 0; i < iterationCount; i++)
               {
                  publisher.publish(message, persistence, 4, 0);
                  log.debug("Published message " + i);
               }

               session.close();
            }
            catch (Exception e)
            {
               log.error("error", e);
            }
         }
      };

      TopicSession session = topicConnection.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
      Topic topic = (Topic) context.lookup(TEST_TOPIC);
      TopicSubscriber subscriber = session.createSubscriber(topic);

      MyMessageListener listener = new MyMessageListener(iterationCount, log);

      queueConnection.start();
      sendThread.start();
      subscriber.setMessageListener(listener);
      topicConnection.start();
      sendSynchMessage();
      getLog().debug("Waiting for all messages");
      synchronized (listener)
      {
         if (listener.i < iterationCount)
            listener.wait();
      }
      getLog().debug("Got all messages");
      subscriber.setMessageListener(null);

      if (explicit)
         session.rollback();
      session.close();

      sendThread.join();
      topicConnection.stop();
      queueConnection.stop();
      assertTrue("Topic should be empty", drainTopic() == 0);
   }

   /**
    * #Description of the Method
    *
    * @param persistence    Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void runAsynchDurableTopicReceiveRollBack(final int persistence, final boolean explicit) throws Exception
   {
      getLog().debug("====> runAsynchDurableTopicReceiveRollBack persistence=" + persistence + " explicit=" + explicit);
      drainQueue();
      drainDurableTopic();

      final int iterationCount = getIterationCount();
      final Logger log = getLog();

      Thread sendThread = new Thread()
      {
         public void run()
         {
            try
            {

               TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
               Topic topic = (Topic) context.lookup(TEST_DURABLE_TOPIC);

               TopicPublisher publisher = session.createPublisher(topic);

               waitForSynchMessage();

               BytesMessage message = session.createBytesMessage();
               message.writeBytes(PAYLOAD);
               message.setStringProperty("TEST_NAME", "runAsynchDurableTopicReceiveRollback");
               message.setIntProperty("TEST_PERSISTENCE", persistence);
               message.setBooleanProperty("TEST_EXPLICIT", explicit);

               for (int i = 0; i < iterationCount; i++)
               {
                  publisher.publish(message, persistence, 4, 0);
                  log.debug("Published message " + i);
               }

               session.close();
            }
            catch (Exception e)
            {
               log.error("error", e);
            }
         }
      };

      TopicSession session = topicDurableConnection.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
      Topic topic = (Topic) context.lookup(TEST_DURABLE_TOPIC);
      TopicSubscriber subscriber = session.createDurableSubscriber(topic, "test");
      try
      {
         MyMessageListener listener = new MyMessageListener(iterationCount, log);

         queueConnection.start();
         sendThread.start();
         subscriber.setMessageListener(listener);
         topicDurableConnection.start();
         sendSynchMessage();
         getLog().debug("Waiting for all messages");
         synchronized (listener)
         {
            if (listener.i < iterationCount)
               listener.wait();
         }
         getLog().debug("Got all messages");
         subscriber.setMessageListener(null);
         subscriber.close();

         if (explicit)
            session.rollback();
         session.close();

         sendThread.join();
         topicDurableConnection.stop();
         queueConnection.stop();
         assertTrue("Topic should be full", drainDurableTopic() == iterationCount);
      }
      finally
      {
         removeDurableSubscription();
      }
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testQueueSendRollBack() throws Exception
   {

      getLog().debug("Starting AsynchQueueSendRollBack test");

      runQueueSendRollBack(DeliveryMode.NON_PERSISTENT, false);
      runQueueSendRollBack(DeliveryMode.PERSISTENT, false);
      runQueueSendRollBack(DeliveryMode.NON_PERSISTENT, true);
      runQueueSendRollBack(DeliveryMode.PERSISTENT, true);

      getLog().debug("AsynchQueueSendRollBack passed");
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testAsynchQueueReceiveBack() throws Exception
   {

      getLog().debug("Starting AsynchQueueReceiveRollBack test");

      runAsynchQueueReceiveRollBack(DeliveryMode.NON_PERSISTENT, false);
      runAsynchQueueReceiveRollBack(DeliveryMode.PERSISTENT, false);
      runQueueSendRollBack(DeliveryMode.NON_PERSISTENT, true);
      runQueueSendRollBack(DeliveryMode.PERSISTENT, true);

      getLog().debug("AsynchQueueReceiveRollBack passed");
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testTopicSendRollBack() throws Exception
   {

      getLog().debug("Starting AsynchTopicSendRollBack test");

      runTopicSendRollBack(DeliveryMode.NON_PERSISTENT, false);
      runTopicSendRollBack(DeliveryMode.PERSISTENT, false);
      runTopicSendRollBack(DeliveryMode.NON_PERSISTENT, true);
      runTopicSendRollBack(DeliveryMode.PERSISTENT, true);

      getLog().debug("AsynchTopicSendRollBack passed");
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testAsynchTopicReceiveRollBack() throws Exception
   {

      getLog().debug("Starting AsynchTopicReceiveRollBack test");

      runAsynchTopicReceiveRollBack(DeliveryMode.NON_PERSISTENT, false);
      runAsynchTopicReceiveRollBack(DeliveryMode.PERSISTENT, false);
      runAsynchTopicReceiveRollBack(DeliveryMode.NON_PERSISTENT, true);
      runAsynchTopicReceiveRollBack(DeliveryMode.PERSISTENT, true);

      getLog().debug("AsynchTopicReceiveRollBack passed");
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testAsynchDurableTopicReceiveRollBack() throws Exception
   {

      getLog().debug("Starting AsynchDurableTopicReceiveRollBack test");

      runAsynchDurableTopicReceiveRollBack(DeliveryMode.NON_PERSISTENT, false);
      runAsynchDurableTopicReceiveRollBack(DeliveryMode.PERSISTENT, false);
      runAsynchDurableTopicReceiveRollBack(DeliveryMode.NON_PERSISTENT, true);
      runAsynchDurableTopicReceiveRollBack(DeliveryMode.PERSISTENT, true);

      getLog().debug("AsynchDurableTopicReceiveRollBack passed");
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void removeDurableSubscription() throws Exception
   {

      TopicSession session = topicDurableConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      session.unsubscribe("test");
   }

   /**
    * The JUnit setup method
    *
    * @exception Exception  Description of Exception
    */
   protected void setUp() throws Exception
   {
      super.setUp();
      
      JMSDestinationsUtil.setupBasicDestinations();
      
      getLog().debug("START TEST " + getName());
      context = getInitialContext();

      QueueConnectionFactory queueFactory = (QueueConnectionFactory) context.lookup(QUEUE_FACTORY);
      queueConnection = queueFactory.createQueueConnection();

      TopicConnectionFactory topicFactory = (TopicConnectionFactory) context.lookup(TOPIC_FACTORY);
      topicConnection = topicFactory.createTopicConnection();
      topicDurableConnection = topicFactory.createTopicConnection("john", "needle");
      if (JMSDestinationsUtil.isHornetQ())
      {
    	  // HornetQ doesn't support clientID associated with the user/password,
    	  // hence we need to set it manually
    	  topicDurableConnection.setClientID("someClient");
      }

      getLog().debug("Connection to JBossMQ established.");
   }
   
   protected void tearDown() throws Exception
   {
      try
      {
         if (topicDurableConnection != null)
         {
            topicDurableConnection.close();
            topicDurableConnection = null;
         }
      }
      catch (JMSException ignored)
      {
      }
      try
      {
         if (topicConnection != null)
         {
            topicConnection.close();
            topicConnection = null;
         }
      }
      catch (JMSException ignored)
      {
      }
      try
      {
         if (queueConnection != null)
         {
            queueConnection.close();
            queueConnection = null;
         }
      }
      catch (JMSException ignored)
      {
      }
      
      JMSDestinationsUtil.destroyDestinations();
      
      
      super.tearDown();
   }

   // Emptys out all the messages in a queue
   private int drainQueue() throws Exception
   {
      getLog().debug("Draining Queue");
      queueConnection.start();

      QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue) context.lookup(TEST_QUEUE);

      QueueReceiver receiver = session.createReceiver(queue);
      Message message = receiver.receive(50);
      int c = 0;
      while (message != null)
      {
         c++;
         message = receiver.receive(50);
      }

      getLog().debug("  Drained " + c + " messages from the queue");

      session.close();

      queueConnection.stop();

      return c;
   }

   // Emptys out all the messages in a topic
   private int drainTopic() throws Exception
   {
      getLog().debug("Draining Topic");
      topicConnection.start();

      final TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      Topic topic = (Topic) context.lookup(TEST_TOPIC);
      TopicSubscriber subscriber = session.createSubscriber(topic);

      Message message = subscriber.receive(50);
      int c = 0;
      while (message != null)
      {
         c++;
         message = subscriber.receive(50);
      }

      getLog().debug("  Drained " + c + " messages from the topic");

      session.close();

      topicConnection.stop();

      return c;
   }

   // Emptys out all the messages in a durable topic
   private int drainDurableTopic() throws Exception
   {
      getLog().debug("Draining Durable Topic");
      topicDurableConnection.start();

      final TopicSession session = topicDurableConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      Topic topic = (Topic) context.lookup(TEST_DURABLE_TOPIC);
      TopicSubscriber subscriber = session.createDurableSubscriber(topic, "test");

      Message message = subscriber.receive(50);
      int c = 0;
      while (message != null)
      {
         c++;
         message = subscriber.receive(50);
      }

      getLog().debug("  Drained " + c + " messages from the durable topic");

      session.close();

      topicDurableConnection.stop();

      return c;
   }

   private void waitForSynchMessage() throws Exception
   {
      getLog().debug("Waiting for Synch Message");
      QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue) context.lookup(TEST_QUEUE);

      QueueReceiver receiver = session.createReceiver(queue);
      receiver.receive();
      session.close();
      getLog().debug("Got Synch Message");
   }

   private void sendSynchMessage() throws Exception
   {
      getLog().debug("Sending Synch Message");
      QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue) context.lookup(TEST_QUEUE);

      QueueSender sender = session.createSender(queue);

      Message message = session.createMessage();
      sender.send(message);

      session.close();
      getLog().debug("Sent Synch Message");
   }

   public class MyMessageListener implements MessageListener
   {
      public int i = 0;

      public int iterationCount;

      public Logger log;

      public MyMessageListener(int iterationCount, Logger log)
      {
         this.iterationCount = iterationCount;
         this.log = log;
      }

      public void onMessage(Message message)
      {
         synchronized (this)
         {
            i++;
            log.debug("Got message " + i);
            if (i >= iterationCount)
               this.notify();
         }
      }
   }

   public int getIterationCount()
   {
      return 5;
   }

   
}
