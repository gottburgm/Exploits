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

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;

import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * Second phase of JACC Policy deployment.  If the DU has a policy attachment
 * link it to any parent PC and then commit it.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author adrian@jboss.org
 * @version $Revision: 85945 $
 */
public class JaccCommitDeployer extends AbstractRealDeployer
{
   /**
    * Create a new JaccCommitDeployer.
    */
   public JaccCommitDeployer()
   {
      setInput(PolicyConfiguration.class);
   }
   
   public void internalDeploy(DeploymentUnit unit) throws org.jboss.deployers.spi.DeploymentException
   {
      PolicyConfiguration pc = unit.getAttachment(PolicyConfiguration.class);
      if (pc == null)
         return;

      DeploymentUnit parent = unit.getParent();
      if (parent == null)
         throw new IllegalStateException("Unit has not parent: " + unit);
      PolicyConfiguration parentPc = parent.getAttachment(PolicyConfiguration.class);
      try
      {
         if (parentPc != null && pc != parentPc)
         {
            parentPc.linkConfiguration(pc);
         }
         pc.commit();
      }
      catch (PolicyContextException e)
      {
         throw new RuntimeException("Failed to commit PolicyConfiguration for unit: " + unit.getName(), e);
      }
   }

}
