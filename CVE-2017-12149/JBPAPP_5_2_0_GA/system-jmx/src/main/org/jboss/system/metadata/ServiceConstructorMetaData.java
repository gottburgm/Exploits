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
import java.io.Serializable;
import java.lang.reflect.Constructor;

import org.jboss.common.beans.property.BeanUtils;
import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.system.ConfigurationException;
import org.jboss.util.Classes;
import org.jboss.util.StringPropertyReplacer;

/**
 * ServiceConstructorMetaData.
 * 
 * This class is based on the old ConstructorInfo from ServiceCreator
 * 
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 113110 $
 */
public class ServiceConstructorMetaData
   implements Serializable
{
   private static final long serialVersionUID = 1;

   /** An empty parameters */
   public static final Object[] EMPTY_PARAMETERS = {};

   /** An empty parameters list. */
   public static final String[] EMPTY_PARAMS = {};

   /** An signature list. */
   public static final String[] EMPTY_SIGNATURE = {};

   /** The constructor signature. */
   private String[] signature = EMPTY_SIGNATURE;

   /** The constructor parameters. */
   private String[] params = EMPTY_PARAMS;

   /** The real parameters */
   private Object[] parameters;
   
   /**
    * Get the params.
    * 
    * @return the params.
    */
   public String[] getParams()
   {
      return params;
   }

   /**
    * Set the params.
    * 
    * @param params the params.
    */
   public void setParams(String[] params)
   {
      if (params == null)
         throw new IllegalArgumentException("Null params");
      this.params = params;
   }

   /**
    * Get the signature.
    * 
    * @return the signature.
    * @throws ConfigurationException if there is a problem with the signature
    */
   public String[] getSignature() throws ConfigurationException
   {
      for (String string : signature)
      {
         if (string == null || string.trim().length() == 0)
            throw new ConfigurationException("Missing or empty 'type' attribute in constructor arg");
      }
      return signature;
   }

   /**
    * Set the signature.
    * 
    * @param signature the signature.
    */
   public void setSignature(String[] signature)
   {
      if (signature == null)
         throw new IllegalArgumentException("Null signature");
      this.signature = signature;
   }
   
   /**
    * Get the parameters
    * 
    * @param cl the class loader
    * @return the parameters
    * @throws Exception for any error
    */
   public Object[] getParameters(ClassLoader cl) throws Exception
   {
      if (parameters != null)
         return parameters;
      
      if (params.length == 0)
         return EMPTY_PARAMETERS;

      String[] signature = getSignature();
      
      Object[] result = new Object[params.length];
      for (int i = 0; i < result.length; ++i)
      {
         if (params[i] == null)
            throw new ConfigurationException("Missing 'value' attribute in constructor arg");
         
         String value = StringPropertyReplacer.replaceProperties(params[i]); 
         Object realValue = value;

         if (signature[i] != null)
         {
            // See if it is a primitive type first
            Class typeClass = Classes.getPrimitiveTypeForName(signature[i]);
            if (typeClass == null)
               typeClass = cl.loadClass(signature[i]);

            // Convert the string to the real value
            PropertyEditor editor = PropertyEditorFinder.getInstance().find(typeClass);
            if (editor == null)
            {
               try
               {
                  // See if there is a ctor(String) for the type
                  Class[] sig = {String.class};
                  Constructor ctor = typeClass.getConstructor(sig);
                  Object[] args = {value};
                  realValue = ctor.newInstance(args);
               }
               catch (Exception e)
               {
                  throw new ConfigurationException("No property editor for type: " + typeClass);
               }
            }
            else
            {
               editor.setAsText(value);
               realValue = editor.getValue();
            }
         }
         result[i] = realValue;
      }
      return result;
   }

   /**
    * Set the parameters.
    * 
    * @param parameters the parameters.
    */
   public void setParameters(Object[] parameters)
   {
      this.parameters = parameters;
   }
}
