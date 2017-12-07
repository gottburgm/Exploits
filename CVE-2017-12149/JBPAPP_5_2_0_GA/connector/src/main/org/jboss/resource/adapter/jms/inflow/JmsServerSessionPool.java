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
package org.jboss.resource.adapter.jms.inflow;

import java.util.ArrayList;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.ServerSession;
import javax.jms.ServerSessionPool;
import javax.jms.Topic;

import org.jboss.logging.Logger;

/**
 * A generic jms session pool.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 100217 $
 */
public class JmsServerSessionPool implements ServerSessionPool
{
   /** The logger */
   private static final Logger log = Logger.getLogger(JmsServerSessionPool.class);
      
   /** The activation */
   JmsActivation activation;

   /** The consumer */
   ConnectionConsumer consumer;

   /** The server sessions */
   ArrayList serverSessions = new ArrayList();
   
   /** Whether the pool is stopped */
   boolean stopped = false;
   
   /** The number of sessions */
   int sessionCount = 0;
   
   
   /**
    * Create a new session pool
    * 
    * @param activation the jms activation
    */
   public JmsServerSessionPool(JmsActivation activation)
   {
      this.activation = activation;
   }

   /**
    * @return the activation
    */
   public JmsActivation getActivation()
   {
      return activation;
   }
   
   /**
    * Start the server session pool
    * 
    * @throws Exeption for any error
    */
   public void start() throws Exception
   {
      setupSessions();
      setupConsumer();
   }

   /**
    * Stop the server session pool
    */
   public void stop()
   {
      teardownConsumer();
      teardownSessions();
   }
   
   public ServerSession getServerSession() throws JMSException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("getServerSession");

      ServerSession result = null;
      
      try
      {
         synchronized (serverSessions)
         {
            while (true)
            {
               int sessionsSize = serverSessions.size();
               
               if (stopped)
                  throw new Exception("Cannot get a server session after the pool is stopped");
               
               else if (sessionsSize > 0)
               {
                  result = (ServerSession) serverSessions.remove(sessionsSize-1);
                  break;
               }
               
               else
               {
                  try
                  {
                     serverSessions.wait();
                  }
                  catch (InterruptedException ignored)
                  {
                  }
               }
            }
         }
      }
      catch (Throwable t)
      {
         throw new JMSException("Unable to get a server session " + t);
      }
      
      if (trace)
         log.trace("Returning server session " + result);
      
      return result;
   }

   /**
    * Return the server session
    * 
    * @param session the session
    */
   protected void returnServerSession(JmsServerSession session)
   {
      synchronized (serverSessions)
      {
         if (stopped)
         {
            session.teardown();
            --sessionCount;
         }
         else
            serverSessions.add(session);
         serverSessions.notifyAll();
      }
   }
   
   /**
    * Setup the sessions
    * 
    * @throws Exeption for any error
    */
   protected void setupSessions() throws Exception
   {
      JmsActivationSpec spec = activation.getActivationSpec();
      ArrayList clonedSessions = null;
      
      // Create the sessions
      synchronized (serverSessions)
      {
         for (int i = 0; i < spec.getMaxSessionInt(); ++i)
         {
            JmsServerSession session = new JmsServerSession(this);
            serverSessions.add(session);
         }
         sessionCount = serverSessions.size();
         clonedSessions = (ArrayList) serverSessions.clone();

      }
      
      // Start the sessions
      for (int i = 0; i < clonedSessions.size(); ++ i)
      {
         JmsServerSession session = (JmsServerSession) clonedSessions.get(i);
         session.setup();
      }
   }

   /**
    * Stop the sessions
    */
   protected void teardownSessions()
   {
      synchronized (serverSessions)
      {
         // Disallow any new sessions
         stopped = true;
         serverSessions.notifyAll();
         
         // Stop inactive sessions
         for (int i = 0; i < serverSessions.size(); ++i)
         {
            JmsServerSession session = (JmsServerSession) serverSessions.get(i);
            session.teardown();
            --sessionCount;
         }

         serverSessions.clear();

         if (activation.getActivationSpec().isForceClearOnShutdown())
         {        
            int attempts = 0;
            int forceClearAttempts = activation.getActivationSpec().getForceClearAttempts();
            long forceClearInterval = activation.getActivationSpec().getForceClearOnShutdownInterval();
            
            log.trace(this + " force clear behavior in effect. Waiting for " + forceClearInterval + " milliseconds for " + forceClearAttempts + " attempts.");
           
            while((sessionCount > 0) && (attempts < forceClearAttempts))
            {
               try
               {
                  int currentSessions = sessionCount;
                  serverSessions.wait(forceClearInterval);
                  // Number of session didn't change
                  if (sessionCount == currentSessions)
                  {
                     ++attempts;
                     log.trace(this + " clear attempt failed " + attempts); 
                  }
               }
               catch(InterruptedException ignore)
               {
               }
            
            }
         }
         else
         {
            // Wait for inuse sessions
            while (sessionCount > 0)
            {
               try
               {
                  serverSessions.wait();
               }
               catch (InterruptedException ignore)
               {
               }
            }
         }
      }
   }
   
   /**
    * Setup the connection consumer
    * 
    * @throws Exeption for any error
    */
   protected void setupConsumer() throws Exception
   {
      Connection connection = activation.getConnection();
      JmsActivationSpec spec = activation.getActivationSpec();
      String selector = spec.getMessageSelector();
      int maxMessages = spec.getMaxMessagesInt();
      if (activation.isTopic())
      {
         Topic topic = (Topic) activation.getDestination();
         String subscriptionName = spec.getSubscriptionName();
         if (spec.isDurable())
            consumer = connection.createDurableConnectionConsumer(topic, subscriptionName, selector, this, maxMessages);
         else
            consumer = connection.createConnectionConsumer(topic, selector, this, maxMessages);
      }
      else
      {
         Queue queue = (Queue) activation.getDestination();
         consumer = connection.createConnectionConsumer(queue, selector, this, maxMessages);
      }
      log.debug("Created consumer " + consumer);

      if (consumer == null)
         throw new JMSException("Consumer is null");
   }

   /**
    * Stop the connection consumer
    */
   protected void teardownConsumer()
   {
      try
      {
         if (consumer != null)
         {
            log.debug("Closing the " + consumer);
            consumer.close();
         }
      }
      catch (Throwable t)
      {
         log.debug("Error closing the consumer " + consumer, t);
      }
   }

}
