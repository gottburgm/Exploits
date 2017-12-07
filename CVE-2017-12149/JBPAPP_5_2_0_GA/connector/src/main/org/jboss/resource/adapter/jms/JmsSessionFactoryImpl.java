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

import java.util.HashSet;
import java.util.Iterator;

import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.logging.Logger;
import org.jboss.resource.connectionmanager.JTATransactionChecker;

/**
 * Implements the JMS Connection API and produces {@link JmsSession} objects.
 *
 * @author  <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version <tt>$Revision: 85242 $</tt>
 */
public class JmsSessionFactoryImpl
   implements JmsSessionFactory, Referenceable
{
   private static final Logger log = Logger.getLogger(JmsSessionFactoryImpl.class);

   /** Are we closed? */
   private boolean closed = false;

   /** Whether trace is enabled */
   private boolean trace = log.isTraceEnabled();
   
   private Reference reference;

   // Used from JmsConnectionFactory
   private String userName;
   private String password;
   private String clientID;
   private int type;

   /* Whether we are started */
   private boolean started = false;
   
   /** JmsRa own factory */
   private JmsManagedConnectionFactory mcf;

   /** Hook to the appserver */
   private ConnectionManager cm;

   /** The sessions */
   private HashSet sessions = new HashSet();

   /** The temporary queues */
   private HashSet tempQueues = new HashSet();

   /** The temporary topics */
   private HashSet tempTopics = new HashSet();
   
   public JmsSessionFactoryImpl(final ManagedConnectionFactory mcf,
                                final ConnectionManager cm,
                                final int type)
   {
      this.mcf = (JmsManagedConnectionFactory) mcf;
      this.cm = cm;
      
      if (cm == null)
         // This is standalone usage, no appserver
         this.cm = new JmsConnectionManager();
      else 
         this.cm = cm;

      this.type = type;

      if (trace)
         log.trace("mcf=" + mcf + ", cm=" + cm + ", type=" + type);
   }

   public void setReference(final Reference reference)
   {
      this.reference = reference;
   }
    
   public Reference getReference()
   {
      return reference;
   }
    
   // --- API for JmsConnectionFactoryImpl
   
   public void setUserName(final String name) 
   {
      userName = name;
   }
    
   public void setPassword(final String password) 
   {
      this.password = password;
   }

   //---- QueueConnection ---
   
   public QueueSession createQueueSession(final boolean transacted, 
                                          final int acknowledgeMode) 
      throws JMSException
   {
      checkClosed();
      if (type == JmsConnectionFactory.TOPIC)
         throw new IllegalStateException("Can not get a queue session from a topic connection");
      return allocateConnection(transacted, acknowledgeMode, type);
   }
    
   public ConnectionConsumer createConnectionConsumer
      (Queue queue,
       String messageSelector,
       ServerSessionPool sessionPool,
       int maxMessages) 
      throws JMSException 
   {
      throw new IllegalStateException(ISE);
   }
    
   //--- TopicConnection ---
    
   public TopicSession createTopicSession(final boolean transacted, 
                                          final int acknowledgeMode) 
      throws JMSException
   { 
      checkClosed();
      if (type == JmsConnectionFactory.QUEUE)
         throw new IllegalStateException("Can not get a topic session from a queue connection");
      return allocateConnection(transacted, acknowledgeMode, type);
   }

   public ConnectionConsumer createConnectionConsumer
      (Topic topic,
       String messageSelector,
       ServerSessionPool sessionPool,
       int maxMessages) 
      throws JMSException 
   {
      throw new IllegalStateException(ISE);
   }		       

   public ConnectionConsumer createDurableConnectionConsumer(
      Topic topic, 
      String subscriptionName,
      String messageSelector,
      ServerSessionPool sessionPool, 
      int maxMessages) 
      throws JMSException
   {
      throw new IllegalStateException(ISE);
   }
   
   //--- All the Connection methods
   
   public String getClientID() throws JMSException
   {
      checkClosed();

      if (clientID == null)
         return mcf.getClientID();

      return clientID;
   }
    
   public void setClientID(String cID) throws JMSException
   {
      if (mcf.isStrict())
         throw new IllegalStateException(ISE);
      
      checkClosed();
      if (clientID != null)
         throw new IllegalStateException("Cannot change client id");
      clientID = cID;
   }
    
   public ConnectionMetaData getMetaData() throws JMSException
   {
      checkClosed();
      return mcf.getMetaData();
   }
    
   public ExceptionListener getExceptionListener() throws JMSException
   {
      throw new IllegalStateException(ISE);
   }
    
   public void setExceptionListener(ExceptionListener listener)
      throws JMSException
   {
      throw new IllegalStateException(ISE);
   }
    
   public void start() throws JMSException
   {
      checkClosed();
      if (trace)
         log.trace("start() " + this);
      synchronized (sessions)
      {
         if (started)
            return;
         started = true;
         for (Iterator i = sessions.iterator(); i.hasNext();)
         {
            JmsSession session = (JmsSession) i.next();
            session.start();
         }
      }
   }
    
   public void stop() throws JMSException
   {
      if (mcf.isStrict())
         throw new IllegalStateException(ISE);
      checkClosed();
      if (trace)
         log.trace("stop() " + this);
      synchronized (sessions)
      {
         if (started == false)
            return;
         started = true;
         for (Iterator i = sessions.iterator(); i.hasNext();)
         {
            JmsSession session = (JmsSession) i.next();
            session.stop();
         }
      }
   }

   public void close() throws JMSException
   {
      if (closed)
         return;
      closed = true;

      if (trace)
         log.trace("close() " + this);
      
      synchronized (sessions)
      {
         for (Iterator i = sessions.iterator(); i.hasNext();)
         {
            JmsSession session = (JmsSession) i.next();
            try
            {
               session.closeSession();
            }
            catch (Throwable t)
            {
               log.trace("Error closing session", t);
            }
            i.remove();
         }
      }
      
      synchronized (tempQueues)
      {
         for (Iterator i = tempQueues.iterator(); i.hasNext();)
         {
            TemporaryQueue temp = (TemporaryQueue) i.next();
            try
            {
               if (trace)
                  log.trace("Closing temporary queue " + temp + " for " + this);
               temp.delete();
            }
            catch (Throwable t)
            {
               log.trace("Error deleting temporary queue", t);
            }
            i.remove();
         }
      }
      
      synchronized (tempTopics)
      {
         for (Iterator i = tempTopics.iterator(); i.hasNext();)
         {
            TemporaryTopic temp = (TemporaryTopic) i.next();
            try
            {
               if (trace)
                  log.trace("Closing temporary topic " + temp + " for " + this);
               temp.delete();
            }
            catch (Throwable t)
            {
               log.trace("Error deleting temporary queue", t);
            }
            i.remove();
         }
      }
   }

   public void closeSession(JmsSession session) throws JMSException
   {
      synchronized (sessions)
      {
         sessions.remove(session);
      }
   }
   
   public void addTemporaryQueue(TemporaryQueue temp)
   {
      synchronized(tempQueues)
      {
         tempQueues.add(temp);
      }
   }
   
   public void addTemporaryTopic(TemporaryTopic temp)
   {
      synchronized(tempTopics)
      {
         tempTopics.add(temp);
      }
   }
   
   // -- JMS 1.1

   public ConnectionConsumer createConnectionConsumer(Destination destination, ServerSessionPool pool, int maxMessages) throws JMSException
   {
      throw new IllegalStateException(ISE);
   }

   public ConnectionConsumer createConnectionConsumer(Destination destination, String name, ServerSessionPool pool, int maxMessages) throws JMSException
   {
      throw new IllegalStateException(ISE);
   }

   public Session createSession(boolean transacted, int acknowledgeMode)
      throws JMSException
   {
      checkClosed();
      return allocateConnection(transacted, acknowledgeMode, type);
   }

   protected JmsSession allocateConnection(boolean transacted, int acknowledgeMode, int sessionType) throws JMSException
   {
      try
      {
         synchronized (sessions)
         {
            if (mcf.isStrict() && sessions.isEmpty() == false)
               throw new IllegalStateException("Only allowed one session per connection. See the J2EE spec, e.g. J2EE1.4 Section 6.6");
            if (transacted)
               acknowledgeMode = Session.SESSION_TRANSACTED;
            JmsConnectionRequestInfo info = new JmsConnectionRequestInfo(transacted, acknowledgeMode, sessionType);
            info.setUserName(userName);
            info.setPassword(password);
            info.setClientID(clientID);
            info.setDefaults(mcf.getProperties());

            if (trace)
               log.trace("Allocating session for " + this + " with request info=" + info);
            JmsSession session = (JmsSession) cm.allocateConnection(mcf, info);
            try
            {
               if (trace)
                  log.trace("Allocated  " + this + " session=" + session);
               session.setJmsSessionFactory(this);
               if (started)
                  session.start();
               sessions.add(session);
               return session;
            }
            catch (Throwable t)
            {
               try
               {
                  session.close();
               }
               catch (Throwable ignored)
               {
               }
               if (t instanceof Exception)
                  throw (Exception) t;
               else
                  throw new RuntimeException("Unexpected error: ", t);
            }
         }
      }
      catch (Exception e)
      {
         log.error("could not create session", e);
         
         JMSException je = new JMSException
            ("Could not create a session: " + e);
         je.setLinkedException(e);
         throw je;
      }               
   }  

   protected void checkClosed() throws IllegalStateException
   {
      if (closed)
         throw new IllegalStateException("The connection is closed");
      checkTransactionActive();
   }
   
   /**
    * Check whether a tranasction is active
    *
    * @throws IllegalStateException if the transaction is not active, preparing, prepared or committing or for any error in the transaction manager
    */
   protected void checkTransactionActive() throws IllegalStateException
   {
      if (cm == null)
         throw new IllegalStateException("No connection manager");
      try
      {
         if (cm instanceof JTATransactionChecker)
            ((JTATransactionChecker) cm).checkTransactionActive();
      }
      catch (Exception e)
      {
         IllegalStateException ex = new IllegalStateException("Transaction not active");
         ex.initCause(e);
         ex.setLinkedException(e);
         throw ex;
      }
   }
}
