/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.as.integration.hornetq.deployers.pojo;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.as.integration.hornetq.management.HornetQControlRuntimeDispatchPlugin;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.registry.KernelRegistry;
import org.jboss.logging.Logger;

/**
 * The abstract control reference deployment. This registers a {@code KernelRegistryEntry}
 * based on the deployment name, which is used to handle runtime dispatching from the 
 * ProfileService {@code RuntimeComponentDispatcher}.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
@SuppressWarnings("deprecation")
public class AbstractHornetQControlReferenceDeployment
{

   /** The logger. */
   private static final Logger log = Logger.getLogger("org.jboss.as.integration.hornetq.management");
   
   /** The jboss kernel. */
   private Kernel kernel;
   
   /** The mbean server. */
   private MBeanServer mbeanServer;
   
   public Kernel getKernel()
   {
      return kernel;
   }
   
   public void setKernel(Kernel kernel)
   {
      this.kernel = kernel;
   }
   
   public MBeanServer getMbeanServer()
   {
      return mbeanServer;
   }
   
   public void setMbeanServer(MBeanServer mbeanServer)
   {
      this.mbeanServer = mbeanServer;
   }
   
   protected void registerControlReference(ObjectName objectName)
   {
      try
      {
         getKernelRegistry().registerEntry(objectName, 
               new HornetQControlRuntimeDispatchPlugin(objectName, mbeanServer));
      }
      catch(Exception e)
      {
         log.debug("failed to register hornetQ control runtime dispatcher plugin", e);
      }
   }
   
   protected void unregisterControlReference(ObjectName objectName)
   {
      try
      {
         getKernelRegistry().unregisterEntry(objectName);
      }
      catch(Exception e)
      {
         log.debug("failed to unregister hornetQ control runtime dispatcher plugin", e);
      }
   }
   
   protected KernelRegistry getKernelRegistry()
   {
      if(kernel == null)
      {
         throw new IllegalStateException("null kernel");
      }
      return kernel.getRegistry();
   }
   
}

