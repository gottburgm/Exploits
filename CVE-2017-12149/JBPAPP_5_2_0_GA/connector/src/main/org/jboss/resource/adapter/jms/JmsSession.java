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

import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.resource.spi.ConnectionEvent;

import org.jboss.logging.Logger;

/**
 * Adapts the JMS QueueSession and TopicSession API to a JmsManagedConnection.
 * 
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman </a>.
 * @author <a href="mailto:jason@planet57.com">Jason Dillon </a>.
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 111797 $
 */
public class JmsSession implements Session, QueueSession, TopicSession
{
   private static final Logger log = Logger.getLogger(JmsSession.class);

   /** The managed connection for this session. */
   private volatile JmsManagedConnection mc;
   private JmsManagedConnection lockedMC;
   private int lockCount;

   /** The connection request info */
   private JmsConnectionRequestInfo info;

   /** The session factory for this session */
   private JmsSessionFactoryImpl sf;
   
   /** The message consumers */
   private HashSet consumers = new HashSet();
   
   /** The message producers */
   private HashSet producers = new HashSet();
   
   /** Whether trace is enabled */
   private boolean trace = log.isTraceEnabled();
   
   /**
    * Construct a <tt>JmsSession</tt>.
    * 
    * @param mc The managed connection for this session.
    */
   public JmsSession(final JmsManagedConnection mc, JmsConnectionRequestInfo info)
   {
      this.mc = mc;
      this.lockedMC = null;
      this.lockCount = 0;
      this.info = info;
      if (trace)
         log.trace("new JmsSession " + this + " mc=" + mc + " cri=" + info);
   }

   public void setJmsSessionFactory(JmsSessionFactoryImpl sf)
   {
      this.sf = sf;
   }
   
   protected void lock() throws JMSException
   {
      JmsManagedConnection mc = this.mc;
      if (mc != null)
      {
         mc.tryLock();

         if (lockedMC == null)
            lockedMC = mc;

         lockCount++;
      }
      else
         throw new IllegalStateException("Connection is not associated with a managed connection. " + this);
   }

   protected void unlock()
   {
      JmsManagedConnection mc = this.lockedMC;
      if (--lockCount == 0)
         lockedMC = null;

      if (mc != null)
         mc.unlock();
   }
   
   /**
    * Ensure that the session is opened.
    * 
    * @return The session
    * 
    * @throws IllegalStateException The session is closed
    */
   Session getSession() throws JMSException
   {
      // ensure that the connection is opened
      if (mc == null)
         throw new IllegalStateException("The session is closed");
      
      checkTransactionActive();
      
      Session session = mc.getSession();
      if (trace)
         log.trace("getSession " + session + " for " + this);
      return session;
   }

   void checkTransactionActive() throws IllegalStateException
   {
      if (sf != null)
         sf.checkTransactionActive();
   }
   
   // ---- Session API

   public BytesMessage createBytesMessage() throws JMSException
   {
      Session session = getSession();
      if (trace)
         log.trace("createBytesMessage" + session);
      return session.createBytesMessage();
   }

   public MapMessage createMapMessage() throws JMSException
   {
      Session session = getSession();
      if (trace)
         log.trace("createMapMessage" + session);
      return session.createMapMessage();
   }

   public Message createMessage() throws JMSException
   {
      Session session = getSession();
      if (trace)
         log.trace("createMessage" + session);
      return session.createMessage();
   }

   public ObjectMessage createObjectMessage() throws JMSException
   {
      Session session = getSession();
      if (trace)
         log.trace("createObjectMessage" + session);
      return session.createObjectMessage();
   }

   public ObjectMessage createObjectMessage(Serializable object) throws JMSException
   {
      Session session = getSession();
      if (trace)
         log.trace("createObjectMessage(Object)" + session);
      return session.createObjectMessage(object);
   }

   public StreamMessage createStreamMessage() throws JMSException
   {
      Session session = getSession();
      if (trace)
         log.trace("createStreamMessage" + session);
      return session.createStreamMessage();
   }

   public TextMessage createTextMessage() throws JMSException
   {
      Session session = getSession();
      if (trace)
         log.trace("createTextMessage" + session);
      return session.createTextMessage();
   }

   public TextMessage createTextMessage(String string) throws JMSException
   {
      Session session = getSession();
      if (trace)
         log.trace("createTextMessage(String)" + session);
      return session.createTextMessage(string);
   }


   public boolean getTransacted() throws JMSException
   {
      getSession(); // check closed
      return info.isTransacted();
   }

   /**
    * Always throws an Exception.
    * 
    * @throws IllegalStateException Method not allowed.
    */
   public MessageListener getMessageListener() throws JMSException
   {
      throw new IllegalStateException("Method not allowed");
   }

   /**
    * Always throws an Exception.
    * 
    * @throws IllegalStateException Method not allowed.
    */
   public void setMessageListener(MessageListener listener) throws JMSException
   {
      throw new IllegalStateException("Method not allowed");
   }

   /**
    * Always throws an Error.
    * 
    * @throws Error Method not allowed.
    */
   public void run()
   {
      // should this really throw an Error?
      throw new Error("Method not allowed");
   }

   /**
    * Closes the session. Sends a ConnectionEvent.CONNECTION_CLOSED to the
    * managed connection.
    * 
    * @throws JMSException Failed to close session.
    */
   public void close() throws JMSException
   {
      sf.closeSession(this);
      closeSession();
   }

   // FIXME - is this really OK, probably not
   public void commit() throws JMSException
   {
      lock();
      try
      {
         Session session = getSession();
         if (info.isTransacted() == false)
            throw new IllegalStateException("Session is not transacted");
         if (trace)
            log.trace("Commit session " + this);
         session.commit();
      }
      finally
      {
         unlock();
      }
   }

   public void rollback() throws JMSException
   {
      lock();
      try
      {
         Session session = getSession();
         if (info.isTransacted() == false)
            throw new IllegalStateException("Session is not transacted");
         if (trace)
            log.trace("Rollback session " + this);
         session.rollback();
      }
      finally
      {
         unlock();
      }
   }

   public void recover() throws JMSException
   {
      lock();
      try
      {
         Session session = getSession();
         if (info.isTransacted())
            throw new IllegalStateException("Session is transacted");
         if (trace)
            log.trace("Recover session " + this);
         session.recover();
      }
      finally
      {
         unlock();
      }
   }

   // --- TopicSession API

   public Topic createTopic(String topicName) throws JMSException
   {
      if(info.getType() == JmsConnectionFactory.QUEUE)
      {
         throw new IllegalStateException("Cannot create topic for javax.jms.QueueSession");         
      }

      Session session = getSession();
      if (trace)
         log.trace("createTopic " + session + " topicName=" + topicName);
      Topic result = session.createTopic(topicName);
      if (trace)
         log.trace("createdTopic " + session + " topic=" + result);
      return result;
   }

   public TopicSubscriber createSubscriber(Topic topic) throws JMSException
   {
      lock();
      try
      {
         TopicSession session = getTopicSession();
         if (trace)
            log.trace("createSubscriber " + session + " topic=" + topic);
         TopicSubscriber result = session.createSubscriber(topic);
         result = new JmsTopicSubscriber(result, this);
         if (trace)
            log.trace("createdSubscriber " + session + " JmsTopicSubscriber=" + result);
         addConsumer(result);
         return result;
      }
      finally
      {
         unlock();
      }
   }

   public TopicSubscriber createSubscriber(Topic topic, String messageSelector, boolean noLocal) throws JMSException
   {
      lock();
      try
      {
         TopicSession session = getTopicSession();
         if (trace)
            log.trace("createSubscriber " + session + " topic=" + topic + " selector=" + messageSelector + " noLocal=" + noLocal);
         TopicSubscriber result = session.createSubscriber(topic, messageSelector, noLocal);
         result = new JmsTopicSubscriber(result, this);
         if (trace)
            log.trace("createdSubscriber " + session + " JmsTopicSubscriber=" + result);
         addConsumer(result);
         return result;
      }
      finally
      {
         unlock();
      }
   }

   public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException
   {
      if(info.getType() == JmsConnectionFactory.QUEUE)
      {
         throw new IllegalStateException("Cannot create durable subscriber from javax.jms.QueueSession");         
      }
      
      lock();
      try
      {
         Session session = getSession();
         if (trace)
            log.trace("createDurableSubscriber " + session + " topic=" + topic + " name=" + name);
         TopicSubscriber result = session.createDurableSubscriber(topic, name);
         result = new JmsTopicSubscriber(result, this);
         if (trace)
            log.trace("createdDurableSubscriber " + session + " JmsTopicSubscriber=" + result);
         addConsumer(result);
         return result;
      }
      finally
      {
         unlock();
      }
   }

   public TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector, boolean noLocal)
         throws JMSException
   {
      lock();
      try
      {
         Session session = getSession();
         if (trace)
            log.trace("createDurableSubscriber " + session + " topic=" + topic + " name=" + name + " selector=" + messageSelector + " noLocal=" + noLocal);
         TopicSubscriber result = session.createDurableSubscriber(topic, name, messageSelector, noLocal);
         result = new JmsTopicSubscriber(result, this);
         if (trace)
            log.trace("createdDurableSubscriber " + session + " JmsTopicSubscriber=" + result);
         addConsumer(result);
         return result;
      }
      finally
      {
         unlock();
      }
   }

   public TopicPublisher createPublisher(Topic topic) throws JMSException
   {
      lock();
      try
      {
         TopicSession session = getTopicSession();
         if (trace)
            log.trace("createPublisher " + session + " topic=" + topic);
         TopicPublisher result = session.createPublisher(topic);
         result = new JmsTopicPublisher(result, this);
         if (trace)
            log.trace("createdPublisher " + session + " publisher=" + result);
         addProducer(result);
         return result;
      }
      finally
      {
         unlock();
      }
   }

   public TemporaryTopic createTemporaryTopic() throws JMSException
   {
      if(info.getType() == JmsConnectionFactory.QUEUE)
      {
         throw new IllegalStateException("Cannot create temporary topic for javax.jms.QueueSession");         
      }
      
      lock();
      try
      {
         Session session = getSession();
         if (trace)
            log.trace("createTemporaryTopic " + session);
         TemporaryTopic temp = session.createTemporaryTopic();
         if (trace)
            log.trace("createdTemporaryTopic " + session + " temp=" + temp);
         sf.addTemporaryTopic(temp);
         return temp;
      }
      finally
      {
         unlock();
      }
   }

   public void unsubscribe(String name) throws JMSException
   {
      if(info.getType() == JmsConnectionFactory.QUEUE)
      {
         throw new IllegalStateException("Cannot unsubscribe for javax.jms.QueueSession");         
      }

      lock();
      try
      {
         Session session = getSession();
         if (trace)
            log.trace("unsubscribe " + session + " name=" + name);
         session.unsubscribe(name);
      }
      finally
      {
         unlock();
      }
   }

   //--- QueueSession API

   public QueueBrowser createBrowser(Queue queue) throws JMSException
   {
      
      if(info.getType() == JmsConnectionFactory.TOPIC)
      {
         throw new IllegalStateException("Cannot create browser for javax.jms.TopicSession");
         
      }

      Session session = getSession();
      if (trace)
         log.trace("createBrowser " + session + " queue=" + queue);
      QueueBrowser result = session.createBrowser(queue);
      if (trace)
         log.trace("createdBrowser " + session + " browser=" + result);
      return result;
   }

   public QueueBrowser createBrowser(Queue queue, String messageSelector) throws JMSException
   {
      Session session = getSession();
      if (trace)
         log.trace("createBrowser " + session + " queue=" + queue + " selector=" + messageSelector);
      QueueBrowser result = session.createBrowser(queue, messageSelector);
      if (trace)
         log.trace("createdBrowser " + session + " browser=" + result);
      return result;
   }

   public Queue createQueue(String queueName) throws JMSException
   {
      if(info.getType() == JmsConnectionFactory.TOPIC)
      {
         throw new IllegalStateException("Cannot create browser or javax.jms.TopicSession");
         
      }

      Session session = getSession();
      if (trace)
         log.trace("createQueue " + session + " queueName=" + queueName);
      Queue result = session.createQueue(queueName);
      if (trace)
         log.trace("createdQueue " + session + " queue=" + result);
      return result;
   }

   public QueueReceiver createReceiver(Queue queue) throws JMSException
   {
      lock();
      try
      {
         QueueSession session = getQueueSession();
         if (trace)
            log.trace("createReceiver " + session + " queue=" + queue);
         QueueReceiver result = session.createReceiver(queue);
         result = new JmsQueueReceiver(result, this);
         if (trace)
            log.trace("createdReceiver " + session + " receiver=" + result);
         addConsumer(result);
         return result;
      }
      finally
      {
         unlock();
      }
   }

   public QueueReceiver createReceiver(Queue queue, String messageSelector) throws JMSException
   {
      lock();
      try
      {
         QueueSession session = getQueueSession();
         if (trace)
            log.trace("createReceiver " + session + " queue=" + queue + " selector=" + messageSelector);
         QueueReceiver result = session.createReceiver(queue, messageSelector);
         result = new JmsQueueReceiver(result, this);
         if (trace)
            log.trace("createdReceiver " + session + " receiver=" + result);
         addConsumer(result);
         return result;
      }
      finally
      {
         unlock();
      }
   }

   public QueueSender createSender(Queue queue) throws JMSException
   {
      lock();
      try
      {
         QueueSession session = getQueueSession();
         if (trace)
            log.trace("createSender " + session + " queue=" + queue);
         QueueSender result = session.createSender(queue);
         result = new JmsQueueSender(result, this);
         if (trace)
            log.trace("createdSender " + session + " sender=" + result);
         addProducer(result);
         return result;
      }
      finally
      {
         unlock();
      }
   }

   public TemporaryQueue createTemporaryQueue() throws JMSException
   {
      if(info.getType() == JmsConnectionFactory.TOPIC)
      {
         throw new IllegalStateException("Cannot create temporary queue for javax.jms.TopicSession");
         
      }

      lock();
      try
      {
         Session session = getSession();
         if (trace)
            log.trace("createTemporaryQueue " + session);
         TemporaryQueue temp = session.createTemporaryQueue();
         if (trace)
            log.trace("createdTemporaryQueue " + session + " temp=" + temp);
         sf.addTemporaryQueue(temp);
         return temp;
      }
      finally
      {
         unlock();
      }
   }

   // -- JMS 1.1

   public MessageConsumer createConsumer(Destination destination) throws JMSException
   {
      lock();
      try
      {
         Session session = getSession();
         if (trace)
            log.trace("createConsumer " + session + " dest=" + destination);
         MessageConsumer result = session.createConsumer(destination);
         result = new JmsMessageConsumer(result, this);
         if (trace)
            log.trace("createdConsumer " + session + " consumer=" + result);
         addConsumer(result);
         return result;
      }
      finally
      {
         unlock();
      }
   }

   public MessageConsumer createConsumer(Destination destination, String messageSelector) throws JMSException
   {
      lock();
      try
      {
         Session session = getSession();
         if (trace)
            log.trace("createConsumer " + session + " dest=" + destination + " messageSelector=" + messageSelector);
         MessageConsumer result = session.createConsumer(destination, messageSelector);
         result = new JmsMessageConsumer(result, this);
         if (trace)
            log.trace("createdConsumer " + session + " consumer=" + result);
         addConsumer(result);
         return result;
      }
      finally
      {
         unlock();
      }
   }

   public MessageConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal)
         throws JMSException
   {
      lock();
      try
      {
         Session session = getSession();
         if (trace)
            log.trace("createConsumer " + session + " dest=" + destination + " messageSelector=" + messageSelector + " noLocal=" + noLocal);
         MessageConsumer result = session.createConsumer(destination, messageSelector, noLocal);
         result = new JmsMessageConsumer(result, this);
         if (trace)
            log.trace("createdConsumer " + session + " consumer=" + result);
         addConsumer(result);
         return result;
      }
      finally
      {
         unlock();
      }
   }

   public MessageProducer createProducer(Destination destination) throws JMSException
   {
      lock();
      try
      {
         Session session = getSession();
         if (trace)
            log.trace("createProducer " + session + " dest=" + destination);
         MessageProducer result = getSession().createProducer(destination);
         result = new JmsMessageProducer(result, this);
         if (trace)
            log.trace("createdProducer " + session + " producer=" + result);
         addProducer(result);
         return result;
      }
      finally
      {
         unlock();
      }
   }

   public int getAcknowledgeMode() throws JMSException
   {
      getSession(); // check closed
      return info.getAcknowledgeMode();
   }

   // --- JmsManagedConnection api

   void setManagedConnection(final JmsManagedConnection mc)
   {
      if (this.mc != null)
         this.mc.removeHandle(this);
      this.mc = mc;
   }

   void destroy()
   {
      mc = null;
      lockedMC = null;
      lockCount = 0;
   }

   void start() throws JMSException
   {
      if (mc != null)
         mc.start();
   }

   void stop() throws JMSException
   {
      if (mc != null)
         mc.stop();
   }

   void checkStrict() throws JMSException
   {
      if (mc != null && mc.getManagedConnectionFactory().isStrict())
         throw new IllegalStateException(JmsSessionFactory.ISE);
   }
   
   void closeSession() throws JMSException
   {
      if (mc != null)
      {
         log.trace("Closing session");

         try
         {
            mc.stop();
         }
         catch (Throwable t)
         {
            log.trace("Error stopping managed connection", t);
         }
         
         synchronized (consumers)
         {
            for (Iterator i = consumers.iterator(); i.hasNext();)
            {
               JmsMessageConsumer consumer = (JmsMessageConsumer) i.next();
               try
               {
                  consumer.closeConsumer();
               }
               catch (Throwable t)
               {
                  log.trace("Error closing consumer", t);
               }
               i.remove();
            }
         }

         synchronized (producers)
         {
            for (Iterator i = producers.iterator(); i.hasNext();)
            {
               JmsMessageProducer producer = (JmsMessageProducer) i.next();
               try
               {
                  producer.closeProducer();
               }
               catch (Throwable t)
               {
                  log.trace("Error closing producer", t);
               }
               i.remove();
            }
         }
         
         mc.removeHandle(this);
         ConnectionEvent ev = new ConnectionEvent(mc, ConnectionEvent.CONNECTION_CLOSED);
         ev.setConnectionHandle(this);
         mc.sendEvent(ev);
         mc = null;
      }
   }
   
   void addConsumer(MessageConsumer consumer)
   {
      synchronized (consumers)
      {
         consumers.add(consumer);
      }
   }
   
   void removeConsumer(MessageConsumer consumer)
   {
      synchronized (consumers)
      {
         consumers.remove(consumer);
      }
   }
   
   void addProducer(MessageProducer producer)
   {
      synchronized (producers)
      {
         producers.add(producer);
      }
   }
   
   void removeProducer(MessageProducer producer)
   {
      synchronized (producers)
      {
         producers.remove(producer);
      }
   }
   
   QueueSession getQueueSession() throws JMSException
   {
      Session s = getSession();
      if( !(s instanceof QueueSession) )
         throw new InvalidDestinationException("Attempting to use QueueSession methods on: "+this);
      return (QueueSession) s; 
   }
   
   TopicSession getTopicSession() throws JMSException
   {
      Session s = getSession();
      if( !(s instanceof TopicSession) )
         throw new InvalidDestinationException("Attempting to use TopicSession methods on: "+this);
      return (TopicSession) s; 
   }
}
