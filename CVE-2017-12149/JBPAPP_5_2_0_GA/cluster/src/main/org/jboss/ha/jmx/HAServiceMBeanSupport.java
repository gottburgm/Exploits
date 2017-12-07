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
package org.jboss.ha.jmx;

import javax.management.Notification;

import org.jboss.ha.framework.interfaces.HAService;
import org.jboss.ha.framework.server.HAServiceImpl;
import org.jboss.ha.framework.server.HAServiceRpcHandler;

/**
 * Management Bean for an HA-Service.
 * Provides a convenient common base for cluster symmetric MBeans.
 * 
 * This class is also a user transparent extension
 * of the standard NotificationBroadcasterSupport
 * to a clustered environment.
 * Listeners register with their local broadcaster.
 * Invoking sendNotification() on any broadcaster,
 * will notify all listeners in the same cluster partition.
 * TODO: The performance can be further optimized by avoiding broadcast messages
 * when remote listener nodes are not interested (e.g. have no local subscribers)
 * or by iterating locally over filters or remote listeners.
 * 
 * @author  Ivelin Ivanov <ivelin@apache.org>
 * @author Paul Ferraro
 * @version $Revision: 87733 $
 *
 */
public class HAServiceMBeanSupport
   extends AbstractHAServiceMBeanSupport<HAService<Notification>>
   implements HAServiceRpcHandler<Notification>
{
   /**
    * @see org.jboss.ha.jmx.AbstractHAServiceMBeanSupport#createHAService()
    */
   @Override
   protected HAService<Notification> createHAService()
   {
      return new HAServiceImpl<Notification>(this, this)
      {
         /**
          * Expose HAServiceMBeanSupport subclass methods to rpc handler
          * @see org.jboss.ha.framework.server.HAServiceImpl#getRpcHandler()
          */
         @Override
         protected HAServiceRpcHandler<Notification> getRpcHandler()
         {
            return HAServiceMBeanSupport.this;
         }
      };
   }
}

