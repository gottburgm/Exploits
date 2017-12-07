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

package org.jboss.test.services.binding.test;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Set;

import org.jboss.services.binding.DuplicateServiceException;
import org.jboss.services.binding.NoSuchBindingException;
import org.jboss.services.binding.ServiceBinding;
import org.jboss.services.binding.ServiceBindingMetadata;
import org.jboss.services.binding.ServiceBindingStore;

/**
 * Mock implementation of ServiceBindingStore.  Stores a single ServiceBinding.
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public class MockServiceBindingStore implements ServiceBindingStore
{
   public static final String HOSTNAME = "192.168.1.10";
   
   private final String serverName;
   private ServiceBinding binding;
   
   public MockServiceBindingStore(ServiceBinding binding, String serverName)
   {
      this.binding = binding;
      this.serverName = serverName;
   }

   public ServiceBinding getServiceBinding(String serverName, String serviceName, String bindingName)
         throws NoSuchBindingException
   {
      if (this.binding == null 
            || this.serverName.equals(serverName) == false
            || this.binding.getServiceName().equals(serviceName) == false
            || safeEquals(this.binding.getBindingName(), bindingName) == false)
      {
         throw new NoSuchBindingException(serverName, serviceName, bindingName);
      }
      
      return binding;
   }
   
   public Set<ServiceBinding> getServiceBindings(String serverName)
   {
      if (this.serverName.equals(serverName) == false)
         throw new IllegalArgumentException("Invalid serverName " + serverName);
      return Collections.singleton(binding);
   }

   public void removeServiceBinding(String serverName, ServiceBindingMetadata binding)
   {
      throw new UnsupportedOperationException("unimplemented");
   }

   public void addServiceBinding(String serverName, ServiceBindingMetadata binding) 
     throws DuplicateServiceException, UnknownHostException
   {
      if (this.binding != null)
         throw new IllegalStateException("MockServiceBindingStore already has a binding");
      if (this.serverName.equals(serverName) == false)
         throw new IllegalArgumentException("Invalid serverName " + serverName);
      
      this.binding = new ServiceBinding(binding, getDefaultHostName(serverName), getDefaultPortOffset(serverName));
   }

   public String getDefaultHostName(String serverName)
   {      
      return HOSTNAME;
   }

   public int getDefaultPortOffset(String serverName)
   {
      return 1000;
   }

   /**
    * Set the binding.
    * 
    * @param binding The binding to set.
    */
   public void setBinding(ServiceBinding binding)
   {
      this.binding = binding;
   }
   
   private boolean safeEquals(Object a, Object b)
   {
      return (a == b || (a != null && a.equals(b)));
   }

}
