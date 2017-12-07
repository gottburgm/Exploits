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
import javax.management.Notification;

/**
 * NotificationEntry represents a Notification passed back to the client from a remote
 * server so that the client can properly coorelate the Notification object with a specific
 * client-side listener object, based on the handback, which was passed to the server by the
 * client as a specific, unique key.
 *
 * @author <a href="mailto:jhaynie@vocalocity.net">Jeff Haynie</a>
 * @version $Revision: 81023 $
 */
public class NotificationEntry implements Serializable
{
   static final long serialVersionUID = -8038783215990131189L;
   
   private final Notification notification;
   private final Object handback;

   public NotificationEntry(Notification n, Object h)
   {
      this.notification = n;
      this.handback = h;
   }

   /**
    * return the original Notification object
    *
    * @return
    */
   public Notification getNotification()
   {
      return notification;
   }

   /**
    * return the client Handback Object, which is used to find the
    * appropriate client-side Listener to re-dispatch the notification to
    *
    * @return
    */
   public Object getHandBack()
   {
      return handback;
   }

   public String toString()
   {
      return "NotificationEntry [notification:" + notification + ",handback:" + handback + "]";
   }
}
