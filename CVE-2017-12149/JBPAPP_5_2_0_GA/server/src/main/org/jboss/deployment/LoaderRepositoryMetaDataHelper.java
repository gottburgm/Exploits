/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.common.jboss.LoaderRepositoryConfigMetaData;
import org.jboss.metadata.common.jboss.LoaderRepositoryMetaData;
import org.jboss.mx.loading.LoaderRepositoryFactory.LoaderRepositoryConfig;
import org.jboss.mx.server.ServerConstants;
import org.jboss.system.deployers.LoaderRepositoryConfigHelper;

/**
 * LoaderRepositoryMetaDataHelper.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class LoaderRepositoryMetaDataHelper
{
   /**
    * Populate the deployment's classloading metadata from a loader repository metadata
    * with parent delegation false.
    * 
    * @param unit the deployment unit
    * @param loaderMetaData the loader meta data
    * @return the classloading metadata
    * @throws DeploymentException for any error
    */
   public static ClassLoadingMetaData create(DeploymentUnit unit, LoaderRepositoryMetaData loaderMetaData) throws DeploymentException
   {
      return create(unit, loaderMetaData, false);
   }

   /**
    * Populate the deployment's classloading metadata from a loader repository metadata
    * 
    * @param unit the deployment unit
    * @param loaderMetaData the loader repository metadata
    * @param parentDelegation the default value for parent delegation
    * @return the classloading metadata
    * @throws DeploymentException for any error
    */
   public static ClassLoadingMetaData create(DeploymentUnit unit, LoaderRepositoryMetaData loaderMetaData, boolean parentDelegation) throws DeploymentException
   {
      if (unit == null)
         throw new IllegalArgumentException("Null unit");
      
      ClassLoadingMetaData clmd = unit.getAttachment(ClassLoadingMetaData.class);
      if (clmd != null)
         return clmd;
      
      clmd = create(unit.getName(), loaderMetaData, parentDelegation);
      if (clmd != null)
         unit.addAttachment(ClassLoadingMetaData.class, clmd);
      return clmd;
   }

   /**
    * Create classloading metadata from a loader repository metadata
    * with parent delegation false
    * 
    * @param deploymentName the deployment name
    * @param loaderMetaData the loader repository metadata
    * @return the classloading metadata
    * @throws DeploymentException for any error
    */
   public static ClassLoadingMetaData create(String deploymentName, LoaderRepositoryMetaData loaderMetaData) throws DeploymentException
   {
      return create(deploymentName, loaderMetaData, false);
   }

   /**
    * Create classloading metadata from a loader repository metadata
    * 
    * @param deploymentName the deployment name
    * @param loaderMetaData the loader repository metadata
    * @param parentDelegation the default value for parent delegation
    * @return the classloading metadata
    * @throws DeploymentException for any error
    */
   public static ClassLoadingMetaData create(String deploymentName, LoaderRepositoryMetaData loaderMetaData, boolean parentDelegation) throws DeploymentException
   {
      if (deploymentName == null)
         throw new IllegalArgumentException("Null deployment name");
      if (loaderMetaData == null)
         throw new IllegalArgumentException("Null loader repository metadata");

      
      LoaderRepositoryConfig repositoryConfig = new LoaderRepositoryConfig();
      
      repositoryConfig.repositoryClassName = loaderMetaData.getLoaderRepositoryClass();
      if (repositoryConfig.repositoryClassName == null || repositoryConfig.repositoryClassName.length() == 0)
         repositoryConfig.repositoryClassName = ServerConstants.DEFAULT_SCOPED_REPOSITORY_CLASS;

      // Get the object name of the repository
      String name = loaderMetaData.getName();
      if (name != null)
      {
         try
         {
            repositoryConfig.repositoryName = new ObjectName(name.trim());
         }
         catch (MalformedObjectNameException e)
         {
            throw new DeploymentException("Loader repository name is malformed: " + name, e);
         }
      }
      
      StringBuilder configData = new StringBuilder();
      Set<LoaderRepositoryConfigMetaData> children = loaderMetaData.getLoaderRepositoryConfig();
      if (children != null)
      {
         for (LoaderRepositoryConfigMetaData child : children)
         {
            // This looks stupid? Why inside a loop?
            String parserClassName = child.getConfigParserClass();
            if (parserClassName == null || parserClassName.length() == 0)
               repositoryConfig.configParserClassName = ServerConstants.DEFAULT_SCOPED_REPOSITORY_PARSER_CLASS;
            else
               repositoryConfig.configParserClassName = parserClassName;
            
            // Append all config
            String childConfig = child.getConfig();
            if (childConfig != null)
               configData.append(childConfig);
         }
      }
      repositoryConfig.repositoryConfig = configData.toString().trim();

      return LoaderRepositoryConfigHelper.create(name, repositoryConfig, parentDelegation);
   }
}
