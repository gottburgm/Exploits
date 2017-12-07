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
package org.jboss.management.mejb;

import javax.management.Notification;
import java.util.ArrayList;
import java.util.List;

/**
 * Remote Listener using Polling to send the event
 *
 * @author ???
 * @version $Revision: 81025 $
 * @jmx:mbean extends="org.jboss.management.mejb.ListenerMBean"
 */
public class PollingNotificationListener
        implements PollingNotificationListenerMBean
{
   private List mList;
   private int mMaximumSize = 1000;

   public PollingNotificationListener(int pListSize, int pMaximumListSize)
   {
      if (pListSize <= 0)
      {
         pListSize = 1000;
      }
      mList = new ArrayList(pListSize);
      if (pMaximumListSize > 0 && pMaximumListSize > pListSize)
      {
         mMaximumSize = pMaximumListSize;
      }
   }

   /**
    * Handles the given notification by sending this to the remote
    * client listener
    *
    * @param pNotification Notification to be send
    * @param pHandback     Handback object
    */
   public void handleNotification(Notification pNotification, Object pHandback)
   {
      synchronized (mList)
      {
         if (mList.size() <= mMaximumSize)
         {
            mList.add(pNotification);
         }
      }
   }

   //
   // jason: this is illegal usage of attributes, drop the no-args version
   //

   /**
    * @jmx:managed-attribute
    */
   public List getNotifications()
   {
      return getNotifications(mMaximumSize);
   }

   /**
    * @jmx:managed-attribute
    */
   public List getNotifications(int pMaxiumSize)
   {
      List lReturn = null;
      synchronized (mList)
      {
         pMaxiumSize = pMaxiumSize > mList.size() ? mList.size() : pMaxiumSize;
         lReturn = new ArrayList(mList.subList(0, pMaxiumSize));
         mList.removeAll(lReturn);
      }

      return lReturn;
   }

}
