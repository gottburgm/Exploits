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

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.jboss.system.ServiceMBean;



/**
 * 
 * @see org.jboss.ha.jmx.notification.examples.HANotificationBroadcasterExampleMBean
 * 
 * @author  Ivelin Ivanov <ivelin@apache.org>
 *
 */
public interface HANotificationBroadcasterClientExampleMBean
  extends ServiceMBean
{
  /**
   * @return the name of the broadcaster MBean
   */
  public String getHANotificationBroadcasterName();

  /**
   * 
   * Sets the name of the broadcaster MBean.
   * 
   * @param 
   */
  public void setHANotificationBroadcasterName( String newBroadcasterName );

  /**
   * Broadcasts a notification to the cluster partition
   * via the HANBExample MBean
   *
   */
  public void sendTextMessageViaHANBExample(String message) 
    throws InstanceNotFoundException, MBeanException, ReflectionException;
  
  /**
   * Lists the notifications received on the cluster partition
   */
  public Collection getReceivedNotifications();
}
