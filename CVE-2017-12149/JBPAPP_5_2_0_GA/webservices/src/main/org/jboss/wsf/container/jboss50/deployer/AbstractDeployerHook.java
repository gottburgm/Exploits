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
package org.jboss.wsf.container.jboss50.deployer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.metadata.serviceref.VirtualFileAdaptor;
import org.jboss.virtual.VirtualFile;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.*;

/**
 * An abstract web service deployer.
 * 
 * @author Thomas.Diesler@jboss.org
 * @author Heiko.Braun@jboss.com
 * 
 * @since 25-Apr-2007
 */
public abstract class AbstractDeployerHook implements DeployerHook
{
   // provide logging
   protected final Logger log = Logger.getLogger(getClass());
   
   private static final WSVirtualFileFilter WS_FILEFILTER = new WSVirtualFileFilter();

   private DeploymentAspectManager deploymentAspectManager;
   private DeploymentModelFactory deploymentModelFactory;

   protected String deploymentManagerName;

   /** MC provided property **/
   public void setDeploymentManagerName(String deploymentManagerName)
   {
      this.deploymentManagerName = deploymentManagerName;
   }

   public DeploymentAspectManager getDeploymentAspectManager()
   {
      if(null == deploymentAspectManager)
      {
         SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
         deploymentAspectManager = spiProvider.getSPI(DeploymentAspectManagerFactory.class).getDeploymentAspectManager( deploymentManagerName );
      }

      return deploymentAspectManager;
   }

   public DeploymentModelFactory getDeploymentModelFactory()
   {
      if(null == deploymentModelFactory)
      {
         SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
         deploymentModelFactory = spiProvider.getSPI(DeploymentModelFactory.class);
      }

      return deploymentModelFactory;
   }

   public ArchiveDeployment newDeployment(DeploymentUnit unit)
   {
      try
      {
         DeploymentModelFactory factory = getDeploymentModelFactory();
         ArchiveDeployment dep = (ArchiveDeployment)factory.newDeployment(unit.getSimpleName(), unit.getClassLoader());
         if (unit instanceof VFSDeploymentUnit)
         {
            VFSDeploymentUnit vfsUnit = (VFSDeploymentUnit)unit;
            List<VirtualFile> virtualFiles = vfsUnit.getMetaDataFiles(WS_FILEFILTER); //get all wsdl and xsd files
            Set<UnifiedVirtualFile> uVirtualFiles = new HashSet<UnifiedVirtualFile>();
            for (VirtualFile vf : virtualFiles)
            {
               // Adding the roots of the virtual files.
               uVirtualFiles.add(new VirtualFileAdaptor(vf.getVFS().getRoot()));
            }
            dep.setMetadataFiles(new LinkedList<UnifiedVirtualFile>(uVirtualFiles));
         }

         if (unit.getParent() != null)
         {
            DeploymentUnit parentUnit = unit.getParent();
            ArchiveDeployment parentDep = (ArchiveDeployment)factory.newDeployment(parentUnit.getSimpleName(), parentUnit.getClassLoader());
            dep.setParent(parentDep);
         }
         return dep;
      }
      catch (Exception ex)
      {
         throw new WSFDeploymentException("Cannot load spi.deployment.Deployment class", ex);
      }
   }

   public Endpoint newEndpoint(String targetBean)
   {
      try
      {
         return getDeploymentModelFactory().newEndpoint(targetBean);
      }
      catch (Exception ex)
      {
         throw new WSFDeploymentException("Cannot load spi.deployment.Endpoint class", ex);
      }
   }

   /** Return true if this deployment should be ignored
    */
   public boolean ignoreDeployment(DeploymentUnit unit)
   {
      String name = unit.getName();
      return (name.startsWith("jboss:id=") && name.indexOf("service=jacc") > 0);
   }

   /** Get the deployment type this deployer can handle
    */
   public abstract Deployment.DeploymentType getDeploymentType();
}
