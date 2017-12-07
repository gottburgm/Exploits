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
package org.jboss.test.jmx.compliance.notification;

import java.util.ArrayList;
import java.util.Iterator;

import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilterSupport;

import org.jboss.test.jmx.compliance.notification.support.Listener;

import junit.framework.TestCase;

/**
 * Notification Broadcaster Support tests.
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 */
public class NotificationBroadcasterSupportTestCase
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
   public NotificationBroadcasterSupportTestCase(String s)
   {
      super(s);
   }

   // Tests ---------------------------------------------------------------------

   public void testAddNotificationListener()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      broadcaster.addNotificationListener(new Listener(), null, null);

      broadcaster.addNotificationListener(new Listener(), new NotificationFilterSupport(), null);

      broadcaster.addNotificationListener(new Listener(), null, new Object());

      broadcaster.addNotificationListener(new Listener(), new NotificationFilterSupport(), new Object());
   }

   public void testAddNotificationListenerErrors()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      boolean caught = false;
      try
      {
         broadcaster.addNotificationListener(null, null, null);
      }
      catch (IllegalArgumentException e)
      {
         caught = true;
      }
      assertTrue("Expected IllegalArgumentException for null listener", caught);

      caught = false;
      try
      {
         broadcaster.addNotificationListener(null, new NotificationFilterSupport(), null);
      }
      catch (IllegalArgumentException e)
      {
         caught = true;
      }
      assertTrue("Expected IllegalArgumentException for null listener", caught);

      caught = false;
      try
      {
         broadcaster.addNotificationListener(null, null, new Object());
      }
      catch (IllegalArgumentException e)
      {
         caught = true;
      }
      assertTrue("Expected IllegalArgumentException for null listener", caught);

      caught = false;
      try
      {
         broadcaster.addNotificationListener(null, new NotificationFilterSupport(), new Object());
      }
      catch (IllegalArgumentException e)
      {
         caught = true;
      }
      assertTrue("Expected IllegalArgumentException for null listener", caught);
   }

   public void testNotificationWithNoListeners()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      clear();
      createNotification(broadcaster);
   }

   public void testSimpleNotification()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      broadcaster.addNotificationListener(listener, null, null);

      clear();
      createNotification(broadcaster);

      compare(sent, received(listener, null));
   }

   public void testSimpleFilteredNotification()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType(DEFAULT_TYPE);
      broadcaster.addNotificationListener(listener, filter, null);

      clear();
      createNotification(broadcaster);

      compare(apply(sent, filter), received(listener, null));
   }

   public void testSimpleFilteredOutNotification()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.disableType(DEFAULT_TYPE);
      broadcaster.addNotificationListener(listener, filter, null);

      clear();
      createNotification(broadcaster);

      compare(apply(sent, filter), received(listener, null));
   }

   public void testSimpleNotificationWithHandback()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      Object handback = new Object();
      broadcaster.addNotificationListener(listener, null, handback);

      clear();
      createNotification(broadcaster);

      compare(sent, received(listener, handback));
      compare(EMPTY, received(listener, null));
   }

   public void testTwoNotifications()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      broadcaster.addNotificationListener(listener, null, null);

      clear();
      createNotification(broadcaster);
      createNotification(broadcaster);

      compare(sent, received(listener, null));
   }

   public void testTwoNotificationsWithDifferentTypes()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType(DEFAULT_TYPE);
      filter.enableType(ANOTHER_TYPE);
      broadcaster.addNotificationListener(listener, filter, null);

      clear();
      createNotification(broadcaster);
      createNotification(broadcaster, ANOTHER_TYPE);

      compare(apply(sent, filter), received(listener, null));
   }

   public void testTwoNotificationsWithDifferentTypesOneFilteredOut()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType(DEFAULT_TYPE);
      broadcaster.addNotificationListener(listener, filter, null);

      clear();
      createNotification(broadcaster);
      createNotification(broadcaster, ANOTHER_TYPE);

      compare(apply(sent, filter), received(listener, null));
   }

   public void testTwoListeners()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener1 = new Listener();
      broadcaster.addNotificationListener(listener1, null, null);
      Listener listener2 = new Listener();
      broadcaster.addNotificationListener(listener2, null, null);

      clear();
      createNotification(broadcaster);

      compare(sent, received(listener1, null));
      compare(sent, received(listener2, null));
   }

   public void testOneListenerRegisteredTwice()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      broadcaster.addNotificationListener(listener, null, null);
      broadcaster.addNotificationListener(listener, null, null);

      clear();
      createNotification(broadcaster);

      ArrayList copySent = new ArrayList(sent);
      copySent.addAll(sent);
      compare(copySent, received(listener, null));
   }

   public void testOneListenerRegisteredTwiceOneFilteredOut()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      broadcaster.addNotificationListener(listener, null, null);
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.disableType(DEFAULT_TYPE);
      broadcaster.addNotificationListener(listener, filter, null);

      clear();
      createNotification(broadcaster);

      compare(sent, received(listener, null));
   }

   public void testOneListenerRegisteredWithDifferentHandbacks()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      Object handback1 = new Object();
      broadcaster.addNotificationListener(listener, null, handback1);
      Object handback2 = new Object();
      broadcaster.addNotificationListener(listener, null, handback2);

      clear();
      createNotification(broadcaster);

      compare(sent, received(listener, handback1));
      compare(sent, received(listener, handback2));
      compare(EMPTY, received(listener, null));
   }

   public void testOneListenerRegisteredWithDifferentHandbacksOneFilteredOut()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      Object handback1 = new Object();
      broadcaster.addNotificationListener(listener, null, handback1);
      Object handback2 = new Object();
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.disableType(DEFAULT_TYPE);
      broadcaster.addNotificationListener(listener, filter, handback2);

      clear();
      createNotification(broadcaster);

      compare(sent, received(listener, handback1));
      compare(apply(sent, filter), received(listener, handback2));
      compare(EMPTY, received(listener, null));
   }

   public void testOneListenerRegisteredWithTwoBroadcasters()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster1 = new NotificationBroadcasterSupport();
      NotificationBroadcasterSupport broadcaster2 = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      broadcaster1.addNotificationListener(listener, null, null);
      broadcaster2.addNotificationListener(listener, null, null);

      createNotification(broadcaster1);
      createNotification(broadcaster2);

      compare(sent, received(listener, null));
   }

   public void testRemoveListener()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      broadcaster.addNotificationListener(listener, null, null);
      broadcaster.removeNotificationListener(listener);

      createNotification(broadcaster);

      compare(EMPTY, received(listener, null));
   }

   public void testRegisteredTwiceRemoveListener()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      broadcaster.addNotificationListener(listener, null, null);
      broadcaster.addNotificationListener(listener, null, null);
      broadcaster.removeNotificationListener(listener);

      createNotification(broadcaster);

      compare(EMPTY, received(listener, null));
   }

   public void testRegisteredWithFilterRemoveListener()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType(DEFAULT_TYPE);
      broadcaster.addNotificationListener(listener, filter, null);
      broadcaster.removeNotificationListener(listener);

      createNotification(broadcaster);

      compare(EMPTY, received(listener, null));
   }

   public void testRegisteredWithHandbackRemoveListener()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      Object handback = new Object();
      broadcaster.addNotificationListener(listener, null, handback);
      broadcaster.removeNotificationListener(listener);

      createNotification(broadcaster);

      compare(EMPTY, received(listener, null));
      compare(EMPTY, received(listener, handback));
   }

   public void testRegisteredWithFilterHandbackRemoveListener()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      Object handback = new Object();
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType(DEFAULT_TYPE);
      broadcaster.addNotificationListener(listener, filter, handback);
      broadcaster.removeNotificationListener(listener);

      createNotification(broadcaster);

      compare(EMPTY, received(listener, null));
      compare(EMPTY, received(listener, handback));
   }

   public void testRegisterRemoveListenerRegister()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      broadcaster.addNotificationListener(listener, null, null);
      broadcaster.removeNotificationListener(listener);
      broadcaster.addNotificationListener(listener, null, null);

      createNotification(broadcaster);

      compare(sent, received(listener, null));
   }

   public void testRemoveListenerErrors()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();

      boolean caught = false;
      try
      {
         broadcaster.removeNotificationListener(null);
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
      }
      assertTrue("Expected ListenerNotFoundException for null listener", caught);

      caught = false;
      try
      {
         broadcaster.removeNotificationListener(listener);
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
      }
      assertTrue("Expected ListenerNotFoundException for listener never added", caught);

      caught = false;
      try
      {
         broadcaster.addNotificationListener(listener, null, null);
         broadcaster.removeNotificationListener(listener);
         broadcaster.removeNotificationListener(listener);
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
      }
      assertTrue("Expected ListenerNotFoundException for listener remove twice", caught);
   }

   public void testRemoveTripletListenerOnly()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      broadcaster.addNotificationListener(listener, null, null);
      broadcaster.removeNotificationListener(listener, null, null);

      createNotification(broadcaster);

      compare(EMPTY, received(listener, null));
   }

   public void testRemoveTripletListenerFilter()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType(DEFAULT_TYPE);
      broadcaster.addNotificationListener(listener, filter, null);
      broadcaster.removeNotificationListener(listener, filter, null);

      createNotification(broadcaster);

      compare(EMPTY, received(listener, null));
   }

   public void testRemoveTripletListenerHandback()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      Object handback = new Object();
      broadcaster.addNotificationListener(listener, null, handback);
      broadcaster.removeNotificationListener(listener, null, handback);

      createNotification(broadcaster);

      compare(EMPTY, received(listener, null));
      compare(EMPTY, received(listener, handback));
   }

   public void testRemoveTripletListenerFilterHandback()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      Object handback = new Object();
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType(DEFAULT_TYPE);
      broadcaster.addNotificationListener(listener, filter, handback);
      broadcaster.removeNotificationListener(listener, filter, handback);

      createNotification(broadcaster);

      compare(EMPTY, received(listener, null));
      compare(EMPTY, received(listener, handback));
   }

   public void testRegisterTwiceRemoveOnceTriplet()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      Object handback = new Object();
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType(DEFAULT_TYPE);
      broadcaster.addNotificationListener(listener, filter, handback);
      broadcaster.addNotificationListener(listener, filter, handback);
      broadcaster.removeNotificationListener(listener, filter, handback);

      createNotification(broadcaster);

      compare(EMPTY, received(listener, null));
      compare(sent, received(listener, handback));
   }

   public void testRegisterTwiceRemoveTwiceTriplet()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      Object handback = new Object();
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType(DEFAULT_TYPE);
      broadcaster.addNotificationListener(listener, filter, handback);
      broadcaster.addNotificationListener(listener, filter, handback);
      broadcaster.removeNotificationListener(listener, filter, handback);
      broadcaster.removeNotificationListener(listener, filter, handback);

      createNotification(broadcaster);

      compare(EMPTY, received(listener, null));
      compare(EMPTY, received(listener, handback));
   }

   public void testRegisterTwoRemoveOneTripletListener()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      Object handback = new Object();
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType(DEFAULT_TYPE);
      broadcaster.addNotificationListener(listener, filter, handback);
      broadcaster.addNotificationListener(listener, null, null);
      broadcaster.removeNotificationListener(listener, null, null);

      createNotification(broadcaster);

      compare(EMPTY, received(listener, null));
      compare(sent, received(listener, handback));
   }

   public void testRegisterTwoRemoveOneTripletListenerFilter()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      Object handback = new Object();
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType(DEFAULT_TYPE);
      broadcaster.addNotificationListener(listener, filter, handback);
      broadcaster.addNotificationListener(listener, filter, null);
      broadcaster.removeNotificationListener(listener, filter, null);

      createNotification(broadcaster);

      compare(EMPTY, received(listener, null));
      compare(sent, received(listener, handback));
   }

   public void testRegisterTwoRemoveOneTripletListenerHandback()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      Object handback = new Object();
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType(DEFAULT_TYPE);
      broadcaster.addNotificationListener(listener, filter, handback);
      broadcaster.addNotificationListener(listener, null, handback);
      broadcaster.removeNotificationListener(listener, null, handback);

      createNotification(broadcaster);

      compare(EMPTY, received(listener, null));
      compare(sent, received(listener, handback));
   }

   public void testRegisterTwoRemoveOneTripletListenerFilterHandback()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

      Listener listener = new Listener();
      Object handback = new Object();
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType(DEFAULT_TYPE);
      broadcaster.addNotificationListener(listener, null, null);
      broadcaster.addNotificationListener(listener, filter, handback);
      broadcaster.removeNotificationListener(listener, filter, handback);

      createNotification(broadcaster);

      compare(sent, received(listener, null));
      compare(EMPTY, received(listener, handback));
   }

   public void testRemoveTripletErrors()
      throws Exception
   {
      NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();
      Object handback = new Object();
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType(DEFAULT_TYPE);

      Listener listener = new Listener();

      boolean caught = false;
      try
      {
         broadcaster.removeNotificationListener(null, null, null);
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
      }
      assertTrue("Expected ListenerNotFoundException for null listener", caught);

      caught = false;
      try
      {
         broadcaster.removeNotificationListener(listener, null, null);
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
      }
      assertTrue("Expected ListenerNotFoundException for listener never added", caught);

      caught = false;
      try
      {
         broadcaster.addNotificationListener(listener, null, null);
         broadcaster.removeNotificationListener(listener, null, null);
         broadcaster.removeNotificationListener(listener, null, null);
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
      }
      assertTrue("Expected ListenerNotFoundException for listener remove twice", caught);

      caught = false;
      try
      {
         broadcaster.addNotificationListener(listener, filter, null);
         broadcaster.removeNotificationListener(listener, new NotificationFilterSupport(), null);
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
         broadcaster.removeNotificationListener(listener, filter, null);
      }
      assertTrue("Expected ListenerNotFoundException for wrong filter", caught);

      caught = false;
      try
      {
         broadcaster.addNotificationListener(listener, null, handback);
         broadcaster.removeNotificationListener(listener, null, new Object());
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
         broadcaster.removeNotificationListener(listener, null, handback);
      }
      assertTrue("Expected ListenerNotFoundException for wrong handback", caught);

      caught = false;
      try
      {
         broadcaster.addNotificationListener(listener, filter, handback);
         broadcaster.removeNotificationListener(listener, new NotificationFilterSupport(), new Object());
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
         broadcaster.removeNotificationListener(listener, filter, handback);
      }
      assertTrue("Expected ListenerNotFoundException for wrong filter and handback", caught);

      caught = false;
      try
      {
         broadcaster.addNotificationListener(listener, filter, handback);
         broadcaster.removeNotificationListener(listener, null, null);
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
         broadcaster.removeNotificationListener(listener, filter, handback);
      }
      assertTrue("Expected ListenerNotFoundException for listener remove with wrong triplet 1", caught);

      caught = false;
      try
      {
         broadcaster.addNotificationListener(listener, filter, handback);
         broadcaster.removeNotificationListener(listener, filter, null);
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
         broadcaster.removeNotificationListener(listener, filter, handback);
      }
      assertTrue("Expected ListenerNotFoundException for listener remove with wrong triplet 2", caught);

      caught = false;
      try
      {
         broadcaster.addNotificationListener(listener, filter, handback);
         broadcaster.removeNotificationListener(listener, null, handback);
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
         broadcaster.removeNotificationListener(listener, filter, handback);
      }
      assertTrue("Expected ListenerNotFoundException for listener remove with wrong triplet 3", caught);

      caught = false;
      try
      {
         broadcaster.addNotificationListener(listener, filter, null);
         broadcaster.removeNotificationListener(listener, null, null);
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
         broadcaster.removeNotificationListener(listener, filter, null);
      }
      assertTrue("Expected ListenerNotFoundException for listener remove with wrong triplet 4", caught);

      caught = false;
      try
      {
         broadcaster.addNotificationListener(listener, filter, null);
         broadcaster.removeNotificationListener(listener, null, handback);
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
         broadcaster.removeNotificationListener(listener, filter, null);
      }
      assertTrue("Expected ListenerNotFoundException for listener remove with wrong triplet 5", caught);

      caught = false;
      try
      {
         broadcaster.addNotificationListener(listener, filter, null);
         broadcaster.removeNotificationListener(listener, filter, handback);
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
         broadcaster.removeNotificationListener(listener, filter, null);
      }
      assertTrue("Expected ListenerNotFoundException for listener remove with wrong triplet 6", caught);

      caught = false;
      try
      {
         broadcaster.addNotificationListener(listener, null, handback);
         broadcaster.removeNotificationListener(listener, null, null);
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
         broadcaster.removeNotificationListener(listener, null, handback);
      }
      assertTrue("Expected ListenerNotFoundException for listener remove with wrong triplet 7", caught);

      caught = false;
      try
      {
         broadcaster.addNotificationListener(listener, null, handback);
         broadcaster.removeNotificationListener(listener, filter, null);
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
         broadcaster.removeNotificationListener(listener, null, handback);
      }
      assertTrue("Expected ListenerNotFoundException for listener remove with wrong triplet 8", caught);

      caught = false;
      try
      {
         broadcaster.addNotificationListener(listener, null, handback);
         broadcaster.removeNotificationListener(listener, filter, handback);
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
         broadcaster.removeNotificationListener(listener, null, handback);
      }
      assertTrue("Expected ListenerNotFoundException for listener remove with wrong triplet 9", caught);

      caught = false;
      try
      {
         broadcaster.addNotificationListener(listener, null, null);
         broadcaster.removeNotificationListener(listener, filter, null);
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
         broadcaster.removeNotificationListener(listener, null, null);
      }
      assertTrue("Expected ListenerNotFoundException for listener remove with wrong triplet 10", caught);

      caught = false;
      try
      {
         broadcaster.addNotificationListener(listener, null, null);
         broadcaster.removeNotificationListener(listener, null, handback);
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
         broadcaster.removeNotificationListener(listener, null, null);
      }
      assertTrue("Expected ListenerNotFoundException for listener remove with wrong triplet 11", caught);

      caught = false;
      try
      {
         broadcaster.addNotificationListener(listener, null, null);
         broadcaster.removeNotificationListener(listener, filter, handback);
      }
      catch (ListenerNotFoundException e)
      {
         caught = true;
         broadcaster.removeNotificationListener(listener, null, null);
      }
      assertTrue("Expected ListenerNotFoundException for listener remove with wrong triplet 12", caught);
   }

   // Support -------------------------------------------------------------------

   private void createNotification(NotificationBroadcasterSupport broadcaster)
   {
      createNotification(broadcaster, DEFAULT_TYPE);
   }

   private void createNotification(NotificationBroadcasterSupport broadcaster, String type)
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