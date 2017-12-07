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
package javax.management.j2ee;

import javax.management.*;
import javax.ejb.EJBObject;
import java.rmi.RemoteException;
import java.util.Set;

/**
 * The Management interface provides the APIs to navigate and manipulate managed objects.
 * The J2EE Management EJB component (MEJB) must implement this as its remote interface.
 * 
 * @author thomas.diesler@jboss.org
 */
public interface Management extends EJBObject
{
   /**
    * Gets the names of managed objects controlled by the MEJB.
    * This method enables any of the following to be obtained:
    * The names of all managed objects, the names of a set of managed objects specified by pattern matching on the
    * ObjectName, a specific managed object name (equivalent to testing whether a managed object is registered).
    * When the object name is null or no domain and key properties are specified, all objects are selected.
    * It returns the set of J2EEObjectNames for the managed objects selected.
    *
    * @param name The object name pattern identifying the managed objects to be retrieved. If null or no domain and key properties are specified, all the managed objects registered will be retrieved.
    * @return A set containing the ObjectNames for the managed objects selected. If no managed object satisfies the query, an empty set is returned.
    * @throws RemoteException A communication exception occurred during the execution of a remote method call
    */
   public Set queryNames(ObjectName name, QueryExp query)
           throws RemoteException;

   /**
    * Checks whether a managed object, identified by its object name, is already registered with the MEJB.
    *
    * @param name The object name of the managed object to be checked.
    * @return True if the managed object is already registered in the MEJB, false otherwise.
    * @throws RemoteException A communication exception occurred during the execution of a remote method call
    */
   public boolean isRegistered(ObjectName name)
           throws RemoteException;


   /**
    * Returns the number of managed objects registered in the MEJB.
    *
    * @throws RemoteException A communication exception occurred during the execution of a remote method call
    */
   public Integer getMBeanCount()
           throws RemoteException;

   /**
    * This method discovers the attributes and operations that a managed object exposes for management.
    *
    * @param name The name of the managed object to analyze
    * @return An instance of MBeanInfo allowing the retrieval of all attributes and operations of this managed object.
    * @throws IntrospectionException    An exception occurs during introspection.
    * @throws InstanceNotFoundException The managed object specified is not found.
    * @throws ReflectionException       An exception occurred when trying to perform reflection on a managed object
    * @throws RemoteException           A communication exception occurred during the execution of a remote method call
    */
   public MBeanInfo getMBeanInfo(ObjectName name)
           throws IntrospectionException, InstanceNotFoundException, ReflectionException, RemoteException;


   /**
    * Gets the value of a specific attribute of a named managed object. The managed object is identified by its object name.
    *
    * @param name      The object name of the managed object from which the attribute is to be retrieved.
    * @param attribute A String specifying the name of the attribute to be retrieved.
    * @return The value of the retrieved attribute.
    * @throws AttributeNotFoundException - The attribute specified is not accessible in the managed object.
    * @throws MBeanException             - Wraps an exception thrown by the managed object's getter.
    * @throws InstanceNotFoundException  - The managed object specified is not registered in the MEJB.
    * @throws ReflectionException        - An exception occurred when trying to invoke the getAttribute method of a Dynamic MBean
    * @throws RemoteException            - A communication exception occurred during the execution of a remote method call getAttributes
    */
   public Object getAttribute(ObjectName name, String attribute)
           throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, RemoteException;

   /**
    * Enables the values of several attributes of a named managed object. The managed object is identified by its object name.
    *
    * @param name       The object name of the managed object from which the attributes are retrieved.
    * @param attributes A list of the attributes to be retrieved.
    * @return The list of the retrieved attributes.
    * @throws InstanceNotFoundException - The managed object specified is not registered in the MEJB.
    * @throws ReflectionException       - An exception occurred when trying to invoke the getAttributes method of a Dynamic MBean.
    * @throws RemoteException           - A communication exception occurred during the execution of a remote method call
    */
   public AttributeList getAttributes(ObjectName name, String[] attributes)
           throws InstanceNotFoundException, ReflectionException, RemoteException;

   /**
    * Sets the value of a specific attribute of a named managed object. The managed object is identified by its object name.
    *
    * @param name      The name of the managed object within which the attribute is to be set.
    * @param attribute The identification of the attribute to be set and the value it is to be set to.
    * @throws InstanceNotFoundException
    * @throws AttributeNotFoundException
    * @throws InstanceNotFoundException      - The managed object specified is not registered in the MEJB.
    * @throws AttributeNotFoundException     - The attribute specified is not accessible in the managed object.
    * @throws InvalidAttributeValueException - The value specified for the attribute is not valid.
    * @throws MBeanException                 - Wraps an exception thrown by the managed object's setter.
    * @throws ReflectionException            - An exception occurred when trying to invoke the setAttribute method of a Dynamic MBean.
    * @throws RemoteException                - A communication exception occurred during the execution of a remote method call setAttributes
    */
   public void setAttribute(ObjectName name, Attribute attribute)
           throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException, RemoteException;


   /**
    * Sets the values of several attributes of a named managed object. The managed object is identified by its object name.
    *
    * @param name       The object name of the managed object within which the attributes are to be set.
    * @param attributes A list of attributes: The identification of the attributes to be set and the values they are to be set to.
    * @return The list of attributes that were set, with their new values.
    * @throws InstanceNotFoundException - The managed object specified is not registered in the MEJB.
    * @throws ReflectionException       - An exception occurred when trying to invoke the setAttributes method of a Dynamic MBean.
    * @throws RemoteException           - A communication exception occurred during the execution of a remote method call invoke
    */
   public AttributeList setAttributes(ObjectName name, AttributeList attributes)
           throws InstanceNotFoundException, ReflectionException, RemoteException;

   /**
    * Invokes an operation on a managed object
    *
    * @param name          The object name of the managed object on which the method is to be invoked.
    * @param operationName The name of the operation to be invoked.
    * @param params        An array containing the parameters to be set when the operation is invoked
    * @param signature     An array containing the signature of the operation. The class objects will be loaded using the same class loader as the one used for loading the managed object on which the operation was invoked.
    * @return The object returned by the operation, which represents the result of invoking the operation on the managed object specified.
    * @throws InstanceNotFoundException - The managed object specified is not registered in the MEJB.
    * @throws MBeanException            - Wraps an exception thrown by the managed object's invoked method.
    * @throws ReflectionException       - Wraps a java.lang.Exception thrown while trying to invoke the method.
    * @throws RemoteException           - A communication exception occurred during the execution of a remote method call getDefaultDomain
    */
   public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
           throws InstanceNotFoundException, MBeanException, ReflectionException, RemoteException;


   /**
    * Returns the default domain name of this MEJB.
    *
    * @throws RemoteException RemoteException - A communication exception occurred during the execution of a remote method call getListenerRegistry
    */
   public String getDefaultDomain() throws RemoteException;

   /**
    * Returns the listener registry implementation for this MEJB. The listener registry implements the methods that enable clints to add and remove event notification listeners managed objects
    *
    * @return An implementation of javax.management.j2ee.ListenerRegistration
    * @throws RemoteException - A communication exception occurred during the execution of a remote method call
    */
   public ListenerRegistration getListenerRegistry()
           throws RemoteException;

}

