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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.NotificationResult;
import javax.management.remote.TargetedNotification;
import org.jboss.logging.Logger;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;

/**
 * @author <a href="mailto:telrod@e2technologies.net">Tom Elrod</a>
 */
public class ClientNotificationProxy implements NotificationListener
{
   private SynchronizedInt idCounter = new SynchronizedInt(0);
   private Map listenerHolders = new HashMap();
   private List clientListenerNotifications = new ArrayList();

   private long startSequence = 0;
   private long currentSequence = 0;

   public static final int DEFAULT_MAX_NOTIFICATION_BUFFER_SIZE = 1024;
   public static final String MAX_NOTIFICATION_BUFFER_SIZE_KEY = "jmx.remote.notification.buffer.size";

   private int maxNumberOfNotifications = DEFAULT_MAX_NOTIFICATION_BUFFER_SIZE;

   private static final Logger log = Logger.getLogger(ClientNotificationProxy.class);

   public ClientNotificationProxy()
   {
      String maxVal = System.getProperty(MAX_NOTIFICATION_BUFFER_SIZE_KEY);
      if(maxVal != null && maxVal.length() > 0)
      {
         try
         {
            maxNumberOfNotifications = Integer.parseInt(maxVal);
         }
         catch(NumberFormatException e)
         {
            log.error("Could not convert max notification buffer size property value " + maxVal + " to a number.  " +
                      "Will use default value of " + DEFAULT_MAX_NOTIFICATION_BUFFER_SIZE);
         }
      }
   }

   public Integer createListenerId(ObjectName name, NotificationFilter filter)
   {
      Integer id = new Integer(idCounter.increment());
      listenerHolders.put(id, new ClientListenerHolder(name, null, filter, id));
      return id;
   }

   public void handleNotification(Notification notification, Object o)
   {
      Integer id = (Integer) o;
      addClientNotification(id, notification);
   }

   private void addClientNotification(Integer id, Notification notification)
   {
      synchronized(clientListenerNotifications)
      {
         if(clientListenerNotifications.size() == maxNumberOfNotifications)
         {
            clientListenerNotifications.remove(0);
            startSequence++;
         }
         TargetedNotification targetedNotification = new TargetedNotification(notification, id);
         clientListenerNotifications.add(targetedNotification);
         currentSequence++;
      }
   }

   public NotificationFilter removeListener(Integer id)
   {
      ClientListenerHolder holder = (ClientListenerHolder) listenerHolders.remove(id);
      return holder.getFilter();
   }

   public NotificationResult fetchNotifications(long clientSequenceNumber, int maxNotifications, long timeout)
   {
      if(clientSequenceNumber < 0)
      {
         //TODO: -TME this means will be the next notification that comes in (JBREM-150)
      }

      NotificationResult result = null;
      boolean waitForTimeout = true;
      boolean timeoutReached = false;

      int startIndex = 0;

      while(waitForTimeout)
      {
         synchronized(clientListenerNotifications)
         {
            waitForTimeout = false;

            // since the startSequence should be in sync with the first (0) index of the clientListenerNotifications,
            // will use this to determine how far up the index to start.
            if(clientSequenceNumber > startSequence)
            {
               startIndex = (int) (clientSequenceNumber - startSequence);

               if(startIndex > clientListenerNotifications.size())
               {
                  if(timeout > 0 && !timeoutReached)
                  {
                     //need to wait
                     try
                     {
                        clientListenerNotifications.wait(timeout);
                        waitForTimeout = true;
                        timeoutReached = true;
                     }
                     catch(InterruptedException e)
                     {
                        log.debug("Caught InterruptedException waiting for clientListenerNotifications.");
                     }
                  }
                  else
                  {
                     startIndex = clientListenerNotifications.size();
                  }
               }
            }

            int endIndex = maxNotifications > (clientListenerNotifications.size() - startIndex) ? clientListenerNotifications.size() : maxNotifications;

            // handle timeout
            if(endIndex == startIndex)
            {
               if(timeout > 0 && !timeoutReached)
               {
                  //need to wait
                  try
                  {
                     clientListenerNotifications.wait(timeout);
                     waitForTimeout = true;
                     timeoutReached = true;
                  }
                  catch(InterruptedException e)
                  {
                     log.debug("Caught InterruptedException waiting for clientListenerNotifications.");
                  }
               }
            }

            List fetchedNotifications = clientListenerNotifications.subList(startIndex, endIndex);
            TargetedNotification[] targetedNotifications = (TargetedNotification[]) fetchedNotifications.toArray(new TargetedNotification[fetchedNotifications.size()]);

            result = new NotificationResult(clientSequenceNumber, currentSequence, targetedNotifications);
         }
      }

      return result;
   }

   public ClientListenerHolder[] getListeners()
   {
      Collection holders = listenerHolders.values();
      ClientListenerHolder[] holderArray = (ClientListenerHolder[])holders.toArray(new ClientListenerHolder[holders.size()]);
      return holderArray;
   }

}
