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
package org.jboss.system.server.profileservice.persistence;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.managed.api.ManagedObject;
import org.jboss.metatype.api.types.ArrayMetaType;
import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.values.ArrayValue;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.EnumValue;
import org.jboss.metatype.api.values.GenericValue;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.MetaValueFactory;
import org.jboss.metatype.api.values.PropertiesMetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.TableValue;
import org.jboss.system.server.profileservice.persistence.xml.NullValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedArrayValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedCollectionValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedCompositeValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedEnumValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedGenericValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;
import org.jboss.system.server.profileservice.persistence.xml.PersistedPair;
import org.jboss.system.server.profileservice.persistence.xml.PersistedPropertiesValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedSimpleValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedTableValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedValue;

/**
 * The AbstractValuePersistence creates a xml representation of MetaValues
 * which are used for the ProfileService attachment persistence.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class AbstractValuePersistence
{
   
   /** The logger. */
   private static final Logger log = Logger.getLogger(AbstractValuePersistence.class);

   /** The meta value factory. */
   private final MetaValueFactory metaValueFactory;
   
   /** The plugin. */
   private final ManagedObjectPersistencePlugin plugin;
   
   public AbstractValuePersistence(ManagedObjectPersistencePlugin callback, MetaValueFactory metaValueFactory)
   {
      if(callback == null)
         throw new IllegalArgumentException("Null managed object persistence callback.");
      if(metaValueFactory == null)
         throw new IllegalArgumentException("Null meta value factory.");
      
      this.plugin = callback;
      this.metaValueFactory = metaValueFactory;
   }
   
   /**
    * Get the meta value factory.
    * 
    * @return the meta value factory
    */
   public MetaValueFactory getMetaValueFactory()
   {
      return this.metaValueFactory;
   }
   
   protected ManagedObjectPersistencePlugin getPlugin()
   {
      return this.plugin;
   }
   
   /**
    * Create the peristed xml meta data.
    * 
    * @param value the meta value
    * @param metaType the meta type
    * @return the xml value
    */
   protected PersistedValue createPersistedValue(MetaValue value, MetaType metaType)
   {
      return createPersistedValue(value, metaType, null);
   }
   
   /**
    * Create the peristed xml meta data.
    * 
    * @param value the meta value
    * @param metaType the meta type
    * @param persisted
    * @return the xml value.
    */
   protected PersistedValue createPersistedValue(MetaValue value, MetaType metaType, PersistedValue persisted)
   {
      if(log.isTraceEnabled())
         log.trace("creating persisted value for : " + value + " with metaType " + metaType);
      
      if(value == null)
         return new NullValue();

      // Override the metaType e.g. the MapCompositeValueSupport
      metaType = value.getMetaType();


      PersistedValue persistedValue = null;
      if(metaType.isSimple())
      {
         persistedValue = createSimpleValue(
               (SimpleValue) value);
      }
      else if(metaType.isEnum())
      {
         persistedValue = createEnumValue(
               (EnumValue) value);
      }
      else if(metaType.isCollection())
      {
         persistedValue = createCollectionValue(
               (CollectionValue) value);
      }
      else if(metaType.isGeneric())
      {
         persistedValue = createGenericValue(
               (GenericValue) value);
      }
      else if(metaType.isComposite())
      {
         persistedValue = createCompositeValue(
               (CompositeValue) value,
               (CompositeMetaType) metaType);
      }
      else if(metaType.isArray())
      {
         persistedValue = createArrayValue(
               (ArrayValue) value,
               (ArrayMetaType) metaType);
      }
      else if(metaType.isTable())
      {
         persistedValue = createTableValue(
               (TableValue) value);
      }
      else if(metaType.isProperties())
      {
         persistedValue = createPropertiesValue(
               (PropertiesMetaValue) value);
      }
      else
      {
         throw new IllegalStateException("unknown metaType");
      }
      return persistedValue;
   }
   

   /**
    * Create the persistence enum value.
    * 
    * @param value the enum value
    * @return the enum xml meta data
    */
   protected PersistedEnumValue createEnumValue(EnumValue value)
   {
      PersistedEnumValue persistedValue = new PersistedEnumValue();
      persistedValue.setValue(value.getValue());
      return persistedValue;
   }

   /**
    * Create the persistence simple value.
    * 
    * @param value the simple value
    * @return the simple xml meta data
    */
   protected PersistedSimpleValue createSimpleValue(SimpleValue value)
   {
      PersistedSimpleValue persistedValue = new PersistedSimpleValue();
      persistedValue.setValue(convertSimple2String(value));
      return persistedValue;
   }
   
   /**
    * Create the persistence collection value.
    * 
    * @param value the collection value
    * @return the collection xml meta data
    */
   protected PersistedCollectionValue createCollectionValue(CollectionValue value)
   {
      PersistedCollectionValue collection = new PersistedCollectionValue();
      for(MetaValue child : value.getElements())
      {
         PersistedValue persistedValue = createPersistedValue(child, child.getMetaType());
         collection.addValue(persistedValue);
      }
      return collection;
   }

   /**
    * Create the persistence generic value.
    * 
    * @param value the generic value
    * @return the generic xml meta data
    */
   protected PersistedGenericValue createGenericValue(GenericValue value)
   {
      //
      PersistedGenericValue generic = new PersistedGenericValue();
      return createGenericValue(value, generic);
   }
   
   /**
    * Create the persistence generic value.
    * 
    * @param value the generic value
    * @param the persisted generic value
    * @return the generic xml meta data
    */
   protected PersistedGenericValue createGenericValue(GenericValue value, PersistedGenericValue generic)
   {
      Object o = value.getValue();
      if(o == null)
         return generic;
      
      if(o instanceof ManagedObject)
      {
         PersistedManagedObject mo;
         
         if(generic.getManagedObject() == null)
            mo = plugin.createPersistedManagedObject((ManagedObject) o);
         else
            mo = plugin.createPersistedManagedObject(generic.getManagedObject(), (ManagedObject) o);
         
         generic.setManagedObject(mo);
      }
      else
      {
         throw new IllegalStateException("The value of GenericValue must be a ManagedObject: " + value);
      }
      return generic;
   }

   /**
    * Create the persistence array value.
    * 
    * @param value the array value
    * @return
    */
   protected PersistedArrayValue createArrayValue(ArrayValue value, ArrayMetaType metaType)
   {
      //
      PersistedArrayValue array = new PersistedArrayValue();
      MetaType elementType = metaType.getElementType();
      for (int i = 0; i < value.getLength(); i++)
      {
         PersistedValue persistedValue = null;
         Object subElement = value.getValue(i);

         if (subElement instanceof MetaValue)
         {
            persistedValue = createPersistedValue((MetaValue) subElement, elementType);
         }
         else if (subElement != null && subElement.getClass().isArray())
         {
            persistedValue = unwrapArray(array, subElement, elementType);
         }
         // Add to parent
         array.addValue(persistedValue);
      }
      return array;
   }
   
   /**
    * Unwrap array.
    * 
    * @param array the parent array
    * @param element the array value
    * @param type the element meta type
    * @return the persistence xml meta data
    */
   protected PersistedArrayValue unwrapArray(PersistedArrayValue array, Object element, MetaType type)
   {
      PersistedArrayValue newElement = new PersistedArrayValue();
      int subSize = Array.getLength(element);
      for (int i = 0; i < subSize; i++)
      {
         PersistedValue persistedValue = null;
         Object subElement = Array.get(element, i);
         if (subElement instanceof MetaValue)
         {
            persistedValue = createPersistedValue((MetaValue) subElement, type);
         }
         else if (subElement != null && subElement.getClass().isArray())
         {
            persistedValue = unwrapArray(newElement, subElement, type);
         }

         newElement.addValue(persistedValue);
      }
      return newElement;
   }
   
   /**
    * Create the persistence composite value.
    * 
    * @param value the composite value
    * @param metaType the composite meta type
    * @param the persisted composite value
    * @return the persistence composite xml meta data
    */
   protected PersistedCompositeValue createCompositeValue(CompositeValue value, CompositeMetaType metaType)
   {
      PersistedCompositeValue composite = new PersistedCompositeValue();
      // Fix the values
      List<PersistedValue> values = composite.getValues();
      if(values == null)
      {
         values = new ArrayList<PersistedValue>();
         composite.setValues(values);
      }
      for(String item : metaType.itemSet())
      {
         MetaType itemType = metaType.getType(item);
         MetaValue itemValue = value.get(item);
         
         // Create item 
         PersistedValue persistedValue = createPersistedValue(itemValue, itemType);
         persistedValue.setName(item);
         
         values.add(persistedValue);
      }
      return composite;
   }
   
   /**
    * Create the persistence table value.
    * 
    * @param value the table value
    * @param the persisted table
    * @return the persistence table xml meta data
    */   
   protected PersistedTableValue createTableValue(TableValue value)
   {
      PersistedTableValue table = new PersistedTableValue();
      // Fix the entries
      List<PersistedCompositeValue> entries = table.getEntries();
      if(entries == null)
      {
         entries = new ArrayList<PersistedCompositeValue>();
         table.setEntries(entries);
      }
      // Process values
      Collection<CompositeValue> values = value.values();
      for(CompositeValue entry : values)
      {
         entries.add(createCompositeValue(entry, entry.getMetaType()));
      }
      return table;
   }
   
   /**
    * Create the persistence properties value.
    * 
    * @param value the properties value
    * @param the persisted properties
    * @return the persistence properties xml meta data
    */
   protected PersistedValue createPropertiesValue(PropertiesMetaValue value)
   {
      PersistedPropertiesValue properties = new PersistedPropertiesValue();
      List<PersistedPair> pairs = properties.getEntries();
      if(pairs == null)
      {
         pairs = new ArrayList<PersistedPair>();
         properties.setEntries(pairs);
      }
      for(Object key : value.keySet())
      {
         Object kvalue = value.get(key);
         PersistedPair pair = new PersistedPair(key.toString(), kvalue.toString());
         pairs.add(pair);
      }
      return properties;
   }

   /**
    * Create a emtpy xml meta data, based on the meta type
    * 
    * @param metaType the meta type
    * @return the peristence value
    */
   protected static PersistedValue emtpyPersistedValue(MetaType metaType)
   {
      if(metaType.isSimple())
      {
         return new PersistedSimpleValue(); 
      }
      else if(metaType.isEnum())
      {
         return new PersistedEnumValue();
      }
      else if(metaType.isCollection())
      {
         return new PersistedCollectionValue();
      }
      else if(metaType.isGeneric())
      {
         return new PersistedGenericValue();
      }
      else if(metaType.isComposite())
      {
         return new PersistedCompositeValue();
      }
      else if(metaType.isTable())
      {
         return new PersistedTableValue();
      }
      else if(metaType.isArray())
      {
         return new PersistedArrayValue();
      }
      else if(metaType.isProperties())
      {
         return new PersistedPropertiesValue();
      }
      else
      {
         throw new IllegalStateException("unknown metaType");
      }
   }
   
   /**
    * Convert a simple meta value to a String.
    * 
    * @param value the simple meta value.
    * @return the string.
    */
   protected String convertSimple2String(SimpleValue value)
   {       
      if(value == null)
         throw new IllegalArgumentException("Null value.");
      
      Object unwrappedValue = getMetaValueFactory().unwrap(value);
      if(unwrappedValue == null)
         return null; 
      // Convert to String
      return ("" + unwrappedValue);
   }
   
}