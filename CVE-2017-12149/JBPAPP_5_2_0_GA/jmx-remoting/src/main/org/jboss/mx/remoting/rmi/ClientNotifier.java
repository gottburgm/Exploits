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

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.Timer;
import java.io.IOException;

import javax.management.ObjectName;
import javax.management.NotificationListener;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.NotificationResult;
import javax.management.remote.TargetedNotification;

import org.jboss.logging.Logger;
import org.jboss.util.threadpool.BasicThreadPool;
import org.jboss.util.threadpool.BlockingMode;

import EDU.oswego.cs.dl.util.concurrent.ReaderPreferenceReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.SyncMap;

/**
 * @author <a href="mailto:telrod@e2technologies.net">Tom Elrod</a>
 */
/*
 * The ClientNotifier is created within the RMIConnector but only used within the
 * ClientMBeanServerConnection so that there is only one instance of the ClientNotifier
 * per JMXConnector.  This is because the user could get multiple ClientMBeanServerConnections
 * (via RMIConnector::getMBeanServerConnection()), so don't want to create ClientNotifier within
 * ClientMBeanServerConnections because would then multiple of the them to track and close when
 * the connector itself is closed.
 */
public class ClientNotifier extends TimerTask
{
   private Map clientListeners = null;
   private Timer fetchTimer = null;
   private RMIConnection connection = null;

   //TODO: -TME Need to make fetch timeout configurable (JBREM-151)
   private long fetchTimeout = 1000;
   private long clientSequenceNumber = -1;
   private int maxNotifications = 10;

   private BasicThreadPool notifierPool = null;
   private int maxNumberThreads = 20;

   private static final Logger log = Logger.getLogger(ClientNotifier.class);


   public ClientNotifier(RMIConnection rmiConnection)
   {
      this.connection = rmiConnection;
      clientListeners = new SyncMap(new HashMap(), new ReaderPreferenceReadWriteLock());
      fetchTimer = new Timer(true);
      //TODO: -TME Need to make the fetch period configurable (JBREM-151)
      fetchTimer.schedule(this, 1000, 1000);

      notifierPool = new BasicThreadPool("JBoss JMX Remoting client notifier");
      notifierPool.setMaximumPoolSize(maxNumberThreads);
      notifierPool.setBlockingMode(BlockingMode.WAIT);
   }

   /**
    * The action to be performed by this timer task.
    */
   public void run()
   {
      try
      {
         NotificationResult result = connection.fetchNotifications(clientSequenceNumber, maxNotifications, fetchTimeout);
         if(result != null)
         {
            clientSequenceNumber = result.getNextSequenceNumber();
            TargetedNotification[] targetedNotifications = result.getTargetedNotifications();
            if(targetedNotifications != null)
            {
               deliverNotifications(targetedNotifications);
            }
         }
         else
         {
            log.error("Fetched notifications and result was null.");
         }
      }
      catch(IOException e)
      {
         log.error("Error fetching notifications for sequence number " + clientSequenceNumber, e);
      }
   }

   private void deliverNotifications(TargetedNotification[] targetedNotifications)
   {
      for(int x = 0; x < targetedNotifications.length; x++)
      {
         TargetedNotification targetedNotification = targetedNotifications[x];
         Integer id = targetedNotification.getListenerID();
         ClientListenerHolder holder = (ClientListenerHolder)clientListeners.get(id);
         if(holder != null)
         {
            final Notification notification = targetedNotification.getNotification();
            boolean deliverNotification = true;
            if(holder.getFilterOnClient())
            {
               NotificationFilter filter = holder.getFilter();
               if(!filter.isNotificationEnabled(notification))
               {
                  deliverNotification = false;
               }
            }
            if(deliverNotification)
            {
               final NotificationListener listener = holder.getListener();
               final Object handback = holder.getHandback();
               Runnable notifyRun = new Runnable()
               {
                  public void run()
                  {
                     try
                     {
                        listener.handleNotification(notification, handback);
                     }
                     catch(Throwable e)
                     {
                        log.error("Error delivering notification to listener: " + listener, e);
                     }
                  }
               };
               notifierPool.run(notifyRun);
            }
         }
      }
   }

   public boolean exists(ClientListenerHolder holder)
   {
      return clientListeners.containsValue(holder);
   }


   public void addNotificationListener(Integer listenerID, ClientListenerHolder holder)
   {
      clientListeners.put(listenerID, holder);
   }

   public Integer[] getListeners(ObjectName name, NotificationListener listener)
   {
      List idList = new ArrayList();
      Set keys = clientListeners.keySet();
      Iterator itr = keys.iterator();
      while(itr.hasNext())
      {
         Integer id = (Integer)itr.next();
         ClientListenerHolder holder = (ClientListenerHolder)clientListeners.get(id);
         if(holder.getObjectName().equals(name) && holder.getListener().equals(listener))
         {
            idList.add(id);
         }
      }
      Integer[] ids = new Integer[idList.size()];
      ids = (Integer[])idList.toArray(ids);
      return ids;
   }

   public void removeListeners(Integer[] ids)
   {
      for(int x = 0; x < ids.length; x++)
      {
         clientListeners.remove(ids[x]);
      }
   }

   public Integer getListener(ClientListenerHolder clientListenerHolder)
   {
      Iterator itr = clientListeners.entrySet().iterator();
      while(itr.hasNext())
      {
         Map.Entry value = (Map.Entry)itr.next();
         if(value.getValue().equals(clientListenerHolder))
         {
            return (Integer)value.getKey();
         }
      }
      return null;
   }

   public void close()
   {
      fetchTimer.cancel();
   }
}
