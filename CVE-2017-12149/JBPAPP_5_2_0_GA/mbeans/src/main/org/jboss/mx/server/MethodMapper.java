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
package org.jboss.mx.server;

import java.lang.reflect.Method;
import java.util.HashMap;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.Descriptor;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import org.jboss.mx.modelmbean.ModelMBeanConstants;

/**
 * Helper class for resolving JMX *Info objects against Method objects. It's typically
 * used during the construction of dispatchers during MBean registration/creation.
 *
 * @author <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 * @author <a href="mailto:juha@jboss.org">Juha Lindfors</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81026 $
 */
public class MethodMapper
{
   // Attributes ----------------------------------------------------

   private HashMap map = null;
   
   // Static --------------------------------------------------------
   
   /**
    * Generates a signature string for an attribute getter method using standard rules
    */
   public static String getterSignature(MBeanAttributeInfo info)
   {
      if (null == info)
      {
         throw new IllegalArgumentException("MBeanAttributeInfo cannot be null");
      }

      String sig = null;
      if( info instanceof ModelMBeanAttributeInfo )
      {
         ModelMBeanAttributeInfo minfo = (ModelMBeanAttributeInfo) info;
         Descriptor desc = minfo.getDescriptor();
         String methodName = (String) desc.getFieldValue(ModelMBeanConstants.GET_METHOD);
         if( methodName != null )
            sig = methodSignature(info.getType(), methodName, null);
      }
      if( sig == null )
      {
         String prefix = (info.isIs()) ? "is" : "get";
         sig = methodSignature(info.getType(), prefix + info.getName(), null);
      }
      return sig;
   }

   /**
    * Generates a signature string for an attribute setter method using standard rules
    */
   public static String setterSignature(MBeanAttributeInfo info)
   {
      if (null == info)
      {
         throw new IllegalArgumentException("MBeanAttributeInfo cannot be null");
      }

      String sig = null;
      if( info instanceof ModelMBeanAttributeInfo )
      {
         ModelMBeanAttributeInfo minfo = (ModelMBeanAttributeInfo) info;
         Descriptor desc = minfo.getDescriptor();
         String methodName = (String) desc.getFieldValue(ModelMBeanConstants.SET_METHOD);
         String[] typeSig = {info.getType()};
         if( methodName != null )
            sig = methodSignature(Void.TYPE.getName(), methodName, typeSig);
      }
      if( sig == null )
      {
         String[] typeSig = {info.getType()};
         sig = methodSignature(Void.TYPE.getName(), "set" + info.getName(), typeSig);
      }
      return sig;
   }

   /**
    * Generates a signature string using the operation info.
    */
   public static String operationSignature(MBeanOperationInfo info)
   {
      if (null == info)
      {
         throw new IllegalArgumentException("MBeanOperationInfo cannot be null");
      }

      MBeanParameterInfo[] params = info.getSignature();
      String[] signature = new String[params.length];
      for (int i = 0; i < signature.length; i++)
      {
         signature[i] = params[i].getType();
      }
      return methodSignature(info.getReturnType(), info.getName(), signature);
   }

   /**
    * Generates a signature string using a Method object.
    */
   public static String methodSignature(Method method)
   {
      if (null == method)
      {
         throw new IllegalArgumentException("Method cannot be null");
      }

      Class[] paramtypes = method.getParameterTypes();
      String[] signature = new String[paramtypes.length];
      for (int i = 0; i < signature.length; i++)
      {
         signature[i] = paramtypes[i].getName();
      }
      return methodSignature(method.getReturnType().getName(), method.getName(), signature);
   }

   /**
    * Generates a signature string using the supplied signature arguments.
    */
   public static String methodSignature(String returnType, String name, String[] signature)
   {
      if (null == returnType)
      {
         throw new IllegalArgumentException("returnType cannot be null");
      }
      if (null == name)
      {
         throw new IllegalArgumentException("method name cannot be null");
      }

      StringBuffer buf = new StringBuffer(returnType).append(';').append(name);
      if (null == signature)
      {
         return buf.toString();
      }

      for (int i = 0; i < signature.length; i++)
      {
         buf.append(';').append(signature[i]); // the ; char ensures uniqueness
      }

      return buf.toString();
   }

   /** Used to see if a ModelMBean has the operation in question.
    * 
    * @param info
    * @param mbean
    * @return The mbean method if found, null otherwise
    */ 
   public static Method lookupOperation(MBeanOperationInfo info, Object mbean)
   {
      Class mbeanClass = mbean.getClass();
      Method m = null;
      try
      {
         ClassLoader loader = mbeanClass.getClassLoader();
         MBeanParameterInfo[] params = info.getSignature();
         Class[] signature = new Class[params.length];
         for (int i = 0; i < signature.length; i++)
         {
            Class type = loader.loadClass(params[i].getType());
            signature[i] = type;
         }
         m = mbeanClass.getMethod(info.getName(), signature);
         // The modelmbean does not provide methods from object
         if (Object.class.equals(m.getDeclaringClass()))
            m = null;
      }
      catch(Exception e)
      {
      }

      return m;
   }
   
   // Constructors --------------------------------------------------
   
   /**
    * Constructs a mapper by reflecting on the class.
    */
   public MethodMapper(Class resourceClass)
   {
      map = createMap(resourceClass);
   }

   
   // Public --------------------------------------------------------
   
   /**
    * Return a method matching the signature defined in the operation info
    */
   public Method lookupOperation(MBeanOperationInfo info)
   {
      if (null == info)
      {
         throw new IllegalArgumentException("MBeanOperationInfo cannot be null");
      }

      return (Method) map.get(operationSignature(info));
   }

   /**
    * Return a method matching the getter signature expected for an attribute.
    */
   public Method lookupGetter(MBeanAttributeInfo info)
   {
      if (null == info)
      {
         throw new IllegalArgumentException("MBeanAttributeInfo cannot be null");
      }

      return (Method) map.get(getterSignature(info));
   }

   /**
    * Return a method matching the setter signature expected for an attribute
    */
   public Method lookupSetter(MBeanAttributeInfo info)
   {
      if (null == info)
      {
         throw new IllegalArgumentException("MBeanAttributeInfo cannot be null");
      }

      return (Method) map.get(setterSignature(info));
   }

   /**
    * Return a method matching the specified signature
    */
   public Method lookupMethod(String returnType, String name, String[] signature)
   {
      if (null == returnType)
      {
         throw new IllegalArgumentException("returnType cannot be null");
      }

      if (null == name)
      {
         throw new IllegalArgumentException("method name cannot be null");
      }

      return (Method) map.get(methodSignature(returnType, name, signature));
   }

   public String toString()
   {
      return map.toString();
   }
   
   // Protected -----------------------------------------------------
   
   /**
    * creates the signature string to Method HashMap.
    */
   protected HashMap createMap(Class resourceClass)
   {
      HashMap cmap = new HashMap();
      if (resourceClass != null)
      {
         Method[] methods = resourceClass.getMethods();
         for (int i = 0; i < methods.length; i++)
         {
            Method method = methods[i];
            cmap.put(methodSignature(method), method);
         }
      }
      return cmap;
   }
   
}
