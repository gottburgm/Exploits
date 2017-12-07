/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.system.server.profileservice.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.beans.info.spi.PropertyInfo;
import org.jboss.logging.Logger;
import org.jboss.managed.api.Fields;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;
import org.jboss.system.server.profileservice.persistence.xml.PersistedProperty;
import org.jboss.system.server.profileservice.persistence.xml.PersistedValue;

/**
 * A abstract ManagedObject persistence helper.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public abstract class AbstractManagedObjectPersistence 
{

   /** The logger. */
   private static final Logger log = Logger.getLogger(AbstractManagedObjectPersistence.class);
   
   /** The value persistence. */
   private final AbstractValuePersistence valuePersistence;
   
   protected AbstractManagedObjectPersistence(AbstractValuePersistence valuePersistence)
   {
      if(valuePersistence == null)
         throw new IllegalArgumentException("null value persistence.");
      
      this.valuePersistence = valuePersistence;
   }
   
   public AbstractValuePersistence getValuePersistence()
   {
      return valuePersistence;
   }
   
   /**
    * Create a persisted managed object.
    * 
    * @param mo the managed object
    * @return the persistence xml meta data for managed object
    */
   public PersistedManagedObject createPersistedManagedObject(ManagedObject mo)
   {
      PersistedManagedObject persisted = new PersistedManagedObject();
      return createPersistedManagedObject(persisted, mo);
   }
   
   /**
    * Process a managed object.
    * 
    * @param persisted the xml meta data
    * @param mo the managed object
    * @return isModified
    */
   public PersistedManagedObject createPersistedManagedObject(PersistedManagedObject persisted, ManagedObject mo)
   {
      if(persisted == null)
         throw new IllegalArgumentException("Null persisted managed object.");
      if(mo == null)
         throw new IllegalArgumentException("Null managed object");
      
      // Set the template and class-name 
      String className = mo.getAttachmentName();
      if(mo.getAttachment() != null)
      {
         Class<?> attachment = mo.getAttachment().getClass();
         className = attachment.getName();
         // Set the template name
         if(className.equals(mo.getAttachmentName()) == false)
         {
            // If the MO template is different from the actual attachment
            persisted.setTemplateName(mo.getAttachmentName());
         }
      }
      String name = mo.getName();
      if(mo.getComponentName() != null && mo.getComponentName() instanceof String)
         name = (String) mo.getComponentName();
      
      if(persisted.getOriginalName() == null)
         persisted.setOriginalName(name);

      // Set the managed-object meta information
      persisted.setName(name);
      persisted.setClassName(className);

      return persisted;
   }

   /**
    * Process the properties of the ManagedObject.
    * 
    * @param persisted the persisted managed object
    * @param mo the managed object
    */
   protected void processProperties(PersistedManagedObject persisted, ManagedObject mo)
   {
      boolean trace = log.isTraceEnabled();
      processProperties(persisted, mo, trace);
   }
   
   /**
    * Process the properties of the ManagedObject.
    * 
    * @param persisted the persisted managed object
    * @param mo the managed object
    * @param trace enable trace logs
    */
   protected void processProperties(PersistedManagedObject persisted, ManagedObject mo, boolean trace)
   {
      if(persisted == null)
         throw new IllegalArgumentException("Null persisted object");
      if(mo == null)
         throw new IllegalArgumentException("Null managed object.");
     
      //
      Map<String, PersistedProperty> properties = getPersistedProperties(persisted);
      for(String propertyName : mo.getPropertyNames())
      {
         //
         ManagedProperty property = mo.getProperty(propertyName);
         PersistedProperty persistedProperty = properties.get(propertyName);
         
         if(persistedProperty == null)
         {
            // Create a new persisted property
            persistedProperty = createPersistedProperty(property);
         }
         
         // Process
         if(processProperty(property, persistedProperty, trace))
         {
            persisted.getProperties().add(persistedProperty);
         }
      }
   }
   
   /**
    * Create a persisted property.
    * 
    * @param property the managed Property.
    * @param persisted
    * @return
    */
   protected PersistedProperty createPersistedProperty(ManagedProperty property)
   {
      if(property == null)
         throw new IllegalArgumentException("Null managed property.");
      
      PersistedProperty persisted = new PersistedProperty();
      persisted.setName(property.getName());
      return persisted;
   }

   /**
    * Process a managed property.
    * 
    * @param property the managed property
    * @param persisted the persisted property
    * @return true, if the property was processed
    */
   protected boolean processProperty(ManagedProperty property, PersistedProperty persisted)
   {
      boolean trace = log.isTraceEnabled();
      return processProperty(property, persisted, trace);
   }
   
   /**
    * Process a managed property.
    * 
    * @param property the managed property
    * @param persisted the persisted property
    * @param trace enable trace logs
    * @return true, if the property was processed
    */   
   protected boolean processProperty(ManagedProperty property, PersistedProperty persisted, boolean trace)
   {
      if(property == null)
         throw new IllegalArgumentException("Null managed property.");
      if(persisted == null)
         throw new IllegalArgumentException("Null persisted property.");
      
      boolean processed = false;
      // Check if we need to process this property
      if(isProcessProperty(property, trace))
      {
         // 
         MetaValue metaValue = property.getField(Fields.VALUE, MetaValue.class);
         MetaType metaType = property.getField(Fields.META_TYPE, MetaType.class);
         // Override metaType
         if(metaValue != null)
            metaType = metaValue.getMetaType();
         
         // Create the persisted value
         PersistedValue value = createPersistedValue(metaValue, metaType, persisted.getValue());
         if(value != null)
         {
            persisted.setValue(value);
            if(trace)
               log.trace("value for property ("+ property.getName() +"): " + value);
            processed = true;
         }
      }
      return processed;
   }
   
   /**
    * Create a persisted value. This delegates the value creation
    * to the ValuePeristence.
    * 
    * @param metaValue the meta value
    * @param metaType the meta type
    * @param persisted the persisted value
    * @return the created persisted value
    */
   private PersistedValue createPersistedValue(MetaValue metaValue, MetaType metaType, PersistedValue persisted)
   {
      return getValuePersistence().createPersistedValue(metaValue, metaType, persisted);
   }
   
   /**
    * Does this property needs to be processed.
    * 
    * @param property the managed property
    * @param trace enable trace logs
    * @return false if the property does not need to be processed otherwise true
    */
   protected boolean isProcessProperty(ManagedProperty property, boolean trace)
   {
      boolean process = false;
      // 
      if(property == null)
         return process;
      
      // Skip non configuration properties
      if(property.hasViewUse(ViewUse.CONFIGURATION) == false)
      {
         if(trace)
            log.trace("Skip non configuration property: " + property.getName());
         return process;
      }
      // Skip read only properties
      if(property.isReadOnly())
      {
         if(trace)
            log.trace("Skip readOnly property: " + property.getName());
         return process;
      }
      // Skip removed properties
      if(property.isRemoved())
      {
         if(trace)
            log.trace("Skip removed property: " + property.getName());
         return process;
      }
      // Skip read only properties
      PropertyInfo propertyInfo = property.getField(Fields.PROPERTY_INFO, PropertyInfo.class);
      if(propertyInfo != null && propertyInfo.isReadable() == false)
      {
         if(trace)
            log.trace("Skip non readable property: " + property.getName());
         return process;
      }      
      return true;
   }
   
   /**
    * Get a map of persisted managed objects, with the property name as key.
    * 
    * @param persisted the persisted managed object
    * @return a map of persisted properties
    */
   protected static Map<String, PersistedProperty> getPersistedProperties(PersistedManagedObject persisted)
   {
      if(persisted == null)
         throw new IllegalArgumentException("Null persisted managed object.");
      
      Map<String, PersistedProperty> properties = new HashMap<String, PersistedProperty>();
      List<PersistedProperty> list = persisted.getProperties();
      if(list == null)
      {
         list = new ArrayList<PersistedProperty>();
         persisted.setProperties(list);
      }
      if(list.isEmpty() == false)
      {
         for(PersistedProperty p : list)
            properties.put(p.getName(), p);
      }
      return properties;
   }
   
}

