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

import javax.management.ObjectName;

import org.jboss.aop.microcontainer.aspects.jmx.JMX;
import org.jboss.dependency.spi.ControllerContext;
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
public abstract class AbstractServiceControllerLifecycleCallback
{
   /** The service controller */
   private ServiceController serviceController;
   
   /** The MBean Registry Object Name */
   protected static ObjectName MBEAN_REGISTRY = ObjectNameFactory.create(ServerConstants.MBEAN_REGISTRY);

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
   
   public abstract void install(ControllerContext context) throws Exception;
   
   public abstract void uninstall(ControllerContext context) throws Exception;
   
   protected JMX readJmxAnnotation(ControllerContext context) throws Exception
   {
      MetaData metaData = context.getScopeInfo().getMetaData();
      if (metaData != null)
         return metaData.getAnnotation(JMX.class);
      return null;
   }
   
   protected ObjectName createObjectName(ControllerContext context, JMX jmx) throws Exception
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
            objectName = new ObjectName("jboss.pojo:name='" + name + "'");            
         }
      }
      
      return objectName;
   }
   
   protected ObjectName determineObjectName(ControllerContext context) throws Exception
   {
      JMX jmx = readJmxAnnotation(context);
      return createObjectName(context, jmx); 
   }
}