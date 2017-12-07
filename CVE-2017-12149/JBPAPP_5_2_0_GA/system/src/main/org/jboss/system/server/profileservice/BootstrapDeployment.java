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
package org.jboss.system.server.profileservice;

import org.jboss.deployers.plugins.attachments.AttachmentsImpl;
import org.jboss.deployers.plugins.structure.ContextInfoImpl;
import org.jboss.deployers.plugins.structure.StructureMetaDataImpl;
import org.jboss.deployers.spi.DeploymentState;
import org.jboss.deployers.spi.management.KnownDeploymentTypes;
import org.jboss.deployers.spi.structure.ContextInfo;
import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentContext;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentUnit;
import org.jboss.kernel.spi.deployment.KernelDeployment;

/**
 * A DeploymentUnit for exposing the MCServer deployments
 * 
 * @see org.jboss.bootstrap.spi.microcontainer.MCServer#getDeployments
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 87850 $
 */
public class BootstrapDeployment extends AbstractDeploymentUnit
   implements DeploymentUnit
{
   private static final long serialVersionUID = 1;
   private AttachmentsImpl predeterminedManagedObjects = new AttachmentsImpl();
   private transient KernelDeployment deployment;

   public BootstrapDeployment(KernelDeployment deployment)
   {
      super(new AbstractDeploymentContext(deployment.getName(), ""));
      this.deployment = deployment;
      // Create a pre-determined, DEPLOYED deployment
      StructureMetaData structure = new StructureMetaDataImpl();
      ContextInfo rootInfo = new ContextInfoImpl("");
      structure.addContext(rootInfo);
      predeterminedManagedObjects.addAttachment(KnownDeploymentTypes.class, KnownDeploymentTypes.MCBeans);
      predeterminedManagedObjects.addAttachment(StructureMetaData.class, structure);
      predeterminedManagedObjects.addAttachment(KernelDeployment.class, deployment);
      DeploymentContext rootContext = getDeploymentContext();
      rootContext.setState(DeploymentState.DEPLOYED);
      predeterminedManagedObjects.addAttachment(DeploymentContext.class, rootContext);
      rootContext.setPredeterminedManagedObjects(predeterminedManagedObjects);
   }

   public String getName()
   {
      return deployment.getName();
   }

   public String getSimpleName()
   {
      String name = deployment.getName();
      int slash = name.lastIndexOf('/');
      if(slash > 0)
         name = name.substring(slash+1);
      return name;
   }


}
