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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import org.jboss.logging.Logger;
import org.jboss.remoting.Client;
import org.jboss.remoting.ConnectionFailedException;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.Subsystem;
import org.jboss.remoting.invocation.NameBasedInvocation;

/**
 * MBeanServerClientInvokerProxy is an MBeanServer dynamic proxy that will forward all
 * MBeanServer requests to a remote MBeanServer via a RemoteClientInvoker.
 *
 * @author <a href="mailto:jhaynie@vocalocity.net">Jeff Haynie</a>
 * @version $Revision: 81023 $
 */
public class MBeanServerClientInvokerProxy implements InvocationHandler
{
   private static final Logger log = Logger.getLogger(MBeanServerClientInvokerProxy.class.getName());

   private final String serverId;
   private final String localJmxId;
   private final transient InvokerLocator locator;
   private final transient Client client;
   private final transient Map paramMap = new HashMap();
   private NotificationPoller poller = new NotificationPoller();
   private Timer pollTimer = new Timer(true);
   private static transient Map proxies = new HashMap();
   private transient Set listeners = new HashSet();

   private MBeanServerClientInvokerProxy(ClassLoader cl, InvokerLocator invoker, String localJmxId, String id)
         throws Exception
   {
      this.localJmxId = localJmxId;
      this.serverId = id;
      if(this.serverId == null)
      {
         throw new IllegalArgumentException("MBeanServer ID not found - make sure the NetworkRegistry MBean is running");
      }
      this.client = new Client(cl, invoker, Subsystem.JMX, null);
      this.locator = new InvokerLocator(invoker.getLocatorURI());
      this.proxies.put(id, this);
      setupPollingTimer();
   }

   /**
    * remove the proxy for a given JMX id
    *
    * @param id
    * @return
    */
   public static synchronized MBeanServerClientInvokerProxy remove(String id)
   {
      return (MBeanServerClientInvokerProxy) proxies.remove(id);
   }

   /**
    * get a proxy for a given JMX Id
    *
    * @param id
    * @return
    */
   public static synchronized MBeanServerClientInvokerProxy get(String id)
   {
      return (MBeanServerClientInvokerProxy) proxies.get(id);
   }

   /**
    * setup the polling based on the <tt>pollinterval</tt> locator attribute. <P>
    * <p/>
    * For example, to set the pollinterval to every 2.5 seconds, you would configure
    * the client invoker locator to be:
    * <p/>
    * <CODE><PRE>
    * soap://192.168.10.1/pollinterval=2500
    * </PRE></CODE>
    * <p/>
    * The default interval if not specified is 1000, for every 1 second. You can
    * disable polling by setting the interval to <tt>&lt;=0</tt>.
    */
   protected void setupPollingTimer()
   {
      Map map = this.locator.getParameters();
      long delay = 1000; // default
      if(map != null)
      {
         String ds = (String) map.get("pollinterval");
         if(ds != null)
         {
            delay = Integer.parseInt(ds);
         }
      }
      if(delay > 0)
      {
         this.pollTimer.scheduleAtFixedRate(poller, 2000, delay);
      }
   }

   /**
    * return the remote JMX id
    *
    * @return
    */
   public synchronized String getServerId()
   {
      return this.serverId;
   }

   /**
    * return the invoker locator
    *
    * @return
    */
   public InvokerLocator getLocator()
   {
      return locator;
   }

   public static synchronized MBeanServer create(InvokerLocator locator, String localJmxId, String jmxId) throws Exception
   {
      // always use the same one previously registered if possible
      MBeanServer mbeanserver = MBeanServerRegistry.getMBeanServerFor(locator);
      if(mbeanserver != null)
      {
         return mbeanserver;
      }
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      if(cl == null)
      {
         cl = MBeanServerClientInvokerProxy.class.getClassLoader();
      }
      MBeanServerClientInvokerProxy handler = new MBeanServerClientInvokerProxy(cl, locator, localJmxId, jmxId);
      mbeanserver = (MBeanServer) Proxy.newProxyInstance(MBeanServerClientInvokerProxy.class.getClassLoader(), new Class[]{MBeanServer.class}, handler);
      MBeanServerRegistry.register(mbeanserver, handler);
      log.debug("created MBeanServer proxy to remote mbeanserver at: " + locator + " for JMX Id: " + jmxId + " from our JMX id: " + localJmxId);
      return mbeanserver;
   }

   /**
    * called to destroy the proxy and the invoker
    */
   public synchronized void destroy()
   {
      if(log.isTraceEnabled())
      {
         log.trace("destroy called on: " + this + " for locator: " + locator + ", and serverid: " + serverId);
      }
      client.disconnect();
      MBeanServerRegistry.unregister(this);
      cancelPoller();
      removeListeners();
      paramMap.clear();
      proxies.remove(serverId);
   }

   /**
    * cancel the running poller
    */
   private synchronized void cancelPoller()
   {
      if(poller != null)
      {
         poller.cancel();
         poller = null;
      }
      if(pollTimer != null)
      {
         pollTimer.cancel();
         pollTimer = null;
      }
   }

   private synchronized void removeListeners()
   {
      Iterator iter = listeners.iterator();
      while(iter.hasNext())
      {
         Object key = iter.next();
         ClientListener.remove(key);
         iter.remove();
      }
   }

   private void addNotificationListener(Method m, Object args[], String sig[], Map payload)
         throws Throwable
   {
      String methodName = m.getName();
      Object key = ClientListener.register(serverId, (ObjectName) args[0], args[1], (NotificationFilter) args[2], args[3]);
      listeners.add(key);
      Object a[] = new Object[]{args[0], null, args[2], key};

      // make sure we pass our local id as session id
      client.setSessionId(serverId);
      client.invoke(new NameBasedInvocation(methodName, a, sig),
                    payload);
   }

   private void removeNotificationListener(Method m, Object args[], String sig[], Map payload)
         throws Throwable
   {
      String methodName = m.getName();
      Object id = ClientListener.makeId(serverId, (ObjectName) args[0], args[1]);
      listeners.remove(id);
      ClientListener cl = ClientListener.remove(id);
      Object a[] = new Object[]{args[0], null, id};
      if(cl != null)
      {
         // make sure we pass our local id as session id
         client.setSessionId(serverId);
         client.invoke(new NameBasedInvocation(methodName,
                                               a,
                                               new String[]{ObjectName.class.getName(),
                                                            sig[1],
                                                            Integer.class.getName()}),
                       payload);
      }
   }


   private boolean proxyEquals(Object proxy)
   {
      return (proxy.getClass() == this.getClass() && ((MBeanServerClientInvokerProxy) proxy).serverId.equals(serverId));
   }

   private Integer proxyHashcode()
   {
      return new Integer(client.hashCode() + serverId.hashCode());
   }

   public Object invoke(Object proxy, Method method, Object[] args)
         throws Throwable
   {

      String methodName = method.getName();

      // handle Object.class methods, so we don't go across the wire
      if(method.getDeclaringClass() == Object.class)
      {
         if(methodName.equals("equals"))
         {
            return new Boolean(proxyEquals(args[0]));
         }
         else if(methodName.equals("hashCode"))
         {
            return proxyHashcode();
         }
         else if(methodName.equals("toString"))
         {
            return "MBeanServerClientInvokerProxy [serverid:" + serverId + ",locator:" + client.getInvoker().getLocator() + "]";
         }
      }

      String sig[] = getMethodSignature(method);
      Map payload = new HashMap(1);
      if(methodName.equals("addNotificationListener"))
      {
         addNotificationListener(method, args, sig, payload);
         return null;
      }
      else if(methodName.equals("removeNotificationListener"))
      {
         removeNotificationListener(method, args, sig, payload);
         return null;
      }
      Object value = null;
      try
      {
         // make sure we pass our local id as session id
         client.setSessionId(serverId);
         value = client.invoke(new NameBasedInvocation(methodName, args, sig),
                               payload);
      }
      catch(Throwable throwable)
      {
         if(log.isTraceEnabled())
         {
            log.trace("remote invocation failed for method: " + methodName + " to: " + serverId, throwable);
         }
         if(throwable instanceof ConnectionFailedException)
         {
            destroy();
         }
         rethrowMBeanException(throwable);
      }
      finally
      {
         if(payload != null)
         {
            // if the payload isn't null
            NotificationQueue queue = (NotificationQueue) payload.get("notifications");
            if(queue != null && queue.isEmpty() == false)
            {
               deliverNotifications(queue, false);
            }
         }
      }

      return value;
   }

   private void rethrowMBeanException(Throwable throwable) throws MBeanException, ReflectionException
   {
      if(throwable instanceof MBeanException)
      {
         throw (MBeanException) throwable;
      }
      else if(throwable instanceof ReflectionException)
      {
         throw (ReflectionException) throwable;
      }
      else
      {
         if(throwable instanceof UndeclaredThrowableException)
         {
            UndeclaredThrowableException ut = (UndeclaredThrowableException) throwable;
            if(ut instanceof Exception)
            {
               throw new MBeanException((Exception) ut.getUndeclaredThrowable(), ut.getMessage());
            }
            else
            {
               throw new MBeanException(new Exception(ut.getUndeclaredThrowable().getMessage()));
            }
         }
         else
         {
            if(throwable instanceof Exception)
            {
               throw new MBeanException((Exception) throwable, throwable.getMessage());
            }
            throw new MBeanException(new Exception(throwable.getMessage()));
         }
      }
   }

   public void deliverNotifications(NotificationQueue queue, boolean async) throws InterruptedException
   {
      if(async && poller != null)
      {
         // we're receiving async, kill the poller thread, no need for it
         cancelPoller();
      }
      if(queue == null)
      {
         return;
      }

      Iterator iter = queue.iterator();
      while(iter.hasNext())
      {
         final NotificationEntry entry = (NotificationEntry) iter.next();
         final ClientListener listener = ClientListener.get(entry.getHandBack());
         if(listener != null)
         {
            if(listener.listener instanceof NotificationListener)
            {
               if(log.isTraceEnabled())
               {
                  log.trace("sending notification for entry: " + entry + " to: " + listener.listener);
               }
               try
               {
                  ((NotificationListener) listener.listener).handleNotification(entry.getNotification(), listener.handback);
               }
               catch(Throwable ex)
               {
                  log.error("Error sending notification: " + entry.getNotification() + " to listener: " + listener);
               }
            }
            else
            {
               //ObjectName l = (ObjectName)listener.listener;
               //TODO: implement
               log.error("called unimplemented addListener method", new Exception());
            }
         }
         else
         {
            log.warn("couldn't find client listener for handback: " + entry.getHandBack() + "\nentry:" + entry + "\nqueue: " + queue + "\ndump: " + ClientListener.dump());
         }
      }
   }

   public String[] getMethodSignature(Method method)
   {
      if(paramMap.containsKey(method))
      {
         return (String[]) paramMap.get(method);
      }
      Class paramTypes[] = method.getParameterTypes();
      String sig[] = (paramTypes == null) ? null : new String[paramTypes.length];
      if(paramTypes != null)
      {
         for(int c = 0; c < sig.length; c++)
         {
            sig[c] = paramTypes[c].getName();
         }
      }
      paramMap.put(method, sig);
      return sig;
   }

   /**
    * notification pooler is a timer task that will poll a remote server invoker for
    * notifications and re-dispatch them locally
    */
   private final class NotificationPoller extends TimerTask
   {
      public void run()
      {
         try
         {
            // we only poll if we're connected, and we have active listeners
            // attached remotely
            if(client.isConnected() && ClientListener.hasListeners())
            {
               Map payload = new HashMap(1);
               // transport the special method that will just return null, but will also return
               // the notification queue for the session in the payload
               client.setSessionId(serverId);
               Boolean continuePolling = (Boolean) client.invoke(new NameBasedInvocation("$GetNotifications$",
                                                                                         new Object[]{},
                                                                                         new String[]{}),
                                                                 payload);
               NotificationQueue queue = (NotificationQueue) payload.get("notifications");
               if(queue != null && queue.isEmpty() == false)
               {
                  // we have notifications, deliver locally,
                  deliverNotifications(queue, false);
               }
               if(continuePolling.booleanValue() == false)
               {
                  cancelPoller();
               }
            }
         }
         catch(ConnectionFailedException cnf)
         {
            // remove ourself
            destroy();
         }
         catch(Throwable ex)
         {
            //FIXME - what to do?
            ex.printStackTrace();
         }
      }
   }
}
