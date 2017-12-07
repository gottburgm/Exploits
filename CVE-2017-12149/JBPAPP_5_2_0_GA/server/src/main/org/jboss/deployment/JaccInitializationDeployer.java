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

import java.util.Set;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;

import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * Create a JACC policy if the DeploymentUnit contains a named attachment
 * for example, if it contains org.jboss.metadata.ear.spec.EarMetaData.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author adrian@jboss.org 
 * @version $Revision: 85945 $
 */
public class JaccInitializationDeployer extends AbstractRealDeployer
{
   /**
    * Create a new JaccInitializationDeployer.
    */
   public JaccInitializationDeployer()
   {
      setOutput(PolicyConfiguration.class);
   }
   
   private Set<String> acceptedAttachments;

   public Set<String> getAcceptedAttachments()
   {
      return acceptedAttachments;
   }

   public void setAcceptedAttachments(Set<String> acceptedAttachments)
   {
      this.acceptedAttachments = acceptedAttachments;
   }

   public void internalDeploy(DeploymentUnit unit) throws org.jboss.deployers.spi.DeploymentException
   {
      boolean accepted = false;
      for (String accept : acceptedAttachments)
      {
         if (unit.isAttachmentPresent(accept))
         {
            accepted = true;
            break;
         }
      }
      if (accepted == false)
         return;

      String contextID = unit.getName();
      PolicyConfiguration pc = null;
      try
      {
         PolicyConfigurationFactory pcFactory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
         pc = pcFactory.getPolicyConfiguration(contextID, true);
      }
      catch (Exception e)
      {
         throw new RuntimeException("failed to initialize JACC for unit: " + unit.getName(), e);
      }
      unit.addAttachment(PolicyConfiguration.class, pc);
   }
}
