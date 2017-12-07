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
package org.jboss.deployment;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.aop.microcontainer.aspects.jmx.JMX;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaData;
import org.jboss.classloading.spi.metadata.ExportAll;
import org.jboss.classloading.spi.version.Version;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.common.jboss.LoaderRepositoryMetaData;
import org.jboss.metadata.ear.jboss.JBossAppMetaData;

/**
 * EarClassLoaderDeployer.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 87282 $
 */
@JMX(name="jboss.j2ee:service=EARClassLoaderDeployer", exposedInterface=EarClassLoaderDeployerMBean.class)
public class EarClassLoaderDeployer extends AbstractSimpleRealDeployer<JBossAppMetaData> implements EarClassLoaderDeployerMBean
{
   /** Whether to isolated ear deployments */
   private boolean isolated = false;

   /**
    * @return whether ear deployments should be isolated
    */
   public boolean isIsolated()
   {
      return isolated;
   }
   
   /**
    * @param isolated whether ear deployments should be isolated
    */
   public void setIsolated(boolean isolated)
   {
      this.isolated = isolated;
   }
   
   /**
    * Create a new EarClassLoaderDeployer.
    */
   public EarClassLoaderDeployer()
   {
      super(JBossAppMetaData.class);
      setOutput(ClassLoadingMetaData.class);
      setStage(DeploymentStages.POST_PARSE);
      setTopLevelOnly(true);
   }

   @Override
   public void deploy(DeploymentUnit unit, JBossAppMetaData metaData) throws DeploymentException
   {
      ClassLoadingMetaData classLoadingMetaData = unit.getAttachment(ClassLoadingMetaData.class);
      if (classLoadingMetaData != null)
         return;

      LoaderRepositoryMetaData lrmd = metaData.getLoaderRepository();
      if (lrmd != null && LoaderRepositoryMetaDataHelper.create(unit, lrmd) != null)
         return;

      // For isolated automatically create the classloader in a new domain
      if (isolated)
      {
         String domain = EARDeployment.getJMXName(metaData, unit) + ",extension=LoaderRepository";
         try
         {
            ObjectName canonical = ObjectName.getInstance(domain);
            domain = canonical.getCanonicalName();
         }
         catch (MalformedObjectNameException ignored)
         {
            // Not a JMX ObjectName???
         }
         classLoadingMetaData = new ClassLoadingMetaData();
         classLoadingMetaData.setName(unit.getName());
         classLoadingMetaData.setDomain(domain);
         classLoadingMetaData.setExportAll(ExportAll.NON_EMPTY);
         classLoadingMetaData.setImportAll(true);
         classLoadingMetaData.setVersion(Version.DEFAULT_VERSION);
         classLoadingMetaData.setJ2seClassLoadingCompliance(false);
         unit.addAttachment(ClassLoadingMetaData.class, classLoadingMetaData);
      }
   }
}
