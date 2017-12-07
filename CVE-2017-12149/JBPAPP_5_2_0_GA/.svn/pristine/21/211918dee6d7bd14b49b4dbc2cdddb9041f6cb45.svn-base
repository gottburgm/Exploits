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

import javax.management.JMException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Client-side RMI Listener to receive the message and send to the
 * clients listener. Its stub is used on the server-side to hand
 * the Notifications over to this class.
 *
 * @author <A href="mailto:andreas@jboss.org">Andreas &quot;Mad&quot; Schaefer</A>
 */
public class RMIClientNotificationListener
        extends ClientNotificationListener
        implements RMIClientNotificationListenerInterface
{

   public RMIClientNotificationListener(ObjectName pSender,
                                        NotificationListener pClientListener,
                                        Object pHandback,
                                        NotificationFilter pFilter,
                                        MEJB pConnector)
           throws RemoteException,
           JMException
   {
      super(pSender, pClientListener, pHandback);
      // Export the RMI object to become a callback object
      Remote lStub = UnicastRemoteObject.exportObject(this);
      // Register the listener as MBean on the remote JMX server
      createListener(pConnector,
              "org.jboss.management.mejb.RMINotificationListener",
              new Object[]{lStub},
              new String[]{RMIClientNotificationListenerInterface.class.getName()});
      addNotificationListener(pConnector, pFilter);
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
           throws RemoteException
   {
      try
      {
         mClientListener.handleNotification(pNotification,
                 mHandback);
      }
      catch (RuntimeException re)
      {
         throw new RemoteException("Exceptions returned by the client listener", re);
      }
      catch (Error e)
      {
         throw new RemoteException("Error returned by the client listener", e);
      }
   }
}
