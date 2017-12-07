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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

import org.jboss.logging.Logger;

/**
 * JmsMessageProducer.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class JmsMessageProducer implements MessageProducer
{
   private static final Logger log = Logger.getLogger(JmsMessageConsumer.class);

   /** The wrapped message producer */
   MessageProducer producer;
   
   /** The session for this consumer */
   JmsSession session;
   
   /** Whether trace is enabled */
   private boolean trace = log.isTraceEnabled();

   /**
    * Create a new wrapper
    * 
    * @param producer the producer
    * @param session the session
    */
   public JmsMessageProducer(MessageProducer producer, JmsSession session)
   {
      this.producer = producer;
      this.session = session;
      
      if (trace)
         log.trace("new JmsMessageProducer " + this + " producer=" + producer + " session=" + session);
   }

   public void close() throws JMSException
   {
      if (trace)
         log.trace("close " + this);
      try
      {
         closeProducer();
      }
      finally
      {
         session.removeProducer(this);
      }
   }

   public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive)
         throws JMSException
   {
      session.lock();
      try
      {
         if (trace)
            log.trace("send " + this + " destination=" + destination + " message=" + message + " deliveryMode=" + deliveryMode + " priority=" + priority + " ttl=" + timeToLive);
         checkState();
         producer.send(destination, message, deliveryMode, priority, timeToLive);
         if (trace)
            log.trace("sent " + this + " result=" + message);
      }
      finally
      {
         session.unlock();
      }
   }

   public void send(Destination destination, Message message) throws JMSException
   {
      session.lock();
      try
      {
         if (trace)
            log.trace("send " + this + " destination=" + destination + " message=" + message);
         checkState();
         producer.send(destination, message);
         if (trace)
            log.trace("sent " + this + " result=" + message);
      }
      finally
      {
         session.unlock();
      }
   }

   public void send(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException
   {
      session.lock();
      try
      {
         if (trace)
            log.trace("send " + this + " message=" + message + " deliveryMode=" + deliveryMode + " priority=" + priority + " ttl=" + timeToLive);
         checkState();
         producer.send(message, deliveryMode, priority, timeToLive);
         if (trace)
            log.trace("sent " + this + " result=" + message);
      }
      finally
      {
         session.unlock();
      }
   }

   public void send(Message message) throws JMSException
   {
      session.lock();
      try
      {
         if (trace)
            log.trace("send " + this + " message=" + message);
         checkState();
         producer.send(message);
         if (trace)
            log.trace("sent " + this + " result=" + message);
      }
      finally
      {
         session.unlock();
      }
   }

   public int getDeliveryMode() throws JMSException
   {
      return producer.getDeliveryMode();
   }

   public Destination getDestination() throws JMSException
   {
      return producer.getDestination();
   }

   public boolean getDisableMessageID() throws JMSException
   {
      return producer.getDisableMessageID();
   }

   public boolean getDisableMessageTimestamp() throws JMSException
   {
      return producer.getDisableMessageTimestamp();
   }

   public int getPriority() throws JMSException
   {
      return producer.getPriority();
   }

   public long getTimeToLive() throws JMSException
   {
      return producer.getTimeToLive();
   }

   public void setDeliveryMode(int deliveryMode) throws JMSException
   {
      producer.setDeliveryMode(deliveryMode);
   }

   public void setDisableMessageID(boolean value) throws JMSException
   {
      producer.setDisableMessageID(value);
   }

   public void setDisableMessageTimestamp(boolean value) throws JMSException
   {
      producer.setDisableMessageTimestamp(value);
   }

   public void setPriority(int defaultPriority) throws JMSException
   {
      producer.setPriority(defaultPriority);
   }

   public void setTimeToLive(long timeToLive) throws JMSException
   {
      producer.setTimeToLive(timeToLive);
   }

   void checkState() throws JMSException
   {
      session.checkTransactionActive();
   }

   void closeProducer() throws JMSException
   {
      producer.close();
   }
}
