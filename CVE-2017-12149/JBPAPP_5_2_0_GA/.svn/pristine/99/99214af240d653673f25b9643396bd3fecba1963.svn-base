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

package org.jboss.web.tomcat.service.deployers;

import org.jboss.dependency.spi.ControllerContext;
import org.jboss.kernel.spi.dependency.KernelController;

/**
 * Service locator utility for finding microcontainer beans associated with
 * the <code>KernelController</code> that manages the local {@link TomcatService}.
 * 
 * @author Brian Stansberry
 *
 */
public class JBossWebMicrocontainerBeanLocator
{
   private static KernelController kernelController;
   
   /**
    * Returns the bean installed under the given name.
    * 
    * @param beanName the name of the bean
    * @return the bean, or <code>null</code> if no bean is installed under <code>name</code>
    * 
    * @throws IllegalStateException if no KernelController is available to perform
    *                               the lookup
    */
   public static Object getInstalledBean(Object beanName)
   {
      if (kernelController == null)
      {
         throw new IllegalStateException("KernelController not installed");
      }
      
      ControllerContext context = kernelController.getInstalledContext(beanName);
      return context == null ? null : context.getTarget();
   }
   
   static void setKernelController(KernelController controller)
   {
      kernelController = controller;
   }
   
   /** Prevent instantiation */
   private JBossWebMicrocontainerBeanLocator()
   {
      
   }

}
