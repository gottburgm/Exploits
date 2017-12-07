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
package org.jboss.test.jbossmessaging.perf;

import java.util.concurrent.CountDownLatch;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
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

import org.jboss.logging.Logger;
import org.jboss.test.JBossJMSTestCase;
import org.jboss.test.util.jms.JMSDestinationsUtil;
/**
 * JMSPerfStressTestCase.java Some simple tests of JMS provider
 *
 * @author <a href="mailto:richard.achmatowicz@jboss.com">Richard Achmatowicz</a>
 * @author
 * @version
 */

public class JMSPerfStressTestCase extends JBossJMSTestCase
{

   // Provider specific
   static String TOPIC_FACTORY = "ConnectionFactory";
   static String QUEUE_FACTORY = "ConnectionFactory";

   static String TEST_QUEUE = "queue/testQueue";
   static String TEST_TOPIC = "topic/testTopic";

   //   static int PERFORMANCE_TEST_ITERATIONS = 1000;
   static byte[] PERFORMANCE_TEST_DATA_PAYLOAD = new byte[10 * 1024];

   static int TRANS_NONE = 0;
   static int TRANS_INDIVIDUAL = 1;
   static int TRANS_TOTAL = 2;
   static String[] TRANS_DESC = {"NOT", "individually", "totally"};

   //JMSProviderAdapter providerAdapter;
   static Context context;
   static QueueConnection queueConnection;
   static TopicConnection topicConnection;

   /**
    * Constructor for the JMSPerfStressTestCase object
    *
    * @param name           Description of Parameter
    * @exception Exception  Description of Exception
    */
   public JMSPerfStressTestCase(String name) throws Exception
   {
      super(name);
   }


   /**
    * #Description of the Method
    *
    * @param transacted     Description of Parameter
    * @param persistence    Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void runAsynchQueuePerformance(final int transacted, final int persistence) throws Exception
   {
      {
         queueConnection.start();
         drainQueue();
         queueConnection.stop();
      }
      final int iterationCount = getIterationCount();
      final Logger log = getLog();

      Thread sendThread =
         new Thread()
         {
            /**
             * Main processing method for the JBossMQPerfStressTestCase object
             */
            public void run()
            {
               try
               {
                  QueueSession session = queueConnection.createQueueSession(transacted != TRANS_NONE, Session.AUTO_ACKNOWLEDGE);
                  Queue queue = (Queue)context.lookup(TEST_QUEUE);

                  QueueSender sender = session.createSender(queue);

                  BytesMessage message = session.createBytesMessage();
                  message.writeBytes(PERFORMANCE_TEST_DATA_PAYLOAD);

                  long startTime = System.currentTimeMillis();
                  for (int i = 0; i < iterationCount; i++)
                  {
                     //sender.send(queue, message, persistence, 4, 0);
                     sender.send(message, persistence, 4, 0);
                     //getLog().debug("  Sent #"+i);
                     if (transacted == TRANS_INDIVIDUAL)
                     {
                        session.commit();
                     }
                  }

                  if (transacted == TRANS_TOTAL)
                  {
                     session.commit();
                  }

                  long endTime = System.currentTimeMillis();

                  session.close();

                  long pTime = endTime - startTime;
                  log.debug("  sent all messages in " + ((double)pTime / 1000) + " seconds. ");
               }
               catch (Exception e)
               {
                  log.error("error", e);
               }
            }
         };

      final QueueSession session = queueConnection.createQueueSession(transacted != TRANS_NONE, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue)context.lookup(TEST_QUEUE);
      QueueReceiver receiver = session.createReceiver(queue);

      MessageListener listener =
         new MessageListener()
         {
            long startTime = System.currentTimeMillis();
            int i = 0;

            /**
             * #Description of the Method
             *
             * @param message  Description of Parameter
             */
            public void onMessage(Message message)
            {
               try
               {
				if( transacted == TRANS_INDIVIDUAL )
					session.commit();
                  i++;
               }
               catch (JMSException e)
               {
                  getLog().error("Unable to commit", e);
                  synchronized (this)
                  {
                     this.notify();
                  }
               }
               if (i >= iterationCount)
               {
                  long endTime = System.currentTimeMillis();
                  long pTime = endTime - startTime;
                  log.debug("  received all messages in " + ((double)pTime / 1000) + " seconds. ");

                  synchronized (this)
                  {
                     this.notify();
                  }
               }
            }
         };

      getLog().debug("  Asynch Queue: This test will send " + getIterationCount() + " "
             + (persistence == DeliveryMode.PERSISTENT ? "persistent" : "non-persistent") + " messages. Each with a payload of "
             + ((double)PERFORMANCE_TEST_DATA_PAYLOAD.length / 1024) + "Kb"
             + " Session is " + TRANS_DESC[transacted] + " transacted");
      long startTime = System.currentTimeMillis();
      sendThread.start();
      receiver.setMessageListener(listener);
      synchronized (listener)
      {
         queueConnection.start();
         listener.wait();
      }

      if (transacted == TRANS_TOTAL)
      {
         session.commit();
      }

      session.close();
      sendThread.join();
      long endTime = System.currentTimeMillis();
      long pTime = endTime - startTime;
      getLog().debug("  All threads finished after: " + ((double)pTime / 1000) + " seconds. ");

   }

   /**
    * #Description of the Method
    *
    * @param transacted     Description of Parameter
    * @param persistence    Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void runAsynchTopicPerformance(final int transacted, final int persistence) throws Exception
   {
      {
         queueConnection.start();
         drainQueue();
      }

      final int iterationCount = getIterationCount();
      final Logger log = getLog();
      
      final CountDownLatch startFlag = new CountDownLatch(1);

      Thread sendThread =
         new Thread()
         {
            /**
             * Main processing method for the JMSPerfStressTestCase object
             */
            public void run()
            {
               try
               {

                  TopicSession session = topicConnection.createTopicSession(transacted != TRANS_NONE, Session.AUTO_ACKNOWLEDGE);
                  Topic topic = (Topic)context.lookup(TEST_TOPIC);

                  TopicPublisher publisher = session.createPublisher(topic);

                  startFlag.await();

                  BytesMessage message = session.createBytesMessage();
                  message.writeBytes(PERFORMANCE_TEST_DATA_PAYLOAD);

                  long startTime = System.currentTimeMillis();
                  for (int i = 0; i < iterationCount; i++)
                  {
                     publisher.publish(message, persistence, 4, 0);
                     //publisher.publish(topic, message, persistence, 4, 0);
                     //getLog().debug("  Sent #"+i);
                     if (transacted == TRANS_INDIVIDUAL)
                     {
                        session.commit();
                     }
                  }

                  if (transacted == TRANS_TOTAL)
                  {
                     session.commit();
                  }

                  long endTime = System.currentTimeMillis();
                  session.close();

                  long pTime = endTime - startTime;
                  log.debug("  sent all messages in " + ((double)pTime / 1000) + " seconds. ");
               }
               catch (Exception e)
               {
                  log.error("error", e);
               }
            }
         };

      final TopicSession session = topicConnection.createTopicSession(transacted != TRANS_NONE, Session.AUTO_ACKNOWLEDGE);
      Topic topic = (Topic)context.lookup(TEST_TOPIC);
      TopicSubscriber subscriber = session.createSubscriber(topic);

      MessageListener listener =
         new MessageListener()
         {
            long startTime = System.currentTimeMillis();
            int i = 0;

            /**
             * #Description of the Method
             *
             * @param message  Description of Parameter
             */
            public void onMessage(Message message)
            {
               try
               {
				if( transacted == TRANS_INDIVIDUAL )
					session.commit();
                  i++;
               }
               catch (JMSException e)
               {
                  getLog().error("Unable to commit", e);
                  synchronized (this)
                  {
                     this.notify();
                  }
               }
               if (i >= iterationCount)
               {
                  long endTime = System.currentTimeMillis();
                  long pTime = endTime - startTime;
                  log.debug("  received all messages in " + ((double)pTime / 1000) + " seconds. ");

                  synchronized (this)
                  {
                     this.notify();
                  }
               }
            }
         };

      getLog().debug("  Asynch Topic: This test will send " + getIterationCount() + " "
             + (persistence == DeliveryMode.PERSISTENT ? "persistent" : "non-persistent") + " messages. Each with a payload of "
             + ((double)PERFORMANCE_TEST_DATA_PAYLOAD.length / 1024) + "Kb"
             + " Session is " + TRANS_DESC[transacted] + " transacted");
      long startTime = System.currentTimeMillis();
      sendThread.start();
      subscriber.setMessageListener(listener);
      startFlag.countDown();
      synchronized (listener)
      {
         topicConnection.start();
         listener.wait();
      }

      if (transacted == TRANS_TOTAL)
      {
         session.commit();
      }

      session.close();
      sendThread.join();
      long endTime = System.currentTimeMillis();
      long pTime = endTime - startTime;
      getLog().debug("  All threads finished after: " + ((double)pTime / 1000) + " seconds. ");

   }

   /**
    * #Description of the Method
    *
    * @param transacted     Description of Parameter
    * @param persistence    Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void runSynchQueuePerformance(final int transacted, final int persistence) throws Exception
   {
      {
         queueConnection.start();
         drainQueue();
      }
      final int iterationCount = getIterationCount();
      final Logger log = getLog();

      Thread sendThread =
         new Thread()
         {
            /**
             * Main processing method for the JMSPerfStressTestCase object
             */
            public void run()
            {
               try
               {
                  QueueSession session = queueConnection.createQueueSession(transacted != TRANS_NONE, Session.AUTO_ACKNOWLEDGE);
                  Queue queue = (Queue)context.lookup(TEST_QUEUE);

                  QueueSender sender = session.createSender(queue);

                  BytesMessage message = session.createBytesMessage();
                  message.writeBytes(PERFORMANCE_TEST_DATA_PAYLOAD);

                  long startTime = System.currentTimeMillis();
                  for (int i = 0; i < iterationCount; i++)
                  {
                     sender.send( message, persistence, 4, 0);
                     //sender.send(queue, message, persistence, 4, 0);
                     //getLog().debug("  Sent #"+i);
                     if (transacted == TRANS_INDIVIDUAL)
                     {
                        session.commit();
                     }
                  }

                  if (transacted == TRANS_TOTAL)
                  {
                     session.commit();
                  }

                  session.close();

                  long endTime = System.currentTimeMillis();

                  long pTime = endTime - startTime;
                  log.debug("  sent all messages in " + ((double)pTime / 1000) + " seconds. ");
               }
               catch (Exception e)
               {
                  log.error("error", e);
               }
            }
         };

      Thread recvThread =
         new Thread()
         {
            /**
             * Main processing method for the JMSPerfStressTestCase object
             */
            public void run()
            {
               try
               {

                  QueueSession session = queueConnection.createQueueSession(transacted != TRANS_NONE, Session.AUTO_ACKNOWLEDGE);
                  Queue queue = (Queue)context.lookup(TEST_QUEUE);

                  QueueReceiver receiver = session.createReceiver(queue);
                  long startTime = System.currentTimeMillis();
                  for (int i = 0; i < iterationCount; i++)
                  {
                     receiver.receive();
                     //getLog().debug("  Received #"+i);
                     if (transacted == TRANS_INDIVIDUAL)
                     {
                        session.commit();
                     }
                  }

                  if (transacted == TRANS_TOTAL)
                  {
                     session.commit();
                  }

                  long endTime = System.currentTimeMillis();

                  session.close();

                  long pTime = endTime - startTime;
                  log.debug("  received all messages in " + ((double)pTime / 1000) + " seconds. ");

               }
               catch (Exception e)
               {
                  log.error("error", e);
               }
            }
         };

      getLog().debug("  Synch Queue: This test will send " + getIterationCount() + " "
             + (persistence == DeliveryMode.PERSISTENT ? "persistent" : "non-persistent") + " messages. Each with a payload of "
             + ((double)PERFORMANCE_TEST_DATA_PAYLOAD.length / 1024) + "Kb"
             + " Session is " + TRANS_DESC[transacted] + " transacted");
      long startTime = System.currentTimeMillis();
      sendThread.start();
      recvThread.start();
      sendThread.join();
      recvThread.join();
      long endTime = System.currentTimeMillis();
      long pTime = endTime - startTime;
      getLog().debug("  All threads finished after: " + ((double)pTime / 1000) + " seconds. ");

   }

   /**
    * #Description of the Method
    *
    * @param transacted     Description of Parameter
    * @param persistence    Description of Parameter
    * @exception Exception  Description of Exception
    */
   public void runSynchTopicPerformance(final int transacted, final int persistence) throws Exception
   {
      {
         queueConnection.start();
         topicConnection.start();
         drainQueue();
      }
      final int iterationCount = getIterationCount();
      final Logger log = getLog();
      
      final CountDownLatch alignFlag = new CountDownLatch(2);
      

      Thread sendThread =
         new Thread("sendThread transacted = " + transacted + " persistent = " + persistence)
         {
            /**
             * Main processing method for the JBossMQPerfStressTestCase object
             */
            public void run()
            {
               try
               {

                  TopicSession session = topicConnection.createTopicSession(transacted != TRANS_NONE, Session.AUTO_ACKNOWLEDGE);
                  Topic topic = (Topic)context.lookup(TEST_TOPIC);

                  TopicPublisher publisher = session.createPublisher(topic);

                  alignFlag.countDown();
                  alignFlag.await();

                  BytesMessage message = session.createBytesMessage();
                  message.writeBytes(PERFORMANCE_TEST_DATA_PAYLOAD);

                  long startTime = System.currentTimeMillis();
                  for (int i = 0; i < iterationCount; i++)
                  {
                     publisher.publish(message, persistence, 4, 0);
                     //publisher.publish(topic, message, persistence, 4, 0);
                     //getLog().debug("  Sent #"+i);
                     if (transacted == TRANS_INDIVIDUAL)
                     {
                        session.commit();
                     }
                  }

                  if (transacted == TRANS_TOTAL)
                  {
                     session.commit();
                  }

                  long endTime = System.currentTimeMillis();

                  session.close();

                  long pTime = endTime - startTime;
                  log.debug("  sent all messages in " + ((double)pTime / 1000) + " seconds. ");
               }
               catch (Exception e)
               {
                  log.error("error", e);
               }
            }
         };

      Thread recvThread =
         new Thread("recvThread transacted = " + transacted + " persistent = " + persistence)
         {
            /**
             * Main processing method for the JBossMQPerfStressTestCase object
             */
            public void run()
            {
               try
               {

                  TopicSession session = topicConnection.createTopicSession(transacted != TRANS_NONE, Session.AUTO_ACKNOWLEDGE);
                  Topic topic = (Topic)context.lookup(TEST_TOPIC);
                  TopicSubscriber subscriber = session.createSubscriber(topic);

                  alignFlag.countDown();
                  alignFlag.await();

                  long startTime = System.currentTimeMillis();
                  for (int i = 0; i < iterationCount; i++)
                  {
                     subscriber.receive();
                     //getLog().debug("  Received #"+i);
                     if (transacted == TRANS_INDIVIDUAL)
                     {
                        session.commit();
                     }
                  }

                  if (transacted == TRANS_TOTAL)
                  {
                     session.commit();
                  }

                  long endTime = System.currentTimeMillis();

                  session.close();

                  long pTime = endTime - startTime;
                  log.debug("  received all messages in " + ((double)pTime / 1000) + " seconds. ");

               }
               catch (Exception e)
               {
                  log.error("error", e);
               }
            }
         };

      getLog().debug("  Synch Topic: This test will send " + getIterationCount() + " "
             + (persistence == DeliveryMode.PERSISTENT ? "persistent" : "non-persistent") + " messages. Each with a payload of "
             + ((double)PERFORMANCE_TEST_DATA_PAYLOAD.length / 1024) + "Kb"
             + " Session is " + TRANS_DESC[transacted] + " transacted");
      long startTime = System.currentTimeMillis();
      sendThread.start();
      recvThread.start();
      sendThread.join();
      recvThread.join();
      long endTime = System.currentTimeMillis();
      long pTime = endTime - startTime;
      getLog().debug("  All threads finished after: " + ((double)pTime / 1000) + " seconds. ");

   }
   
   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testAsynchQueuePerformance() throws Exception
   {

      getLog().debug("Starting AsynchQueuePerformance test");

      runAsynchQueuePerformance(TRANS_NONE, DeliveryMode.NON_PERSISTENT);
      runAsynchQueuePerformance(TRANS_NONE, DeliveryMode.PERSISTENT);
      runAsynchQueuePerformance(TRANS_INDIVIDUAL, DeliveryMode.NON_PERSISTENT);
      runAsynchQueuePerformance(TRANS_INDIVIDUAL, DeliveryMode.PERSISTENT);
      runAsynchQueuePerformance(TRANS_TOTAL, DeliveryMode.NON_PERSISTENT);
      runAsynchQueuePerformance(TRANS_TOTAL, DeliveryMode.PERSISTENT);

      getLog().debug("AsynchQueuePerformance passed");
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testAsynchTopicPerformance() throws Exception
   {

      getLog().debug("Starting AsynchTopicPerformance test");

      runAsynchTopicPerformance(TRANS_NONE, DeliveryMode.NON_PERSISTENT);
      runAsynchTopicPerformance(TRANS_NONE, DeliveryMode.PERSISTENT);
      runAsynchTopicPerformance(TRANS_INDIVIDUAL, DeliveryMode.NON_PERSISTENT);
      runAsynchTopicPerformance(TRANS_INDIVIDUAL, DeliveryMode.PERSISTENT);
      runAsynchTopicPerformance(TRANS_TOTAL, DeliveryMode.NON_PERSISTENT);
      runAsynchTopicPerformance(TRANS_TOTAL, DeliveryMode.PERSISTENT);

      getLog().debug("AsynchTopicPerformance passed");
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testSynchQueuePerformance() throws Exception
   {

      getLog().debug("Starting SynchQueuePerformance test");

      runSynchQueuePerformance(TRANS_NONE, DeliveryMode.NON_PERSISTENT);
      runSynchQueuePerformance(TRANS_NONE, DeliveryMode.PERSISTENT);
      runSynchQueuePerformance(TRANS_INDIVIDUAL, DeliveryMode.NON_PERSISTENT);
      runSynchQueuePerformance(TRANS_INDIVIDUAL, DeliveryMode.PERSISTENT);
      runSynchQueuePerformance(TRANS_TOTAL, DeliveryMode.NON_PERSISTENT);
      runSynchQueuePerformance(TRANS_TOTAL, DeliveryMode.PERSISTENT);

      getLog().debug("SynchQueuePerformance passed");
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testSynchTopicPerformance() throws Exception
   {

      getLog().debug("Starting SynchTopicPerformance test");

      runSynchTopicPerformance(TRANS_NONE, DeliveryMode.NON_PERSISTENT);
      runSynchTopicPerformance(TRANS_NONE, DeliveryMode.PERSISTENT);
      runSynchTopicPerformance(TRANS_INDIVIDUAL, DeliveryMode.NON_PERSISTENT);
      runSynchTopicPerformance(TRANS_INDIVIDUAL, DeliveryMode.PERSISTENT);
      runSynchTopicPerformance(TRANS_TOTAL, DeliveryMode.NON_PERSISTENT);
      runSynchTopicPerformance(TRANS_TOTAL, DeliveryMode.PERSISTENT);

      getLog().debug("SynchTopicPerformance passed");
   }

//   public static TestSuite suite() throws Exception
//   {
//      TestSuite suite = new TestSuite();
//      suite.addTest(new JMSPerfStressTestCase("testSynchTopicPerformance"));
//      return suite;
//   }


   /**
    * The JUnit setup method
    *
    * @exception Exception  Description of Exception
    */
   protected void setUp() throws Exception
   {
	   // perform any setUp() required by the base class
	   super.setUp() ;
      JMSDestinationsUtil.setupBasicDestinations();

	   if (context == null)
	   {
		   Logger log = getLog() ;
		   if (log == null)
			   System.out.println("JMSPerfStressTestCase: getLog() returned null") ;

		   getLog().debug("JMSPerfStresTestCase - setUp") ;

		   context = getInitialContext();
	   }

	   QueueConnectionFactory queueFactory = (QueueConnectionFactory)context.lookup(QUEUE_FACTORY);
	   queueConnection = queueFactory.createQueueConnection();

	   TopicConnectionFactory topicFactory = (TopicConnectionFactory)context.lookup(TOPIC_FACTORY);
	   topicConnection = topicFactory.createTopicConnection();

	   getLog().debug("Connection to JMS provider established.");

   }
   
   protected void tearDown() throws Exception
   {
	   try {queueConnection.close();} catch (Throwable ignored){}
	   try {topicConnection.close();} catch (Throwable ignored){}
	   
	   JMSDestinationsUtil.destroyDestinations();
   }


   // Emptys out all the messages in a queue
   private void drainQueue() throws Exception
   {

      QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue)context.lookup(TEST_QUEUE);

      QueueReceiver receiver = session.createReceiver(queue);
      Message message = receiver.receive(50);
      int c = 0;
      while (message != null)
      {
         message = receiver.receive(50);
         c++;
      }

      if (c != 0)
      {
         getLog().debug("  Drained " + c + " messages from the queue");
      }

      session.close();

   }


}
