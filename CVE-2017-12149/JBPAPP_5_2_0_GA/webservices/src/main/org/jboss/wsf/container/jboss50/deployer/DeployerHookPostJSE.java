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

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.wsf.spi.deployment.Deployment;

/**
 * @author Heiko.Braun@jboss.com
 * @author Thomas.Diesler@jboss.com
 */
public abstract class DeployerHookPostJSE extends AbstractDeployerHookJSE
{
   /**
    * The deployment should be created in phase 1.
    */
   public Deployment createDeployment(DeploymentUnit unit)
   {
      Deployment dep = unit.getAttachment(Deployment.class);
      if (null == dep)
         throw new IllegalStateException("spi.Deployment missing. It should be created in Phase 1");

      return dep;
   }

   /**
    * A phase 2 deployer hook needs to reject first-place
    * JSE deployments and wait for those that are re-written.
    * We rely on the fact that spi.Deployment is created in phase 1.    
    */
   @Override
   public boolean isWebServiceDeployment(DeploymentUnit unit)
   {
      if (super.isWebServiceDeployment(unit) == false)
         return false;

      Deployment deployment = unit.getAttachment(Deployment.class);
      return deployment != null;
   }
}
