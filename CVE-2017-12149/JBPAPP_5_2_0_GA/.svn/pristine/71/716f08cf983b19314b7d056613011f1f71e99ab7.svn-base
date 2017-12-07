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
package org.jboss.deployment;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.WebMetaData;

/**
 * Support jsp's w/o web.xml.
 * See JBAS-6062 for more details.
 *
 * @author ales.justin@jboss.org
 */
public class LegacyWebXmlLessDeployer extends AbstractDeployer
{
   public LegacyWebXmlLessDeployer()
   {
      addInput(WebMetaData.class);
      addInput(JBossWebMetaData.class);
      setOutput(JBossWebMetaData.class);
      setStage(DeploymentStages.POST_PARSE);
   }

   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      if (unit.getSimpleName().endsWith(".war"))
      {
         if (unit.isAttachmentPresent(JBossWebMetaData.class))
            return;

         // only care about true deployments
         if (unit instanceof VFSDeploymentUnit == false)
            return;

         log.debug("Web archive doesn't contain web.xml: " + unit.getName());
         unit.getTransientManagedObjects().addAttachment(JBossWebMetaData.class, new JBossWebMetaData());
      }
   }
}