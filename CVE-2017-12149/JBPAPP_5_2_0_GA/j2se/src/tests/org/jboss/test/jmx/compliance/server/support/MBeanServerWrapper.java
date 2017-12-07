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
package org.jboss.test.jmx.compliance.server.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.management.MBeanServer;

/**
 * A wrapper for an MBeanServer
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 81019 $
 */
public class MBeanServerWrapper
   implements InvocationHandler
{
   public MBeanServer server;

   public boolean invoked = false;

   private static Method EQUALS;

   static
   {
      try
      {
         EQUALS = Object.class.getClass().getMethod("equals", new Class[] { Object.class });
      }
      catch (Exception e)
      {
         throw new RuntimeException(e.toString());
      }
   }

   public static MBeanServer getWrapper()
   {
      return (MBeanServer) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), 
                                                  new Class[] { MBeanServer.class },
                                                  new MBeanServerWrapper());
   }

   public static MBeanServerWrapper getHandler(MBeanServer proxy)
   {
      return (MBeanServerWrapper) Proxy.getInvocationHandler(proxy);
   }

   public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable
   {
      invoked = true;
      if (method.equals(EQUALS))
         return new Boolean(proxy == args[0]);
      if (method.getName().equals("queryMBeans"))
         throw new MBeanServerReplaced();
      return method.invoke(server, args);
   }
}
