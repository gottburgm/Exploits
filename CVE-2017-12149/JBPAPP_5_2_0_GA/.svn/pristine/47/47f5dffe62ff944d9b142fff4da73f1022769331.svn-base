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

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.LinkedList;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.jboss.system.ServiceMBeanSupport;

/**
 * 
 * This MBean is an example that shows how to delegate notification services to a HANotificationBroadcaster.
 * Use the sendNotiication() operation to trigger new clustered notifications.
 * Observe the status of each instance of this mbean in the participating cluster partition nodes.
 * 
 * @author  Ivelin Ivanov <ivelin@apache.org>
 *
 */
public class HANotificationBroadcasterClientExample
  extends ServiceMBeanSupport
  implements HANotificationBroadcasterClientExampleMBean, NotificationListener
{

  /**
   * 
   * On service start, subscribes to notification sent by this broadcaster or its remote peers.
   * 
   */
  protected void startService() throws Exception
  {
    super.startService();
    addHANotificationListener(this);
  }

  /**
   * 
   * On service stop, unsubscribes to notification sent by this broadcaster or its remote peers.
   * 
   */
  protected void stopService() throws Exception
  {
    removeHANotificationListener(this);
    super.stopService();
  }

  /**
   * Broadcasts a notification to the cluster partition.
   * 
   * This example does not ensure that a notification sequence number 
   * is unique throughout the partition.
   * 
   */
  public void sendTextMessageViaHANBExample(String message) 
    throws InstanceNotFoundException, MBeanException, ReflectionException
  {
    long now = System.currentTimeMillis();
    Notification notification =
      new Notification(
        "hanotification.example.counter",
        super.getServiceName(),
        now,
        now,
        message);
    server.invoke(
        broadcasterName_,
        "sendNotification",
        new Object[] { notification },
        new String[] { Notification.class.getName() }
        );
  }

  /**
   * Lists the notifications received on the cluster partition
   */
  public Collection getReceivedNotifications()
  {
    return messages_;
  }

  /**
   * @return the name of the broadcaster MBean
   */
  public String getHANotificationBroadcasterName()
  {
    return broadcasterName_ == null ? null : broadcasterName_.toString();
  }

  /**
   * 
   * Sets the name of the broadcaster MBean.
   * 
   * @param 
   */
  public void setHANotificationBroadcasterName(String newBroadcasterName)
    throws InvalidParameterException
  {
    if (newBroadcasterName == null)
    {
      throw new InvalidParameterException("Broadcaster MBean must be specified");
    }
    try
    {
      broadcasterName_ = new ObjectName(newBroadcasterName);
    }
    catch (MalformedObjectNameException mone)
    {
      log.error("Broadcaster MBean Object Name is malformed", mone);
      throw new InvalidParameterException("Broadcaster MBean is not correctly formatted");
    }
  }

  protected void addHANotificationListener(NotificationListener listener) throws InstanceNotFoundException
  {
    server.addNotificationListener(broadcasterName_, listener, 
      /* no need for filter */ null, 
      /* no handback object */ null);
  }

  protected void removeHANotificationListener(NotificationListener listener) throws InstanceNotFoundException, ListenerNotFoundException
  {
    server.removeNotificationListener(broadcasterName_, listener);
  }


  public void handleNotification(
    Notification notification,
    java.lang.Object handback)
  {
    messages_.add(notification.getMessage());
  }
  
  // Attributes ----------------------------------------------

  Collection messages_ = new LinkedList();

  /**
   * The broadcaster MBean that this class listens to and delegates HA notifications to
   */
  ObjectName broadcasterName_ = null;

}
