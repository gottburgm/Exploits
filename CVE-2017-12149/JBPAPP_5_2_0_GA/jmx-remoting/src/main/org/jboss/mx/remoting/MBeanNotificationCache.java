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
package org.jboss.mx.remoting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import org.jboss.logging.Logger;
import org.jboss.remoting.Client;
import org.jboss.remoting.ConnectionFailedException;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.InvokerRegistry;
import org.jboss.remoting.ServerInvoker;
import org.jboss.remoting.Subsystem;
import org.jboss.remoting.invocation.NameBasedInvocation;
import org.jboss.remoting.network.NetworkNotification;
import org.jboss.remoting.network.NetworkRegistryFinder;
import org.jboss.remoting.transport.ClientInvoker;

import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;

/**
 * MBeanNotificationCache is an object that queues all the server side JMX notifications on behalf
 * of a client invoker.
 *
 * @author <a href="mailto:jhaynie@vocalocity.net">Jeff Haynie</a>
 * @version $Revision: 81023 $
 */
public class MBeanNotificationCache implements NotificationListener
{
   private static final Logger log = Logger.getLogger(MBeanNotificationCache.class.getName());
   private final MBeanServer server;
   private final List listeners = new ArrayList();
   private final Map queue = new HashMap();
   private final ObjectName networkRegistry;
   private final ServerInvoker serverInvoker;
   private final String localServerId;

   public MBeanNotificationCache(ServerInvoker invoker, MBeanServer server)
         throws Exception
   {
      this.server = server;
      this.serverInvoker = invoker;
      this.localServerId = JMXUtil.getServerId(server);

      networkRegistry = NetworkRegistryFinder.find(server);
      if(networkRegistry == null)
      {
         throw new Exception("Couldn't find the required NetworkRegistryMBean in this MBeanServer");
      }
      // add ourself as a listener for detection failed events
      server.addNotificationListener(networkRegistry, this, null, this);
   }

   public void handleNotification(Notification notification, Object o)
   {
      if(notification instanceof NetworkNotification && o != null && this.equals(o))
      {
         String type = notification.getType();
         if(type.equals(NetworkNotification.SERVER_REMOVED))
         {
            // server has failed
            NetworkNotification nn = (NetworkNotification) notification;
            String sessionId = nn.getIdentity().getJMXId();
            List failed = new ArrayList();
            synchronized(listeners)
            {
               Iterator iter = listeners.iterator();
               while(iter.hasNext())
               {
                  Listener listener = (Listener) iter.next();
                  if(sessionId.equals(listener.sessionId))
                  {
                     // just put into a list, so we only sync min time
                     failed.add(listener);
                  }
               }
            }
            if(failed.isEmpty() == false)
            {
               // walk through and remove each listener that has failed
               Iterator iter = failed.iterator();
               while(iter.hasNext())
               {
                  Listener listener = (Listener) iter.next();
                  if(log.isTraceEnabled())
                  {
                     log.trace("++ Removed orphaned listener because server failed: " + nn.getIdentity());
                  }
                  try
                  {
                     removeNotificationListener(listener.locator, listener.sessionId, listener.objectName, listener.handback);
                  }
                  catch(Exception ig)
                  {
                  }
                  listener = null;
               }
               failed = null;
            }
            synchronized(queue)
            {
               queue.remove(sessionId);
            }
         }
      }
   }

   public synchronized void destroy()
   {
      if(log.isTraceEnabled())
      {
         log.trace("destroy call on notification cache");
      }
      synchronized(listeners)
      {
         Iterator iter = listeners.iterator();
         while(iter.hasNext())
         {
            Listener l = (Listener) iter.next();
            try
            {
               removeNotificationListener(l.locator, l.sessionId, l.objectName, l.handback);
            }
            catch(Exception e)
            {
            }
            // remove will remove from the listeners list
         }
      }
      synchronized(queue)
      {
         queue.clear();
      }
      try
      {
         server.removeNotificationListener(networkRegistry, this);
      }
      catch(Exception ig)
      {
      }
   }

   public void addNotificationListener(InvokerLocator clientLocator, String sessionId, ObjectName objectName, NotificationFilter filter, Object handback)
         throws InstanceNotFoundException
   {
      if(log.isTraceEnabled())
      {
         log.trace("remote notification listener added for client [" + clientLocator + "] on objectName [" + objectName + "] and mbeanServerId [" + sessionId + "], filter: " + filter + ", handback: " + handback);
      }
      Listener l = new Listener(clientLocator, sessionId, objectName, filter, handback);
      synchronized(this.listeners)
      {
         if(this.listeners.contains(l) == false)
         {
            this.listeners.add(l);
            server.addNotificationListener(objectName, l, filter, handback);
         }
      }
   }

   public void removeNotificationListener(InvokerLocator clientLocator, String sessionId, ObjectName objectName, Object handback)
         throws InstanceNotFoundException, ListenerNotFoundException
   {
      if(log.isTraceEnabled())
      {
         log.trace("removeNotificationListener called with clientLocator: " + clientLocator + ", sessionId: " + sessionId + ", objectName: " + objectName);
      }
      synchronized(this.listeners)
      {
         Iterator iter = listeners.iterator();
         while(iter.hasNext())
         {
            Listener l = (Listener) iter.next();
            if(l.locator.equals(clientLocator) && l.objectName.equals(objectName) && l.sessionId.equals(sessionId))
            {
               if(log.isTraceEnabled())
               {
                  log.trace("remote notification listener removed for client [" + clientLocator + "] on objectName [" + objectName + "] and MBeanServerId [" + sessionId + "]");
               }
               iter.remove();
               server.removeNotificationListener(objectName, l, l.filter, handback);
               l.destroy();
               l = null;
            }
         }
      }
   }

   /**
    * pull notifications for a given sessionId and return the queue or null if none pending
    *
    * @param sessionId
    * @return
    */
   public NotificationQueue getNotifications(String sessionId)
   {
      synchronized(queue)
      {
         // remove the queue object each time, if it exists, the
         // listener will re-create a new one on each notification
         return (NotificationQueue) queue.remove(sessionId);
      }
   }

   private final class Listener implements NotificationListener
   {
      final ObjectName objectName;
      final Object handback;
      final NotificationFilter filter;
      final InvokerLocator locator;
      final String sessionId;
      private ClientInvoker clientInvoker;
      private Client client;
      private boolean asyncSend = false;
      private LinkedQueue asyncQueue;
      private int counter = 0;
      private BiDirectionClientNotificationSender biDirectionalSender;

      Listener(InvokerLocator locator, String sessionId, ObjectName objectName, NotificationFilter filter, Object handback)
      {
         this.objectName = objectName;
         this.filter = filter;
         this.locator = locator;
         this.sessionId = sessionId;
         this.handback = handback;


         if(serverInvoker.isTransportBiDirectional())
         {
            // attempt connection
            connectAsync();
         }
      }

      synchronized void destroy()
      {
         if(log.isTraceEnabled())
         {
            log.trace("destroy called on client [" + locator + "], session id [" + sessionId + "]");
         }
         try
         {
            removeNotificationListener(locator, sessionId, objectName, handback);
         }
         catch(Throwable e)
         {
         }
         if(biDirectionalSender != null)
         {
            biDirectionalSender.running = false;
            biDirectionalSender.interrupt();
            biDirectionalSender = null;
            while(asyncQueue != null && asyncQueue.isEmpty() == false)
            {
               try
               {
                  asyncQueue.take();
               }
               catch(InterruptedException ex)
               {
                  break;
               }
            }
            asyncQueue = null;
         }
         if(client != null)
         {
            try
            {
               client.disconnect();
            }
            finally
            {
               client = null;
            }
         }
      }

      private void connectAsync()
      {
         try
         {
            if(log.isTraceEnabled())
            {
               log.trace("attempting an bi-directional connection back to client [" + locator + "], server id [" + sessionId + "]");
            }
            // attempt connection back
            clientInvoker = InvokerRegistry.createClientInvoker(locator);
            clientInvoker.connect();
            client = new Client(Thread.currentThread().getContextClassLoader(), clientInvoker, Subsystem.JMX);
            asyncQueue = new LinkedQueue();
            biDirectionalSender = new BiDirectionClientNotificationSender();
            biDirectionalSender.start();
            asyncSend = true;
         }
         catch(Throwable e)
         {
            log.debug("attempted a bi-directional connection back to client [" + locator + "], but it failed", e);
         }
      }

      private final class BiDirectionClientNotificationSender extends Thread
      {
         private boolean running = true;

         public void run()
         {
            NotificationQueue nq = new NotificationQueue(sessionId);
            int count = 0;
            long lastTx = 0;
            while(running)
            {
               try
               {
                  while(count < 10 && !asyncQueue.isEmpty())
                  {
                     // as long as we have entries w/o blocking, add them
                     NotificationEntry ne = (NotificationEntry) asyncQueue.take();
                     nq.add(ne);
                     count++;
                     counter++;
                  }
                  // take up to 10 notifications before forcing a send ,or if we block for
                  // more than 2 secs
                  if((count > 10 || asyncQueue.isEmpty() || System.currentTimeMillis() - lastTx >= 2000) && nq.isEmpty() == false)
                  {
                     // send back to client
                     try
                     {
                        if(log.isTraceEnabled())
                        {
                           log.trace("sending notification queue [" + nq + "] to client [" + locator + "] with sessionId [" + sessionId + "], counter=" + counter + " ,count=" + count);
                        }
                        lastTx = System.currentTimeMillis();
                        client.setSessionId(localServerId);
                        client.invoke(new NameBasedInvocation("$NOTIFICATIONS$",
                                                              new Object[]{nq},
                                                              new String[]{NotificationQueue.class.getName()}),
                                      null);
                     }
                     catch(Throwable t)
                     {
                        if(t instanceof ConnectionFailedException)
                        {
                           if(log.isTraceEnabled())
                           {
                              log.trace("Client is dead during invocation");
                           }
                           Listener.this.destroy();
                           break;
                        }
                        else
                        {
                           log.warn("Error sending async notifications to client: " + locator, t);
                        }
                     }
                     finally
                     {
                        // clear the items in the queue, if any
                        nq.clear();
                        count = 0;
                     }
                  }
                  else if(asyncQueue.isEmpty())
                  {
                     // this will block
                     if(log.isTraceEnabled())
                     {
                        log.trace("blocking on more notifications to arrive");
                     }
                     NotificationEntry ne = (NotificationEntry) asyncQueue.take();
                     nq.add(ne);
                     count += 1;
                     counter++;
                  }
               }
               catch(InterruptedException ex)
               {
                  break;
               }
            }
         }
      }

      public void handleNotification(Notification notification, Object o)
      {
         if(log.isTraceEnabled())
         {
            log.trace("(" + (asyncSend ? "async" : "polling") + ") notification received ..." + notification + " for client [" + locator + "]");
         }
         if(asyncSend == false)
         {
            // not async, we are going to queue for polling ...
            NotificationQueue q = null;

            synchronized(queue)
            {
               // get the queue
               q = (NotificationQueue) queue.get(sessionId);
               if(q == null)
               {
                  // doesn't exist, create it
                  q = new NotificationQueue(sessionId);
                  queue.put(sessionId, q);
               }
               if(log.isTraceEnabled())
               {
                  log.trace("added notification to polling queue: " + notification + " for sessionId: " + sessionId);
               }
               q.add(new NotificationEntry(notification, handback));
            }
         }
         else
         {
            if(asyncQueue != null)
            {
               // this is a bi-directional client, send it immediately
               try
               {
                  asyncQueue.put(new NotificationEntry(notification, handback));
               }
               catch(InterruptedException ie)
               {

               }
            }
         }
      }
   }
}
