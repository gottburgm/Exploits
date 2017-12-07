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
package org.jboss.mx.remoting.tracker;

import java.io.Serializable;
import java.util.List;
import javax.management.AttributeChangeNotification;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.ObjectName;

/**
 * MBeanTrackerFilter
 *
 * @author <a href="mailto:jhaynie@vocalocity.net">Jeff Haynie</a>
 * @version $Revision: 81023 $
 */
public class MBeanTrackerFilter implements NotificationFilter, Serializable
{
   static final long serialVersionUID = -4239778153506655691L;
   protected final String classNames[];
   protected final String serverId;
   protected final boolean wantNotifications;

   public MBeanTrackerFilter(String serverId, String cn[], boolean wantNotifications)
   {
      this.serverId = serverId;
      this.classNames = cn;
      this.wantNotifications = wantNotifications;
   }

   public boolean isNotificationEnabled(Notification notification)
   {
      if(notification instanceof MBeanServerNotification)
      {
         MBeanServerNotification n = (MBeanServerNotification) notification;
         ObjectName mbean = n.getMBeanName();
         // find the server using the server id
         List list = MBeanServerFactory.findMBeanServer(serverId);
         if(list.isEmpty() == false)
         {
            MBeanServer server = (MBeanServer) list.get(0);
            if(notification.getType().equals(MBeanServerNotification.REGISTRATION_NOTIFICATION))
            {
               if(classNames == null)
               {
                  return true;
               }
               for(int c = 0; c < classNames.length; c++)
               {
                  try
                  {
                     if(server.isInstanceOf(mbean, classNames[c]))
                     {
                        // add an interest, since we can't call this same method later when
                        // the mbean is unregistered
                        return true;
                     }
                  }
                  catch(Exception ex)
                  {
//                            ex.printStackTrace();
                  }
               }
            }
            else if(notification.getType().equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION))
            {
               return false == (mbean.getDomain().equals("mx.remoting") ||
                                mbean.getDomain().equals("JMImplementation"));
            }
         }

      }
      else if(notification instanceof AttributeChangeNotification)
      {
         // we want state changes directly
         AttributeChangeNotification ch = (AttributeChangeNotification) notification;
         if(ch.getAttributeName().equals("State") &&
            (ch.getAttributeType().equals(Integer.TYPE.getName()) || ch.getAttributeType().equals(Integer.class.getName())))
         {
            return true;
         }
      }
      if(wantNotifications)
      {
         Object src = notification.getSource();
         if(src instanceof ObjectName)
         {
            ObjectName obj = (ObjectName) src;
            // find the server using the server id
            List list = MBeanServerFactory.findMBeanServer(serverId);
//                log.debug("list of servers=="+list);
            if(list.isEmpty() == false)
            {
               MBeanServer server = (MBeanServer) list.get(0);
               if(classNames == null)
               {
                  return true;
               }
               for(int c = 0; c < classNames.length; c++)
               {
                  try
                  {
                     if(server.isInstanceOf(obj, classNames[c]))
                     {
                        // add an interest, since we can't call this same method later when
                        // the mbean is unregistered
                        return true;
                     }
                  }
                  catch(InstanceNotFoundException inf)
                  {
                     return false;
                  }
                  catch(Exception ex)
                  {
                     ex.printStackTrace();
                  }
               }
            }
         }
      }
      return false;
   }

}

