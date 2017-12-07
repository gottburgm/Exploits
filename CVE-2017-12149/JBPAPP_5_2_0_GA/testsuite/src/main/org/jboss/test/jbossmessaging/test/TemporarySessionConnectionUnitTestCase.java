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

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.naming.Context;

import org.jboss.test.JBossJMSTestCase;

/**
 * Tests for temporaries and session/connection consumer construction
 *
 * @author <a href="mailto:richard.achmatowicz@jboss.com">Richard Achmatowicz</a>
 * @author <a href="mailto:adrian@jboss.org>Adrian Brock</a>
 * @version <tt>$Revision: 105321 $</tt>
 */
public class TemporarySessionConnectionUnitTestCase extends JBossJMSTestCase
{
   static String QUEUE_FACTORY = "ConnectionFactory";

   QueueConnection queueConnection;

   public TemporarySessionConnectionUnitTestCase(String name) throws Exception
   {
      super(name);
   }

   public void testTemporaryDifferentSession() throws Exception
   {
      connect();
      try
      {
         QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         TemporaryQueue temp = session.createTemporaryQueue(); 
         session.createConsumer(temp);
         session.close();
         session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         session.createConsumer(temp).close();
      }
      finally
      {
         disconnect();
      }
   }

   public void testTemporaryDifferentConnection() throws Exception
   {
      connect();
      try
      {
         QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         TemporaryQueue temp = session.createTemporaryQueue(); 
         session.createConsumer(temp);
         disconnect();
         connect();
         session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
         try
         {
            session.createConsumer(temp).close();
            fail("Should not be able to consume a temporary on different connection");
         }
         catch (JMSException expected)
         {
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
}
