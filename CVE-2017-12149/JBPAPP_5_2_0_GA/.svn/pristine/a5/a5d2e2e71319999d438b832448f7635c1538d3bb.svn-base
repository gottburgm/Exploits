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
package org.jboss.management.j2ee.deployers;

import javax.management.MBeanServer;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;

/**
 * Abstract vfs jsr77 deployer.
 *
 * @param <T> exact input type
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class AbstractVFSJSR77Deployer<T> extends AbstractJSR77Deployer<T>
{
   protected AbstractVFSJSR77Deployer(Class<T> input)
   {
      super(input);
      setRequiresVFSDeployment(true);
   }

   protected void deployJsr77(MBeanServer server, DeploymentUnit unit, T metaData) throws Throwable
   {
      if (unit instanceof VFSDeploymentUnit)
         deployJsr77(server, (VFSDeploymentUnit)unit, metaData);
   }

   /**
    * Deploy jsr77 view.
    *
    * @param server the mbean server
    * @param unit the deployment unit
    * @param metaData the metadata
    * @throws Throwable for any error
    */
   protected abstract void deployJsr77(MBeanServer server, VFSDeploymentUnit unit, T metaData) throws Throwable;

   protected void undeployJsr77(MBeanServer server, DeploymentUnit unit, T metaData)
   {
      if (unit instanceof VFSDeploymentUnit)
         undeployJsr77(server, (VFSDeploymentUnit)unit, metaData);
   }

   /**
    * Undeploy jsr77 view.
    *
    * @param server the mbean server
    * @param unit the deployment unit
    * @param metaData the metadata
    */
   protected abstract void undeployJsr77(MBeanServer server, VFSDeploymentUnit unit, T metaData);
}