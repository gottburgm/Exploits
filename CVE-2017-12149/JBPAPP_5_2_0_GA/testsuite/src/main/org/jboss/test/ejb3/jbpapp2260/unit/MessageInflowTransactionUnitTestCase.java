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
package org.jboss.test.ejb3.jbpapp2260.unit;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

import junit.framework.Test;

import org.jboss.logging.Logger;
import org.jboss.test.ejb3.common.EJB3TestCase;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class MessageInflowTransactionUnitTestCase extends EJB3TestCase
{
   private static final Logger log = Logger.getLogger(MessageInflowTransactionUnitTestCase.class);
   
   private static final String QUEUE_NAME = "jbpapp2260";
   
   /**
    * Overall timeout to wait for anything to happen in milliseconds.
    */
   private static final long TIMEOUT = 30000;
   
   public MessageInflowTransactionUnitTestCase(String name)
   {
      super(name);
   }

   public Object sendMessage(String queueName, String text) throws Exception
   {
      return sendMessage(queueName, text, 1);
   }
   
   public Object sendMessage(String queueName, String text, int redeliveryLimit) throws Exception
   {
      Queue queue = lookup("queue/" + queueName, Queue.class);
      QueueConnectionFactory factory = getQueueConnectionFactory();
      QueueConnection conn = factory.createQueueConnection();
      try
      {
         QueueSession session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
         TemporaryQueue replyQueue = session.createTemporaryQueue();
         QueueReceiver receiver = session.createReceiver(replyQueue);
         QueueSender sender = session.createSender(queue);
         conn.start();
         try
         {
            TextMessage msg = session.createTextMessage(text);
            msg.setJMSReplyTo(replyQueue);
            msg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
            msg.setIntProperty("JMS_JBOSS_REDELIVERY_LIMIT", redeliveryLimit);
      
            sender.send(msg);
            
            Message reply = receiver.receive(TIMEOUT);
            assertNotNull(reply);
            
            Serializable obj = ((ObjectMessage) reply).getObject();
            if(obj instanceof Exception)
               throw (Exception) obj;
            else
               return obj;
         }
         finally
         {
            conn.stop();
            sender.close();
            receiver.close();
            session.close();
         }
      }
      finally
      {
         conn.close();
      }
   }
   
   public void testDefault() throws Exception
   {
      Map<String, AtomicInteger> result = (Map<String, AtomicInteger>) sendMessage(QUEUE_NAME, "testDefault");
      int base = result.size();
      result = (Map<String, AtomicInteger>) sendMessage(QUEUE_NAME, "testDefault");
      assertEquals(base + 1, result.size());
      result = (Map<String, AtomicInteger>) sendMessage(QUEUE_NAME, "testDefault");
      assertEquals(base + 2, result.size());
   }
   
   public void testStress() throws Exception
   {
      Map<String, AtomicInteger> result = (Map<String, AtomicInteger>) sendMessage(QUEUE_NAME, "testDefault");
      int base = result.size();
      
      Queue queue = lookup("queue/" + QUEUE_NAME, Queue.class);
      QueueConnectionFactory factory = getQueueConnectionFactory();
      QueueConnection conn = factory.createQueueConnection();
      try
      {
         QueueSession session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
         TemporaryQueue replyQueue = session.createTemporaryQueue();
         QueueReceiver receiver = session.createReceiver(replyQueue);
         QueueSender sender = session.createSender(queue);
         conn.start();
         try
         {
            log.info("Firing messages...");
            
            for(int i = 0; i < 100; i++)
            {
               TextMessage msg = session.createTextMessage("message #" + i);
               msg.setJMSReplyTo(replyQueue);
               msg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
               msg.setIntProperty("JMS_JBOSS_REDELIVERY_LIMIT", 1);
         
               sender.send(msg);
            }
            
            log.info("Waiting for replies...");
            
            for(int i = 0; i < 100; i++)
            {
               Message reply = receiver.receive(TIMEOUT);
               assertNotNull(reply);
               
               Serializable obj = ((ObjectMessage) reply).getObject();
               if(obj instanceof Exception)
                  throw (Exception) obj;
               
               result = (Map<String, AtomicInteger>) obj;
               assertEquals("Message was not delivered in unique transaction", base + 1 + i, result.size());
            }
            
            log.info("Done");
         }
         finally
         {
            conn.stop();
            sender.close();
            receiver.close();
            session.close();
         }
      }
      finally
      {
         conn.close();
      }
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(MessageInflowTransactionUnitTestCase.class, "jbpapp2260.jar");
   }
}
