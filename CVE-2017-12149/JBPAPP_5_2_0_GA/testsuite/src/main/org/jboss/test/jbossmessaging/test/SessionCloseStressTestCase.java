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

import java.util.Random;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;

import junit.framework.Test;

import org.jboss.test.JBossJMSTestCase;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/**
 * Tests for receiving while closing the session
 *
 * @author <a href="mailto:richard.achmatowicz@jboss.com">Richard Achmatowicz</a>
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version <tt>$Revision: 105321 $</tt>
 */
public class SessionCloseStressTestCase extends JBossJMSTestCase
{
   static String QUEUE_FACTORY = "ConnectionFactory";
   static String QUEUE = "queue/testQueue";

   QueueConnection queueConnection;
   Queue queue;
   
   public SessionCloseStressTestCase(String name) throws Exception
   {
      super(name);
   }

   public abstract class TestRunnable implements Runnable
   {
      public Throwable error = null;
      
      public abstract void doRun() throws Exception;
      
      public void run()
      {
         try
         {
            doRun();
         }
         catch (Throwable t)
         {
            log.error("Error in " + Thread.currentThread(), t);
            error = t;
         }
      }
   }
   
   public class SessionRunnable extends TestRunnable
   {
      MessageConsumer consumer;
      
      int received = 0;
      
      public void doRun() throws Exception
      {
         QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageProducer producer = session.createProducer(queue);
         for (int i = 0; i < getIterationCount(); ++i)
         {
            Message message = session.createTextMessage("" + i);
            producer.send(message);
         }
         producer.close();
         consumer = session.createConsumer(queue);
         waitForMessages();
         session.close();
      }
      
      public synchronized MessageConsumer getConsumer() throws Exception
      {
         while (true)
         {
            if (consumer != null)
               return consumer;
            wait();
         }
      }
      
      public synchronized void incReceived()
      {
         ++received;
         notifyAll();
      }
      
      public synchronized void waitForMessages() throws Exception
      {
         notifyAll();
         int target = new Random().nextInt(getIterationCount());
         while (received < target)
            wait();
      }
   }
   
   public class ReceiverRunnable extends TestRunnable
   {
      SessionRunnable sessionRunnable;
      
      public ReceiverRunnable(SessionRunnable sessionRunnable)
      {
         this.sessionRunnable = sessionRunnable;
      }
      
      public void doRun() throws Exception
      {
         MessageConsumer consumer = sessionRunnable.getConsumer();
         try
         {
            while (true)
            {
               consumer.receive();
               sessionRunnable.incReceived();
            }
         }
         catch (JMSException expected)
         {
            if (expected.getMessage().indexOf("closed") == -1)
               throw expected;
         }
      }
   }
   
   public class ReceiverNoWaitRunnable extends TestRunnable
   {
      SessionRunnable sessionRunnable;
      
      public ReceiverNoWaitRunnable(SessionRunnable sessionRunnable)
      {
         this.sessionRunnable = sessionRunnable;
      }
      
      public void doRun() throws Exception
      {
         MessageConsumer consumer = sessionRunnable.getConsumer();
         try
         {
            while (true)
            {
               if (consumer.receiveNoWait() != null)
                  sessionRunnable.incReceived();
            }
         }
         catch (JMSException expected)
         {
            if (expected.getMessage().indexOf("closed") == -1)
               throw expected;
         }
      }
   }
   
   public class ReceiverMessageListenerRunnable extends TestRunnable implements MessageListener
   {
      SessionRunnable sessionRunnable;
      
      public ReceiverMessageListenerRunnable(SessionRunnable sessionRunnable)
      {
         this.sessionRunnable = sessionRunnable;
      }
      
      public void onMessage(Message message)
      {
         sessionRunnable.incReceived();
      }
      
      public void doRun() throws Exception
      {
         MessageConsumer consumer = sessionRunnable.getConsumer();
         try
         {
            consumer.setMessageListener(this);
         }
         catch (JMSException expected)
         {
            if (expected.getMessage().indexOf("closed") == -1)
               throw expected;
         }
      }
   }
   
   public void testSessionCloseCompetesWithReceive() throws Exception
   {
      connect();
      try
      {
         for (int i = 0; i < getThreadCount(); ++i)
         {
            SessionRunnable sessionRunnable = new SessionRunnable(); 
            Thread sessionThread = new Thread(sessionRunnable, "Session");
            Thread consumerThread = new Thread(new ReceiverRunnable(sessionRunnable), "Consumer");
            consumerThread.start();
            sessionThread.start();
            sessionThread.join();
            consumerThread.join();
            assertNull(sessionRunnable.error);

            // Drain the queue
            QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(queue);
            while (consumer.receiveNoWait() != null);
            session.close();
         }
      }
      finally
      {
         disconnect();
      }
   }
   
   public void testSessionCloseCompetesWithReceiveNoWait() throws Exception
   {
      connect();
      try
      {
         for (int i = 0; i < getThreadCount(); ++i)
         {
            SessionRunnable sessionRunnable = new SessionRunnable(); 
            Thread sessionThread = new Thread(sessionRunnable, "Session");
            Thread consumerThread = new Thread(new ReceiverNoWaitRunnable(sessionRunnable), "Consumer");
            consumerThread.start();
            sessionThread.start();
            sessionThread.join();
            consumerThread.join();
            assertNull(sessionRunnable.error);

            // Drain the queue
            QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(queue);
            while (consumer.receiveNoWait() != null);
            session.close();
         }
      }
      finally
      {
         disconnect();
      }
   }
   
   public void testSessionCloseCompetesWithMessageListener() throws Exception
   {
      connect();
      try
      {
         for (int i = 0; i < getThreadCount(); ++i)
         {
            SessionRunnable sessionRunnable = new SessionRunnable(); 
            Thread sessionThread = new Thread(sessionRunnable, "Session");
            Thread consumerThread = new Thread(new ReceiverMessageListenerRunnable(sessionRunnable), "Consumer");
            consumerThread.start();
            sessionThread.start();
            sessionThread.join();
            consumerThread.join();
            assertNull(sessionRunnable.error);

            // Drain the queue
            QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(queue);
            while (consumer.receiveNoWait() != null);
            session.close();
         }
      }
      finally
      {
         disconnect();
      }
   }

   protected void connect() throws Exception
   {
      Context context = getInitialContext();
      queue = (Queue) context.lookup(QUEUE);
      QueueConnectionFactory queueFactory = (QueueConnectionFactory) context.lookup(QUEUE_FACTORY);
      queueConnection = queueFactory.createQueueConnection();
      queueConnection.start();

      getLog().debug("Connection established.");
   }

   protected void disconnect()
   {
      try
      {
         if (queueConnection != null)
            queueConnection.close();
      }
      catch (Exception ignored)
      {
      }

      getLog().debug("Connection closed.");
   }

   public void setUp() throws Exception
   {
      super.setUp();
      JMSDestinationsUtil.setupBasicDestinations();
   }
   
   public void tearDown() throws Exception
   {
      JMSDestinationsUtil.destroyDestinations();
      super.tearDown();
   }

}
