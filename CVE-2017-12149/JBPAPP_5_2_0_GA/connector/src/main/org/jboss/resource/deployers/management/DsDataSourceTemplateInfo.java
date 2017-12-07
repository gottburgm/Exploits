/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.deployers.management;

import java.io.ObjectStreamException;
import java.lang.annotation.Annotation;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.annotation.factory.AnnotationProxy;
import org.jboss.managed.api.Fields;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.annotation.ManagementObjectID;
import org.jboss.managed.plugins.BasicDeploymentTemplateInfo;
import org.jboss.managed.plugins.DefaultFieldsImpl;
import org.jboss.managed.plugins.ManagedPropertyImpl;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValueSupport;

/**
 * The template for creating jca datasources, connection factories
 * 
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 110845 $
 */
public class DsDataSourceTemplateInfo extends BasicDeploymentTemplateInfo
{
   private static final long serialVersionUID = 1;
   
   /** The default values. */
   private static final Map<String, MetaValue> defaultValues = new HashMap<String, MetaValue>();
   
   /** The property name mappings. */
   private Map<String, String> propertyNameMappings;
   
   /** The datasource type. */
   private String dsType = "local-tx-datasource";
   
   static
   {
      // populate the default values
      defaultValues.put("use-java-context", new SimpleValueSupport(SimpleMetaType.BOOLEAN_PRIMITIVE, true));
      defaultValues.put("min-pool-size", new SimpleValueSupport(SimpleMetaType.INTEGER_PRIMITIVE, 0));
      defaultValues.put("max-pool-size", new SimpleValueSupport(SimpleMetaType.INTEGER_PRIMITIVE, 10));
      defaultValues.put("blocking-timeout-millis", new SimpleValueSupport(SimpleMetaType.LONG_PRIMITIVE, 3000));
      defaultValues.put("idle-timeout-minutes", new SimpleValueSupport(SimpleMetaType.INTEGER_PRIMITIVE, 30));
      defaultValues.put("background-validation", new SimpleValueSupport(SimpleMetaType.BOOLEAN_PRIMITIVE, false));
      defaultValues.put("background-validation-millis", new SimpleValueSupport(SimpleMetaType.LONG_PRIMITIVE, 0));
      defaultValues.put("validate-on-match", new SimpleValueSupport(SimpleMetaType.BOOLEAN_PRIMITIVE, true));
      defaultValues.put("interleaving", new SimpleValueSupport(SimpleMetaType.BOOLEAN_PRIMITIVE, false));
      defaultValues.put("allocation-retry", new SimpleValueSupport(SimpleMetaType.INTEGER_PRIMITIVE, 0));
      defaultValues.put("allocation-retry-wait-millis", new SimpleValueSupport(SimpleMetaType.INTEGER_PRIMITIVE, 5000));
      defaultValues.put("prepared-statement-cache-size", new SimpleValueSupport(SimpleMetaType.INTEGER_PRIMITIVE, 0));
      defaultValues.put("share-prepared-statements", new SimpleValueSupport(SimpleMetaType.BOOLEAN_PRIMITIVE, false));
      defaultValues.put("set-tx-query-timeout", new SimpleValueSupport(SimpleMetaType.BOOLEAN_PRIMITIVE, false));
      defaultValues.put("query-timeout", new SimpleValueSupport(SimpleMetaType.INTEGER_PRIMITIVE, 0));
      defaultValues.put("use-fast-fail", new SimpleValueSupport(SimpleMetaType.BOOLEAN_PRIMITIVE, false));
   }

   public DsDataSourceTemplateInfo(String arg0, String arg1, Map<String, ManagedProperty> arg2)
   {
      super(arg0, arg1, arg2);
   }

   public DsDataSourceTemplateInfo(String name, String description, String datasourceType)
   {
      super(name, description);
      this.dsType = datasourceType;
   }

   public Map<String, String> getPropertyNameMappings()
   {
      return propertyNameMappings;
   }
   public void setPropertyNameMappings(Map<String, String> propertyNameMappings)
   {
      this.propertyNameMappings = propertyNameMappings;
   }


   public String getConnectionFactoryType()
   {
      return dsType;
   }

   public void setConnectionFactoryType(String dsType)
   {
      this.dsType = dsType;
   }

   public void start()
   {
      populate();
   }

   @Override
   public DsDataSourceTemplateInfo copy()
   {
      DsDataSourceTemplateInfo copy = new DsDataSourceTemplateInfo(getName(), getDescription(), getProperties());
      copy.setPropertyNameMappings(propertyNameMappings);
      copy.setConnectionFactoryType(getConnectionFactoryType());
      super.copy(copy);
      copy.populate();
      return copy;
   }

   private void populate()
   {
      populateDefaultValues();
      // Pass the 
      DefaultFieldsImpl fields = new DefaultFieldsImpl("dsType");
      fields.setDescription("The datasource type");
      fields.setMetaType(SimpleMetaType.STRING);
      fields.setValue(SimpleValueSupport.wrap(dsType));
      fields.setField(Fields.READ_ONLY, Boolean.TRUE);
      ManagedPropertyImpl dsTypeMP = new ManagedPropertyImpl(fields);
      addProperty(dsTypeMP);
      
      // DataSource
//      if("local-tx-datasource".equals(dsType))
//         createLocalTxDsTemplate();
//      else if("xa-datasource".equals(dsType))
//         createXaDsTemplate();
//      else if("tx-connection-factory".equals(dsType))
//         createTxCfTemplate();
//      else if("no-tx-datasource".equals(dsType))
//         createNoTxDsTemplate();
//      else if("no-tx-connection-factory".equals(dsType))
//         createNoTxCfTemplate();
//      else
//         throw new IllegalStateException("Unsupported dsType: " + dsType);      
   }
   
   private void populateDefaultValues()
   {
      if(getProperties() == null) return;
      for(ManagedProperty property : getProperties().values())
      {
         String name = property.getName();
         
         
         // Create a new (non-writethrough) managed property
         Fields f = property.getFields();
         if( propertyNameMappings != null )
         {
            String mappedName = propertyNameMappings.get(name);
            if( mappedName != null )
               f.setField(Fields.MAPPED_NAME, mappedName);
         }
         
         ManagedPropertyImpl newProperty = new ManagedPropertyImpl(f);
         
         MetaValue v = defaultValues.get(name);
         if(v != null)
         {
            newProperty.setValue(v);
         }
         
         // Override
         addProperty(newProperty);
      }
   }
   
   private void addID(ManagedPropertyImpl mp)
   {
      // TODO
      Map<String, Annotation> annotations = new HashMap<String, Annotation>();
      Map<String, Object> idFields = Collections.emptyMap();
      try
      {
         ManagementObjectID id = (ManagementObjectID) AnnotationProxy.createProxy(idFields, ManagementObjectID.class);
         annotations.put(ManagementObjectID.class.getName(), id);
         mp.setAnnotations(annotations);
      }
      catch(Exception e)
      {
         throw new UndeclaredThrowableException(e);
      }
   }

   /**
    * Expose only plain BasicDeploymentTemplateInfo to avoid leaking server types.
    *
    * @return simpler ManagedPropertyImpl
    * @throws java.io.ObjectStreamException for any error
    */
   private Object writeReplace() throws ObjectStreamException
   {
      BasicDeploymentTemplateInfo info = new BasicDeploymentTemplateInfo(getName(), getDescription(), getProperties());
      return info;
   }
}
