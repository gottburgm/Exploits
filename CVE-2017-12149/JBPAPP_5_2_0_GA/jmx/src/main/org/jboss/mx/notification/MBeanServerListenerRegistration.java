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

import javax.management.ListenerNotFoundException;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

/**
 * The notification listener registration for a listener in
 * the mbeanserver. The listener is proxied so we can
 * replace the source of the notification with the object name.<p>
 *
 * We also handle the registration with the broadcaster.
 * 
 * @see org.jboss.mx.notification.ListenerRegistry
 * @see org.jboss.mx.notification.ListenerRegistrationFactory
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 81022 $
 */
public class MBeanServerListenerRegistration
   extends DefaultListenerRegistration
{
   // Attributes ----------------------------------------------------

   /**
    * The notification listener proxy
    */
   private NotificationListener proxy;

   private NotificationFilter filterProxy;

   /**
    * The notification broadcaster
    */
   private NotificationBroadcaster broadcaster;

   // Constructor ---------------------------------------------------

   /**
    * Create a listener registration
    *
    * @param name the object name to use as the notifiation source
    * @param broadcaster the notification broadcaster
    * @param listener the notification listener
    * @param filter the notification filter
    * @param handback the handback object  a
    */
   public MBeanServerListenerRegistration(ObjectName name,
                                          NotificationBroadcaster broadcaster,
                                          NotificationListener listener,
                                          NotificationFilter filter,
                                          Object handback)
   {
       super(listener, filter, handback);
       proxy = (NotificationListener) NotificationListenerProxy.newInstance(name, listener);
       this.broadcaster = broadcaster;
       this.filterProxy = (filter==null) ? null : new NotificationFilterProxy(name,filter);
       broadcaster.addNotificationListener(proxy, filterProxy, handback);
   }

   // Public --------------------------------------------------------

   // ListenerRegistration Implementation ---------------------------

   public NotificationListener getListener()
   {
      return proxy;
   }

    public NotificationFilter getFilter()
    {
        return filterProxy;
    }

   public void removed()
   {
      try
      {
         if (broadcaster instanceof NotificationEmitter)
            ((NotificationEmitter) broadcaster).removeNotificationListener(getListener(), getFilter(), getHandback());
         else
            broadcaster.removeNotificationListener(getListener());
      }
      catch (ListenerNotFoundException ignored)
      {
      }
   }
}
