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
package org.jboss.management.j2ee.factory;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.logging.Logger;
import org.jboss.management.j2ee.MBean;
import org.jboss.management.j2ee.ServiceModule;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81025 $
 */
public class ServiceModuleFactory
        implements ManagedObjectFactory
{
   private static Logger log = Logger.getLogger(ServiceModuleFactory.class);

   /**
    * Create JSR-77 SAR-Module
    *
    * @param server the MBeanServer context
    * @param data   arbitrary data associated with the creation context
    */
   public ObjectName create(MBeanServer server, Object data)
   {
      if ((data instanceof DeploymentInfo) == false)
         return null;

      DeploymentInfo di = (DeploymentInfo) data;
      String moduleName = di.shortName;
      ObjectName sarName = ServiceModule.create(server, moduleName, di.localUrl);
      if (sarName != null)
      {
         log.debug("Created ServiceModule: " + sarName);
      }

      try
      {
         List mbeans = di.mbeans;
         for (int i = 0; i < mbeans.size(); i++)
         {
            ObjectName mbeanName = (ObjectName) mbeans.get(i);
            // Create JSR-77 MBean
            MBean.create(server, sarName.toString(), mbeanName);
            log.debug("Create MBean, name: " + mbeanName + ", SAR Module: " + sarName);
         }
      }
      catch (Throwable e)
      {
         log.debug("Failed to create MBean, sarName:" + sarName, e);
      }

      return sarName;
   }

   public void destroy(MBeanServer server, Object data)
   {
      if ((data instanceof DeploymentInfo) == false)
         return;

      DeploymentInfo di = (DeploymentInfo) data;
      List services = di.mbeans;
      int lastService = services.size();

      for (ListIterator i = services.listIterator(lastService); i.hasPrevious();)
      {
         ObjectName name = (ObjectName) i.previous();
         try
         {
            // Destroy JSR-77 MBean
            MBean.destroy(server, name.toString());
            log.debug("Destroy MBean, name: " + name);
         }
         catch (Throwable e)
         {
            log.debug("Failed to remove remove JSR-77 MBean", e);
         } // end of try-catch
      }

      // Remove JSR-77 SAR-Module
      String moduleName = di.shortName;
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
