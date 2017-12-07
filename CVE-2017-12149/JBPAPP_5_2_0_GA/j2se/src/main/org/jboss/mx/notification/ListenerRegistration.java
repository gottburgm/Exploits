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
 * A notification listener registration.<p>
 * 
 * @see org.jboss.mx.notification.ListenerRegistry
 * @see org.jboss.mx.notification.ListenerRegistrationFactory
 * 
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 81019 $
 */
public interface ListenerRegistration
{
   /**
    * Retrieve the listener for this registration.
    *
    * @return the listener
    */
   NotificationListener getListener();

   /**
    * Retrieve the filter for this registration.
    *
    * @return the listener
    */
   NotificationFilter getFilter();

   /**
    * Retrieve the handback object for this registration.
    *
    * @return the handback object
    */
   Object getHandback();

   /**
    * Retrieve the listener that was registered.
    *
    * This might be different from listener to use,
    * e.g. the registration factory may generate a proxy.
    *
    * @return the listener
    */
   NotificationListener getRegisteredListener();

   /**
    * Retrieve the filter that was registered.
    *
    * This might be different from filter to use,
    * e.g. the registration factory may generate a proxy.
    *
    * @return the filter
    */
   NotificationFilter getRegisteredFilter();

   /**
    * Used to tell the registration it is no longer required.
    */
   void removed();
}
