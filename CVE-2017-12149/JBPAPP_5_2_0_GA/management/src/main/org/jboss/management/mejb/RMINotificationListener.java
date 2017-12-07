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

import javax.management.Notification;
import java.rmi.RemoteException;

/**
 * Notification Listener Implementation registered as
 * MBean on the remote JMX Server and the added as
 * Notification Listener on the remote JMX Server.
 * Each notification received will be transfered to
 * the remote client using RMI Callback Objects.
 *
 * @author <A href="mailto:andreas@jboss.org">Andreas Schaefer</A>
 * @version $Revision: 81025 $
 * @jmx:mbean extends="org.jboss.management.mejb.ListenerMBean"
 */
public class RMINotificationListener
        implements RMINotificationListenerMBean
{
   private static final Logger log = Logger.getLogger(RMINotificationListener.class);

   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------

   private RMIClientNotificationListenerInterface mClientListener;

   // -------------------------------------------------------------------------
   // Constructor
   // -------------------------------------------------------------------------

   /**
    * Creates the RMI Notification Listener MBean implemenation which
    * will be registered at the remote JMX Server as notificatin listener
    * and then send the notification over the provided RMI Notification
    * sender to the client
    *
    * @param pClientListener RMI-Stub used to transfer the Notification over
    *                        the wire.
    */
   public RMINotificationListener(RMIClientNotificationListenerInterface pClientListener)
   {
      log.debug("RMINotificationListener(), client listener: " + pClientListener);
      mClientListener = pClientListener;
   }

   // -------------------------------------------------------------------------
   // Public Methods
   // -------------------------------------------------------------------------

   /**
    * Handles the given notifcation event and passed it to the registered
    * RMI Notification Sender
    *
    * @param pNotification NotificationEvent
    * @param pHandback     Handback object
    */
   public void handleNotification(Notification pNotification,
                                  Object pHandback)
   {
      try
      {
         log.debug("RMINotificationListener.handleNotification() " +
                 ", notification: " + pNotification +
                 ", handback: " + pHandback +
                 ", client listener: " + mClientListener);
         mClientListener.handleNotification(pNotification, pHandback);
      }
      catch (RemoteException e)
      {
         throw new org.jboss.util.NestedRuntimeException(e);
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
      if (pTest instanceof RMINotificationListener)
      {
         return mClientListener.equals(((RMINotificationListener) pTest).mClientListener);
      }
      return false;
   }

   /**
    * @return							Hashcode of the remote listener
    */
   public int hashCode()
   {
      return mClientListener.hashCode();
   }
}
