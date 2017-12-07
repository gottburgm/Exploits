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
import org.jboss.management.j2ee.J2EEApplication;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.HashMap;

/**
 * A factory for mapping EARDeployer deployments to J2EEApplications
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81025 $
 */
public class EARModuleFactory
        implements ManagedObjectFactory
{
   private static Logger log = Logger.getLogger(EARModuleFactory.class);

   private HashMap deploymentToModuleNameMap = new HashMap();

   /**
    * Create JSR-77 J2EEApplication
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
      ObjectName jsr77Name = J2EEApplication.create(server, moduleName, di.localUrl);
      deploymentToModuleNameMap.put(di, jsr77Name);
      if (jsr77Name != null)
      {
         log.debug("Created J2EEApplication: " + jsr77Name);
      }

      return jsr77Name;
   }

   /**
    * Destroy JSR-77 J2EEApplication
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
      // Remove JSR-77 EAR-Module
      if (jsr77Name != null)
      {
         J2EEApplication.destroy(server, jsr77Name);
         log.debug("Removed J2EEApplication: " + jsr77Name);
      }
   }
}
