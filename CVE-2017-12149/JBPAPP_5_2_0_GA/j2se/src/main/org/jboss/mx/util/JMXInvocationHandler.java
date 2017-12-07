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
package org.jboss.mx.util;

import java.io.Serializable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.util.HashMap;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;

/**
 * Invocation handler for MBean proxies.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81019 $
 *   
 */
public class JMXInvocationHandler 
      implements ProxyContext, InvocationHandler, Serializable
{  
   private static final long serialVersionUID = 3714728148040623702L;
   
   // Attributes -------------------------------------------------
   
   /**
    * Reference to the MBean server this proxy connects to.
    */
   protected MBeanServer server    = null;
   
   /**
    * The object name of the MBean this proxy represents.
    */
   protected ObjectName objectName = null;
   
   /**
    * Default exception handler for the proxy.
    */
   private ProxyExceptionHandler handler = new DefaultExceptionHandler();
   
   /**
    * MBean attribute meta data.
    */
   private HashMap attributeMap          = new HashMap();
   
   /**
    * Indicates whether Object.toString() should be delegated to the resource
    * or handled by the proxy.
    */
   private boolean delegateToStringToResource = false;

   /**
    * Indicates whether Object.equals() should be delegated to the resource
    * or handled by the proxy.
    */
   private boolean delegateEqualsToResource   = false;

   /**
    * Indicates whether Object.hashCode() should be delegated to the resource
    * or handled by the proxy.
    */
   private boolean delegateHashCodeToResource = false;
   
   
   // Constructors -----------------------------------------------
   
   /**
    * Constructs a new JMX MBean Proxy invocation handler.
    *
    * @param server  reference to the MBean server this proxy connects to
    * @param name    object name of the MBean this proxy represents
    *
    * @throws MBeanProxyCreationException wraps underlying JMX exceptions in
    *         case the proxy creation fails
    */
   public JMXInvocationHandler(MBeanServer server, ObjectName name) 
      throws MBeanProxyCreationException
   {
      try
      {
         if (server == null)
            throw new MBeanProxyCreationException("null agent reference");
            
         this.server     = server;
         this.objectName = name;
         
         MBeanInfo info = server.getMBeanInfo(objectName);
         MBeanAttributeInfo[] attributes = info.getAttributes();
         MBeanOperationInfo[] operations = info.getOperations();
         
         // collect the MBean attribute metadata for standard mbean proxies
         for (int i = 0; i < attributes.length; ++i)
            attributeMap.put(attributes[i].getName(), attributes[i]);
         
         // Check whether the target resource exposes the common object methods.
         // Dynamic Proxy will delegate these methods automatically to the
         // invoke() implementation.
         for (int i = 0; i < operations.length; ++i)
         {
            if (operations[i].getName().equals("toString") &&
                operations[i].getReturnType().equals("java.lang.String") &&
                operations[i].getSignature().length == 0)
            {
               delegateToStringToResource = true;  
            }
            
            else if (operations[i].getName().equals("equals") &&
                     operations[i].getReturnType().equals(Boolean.TYPE.getName()) &&
                     operations[i].getSignature().length == 1 &&
                     operations[i].getSignature() [0].getType().equals("java.lang.Object"))
            {
               delegateEqualsToResource = true;
            }
            
            else if (operations[i].getName().equals("hashCode") &&
                     operations[i].getReturnType().equals(Integer.TYPE.getName()) &&
                     operations[i].getSignature().length == 0)
            {
               delegateHashCodeToResource = true;  
            }
         }
      }
      catch (InstanceNotFoundException e)
      {
         throw new MBeanProxyCreationException("Object name " + name + " not found: " + e.toString());
      }
      catch (IntrospectionException e)
      {
         throw new MBeanProxyCreationException(e.toString());
      }
      catch (ReflectionException e)
      {
         throw new MBeanProxyCreationException(e.toString());
      }
   }
   
   
   // InvocationHandler implementation ---------------------------
   
   public Object invoke(Object proxy, Method method, Object[] args) 
      throws Exception
   {
      Class declaringClass = method.getDeclaringClass();
      
      // Handle methods from Object class. If the target resource exposes 
      // operation metadata with same signature then the invocations will be
      // delegated to the target. Otherwise this instance of invocation handler
      // will execute them.
      if (declaringClass == Object.class)
         return handleObjectMethods(method, args);
      
      // Check methods from ProxyContext interface. If invoked, delegate
      // to the context implementation part of this invocation handler.
      if (declaringClass == ProxyContext.class)
         return method.invoke(this, args);
      
      // Check methods from DynamicMBean interface. This allows the proxy
      // to be used in cases where the underlying metadata has changed (a la
      // Dynamic MBean).
      if (declaringClass == DynamicMBean.class)
         return handleDynamicMBeanInvocation(method, args);
      
      try 
      {
         String methodName = method.getName();
         
         // Assume a get/setAttribute convention on the typed proxy interface.
         // If the MBean metadata exposes a matching attribute then use the
         // MBeanServer attribute accessors to read/modify the value. If not,
         // fallback to MBeanServer.invoke() assuming this is an operation
         // invocation despite the accessor naming convention.
         
         // getter
         if (methodName.startsWith("get") && args == null)
         {
            String attrName = methodName.substring(3, methodName.length());
            
            // check that the metadata exists
            MBeanAttributeInfo info = (MBeanAttributeInfo)attributeMap.get(attrName);
            if (info != null)
            {
               String retType  = method.getReturnType().getName();
      
               // check for correct return type on the getter         
               if (retType.equals(info.getType())) 
               {
                  return server.getAttribute(objectName, attrName);
               }
            }
         }
         
         // boolean getter
         else if (methodName.startsWith("is") && args == null)
         {
            String attrName = methodName.substring(2, methodName.length());
            
            // check that the metadata exists
            MBeanAttributeInfo info = (MBeanAttributeInfo)attributeMap.get(attrName);
            if (info != null && info.isIs())
            {
               Class retType = method.getReturnType();
               
               // check for correct return type on the getter
               if (retType.equals(Boolean.class) || retType.equals(Boolean.TYPE))
               {
                  return server.getAttribute(objectName, attrName);
               }
            }
         }
         
         // setter
         else if (methodName.startsWith("set") && args != null && args.length == 1)
         {
            String attrName = methodName.substring(3, methodName.length());
            
            // check that the metadata exists
            MBeanAttributeInfo info = (MBeanAttributeInfo)attributeMap.get(attrName);
            if (info != null && method.getReturnType().equals(Void.TYPE))
            {
               ClassLoader cl = Thread.currentThread().getContextClassLoader();
               
               Class signatureClass = null;
               String classType     = info.getType();
               
               if (isPrimitive(classType))
                  signatureClass = getPrimitiveClass(classType);
               else
                  signatureClass = cl.loadClass(info.getType());
               
               if (signatureClass.isAssignableFrom(args[0].getClass()))
               {
                  server.setAttribute(objectName, new Attribute(attrName, args[0]));
                  return null;
               }
            }
         }

         String[] signature = null;
         
         if (args != null)
         {
            signature = new String[args.length];
            Class[] sign = method.getParameterTypes();
            
            for (int i = 0; i < sign.length; ++i)
               signature[i] = sign[i].getName();
         }
         
         return server.invoke(objectName, methodName, args, signature);
      }
      catch (InstanceNotFoundException e)
      {
         return getExceptionHandler().handleInstanceNotFound(this, e, method, args);
      }
      catch (AttributeNotFoundException e)
      {
         return getExceptionHandler().handleAttributeNotFound(this, e, method, args);
      }
      catch (InvalidAttributeValueException e)
      {
         return getExceptionHandler().handleInvalidAttributeValue(this, e, method, args);
      }
      catch (MBeanException e)
      {
         return getExceptionHandler().handleMBeanException(this, e, method, args);
      }
      catch (ReflectionException e)
      {
         return getExceptionHandler().handleReflectionException(this, e, method, args);
      }
      catch (RuntimeOperationsException e)
      {
         return getExceptionHandler().handleRuntimeOperationsException(this, e, method, args);
      }
      catch (RuntimeMBeanException e)
      {
         return getExceptionHandler().handleRuntimeMBeanException(this, e, method, args);
      }
      catch (RuntimeErrorException e)
      {
         return getExceptionHandler().handleRuntimeError(this, e, method, args);
      }
   }

   public ProxyExceptionHandler getExceptionHandler()
   {
      return handler;
   }
   
   
   // ProxyContext implementation -----------------------------------
   
   // The proxy provides an access point for the client to methods not part
   // of the MBean's management interface. It can be used to configure the
   // invocation (with context, client side interceptors, RPC), exception
   // handling, act as an access point to MBean server interface and so on.
      
   public void setExceptionHandler(ProxyExceptionHandler handler)
   {
      this.handler = handler;
   }
   
   public MBeanServer getMBeanServer() 
   {
      return server;
   }      

   public ObjectName getObjectName()
   {
      return objectName;
   }
   
   
   // Object overrides ----------------------------------------------
   
   public String toString() 
   {
      return "MBeanProxy for " + objectName + " (Agent ID: " + AgentID.get(server) + ")";
   }
   
   
   // Private -------------------------------------------------------
   
   private Object handleObjectMethods(Method method, Object[] args)
      throws InstanceNotFoundException, ReflectionException,
      IntrospectionException, MBeanException
   {
      if (method.getName().equals("toString"))
      {
         if (delegateToStringToResource)
            return server.invoke(objectName, "toString", null, null);
         else
            return toString();
      }
      
      else if (method.getName().equals("equals"))
      {
         if (delegateEqualsToResource)
         {
            return server.invoke(objectName, "equals", 
                                 new Object[] { args[0] },
                                 new String[] { "java.lang.Object" }
            );
         }
         else if (Proxy.isProxyClass(args[0].getClass()))
         {
            Proxy prxy = (Proxy)args[0];
            return new Boolean(this.equals(Proxy.getInvocationHandler(prxy)));
         }
         else
         {
            return new Boolean(this.equals(args[0]));
         }
      }
      
      else if (method.getName().equals("hashCode"))
      {
         if (delegateHashCodeToResource)
            return server.invoke(objectName, "hashCode", null, null);
         else  
            return new Integer(this.hashCode());
      }
      
      else throw new Error("Unexpected method invocation!");
   }
   
   private Object handleDynamicMBeanInvocation(Method method, Object[] args)
      throws InstanceNotFoundException, ReflectionException,
      IntrospectionException, MBeanException, AttributeNotFoundException,
      InvalidAttributeValueException
   {
      String methodName = method.getName();
      
      if (methodName.equals("setAttribute"))
      {
         server.setAttribute(objectName, (Attribute)args[0]);
         return null;
      }
      else if (methodName.equals("setAttributes"))
         return server.setAttributes(objectName, (AttributeList)args[0]);
      else if (methodName.equals("getAttribute"))
         return server.getAttribute(objectName, (String)args[0]);
      else if (methodName.equals("getAttributes"))
         return server.getAttributes(objectName, (String[])args[0]);
      else if (methodName.equals("invoke"))
         return server.invoke(objectName, (String)args[0], (Object[])args[1], (String[])args[2]);
      else if (methodName.equals("getMBeanInfo"))
         return server.getMBeanInfo(objectName);
      
      else throw new Error("Unexpected method invocation!");
   }

   private boolean isPrimitive(String type)
   {
      if (type.equals(Integer.TYPE.getName()))   return true;
      if (type.equals(Long.TYPE.getName()))      return true;
      if (type.equals(Boolean.TYPE.getName()))   return true;
      if (type.equals(Byte.TYPE.getName()))      return true;
      if (type.equals(Character.TYPE.getName())) return true;
      if (type.equals(Short.TYPE.getName()))     return true;
      if (type.equals(Float.TYPE.getName()))     return true;
      if (type.equals(Double.TYPE.getName()))    return true;
      if (type.equals(Void.TYPE.getName()))      return true;
      
      return false;
   }

   private Class getPrimitiveClass(String type)
   {
      if (type.equals(Integer.TYPE.getName()))  return Integer.TYPE;
      if (type.equals(Long.TYPE.getName()))     return Long.TYPE;
      if (type.equals(Boolean.TYPE.getName()))  return Boolean.TYPE;
      if (type.equals(Byte.TYPE.getName()))     return Byte.TYPE;
      if (type.equals(Character.TYPE.getName()))return Character.TYPE;
      if (type.equals(Short.TYPE.getName()))    return Short.TYPE;
      if (type.equals(Float.TYPE.getName()))    return Float.TYPE;
      if (type.equals(Double.TYPE.getName()))   return Double.TYPE;
      if (type.equals(Void.TYPE.getName()))     return Void.TYPE;
      
      return null;
   }
   
}
      


