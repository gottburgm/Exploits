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
package org.jboss.deployment.security;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;

import org.jboss.ejb.EJBPermissionMapping;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData; 

//$Id: EjbPolicyConfigurationFacade.java 86126 2009-03-19 19:46:45Z anil.saldhana@jboss.com $

/**
 *  A facade for constructing Permissions into the PolicyConfiguration
 *  @author Anil.Saldhana@redhat.com
 *  @since  Feb 18, 2008 
 *  @version $Revision: 86126 $
 */
public class EjbPolicyConfigurationFacade<T extends JBossMetaData> 
extends PolicyConfigurationFacade<JBossMetaData>
{ 
   public EjbPolicyConfigurationFacade(String id, T md)
   {
      super(id, md); 
   }

   @Override
   protected void createPermissions(JBossMetaData metaData, 
         PolicyConfiguration policyConfiguration) throws PolicyContextException
   {
      JBossEnterpriseBeansMetaData beans = metaData.getEnterpriseBeans();
      for(JBossEnterpriseBeanMetaData jBossEnterpriseBeanMetaData : beans)
      {
         EJBPermissionMapping.createPermissions(jBossEnterpriseBeanMetaData, 
                                                           policyConfiguration);
      } 
   } 
}