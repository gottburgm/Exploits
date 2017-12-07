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
package org.jboss.management.j2ee.deployers;

import java.util.List;
import java.util.ListIterator;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.management.j2ee.MBean;
import org.jboss.management.j2ee.ServiceModule;
import org.jboss.system.metadata.ServiceDeployment;
import org.jboss.system.metadata.ServiceMetaData;

/**
 * Service module jsr77 deployer.
 * 
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class ServiceModuleJSR77Deployer extends AbstractVFSJSR77Deployer<ServiceDeployment>
{
   public ServiceModuleJSR77Deployer()
   {
      super(ServiceDeployment.class);
   }

   protected void deployJsr77(MBeanServer server, VFSDeploymentUnit unit, ServiceDeployment metaData) throws Throwable
   {
      ObjectName sarName = ServiceModule.create(server, unit.getSimpleName(), unit.getRoot().toURL());
      if (sarName != null)
      {
         log.debug("Created ServiceModule: " + sarName);
      }

      List<ServiceMetaData> beans = metaData.getServices();
      if (beans != null && beans.isEmpty() == false)
      {
         for (ServiceMetaData bean : beans)
         {
            ObjectName mbeanName = bean.getObjectName();
            // Create JSR-77 MBean
            MBean.create(server, sarName.toString(), mbeanName);
            log.debug("Create MBean, name: " + mbeanName + ", SAR Module: " + sarName);
         }
      }
   }

   protected void undeployJsr77(MBeanServer server, VFSDeploymentUnit unit, ServiceDeployment metaData)
   {
      List<ServiceMetaData> beans = metaData.getServices();
      if (beans != null && beans.isEmpty() == false)
      {
         ListIterator<ServiceMetaData> iter = beans.listIterator(beans.size());
         while(iter.hasPrevious())
         {
            ObjectName name = iter.previous().getObjectName();
            try
            {
               // Destroy JSR-77 MBean
               MBean.destroy(server, name.toString());
               log.debug("Destroy MBean, name: " + name);
            }
            catch (Throwable e)
            {
               log.debug("Failed to remove remove JSR-77 MBean", e);
            }
         }
      }

      // Remove JSR-77 SAR-Module
      String moduleName = unit.getSimpleName();
      try
      {
         ServiceModule.destroy(server, moduleName);
         log.debug("Removed JSR-77 SAR: " + moduleName);
      }
      catch (Throwable e)
      {
         log.debug("Failed to remove JSR-77 SAR: " + moduleName);
      }
   }
}