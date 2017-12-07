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

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.management.j2ee.JCAResource;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentGroup;

/**
 * JCA resource jsr77 deployer.
 * 
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public class JCAResourceJSR77Deployer extends AbstractJSR77Deployer<ManagedConnectionFactoryDeploymentGroup>
{
   public JCAResourceJSR77Deployer()
   {
      super(ManagedConnectionFactoryDeploymentGroup.class);
   }

   protected void deployJsr77(MBeanServer server, DeploymentUnit unit, ManagedConnectionFactoryDeploymentGroup metaData) throws Throwable
   {
      /* Get the RARDeployment service name by looking for the mbean in the
      deployment with a name matching service=xxxDS. This relies on the naming
      pattern created by the JCA CM deployer.
      */
      ObjectName rarDeployService = null;
      ObjectName cmService = null;
      ObjectName poolService = null;
      Iterable<ObjectName> iter = extractComponentObjectNames(server, unit, metaData); // TODO
      for (ObjectName oname : iter)
      {
         String name = oname.getKeyProperty("service");
         if (name.equals("ManagedConnectionFactory") || name.endsWith("DS"))
            rarDeployService = oname;
         else if (name.endsWith("CM"))
            cmService = oname;
         else if (name.endsWith("Pool"))
            poolService = oname;
      }
      if (rarDeployService == null || cmService == null)
      {
         log.debug("Failed to find a service=xxxDS match");
         return;
      }

      try
      {
         /* Now to tie this CM back to its rar query the rarDeployService for
            the org.jboss.resource.RARDeployment service created by the RARDeployer.
          */
         ObjectName rarService = (ObjectName) server.getAttribute(rarDeployService, "OldRarDeployment");
         // Get the ResourceAdapter JSR77 name
         ObjectName jsr77RAName = getObjectName(unit, rarService.getCanonicalName());
         // Now build the JCAResource
         String resName = rarDeployService.getKeyProperty("name");
         JCAResource.create(server, resName, jsr77RAName, cmService, rarDeployService, poolService);
      }
      catch (Exception e)
      {
         log.debug("Failed to create JCAResource", e);
      }
   }

   protected void undeployJsr77(MBeanServer server, DeploymentUnit unit, ManagedConnectionFactoryDeploymentGroup metaData)
   {
      Iterable<ObjectName> mbeans = extractComponentObjectNames(server, unit, metaData); // TODO
      String resName = getDeploymentResName(mbeans);
      JCAResource.destroy(server, resName);
   }

   private String getDeploymentResName(Iterable<ObjectName> mbeans)
   {
      String resName = null;
      /* Get the RARDeployment service name by looking for the mbean in the
         deployment with a name matching service=xxxDS. This relies on the naming
         pattern created by the JCA CM deployer.
      */
      ObjectName rarDeployService;
      for (ObjectName oname : mbeans)
      {
         String name = oname.getKeyProperty("service");
         if (name.equals("ManagedConnectionFactory") || name.endsWith("DS"))
         {
            rarDeployService = oname;
            resName = rarDeployService.getKeyProperty("name");
            break;
         }
      }
      return resName;
   }
}