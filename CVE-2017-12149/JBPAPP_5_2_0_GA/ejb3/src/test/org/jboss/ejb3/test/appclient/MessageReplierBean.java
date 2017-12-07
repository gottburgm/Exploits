/*
  * JBoss, Home of Professional Open Source
  * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
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
package org.jboss.ejb3.test.appclient;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.jboss.logging.Logger;

/**
 * This message bean will just reply the message.
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: 85945 $
 */
@MessageDriven(activationConfig = {
   @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
   @ActivationConfigProperty(propertyName="destination", propertyValue="queue/messageReplier")
})
public class MessageReplierBean implements MessageListener
{
   private static final Logger log = Logger.getLogger(MessageReplierBean.class);
   
   @Resource(mappedName="java:ConnectionFactory")
   private ConnectionFactory connectionFactory;
   
   public void onMessage(Message message)
   {
      try
      {
         if(message.getJMSReplyTo() != null)
         {
            Destination destination = message.getJMSReplyTo();
            
            Connection conn = connectionFactory.createConnection();
            try
            {
               Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
               MessageProducer producer = session.createProducer(destination);
               producer.send(destination, message);
               producer.close();
               session.close();
            }
            finally
            {
               conn.close();
            }
         }
         else
            log.info("no reply to specified");
      }
      catch(JMSException e)
      {
         throw new EJBException(e);
      }
   }
}
