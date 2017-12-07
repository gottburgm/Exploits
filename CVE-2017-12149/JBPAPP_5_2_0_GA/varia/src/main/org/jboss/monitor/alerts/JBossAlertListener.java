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
package org.jboss.monitor.alerts;

import org.jboss.system.ServiceMBeanSupport;

import javax.management.NotificationListener;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.NotificationFilter;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81038 $
 *
 **/
public abstract class JBossAlertListener extends ServiceMBeanSupport implements JBossAlertListenerMBean
{
   private String alertName;

   public String getAlertName()
   {
      return alertName;
   }

   public void setAlertName(String alertName)
   {
      this.alertName = alertName;
   }
}
