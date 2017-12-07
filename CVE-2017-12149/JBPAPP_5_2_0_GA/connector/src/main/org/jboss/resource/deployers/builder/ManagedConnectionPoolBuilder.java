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
package org.jboss.resource.deployers.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentMetaData;
import org.jboss.resource.metadata.mcf.SecurityDeploymentType;
import org.jboss.system.metadata.ServiceAttributeMetaData;

/**
 * A ManagedConnectionPoolBuilder.
 * 
 * @author <a href="weston.price@jboss.org">Weston Price</a>
 * @version $Revision: 111895 $
 */
public class ManagedConnectionPoolBuilder extends AbstractBuilder 
{
   private static final String POOL = "org.jboss.resource.connectionmanager.JBossManagedConnectionPool";   
   private static final Map<SecurityDeploymentType, String> securityTypeMap = new HashMap<SecurityDeploymentType, String>();
   private static final String MCF_JMX = "jboss.jca:service=ManagedConnectionFactory,name=";
   
   static
   {
      securityTypeMap.put(SecurityDeploymentType.NONE, "ByNothing");      
      securityTypeMap.put(SecurityDeploymentType.APPLICATION, "ByApplication");
      securityTypeMap.put(SecurityDeploymentType.DOMAIN, "ByContainer");
      securityTypeMap.put(SecurityDeploymentType.DOMAIN_AND_APPLICATION, "ByContainerAndApplication");   
   }
      
   @Override
   public ObjectName buildObjectName(ManagedConnectionFactoryDeploymentMetaData md)
   {
     return ObjectNameFactory.create("jboss.jca:service=ManagedConnectionPool,name=" + md.getJndiName());    
   }
   
   @Override
   public String getCode(ManagedConnectionFactoryDeploymentMetaData md)
   {
      return POOL;
   }
   
   @Override
   public List<ServiceAttributeMetaData> buildAttributes(ManagedConnectionFactoryDeploymentMetaData md)
   {
      List<ServiceAttributeMetaData> poolAttributes = new ArrayList<ServiceAttributeMetaData>();
      ServiceAttributeMetaData poolAttribute = null;

      poolAttribute = buildSimpleAttribute("PoolJndiName", md.getJndiName());
      poolAttributes.add(poolAttribute);
      
      poolAttribute = buildSimpleAttribute("MinSize", String.valueOf(md.getMinSize()));      
      poolAttributes.add(poolAttribute);
      
      poolAttribute = buildSimpleAttribute("MaxSize", String.valueOf(md.getMaxSize()));
      poolAttributes.add(poolAttribute);
      
      poolAttribute = buildSimpleAttribute("BlockingTimeoutMillis", String.valueOf(md.getBlockingTimeoutMilliSeconds()));
      poolAttributes.add(poolAttribute);

      poolAttribute = buildSimpleAttribute("IdleTimeoutMinutes", String.valueOf(md.getIdleTimeoutMinutes()));
      poolAttributes.add(poolAttribute);

      poolAttribute = buildSimpleAttribute("BackGroundValidationMillis", String.valueOf(md.getBackgroundValidationMillis()));
      poolAttributes.add(poolAttribute);
      
      poolAttribute = buildSimpleAttribute("PreFill", String.valueOf(md.getPrefill()));
      poolAttributes.add(poolAttribute);
      
      poolAttribute = buildSimpleAttribute("StrictMin", String.valueOf(md.getUseStrictMin()));
      poolAttributes.add(poolAttribute);    
      
      poolAttribute = buildSimpleAttribute("StatisticsFormatter", md.getStatisticsFormatter());
      poolAttributes.add(poolAttribute);
            
      poolAttribute = buildSimpleAttribute("UseFastFail", String.valueOf(md.getUseFastFail()));
      poolAttributes.add(poolAttribute);

      //Temp fix for JAXB marshalling issue
      if(md.getSecurityMetaData() == null)
      {
         poolAttribute = buildSimpleAttribute("Criteria", "ByNothing");         
      }
      else
      {
         poolAttribute = buildSimpleAttribute("Criteria", String.valueOf(getCriteria(md.getSecurityMetaData().getSecurityDeploymentType())));
      }

      poolAttributes.add(poolAttribute);

      poolAttribute = buildSimpleAttribute("NoTxSeparatePools", String.valueOf(md.getNoTxSeparatePools()));
      poolAttributes.add(poolAttribute);
            
      poolAttribute = buildDependencyAttribute("ManagedConnectionFactoryName", MCF_JMX + md.getJndiName());
      poolAttributes.add(poolAttribute);
      
      return poolAttributes;

   }

   private String getCriteria(SecurityDeploymentType type)
   {
      return securityTypeMap.get(type);      
   }

}
