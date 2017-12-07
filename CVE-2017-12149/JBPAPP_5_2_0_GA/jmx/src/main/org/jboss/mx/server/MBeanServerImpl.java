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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.security.ProtectionDomain;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.JMException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerDelegate;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.JMRuntimeException;
import javax.management.MBeanPermission;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanTrustPermission;
import javax.management.loading.ClassLoaderRepository;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.jboss.logging.Logger;
import org.jboss.mx.loading.LoaderRepository;
import org.jboss.mx.modelmbean.ModelMBeanConstants;
import org.jboss.mx.modelmbean.RequiredModelMBeanInstantiator;
import org.jboss.mx.notification.MBeanServerListenerRegistry;
import org.jboss.mx.server.registry.MBeanEntry;
import org.jboss.mx.server.registry.MBeanRegistry;
import org.jboss.mx.service.ServiceConstants;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.mx.util.PropertyAccess;
import org.jboss.util.NestedRuntimeException;

/**
 * MBean server implementation. 
 *
 * The MBean server behaviour can be configured by setting the following
 * system properties: <ul>
 *    <li><tt>jbossmx.loader.repository.class</tt>
 ({@link ServerConstants#LOADER_REPOSITORY_CLASS_PROPERTY LOADER_REPOSITORY_CLASS_PROPERTY})</li>
 *    <li><tt>jbossmx.mbean.registry.class</tt>
 ({@link ServerConstants#MBEAN_REGISTRY_CLASS_PROPERTY MBEAN_REGISTRY_CLASS_PROPERTY})</li>
 *    <li><tt>jbossmx.required.modelmbean.class</tt>
 ({@link ServerConstants#REQUIRED_MODELMBEAN_CLASS_PROPERTY REQUIRED_MODELMBEAN_CLASS_PROPERTY})</li>
 * </ul>
 *
 * The loader repository is used for managing class loaders in the MBean server.
 * The default repository uses the <tt>UnifiedLoaderRepository</tt> implementation
 * ({@link ServerConstants#DEFAULT_LOADER_REPOSITORY_CLASS DEFAULT_LOADER_REPOSITORY_CLASS}).<p>
 *
 * The default registry is
 * ({@link ServerConstants#DEFAULT_MBEAN_REGISTRY_CLASS DEFAULT_MBEAN_REGISTRY_CLASS}).<p>
 *
 * The <tt>RequiredModelMBean</tt> uses <tt>XMBean</tt> implementation by default
 * ({@link ServerConstants#DEFAULT_REQUIRED_MODELMBEAN_CLASS DEFAULT_REQUIRED_MODELMBEAN_CLASS}).
 *
 * @see javax.management.MBeanServer
 * @see javax.management.modelmbean.RequiredModelMBean
 * @see org.jboss.mx.server.ServerConstants
 * @see org.jboss.mx.loading.LoaderRepository
 * @see org.jboss.mx.loading.UnifiedLoaderRepository3
 * @see org.jboss.mx.modelmbean.XMBean
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @author  <a href="mailto:thomas.diesler@jboss.com">Thomas Diesler</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81022 $
 */
public class MBeanServerImpl
   implements MBeanServer, ServerConstants, ServiceConstants, ModelMBeanConstants
{
   // Constants ------------------------------------------------------

   /**
    * No parameters array
    */
   private static final Object[] NOPARAMS = new Object[0];

   /**
    * No signature array
    */
   private static final String[] NOSIG = new String[0];

   // Attributes ----------------------------------------------------

   /**
    * The wrapping MBeanServer
    */
   protected MBeanServer outer = null;

   /**
    * Registry used by this server to map MBean object names to resource references.
    */
   protected MBeanRegistry registry = null;

   /**
    * The notification registrations
    */
   private MBeanServerListenerRegistry listeners = new MBeanServerListenerRegistry();

   /**
    * This server's class loader repository
    */
   private ClassLoaderRepository classLoaderRepository;

   // Static --------------------------------------------------------

   /**
    * The logger
    */
   private static Logger log = Logger.getLogger(MBeanServerImpl.class);

   
   // Constructors --------------------------------------------------

   /**
    * Creates an MBean server implementation with a given default domain name and
    * registers the mandatory server delegate MBean to the server
    * ({@link ServerConstants#MBEAN_SERVER_DELEGATE MBEAN_SERVER_DELEGATE}).
    *
    * @param defaultDomain default domain name
    * @param outer the wrapping MBeanServer, passed to MBeans
    *        at registration.
    * @param delegate the delegate to use
    *        for Notifications.
    */
   public MBeanServerImpl(String defaultDomain, MBeanServer outer, MBeanServerDelegate delegate)
   {
      // Determine the MBeanServer to pass to MBeans
      if (outer == null)
         this.outer = this;
      else
         this.outer = outer;

      // the very first thing to do is to create a class loader repository
      this.classLoaderRepository = getClassLoaderRepository();

      // the second first thing to do is to create a registry instance
      this.registry = createRegistry(defaultDomain);
      
      // The first MBean to be registered should be the server delegate
      // to guarantee correct functionality (other MBeans may be dependent
      // on the existence of the delegate).
      try
      {
         // the magic token that allows us to register to the 
         // protected JMImplementation domain
         HashMap valueMap = new HashMap();
         valueMap.put(JMI_DOMAIN, JMI_DOMAIN);

         // register the delegate
         registry.registerMBean(delegate,
                 new ObjectName(MBEAN_SERVER_DELEGATE),
                 valueMap);
         
         // We expose the registry as an MBean for other components 
         ModelMBean rmm = RequiredModelMBeanInstantiator.instantiate();
         rmm.setModelMBeanInfo(getRegistryManagementInterface());
         rmm.setManagedResource(registry, "ObjectReference");

         // register the registry MBean
         registry.registerMBean(rmm, new ObjectName(MBEAN_REGISTRY), valueMap);
         
         // register the loader repository
         //String loaderClassMBeanName = classLoaderRepository.getClass().getName() + "MBean";
         //ClassLoader cl = classLoaderRepository.getClass().getClassLoader();
         //Class mbean = cl.loadClass(loaderClassMBeanName);
         
         //there must be a class with the MBean extension.
         ObjectName loaderName = new ObjectName(DEFAULT_LOADER_NAME);
         registry.registerMBean(classLoaderRepository, loaderName, valueMap);

      }
      catch (Exception e)
      {
         throw new RuntimeException("Cannot create MBeanServer", e);
      }
   }

   // MBeanServer implementation ------------------------------------

   public Object instantiate(String className)
           throws ReflectionException, MBeanException
   {
      return instantiate(className, (ClassLoader) null, NOPARAMS, NOSIG);
   }

   public Object instantiate(String className, Object[] params, String[] signature)
           throws ReflectionException, MBeanException
   {
      return instantiate(className, (ClassLoader) null, params, signature);
   }

   public Object instantiate(String className, ObjectName loaderName)
           throws ReflectionException, MBeanException, InstanceNotFoundException
   {
      return instantiate(className, loaderName, NOPARAMS, NOSIG);
   }

   public Object instantiate(String className, ObjectName loaderName, Object[] params, String[] signature)
           throws ReflectionException, MBeanException, InstanceNotFoundException
   {
      ClassLoader cl = null;

      // if instantiate() is called with null loader name, we use the cl that
      // loaded the MBean server (see javadoc)

      try
      {
         if (loaderName != null)
            cl = (ClassLoader) registry.get(loaderName).getResourceInstance();
      }
      catch (ClassCastException e)
      {
         throw new ReflectionException(e, loaderName + " is not a class loader.");
      }

      if (cl == null)
         cl = this.getClass().getClassLoader();
      if (cl == null)
         cl = ClassLoader.getSystemClassLoader();

      return instantiate(className, cl, params, signature);
   }

   public ObjectInstance createMBean(String className, ObjectName name)
           throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException
   {
      try
      {
         Object mbean = instantiate(className);
         return registerMBean(mbean, name, (ClassLoader) null);
      }
      catch (SecurityException e)
      {
         throw e;
      }
      catch (ReflectionException refex)
      {
         // Note, the CTS wants a NotCompliantMBeanException for this case
         if (refex.getCause() instanceof InstantiationException)
            throw new NotCompliantMBeanException("Cannot instanciate MBean: " + className);
         else
            throw refex;
      }
   }

   public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature)
           throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException
   {
      try
      {
         Object mbean = instantiate(className, params, signature);
         return registerMBean(mbean, name, (ClassLoader) null);
      }
      catch (ReflectionException refex)
      {
         return handleExceptionOnCreate(refex, className);
      }
   }

   public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName)
           throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException
   {
      try
      {
         Object mbean = instantiate(className, loaderName);
         return registerMBean(mbean, name, loaderName);
      }
      catch (ReflectionException refex)
      {
         return handleExceptionOnCreate(refex, className);
      }
   }

   public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature)
           throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException
   {
      try
      {
         Object mbean = instantiate(className, loaderName, params, signature);
         return registerMBean(mbean, name, loaderName);
      }
      catch (ReflectionException refex)
      {
         return handleExceptionOnCreate(refex, className);
      }
   }

   /**
    * The CTS wants a NotCompliantMBeanException in case the MBean cannot be created.
    * We need this, because instanciate cannot throw NotCompliantMBeanException.
    */
   private ObjectInstance handleExceptionOnCreate(ReflectionException refex, String className)
           throws NotCompliantMBeanException, ReflectionException
   {
      if (refex.getCause() instanceof InstantiationException)
         throw new NotCompliantMBeanException("Cannot instanciate MBean: " + className);

      throw refex;
   }

   /**
    * Registers a pre-existing object as an MBean with the MBean server. If the object name given is null,
    * the MBean must provide its own name by implementing the MBeanRegistration interface and returning the name
    * from the preRegister method.
    */
   public ObjectInstance registerMBean(Object object, ObjectName name)
           throws InstanceAlreadyExistsException,
           MBeanRegistrationException,
           NotCompliantMBeanException
   {
      return registerMBean(object, name, (ClassLoader) null);
   }

   public void unregisterMBean(ObjectName name)
           throws InstanceNotFoundException, MBeanRegistrationException
   {
      // Get the mbean to remove
      MBeanEntry entry = registry.get(name);
      Object mbean = entry.getResourceInstance();
      name = entry.getObjectName();

      checkMBeanPermission(entry.getResourceClassName(), null, name, "unregisterMBean");

      try
      {
         final Object[] args = {name};
         final String[] sig = {ObjectName.class.getName()};
         try
         {
            AccessController.doPrivileged(
               new PrivilegedExceptionAction()
               {
                  public Object run() throws Exception
                  {
                     return invoke(new ObjectName(MBEAN_REGISTRY),
                        "unregisterMBean", args, sig);
                  }
               }
            );
         }
         catch(PrivilegedActionException e)
         {
            throw e.getException();
         }
      }
      catch (Throwable t)
      {
         Throwable result = JMXExceptionDecoder.decodeToJMXException(t);
         if (result instanceof InstanceNotFoundException)
            throw (InstanceNotFoundException) result;
         if (result instanceof MBeanRegistrationException)
            throw (MBeanRegistrationException) result;
         if ( result instanceof JMRuntimeException )
            throw (JMRuntimeException) result;
         if (result instanceof MBeanException)
         {
            MBeanException e = (MBeanException) result;
            t = e.getTargetException();
            if (t instanceof InstanceNotFoundException)
               throw (InstanceNotFoundException) t;
            if (t instanceof MBeanRegistrationException)
               throw (MBeanRegistrationException) t;
         }
         if (result instanceof RuntimeException)
            throw new RuntimeMBeanException((RuntimeException) result);
         if (result instanceof Error)
            throw new RuntimeErrorException((Error) result);

         // for some other reason, unregistration failed
         throw new MBeanRegistrationException(new InvocationTargetException(t), "Cannot unregister MBean");
      }
      
      // Unregistration worked, remove any proxies for a broadcaster
      if (mbean instanceof NotificationBroadcaster)
         listeners.remove(name);
   }

   public ObjectInstance getObjectInstance(ObjectName name)
           throws InstanceNotFoundException
   {
      ObjectInstance oi = registry.getObjectInstance(name);
      checkMBeanPermission(oi.getClassName(), null, name,
         "getObjectInstance");

      return oi;
   }

   public Set queryMBeans(ObjectName name, QueryExp query)
   {
      // At least one mbean must be queriable
      checkMBeanPermission(null, null, null, "queryMBeans");

      // Set up the query
      Set result = new HashSet();
      if (query != null)
         query.setMBeanServer(outer);

      SecurityManager sm = System.getSecurityManager();
      // Get the possible MBeans
      List entries = registry.findEntries(name);
      Iterator iterator = entries.iterator();
      while (iterator.hasNext())
      {
         // Check each MBean against the query
         MBeanEntry entry = (MBeanEntry) iterator.next();
         ObjectName objectName = entry.getObjectName();
         // The permission check must be done before the query is applied
         if( sm != null )
         {
            try
            {
               checkMBeanPermission(entry.getResourceClassName(), null,
                  objectName, "queryMBeans");
            }
            catch(SecurityException e)
            {
               if( log.isTraceEnabled() )
                  log.trace("Excluded mbean due to security: "+objectName);
               continue;
            }            
         }
         // Check the mbean against the query
         if (queryMBean(objectName, query) == true)
         {
            try
            {
               ObjectInstance instance = registry.getObjectInstance(objectName);
               result.add(instance);
            }
            catch (InstanceNotFoundException ignored)
            {
            }
         }
      }

      return result;
   }

   public Set queryNames(ObjectName name, QueryExp query)
   {
      // At least one mbean must be queriable
      checkMBeanPermission(null, null, null, "queryNames");

      // Set up the query
      Set result = new HashSet();
      if (query != null)
         query.setMBeanServer(outer);

      SecurityManager sm = System.getSecurityManager();
      // Get the possible MBeans
      List entries = registry.findEntries(name);
      Iterator iterator = entries.iterator();
      while (iterator.hasNext())
      {
         // Check each MBean against the query
         MBeanEntry entry = (MBeanEntry) iterator.next();
         ObjectName objectName = entry.getObjectName();
         // The permission check must be done before the query is applied
         if( sm != null )
         {
            try
            {
               checkMBeanPermission(entry.getResourceClassName(), null,
                  objectName, "queryNames");
            }
            catch(SecurityException e)
            {
               if( log.isTraceEnabled() )
                  log.trace("Excluded mbean due to security: "+objectName);
               continue;
            }            
         }
         // Check the mbean against the query
         if (queryMBean(objectName, query) == true)
            result.add(objectName);
      }

      return result;
   }

   public boolean isRegistered(ObjectName name)
   {
      return registry.contains(name);
   }

   public java.lang.Integer getMBeanCount()
   {
      return new Integer(registry.getSize());
   }

   public Object getAttribute(ObjectName name, String attribute)
           throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException
   {
      MBeanEntry entry = registry.get(name);
      checkMBeanPermission(entry.getResourceClassName(), attribute, name,
         "getAttribute");

      MBeanInvoker mbean = entry.getInvoker();

      return mbean.getAttribute(attribute);
   }

   public AttributeList getAttributes(ObjectName name, String[] attributes)
           throws InstanceNotFoundException, ReflectionException
   {
      MBeanEntry entry = registry.get(name);
      String className = entry.getResourceClassName();
      /* Access to an attribute is required and this check will fail only
      if access to no attributes are allowed and will result in a security
      exception rather than an empty list.
      */
      checkMBeanPermission(className, null, name, "getAttribute");
      
      MBeanInvoker mbean = entry.getInvoker();
      AttributeList list = mbean.getAttributes(attributes);
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
      {
         // Remove any attributes that are not allowed
         Iterator iter = list.iterator();
         while( iter.hasNext() )
         {
            Attribute attr = (Attribute) iter.next();
            String aname = attr.getName();
            try
            {
               checkMBeanPermission(className, aname, name, "getAttribute");
            }
            catch(SecurityException e)
            {
               if( log.isTraceEnabled() )
                  log.trace("Excluded attribute due to security: "+aname);
               iter.remove();
            }
         }
      }
      return list;
   }

   public void setAttribute(ObjectName name, Attribute attribute)
           throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
   {
      MBeanEntry entry = registry.get(name);
      String attributeName = null;
      if (attribute != null)
         attributeName = attribute.getName();
      checkMBeanPermission(entry.getResourceClassName(), attributeName,
         name, "setAttribute");

      MBeanInvoker mbean = entry.getInvoker();

      mbean.setAttribute(attribute);
   }

   public AttributeList setAttributes(ObjectName name, AttributeList attributes)
           throws InstanceNotFoundException, ReflectionException
   {
      MBeanEntry entry = registry.get(name);

      String className = entry.getResourceClassName();
      /* Access to an attribute is required and this check will fail only
      if access to no attributes are allowed and will result in a security
      exception rather than an empty list.
      */
      checkMBeanPermission(className, null, name, "setAttribute");

      MBeanInvoker mbean = entry.getInvoker();
      AttributeList list = mbean.setAttributes(attributes);
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
      {
         // Remove any attributes that are not allowed
         Iterator iter = list.iterator();
         while( iter.hasNext() )
         {
            Attribute attr = (Attribute) iter.next();
            String aname = attr.getName();
            try
            {
               checkMBeanPermission(className, aname, name, "setAttribute");
            }
            catch(SecurityException e)
            {
               if( log.isTraceEnabled() )
                  log.trace("Excluded attribute due to security: "+aname);
               iter.remove();
            }
         }
      }
      return list;
   }

   public Object invoke(ObjectName name, String operationName, Object[] params,
      String[] signature)
      throws InstanceNotFoundException, MBeanException, ReflectionException
   {
      MBeanEntry entry = registry.get(name);
      checkMBeanPermission(entry.getResourceClassName(), operationName, name,
         "invoke");

      MBeanInvoker mbean = entry.getInvoker();

      return mbean.invoke(operationName, params, signature);
   }

   public MBeanInfo getMBeanInfo(ObjectName name)
      throws InstanceNotFoundException, IntrospectionException,
      ReflectionException
   {
      MBeanEntry entry = registry.get(name);
      checkMBeanPermission(entry.getResourceClassName(), null, name,
         "getMBeanInfo");
      try
      {
         MBeanInvoker invoker = entry.getInvoker();
         return invoker.getMBeanInfo();
      }
      catch (Exception e)
      {
         JMException result = ExceptionHandler.handleException(e);
         if (result instanceof InstanceNotFoundException)
            throw (InstanceNotFoundException) result;
         if (result instanceof IntrospectionException)
            throw (IntrospectionException) result;
         if (result instanceof ReflectionException)
            throw (ReflectionException) result;
         throw new RuntimeException("Cannot obtain MBeanInfo " + name, result);
      }
   }

   public String getDefaultDomain()
   {
      return registry.getDefaultDomain();
   }

   public String[] getDomains()
   {
      checkMBeanPermission(null, null, null, "getDomains");
      String[] domains = registry.getDomains();
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
      {
         ArrayList tmp = new ArrayList();
         // Remove any domains that are not allowed
         int length = domains != null ? domains.length : 0;
         for(int n = 0; n < length; n ++)
         {
            String domain = domains[n];
            try
            {
               ObjectName name = new ObjectName(domain, "x", "x");
               checkMBeanPermission(null, null, name, "getDomains");
               tmp.add(domain);
            }
            catch(MalformedObjectNameException e)
            {
               // Should not be possible
            }
            catch(SecurityException e)
            {
               if( log.isTraceEnabled() )
                  log.trace("Excluded domain due to security: "+domain);
            }
         }
         domains = new String[tmp.size()];
         tmp.toArray(domains);
      }
      return domains;
   }

   /**
    * Adds a listener to a registered MBean.
    *
    * A notification emitted by the MBean will be forwarded by the MBeanServer to the listener.
    * If the source of the notification is a reference to the MBean object, the MBean server will replace
    * it by the MBean's ObjectName. Otherwise the source is unchanged.
    */
   public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback)
           throws InstanceNotFoundException
   {
      MBeanEntry entry = registry.get(name);
      if (NotificationBroadcaster.class.isInstance(entry.getResourceInstance()) == false)
         throw new RuntimeOperationsException(new IllegalArgumentException("The MBean " + name + " exists but does not implement the NotificationBroadcaster interface."));

      if (listener == null)
         throw new RuntimeOperationsException(new IllegalArgumentException("Cannot add null listener"));

      checkMBeanPermission(entry.getResourceClassName(), null, name,
         "addNotificationListener");

      ClassLoader newTCL = entry.getClassLoader();
      NotificationBroadcaster broadcaster = entry.getInvoker();

      ClassLoader oldTCL = TCLAction.UTIL.getContextClassLoader();
      final boolean setCl = newTCL != oldTCL && newTCL != null;
      try
      {
         if (setCl)
            TCLAction.UTIL.setContextClassLoader(newTCL);

         listeners.add(entry.getObjectName(), broadcaster, listener, filter, handback);
      }
      finally
      {
         if (setCl)
            TCLAction.UTIL.setContextClassLoader(oldTCL);
      }
   }

   /**
    * Adds a listener to a registered MBean.
    *
    * A notification emitted by the MBean will be forwarded by the MBeanServer to the listener.
    * If the source of the notification is a reference to the MBean object, the MBean server will replace
    * it by the MBean's ObjectName. Otherwise the source is unchanged.
    *
    * The listener object that receives notifications is the one that is registered with the given name at the time this
    * method is called. Even if it is subsequently unregistered, it will continue to receive notifications.
    */
   public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback)
           throws InstanceNotFoundException
   {
      MBeanEntry entry = registry.get(name);
      if (NotificationBroadcaster.class.isInstance(entry.getResourceInstance()) == false)
         throw new RuntimeOperationsException(new IllegalArgumentException("The MBean " + name + " exists but does not implement the NotificationBroadcaster interface."));

      MBeanEntry listenerEntry = registry.get(listener);
      if (NotificationListener.class.isInstance(listenerEntry.getResourceInstance()) == false)
         throw new RuntimeOperationsException(new IllegalArgumentException("The MBean " + listener + " exists but does not implement the NotificationListener interface."));

      checkMBeanPermission(entry.getResourceClassName(), null, name,
         "addNotificationListener");

      ClassLoader newTCL = entry.getClassLoader();
      NotificationBroadcaster broadcaster = entry.getInvoker();

      ClassLoader oldTCL = TCLAction.UTIL.getContextClassLoader();
      final boolean setCl = newTCL != oldTCL && newTCL != null;
      try
      {
         if (setCl)
            TCLAction.UTIL.setContextClassLoader(newTCL);

         listeners.add(entry.getObjectName(), broadcaster,
                 (NotificationListener) registry.get(listener).getResourceInstance(), filter, handback);
      }
      finally
      {
         if (setCl)
            TCLAction.UTIL.setContextClassLoader(oldTCL);
      }
   }

   /**
    * Removes a listener from a registered MBean.
    *
    * If the listener is registered more than once, perhaps with different filters or callbacks,
    * this method will remove all those registrations.
    */
   public void removeNotificationListener(ObjectName name, NotificationListener listener)
           throws InstanceNotFoundException, ListenerNotFoundException
   {
      MBeanEntry entry = registry.get(name);
      if (NotificationBroadcaster.class.isInstance(entry.getResourceInstance()) == false)
         throw new RuntimeOperationsException(new IllegalArgumentException("The MBean " + name + " exists but does not implement the NotificationBroadcaster interface."));

      checkMBeanPermission(entry.getResourceClassName(), null, name,
         "removeNotificationListener");

      ClassLoader newTCL = entry.getClassLoader();

      ClassLoader oldTCL = TCLAction.UTIL.getContextClassLoader();
      final boolean setCl = newTCL != oldTCL && newTCL != null;
      try
      {
         if (setCl)
            TCLAction.UTIL.setContextClassLoader(newTCL);

         listeners.remove(entry.getObjectName(), listener);
      }
      finally
      {
         if (setCl)
            TCLAction.UTIL.setContextClassLoader(oldTCL);
      }
   }

   /**
    * Removes a listener from a registered MBean.
    *
    * If the listener is registered more than once, perhaps with different filters or callbacks,
    * this method will remove all those registrations.
    */
   public void removeNotificationListener(ObjectName name, ObjectName listener)
           throws InstanceNotFoundException, ListenerNotFoundException
   {
      MBeanEntry entry = registry.get(name);
      if (NotificationBroadcaster.class.isInstance(entry.getResourceInstance()) == false)
         throw new RuntimeOperationsException(new IllegalArgumentException("The MBean " + name + " exists but does not implement the NotificationBroadcaster interface."));

      MBeanEntry listenerEntry = registry.get(listener);
      if (NotificationListener.class.isInstance(listenerEntry.getResourceInstance()) == false)
         throw new RuntimeOperationsException(new IllegalArgumentException("The MBean " + listener + " exists but does not implement the NotificationListener interface."));

      checkMBeanPermission(entry.getResourceClassName(), null, name,
         "removeNotificationListener");

      ClassLoader newTCL = entry.getClassLoader();

      ClassLoader oldTCL = TCLAction.UTIL.getContextClassLoader();
      final boolean setCl = newTCL != oldTCL && newTCL != null;
      try
      {
         if (setCl)
            TCLAction.UTIL.setContextClassLoader(newTCL);

         listeners.remove(entry.getObjectName(), (NotificationListener) registry.get(listener).getResourceInstance());
      }
      finally
      {
         if (setCl)
            TCLAction.UTIL.setContextClassLoader(oldTCL);
      }
   }

   /**
    * Removes a listener from a registered MBean.
    *
    * The MBean must have a listener that exactly matches the given listener, filter, and handback parameters.
    * If there is more than one such listener, only one is removed.
    *
    * The filter and handback parameters may be null if and only if they are null in a listener to be removed.
    */
   public void removeNotificationListener(ObjectName name,
      NotificationListener listener, NotificationFilter filter, Object handback)
      throws InstanceNotFoundException, ListenerNotFoundException
   {
      MBeanEntry entry = registry.get(name);
      if (NotificationBroadcaster.class.isInstance(entry.getResourceInstance()) == false)
         throw new RuntimeOperationsException(new IllegalArgumentException("The MBean " + name + " exists but does not implement the NotificationBroadcaster interface."));

      checkMBeanPermission(entry.getResourceClassName(), null, name,
         "removeNotificationListener");

      ClassLoader newTCL = entry.getClassLoader();

      ClassLoader oldTCL = TCLAction.UTIL.getContextClassLoader();
      final boolean setCl = newTCL != oldTCL && newTCL != null;
      try
      {
         if (setCl)
            TCLAction.UTIL.setContextClassLoader(newTCL);

         listeners.remove(entry.getObjectName(), listener, filter, handback);
      }
      finally
      {
         if (setCl)
            TCLAction.UTIL.setContextClassLoader(oldTCL);
      }
   }

   /**
    * Removes a listener from a registered MBean.
    *
    * The MBean must have a listener that exactly matches the given listener, filter, and handback parameters.
    * If there is more than one such listener, only one is removed.
    *
    * The filter and handback parameters may be null if and only if they are null in a listener to be removed.
    */
   public void removeNotificationListener(ObjectName name, ObjectName listener,
      NotificationFilter filter, Object handback)
      throws InstanceNotFoundException, ListenerNotFoundException
   {
      MBeanEntry entry = registry.get(name);
      if (NotificationBroadcaster.class.isInstance(entry.getResourceInstance()) == false)
         throw new RuntimeOperationsException(new IllegalArgumentException("The MBean " + name + " exists but does not implement the NotificationBroadcaster interface."));

      MBeanEntry listenerEntry = registry.get(listener);
      if (NotificationListener.class.isInstance(listenerEntry.getResourceInstance()) == false)
         throw new RuntimeOperationsException(new IllegalArgumentException("The MBean " + listener + " exists but does not implement the NotificationListener interface."));

      checkMBeanPermission(entry.getResourceClassName(), null, name,
         "removeNotificationListener");

      ClassLoader newTCL = entry.getClassLoader();

      ClassLoader oldTCL = TCLAction.UTIL.getContextClassLoader();
      final boolean setCl = newTCL != oldTCL && newTCL != null;
      try
      {
         if (setCl)
            TCLAction.UTIL.setContextClassLoader(newTCL);

         listeners.remove(entry.getObjectName(), (NotificationListener) registry.get(listener).getResourceInstance(),
                 filter, handback);
      }
      finally
      {
         if (setCl)
            TCLAction.UTIL.setContextClassLoader(oldTCL);
      }
   }

   public boolean isInstanceOf(ObjectName name, String className)
           throws InstanceNotFoundException
   {
      // Get the MBean's class name
      MBeanEntry entry = registry.get(name);
      String mbeanClassName = entry.getResourceClassName();
      checkMBeanPermission(mbeanClassName, null, name, "isInstanceOf");

      // The names are the same
      if (className.equals(mbeanClassName))
         return true;

      // Try to load both classes
      Class mbeanClass = null;
      Class testClass = null;
      ClassLoader cl = getClassLoaderFor(name);
      try
      {
         mbeanClass = cl.loadClass(mbeanClassName);
         testClass = cl.loadClass(className);
      }
      catch (ClassNotFoundException e)
      {
         return false;
      }

      // Check whether it is assignable
      if (testClass.isAssignableFrom(mbeanClass))
         return true;
      else
         return false;
   }

   /**
    * @deprecated
    */
   public ObjectInputStream deserialize(ObjectName name, byte[] data) throws InstanceNotFoundException, OperationsException
   {
      try
      {
         ClassLoader cl = this.getClassLoaderFor(name);
         return new ObjectInputStreamWithClassLoader(new ByteArrayInputStream(data), cl);
      }
      catch (IOException e)
      {
         throw new OperationsException("I/O exception deserializing: " + e.getMessage());
      }
   }

   /**
    * @deprecated
    */
   public ObjectInputStream deserialize(String className, byte[] data)
      throws OperationsException, ReflectionException
   {
      try
      {
         Class c = this.getClassLoaderRepository().loadClass(className);
         ClassLoader cl = c.getClassLoader();
         return new ObjectInputStreamWithClassLoader(new ByteArrayInputStream(data), cl);
      }
      catch (IOException e)
      {
         throw new OperationsException("I/O exception deserializing: " + e.getMessage());
      }
      catch (ClassNotFoundException e)
      {
         throw new ReflectionException(e, "Class not found from default repository: " + className);
      }
   }

   /**
    * @deprecated
    */
   public ObjectInputStream deserialize(String className, ObjectName loaderName,
      byte[] data)
      throws InstanceNotFoundException, OperationsException, ReflectionException
   {
      try
      {
         ClassLoader cl = this.getClassLoader(loaderName);
         return new ObjectInputStreamWithClassLoader(new ByteArrayInputStream(data), cl);
      }
      catch (IOException e)
      {
         throw new OperationsException("I/O exception deserializing: " + e.getMessage());
      }
   }

   public ClassLoader getClassLoaderFor(ObjectName name)
           throws InstanceNotFoundException
   {
      MBeanEntry entry = registry.get(name);
      checkMBeanPermission(entry.getResourceClassName(), null, name,
         "getClassLoaderFor");
      
      ClassLoader cl = entry.getClassLoader();
      if (cl == null)
         cl = entry.getResourceInstance().getClass().getClassLoader();
      if (cl == null)
         cl = ClassLoader.getSystemClassLoader();
      return cl;
   }

   /**
    * 
    * @param name The ObjectName of the ClassLoader. May be null, in which case
    * the MBean server's own ClassLoader is returned.
    * @return
    * @throws InstanceNotFoundException
    */ 
   public ClassLoader getClassLoader(ObjectName name)
           throws InstanceNotFoundException
   {
      Object loader = null;
      if( name == null )
      {
         checkMBeanPermission(null, null, name, "getClassLoader");
         loader = getClass().getClassLoader();
         if (loader == null)
            loader = ClassLoader.getSystemClassLoader();
      }
      else
      {
         MBeanEntry entry = registry.get(name);
         checkMBeanPermission(entry.getResourceClassName(), null, name,
            "getClassLoader");
         loader = entry.getResourceInstance();
      }

      if ((loader instanceof ClassLoader) == false)
         throw new InstanceNotFoundException("Not a classloader " + name);
      return (ClassLoader) loader;
   }

   /**
    * Retrieve the classloader repository for this mbean server
    *
    * @return the classloader repository
    */
   public ClassLoaderRepository getClassLoaderRepository()
   {
      checkMBeanPermission(null, null, null, "getClassLoaderRepository");

      // we don't need to synchronize, because this is the first thing we do in the constructor
      if (classLoaderRepository == null)
      {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         String className = PropertyAccess.getProperty(LOADER_REPOSITORY_CLASS_PROPERTY, DEFAULT_LOADER_REPOSITORY_CLASS);
         PropertyAccess.setProperty(LOADER_REPOSITORY_CLASS_PROPERTY, className);

         try
         {
            Class repository = cl.loadClass(className);
            classLoaderRepository = (LoaderRepository) repository.newInstance();
         }
         catch (ClassNotFoundException e)
         {
            throw new Error("Cannot instantiate loader repository class: " + className);
         }
         catch (ClassCastException e)
         {
            throw new Error("Loader repository is not an instance of LoaderRepository: " + className);
         }
         catch (Exception e)
         {
            throw new Error("Error creating loader repository: " + e);
         }
      }

      return classLoaderRepository;
   }


   public void releaseServer()
   {
      //   shutdown the loader repository
//      try
//      {
//         invoke(new ObjectName(DEFAULT_LOADER_NAME),
//                "releaseLoaderRepository",
//                new Object[0],
//                new String[0] );
//      }
//      catch (Exception e)
//      {
//         log.error("Unable to shutdown loader repository");
//         e.printStackTrace();
//      }

      registry.releaseRegistry();
      listeners.removeAll();
      listeners = null;
      registry = null;
   }


   // Protected -----------------------------------------------------

   /**
    * Instantiate an object, the passed classloader is set as the
    * thread's context classloader for the duration of this method.
    *
    * @param className the class name of the object to instantiate
    * @param cl the thread classloader, pass null to use the ClassLoaderRepository
    * @param params the parameters for the constructor
    * @param signature the signature of the constructor
    * @exception ReflectionException wraps a ClassCastException or
    *            any Exception trying to invoke the constructor
    * @exception MBeanException wraps any exception thrown by the constructor
    * @exception RuntimeOperationsException Wraps an IllegalArgument for a
    *            null className
    */
   protected Object instantiate(String className, ClassLoader cl, Object[] params, String[] signature)
           throws ReflectionException, MBeanException
   {
      if (className == null)
         throw new RuntimeOperationsException(new IllegalArgumentException("Null className"));

      if (className.equals(""))
         throw new ReflectionException(new ClassNotFoundException("empty class name"));

      if (params == null)
         params = NOPARAMS;

      if (signature == null)
         signature = NOSIG;

      checkMBeanPermission(className, null, null, "instantiate");

      ClassLoader oldTCL = TCLAction.UTIL.getContextClassLoader();

      boolean setCl = false;
      try
      {
         Class clazz = null;
         if (cl != null)
         {
            if (cl != oldTCL)
            {
               setCl = true;
               TCLAction.UTIL.setContextClassLoader(cl);
            }
            clazz = cl.loadClass(className);
         }
         else
            clazz = classLoaderRepository.loadClass(className);

         Class[] sign = new Class[signature.length];
         for (int i = 0; i < signature.length; ++i)
         {
            if (LoaderRepository.getNativeClassForName(signature[i]) == null)
            {
               try
               {
                  if (cl != null)
                     sign[i] = cl.loadClass(signature[i]);
                  else
                     sign[i] = classLoaderRepository.loadClass(signature[i]);
               }
               catch (ClassNotFoundException e)
               {
                  throw new ReflectionException(e, "Constructor parameter class not found: " + signature[i]);
               }
            }
            else
            {
               sign[i] = LoaderRepository.getNativeClassForName(signature[i]);
            }
         }

         Constructor constructor = clazz.getConstructor(sign);
         return constructor.newInstance(params);
      }
      catch (Throwable t)
      {
         handleInstantiateExceptions(t, className);
         log.error("Unhandled exception instantiating class: " + className, t);
         return null;
      }
      finally
      {
         if (setCl)
            TCLAction.UTIL.setContextClassLoader(oldTCL);
      }
   }

   /**
    * Handles errors thrown during class instantiation
    */
   protected void handleInstantiateExceptions(Throwable t, String className)
           throws ReflectionException, MBeanException
   {
      if (t instanceof ReflectionException)
         throw (ReflectionException) t;

      else if (t instanceof ClassNotFoundException)
         throw new ReflectionException((Exception) t, "Class not found: " + className);

      else if (t instanceof InstantiationException)
         throw new ReflectionException((Exception) t, "Cannot instantiate: " + className);

      else if (t instanceof IllegalAccessException)
         throw new ReflectionException((Exception) t, "Illegal access to constructor: " + className);

      else if (t instanceof NoSuchMethodException)
         throw new ReflectionException((Exception) t, "Cannot find such a public constructor: " + className);

      else if (t instanceof SecurityException)
         throw new ReflectionException((Exception) t, "Can't access constructor for " + className);

      else if (t instanceof InvocationTargetException)
      {
         Throwable root = ((InvocationTargetException) t).getTargetException();

         if (root instanceof RuntimeException)
            throw new RuntimeMBeanException((RuntimeException) root, className + " constructor has thrown an exception: " + root.toString());
         else if (root instanceof Error)
            throw new RuntimeErrorException((Error) root, className + " constructor has thrown an error: " + root.toString());
         else if (root instanceof Exception)
            throw new MBeanException((Exception) root, className + " constructor has thrown an exception: " + root.toString());

         throw new Error("Something went wrong with handling the exception from " + className + " default constructor.");
      }

      else if (t instanceof ExceptionInInitializerError)
      {
         Throwable root = ((ExceptionInInitializerError) t).getException();

         // the root cause can be only a runtime exception
         if (root instanceof RuntimeException)
            throw new RuntimeMBeanException((RuntimeException) root, "Exception in class " + className + " static initializer: " + root.toString());
         else
         // shouldn't get here
            throw new Error("ERROR: it turns out the root cause is not always a runtime exception!");
      }

      else if (t instanceof IllegalArgumentException)
      {
         // if mismatch between constructor instance args and supplied args -- shouldn't happen
         throw new Error("Error in the server: mismatch between expected constructor arguments and supplied arguments.");
      }

      else if (t instanceof Error)
      {
         throw new RuntimeErrorException((Error) t, "instantiating " + className + " failed: " + t.toString());
      }
   }


   /**
    * Register an MBean<p>
    *
    * The classloader is used as the thread context classloader during
    * access to the mbean and it's interceptors
    *
    * @param mbean the mbean to register
    * @param name the object name to register
    * @param loaderName the object name of a class loader also used as
    *        as the MBeans TCL
    * @exception InstanceAlreadyExistsException when already registered
    * @exception MBeanRegistrationException when
    *            preRegister(MBeanServer, ObjectName) throws an exception
    * @exception NotCompliantMBeanException when the object is not an MBean
    */
   protected ObjectInstance registerMBean(Object mbean, ObjectName name, ObjectName loaderName)
           throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException
   {
      ClassLoader cl = null;

      //  If the loader name is null, the ClassLoader that loaded the MBean Server will be used.
      if (loaderName == null)
      {
         cl = getClass().getClassLoader();
         if (cl == null)
            cl = ClassLoader.getSystemClassLoader();
      }
      else
      {
         try
         {
            cl = (ClassLoader) registry.get(loaderName).getResourceInstance();
         }
         catch (ClassCastException e)
         {
            throw new ReflectionException(e, loaderName + " is not a class loader.");
         }
      }

      return registerMBean(mbean, name, cl);
   }

   /**
    * Register an MBean<p>
    *
    * The classloader is used as the thread context classloader during
    * access to the mbean and it's interceptors
    *
    * @param object the mbean to register
    * @param name the object name to register
    * @param cl the thread classloader, pass null for the current one
    * @exception InstanceAlreadyExistsException when already registered
    * @exception MBeanRegistrationException when
    *            preRegister(MBeanServer, ObjectName) throws an exception
    * @exception NotCompliantMBeanException when the object is not an MBean
    */
   protected ObjectInstance registerMBean(Object object, ObjectName name,
                                          ClassLoader cl)
           throws InstanceAlreadyExistsException,
           MBeanRegistrationException,
           NotCompliantMBeanException
   {
      final Class objectClass = object.getClass();
      String className = objectClass.getName();

      // Check that the caller has the ability to create/register mbeans
      checkMBeanPermission(className, null, name, "registerMBean");

      // Check that the mbean class is from a trusted source
      if( System.getSecurityManager() != null )
      {
         ProtectionDomain pd = (ProtectionDomain) AccessController.doPrivileged(
            new PrivilegedAction()
            {
               public Object run()
               {
                  return objectClass.getProtectionDomain();
               }
            }
         );
         if( pd != null )
         {
            MBeanTrustPermission p = new MBeanTrustPermission("register");
            if( pd.implies(p) == false )
            {
               String msg = "MBeanTrustPermission(register) not implied by "
                  + "protection domain of mbean class: "+className+", pd: "+pd;
               throw new SecurityException(msg);
            }
         }
      }

      HashMap valueMap = null;
      if (cl != null)
      {
         valueMap = new HashMap();
         valueMap.put(CLASSLOADER, cl);
      }

      try
      {
         final Object[] args = {object, name, valueMap};
         final String[] sig = {Object.class.getName(),
            ObjectName.class.getName(), Map.class.getName()};
         try
         {
            ObjectInstance oi = (ObjectInstance) AccessController.doPrivileged(
               new PrivilegedExceptionAction()
               {
                  public Object run() throws Exception
                  {
                     return invoke(new ObjectName(MBEAN_REGISTRY),
                        "registerMBean", args, sig);
                  }
               }
            );
            return oi;
         }
         catch(PrivilegedActionException e)
         {
            throw e.getException();
         }
      }
      catch (Throwable t)
      {
         Throwable result = JMXExceptionDecoder.decodeToJMXException(t);
         if (result instanceof InstanceAlreadyExistsException)
            throw (InstanceAlreadyExistsException) result;
         if (result instanceof MBeanRegistrationException)
            throw (MBeanRegistrationException) result;
         if (result instanceof NotCompliantMBeanException)
            throw (NotCompliantMBeanException) result;
         if ( result instanceof JMRuntimeException )
            throw (JMRuntimeException) result;
         if (result instanceof MBeanException)
         {
            MBeanException e = (MBeanException) result;
            t = e.getTargetException();
            if (t instanceof InstanceAlreadyExistsException)
               throw (InstanceAlreadyExistsException) t;
            if (t instanceof MBeanRegistrationException)
               throw (MBeanRegistrationException) t;
            if (t instanceof NotCompliantMBeanException)
               throw (NotCompliantMBeanException) t;
         }
         if (result instanceof RuntimeException)
            throw new RuntimeMBeanException((RuntimeException) result);
         if (result instanceof Error)
            throw new RuntimeErrorException((Error) result);

         // for some other reason, registration failed
         throw new MBeanRegistrationException(new InvocationTargetException(t), "Cannot register MBean");
      }
   }

   // Private -------------------------------------------------------

   /**
    * Query an MBean against the query
    *
    * @param objectName the object name of the mbean to check
    * @param queryExp the query expression to test
    * @return true when the query applies to the MBean or the query is null,
    *         false otherwise.
    */
   protected boolean queryMBean(ObjectName objectName, QueryExp queryExp)
   {
      if (queryExp == null)
         return true;

      try
      {
         return queryExp.apply(objectName);
      }
      catch (Exception e)
      {
         return false;
      }
   }


   protected MBeanRegistry createRegistry(String defaultDomain)
   {
      // Find the registry implementation class: can be configured via
      // MBEAN_REGISTRY_CLASS_PROPERTY by the client -- if not found use
      // the class defined in DEFAULT_MBEAN_REGISTRY_CLASS (see ServerConstants)
      String registryClass = PropertyAccess.getProperty(ServerConstants.MBEAN_REGISTRY_CLASS_PROPERTY,
              ServerConstants.DEFAULT_MBEAN_REGISTRY_CLASS);

      try
      {
         // Try loading registry class via thread context classloader
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         Class clazz = cl.loadClass(registryClass);

         // retrieve the constructor <init>(MBeanServer srvr, String defaultDomain, ClassLoaderRepository clr)
         Constructor constructor = clazz.getConstructor(new Class[] {MBeanServer.class, String.class, ClassLoaderRepository.class});

         // instantiate registry
         return (MBeanRegistry) constructor.newInstance(new Object[] {outer, defaultDomain, classLoaderRepository});
      }
      // Any exception preventing the registry to be created will cause the agent to fail.
      // However, try to give detailed exception messages to indicate config errors.
      catch (ClassNotFoundException e)
      {
         throw new NestedRuntimeException("The MBean registry implementation class " + registryClass +
                 " was not found: ", e);
      }
      catch (NoSuchMethodException e)
      {
         throw new NestedRuntimeException("The MBean registry implementation class " + registryClass +
                 " must contain a default <init>(MBeanServer srvr, String domain) " +
                 " constructor.", e);
      }
      catch (InstantiationException e)
      {
         throw new NestedRuntimeException("Cannot instantiate class " + registryClass + ": ", e);
      }
      catch (IllegalAccessException e)
      {
         throw new NestedRuntimeException("Unable to create the MBean registry instance. Illegal access " +
                 "to class " + registryClass + " constructor: ", e);
      }
      catch (InvocationTargetException e)
      {
         throw new NestedRuntimeException("Unable to create the MBean registry instance. Class " + registryClass +
                 " has raised an exception in constructor: ", e.getTargetException());
      }
   }


   // Private -------------------------------------------------------
   
   // FIXME: externalize this
   private ModelMBeanInfo getRegistryManagementInterface()
   {
      final boolean READABLE = true;
      final boolean WRITABLE = true;
      final boolean BOOLEAN = true;

      // Default Domain attribute
      DescriptorSupport descDefaultDomain = new DescriptorSupport();
      descDefaultDomain.setField("name", "DefaultDomain");
      descDefaultDomain.setField("descriptorType", "attribute");
      descDefaultDomain.setField("displayName", "Default Domain");
      descDefaultDomain.setField("default", getDefaultDomain());
      descDefaultDomain.setField("currencyTimeLimit", "-1");
      ModelMBeanAttributeInfo defaultDomainInfo =
              new ModelMBeanAttributeInfo
                      ("DefaultDomain", String.class.getName(),
                              "The domain to use when an object name has no domain",
                              READABLE, !WRITABLE, !BOOLEAN,
                              descDefaultDomain);

      // Size attribute
      DescriptorSupport descSize = new DescriptorSupport();
      descSize.setField("name", "Size");
      descSize.setField("descriptorType", "attribute");
      descSize.setField("displayName", "Size");
      descSize.setField("getMethod", "getSize");
      ModelMBeanAttributeInfo sizeInfo =
              new ModelMBeanAttributeInfo
                      ("Size", Integer.TYPE.getName(),
                              "The number of MBeans registered in the MBean Server",
                              READABLE, !WRITABLE, !BOOLEAN,
                              descSize);

      // registerMBean operation
      DescriptorSupport descRegisterMBean = new DescriptorSupport();
      descRegisterMBean.setField("name", "registerMBean");
      descRegisterMBean.setField("descriptorType", "operation");
      descRegisterMBean.setField("role", "operation");
      MBeanParameterInfo[] registerMBeanParms =
              new MBeanParameterInfo[]
              {
                 new MBeanParameterInfo
                         ("Resource",
                                 Object.class.getName(),
                                 "A compliant MBean to be registered in the MBean Server"),
                 new MBeanParameterInfo
                         ("ObjectName",
                                 ObjectName.class.getName(),
                                 "The object name of the MBean"),
                 new MBeanParameterInfo
                         ("ValueMap",
                                 Map.class.getName(),
                                 "Values associated with the registration"),
              };
      ModelMBeanOperationInfo registerMBeanInfo =
              new ModelMBeanOperationInfo
                      ("registerMBean",
                              "Adds an MBean in the MBeanServer",
                              registerMBeanParms,
                              ObjectInstance.class.getName(),
                              ModelMBeanOperationInfo.ACTION_INFO,
                              descRegisterMBean);

      // unregisterMBean operation
      DescriptorSupport descUnregisterMBean = new DescriptorSupport();
      descUnregisterMBean.setField("name", "unregisterMBean");
      descUnregisterMBean.setField("descriptorType", "operation");
      descUnregisterMBean.setField("role", "operation");
      MBeanParameterInfo[] unregisterMBeanParms =
              new MBeanParameterInfo[]
              {
                 new MBeanParameterInfo
                         ("ObjectName",
                                 ObjectName.class.getName(),
                                 "The object name of the MBean to remove")
              };
      ModelMBeanOperationInfo unregisterMBeanInfo =
              new ModelMBeanOperationInfo
                      ("unregisterMBean",
                              "Removes an MBean from the MBeanServer",
                              unregisterMBeanParms,
                              Void.TYPE.getName(),
                              ModelMBeanOperationInfo.ACTION,
                              descUnregisterMBean);

      // getSize operation
      DescriptorSupport descGetSize = new DescriptorSupport();
      descGetSize.setField("name", "getSize");
      descGetSize.setField("descriptorType", "operation");
      descGetSize.setField("role", "getter");
      MBeanParameterInfo[] getSizeParms = new MBeanParameterInfo[0];
      ModelMBeanOperationInfo getSizeInfo =
              new ModelMBeanOperationInfo
                      ("getSize",
                              "Gets the number of MBeans registered",
                              getSizeParms,
                              Integer.TYPE.getName(),
                              ModelMBeanOperationInfo.INFO,
                              descGetSize);

      // get operation
      DescriptorSupport descGet = new DescriptorSupport();
      descGet.setField("name", "get");
      descGet.setField("descriptorType", "operation");
      descGet.setField("role", "operation");
      MBeanParameterInfo[] getParam = new MBeanParameterInfo[1];
      getParam[0] = new MBeanParameterInfo("ObjectName", ObjectName.class.getName(), "object name to find");
      ModelMBeanOperationInfo getInfo =
              new ModelMBeanOperationInfo
                      ("get",
                              "Gets the MBeanEntry for a given ObjectName",
                              getParam,
                              MBeanEntry.class.getName(),
                              ModelMBeanOperationInfo.INFO,
                              descGet);

      // getValue operation
      DescriptorSupport descGetValue = new DescriptorSupport();
      descGetValue.setField("name", "getValue");
      descGetValue.setField("descriptorType", "operation");
      descGetValue.setField("role", "operation");
      MBeanParameterInfo[] getValueParms = new MBeanParameterInfo[]
      {
         new MBeanParameterInfo
                 ("ObjectName",
                         ObjectName.class.getName(),
                         "The object name of the registered MBean"),
         new MBeanParameterInfo
                 ("Key",
                         String.class.getName(),
                         "The key to the value stored")
      };
      ModelMBeanOperationInfo getValueInfo =
              new ModelMBeanOperationInfo
                      ("getValue",
                              "Get a value stored in the MBean's registration",
                              getValueParms,
                              Object.class.getName(),
                              ModelMBeanOperationInfo.INFO,
                              descGetValue);

      // Construct the modelmbean
      DescriptorSupport descMBean = new DescriptorSupport();
      descMBean.setField("name", RequiredModelMBeanInstantiator.getClassName());
      descMBean.setField("descriptorType", "MBean");
      descMBean.setField("displayName", "MBeanServer Registry");
      ModelMBeanAttributeInfo[] attrInfo = new ModelMBeanAttributeInfo[]
      {
         defaultDomainInfo,
         sizeInfo
      };
      ModelMBeanConstructorInfo[] ctorInfo = null;
      ModelMBeanOperationInfo[] opInfo = new ModelMBeanOperationInfo[]
      {
         registerMBeanInfo,
         unregisterMBeanInfo,
         getSizeInfo,
         getValueInfo,
         getInfo
      };
      ModelMBeanNotificationInfo[] notifyInfo = null;
      ModelMBeanInfoSupport info = new ModelMBeanInfoSupport
              (RequiredModelMBeanInstantiator.getClassName(),
                      "Managed Bean Registry",
                      attrInfo,
                      ctorInfo,
                      opInfo,
                      notifyInfo,
                      descMBean);

      return info;
   }

   private void checkMBeanPermission(String className, String member,
      ObjectName objectName, String action)
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
      {
         MBeanPermission p = new MBeanPermission(className, member, objectName,
            action);
         sm.checkPermission(p);
      }
   }
   
   // Object overrides ----------------------------------------------
   
   /**
    * Simple toString() revealing default domain
    */
   public String toString()
   {
      return super.toString() + "[ defaultDomain='" + this.getDefaultDomain() + "' ]";
   }   
}
