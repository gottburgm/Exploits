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
package org.jboss.services.binding;

import java.net.UnknownHostException;
import java.util.Set;


/** 
 * SPI through which {@link ServiceBindingManager} interacts with a store 
 * of service binding metadata.
 *
 * @version $Revision: 85945 $
 * @author <a href="mailto:bitpushr@rochester.rr.com">Mike Finn</a>.
 * @author Scott.Stark@jboss.org
 * @author Brian Stansberry
 */
public interface ServiceBindingStore 
{
   /** 
    * Obtain a ServiceBinding object for the given server name, target
    * service and binding name.
    *
    * @param serverName the {@link ServiceBindingManager#getServerName() name identifying the server instance}
    *  in which the service is running.
    * @param serviceName the name of the service
    * @param bindingName the name of the binding, or <code>null</code> to indicate
    *                    the default binding.
    * @return the ServiceBinding if one exists for the <serverName, serviceName, bindingName>
    *         tuple.
    *         
    * @throws NoSuchBindingException if no matching binding exists
    * 
    * @throws IllegalArgumentException if serverName is unknown to the store.
    */
   ServiceBinding getServiceBinding(String serverName, String serviceName, String bindingName) 
      throws NoSuchBindingException;

   /**
    * Gets all service bindings for the given server name.
    * 
    * @param serverName the {@link ServiceBindingManager#getServerName() name identifying the server instance}
    *  in which the service is running. Cannot be <code>null</code>.
    *  
    * @return the set of service bindings for the server name. Will not be null.
    * 
    * @throws IllegalArgumentException if serverName is unknown to the store.
    */
   Set<ServiceBinding> getServiceBindings(String serverName);
   
   /** 
    * Add a ServiceBinding to the store for the given serverName.
    *
    * @param serverName the name identifying the JBoss server instance in
    *    which the service is running.
    * @param binding metadata for the binding to add
    * 
    * @throws DuplicateServiceException thrown if a configuration for the
    *    <serverName, serviceName> pair already exists.
    * @throws UnknownHostException if the host specified by the metadata is unknown 
    * 
    * @throws IllegalArgumentException if serverName is unknown to the store.
    */
   void addServiceBinding(String serverName, ServiceBindingMetadata binding)
      throws DuplicateServiceException, UnknownHostException;

   /** 
    * Remove a ServiceBinding from the store for the given serverName.
    *
    * @param serverName the name identifying the JBoss server instance in
    *    which the service is running.
    * @param binding the binding
    * 
    * @throws IllegalArgumentException if serverName is unknown to the store.
    */
   void removeServiceBinding(String serverName, ServiceBindingMetadata binding);
   
   /**
    * Gets the offset from a base value that by default should be added to
    * port values for a given serverName.
    * 
    * @param serverName the name of the binding set
    * @return the offset
    * 
    * @throws IllegalArgumentException if serverName is unknown to the store.
    */
   int getDefaultPortOffset(String serverName);
   
   /**
    * Gets the default value to use as the host name for the given serverName.
    * 
    * @param serverName the name of the binding set
    * @return the host name
    * 
    * @throws IllegalArgumentException if serverName is unknown to the store.
    */
   String getDefaultHostName(String serverName);
   
}
