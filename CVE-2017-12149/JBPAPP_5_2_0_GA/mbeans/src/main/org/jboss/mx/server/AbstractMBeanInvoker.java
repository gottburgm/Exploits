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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.Descriptor;
import javax.management.InvalidAttributeValueException;
import javax.management.JMRuntimeException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;

import org.jboss.logging.Logger;
import org.jboss.mx.interceptor.AttributeDispatcher;
import org.jboss.mx.interceptor.Interceptor;
import org.jboss.mx.interceptor.ReflectedDispatcher;
import org.jboss.mx.metadata.StandardMetaData;
import org.jboss.mx.modelmbean.ModelMBeanConstants;
import org.jboss.mx.server.InvocationContext.NullDispatcher;
import org.jboss.mx.server.registry.MBeanEntry;
import org.jboss.util.Strings;

/**
 * A base MBeanInvoker class that provides common state
 * 
 * @author <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>.
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>.
 * @version $Revision: 81026 $
 */
public abstract class AbstractMBeanInvoker
   implements MBeanInvoker
{
   /**
    * Used to propagate the MBeanEntry during the preRegister callback
    */
   static ThreadLocal preRegisterInfo = new ThreadLocal();

   // Attributes ----------------------------------------------------

   /**
    * The target object for this invoker.
    */
   private Object resource = null;
   /**
    * The mbean server register entry used for the TCL
    */
   protected MBeanEntry resourceEntry = null;

   /**
    * Whether this is a dynamic resource
    */
   protected boolean dynamicResource = true;

   /**
    * The metadata describing this MBean.
    */
   protected MBeanInfo info = null;

   protected Map attributeContextMap = new HashMap();
   protected Map operationContextMap = new HashMap();
   protected Map constructorContextMap = new HashMap();

   protected InvocationContext getMBeanInfoCtx = null;
   protected InvocationContext preRegisterCtx = null;
   protected InvocationContext postRegisterCtx = null;
   protected InvocationContext preDeregisterCtx = null;
   protected InvocationContext postDeregisterCtx = null;

   // TODO: allow to config invoker specific logs
   //     : multitarget mbean for invoker + log?

   protected Logger log = Logger.getLogger(AbstractMBeanInvoker.class);

   /**
    * The MBeanServer passed in to preRegister
    */

   private MBeanServer server;

   /**
    * Set the MBeanEntry thread local value.
    * @param entry - the entry that will be used on successful registration
    */
   public static void setMBeanEntry(MBeanEntry entry)
   {
      preRegisterInfo.set(entry);
   }

   /**
    * An accessor for the MBeanEntry thread local
    * @return
    */
   public static MBeanEntry getMBeanEntry()
   {
      return (MBeanEntry) preRegisterInfo.get();
   }
   // Constructors --------------------------------------------------

   /**
    * Constructs a new invoker.
    */
   public AbstractMBeanInvoker()
   {
   }

   /**
    * Constructs a new invoker with a given target resource.
    */
   public AbstractMBeanInvoker(Object resource)
   {
      this.resource = resource;
   }

   /**
    * Constructs an invoker with the target resource entry.
    * @param resourceEntry
    */
   public AbstractMBeanInvoker(MBeanEntry resourceEntry)
   {
      this.resourceEntry = resourceEntry;
      this.resource = resourceEntry.getResourceInstance();
   }

   // DynamicMBean implementation -----------------------------------

   /**
    * Invokes the target resource. The default invocation used by this invoker
    * implement sends the invocation through a stack of interceptors before
    * reaching the target method.
    * @param operationName name of the target method
    * @param args argumetns for the target method
    * @param signature signature of the target method
    * @throws MBeanException if the target method raised a hecked exception
    * @throws ReflectionException if there was an error trying to resolve or
    * invoke the target method
    * @throws RuntimeMBeanException if the target method raised an unchecked
    * exception
    */
   public Object invoke(String operationName, Object[] args, String[] signature)
      throws MBeanException, ReflectionException
   {

      // TODO:  __JBOSSMX_INVOCATION

      if (operationName == null)
         throw new ReflectionException(new IllegalArgumentException("Null operation name"));
      
      // If we have dynamic capability, check for a dynamic invocation
      String opName = operationName;
      if (dynamicResource)
      {
         int dot = operationName.lastIndexOf('.');
         if (dot != -1)
         {
            if (dot < operationName.length() - 1)
               opName = operationName.substring(dot + 1);
         }
      }
      
      // get the server side invocation context
      OperationKey key = new OperationKey(opName, signature);
      InvocationContext ctx = (InvocationContext) operationContextMap.get(key);

      // if the server does not contain this context, we do not have the operation
      if (ctx == null)
      {
         // This is just stupid - the RI is fundamentally broken and hence the spec
         boolean operationExists = false;
         if (dynamicResource)
         {
            for (Iterator i = operationContextMap.keySet().iterator(); i.hasNext();)
            {
               OperationKey thisKey = (OperationKey) i.next();
               if (opName.equals(thisKey.keys[0]))
               {
                  operationExists = true;
                  break;
               }
            }
            if (operationExists)
               throw new ReflectionException(new NoSuchMethodException("Unable to find operation " + operationName +
                  getSignatureString(signature)));
         }
         throw new ReflectionException(new IllegalArgumentException("Unable to find operation " + operationName +
            getSignatureString(signature)));
      }

      // create the invocation object
      Invocation invocation = new Invocation();

      // copy the server's invocation context to the invocation
      invocation.addContext(ctx);

      // set the invocation's entry point
      invocation.setType(InvocationContext.OP_INVOKE);

      // Use the passed operation
      invocation.setName(operationName);
      
      // set the args
      invocation.setArgs(args);

      override(invocation);

      ClassLoader mbeanTCL = resourceEntry.getClassLoader();
      final ClassLoader ccl = TCLAction.UTIL.getContextClassLoader();
      boolean setCl = ccl != mbeanTCL && mbeanTCL != null;
      if (setCl)
      {
         TCLAction.UTIL.setContextClassLoader(mbeanTCL);
      }

      try
      {
         // the default invocation implementation will invoke each interceptor
         // declared in the invocation context before invoking the target method
         return invocation.invoke();
      }
      catch (MBeanException e)
      {
         throw e;
      }
      catch (ReflectionException e)
      {
         throw e;
      }
      catch (JMRuntimeException e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         rethrowAsMBeanException(t);
         return null;
      }

         // TODO: should be fixed by adding invocation return value object
      finally
      {
         Descriptor descriptor = invocation.getDescriptor();
         if (descriptor != null)
         {
            ctx.setDescriptor(descriptor);
            if (dynamicResource && ModelMBeanConstants.OPERATION_DESCRIPTOR.equals(descriptor.getFieldValue(ModelMBeanConstants.DESCRIPTOR_TYPE)))
            {
               ModelMBeanInfoSupport minfo = (ModelMBeanInfoSupport) info;
               minfo.setDescriptor(descriptor, ModelMBeanConstants.OPERATION_DESCRIPTOR);
            }
         }
         invocation.setArgs(null);
         invocation.setDescriptor(null);
         invocation.setDispatcher(null);

         if (setCl)
         {
            TCLAction.UTIL.setContextClassLoader(ccl);
         }
      }

   }

   /**
    * Returns an attribte value. The request for the value is forced through a
    * set of interceptors before the value is returned.
    * @param attribute attribute name
    * @return attribute value
    * @throws AttributeNotFoundException if the requested attribute is not part
    * of the MBean's management interface
    * @throws MBeanException if retrieving the attribute value causes an
    * application exception
    * @throws ReflectionException if there was an error trying to retrieve the
    * attribute value
    */
   public Object getAttribute(String attribute)
      throws AttributeNotFoundException, MBeanException, ReflectionException
   {
      // TODO:  __JBOSSMX_INVOCATION

      if (attribute == null)
         throw new RuntimeOperationsException(new IllegalArgumentException("Cannot get null attribute"));

      // lookup the server side invocation context
      InvocationContext ctx = (InvocationContext) attributeContextMap.get(attribute);

      // if we don't have a server side invocation context for the attribute,
      // it does not exist as far as we are concerned
      if (ctx == null)
         throw new AttributeNotFoundException("not found: " + attribute);

      if (ctx.isReadable() == false)
         throw new AttributeNotFoundException("Attribute '" + attribute + "' found, but it is not readable");

      // create the invocation object
      Invocation invocation = new Invocation();

      // copy the server's invocation context to the invocation
      invocation.addContext(ctx);

      // indicate the invocation access point was getAttribute() method
      invocation.setType(InvocationContext.OP_GETATTRIBUTE);
      invocation.setArgs(null);

      override(invocation);

      ClassLoader mbeanTCL = resourceEntry.getClassLoader();
      final ClassLoader ccl = TCLAction.UTIL.getContextClassLoader();
      boolean setCl = ccl != mbeanTCL && mbeanTCL != null;
      if (setCl)
      {
         TCLAction.UTIL.setContextClassLoader(mbeanTCL);
      }

      try
      {
         return invocation.invoke();
      }
      catch (AttributeNotFoundException e)
      {
         throw e;
      }
      catch (MBeanException e)
      {
         throw e;
      }
      catch (ReflectionException e)
      {
         throw e;
      }
      catch (JMRuntimeException e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         rethrowAsMBeanException(t);
         return null;
      }

         // TODO: should be fixed by adding invocation return value object
      finally
      {
         Descriptor attrDesc = invocation.getDescriptor();
         ctx.setDescriptor(attrDesc);
         updateAttributeInfo(attrDesc);

         if (setCl)
         {
            TCLAction.UTIL.setContextClassLoader(ccl);
         }
      }
   }

   /**
    * Sets an attribute value. The operation is forced through a set of
    * interceptors before the new value for the attribute is set.
    * @param attribute new attribute value
    * @throws AttributeNotFoundException if the requested attribute is not part
    * of the MBean's management interface
    * @throws InvalidAttributeValueException if the attribute contains a value
    * not suitable for the attribute
    * @throws MBeanException if setting the attribute value causes an
    * application exception
    * @throws ReflectionException if there was an error trying to set the
    * attribute value.
    */
   public void setAttribute(Attribute attribute) throws AttributeNotFoundException,
      InvalidAttributeValueException, MBeanException, ReflectionException
   {
      // TODO:  __JBOSSMX_INVOCATION

      if (attribute == null)
         throw new InvalidAttributeValueException("null attribute");

      // lookup the server side invocation context
      String name = attribute.getName();
      InvocationContext ctx = (InvocationContext) attributeContextMap.get(name);

      // if we don't have a server side invocation context for the attribute,
      // it does not exist as far as we are concerned
      if (ctx == null)
         throw new AttributeNotFoundException("not found: " + name);
      else if (ctx.isWritable() == false)
      {
         throw new AttributeNotFoundException("Attribute '" + name
            + "' is not writable");
      }

      // create the invocation object
      Invocation invocation = new Invocation();

      // copy the server context to the invocation
      invocation.addContext(ctx);

      // indicate the access point as setAttribute()
      invocation.setType(InvocationContext.OP_SETATTRIBUTE);

      // set the attribute value as the argument
      invocation.setArgs(new Object[]{attribute.getValue()});

      override(invocation);

      ClassLoader mbeanTCL = resourceEntry.getClassLoader();
      final ClassLoader ccl = TCLAction.UTIL.getContextClassLoader();
      boolean setCl = ccl != mbeanTCL && mbeanTCL != null;
      if (setCl)
      {
         TCLAction.UTIL.setContextClassLoader(mbeanTCL);
      }

      try
      {
         // the default invocation implementation will invoke each interceptor
         // declared in the invocation context before invoking the target method
         invocation.invoke();
      }
      catch (AttributeNotFoundException e)
      {
         throw e;
      }
      catch (InvalidAttributeValueException e)
      {
         throw e;
      }
      catch (MBeanException e)
      {
         throw e;
      }
      catch (ReflectionException e)
      {
         throw e;
      }
      catch (JMRuntimeException e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         rethrowAsMBeanException(t);
      }

         // TODO: should be fixed by adding invocation return value object
      finally
      {
         /* Obtain the updated attribute descriptor and propagate to the
         invocation context and ModelMBeanInfo. The latter is required in
         order for getMBeanInfo() to show an updated view.
         */
         Descriptor attrDesc = invocation.getDescriptor();
         ctx.setDescriptor(attrDesc);
         updateAttributeInfo(attrDesc);

         if (setCl)
         {
            TCLAction.UTIL.setContextClassLoader(ccl);
         }
      }
   }

   public MBeanInfo getMBeanInfo()
   {
      // create the invocation object
      Invocation invocation = new Invocation(getMBeanInfoCtx);

      // set the invocation's access point as getMBeanInfo()
      invocation.setType(InvocationContext.OP_GETMBEANINFO);

      if (resourceEntry == null)
         resourceEntry = getMBeanEntry();
      ClassLoader mbeanTCL = resourceEntry.getClassLoader();
      final ClassLoader ccl = TCLAction.UTIL.getContextClassLoader();
      boolean setCl = ccl != mbeanTCL && mbeanTCL != null;
      if (setCl)
      {
         TCLAction.UTIL.setContextClassLoader(mbeanTCL);
      }

      try
      {
         MBeanInfo info = (MBeanInfo) invocation.invoke();
         return info;
      }
      catch (JMRuntimeException e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         rethrowAsRuntimeMBeanException(t);
         return null;
      }
      finally
      {
         if (setCl)
         {
            TCLAction.UTIL.setContextClassLoader(ccl);
         }
      }
   }

   public AttributeList getAttributes(java.lang.String[] attributes)
   {
      if (attributes == null)
         throw new IllegalArgumentException("null array");

      AttributeList list = new AttributeList();

      for (int i = 0; i < attributes.length; ++i)
      {
         try
         {
            list.add(new Attribute(attributes[i], getAttribute(attributes[i])));
         }
         catch (Throwable ignored)
         {
            // if the attribute could not be retrieved, skip it
         }
      }

      return list;
   }

   public AttributeList setAttributes(AttributeList attributes)
   {
      if (attributes == null)
         throw new IllegalArgumentException("null list");

      AttributeList results = new AttributeList();
      Iterator it = attributes.iterator();

      while (it.hasNext())
      {
         Attribute attr = (Attribute) it.next();
         try
         {
            setAttribute(attr);
            results.add(attr);
         }
         catch (Throwable ignored)
         {
            // if unable to set the attribute, skip it
            if (log.isTraceEnabled())
               log.trace("Unhandled setAttribute() for attribute: " + attr.getName(), ignored);
         }
      }

      return results;
   }


   // MBeanRegistration implementation ------------------------------

   /**
    * Initializes this invoker. At the registration time we can be sure that all
    * of the metadata is available and initialize the invoker and cache the data
    * accordingly.   <p>
    *
    * Subclasses that override the <tt>preRegister</tt> method must make sure
    * they call <tt>super.preRegister()</tt> in their implementation to ensure
    * proper initialization of the invoker.
    */
   public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception
   {
      this.resourceEntry = (MBeanEntry) preRegisterInfo.get();
      this.server = server;

      ObjectName mbeanName = null;
      Descriptor mbeanDescriptor = null;
      if( info instanceof ModelMBeanInfo )
      {
         ModelMBeanInfo minfo = (ModelMBeanInfo) info;
         try
         {
            mbeanDescriptor = minfo.getDescriptor("",
                           ModelMBeanConstants.MBEAN_DESCRIPTOR);
            String type = (String) mbeanDescriptor.getFieldValue(
               ModelMBeanConstants.MBEAN_SERVER_INJECTION_TYPE);
            if( type != null )
            {
               inject(ModelMBeanConstants.MBEAN_SERVER_INJECTION_TYPE,
                  type, MBeanServer.class, getServer());
            }
         }
         catch (MBeanException e)
         {
            log.warn("Failed to obtain descriptor: "+ModelMBeanConstants.MBEAN_DESCRIPTOR, e);
         }

      }

      ClassLoader mbeanTCL = resourceEntry.getClassLoader();
      final ClassLoader ccl = TCLAction.UTIL.getContextClassLoader();
      boolean setCl = ccl != mbeanTCL && mbeanTCL != null;
      if (setCl)
      {
         TCLAction.UTIL.setContextClassLoader(mbeanTCL);
      }

      try
      {
         initAttributeContexts(info.getAttributes());

         initOperationContexts(info.getOperations());

         if (resource != null)
            initDispatchers();

         mbeanName = invokePreRegister(server, name);
         if( mbeanDescriptor != null )
         {
            Object value = mbeanDescriptor.getFieldValue(
            ModelMBeanConstants.OBJECT_NAME_INJECTION_TYPE);
            String type = (String) value;
            if( type != null )
            {
               inject(ModelMBeanConstants.OBJECT_NAME_INJECTION_TYPE,
                  type, ObjectName.class, mbeanName);
            }
         }
      }
      finally
      {
         if (setCl)
         {
            TCLAction.UTIL.setContextClassLoader(ccl);
         }
      }
      return mbeanName;
   }

   /**
    */
   public void postRegister(Boolean registrationSuccessful)
   {
      invokePostRegister(registrationSuccessful);
   }

   /**
    */
   public void preDeregister() throws Exception
   {
      invokePreDeregister();
   }

   /**
    */
   public void postDeregister()
   {
      invokePostDeregister();
      this.server = null;
   }


   // NotificationEmitter implementation ------------------------

   public void addNotificationListener(NotificationListener listener,
      NotificationFilter filter, Object handback)
   {
      addNotificationListenerToResource(listener, filter, handback);
   }

   protected void addNotificationListenerToResource(NotificationListener listener, NotificationFilter filter, Object handback)
   {
      if (resource instanceof NotificationBroadcaster)
      {
         ((NotificationBroadcaster) resource).addNotificationListener(listener, filter, handback);
      }
      else
      {
         throw new RuntimeMBeanException(new IllegalArgumentException("Target XXX is not a notification broadcaster"

            // FIXME: add the XXX object name, store from registration
         ));
      }
   }

   public void removeNotificationListener(NotificationListener listener)
      throws ListenerNotFoundException
   {
      removeNotificationListenerFromResource(listener);
   }

   protected void removeNotificationListenerFromResource(NotificationListener listener)
      throws ListenerNotFoundException
   {
      if (resource instanceof NotificationBroadcaster)
      {
         ((NotificationBroadcaster) resource).removeNotificationListener(listener);
      }
      else
      {
         throw new RuntimeMBeanException(new IllegalArgumentException("Target XXX is not a notification broadcaster"

            // FIXME: add the XXX object name, store from registration
         ));
      }
   }

   public void removeNotificationListener(NotificationListener listener,
      NotificationFilter filter,
      Object handback)
      throws ListenerNotFoundException
   {
      removeNotificationListenerFromResource(listener, filter, handback);
   }

   protected void removeNotificationListenerFromResource(NotificationListener listener,
      NotificationFilter filter,
      Object handback)
      throws ListenerNotFoundException
   {
      if (resource instanceof NotificationEmitter)
      {
         ((NotificationEmitter) resource).removeNotificationListener(listener, filter, handback);
      }
      else if (resource instanceof NotificationBroadcaster)
      {
         //JGH NOTE: looks like a listener against the MBeanServer is
         //wrapped as a XMBean which has a broadcaster that is an NotificationEmitter
         //but this resource target is a NotificationBroadcaster, in which case,
         //w/o this .. you'll get a resource failure below
         removeNotificationListener(listener);
      }
      else
      {
         throw new RuntimeMBeanException(new IllegalArgumentException("Target XXX is not a notification emitter"

            // FIXME: add the XXX object name, store from registration
         ));
      }
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      return getNotificationInfoFromResource();
   }

   protected MBeanNotificationInfo[] getNotificationInfoFromResource()
   {
      if (resource instanceof NotificationBroadcaster)
      {
         return ((NotificationBroadcaster) resource).getNotificationInfo();
      }
      else
         return new MBeanNotificationInfo[]{};
   }


   // MBeanInvoker implementation -----------------------------------

   public MBeanInfo getMetaData()
   {
      return info;
   }
   
   public Object getResource()
   {
      return resource;
   }

   /**
    * Sets the XMBean resource and optionally allows the resource to interact
    * with the jmx microkernel via the following injection points:
    * #ModelMBeanConstants.MBEAN_SERVER_INJECTION_TYPE
    * #ModelMBeanConstants.MBEAN_INFO_INJECTION_TYPE
    * #ModelMBeanConstants.OBJECT_NAME_INJECTION_TYPE
    * @param resource - the model mbean resource
    */
   public void setResource(Object resource)
   {
      this.resource = resource;
   }

   public ObjectName getObjectName()
   {
      if (resourceEntry == null)
         return null;
      else
         return resourceEntry.getObjectName();
   }

   public void updateAttributeInfo(Descriptor attrDesc) throws MBeanException
   {
      ModelMBeanInfoSupport minfo = (ModelMBeanInfoSupport) info;
      minfo.setDescriptor(attrDesc, ModelMBeanConstants.ATTRIBUTE_DESCRIPTOR);
   }
   
   /**
    * Add dynamically an operation interceptor, first in the chain.
    */
   public void addOperationInterceptor(Interceptor interceptor)
   {
      if (operationContextMap != null && interceptor != null)
      {
         // Go through all the operation InvocationContext and add the interceptor
         for (Iterator it = operationContextMap.entrySet().iterator(); it.hasNext();)
         {
            Map.Entry entry = (Map.Entry) it.next();
   
            InvocationContext ctx = (InvocationContext) entry.getValue();
            List list = ctx.getInterceptors();

            // to make the interceptor list update atomic, make a new ArrayList,
            // add the new interceptor first and copy over the old ones,
            // then update the context
            List newList = new ArrayList();
            newList.add(interceptor);
            
            if (list != null)
            {
               newList.addAll(list);
            }
            
            ctx.setInterceptors(newList);
         }         
      }
   }
   
   /** 
    * Remove the specified operation interceptor 
    */
   public void removeOperationInterceptor(Interceptor interceptor)
   {
      if (operationContextMap != null && interceptor != null)
      {
         // Go through all the operation InvocationContext and remove the interceptor
         for (Iterator it = operationContextMap.entrySet().iterator(); it.hasNext();)
         {
            Map.Entry entry = (Map.Entry) it.next();

            InvocationContext ctx = (InvocationContext) entry.getValue();
            List list = ctx.getInterceptors();
            
            // to make the interceptor list update atomic, make a copy of the list
            // remove the interceptor (if found), then update the context
            if (list != null)
            {
               List newList = new ArrayList(list);

               // this should probably work, whether or not equals() is implemented
               // it'll remove the first occurence
               newList.remove(interceptor);
               
               ctx.setInterceptors(newList);
            }
         }
      }
   }
   
   // Other Public Methods ------------------------------------------
   
   public void suspend()
   {
   }

   public void suspend(long wait) throws TimeoutException
   {
   }

   public void suspend(boolean force)
   {
   }

   public boolean isSuspended()
   {
      return false;
   }

   public void setInvocationTimeout(long time)
   {
   }

   public long getInvocationTimeout()
   {
      return 0l;
   }

   public void resume()
   {
   }

   public MBeanServer getServer()
   {
      return server;
   }

   // Protected -----------------------------------------------------

   /**
    * Inject context from the xmbean layer to the resource
    * @param type - the type of injection 
    * @param name - the setter method name of the resource
    * @param argType - the injection data type
    * @param value - the injection data value to pass to the setter
    */ 
   protected void inject(String type, String name, Class argType, Object value)
   {
      try
      {
         Class resClass = resource.getClass();
         Class[] sig = {argType};
         Method setter = resClass.getMethod(name, sig);
         Object[] args = {value};
         setter.invoke(resource, args);
      }
      catch(NoSuchMethodException e)
      {
         log.debug("Setter not found: "+name+"("+argType+")", e);
      }
      catch(Exception e)
      {
         log.warn("Failed to inject type: "+type+" using setter: "+name, e);
      }
   }

   protected ObjectName invokePreRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      if (resource instanceof MBeanRegistration)
         return ((MBeanRegistration) resource).preRegister(server, name);

      return name;
   }

   protected void invokePostRegister(Boolean b)
   {
      if (resource instanceof MBeanRegistration)
         ((MBeanRegistration) resource).postRegister(b);
   }

   protected void invokePreDeregister() throws Exception
   {
      if (resource instanceof MBeanRegistration)
         ((MBeanRegistration) resource).preDeregister();
   }

   protected void invokePostDeregister()
   {
      if (resource instanceof MBeanRegistration)
         ((MBeanRegistration) resource).postDeregister();
   }

   protected void initAttributeContexts(MBeanAttributeInfo[] attributes)
   {
      // create invocation contexts for attributes
      for (int i = 0; i < attributes.length; ++i)
      {
         InvocationContext ctx = new InvocationContext();

         // fill in some default values, the attribute name
         ctx.setName(attributes[i].getName());

         ctx.setAttributeType(attributes[i].getType());

         // set myself as the invoker
         ctx.setInvoker(this);

         //ctx.add(InvocationContext.ATTRIBUTE_ACCESS, getAccessCode(attributes[i]));

         // store
         attributeContextMap.put(attributes[i].getName(), ctx);
      }
      if (log.isTraceEnabled())
         log.trace(getObjectName() + " configured attribute contexts: " + operationContextMap);      
   }

   protected void initOperationContexts(MBeanOperationInfo[] operations)
   {
      // create invocation contexts for operations
      for (int i = 0; i < operations.length; ++i)
      {
         InvocationContext ctx = new InvocationContext();

         // extract operation name + signature
         String opName = operations[i].getName();
         MBeanParameterInfo[] signature = operations[i].getSignature();
         String returnType = operations[i].getReturnType();

         // name is unchanged, fill in the context
         ctx.setName(opName);

         // signature doesn't change..
         ctx.setSignature(signature);

         // return type
         ctx.setReturnType(returnType);
         
         // set myself as the invoker
         ctx.setInvoker(this);

         // add impact as part of ctx map (rarely accessed information)
         //ctx.add(InvocationContext.OPERATION_IMPACT, operations[i].getImpact());

         // create an operation key consisting of the name + signature
         // (required for overloaded operations)
         OperationKey opKey = new OperationKey(opName, signature);

         // store
         operationContextMap.put(opKey, ctx);
      }
      
      if (log.isTraceEnabled())
         log.trace(getObjectName() + " configured operation contexts: " + operationContextMap);         
   }

   protected void initDispatchers()
   {
      boolean trace = log.isTraceEnabled();
      
      // locate the resource class to receive the invocations
      Class clazz = null;
      if (resource != null)
      {
         clazz = resource.getClass();
         
         // JBAS-1704, if the target class is *not* public, look for
         // an exposed MBean interface, if one exists.
         // This should be checking if we are dealing with a standard
         // mbean (but not a standard mbean deployed as a model mbean)
         // but it doesn't look convenient from this baseclass.
         if (Modifier.isPublic(clazz.getModifiers()) == false)
         {
            clazz = StandardMetaData.findStandardInterface(clazz);
         }
      }
      
      // map the Methods on the target resource for easy access
      MethodMapper mmap = new MethodMapper(clazz);
      if (trace)
         log.trace(getObjectName() + " " + clazz + " map=" + mmap);
      
      MBeanOperationInfo[] operations = info.getOperations();
      
      // Set the dispatchers for the operations
      for (int i = 0; i < operations.length; ++i)
      {
         MBeanOperationInfo op = operations[i];
         OperationKey opKey = new OperationKey(op.getName(), op.getSignature());
         InvocationContext ctx = (InvocationContext) operationContextMap.get(opKey);

         Interceptor dispatcher = ctx.getDispatcher();
         
         // Reconfigure if we have a Null or Reflected dispatcher
         if (dispatcher instanceof NullDispatcher || (dispatcher instanceof ReflectedDispatcher))
         {
            Object target = null;
            dispatcher = null;
            Method m = mmap.lookupOperation(op);
            if (m == null)
            {
               // Look for an method on the model mbean
               m = MethodMapper.lookupOperation(op, this);
               if (m != null)
               {
                  // operation found on the 'this' invoker
                  target = this;
                  dispatcher = new ReflectedDispatcher(m, dynamicResource);
               }
               else
               {
                  // operation not found, use late binding
                  // What is this late binding attempt and should there be a warning?
                  dispatcher = new ReflectedDispatcher(dynamicResource);
               }
            }
            else
            {
               // operation found on the resource
               target = resource;
               dispatcher = new ReflectedDispatcher(m, dynamicResource);
            }
            if (trace)
               log.trace(getObjectName() + " will dispatch op=" + opKey + 
                  " to " + Strings.defaultToString(target) +
                  " method= " + m);            
            ctx.setTarget(target);
            ctx.setDispatcher(dispatcher);
         }
      }

      // Set the dispatchers for the attributes with getters/setters
      MBeanAttributeInfo[] attributes = info.getAttributes();
      for (int i = 0; i < attributes.length; ++i)
      {
         MBeanAttributeInfo attribute = attributes[i];
         String name = attribute.getName();
         InvocationContext ctx = (InvocationContext) attributeContextMap.get(name);

         Method getter = mmap.lookupGetter(attribute);
         Method setter = mmap.lookupSetter(attribute);
         ctx.setDispatcher(new AttributeDispatcher(getter, setter, dynamicResource));
         ctx.setTarget(resource);
      }
   }

   /**
    * Placeholder to allow subclasses to override the invocation
    * @param invocation the invocation
    * @throws MBeanException for any error
    */
   protected void override(Invocation invocation) throws MBeanException
   {
   }

   protected String getSignatureString(String[] signature)
   {
      if (signature == null)
         return "()";
      if (signature.length == 0)
         return "()";

      StringBuffer sbuf = new StringBuffer(512);

      sbuf.append("(");
      for (int i = 0; i < signature.length - 1; ++i)
      {
         sbuf.append(signature[i]);
         sbuf.append(",");
      }
      sbuf.append(signature[signature.length - 1]);
      sbuf.append(")");

      return sbuf.toString();
   }


   // Inner classes -------------------------------------------------
   protected final class OperationKey
   {
      String[] keys = null;
      int hash = 0;

      public OperationKey(final String name, final String type)
      {
         if (type != null)
         {
            keys = new String[2];

            keys[0] = name;
            keys[1] = type;

            hash = name.hashCode();
         }

         else
         {
            keys = new String[]{name};
            hash = name.hashCode();
         }
      }

      public OperationKey(final String name, final String[] signature)
      {
         if (signature != null)
         {
            keys = new String[signature.length + 1];

            keys[0] = name;

            System.arraycopy(signature, 0, keys, 1, signature.length);

            hash = name.hashCode();
         }

         else
         {
            keys = new String[]{name};
            hash = name.hashCode();
         }
      }

      public OperationKey(String name, MBeanParameterInfo[] signature)
      {
         if (signature == null)
            signature = new MBeanParameterInfo[0];

         keys = new String[signature.length + 1];

         keys[0] = name;

         for (int i = 0; i < signature.length; ++i)
         {
            keys[i + 1] = signature[i].getType();
         }

         hash = name.hashCode();
      }

      public OperationKey(MBeanOperationInfo info)
      {
         this(info.getName(), info.getSignature());
      }

      public int hashCode()
      {
         return hash;
      }

      public boolean equals(Object o)
      {
         OperationKey target = (OperationKey) o;

         if (target.keys.length != keys.length)
            return false;

         for (int i = 0; i < keys.length; ++i)
         {
            if (!(keys[i].equals(target.keys[i])))
               return false;
         }

         return true;
      }

      public String toString()
      {
         StringBuffer buffer = new StringBuffer(50);
         buffer.append(keys[0]).append("(");

         for (int i = 1; i < keys.length - 1; ++i)
         {
            buffer.append(keys[i]).append(',');
         }

         if (keys.length > 1)
            buffer.append(keys[keys.length - 1]);
         buffer.append(")");
         return buffer.toString();
      }
   }

   private void rethrowAsMBeanException(Throwable t) throws MBeanException
   {
      if (t instanceof RuntimeException)
         throw new RuntimeMBeanException((RuntimeException) t);
      else if (t instanceof Error)
         throw new RuntimeErrorException((Error) t);
      else
         throw new MBeanException((Exception) t);
   }

   private void rethrowAsRuntimeMBeanException(Throwable t)
   {
      if (t instanceof RuntimeException)
         throw new RuntimeMBeanException((RuntimeException) t);
      else if (t instanceof Error)
         throw new RuntimeErrorException((Error) t);
      else
         throw new RuntimeMBeanException(new RuntimeException("Unhandled exception", t));
   }
}