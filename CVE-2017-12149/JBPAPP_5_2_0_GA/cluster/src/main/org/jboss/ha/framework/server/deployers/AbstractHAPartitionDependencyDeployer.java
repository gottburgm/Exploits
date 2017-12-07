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
package org.jboss.ha.framework.server.deployers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb.deployers.MergedJBossMetaDataDeployer;
import org.jboss.metadata.ejb.jboss.ClusterConfigMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossEntityBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

public abstract class AbstractHAPartitionDependencyDeployer extends AbstractDeployer
{

   private HAPartitionDependencyCreator dependencyCreator;

   public AbstractHAPartitionDependencyDeployer()
   {
      super();     
      addInput(MergedJBossMetaDataDeployer.EJB_MERGED_ATTACHMENT_NAME);
      addOutput(MergedJBossMetaDataDeployer.EJB_MERGED_ATTACHMENT_NAME);
      setStage(DeploymentStages.POST_CLASSLOADER);
   }

   public synchronized HAPartitionDependencyCreator getHaPartitionDependencyCreator()
   {
      if (dependencyCreator == null)
      {
         dependencyCreator = DefaultHAPartitionDependencyCreator.INSTANCE;
      }
      return dependencyCreator;
   }

   public synchronized void setHaPartitionDependencyCreator(HAPartitionDependencyCreator dependencyCreator)
   {
      this.dependencyCreator = dependencyCreator;
   }

   /**
    * Adds the dependency to relevant metadata.
    * 
    * {@inheritDoc}
    */
   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      JBossMetaData metaData = unit.getAttachment(MergedJBossMetaDataDeployer.EJB_MERGED_ATTACHMENT_NAME, JBossMetaData.class);
      if (metaData != null && accepts(metaData))
      {
         JBossEnterpriseBeansMetaData beans = metaData.getEnterpriseBeans();
         if (beans != null)
         {
            for (Iterator<JBossEnterpriseBeanMetaData> it = beans.iterator(); it.hasNext(); )
            {
               JBossEnterpriseBeanMetaData bmd = it.next();
               ClusterConfigMetaData ccmd = getClusteredBeanClusterConfig(bmd);
               if (ccmd != null)
               {
                  addHAPartitionDependency(bmd, ccmd);
               }
            }
         }
      }
   }
   
   // --------------------------------------------------------------  Protected
   
   protected abstract boolean accepts(JBossMetaData metaData);
   
   protected abstract void configureDeploymentStage();
   
   protected ClusterConfigMetaData getClusteredBeanClusterConfig(JBossEnterpriseBeanMetaData bmd)
   {
      if (bmd instanceof JBossSessionBeanMetaData)
      {
         JBossSessionBeanMetaData sbmd = (JBossSessionBeanMetaData) bmd;
         if (sbmd.isClustered())
         {
            return sbmd.getClusterConfig();
         }               
      }
      else if (bmd instanceof JBossEntityBeanMetaData)
      {
         JBossEntityBeanMetaData ebmd = (JBossEntityBeanMetaData) bmd;
         if (ebmd.isClustered())
         {
            return ebmd.getClusterConfig();
         }               
      }
      return null;
   }
   
   // ----------------------------------------------------------------  Private
   
   private void addHAPartitionDependency(JBossEnterpriseBeanMetaData bmd, ClusterConfigMetaData ccmd)
   {
      String dependencyName = getHaPartitionDependencyCreator().getHAPartitionDependencyName(ccmd.getPartitionName());
      Set<String> depends = bmd.getDepends();
      if (depends == null)
      {
         depends = new HashSet<String>();
      }
      depends.add(dependencyName);
      bmd.setDepends(depends);
   }

}