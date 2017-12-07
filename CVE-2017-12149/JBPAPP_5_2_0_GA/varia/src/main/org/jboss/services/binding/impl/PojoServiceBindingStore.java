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

package org.jboss.services.binding.impl;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.logging.Logger;
import org.jboss.services.binding.DuplicateServiceException;
import org.jboss.services.binding.NoSuchBindingException;
import org.jboss.services.binding.ServiceBinding;
import org.jboss.services.binding.ServiceBindingMetadata;
import org.jboss.services.binding.ServiceBindingStore;

/**
 * A Pojo implementation of {@link ServiceBindingStore}.
 * 
 * @author Brian Stansberry
 * @version $Revision: 88905 $
 */
public class PojoServiceBindingStore 
   implements ServiceBindingStore
{
   private static final Logger log = Logger.getLogger(PojoServiceBindingStore.class);
   
   /** Dummy value to make ConcurrentHashMap act like a Set */
   private static final Object VALUE = new Object();
   
   // ------------------------------------------------------------ Constructors
   
   /** All bindings */
   private final ConcurrentMap<ServiceBindingKey, ServiceBinding> bindings = 
      new ConcurrentHashMap<ServiceBindingKey, ServiceBinding>(16, (float) .75, 2);
   
   /** Injected binding sets*/
   private final ConcurrentMap<String, ServiceBindingSet> bindingSets = 
      new ConcurrentHashMap<String, ServiceBindingSet>(16, (float) .75, 2);
   
   /** Injected base bindings whose ports are incremented for each set */
   private final Map<ServiceBindingMetadata, Object> standardBindings = 
      new ConcurrentHashMap<ServiceBindingMetadata, Object>(16, (float) .75, 2);
   
   private boolean started;
 
   // ------------------------------------------------------------ Constructors
   
   /**
    * Creates a new PojoServiceBindingStore
    */
   public PojoServiceBindingStore() {}
   
   public PojoServiceBindingStore(Set<ServiceBindingSet> bindingSets, Set<ServiceBindingMetadata> standardBindings)
   {
      setServiceBindingSetsInternal(bindingSets);
      setStandardBindingsInternal(standardBindings);
   }
   
   // ----------------------------------------------------- ServiceBindingStore
   
   public synchronized void addServiceBinding(String serverName, ServiceBindingMetadata metadata) 
      throws DuplicateServiceException, UnknownHostException
   {      
      addServiceBindingInternal(serverName, metadata, true);
      
      log.debug("added binding " + metadata.getFullyQualifiedName() + " to " + serverName);
   }

   public synchronized ServiceBinding getServiceBinding(String serverName, String serviceName, String bindingName)
         throws NoSuchBindingException
   {
      ServiceBinding binding = bindings.get(new ServiceBindingKey(serverName, serviceName, bindingName));
      if (binding == null)
      {
         throw new NoSuchBindingException(serverName, serviceName, bindingName);
      }
      return binding;
   }
   
   public synchronized Set<ServiceBinding> getServiceBindings(String serverName)
   {
      validateServerName(serverName);
      
      Set<ServiceBinding> result = new HashSet<ServiceBinding>();
      for (Map.Entry<ServiceBindingKey, ServiceBinding> entry : bindings.entrySet())
      {
         if (serverName.equals(entry.getKey().serverName))
         {
            result.add(entry.getValue());
         }
      }
      
      return result;
   }

   public synchronized void removeServiceBinding(String serverName, ServiceBindingMetadata metadata)
   {
      validateServerName(serverName);
      bindings.remove(new ServiceBindingKey(serverName, metadata));
      
      // For management purposes, treat this as an override
      ServiceBindingSet bindingSet = bindingSets.get(serverName);
      bindingSet.getOverrideBindings().remove(metadata);
   }

   public synchronized String getDefaultHostName(String serverName)
   {
      validateServerName(serverName);
      return bindingSets.get(serverName).getDefaultHostName();
   }

   public synchronized int getDefaultPortOffset(String serverName)
   {
      validateServerName(serverName);
      return bindingSets.get(serverName).getPortOffset();
   }
   
   // ------------------------------------------------------------------ Public
   
   /**
    * Sets the base set of bindings that should be associated with each binding set,
    * adjusted to conform to the binding set's defaultHostName and offset.
    * 
    * @param bindings the set of base bindings. May be <code>null</code>
    * @throws DuplicateServiceException 
    * @throws UnknownHostException 
    * 
    * @throws IllegalStateException if invoked after {@link #start()}
    */
   public synchronized void setStandardBindings(Set<ServiceBindingMetadata> bindings) throws UnknownHostException, DuplicateServiceException
   {      
      setStandardBindingsInternal(bindings);
      
      if (started)
      {
         log.debug("updated standard bindings injected");
         if (log.isTraceEnabled())
         {
            for (ServiceBindingMetadata sbm : bindings)
            {
               log.trace(sbm.getFullyQualifiedName() + " port is " + sbm.getPort() + " " + sbm.isFixedHostName() + "/" + sbm.isFixedPort());
            }
         }
         
         establishBindings();
      }
   }
   
   public synchronized void setServiceBindingSets(Set<ServiceBindingSet> sets) throws UnknownHostException, DuplicateServiceException
   {      
      setServiceBindingSetsInternal(sets);     
      
      if (started)
      {
         log.debug("updated ServiceBindingSets injected");
         if (log.isTraceEnabled())
         {
            for (ServiceBindingSet set : sets)
            {
               log.trace(set.getName() + " offset is " + set.getPortOffset() + " defaultHostName is " + set.getDefaultHostName());
               java.util.Set<ServiceBindingMetadata> ovr = set.getOverrideBindings();
               for (ServiceBindingMetadata sbm : ovr)
               {
                  log.trace(sbm.getFullyQualifiedName() + " port is " + sbm.getPort() + " " + sbm.isFixedHostName() + "/" + sbm.isFixedPort());
               }
            }
         }
         establishBindings();
      }
   }
   
   /**
    * Builds the runtime sets of bindings from the injected base bindings
    * and ServiceBindingSets.
    * 
    * @throws DuplicateServiceException
    * @throws UnknownHostException
    */
   public void start() throws DuplicateServiceException, UnknownHostException
   {
      establishBindings();
      
      this.started = true;
   }
   
   public void stop()
   {
      this.bindings.clear();
            
      this.started = false;
   }
   
   // -------------------------------------------------------------- Management

   /**
    * Gets the base set of bindings that should be associated with each binding set,
    * but with that binding set's {@link ServiceBindingSet#getPortOffset() port offset}
    * applied to the port value.
    * 
    * @return the set of base bindings
    */
   public Set<ServiceBindingMetadata> getStandardBindings()
   {
      return new HashSet<ServiceBindingMetadata>(standardBindings.keySet());
   }
   
   /**
    * See {@link #getServiceBindingSets()}
    * 
    * @return the binding sets
    * 
    * @deprecated use {@link #getServiceBindingSets()}
    */
   public Set<ServiceBindingSet> getBindingSets()
   {
      return getServiceBindingSets();
   }
   
   /**
    * Gets the {@link ServiceBindingSet}s associated with this store.
    * 
    * @return  the binding sets. Will not return <code>null</code>
    */
   public Set<ServiceBindingSet> getServiceBindingSets()
   {
      return new HashSet<ServiceBindingSet>(bindingSets.values());
   }
   
   /** 
    * Add a ServiceBinding to all binding sets in the store. For each binding 
    * set, a new ServiceBinding is added whose serviceName and bindingName
    * properties match the passed binding. If <the given <code>binding</code>'s
    * <code>fixeHostName</code> property is <code>false</code>, the new binding's 
    * hostName matches the target set's {@link #getDefaultHostName(String) default host name}.
    * If <code>binding</code>'s <code>fixedPort</code> property is <code>false</code>, 
    * the new binding's port is derived by taking the port from the passed binding 
    * and incrementing it by the target set's 
    * {@link #getDefaultPortOffset(String) default port offset}.
    *
    * @param metadata metadata about the binding to add
    * 
    * @throws DuplicateServiceException thrown if a configuration for the
    *    <serverName, serviceName> pair already exists.
    */
   public synchronized void addServiceBinding(ServiceBindingMetadata metadata) throws DuplicateServiceException
   {
      // Add to the runtime objects
      for (ServiceBindingSet bindingSet : bindingSets.values())
      {
         try
         {
            addServiceBindingInternal(bindingSet.getName(), metadata, false);
         }
         catch (UnknownHostException e)
         {
            String hostName = metadata.isFixedHostName() ? metadata.getHostName() : bindingSet.getDefaultHostName();
            throw new IllegalStateException("Cannot convert " + hostName + " into an InetAddress");
         }
      }
      
      // Add to the managed object map
      standardBindings.put(metadata, VALUE);
   }
   
   /** 
    * Creates a new {@link ServiceBindingMetadata} from the given params
    * and calls {@link #addServiceBinding(ServiceBindingMetadata)}.
    *
    * @param serviceName the name of the service. Cannot be <code>null</code>
    * @param bindingName name qualifier for the binding within the service.
    *                    May be <code>null</code>
    * @param hostName hostname or IP address to which the binding should be
    *                 bound. Use <code>null</code> to indicate the host name
    *                 should be the default host name for each binding set
    * @param serviceConfig the configuration to add
    * @param fixed <code>true</code> if the binding's port should remain fixed
    *              when added to each binding set; <code>false</code> if it 
    *              should be offset by the binding set's port offset
    * 
    * @throws DuplicateServiceException thrown if a configuration for the
    *    <serverName, serviceName> pair already exists.
    */
   public synchronized void addServiceBinding(String serviceName, String bindingName, String hostName, int port, boolean fixedPort) 
      throws DuplicateServiceException, UnknownHostException
   {
      addServiceBinding(serviceName, bindingName, null, hostName, port, false, fixedPort);
   }
   
   /** 
    * Creates a new {@link ServiceBindingMetadata} from the given params
    * and calls {@link #addServiceBinding(ServiceBindingMetadata)}.
    *
    * @param serviceName the name of the service. Cannot be <code>null</code>
    * @param bindingName name qualifier for the binding within the service.
    *                    May be <code>null</code>
    * @param description helpful description of the binding; may be <code>null</code>
    * @param hostName hostname or IP address to which the binding should be
    *                 bound. Often <code>null</code> since the host name typically
    *                 comes from the default host name for each binding set
    * @param port  port the binding should use
    * @param fixedHostName <code>true</code> if the binding's <code>hostName</code>
    *              should remain fixed when added to each binding set; 
    *              <code>false</code> if it should be changed to the binding set's 
    *              default host name
    * @param fixedPort <code>true</code> if the binding's port should remain fixed
    *              when added to each binding set; <code>false</code> if it 
    *              should be offset by the binding set's port offset
    * 
    * @throws DuplicateServiceException thrown if a configuration for the
    *    <serverName, serviceName> pair already exists.
    */
   public synchronized void addServiceBinding(String serviceName, String bindingName, String description, String hostName, int port, boolean fixedHostName, boolean fixedPort) 
      throws DuplicateServiceException, UnknownHostException
   {
      ServiceBindingMetadata metadata = new ServiceBindingMetadata();
      metadata.setServiceName(serviceName);
      metadata.setBindingName(bindingName);
      metadata.setHostName(hostName);
      metadata.setPort(port);
      metadata.setFixedPort(fixedPort);
      
      addServiceBinding(metadata);
   }

   /** 
    * Remove a service configuration from all binding sets in the store.
    *
    * @param metadata the binding
    */
   public synchronized void removeServiceBinding(ServiceBindingMetadata metadata)
   {
      // Remove from runtime sets
      for (String serverName : bindingSets.keySet())
      {
         removeServiceBinding(serverName, metadata);
      }
      
      // Remove from managed set
      standardBindings.remove(metadata); 
   }

   /** 
    * Remove a service configuration from all binding sets in the store.
    *
    * @param serviceName the name of the service. Cannot be <code>null</code>
    * @param bindingName name qualifier for the binding within the service.
    *                    May be <code>null</code>
    */
   public synchronized void removeServiceBinding(String serviceName, String bindingName)
   {
      ServiceBindingMetadata metadata = new ServiceBindingMetadata(serviceName, bindingName);
      removeServiceBinding(metadata); 
   }
   
   // ------------------------------------------------------------------ Private

   private static boolean safeEquals(Object a, Object b)
   {      
      return (a == b || (a != null && a.equals(b)));
   }
   
   private void validateServerName(String serverName)
   {
      if (bindingSets.containsKey(serverName) == false)
         throw new IllegalArgumentException("unknown serverName " +serverName);
   }

   private void setServiceBindingSetsInternal(Set<ServiceBindingSet> sets)
   {
      this.bindingSets.clear();
      
      if (sets != null)
      {      
         for (ServiceBindingSet bindingSet : sets)
         {
            this.bindingSets.put(bindingSet.getName(), bindingSet);
         }
      }
   }

   private void setStandardBindingsInternal(Set<ServiceBindingMetadata> bindings)
   {
      standardBindings.clear();
      if (bindings != null)
      {
         for (ServiceBindingMetadata binding : bindings)
         {
            standardBindings.put(binding, VALUE);
         }
      }
   }
   
   private void establishBindings() throws UnknownHostException, DuplicateServiceException
   {
      synchronized (this)
      {
         this.bindings.clear();
         
         // Establish the override bindings first, so when we add the
         // fixed and portOffset, we get DuplicateServiceException
         for (ServiceBindingSet bindingSet : bindingSets.values())
         {
            for (ServiceBindingMetadata binding : bindingSet.getOverrideBindings())
            {
               addServiceBindingInternal(bindingSet.getName(), binding, false);
            }
         }
         
         // Establish the standard bindings   
         for (ServiceBindingMetadata metadata : standardBindings.keySet())
         {
            for (ServiceBindingSet bindingSet : bindingSets.values())
            {
               try
               {
                  addServiceBindingInternal(bindingSet.getName(), metadata, false);
               }
               catch (DuplicateServiceException e)
               {
                  if (bindingSet.getOverrideBindings().contains(metadata) == false)
                  {
                     throw e;
                  }
               }
            }
         }
      }
   }
   
   private void addServiceBindingInternal(String serverName, ServiceBindingMetadata metadata, boolean addToBindingSet) 
      throws DuplicateServiceException, UnknownHostException
   {      
      validateServerName(serverName);
      ServiceBindingSet bindingSet = bindingSets.get(serverName);
      ServiceBinding binding = new ServiceBinding(metadata, bindingSet.getDefaultHostName(), bindingSet.getPortOffset());
      ServiceBinding oldBinding = bindings.putIfAbsent(new ServiceBindingKey(serverName, metadata), binding);
      if (oldBinding != null && 
            (safeEquals(oldBinding.getHostName(), binding.getHostName()) == false
               || oldBinding.getPort() != binding.getPort()))
      {
         throw new DuplicateServiceException(serverName, binding);
      }
      
      if (addToBindingSet)
      {
         // For management purposes, treat this as an override
         bindingSet.getOverrideBindings().add(metadata);
      }
   }

   private static class ServiceBindingKey
   {
      private final String serverName;
      private final String serviceName;
      private final String bindingName;
      
      private ServiceBindingKey(String serverName, ServiceBindingMetadata binding)
      {
         if (serverName == null)
         {
            throw new IllegalArgumentException("serverName is null");
         }
         if (binding == null)
         {
            throw new IllegalArgumentException("binding is null");
         }
         if (binding.getServiceName() == null)
         {
            throw new IllegalStateException("binding's serviceName is null");
         }
         
         this.serverName = serverName;
         this.serviceName = binding.getServiceName();
         this.bindingName = binding.getBindingName();
      }
      
      private ServiceBindingKey(String serverName, String serviceName, String bindingName)
      {
         if (serverName == null)
         {
            throw new IllegalArgumentException("serverName is null");
         }

         if (serviceName == null)
         {
            throw new IllegalArgumentException("serviceName is null");
         }
         
         this.serverName  = serverName;
         this.serviceName = ServiceBindingMetadata.canonicalizeServiceName(serviceName);
         this.bindingName = bindingName;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj instanceof ServiceBindingKey)
         {
            ServiceBindingKey other = (ServiceBindingKey) obj;
            return (this.serverName.equals(other.serverName)
                    && this.serviceName.equals(other.serviceName)
                    && safeEquals(this.bindingName, other.bindingName));
         }
         return false;
      }

      @Override
      public int hashCode()
      {
         int result = 17;
         result += 23 * this.serverName.hashCode();
         result += 23 * this.serviceName.hashCode();
         result += 23 * (this.bindingName == null ? 0 : this.bindingName.hashCode());
         return result;
      }
      
   }

}
