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
import javax.management.Notification;
import org.jboss.mx.remoting.MBeanLocator;

/**
 * MBeanTrackerAction
 *
 * @author <a href="mailto:jhaynie@vocalocity.net">Jeff Haynie</a>
 * @version $Revision: 81023 $
 */
public interface MBeanTrackerAction extends Serializable
{
   /**
    * called when an MBean is registered with the MBeanServer
    *
    * @param locator
    */
   public void mbeanRegistered(MBeanLocator locator);

   /**
    * called when an MBean is unregistered with the MBeanServer
    *
    * @param locator
    */
   public void mbeanUnregistered(MBeanLocator locator);

   /**
    * called when a mbean notification is fired
    *
    * @param locator
    * @param notification
    * @param handback
    */
   public void mbeanNotification(MBeanLocator locator, Notification notification, Object handback);

   /**
    * called when the mbean state changes.  Note: this method will only be called on MBeans that have a
    * <tt>State</tt> attribute and where state change attribute notifications are fired
    *
    * @param locator
    * @param oldState
    * @param newState
    */
   public void mbeanStateChanged(MBeanLocator locator, int oldState, int newState);
}
