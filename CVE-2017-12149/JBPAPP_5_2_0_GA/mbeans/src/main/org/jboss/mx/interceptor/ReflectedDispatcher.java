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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.management.Descriptor;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;

import org.jboss.mx.modelmbean.ModelMBeanConstants;
import org.jboss.mx.server.Invocation;

/**
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81026 $
 *   
 */
public class ReflectedDispatcher extends AbstractInterceptor
{
   
   // Static --------------------------------------------------------
  
   // Attributes ----------------------------------------------------
   
   protected Method method = null;
   
   protected boolean dynamic;
   
   // Constructors --------------------------------------------------
   
   public ReflectedDispatcher()
   {
      super("Reflected Dispatcher");
   }
   
   public ReflectedDispatcher(boolean dynamic)
   {
      this();
      this.dynamic = dynamic;
   }
   
   public ReflectedDispatcher(Method m, boolean dynamic)
   {
      this(dynamic);
      this.method = m;
   }
  
   // Dispatcher implementation -------------------------------------
   
   public Object invoke(Invocation invocation) throws Throwable
   {
      Method invokeMethod = method;
      Object target = invocation.getTarget();
      String operationName = invocation.getName(); 
      if (dynamic)
      {
         // See whether we have a fqn
         String opName = operationName;
         String opClass = null; 
         int dot = opName.lastIndexOf('.');
         if (dot != -1)
         {
            opClass = operationName.substring(0, dot);
            opName = operationName.substring(dot+1);
         }
         
         // Does the descriptor have a target?
         Descriptor descriptor = invocation.getDescriptor();
         if (descriptor != null)
         {
            Object descriptorTarget = descriptor.getFieldValue(ModelMBeanConstants.TARGET_OBJECT);
            if (descriptorTarget != null)
            {
               String targetType = (String) descriptor.getFieldValue(ModelMBeanConstants.TARGET_TYPE);
               if (ModelMBeanConstants.OBJECT_REF.equalsIgnoreCase(targetType) == false)
                  throw new InvalidTargetObjectTypeException("Target type is " + targetType);
               target = descriptorTarget;

               // Determine the method
               Class clazz = null;
               String className = (String) descriptor.getFieldValue(ModelMBeanConstants.CLASS);
               if (className == null)
                  className = opClass;
               if (className == null)
                  clazz = target.getClass();
               else
               {
                  try
                  {
                     if (clazz == null)
                        clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                  }
                  catch (Exception e)
                  {
                     throw new ReflectionException(e, "Error loading class for operation " + opName);
                  }
               }
               Class[] sig;
               try
               {
                  sig = invocation.getSignatureClasses();
               }
               catch (Exception e)
               {
                  throw new ReflectionException(e, "Error loading signature classes for operation " + opName);
               }
               try
               {
                  invokeMethod = clazz.getDeclaredMethod(opName, sig);
               }
               catch (Exception e)
               {
                  throw new ReflectionException(e, "Error getting method for operation " + opName);
               }
            }
         }
      }
      
      if (target == null)
      {
         String msg = "Failed to find method for operation: " + invocation
            + " on resource: " + invocation.getInvoker().getResource()
            + " objectName: " + invocation.getInvoker().getObjectName();
         throw new ReflectionException(new NullPointerException(msg));
      }

      try
      {
         Object[] args = invocation.getArgs();
         return invokeMethod.invoke(target, args);
      }
      catch (NullPointerException e)
      {
         throw new NullPointerException("Error in operation=" + operationName + " method=" + method + " target=" + target);
      }
      catch (Throwable t)
      {
         handleInvocationExceptions(t);
         return null;
      }
   }

   // Protected -----------------------------------------------------
   protected void handleInvocationExceptions(Throwable t) throws Throwable
   {      
      // the invoked method threw an exception
      if (t instanceof InvocationTargetException)
      {
         t = ((InvocationTargetException) t).getTargetException();
         if (t instanceof RuntimeOperationsException)
            throw (RuntimeOperationsException) t;
         else if (t instanceof RuntimeException)
            throw new RuntimeMBeanException((RuntimeException) t);
         else if (t instanceof Error)
            throw new RuntimeErrorException((Error) t);
         else if (t instanceof Exception)
            throw new MBeanException((Exception) t);
         else
            throw t;
      }
      else if (t instanceof Exception)
         throw new ReflectionException((Exception) t);
      else if (t instanceof Error)
         throw new RuntimeErrorException((Error) t);
      else
         throw t;
   }
      
}
      

