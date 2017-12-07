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

import java.util.HashMap;
import java.util.Iterator;

import javax.management.JMException;
import javax.management.ListenerNotFoundException;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

/**
 * A notification listener registry per ObjectName.
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>
 *
 * @version $Revision: 81022 $
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
public class MBeanServerListenerRegistry
{
   // Attributes ----------------------------------------------------

   /**
    * A map of object names to listener registries.
    */
   private HashMap registries = new HashMap();

   // Constructor ---------------------------------------------------

   /**
    * Create a notification listener registry
    */
   public MBeanServerListenerRegistry()
   {
   }

   // Public --------------------------------------------------------

   /**
    * Adds a listener to the mbean
    *
    * @param name the object name
    * @param broadcaster the broadcaster
    * @param listener the listener to register
    * @param filter filters the notifications in the broadcaster, can be null
    * @param handback the object to include in the notification, can be null
    * @exception IllegalArgumentException for a null object name or listener
    */
   public void add(ObjectName name, NotificationBroadcaster broadcaster,
                   NotificationListener listener, NotificationFilter filter, Object handback)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");
      if (listener == null)
         throw new IllegalArgumentException("Null listener");

      ListenerRegistry registry = null;
      synchronized(registries)
      {
         registry = (ListenerRegistry) registries.get(name);
         if (registry == null)
            registry = new ListenerRegistry(new MBeanServerListenerRegistrationFactory(name, broadcaster));
         registries.put(name, registry);
      }

      try
      {
         registry.add(listener, filter, handback);
      }
      catch (JMException e)
      {
         // This shouldn't happen
         throw new RuntimeException(e.toString());
      }
   }

   /**
    * Removes all registrations for an mbean broadcaster.
    *
    * @param name the object name
    * @exception IllegalArgumentException for a null object name
    */
   public void remove(ObjectName name)
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");

      ListenerRegistry registry = null;
      synchronized(registries)
      {
         registry = (ListenerRegistry) registries.remove(name);
         if (registry == null)
            return;
      }

      // Remove the registrations with the MBean
      for (ListenerRegistry.ListenerRegistrationIterator iterator = registry.iterator(); iterator.hasNext();)
      {
         ListenerRegistration registration = iterator.nextRegistration();
         registration.removed();
      }
   }

   /**
    * Removes all registrations for a listener.
    *
    * @param name the object name
    * @param listener the listener to remove
    * @exception ListenerNotFoundException when the listener is not registered
    * @exception IllegalArgumentException for a null object name
    */
   public void remove(ObjectName name, NotificationListener listener)
      throws ListenerNotFoundException
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");

      synchronized(registries)
      {
         ListenerRegistry registry = (ListenerRegistry) registries.get(name);
         if (registry == null)
            throw new ListenerNotFoundException("Listener not found " + listener + " for object name " + name);

         registry.remove(listener);
         if (registry.isEmpty())
            registries.remove(name);
      }
   }

   /**
    * Removes only the registrations for a listener that match the filter and handback.
    *
    * @param name the object name
    * @param listener the listener to remove
    * @param filter the filter of the registration to remove
    * @param handback the handback object of the registration to remove
    * @exception ListenerNotFoundException when the listener is not registered
    * @exception IllegalArgumentException for a null object name
    */
   public void remove(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback)
      throws ListenerNotFoundException
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");

      synchronized(registries)
      {
         ListenerRegistry registry = (ListenerRegistry) registries.get(name);
         if (registry == null)
            throw new ListenerNotFoundException("Listener not found listener=" + listener +
                                                " filter=" + filter + " handback=" + handback +
                                                " for object name " + name);

         registry.remove(listener, filter, handback); 
         if (registry.isEmpty())
            registries.remove(name);
      }
   }

   /**
    *  Clears all listener registries from this registry.
    */
   public void removeAll()
   {
      synchronized (registries)
      {
         Iterator it = registries.keySet().iterator();

         while (it.hasNext())
         {
            ListenerRegistry registry = (ListenerRegistry)registries.get(it.next());
            registry.removeAll();
         }
      }
   }

}
