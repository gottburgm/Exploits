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
package org.jboss.management.mejb;

import org.jboss.logging.Logger;

import javax.jms.*;
import javax.management.Notification;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

/**
 * Remote Listener using JMS to send the event
 *
 * @author ???
 * @version $Revision: 81025 $
 * @jmx:mbean extends="org.jboss.management.mejb.ListenerMBean"
 */
public class JMSNotificationListener
        implements JMSNotificationListenerMBean
{
   private static final Logger log = Logger.getLogger(JMSNotificationListener.class);

   // JMS Queue Session and Sender must be created on the server-side
   // therefore they are transient and created on the first notification
   // call
   private transient QueueSender mSender;
   private transient QueueSession mSession;
   private String mJNDIName;
   private Queue mQueue;

   public JMSNotificationListener(String pJNDIName,
                                  Queue pQueue) throws JMSException
   {
      mJNDIName = pJNDIName;
      mQueue = pQueue;
   }

   /**
    * Handles the given notification by sending this to the remote
    * client listener
    *
    * @param pNotification Notification to be send
    * @param pHandback     Handback object
    */
   public void handleNotification(Notification pNotification,
                                  Object pHandback)
   {
      try
      {
         if (mSender == null)
         {
            // Get QueueConnectionFactory, create Connection, Session and then Sender
            QueueConnection lConnection = getQueueConnection(mJNDIName);
            mSession = lConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            mSender = mSession.createSender(mQueue);
         }
         // Create a message and send to the Queue
         Message lMessage = mSession.createObjectMessage(pNotification);
         mSender.send(lMessage);
      }
      catch (Exception e)
      {
         log.error("failed to handle notification", e);
      }
   }

   /**
    * Test if this and the given Object are equal. This is true if the given
    * object both refer to the same local listener
    *
    * @param pTest Other object to test if equal
    * @return							True if both are of same type and
    * refer to the same local listener
    */
   public boolean equals(Object pTest)
   {
      if (pTest instanceof JMSNotificationListener)
      {
         try
         {
            return mQueue.getQueueName().equals(((JMSNotificationListener) pTest).mQueue.getQueueName());
         }
         catch (JMSException e)
         {
            log.error("unexpcted failure while tetsing equality", e);
         }
      }
      return false;
   }

   /**
    * @return							Hashcode of the local listener
    */
   public int hashCode()
   {
      return mQueue.hashCode();
   }

   /**
    * Creates a SurveyManagement bean.
    *
    * @return Returns a SurveyManagement bean for use by the Survey handler.
    */
   private QueueConnection getQueueConnection(String pJNDIName)
           throws NamingException, JMSException
   {
      Context aJNDIContext = new InitialContext();
      Object aRef = aJNDIContext.lookup(pJNDIName);
      QueueConnectionFactory aFactory = (QueueConnectionFactory)
              PortableRemoteObject.narrow(aRef, QueueConnectionFactory.class);
      QueueConnection lConnection = aFactory.createQueueConnection();
      lConnection.start();
      return lConnection;
   }
}
