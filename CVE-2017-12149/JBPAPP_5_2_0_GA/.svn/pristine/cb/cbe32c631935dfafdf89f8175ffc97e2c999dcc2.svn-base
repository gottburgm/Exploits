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
package test.implementation.notification;

import java.util.ArrayList;
import java.util.Iterator;

import javax.management.Notification;
import javax.management.NotificationFilterSupport;

import junit.framework.TestCase;

import org.jboss.mx.notification.AsynchNotificationBroadcasterSupport;

import test.implementation.notification.support.Listener;

/**
 * Asynch Notification Broadcaster Support tests.
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */
public class AsynchNotificationBroadcasterSupportTestCase
  extends TestCase
{
   // Attributes ----------------------------------------------------------------

   /**
    * The sent notifications
    */
   private ArrayList sent = new ArrayList();

   /**
    * The next notification sequence
    */
   private long sequence = 0;

   /**
    * The default notification type
    */
   private static final String DEFAULT_TYPE = "DefaultType";

   /**
    * A different notification type
    */
   private static final String ANOTHER_TYPE = "AnotherType";

   /**
    * No notifications
    */
   private static final ArrayList EMPTY = new ArrayList();

   // Constructor ---------------------------------------------------------------

   /**
    * Construct the test
    */
   public AsynchNotificationBroadcasterSupportTestCase(String s)
   {
      super(s);
   }

   // Tests ---------------------------------------------------------------------

   public void testAsynchDelivery()
      throws Exception
   {
      AsynchNotificationBroadcasterSupport broadcaster = new AsynchNotificationBroadcasterSupport();

      Listener listener = new Listener();
      broadcaster.addNotificationListener(listener, null, null);

      clear();
      createNotification(broadcaster);

      compare(EMPTY, received(listener, null));

      listener.doNotify(true);
      listener.doWait(false);

      compare(sent, received(listener, null));
   }

   public void testAsynchDeliveryTwice()
      throws Exception
   {
      AsynchNotificationBroadcasterSupport broadcaster = new AsynchNotificationBroadcasterSupport();

      Listener listener1 = new Listener();
      broadcaster.addNotificationListener(listener1, null, null);
      Listener listener2 = new Listener();
      broadcaster.addNotificationListener(listener2, null, null);

      clear();
      createNotification(broadcaster);

      compare(EMPTY, received(listener1, null));
      compare(EMPTY, received(listener2, null));

      listener1.doNotify(true);
      listener1.doWait(false);

      compare(sent, received(listener1, null));
      compare(EMPTY, received(listener2, null));

      listener2.doNotify(true);
      listener2.doWait(false);

      compare(sent, received(listener2, null));
   }

   // Support -------------------------------------------------------------------

   private void createNotification(AsynchNotificationBroadcasterSupport broadcaster)
   {
      createNotification(broadcaster, DEFAULT_TYPE);
   }

   private void createNotification(AsynchNotificationBroadcasterSupport broadcaster, String type)
   {
      synchronized(this)
      {
         sequence++;
      }
      Notification notification = new Notification(type, broadcaster, sequence);
      sent.add(notification);
      broadcaster.sendNotification(notification);
   }

   private void clear()
   {
      sent.clear();
   }

   private ArrayList apply(ArrayList sent, NotificationFilterSupport filter)
   {
      ArrayList result = new ArrayList();
      for (Iterator iterator = sent.iterator(); iterator.hasNext();)
      {
         Notification notification = (Notification) iterator.next();
         if (filter.isNotificationEnabled(notification))
            result.add(notification);
      }
      return result;
   }

   private ArrayList received(Listener listener, Object object)
   {
      ArrayList result = (ArrayList) listener.notifications.get(object);
      if (result == null)
         result = EMPTY;
      return result;
   }

   private void compare(ArrayList passedSent, ArrayList passedReceived)
      throws Exception
   {
      ArrayList sent = new ArrayList(passedSent);
      ArrayList received = new ArrayList(passedReceived);

      for (Iterator iterator = sent.iterator(); iterator.hasNext();)
      {
          Notification notification = (Notification) iterator.next();
          boolean found = received.remove(notification);
          assertTrue("Expected notification " + notification, found);
      }

      assertTrue("Didn't expect notification(s) " + received, received.isEmpty());
   }
}