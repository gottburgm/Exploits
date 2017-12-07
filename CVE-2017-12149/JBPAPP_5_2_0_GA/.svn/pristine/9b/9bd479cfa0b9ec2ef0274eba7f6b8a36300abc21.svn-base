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
package org.jboss.test.ejb3.common;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;

import org.jboss.logging.Logger;

/**
 * Sets up a MDB which can reply to messages via sendReply.
 * 
 * Make sure that postConstruct are preDestroy are properly executed when overridden.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public abstract class AbstractReplierMDB implements MessageListener
{
   private static final Logger log = Logger.getLogger(AbstractReplierMDB.class);
   
   @Resource(mappedName="java:/ConnectionFactory")
   private QueueConnectionFactory factory;
   
   private QueueConnection connection;
   private QueueSession session;
   private QueueSender sender;
   
   /**
    * Send an object message to the specified destination.
    * 
    * @param destination
    * @param obj
    * @throws JMSException
    */
   protected void sendReply(Destination destination, Serializable obj) throws JMSException
   {
      Message message = session.createObjectMessage(obj);
      sender.send(destination, message);
   }
   
   @PostConstruct
   public void postConstruct()
   {
      try
      {
         connection = factory.createQueueConnection();
         session = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
         sender = session.createSender(null);
      }
      catch(JMSException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   @PreDestroy
   public void preDestroy()
   {
      try
      {
         if(sender != null)
            sender.close();
         if(session != null)
            session.close();
         if(connection != null)
            connection.close();
      }
      catch(JMSException e)
      {
         throw new RuntimeException(e);
      }
   }
}
