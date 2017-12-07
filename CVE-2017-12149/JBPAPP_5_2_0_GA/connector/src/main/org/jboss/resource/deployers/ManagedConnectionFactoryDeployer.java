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
package org.jboss.resource.deployers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.spi.management.KnownComponentTypes;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.managed.api.ComponentType;
import org.jboss.resource.deployers.builder.AbstractBuilder;
import org.jboss.resource.metadata.mcf.LocalDataSourceDeploymentMetaData;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentGroup;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentMetaData;
import org.jboss.resource.metadata.mcf.NoTxDataSourceDeploymentMetaData;
import org.jboss.resource.metadata.mcf.XADataSourceDeploymentMetaData;
import org.jboss.resource.metadata.repository.DefaultJCAMetaDataRepository;
import org.jboss.system.metadata.ServiceDeployment;
import org.jboss.system.metadata.ServiceMetaData;

/**
 * A ManagedConnectionFactoryDeployer.
 * 
 * @author <a href="weston.price@jboss.org">Weston Price</a>
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @version $Revision: 85945 $
 */
public class ManagedConnectionFactoryDeployer
   extends AbstractSimpleRealDeployer<ManagedConnectionFactoryDeploymentGroup>
{
   /** The repository */
   private DefaultJCAMetaDataRepository repository;
   
   /** The builders */
   private List<AbstractBuilder> builders = new ArrayList<AbstractBuilder>();
   /** The ManagedConnectionFactoryDeploymentGroup props to manage */
   private String[] propNames = {
         "jndiName",
         "jdbcURL",
         "userName",
         "passWord",
         "driverClass",
         "minPoolSize",
         "maxPoolSize",
         "securityMetaData",
         "managedConnectionFactoryProperties",
   };
   /**
    * A mapping from the managed property name to the attachment property name.
    */
   private Map<String, String> propertyNameMappings = new HashMap<String, String>();

   public Map<String, String> getPropertyNameMappings()
   {
      return propertyNameMappings;
   }

   public void setPropertyNameMappings(Map<String, String> mangedToMBeanNames)
   {
      propertyNameMappings.clear();
      /*
      propertyNameMappings.putAll(mangedToMBeanNames);
      */
      // Need to reverse the mapping
      for(String key : mangedToMBeanNames.keySet())
      {
         String value = mangedToMBeanNames.get(key);
         propertyNameMappings.put(value, key);
      }
   }

   public String[] getPropNames()
   {
      return propNames;
   }

   public void setPropNames(String[] propNames)
   {
      this.propNames = propNames;
   }

   /**
    * Create a new ManagedConnectionFactoryDeployer.
    */
   public ManagedConnectionFactoryDeployer()
   {
      super(ManagedConnectionFactoryDeploymentGroup.class);
      setOutput(ServiceDeployment.class);
   }

   public void setBuilders(List<AbstractBuilder> builders)
   {
      this.builders = builders;
   }
   
   public List<AbstractBuilder> getBuilders()
   {
      return this.builders;
   }

   /**
    * Get the repository.
    * 
    * @return the repository.
    */
   public DefaultJCAMetaDataRepository getRepository()
   {
      return repository;
   }

   /**
    * Set the repository.
    * 
    * @param repository The repository to set.
    */
   public void setRepository(DefaultJCAMetaDataRepository repository)
   {
      this.repository = repository;
   }
   
   @Override
   public void deploy(DeploymentUnit unit, ManagedConnectionFactoryDeploymentGroup group) throws DeploymentException
   {
      
      List<ManagedConnectionFactoryDeploymentMetaData> deployments = group.getDeployments();
      ServiceDeployment serviceDeployment = new ServiceDeployment();
      List<ServiceMetaData> componentServices = new ArrayList<ServiceMetaData>();            
      List<ServiceMetaData> serviceDefintion = group.getServices();

      serviceDeployment.setName(unit.getName()+" services");
      //For some reason, this didn't like the addAll method
      for (ServiceMetaData data : serviceDefintion)
      {
         componentServices.add(data);
      }

      ComponentType type = null;
      for (ManagedConnectionFactoryDeploymentMetaData data : deployments)
      {
         // TODO: there should be multiple component types
         if( type == null )
         {
            if( data instanceof LocalDataSourceDeploymentMetaData )
               type = KnownComponentTypes.DataSourceTypes.LocalTx.getType();
            if( data instanceof NoTxDataSourceDeploymentMetaData )
               type = KnownComponentTypes.DataSourceTypes.NoTx.getType();
            if( data instanceof XADataSourceDeploymentMetaData )
               type = KnownComponentTypes.DataSourceTypes.XA.getType();
         }

         for (AbstractBuilder builder : builders)
         {
            ServiceMetaData candidate = builder.build(data);
            
            if(candidate != null)
            {
               componentServices.add(candidate);
            }
         }
      }
      
      serviceDeployment.setServices(componentServices);
      unit.addAttachment(ServiceDeployment.class, serviceDeployment);
      if( type != null )
         unit.addAttachment(ComponentType.class, type);
   }

   @Override
   public void undeploy(DeploymentUnit unit, ManagedConnectionFactoryDeploymentGroup deployment)
   {      
   }

}
