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
package org.jboss.web.tomcat.service.deployers;

import java.io.ByteArrayInputStream;
import java.util.Properties;
import java.util.Set;

import org.jboss.classloader.spi.filter.ClassFilter;
import org.jboss.classloader.spi.filter.RecursivePackageClassFilter;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.classloading.spi.metadata.ExportAll;
import org.jboss.classloading.spi.version.Version;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.vfs.spi.deployer.AbstractSimpleVFSRealDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.metadata.common.jboss.LoaderRepositoryConfigMetaData;
import org.jboss.metadata.common.jboss.LoaderRepositoryMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;

/**
 * The war deployment class loader deployer.
 * TODO: this deployer should not exist. Metadata should be
 * driving the existing top-level class loader deployer.
 * 
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @author ales.justin@jboss.org
 */
public class WarClassLoaderDeployer extends AbstractSimpleVFSRealDeployer<JBossWebMetaData>
{
   /** The parent class loader first model flag */
   private boolean java2ClassLoadingCompliance = false;

   /** Package names that should be ignored for class loading */
   private String filteredPackages;

   /**
    * Create a new WarClassLoaderDeployer.
    */
   public WarClassLoaderDeployer()
   {
      super(JBossWebMetaData.class);
      setStage(DeploymentStages.POST_PARSE);
      addInput(ClassLoadingMetaData.class);
      setOutput(ClassLoadingMetaData.class);
   }

   public boolean isJava2ClassLoadingCompliance()
   {
      return java2ClassLoadingCompliance;
   }
   
   public void setJava2ClassLoadingCompliance(boolean flag)
   {
      this.java2ClassLoadingCompliance = flag;
   }

   public String getFilteredPackages()
   {
      return filteredPackages;
   }
   public void setFilteredPackages(String pkgs)
   {
      this.filteredPackages = pkgs;
   }

   @Override
   public void deploy(VFSDeploymentUnit unit, JBossWebMetaData metaData) throws DeploymentException
   {
      // Ignore if it already has classloading
      if (unit.isAttachmentPresent(ClassLoadingMetaData.class))
         return;

      // The default domain name is the unit name
      String domainName = unit.getName();
      
      // The default classloading compliance is on the deployer
      boolean j2seClassLoadingCompliance = java2ClassLoadingCompliance;
      
      // Do we have a legacy classloading element?
      org.jboss.metadata.web.jboss.ClassLoadingMetaData webCLMD = metaData.getClassLoading();
      if (webCLMD != null)
      {
         // Was the complince set?
         if (webCLMD.wasJava2ClassLoadingComplianceSet())
            j2seClassLoadingCompliance = webCLMD.isJava2ClassLoadingCompliance();

         // Does it have a loader repository
         LoaderRepositoryMetaData lrmd = webCLMD.getLoaderRepository();
         if (lrmd != null)
         {
            // Use the trimmed repository name as the domain
            String repositoryName = lrmd.getName();
            if (repositoryName != null)
            {
               repositoryName = repositoryName.trim();
               if (repositoryName != null)
               {
                  domainName = repositoryName;
                  
                  // If there was no compliance set see if the loader repository has one
                  if (webCLMD.wasJava2ClassLoadingComplianceSet() == false)
                  {
                     Set<LoaderRepositoryConfigMetaData> configs = lrmd.getLoaderRepositoryConfig();
                     if (configs != null && configs.isEmpty() == false)
                     {
                        LoaderRepositoryConfigMetaData lrcmd = configs.iterator().next();

                        Properties props = new Properties();
                        String config = lrcmd.getConfig();
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
                        if( java2ParentDelegation == null )
                        {
                           // Check for previous mis-spelled property name
                           java2ParentDelegation = props.getProperty("java2ParentDelegaton", "false");
                        }
                        j2seClassLoadingCompliance = Boolean.valueOf(java2ParentDelegation);
                     }
                  }
               }
            }
         }
      }

      // Create a classloading metadata
      // NOTE: Don't explicitly set the parentDomain otherwise it will create a top level classloader
      //       for subdeployments rather than a classloader hanging off the main deployment's classloader
      ClassLoadingMetaData classLoadingMetaData = new ClassLoadingMetaData();
      classLoadingMetaData.setName(unit.getName());
      classLoadingMetaData.setDomain(domainName);
      classLoadingMetaData.setExportAll(ExportAll.NON_EMPTY);
      classLoadingMetaData.setImportAll(true);
      classLoadingMetaData.setVersion(Version.DEFAULT_VERSION);
      classLoadingMetaData.setJ2seClassLoadingCompliance(j2seClassLoadingCompliance);
      ClassFilter filter = null;
      if (filteredPackages != null)
      {
         filter = RecursivePackageClassFilter.createRecursivePackageClassFilterFromString(filteredPackages);
         classLoadingMetaData.setExcluded(filter);
      }
      unit.addAttachment(ClassLoadingMetaData.class, classLoadingMetaData);
   }
}
