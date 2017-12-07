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
package org.jboss.ha.jmx.examples;

import java.util.Collection;
import java.util.LinkedList;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.jboss.ha.jmx.HAServiceMBeanSupport;

/**
 * 
 * This MBean is an example showing how to extend a cluster notification broadcaster 
 * Use the sendNotiication() operation to trigger new clustered notifications.
 * Observe the status of each instance of this mbean in the participating cluster partition nodes.
 * 
 * @author  Ivelin Ivanov <ivelin@apache.org>
 *
 */
public class HANotificationBroadcasterExample
  extends HAServiceMBeanSupport
  implements HANotificationBroadcasterExampleMBean
{
  
  /**
   * 
   * On service start, subscribes to notification sent by this broadcaster or its remote peers.
   * 
   */
  protected void startService() throws Exception
  {
    super.startService();
    addNotificationListener(listener_, /* no need for filter */ null, /* no handback object */ null);
  }
  
  /**
   * 
   * On service stop, unsubscribes to notification sent by this broadcaster or its remote peers.
   * 
   */  
  protected void stopService() throws Exception
  {
    removeNotificationListener(listener_);
    super.stopService();
  }
  
  /**
   * Broadcasts a notification to the cluster partition.
   * 
   * This example does not ensure that a notification sequence number 
   * is unique throughout the partition.
   * 
   */
  public void sendTextMessage(String message)
  {
    long now = System.currentTimeMillis();
    Notification notification =  
      new Notification("hanotification.example.counter", super.getServiceName(), now, now, message);
    sendNotification(notification);
  }

  /**
   * Lists the notifications received on the cluster partition
   */
  public Collection getReceivedNotifications()
  {
    return messages_;
  }


  Collection messages_ = new LinkedList();
 
  NotificationListener listener_ = new NotificationListener()
  {
    public void handleNotification(Notification notification,
                                   java.lang.Object handback)
     {
       messages_.add( notification.getMessage() );
     }
  };
  
}
