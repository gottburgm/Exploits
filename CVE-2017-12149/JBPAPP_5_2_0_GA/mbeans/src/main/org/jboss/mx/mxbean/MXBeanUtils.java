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
package org.jboss.mx.mxbean;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.management.DynamicMBean;
import javax.management.ObjectName;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import org.jboss.util.collection.WeakValueHashMap;

/**
 * Utils.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class MXBeanUtils
{
   /** A cache of methods to keys */
   private static final Map<Method, String> compositeDataKeyCache = Collections.synchronizedMap(new WeakHashMap<Method, String>()); 

   /** A cache of classes to key to getters */
   private static final Map<Class, Map<String, Method>> compositeDataMethodCache = Collections.synchronizedMap(new WeakHashMap<Class, Map<String, Method>>()); 
   
   /** The map key */
   public static final String MAP_KEY = "key";

   /** The map value */
   public static final String MAP_VALUE = "value";
   
   /** Map index names */
   public static final String[] MAP_INDEX_NAMES = { MAP_KEY };
   
   /** Map item names */
   public static final String[] MAP_ITEM_NAMES = { MAP_KEY, MAP_VALUE };
   
   /**
    * Get the OpenType for a class
    * 
    * @param type the type
    * @return the open type
    */
   public static OpenType getOpenType(Type type)
   {
      if (type == null)
         throw new IllegalArgumentException("Null type");

      OpenType result = checkType(type);
      if (result != null)
         return result;
      Class clazz = (Class) type;
      return CompositeTypeMetaDataFactory.getCompositeType(clazz);
   }

   /**
    * Get the SimpleType for a class
    * 
    * @param type the type
    * @return the open type
    * @throws Exception for any error 
    */
   public static SimpleType getSimpleType(Class type) throws Exception
   {
      SimpleType simpleType = checkSimpleType(type);
      if (simpleType == null)
         throw new IllegalArgumentException("Not a SimpleType: " + type.getName());
      return simpleType;
   }

   /**
    * Get the for a class that is not composite
    * 
    * @param type the type
    * @return the open type or null if composite
    */
   public static OpenType checkType(Type type)
   {
      OpenType result = checkSimpleType(type);
      if (result != null)
         return result;
      result = checkEnum(type);
      if (result != null)
         return result;
      result = checkArray(type);
      if (result != null)
         return result;
      result = checkCollection(type);
      if (result != null)
         return result;
      return checkMap(type);
   }
   
   /**
    * Create a composite data proxy
    * 
    * @param <T> the interface type
    * @param intf the interface type
    * @param compositeData the composite data
    * @return the proxy
    */
   public static <T> T createCompositeDataProxy(Class<T> intf, CompositeData compositeData)
   {
      if (intf == null)
         throw new IllegalArgumentException("Null interface");
      InvocationHandler handler = new CompositeDataInvocationHandler(compositeData);
      Object object = Proxy.newProxyInstance(intf.getClassLoader(), new Class[] { intf }, handler);
      return intf.cast(object);
   }
   
   /**
    * Construct some open data
    * 
    * @param type the type
    * @param value the value
    * @param context the context
    * @return the open data
    * @throws Exception for any error
    */
   public static Object construct(Type type, Object value, Object context) throws Exception
   {
      OpenType openType = getOpenType(type);
      return construct(openType, value, context);
   }
   
   /**
    * Construct some open data
    * 
    * @param openType the open type
    * @param value the value
    * @param context the context
    * @return the open data
    * @throws Exception for any error
    */
   public static Object construct(OpenType openType, Object value, Object context) throws Exception
   {
      if (openType instanceof SimpleType)
         return constructSimpleData(value);
      if (openType.isArray())
         return constructArrayData(openType, value, context);
      if (openType instanceof TabularType)
         return constructTabularData(openType, value, context);
      return constructCompositeData(openType, value, context);
   }
   
   /**
    * Reconstruct a type from an object
    * 
    * @param type the type
    * @param value the value
    * @param context for error reporting
    * @return the object
    * @throws Exception for any error
    */
   public static Object reconstruct(Type type, Object value, Object context) throws Exception
   {
      OpenType openType = MXBeanUtils.getOpenType(type);
      return reconstruct(openType, type, value, context);
   }
   
   /**
    * Reconstruct a type from an object
    * 
    * @param openType the open type
    * @param type the type
    * @param value the value
    * @param context for error reporting
    * @return the object
    * @throws Exception for any error
    */
   public static Object reconstruct(OpenType openType, Type type, Object value, Object context) throws Exception
   {
      if (openType instanceof SimpleType)
         return reconstructSimpleData(type, value, context);
      if (openType.isArray())
         return reconstructArrayData(openType, type, value, context);
      if (openType instanceof TabularType)
         return reconstructTabularData(openType, type, value, context);
      return reconstructCompositeData(openType, type, value, context);
   }

   /**
    * Get the SimpleType for a class
    * 
    * @param type the type
    * @return the simple type or null if not a simple type
    */
   public static SimpleType checkSimpleType(Type type)
   {
      if (BigDecimal.class.equals(type))
         return SimpleType.BIGDECIMAL;
      if (BigInteger.class.equals(type))
         return SimpleType.BIGINTEGER;
      if (Boolean.class.equals(type))
         return SimpleType.BOOLEAN;
      if (Boolean.TYPE.equals(type))
         return SimpleType.BOOLEAN;
      if (Byte.class.equals(type))
         return SimpleType.BYTE;
      if (Byte.TYPE.equals(type))
         return SimpleType.BYTE;
      if (Character.class.equals(type))
         return SimpleType.CHARACTER;
      if (Character.TYPE.equals(type))
         return SimpleType.CHARACTER;
      if (Date.class.equals(type))
         return SimpleType.DATE;
      if (Double.class.equals(type))
         return SimpleType.DOUBLE;
      if (Double.TYPE.equals(type))
         return SimpleType.DOUBLE;
      if (Float.class.equals(type))
         return SimpleType.FLOAT;
      if (Float.TYPE.equals(type))
         return SimpleType.FLOAT;
      if (Integer.class.equals(type))
         return SimpleType.INTEGER;
      if (Integer.TYPE.equals(type))
         return SimpleType.INTEGER;
      if (Long.class.equals(type))
         return SimpleType.LONG;
      if (Long.TYPE.equals(type))
         return SimpleType.LONG;
      if (ObjectName.class.equals(type))
         return SimpleType.OBJECTNAME;
      if (Short.class.equals(type))
         return SimpleType.SHORT;
      if (Short.TYPE.equals(type))
         return SimpleType.SHORT;
      if (String.class.equals(type))
         return SimpleType.STRING;
      if (Void.class.equals(type))
         return SimpleType.VOID;
      return null;
   }

   /**
    * Get the simple type for an enum
    * 
    * @param type the type
    * @return return the enum type or null if it is not an enum
    */
   public static SimpleType checkEnum(Type type)
   {
      if (type instanceof Class == false)
         return null;
      Class clazz = (Class) type;
      if (clazz.isEnum() || Enum.class.equals(clazz))
         return SimpleType.STRING;
      return null;
   }

   /**
    * Construct a simple type open data
    * 
    * @param value the value
    * @return the simple type
    */
   public static Object constructSimpleData(Object value)
   {
      if (value != null && value instanceof Enum)
      {
         Enum enumeration = (Enum) value;
         return enumeration.name();
      }
      return value;
   }

   /**
    * Reconstruct a simple type open data
    * 
    * @param type the type
    * @param value the value
    * @param context the context
    * @return the simple type
    */
   @SuppressWarnings("unchecked")
   private static Object reconstructSimpleData(Type type, Object value, Object context)
   {
      if (type instanceof Class)
      {
         if (value != null)
         {
            Class clazz = (Class) type;
            if (clazz.isEnum() || Enum.class.equals(clazz))
            {
               String string = (String) value;
               return Enum.valueOf(clazz, string);
            }
         }
         else
         {
            Class clazz = (Class) type;
            if (clazz.isPrimitive())
               throw new IllegalArgumentException("Attempt to use null as a primitive for: " + context);
            return null;
         }
      }
      return value;
   }

   /**
    * Get the array type for a class
    * 
    * @param type the type
    * @return return the array type or null if it is not an array
    */
   public static ArrayType checkArray(Type type)
   {
      if (type instanceof Class)
      {
         Class clazz = (Class) type;
         if (clazz.isArray() == false)
            return null;
         int dimension = 1;
         Class componentType = clazz.getComponentType();
         while (componentType.isArray())
         {
            ++dimension;
            componentType = componentType.getComponentType();
         }
         OpenType componentOpenType = getOpenType(componentType);
         try
         {
            return new ArrayType(dimension, componentOpenType);
         }
         catch (RuntimeException e)
         {
            throw e;
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      if (type instanceof GenericArrayType)
      {
         GenericArrayType arrayType = (GenericArrayType) type;
         int dimension = 1;
         Type componentType = arrayType.getGenericComponentType();
         while (componentType instanceof GenericArrayType)
         {
            ++dimension;
            arrayType = (GenericArrayType) componentType;
            componentType = arrayType.getGenericComponentType();
         }
         OpenType componentOpenType = getOpenType(componentType);
         try
         {
            return new ArrayType(dimension, componentOpenType);
         }
         catch (RuntimeException e)
         {
            throw e;
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      return null;
   }

   /**
    * Get the collection type for a class
    * 
    * @param type the type
    * @return return the array type or null if it is not a collection
    */
   public static ArrayType checkCollection(Type type)
   {
      if (type instanceof ParameterizedType == false)
      {
         if (type instanceof Class)
            return checkCollectionClass((Class) type);
         else
            return null;
      }
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type rawType = parameterizedType.getRawType();
      if (rawType instanceof Class == false)
         return null;
      Class rawClass = (Class) rawType;
      if (Collection.class.isAssignableFrom(rawClass) == false)
         return null;
      Type componentType = parameterizedType.getActualTypeArguments()[0];
      OpenType componentOpenType = getOpenType(componentType);
      try
      {
         return new ArrayType(1, componentOpenType);
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Get the collection type for a class
    * 
    * @param clazz the class
    * @return return the array type or null if it is not a collection
    */
   public static ArrayType checkCollectionClass(Class clazz)
   {
      if (Collection.class.isAssignableFrom(clazz) == false)
         return null;
      OpenType componentOpenType = getOpenType(Object.class);
      try
      {
         return new ArrayType(1, componentOpenType);
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Construct an array type open data
    *
    * @param openType the open type
    * @param value the value
    * @param context the context
    * @return the open data
    * @throws Exception for any error
    */
   public static Object constructArrayData(OpenType openType, Object value, Object context) throws Exception
   {
      if (value == null)
         return null;

      ArrayType arrayType = (ArrayType) openType;
      OpenType elementType = arrayType.getElementOpenType();
      int dimension = arrayType.getDimension();

      Class clazz = value.getClass();
      if (clazz.isArray())
      {
         Object[] oldArray = (Object[]) value;
         Class<?> componentType = Class.forName(arrayType.getClassName());
         return constructArray(elementType, componentType.getComponentType(), dimension, oldArray, context);
      }
      if (value instanceof Collection)
      {
         Collection c = (Collection) value;
         Object[] oldArray = c.toArray();
         Class<?> componentType = Class.forName(arrayType.getClassName()); 
         return constructArray(elementType, componentType.getComponentType(), dimension, oldArray, context);
      }
      throw new UnsupportedOperationException("Cannot construct array for: " + value);
   }
   
   /**
    * Construct an array of open data
    * 
    * @param elementType the element type
    * @param componentType the componentType
    * @param dimension the dimension
    * @param oldArray the old array
    * @param context the context
    * @return the array
    * @throws Exception for any error
    */
   private static Object[] constructArray(OpenType elementType, Class<?> componentType, int dimension, Object[] oldArray, Object context) throws Exception
   {
      if (oldArray == null)
         return null;
      
      Object[] newArray = (Object[]) Array.newInstance(componentType, oldArray.length);
      if (dimension > 1)
      {
         for (int i = 0; i < oldArray.length; ++i)
         {
            Object[] nestedOld = (Object[]) oldArray[i];
            newArray[i] = constructArray(elementType, componentType.getComponentType(), dimension-1, nestedOld, context);
         }
      }
      else
      {
         if (Object.class.equals(componentType))
         {
            for (int i = 0; i < oldArray.length; ++i)
               newArray[i] = oldArray[i];
         }
         else
         {
            for (int i = 0; i < oldArray.length; ++i)
               newArray[i] = construct(elementType, oldArray[i], context);
         }
      }

      return newArray;
   }
   
   /**
    * Reconstruct an array type
    *
    * @param openType the open type
    * @param type the type
    * @param value the value
    * @param context the context
    * @return the value
    * @throws Exception for any error
    */
   public static Object reconstructArrayData(OpenType openType, Type type, Object value, Object context) throws Exception
   {
      if (value == null)
         return null;

      ArrayType arrayType = (ArrayType) getOpenType(type);
      OpenType elementType = arrayType.getElementOpenType();
      int dimension = arrayType.getDimension();
      Object[] oldArray = (Object[]) value;
      if (type instanceof Class)
      {
         Class clazz = (Class) type;
         if (clazz.isArray())
            return reconstructArray(elementType, clazz.getComponentType(), dimension, oldArray, context);
         // TODO FIXME
         // else if (Set.class.isAssignableFrom(clazz))
         //    return createSet(oldArray);
         // else if (Collection.class.isAssignableFrom(clazz))
         //    return createCollection(oldArray);
      }
      else if (type instanceof ParameterizedType)
      {
         ParameterizedType parameterizedType = (ParameterizedType) type;
         Type rawType = parameterizedType.getRawType();
         if (rawType instanceof Class)
         {
            Class raw = (Class) rawType;
            if (Set.class.isAssignableFrom(raw))
               return createSet(oldArray);
            else if (Collection.class.isAssignableFrom(raw))
               return createCollection(oldArray);
         }
      }
      throw new UnsupportedOperationException("Cannot convert array type: " + type);
   }
   
   /**
    * Reconstruct an array
    * 
    * @param elementType the element type
    * @param componentType the componentType
    * @param dimension the dimension
    * @param oldArray the old array of open data
    * @param context the context
    * @return the array
    * @throws Exception for any error
    */
   private static Object[] reconstructArray(OpenType elementType, Class componentType, int dimension, Object[] oldArray, Object context) throws Exception
   {
      if (oldArray == null)
         return null;
      
      Object[] newArray = (Object[]) Array.newInstance(componentType, oldArray.length);
      if (dimension > 1)
      {
         for (int i = 0; i < oldArray.length; ++i)
         {
            Object[] nestedOld = (Object[]) oldArray[i];
            newArray[i] = reconstructArray(elementType, componentType.getComponentType(), dimension-1, nestedOld, context);
         }
      }
      else
      {
         for (int i = 0; i < oldArray.length; ++i)
            newArray[i] = reconstruct(elementType, componentType, oldArray[i], context);
      }

      return newArray;
   }

   /**
    * Create a collection
    * 
    * @param array the array
    * @return the collection
    */
   private static Collection createCollection(Object[] array)
   {
      return Arrays.asList(array);
   }
   
   /**
    * Create a set
    * 
    * @param array the array
    * @return the set
    */
   @SuppressWarnings("unchecked")
   private static Set createSet(Object[] array)
   {
      HashSet result = new HashSet(array.length);
      for (int i = 0; i < array.length; ++i)
         result.add(array[i]);
      return result;
   }

   /**
    * Get the map type for a class
    * 
    * @param type the type
    * @return return the tabular type or null if it is not a collection
    */
   public static TabularType checkMap(Type type)
   {
      if (type instanceof ParameterizedType == false)
      {
         if (type instanceof Class)
            return checkMapClass((Class) type);
         else
            return null;
      }
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type rawType = parameterizedType.getRawType();
      if (rawType instanceof Class == false)
         return null;
      Class rawClass = (Class) rawType;
      if (Map.class.isAssignableFrom(rawClass) == false)
         return null;
      Type[] args = parameterizedType.getActualTypeArguments();
      Type keyType = args[0];
      Type valueType = args[1];
      return createMapType(keyType, valueType);
   }

   /**
    * Get the map type for a class
    * 
    * @param clazz the class
    * @return return the tabular type or null if it is not a collection
    */
   public static TabularType checkMapClass(Class clazz)
   {
      if (Map.class.isAssignableFrom(clazz) == false)
         return null;
      return createMapType(Object.class, Object.class);
   }
   
   /**
    * Create a map type
    * 
    * @param keyType the key type
    * @param valueType the value type
    * @return the map type
    */
   public static TabularType createMapType(Type keyType, Type valueType)
   {
      String name = Map.class.getName();
      OpenType[] itemTypes = { getOpenType(keyType), getOpenType(valueType) };
      try
      {
         CompositeType entryType = createMapEntryType(itemTypes);
         return new TabularType(name, name, entryType, MAP_INDEX_NAMES);
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   /**
    * Create a map type
    * 
    * @param itemTypes the item types
    * @return the map entry type
    */
   private static CompositeType createMapEntryType(OpenType[] itemTypes)
   {
      String entryName = Map.Entry.class.getName();
      try
      {
         return new CompositeType(entryName, entryName, MAP_ITEM_NAMES, MAP_ITEM_NAMES, itemTypes);
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Construct a tabular type open data
    *
    * @param openType the open type
    * @param value the value
    * @param context the context
    * @return the open data
    * @throws Exception for any error
    */
   @SuppressWarnings("unchecked")
   public static Object constructTabularData(OpenType openType, Object value, Object context) throws Exception
   {
      if (value == null)
         return null;

      TabularType tabularType = (TabularType) openType;

      if (value instanceof Map)
      {
         TabularDataSupport table = new TabularDataSupport(tabularType);
         CompositeType entryType = tabularType.getRowType();
         OpenType keyType = entryType.getType(MAP_KEY);
         OpenType valueType = entryType.getType(MAP_VALUE);
         
         Map<Object, Object> m = (Map<Object, Object>) value;
         for (Iterator<Map.Entry<Object, Object>> i = m.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry<Object, Object> entry = i.next();
            Object key = construct(keyType, entry.getKey(), context);
            Object val = construct(valueType, entry.getValue(), context);
            CompositeDataSupport data = new CompositeDataSupport(entryType, MXBeanUtils.MAP_ITEM_NAMES, new Object[] { key, val });
            table.put(data);
         }
         return table;
      }
      throw new UnsupportedOperationException("Cannot construct map for: " + value);
   }
   
   /**
    * Reconstruct a tabular type
    *
    * @param openType the open type
    * @param type the type
    * @param value the value
    * @param context the context
    * @return the value
    * @throws Exception for any error
    */
   public static Object reconstructTabularData(OpenType openType, Type type, Object value, Object context) throws Exception
   {
      if (value == null)
         return null;

      TabularType tabularType = (TabularType) getOpenType(type);
      if (type instanceof Class)
      {
         // TODO FIXME
         // Class clazz = (Class) type;
         // if (Map.class.isAssignableFrom(clazz))
         //    return createMap(tabularType, Object.class, Object.class, value, context);
      }
      else if (type instanceof ParameterizedType)
      {
         ParameterizedType parameterizedType = (ParameterizedType) type;
         Type rawType = parameterizedType.getRawType();
         if (rawType instanceof Class)
         {
            Class raw = (Class) rawType;
            if (Map.class.isAssignableFrom(raw))
            {
               Type keyType = parameterizedType.getActualTypeArguments()[0];
               Type valueType = parameterizedType.getActualTypeArguments()[1];
               return createMap(tabularType, keyType, valueType, value, context);
            }
         }
      }
      throw new UnsupportedOperationException("Cannot convert map type: " + type);
   }

   /**
    * Create a map
    * 
    * @param openType the open type
    * @param keyType the key type
    * @param valueType the value type
    * @param value the value
    * @param context the context
    * @return the map
    * @throws Exception for any problem
    */
   @SuppressWarnings("unchecked")
   private static Map createMap(TabularType openType, Type keyType, Type valueType, Object value, Object context) throws Exception
   {
      if (value == null)
         return null;
      
      Map<Object, Object> result = new HashMap<Object, Object>();
      
      TabularData table = (TabularData) value;
      Collection<?> values = table.values();
      for (Object v : values)
      {
         CompositeData entry = CompositeData.class.cast(v);
         Object key = reconstruct(keyType, entry.get(MAP_KEY), context);
         Object val = reconstruct(valueType, entry.get(MAP_VALUE), context);
         result.put(key, val);
      }
      
      return result;
   }

   /**
    * Construct composite type open data
    *
    * @param openType the open type
    * @param value the value
    * @param context the context
    * @return the open data
    * @throws Exception for any error
    */
   @SuppressWarnings("unchecked")
   public static Object constructCompositeData(OpenType openType, Object value, Object context) throws Exception
   {
      if (value == null)
         return null;
      
      Class clazz = value.getClass();
      
      CompositeType compositeType = (CompositeType) openType;
      Set<String> nameSet = compositeType.keySet();
      String[] names = nameSet.toArray(new String[nameSet.size()]);
      
      Object[] values = new Object[names.length];
      
      for (int i = 0 ; i < names.length; ++i)
      {
         String name = names[i];
         OpenType itemType = compositeType.getType(name);
         Method method = getCompositeDataMethod(clazz, name, itemType == SimpleType.BOOLEAN);
         Object itemValue = method.invoke(value, null);
         values[i] = construct(itemType, itemValue, context);
      }
      return new CompositeDataSupport(compositeType, names, values);
   }

   /**
    * Reconstruct a composite type
    *
    * @param openType the open type
    * @param type the type
    * @param value the value
    * @param context the context
    * @return the value
    * @throws Exception for any error
    */
   public static Object reconstructCompositeData(OpenType openType, Type type, Object value, Object context) throws Exception
   {
      if (value == null)
         return null;
      
      CompositeData compositeData = (CompositeData) value;
      CompositeDataInvocationHandler handler = new CompositeDataInvocationHandler(compositeData);
      Class clazz = (Class) type;
      Class[] interfaces = null;
      if (clazz.isInterface())
         interfaces = new Class[] { clazz };
      else
         interfaces = clazz.getInterfaces();
      return Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, handler);
   }
   
   /**
    * Get the key for a composite data getter method
    * 
    * @param method the method
    * @return the key
    */
   public static String getCompositeDataKey(Method method)
   {
      String key = compositeDataKeyCache.get(method);
      if (key != null)
         return key;

      StringBuilder fieldName = null;
      
      Class returnType = method.getReturnType();
      Class[] paramTypes = method.getParameterTypes();
      if (Void.TYPE.equals(returnType) == false && paramTypes.length == 0)
      {
         String name = method.getName();
         if (name.startsWith("is") && name.length() > 2)
         {
            if (Boolean.TYPE.equals(returnType))
            {
               fieldName = new StringBuilder();
               fieldName.append(Character.toLowerCase(name.charAt(2)));
               if (name.length() > 3)
                  fieldName.append(name.substring(3));
            }
         }
         else if (name.startsWith("get") && name.length() > 3)
         {
            fieldName = new StringBuilder();
            fieldName.append(Character.toLowerCase(name.charAt(3)));
            if (name.length() > 4)
               fieldName.append(name.substring(4));
         }
      }
      
      if (fieldName == null)
         return null;
      
      String result = fieldName.toString();
      compositeDataKeyCache.put(method, result);
      return result;
   }
   
   /**
    * Get the key for a composite data getter method
    * 
    * @param clazz the class
    * @param key the key
    * @param isBoolean whether it is boolean
    * @return the method
    * @throws Exception for any error
    */
   @SuppressWarnings("unchecked")
   public static Method getCompositeDataMethod(Class clazz, String key, boolean isBoolean) throws Exception
   {
      Map<String, Method> cache = compositeDataMethodCache.get(clazz);
      if (cache != null)
      {
         Method method = cache.get(key);
         if (method != null)
            return method;
      }

      StringBuilder name = new StringBuilder();
      name.append(Character.toUpperCase(key.charAt(0)));
      if (key.length() > 1)
         name.append(key.substring(1));
      Method method = null;
      try
      {
         method = clazz.getMethod("get" + name, null); 
      }
      catch (NoSuchMethodException e)
      {
         if (isBoolean)
         {
            try
            {
               method = clazz.getMethod("is" + name, null);
            }
            catch (NoSuchMethodException ignored)
            {
               throw e;
            }
         }
         else
         {
            throw e;
         }
      }
      
      if (cache == null)
      {
         cache = new WeakValueHashMap();
         compositeDataMethodCache.put(clazz, cache);
      }
      cache.put(key, method);
      return method;
   }
   
   /**
    * Create a new MXBean
    * 
    * @param resource the resource
    * @param mxbeanInterface the interface
    * @return the MXBean
    */
   public static DynamicMBean createMXBean(Object resource, Class<?> mxbeanInterface)
   {
      try
      {
         return new MXBeanDelegate(resource, mxbeanInterface);
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error creating MXBean", e);
      }
   }
}
