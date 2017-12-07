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
package org.jboss.mx.server.registry;

import java.util.List;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

/**
 * Implementations of this interface can be plugged into the MBeanServer,
 * to control the registration and deregistration of MBeans. <p>
 * 
 * The registry can expose itself for management under the object name
 * defined the name defined in {@link ServerConstants}
 * When this is the case, the MBeanServer will perform the
 * register/unregister operations dynamically.
 * 
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 81022 $
 *
 * @see MBeanEntry
 */
public interface MBeanRegistry
{
   /**
    * Register an mbean.<p>
    *
    * This method is invoked by the MBeanServer for
    * createMBean() and registerMBean().<p>
    *
    * The object name passed maybe unqualified.<p>
    *
    * The map is a user definable string to value object map for holding
    * information against a registered object. This map may contain metadata
    * related to the registration, such as registration date/time, classloader
    * references, etc. <p>
    *
    * Pass {@link org.jboss.mx.server.ServerConstants#JMI_DOMAIN JMI_DOMAIN} in
    * both the key and values of the values map to get access to the reserved
    * domain. It is removed from the map during registration to save memory.<p>
    *
    * Pass {@link ServerConstants#CLASSLOADER CLASSLOADER} in the values map to 
    * set the context classloader<p>
    *
    * Other values are user definable and can be retrieved using the
    * getValue(ObjectName,String) method.
    *
    * @param object     the mbean to register.
    * @param name       the object name to assign to the mbean.
    * @param valueMap   a map of other information to include in the
    *                   registry
    *
    * @return an object instance for the registered mbean
    *
    * @exception InstanceAlreadyExistsException when the object name
    *            is already registered.
    * @exception MBeanRegistrationException when an exception is raised
    *            during preRegister for the mbean.
    * @exception NotCompliantMBeanException when the passed object is
    *            a valid mbean.
    */
   ObjectInstance registerMBean(Object object, ObjectName name, Map valueMap)
      throws InstanceAlreadyExistsException, 
             MBeanRegistrationException, 
             NotCompliantMBeanException;

   /**
    * Unregister an mbean. <p>
    *
    * This method is invoked by the MBeanServer for
    * unregisterMBean().<p>
    *
    * The object name passed maybe unqualified.<p>
    *
    * MBeans in JMImplementation cannot be unregistered
    *
    * @param name the mbean to unregister.
    *
    * @exception InstanceNotFoundException when the object name is
    *            not registered.
    * @exception MBeanRegistrationException when an exception is raised
    *            during preDeregister for the mbean.
    */
   void unregisterMBean(ObjectName name)
      throws InstanceNotFoundException, MBeanRegistrationException;

   /**
    * Retrieve the registration for an object name.<p>
    *
    * This method is invoked by the MBeanServer for
    * methods passing an ObjectName that are not covered in other methods.<p>
    *
    * The object name passed maybe unqualified.
    *
    * @param name the object name to retrieve
    *
    * @return the mbean's registration
    *
    * @exception InstanceNotFoundException when the object name is not
    *            registered.
    */
   public MBeanEntry get(ObjectName name)
      throws InstanceNotFoundException;
   
   /**
    * Retrieve the default domain for this registry.<p>
    *
    * @return the default domain
    */
   String getDefaultDomain();
   
   /**
    * Retrieve the domains for this registry.<p>
    *
    * @return the domains
    */
   String[] getDomains();

   /**
    * Retrieve the object instance for an object name.
    *
    * @param name the object name of the mbean
    * @return the object instance
    * @exception InstanceNotFoundException when the object name is not
    *            registered
    */
   public ObjectInstance getObjectInstance(ObjectName name)
      throws InstanceNotFoundException;

   /**
    * Retrieve a value from the registration.
    *
    * @param name the object name of the mbean
    * @param key the key to the value
    * @return the value or null if there is no such value
    * @exception InstanceNotFoundException when the object name is not
    *            registered
    */
   public Object getValue(ObjectName name, String key)
      throws InstanceNotFoundException;

   /**
    * Test whether an object name is registered. <p>
    *
    * This method is invoked by the MBeanServer for
    * isRegistered().<p>
    *
    * The object name passed maybe unqualified.
    *
    * @param name the object name
    * @return true when the object name is registered, false otherwise
    */
   boolean contains(ObjectName name);

   /**
    * Return a List of MBeanEntry objects with ObjectNames that match the 
    * specified pattern. <p>
    *
    * This method is invoked by the MBeanServer for
    * queryMBeans() and queryNames().
    *
    * @param pattern the pattern to match
    * @return a List of entries matching the pattern
    */
   public List findEntries(ObjectName pattern);
   
   /**
    * Retrieve the number of mbeans registered.<p>
    *
    * This method is invoked by the MBeanServer for
    * getMBeanCount().
    *
    * @return the number of mbeans registered.
    */
   int getSize();

   /**
    *  Invoked before the MBean server instance is released. Implementing
    *  this method lets the registry to process any clean up before the
    *  MBean server is shut down.
    */
   void releaseRegistry();

}
