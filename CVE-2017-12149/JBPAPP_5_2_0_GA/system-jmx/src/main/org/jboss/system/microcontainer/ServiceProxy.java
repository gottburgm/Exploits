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
package org.jboss.system.microcontainer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.system.Service;
import org.jboss.system.ServiceController;

/**
 * An implementation of InvocationHandler used to proxy of the Service
 * interface for mbeans. It determines which of the start/stop
 * methods of the Service interface an mbean implements by inspecting its
 * MBeanOperationInfo values. Each Service interface method that has a
 * matching operation is forwarded to the mbean by invoking the method
 * through the MBeanServer object.<p>
 * 
 * This class is based on the old ServiceConfigurator
 * 
 * @author <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:hiram@jboss.org">Hiram Chirino</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 90438 $
 */
public class ServiceProxy implements InvocationHandler
{
   /**
    * A mapping from the Service interface method names to the corresponding
    * index into the ServiceProxy.hasOp array.
    */
   private static Map<String, Integer> serviceOpMap = new HashMap<String, Integer>();

   // A singleton proxy with no callouts
   private static Service NO_LIFECYCLE_CALLOUT;
   
   /**
    * Initialize the service operation map.
    */
   static
   {
      serviceOpMap.put("create", 0);
      serviceOpMap.put("start", 1);
      serviceOpMap.put("destroy", 2);
      serviceOpMap.put("stop", 3);
      Class<?>[] interfaces = { Service.class };
      NO_LIFECYCLE_CALLOUT = (Service) Proxy.newProxyInstance(Service.class.getClassLoader(), interfaces, NoLifecycleCallout.INSTANCE);
   }

   private boolean[] hasOp = {false, false, false, false};
   private ObjectName objectName;
   private MBeanServer server;

   /** Whether we have the lifecycle method */
   private boolean hasJBossInternalLifecycle;

   /**
    * Get the Service interface through which the mbean given by objectName will be managed.
    *
    * @param objectName the object name
    * @param server the mbean server
    * @return The Service value
    * @throws Exception for any error
    */
   public static Service getServiceProxy(ObjectName objectName, MBeanServer server) throws Exception
   {
      return getServiceProxy(objectName, server, true);
   }

   /**
    * Get the Service interface through which the mbean given by objectName will be managed.
    *
    * @param objectName the object name
    * @param server the mbean server
    * @param includeLifecycle include lifecycle
    * @return The Service value
    * @throws Exception for any error
    */
   public static Service getServiceProxy(ObjectName objectName, MBeanServer server, boolean includeLifecycle) throws Exception
   {
      if (includeLifecycle)
      {
         MBeanInfo info = server.getMBeanInfo(objectName);
         MBeanOperationInfo[] opInfo = info.getOperations();
         Class<?>[] interfaces = { Service.class };
         InvocationHandler handler = new ServiceProxy(objectName, server, opInfo);
         return (Service) Proxy.newProxyInstance(Service.class.getClassLoader(), interfaces, handler);
      }
      else
      {
         return NO_LIFECYCLE_CALLOUT;
      }
   }

   /**
    * Go through the opInfo array and for each operation that matches on of
    * the Service interface methods set the corresponding hasOp array value
    * to true.
    *
    * @param objectName the object name
    * @param server the mbean server
    * @param opInfo the MBean operation info
    */
   public ServiceProxy(ObjectName objectName, MBeanServer server, MBeanOperationInfo[] opInfo)
   {
      this.server = server;
      this.objectName = objectName;

      for (MBeanOperationInfo info : opInfo)
      {
         String name = info.getName();

         if (name.equals(ServiceController.JBOSS_INTERNAL_LIFECYCLE))
         {
            hasJBossInternalLifecycle = true;
            continue;
         }

         Integer opID = serviceOpMap.get(name);
         if (opID == null)
         {
            continue;
         }

         // Validate that is a no-arg void return type method
         if (info.getReturnType().equals("void") == false)
         {
            continue;
         }
         if (info.getSignature().length != 0)
         {
            continue;
         }

         hasOp[opID] = true;
      }
   }

   /**
    * Map the method name to a Service interface method index and if the
    * corresponding hasOp array element is true, dispatch the method to the
    * mbean we are proxying.
    *
    * @param proxy the proxy
    * @param method the method
    * @param args the args
    * @return always null.
    * @throws Throwable for any error
    */
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      String name = method.getName();

      if (hasJBossInternalLifecycle)
      {
         try
         {
            server.invoke(objectName, ServiceController.JBOSS_INTERNAL_LIFECYCLE, new Object[] { name }, ServiceController.JBOSS_INTERNAL_LIFECYCLE_SIG);
            return null;
         }
         catch (Exception e)
         {
            throw JMXExceptionDecoder.decode(e);
         }
      }

      Integer opID = serviceOpMap.get(name);

      if (opID != null && hasOp[opID])
      {
         // deal with those pesky JMX exceptions
         try
         {
            String[] sig = {};
            server.invoke(objectName, name, args, sig);
         }
         catch (Exception e)
         {
            throw JMXExceptionDecoder.decode(e);
         }
      }

      return null;
   }
   
   private static class NoLifecycleCallout implements InvocationHandler
   {
      private static NoLifecycleCallout INSTANCE = new NoLifecycleCallout();
      
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
      {
         return null;
      }
   }
}
