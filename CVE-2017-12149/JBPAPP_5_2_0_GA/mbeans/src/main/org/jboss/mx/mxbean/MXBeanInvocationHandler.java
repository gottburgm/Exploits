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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerConnection;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanInfo;
import javax.management.openmbean.OpenMBeanOperationInfo;
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenType;

import org.jboss.mx.util.JMXExceptionDecoder;

/**
 * MXBeanInvocationHandler.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class MXBeanInvocationHandler implements InvocationHandler, Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -2872014223541692039L;
   
   private static final Class[] LISTENER = new Class[] { NotificationListener.class };
   private static final Class[] TRIPLET = new Class[] { NotificationListener.class, NotificationFilter.class, Object.class };

   private static final Method EQUALS;
   private static final Method HASH_CODE;
   private static final Method TO_STRING;

   private static final Method ADD_NOTIFICATION_LISTENER;
   private static final Method GET_NOTIFICATION_INFO;
   private static final Method REMOVE_NOTIFICATION_LISTENER;
   private static final Method REMOVE_NOTIFICATION_LISTENER_TRIPLET;

   /** The connection */
   private MBeanServerConnection mbeanServerConnection;
   
   /** The interface */
   private Class<?> mxbeanInterface;
   
   /** The object name */
   private ObjectName objectName;

   /** The method mappings */
   private transient Map<Method, Action> mappings;
   
   /** The MBean Info */
   private transient OpenMBeanInfo mbeanInfo;

   static
   {
      try
      {
         ADD_NOTIFICATION_LISTENER = NotificationBroadcaster.class.getDeclaredMethod("addNotificationListener",  TRIPLET);
         GET_NOTIFICATION_INFO = NotificationBroadcaster.class.getDeclaredMethod("getNotificationInfo",  new Class[0]);
         REMOVE_NOTIFICATION_LISTENER = NotificationBroadcaster.class.getDeclaredMethod("removeNotificationListener",  LISTENER);
         REMOVE_NOTIFICATION_LISTENER_TRIPLET = NotificationEmitter.class.getDeclaredMethod("removeNotificationListener",  TRIPLET);
         EQUALS = Object.class.getDeclaredMethod("equals",  new Class[] { Object.class });
         HASH_CODE = Object.class.getDeclaredMethod("hashCode",  new Class[0]);
         TO_STRING = Object.class.getDeclaredMethod("toString",  new Class[0]);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   /**
    * Create a new MXBeanInvocationHandler.
    * 
    * @param mbeanServerConnection the connection
    * @param mxbeanInterface the interface
    * @param objectName the object name
    */
   public MXBeanInvocationHandler(MBeanServerConnection mbeanServerConnection, Class<?> mxbeanInterface, ObjectName objectName)
   {
      if (mbeanServerConnection == null)
         throw new IllegalArgumentException("Null mbeanServerConnection");
      if (mxbeanInterface == null)
         throw new IllegalArgumentException("Null mxmbeanInterface");
      if (objectName == null)
         throw new IllegalArgumentException("Null objectName");

      this.mbeanServerConnection = mbeanServerConnection;
      this.mxbeanInterface = mxbeanInterface;
      this.objectName = objectName;
   }

   /**
    * Get the mbeanServerConnection.
    * 
    * @return the mbeanServerConnection.
    */
   public MBeanServerConnection getMBeanServerConnection()
   {
      return mbeanServerConnection;
   }

   /**
    * Get the mxbeanInterface.
    * 
    * @return the mxbeanInterface.
    */
   public Class<?> getMXBeanInterface()
   {
      return mxbeanInterface;
   }

   /**
    * Get the objectName.
    * 
    * @return the objectName.
    */
   public ObjectName getObjectName()
   {
      return objectName;
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      try
      {
         return getAction(proxy, method).perform(args);
      }
      catch (Throwable t)
      {
         throw JMXExceptionDecoder.decode(t);
      }
   }

   /**
    * Get the actions for this method
    * @param proxy the proxy
    * @param method the method
    * @return the action
    * @throws Throwable for any error
    */
   private Action getAction(Object proxy, Method method) throws Throwable
   {
      // Doesn't really matter if the mappings are
      // setup twice by two different threads, they are the same.
      if (mappings == null)
         mappings = getMappings(proxy);

      // Check the action
      Action result = mappings.get(method);
      if (result == null)
         throw new UnsupportedOperationException("Unknown method: " + method);

      // Return the result
      return result;
   }

   /**
    * Set up the mappings
    * 
    * @param proxy the proxy 
    * @return the mapping
    * @throws Throwable for any error
    */
   private Map<Method, Action> getMappings(Object proxy) throws Throwable
   {
      mbeanInfo = (OpenMBeanInfo) mbeanServerConnection.getMBeanInfo(objectName);
      Map<String, OpenMBeanAttributeInfo> attributesMap = new HashMap<String, OpenMBeanAttributeInfo>();
      MBeanAttributeInfo[] attributes = mbeanInfo.getAttributes();
      for (int i = 0; i < attributes.length; ++i)
      {
         OpenMBeanAttributeInfo openAttribute = (OpenMBeanAttributeInfo) attributes[i];
         attributesMap.put(openAttribute.getName(), openAttribute);
      }
      MBeanOperationInfo[] operations = mbeanInfo.getOperations();
      
      Map<Method, Action> result = new HashMap<Method, Action>();

      Class[] interfaces = proxy.getClass().getInterfaces();
      for (int i = 0; i < interfaces.length; ++i)
      {
         if (NotificationBroadcaster.class.isAssignableFrom(interfaces[i]))
         {
            result.put(ADD_NOTIFICATION_LISTENER, new AddNotificationListenerAction());
            result.put(GET_NOTIFICATION_INFO, new GetNotificationInfoAction());
            result.put(REMOVE_NOTIFICATION_LISTENER, new RemoveNotificationListenerAction());
            result.put(REMOVE_NOTIFICATION_LISTENER_TRIPLET, new RemoveNotificationListenerTripletAction());
         }
         else
         {
            Method[] methods = interfaces[i].getMethods();
            for (Method method : methods)
            {
               String methodName = method.getName();
               Class returnType = method.getReturnType();
               Class[] parameterTypes = method.getParameterTypes();

               // Getter
               if (methodName.startsWith("get")  && 
                   methodName.length() > 3 &&
                   Void.TYPE.equals(returnType) == false &&
                   parameterTypes.length == 0)
               {
                  String name = methodName.substring(3);
                  OpenMBeanAttributeInfo attribute = attributesMap.get(name);
                  if (attribute != null)
                  {
                     Type type = method.getGenericReturnType();
                     result.put(method, new GetAction(attribute, type));
                     continue;
                  }
               }
               // Getter (is)
               else if(methodName.startsWith("is")  && 
                     methodName.length() > 2 &&
                     Boolean.TYPE.equals(returnType) &&
                     parameterTypes.length == 0)
               {
                  String name = methodName.substring(2);
                  OpenMBeanAttributeInfo attribute = attributesMap.get(name);
                  if (attribute != null)
                  {
                     Type type = method.getGenericReturnType();
                     result.put(method, new GetAction(attribute, type));
                     continue;
                  }
               }
               // Setter
               else if(methodName.startsWith("set")  && 
                     methodName.length() > 3 &&
                     Void.TYPE.equals(returnType) &&
                     parameterTypes.length == 1)
               {
                  String name = methodName.substring(3);
                  OpenMBeanAttributeInfo attribute = attributesMap.get(name);
                  if (attribute != null)
                  {
                     result.put(method, new SetAction(attribute));
                     continue;
                  }
               }
               // Invoker
               OpenMBeanOperationInfo operation = findOperation(methodName, method.getGenericParameterTypes(), operations);
               if (operation != null)
               {
                  String[] signature = getSignature(method);
                  Type type = method.getGenericReturnType();
                  result.put(method, new InvokeAction(operation, signature, type));
               }
               else
               {
                  result.put(method, new InvalidAction(method));
               }
            }
         }
      }

      // Add the Object mappings
      result.put(EQUALS, new EqualsAction());
      result.put(HASH_CODE, new HashCodeAction());
      result.put(TO_STRING, new ToStringAction());
      
      return result;
   }

   private static OpenMBeanOperationInfo findOperation(String name, Type[] parameterTypes, MBeanOperationInfo[] operations)
   {
      OpenType[] signature = getSignature(parameterTypes);
      for (int i = 0; i < operations.length; ++i)
      {
         if (operations[i].getName().equals(name) == false)
            continue;
         MBeanParameterInfo[] parameters = operations[i].getSignature();
         boolean match = true;
         for (int p = 0; p < parameters.length && match; ++p)
         {
            OpenMBeanParameterInfo openMBeanParameterInfo = (OpenMBeanParameterInfo) parameters[p];
            if (signature[p].equals(openMBeanParameterInfo.getOpenType()) == false)
               match = false;
         }
         if (match)
            return (OpenMBeanOperationInfo) operations[i];
      }
      return null;
   }
   
   private static String[] getSignature(final Method method)
   {
      Class[] parameterTypes = method.getParameterTypes();
      String[] signature = new String[parameterTypes.length];
      for (int p = 0; p < parameterTypes.length; ++p)
          signature[p] = parameterTypes[p].getName();
      return signature;
   }
   
   private static OpenType[] getSignature(final Type[] parameterTypes)
   {
      OpenType[] signature = new OpenType[parameterTypes.length];
      for (int p = 0; p < parameterTypes.length; ++p)
          signature[p] = MXBeanUtils.getOpenType(parameterTypes[p]);
      return signature;
   }

   private interface Action
   {
      public Object perform(Object[] args) throws Throwable;
   }

   private class GetAction implements Action
   {
      private OpenMBeanAttributeInfo attribute;
      private Type type;

      public GetAction(OpenMBeanAttributeInfo attribute, Type type)
      {
         this.attribute = attribute;
         this.type = type;
      }

      public Object perform(Object[] args) throws Throwable
      {
         Object result = mbeanServerConnection.getAttribute(objectName, attribute.getName());
         
         return MXBeanUtils.reconstruct(attribute.getOpenType(), type, result, "Get attribute: " + attribute.getName());
      }
   }

   private class SetAction implements Action
   {
      private OpenMBeanAttributeInfo attribute;

      public SetAction(OpenMBeanAttributeInfo attribute)
      {
         this.attribute = attribute;
      }

      public Object perform(Object[] args) throws Throwable
      {
         Object value = MXBeanUtils.construct(attribute.getOpenType(), args[0], "Set attribute: " + attribute.getName());
         
         Attribute attr = new Attribute(attribute.getName(), value);
         mbeanServerConnection.setAttribute(objectName, attr);
         return null;
      }
   }

   private class InvokeAction implements Action
   {
      private OpenMBeanOperationInfo operation;
      private String[] signature;
      private Type type;

      public InvokeAction(OpenMBeanOperationInfo operation, String[] signature, Type type)
      {
         this.operation = operation;
         this.signature = signature;
         this.type = type;
      }

      public Object perform(Object[] args) throws Throwable
      {
         MBeanParameterInfo[] parameters = operation.getSignature();
         Object[] arguments = new Object[args.length];
         for (int i = 0; i < parameters.length; ++i)
         {
            OpenMBeanParameterInfo parameter = (OpenMBeanParameterInfo) parameters[i];
            arguments[i] = MXBeanUtils.construct(parameter.getOpenType(), args[i], operation.getName());
         }
         
         Object result = mbeanServerConnection.invoke(objectName, operation.getName(), arguments, signature);
         
         return MXBeanUtils.reconstruct(operation.getReturnOpenType(), type, result, operation.getName());
      }
   }

   private class InvalidAction implements Action
   {
      private Method method;
      
      public InvalidAction(Method method)
      {
         this.method = method;
      }
      
      public Object perform(Object[] args) throws Throwable
      {
         throw new UnsupportedOperationException(method + " is not mapped to the MBeanInfo operations for " + objectName);
      }
   }
   
   private class EqualsAction implements Action
   {
      public Object perform(Object[] args) throws Throwable
      {
         Object object = args[0];
         if (object == null || object instanceof Proxy == false)
            return false;
         InvocationHandler handler = Proxy.getInvocationHandler(object);
         if (handler instanceof MXBeanInvocationHandler == false)
            return false;
         MXBeanInvocationHandler other = (MXBeanInvocationHandler) handler;
         return mbeanServerConnection.equals(other.mbeanServerConnection) && objectName.equals(other.objectName);
      }
   }
   
   private class HashCodeAction implements Action
   {
      public Object perform(Object[] args) throws Throwable
      {
         return objectName.hashCode();
      }
   }
   
   private class ToStringAction implements Action
   {
      public Object perform(Object[] args) throws Throwable
      {
         return "MXBeanInvocationHandler(" + objectName + ")";
      }
   }
   
   private class AddNotificationListenerAction implements Action
   {
      public Object perform(Object[] args) throws Throwable
      {
         NotificationListener listener = (NotificationListener) args[0];
         NotificationFilter filter = (NotificationFilter) args[1];
         Object handback = args[2];
         mbeanServerConnection.addNotificationListener(objectName, listener, filter, handback);
         return null;
      }
   }

   private class GetNotificationInfoAction implements Action
   {
      public Object perform(Object[] args) throws Throwable
      {
         return mbeanServerConnection.getMBeanInfo(objectName).getNotifications();
      }
   }

   private class RemoveNotificationListenerAction implements Action
   {
      public Object perform(Object[] args) throws Throwable
      {
         NotificationListener listener = (NotificationListener) args[0];
         mbeanServerConnection.removeNotificationListener(objectName, listener);
         return null;
      }
   }

   private class RemoveNotificationListenerTripletAction implements Action
   {
      public Object perform(Object[] args) throws Throwable
      {
         NotificationListener listener = (NotificationListener) args[0];
         NotificationFilter filter = (NotificationFilter) args[1];
         Object handback = args[2];
         mbeanServerConnection.removeNotificationListener(objectName, listener, filter, handback);
         return null;
      }
   }
}
