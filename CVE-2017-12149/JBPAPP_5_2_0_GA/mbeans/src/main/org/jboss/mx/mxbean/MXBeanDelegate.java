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
import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanInfo;

import org.jboss.logging.Logger;
import org.jboss.mx.server.ExceptionHandler;

/**
 * MXBeanDelegate.
 * 
 * FIXME: Reflection madness
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class MXBeanDelegate extends NotificationBroadcasterSupport implements DynamicMBean, MBeanRegistration, NotificationEmitter
{
   /** The logger */
   private static final Logger log = Logger.getLogger(MXBeanDelegate.class);

   /** The implementation object */
   private Object implementation;

   /** The management interface */
   private Class mbeanInterface;

   /** The cached mbeaninfo */
   private OpenMBeanInfo cachedMBeanInfo;
   
   /** The attribute mapping */
   private Map<String, OpenMBeanAttributeInfo> attributeMapping;
   
   /** The getters */
   private Map<String, Method> getters;
   
   /** The setters */
   private Map<String, Method> setters;
   
   /** The operatinns */
   private Map<String, Method> operations;

   /**
    * Construct a DynamicMBean from the given implementation object
    * and the passed management interface class.
    *
    * @param implementation the object implementing the mbean
    * @param mbeanInterface the management interface of the mbean
    * @exception IllegalArgumentException for a null implementation
    * @exception NotCompliantMBeanException if the management interface
    *            does not follow the JMX design patterns or the implementation
    *            does not implement the interface
    */
   public MXBeanDelegate(Object implementation, Class mbeanInterface) throws NotCompliantMBeanException
   {
      this.implementation = implementation;
      this.mbeanInterface = mbeanInterface;
   }

   /**
    * Construct a DynamicMBean from this object
    * and the passed management interface class.<p>
    *
    * Used in subclassing
    *
    * @param mbeanInterface the management interface of the mbean
    * @exception NotCompliantMBeanException if the management interface
    *            does not follow the JMX design patterns or this
    *            does not implement the interface
    */
   protected MXBeanDelegate(Class mbeanInterface) throws NotCompliantMBeanException
   {
      this.implementation = this;
      this.mbeanInterface = mbeanInterface;
   }

   /**
    * Retrieve the implementation object
    *
    * @return the implementation
    */
   public Object getImplementation()
   {
      return implementation;
   }

   /**
    * Replace the implementation object
    *
    * @todo make this work after the mbean is registered
    * @param implementation the new implementation
    * @exception IllegalArgumentException for a null parameter
    * @exception NotCompliantMBeanException if the new implementation
    *            does not implement the interface supplied at
    *            construction
    */
   public void setImplementation(Object implementation) throws NotCompliantMBeanException
   {
      if (implementation == null)
         throw new IllegalArgumentException("Null implementation");
      this.implementation = implementation;
   }

   /**
    * Retrieve the implementation class
    *
    * @return the class of the implementation
    */
   public Class getImplementationClass()
   {
      return implementation.getClass();
   }

   /**
    * Retrieve the management interface
    *
    * @return the management interface
    */
   public final Class getMBeanInterface()
   {
      return mbeanInterface;
   }

   public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException
   {
      try
      {
         OpenMBeanAttributeInfo attributeInfo = attributeMapping.get(attribute);
         if (attributeInfo == null)
            throw new AttributeNotFoundException(attribute);
         MBeanAttributeInfo mbeanAttributeInfo = (MBeanAttributeInfo) attributeInfo;
         if (mbeanAttributeInfo.isReadable() == false)
            throw new AttributeNotFoundException("Attribute is not readable: " + attribute);
         
         Method method = getters.get(attribute);
         if (method == null)
            throw new NoSuchMethodException("No method to get attribute: " + attribute);
         
         Object result = method.invoke(implementation, new Object[0]);
         
         return MXBeanUtils.construct(attributeInfo.getOpenType(), result, "Get attribute: " + attribute);
      }
      catch (Exception e)
      {
         JMException result = ExceptionHandler.handleException(e);
         if (result instanceof AttributeNotFoundException)
            throw (AttributeNotFoundException)result;
         if (result instanceof MBeanException)
            throw (MBeanException)result;
         if (result instanceof ReflectionException)
            throw (ReflectionException)result;
         throw new MBeanException(e, "Cannot get attribute: " + attribute);
      }
   }

   public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
   {
      try
      {
         String attributeName = attribute.getName();
         OpenMBeanAttributeInfo attributeInfo = attributeMapping.get(attributeName);
         if (attributeInfo == null)
            throw new AttributeNotFoundException(attributeName);
         MBeanAttributeInfo mbeanAttributeInfo = (MBeanAttributeInfo) attributeInfo;
         if (mbeanAttributeInfo.isWritable() == false)
            throw new AttributeNotFoundException("Attribute is not writable: " + attributeName);
         
         Method method = setters.get(attributeName);
         if (method == null)
            throw new NoSuchMethodException("No method to set attribute: " + attribute);

         Object value = MXBeanUtils.reconstruct(method.getGenericParameterTypes()[0], attribute.getValue(), method);
         
         method.invoke(implementation, new Object[] { value });
      }
      catch (Exception e)
      {
         JMException result = ExceptionHandler.handleException(e);
         if (result instanceof AttributeNotFoundException)
            throw (AttributeNotFoundException)result;
         if (result instanceof InvalidAttributeValueException)
            throw (InvalidAttributeValueException)result;
         if (result instanceof MBeanException)
            throw (MBeanException)result;
         if (result instanceof ReflectionException)
            throw (ReflectionException)result;
         throw new MBeanException(e, "Cannot set attribute: " + attribute);
      }
   }

   public AttributeList getAttributes(String[] attributes)
   {
      try
      {
         AttributeList attrList = new AttributeList(attributes.length);
         for (int i = 0; i < attributes.length; i++)
         {
            String name = attributes[i];
            Object value = getAttribute(name);
            attrList.add(new Attribute(name, value));
         }
         return attrList;
      }
      catch (Exception e)
      {
         JMException result = ExceptionHandler.handleException(e);
         // Why is this not throwing the same exceptions as getAttribute(String)
         throw new RuntimeException("Cannot get attributes", result);
      }
   }

   public AttributeList setAttributes(AttributeList attributes)
   {
      AttributeList result = new AttributeList(attributes.size());
      for (int i = 0; i < attributes.size(); ++i)
      {
         Attribute attr = (Attribute) attributes.get(i);
         String name = attr.getName();
         try
         {
            setAttribute(attr);
            result.add(new Attribute(name, attr.getValue()));
         }
         catch (Throwable t)
         {
            JMException e = ExceptionHandler.handleException(t);
            result.add(new Attribute(name, e));
         }
      }
      return result;
   }

   public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException
   {
      try
      {
         String signatureString = getSignatureString(actionName, signature);
         Method method = operations.get(signatureString);
         if (method == null)
            throw new NoSuchMethodException("Cannot find method for operation: " + signatureString);

         Object[] parameters = params;
         if (params.length > 0)
         {
            parameters = new Object[params.length];
            Type[] parameterTypes = method.getGenericParameterTypes();
            for (int i = 0; i < parameters.length; ++i)
               parameters[i] = MXBeanUtils.reconstruct(parameterTypes[i], params[i], method);
         }
         
         Object result = method.invoke(implementation, parameters);
         
         if (result == null)
            return null;
         Type returnType = method.getGenericReturnType();
         return MXBeanUtils.construct(returnType, result, method);
      }
      catch (Exception e)
      {
         JMException result = ExceptionHandler.handleException(e);
         if (result instanceof MBeanException)
            throw (MBeanException)result;
         if (result instanceof ReflectionException)
            throw (ReflectionException)result;
         throw new MBeanException(e, "Cannot invoke: " + actionName);
      }
   }

   public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception
   {
      return name;
   }

   public void postRegister(Boolean registrationDone)
   {
   }

   public void preDeregister() throws Exception
   {
   }

   public void postDeregister()
   {
   }

   public MBeanInfo getMBeanInfo()
   {
      OpenMBeanInfo info = getCachedMBeanInfo();
      if (info == null)
      {
         try
         {
            info = buildMBeanInfo();
            cacheMBeanInfo(info);
         }
         catch (NotCompliantMBeanException e)
         {
            log.error("Unexcepted exception", e);
            throw new IllegalStateException("Unexcepted exception " + e.toString());
         }

      }
      return (MBeanInfo) info;
   }

   /**
    * Retrieve the cached mbean info
    *
    * @return the cached mbean info
    */
   public OpenMBeanInfo getCachedMBeanInfo()
   {
      return cachedMBeanInfo;
   }

   /**
    * Sets the cached mbean info
    *
    * @param info the mbeaninfo to cache, can be null to erase the cache
    */
   public void cacheMBeanInfo(OpenMBeanInfo info)
   {
      cachedMBeanInfo = info;
      Map<String, OpenMBeanAttributeInfo> attributeMapping = new HashMap<String, OpenMBeanAttributeInfo>();
      MBeanAttributeInfo[] attributes = info.getAttributes();
      for (int i = 0; i < attributes.length; ++i)
      {
         OpenMBeanAttributeInfo attribute = (OpenMBeanAttributeInfo) attributes[i];
         attributeMapping.put(attribute.getName(), attribute);
      }
      this.attributeMapping = attributeMapping;

      try
      {
         HashMap<String, Method> getters = new HashMap<String, Method>();
         HashMap<String, Method> setters = new HashMap<String, Method>();

         HashMap<String, Method> operations = new HashMap<String, Method>();

         Method[] methods = implementation.getClass().getMethods();
         for (Method method : methods)
         {
            String methodName = method.getName();
            Type[] signature = method.getGenericParameterTypes();
            Type returnType = method.getGenericReturnType();

            if (methodName.startsWith("set") &&
                methodName.length() > 3 && 
                signature.length == 1 && 
                returnType == Void.TYPE)
            {
               String key = methodName.substring(3, methodName.length());
               Method setter = setters.get(key);
               if (setter != null && setter.getGenericParameterTypes()[0].equals(signature[0]) == false)
                  throw new RuntimeException("overloaded type for attribute set: " + key);
               setters.put(key, method);
            }
            else if (methodName.startsWith("get") &&
                     methodName.length() > 3 &&
                     signature.length == 0 &&
                     returnType != Void.TYPE)
            {
               String key = methodName.substring(3, methodName.length());
               Method getter = getters.get(key);
               if (getter != null && getter.getName().startsWith("is"))
                  throw new RuntimeException("mixed use of get/is for attribute " + key);
               getters.put(key, method);
            }
            else if (methodName.startsWith("is") &&
                     methodName.length() > 2 &&
                     signature.length == 0 &&
                     returnType == Boolean.TYPE)
            {
               String key = methodName.substring(2, methodName.length());
               Method getter = getters.get(key);
               if (getter != null && getter.getName().startsWith("get"))
                  throw new RuntimeException("mixed use of get/is for attribute " + key);
               getters.put(key, method);
            }
            else
            {
               operations.put(getSignatureString(method), method);
            }
         }
         this.getters = getters;
         this.setters = setters;
         this.operations = operations;
      }
      catch (RuntimeException e)
      {
         log.error("Error: ", e);
         throw e;
      }
      catch (Error e)
      {
         log.error("Error: ", e);
         throw e;
      }
   }

   /**
    * Builds a default MBeanInfo for this MBean, using the Management Interface specified for this MBean.
    *
    * While building the MBeanInfo, this method calls the customization hooks that make it possible for subclasses to
    * supply their custom descriptions, parameter names, etc...
    * 
    * @return the mbean info
    * @throws NotCompliantMBeanException when not a valid mbean
    */
   public OpenMBeanInfo buildMBeanInfo() throws NotCompliantMBeanException
   {
      if (implementation == null)
         throw new IllegalArgumentException("Null implementation");

      MXBeanMetaData metadata = new MXBeanMetaData(implementation, mbeanInterface);
      return (OpenMBeanInfo) metadata.build();
   }

   /**
    * Get a signature string for a method
    * 
    * @param method the method
    * @return the signature
    */
   private String getSignatureString(Method method)
   {
      String name = method.getName();
      Class[] signature = method.getParameterTypes();
      StringBuilder buffer = new StringBuilder(512);
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

   /**
    * Get a signature string for a method
    * 
    * @param operation the operation
    * @param signature the signature
    * @return the signature
    */
   private String getSignatureString(String operation, String[] signature)
   {
      StringBuilder buffer = new StringBuilder(512);
      buffer.append(operation);
      buffer.append("(");
      if (signature != null)
      {
         for (int i = 0; i < signature.length; i++)
         {
            buffer.append(signature[i]);
            if (i < signature.length-1)
               buffer.append(",");
         }
      }
      buffer.append(")");
      return buffer.toString();
   }
}
