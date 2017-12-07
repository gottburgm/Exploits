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

import javax.jms.*;
import javax.management.JMException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;
import java.util.Hashtable;

/**
 * Local JMS Listener to receive the message and send to the listener
 *
 * @author ???
 * @version $Revision: 81025 $
 */
public class JMSClientNotificationListener
        extends ClientNotificationListener
        implements MessageListener
{
   public JMSClientNotificationListener(ObjectName pSender,
                                        NotificationListener pClientListener,
                                        Object pHandback,
                                        NotificationFilter pFilter,
                                        String pQueueJNDIName,
                                        String pServerName,
                                        MEJB pConnector) throws
           JMSException,
           JMException,
           NamingException,
           RemoteException
   {
      super(pSender, pClientListener, pHandback);

      // Get the JMS QueueConnectionFactory from the J2EE server
      QueueConnection lConnection = getQueueConnection(pServerName, pQueueJNDIName);
      // Create JMS Session and create temporary Queue
      QueueSession lSession = lConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue lQueue = lSession.createTemporaryQueue();
      // Register the listener as MBean on the remote JMX server
      createListener(pConnector,
              "org.jboss.management.mejb.JMSNotificationListener",
              new Object[]{pQueueJNDIName, lQueue},
              new String[]{String.class.getName(), Queue.class.getName()});
      // Create JMS message receiver, create local message listener and set it as message
      // listener to the receiver
      QueueReceiver lReceiver = lSession.createReceiver(lQueue, null);
      lReceiver.setMessageListener(this);
      addNotificationListener(pConnector, pFilter);
   }

   public void onMessage(Message pMessage)
   {
      try
      {
         // Unpack the Notification from the Message and hand it over to the clients
         // Notification Listener
         Notification lNotification = (Notification) ((ObjectMessage) pMessage).getObject();
         mClientListener.handleNotification(lNotification, mHandback);
      }
      catch (JMSException e)
      {
         log.error("failed to handle notification", e);
      }
   }

   /**
    * Creates a SurveyManagement bean.
    *
    * @return Returns a SurveyManagement bean for use by the Survey handler.
    */
   private QueueConnection getQueueConnection(String pServerName, String pQueueJNDIName)
           throws NamingException, JMSException
   {
      Context lJNDIContext = null;
      if (pServerName != null)
      {
         Hashtable lProperties = new Hashtable();
         lProperties.put(Context.PROVIDER_URL, pServerName);
         lJNDIContext = new InitialContext(lProperties);
      }
      else
      {
         lJNDIContext = new InitialContext();
      }
      Object aRef = lJNDIContext.lookup(pQueueJNDIName);
      QueueConnectionFactory aFactory = (QueueConnectionFactory)
              PortableRemoteObject.narrow(aRef, QueueConnectionFactory.class);
      QueueConnection lConnection = aFactory.createQueueConnection();
      lConnection.start();
      return lConnection;
   }

}
