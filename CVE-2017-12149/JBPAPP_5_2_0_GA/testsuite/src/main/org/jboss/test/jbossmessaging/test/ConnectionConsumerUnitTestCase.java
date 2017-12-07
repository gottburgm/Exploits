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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.InitialContext;

import org.jboss.test.JBossJMSTestCase;
import org.jboss.test.jbossmessaging.MockServerSessionPool;

/**
 * ConnectionConsumerUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 105321 $
 */
public class ConnectionConsumerUnitTestCase extends JBossJMSTestCase
{
   /**
    * Create a new ConnectionConsumerUnitTestCase.
    * 
    * @param name the test name
    */
   public ConnectionConsumerUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testConnectionConsumerWrongTemporaryDestination() throws Exception
   {
      InitialContext ctx = getInitialContext();
      ConnectionFactory factory = (ConnectionFactory) ctx.lookup("ConnectionFactory");
      Queue queue = null;
      Connection connection = factory.createConnection();
      try
      {
         Session s = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         queue = s.createTemporaryQueue();
         Connection connection2 = factory.createConnection();
         try
         {
            connection2.createConnectionConsumer(queue, "", MockServerSessionPool.getServerSessionPool(), 1);
            fail("Expected an error listening to a temporary destination from a different connection");
         }
         catch (JMSException expected)
         {
            log.debug("Got the expected jms exception", expected);
         }
         finally
         {
            connection2.close();
         }
      }
      finally
      {
         connection.close();
      }
   }
   
   public void testQueueConnectionConsumerWrongTemporaryDestination() throws Exception
   {
      InitialContext ctx = getInitialContext();
      QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup("ConnectionFactory");
      Queue queue = null;
      QueueConnection connection = factory.createQueueConnection();
      try
      {
         QueueSession s = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         queue = s.createTemporaryQueue();
         QueueConnection connection2 = factory.createQueueConnection();
         try
         {
            connection2.createConnectionConsumer(queue, "", MockServerSessionPool.getServerSessionPool(), 1);
            fail("Expected an error listening to a temporary destination from a different connection");
         }
         catch (JMSException expected)
         {
            log.debug("Got the expected jms exception", expected);
         }
         finally
         {
            connection2.close();
         }
      }
      finally
      {
         connection.close();
      }
   }
   
   public void testTopicConnectionConsumerWrongTemporaryDestination() throws Exception
   {
      InitialContext ctx = getInitialContext();
      TopicConnectionFactory factory = (TopicConnectionFactory) ctx.lookup("ConnectionFactory");
      Topic topic = null;
      TopicConnection connection = factory.createTopicConnection();
      try
      {
         TopicSession s = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
         topic = s.createTemporaryTopic();
         TopicConnection connection2 = factory.createTopicConnection();
         try
         {
            connection2.createConnectionConsumer(topic, "", MockServerSessionPool.getServerSessionPool(), 1);
            fail("Expected an error listening to a temporary destination from a different connection");
         }
         catch (JMSException expected)
         {
            log.debug("Got the expected jms exception", expected);
         }
         finally
         {
            connection2.close();
         }
      }
      finally
      {
         connection.close();
      }
   }
}
