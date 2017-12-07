/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
import org.jboss.management.j2ee.Servlet;
import org.jboss.management.j2ee.WebModule;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A factory for mapping WARDeployer deployments to WebModules
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 62057 $
 */
public class WebModuleFactory
        implements ManagedObjectFactory
{
   private static Logger log = Logger.getLogger(WebModuleFactory.class);
   private HashMap deploymentToModuleNameMap = new HashMap();
   private HashMap containerToModuleNameMap = new HashMap();

   /**
    * Create JSR-77 WebModule
    *
    * @param server the MBeanServer context
    * @param data   arbitrary data associated with the creation context
    */
   public ObjectName create(MBeanServer server, Object data)
   {
      if ((data instanceof DeploymentInfo) == false)
         return null;

      DeploymentInfo di = (DeploymentInfo) data;
      String warName = di.shortName;
      ObjectName webModuleService = di.deployedObject;
      String earName = FactoryUtils.findEarParent(di);
      ObjectName jsr77Name = WebModule.create(server,
              earName, warName, di.localUrl, webModuleService);
      deploymentToModuleNameMap.put(di, jsr77Name);
      Iterator servlets = di.mbeans.iterator();
      while (servlets.hasNext())
      {
         Object next = servlets.next();
         try
         {
            ObjectName servletName = (ObjectName) next;
            createServlet(server, jsr77Name, servletName);
         }
         catch (Throwable e)
         {
            log.debug("Failed to create JSR-77 servlet: " + next, e);
         }
      }
      return jsr77Name;
   }

   /**
    * Destroy JSR-77 WebModule
    *
    * @param server the MBeanServer context
    * @param data   arbitrary data associated with the creation context
    */
   public void destroy(MBeanServer server, Object data)
   {
      if ((data instanceof DeploymentInfo) == false)
         return;

      DeploymentInfo di = (DeploymentInfo) data;
      ObjectName jsr77Name = (ObjectName) deploymentToModuleNameMap.remove(di);

      log.debug("Destroy module: " + jsr77Name);
      Iterator servlets = di.mbeans.iterator();
      while (servlets.hasNext())
      {
         Object next = servlets.next();
         try
         {
            ObjectName servletName = (ObjectName) next;
            destroyServlet(server, servletName);
         }
         catch (Throwable e)
         {
            log.debug("Failed to destroy JSR-77 servlet: " + next, e);
         }
      }

      if (jsr77Name != null)
      {
         WebModule.destroy(server, jsr77Name);
      }
   }

   /**
    * Create JSR-77 Servlet
    *
    * @param mbeanServer        the MBeanServer context
    * @param webModuleName      the JSR77 name of the servlet's WebModule
    * @param servletServiceName The jboss servlet mbean name
    */
   public ObjectName createServlet(MBeanServer mbeanServer, ObjectName webModuleName,
                                   ObjectName servletServiceName)
   {
      ObjectName jsr77Name = null;
      // We don't currently have a web container mbean
      ObjectName webContainerName = null;
      try
      {
         log.debug("Creating servlet: " + servletServiceName);
         String servletName = servletServiceName.getKeyProperty("name");
         if (servletName != null)
         {
            // Only treat resources with names as potential servlets
            jsr77Name = Servlet.create(mbeanServer, webModuleName,
                    webContainerName, servletServiceName);
            containerToModuleNameMap.put(servletServiceName, jsr77Name);
            log.debug("Created servlet: " + servletServiceName + ", module: " + jsr77Name);
         }
      }
      catch (Exception e)
      {
         log.debug("Failed to create servlet: " + servletServiceName, e);
      }

      return jsr77Name;
   }

   /**
    * Destroy JSR-77 Servlet
    *
    * @param mbeanServer        the MBeanServer context
    * @param servletServiceName The jboss servlet mbean name
    */
   public void destroyServlet(MBeanServer server, ObjectName servletServiceName)
   {
      ObjectName jsr77Name = (ObjectName) containerToModuleNameMap.remove(servletServiceName);

      log.debug("Destroy container: " + servletServiceName + ", module: " + jsr77Name);
      if (jsr77Name != null)
      {
         Servlet.destroy(server, jsr77Name);
      }
   }

}
