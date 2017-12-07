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
package org.jboss.mx.remoting.event;

import javax.management.Notification;
import javax.management.NotificationFilter;

/**
 * Composite-pattern based filter
 *
 * @author Jeff Haynie
 * @version $Revision: 81084 $
 */
public class CompositeEventFilter implements NotificationFilter
{
   static final long serialVersionUID = -4670317046721324670L;
   public static final int AND = 0;
   public static final int OR = 1;

   protected int operator = AND;
   protected NotificationFilter filters[];

   /**
    * create a filter composite of filters using the specific operator
    */
   public CompositeEventFilter(NotificationFilter filters[], int operator)
   {
      this.filters = filters;
      this.operator = operator;
   }

   /**
    * create a filter composite of filters using the AND operator
    */
   public CompositeEventFilter(NotificationFilter filters[])
   {
      this(filters, AND);
   }

   public boolean isNotificationEnabled(Notification event)
   {
      Class cl = event.getClass();
      for(int c = 0; c < filters.length; c++)
      {
         if(operator == AND)
         {
            if(filters[c] != null && filters[c].isNotificationEnabled(event) == false)
            {
               return false;
            }
         }
         else
         {
            if(filters[c] != null && filters[c].isNotificationEnabled(event))
            {
               return true;
            }
         }
      }
      return (operator == AND ? true : false);
   }
}

