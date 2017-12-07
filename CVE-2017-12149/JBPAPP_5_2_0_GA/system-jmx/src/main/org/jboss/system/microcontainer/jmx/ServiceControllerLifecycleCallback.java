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
package org.jboss.system.microcontainer.jmx;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.jboss.aop.microcontainer.aspects.jmx.JMX;
import org.jboss.classloading.spi.RealClassLoader;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.dispatch.InvokeDispatchContext;
import org.jboss.logging.Logger;
import org.jboss.metadata.spi.MetaData;
import org.jboss.mx.server.ServerConstants;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceController;

/**
 * ServiceControllerLifecycleCallback.
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class ServiceControllerLifecycleCallback
{
   /** The log */
   private static final Logger log = Logger.getLogger(ServiceControllerLifecycleCallback.class);

   /** The service controller */
   private ServiceController serviceController;
   
   /** The MBean Registry Object Name */
   private static ObjectName MBEAN_REGISTRY = ObjectNameFactory.create(ServerConstants.MBEAN_REGISTRY);

   /**
    * Get the serviceController.
    * 
    * @return the serviceController.
    */
   public ServiceController getServiceController()
   {
      return serviceController;
   }

   /**
    * Set the serviceController.
    * 
    * @param serviceController the serviceController.
    */
   public void setServiceController(ServiceController serviceController)
   {
      this.serviceController = serviceController;
   }

   public void create() throws Exception
   {
      if (serviceController == null)
         throw new IllegalStateException("No service controller configured");
   }
   
   public void install(ControllerContext context) throws Exception
   {
      JMX jmx = readJmxAnnotation(context);
      ObjectName objectName = createObjectName(context, jmx); 

      Class<?> intfClass = null;
      boolean registerDirectly = false;
      if (jmx != null)
      {
         intfClass = jmx.exposedInterface();
         registerDirectly = jmx.registerDirectly();
      }
      // NOTE: The cast to Class is necessary for compilation under JDK6
      Object mbean = (registerDirectly ? context.getTarget() 
                                       : new StandardMBean(context.getTarget(), (Class) intfClass));
      MBeanServer server = serviceController.getMBeanServer();
      ClassLoader cl = null;
      if (context instanceof InvokeDispatchContext)
      {
         try
         {
            cl = ((InvokeDispatchContext) context).getClassLoader();
         }
         catch (Throwable t)
         {
            log.debug("Unable to get classloader from " + context + " " + t);
         }
         if (cl == null)
            cl = Thread.currentThread().getContextClassLoader();
      }
      
      ObjectName classLoaderName = null;
      while (cl != null)
      {
         if (cl instanceof RealClassLoader)
         {
            classLoaderName = ((RealClassLoader) cl).getObjectName();
            break;
         }
         cl = cl.getParent();
      }
      
      if (classLoaderName != null)
      {
         HashMap valueMap = new HashMap();
         valueMap.put(ServerConstants.CLASSLOADER, cl);
         final Object[] args = {mbean, objectName, valueMap};
         final String[] sig = {Object.class.getName(),
         ObjectName.class.getName(), Map.class.getName()};
         server.invoke(MBEAN_REGISTRY, "registerMBean", args, sig);
      }
      else
         server.registerMBean(mbean, objectName);
      try
      {
         serviceController.start(objectName);
      }
      catch (Exception e)
      {
         try
         {
            server.unregisterMBean(objectName);
         }
         catch (Exception t)
         {
            log.debug("Error unregistering mbean", t);
         }
         throw e;
      }
      log.debug("Registered MBean " + objectName);
   }
   
   public void uninstall(ControllerContext context) throws Exception
   {
      JMX jmx = readJmxAnnotation(context);
      ObjectName objectName = createObjectName(context, jmx); 

      try
      {
         log.debug("Unregistering MBean " + objectName);
         serviceController.destroy(objectName);
      }
      finally
      {
         try
         {
            serviceController.remove(objectName);
         }
         catch(Exception e)
         {
            log.debug("Error unregistering mbean", e);
         }
      }
      log.debug("Unregistered MBean " + objectName);
   }
   
   private JMX readJmxAnnotation(ControllerContext context) throws Exception
   {
      MetaData metaData = context.getScopeInfo().getMetaData();
      if (metaData != null)
         return metaData.getAnnotation(JMX.class);
      return null;
   }
   
   private ObjectName createObjectName(ControllerContext context, JMX jmx) throws Exception
   {
      ObjectName objectName = null;
      if (jmx != null)
      {
         String jmxName = jmx.name();
         if (jmxName != null && jmxName.length() > 0)
            objectName = new ObjectName(jmxName);
      }
      
      if (objectName == null)
      {
         // try to build one from the bean name
         String name = (String) context.getName();
         
         if (name.contains(":"))
         {
            objectName = new ObjectName(name);
         }
         else
         {
            objectName = new ObjectName("test:name='" + name + "'");            
         }
      }
      
      return objectName;
   }
}