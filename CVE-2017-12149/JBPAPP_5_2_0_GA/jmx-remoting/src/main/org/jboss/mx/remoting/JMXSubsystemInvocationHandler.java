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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.NotificationFilter;
import javax.management.ObjectName;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.callback.InvokerCallbackHandler;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.ServerInvocationHandler;
import org.jboss.remoting.ServerInvoker;
import org.jboss.remoting.ident.Identity;
import org.jboss.remoting.invocation.NameBasedInvocation;

/**
 * JMXSubsystemInvocationHandler is a ServerInvocationHandler that will forward requests to the
 * MBeanServer and return the results from the MBeanServer.
 *
 * @author <a href="mailto:jhaynie@vocalocity.net">Jeff Haynie</a>
 * @version $Revision: 81023 $
 */
public class JMXSubsystemInvocationHandler implements ServerInvocationHandler
{
   private static final Logger log = Logger.getLogger(JMXSubsystemInvocationHandler.class);
   private MBeanServer server;
   private MBeanNotificationCache notificationCache;
   private ServerInvoker invoker;
   private Identity identity;

   private static Method getObjectInstance;
   private static Method isRegistered;
   private static Method getAttribute;
   private static Method getAttributes;
   private static Method setAttribute;
   private static Method setAttributes;
   private static Method invoke;
   private static Method getMBeanInfo;

   static
   {
      try
      {
         Class LObject = (new Object[0]).getClass();
         Class LString = (new String[0]).getClass();

         Class[] Sig_ObjectName =
               new Class[]{ObjectName.class};
         Class[] Sig_ObjectName_String =
               new Class[]{ObjectName.class, String.class};
         Class[] Sig_ObjectName_LString =
               new Class[]{ObjectName.class, LString};
         Class[] Sig_ObjectName_Attribute =
               new Class[]{ObjectName.class, Attribute.class};
         Class[] Sig_ObjectName_AttributeList =
               new Class[]{ObjectName.class, AttributeList.class};
         Class[] Sig_ObjectName_String_LObject_LString =
               new Class[]{ObjectName.class, String.class, LObject, LString};

         getObjectInstance = MBeanServer.class.getMethod("getObjectInstance", Sig_ObjectName);
         isRegistered = MBeanServer.class.getMethod("isRegistered", Sig_ObjectName);
         getAttribute = MBeanServer.class.getMethod("getAttribute", Sig_ObjectName_String);
         getAttributes = MBeanServer.class.getMethod("getAttributes", Sig_ObjectName_LString);
         setAttribute = MBeanServer.class.getMethod("setAttribute", Sig_ObjectName_Attribute);
         setAttributes = MBeanServer.class.getMethod("setAttributes", Sig_ObjectName_AttributeList);
         invoke = MBeanServer.class.getMethod("invoke", Sig_ObjectName_String_LObject_LString);
         getMBeanInfo = MBeanServer.class.getMethod("getMBeanInfo", Sig_ObjectName);
      }
      catch(Exception e)
      {
         throw new RuntimeException("Error resolving methods", e);
      }
   }

   public JMXSubsystemInvocationHandler()
   {
      super();
   }

   /**
    * set the invoker that owns this handler
    *
    * @param invoker
    */
   public void setInvoker(ServerInvoker invoker)
   {
      this.invoker = invoker;
   }

   /**
    * set the mbean server that the handler can reference
    *
    * @param server
    */
   public void setMBeanServer(MBeanServer server)
   {
      this.server = server;
      identity = Identity.get(server);
      // make sure our local server is set
      MBeanTransportPreference.setLocalServer(server, identity);
      if(log.isTraceEnabled())
      {
         log.trace("setMBeanServer called with: " + server + " with identity: " + identity);
      }
   }

   /**
    * method is called to destroy the handler and remove all pending notifications and listeners
    * from the notification cache
    */
   public synchronized void destroy()
   {
      if(notificationCache != null)
      {
         notificationCache.destroy();
         notificationCache = null;
      }
   }

   protected void finalize() throws Throwable
   {
      destroy();
      super.finalize();
   }

   /**
    * pull any pending notifications from the queue and place in the return payload
    *
    * @param sessionId
    * @param payload
    */
   private void storeNotifications(String sessionId, Map payload)
   {
      NotificationQueue q = (notificationCache == null) ? null : notificationCache.getNotifications(sessionId);
      if(q != null)
      {
         payload.put("notifications", q);
      }
   }

   public Object invoke(InvocationRequest invocation)
         throws Throwable
   {
      if(this.server == null)
      {
         throw new IllegalStateException("invoke called prior to mbean server being set");
      }
      try
      {
         NameBasedInvocation nbi = (NameBasedInvocation) invocation.getParameter();
         String methodName = nbi.getMethodName();
         Object args [] = nbi.getParameters();
         String signature [] = nbi.getSignature();
         String sessionId = invocation.getSessionId();

         // this method is called by a polling client for notifications
         if(methodName.equals("$GetNotifications$"))  //FIXME- JGH: make this a little better
         {
            //                if (notificationCache!=null && notificationCache.isConnectedBidirectionally(invocation.getClientLocator()))
            //                {
            //                    return new Boolean(false);
            //                }
            // just return, since the finally will automatically stick the queue in the
            // return payload for us.
            return new Boolean(true);
         }
         if(methodName.equals("$NOTIFICATIONS$")) //FIXME- JGH: make this a little better
         {
            // we are receiving async notifications from a remote server
            NotificationQueue queue = (NotificationQueue) args[0];
            MBeanServerClientInvokerProxy p = MBeanServerClientInvokerProxy.get(queue.getSessionID());
            if(p != null)
            {
               if(log.isTraceEnabled())
               {
                  log.trace("received remote notifications for JMX id: " + queue.getSessionID() + ", queue: " + queue);
               }
               p.deliverNotifications(queue, true);
            }
            else
            {
               log.warn("couldn't find a client invoker proxy for mbean serverid: " + queue.getSessionID() + ", dropping notifications [" + queue + "]");
            }
            return null;
         }
         // add and remove are special cases, handle those accordingly
         if(methodName.equals("addNotificationListener") && signature.length == 4)
         {
            // listener field is always null, since we don't send it across
            handleAddNotificationListener(invocation.getLocator(), sessionId, (ObjectName) args[0], (NotificationFilter) args[2], args[3]);
            return null;
         }
         else if(methodName.equals("removeNotificationListener") && signature.length == 3)
         {
            // listener field is always null, since we don't send it across
            handleRemoveNotificationListener(invocation.getLocator(), sessionId, (ObjectName) args[0], args[2]);
            return null;
         }
         Object _args[] = (args == null && signature != null) ? new Object[signature.length] : args;
         // get the mbean server method that's being invoked
         Method method = getMethod(methodName, signature);
         // transport against the mbean server
         return method.invoke(server, _args);
      }
      catch(Throwable ex)
      {
         if(ex instanceof UndeclaredThrowableException)
         {
            UndeclaredThrowableException ut = (UndeclaredThrowableException) ex;
            Throwable ute = ut.getUndeclaredThrowable();
            if(ute instanceof Exception)
            {
               throw new MBeanException((Exception) ute, ut.getUndeclaredThrowable().getMessage());
            }
            else
            {
               throw new MBeanException(new Exception(ute.getMessage()), ute.getMessage());
            }
         }
         if(ex instanceof InvocationTargetException)
         {
            throw ((InvocationTargetException) ex).getTargetException();
         }
         throw ex;
      }
      finally
      {
         // on each invocation, we go ahead and deliver back
         // and pending notifications for this session to the remote
         // end
         if(notificationCache != null)
         {
            storeNotifications(invocation.getSessionId(), invocation.getReturnPayload());
         }
      }
   }

   private synchronized void handleAddNotificationListener(InvokerLocator locator, String sessionId, ObjectName objName, NotificationFilter filter, Object handback)
         throws Throwable
   {
      if(notificationCache == null)
      {
         notificationCache = new MBeanNotificationCache(invoker, server);
      }
      notificationCache.addNotificationListener(locator, sessionId, objName, filter, handback);
   }

   private synchronized void handleRemoveNotificationListener(InvokerLocator locator, String sessionId, ObjectName objName, Object key)
         throws Throwable
   {
      if(notificationCache == null)
      {
         return;
      }
      notificationCache.removeNotificationListener(locator, sessionId, objName, key);
   }

   /**
    * convenience method to lookup the Method object for a given method and signature
    *
    * @param methodName
    * @param sig
    * @return
    * @throws java.lang.Throwable
    */
   private Method getMethod(String methodName, String sig[])
         throws Throwable
   {
      if(methodName.equals("invoke"))
      {
         return invoke;
      }
      else if(methodName.equals("getAttribute"))
      {
         return getAttribute;
      }
      else if(methodName.equals("setAttribute"))
      {
         return setAttribute;
      }
      else if(methodName.equals("getAttributes"))
      {
         return getAttributes;
      }
      else if(methodName.equals("setAttributes"))
      {
         return setAttributes;
      }
      else if(methodName.equals("setAttributes"))
      {
         return setAttributes;
      }
      else if(methodName.equals("getMBeanInfo"))
      {
         return getMBeanInfo;
      }
      else if(methodName.equals("getObjectInstance"))
      {
         return getObjectInstance;
      }
      else if(methodName.equals("isRegistered"))
      {
         return isRegistered;
      }

      Class[] params = null;
      if(sig != null)
      {
         params = new Class[sig.length];
         for(int i = 0; i < sig.length; ++i)
         {
            params[i] = Class.forName(sig[i]);
         }
      }
      return MBeanServer.class.getMethod(methodName, params);
   }

   //NOTE: These were added as part of the new remoting callback,
   // but not yet implemented (need to compile). JMX remoting should
   // still work using the old way. -TME
   public void addListener(InvokerCallbackHandler callbackHandler)
   {
      //TODO: Need to implement -TME
   }

   public void removeListener(InvokerCallbackHandler callbackHandler)
   {
      //TODO: Need to implement -TME
   }
}
