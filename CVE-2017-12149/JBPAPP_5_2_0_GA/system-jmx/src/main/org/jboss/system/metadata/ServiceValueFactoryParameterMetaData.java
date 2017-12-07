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

package org.jboss.system.metadata;

import java.beans.PropertyEditor;
import java.lang.reflect.Constructor;

import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.util.Classes;

/**
 * A ServiceParameterTypeMetaData.
 * 
 * @author Brian Stansberry
 * @version $Revision: 113110 $
 */
public class ServiceValueFactoryParameterMetaData
{
   private final String textValue;
   private final String valueTypeName;
   private String parameterTypeName;
   
   public ServiceValueFactoryParameterMetaData(String textValue)
   {
      this(textValue, null, null);
   }
   
   public ServiceValueFactoryParameterMetaData(String textValue, String parameterTypeName)
   {
      this(textValue, parameterTypeName, null);
   }
   
   public ServiceValueFactoryParameterMetaData(String textValue, String parameterTypeName, String valueTypeName)
   {
      this.textValue = textValue;
      this.valueTypeName = valueTypeName;
      this.parameterTypeName = parameterTypeName;
   }
   
   public String getParameterTypeName()
   {
      return parameterTypeName;
   }
   
   public void setParameterTypeName(String parameterTypeName)
   {
      this.parameterTypeName = parameterTypeName;
   }
   
   public String getTextValue()
   {
      return this.textValue;
   }
   
   public String getValueTypeName()
   {
      return this.valueTypeName;
   }
   
   public Object getValue(ServiceValueContext valueContext) throws DeploymentException
   {
      if (this.parameterTypeName == null)
         throw new IllegalStateException("Must set parameterTypeName");
      
      String desiredType = (this.valueTypeName == null ? parameterTypeName : valueTypeName);
      
      return getValue(valueContext.getClassloader(), this.textValue, desiredType, valueContext.getAttributeInfo().getName());
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      
      if (obj instanceof ServiceValueFactoryParameterMetaData)
      {
         ServiceValueFactoryParameterMetaData other = (ServiceValueFactoryParameterMetaData) obj;
         return safeEquals(this.textValue, other.textValue)
                  && safeEquals(this.parameterTypeName, other.parameterTypeName)
                  && safeEquals(this.valueTypeName, other.valueTypeName);                 
      }
      return false;
   }

   @Override
   public int hashCode()
   {
      int result = 17;
      result += 29 * (this.textValue == null ? 0 : this.textValue.hashCode());
      result += 29 * (this.parameterTypeName == null ? 0 : this.parameterTypeName.hashCode());
      result += 29 * (this.valueTypeName == null ? 0 : this.valueTypeName.hashCode());
      return result;
   }
   
   @Override
   public String toString()
   {
      return new StringBuilder(getClass().getSimpleName())
        .append("{textValue=").append(textValue)
        .append(",parameterTypeName=").append(parameterTypeName)
        .append(",valueTypeName=").append(valueTypeName).toString();
   }

   private boolean safeEquals(Object a, Object b)
   {
      return (a == b || (a != null && a.equals(b)));
   }

   public static Object getValue(ClassLoader serviceValueContextClassloader, String textValue, String typeName, String targetAttributeName) throws DeploymentException
   {
      if (textValue == null)
         return null;

      // see if it is a primitive type first
      Class typeClass = Classes.getPrimitiveTypeForName(typeName);
      if (typeClass == null)
      {
         // nope try look up
         try
         {
            typeClass = serviceValueContextClassloader.loadClass(typeName);
         }
         catch (ClassNotFoundException e)
         {
            throw new DeploymentException("Class not found for attribute: " + targetAttributeName, e);
         }
      }
      
      if (String.class == typeClass)
         return textValue;

      PropertyEditor editor = PropertyEditorFinder.getInstance().find(typeClass);
      if (editor == null)
      {
         try
         {
            // See if there is a ctor(String) for the type
            Class[] sig = {String.class};
            Constructor ctor = typeClass.getConstructor(sig);
            Object[] args = {textValue};
            return ctor.newInstance(args);
         }
         catch (Exception e)
         {
            throw new DeploymentException("No property editor for attribute: " + targetAttributeName + "; type=" + typeClass.getName());
         }
      }
      else
      {
         // JBAS-1709, temporarily switch the TCL so that property
         // editors have access to the actual deployment ClassLoader.
         ClassLoader tcl = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader(serviceValueContextClassloader);
         try 
         {
            editor.setAsText(textValue);
            return editor.getValue();
         }
         finally 
         {
            Thread.currentThread().setContextClassLoader(tcl);
         }
      }
   }
}
