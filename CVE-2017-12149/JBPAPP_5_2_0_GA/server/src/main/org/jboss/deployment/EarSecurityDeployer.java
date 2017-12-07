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

import org.jboss.deployment.security.AbstractSecurityDeployer;
import org.jboss.deployment.security.EarJaccPolicy;
import org.jboss.deployment.security.EarPolicyConfigurationFacade;
import org.jboss.metadata.ear.jboss.JBossAppMetaData;
import org.jboss.system.metadata.ServiceMetaData;

//$Id: EarSecurityDeployer.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

/**
 *  Security Deployer for ear
 *  @author Anil.Saldhana@redhat.com
 *  @since  Feb 17, 2008 
 *  @version $Revision: 85945 $
 */
public class EarSecurityDeployer extends AbstractSecurityDeployer<JBossAppMetaData>
{    
   
   @Override
   protected ServiceMetaData getServiceMetaData()
   {
      ServiceMetaData subjaccPolicy = new ServiceMetaData();
      subjaccPolicy.setCode(EarPolicyConfigurationFacade.class.getName());
      return subjaccPolicy;
   }

   @Override
   protected Class<JBossAppMetaData> getMetaDataClassType()
   {
      return JBossAppMetaData.class;
   }
   
   protected String getJaccPolicyName()
   {
      return EarJaccPolicy.class.getName();
   } 
}