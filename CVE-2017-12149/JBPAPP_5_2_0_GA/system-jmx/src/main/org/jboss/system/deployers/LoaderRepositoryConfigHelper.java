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
package org.jboss.system.deployers;

import java.io.ByteArrayInputStream;
import java.util.Properties;

import javax.management.ObjectName;

import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.classloading.spi.metadata.ExportAll;
import org.jboss.classloading.spi.version.Version;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.mx.loading.LoaderRepositoryFactory.LoaderRepositoryConfig;

/**
 * LoaderRepositoryConfigHelper.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class LoaderRepositoryConfigHelper
{
   /**
    * Populate the deployment's classloading metadata from a loader repository config
    * with parent delegation false.
    * 
    * @param unit the deployment unit
    * @param loaderConfig the loader repository config
    * @return the classloading metadata
    * @throws DeploymentException for any error
    */
   public static ClassLoadingMetaData create(DeploymentUnit unit, LoaderRepositoryConfig loaderConfig) throws DeploymentException
   {
      return create(unit, loaderConfig, false);
   }

   /**
    * Populate the deployment's classloading metadata from a loader repository config
    * 
    * @param unit the deployment unit
    * @param loaderConfig the loader repository config
    * @param parentDelegation the default value for parent delegation
    * @return the classloading metadata
    * @throws DeploymentException for any error
    */
   public static ClassLoadingMetaData create(DeploymentUnit unit, LoaderRepositoryConfig loaderConfig, boolean parentDelegation) throws DeploymentException
   {
      if (unit == null)
         throw new IllegalArgumentException("Null unit");
      
      ClassLoadingMetaData clmd = unit.getAttachment(ClassLoadingMetaData.class);
      if (clmd != null)
         return clmd;
      
      clmd = create(unit.getName(), loaderConfig, parentDelegation);
      if (clmd != null)
         unit.addAttachment(ClassLoadingMetaData.class, clmd);
      return clmd;
   }

   /**
    * Create classloading metadata from a loader repository config
    * with parent delegation false
    * 
    * @param deploymentName the deployment name
    * @param loaderConfig the loader repository config
    * @return the classloading metadata
    * @throws DeploymentException for any error
    */
   public static ClassLoadingMetaData create(String deploymentName, LoaderRepositoryConfig loaderConfig) throws DeploymentException
   {
      return create(deploymentName, loaderConfig, false);
   }

   /**
    * Create classloading metadata from a loader repository config
    * 
    * @param deploymentName the deployment name
    * @param loaderConfig the loader repository config
    * @param parentDelegation the default value for parent delegation
    * @return the classloading metadata
    * @throws DeploymentException for any error
    */
   public static ClassLoadingMetaData create(String deploymentName, LoaderRepositoryConfig loaderConfig, boolean parentDelegation) throws DeploymentException
   {
      if (deploymentName == null)
         throw new IllegalArgumentException("Null deployment name");
      if (loaderConfig == null)
         throw new IllegalArgumentException("Null loader config");
      
      ObjectName name = loaderConfig.repositoryName;
      if (name == null)
         return null;
      String domain = name.getCanonicalName().trim();
      if (domain.length() == 0)
         return null;
      ClassLoadingMetaData metaData = new ClassLoadingMetaData();
      metaData.setName(deploymentName);
      metaData.setDomain(domain);
      metaData.setExportAll(ExportAll.NON_EMPTY);
      metaData.setImportAll(true);
      metaData.setVersion(Version.DEFAULT_VERSION);

      Properties props = new Properties();
      String config = loaderConfig.repositoryConfig;
      try
      {
         if (config != null)
         {
            ByteArrayInputStream bais = new ByteArrayInputStream(config.getBytes());
            props.load(bais);
         }
      }
      catch (Exception e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Error parsing repository config " + config, e);
      }
      String java2ParentDelegation = props.getProperty("java2ParentDelegation");
      // Check for previous mis-spelled property name
      if( java2ParentDelegation == null )
         java2ParentDelegation = props.getProperty("java2ParentDelegaton");
      boolean useParentFirst = parentDelegation;
      if (java2ParentDelegation != null)
         useParentFirst = Boolean.valueOf(java2ParentDelegation);
      metaData.setJ2seClassLoadingCompliance(useParentFirst);
      return metaData;
   }
}
