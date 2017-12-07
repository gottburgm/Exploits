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
package org.jboss.wsf.container.jboss50.deployment.metadata;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentAspect;

/**
 * A deployer that builds the UnifiedDeploymentInfo 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class ContainerMetaDataDeploymentAspect extends DeploymentAspect
{
   private ContainerMetaDataAdapter metaDataAdapter = new ContainerMetaDataAdapter();

   public void setMetaDataAdapter(ContainerMetaDataAdapter adapter)
   {
      this.metaDataAdapter = adapter;
   }

   @Override
   public void create(Deployment dep)
   {
      DeploymentUnit unit = dep.getAttachment(DeploymentUnit.class);
      if (unit == null)
         throw new IllegalStateException("Cannot obtain deployment unit");

      metaDataAdapter.buildContainerMetaData(dep, unit);
   }
}
