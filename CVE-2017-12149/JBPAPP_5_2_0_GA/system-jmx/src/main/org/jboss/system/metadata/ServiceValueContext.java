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
package org.jboss.system.metadata;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServer;

import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.system.ServiceController;

/**
 * ServiceValueContext.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class ServiceValueContext
{
   /** The MBeanServer */ 
   private MBeanServer server;
   
   /** The service controller */
   private ServiceController controller;
   
   /** The MBeanAttributeInfo */
   private MBeanAttributeInfo attributeInfo;
   
   /** The ClassLoader */
   private ClassLoader classloader;
   
   /** Whether to trim */
   private boolean trim;
   
   /** Whether to replace */
   private boolean replace;

   /**
    * Create a new ServiceValueContext.
    */
   public ServiceValueContext()
   {
   }

   /**
    * Create a new ServiceValueContext.
    * 
    * @param server the server
    * @param controller the service controller
    * @param classloader the classloader
    */
   public ServiceValueContext(MBeanServer server, ServiceController controller, ClassLoader classloader)
   {
      this.server = server;
      this.controller = controller;
      this.classloader = classloader;
   }

   /**
    * Create a new ServiceValueContext.
    * 
    * @param server the server
    * @param controller the service controller
    * @param attributeInfo the attribute info
    * @param classloader the classloader
    */
   public ServiceValueContext(MBeanServer server, ServiceController controller, MBeanAttributeInfo attributeInfo, ClassLoader classloader)
   {
      this.server = server;
      this.controller = controller;
      this.attributeInfo = attributeInfo;
      this.classloader = classloader;
   }

   /**
    * Get the attributeInfo.
    * 
    * @return the attributeInfo.
    */
   public MBeanAttributeInfo getAttributeInfo()
   {
      return attributeInfo;
   }

   /**
    * Set the attributeInfo.
    * 
    * @param attributeInfo the attributeInfo.
    */
   public void setAttributeInfo(MBeanAttributeInfo attributeInfo)
   {
      this.attributeInfo = attributeInfo;
   }

   /**
    * Get the classloader.
    * 
    * @return the classloader.
    */
   public ClassLoader getClassloader()
   {
      return classloader;
   }

   /**
    * Set the classloader.
    * 
    * @param classloader the classloader.
    */
   public void setClassloader(ClassLoader classloader)
   {
      this.classloader = classloader;
   }

   /**
    * Get the replace.
    * 
    * @return the replace.
    */
   public boolean isReplace()
   {
      return replace;
   }

   /**
    * Set the replace.
    * 
    * @param replace the replace.
    */
   public void setReplace(boolean replace)
   {
      this.replace = replace;
   }

   /**
    * Get the server.
    * 
    * @return the server.
    */
   public MBeanServer getServer()
   {
      if (server == null)
      {
         if (controller == null)
            throw new IllegalStateException("No MBeanServer");
         else
            return controller.getMBeanServer();
      }
      return server;
   }

   /**
    * Set the server.
    * 
    * @param server the server.
    */
   public void setServer(MBeanServer server)
   {
      this.server = server;
   }

   /**
    * Get the service controller.
    * 
    * @return the controller.
    */
   public ServiceController getServiceController()
   {
      if (controller == null)
         throw new IllegalStateException("No ServiceController");
      return controller;
   }

   /**
    * Set the service controller.
    * 
    * @param controller the controller.
    */
   public void setServiceController(ServiceController controller)
   {
      this.controller = controller;
   }

   /**
    * Get the trim.
    * 
    * @return the trim.
    */
   public boolean isTrim()
   {
      return trim;
   }

   /**
    * Set the trim.
    * 
    * @param trim the trim.
    */
   public void setTrim(boolean trim)
   {
      this.trim = trim;
   }
   
   /**
    * Get the kernel
    * 
    * @return the kernel
    */
   public Kernel getKernel()
   {
      return getServiceController().getKernel();
   }
   
   /**
    * Get the controller
    * 
    * @return the controller
    */
   public KernelController getController()
   {
      return getKernel().getController();
   }
}
