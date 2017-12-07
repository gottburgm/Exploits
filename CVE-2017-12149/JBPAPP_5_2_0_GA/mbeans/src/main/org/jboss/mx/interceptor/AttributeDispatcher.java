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
package org.jboss.mx.interceptor;

import java.lang.reflect.Method;

import javax.management.Descriptor;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ServiceNotFoundException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;

import org.jboss.mx.modelmbean.ModelMBeanConstants;
import org.jboss.mx.server.Invocation;
import org.jboss.mx.server.MBeanInvoker;

/** A dispatcher used by the AbstractMBeanInvoker class for the attribute
 * getter and setter dispatch.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81026 $
 */
public class AttributeDispatcher
   extends ReflectedDispatcher
{
   private Method getter;
   private Method setter;

   public AttributeDispatcher(Method getter, Method setter, boolean dynamic)
   {
      super(dynamic);
      setName("Attribute Dispatcher");
      this.getter = getter;
      this.setter = setter;
   }
   
   /** Dispatch the attribute set or get. A get is identified by a dispatch
    * with a null args value.
    * 
    * @return the result of the attribute accessor invocation
    * @throws InvocationException
    */ 
   public Object invoke(Invocation invocation) throws Throwable
   {
      Object target = invocation.getTarget();
      
      Object value = null;
      Object[] args = invocation.getArgs();
      // Getter
      if( args == null )
      {
         Method getMethod = getter;
         if (dynamic)
         {
            Descriptor d = invocation.getDescriptor();
            if (d != null)
            {
               Object descriptorTarget = d.getFieldValue(ModelMBeanConstants.TARGET_OBJECT);
               if (descriptorTarget != null)
               {
                  String targetType = (String) d.getFieldValue(ModelMBeanConstants.TARGET_TYPE);
                  if (ModelMBeanConstants.OBJECT_REF.equalsIgnoreCase(targetType) == false)
                     throw new InvalidTargetObjectTypeException("Target type is " + targetType);
                  target = descriptorTarget;
               }
               String getMethodString = (String) d.getFieldValue(ModelMBeanConstants.GET_METHOD);
               if (getMethodString != null && (getMethod == null || getMethodString.equals(getMethod.getName()) == false))
               {
                  MBeanInvoker invoker = invocation.getInvoker();
                  Object object = invoker.invoke(getMethodString, new Object[0], new String[0]);
                  checkAssignable(getMethodString, invocation.getAttributeTypeClass(), object);
                  return object;
               }
            }
         }
         if (target == null)
            throw new MBeanException(new ServiceNotFoundException("No Target"));
         try
         {
            value = getMethod.invoke(target, args);
         }
         catch (Throwable t)
         {
            handleInvocationExceptions(t);
            return null;
         }
      }
      // Setter
      else
      {
         Method setMethod = setter;
         if (dynamic)
         {
            Descriptor d = invocation.getDescriptor();
            if (d != null)
            {
               Object descriptorTarget = d.getFieldValue(ModelMBeanConstants.TARGET_OBJECT);
               if (descriptorTarget != null)
               {
                  String targetType = (String) d.getFieldValue(ModelMBeanConstants.TARGET_TYPE);
                  if (ModelMBeanConstants.OBJECT_REF.equalsIgnoreCase(targetType) == false)
                     throw new InvalidTargetObjectTypeException("Target type is " + targetType);
                  target = descriptorTarget;
               }
               String setMethodString = (String) d.getFieldValue(ModelMBeanConstants.SET_METHOD);
               if (setMethodString != null && (setMethod == null || setMethodString.equals(setMethod.getName()) == false))
               {
                  MBeanInvoker invoker = invocation.getInvoker();
                  return invoker.invoke(setMethodString, new Object[] { args[0] }, new String[] { invocation.getAttributeType() });
               }
            }
         }
         if (target == null)
            throw new MBeanException(new ServiceNotFoundException("No Target"));
         try
         {
            value = setMethod.invoke(target, args);
         }
         catch (Throwable t)
         {
            handleInvocationExceptions(t);
            return null;
         }
      }
      return value;
   }
   
   protected void checkAssignable(String context, Class clazz, Object value) throws InvalidAttributeValueException, ClassNotFoundException
   {
      if (value != null && clazz.isAssignableFrom(value.getClass()) == false)
         throw new InvalidAttributeValueException(context + " has class " + value.getClass() + " loaded from " + value.getClass().getClassLoader() +
            " that is not assignable to attribute class " + clazz + " loaded from " + clazz.getClassLoader());
   }
}
   