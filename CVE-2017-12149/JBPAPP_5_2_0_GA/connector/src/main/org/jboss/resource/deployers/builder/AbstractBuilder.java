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

import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentMetaData;
import org.jboss.system.metadata.ServiceAttributeMetaData;
import org.jboss.system.metadata.ServiceConstructorMetaData;
import org.jboss.system.metadata.ServiceDependencyMetaData;
import org.jboss.system.metadata.ServiceDependencyValueMetaData;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.metadata.ServiceTextValueMetaData;

/**
 * A AbstractBuilder.
 * 
 * @author <a href="weston.price@jboss.org">Weston Price</a>
 * @version $Revision: 85945 $
 */
public abstract class AbstractBuilder
{   
   //TODO make this simple string in anticipation of move to MC
   public abstract ObjectName buildObjectName(ManagedConnectionFactoryDeploymentMetaData md);
   public abstract String getCode(ManagedConnectionFactoryDeploymentMetaData md);

   public ServiceMetaData buildService(ManagedConnectionFactoryDeploymentMetaData mcfmd)
   {
      ServiceMetaData md = new ServiceMetaData();
      ObjectName on = buildObjectName(mcfmd);
      md.setObjectName(on);
      String code = getCode(mcfmd);
      md.setCode(code);
      ServiceConstructorMetaData cmd = buildConstructor(mcfmd);
      md.setConstructor(cmd);
      return md;
      
   }
   
   
   public ServiceMetaData build(ManagedConnectionFactoryDeploymentMetaData mcfmd)
   {
      ServiceMetaData md = buildService(mcfmd);
      List<ServiceAttributeMetaData> attributes = buildAttributes(mcfmd);
      md.setAttributes(attributes);      
      List<ServiceDependencyMetaData> dependencies = buildDependencies(mcfmd);
      md.setDependencies(dependencies);
      
      return md;      
      
   }
   
   public abstract List<ServiceAttributeMetaData> buildAttributes(ManagedConnectionFactoryDeploymentMetaData md);
   
   public ServiceConstructorMetaData buildConstructor(ManagedConnectionFactoryDeploymentMetaData mcfmd)
   {
      ServiceConstructorMetaData constructor = new ServiceConstructorMetaData();
      constructor.setParameters(new Object[]{});
      constructor.setParams(new String[]{});      
      return constructor;
   
   }
   
   
   public ServiceAttributeMetaData buildSimpleAttribute(String name, String value)
   {
      ServiceAttributeMetaData att = new ServiceAttributeMetaData();
      att.setName(name);
      ServiceTextValueMetaData dep = new ServiceTextValueMetaData(value);
      att.setValue(dep);
      return att;
      
   }
   
   public ServiceDependencyMetaData buildDependency(String dependencyName)
   {
      ServiceDependencyMetaData dependency = new ServiceDependencyMetaData();
      dependency.setIDependOn(dependencyName);
      return dependency;      
       
   }
   
   public List<ServiceDependencyMetaData> buildDependencies(ManagedConnectionFactoryDeploymentMetaData md)
   {      
      return new ArrayList<ServiceDependencyMetaData>();      
   }

   public ServiceAttributeMetaData buildDependencyAttribute(String name, String dependency)
   {
      ServiceAttributeMetaData att = new ServiceAttributeMetaData();
      ServiceDependencyValueMetaData dep = new ServiceDependencyValueMetaData();
      dep.setDependency(dependency);      
         
      if(name != null)
         att.setName(name);
      
      att.setValue(dep);
      return att;
   
   }
   
   
}
