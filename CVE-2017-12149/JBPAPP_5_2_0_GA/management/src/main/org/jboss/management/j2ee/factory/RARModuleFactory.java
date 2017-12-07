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
import org.jboss.management.j2ee.ResourceAdapter;
import org.jboss.management.j2ee.ResourceAdapterModule;
import org.jboss.resource.metadata.ConnectorMetaData;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.HashMap;

/**
 * A factory for mapping RARDeployer deployments to ResourceAdaptorModules
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 62057 $
 */
public class RARModuleFactory
        implements ManagedObjectFactory
{
   private static Logger log = Logger.getLogger(RARModuleFactory.class);
   private static HashMap moduleServiceToMgmtMap = new HashMap();
   private HashMap deploymentToModuleNameMap = new HashMap();

   static ObjectName getResourceAdapterName(ObjectName rarService)
   {
      ObjectName jsr77Name = (ObjectName) moduleServiceToMgmtMap.get(rarService);
      return jsr77Name;
   }

   /**
    * Create JSR-77 EJBModule
    *
    * @param server the MBeanServer context
    * @param data   arbitrary data associated with the creation context
    */
   public ObjectName create(MBeanServer mbeanServer, Object data)
   {
      if ((data instanceof DeploymentInfo) == false)
         return null;

      DeploymentInfo di = (DeploymentInfo) data;
      ConnectorMetaData metaData = (ConnectorMetaData) di.metaData;

      // Create the ResourceAdapterModule
      String rarName = di.shortName;
      ObjectName rarService = di.deployedObject;
      ObjectName jsr77ModuleName = ResourceAdapterModule.create(mbeanServer,
              FactoryUtils.findEarParent(di),
              rarName,
              di.localUrl);
      deploymentToModuleNameMap.put(di, jsr77ModuleName);
      log.debug("Created module: " + jsr77ModuleName);

      // Create the ResourceAdapter
      ObjectName jsr77RAName = ResourceAdapter.create(mbeanServer,
              metaData.getDescription().getDisplayName(), jsr77ModuleName, rarService);
      // Register a mapping from the RARDeployment service to the ResourceAdapter
      moduleServiceToMgmtMap.put(rarService, jsr77RAName);

      return jsr77ModuleName;
   }

   /**
    * Destroy JSR-77 EJBModule
    *
    * @param server the MBeanServer context
    * @param data   arbitrary data associated with the creation context
    */
   public void destroy(MBeanServer mbeanServer, Object data)
   {
      if ((data instanceof DeploymentInfo) == false)
         return;

      DeploymentInfo di = (DeploymentInfo) data;
      ConnectorMetaData metaData = (ConnectorMetaData) di.metaData;
      ObjectName jsr77Name = (ObjectName) deploymentToModuleNameMap.get(di);

      ResourceAdapter.destroy(mbeanServer, metaData.getDescription().getDisplayName());
      log.debug("Destroy module: " + jsr77Name);
      if (jsr77Name != null)
      {
         ResourceAdapterModule.destroy(mbeanServer, jsr77Name);
      }
   }

}
