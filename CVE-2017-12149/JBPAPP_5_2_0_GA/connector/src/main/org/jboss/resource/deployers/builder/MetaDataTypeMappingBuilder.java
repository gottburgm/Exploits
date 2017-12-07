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
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentMetaData;
import org.jboss.system.metadata.ServiceAttributeMetaData;
import org.jboss.system.metadata.ServiceMetaData;

/**
 * A MetaDataTypeMappingBuilder.
 * 
 * @author <a href="weston.price@jboss.org">Weston Price</a>
 * @version $Revision: 85945 $
 */
public class MetaDataTypeMappingBuilder extends AbstractBuilder
{
   private static final String BINDING_JMX = "jboss.jdbc:service=metadata,datasource=";
   private static final String BINDING = "org.jboss.ejb.plugins.cmp.jdbc.metadata.DataSourceMetaData";

   @Override
   public List<ServiceAttributeMetaData> buildAttributes(ManagedConnectionFactoryDeploymentMetaData md)
   {
   
      List<ServiceAttributeMetaData> attributes = new ArrayList<ServiceAttributeMetaData>();
      ServiceAttributeMetaData attribute = null;
      
      attribute = buildDependencyAttribute("MetadataLibrary", "jboss.jdbc:service=metadata");
      attributes.add(attribute);
      
      String typeMapping = null;
      
      if(md.getDBMSMetaData() != null)
      {
         if(md.getDBMSMetaData().getTypeMapping() != null)
         {
            typeMapping = md.getDBMSMetaData().getTypeMapping();
         }
      
      }else if(md.getTypeMapping() != null)
      {
         typeMapping = md.getTypeMapping();
      }
      
      if(typeMapping != null)
      {
         attribute = buildSimpleAttribute("TypeMapping", typeMapping);
         attributes.add(attribute);
      }
            
      return attributes;
   }
   
   @Override
   public ServiceMetaData build(ManagedConnectionFactoryDeploymentMetaData mcfmd)
   {
      ServiceMetaData md = null;
      
      if(mcfmd.getTypeMapping() != null || mcfmd.getDBMSMetaData() != null)
      {
         md = super.build(mcfmd);
         
      }      
      
      return md;
      
   }
   
   @Override
   public ObjectName buildObjectName(ManagedConnectionFactoryDeploymentMetaData md)
   {
      ObjectName on = null;
      
      if(md.getTypeMapping() != null || md.getDBMSMetaData() != null)
      {
         on =  ObjectNameFactory.create(BINDING_JMX + md.getJndiName());   
         
      }
      
      return on;
   }

   @Override
   public String getCode(ManagedConnectionFactoryDeploymentMetaData md)
   {
      return BINDING;
   }

}
