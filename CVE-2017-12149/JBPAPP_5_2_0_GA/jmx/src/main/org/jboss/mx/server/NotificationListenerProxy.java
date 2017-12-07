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
package org.jboss.mx.server;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

/**
 * A notification listener used to forward notifications to listeners
 * added through the mbean server.<p>
 *
 * The original source is replaced with the object name.
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 81022 $
 */
public class NotificationListenerProxy
   implements NotificationListener
{
   // Constants ---------------------------------------------------

   // Attributes --------------------------------------------------

   /**
    * The original listener
    */
   private NotificationListener listener;

   /**
    * The object name we are proxying
    */
   private ObjectName name;

   // Static ------------------------------------------------------

   // Constructors ------------------------------------------------

   /**
    * Create a new Notification Listener Proxy
    * 
    * @param name the object name
    * @param listener the original listener
    */
   public NotificationListenerProxy(ObjectName name, 
                                    NotificationListener listener)
   {
      this.name = name;
      this.listener = listener;
   }

   // Public ------------------------------------------------------

   // implementation NotificationListener -------------------------

   public void handleNotification(Notification notification, Object handback)
   {
      if (notification == null)
         return;

      // Forward the notification with the object name as source
      // FIXME: This overwrites the original source, there is no way
      //        to put it back with the current spec
      notification.setSource(name);
      listener.handleNotification(notification, handback);
   }

   // overrides ---------------------------------------------------

   // Protected ---------------------------------------------------

   // Private -----------------------------------------------------

   // Inner classes -----------------------------------------------
}
