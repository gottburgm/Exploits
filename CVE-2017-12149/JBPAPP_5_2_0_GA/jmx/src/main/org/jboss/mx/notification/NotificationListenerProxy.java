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
package org.jboss.mx.notification;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

/**
 * A notification listener used to forward notifications to listeners
 * added through the mbean server.<p>
 *
 * The original source is replaced with the object name.
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @author  <a href="mailto:telrod@e2technologies.net">Tom Elrod</a>.
 * @version $Revision: 81022 $
 */
public class NotificationListenerProxy
   implements InvocationHandler
{
   // Constants ---------------------------------------------------

   // Attributes --------------------------------------------------

    /**
    * The original listener
    */
   private NotificationListener listener;

   /**
    * The object name we are proxying
    */
   private ObjectName name;

   /**
    * The implementation method for NotificationListener
    * that we want to intercept so that we can set the source
    * of the Notification object.
    */
   private static final String METHODNAME = "handleNotification";

   /**
    * Calculated hascode
    */
   private final Integer hashCode;

   // Static ------------------------------------------------------

     public static Object newInstance(ObjectName name,
                                      NotificationListener listener)
     {
         // Using set so don't have interface duplicates (which shouldn't happen anyways)
         java.util.HashSet set = new java.util.HashSet();

         // Walk the class heirarchy tree and get all interfaces.
         Class currentClass = listener.getClass();
         while(currentClass != null)
         {
             Class[] classInterfaces = currentClass.getInterfaces();
             for(int i = 0; i < classInterfaces.length; i++)
             {
                set.add(classInterfaces[i]);
             }
             currentClass = currentClass.getSuperclass();
         }
         Class[] interfaces = new Class[set.size()];
         interfaces = (Class[])set.toArray(interfaces);

         return Proxy.newProxyInstance(listener.getClass().getClassLoader(),
                                       interfaces,
                                       new NotificationListenerProxy(name, listener));
     }

   // Constructors ------------------------------------------------

   /**
    * Create a new Notification Listener Proxy
    *
    * @param name the object name
    * @param listener the original listener
    */
   public NotificationListenerProxy(ObjectName name,
                                    NotificationListener listener)
   {
      this.name = name;
      this.listener = listener;
      this.hashCode = new Integer(System.identityHashCode(this));

      // Could add code to set the METHODNAME variable based
      // on the method signature names from the actual listener
      // interface passed (which would be done dynamically so would
      // not be hard-coded), in case the NotificationListener
      // method signature changes.
   }

   // Public ------------------------------------------------------

    // implementation InvocationHandler
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable
    {
        String localMethodName = method.getName();
        // check to see if calling handleNotification() method
        if(localMethodName.equals(METHODNAME))
        {
            for(int x = 0; x < args.length; x++)
            {
                if(args[x] instanceof Notification)
                {
                    // Forward the notification with the object name as source
                    // FIXME: This overwrites the original source, there is no way
                    //        to put it back with the current spec
                    ((Notification)args[x]).setSource(name);
                }
            }
        }
        else if (localMethodName.equals("hashCode"))
        {
            return proxyHashCode(proxy);
        }
        else if (localMethodName.equals("equals"))
        {
            return proxyEquals(proxy, args[0]);
        }
        else if (localMethodName.equals("toString"))
        {
            return proxyToString(proxy);
        }
        return method.invoke(listener, args);
    }

    protected Integer proxyHashCode(Object proxy)
    {
        return this.hashCode;
    }

    protected Boolean proxyEquals(Object proxy, Object other)
    {
        return (proxy == other ? Boolean.TRUE : Boolean.FALSE);
    }

    protected String proxyToString(Object proxy)
    {
        return proxy.getClass().getName() + '@' + Integer.toHexString(proxy.hashCode());
    }

   // overrides ---------------------------------------------------

   // Protected ---------------------------------------------------

   // Private -----------------------------------------------------

   // Inner classes -----------------------------------------------
}
