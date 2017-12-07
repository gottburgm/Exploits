/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.profileservice.management;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.Descriptor;
import javax.management.DescriptorAccess;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.managed.api.Fields;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedParameter;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.ManagedOperation.Impact;
import org.jboss.managed.api.annotation.ActivationPolicy;
import org.jboss.managed.api.annotation.DefaultValueBuilderFactory;
import org.jboss.managed.api.annotation.FieldsFactory;
import org.jboss.managed.api.annotation.ManagementConstants;
import org.jboss.managed.api.annotation.ManagementObjectID;
import org.jboss.managed.api.annotation.ManagementObjectRef;
import org.jboss.managed.api.annotation.ManagementOperation;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ManagementPropertyFactory;
import org.jboss.managed.api.annotation.ManagementRuntimeRef;
import org.jboss.managed.api.annotation.Masked;
import org.jboss.managed.api.annotation.RunStateProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.managed.plugins.DefaultFieldsImpl;
import org.jboss.managed.plugins.ManagedObjectImpl;
import org.jboss.managed.plugins.ManagedOperationImpl;
import org.jboss.managed.plugins.ManagedParameterImpl;
import org.jboss.managed.plugins.ManagedPropertyImpl;
import org.jboss.managed.plugins.factory.AbstractManagedObjectFactory;
import org.jboss.metadata.spi.MetaData;
import org.jboss.metatype.api.annotations.MetaMapping;
import org.jboss.metatype.api.annotations.MetaMappingFactory;
import org.jboss.metatype.api.types.ArrayMetaType;
import org.jboss.metatype.api.types.CollectionMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.MetaTypeFactory;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.spi.values.DefaultValueBuilder;
import org.jboss.metatype.spi.values.MetaMapper;
import org.jboss.metatype.spi.values.MetaMapperFactory;

/**
 * A type of ManagedObject factory that generates a ManagedObject from an MBean
 * MBeanInfo.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision:$
 */
public class MBeanManagedObjectFactory
{
   private static Logger log = Logger.getLogger(MBeanManagedObjectFactory.class);
   /** The meta type factory */
   private MetaTypeFactory metaTypeFactory = MetaTypeFactory.getInstance();

   public MetaTypeFactory getMetaTypeFactory()
   {
      return metaTypeFactory;
   }
   public void setMetaTypeFactory(MetaTypeFactory metaTypeFactory)
   {
      this.metaTypeFactory = metaTypeFactory;
   }

   /**
    * Builds a ManagedObject from the MBeanInfo.
    * TODO: none of the org.jboss.managed.api.annotation.* annotations are
    * taken from the MBeanInfo. The descriptor feature could be used for this.
    *
    * @param mbean
    * @param info
    * @param mbeanLoader
    * @param metaData
    * @return
    * @throws Exception
    */

   public ManagedObject getManagedObject(ObjectName mbean, MBeanInfo info,
         ClassLoader mbeanLoader, MetaData metaData)
      throws Exception
   {
      return getManagedObject(mbean, info, mbeanLoader, metaData, null, null);
   }

   // FIXME - Hack until metadata mechanism is provided
   public ManagedObject getManagedObject(ObjectName mbean, MBeanInfo info,
         ClassLoader mbeanLoader, MetaData metaData, ViewUse[] defaultViewUse, Map<String, String> propertyMetaMappings)
      throws Exception
   {
      boolean trace = log.isTraceEnabled();

      // Process the ManagementObject fields
      boolean isRuntime = false;
      String name = mbean.getCanonicalName();
      String nameType = null;
      String attachmentName = null;
      Class<? extends Fields> moFieldsFactory = null;
      Class<? extends ManagedProperty> moPropertyFactory = null;

      // Build the ManagedProperties
      Set<ManagedProperty> properties = new HashSet<ManagedProperty>();

      MBeanAttributeInfo[] attributes = info.getAttributes();
      for(MBeanAttributeInfo propertyInfo : attributes)
      {

            ManagementProperty managementProperty = getAnnotation(ManagementProperty.class, propertyInfo, metaData);
            ManagementObjectID id = getAnnotation(ManagementObjectID.class, propertyInfo, metaData);
            ManagementObjectRef ref = getAnnotation(ManagementObjectRef.class, propertyInfo, metaData);
            ManagementRuntimeRef runtimeRef = getAnnotation(ManagementRuntimeRef.class, propertyInfo, metaData);
            RunStateProperty rsp = getAnnotation(RunStateProperty.class, propertyInfo, metaData);
            Masked masked = getAnnotation(Masked.class, propertyInfo, metaData);
            DefaultValueBuilderFactory defaultsFactory = getAnnotation(DefaultValueBuilderFactory.class, propertyInfo, metaData);
            HashMap<String, Annotation> propAnnotations = new HashMap<String, Annotation>();
            if (managementProperty != null)
               propAnnotations.put(ManagementProperty.class.getName(), managementProperty);
            if (id != null)
            {
               propAnnotations.put(ManagementObjectID.class.getName(), id);
               // This overrides the MO nameType
               nameType = id.type();
            }
            if (ref != null)
               propAnnotations.put(ManagementObjectRef.class.getName(), ref);
            if (runtimeRef != null)
               propAnnotations.put(ManagementRuntimeRef.class.getName(), runtimeRef);
            if (rsp != null)
               propAnnotations.put(RunStateProperty.class.getName(), rsp);
            if (masked != null)
               propAnnotations.put(Masked.class.getName(), masked);

            // Check whether this property should be included
            boolean includeProperty = propertyInfo.isReadable() | propertyInfo.isWritable();

            if (includeProperty)
            {
               Fields fields = null;
               Class<? extends Fields> factory = moFieldsFactory;
               FieldsFactory ff = getAnnotation(FieldsFactory.class, propertyInfo, metaData);
               if(ff != null)
                  factory = ff.value();
               if (factory != null)
               {
                  try
                  {
                     fields = factory.newInstance();
                  }
                  catch (Exception e)
                  {
                     log.debug("Failed to created Fields", e);
                  }
               }
               if (fields == null)
                  fields = new DefaultFieldsImpl();

               if( propertyInfo instanceof Serializable )
               {
                  Serializable pinfo = Serializable.class.cast(propertyInfo);
                  fields.setField(Fields.PROPERTY_INFO, pinfo);
               }

               String propertyName = propertyInfo.getName();
               if (managementProperty != null)
                  propertyName = managementProperty.name();
               if( propertyName.length() == 0 )
                  propertyName = propertyInfo.getName();
               fields.setField(Fields.NAME, propertyName);

               // This should probably always the the propertyInfo name?
               String mappedName = propertyInfo.getName();
               if (managementProperty != null)
                  mappedName = managementProperty.mappedName();
               if( mappedName.length() == 0 )
                  mappedName = propertyInfo.getName();
               fields.setField(Fields.MAPPED_NAME, mappedName);

               String description = ManagementConstants.GENERATED;
               if (managementProperty != null)
                  description = managementProperty.description();
               if (description.equals(ManagementConstants.GENERATED))
                  description = propertyName;
               fields.setField(Fields.DESCRIPTION, description);

               if (trace)
               {
                  log.trace("Building MangedProperty(name="+propertyName
                        +",mappedName="+mappedName
                        +") ,annotations="+propAnnotations);
               }

               boolean mandatory = false;
               if (managementProperty != null)
                  mandatory = managementProperty.mandatory();
               if (mandatory)
                  fields.setField(Fields.MANDATORY, Boolean.TRUE);

               boolean readOnly = propertyInfo.isWritable() == false;
               if (readOnly == false && managementProperty != null)
                  readOnly = managementProperty.readOnly();
               if (readOnly)
                  fields.setField(Fields.READ_ONLY, Boolean.TRUE);

               boolean managed = false;
               if (managementProperty != null)
                  managed = managementProperty.managed();
               // View Use
               if (managementProperty != null)
               {
                  ViewUse[] use = managementProperty.use();
                  fields.setField(Fields.VIEW_USE, use);
               }
               else if (defaultViewUse != null)
               {
                  fields.setField(Fields.VIEW_USE, defaultViewUse);
               }
               // ActivationPolicy
               ActivationPolicy apolicy = ActivationPolicy.IMMEDIATE;
               if (managementProperty != null)
               {
                  apolicy = managementProperty.activationPolicy();
               }
               fields.setField(Fields.ACTIVATION_POLICY, apolicy);
               // The managed property type
               MetaMapper[] mapperReturn = {null};
               String propertyType = propertyInfo.getType();
               MetaType metaType = null;
               Class<?> type = null;
               try
               {
                  type = loadTypeClass(propertyType, mbeanLoader);
                  metaType = this.getMetaType(propertyInfo, type, metaData, false, propertyMetaMappings, mapperReturn);
               }
               catch(Exception e)
               {
                  log.debug("Failed to create ManagedProperty on failure to load type:"+propertyType+", for property: "+propertyInfo.getName());
                  continue;
               }

               // Determine meta type based on property type
               if(metaType == null)
               {
                  if (managed)
                  {
                     if(type.isArray())
                        metaType = new ArrayMetaType(1, AbstractManagedObjectFactory.MANAGED_OBJECT_META_TYPE);
                     else if (Collection.class.isAssignableFrom(type))
                        metaType = new CollectionMetaType(type.getName(), AbstractManagedObjectFactory.MANAGED_OBJECT_META_TYPE);
                     else
                        metaType = AbstractManagedObjectFactory.MANAGED_OBJECT_META_TYPE;
                  }
                  else
                  {
                     metaType = metaTypeFactory.resolve(type);
                  }
               }
               fields.setField(Fields.META_TYPE, metaType);

               // Default value
               if(managementProperty != null)
               {
                  String defaultValue = managementProperty.defaultValue();
                  if(defaultValue.length() > 0)
                  {
                     try
                     {
                       // Check for a DefaultValueBuilderFactory
                        DefaultValueBuilder builder = null;
                        if(defaultsFactory != null)
                        {
                              Class<? extends DefaultValueBuilder> factoryClass = defaultsFactory.value();
                              builder = factoryClass.newInstance();
                        }
                        if(builder != null)
                        {
                           MetaValue defaultMV = builder.buildMetaValue(defaultValue);
                           if(defaultMV != null)
                              fields.setField(Fields.DEFAULT_VALUE, defaultMV);
                        }
                        else
                        {
                           log.warn("Failed to find DefaultValueBuilder for type: "+metaType);
                        }
                     }
                     catch(Exception e)
                     {
                        log.warn("Failed to create default value for: "+propertyInfo, e);
                     }
                  }
               }

               // Property annotations
               if (propAnnotations.isEmpty() == false)
                  fields.setField(Fields.ANNOTATIONS, propAnnotations);

               ManagedProperty property = null;
               Class<? extends ManagedProperty> mpClass = moPropertyFactory;
               ManagementPropertyFactory mpf = getAnnotation(ManagementPropertyFactory.class, propertyInfo, metaData);
               if (mpf != null)
                  mpClass = mpf.value();
               if (mpClass != null)
                  property = AbstractManagedObjectFactory.createManagedProperty(mpClass, fields);
               if (property == null)
                  property = new ManagedPropertyImpl(fields);
               // Pass the MetaMapper as an attachment
               if (mapperReturn[0] != null)
                  property.setTransientAttachment(MetaMapper.class.getName(), mapperReturn[0]);
               properties.add(property);
            }
            else if (trace)
               log.trace("Ignoring property: " + propertyInfo);
      }

      /* TODO: Operations. In general the bean metadata does not contain
         operation information.
      */
      Set<ManagedOperation> operations = new HashSet<ManagedOperation>();

      MBeanOperationInfo[] methodInfos = info.getOperations();
      if (methodInfos != null && methodInfos.length > 0)
      {
         for (MBeanOperationInfo methodInfo : methodInfos)
         {
            ManagementOperation managementOp = getAnnotation(ManagementOperation.class, methodInfo, metaData);
            try
            {
               ManagedOperation op = getManagedOperation(methodInfo, managementOp, mbeanLoader, metaData);
               operations.add(op);
            }
            catch(Exception e)
            {
               log.debug("Failed to create ManagedOperation for: "+methodInfo.getName(), e);
            }
         }
      }

      ManagedObjectImpl result = new ManagedObjectImpl(mbean.getCanonicalName(), properties);
      // TODO
      Map<String, Annotation> empty = Collections.emptyMap();
      result.setAnnotations(empty);
      // Set the component name to name if this is a runtime MO with a name specified
      result.setComponentName(name);
      if (nameType != null)
         result.setNameType(nameType);
      if (attachmentName != null)
         result.setAttachmentName(attachmentName);
      if (operations.size() > 0 )
         result.setOperations(operations);
      for (ManagedProperty property : properties)
         property.setManagedObject(result);
      result.setTransientAttachment(MBeanInfo.class.getName(), info);

      // Marker for associating the correct dispatcher
      result.setTransientAttachment(MBeanRuntimeComponentDispatcher.class.getName(), true);
      return result;
   }

   protected <X extends Annotation> X getAnnotation(Class<X> annotationType,
         MBeanFeatureInfo info, MetaData metaData)
   {
      X annotation = null;
      if(metaData != null)
      {
         annotation = metaData.getAnnotation(annotationType);
         if(annotation == null && info instanceof DescriptorAccess)
         {
            DescriptorAccess daccess = (DescriptorAccess) info;
            Descriptor descriptor = daccess.getDescriptor();
            annotation = getAnnotation(annotationType, descriptor);
         }
      }
      return annotation;
   }
   protected <X extends Annotation> X getAnnotation(Class<X> annotationType,
         Descriptor descriptor)
   {
      // TODO...
      return null;
   }

   /**
    * Get the MetaType for info by looking for MetaMapping/MetaMappingFactory
    * annotations in addition to the info type.
    *
    * @param methodInfo
    * @param metaData
    * @return the MetaType for info's type
    */
   protected MetaType getMetaType(MBeanFeatureInfo info, Type infoType, MetaData metaData,
         boolean useTypeFactory, Map<String, String> propertyMetaMappings, MetaMapper[] mapperReturn)
   {
      MetaType returnType = null;
      // First look for meta mappings
      MetaMapper<?> metaMapper = null;
      MetaMapping metaMapping = getAnnotation(MetaMapping.class, info, metaData);
      MetaMappingFactory metaMappingFactory = getAnnotation(MetaMappingFactory.class, info, metaData);
      if(metaMappingFactory != null)
      {
         Class<? extends MetaMapperFactory<?>> mmfClass = metaMappingFactory.value();
         try
         {
            MetaMapperFactory<?> mmf = mmfClass.newInstance();
            String[] args = metaMappingFactory.args();
            if(args.length > 0)
               metaMapper = mmf.newInstance(args);
            else
               metaMapper = mmf.newInstance();
         }
         catch(Exception e)
         {
            log.debug("Failed to create MetaMapperFactory: "+metaMappingFactory, e);
         }
      }
      if(metaMapping != null)
      {
         // Use the mapping for the type
         Class<? extends MetaMapper<?>> mapperClass = metaMapping.value();
         try
         {
            metaMapper = mapperClass.newInstance();
         }
         catch(Exception e)
         {
            log.debug("Failed to create MetaMapper: "+metaMapping, e);
         }
      }

      if (info instanceof MBeanAttributeInfo && propertyMetaMappings != null)
      {
         String className = propertyMetaMappings.get(info.getName());
         if (className != null)
         {
            try
            {
               // Use the same loader of the profile service
               metaMapper = (MetaMapper<?>)Class.forName(className).newInstance();
            }
            catch (Exception e)
            {
               log.debug("Failed to create MetaMapper: " + className + " for property: " + info.getName());
            }
         }
      }

      if(metaMapper != null)
      {
         returnType = metaMapper.getMetaType();
         // Return the MetaMapper
         if(mapperReturn != null && mapperReturn.length > 0)
            mapperReturn[0] = metaMapper;
      }

      if(returnType == null && useTypeFactory)
      {
         // Use the type factory to convert the info type
         returnType = metaTypeFactory.resolve(infoType);
      }
      return returnType;
   }

   protected ManagedOperation getManagedOperation(MBeanOperationInfo methodInfo,
         ManagementOperation opAnnotation, ClassLoader mbeanLoader, MetaData metaData)
      throws Exception
   {
      String name = methodInfo.getName();
      String description = opAnnotation != null ? opAnnotation.description() : name;
      Impact impact = Impact.Unknown;
      switch(methodInfo.getImpact())
      {
         case MBeanOperationInfo.ACTION:
            impact = Impact.WriteOnly;
            break;
         case MBeanOperationInfo.ACTION_INFO:
            impact = Impact.ReadWrite;
            break;
         case MBeanOperationInfo.INFO:
            impact = Impact.ReadOnly;
            break;
         case MBeanOperationInfo.UNKNOWN:
            impact = Impact.Unknown;
            break;
      }
      // The op return type
      MetaMapper[] returnTypeMapper = {null};
      Class<?> returnTypeClass = loadTypeClass(methodInfo.getReturnType(), mbeanLoader);
      MetaType returnType = getMetaType(methodInfo, returnTypeClass, metaData, true, null, returnTypeMapper);

      // Process the op parameters
      ArrayList<ManagedParameter> mparams = new ArrayList<ManagedParameter>();
      MBeanParameterInfo[] paramInfo = methodInfo.getSignature();
      if( paramInfo != null )
      {
         for(int i = 0; i < paramInfo.length; i ++)
         {
            MBeanParameterInfo pinfo = paramInfo[i];
            String pname = pinfo.getName();
            String pdescription = pinfo.getDescription();

            // Generate a name if there is none
            if (pname == null)
               pname = "arg#" + i;
            Fields fields =  new DefaultFieldsImpl(pname);
            if (pdescription != null)
               fields.setField(Fields.DESCRIPTION, pdescription);
            MetaMapper[] paramMapper = {null};
            Class<?> paramType = loadTypeClass(pinfo.getType(), mbeanLoader);
            MetaType metaType = getMetaType(pinfo, paramType, metaData, true, null, paramMapper);
            fields.setField(Fields.META_TYPE, metaType);


            ManagedParameterImpl mp = new ManagedParameterImpl(fields);
            if(paramMapper[0] != null)
               mp.setTransientAttachment(MetaMapper.class.getName(), paramMapper[0]);
            mparams.add(mp);
         }
      }
      ManagedParameter[] parameters = new ManagedParameter[mparams.size()];
      mparams.toArray(parameters);

      ManagedOperationImpl op = new ManagedOperationImpl(name, description, impact, parameters, returnType);
      if(returnTypeMapper[0] != null)
         op.setTransientAttachment(MetaMapper.class.getName(), returnTypeMapper[0]);
      return op;
   }

   protected Class<?> loadTypeClass(String propertyType, ClassLoader loader)
      throws ClassNotFoundException
   {
      Class<?> type = null;
      // Check for a primitive type
      if(propertyType.equals("byte"))
         type = byte.class;
      else if(propertyType.equals("char"))
         type = char.class;
      else if(propertyType.equals("short"))
         type = short.class;
      else if(propertyType.equals("int"))
         type = int.class;
      else if(propertyType.equals("long"))
         type = long.class;
      else if(propertyType.equals("float"))
         type = float.class;
      else if(propertyType.equals("double"))
         type = double.class;
      else if(propertyType.equals("void"))
         type = void.class;
      else if(propertyType.equals("boolean"))
         type = boolean.class;

      else
      {
         type = loader.loadClass(propertyType);
      }
      return type;

   }
}
