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

import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.ServerSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestSuite;

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.jbossmessaging.JMSTestCase;
import org.jboss.test.util.jms.JMSDestinationsUtil;

import EDU.oswego.cs.dl.util.concurrent.CountDown;

/**
 * Basic tests using the jms 1.1 producer/consumer apis.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 105789 $
 */
public class Jms11UnitTest extends JMSTestCase
{
   /** The default TopicFactory jndi name */
   static String TOPIC_FACTORY = "/ConnectionFactory";
   /** The default QueueFactory jndi name */
   static String QUEUE_FACTORY = "/ConnectionFactory";

   static String TEST_QUEUE = "queue/testQueue";
   static String TEST_TOPIC = "topic/testTopic";
   static String TEST_DURABLE_TOPIC = "topic/testDurableTopic";

   //JMSProviderAdapter providerAdapter;
   static Context context;
   static Connection queueConnection;
   static Connection topicConnection;

   public Jms11UnitTest(String name) throws Exception
   {
      super(name);
   }

   protected void tearDown() throws Exception
   {
	   disconnect();
	   JMSDestinationsUtil.destroyDestinations();
	   context.close();
	   context = null;
      super.tearDown();
   }
   
   public void setUp() throws Exception
   {
      super.setUp();
      JMSDestinationsUtil.setupBasicDestinations();
      context = new InitialContext();
   }
   
   protected void connect() throws Exception
   {
      ConnectionFactory queueFactory = (ConnectionFactory) context.lookup(QUEUE_FACTORY);
      queueConnection = queueFactory.createConnection();

      ConnectionFactory topicFactory = (ConnectionFactory) context.lookup(TOPIC_FACTORY);
      topicConnection = topicFactory.createConnection();

   }

   protected void disconnect() throws Exception
   {
	  if (queueConnection!=null)
	  {
	      queueConnection.close();
	      queueConnection = null;
	  }
      
	  if (topicConnection != null)
	  {
	      topicConnection.close();
	      topicConnection = null;
	  }

   }

   /**
    * Test that messages are ordered by message arrival and priority.
    * This also tests :
    * 		Using a non-transacted AUTO_ACKNOWLEDGE session
    *		Using a MessageConsumer
    *		Using a QueueSender
    *			Sending PERSITENT and NON_PERSISTENT text messages.
    *		Using a QueueBrowser
    */
   public void testQueueMessageOrder() throws Exception
   {

      getLog().debug("Starting QueueMessageOrder test");

      connect();

      queueConnection.start();

      Session session = queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue) context.lookup(TEST_QUEUE);
      MessageProducer sender = session.createProducer(queue);

      TextMessage message = session.createTextMessage();
      message.setText("Normal message");
      sender.send(message, DeliveryMode.NON_PERSISTENT, 4, 0);
      message.setText("Persistent message");
      sender.send(message, DeliveryMode.PERSISTENT, 4, 0);
      message.setText("High Priority Persistent message");
      sender.send(message, DeliveryMode.PERSISTENT, 9, 0);

      QueueBrowser browser = session.createBrowser(queue);
      Enumeration i = browser.getEnumeration();
      getLog().debug(message.getText());

      message = (TextMessage) i.nextElement();
      getLog().debug(message.getText());

      message = (TextMessage) i.nextElement();
      getLog().debug(message.getText());

      disconnect();
      getLog().debug("QueueMessageOrder passed");
   }

   /**
    * Test that temporary queues can be deleted.
    */
   public void testTemporaryQueueDelete() throws Exception
   {

      getLog().debug("Starting TemporaryQueueDelete test");
      connect();

      Session session = queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      TemporaryQueue queue = session.createTemporaryQueue();

      queue.delete();

      disconnect();

      getLog().debug("TemporaryQueueDelete passed");
   }

   /**
    * Test that temporary topics can be deleted.
    */
   public void testTemporaryTopicDelete() throws Exception
   {

      getLog().debug("Starting TemporaryTopicDelete test");
      connect();

      Session session = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      TemporaryTopic topic = session.createTemporaryTopic();

      topic.delete();

      disconnect();

      getLog().debug("TemporaryTopicDelete passed");
   }

   /**
    * Test invalid destination trying to browse a message.
    */
   public void testInvalidDestinationQueueBrowse() throws Exception
   {

      getLog().debug("Starting InvalidDestinationQueueBrowse test");
      connect();

      Session session = queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      TemporaryQueue queue = session.createTemporaryQueue();
      QueueBrowser browser = session.createBrowser(queue);
      queue.delete();

      boolean caught = false;
      try
      {
         browser.getEnumeration();
      }
      catch (InvalidDestinationException expected)
      {
         caught = true;
      }

      disconnect();

      assertTrue("Expected an InvalidDestinationException", caught);

      getLog().debug("InvalidDestinationQueueBrowse passed");
   }

   /**
    * Test errors trying on topic subscribe.
    */
   public void testErrorsTopicSubscribe() throws Exception
   {

      getLog().debug("Starting InvalidDestinationTopicSubscribe test");
      connect();

      try
      {
         Session session = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         Topic topic = (Topic) context.lookup(TEST_TOPIC);
         TemporaryTopic temp = session.createTemporaryTopic();

         boolean caught = false;
         try
         {
            session.createConsumer(null);
         }
         catch (InvalidDestinationException expected)
         {
            caught = true;
         }
         assertTrue("Expected an InvalidDestinationException for a null topic", caught);

         caught = false;
         try
         {
            session.createConsumer(null, null, true);
         }
         catch (InvalidDestinationException expected)
         {
            caught = true;
         }
         assertTrue("Expected an InvalidDestinationException for a null topic", caught);

         caught = false;
         try
         {
            session.createDurableSubscriber(null, "NotUsed");
         }
         catch (InvalidDestinationException expected)
         {
            caught = true;
         }
         assertTrue("Expected an InvalidDestinationException for a null topic", caught);

         caught = false;
         try
         {
            session.createDurableSubscriber(temp, "NotUsed");
         }
         catch (JMSException expected)
         {
            caught = true;
         }
         assertTrue("Expected an InvalidDestinationException for a temporary topic", caught);

         caught = false;
         try
         {
            session.createDurableSubscriber(null, "NotUsed", null, true);
         }
         catch (JMSException expected)
         {
            caught = true;
         }
         assertTrue("Expected an InvalidDestinationException for a null topic", caught);

         caught = false;
         try
         {
            session.createDurableSubscriber(temp, "NotUsed", null, true);
         }
         catch (JMSException expected)
         {
            caught = true;
         }
         assertTrue("Expected an InvalidDestinationException for a temporary topic", caught);

//         caught = false;
//         try
//         {
//            session.createDurableSubscriber(topic, null);
//         }
//         catch (Exception expected)
//         {
//            caught = true;
//         }
//         assertTrue("Expected a Exception for a null subscription", caught);
//
//         caught = false;
//         try
//         {
//            session.createDurableSubscriber(topic, null, null, false);
//         }
//         catch (Exception expected)
//         {
//            caught = true;
//         }
//         assertTrue("Expected a Exception for a null subscription", caught);

         caught = false;
         try
         {
            session.createDurableSubscriber(topic, "  ");
         }
         catch (Exception expected)
         {
            caught = true;
         }
         assertTrue("Expected a Exception for an empty subscription", caught);

         caught = false;
         try
         {
            session.createDurableSubscriber(topic, "  ", null, false);
         }
         catch (Exception expected)
         {
            caught = true;
         }
         assertTrue("Expected a Exception for an empty subscription", caught);
      }
      finally
      {
         disconnect();
      }

      getLog().debug("InvalidDestinationTopicSubscriber passed");
   }

   /**
    * Test create queue.
    */
   public void testCreateQueue() throws Exception
   {

      getLog().debug("Starting create queue test");
      connect();

      Session session = queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      
      Queue jndiQueue = (Queue) context.lookup("/queue/testQueue");
      Queue createQueue = session.createQueue(jndiQueue.getQueueName());
      assertTrue("Failed for " + QUEUE_FACTORY, jndiQueue.equals(createQueue));

      getLog().debug("InvalidDestinationTopicSubscriber passed");
   }

   public void testMessageListener() throws Exception
   {
      getLog().debug("Starting create queue test");

      connect();
      queueConnection.start();
      final CountDown counter1 = new CountDown(3);

      Session session = queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue) context.lookup(TEST_QUEUE);

      MessageConsumer receiver = session.createConsumer(queue);
      receiver.setMessageListener(new MessageListener()
      {
         public void onMessage(Message msg)
         {
            Logger log = Logger.getLogger(getClass().getName());
            log.debug("ML");
            try
            {
               if (msg instanceof TextMessage)
               {
                  log.debug(((TextMessage) msg).getText());
                  counter1.release();
               }
            }
            catch (Exception e)
            {
            }
         }
      });

      MessageProducer sender = session.createProducer(queue);

      TextMessage message = session.createTextMessage();
      message.setText("Normal message");
      sender.send(message, DeliveryMode.NON_PERSISTENT, 4, 0);
      //sender.send(queue, message, DeliveryMode.NON_PERSISTENT, 4, 0);
      message.setText("Persistent message");
      sender.send(message, DeliveryMode.PERSISTENT, 4, 0);
      //sender.send(queue, message, DeliveryMode.PERSISTENT, 4, 0);
      message.setText("High Priority Persistent message");
      sender.send(message, DeliveryMode.PERSISTENT, 9, 0);
      //sender.send(queue, message, DeliveryMode.PERSISTENT, 9, 0);

      // Wait for the msgs to be received
      counter1.acquire();
      log.debug("MessageListener1 received the TMs sent");

      final CountDown counter2 = new CountDown(2);
      receiver.setMessageListener(new MessageListener()
      {
         public void onMessage(Message msg)
         {
            Logger log = Logger.getLogger(getClass().getName());
            log.debug("ML 2");
            try
            {
               if (msg instanceof TextMessage)
               {
                  log.debug(((TextMessage) msg).getText());
                  counter2.release();
               }
            }
            catch (Exception e)
            {
            }
         }
      });

      message.setText("Persistent message");
      sender.send(message, DeliveryMode.PERSISTENT, 4, 0);
      //sender.send(queue, message, DeliveryMode.PERSISTENT, 4, 0);
      message.setText("High Priority Persistent message");
      sender.send(message, DeliveryMode.PERSISTENT, 9, 0);
      //sender.send(queue, message, DeliveryMode.PERSISTENT, 9, 0);

      // Wait for the msgs to be received
      counter2.acquire();
      log.debug("MessageListener2 received the TMs sent");

      receiver.setMessageListener(null);

      message.setText("Persistent message");
      sender.send(message, DeliveryMode.PERSISTENT, 4, 0);
      //sender.send(queue, message, DeliveryMode.PERSISTENT, 4, 0);
      message.setText("High Priority Persistent message");
      sender.send(message, DeliveryMode.PERSISTENT, 9, 0);
      //sender.send(queue, message, DeliveryMode.PERSISTENT, 9, 0);

      sender.close();
      disconnect();
      getLog().debug("MessageListener test passed");
   }

   public void testApplicationServerStuff() throws Exception
   {
      getLog().debug("Starting testing app server stuff");
      connect();

      Queue testQueue = (Queue) context.lookup(TEST_QUEUE);
      final Session session = queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

      session.setMessageListener(new MessageListener()
      {
         public void onMessage(Message mess)
         {
            Logger log = Logger.getLogger(getClass().getName());
            log.debug("Processing message");
            try
            {
               if (mess instanceof TextMessage)
                  log.debug(((TextMessage) mess).getText());
            }
            catch (Exception e)
            {
               log.error("Error", e);
            }
         }
      });

      MessageProducer sender = session.createProducer(testQueue);
      sender.send(session.createTextMessage("Hi"));
      sender.send(session.createTextMessage("There"));
      sender.send(session.createTextMessage("Guys"));
      queueConnection.createConnectionConsumer(testQueue, null, new ServerSessionPool()
      {
         public ServerSession getServerSession()
         {
            Logger.getLogger(getClass().getName()).debug("Getting server session.");
            return new ServerSession()
            {
               public Session getSession()
               {
                  return session;
               }
               public void start()
               {
                  Logger.getLogger(getClass().getName()).debug("Starting server session.");
                  session.run();
               }
            };
         }
      }, 9);

      queueConnection.start();

      try
      {
         Thread.sleep(5 * 1000);
      }
      catch (Exception e)
      {
      }

      disconnect();
      getLog().debug("Testing app server stuff passed");
   }

   private void drainMessagesForTopic(MessageConsumer sub) throws JMSException
   {
      Message msg = sub.receive(50);
      int c = 0;
      while (msg != null)
      {
         c++;
         if (msg instanceof TextMessage)
            getLog().debug(((TextMessage) msg).getText());
         msg = sub.receive(50);
      }
      getLog().debug("Received " + c + " messages from topic.");
   }

   public void testTopics() throws Exception
   {
      getLog().debug("Starting Topic test");
      connect();

      TopicConnectionFactory topicFactory = (TopicConnectionFactory) context.lookup(TOPIC_FACTORY);
      topicConnection = topicFactory.createTopicConnection("john", "needle");
      
      if (JMSDestinationsUtil.isHornetQ())
      {
    	  // HornetQ doesn't support pre-defined users with client ids 
    	  topicConnection.setClientID("john_id");
      }

      topicConnection.start();

      //set up some subscribers to the topic
      Session session = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Topic topic = (Topic) context.lookup(TEST_TOPIC);

      TopicSubscriber sub1 = session.createDurableSubscriber(topic, "sub1");
      MessageConsumer sub2 = session.createConsumer(topic);
      MessageConsumer sub3 = session.createConsumer(topic);

      //Now a sender
      MessageProducer sender = session.createProducer(topic);

      //send some messages
      sender.send(session.createTextMessage("Message 1"));
      sender.send(session.createTextMessage("Message 2"));
      sender.send(session.createTextMessage("Message 3"));
      drainMessagesForTopic(sub1);
      drainMessagesForTopic(sub2);
      drainMessagesForTopic(sub3);

      //close some subscribers
      sub1.close();
      sub2.close();

      //send some more messages
      sender.send(session.createTextMessage("Message 4"));
      sender.send(session.createTextMessage("Message 5"));
      sender.send(session.createTextMessage("Message 6"));

      //give time for message 4 to be negatively acked (as it will be cause last receive timed out)
      try
      {
         Thread.sleep(5 * 1000);
      }
      catch (InterruptedException e)
      {
      }

      drainMessagesForTopic(sub3);

      //open subscribers again.
      sub1 = session.createDurableSubscriber(topic, "sub1");
      sub2 = session.createConsumer(topic);

      //Send a final message
      sender.send(session.createTextMessage("Final message"));
      sender.close();

      drainMessagesForTopic(sub1);
      drainMessagesForTopic(sub2);
      drainMessagesForTopic(sub3);

      sub1.close();
      sub2.close();
      sub3.close();

      session.unsubscribe("sub1");

      topicConnection.stop();
      topicConnection.close();

      disconnect();
      getLog().debug("Topic test passed");
   }

   /**
    * Test to seeif the NoLocal feature of topics works.
    * Messages sended from the same connection should not
    * be received by Subscribers on the same connection.
    */
   public void testTopicNoLocal() throws Exception
   {
      getLog().debug("Starting TopicNoLocal test");
      connect();

      TopicConnectionFactory topicFactory = (TopicConnectionFactory) context.lookup(TOPIC_FACTORY);
      TopicConnection topicConnection1 = topicFactory.createTopicConnection();
      topicConnection1.start();
      TopicConnection topicConnection2 = topicFactory.createTopicConnection();
      topicConnection2.start();

      // We don't want local messages on this topic.
      Session session1 = topicConnection1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Topic topic = (Topic) context.lookup(TEST_TOPIC);
      MessageConsumer subscriber1 = session1.createConsumer(topic, null, true);
      MessageProducer sender1 = session1.createProducer(topic);

      //Now a sender
      Session session2 = topicConnection2.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer sender2 = session2.createProducer(topic);

      drainMessagesForTopic(subscriber1);

      //send some messages
      sender1.send(session1.createTextMessage("Local Message"));
      sender2.send(session2.createTextMessage("Remote Message"));

      // Get the messages, we should get the remote message
      // but not the local message
      TextMessage msg1 = (TextMessage) subscriber1.receive(2000);
      if (msg1 == null)
      {
         fail("Did not get any messages");
      }
      else
      {
         getLog().debug("Got message: " + msg1);
         if (msg1.getText().equals("Local Message"))
         {
            fail("Got a local message");
         }
         TextMessage msg2 = (TextMessage) subscriber1.receive(2000);
         if (msg2 != null)
         {
            getLog().debug("Got message: " + msg2);
            fail("Got an extra message.  msg1:" + msg1 + ", msg2:" + msg2);
         }
      }

      topicConnection1.stop();
      topicConnection1.close();
      topicConnection2.stop();
      topicConnection2.close();

      disconnect();
      getLog().debug("TopicNoLocal test passed");
   }

   /**
    * Test to see whether no local works if a message
    * was created somewhere else.
    */
   public void testTopicNoLocalBounce() throws Exception
   {
      getLog().debug("Starting TopicNoLocalBounce test");
      connect();

      TopicConnectionFactory topicFactory = (TopicConnectionFactory) context.lookup(TOPIC_FACTORY);
      TopicConnection topicConnection1 = topicFactory.createTopicConnection();
      topicConnection1.start();
      TopicConnection topicConnection2 = topicFactory.createTopicConnection();
      topicConnection2.start();

      // Session 1
      Session session1 = topicConnection1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Topic topic = (Topic) context.lookup(TEST_TOPIC);
      MessageConsumer subscriber1 = session1.createConsumer(topic, null, true);
      MessageProducer sender1 = session1.createProducer(topic);

      // Session 2
      Session session2 = topicConnection2.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer subscriber2 = session2.createConsumer(topic, null, true);
      MessageProducer sender2 = session2.createProducer(topic);

      drainMessagesForTopic(subscriber1);
      drainMessagesForTopic(subscriber2);

      //send the message
      sender1.send(session1.createTextMessage("Message"));

      assertTrue("Subscriber1 should not get a message", subscriber1.receiveNoWait() == null);
      TextMessage msg = (TextMessage) subscriber2.receive(2000);
      assertTrue("Subscriber2 should get a message, got " + msg, msg != null && msg.getText().equals("Message"));

      //send it back
      sender2.send(msg);

      msg = (TextMessage) subscriber1.receive(2000);
      assertTrue("Subscriber1 should get a message, got " + msg, msg != null && msg.getText().equals("Message"));
      assertTrue("Subscriber2 should not get a message", subscriber2.receiveNoWait() == null);

      topicConnection1.stop();
      topicConnection1.close();
      topicConnection2.stop();
      topicConnection2.close();

      disconnect();
      getLog().debug("TopicNoLocalBounce test passed");
   }

   /**
    * Test subscribing to a topic with one selector, then changing to another
    */
   public void testTopicSelectorChange() throws Exception
   {
      getLog().debug("Starting TopicSelectorChange test");

      getLog().debug("Create topic connection");
      TopicConnectionFactory topicFactory = (TopicConnectionFactory) context.lookup(TOPIC_FACTORY);
      topicConnection = topicFactory.createTopicConnection("john", "needle");
      
      if (JMSDestinationsUtil.isHornetQ())
      {
    	  // HornetQ doesn't support pre-defined users with client ids 
    	  topicConnection.setClientID("john_id");
      }


      topicConnection.start();

      try
      {
         getLog().debug("Retrieving Topic");
         Topic topic = (Topic) context.lookup(TEST_DURABLE_TOPIC);

         getLog().debug("Creating a send session");
         Session sendSession = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageProducer sender = sendSession.createProducer(topic);

         getLog().debug("Clearing the topic");
         Session subSession = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageConsumer subscriber = subSession.createDurableSubscriber(topic, "test");
         Message message = subscriber.receive(50);
         while (message != null)
            message = subscriber.receive(50);
         subSession.close();

         getLog().debug("Subscribing to topic, looking for Value = 'A'");
         subSession = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         subscriber = subSession.createDurableSubscriber(topic, "test", "Value = 'A'", false);

         getLog().debug("Send some messages");
         message = sendSession.createTextMessage("Message1");
         message.setStringProperty("Value", "A");
         sender.send(message);
         message = sendSession.createTextMessage("Message2");
         message.setStringProperty("Value", "A");
         sender.send(message);
         message = sendSession.createTextMessage("Message3");
         message.setStringProperty("Value", "B");
         sender.send(message);

         getLog().debug("Retrieving the A messages");
         message = subscriber.receive(2000);
         assertTrue("Expected message 1", message != null);
         assertTrue("Should get an A", message.getStringProperty("Value").equals("A"));
         message = subscriber.receive(2000);
         assertTrue("Expected message 2", message != null);
         assertTrue("Should get a second A", message.getStringProperty("Value").equals("A"));
         assertTrue("That should be it for A", subscriber.receive(2000) == null);

         getLog().debug("Closing the subscriber without acknowledgement");
         subSession.close();

         getLog().debug("Subscribing to topic, looking for Value = 'B'");
         subSession = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         subscriber = subSession.createDurableSubscriber(topic, "test", "Value = 'B'", false);

         getLog().debug("Retrieving the non-existent B messages");
         assertTrue("B should not be there", subscriber.receive(2000) == null);

         getLog().debug("Closing the subscriber.");
         subSession.close();

         getLog().debug("Subscribing to topic, looking for those Value = 'A'");
         subSession = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         subscriber = subSession.createDurableSubscriber(topic, "test", "Value = 'A'", false);
         assertTrue("Should not be any A the subscription was changed", subscriber.receive(2000) == null);
         subSession.close();

         getLog().debug("Subscribing to topic, looking for everything");
         subSession = topicConnection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
         subscriber = subSession.createDurableSubscriber(topic, "test", null, false);

         message = sendSession.createTextMessage("Message4");
         message.setStringProperty("Value", "A");
         sender.send(message);

         message = subscriber.receive(2000);
         assertTrue("Expected message 4", message != null);
         assertTrue("Should be an A which we don't acknowledge", message.getStringProperty("Value").equals("A"));
         subSession.close();

         getLog().debug("Subscribing to topic, looking for the Value = 'A'");
         subSession = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         subscriber = subSession.createDurableSubscriber(topic, "test", "Value = 'A'", false);
         assertTrue(
            "Should not be any A, the subscription was changed. Even though the old and new selectors match the message",
            subscriber.receive(2000) == null);
         subSession.close();

         getLog().debug("Closing the send session");
         sendSession.close();

         getLog().debug("Removing the subscription");
         subSession = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         subSession.unsubscribe("test");

      }
      finally
      {
         getLog().debug("Closing the connection");
         topicConnection.close();
      }

      getLog().debug("TopicSelectorChange test passed");
   }

   /**
    * Test subscribing to a topic with a null and empty selector
    */
   public void testTopicSelectorNullOrEmpty() throws Exception
   {
      getLog().debug("Starting TopicSelectorNullOrEmpty test");

      getLog().debug("Create topic connection");
      TopicConnectionFactory topicFactory = (TopicConnectionFactory) context.lookup(TOPIC_FACTORY);
      topicConnection = topicFactory.createTopicConnection("john", "needle");
      
      if (JMSDestinationsUtil.isHornetQ())
      {
    	  // HornetQ doesn't support pre-defined users with client ids 
    	  topicConnection.setClientID("john_id");
      }


      topicConnection.start();

      try
      {
         getLog().debug("Retrieving Topic");
         Topic topic = (Topic) context.lookup(TEST_DURABLE_TOPIC);

         getLog().debug("Creating a send session");
         Session sendSession = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageProducer sender = sendSession.createProducer(topic);

         getLog().debug("Clearing the topic");
         Session subSession = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageConsumer subscriber = subSession.createDurableSubscriber(topic, "test");
         TextMessage message = (TextMessage) subscriber.receive(50);
         while (message != null)
            message = (TextMessage) subscriber.receive(50);
         subSession.close();

         getLog().debug("Subscribing to topic, with null selector");
         subSession = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         subscriber = subSession.createDurableSubscriber(topic, "test", null, false);

         getLog().debug("Send a message");
         message = sendSession.createTextMessage("Message1");
         sender.send(message);

         getLog().debug("Retrieving the message");
         message = (TextMessage) subscriber.receive(2000);
         assertTrue("Expected message 1", message != null);
         assertTrue("Should get Message1", message.getText().equals("Message1"));
         getLog().debug("Closing the subscriber");
         subSession.close();

         getLog().debug("Subscribing to topic, with an empty selector");
         subSession = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         subscriber = subSession.createDurableSubscriber(topic, "test", "", false);

         getLog().debug("Send a message");
         message = sendSession.createTextMessage("Message2");
         sender.send(message);

         getLog().debug("Retrieving the message");
         message = (TextMessage) subscriber.receive(2000);
         assertTrue("Expected message 2", message != null);
         assertTrue("Should get Message2", message.getText().equals("Message2"));
         getLog().debug("Closing the subscriber");

         getLog().debug("Removing the subscription");
         subscriber.close();
         subSession = topicConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         subSession.unsubscribe("test");
         subSession.close();

      }
      finally
      {
         getLog().debug("Closing the connection");
         topicConnection.close();
      }

      getLog().debug("TopicSelectorNullOrEmpty test passed");
   }

   /**
    * Test sending/receiving an outdated message
    */
   public void testSendReceiveOutdated() throws Exception
   {
      getLog().debug("Starting SendReceiveOutdated test");

      connect();
      try
      {
         queueConnection.start();
         queueConnection.stop();

         Session session = queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         Queue queue = (Queue) context.lookup(TEST_QUEUE);
         MessageProducer sender = session.createProducer(queue);
         MessageConsumer receiver = session.createConsumer(queue);

         // Send a message that has expired
         TextMessage message = session.createTextMessage("Outdated");
         sender.send(message, DeliveryMode.PERSISTENT, 4, 1);
         Thread.sleep(10);

         // Send a message that has not expired
         message = session.createTextMessage("OK");
         sender.send(message);

         // Try to receive the message the not expired message
         queueConnection.start();
         message = (TextMessage) receiver.receive(5000);
         assertEquals("OK", message.getText());

         // Should be no more
         assertTrue("Didn't expect anymore messages", receiver.receiveNoWait() == null);
      }
      finally
      {
         disconnect();
      }

      getLog().debug("SendReceiveOutdated test passed");
   }

   public void testSendReceiveExpired() throws Exception
   {
      getLog().debug("Starting testSendReceiveExpired test");

      connect();
      try
      {
         queueConnection.start();

         Session session = queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         Queue queue = (Queue) context.lookup(TEST_QUEUE);
         MessageProducer sender = session.createProducer(queue);
         MessageConsumer receiver = session.createConsumer(queue);

         // Send a message that expires in 5 seconds
         TextMessage message = session.createTextMessage("5 Second Expiration");
         sender.send(message, DeliveryMode.PERSISTENT, 4, 5*1000);
         // Send a message that has not expired
         message = session.createTextMessage("OK");
         sender.send(message);
         // Sleep 6 seconds
         Thread.sleep(6*1000);
         // Try to receive the OK message
         message = (TextMessage) receiver.receive(5000);
         assertEquals("OK", message.getText());

         // Should be no more
         assertTrue("Didn't expect anymore messages", receiver.receiveNoWait() == null);

         // Send a message that expires in 10 seconds
         message = session.createTextMessage("10 Second Expiration");
         sender.send(message, DeliveryMode.PERSISTENT, 4, 10*1000);
         // Send a message that has not expired
         message = session.createTextMessage("OK");
         sender.send(message);
         // Sleep 1 seconds
         Thread.sleep(1*1000);
         // Try to receive the messages
         message = (TextMessage) receiver.receive(5000);
         assertEquals("10 Second Expiration", message.getText());
         message = (TextMessage) receiver.receive(5000);
         assertEquals("OK", message.getText());

         // Should be no more
         assertTrue("Didn't expect anymore messages", receiver.receiveNoWait() == null);
         
         // Test that JMSExpiration has no affect
         message = session.createTextMessage("5 Second Expiration");
         message.setJMSExpiration(System.currentTimeMillis() + 5*1000);
         sender.send(message, DeliveryMode.PERSISTENT, 4, 0);
         // Send a message that has not expired
         message = session.createTextMessage("OK");
         sender.send(message);
         // Sleep 6 seconds
         Thread.sleep(6*1000);
         // Try to receive the OK message
         message = (TextMessage) receiver.receive(5000);
         assertEquals("5 Second Expiration", message.getText());
         message = (TextMessage) receiver.receive(5000);
         assertEquals("OK", message.getText());
         assertTrue("Didn't expect anymore messages", receiver.receiveNoWait() == null);
      }
      finally
      {
         disconnect();
      }
   }

   class Synch
   {
      boolean waiting = false;
      String text;
      public synchronized void doWait(long timeout) throws InterruptedException
      {
         waiting = true;
         this.wait(timeout);
      }
      public synchronized void doNotify() throws InterruptedException
      {
         while (waiting == false)
            wait(100);
         this.notifyAll();
      }
      public String getText()
      {
         return text;
      }
      public void setText(String text)
      {
         this.text = text;
      }
   }

   /**
    * Test sending/listening an outdated message
    */
   public void testSendListenOutdated() throws Exception
   {
      getLog().debug("Starting SendListenOutdated test");

      connect();
      try
      {
         queueConnection.start();
         queueConnection.stop();

         Session session = queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         Queue queue = (Queue) context.lookup(TEST_QUEUE);
         MessageProducer sender = session.createProducer(queue);
         MessageConsumer receiver = session.createConsumer(queue);

         // Send a message that has expired
         TextMessage message = session.createTextMessage("Outdated");
         sender.send(message, DeliveryMode.PERSISTENT, 4, 1);
         Thread.sleep(100);

         // Send a message that has not expired
         message = session.createTextMessage("OK");
         sender.send(message);

         // Try to receive the message the not expired message
         final Synch synch = new Synch();
         MessageListener messagelistener = new MessageListener()
         {
            public void onMessage(Message message)
            {
               listenOutdated(message, synch);
            }
         };
         receiver.setMessageListener(messagelistener);
         queueConnection.start();

         synch.doWait(10000);
         assertEquals("OK", synch.getText());
      }
      finally
      {
         disconnect();
      }

      getLog().debug("SendListenOutdated test passed");
   }

   private void listenOutdated(Message message, Synch synch)
   {
      try
      {
         synch.setText(((TextMessage) message).getText());
      }
      catch (Throwable t)
      {
         log.error("Error:", t);
      }
      finally
      {
         try
         {
            synch.doNotify();
         }
         catch (Throwable t)
         {
            log.error("Error:", t);
         }
      }
   }
}
