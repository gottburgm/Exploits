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
package org.jboss.system.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.management.ObjectName;

import org.jboss.dependency.spi.ControllerMode;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementObjectID;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.w3c.dom.Element;

/**
 * ServiceMetaData.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author Anil.Saldhana@redhat.com
 * @version $Revision: 85945 $
 */
@ManagementObject
public class ServiceMetaData extends AbstractMetaDataVisitorNode
   implements Serializable
{
   private static final long serialVersionUID = 1;

   /** The default XMBean class, when one is not specified */
   public static final String XMBEAN_CODE = "org.jboss.mx.modelmbean.XMBean";
   
   /** The ObjectName */
   private ObjectName objectName;
   
   /** The code */
   private String code;

   /** The mode */
   private ControllerMode mode;
   
   /** The interface name */
   private String interfaceName;
   
   /** The constructor */
   private ServiceConstructorMetaData constructor;
   
   /** The XMBean dds */
   private String xmbeanDD;
   
   /** The XMBean code */
   private String xmbeanCode = XMBEAN_CODE;
   
   /** The XMBean descriptor */
   private Element xmbeanDescriptor;
   
   /** The attributes */
   private List<ServiceAttributeMetaData> attributes = Collections.emptyList();
   
   /** The dependencies */
   private List<ServiceDependencyMetaData> dependencies = Collections.emptyList();
   /** The annotations */
   private List<ServiceAnnotationMetaData> annotations = Collections.emptyList();

   /** The class loader */
   private ObjectName classLoaderName;

   private List<String> aliases;

   /**
    * Get the objectName.
    * 
    * @return the objectName.
    */
   @ManagementProperty(description="The service JMX object name")
   @ManagementObjectID(type="ServiceMBean")
   public ObjectName getObjectName()
   {
      return objectName;
   }

   /**
    * Set the objectName.
    * 
    * @param objectName the objectName.
    */
   public void setObjectName(ObjectName objectName)
   {
      if (objectName == null)
         throw new IllegalArgumentException("Null objectName");
      this.objectName = objectName;
   }

   /**
    * Get the code.
    * 
    * @return the code.
    */
   @ManagementProperty(description="The service bean class")
   public String getCode()
   {
      return code;
   }

   /**
    * Set the code.
    * 
    * @param code the code.
    */
   public void setCode(String code)
   {
      if (code == null)
         throw new IllegalArgumentException("Null code");
      this.code = code;
   }

   /**
    * Get the mode.
    * 
    * @return the mode.
    */
   public ControllerMode getMode()
   {
      return mode;
   }

   /**
    * Set the mode.
    * 
    * @param mode the mode.
    */
   public void setMode(ControllerMode mode)
   {
      this.mode = mode;
   }

   /**
    * Get the constructor.
    * 
    * @return the constructor.
    */
   public ServiceConstructorMetaData getConstructor()
   {
      return constructor;
   }

   /**
    * Set the constructor.
    * 
    * @param constructor the constructor.
    */
   public void setConstructor(ServiceConstructorMetaData constructor)
   {
      this.constructor = constructor;
   }

   /**
    * Get the interfaceName.
    * 
    * @return the interfaceName.
    */
   public String getInterfaceName()
   {
      return interfaceName;
   }

   /**
    * Set the interfaceName.
    * 
    * @param interfaceName the interfaceName.
    */
   public void setInterfaceName(String interfaceName)
   {
      this.interfaceName = interfaceName;
   }

   /**
    * Get the xmbeanCode.
    * 
    * @return the xmbeanCode.
    */
   public String getXMBeanCode()
   {
      return xmbeanCode;
   }

   /**
    * Set the xmbeanCode.
    * 
    * @param xmbeanCode the xmbeanCode.
    */
   public void setXMBeanCode(String xmbeanCode)
   {
      if (xmbeanCode == null)
         throw new IllegalArgumentException("Null xmbeanCode");
      this.xmbeanCode = xmbeanCode;
   }

   /**
    * Get the xmbeanDD.
    * 
    * @return the xmbeanDD.
    */
   public String getXMBeanDD()
   {
      return xmbeanDD;
   }

   /**
    * Set the xmbeanDD.
    * 
    * @param xmbeanDD the xmbeanDD.
    */
   public void setXMBeanDD(String xmbeanDD)
   {
      this.xmbeanDD = xmbeanDD;
   }

   /**
    * Get the xmbeanDescriptor.
    * 
    * @return the xmbeanDescriptor.
    */
   public Element getXMBeanDescriptor()
   {
      return xmbeanDescriptor;
   }

   /**
    * Set the xmbeanDescriptor.
    * 
    * @param xmbeanDescriptor the xmbeanDescriptor.
    */
   public void setXMBeanDescriptor(Element xmbeanDescriptor)
   {
      this.xmbeanDescriptor = xmbeanDescriptor;
   }
   
   /**
    * Add an attribute
    * @param serviceAttributeMetaData
    */
   public void addAttribute(ServiceAttributeMetaData serviceAttributeMetaData)
   {
      if(serviceAttributeMetaData == null)
         throw new IllegalArgumentException("null serviceAttributeMetaData");
      if(this.attributes.size() == 0)
         this.attributes = new ArrayList<ServiceAttributeMetaData>();
      if(this.attributes.contains(serviceAttributeMetaData) == false)
         this.attributes.add(serviceAttributeMetaData); 
   }

   /**
    * Add an attribute
    * @param attribute
    */
   public void addAttributes(List<ServiceAttributeMetaData> aAttributeList)
   {
      if(aAttributeList == null)
         throw new IllegalArgumentException("Null attribute");
      if(this.attributes.size() == 0)
         this.attributes = aAttributeList;
      else
      {
         //Avoid duplicates
         for(ServiceAttributeMetaData serviceAttributeMetaData: aAttributeList)
         {
            addAttribute(serviceAttributeMetaData);
         }   
      } 
   }
   
   /**
    * Remove a list of Attributes
    * @param aAttributeList
    */
   public void removeAttributes(List<ServiceAttributeMetaData> aAttributeList)
   {
      this.attributes.removeAll(aAttributeList);
   }
   
   
   /**
    * Get the attributes.
    * 
    * @return the attributes.
    */
   public List<ServiceAttributeMetaData> getAttributes()
   {
      return attributes;
   }

   /**
    * Set the attributes.
    * 
    * @param attributes the attributes.
    */
   public void setAttributes(List<ServiceAttributeMetaData> attributes)
   {
      if (attributes == null)
         throw new IllegalArgumentException("Null attributes");
      this.attributes = attributes;
   }

   /**
    * Get the dependencies.
    * 
    * @return the dependencies.
    */
   public List<ServiceDependencyMetaData> getDependencies()
   {
      return dependencies;
   }

   /**
    * Add  dependency
    * @param dependencyList
    */
   public void addDependency(ServiceDependencyMetaData serviceDependencyMetaData)
   {
      if(serviceDependencyMetaData == null)
         throw new IllegalArgumentException("Null serviceDependencyMetaData");
      if(this.dependencies.size() == 0)
         this.dependencies = new ArrayList<ServiceDependencyMetaData>();
      
       if(this.dependencies.contains(serviceDependencyMetaData) == false)
          this.dependencies.add(serviceDependencyMetaData); 
   }
   
   /**
    * Add  dependencies
    * @param dependencyList
    */
   public void addDependencies(List<ServiceDependencyMetaData> dependencyList)
   {
      if(dependencyList == null)
         throw new IllegalArgumentException("Null dependency");
      if(this.dependencies.size() == 0)
         this.dependencies = dependencyList;
      else
      {
         for(ServiceDependencyMetaData serviceDependencyMetaData: dependencyList)
         {
           addDependency(serviceDependencyMetaData);    
         }
      }  
   }
   
   /**
    * Remove a dependency
    * @param dependency
    */
   public void removeDependency(ServiceDependencyMetaData dependency)
   {
      this.dependencies.remove(dependency);
   }
   
   /**
    * Set the dependencies.
    * 
    * @param dependencies the dependencies.
    */
   public void setDependencies(List<ServiceDependencyMetaData> dependencies)
   {
      if (dependencies == null)
         throw new IllegalArgumentException("Null dependencies");
      this.dependencies = dependencies;
   }

   /**
    * Get the service annotations
    * @return the annotations
    */
   public List<ServiceAnnotationMetaData> getAnnotations()
   {
      return annotations;
   }
   /**
    * Set the service annotations
    * @param annotation - the annotations metadata
    */
   public void setAnnotations(List<ServiceAnnotationMetaData> annotations)
   {
      if (annotations == null)
         throw new IllegalArgumentException("Null annotations");
      this.annotations = annotations;      
   }

   /**
    * Get the classLoaderName.
    * 
    * @return the classLoaderName.
    */
   public ObjectName getClassLoaderName()
   {
      return classLoaderName;
   }

   /**
    * Set the classLoaderName.
    * 
    * @param classLoaderName the classLoaderName.
    */
   public void setClassLoaderName(ObjectName classLoaderName)
   {
      this.classLoaderName = classLoaderName;
   }

   /**
    * Get the aliases.
    *
    * @return aliases
    */
   public List<String> getAliases()
   {
      return aliases;
   }

   /**
    * Set the aliases.
    *
    * @param aliases the aliases
    */
   public void setAliases(List<String> aliases)
   {
      this.aliases = aliases;
   }

   protected void addChildren(Set<ServiceMetaDataVisitorNode> children)
   {
      children.addAll(attributes);
      children.addAll(dependencies);
   }

}
