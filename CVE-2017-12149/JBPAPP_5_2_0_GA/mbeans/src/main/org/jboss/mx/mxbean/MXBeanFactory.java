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
package org.jboss.mx.mxbean;

import java.lang.reflect.Proxy;

import javax.management.DynamicMBean;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 * MXBeanFactory.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class MXBeanFactory
{
   /**
    * Create a proxy
    * 
    * @param <T> the interface type
    * @param mbeanServerConnection the connection
    * @param objectName the object name
    * @param mxbeanInterface the interface
    * @return the proxy
    */
   public static <T> T makeProxy(MBeanServerConnection mbeanServerConnection, ObjectName objectName, Class<T> mxbeanInterface)
   {
      MXBeanInvocationHandler handler = new MXBeanInvocationHandler(mbeanServerConnection, mxbeanInterface, objectName);
      Object object = Proxy.newProxyInstance(mxbeanInterface.getClassLoader(), new Class[] { mxbeanInterface }, handler);
      return mxbeanInterface.cast(object);
   }
   
   /**
    * Create a new MXBean
    * 
    * @param resource the resource
    * @return the MXBean
    */
   public static DynamicMBean newMXBean(Object resource)
   {
      return MXBeanUtils.createMXBean(resource, null);
   }

   /**
    * Create a new MXBean
    * 
    * @param <T> the interface type
    * @param resource the resource
    * @param mxbeanInterface the interface
    * @return the MXBean
    */
   public static <T> DynamicMBean newMXBean(T resource, Class<T> mxbeanInterface)
   {
      return MXBeanUtils.createMXBean(resource, mxbeanInterface);
   }
}
