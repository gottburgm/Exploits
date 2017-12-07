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

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.management.j2ee.Servlet;
import org.jboss.management.j2ee.WebModule;
import org.jboss.management.j2ee.factory.FactoryUtils;
import org.jboss.metadata.web.jboss.JBossWebMetaData;

/**
 * War module jsr77 view deployer.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class WebModuleJSR77Deployer extends AbstractVFSJSR77Deployer<JBossWebMetaData>
{
   public WebModuleJSR77Deployer()
   {
      super(JBossWebMetaData.class);
   }

   protected void deployJsr77(MBeanServer server, VFSDeploymentUnit unit, JBossWebMetaData metaData) throws Throwable
   {
      String warName = unit.getSimpleName();
      ObjectName webModuleService = extractRootObjectName(server, unit, metaData); // TODO
      String earName = FactoryUtils.findEarParent(unit);
      ObjectName jsr77Name = WebModule.create(server, earName, warName, unit.getRoot().toURL(), webModuleService);
      putObjectName(unit, WebModule.class.getName(), jsr77Name);
      Iterable<ObjectName> servlets = extractComponentObjectNames(server, unit, metaData); // TODO
      for (ObjectName servletName : servlets)
      {
         try
         {
            createServlet(server, unit, jsr77Name, servletName);
         }
         catch (Throwable e)
         {
            log.debug("Failed to create JSR-77 servlet: " + servletName, e);
         }
      }
   }

   protected void undeployJsr77(MBeanServer server, VFSDeploymentUnit unit, JBossWebMetaData metaData)
   {
      ObjectName jsr77Name = removeObjectName(unit, WebModule.class.getName());
      log.debug("Destroy module: " + jsr77Name);
      Iterable<ObjectName> servlets = extractComponentObjectNames(server, unit, metaData); // TODO
      for (ObjectName servletName : servlets)
      {
         try
         {
            destroyServlet(server, unit, servletName);
         }
         catch (Throwable e)
         {
            log.debug("Failed to destroy JSR-77 servlet: " + servletName, e);
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
    * @param unit the deployment unit
    * @param webModuleName      the JSR77 name of the servlet's WebModule
    * @param servletServiceName The jboss servlet mbean name
    * @return servlet's jsr77 object name
    */
   public ObjectName createServlet(MBeanServer mbeanServer, VFSDeploymentUnit unit, ObjectName webModuleName, ObjectName servletServiceName)
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
            jsr77Name = Servlet.create(mbeanServer, webModuleName, webContainerName, servletServiceName);
            putObjectName(unit, servletServiceName.getCanonicalName(), jsr77Name);
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
    * @param server        the MBeanServer context
    * @param unit the deployment unit
    * @param servletServiceName The jboss servlet mbean name
    */
   public void destroyServlet(MBeanServer server, VFSDeploymentUnit unit, ObjectName servletServiceName)
   {
      ObjectName jsr77Name = removeObjectName(unit, servletServiceName.getCanonicalName());
      log.debug("Destroy container: " + servletServiceName + ", module: " + jsr77Name);
      if (jsr77Name != null)
      {
         Servlet.destroy(server, jsr77Name);
      }
   }
}