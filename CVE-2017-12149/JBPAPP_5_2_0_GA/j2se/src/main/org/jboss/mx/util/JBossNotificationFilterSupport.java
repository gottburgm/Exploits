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
package org.jboss.mx.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.management.Notification;
import javax.management.NotificationFilter;

import org.jboss.util.collection.CollectionsFactory;

/**
 * A replacement for {@link javax.management.NotificationFilterSupport}
 * that avoids synchronization when reading the enabled notification types
 * by using copy-on-write semantics.
 * 
 * Reading operation operate on the latest snapshot of the enabledTypes. 
 * 
 * Mutating operations synchronize on 'this', only because of the
 * addIfAbsent logic in enableType(). This could be avoided by
 * using java.util.concurrent or EDU.oswego.cs.dl.util.concurrent
 * directly, rather than org.jboss.util.CollectionsFactory.
 * 
 * In any case, mutating operations are rare when dealing with
 * NotificationFilters. The common usage is to configure it once and
 * be done with it.
 *  
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>.
 * @version $Revision: 81019 $
 * @since 4.0.3
 */
public class JBossNotificationFilterSupport
   implements NotificationFilter, Serializable
{
   // Constants ---------------------------------------------------

   private static final long serialVersionUID = 6442164418782871672L;

   // Attributes --------------------------------------------------

   /**
    * Enabled notification types.
    */
   private List enabledTypes;

   // Static ------------------------------------------------------

   // Constructors ------------------------------------------------

   /**
    * Default CTOR.
    * 
    * Create a filter that filters out all notification types.
    */
   public JBossNotificationFilterSupport()
   {
      enabledTypes = CollectionsFactory.createCopyOnWriteList();
   }

   // Public ------------------------------------------------------

   /**
    * Disable all notification types. Rejects all notifications.
    */
   public void disableAllTypes()
   {
      synchronized(this)
      {
         enabledTypes.clear();
      }
   }

   /**
    * Disable a notification type.
    *
    * @param type the notification type to disable.
    */
   public void disableType(String type)
   {
      synchronized(this)
      {
         // Null won't be in the list anyway.
         enabledTypes.remove(type);
      }
   }

   /**
    * Enable a notification type.
    *
    * @param type the notification type to enable.
    * @exception IllegalArgumentException for a null type
    */
   public void enableType(String type) throws IllegalArgumentException
   {
      if (type == null)
      {
         throw new IllegalArgumentException("null notification type");
      }
      synchronized(this)
      {
         if (enabledTypes.contains(type) == false)
         {
            enabledTypes.add(type);
         }
      }
   }

   /**
    * Get all the enabled notification types.<p>
    *
    * Returns a vector of enabled notification type.<br>
    * An empty vector means all types disabled.
    *
    * @return the vector of enabled types.
    */
   public Vector getEnabledTypes()
   {
      return new Vector(enabledTypes);
   }

   /**
    * @return human readable string.
    */
   public String toString()
   {
      StringBuffer sb = new StringBuffer(100);
      
      sb.append(getClass().getName()).append(':');
      sb.append(" enabledTypes=").append(getEnabledTypes());
      
      return sb.toString();
   }
   
   // NotificationFilter implementation ---------------------------

   /**
    * Test to see whether this notification is enabled
    *
    * @param notification the notification to filter
    * @return true when the notification should be sent, false otherwise
    * @exception IllegalArgumentException for null notification.
    */
   public boolean isNotificationEnabled(Notification notification)
   {
      if (notification == null)
      {
         throw new IllegalArgumentException("null notification");
      }
      // Check if it is enabled
      String notificationType = notification.getType();
      for (Iterator i = enabledTypes.iterator(); i.hasNext(); )
      {
         String type = (String)i.next();
         if (notificationType.startsWith(type))
         {
            return true;
         }
      }
      return false;
   }

   // Private -----------------------------------------------------
 
}
