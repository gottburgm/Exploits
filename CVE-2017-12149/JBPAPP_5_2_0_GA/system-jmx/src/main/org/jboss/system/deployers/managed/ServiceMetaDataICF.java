/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.system.deployers.managed;

import java.beans.PropertyEditor;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.beans.info.spi.BeanInfo;
import org.jboss.beans.info.spi.PropertyInfo;
import org.jboss.common.beans.property.BeanUtils;
import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.spi.factory.InstanceClassFactory;
import org.jboss.metadata.spi.MetaData;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.MetaValueFactory;
import org.jboss.metatype.spi.values.MetaMapper;
import org.jboss.system.ServiceConfigurator;
import org.jboss.system.ServiceController;
import org.jboss.system.metadata.ServiceAnnotationMetaData;
import org.jboss.system.metadata.ServiceAttributeMetaData;
import org.jboss.system.metadata.ServiceDependencyValueMetaData;
import org.jboss.system.metadata.ServiceElementValueMetaData;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.metadata.ServiceTextValueMetaData;
import org.jboss.system.metadata.ServiceValueContext;
import org.jboss.system.metadata.ServiceValueMetaData;
import org.w3c.dom.Element;

/**
 * The InstanceClassFactory implementation for ServiceMetaData.
 * 
 * @author Scott.Stark@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 113110 $
 */
public class ServiceMetaDataICF implements InstanceClassFactory<ServiceMetaData>
{
   private static final Logger log = Logger.getLogger(ServiceMetaDataICF.class);

   private static final String MOCLASS_ANNOTATION = '@' + ManagementObjectClass.class.getName();
   //private static final ThreadLocal<> attributeMap;

   private MBeanServer mbeanServer;
   private ServiceController controller;

   /** The meta value factory */
   private MetaValueFactory metaValueFactory = MetaValueFactory.getInstance(); 

   public MBeanServer getMbeanServer()
   {
      return mbeanServer;
   }

   public void setMbeanServer(MBeanServer mbeanServer)
   {
      this.mbeanServer = mbeanServer;
   }

   public ServiceController getController()
   {
      return controller;
   }
   public void setController(ServiceController controller)
   {
      this.controller = controller;
   }

   public Class<ServiceMetaData> getType()
   {
      return ServiceMetaData.class;
   }

   public Class<? extends Serializable> getManagedObjectClass(ServiceMetaData md)
      throws ClassNotFoundException
   {
      ClassLoader prevLoader = SecurityActions.getContextClassLoader();
      try
      {
         ClassLoader loader = getServiceMetaDataCL(md);
         Class moClass = loader.loadClass(md.getCode());
         // Set the mbean class loader as the TCL
         SecurityActions.setContextClassLoader(loader);

         // Looks for a ManagementObjectClass annotation that defines
         // an alternate class to scan for management annotations
         List<ServiceAnnotationMetaData> samlist = md.getAnnotations();
         for (ServiceAnnotationMetaData sam : samlist)
         {
            // Annotations are not yet introduced to the actual mbean
            // so just look for the annotation string
            String anString = sam.getAnnotation();
            if (anString.startsWith(MOCLASS_ANNOTATION))
            {
               Class<?> originalClass = moClass;
               ManagementObjectClass moc = (ManagementObjectClass)sam.getAnnotationInstance(loader);
               moClass = moc.code();
               log.debug("Using alternate class '" + moClass + "' for class " + originalClass);
               break;
            }  
         }
         return moClass;
      }
      catch(InstanceNotFoundException e)
      {
         throw new ClassNotFoundException("Failed to obtain mbean class loader", e);
      }
      finally
      {
         SecurityActions.setContextClassLoader(prevLoader);
      }
   }

   public MetaValue getValue(BeanInfo beanInfo, ManagedProperty property,
         MetaData metaData,
         ServiceMetaData md)
   {
      // First look to the mapped name
      String name = property.getMappedName();
      if (name == null)
         name = property.getName();

      ClassLoader prevLoader = SecurityActions.getContextClassLoader();
      Object value = null;
      MetaType metaType = property.getMetaType();
      MetaValue mvalue = null;
      ObjectName mbean = md.getObjectName();
      String attrName = null;

      try
      {
         ClassLoader loader = getServiceMetaDataCL(md);
         // Set the mbean class loader as the TCL
         SecurityActions.setContextClassLoader(loader);

         // Get the attribute value from the metadata
         for (ServiceAttributeMetaData amd : md.getAttributes())
         {
            // The compare is case-insensitve due to the attribute/javabean case mismatch
            if (amd.getName().equalsIgnoreCase(name))
            {
               value = amd.getValue();
               attrName = amd.getName();
               break;
            }
         }
         // If the value is null, look to mbean for the value
         if (value == null && getMbeanServer() != null)
         {
            try
            {
               value = getMbeanServer().getAttribute(mbean, name);
            }
            catch (AttributeNotFoundException e)
            {
               // Try the alternate name
               String attribute = name;
               if(Character.isUpperCase(name.charAt(0)))
                  attribute = Character.toLowerCase(name.charAt(0)) + name.substring(1);
               else
                  attribute = Character.toUpperCase(name.charAt(0)) + name.substring(1);
               try
               {
                  value = getMbeanServer().getAttribute(mbean, attribute);
               }
               catch(Exception e2)
               {
                  log.debug("Failed to get value from mbean for: "+attribute, e2);
               }               
            }
            catch(Exception e)
            {
               log.debug("Failed to get value from mbean for: "+name, e);
            }
         }
   
         // Unwrap the ServiceValueMetaData types
         try
         {
            if (value instanceof ServiceTextValueMetaData)
            {
               ServiceTextValueMetaData text = (ServiceTextValueMetaData) value;
               try
               {
                  // TODO: cache this somehow
                  HashMap<String, MBeanAttributeInfo> attrs = ServiceConfigurator.getAttributeMap(mbeanServer, mbean);
                  MBeanAttributeInfo mbi = attrs.get(attrName);
                  ServiceValueContext svc = new ServiceValueContext(mbeanServer, controller, mbi, loader);
                  value = text.getValue(svc);
               }
               catch(Exception e)
               {
                  // TODO: better way to determine if the bean was installed, as this does not make much sense
                  PropertyEditor editor = PropertyEditorFinder.getInstance().find(BeanUtils.findClass(metaType.getTypeName()));
                  editor.setAsText(text.getText());
                  value = editor.getValue();
               }
            }
            else if (value instanceof ServiceDependencyValueMetaData)
            {
               ServiceDependencyValueMetaData depends = (ServiceDependencyValueMetaData) value;
               value = depends.getObjectName();
            }
            else if (value instanceof ServiceElementValueMetaData)
            {
               value = ((ServiceElementValueMetaData)value).getElement();
            }
            // TODO: unwrap other ServiceValueMetaData types
         }
         catch(Exception e)
         {
            log.debug("Failed to get value from mbean for: "+name, e);
         }
   
         PropertyInfo propertyInfo = beanInfo.getProperty(name);
         MetaMapper metaMapper = property.getTransientAttachment(MetaMapper.class);
         try
         {
            if(metaMapper != null)
            {
               mvalue = metaMapper.createMetaValue(property.getMetaType(), value);
            }
            else
            {
               mvalue = metaValueFactory.create(value, propertyInfo.getType());
            }
         }
         catch(Exception e)
         {
            log.debug("Failed to get property value for bean: "+beanInfo.getName()
                  +", property: "+propertyInfo.getName(), e);
            mvalue = metaValueFactory.create(null, propertyInfo.getType());
            return mvalue;
         }
      }
      catch(InstanceNotFoundException e)
      {
         throw new IllegalStateException("Failed to obtain mbean class loader", e);
      }
      finally
      {
         SecurityActions.setContextClassLoader(prevLoader);
      }
      return mvalue;
   }

   public void setValue(BeanInfo beanInfo, ManagedProperty property, ServiceMetaData md, MetaValue value)
   {
      ClassLoader prevLoader = SecurityActions.getContextClassLoader();
      try
      {
         ClassLoader loader = getServiceMetaDataCL(md);
         // Set the mbean class loader as the TCL
         SecurityActions.setContextClassLoader(loader);

         // First look to the mapped name
         String name = property.getMappedName();
         if (name == null)
            name = property.getName();
   
         // Get the attribute value
         ServiceValueMetaData attributeValue = null;
         for (ServiceAttributeMetaData amd : md.getAttributes())
         {
            // The compare is case-insensitive due to the attribute/javabean case mismatch
            if (amd.getName().equalsIgnoreCase(name))
            {
               attributeValue = amd.getValue();
               break;
            }
         }
         // There may not be an attribute value, see if there is a matching property
         
         // Unwrap the value before, so that we can recreate empty values
         Object plainValue = null;
         // Look for a MetaMapper
         MetaType propertyType = property.getMetaType();
         MetaMapper metaMapper = property.getTransientAttachment(MetaMapper.class);
         Type mappedType = null; 
         if(metaMapper != null)
         {
            mappedType = metaMapper.mapToType();
            plainValue = metaMapper.unwrapMetaValue(value);
         }
         else
         {
            PropertyInfo propertyInfo = beanInfo.getProperty(name);
            plainValue = metaValueFactory.unwrap(value, propertyInfo.getType());
         }

         if (attributeValue == null)
         {
            String aname = mapAttributeName(md, name);
            if(aname != null)
            {
               ServiceAttributeMetaData attr = new ServiceAttributeMetaData();
               attr.setName(aname);
               // Check if this is mapped to a Element
               if(mappedType != null && mappedType.equals(Element.class))
               {
                  attributeValue = new ServiceElementValueMetaData();
               }
               else if(plainValue != null)
               {                 
                  // Create a text value
                  String textValue = String.valueOf(plainValue);
                  // Don't create a empty value
                  if(textValue.trim().length() > 0 )
                     attributeValue = new ServiceTextValueMetaData(textValue);
               }
               // Don't create a null serviceAttribute
               if(attributeValue == null)
                  return;
               
               // Add
               attr.setValue(attributeValue);
               md.addAttribute(attr);
            }
         }
         if (attributeValue != null)
         {
            // Unwrap the ServiceValueMetaData types
            if (attributeValue instanceof ServiceTextValueMetaData)
            {
               String textValue = plainValue != null ? String.valueOf(plainValue) : null; 
               ServiceTextValueMetaData text = (ServiceTextValueMetaData) attributeValue;
               text.setText(textValue);
            }
            else if (attributeValue instanceof ServiceElementValueMetaData)
            {
               if(plainValue != null)
                  ((ServiceElementValueMetaData) attributeValue).setElement((Element) plainValue);
            }
            else if (attributeValue instanceof ServiceDependencyValueMetaData)
            {
               ServiceDependencyValueMetaData depends = (ServiceDependencyValueMetaData) attributeValue;
               if (plainValue instanceof ObjectName)
                  depends.setObjectName((ObjectName) plainValue);
               else
                  depends.setDependency(String.valueOf(plainValue));
            }
            // TODO: unwrap other ServiceValueMetaData types
            else
            {
               throw new IllegalArgumentException("Unhandled attribute value type: " + name + "/" + md+", class="+attributeValue.getClass());               
            }
         }
         else
            throw new IllegalArgumentException("No matching attribute found: " + name + "/" + md);
      }
      catch(InstanceNotFoundException e)
      {
         throw new IllegalStateException("Failed to obtain mbean class loader", e);
      }
      finally
      {
         SecurityActions.setContextClassLoader(prevLoader);
      }
   }

   /**
    * The service context uses the canonical object name string
    * @return the service metadata canonical object name string
    */
   public Object getComponentName(BeanInfo beanInfo, ManagedProperty property, ServiceMetaData md, MetaValue value)
   {
      ObjectName objectName = md.getObjectName();
      String canonicalName = objectName.getCanonicalName();
      return canonicalName;
   }

   /**
    * Obtains the ServiceMetaData class loader from the
    * getClassLoaderName value if there is an mbeanServer.
    * 
    * @param md - the mbean metadata
    * @return the ServiceMetaData.ClassLoaderName class loader if
    *    the mbeanServer has been set, the current TCL otherwise.
    * @throws InstanceNotFoundException if no mbean class loader can be
    *    found by the ServiceMetaData.ClassLoaderName
    */
   private ClassLoader getServiceMetaDataCL(ServiceMetaData md)
      throws InstanceNotFoundException
   {
      ClassLoader loader = null;
      if(mbeanServer != null)
         loader = mbeanServer.getClassLoader(md.getClassLoaderName());
      // Fallback to TCL if there is no mbeanServer
      if(loader == null)
         loader = Thread.currentThread().getContextClassLoader();
      return loader;
   }

   /**
    * Try to find a matching mbean attribute
    * @param name
    * @return
    */
   private String mapAttributeName(ServiceMetaData md, String name)
   {
      ObjectName mbean = md.getObjectName();
      String attrName = null;
      try
      {
         mbeanServer.getAttribute(mbean, name);
         attrName = name;
      }
      catch(Exception e)
      {
         char c = name.charAt(0);
         if(Character.isLowerCase(c))
            name = Character.toUpperCase(c) + name.substring(1);
         else
            name = Character.toLowerCase(c) + name.substring(1);
         try
         {
            mbeanServer.getAttribute(mbean, name);
            attrName = name;
         }
         catch(Exception e2)
         {
         }
      }
      // FIXME 
      if(attrName == null) 
      {
         char c = name.charAt(0);
         name = Character.toUpperCase(c) + name.substring(1);
         return name;
      }
      return attrName;
   }
}
