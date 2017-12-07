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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.management.JMException;
import javax.management.ListenerNotFoundException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

/**
 * A notification listener registry.<p>
 *
 * For addition and removal, the registrations are deeply cloned to 
 * allow the registrations to be iterated externally without 
 * incurring the cost of synchronization.
 * 
 * @see org.jboss.mx.notification.ListenerRegistration
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 *
 * @version $Revision: 81019 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20030806 Juha Lindfors:</b>
 * <ul>
 * <li>
 *   Added removeAll()
 * </ul>
 *
 */
public class ListenerRegistry
{
   // Attributes ----------------------------------------------------

   /**
    * A map of listeners to a list of NotificationRegistrations.
    */
   private HashMap listeners = new HashMap();

   /**
    * The factory used to generate the listener registration.
    */
   private ListenerRegistrationFactory factory;


   // Constructor ---------------------------------------------------

   /**
    * Create a notification listener registry using the default
    * listener registration factory.
    */
   public ListenerRegistry()
   {
      this(null);
   }

   /**
    * Create a notification listener registry using the passed
    * listener registration factory.<p>
    *
    * @param factory the factory to create registrations, use
    *        null for the default factory
    */
   public ListenerRegistry(ListenerRegistrationFactory factory)
   {
      if (factory == null)
         this.factory = new DefaultListenerRegistrationFactory();
      else
         this.factory = factory;
   }

   // Public --------------------------------------------------------

   /**
    * Adds a listener to a broadcaster<p>
    *
    * @param listener the listener to register
    * @param filter filters the notifications in the broadcaster, can be null
    * @param handback the object to include in the notification, can be null
    * @exception IllegalArgumentException for a null listener
    * @exception JMException for an error adding to the registry
    */
   public void add(NotificationListener listener, NotificationFilter filter, Object handback)
      throws JMException
   {
      if (listener == null)
         throw new IllegalArgumentException("Null listener");

      synchronized(listeners)
      {
         HashMap newListeners = (HashMap) listeners.clone();

         ArrayList registrations = (ArrayList) newListeners.get(listener);
         if (registrations == null)
         {
            registrations = new ArrayList();
            newListeners.put(listener, registrations);
         }
         else
         {
            registrations = (ArrayList) registrations.clone();
            newListeners.put(listener, registrations);
         }

         registrations.add(factory.create(listener, filter, handback));

         listeners = newListeners;
      }
   }

   /**
    * Removes all registrations for a listener.
    *
    * @param listener the listener to remove
    * @exception ListenerNotFoundException when the listener is not registered
    */
   public void remove(NotificationListener listener)
      throws ListenerNotFoundException
   {
      ArrayList registrations = null;
      synchronized(listeners)
      {
         if (listeners.containsKey(listener) == false)
            throw new ListenerNotFoundException("Listener not found " + listener);

         HashMap newListeners = (HashMap) listeners.clone();

         registrations = (ArrayList) newListeners.remove(listener);

         listeners = newListeners;
      }

      for (Iterator iterator = registrations.iterator(); iterator.hasNext();)
      {
          ListenerRegistration registration = (ListenerRegistration) iterator.next();
          registration.removed();
      }
   }

   /**
    * Removes only the registrations for a listener that match the filter and handback.
    *
    * @param listener the listener to remove
    * @param filter the filter of the registration to remove
    * @param handback the handback object of the registration to remove
    * @exception ListenerNotFoundException when the listener is not registered
    */
   public void remove(NotificationListener listener, NotificationFilter filter, Object handback)
      throws ListenerNotFoundException
   {
      ListenerRegistration registration = null;
      synchronized(listeners)
      {
         ArrayList registrations = (ArrayList) listeners.get(listener);
         if (registrations == null)
            throw new ListenerNotFoundException("No registristrations for listener not listener=" + listener +
                                                " filter=" + filter + " handback=" + handback);

         registration = new DefaultListenerRegistration(listener, filter, handback);
         int index = registrations.indexOf(registration);
         if (index == -1)
            throw new ListenerNotFoundException("Listener not found listener=" + listener +
                                                " filter=" + filter + " handback=" + handback);

         HashMap newListeners = (HashMap) listeners.clone();

         registrations = (ArrayList) registrations.clone();
         registration = (ListenerRegistration) registrations.remove(index);
         if (registrations.isEmpty())
            newListeners.remove(listener);
         else
            newListeners.put(listener, registrations);

         listeners = newListeners;
      }

      registration.removed();
   }

   /**
    *  Removes all listeners from this listener registry.
    */
   public void removeAll()
   {
      synchronized (listeners)
      {
         listeners.clear();
      }
   }

   /**
    * Retrieve an iterator over the registrations<p>
    *
    * The iterator behaves like a snapshot of the registrations
    * is taken during this operation.
    *
    * @return the iterator
    */
   public ListenerRegistrationIterator iterator()
   {
      return new ListenerRegistrationIterator();
   }

   /**
    * Test whether the registry is empty
    *
    * @return true when it is empty, false otherwise
    */
   public boolean isEmpty()
   {
      return listeners.isEmpty();
   }

   // Inner Classes -------------------------------------------------

   public class ListenerRegistrationIterator
      implements Iterator
   {
      private Iterator listenerIterator;
      private Iterator registrationIterator;

      /**
       * Constructs a new ListenerRegistration iterator
       */
      public ListenerRegistrationIterator()
      {
         listenerIterator = listeners.values().iterator();
      }

      public boolean hasNext()
      {
         if (registrationIterator == null || registrationIterator.hasNext() == false)
         {
            do
            {
               if (listenerIterator.hasNext() == false)
                  return false;

               registrationIterator = ((ArrayList) listenerIterator.next()).iterator();
            }
            while (registrationIterator.hasNext() == false);
         }
         return true;
      }

      public Object next()
      {
         if (hasNext() == false)
            throw new NoSuchElementException("Use hasNext before next");

         return registrationIterator.next();
      }

      /**
       * Convenience method to returned a typed object
       */
      public ListenerRegistration nextRegistration()
      {
         return (ListenerRegistration) next();
      }

      /**
       * @exception UnsupportedOpertionException remove is not supported
       */
      public void remove()
      {
         throw new UnsupportedOperationException("remove is not supported");
      }
   }
}
