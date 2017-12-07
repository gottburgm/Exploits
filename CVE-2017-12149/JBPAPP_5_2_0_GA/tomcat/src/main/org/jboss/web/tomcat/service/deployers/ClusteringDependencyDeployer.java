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

package org.jboss.web.tomcat.service.deployers;

import java.util.ArrayList;
import java.util.List;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.web.jboss.JBossWebMetaData;

/**
 * Adds a dependency on a clustered cache.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85945 $
 */
public class ClusteringDependencyDeployer extends AbstractDeployer
{
   private String clusterCacheDependency;
   
   /**
    * Create a new ClusteringDependencyDeployer.
    * 
    */
   public ClusteringDependencyDeployer()
   {      
      setStage(DeploymentStages.DESCRIBE);
      setInput(JBossWebMetaData.class);
      setOutput(JBossWebMetaData.class);
   }

   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      JBossWebMetaData metaData = unit.getAttachment(JBossWebMetaData.class);
      if( metaData != null && metaData.getDistributable() != null )
      {
         if (clusterCacheDependency != null)
         {
            log.debug("Adding dependency on " + clusterCacheDependency + " to " + unit.getName());
            List<String> depends = metaData.getDepends();
            if (depends == null)
               depends = new ArrayList<String>();
            if (!depends.contains(clusterCacheDependency))
            {
               depends.add(clusterCacheDependency);
            }
            metaData.setDepends(depends);
         }
         else
         {
            log.warn("clusterCacheDependency is null; either configure it or remove this deployer");
         }
      }
   }

   public String getClusterCacheDependency()
   {
      return clusterCacheDependency;
   }

   public void setClusterCacheDependency(String clusterDependency)
   {
      this.clusterCacheDependency = clusterDependency;
   }

}
