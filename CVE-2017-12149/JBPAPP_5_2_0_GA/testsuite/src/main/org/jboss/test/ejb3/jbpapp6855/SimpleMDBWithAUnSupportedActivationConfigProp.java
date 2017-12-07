/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.test.ejb3.jbpapp6855;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;

import org.jboss.logging.Logger;

/**
 * User: Jaikiran Pai
 */
@MessageDriven(activationConfig =
        {
                @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
                @ActivationConfigProperty(propertyName = "destination", propertyValue = SimpleMDBWithAUnSupportedActivationConfigProp.QUEUE_JNDI_NAME),
                // Unsupported activation config property. Should *not* result in deployment failure
                @ActivationConfigProperty(propertyName = "foo", propertyValue = "bar")
        })
public class SimpleMDBWithAUnSupportedActivationConfigProp implements MessageListener
{
   public static final String QUEUE_JNDI_NAME = "queue/auto-create/jbpapp-6855";

   private static Logger logger = Logger.getLogger(SimpleMDBWithAUnSupportedActivationConfigProp.class);

   public static final String REPLY = "You have successfully established contact with the MDB";

   @Resource(mappedName="java:/ConnectionFactory")
   private QueueConnectionFactory qFactory;

   @Override
   public void onMessage(Message message)
   {
      // do nothing
      logger.info("Received message " + message);
      try
      {
         sendReply((Queue) message.getJMSReplyTo(), message.getJMSMessageID());
      }
      catch (JMSException e)
      {
         throw new RuntimeException(e);
      }
   }

   private void sendReply(Queue destination, String jmsMessageID) throws JMSException
   {
      if (destination == null) {
         return;
      }
      QueueConnection conn = qFactory.createQueueConnection();
      try
      {
         QueueSession session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
         QueueSender sender = session.createSender(destination);
         TextMessage message = session.createTextMessage(REPLY);
         message.setJMSCorrelationID(jmsMessageID);
         sender.send(message);
      }
      finally
      {
         conn.close();
      }
   }
}
