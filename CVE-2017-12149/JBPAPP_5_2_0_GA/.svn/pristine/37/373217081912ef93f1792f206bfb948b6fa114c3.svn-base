/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.deployment;

import java.beans.PropertyEditor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.logging.Logger;
import org.jboss.resource.metadata.ConfigPropertyMetaData;
import org.jboss.util.Classes;

/**
 * Utility used to handle setting configuration properties from ra.xml file.
 * 
 * @author <a href="baileyje@gmail.com">John Bailey</a>
 * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @version $Revision: $
 */
public class ConfigPropertyHandler
{
   private static final Logger log = Logger.getLogger(ConfigPropertyHandler.class);

   /** A map of possible alternate property types to try. Used to support primitive types on ResourceAdapter impls **/
   private static Map<String, Class<?>> PRIMATIVE_TYPE_ALTERNATES = new HashMap<String, Class<?>>();
   static
   {
      PRIMATIVE_TYPE_ALTERNATES.put(Boolean.class.getName(), Boolean.TYPE);
      PRIMATIVE_TYPE_ALTERNATES.put(Byte.class.getName(), Byte.TYPE);
      PRIMATIVE_TYPE_ALTERNATES.put(Character.class.getName(), Character.TYPE);
      PRIMATIVE_TYPE_ALTERNATES.put(Double.class.getName(), Double.TYPE);
      PRIMATIVE_TYPE_ALTERNATES.put(Float.class.getName(), Float.TYPE);
      PRIMATIVE_TYPE_ALTERNATES.put(Integer.class.getName(), Integer.TYPE);
      PRIMATIVE_TYPE_ALTERNATES.put(Long.class.getName(), Long.TYPE);
      PRIMATIVE_TYPE_ALTERNATES.put(Short.class.getName(), Short.TYPE);
   }

   /** Object to set the Config Property on **/
   private final Object configTarget;

   /** Object type to set the Config Property on **/
   private final Class configTargetType;

   /** Identifier used to trace property values being set **/
   private final String traceIdentifier;

   /**
    * Constructor
    * 
    * @param configTarget
    * @param configTargetType
    */
   public ConfigPropertyHandler(Object configTarget, Class configTargetType)
   {
      this(configTarget, configTargetType, "");
   }

   /**
    * Constructor with trace string
    * @param configTarget
    * @param configTargetType
    * @param traceIdentifier
    */
   public ConfigPropertyHandler(Object configTarget, Class configTargetType, String traceIdentifier)
   {
      super();
      this.configTarget = configTarget;
      this.configTargetType = configTargetType;
      this.traceIdentifier = traceIdentifier;
   }

   /**
    * Handles setting a configuration property on a target object.
    * 
    * @param configProperty
    * @throws Exception
    */
   public void handle(ConfigProperty configProperty) throws Exception
   {
      handle(configProperty, true);
   }

   /**
    * Handles setting a configuration property on a target object.
    * 
    * @param configProperty
    * @param mustExist
    * @throws Exception
    */
   public void handle(ConfigProperty configProperty, boolean mustExist) throws Exception
   {
      if (log.isTraceEnabled())
      {
         log.trace("Handling config property - " + configProperty);
      }

      String propertyName = configProperty.getName();
      if (propertyName == null || propertyName.length() == 0)
      {
         throw new IllegalArgumentException("Null or empty attribute name " + propertyName);
      }

      Object propertyValue = configProperty.getValue();

      if (propertyValue == null)
      {
         if (log.isDebugEnabled())
         {
            log.debug("Not setting config property with null value " + configProperty);
         }
         return;
      }

      // Generate the setter name
      String setterName = getSetterName(propertyName);

      // Get the setter method using the setter name
      Method method = null;
      try
      {
         method = getMethod(setterName, configProperty.getType());
      }
      catch (NoSuchMethodException nsme)
      {
         String error = "The class '" + configTargetType + "' has no setter for config property '" + propertyName + "'";
         if (mustExist)
         {
            throw new IllegalArgumentException(error, nsme);
         }
         if (log.isTraceEnabled())
         {
            log.trace(error, nsme);
         }
         return;
      }

      if (log.isTraceEnabled())
      {
         log.trace(traceIdentifier + " setting property=" + propertyName + " value=" + configProperty.getValue());
      }

      // Invoke the setter
      method.invoke(configTarget, new Object[] {configProperty.getValue()});
   }

   /**
    * Handles setting a configuration property on a target object based on ConfigPropertyMetaData.
    * 
    * @param configPropertyMetaData
    * @throws Exception
    */
   public void handle(ConfigPropertyMetaData configPropertyMetaData) throws Exception
   {
      this.handle(configPropertyMetaData, true);
   }

   /**
    * Handles setting a configuration property on a target object based on ConfigPropertyMetaData.
    * 
    * @param configPropertyMetaData
    * @param mustExist
    * @throws Exception
    */
   public void handle(ConfigPropertyMetaData configPropertyMetaData, boolean mustExist) throws Exception
   {
      String propertyName = configPropertyMetaData.getName();
      String propertyType = configPropertyMetaData.getType();
      String propertyValue = configPropertyMetaData.getValue();

      if (propertyValue == null || propertyValue.length() == 0)
      {
         log.debug("Not setting config property with null value " + configPropertyMetaData);
         return;
      }

      // See if it is a primitive type alias first
      Class expectedPropertyType = Classes.getPrimitiveTypeForName(propertyType);
      if (expectedPropertyType == null)
      {
         // Not primitive alias, look for it.
         try
         {
            expectedPropertyType = Thread.currentThread().getContextClassLoader().loadClass(propertyType);
         }
         catch (ClassNotFoundException cnfe)
         {
            if (mustExist)
            {
               throw cnfe;
            }
            log.warn("Unable to find class '" + propertyType + "' for " + "property '" + propertyName
                  + "' - skipping property.");
         }
      }

      // Use the actual property type to get the value
      Object value = getValue(propertyName, expectedPropertyType, propertyValue, mustExist);

      // Invoke handle with constructed ConfigProperty
      handle(new ConfigProperty(propertyName, expectedPropertyType, value), mustExist);
   }

   /**
    * Generates the setter name for a property
    * 
    * @param propertyName
    * @return
    */
   private String getSetterName(String propertyName)
   {
      String setter = "set" + Character.toUpperCase(propertyName.charAt(0));
      if (propertyName.length() > 1)
         setter = setter.concat(propertyName.substring(1));
      return setter;
   }

   /**
    * Retrieves the setter method for the property.  Will check the expected type first, 
    * and if not found it will check for primitive alternatives.
    *   
    * @param setterName
    * @param expectedPropertyType
    * @return
    * @throws NoSuchMethodException
    */
   private Method getMethod(String setterName, Class expectedPropertyType) throws NoSuchMethodException
   {
      Method method = null;
      try
      {
         // Check to see if the method exists with the expected type
         method = configTargetType.getMethod(setterName, new Class[] {expectedPropertyType});
      }
      catch (NoSuchMethodException nsme)
      {
         // Check to see if a primitive alternative is available  
         expectedPropertyType = PRIMATIVE_TYPE_ALTERNATES.get(expectedPropertyType.getName());
         if (expectedPropertyType == null)
         {
            // No alternative to try
            throw nsme;
         }
         // Check to see if a setter with the primitive alternative exists
         method = configTargetType.getMethod(setterName, new Class[] {expectedPropertyType});
      }
      return method;
   }

   /**
    * Get the correctly typed value from the string value.  
    * 
    * @param propertyName
    * @param actualType
    * @param value
    * @param mustExist
    * @return
    */
   private Object getValue(String propertyName, Class actualType, String value, boolean mustExist)
   {
      PropertyEditor editor = PropertyEditorFinder.getInstance().find(actualType);
      if (editor == null)
      {
         String error = "No property editor found for property " + propertyName;
         if (mustExist)
         {
            throw new IllegalArgumentException(error);
         }
         else
         {
            log.warn(error);
            return null;
         }
      }
      try
      {
         editor.setAsText(value);
      }
      catch (IllegalArgumentException iae)
      {
         if (mustExist)
         {
            throw iae;
         }
         log.warn("Value '" + value + "' is not valid for property '" + propertyName + "' of class '" + actualType
               + "' - skipping " + "property");
         return null;
      }

      return editor.getValue();
   }
}
