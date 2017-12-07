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
package javax.management.remote;

import java.io.Serializable;
import javax.management.Notification;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class TargetedNotification implements Serializable
{
   private static final long serialVersionUID = 7676132089779300926l;

   private Notification notif;
   private Integer id;

   public TargetedNotification(Notification notification, Integer listenerID)
   {
      this.notif = notification;
      this.id = listenerID;
   }

   public Notification getNotification()
   {
      return notif;
   }

   public Integer getListenerID()
   {
      return id;
   }

   public String toString()
   {
      return "TargetedNotification - listener id: " + id + ", notification: " + notif;
   }
}