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

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.naming.Context;

import org.jboss.test.JBossJMSTestCase;

/**
 * Concurrent delivery tests
 *
 * @author <a href="mailto:richard.achmatowicz@jboss.org">Richard Achmatowicz</a>
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version <tt>$Revision: 105321 $</tt>
 */
public class ConcurrentDeliveryUnitTestCase extends JBossJMSTestCase
{
   static String QUEUE_FACTORY = "ConnectionFactory";

   QueueConnection queueConnection;

   int completed = 0;
   boolean inDelivery = false;
   boolean concurrent = false;
   
   public ConcurrentDeliveryUnitTestCase(String name) throws Exception
   {
      super(name);
   }

   public void testConcurrentDelivery() throws Exception
   {
      connect();
      try
      {
         MyMessageListener messageListener = new MyMessageListener(); 
         
         QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         TemporaryQueue queue1 = session.createTemporaryQueue();
         QueueSender sender1 = session.createSender(queue1);
         QueueReceiver receiver1 = session.createReceiver(queue1);
         receiver1.setMessageListener(messageListener);
         TemporaryQueue queue2 = session.createTemporaryQueue();
         QueueSender sender2 = session.createSender(queue2);
         QueueReceiver receiver2 = session.createReceiver(queue2);
         receiver2.setMessageListener(messageListener);
         Message message = session.createMessage();
         queueConnection.start();

         sender1.send(message);
         sender2.send(message);

         synchronized (messageListener)
         {
            while (completed < 2)
            {
               getLog().debug("Waiting for completion " + completed);
               messageListener.wait();
            }
         }
         getLog().debug("Completed");
         
         if (concurrent)
            fail("Concurrent delivery");
      }
      finally
      {
         disconnect();
      }
   }

   protected void connect() throws Exception
   {
      Context context = getInitialContext();
      QueueConnectionFactory queueFactory = (QueueConnectionFactory) context.lookup(QUEUE_FACTORY);
      queueConnection = queueFactory.createQueueConnection();

      getLog().debug("Connection established.");
   }

   protected void disconnect()
   {
      try
      {
         if (queueConnection != null)
            queueConnection.close();
      }
      catch (Throwable ignored)
      {
         getLog().warn("Ignored", ignored);
      }

      getLog().debug("Connection closed.");
   }

   public class MyMessageListener implements MessageListener
   {
      public void onMessage(Message message)
      {
         synchronized (this)
         {
            if (inDelivery)
               concurrent = true;
            inDelivery = true;
            getLog().debug("In delivery " + message);
         }
         
         try
         {
            Thread.sleep(10000);
         }
         catch (Throwable ignored)
         {
            getLog().warn("Ignored ", ignored);
         }
         
         synchronized (this)
         {
            inDelivery = false;
            ++completed;
            notifyAll();
            getLog().debug("Completed " + message);
         }
      }
   }
}

