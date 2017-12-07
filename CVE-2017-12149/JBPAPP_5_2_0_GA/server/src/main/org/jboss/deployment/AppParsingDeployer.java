/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployment;

import java.util.List;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.metadata.client.spec.ApplicationClientMetaData;
import org.jboss.metadata.ear.spec.EarMetaData;
import org.jboss.metadata.ear.spec.ModuleMetaData;
import org.jboss.metadata.ear.spec.ModulesMetaData;
import org.jboss.metadata.ejb.spec.EjbJarMetaData;
import org.jboss.metadata.web.spec.WebMetaData;
import org.jboss.virtual.VirtualFile;


/**
 * An SchemaResolverDeployer for translating application.xml descriptors into
 * Ear50MetaData instances.
 * 
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @version $Revision: 85945 $
 */
public class AppParsingDeployer extends SchemaResolverDeployer<EarMetaData>
{
   public AppParsingDeployer()
   {
      super(EarMetaData.class);
      setName("application.xml");
   }

   /**
    * Get the virtual file path for the application descriptor in the
    * DeploymentContext.getMetaDataPath.
    * 
    * @return the current virtual file path for the application descriptor
    */
   public String getAppXmlPath()
   {
      return getName();
   }
   
   /**
    * Set the virtual file path for the application descriptor in the
    * DeploymentContext.getMetaDataLocation. The standard path is application.xml
    * to be found in the META-INF metdata path.
    * 
    * @param appXmlPath - new virtual file path for the application descriptor
    */
   public void setAppXmlPath(String appXmlPath)
   {
      setName(appXmlPath);
   }

   protected EarMetaData parse(VFSDeploymentUnit unit, VirtualFile file, EarMetaData root) throws Exception
   {
      EarMetaData ear = super.parse(unit,file, root);
      List<DeploymentUnit> children = unit.getChildren();
      ModulesMetaData modules = ear.getModules();
      if(children != null && modules != null)
      {
         for(DeploymentUnit child : children)
         {
            String moduleName = child.getSimpleName();
            ModuleMetaData module = modules.get(moduleName);
            if(module != null && module.getAlternativeDD() != null)
            {
               VirtualFile altDDFile = unit.getRoot().getChild(module.getAlternativeDD());
               if(altDDFile == null)
                  throw new IllegalStateException("Failed to locate alternative DD '" + module.getAlternativeDD() + "' in " + unit.getRoot().getPathName());
               
               String attachmentName;
               if(module.getType() == ModuleMetaData.ModuleType.Ejb)
                  attachmentName = EjbJarMetaData.class.getName();
               else if(module.getType() == ModuleMetaData.ModuleType.Web)
                  attachmentName = WebMetaData.class.getName();
               else if(module.getType() == ModuleMetaData.ModuleType.Client)
                  attachmentName = ApplicationClientMetaData.class.getName();
               else if(module.getType() == ModuleMetaData.ModuleType.Connector)
                  attachmentName = "org.jboss.resource.metadata.ConnectorMetaData";
               else
                  throw new IllegalStateException("Expected module types in an EAR are ejb, web, java and connector but got " + module.getType() + " for " + child.getName() + " in " + unit.getName());
               
               child.addAttachment(attachmentName + ".altDD", altDDFile);
               if(log.isTraceEnabled())
                  log.trace("attached alt-dd " + altDDFile + " for module " + child.getSimpleName());
            }
         }
      }
      
      return ear;
   }
}
