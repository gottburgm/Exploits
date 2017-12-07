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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import org.jboss.util.UnexpectedThrowable;

/**
 * CompositeTypeMetaData.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class CompositeTypeMetaData
{
   /** The clazz */
   private Class<?> clazz;
   
   /** The composite type */
   private CompositeType compositeType;
   
   /** The items */
   private List<ItemMetaData> items = new ArrayList<ItemMetaData>();
   
   /**
    * Create a new CompositeTypeMetaData.
    * 
    * @param clazz the class
    */
   public CompositeTypeMetaData(Class<?> clazz)
   {
      this.clazz = clazz;
   }
   
   /**
    * Get the composite type
    * 
    * @return the composite type
    */
   public CompositeType getCompositeType()
   {
      if (compositeType == null)
      {
         String typeName = clazz.getName();
         if (items.size() == 0)
            return generateAnObject(typeName);
            
         String[] itemNames = new String[items.size()];
         OpenType[] itemTypes = new OpenType[items.size()];
         for (int i = 0; i < items.size(); ++i)
         {
            ItemMetaData item = items.get(i);
            itemNames[i] = item.getName();
            itemTypes[i] = item.getOpenType();
         }
         try
         {
            compositeType = new CompositeType(typeName, typeName, itemNames, itemNames, itemTypes);
         }
         catch (Throwable t)
         {
            throw new UnexpectedThrowable("Error creating composite type for: " + typeName, t);
         }
      }
      return compositeType;
   }

   /**
    * Generate the metadata
    */
   public void generate()
   {
      if (Object.class.equals(clazz))
      {
         compositeType = generateObject();
         return;
      }
      if (Class.class.equals(clazz))
      {
         compositeType = generateClass();
         return;
      }
      if (ClassLoader.class.equals(clazz))
      {
         compositeType = generateClassLoader();
         return;
      }

      Method[] methods = clazz.getMethods();
      for (Method method : methods)
      {
         Type returnType = method.getGenericReturnType();
         Class<?> declaring = method.getDeclaringClass();
         if (Object.class.equals(declaring) == false)
         {
            String key = MXBeanUtils.getCompositeDataKey(method);
            if (key != null)
               items.add(new ItemMetaData(key, returnType));
         }
      }
   }

   /**
    * Generate the composite data for an object
    * 
    * @return the composite type
    */
   public static CompositeType generateObject()
   {
      return generateAnObject(Object.class.getName());
   }

   /**
    * Generate the composite data for an object
    * 
    * @param name the class name
    * @return the composite type
    */
   private static CompositeType generateAnObject(String name)
   {
      String[] itemNames = { "class" };
      OpenType[] openTypes = { generateClass() };
      return safeCreateCompositeType(name, itemNames, openTypes);
   }

   /**
    * Generate the composite data for a class
    * 
    * @return the composite type
    */
   public static CompositeType generateClass()
   {
      String name = Object.class.getName();
      String[] itemNames = { "name" };
      OpenType[] openTypes = { SimpleType.STRING };
      return safeCreateCompositeType(name, itemNames, openTypes);
   }

   /**
    * Generate the composite data for a classloader
    * 
    * @return the composite type
    */
   public static CompositeType generateClassLoader()
   {
      String name = Object.class.getName();
      String[] itemNames = { "name" };
      OpenType[] openTypes = { SimpleType.STRING };
      return safeCreateCompositeType(name, itemNames, openTypes);
   }

   /**
    * Safely create a composite type
    * 
    * @param name the name
    * @param itemNames the item names
    * @param openTypes the open types
    * @return the composite type
    */
   private static CompositeType safeCreateCompositeType(String name, String[] itemNames, OpenType[] openTypes)
   {
      try
      {
         return new CompositeType(name, name, itemNames, itemNames, openTypes);
      }
      catch (Exception e)
      {
         throw new UnexpectedThrowable(e);
      }
   }
}
