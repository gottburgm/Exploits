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
package org.jboss.mx.remoting.rmi;

import java.io.IOException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedLong;

/**
 * Handles the firing of notifications related to the connection
 * status of the connector.
 *
 * @author <a href="mailto:telrod@e2technologies.net">Tom Elrod</a>
 */
public class ConnectionNotifier extends NotificationBroadcasterSupport
{
   private JMXConnector connector;

   private static SynchronizedLong sequenceNumber = new SynchronizedLong(0);

   public ConnectionNotifier(JMXConnector connector)
   {
      this.connector = connector;
   }

   public void fireConnectedNotification()
   {
      JMXConnectionNotification notification = new JMXConnectionNotification(JMXConnectionNotification.OPENED,
                                                                             connector,
                                                                             getConnectionId(),
                                                                             sequenceNumber.increment(),
                                                                             "JMXConnector connected",
                                                                             null);
   sendNotification(notification);
   }

   private String getConnectionId()
   {
      String id = null;
      try
      {
         id = connector.getConnectionId();
      }
      catch(IOException e)
      {
      }
      return id;
   }

   public void fireClosedNotification()
   {
      JMXConnectionNotification notification = new JMXConnectionNotification(JMXConnectionNotification.CLOSED,
                                                                             connector,
                                                                             getConnectionId(),
                                                                             sequenceNumber.increment(),
                                                                             "JMXConnector closed",
                                                                             null);
      sendNotification(notification);
   }
}
