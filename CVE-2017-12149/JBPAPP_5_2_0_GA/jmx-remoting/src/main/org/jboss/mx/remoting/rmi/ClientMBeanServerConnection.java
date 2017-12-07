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
package org.jboss.mx.remoting.rmi;

import java.io.IOException;
import java.io.NotSerializableException;
import java.rmi.MarshalledObject;
import java.util.Set;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.remote.rmi.RMIConnection;
import javax.security.auth.Subject;

/**
 * This is the class that is passed to the client and is called
 * on by the client to make the remote MBeanServer calls.  This
 * is really just a delegate to the rmi connection.
 *
 * @author <a href="mailto:tom.elrod@jboss.com">Tom Elrod</a>
 */
public class ClientMBeanServerConnection implements MBeanServerConnection
{
   private RMIConnection connection = null;
   private Subject subject = null;
   private transient ClassLoader defaultClassLoader = null;
   private ClientNotifier clientNotifier = null;


   public ClientMBeanServerConnection(RMIConnection rmiConnection, ClientNotifier clientNotifier,
                                      ClassLoader classLoader, Subject subject)
   {
      this.connection = rmiConnection;
      this.clientNotifier = clientNotifier;
      this.defaultClassLoader = classLoader;
      this.subject = subject;
   }

   private ClassLoader activateDefaultClassLoader()
   {
      final ClassLoader current = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(defaultClassLoader);
      return current;
   }

   private void deActivateDefaultClassLoader(ClassLoader previousClassLoader)
   {
      Thread.currentThread().setContextClassLoader(previousClassLoader);
   }

   /**
    * Create an MBean registered using the given object name.<p>
    * <p/>
    * Uses the default contructor.
    *
    * @param className the class name of the mbean
    * @param name      the object name for registration, can be null
    * @return an ObjectInstance describing the registration
    * @throws javax.management.ReflectionException
    *                             for class not found or an exception
    *                             invoking the contructor
    * @throws javax.management.InstanceAlreadyExistsException
    *                             for an MBean already registered
    *                             with the passed or generated ObjectName
    * @throws javax.management.MBeanRegistrationException
    *                             for any exception thrown by the
    *                             MBean's preRegister
    * @throws javax.management.MBeanException
    *                             for any exception thrown by the MBean's constructor
    * @throws javax.management.NotCompliantMBeanException
    *                             if the class name does not correspond to
    *                             a valid MBean
    * @throws javax.management.RuntimeOperationsException
    *                             wrapping an IllegalArgumentException for a
    *                             null class name, the ObjectName could not be determined or it is a pattern
    * @throws java.io.IOException for a communication problem during this operation
    */
   public ObjectInstance createMBean(String className, ObjectName name)
         throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException,
                MBeanException, NotCompliantMBeanException, IOException
   {
      final ClassLoader current = activateDefaultClassLoader();

      try
      {
         return connection.createMBean(className, name, subject);
      }
      finally
      {
         deActivateDefaultClassLoader(current);
      }
   }

   /**
    * Create an MBean registered using the given object name.<p>
    * <p/>
    * The MBean is loaded using the passed classloader. Uses the default contructor.
    *
    * @param className  the class name of the mbean
    * @param loaderName an MBean that implements a classloader
    * @param name       the object name for registration, can be null
    * @return an ObjectInstance describing the registration
    * @throws javax.management.ReflectionException
    *                             for class not found or an exception
    *                             invoking the contructor
    * @throws javax.management.InstanceAlreadyExistsException
    *                             for an MBean already registered
    *                             with the passed or generated ObjectName
    * @throws javax.management.MBeanRegistrationException
    *                             for any exception thrown by the
    *                             MBean's preRegister
    * @throws javax.management.MBeanException
    *                             for any exception thrown by the MBean's constructor
    * @throws javax.management.InstanceNotFoundException
    *                             if the loaderName is not a classloader registered
    *                             in the MBeanServer
    * @throws javax.management.NotCompliantMBeanException
    *                             if the class name does not correspond to
    *                             a valid MBean
    * @throws javax.management.RuntimeOperationsException
    *                             wrapping an IllegalArgumentException for a
    *                             null class name, the ObjectName could not be determined or it is a pattern
    * @throws java.io.IOException for a communication problem during this operation
    */
   public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName)
         throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException,
                MBeanException, NotCompliantMBeanException, InstanceNotFoundException, IOException
   {
      final ClassLoader current = activateDefaultClassLoader();

      try
      {
         return connection.createMBean(className, name, loaderName, subject);
      }
      finally
      {
         deActivateDefaultClassLoader(current);
      }

   }

   /**
    * Create an MBean registered using the given object name.<p>
    * <p/>
    * Uses the specified constructor.
    *
    * @param className the class name of the mbean
    * @param name      the object name for registration, can be null
    * @param params    the parameters for the constructor
    * @param signature the signature of the constructor
    * @return an ObjectInstance describing the registration
    * @throws javax.management.ReflectionException
    *                             for class not found or an exception
    *                             invoking the contructor
    * @throws javax.management.InstanceAlreadyExistsException
    *                             for an MBean already registered
    *                             with the passed or generated ObjectName
    * @throws javax.management.MBeanRegistrationException
    *                             for any exception thrown by the
    *                             MBean's preRegister
    * @throws javax.management.MBeanException
    *                             for any exception thrown by the MBean's constructor
    * @throws javax.management.NotCompliantMBeanException
    *                             if the class name does not correspond to
    *                             a valid MBean
    * @throws javax.management.RuntimeOperationsException
    *                             wrapping an IllegalArgumentException for a
    *                             null class name, the ObjectName could not be determined or it is a pattern
    * @throws java.io.IOException for a communication problem during this operation
    */
   public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature)
         throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException,
                MBeanException, NotCompliantMBeanException, IOException
   {
      MarshalledObject args = new MarshalledObject(params);
      final ClassLoader current = activateDefaultClassLoader();

      try
      {
         return connection.createMBean(className, name, args, signature, subject);
      }
      finally
      {
         deActivateDefaultClassLoader(current);
      }
   }

   /**
    * Create an MBean registered using the given object name.<p>
    * <p/>
    * The MBean is loaded using the passed classloader. Uses the specified constructor.
    *
    * @param className  the class name of the mbean
    * @param loaderName an MBean that implements a classloader
    * @param name       the object name for registration, can be null
    * @param params     the parameters for the constructor
    * @param signature  the signature of the constructor
    * @return an ObjectInstance describing the registration
    * @throws javax.management.ReflectionException
    *                             for class not found or an exception
    *                             invoking the contructor
    * @throws javax.management.InstanceAlreadyExistsException
    *                             for an MBean already registered
    *                             with the passed or generated ObjectName
    * @throws javax.management.MBeanRegistrationException
    *                             for any exception thrown by the
    *                             MBean's preRegister
    * @throws javax.management.MBeanException
    *                             for any exception thrown by the MBean's constructor
    * @throws javax.management.InstanceNotFoundException
    *                             if the loaderName is not a classloader registered
    *                             in the MBeanServer
    * @throws javax.management.NotCompliantMBeanException
    *                             if the class name does not correspond to
    *                             a valid MBean
    * @throws javax.management.RuntimeOperationsException
    *                             wrapping an IllegalArgumentException for a
    *                             null class name, the ObjectName could not be determined or it is a pattern
    * @throws java.io.IOException for a communication problem during this operation
    */
   public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName,
                                     Object[] params, String[] signature)
         throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException,
                MBeanException, NotCompliantMBeanException, InstanceNotFoundException, IOException
   {
      MarshalledObject args = new MarshalledObject(params);
      final ClassLoader current = activateDefaultClassLoader();

      try
      {
         return connection.createMBean(className, name, loaderName, args, signature, subject);
      }
      finally
      {
         deActivateDefaultClassLoader(current);
      }
   }

   /**
    * Unregisters an mbean.
    *
    * @param name the object name of the mbean to unregister
    * @throws javax.management.InstanceNotFoundException
    *                             if the mbean is not registered
    *                             in the MBeanServer
    * @throws javax.management.MBeanRegistrationException
    *                             for any exception thrown by the
    *                             MBean's preDeregister
    * @throws javax.management.RuntimeOperationsException
    *                             wrapping an IllegalArgumentException for a
    *                             null name, or trying to unregister a JMX implementation MBean
    * @throws java.io.IOException for a communication problem during this operation
    */
   public void unregisterMBean(ObjectName name) throws InstanceNotFoundException, MBeanRegistrationException, IOException
   {
      final ClassLoader current = activateDefaultClassLoader();

      try
      {
         connection.unregisterMBean(name, subject);
      }
      finally
      {
         deActivateDefaultClassLoader(current);
      }
   }

   /**
    * Retrieve an MBean's registration information.
    *
    * @param name the object name of the mbean
    * @throws javax.management.InstanceNotFoundException
    *                             if the mbean is not registered
    *                             in the MBeanServer
    * @throws java.io.IOException for a communication problem during this operation
    */
   public ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException, IOException
   {
      final ClassLoader current = activateDefaultClassLoader();

      try
      {
         return connection.getObjectInstance(name, subject);
      }
      finally
      {
         deActivateDefaultClassLoader(current);
      }
   }

   /**
    * Retrieve a set of Object instances
    *
    * @param name  an ObjectName pattern, can be null for all mbeans
    * @param query a query expression to further filter the mbeans, can be null
    *              for no query
    * @throws java.io.IOException for a communication problem during this operation
    */
   public Set queryMBeans(ObjectName name, QueryExp query) throws IOException
   {
      MarshalledObject args = new MarshalledObject(query);
      final ClassLoader current = activateDefaultClassLoader();

      try
      {
         return connection.queryMBeans(name, args, subject);
      }
      finally
      {
         deActivateDefaultClassLoader(current);
      }
   }

   /**
    * Retrieve a set of Object names
    *
    * @param name  an ObjectName pattern, can be null for all mbeans
    * @param query a query expression to further filter the mbeans, can be null
    *              for no query
    * @throws java.io.IOException for a communication problem during this operation
    */
   public Set queryNames(ObjectName name, QueryExp query) throws IOException
   {
      MarshalledObject args = new MarshalledObject(query);
      final ClassLoader current = activateDefaultClassLoader();

      try
      {
         return connection.queryNames(name, args, subject);
      }
      finally
      {
         deActivateDefaultClassLoader(current);
      }
   }

   /**
    * Test whether an mbean is registered.
    *
    * @param name the object name of the mbean
    * @return true when the mbean is registered, false otherwise
    * @throws javax.management.RuntimeOperationsException
    *                             wrapping an IllegalArgumentException for a
    *                             null name
    * @throws java.io.IOException for a communication problem during this operation
    */
   public boolean isRegistered(ObjectName name) throws IOException
   {
      final ClassLoader current = activateDefaultClassLoader();

      try
      {
         return connection.isRegistered(name, subject);
      }
      finally
      {
         deActivateDefaultClassLoader(current);
      }
   }

   /**
    * Retrieve the number of mbeans registered in the server.
    *
    * @return true the number of registered mbeans
    * @throws java.io.IOException for a communication problem during this operation
    */
   public Integer getMBeanCount() throws IOException
   {
      final ClassLoader current = activateDefaultClassLoader();

      try
      {
         return connection.getMBeanCount(subject);
      }
      finally
      {
         deActivateDefaultClassLoader(current);
      }
   }

   /**
    * Retrieve a value from an MBean.
    *
    * @param name      the object name of the mbean
    * @param attribute the attribute name of the value to retrieve
    * @return the value
    * @throws javax.management.ReflectionException
    *                             for an exception invoking the mbean
    * @throws javax.management.MBeanException
    *                             for any exception thrown by the mbean
    * @throws javax.management.InstanceNotFoundException
    *                             if the mbean is not registered
    * @throws javax.management.AttributeNotFoundException
    *                             if the mbean has no such attribute
    * @throws javax.management.RuntimeOperationsException
    *                             wrapping an IllegalArgumentException for a
    *                             null name or attribute
    * @throws java.io.IOException for a communication problem during this operation
    */
   public Object getAttribute(ObjectName name, String attribute)
         throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException
   {
      final ClassLoader current = activateDefaultClassLoader();

      try
      {
         return connection.getAttribute(name, attribute, subject);
      }
      finally
      {
         deActivateDefaultClassLoader(current);
      }
   }

   /**
    * Retrieve a list of values from an MBean.
    *
    * @param name       the object name of the mbean
    * @param attributes the attribute names of the values to retrieve
    * @return the list of values, attributes with errors are ignored
    * @throws javax.management.ReflectionException
    *                             for an exception invoking the mbean
    * @throws javax.management.InstanceNotFoundException
    *                             if the mbean is not registered
    * @throws javax.management.RuntimeOperationsException
    *                             wrapping an IllegalArgumentException for a
    *                             null name or attributes
    * @throws java.io.IOException for a communication problem during this operation
    */
   public AttributeList getAttributes(ObjectName name, String[] attributes)
         throws InstanceNotFoundException, ReflectionException, IOException
   {
      final ClassLoader current = activateDefaultClassLoader();

      try
      {
         return connection.getAttributes(name, attributes, subject);
      }
      finally
      {
         deActivateDefaultClassLoader(current);
      }
   }

   /**
    * Set a value for an MBean.
    *
    * @param name      the object name of the mbean
    * @param attribute the attribute name and value to set
    * @throws javax.management.ReflectionException
    *                             for an exception invoking the mbean
    * @throws javax.management.MBeanException
    *                             for any exception thrown by the mbean
    * @throws javax.management.InstanceNotFoundException
    *                             if the mbean is not registered
    * @throws javax.management.AttributeNotFoundException
    *                             if the mbean has no such attribute
    * @throws javax.management.InvalidAttributeValueException
    *                             if the new value has an incorrect type
    * @throws javax.management.RuntimeOperationsException
    *                             wrapping an IllegalArgumentException for a
    *                             null name or attribute
    * @throws java.io.IOException for a communication problem during this operation
    */
   public void setAttribute(ObjectName name, Attribute attribute)
         throws InstanceNotFoundException, AttributeNotFoundException,
                InvalidAttributeValueException, MBeanException, ReflectionException, IOException
   {
      MarshalledObject args = new MarshalledObject(attribute);
      final ClassLoader current = activateDefaultClassLoader();

      try
      {
         connection.setAttribute(name, args, subject);
      }
      finally
      {
         deActivateDefaultClassLoader(current);
      }
   }

   /**
    * Set a list of values for an MBean.
    *
    * @param name       the object name of the mbean
    * @param attributes the attribute names and values to set
    * @return the list of values, attributes with errors are ignored
    * @throws javax.management.ReflectionException
    *                             for an exception invoking the mbean
    * @throws javax.management.InstanceNotFoundException
    *                             if the mbean is not registered
    * @throws javax.management.RuntimeOperationsException
    *                             wrapping an IllegalArgumentException for a
    *                             null name or attributes
    * @throws java.io.IOException for a communication problem during this operation
    */
   public AttributeList setAttributes(ObjectName name, AttributeList attributes)
         throws InstanceNotFoundException, ReflectionException, IOException
   {
      MarshalledObject args = new MarshalledObject(attributes);
      final ClassLoader current = activateDefaultClassLoader();

      try
      {
         return connection.setAttributes(name, args, subject);
      }
      finally
      {
         deActivateDefaultClassLoader(current);
      }

   }

   /**
    * Invokes an operation on an mbean.
    *
    * @param name          the object name of the mbean
    * @param operationName the operation to perform
    * @param params        the parameters
    * @param signature     the signature of the operation
    * @return any result of the operation
    * @throws javax.management.ReflectionException
    *                             for an exception invoking the mbean
    * @throws javax.management.MBeanException
    *                             for any exception thrown by the mbean
    * @throws javax.management.InstanceNotFoundException
    *                             if the mbean is not registered
    * @throws java.io.IOException for a communication problem during this operation
    */
   public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
         throws InstanceNotFoundException, MBeanException, ReflectionException, IOException
   {
      MarshalledObject args = new MarshalledObject(params);
      final ClassLoader current = activateDefaultClassLoader();

      try
      {
         return connection.invoke(name, operationName, args, signature, subject);
      }
      finally
      {
         deActivateDefaultClassLoader(current);
      }

   }

   /**
    * Retrieve the default domain of the mbeanserver.
    *
    * @return the default domain
    * @throws java.io.IOException for a communication problem during this operation
    */
   public String getDefaultDomain() throws IOException
   {
      final ClassLoader current = activateDefaultClassLoader();

      try
      {
         return connection.getDefaultDomain(subject);
      }
      finally
      {
         deActivateDefaultClassLoader(current);
      }
   }

   /**
    * Retrieve the domains of the mbeanserver.
    *
    * @return the domains
    * @throws java.io.IOException for a communication problem during this operation
    */
   public String[] getDomains() throws IOException
   {
      final ClassLoader current = activateDefaultClassLoader();

      try
      {
         return connection.getDomains(subject);
      }
      finally
      {
         deActivateDefaultClassLoader(current);
      }
   }

   /**
    * Add a notification listener to an MBean.
    *
    * @param name     the name of the MBean broadcasting notifications
    * @param listener the listener to add
    * @param filter   a filter to preprocess notifications
    * @param handback a object to add to any notifications
    * @throws javax.management.InstanceNotFoundException
    *                             if the broadcaster is not registered
    * @throws java.io.IOException for a communication problem during this operation
    */
   public void addNotificationListener(ObjectName name, NotificationListener listener,
                                       NotificationFilter filter, Object handback)
         throws InstanceNotFoundException, IOException
   {
      ClientListenerHolder holder = new ClientListenerHolder(name, listener, filter, handback);
      // if this listener already exists within the ClientNotifier, then assume has already been
      // properly registered as a listener and can ignore call
      if(clientNotifier.exists(holder))
      {
         return;
      }
      else
      {
         MarshalledObject marshalledFilter = null;

         if(filter != null)
         {
            try
            {
               marshalledFilter = new MarshalledObject(filter);
            }
            catch(IOException e)
            {
               holder.setFilterOnClient(true);
            }
         }

         Integer[] listenerIDs = connection.addNotificationListeners(new ObjectName[] {name}, new MarshalledObject[]{marshalledFilter},
                                                                    new Subject[]{subject});
          clientNotifier.addNotificationListener(listenerIDs[0], holder);
      }

   }

   /**
    * Add a notification listener to an MBean.
    *
    * @param name     the name of the MBean broadcasting notifications
    * @param listener the object name listener to add
    * @param filter   a filter to preprocess notifications
    * @param handback a object to add to any notifications
    * @throws javax.management.InstanceNotFoundException
    *                             if the broadcaster or listener is not registered
    * @throws javax.management.RuntimeOperationsException
    *                             wrapping an IllegalArgumentException for a
    *                             null listener or the listener does not implement the Notification Listener interface
    * @throws java.io.IOException for a communication problem during this operation
    */
   public void addNotificationListener(ObjectName name, ObjectName listener,
                                       NotificationFilter filter, Object handback)
         throws InstanceNotFoundException, IOException
   {
      MarshalledObject marshalledFilter = filter != null ? new MarshalledObject(filter) : null;
      MarshalledObject marshalledHandback = handback != null ? new MarshalledObject(handback) : null;
      connection.addNotificationListener(name, listener, marshalledFilter, marshalledHandback, subject);
   }

   /**
    * Removes a listener from an mbean.<p>
    * <p/>
    * All registrations of the listener are removed.
    *
    * @param name     the name of the MBean broadcasting notifications
    * @param listener the object name of the listener to remove
    * @throws javax.management.InstanceNotFoundException
    *                             if the broadcaster or listener is not registered
    * @throws javax.management.ListenerNotFoundException
    *                             if the listener is not registered against the broadcaster
    * @throws java.io.IOException for a communication problem during this operation
    */
   public void removeNotificationListener(ObjectName name, ObjectName listener)
         throws InstanceNotFoundException, ListenerNotFoundException, IOException
   {
      connection.removeNotificationListener(name, listener, subject);
   }

   /**
    * Removes a listener from an mbean.<p>
    * <p/>
    * Only the listener that was registered with the same filter and handback is removed.
    *
    * @param name     the name of the MBean broadcasting notifications
    * @param listener the object name of listener to remove
    * @param filter   the filter of the listener to remove
    * @throws javax.management.InstanceNotFoundException
    *                             if the broadcaster or listener is not registered
    * @throws javax.management.ListenerNotFoundException
    *                             if the listener, filter, handback
    *                             is not registered against the broadcaster
    * @throws java.io.IOException for a communication problem during this operation
    */
   public void removeNotificationListener(ObjectName name, ObjectName listener,
                                          NotificationFilter filter, Object handback)
         throws InstanceNotFoundException, ListenerNotFoundException, IOException
   {
      MarshalledObject marshalledFilter = filter != null ? new MarshalledObject(filter) : null;
      MarshalledObject marshalledHandback = handback != null ? new MarshalledObject(handback) : null;
      connection.removeNotificationListener(name, listener, marshalledFilter, marshalledHandback, subject);
   }

   /**
    * Removes a listener from an mbean.<p>
    * <p/>
    * All registrations of the listener are removed.
    *
    * @param name     the name of the MBean broadcasting notifications
    * @param listener the listener to remove
    * @throws javax.management.InstanceNotFoundException
    *                             if the broadcaster is not registered
    * @throws javax.management.ListenerNotFoundException
    *                             if the listener is not registered against the broadcaster
    * @throws java.io.IOException for a communication problem during this operation
    */
   public void removeNotificationListener(ObjectName name, NotificationListener listener)
         throws InstanceNotFoundException, ListenerNotFoundException, IOException
   {
      Integer[] ids = clientNotifier.getListeners(name, listener);
      if(ids == null || ids.length == 0)
      {
         throw new ListenerNotFoundException("Listener (" + listener + ") not found as a registered listener.");
      }
      connection.removeNotificationListeners(name, ids, subject);
      clientNotifier.removeListeners(ids);
   }

   /**
    * Removes a listener from an mbean.<p>
    * <p/>
    * Only the listener that was registered with the same filter and handback is removed.
    *
    * @param name     the name of the MBean broadcasting notifications
    * @param listener the listener to remove
    * @param filter   the filter of the listener to remove
    * @throws javax.management.InstanceNotFoundException
    *                             if the broadcaster is not registered
    * @throws javax.management.ListenerNotFoundException
    *                             if the listener, filter, handback
    *                             is not registered against the broadcaster
    * @throws java.io.IOException for a communication problem during this operation
    */
   public void removeNotificationListener(ObjectName name, NotificationListener listener,
                                          NotificationFilter filter, Object handback)
         throws InstanceNotFoundException, ListenerNotFoundException, IOException
   {
      Integer id = clientNotifier.getListener(new ClientListenerHolder(name, listener, filter, handback));
      if(id == null)
      {
         throw new ListenerNotFoundException("Listener (" + listener + ") could not be found as registered listener.");
      }

      Integer[] ids = new Integer[]{id};
      connection.removeNotificationListeners(name, ids, subject);
      clientNotifier.removeListeners(ids);
   }

   /**
    * Retrieves the jmx metadata for an mbean
    *
    * @param name the name of the mbean
    * @return the metadata
    * @throws javax.management.IntrospectionException
    *                             for any error during instrospection
    * @throws javax.management.InstanceNotFoundException
    *                             if the mbean is not registered
    * @throws javax.management.ReflectionException
    *                             for any error trying to invoke the operation on the mbean
    * @throws java.io.IOException for a communication problem during this operation
    */
   public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException
   {
      final ClassLoader current = activateDefaultClassLoader();

      try
      {
         return connection.getMBeanInfo(name, subject);
      }
      finally
      {
         deActivateDefaultClassLoader(current);
      }

   }

   /**
    * Tests whether an mbean can be cast to the given type
    *
    * @param name      the name of the mbean
    * @param className the class name to check
    * @return true when it is of that type, false otherwise
    * @throws javax.management.InstanceNotFoundException
    *                             if the mbean is not registered
    * @throws java.io.IOException for a communication problem during this operation
    */
   public boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException, IOException
   {
      final ClassLoader current = activateDefaultClassLoader();

      try
      {
         return connection.isInstanceOf(name, className, subject);
      }
      finally
      {
         deActivateDefaultClassLoader(current);
      }
   }
}