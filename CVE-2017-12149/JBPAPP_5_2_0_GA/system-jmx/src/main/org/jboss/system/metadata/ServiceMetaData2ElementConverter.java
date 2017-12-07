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

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.dependency.spi.ControllerMode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Basic converter for a flattened view of ServiceMetaData to a org.w3c.dom.Element.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85945 $
 */
public class ServiceMetaData2ElementConverter
{
   /** The Document */
   private final Document document;
   
   public ServiceMetaData2ElementConverter() throws Exception
   {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      this.document = documentBuilder.newDocument();      
   }

   public Element createServiceMetaDataElement(ServiceMetaData metaData) throws Exception
   {
      if(metaData == null)
         throw new IllegalArgumentException("Null meta data.");
      
      Element mbean = this.document.createElement("mbean");
      // ObjectName
      setObjectName(metaData, mbean);
      // Code
      setCode(metaData, mbean);
      // Mode
      setControllerMode(metaData, mbean);
      // Constructor
      addConstructor(metaData, mbean);
      // Interface 
      setInterface(metaData, mbean);
      // XMBean attribtues
      setXmbeanAttributes(metaData, mbean);
      
      // Service Attribtues
      List<ServiceAttributeMetaData> attributes = metaData.getAttributes();
      if(attributes != null && ! attributes.isEmpty())
      {
         for(ServiceAttributeMetaData attributeMetaData : attributes)
         {
            addAttributes(attributeMetaData, mbean);
         }
      }
      // Service dependencies
      List<ServiceDependencyMetaData> dependencies = metaData.getDependencies();
      if(dependencies != null && ! dependencies.isEmpty())
      {
         for(ServiceDependencyMetaData dependency : dependencies)
         {
            addDependency(dependency, mbean);
         }
      }
      // Aliases
      List<String> aliases = metaData.getAliases();
      if(aliases != null && ! aliases.isEmpty())
      {
         for(String alias : aliases)
         {
            addAlias(alias, mbean);
         }
      }
      // Annotations
      List<ServiceAnnotationMetaData> annotations = metaData.getAnnotations();
      if(annotations != null && ! annotations.isEmpty())
      {
         for(ServiceAnnotationMetaData annotation : annotations)
         {
            addAnnotation(annotation, mbean);
         }
      }
      return mbean;
   }
   
   private void setObjectName(ServiceMetaData metaData, Element mbean)
   {
      if(metaData.getObjectName() != null)
         mbean.setAttribute("name", metaData.getObjectName().getCanonicalName());
   }
   
   private void setCode(ServiceMetaData metaData, Element mbean)
   {
      if(metaData.getCode() != null)
         mbean.setAttribute("code", metaData.getCode());
   }
   
   private void setControllerMode(ServiceMetaData metaData, Element mbean)
   {
      ControllerMode mode = metaData.getMode();
      if(mode != null)
         mbean.setAttribute("mode", mode.getModeString());
   }
   
   private void addConstructor(ServiceMetaData metaData, Element mbean) throws Exception
   {
      ServiceConstructorMetaData constructorMetaData =  metaData.getConstructor();
      if(constructorMetaData == null)
         return;
      
      Element constructor = document.createElement("constructor");
      String[] params = constructorMetaData.getParams();
      if(params == null || params.length == 0)
         return;
     
      String[] signature = constructorMetaData.getSignature();
      if(signature.length != params.length)
         return; // this should actually not happen
      
      int i = params.length;
      for(int j = 0; j < i; j++)
      {
         Element arg = document.createElement("arg");
         arg.setAttribute("type", signature[j]);
         arg.setAttribute("value", params[j]);
         constructor.appendChild(arg);
      }
      mbean.appendChild(constructor);
   }
   
   private void setInterface(ServiceMetaData metaData, Element mbean)
   {
      if(metaData.getInterfaceName() != null)
         mbean.setAttribute("interface", metaData.getInterfaceName());
   }
   
   private void setXmbeanAttributes(ServiceMetaData metaData, Element mbean)
   {
      if(metaData.getXMBeanDD() != null)
         mbean.setAttribute("xmbean-dd", metaData.getXMBeanDD());
      
      if(metaData.getXMBeanCode() != null)
         mbean.setAttribute("xmbean-code", metaData.getXMBeanCode());
      
      if(metaData.getXMBeanDescriptor() != null)
         mbean.appendChild(metaData.getXMBeanDescriptor());
   }
   
   private void addAttributes(ServiceAttributeMetaData attributeMetaData, Element mbean)
   {
      Element attribute = document.createElement("attribute");
      
      attribute.setAttribute("name", attributeMetaData.getName());
      
      ServiceValueMetaData attributeValue = attributeMetaData.getValue();
      if(attributeValue instanceof ServiceElementValueMetaData)
      {
         ServiceElementValueMetaData value = (ServiceElementValueMetaData) attributeValue;
         attribute = value.getElement();
      }
      else if(attributeValue instanceof ServiceTextValueMetaData)
      {
         ServiceTextValueMetaData value = (ServiceTextValueMetaData) attributeValue;
         attribute.setTextContent(value.getText());
      }
      else if(attributeValue instanceof ServiceDependencyValueMetaData)
      {
         // Do not create a attribute
         attribute = null;
         ServiceDependencyValueMetaData dependsMetaData = (ServiceDependencyValueMetaData) attributeValue;
         // creates <depends/>
         addDependsValue(attributeMetaData.getName(), dependsMetaData, mbean);

      }
      else if (attributeValue instanceof ServiceDependencyListValueMetaData)
      {
         // Do not create an <attribute/>
         attribute = null;
         // Create <depends-list/>
         Element dependsList = document.createElement("depends-list");
         dependsList.setAttribute("optional-attribute-name", attributeMetaData.getName());
         
         ServiceDependencyListValueMetaData dependencyList = (ServiceDependencyListValueMetaData) attributeValue;
         // TODO is this enough ?
         List<String> dependencies = dependencyList.getDependencies();
         if(dependencies != null && ! dependencies.isEmpty())
         {
            for(String dependency : dependencies)
            {
               Element dependsElement = document.createElement("depends-list-element");
               dependsElement.setTextContent(dependency);
               dependsList.appendChild(dependsElement);
            }
            mbean.appendChild(dependsList);
         }
      }
      // <inject/>
      else if (attributeValue instanceof ServiceInjectionValueMetaData)
      {
         Element inject = document.createElement("inject");
         ServiceInjectionValueMetaData value = (ServiceInjectionValueMetaData) attributeValue;
         
         inject.setAttribute("bean", (String) value.getDependency());
         if(value.getProperty() != null)
            inject.setAttribute("property", value.getProperty());
         
         if(value.getDependentState() != null)
            inject.setAttribute("state", value.getDependentState().getStateString());
         
         attribute.appendChild(inject);
      }
      // <value-factory/>
      else if (attributeValue instanceof ServiceValueFactoryValueMetaData)
      {
         Element factory = document.createElement("value-factory");
         ServiceValueFactoryValueMetaData value = (ServiceValueFactoryValueMetaData) attributeValue;
         
         factory.setAttribute("bean", (String) value.getDependency());
         factory.setAttribute("method", value.getMethod());
         
         if(value.getDefaultValue() != null)
            factory.setAttribute("default", value.getDefaultValue().getText());
         
         if(value.getDependentState() != null)
            factory.setAttribute("state", value.getDependentState().getStateString());
         
         List<ServiceValueFactoryParameterMetaData> parameters = value.getParameterMetaData();
         if(parameters != null && ! parameters.isEmpty())
         {
            for(ServiceValueFactoryParameterMetaData param : parameters)
            {
               Element parameter = document.createElement("parameter");
               if(param.getParameterTypeName() != null)
                  parameter.setAttribute("class", param.getParameterTypeName());
               
               if(param.getValueTypeName() != null)
               {
                  Element parameterValue = document.createElement("value");
                  parameterValue.setAttribute("class", param.getValueTypeName());
                  parameterValue.setTextContent(param.getTextValue());
                  parameter.appendChild(parameterValue);
               }
               else
               {
                  parameter.setTextContent(param.getTextValue());
               }
               factory.appendChild(parameter);
            }
         }
         attribute.appendChild(factory);
      }
      
      if(attribute != null)
         mbean.appendChild(attribute);
   }
   
   private void addDependency(ServiceDependencyMetaData dependencyMetaData, Element mbean)
   {
      Element depends = document.createElement("depends");
      depends.setTextContent(dependencyMetaData.getIDependOn());
      mbean.appendChild(depends);
   }
   
   private void addAlias(String alias, Element mbean)
   {
      Element a = document.createElement("alias");
      a.setTextContent(alias);
      mbean.appendChild(a);
   }
   
   private void addAnnotation(ServiceAnnotationMetaData annotation, Element mbean)
   {
      Element a = document.createElement("annotation");
      a.setTextContent(annotation.getAnnotation());
      mbean.appendChild(a);
   }
   
   private void addDependsValue(String name, ServiceDependencyValueMetaData dependsMetaData, Element mbean)
   {
      Element depends = document.createElement("depends");
      depends.setAttribute("optional-attribute-name", name);
      if(dependsMetaData.getProxyType() != null)
         depends.setAttribute("proxy-type", dependsMetaData.getProxyType());
      
      depends.setTextContent(dependsMetaData.getDependency());
      
      mbean.appendChild(depends);
   }

}

