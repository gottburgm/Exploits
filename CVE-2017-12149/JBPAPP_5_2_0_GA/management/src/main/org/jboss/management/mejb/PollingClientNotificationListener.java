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

import javax.management.JMException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

/**
 * Local Polling Listener to receive the message and send to the listener
 *
 * @author Andreas Schaefer
 * @version $Revision: 81025 $
 */
public class PollingClientNotificationListener
        extends ClientNotificationListener
        implements Runnable
{

   private MEJB mConnector;
   private int mSleepingPeriod = 2000;

   public PollingClientNotificationListener(ObjectName pSender,
                                            NotificationListener pClientListener,
                                            Object pHandback,
                                            NotificationFilter pFilter,
                                            int pSleepingPeriod,
                                            int pMaximumListSize,
                                            MEJB pConnector) throws
           JMException,
           RemoteException
   {
      super(pSender, pClientListener, pHandback);
      if (pSleepingPeriod > 0)
      {
         mSleepingPeriod = pSleepingPeriod;
      }
      mConnector = pConnector;
      // Register the listener as MBean on the remote JMX server
      createListener(pConnector,
              "org.jboss.management.mejb.PollingNotificationListener",
              new Object[]{new Integer(pMaximumListSize), new Integer(pMaximumListSize)},
              new String[]{Integer.TYPE.getName(), Integer.TYPE.getName()});
      addNotificationListener(pConnector, pFilter);
      new Thread(this).start();
   }

   public void run()
   {
      while (true)
      {
         try
         {
            try
            {
               List lNotifications = (List) mConnector.invoke(getRemoteListenerName(),
                       "getNotifications",
                       new Object[]{},
                       new String[]{});
               Iterator i = lNotifications.iterator();
               while (i.hasNext())
               {
                  Notification lNotification = (Notification) i.next();
                  mClientListener.handleNotification(lNotification,
                          mHandback);
               }
            }
            catch (Exception e)
            {
               log.error("PollingClientNotificationListener.getNotifications() failed", e);
            }
            Thread.sleep(mSleepingPeriod);
         }
         catch (InterruptedException e)
         {
         }
      }
   }
}
