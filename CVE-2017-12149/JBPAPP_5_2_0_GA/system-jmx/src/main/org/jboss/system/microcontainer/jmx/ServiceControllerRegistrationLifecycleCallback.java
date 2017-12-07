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
import org.jboss.mx.server.ServerConstants;

/**
 * ServiceControllerLifecycleCallback.
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class ServiceControllerRegistrationLifecycleCallback extends AbstractServiceControllerLifecycleCallback
{
   /** The log */
   private static final Logger log = Logger.getLogger(ServiceControllerRegistrationLifecycleCallback.class);
   
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
      MBeanServer server = getServiceController().getMBeanServer();
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
         final Object[] args = {mbean, objectName, setUpClassLoaderProperty(cl)};
         final String[] sig = {Object.class.getName(),
         ObjectName.class.getName(), Map.class.getName()};
         server.invoke(MBEAN_REGISTRY, "registerMBean", args, sig);
      }
      else
         server.registerMBean(mbean, objectName);
      try
      {
         // Don't include the lifecycle callouts unless we know the MBean implementation
         // wants them and supports "double invocation"
         getServiceController().register(objectName, null, false, context.getTarget());
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
      ObjectName objectName = determineObjectName(context); 
      try
      {
         getServiceController().remove(objectName);
      }
      catch(Exception e)
      {
         log.debug("Error unregistering mbean", e);
      }
      log.debug("Unregistered MBean " + objectName);
   }
   
   @SuppressWarnings("unchecked")
   protected HashMap setUpClassLoaderProperty(ClassLoader cl)
   {
      HashMap valueMap = new HashMap();
      valueMap.put(ServerConstants.CLASSLOADER, cl);
      return valueMap;
   }
}