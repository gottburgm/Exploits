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
package test.compliance.core.notification;

import javax.management.JMException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import org.jboss.logging.Logger;
import org.jboss.mx.notification.ListenerRegistration;
import org.jboss.mx.notification.ListenerRegistry;

/**
 * A helper class for notification broadcasters/emitters
 *
 * @author <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 81023 $
 */
public class JBossNotificationBroadcasterSupport
      implements NotificationEmitter
{
   /**
    * The log
    */
   private static final Logger log = Logger.getLogger(JBossNotificationBroadcasterSupport.class);

   /**
    * No notifications is the default
    */
   private static final MBeanNotificationInfo[] NO_NOTIFICATIONS = new MBeanNotificationInfo[0];

   /**
    * The registered listeners
    */
   private ListenerRegistry registry = new ListenerRegistry();

   /**
    * Construct the new notification broadcaster support object
    */
   public JBossNotificationBroadcasterSupport()
   {
   }

   public void addNotificationListener(NotificationListener listener,
                                       NotificationFilter filter,
                                       Object handback)
   {
      try
      {
         registry.add(listener, filter, handback);
      }
      catch(JMException e)
      {
         // This shouldn't happen?
         throw new RuntimeException(e.toString());
      }
   }

   public void removeNotificationListener(NotificationListener listener)
         throws ListenerNotFoundException
   {
      registry.remove(listener);
   }

   public void removeNotificationListener(NotificationListener listener,
                                          NotificationFilter filter,
                                          Object handback)
         throws ListenerNotFoundException
   {
      registry.remove(listener, filter, handback);
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      return NO_NOTIFICATIONS;
   }

   public void sendNotification(Notification notification)
   {
      ListenerRegistry.ListenerRegistrationIterator iterator = registry.iterator();
      while(iterator.hasNext())
      {
         ListenerRegistration registration = iterator.nextRegistration();
         NotificationFilter filter = registration.getFilter();
         if(filter == null)
         {
            handleNotification(registration.getListener(), notification, registration.getHandback());
         }
         else if(filter.isNotificationEnabled(notification))
         {
            handleNotification(registration.getListener(), notification, registration.getHandback());
         }
      }
   }

   /**
    * Handle the notification, the default implementation is to synchronously invoke the listener.
    *
    * @param listener     the listener to notify
    * @param notification the notification
    * @param handback     the handback object
    */
   public void handleNotification(NotificationListener listener,
                                  Notification notification,
                                  Object handback)
   {
      try
      {
         listener.handleNotification(notification, handback);
      }
      catch(Throwable ignored)
      {
         log.debug("Ignored unhandled throwable from listener", ignored);
      }
   }
}
