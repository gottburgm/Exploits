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
package org.jboss.resource.adapter.jms.inflow.dlq;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;

import org.jboss.jms.jndi.JMSProviderAdapter;
import org.jboss.logging.Logger;
import org.jboss.resource.adapter.jms.inflow.DLQHandler;
import org.jboss.resource.adapter.jms.inflow.JmsActivation;
import org.jboss.resource.adapter.jms.inflow.JmsActivationSpec;
import org.jboss.util.naming.Util;

/**
 * An abstract DLQ handler.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public abstract class AbstractDLQHandler implements DLQHandler, ExceptionListener
{
   /** The logger */
   protected static final Logger log = Logger.getLogger(AbstractDLQHandler.class);

   /** The activation */
   protected JmsActivation activation;
   
   /** The DLQ */
   protected Queue dlq;
   
   /** The DLQ Connection*/
   protected QueueConnection connection;

   public boolean handleRedeliveredMessage(Message msg)
   {
      boolean handled = handleDelivery(msg);
      if (handled)
         sendToDLQ(msg);
      return handled;
   }

   public void messageDelivered(Message msg)
   {
   }
   
   public void setup(JmsActivation activation, Context ctx) throws Exception
   {
      this.activation = activation;
      setupDLQDestination(ctx);
      setupDLQConnection(ctx);
   }

   public void teardown()
   {
      teardownDLQConnection();
      teardownDLQDestination();
   }

   public void onException(JMSException exception)
   {
      activation.handleFailure(exception);
   }

   /**
    * Setup the DLQ Destination
    * 
    * @param ctx the naming context
    * @throws Exception for any error
    */
   protected void setupDLQDestination(Context ctx) throws Exception
   {
      String name = activation.getActivationSpec().getDLQJNDIName();
      dlq = (Queue) Util.lookup(ctx, name, Queue.class);
   }

   /**
    * Teardown the DLQ Destination
    */
   protected void teardownDLQDestination()
   {
   }

   /**
    * Setup the DLQ Connection
    * 
    * @param ctx the naming context
    * @throws Exception for any error
    */
   protected void setupDLQConnection(Context ctx) throws Exception
   {
      JmsActivationSpec spec = activation.getActivationSpec();
      String user = spec.getDLQUser();
      String pass = spec.getDLQPassword();
      String clientID = spec.getDLQClientID();
      JMSProviderAdapter adapter = activation.getProviderAdapter();
      String queueFactoryRef = adapter.getQueueFactoryRef();
      log.debug("Attempting to lookup dlq connection factory " + queueFactoryRef);
      QueueConnectionFactory qcf = (QueueConnectionFactory) Util.lookup(ctx, queueFactoryRef, QueueConnectionFactory.class);
      log.debug("Got dlq connection factory " + qcf + " from " + queueFactoryRef);
      log.debug("Attempting to create queue connection with user " + user);
      if (user != null)
         connection = qcf.createQueueConnection(user, pass);
      else
         connection = qcf.createQueueConnection();
      try
      {
         if (clientID != null)
            connection.setClientID(clientID);
         connection.setExceptionListener(this);
         log.debug("Using queue connection " + connection);
      }
      catch (Throwable t)
      {
         try
         {
            connection.close();
         }
         catch (Exception e)
         {
            log.trace("Ignored error closing connection", e);
         }
         connection = null;
         if (t instanceof Exception)
            throw (Exception) t;
         throw new RuntimeException("Error configuring queue connection", t);
      }
   }

   /**
    * Teardown the DLQ Connection
    */
   protected void teardownDLQConnection()
   {
      try
      {
         if (connection != null)
         {
            log.debug("Closing the " + connection);
            connection.close();
         }
      }
      catch (Throwable t)
      {
         log.debug("Error closing the connection " + connection, t);
      }
      connection = null;
   }
   
   /**
    * Do we handle the message?
    * 
    * @param msg the message to handle
    * @return true when we handle it
    */
   protected abstract boolean handleDelivery(Message msg);
   
   /**
    * Warn that a message is being handled by the DLQ
    *
    * @param msg
    * @param count the number of redelivers
    * @param max the maximum number of redeliveries
    */
   protected void warnDLQ(Message msg, int count, int max)
   {
      log.warn("Message redelivered=" + count + " max=" + max + " sending it to the dlq " + msg);
   }
   
   /**
    * Send the message to the dlq
    * 
    * @param msg message to send
    */
   protected void sendToDLQ(Message msg)
   {
      int deliveryMode = getDeliveryMode(msg);
      int priority = getPriority(msg);
      long timeToLive = getTimeToLive(msg);
      
      // If we get a negative time to live that means the message has expired
      if (timeToLive < 0)
      {
         if (log.isTraceEnabled())
            log.trace("Not sending the message to the DLQ, it has expired " + msg);
         return;
      }
      
      Message copy = makeWritable(msg);
      if (copy != null)
         doSend(copy, deliveryMode, priority, timeToLive);
   }
   
   /**
    * Get the delivery mode for the DLQ message
    *
    * @param msg the message
    * @return the delivery mode
    */
   protected int getDeliveryMode(Message msg)
   {
      try
      {
         return msg.getJMSDeliveryMode();
      }
      catch (Throwable t)
      {
         return Message.DEFAULT_DELIVERY_MODE;
      }
   }
   
   /**
    * Get the priority for the DLQ message
    *
    * @param msg the message
    * @return the priority
    */
   protected int getPriority(Message msg)
   {
      try
      {
         return msg.getJMSPriority();
      }
      catch (Throwable t)
      {
         return Message.DEFAULT_PRIORITY;
      }
   }
   
   /**
    * Get the time to live for the DLQ message
    *
    * @param msg the message
    * @return the time to live
    */
   protected long getTimeToLive(Message msg)
   {
      try
      {
         long expires = msg.getJMSExpiration();
         if (expires == Message.DEFAULT_TIME_TO_LIVE)
            return Message.DEFAULT_TIME_TO_LIVE;
         return expires - System.currentTimeMillis();
      }
      catch (Throwable t)
      {
         return Message.DEFAULT_TIME_TO_LIVE;
      }
   }
   
   /**
    * Make a writable copy of the message
    *
    * @param msg the message
    * @return the copied message
    */
   protected Message makeWritable(Message msg)
   {
      boolean trace = log.isTraceEnabled();
      
      try
      {
         HashMap tmp = new HashMap();

         // Save properties
         for (Enumeration en = msg.getPropertyNames(); en.hasMoreElements();)
         {
            String key = (String) en.nextElement();
            tmp.put(key, msg.getObjectProperty(key));
         }
         
         // Make them writable
         msg.clearProperties();

         for (Iterator i = tmp.keySet().iterator(); i.hasNext();)
         {
            String key = (String) i.next();
            try
            {
               msg.setObjectProperty(key, tmp.get(key));
            }
            catch (JMSException ignored)
            {
               if (trace)
                  log.trace("Could not copy message property " + key, ignored);
            }
         }

         msg.setStringProperty(JBOSS_ORIG_MESSAGEID, msg.getJMSMessageID());
         Destination destination = msg.getJMSDestination();
         if (destination != null)
            msg.setStringProperty(JBOSS_ORIG_DESTINATION, destination.toString());

         return msg;
      }
      catch (Throwable t)
      {
         log.error("Unable to make writable " + msg, t);
         return null;
      }
   }
   
   /** 
    * Do the message send
    *
    * @param msg the message
    */
   protected void doSend(Message msg, int deliveryMode, int priority, long timeToLive)
   {
      QueueSession session = null;
      try
      {
         session = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
         QueueSender sender = session.createSender(dlq);
         sender.send(msg, deliveryMode, priority, timeToLive);
      }
      catch (Throwable t)
      {
         handleSendError(msg, t);
      }
      finally
      {
         if (session != null)
         {
            try
            {
               session.close();
            }
            catch (Throwable t)
            {
               log.trace("Ignored ", t);
            }
         }
      }
   }
   
   /**
    * Handle a failure to send the message to the dlq
    *
    * @param msg the message
    * @param t the error
    */
   protected void handleSendError(Message msg, Throwable t)
   {
      log.error("DLQ " + dlq + " error sending message " + msg, t);
   }
}
