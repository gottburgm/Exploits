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
import org.jboss.management.j2ee.ResourceAdapter;
import org.jboss.management.j2ee.ResourceAdapterModule;
import org.jboss.management.j2ee.factory.FactoryUtils;
import org.jboss.resource.metadata.ConnectorMetaData;
import org.jboss.resource.metadata.RARDeploymentMetaData;

/**
 * Rar module jsr77 deployer.
 * 
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class RarModuleJSR77Deployer extends AbstractVFSJSR77Deployer<RARDeploymentMetaData>
{
   public RarModuleJSR77Deployer()
   {
      super(RARDeploymentMetaData.class);
   }

   protected void deployJsr77(MBeanServer server, VFSDeploymentUnit unit, RARDeploymentMetaData rdmd) throws Throwable
   {
      ConnectorMetaData metaData = rdmd.getConnectorMetaData();
      // Create the ResourceAdapterModule
      String rarName = unit.getSimpleName();
      ObjectName rarService = extractRootObjectName(server, unit, rdmd); // TODO
      ObjectName jsr77ModuleName = ResourceAdapterModule.create(server, FactoryUtils.findEarParent(unit), rarName, unit.getRoot().toURL());
      putObjectName(unit, ResourceAdapter.class.getName(), jsr77ModuleName);
      log.debug("Created module: " + jsr77ModuleName);
      // Create the ResourceAdapter
      ObjectName jsr77RAName = ResourceAdapter.create(server, getDisplayName(unit, metaData), jsr77ModuleName, rarService);
      // Register a mapping from the RARDeployment service to the ResourceAdapter
      putObjectName(unit, rarService.getCanonicalName(), jsr77RAName);
   }

   protected void undeployJsr77(MBeanServer server, VFSDeploymentUnit unit, RARDeploymentMetaData rdmd)
   {
      ConnectorMetaData metaData = rdmd.getConnectorMetaData();
      ObjectName jsr77Name = removeObjectName(unit, ResourceAdapter.class.getName());
      ResourceAdapter.destroy(server, getDisplayName(unit, metaData));
      log.debug("Destroy module: " + jsr77Name);
      if (jsr77Name != null)
      {
         ResourceAdapterModule.destroy(server, jsr77Name);
      }
      ObjectName rarService = extractRootObjectName(server, unit, rdmd); // TODO
      if (rarService != null)
         removeObjectName(unit, rarService.getCanonicalName());
   }

   /**
    * Get display name.
    *
    * @param unit the deployment unit
    * @param metaData the connector metadata
    * @return metadata display name
    */
   protected String getDisplayName(VFSDeploymentUnit unit, ConnectorMetaData metaData)
   {
      String displayName = metaData.getDescription().getDisplayName();
      if (displayName == null)
      {
         log.debug("ConnectorMetaData displayname is null: " + metaData);
         displayName = unit.getSimpleName() + " [MISSING-DISPLAY-NAME]";
      }
      return displayName;
   }
}