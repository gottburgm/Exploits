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
package org.jboss.mx.remoting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * NotificationQueue is an object that holds one or more NotificationEntry objects. This
 * object is created and passed from the server to the client during invocation so that the
 * client and re-deliver Notifications to client-side NotificationListeners.
 *
 * @author <a href="mailto:jhaynie@vocalocity.net">Jeff Haynie</a>
 * @version $Revision: 81023 $
 */
public class NotificationQueue implements Serializable
{
   static final long serialVersionUID = -1185639057427341662L;

   private final String sessionId;
   private final List notifications = new ArrayList();

   /**
    * create an empty queue
    *
    * @param sessionId
    */
   public NotificationQueue(String sessionId)
   {
      this.sessionId = sessionId;
   }

   public String toString()
   {
      return "NotificationQueue [sessionId:" + sessionId + ",notifications:" + notifications + "]";
   }

   /**
    * clear the queue
    */
   public void clear()
   {
      notifications.clear();
   }

   /**
    * add an entry to the queue
    *
    * @param notification
    */
   void add(NotificationEntry notification)
   {
      synchronized(notifications)
      {
         notifications.add(notification);
      }
   }

   /**
    * return the session ID associated with the queue
    *
    * @return
    */
   public String getSessionID()
   {
      return sessionId;
   }

   /**
    * return true if there are no entries, false if there are 1..n entries
    *
    * @return
    */
   public boolean isEmpty()
   {
      synchronized(notifications)
      {
         return notifications.isEmpty();
      }
   }

   /**
    * return an Iterator of NotificationEntry objects
    *
    * @return
    */
   public Iterator iterator()
   {
      synchronized(notifications)
      {
         return notifications.iterator();
      }
   }
}
