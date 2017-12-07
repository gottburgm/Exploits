/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.adapter.jms;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.jboss.logging.Logger;

/**
 * A wrapper for a message consumer
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71790 $
 */
public class JmsMessageConsumer implements MessageConsumer
{
   private static final Logger log = Logger.getLogger(JmsMessageConsumer.class);

   /** The wrapped message consumer */
   MessageConsumer consumer;
   
   /** The session for this consumer */
   JmsSession session;
   
   /** Whether trace is enabled */
   private boolean trace = log.isTraceEnabled();

   /**
    * Create a new wrapper
    * 
    * @param consumer the consumer
    * @param session the session
    */
   public JmsMessageConsumer(MessageConsumer consumer, JmsSession session)
   {
      this.consumer = consumer;
      this.session = session;
      
      if (trace)
         log.trace("new JmsMessageConsumer " + this + " consumer=" + consumer + " session=" + session);
   }

   public void close() throws JMSException
   {
      if (trace)
         log.trace("close " + this);
      try
      {
         closeConsumer();
      }
      finally
      {
         session.removeConsumer(this);
      }
   }

   void checkState() throws JMSException
   {
      session.checkTransactionActive();
   }
   
   public MessageListener getMessageListener() throws JMSException
   {
      checkState();
      session.checkStrict();
      return consumer.getMessageListener();
   }
   
   public String getMessageSelector() throws JMSException
   {
      checkState();
      return consumer.getMessageSelector();
   }
   
   public Message receive() throws JMSException
   {
      session.lock();
      try
      {
         if (trace)
            log.trace("receive " + this);
         checkState();
         Message message = consumer.receive();
         if (trace)
            log.trace("received " + this + " result=" + message);
         if (message == null)
            return null;
         else
            return wrapMessage(message);
      }
      finally
      {
         session.unlock();
      }
   }

   public Message receive(long timeout) throws JMSException
   {
      session.lock();
      try
      {
         if (trace)
            log.trace("receive " + this + " timeout=" + timeout);
         checkState();
         Message message = consumer.receive(timeout);
         if (trace)
            log.trace("received " + this + " result=" + message);
         if (message == null)
            return null;
         else
            return wrapMessage(message);
      }
      finally
      {
         session.unlock();
      }
   }

   public Message receiveNoWait() throws JMSException
   {
      session.lock();
      try
      {
         if (trace)
            log.trace("receiveNoWait " + this);
         checkState();
         Message message = consumer.receiveNoWait();
         if (trace)
            log.trace("received " + this + " result=" + message);
         if (message == null)
            return null;
         else
            return wrapMessage(message);
      }
      finally
      {
         session.unlock();
      }
   }
   
   public void setMessageListener(MessageListener listener) throws JMSException
   {
      session.lock();
      try
      {
         checkState();
         session.checkStrict();
         if (listener == null)
            consumer.setMessageListener(null);
         else
            consumer.setMessageListener(wrapMessageListener(listener));
      }
      finally
      {
         session.unlock();
      }
   }

   void closeConsumer() throws JMSException
   {
      consumer.close();
   }
   
   Message wrapMessage(Message message)
   {
      if (message instanceof BytesMessage)
         return new JmsBytesMessage((BytesMessage) message, session);
      else if (message instanceof MapMessage)
         return new JmsMapMessage((MapMessage) message, session);
      else if (message instanceof ObjectMessage)
         return new JmsObjectMessage((ObjectMessage) message, session);
      else if (message instanceof StreamMessage)
         return new JmsStreamMessage((StreamMessage) message, session);
      else if (message instanceof TextMessage)
         return new JmsTextMessage((TextMessage) message, session);
      return new JmsMessage(message, session);
   }
   
   MessageListener wrapMessageListener(MessageListener listener)
   {
      return new JmsMessageListener(listener, this);
   }
}
