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
package org.jboss.web.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployment.security.AbstractSecurityDeployer;
import org.jboss.deployment.security.WarJaccPolicy;
import org.jboss.deployment.security.WarPolicyConfigurationFacade;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.system.metadata.ServiceMetaData;

//$Id: WarSecurityDeployer.java 86126 2009-03-19 19:46:45Z anil.saldhana@jboss.com $

/**
 *  Security Deployer for Web Archives 
 *  @author Anil.Saldhana@redhat.com
 *  @since  Feb 17, 2008 
 *  @version $Revision: 86126 $
 */
public class WarSecurityDeployer extends AbstractSecurityDeployer<JBossWebMetaData>
{   
   
   @Override
   public void deploy(DeploymentUnit unit) throws DeploymentException
   { 
      ClassLoader oldCL = null;
      // Set the TCL
      try
      {
         //JBAS-6607: JBossXACML needs the tcl to locate the xacml policies
         //The TCL would be the CL for VFS for the security deployer beans
         //Deployment Unit CL would be the war CL. Hence pick the DU CL as TCL.
         oldCL = SecurityActions.getContextClassLoader();
         SecurityActions.setContextClassLoader(unit.getClassLoader()); 
         super.deploy(unit);
      }
      finally
      {
         SecurityActions.setContextClassLoader(oldCL); 
      } 
   }

   @Override
   protected ServiceMetaData getServiceMetaData()
   {
      ServiceMetaData serviceMetaData = new ServiceMetaData();
      serviceMetaData.setCode(WarPolicyConfigurationFacade.class.getName());
      return serviceMetaData;
   }

   @Override
   protected Class<JBossWebMetaData> getMetaDataClassType()
   { 
      return JBossWebMetaData.class;
   }

   @Override
   protected String getJaccPolicyName()
   {
      return WarJaccPolicy.class.getName();
   }
}