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

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractComponentDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * This is the deployer that calls the registered DeployerHooks
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 24-Apr-2007
 */
public abstract class AbstractWebServiceDeployer<T> extends AbstractComponentDeployer
{
   // provide logging
   private static final Logger log = Logger.getLogger(AbstractWebServiceDeployer.class);

   private List<DeployerHook> deployerHooks = new LinkedList<DeployerHook>();

   public void addDeployerHook(DeployerHook deployer)
   {
      log.debug("Add deployer hook: " + deployer);
      deployerHooks.add(deployer);
   }

   public void removeDeployerHook(DeployerHook deployer)
   {
      log.debug("Remove deployer hook: " + deployer);
      deployerHooks.remove(deployer);
   }

   @Override
   public void internalDeploy(DeploymentUnit unit) throws DeploymentException
   {
      for (DeployerHook deployer : deployerHooks) deployer.deploy(unit);
   }

   @Override
   public void internalUndeploy(DeploymentUnit unit)
   {
      for (DeployerHook deployer : deployerHooks) deployer.undeploy(unit);
   }
}
