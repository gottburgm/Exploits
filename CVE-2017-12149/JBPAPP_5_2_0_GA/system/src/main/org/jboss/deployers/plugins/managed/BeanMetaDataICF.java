/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployers.plugins.managed;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.jboss.beans.info.spi.BeanInfo;
import org.jboss.beans.info.spi.PropertyInfo;
import org.jboss.beans.metadata.spi.AnnotationMetaData;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.PropertyMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.kernel.plugins.config.Configurator;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.logging.Logger;
import org.jboss.managed.api.Fields;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementObjectClass;
import org.jboss.managed.spi.factory.InstanceClassFactory;
import org.jboss.metadata.spi.MetaData;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.MetaValueFactory;
import org.jboss.metatype.spi.values.MetaMapper;
import org.jboss.reflect.spi.TypeInfo;

/**
 * An InstanceClassFactory for BeanMetaData
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 89033 $
 */
public class BeanMetaDataICF
   implements InstanceClassFactory<BeanMetaData>
{
   private static final Logger log = Logger.getLogger(BeanMetaDataICF.class);
   private KernelController controller;
   /** The meta value factory */
   private MetaValueFactory metaValueFactory = MetaValueFactory.getInstance(); 
   /** */
   private InstanceClassFactory delegateICF;

   public KernelController getController()
   {
      return controller;
   }
   public void setController(KernelController controller)
   {
      this.controller = controller;
   }

   public MetaValueFactory getMetaValueFactory()
   {
      return metaValueFactory;
   }
   public void setMetaValueFactory(MetaValueFactory metaValueFactory)
   {
      this.metaValueFactory = metaValueFactory;
   }

   public InstanceClassFactory getDelegateICF()
   {
      return delegateICF;
   }
   public void setDelegateICF(InstanceClassFactory delegateICF)
   {
      this.delegateICF = delegateICF;
   }

   public Object locateBean(String beanName)
   {
      ControllerContext context = getController().getInstalledContext(beanName);
      if (context == null)
      {
         return null;
      }
      return context.getTarget();
   }

   public Class<BeanMetaData> getType()
   {
      return BeanMetaData.class;
   }

   public Object getComponentName(BeanInfo beanInfo, ManagedProperty property,
         BeanMetaData attachment, MetaValue value)
   {
      return attachment.getName();
   }

   public Class<?> getManagedObjectClass(BeanMetaData attachment)
      throws ClassNotFoundException
   {
      Class<?> mocClass = null;

      // Look for a ManagementObjectClass annotation
      Set<AnnotationMetaData> annotations = attachment.getAnnotations();
      if(annotations != null)
      {
         for(AnnotationMetaData amd : annotations)
         {
            Annotation ann = amd.getAnnotationInstance();
            if(ann instanceof ManagementObjectClass)
            {
               ManagementObjectClass moc = (ManagementObjectClass) ann;
               mocClass = moc.code();
               log.debug("Saw ManagementObjectClass, "+mocClass+" for bean: "+attachment);
               break;
            }
         }
      }
      // Find <annotation/> in the beans.xml 
//      if(annotations != null)
//      {
//         for(AnnotationMetaData amd : annotations)
//         {
//            Annotation ann = amd.getAnnotationInstance();
//            if(ann instanceof ManagementObject)
//            {
//               String beanClassName = attachment.getBean();
//               ClassLoader loader = getClassLoader(attachment);
//               mocClass = loader.loadClass(beanClassName); 
//               log.debug("Saw ManagementObject, "+mocClass+" for bean: "+attachment);
//               break;               
//            }
//         }
//      }
      
      // Use the bean from the metadata
      if(mocClass == null)
      {
         String beanClassName = attachment.getBean();
         if(beanClassName != null && beanClassName.length() > 0)
         {
            // TODO: TCL may not be correct
            ClassLoader loader = getClassLoader(attachment);
            mocClass = loader.loadClass(beanClassName);
            // Make sure it has an ManagementObject annotation
            ManagementObject moAnn = mocClass.getAnnotation(ManagementObject.class);
            if(moAnn == null)
            {
               // Revert back to the BeanMetaData class
               mocClass = attachment.getClass();
            }
            log.debug("Using bean class:, "+mocClass+" for bean: "+attachment);
         }
      }
      return mocClass;
   }

   public MetaValue getValue(BeanInfo beanInfo, ManagedProperty property,
         MetaData metaData,
         BeanMetaData attachment)
   {
      // Get the property from the bean
      // First look to the mapped name
      String name = property.getMappedName();
      if (name == null)
         name = property.getName();
      PropertyInfo propertyInfo = property.getField(Fields.PROPERTY_INFO, PropertyInfo.class);
      if(propertyInfo == null)
         propertyInfo = beanInfo.getProperty(name);
      
      Object bean = locateBean(attachment.getName());
      MetaValue mvalue = null;
      if(propertyInfo.isReadable() == false)
      {
         if(log.isTraceEnabled())
            log.trace("Skipping get of non-readable property: "+propertyInfo);
         return null;
      }

      try
      {
         String getterClassName = propertyInfo.getGetter().getDeclaringClass().getName();
         if(getterClassName.equals(attachment.getClass().getName()))
         {
            // use attachment
            mvalue = delegateICF.getValue(beanInfo, property, metaData, attachment);
         }
         else if(bean != null)
         {
            // use bean (if installed)
            mvalue = delegateICF.getValue(beanInfo, property, metaData, bean);
         }
         else
         {
            // Try to find the property in the meta data
            PropertyMetaData md = null;
            if(attachment.getProperties() != null && attachment.getProperties().isEmpty() == false)
            {
               for(PropertyMetaData bp : attachment.getProperties())
               {
                  if(name.equals(bp.getName()))
                  {
                     md = bp;
                     break;
                  }
               }
               if(md != null)
               {
                  // TODO add metaMapping
                  if(md.getValue() != null)
                  {
                     mvalue = metaValueFactory.create(md.getValue().getUnderlyingValue(),
                           propertyInfo.getType());
                  }
               }
            }
         }
      }
      catch(Throwable e)
      {
         log.debug("Failed to get property value for bean: "+beanInfo.getName()
               +", property: "+propertyInfo.getName(), e);
         mvalue = metaValueFactory.create(null, propertyInfo.getType());
         return mvalue;
      }

      return mvalue;
   }

   public void setValue(BeanInfo beanInfo, ManagedProperty property,
         BeanMetaData attachment, MetaValue value)
   {
      ClassLoader prevLoader = SecurityActions.getContextClassLoader();
      String beanName = attachment.getName();
      // First look to the mapped name
      String name = property.getMappedName();
      if (name == null)
         name = property.getName();
      try
      {
         ClassLoader loader = getClassLoader(attachment);
         // Set the mbean class loader as the TCL
         SecurityActions.setContextClassLoader(loader);

         PropertyInfo propertyInfo = property.getField(Fields.PROPERTY_INFO, PropertyInfo.class);
         if(propertyInfo == null)
            propertyInfo = beanInfo.getProperty(name);
         if(propertyInfo == null)
            throw new IllegalArgumentException("No matching property found: " + name + "/" + beanName);
         
         if(propertyInfo.isWritable() == false)
         {
            if(log.isTraceEnabled())
               log.trace("Skipping get of non-writable property: "+propertyInfo);
            return;
         }
         Object plainValue = unwrapValue(property, propertyInfo.getType(), value);
         Object bean = locateBean(beanName);
         
         // Only update the bean if installed
         if(bean != null)
            propertyInfo.set(bean, plainValue);
         
         BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(attachment);
         builder.addPropertyMetaData(name, plainValue);
         
      }
      catch(Throwable e)
      {
         throw new IllegalStateException("Failed to set property value: "+name + "/" + beanName, e);
      }
      finally
      {
         SecurityActions.setContextClassLoader(prevLoader);
      }
   }

   protected ClassLoader getClassLoader(BeanMetaData bmd)
   {
      ClassLoader loader = null;
      try
      {
         loader = Configurator.getClassLoader(bmd);
      }
      catch(Throwable t)
      {
         log.debug("Failed to load BeanMetaData class loader", t);
      }
      // Fallback to TCL if there is no
      if(loader == null)
         loader = SecurityActions.getContextClassLoader();
      return loader;
   }

   private Object unwrapValue(ManagedProperty property, TypeInfo typeInfo, MetaValue value)
   {
      // Look for a property MetaMapper
      MetaMapper<?> metaMapper = property.getTransientAttachment(MetaMapper.class);
      if(metaMapper != null)
      {
         return metaMapper.unwrapMetaValue(value);
      }
      return metaValueFactory.unwrap(value, typeInfo);
   }
   
}
