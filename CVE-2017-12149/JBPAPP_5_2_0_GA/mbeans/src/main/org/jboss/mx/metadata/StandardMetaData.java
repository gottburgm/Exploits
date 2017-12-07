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
package org.jboss.mx.metadata;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationBroadcaster;

/**
 * This metadata builder implementation builds a MBean info based on the
 * naming rules of the Standard MBeans. The MBean server uses this builder
 * to generate the metadata for Standard MBeans.  <p>
 *
 * In cooperation with the 
 * {@link MBeanInfoConversion#toModelMBeanInfo MBeanInfoConversion} class you
 * can use this builder as a migration tool from Standard to Model MBeans, or
 * for cases where you want the management interface be based on a compile-time
 * type safe interface. It is also possible to subclass this builder
 * implementation to extend it to support more sophisticated introspection rules
 * such as adding descriptors to management interface elements.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 * @author  <a href="mailto:thomas.diesler@jboss.com">Thomas Diesler</a>.
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>.
 */
public class StandardMetaData extends AbstractBuilder
{

   // Attributes ----------------------------------------------------
   
   /**
    * The MBean object instance.
    * Can be <tt>null</tt>.
    */
   private Object mbeanInstance = null;
   
   /**
    * The class of the MBean instance. 
    */
   private Class mbeanClass    = null;
   
   /**
    * The interface used as a basis for constructing the MBean metadata.
    */
   private Class mbeanInterface    = null;


   // Static --------------------------------------------------------

   /**
    * Locates an interface for a class that matches the Standard MBean naming
    * convention.
    *
    * @param   mbeanClass  the class to investigate
    *
    * @return  the Standard MBean interface class or <tt>null</tt> if not found
    */
   public static Class findStandardInterface(Class mbeanClass)
   {
      Class concrete = mbeanClass;
      Class stdInterface = null;
      while (null != concrete)
      {
         stdInterface = findStandardInterface(concrete, concrete.getInterfaces());
         if (null != stdInterface)
         {
            return stdInterface;
         }
         concrete = concrete.getSuperclass();
      }
      return null;
   }

   private static Class findStandardInterface(Class concrete, Class[] interfaces)
   {
      String stdName = concrete.getName() + "MBean";
      Class retval = null;

      // look to see if this class implements MBean std interface
      for (int i = 0; i < interfaces.length; ++i)
      {
         if (interfaces[i].getName().equals(stdName))
         {
            retval = interfaces[i];
            break;
         }
      }

      return retval;
   }

   
   // Constructors --------------------------------------------------
   
   /**
    * Initializes the Standard metadata builder. The JMX metadata is based
    * on the class of the given resource instance.
    * 
    * @param   mbeanInstance  MBean instance
    */
   public StandardMetaData(Object mbeanInstance) throws NotCompliantMBeanException
   {
      this(mbeanInstance.getClass());
      this.mbeanInstance = mbeanInstance;
   }

   /**
    * Initializes the Standard metadata builder. The JMX metadata is based
    * on the given class.
    *
    * @param   mbeanClass  resource class that implements an interface
    *                      adhering to the Standard MBean naming conventions
    */
   public StandardMetaData(Class mbeanClass)  throws NotCompliantMBeanException
   {
      this.mbeanClass     = mbeanClass;
      this.mbeanInterface = StandardMetaData.findStandardInterface(mbeanClass);
      if (this.mbeanInterface == null)
         throw new NotCompliantMBeanException("Cannot obtain management interface for: " + mbeanClass);
   }

   /**
    * Initializes the Standard metadata builder. The JMX metadata is based
    * on the passed mbean interface.
    * 
    * @param   mbInstance  MBean instance
    * @param   mbInterface the management interface
    */
   public StandardMetaData(Object mbInstance, Class mbInterface) throws NotCompliantMBeanException
   {
      this.mbeanInstance = mbInstance;
      this.mbeanClass = mbInstance.getClass();
      this.mbeanInterface = mbInterface;

      // search for it
      if (this.mbeanInterface == null)
         this.mbeanInterface = StandardMetaData.findStandardInterface(mbeanClass);

      if (this.mbeanInterface == null)
         throw new NotCompliantMBeanException("Cannot obtain management interface for: " + mbeanClass);
      if (this.mbeanInterface.isInterface() == false)
         throw new NotCompliantMBeanException("Management interface is not an interface: " + mbeanInterface);
   }

   /**
    * Retrieve the management interface
    */
   public Class getMBeanInterface()
   {
      return mbeanInterface;
   }
   
   // MetaDataBuilder implementation --------------------------------

   public MBeanInfo build() throws NotCompliantMBeanException
   {
      try
      {
         // First check the mbean instance implements the interface
         if (mbeanInterface == null)
            throw new NotCompliantMBeanException("The mbean does not implement a management interface");
         if (mbeanInstance != null && mbeanInterface.isInstance(mbeanInstance) == false)
            throw new NotCompliantMBeanException("The mbean does not implement its management interface " +
                                                 mbeanInterface.getName());

         // First build the constructors
         Constructor[] constructors = mbeanClass.getConstructors();
         MBeanConstructorInfo[] constructorInfo = new MBeanConstructorInfo[constructors.length];
         for (int i = 0; i < constructors.length; ++i)
         {
            constructorInfo[i] = new MBeanConstructorInfo("MBean Constructor.", constructors[i]);
         }

         // Next we have to figure out how the methods in the mbean class map
         // to attributes and operations
         Method[] methods = mbeanInterface.getMethods();
         HashMap getters = new HashMap();
         HashMap setters = new HashMap();

         HashMap operInfo = new HashMap();
         List attrInfo = new ArrayList();

         for (int i = 0; i < methods.length; ++i)
         {
            String methodName = methods[i].getName();
            Class[] signature = methods[i].getParameterTypes();
            Class returnType  = methods[i].getReturnType();

            if (methodName.startsWith("set") && methodName.length() > 3 
                    && signature.length == 1 && returnType == Void.TYPE)
            {
               String key = methodName.substring(3, methodName.length());
               Method setter = (Method) setters.get(key);
               if (setter != null && setter.getParameterTypes()[0].equals(signature[0]) == false)
               {
                  throw new IntrospectionException("overloaded type for attribute set: " + key);
               }
               setters.put(key, methods[i]);
            }
            else if (methodName.startsWith("get") && methodName.length() > 3 
                         && signature.length == 0 && returnType != Void.TYPE)
            {
               String key = methodName.substring(3, methodName.length());
               Method getter = (Method) getters.get(key);
               if (getter != null && getter.getName().startsWith("is"))
               {
                  throw new IntrospectionException("mixed use of get/is for attribute " + key);
               }
               getters.put(key, methods[i]);
            }
            else if (methodName.startsWith("is") && methodName.length() > 2 
                        && signature.length == 0 && isBooleanReturn(returnType))
            {
               String key = methodName.substring(2, methodName.length());
               Method getter = (Method) getters.get(key);
               if (getter != null && getter.getName().startsWith("get"))
               {
                  throw new IntrospectionException("mixed use of get/is for attribute " + key);
               }
               getters.put(key, methods[i]);
            }
            else
            {
               MBeanOperationInfo info = new MBeanOperationInfo("MBean Operation.", methods[i]);
               operInfo.put(getSignatureString(methods[i]), info);
            }
         }

         Object[] keys = getters.keySet().toArray();
         for (int i = 0; i < keys.length; ++i)
         {
            String attrName = (String) keys[i];
            Method getter = (Method) getters.remove(attrName);
            Method setter = (Method) setters.remove(attrName);
            MBeanAttributeInfo info = new MBeanAttributeInfo(attrName, "MBean Attribute.", getter, setter);
            attrInfo.add(info);
         }

         Iterator it = setters.keySet().iterator();
         while (it.hasNext())
         {
            String attrName = (String) it.next();
            Method setter = (Method) setters.get(attrName);
            MBeanAttributeInfo info = new MBeanAttributeInfo(attrName, "MBean Attribute.", null, setter);
            attrInfo.add(info);
         }

         // save away the attribute and operation info objects
         MBeanAttributeInfo[] attributeInfo = (MBeanAttributeInfo[]) attrInfo.toArray(new MBeanAttributeInfo[0]);
         MBeanOperationInfo[] operationInfo = (MBeanOperationInfo[]) operInfo.values().toArray(new MBeanOperationInfo[0]);

         // if the builder was initialized with the resource instance, check if
         // it is a notification broadcaster, and add the appropriate notifications
         // to the interface.
         MBeanNotificationInfo[] notifications = null;
         if (mbeanInstance instanceof NotificationBroadcaster)
         {
            notifications = ((NotificationBroadcaster) mbeanInstance).getNotificationInfo();
         }
         else
         {
            notifications = new MBeanNotificationInfo[0];
         }

         return new MBeanInfo(mbeanClass.getName(), "Management Bean.",
                              attributeInfo, constructorInfo, operationInfo, notifications);

      }
      catch (IntrospectionException e)
      {
         throw new NotCompliantMBeanException(e.getMessage());
      }
   }

   /**
    * JMX standard specifies that only "boolean isX()" style methods
    * represent attributes. "Boolean isX()" methods map to operations.
    */
   private boolean isBooleanReturn(Class returnType)
   {
      return returnType == Boolean.TYPE;
   }

   protected String getSignatureString(Method method)
   {
      String name = method.getName();
      Class[] signature = method.getParameterTypes();
      StringBuffer buffer = new StringBuffer(512);
      buffer.append(name);
      buffer.append("(");
      if (signature != null)
      {
         for (int i = 0; i < signature.length; i++)
         {
            buffer.append(signature[i].getName());
            if (i < signature.length-1)
               buffer.append(",");
         }
      }
      buffer.append(")");
      return buffer.toString();
   }
}

