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
package org.jboss.ejb.deployers;

import org.jboss.deployment.security.AbstractSecurityDeployer;
import org.jboss.deployment.security.EjbJaccPolicy;
import org.jboss.deployment.security.EjbPolicyConfigurationFacade;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.system.metadata.ServiceMetaData;


/**
 *  Security Deployer for ejb-jar
 *  @author Anil.Saldhana@redhat.com
 *  @since  Feb 17, 2008 
 *  @version $Revision: 85945 $
 */
public class EjbSecurityDeployer 
extends AbstractSecurityDeployer<JBossMetaData>
{   
   public EjbSecurityDeployer()
   {
      super(); 
      addInput(MergedJBossMetaDataDeployer.EJB_MERGED_ATTACHMENT_NAME); 
   }  

   @Override
   protected ServiceMetaData getServiceMetaData()
   {
      ServiceMetaData serviceMetaData = new ServiceMetaData();
      serviceMetaData.setCode(EjbPolicyConfigurationFacade.class.getName());
      return serviceMetaData;
   }

   @Override
   protected Class<JBossMetaData> getMetaDataClassType()
   { 
      return JBossMetaData.class;
   }

   @Override
   protected String getJaccPolicyName()
   {
      return EjbJaccPolicy.class.getName();
   }
}