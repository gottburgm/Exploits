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
import org.jboss.management.j2ee.JCAResource;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Iterator;

/**
 * A factory for mapping DataSourceDeployer deployments to JCAResource
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81025 $
 */
public class JCAResourceFactory
        implements ManagedObjectFactory
{
   private static Logger log = Logger.getLogger(JCAResourceFactory.class);

   /**
    * Creates a JCAResource
    *
    * @param mbeanServer
    * @param data   A MBeanServerNotification
    * @return the JCAResource ObjectName
    */
   public ObjectName create(MBeanServer mbeanServer, Object data)
   {
      if ((data instanceof DeploymentInfo) == false)
         return null;

      DeploymentInfo di = (DeploymentInfo) data;
      ObjectName jsr77Name = null;

      /* Get the RARDeployment service name by looking for the mbean in the
      deployment with a name matching service=xxxDS. This relies on the naming
      pattern created by the JCA CM deployer.
      */
      ObjectName rarDeployService = null;
      ObjectName cmService = null;
      ObjectName poolService = null;
      Iterator iter = di.mbeans.iterator();
      while (iter.hasNext())
      {
         ObjectName oname = (ObjectName) iter.next();
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
         return null;
      }

      try
      {
         /* Now to tie this CM back to its rar query the rarDeployService for
         the org.jboss.resource.RARDeployment service created by the RARDeployer.
          */
         ObjectName rarService = (ObjectName) mbeanServer.getAttribute(rarDeployService,
                 "OldRarDeployment");
         // Get the ResourceAdapter JSR77 name
         ObjectName jsr77RAName = RARModuleFactory.getResourceAdapterName(rarService);
         // Now build the JCAResource
         String resName = rarDeployService.getKeyProperty("name");
         jsr77Name = JCAResource.create(mbeanServer, resName, jsr77RAName,
                 cmService, rarDeployService, poolService);
      }
      catch (Exception e)
      {
         log.debug("Failed to create JCAResource", e);
      }

      return jsr77Name;
   }

   /**
    * Destroys the JCAResource
    *
    * @param mbeanServer
    * @param data  A MBeanServerNotification
    */
   public void destroy(MBeanServer mbeanServer, Object data)
   {
      if ((data instanceof DeploymentInfo) == false)
         return;

      DeploymentInfo di = (DeploymentInfo) data;
      String resName = getDeploymentResName(mbeanServer, di);
      JCAResource.destroy(mbeanServer, resName);
   }

   private String getDeploymentResName(MBeanServer mbeanServer, DeploymentInfo di)
   {
      String resName = null;
      /* Get the RARDeployment service name by looking for the mbean in the
      deployment with a name matching service=xxxDS. This relies on the naming
      pattern created by the JCA CM deployer.
      */
      ObjectName rarDeployService = null;
      Iterator iter = di.mbeans.iterator();
      while (iter.hasNext())
      {
         ObjectName oname = (ObjectName) iter.next();
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
