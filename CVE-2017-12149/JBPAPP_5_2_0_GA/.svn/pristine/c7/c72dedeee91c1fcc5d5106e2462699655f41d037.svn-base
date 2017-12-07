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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.metatype.api.types.ArrayMetaType;
import org.jboss.metatype.api.types.CollectionMetaType;
import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.types.EnumMetaType;
import org.jboss.metatype.api.types.GenericMetaType;
import org.jboss.metatype.api.types.MapCompositeMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.Name;
import org.jboss.metatype.api.types.PropertiesMetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.types.TableMetaType;
import org.jboss.metatype.api.values.ArrayValue;
import org.jboss.metatype.api.values.ArrayValueSupport;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.CollectionValueSupport;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.CompositeValueSupport;
import org.jboss.metatype.api.values.EnumValue;
import org.jboss.metatype.api.values.EnumValueSupport;
import org.jboss.metatype.api.values.GenericValue;
import org.jboss.metatype.api.values.GenericValueSupport;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.PropertiesMetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.metatype.api.values.TableValue;
import org.jboss.metatype.api.values.TableValueSupport;
import org.jboss.metatype.plugins.types.StringName;
import org.jboss.reflect.plugins.ValueConvertor;
import org.jboss.system.server.profileservice.persistence.xml.NullValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedArrayValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedCollectionValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedCompositeValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedEnumValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedGenericValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedPair;
import org.jboss.system.server.profileservice.persistence.xml.PersistedPropertiesValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedSimpleValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedTableValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedValue;

/**
 * The abstract value recreation creates MetaValues based on the persisted
 * managed object information and the MetaTypes of the ManagedObject used
 * for persistence. The recreation of ManagedObjects itself is delegated
 * to a ManagedObjectRecreationPlugin.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class AbstractValueRecreation
{
   
   /** The simple types. */
   private final static Map<String, Class<? extends Serializable>> simpleTypes = new HashMap<String, Class<? extends Serializable>>();
   
   /** The logger. */
   private final static Logger log = Logger.getLogger(AbstractValueRecreation.class);
   
   /** The recreation plugin. */
   private final ManagedObjectPersistencePlugin plugin;
   
   static
   {
      // Fill simple types map.
      simpleTypes.put(BigDecimal.class.getName(), BigDecimal.class);
      simpleTypes.put(BigInteger.class.getName(), BigInteger.class);
      simpleTypes.put(Boolean.class.getName(), Boolean.class);
      simpleTypes.put(Byte.class.getName(), Byte.class);
      simpleTypes.put(Character.class.getName(), Character.class);
      simpleTypes.put(Date.class.getName(), Date.class);
      simpleTypes.put(Double.class.getName(), Double.class);
      simpleTypes.put(Float.class.getName(), Float.class);
      simpleTypes.put(Integer.class.getName(), Integer.class);
      simpleTypes.put(Long.class.getName(), Long.class);
      simpleTypes.put(Short.class.getName(), Short.class);
      simpleTypes.put(String.class.getName(), String.class);
      simpleTypes.put(Name.class.getName(), Name.class);
      // primitive classes
      simpleTypes.put(byte.class.getName(), byte.class);
      simpleTypes.put(char.class.getName(), char.class);
      simpleTypes.put(double.class.getName(), double.class);
      simpleTypes.put(float.class.getName(), float.class);
      simpleTypes.put(int.class.getName(), int.class);
      simpleTypes.put(short.class.getName(), short.class);
      simpleTypes.put(long.class.getName(), long.class);
      simpleTypes.put(boolean.class.getName(), boolean.class);
   }
   
   public AbstractValueRecreation(ManagedObjectPersistencePlugin callback)
   {
      if(callback == null)
         throw new IllegalArgumentException("null managed object persistence callback");
      
      this.plugin = callback;
   }
   
   public ManagedObjectPersistencePlugin getPlugin()
   {
      return plugin;
   }
   
   /**
    * Create the meta value, based on the xml persisted
    * value and the generated MetaType.
    * 
    * @param valueElement the persisted xml element
    * @param type the meta type
    * @return the created meta value
    */
   public MetaValue createMetaValue(PersistedValue valueElement, MetaType metaType)
   {
      if(log.isTraceEnabled())
      {
         log.trace("processing value " + valueElement + " type: " + metaType);
      }
      
      if(valueElement instanceof NullValue)
         return null;
      
      if(valueElement == null)
         return null;
      
      MetaValue metaValue = null;
      try
      {
         if(metaType.isSimple())
         {
            metaValue = createSimpleValue(
                  (PersistedSimpleValue) valueElement,
                  (SimpleMetaType) metaType);
         }
         else if(metaType.isEnum())
         {
            metaValue = createEnumValue(
                  (PersistedEnumValue) valueElement,
                  (EnumMetaType) metaType);
         }
         else if(metaType.isCollection())
         {
            metaValue = createCollectionValue(
                  (PersistedCollectionValue) valueElement,
                  (CollectionMetaType) metaType);
         }
         else if(metaType.isGeneric())
         {
            metaValue = createGenericValue(
                  (PersistedGenericValue) valueElement,
                  (GenericMetaType) metaType);
         }
         else if(metaType.isComposite())
         {
            metaValue = createCompositeValue(
                  (PersistedCompositeValue) valueElement,
                  (CompositeMetaType) metaType);
         }
         else if(metaType.isTable())
         {
            metaValue = createTableValue(
                  (PersistedTableValue) valueElement,
                  (TableMetaType)metaType);
         }
         else if(metaType.isArray())
         {
            metaValue = createArrayValue(
                  (PersistedArrayValue) valueElement,
                  (ArrayMetaType) metaType);
         }
         else if(metaType.isProperties())
         {
            metaValue = createPropertiesValue(
                  (PersistedPropertiesValue) valueElement,
                  (PropertiesMetaType) metaType);
         }
         else
         {
            throw new IllegalStateException("unknown metaType");
         }
      }
      finally
      {
         //
      }
      return metaValue;
   }
   
   /**
    * Create simple value.
    * 
    * @param valueElement the persisted xml meta data
    * @param value the simple value
    * @return a simple value
    */
   protected SimpleValue createSimpleValue(PersistedSimpleValue valueElement, SimpleMetaType metaType)
   {
      String elementValue = valueElement.getValue();
      
      Serializable converted = null;
      if(elementValue != null)
      {
         if(metaType.equals(SimpleMetaType.STRING))
         {
            converted = (String) elementValue;
         }
         else if (metaType.equals(SimpleMetaType.NAMEDOBJECT))
         {
            converted = new StringName(elementValue);
         }
         else if (metaType.equals(SimpleMetaType.VOID))
         {  
            // 
         }
         else
         {
            converted = convert2Type(metaType.getTypeName(), elementValue);
         }
      }
      return new SimpleValueSupport(metaType, converted);
   }
 
   /**
    * Process an Enum value.
    * 
    * @param enumElement the persisted xml meta data
    * @param value the enum value
    * @return a enum value
    */
   protected EnumValue createEnumValue(PersistedEnumValue enumElement, EnumMetaType type)
   {
      return new EnumValueSupport(type, enumElement.getValue());
   }
   
   /**
    * Create composite value.
    * 
    * @param composite the persisted xml meta data
    * @param value the composite value
    * @return a composite value
    */
   protected CompositeValue createCompositeValue(PersistedCompositeValue composite, CompositeMetaType type)
   {
      // Handle the mapCompositeMetaType differently
      if(type instanceof MapCompositeMetaType)
         return handleMapCompositeMetaType(composite, (MapCompositeMetaType) type);
      
      // Create composite value
      Map<String, MetaValue> values = new HashMap<String, MetaValue>();
      if(composite.getValues() != null && composite.getValues().isEmpty() == false)
      {
         for(PersistedValue persistedValue : composite.getValues())
         {
            MetaType elementType = type.getType(persistedValue.getName());
            if(elementType == null)
               throw new IllegalStateException("Failed to process composite value: " + persistedValue.getName());
            
            // Create
            MetaValue metaValue = createMetaValue(persistedValue, elementType);
            // Put
            values.put(persistedValue.getName(), metaValue);
         }
      }     
      return new CompositeValueSupport(type, values);
   }

   /**
    * Create the MapCompositeValueSupport value.
    * 
    * @param composite the persisted composite xml meta data
    * @param type the MapComposite meta type
    * @return the MapCompositeValueSupport
    */
   protected MapCompositeValueSupport handleMapCompositeMetaType(PersistedCompositeValue composite, MapCompositeMetaType type)
   {
      Map<String, MetaValue> values = new HashMap<String, MetaValue>();
      if(composite.getValues() != null && composite.getValues().isEmpty() == false)
      {
         for(PersistedValue persistedValue : composite.getValues())
         {
            MetaValue value = createMetaValue(persistedValue, type.getValueType());
            values.put(persistedValue.getName(), value);
         }
      }
      return new MapCompositeValueSupport(values, type);
   }
   
   /**
    * Process a collection.
    * 
    * @param collection the persisted xml meta data
    * @param value the collection value
    * @return a collection value
    */
   protected CollectionValue createCollectionValue(PersistedCollectionValue collection, CollectionMetaType type)
   {
      List<MetaValue> elementList = new ArrayList<MetaValue>();
      if(collection.getValues() != null && collection.getValues().isEmpty() == false)
      {
         for(PersistedValue element : collection.getValues())
         {
            elementList.add(
                  createMetaValue(element, type.getElementType()));
         }  
      }
      return new CollectionValueSupport(type, elementList.toArray(new MetaValue[elementList.size()]));
   }
   
   /**
    * Create generic value. 
    * 
    * @param genericElement the persisted generic xml meta data 
    * @param metaType the generic meta type
    * @return the generic value
    */
   protected GenericValue createGenericValue(PersistedGenericValue genericElement, GenericMetaType metaType)
   {
      Serializable value = null;
      if(genericElement.getManagedObject() != null)
      {
         value = getPlugin().createManagedObject(genericElement.getManagedObject());
      }
      return new GenericValueSupport(metaType, value);
   }
   
   /**
    * Create the table value.
    * 
    * @param table the persisted table value
    * @param type the table meta type
    * @return the table value
    */
   protected TableValue createTableValue(PersistedTableValue table, TableMetaType type)
   {
      TableValueSupport support = new TableValueSupport(type);
      if(table.getEntries() != null && table.getEntries().isEmpty() == false)
      {
         for(PersistedCompositeValue entry : table.getEntries())
         {
            support.put(createCompositeValue(entry, type.getRowType()));
         }         
      }
      return support;
   }
   
   /**
    * Create the properties value.
    * 
    * @param value the persisted properties value
    * @param metaType the properties meta type
    * @return the properties value
    */
   protected MetaValue createPropertiesValue(
         PersistedPropertiesValue value, PropertiesMetaType metaType)
   {
      PropertiesMetaValue properties = new PropertiesMetaValue();
      for(PersistedPair pair : value.getEntries())
      {
         properties.setProperty(pair.getKey(), pair.getValue());
      }

      return properties;
   }

   /**
    * Create array value.
    * 
    * @param valueElement the persisted array xml value
    * @param type the array meta type 
    * @return the array value
    */
   @SuppressWarnings("unchecked")
   protected ArrayValue createArrayValue(PersistedArrayValue valueElement, ArrayMetaType type)
   {
      int size = valueElement.size();
      List values = new ArrayList(size);
      for(PersistedValue elementValue : valueElement.getValues())
      {
         if(elementValue instanceof PersistedArrayValue)
         {
            values.add(
                  recreateArrayValue((PersistedArrayValue) elementValue, type.getElementType()));
         }
         else
         {
            MetaValue value = createMetaValue(elementValue, type.getElementType());
            values.add(value);
         }
      }
      return new ArrayValueSupport(type, values.toArray());
   }

   /**
    * Recreate the array values.
    * 
    * @param valueElement the persisted xml value
    * @param type the element type
    * @return the recreated array
    */
   @SuppressWarnings("unchecked")
   protected Object recreateArrayValue(PersistedArrayValue valueElement, MetaType type)
   {
      List values = new ArrayList(valueElement.size());
      for(PersistedValue elementValue : valueElement.getValues())
      {
         if(elementValue instanceof PersistedArrayValue)
         {
            values.add(
                  recreateArrayValue((PersistedArrayValue) elementValue, type));
         }
         else
         {
            MetaValue value = createMetaValue(elementValue, type);
            values.add(value);
         }
      }
      return values.toArray();
   }
   
   /**
    * Convert simple types.
    * 
    * @param clazz a primitive serializable class.
    * @param value the String
    * @return the converted object, null in case of any failure.
    */
   public Serializable convert2Type(String className, String value)
   {
      if(value == null)
         return null;
      
      Class<?> clazz = simpleTypes.get(className);
      if(clazz == null)
         throw new IllegalStateException("Cannot find simple type entry for "+ value + " and class "+ className);
      
      try
      {
         return (Serializable) ValueConvertor.convertValue(clazz, value);
      }
      catch(Throwable t)
      {
         log.debug("could convert "+ value +" to " + clazz.getName());
         return null;
      }
   }
   
}
