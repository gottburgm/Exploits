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

import java.io.Serializable;
import java.util.HashMap;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.InitialContext;

import org.jboss.test.JBossJMSTestCase;

/**
 * A stress test for an impatient receiver
 *
 * @author <a href="mailto:richard.achmatowicz@jboss.com">Richard Achmatowicz</a>
 * @author    <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version   $Revision: 111291 $
 */
public class ReceiveNackClientStressTestCase extends JBossJMSTestCase implements ExceptionListener
{
   private static final String IMPATIENT = "/queue/Impatient";

   protected QueueConnection queueConnection;
   
   public ReceiveNackClientStressTestCase(String name) throws Exception
   {
      super(name);
   }

   public void onException(JMSException e)
   {
      log.error("Error: ", e);
      try
      {
         queueConnection.close();
      }
      catch (Exception ignored)
      {
      }
   }

   private void drainQueue(String name) throws Exception
   {
      InitialContext context = getInitialContext();

      QueueSession session = queueConnection.createQueueSession(false,
            Session.AUTO_ACKNOWLEDGE);
      Queue queue = (Queue) context.lookup(name);

      QueueReceiver receiver = session.createReceiver(queue);
      queueConnection.start();
      Message message = receiver.receive(50);
      int c = 0;
      while (message != null)
      {
         message = receiver.receive(50);
         c++;
      }

      if (c != 0)
         getLog().debug("  Drained " + c + " messages from the queue");
      session.close();
      queueConnection.stop();

   }

   public void testImpatient() throws Exception
   {
      int target = getIterationCount();
      createQueue("Impatient");
      try
      {
         InitialContext context = getInitialContext();
         QueueConnectionFactory queueFactory = (QueueConnectionFactory) context
               .lookup("ConnectionFactory");
         Queue queue = (Queue) context.lookup(IMPATIENT);
         queueConnection = queueFactory.createQueueConnection();
         drainQueue(IMPATIENT);
         try
         {
            QueueSession session = queueConnection.createQueueSession(false,
                  Session.AUTO_ACKNOWLEDGE);
            QueueSender sender = session.createSender(queue);
            QueueReceiver receiver = session.createReceiver(queue);
            Serializable payload = new HashMap();
            Message message = session.createObjectMessage(payload);
            queueConnection.start();
            int count = 0;
            int sendCount = 0;
            while (count < target)
            {
               if (sendCount <= target)
               {
                  for (int i = 0; i < 10 && ++sendCount <= target; ++i)
                     sender.send(message);
               }
               if (receiver.receive(100) != null)
                  ++count;
            }
         }
         finally
         {
            drainQueue(IMPATIENT);
            queueConnection.close();
         }
      }
      finally
      {
         undeployDestinations();
      }
   }
}
