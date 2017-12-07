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
import java.util.List;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.resource.metadata.mcf.DataSourceDeploymentMetaData;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentMetaData;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryTransactionSupportMetaData;
import org.jboss.system.metadata.ServiceAttributeMetaData;
import org.jboss.system.metadata.ServiceTextValueMetaData;

/**
 * A ConnectionFactoryBindingBuilder.
 * 
 * @author <a href="weston.price@jboss.org">Weston Price</a>
 * @version $Revision: 85945 $
 */
public class ConnectionFactoryBindingBuilder extends AbstractBuilder
{
   private static final String DATASOURCE = "org.jboss.resource.adapter.jdbc.remote.WrapperDataSourceService";
   private static final String CONNECTION_FACTORY = "org.jboss.resource.connectionmanager.ConnectionFactoryBindingService";
   private static final String DATASOURCE_JMX = "jboss.jca:service=DataSourceBinding,name=";
   private static final String CONNNECTION_FACTORY_JMX = "jboss.jca:service=ConnectionFactoryBinding,name=";
   
   @Override
   public List<ServiceAttributeMetaData> buildAttributes(ManagedConnectionFactoryDeploymentMetaData md)
   {
      List<ServiceAttributeMetaData> attributes = new ArrayList<ServiceAttributeMetaData>();
      ServiceAttributeMetaData attribute = new ServiceAttributeMetaData();
      
      attribute = buildSimpleAttribute("JndiName", md.getJndiName());
      attributes.add(attribute);

      attribute = buildSimpleAttribute("UseJavaContext", String.valueOf(md.isUseJavaContext()));
      attributes.add(attribute);
      
      if(!md.isUseJavaContext() && md instanceof DataSourceDeploymentMetaData)
      {
         attribute = buildDependencyAttribute("JMXInvokerName", md.getJmxInvokerName());
         attributes.add(attribute);
      }
      
      attribute = buildDependencyAttribute("ConnectionManager", buildCMName(md));
      attributes.add(attribute);
      return attributes;
   }

   @Override
   public ObjectName buildObjectName(ManagedConnectionFactoryDeploymentMetaData md)
   {
      
      ObjectName on = null;
      
      if(md instanceof DataSourceDeploymentMetaData)
      {
         on = ObjectNameFactory.create(DATASOURCE_JMX + md.getJndiName());
      }
      else
      {
         on = ObjectNameFactory.create(CONNNECTION_FACTORY_JMX + md.getJndiName());
         
      }
      return on;
   }
   
   @Override
   public String getCode(ManagedConnectionFactoryDeploymentMetaData md)
   {
      
      return (md instanceof DataSourceDeploymentMetaData) ? DATASOURCE : CONNECTION_FACTORY;
            
   }

   private String buildCMName(ManagedConnectionFactoryDeploymentMetaData md)
   {
      //THIS IS A HACK!
      String cmType = "jboss.jca:service=";
      String jndiName = md.getJndiName();
      
      if(md.getTransactionSupportMetaData().equals(ManagedConnectionFactoryTransactionSupportMetaData.NONE))
      {
         cmType += "NoTxCM,name=" + jndiName;         
      
      }
      else if(md.getTransactionSupportMetaData().equals(ManagedConnectionFactoryTransactionSupportMetaData.LOCAL))
      {
         if(md instanceof DataSourceDeploymentMetaData)
         {
            cmType += "LocalTxCM,name=" + jndiName;               
         }
         else
         {
            cmType += "TxCM,name=" + jndiName;               
            
         }

      }else
      {
         if(md instanceof DataSourceDeploymentMetaData)
         {
            cmType += "XATxCM,name=" + jndiName;               
            
         }
         else
         {
            cmType += "TxCM,name=" + jndiName;               
            
         }
      }
      
      return cmType;
   }
}
