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
package org.jboss.resource.deployers;

import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.mx.loading.LoaderRepositoryFactory.LoaderRepositoryConfig;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentGroup;
import org.jboss.system.deployers.LoaderRepositoryConfigHelper;

/**
 * ManagedConnectionFactoryClassLoaderDeployer.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class ManagedConnectionFactoryClassLoaderDeployer extends AbstractSimpleRealDeployer<ManagedConnectionFactoryDeploymentGroup>
{
   public ManagedConnectionFactoryClassLoaderDeployer()
   {
      super(ManagedConnectionFactoryDeploymentGroup.class);
      setStage(DeploymentStages.POST_PARSE);
      setTopLevelOnly(true);
      setOutput(ClassLoadingMetaData.class);
   }

   public void deploy(DeploymentUnit unit, ManagedConnectionFactoryDeploymentGroup deployment) throws DeploymentException
   {
      ClassLoadingMetaData classLoadingMetaData = unit.getAttachment(ClassLoadingMetaData.class);
      if (classLoadingMetaData != null)
         return;
      
      LoaderRepositoryConfig loaderConfig = deployment.getLoaderRepositoryConfig();
      if (loaderConfig == null)
         return;

      LoaderRepositoryConfigHelper.create(unit, loaderConfig);
   }
}
