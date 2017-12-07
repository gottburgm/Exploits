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
package org.jboss.mx.remoting.rmi;

import javax.management.ObjectName;
import javax.management.NotificationListener;
import javax.management.NotificationFilter;

/**
 * This is a holder for the listener and related information to
 * be stored on the client side for when notifications are received
 * from the server.  This will help in making sure the correct information
 * is supplied back to original listener when calling to handle notifications.
 *
 * @author <a href="mailto:telrod@e2technologies.net">Tom Elrod</a>
 */
public class ClientListenerHolder
{
   private ObjectName targetMBean;
   private NotificationListener listener;
   private NotificationFilter filter;
   private Object handback;
   private boolean filterOnClient = false;

   public ClientListenerHolder(ObjectName objectName, NotificationListener listener,
                               NotificationFilter filter, Object handback)
   {
      this.targetMBean = objectName;
      this.listener = listener;
      this.filter = filter;
      this.handback = handback;
   }

   /**
    * returns the the hashcode based on target mbean, filter, and handback.
    */
   public int hashCode()
   {
      int hashcode = targetMBean.hashCode() + listener.hashCode() + (filter != null ? filter.hashCode() : 0) + (handback != null ? handback.hashCode() : 0);
      return hashcode;
   }

   /**
    * Will check to see object passed is a ClientListenerHolder and
    */
   public boolean equals(Object obj)
   {
      if(obj != null)
      {
         if(obj instanceof ClientListenerHolder)
         {
            ClientListenerHolder holder = (ClientListenerHolder)obj;
            if(targetMBean.equals(holder.getObjectName()) &&
               listener.equals(holder.getListener()))
            {
               if(filter != null)
               {
                  if(filter.equals(holder.getFilter()))
                  {
                     if(handback != null)
                     {
                        return handback.equals(holder.getHandback());
                     }
                     else
                     {
                        if(holder.getHandback() == null)
                        {
                           return true;
                        }
                        else
                        {
                           return false;
                        }
                     }
                  }
                  else
                  {
                     return false;
                  }
               }
               else
               {
                  if(holder.getFilter() == null)
                  {
                     if(handback != null)
                     {
                        return handback.equals(holder.getHandback());
                     }
                     else
                     {
                        if(holder.getHandback() == null)
                        {
                           return true;
                        }
                        else
                        {
                           return false;
                        }
                     }
                  }
                  else
                  {
                     return false;
                  }
               }
            }
            else
            {
               return false;
            }
         }
         else
         {
            return false;
         }
      }
      else
      {
         return false;
      }
   }

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

   public ObjectName getObjectName()
   {
      return targetMBean;
   }

   public void setFilterOnClient(boolean shouldFilterOnClient)
   {
      filterOnClient = shouldFilterOnClient;
   }

   public boolean getFilterOnClient()
   {
      return filterOnClient;
   }
}
