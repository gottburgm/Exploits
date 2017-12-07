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
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
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
import javax.naming.Context;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.logging.Logger;
import org.jboss.test.JBossJMSTestCase;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/**
 * Rollback tests
 *
 * @author <a href="mailto:richard.achmatowicz@jboss.com">Richard Achmatowicz</a>
 * @author
 * @version $Revision: 105321 $
 */
public class UnackedUnitTestCase extends JBossJMSTestCase
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

   public static Test suite() throws Exception
   {
      // JBAS-3580, the execution order of tests in this test case is important
      // so it must be defined explicitly when running under some JVMs
      TestSuite suite = new TestSuite();
      suite.addTest(new UnackedUnitTestCase("testUnackedQueue"));
      suite.addTest(new UnackedUnitTestCase("testUnackedMultipleConnection"));
      suite.addTest(new UnackedUnitTestCase("testUnackedMultipleSession"));
      suite.addTest(new UnackedUnitTestCase("testUnackedTopic"));
      suite.addTest(new UnackedUnitTestCase("testUnackedDurableTopic"));
      suite.addTest(new UnackedUnitTestCase("testDummyLast"));
      
      return suite;
   }


   /**
    * Constructor the test
    *
    * @param name           Description of Parameter
    * @exception Exception  Description of Exception
    */
   public UnackedUnitTestCase(String name) throws Exception
   {
      super(name);
   }

   /**
    * #Description of the Method
    *
    * @param persistence    Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void runUnackedQueue(final int persistence) throws Exception
   {
      drainQueue();

      final int iterationCount = getIterationCount();

      QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue)context.lookup(TEST_QUEUE);

      QueueSender sender = session.createSender(queue);

      Message message = session.createBytesMessage();
      ((BytesMessage)message).writeBytes(PAYLOAD);

      for (int i = 0; i < iterationCount; i++)
         sender.send(message, persistence, 4, 0);

      session.close();

      session = queueConnection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
      queue = (Queue)context.lookup(TEST_QUEUE);
      QueueReceiver receiver = session.createReceiver(queue);
      queueConnection.start();
      message = receiver.receive(50);
      int c = 0;
      while (message != null)
      {
         message = receiver.receive(50);
         c++;
      }
      assertTrue("Should have received all data unacked", c == iterationCount);

      queueConnection.close();
      QueueConnectionFactory queueFactory = (QueueConnectionFactory)context.lookup(QUEUE_FACTORY);
      queueConnection = queueFactory.createQueueConnection();

      assertTrue("Queue should be full", drainQueue() == iterationCount);

   }

   /**
    * #Description of the Method
    *
    * @param persistence    Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void runUnackedMultipleSession(final int persistence) throws Exception
   {
	  System.out.println("++ runUnackedMultipleSession");
      drainQueue();

      final int iterationCount = getIterationCount();

      QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue)context.lookup(TEST_QUEUE);

      QueueSender sender = session.createSender(queue);

      Message message = session.createBytesMessage();
      ((BytesMessage)message).writeBytes(PAYLOAD);

      for (int i = 0; i < iterationCount; i++)
         sender.send(message, persistence, 4, 0);

      session.close();

      QueueSession session1 = queueConnection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
      QueueSession session2 = queueConnection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
      try
      {
	      queue = (Queue)context.lookup(TEST_QUEUE);
	      QueueReceiver receiver1 = session1.createReceiver(queue);
	      
	      QueueReceiver receiver2 = session2.createReceiver(queue);
	      queueConnection.start();
	
	      int c1=0, c2=0;
	      
	      Message messageConsumer1=null, messageConsumer2=null;
	      
	      Message lastMessageConsumer1 = null, lastMessageConsumer2=null;
	      
	      do
	      {
	    	  messageConsumer1 = receiver1.receive(1000);
	    	  messageConsumer2 = receiver2.receive(1000);
	    	  if (messageConsumer1 != null)
	    	  {
	    		  c1 ++;
	    		  lastMessageConsumer1 = messageConsumer1;
	    	  }
	    	  
	    	  if (messageConsumer2 != null)
	    	  {
	    		  c2 ++;
	    		  lastMessageConsumer2 = messageConsumer2;
	    	  }
	    	  
	    	  System.out.println("messageConsumer1=" + messageConsumer1 + " messageConsumer2=" + messageConsumer2 + " c1=" + c1 + " c2 = " + c2);
	      }
	      while (messageConsumer1!=null || messageConsumer2!=null);
	      
	      assertEquals(iterationCount, c1 + c2);
	      
	      if(lastMessageConsumer1 != null)
            lastMessageConsumer1.acknowledge();
	      if(lastMessageConsumer2 != null)
            lastMessageConsumer2.acknowledge();
	
	      queueConnection.stop();
	      session1.close();
	      session2.close();
      }
      finally
      {
    	  try
    	  {
    		  session1.close();
    	  }
    	  catch (Throwable ignored)
    	  {
    	  }
    	  try
    	  {
    		  session2.close();
    	  }
    	  catch (Throwable ignored)
    	  {
    	  }
      }

	  System.out.println("-- runUnackedMultipleSession");
   }

   /**
    * #Description of the Method
    *
    * @param persistence    Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void runUnackedMultipleConnection(final int persistence) throws Exception
   {
	  System.out.println("++runUnackedMultipleConnection");
      drainQueue();

      final int iterationCount = getIterationCount();

      QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue)context.lookup(TEST_QUEUE);

      QueueSender sender = session.createSender(queue);

      Message message = session.createBytesMessage();
      ((BytesMessage)message).writeBytes(PAYLOAD);

      for (int i = 0; i < iterationCount; i++)
         sender.send(message, persistence, 4, 0);

      session.close();

      QueueConnectionFactory queueFactory = (QueueConnectionFactory)context.lookup(QUEUE_FACTORY);
      QueueConnection queueConnection1 = queueFactory.createQueueConnection();
      QueueConnection queueConnection2 = queueFactory.createQueueConnection();

      try
      {
	      QueueSession session1 = queueConnection1.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
	      queue = (Queue)context.lookup(TEST_QUEUE);
	      QueueReceiver receiver1 = session1.createReceiver(queue);
	
	      QueueSession session2 = queueConnection2.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
	      QueueReceiver receiver2 = session2.createReceiver(queue);
	
	      queueConnection1.start();
	      queueConnection2.start();
	
	      int c1=0, c2=0;
	      
	      Message messageConsumer1=null, messageConsumer2=null;
	      
	      Message lastMessageConsumer1 = null, lastMessageConsumer2=null;
	      
	      do
	      {
	    	  messageConsumer1 = receiver1.receive(100);
	    	  messageConsumer2 = receiver2.receive(100);
	    	  if (messageConsumer1 != null)
	    	  {
	    		  c1 ++;
	    		  lastMessageConsumer1 = messageConsumer1;
	    	  }
	    	  
	    	  if (messageConsumer2 != null)
	    	  {
	    		  c2 ++;
	    		  lastMessageConsumer2 = messageConsumer2;
	    	  }
	    	  
	    	  System.out.println("messageConsumer1=" + messageConsumer1 + " messageConsumer2=" + messageConsumer2 + " c1=" + c1 + " c2 = " + c2);
	      }
	      while (messageConsumer1!=null || messageConsumer2!=null);

	      if (lastMessageConsumer1!=null) lastMessageConsumer1.acknowledge();
	      if (lastMessageConsumer2!=null) lastMessageConsumer2.acknowledge();
	      
	      assertEquals(iterationCount, c1 + c2);
	      
      }
      finally
      {
    	  try{queueConnection1.close();} catch (Throwable ignored){}
    	  try{queueConnection2.close();} catch (Throwable ignored){}
      }

	  System.out.println("--runUnackedMultipleConnection");
   }

   /**
    * #Description of the Method
    *
    * @param persistence    Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void runUnackedTopic(final int persistence) throws Exception
   {
      drainQueue();
      drainTopic();

      final int iterationCount = getIterationCount();
      final Logger log = getLog();

      Thread sendThread =
         new Thread()
         {
            public void run()
            {
               try
               {

                  TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
                  Topic topic = (Topic)context.lookup(TEST_TOPIC);

                  TopicPublisher publisher = session.createPublisher(topic);

                  waitForSynchMessage();

                  BytesMessage message = session.createBytesMessage();
                  message.writeBytes(PAYLOAD);

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

      TopicSession session = topicConnection.createTopicSession(false, Session.CLIENT_ACKNOWLEDGE);
      Topic topic = (Topic)context.lookup(TEST_TOPIC);
      TopicSubscriber subscriber = session.createSubscriber(topic);


      MyMessageListener listener = new MyMessageListener(iterationCount, log);

      queueConnection.start();
      sendThread.start();
      subscriber.setMessageListener(listener);
      topicConnection.start();
      sendSynchMessage();
      synchronized (listener)
      {
         if (listener.i < iterationCount)
            listener.wait(5000);
         
         assertEquals(iterationCount, listener.i);
      }
      sendThread.join();
      topicConnection.close();
      TopicConnectionFactory topicFactory = (TopicConnectionFactory)context.lookup(TOPIC_FACTORY);
      topicConnection = topicFactory.createTopicConnection();
      queueConnection.stop();
      assertTrue("Topic should be empty", drainTopic() == 0);
   }

   /**
    * #Description of the Method
    *
    * @param persistence    Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void runUnackedDurableTopic(final int persistence) throws Exception
   {
      drainQueue();
      drainDurableTopic();

      final int iterationCount = getIterationCount();
      final Logger log = getLog();

      Thread sendThread =
         new Thread()
         {
            public void run()
            {
               try
               {

                  TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
                  Topic topic = (Topic)context.lookup(TEST_DURABLE_TOPIC);

                  TopicPublisher publisher = session.createPublisher(topic);

                  waitForSynchMessage();

                  BytesMessage message = session.createBytesMessage();
                  message.writeBytes(PAYLOAD);

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

      TopicSession session = topicDurableConnection.createTopicSession(false, Session.CLIENT_ACKNOWLEDGE);
      Topic topic = (Topic)context.lookup(TEST_DURABLE_TOPIC);
      TopicSubscriber subscriber = session.createDurableSubscriber(topic, "test");

      MyMessageListener listener = new MyMessageListener(iterationCount, log);

      queueConnection.start();
      sendThread.start();
      subscriber.setMessageListener(listener);
      topicDurableConnection.start();
      sendSynchMessage();
      synchronized (listener)
      {
    	 System.out.println("Hello");
         if (listener.i < iterationCount)
         {
            listener.wait(5000);
         }
         assertEquals(iterationCount, listener.i);
      }
      

      sendThread.join();
      topicDurableConnection.close();
      TopicConnectionFactory topicFactory = (TopicConnectionFactory)context.lookup("PreconfClientIDConnectionfactory");
      topicDurableConnection = topicFactory.createTopicConnection("john", "needle");
      queueConnection.stop();
      assertTrue("Topic should be full", drainDurableTopic() == iterationCount);
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testUnackedQueue() throws Exception
   {

      getLog().debug("Starting UnackedQueue test");

      runUnackedQueue(DeliveryMode.NON_PERSISTENT);
      runUnackedQueue(DeliveryMode.PERSISTENT);

      getLog().debug("UnackedQueue passed");
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testUnackedMultipleSession() throws Exception
   {

      getLog().debug("Starting UnackedMultipleSession test");

      runUnackedMultipleSession(DeliveryMode.NON_PERSISTENT);
      runUnackedMultipleSession(DeliveryMode.PERSISTENT);

      getLog().debug("UnackedMultipleSession passed");
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testUnackedMultipleConnection() throws Exception
   {

      getLog().debug("Starting UnackedMultipleConnection test");

      runUnackedMultipleConnection(DeliveryMode.NON_PERSISTENT);
      runUnackedMultipleConnection(DeliveryMode.PERSISTENT);

      getLog().debug("UnackedMultipleConnection passed");
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testUnackedTopic() throws Exception
   {

      getLog().debug("Starting UnackedTopic test");

      runUnackedTopic(DeliveryMode.NON_PERSISTENT);
      runUnackedTopic(DeliveryMode.PERSISTENT);

      getLog().debug("UnackedTopic passed");
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testUnackedDurableTopic() throws Exception
   {

      getLog().debug("Starting UnackedDurableTopic test");

      runUnackedDurableTopic(DeliveryMode.NON_PERSISTENT);
      runUnackedDurableTopic(DeliveryMode.PERSISTENT);

      getLog().debug("UnackedDurableTopic passed");
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testDummyLast() throws Exception
   {

      TopicSession session = topicDurableConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      session.unsubscribe("test");

      queueConnection.close();
      topicConnection.close();
      topicDurableConnection.close();
   }

   /**
    * The JUnit setup method
    *
    * @exception Exception  Description of Exception
    */
   protected void setUp() throws Exception
   {
       // call setUp() in superclass 
       super.setUp() ;
       
       JMSDestinationsUtil.setupBasicDestinations();

      if (context == null)
      {
         context = getInitialContext();
      }

      QueueConnectionFactory queueFactory = (QueueConnectionFactory)context.lookup(QUEUE_FACTORY);
      queueConnection = queueFactory.createQueueConnection();

      TopicConnectionFactory topicFactory = (TopicConnectionFactory)context.lookup(TOPIC_FACTORY);
      topicConnection = topicFactory.createTopicConnection();
      topicFactory = (TopicConnectionFactory) context.lookup("PreconfClientIDConnectionfactory");
      topicDurableConnection = topicFactory.createTopicConnection("john", "needle");
   }
   
   protected void tearDown() throws Exception
   {
	   if (queueConnection != null)
	   {
		   queueConnection.close();
		   queueConnection = null;
	   }
	   
	   if (topicConnection != null)
	   {
		   topicConnection.close();
		   topicConnection = null;
	   }
	   
	   if (topicDurableConnection != null)
	   {
		   topicDurableConnection.close();
		   topicDurableConnection = null;
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
      Queue queue = (Queue)context.lookup(TEST_QUEUE);

      QueueReceiver receiver = session.createReceiver(queue);
      Message message = receiver.receive(1000);
      int c = 0;
      while (message != null)
      {
         message = receiver.receive(1000);
         c++;
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
      Topic topic = (Topic)context.lookup(TEST_TOPIC);
      TopicSubscriber subscriber = session.createSubscriber(topic);

      Message message = subscriber.receive(1000);
      int c = 0;
      while (message != null)
      {
         message = subscriber.receive(1000);
         c++;
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
      Topic topic = (Topic)context.lookup(TEST_DURABLE_TOPIC);
      TopicSubscriber subscriber = session.createDurableSubscriber(topic, "test");

      Message message = subscriber.receive(1000);
      int c = 0;
      while (message != null)
      {
         message = subscriber.receive(1000);
         c++;
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
      Queue queue = (Queue)context.lookup(TEST_QUEUE);

      QueueReceiver receiver = session.createReceiver(queue);
      receiver.receive();
      session.close();
      getLog().debug("Got Synch Message");
   }

   private void sendSynchMessage() throws Exception
   {
      getLog().debug("Sending Synch Message");
      QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue)context.lookup(TEST_QUEUE);

      QueueSender sender = session.createSender(queue);

      Message message = session.createMessage();
      sender.send(message);

      session.close();
      getLog().debug("Sent Synch Message");
   }

   public class MyMessageListener
      implements MessageListener
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
        	System.out.println("Listener:: Got message " + i);
            i++;
            log.debug("Got message " + i);
            if (i >= iterationCount)
               this.notifyAll();
         }
      }
   }

   public int getIterationCount()
   {
      return 200;
   }
}
