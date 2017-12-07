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
package org.jboss.deployment;

// $Id: DeploymentProvidedDeploymentUnitFilter.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.DeploymentUnitFilter;
import org.jboss.deployers.vfs.spi.structure.helpers.VFS2BaseBridgeDeploymentUnitFilter;

/**
 * A DeploymentUnitFilter that delegates its work the DeploymentUnitFilter that
 * is attached to the DeploymentUnit.
 * 
 * A Deployer can attach a DeploymentUnitFilter to a DeploymentUnit which controlls whether 
 * the DeploymentUnit is to be processed by following Deployers
 *
 * @author Thomas.Diesler@jboss.com
 * @since 04-Mar-2009
 */
public class DeploymentProvidedDeploymentUnitFilter extends VFS2BaseBridgeDeploymentUnitFilter
{
   @Override
   protected boolean doAccepts(DeploymentUnit unit)
   {
      DeploymentUnitFilter filter = unit.getTopLevel().getAttachment(DeploymentUnitFilter.class);
      return filter == null || filter.accepts(unit);
   }
}