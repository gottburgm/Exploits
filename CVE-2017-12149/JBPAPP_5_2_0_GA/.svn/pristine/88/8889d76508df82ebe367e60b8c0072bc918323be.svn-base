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
package org.jboss.spring.kernel;

import org.jboss.kernel.Kernel;

/**
 * Locate object from Kernel.
 * Bind Kernel first.
 *
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 * @see ControllerLocator
 */
public class KernelBinderLocator extends KernelLocator
{

   public synchronized Kernel getKernel()
   {
      Kernel kernel = super.getKernel();
      if (kernel == null)
      {
         kernel = lookupKernel();
         setKernel(kernel);
      }
      return kernel;
   }

   // todo - bind jbossas5 kernel
   private Kernel lookupKernel()
   {
      throw new UnsupportedOperationException("MC kernel lookup not yet implemented!");
   }

}
