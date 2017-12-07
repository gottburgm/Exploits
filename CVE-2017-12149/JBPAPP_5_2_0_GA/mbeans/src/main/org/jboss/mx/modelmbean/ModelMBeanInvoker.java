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
package org.jboss.mx.modelmbean;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.AttributeChangeNotificationFilter;
import javax.management.Descriptor;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.jboss.common.beans.property.finder.PropertyEditorFinder;
import org.jboss.logging.Logger;
import org.jboss.mx.interceptor.AbstractInterceptor;
import org.jboss.mx.interceptor.Interceptor;
import org.jboss.mx.interceptor.ModelMBeanAttributeInterceptor;
import org.jboss.mx.interceptor.ModelMBeanInfoInterceptor;
import org.jboss.mx.interceptor.ModelMBeanInterceptor;
import org.jboss.mx.interceptor.ModelMBeanOperationInterceptor;
import org.jboss.mx.interceptor.NullInterceptor;
import org.jboss.mx.interceptor.ObjectReferenceInterceptor;
import org.jboss.mx.interceptor.PersistenceInterceptor;
import org.jboss.mx.interceptor.PersistenceInterceptor2;
import org.jboss.mx.persistence.NullPersistence;
import org.jboss.mx.persistence.PersistenceManager;
import org.jboss.mx.server.AbstractMBeanInvoker;
import org.jboss.mx.server.Invocation;
import org.jboss.mx.server.InvocationContext;
import org.jboss.mx.server.MBeanInvoker;
import org.jboss.mx.util.JBossNotificationBroadcasterSupport;

/**
 * An extension of the {@link org.jboss.mx.server.MBeanInvoker MBeanInvoker}
 * that implements the base Model MBean functionality, essentially making the
 * Model MBean just another invoker of managed resources.
 *
 * @see javax.management.modelmbean.ModelMBean
 * @see org.jboss.mx.server.MBeanInvoker
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>.
 * @author Matt Munz
 * @version $Revision: 113110 $
 */
public abstract class ModelMBeanInvoker extends AbstractMBeanInvoker
   implements ModelMBean, ModelMBeanConstants
{
   Logger log = Logger.getLogger(ModelMBeanInvoker.class.getName());

   // Attributes ----------------------------------------------------

   /**
    * The resource type string of the managed resource, such as
    * {@link ModelMBeanConstants#OBJECT_REF} or
    * {@link XMBeanConstants#STANDARD_INTERFACE}. This type string can be
    * used by the invoker to determine the behavior implemented by the
    * invocation chain and how the managed resource is exposed to the client
    * programs.
    */
   protected String resourceType = null;
   
   /**
    * Persistence manager.
    */
   protected PersistenceManager persistence = new NullPersistence();

   /**
    * Notification broadcaster for this Model MBean.
    */
   protected JBossNotificationBroadcasterSupport notifier = new JBossNotificationBroadcasterSupport();

   /**
    * Notification sequence number for generic Model MBean notifications.
    */
   protected long notifierSequence = 1;

   /**
    * Notification sequence number for attribute change notifications.
    */
   protected long attrNotifierSequence = 1;


   // Constructors --------------------------------------------------

   /**
    * Default constructor.
    */
   public ModelMBeanInvoker()
   {
   }

   /**
    * Creates a Model MBean instance and initializes it with the given
    * Model MBean metadata.
    *
    * @param   info  Model MBean metadata
    */
   public ModelMBeanInvoker(ModelMBeanInfo info) throws MBeanException
   {
      setModelMBeanInfo(info);
   }



   // ModelMBean implementation -------------------------------------

   /**
    * Sets the MBean metadata for this Model MBean instance.
    *
    * @param   info  Model MBean metadata
    */
   public void setModelMBeanInfo(ModelMBeanInfo info)
      throws MBeanException, RuntimeOperationsException
   {
      if (info == null)
         throw new RuntimeOperationsException(new IllegalArgumentException("MBeanInfo cannot be null"));

      // need to type to an instance of MBeanInfo -- therefore the extra copy here
      this.info = new ModelMBeanInfoSupport(info);

      // Apply the MBeanInfo injection if requested
      ModelMBeanInfo minfo = info;
      Descriptor mbeanDescriptor = null;
      try
      {
         mbeanDescriptor = minfo.getDescriptor("",
                        ModelMBeanConstants.MBEAN_DESCRIPTOR);
      }
      catch (MBeanException e)
      {
         log.warn("Failed to obtain descriptor: "+ModelMBeanConstants.MBEAN_DESCRIPTOR, e);
         return;
      }

      String type = (String) mbeanDescriptor.getFieldValue(
         ModelMBeanConstants.MBEAN_INFO_INJECTION_TYPE);
      if( type != null )
      {
         inject(ModelMBeanConstants.MBEAN_INFO_INJECTION_TYPE,
            type, MBeanInfo.class, info);
      }
   }

   /**
    * Sets the managed resource for this Model MBean instance. The resource
    * type must be known to the Model MBean implementation (see
    * {@link #isSupportedResourceType} for more information).
    *
    * @param   ref            reference to the managed resource
    * @param   resourceType   resource type identification string
    */
   public void setManagedResource(Object ref, String resourceType)
      throws MBeanException, InstanceNotFoundException, InvalidTargetObjectTypeException
   {
      if (!isSupportedResourceType(ref, resourceType))
         throw new InvalidTargetObjectTypeException("Unsupported resource type: " + resourceType);

      setResource(ref);
      this.resourceType = resourceType;

      if (getServer() != null)
      {
         try
         {
            this.init(getServer(), resourceEntry.getObjectName());
         }
         catch(Exception e)
         {
            throw new MBeanException(e, "Failed to init from resource");
         }
      }
   }

   // ModelMBeanNotificationBroadcaster implementation --------------

   public void addNotificationListener(NotificationListener listener,
      NotificationFilter filter,
      Object handback)
   {
      notifier.addNotificationListener(listener, filter, handback);
   }

   public void removeNotificationListener(NotificationListener listener)
      throws ListenerNotFoundException
   {
      notifier.removeNotificationListener(listener);
   }

   public void removeNotificationListener(NotificationListener listener,
      NotificationFilter filter,
      Object handback)
      throws ListenerNotFoundException
   {
      notifier.removeNotificationListener(listener, filter, handback);
   }

   /**
    * Sends a notification with a given string message. The notification
    * type will be set as
    * {@link ModelMBeanConstants#GENERIC_MODELMBEAN_NOTIFICATION GENERIC_MODELMBEAN_NOTIFICATION}.
    *
    * @param ntfyText notification message
    */
   public void sendNotification(String ntfyText)
      throws MBeanException, RuntimeOperationsException
   {
      if( ntfyText == null )
      {
         throw new RuntimeOperationsException(
           new IllegalArgumentException("ntfyText cannot be null")
         );
      }
      Notification notif = new Notification(
         GENERIC_MODELMBEAN_NOTIFICATION, // type
         this, // source
         1, // always 1 - by spec
         ntfyText            // message
      );

      sendNotification(notif);
   }

   /**
    * Sends a notification.
    *
    * @param ntfyObj notification to send
    */
   public void sendNotification(Notification ntfyObj)
      throws MBeanException, RuntimeOperationsException
   {
      if( ntfyObj == null )
      {
         throw new RuntimeOperationsException(
            new IllegalArgumentException("ntfyText cannot be null")
         );
      }
      notifier.sendNotification(ntfyObj);
   }

   /**
    * Sends an attribute change notification.
    *
    * @param   notification attribute change notification to send
    */
   public void sendAttributeChangeNotification(AttributeChangeNotification notification)
      throws MBeanException
   {
      if( notification == null )
      {
         throw new RuntimeOperationsException(
            new IllegalArgumentException("notification cannot be null")
         );
      }
      notifier.sendNotification(notification);
   }

   /**
    * Sends an attribute change notification.
    *
    * @param   oldValue attribute with the old value
    * @param   newValue attribute with the new value
    * @throws IllegalArgumentException - An Attribute object passed in parameter
    * is null or the names of the two Attribute objects in parameter are not
    * the same.
    */
   public void sendAttributeChangeNotification(Attribute oldValue, Attribute newValue)
      throws MBeanException, RuntimeOperationsException
   {
      if( oldValue == null || newValue == null )
      {
         throw new RuntimeOperationsException(
            new IllegalArgumentException("Attribute cannot be null")
         );
      }
      if (!(oldValue.getName().equals(newValue.getName())))
      {
         throw new RuntimeOperationsException(
            new IllegalArgumentException("Attribute name mismatch between oldvalue and newvalue")
         );
      }

      String attr = oldValue.getName();
      String type = ((ModelMBeanInfo) info).getAttribute(attr).getType();

      AttributeChangeNotification notif = new AttributeChangeNotification(
         this, // source
         1, // always 1 - by spec
         System.currentTimeMillis(), // time stamp
         "" + attr + " changed from " + oldValue + " to " + newValue,
         attr, type, // name & type
         oldValue.getValue(),
         newValue.getValue()            // values
      );

      notifier.sendNotification(notif);
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      return info.getNotifications();
   }

   /**
    */
   public void addAttributeChangeNotificationListener(
      NotificationListener listener,
      String attributeName,
      Object handback) throws MBeanException
   {
      // Check the attribute info
      ModelMBeanInfo minfo = (ModelMBeanInfo) info;
      AttributeChangeNotificationFilter filter = null;
      if (attributeName != null)
      {
         ModelMBeanAttributeInfo ainfo = minfo.getAttribute(attributeName);
         if( ainfo == null )
         {
            throw new RuntimeOperationsException(
               new IllegalArgumentException("Attribute does not exist: "+attributeName));         
         }
         filter = new AttributeChangeNotificationFilter();
         filter.enableAttribute(attributeName);
      }
      else
      {
         filter = new AttributeChangeNotificationFilter();
         MBeanAttributeInfo[] allAttributes = minfo.getAttributes();
         for (int i = 0; i < allAttributes.length; ++i)
            filter.enableAttribute(allAttributes[i].getName());
      }
      notifier.addNotificationListener(listener, filter, handback);
   }

   /**
    */
   public void removeAttributeChangeNotificationListener(
      NotificationListener listener,
      String attributeName) throws MBeanException, ListenerNotFoundException
   {
      if( attributeName != null )
      {
         // Check the attribute info
         ModelMBeanInfo minfo = (ModelMBeanInfo) info;
         ModelMBeanAttributeInfo ainfo = minfo.getAttribute(attributeName);
         if( ainfo == null )
         {
            throw new RuntimeOperationsException(
               new IllegalArgumentException("Attribute does not exist: "+attributeName));         
         }
      }
      notifier.removeNotificationListener(listener);
   }

   // PersistentMBean implementation --------------------------------
   public void load() throws MBeanException, InstanceNotFoundException
   {
      if (info == null)
         return;

      persistence.load(this, info);
   }

   public void store() throws MBeanException, InstanceNotFoundException
   {
      persistence.store(info);
   }


   // MBeanRegistration implementation ------------------------------

   /**
    * The default implementation of <tt>preRegister</tt> invokes the
    * {@link #configureInterceptorStack} method which sets up the interceptors
    * for this Model MBean instance. Subclasses may override the
    * <tt>configureInterceptorStack()</tt> method to implement their own
    * interceptor stack configurations. See the JavaDoc for
    * <tt>configureInterceptorStack()</tt> for more information.    <p>
    *
    * After the interceptor configuration, this implementation invokes the
    * {@link #load} method on this Model MBean instance. This will attempt
    * to load a pre-existing management attribute state for this Model MBean
    * instance. See the Javadoc for <tt>load()</tt> for more information.
    */
   public ObjectName invokePreRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      // Check for null metadata and prevent registration if metadata
      // has not been set
      if (info == null)
      {
         throw new RuntimeErrorException(
            new Error("MBeanInfo has not been set."));
      }

      // Set the mbean descriptor on the info context for use by interceptor config
      final ModelMBeanInfo minfo = (ModelMBeanInfo) info;
      Descriptor mbeanDescriptor = minfo.getMBeanDescriptor();
      getMBeanInfoCtx = new InvocationContext();
      getMBeanInfoCtx.setInvoker(this);
      getMBeanInfoCtx.setDescriptor(mbeanDescriptor);
      getMBeanInfoCtx.setDispatcher(new AbstractInterceptor("MBeanInfo Dispatcher")
      {
         public Object invoke(Invocation invocation) throws Throwable
         {
            return minfo;
         }
      });
      // JBAS-33 - No need to register the "getMBeanInfo" context to the operationsContextMap,
      // this is only accessible through AbstractMBeanInvoker.getMBeanInfo().
      // Registering it will result in duplicate interceptor construction.

      // Need to install the setManagedResource op
      // TODO, this is probably uneccessary now so revisit this
      String[] signature = new String[]{"java.lang.Object", "java.lang.String"};
      OperationKey opKey = new OperationKey("setManagedResource", signature);
      InvocationContext ctx = new InvocationContext();
      ctx.setInvoker(this);
      ctx.setDispatcher(new AbstractInterceptor("SetMangedResource Dispatcher")
      {
         public Object invoke(Invocation invocation) throws Throwable
         {
            Object[] args = invocation.getArgs();
            setManagedResource(args[0], (String) args[1]);
            return null;
         }
      });
      operationContextMap.put(opKey, ctx);

      if (getResource() == null )
      {
         return name;
      }
      else
      {
         init(server, name);
      }

      return super.invokePreRegister(server, name);
   }

   // Protected ---------------------------------------------------

   /**
    * 
    * @param server
    * @param name
    * @throws Exception
    */ 
   protected void init(MBeanServer server, ObjectName name)
      throws Exception
   {
      ModelMBeanInfo minfo = (ModelMBeanInfo) info;
      configureInterceptorStack(minfo, server, name);
      initDispatchers();

      // add the resource classname to the MBean info
      Object resource = getResource();
      if (resource != null)
      {
         Descriptor mbeanDescriptor = minfo.getMBeanDescriptor();         
         String resClassName = getResource().getClass().getName();
         mbeanDescriptor.setField(ModelMBeanConstants.RESOURCE_CLASS, resClassName);
         minfo.setMBeanDescriptor(mbeanDescriptor);
      }

      //Set initial values provided in descriptors
      setValuesFromMBeanInfo();

      initPersistence(server, name);

      //Set (and override) values from mbean persistence store.
      load();
   }

   /**
    * initializes the persistence manager based on the info for this bean.
    * If this is successful, loads the bean from the persistence store.
    */
   protected void initPersistence(MBeanServer server, ObjectName name)
      throws MBeanException, InstanceNotFoundException
   {
      Descriptor[] descriptors;
      ModelMBeanInfo minfo = (ModelMBeanInfo) getMetaData();
      
      try
      {
         descriptors = minfo.getDescriptors(MBEAN_DESCRIPTOR);
      }
      catch (MBeanException e)
      {
         log.error("Failed to obtain MBEAN_DESCRIPTORs", e);
         return;
      }

      if (descriptors == null)
      {
         return;
      }
      String persistMgrName = null;
      for (int i = 0; ((i < descriptors.length) && (persistMgrName == null)); i++)
      {
         persistMgrName = (String) descriptors[i].getFieldValue(PERSISTENCE_MANAGER);
      }
      if (persistMgrName == null)
      {
         log.trace("No " + PERSISTENCE_MANAGER
            + " descriptor found, null persistence will be used");
         return;
      }

      try
      {
         persistence = (PersistenceManager) server.instantiate(persistMgrName);
         log.debug("Loaded persistence mgr: " + persistMgrName);
         
         // Add the ObjectName to the ModelMBean Descriptor
         // so that it can be used by the PersistentManager (if needed)
         Descriptor descriptor = minfo.getMBeanDescriptor();
         descriptor.setField(ModelMBeanConstants.OBJECT_NAME, name);
         minfo.setMBeanDescriptor(descriptor);
      }
      catch (Exception cause)
      {
         log.error("Unable to instantiate the persistence manager:"
            + persistMgrName, cause);
      }
   }

   protected void initOperationContexts(MBeanOperationInfo[] operations)
   {
      // make sure we invoke the super class initialization sequence first
      super.initOperationContexts(operations);

      for (int i = 0; i < operations.length; ++i)
      {
         OperationKey key = new OperationKey(operations[i]);

         InvocationContext ctx = (InvocationContext) operationContextMap.get(key);
         ModelMBeanOperationInfo info = (ModelMBeanOperationInfo) operations[i];
         ctx.setDescriptor(info.getDescriptor());
      }
   }

   protected void initAttributeContexts(MBeanAttributeInfo[] attributes)
   {
      super.initAttributeContexts(attributes);

      for (int i = 0; i < attributes.length; ++i)
      {
         ModelMBeanAttributeInfo info = (ModelMBeanAttributeInfo) attributes[i];
         String name = info.getName();
         InvocationContext ctx = (InvocationContext) attributeContextMap.get(name);
         ctx.setDescriptor(info.getDescriptor());
         ctx.setReadable(info.isReadable());
         ctx.setWritable(info.isWritable());
      }
   }

   /** 
    * Build the getMBeanInfo, operation, and attribute interceptor stacks
    * and associated these with the corresponding InvocationContexts.
    * 
    * @param info - the ModelMBean metadata
    * @param server - the MBeanServer the ModelMBean is registering with
    * @param name - the ModelMBean name
    * @throws Exception
    */
   protected void configureInterceptorStack(ModelMBeanInfo info, MBeanServer server, ObjectName name)
      throws Exception
   {
      // Get the MBeanInfo accessor interceptor stack. This is the interceptor
      // stack declared at the model mbean level. In 3.2.3 and earlier this was
      // the interceptor stack for all operation and attribute access so we
      // use this as the default interceptor stack, for all attributes/operations.
      
      List defaultInterceptors = getInterceptors(getMBeanInfoCtx.getDescriptor());
      
      List interceptors = null;
      if (defaultInterceptors != null)
      {
         interceptors = new ArrayList(defaultInterceptors);
      }
      if (interceptors == null)
      {
         // Set the default interceptor stack
         interceptors = getMBeanInfoCtx.getInterceptors();
      }
      // We always add the ModelMBeanInfoInterceptor as we expect that
      // users are specifying additional interceptors, not overriding the
      // source of the ModelMBeanInfo.

      String mbeanName = name != null ? name.toString() : info.getClassName();
      interceptors.add(new ModelMBeanInfoInterceptor(mbeanName));
      getMBeanInfoCtx.setInterceptors(interceptors);

      // Get any custom interceptors specified at the attribute level
      for (Iterator it = attributeContextMap.entrySet().iterator(); it.hasNext();)
      {
         Map.Entry entry = (Map.Entry) it.next();

         InvocationContext ctx = (InvocationContext) entry.getValue();
         List list = getInterceptors(ctx.getDescriptor());
         if (list == null)
         {
            // Use the mbean inteceptors if sepecified
            if (defaultInterceptors != null)
            {
               list = new ArrayList(defaultInterceptors);
            }
            else
            {
               list = new ArrayList();
            }
         }
         // Add the attribute accessor semantic interceptors
         list.add(new PersistenceInterceptor());
         list.add(new ModelMBeanAttributeInterceptor());
         ctx.setInterceptors(list);
      }

      // Get any custom interceptors specified at the operation level
      for (Iterator it = operationContextMap.entrySet().iterator(); it.hasNext();)
      {
         Map.Entry entry = (Map.Entry) it.next();

         InvocationContext ctx = (InvocationContext) entry.getValue();
         List list = getInterceptors(ctx.getDescriptor());
         if (list == null && defaultInterceptors != null)
            list = new ArrayList(defaultInterceptors);
         
         // Add operation caching (not for standard mbeans)
         if (dynamicResource)
         {
            if (list == null)
            {
               list = new ArrayList();
            }
            list.add(new ModelMBeanOperationInterceptor());
         }
            
         if (list != null)
         {
            // Add a noop interceptor since the 3.2.3- interceptors always had
            // to delegate to the next in order to dispatch the operation. Now
            // there is no interceptor for this so this prevents NPEs.
            
            list.add(new NullInterceptor());
            ctx.setInterceptors(list);
         }
      }
   }

   /**
    * 
    * @param d
    * @return
    * @throws Exception
    */
   protected List getInterceptors(Descriptor d) throws Exception
   {
      if (d == null)
         return null;
      Descriptor[] interceptorDescriptors = (Descriptor[]) d.getFieldValue(INTERCEPTORS);
      if (interceptorDescriptors == null)
         return null;

      ArrayList interceptors = new ArrayList();
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      for (int i = 0; i < interceptorDescriptors.length; i++)
      {
         Descriptor desc = interceptorDescriptors[i];
         String code = (String) desc.getFieldValue("code");
         // Ignore the legacy required interceptors
         if (code.equals(ModelMBeanInterceptor.class.getName()) ||
            code.equals(ObjectReferenceInterceptor.class.getName()) ||
            code.equals(PersistenceInterceptor2.class.getName()))
         {
            log.debug("Ignoring obsolete legacy interceptor: " + code);
            continue;
         }

         Class interceptorClass = loader.loadClass(code);
         Interceptor interceptor = null;
         // Check for a ctor(MBeanInvoker)
         Class[] ctorSig = {MBeanInvoker.class};
         try
         {
            Constructor ctor = interceptorClass.getConstructor(ctorSig);
            Object[] ctorArgs = {this};
            interceptor = (Interceptor) ctor.newInstance(ctorArgs);
         }
         catch (Throwable t)
         {
            log.debug("Could not invoke CTOR(MBeanInvoker) for '"
                  + interceptorClass + "', trying default CTOR: " + t.getMessage()); 
            
            // Try the default ctor
            interceptor = (Interceptor) interceptorClass.newInstance();
         }
         interceptors.add(interceptor);

         // Apply any interceptor attributes
         String[] names = desc.getFieldNames();
         HashMap propertyMap = new HashMap();
         if (names.length > 1)
         {
            BeanInfo beanInfo = Introspector.getBeanInfo(interceptorClass);
            PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
            for (int p = 0; p < props.length; p++)
            {
               String fieldName = props[p].getName();
               propertyMap.put(fieldName, props[p]);
            }
            // Map each attribute to the corresponding interceptor property
            for (int n = 0; n < names.length; n++)
            {
               String name = names[n];
               if (name.equals("code"))
                  continue;
               String text = (String) desc.getFieldValue(name);
               PropertyDescriptor pd = (PropertyDescriptor) propertyMap.get(name);
               if (pd == null)
                  throw new IntrospectionException("No PropertyDescriptor for attribute:" + name);
               Method setter = pd.getWriteMethod();
               if (setter != null)
               {
                  Class ptype = pd.getPropertyType();
                  PropertyEditor editor = PropertyEditorFinder.getInstance().find(ptype);
                  if (editor == null)
                     throw new IntrospectionException("Cannot convert string to interceptor attribute:" + name);
                  editor.setAsText(text);
                  Object args[] = {editor.getValue()};
                  setter.invoke(interceptor, args);
               }
            }
         }
      }

      if (interceptors.size() == 0)
         interceptors = null;
      return interceptors;
   }

   protected void setValuesFromMBeanInfo() throws JMException
   {
      for (Iterator it = attributeContextMap.entrySet().iterator(); it.hasNext();)
      {
         Map.Entry entry = (Map.Entry) it.next();
         String key = (String) entry.getKey();

         InvocationContext ctx = (InvocationContext) entry.getValue();
         //Initialize value from descriptor.
         Object value = ctx.getDescriptor().getFieldValue(XMBeanConstants.CACHED_VALUE);
         if (value != null)
         {
            setAttribute(new Attribute(key, value));
         } // end of if ()
      }

   }

   protected boolean isSupportedResourceType(Object resource, String resourceType)
   {
      if (resourceType.equalsIgnoreCase(OBJECT_REF))
         return true;

      return false;
   }

   protected void override(Invocation invocation) throws MBeanException
   {
      // Do we allow for dynamic descriptor changes
      if (dynamicResource && info != null)
      {
         Descriptor current = invocation.getDescriptor();
         if (current != null)
         {
            ModelMBeanInfo mminfo = (ModelMBeanInfo) info;
            Descriptor descriptor = mminfo.getDescriptor((String) current.getFieldValue(NAME), (String) current.getFieldValue(DESCRIPTOR_TYPE));
            if (descriptor != null)
               invocation.setDescriptor(descriptor);
         }
      }
   }
   
}

