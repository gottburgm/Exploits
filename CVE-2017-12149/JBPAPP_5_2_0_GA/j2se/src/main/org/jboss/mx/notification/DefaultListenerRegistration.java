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
package org.jboss.mx.notification;

import javax.management.NotificationFilter;
import javax.management.NotificationListener;

/**
 * The default notification listener registration.
 * 
 * @see org.jboss.mx.notification.ListenerRegistry
 * @see org.jboss.mx.notification.ListenerRegistrationFactory
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 81019 $
 */
public class DefaultListenerRegistration
   implements ListenerRegistration
{
   // Attributes ----------------------------------------------------

   /**
    * The notification listener
    */
   private NotificationListener listener;

   /**
    * The notification filter
    */
   private NotificationFilter filter;

   /**
    * The handback object
    */
   private Object handback;

   // Constructor ---------------------------------------------------

   /**
    * Create a listener registration
    *
    * @param listener the notification listener
    * @param filter the notification filter
    * @param handback the handback object
    */
   public DefaultListenerRegistration(NotificationListener listener,
                                      NotificationFilter filter,
                                      Object handback)
   {
      this.listener = listener;
      this.filter = filter;
      this.handback = handback;
   }

   // Public --------------------------------------------------------

   // ListenerRegistration Implementation ---------------------------

   public NotificationListener getListener()
   {
      return listener;
   }

   public NotificationFilter getFilter()
   {
      return filter;
   }

   public Object getHandback()
   {
      return handback;
   }

   public NotificationListener getRegisteredListener()
   {
      return listener;
   }

   public NotificationFilter getRegisteredFilter()
   {
      return filter;
   }

   public void removed()
   {
   }

   public boolean equals(Object obj)
   {
      if (obj == null || (obj instanceof ListenerRegistration) == false)
         return false;
      ListenerRegistration other = (ListenerRegistration) obj;

      if (getRegisteredListener().equals(other.getRegisteredListener()) == false)
         return false;

      NotificationFilter myFilter = getRegisteredFilter();
      NotificationFilter otherFilter = other.getRegisteredFilter();
      if (myFilter != null && myFilter.equals(otherFilter) == false)
         return false;
      else if (myFilter == null && otherFilter != null)
         return false;

      Object myHandback = getHandback();
      Object otherHandback = other.getHandback();
      if (myHandback != null && myHandback.equals(otherHandback) == false)
         return false;
      else if (myHandback == null && otherHandback != null)
         return false;

      return true;
   }

   public int hashCode()
   {
      int result = listener.hashCode();
      if (filter != null)
         result += filter.hashCode();
      if (handback != null)
         result += handback.hashCode();
      return result;
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer(50);
      buffer.append(getClass()).append(":");
      buffer.append(" listener=").append(getRegisteredListener());
      buffer.append(" filter=")  .append(getRegisteredFilter());
      buffer.append(" handback=").append(getHandback());
      return buffer.toString();
   }
}
