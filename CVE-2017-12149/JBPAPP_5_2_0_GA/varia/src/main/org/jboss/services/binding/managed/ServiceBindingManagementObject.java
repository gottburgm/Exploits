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

package org.jboss.services.binding.managed;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementProperties;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.metatype.api.annotations.MetaMapping;
import org.jboss.services.binding.DuplicateServiceException;
import org.jboss.services.binding.ServiceBinding;
import org.jboss.services.binding.ServiceBindingManager;
import org.jboss.services.binding.ServiceBindingMetadata;
import org.jboss.services.binding.ServiceBindingStore;
import org.jboss.services.binding.ServiceBindingValueSource;
import org.jboss.services.binding.impl.PojoServiceBindingStore;
import org.jboss.services.binding.impl.ServiceBindingSet;

/**
 * Provide a management interface to the overall {@link ServiceBindingManager} system.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
@ManagementObject(name="ServiceBindingManager",
      componentType=@ManagementComponent(type="MCBean", subtype="ServiceBindingManager"),
      properties=ManagementProperties.EXPLICIT,
      description="The ServiceBindingManager enables the centralized management of ports, by service.")
public class ServiceBindingManagementObject
{
   private final ServiceBindingManager bindingManager;
   private final PojoServiceBindingStore bindingStore;

   /**
    * Create a new ServiceBindingManager.
    * 
    * @param serverName
    * @param bindingSets
    * @param standardBindings
    */
   public ServiceBindingManagementObject(String serverName, Set<ServiceBindingSet> bindingSets,
         Set<ServiceBindingMetadata> standardBindings)
   {
      bindingStore = new PojoServiceBindingStore(bindingSets, standardBindings);
      bindingManager = new ServiceBindingManager(serverName, bindingStore);
   }
   
   public ServiceBindingManager getServiceBindingManager()
   {
      return bindingManager;
   }

   /**
    * Gets the value of the <code>serverName</code> param the <code>ServiceBindingManager</code>
    * should pass to <code>ServiceBindingStore</code> when 
    * {@link ServiceBindingStore#getServiceBinding(String, String, String) requesting bindings}.
    * 
    * @return name of the set of bindings this server uses
    */
   @ManagementProperty(description="the name of the binding set the " +
         "ServiceBindingManager should use when resolving bindings",
          use={ViewUse.CONFIGURATION}, readOnly=false)
   public String getActiveBindingSetName()
   {
      return bindingManager.getServerName();
   }
   
   /**
    * Sets the value of the <code>serverName</code> param the <code>ServiceBindingManager</code>
    * should pass to <code>ServiceBindingStore</code> when 
    * {@link ServiceBindingStore#getServiceBinding(String, String, String) requesting bindings}.
    * 
    * param name name of the set of bindings this server uses
    */
   public void setActiveBindingSetName(String name)
   {
      bindingManager.setServerName(name);
   }

   @ManagementProperty(description="the set of service binding configurations associated with this instance",
         use={ViewUse.STATISTIC}, readOnly=true)
   @MetaMapping(value=ServiceBindingMapper.class)
   public Map<String, Set<ServiceBinding>> getServiceBindings()
   {
      Map<String, Set<ServiceBinding>> result = new HashMap<String, Set<ServiceBinding>>();
      for (ServiceBindingSet set : getBindingSets())
      {
         String name = set.getName();
         result.put(name, bindingStore.getServiceBindings(name));
      }
      return result;
   }

   /**
    * Gets the available {@link ServiceBindingSet}s.
    * 
    * @return  the binding sets. Will not return <code>null</code>
    */
   @ManagementProperty(description="the named binding sets")
   @MetaMapping(ServiceBindingSetMapper.class)
   public Set<ServiceBindingSet> getBindingSets()
   {
      return bindingStore.getServiceBindingSets();
   }
   
   /**
    * Sets the available {@link ServiceBindingSet}s.
    * 
    * @param bindingSets the binding sets
    * @throws DuplicateServiceException 
    * @throws UnknownHostException 
    */
   public void setBindingSets(Set<ServiceBindingSet> bindingSets) throws UnknownHostException, DuplicateServiceException
   {
      // The managed objects don't handle any configure value sources, so we need
      // to restore any existing ones
      restoreOverrideBindingValueSources(bindingSets);
      
      bindingStore.setServiceBindingSets(bindingSets);
   }

   /**
    * Gets the base set of bindings that should be associated with each binding set,
    * but with that binding set's {@link ServiceBindingSet#getPortOffset() port offset}
    * applied to the port value.
    * 
    * @return the set of base bindings
    */
   @ManagementProperty(description="the base set of bindings that should be associated " +
        "with each binding set")
   @MetaMapping(ServiceBindingMetadataMapper.class)
   public Set<ServiceBindingMetadata> getStandardBindings()
   {
      return bindingStore.getStandardBindings();
   }

   /**
    * Sets the base set of bindings that should be associated with each binding set,
    * but with that binding set's {@link ServiceBindingSet#getPortOffset() port offset}
    * applied to the port value.
    * 
    * @param bindings the set of base bindings
    * @throws DuplicateServiceException 
    * @throws UnknownHostException 
    */
   public void setStandardBindings(Set<ServiceBindingMetadata> bindings) throws UnknownHostException, DuplicateServiceException
   {
      // The managed objects don't handle any configure value sources, so we need
      // to restore any existing ones
      restoreStandardBindingValueSources(bindings);
      
      bindingStore.setStandardBindings(bindings);
   }
   
   
   private void restoreStandardBindingValueSources(Set<ServiceBindingMetadata> bindings)
   {
      if (bindings != null)
      {
         Set<ServiceBindingMetadata> existing = bindingStore.getStandardBindings();
         restoreServiceBindingValueSources(bindings, existing);      
      }      
   }
   
   
   private void restoreOverrideBindingValueSources(Set<ServiceBindingSet> bindingSets)
   {
      if (bindingSets != null)
      {
         Set<ServiceBindingSet> existingSets = bindingStore.getServiceBindingSets();
         Map<String, Set<ServiceBindingMetadata>> byName = new HashMap<String, Set<ServiceBindingMetadata>>();
         for (ServiceBindingSet set : existingSets)
         {
            byName.put(set.getName(), set.getOverrideBindings());
         }
         
         for (ServiceBindingSet set : bindingSets)
         {
            restoreServiceBindingValueSources(set.getOverrideBindings(), byName.get(set.getName()));
         }   
      }      
   }

   private void restoreServiceBindingValueSources(Set<ServiceBindingMetadata> bindings,
         Set<ServiceBindingMetadata> existing)
   {
      if (bindings != null && existing != null)
      {
         Map<String, ServiceBindingMetadata> byFQN = new HashMap<String, ServiceBindingMetadata>();
         for (ServiceBindingMetadata md : existing)
         {
            byFQN.put(md.getFullyQualifiedName(), md);
         }
         
         for (ServiceBindingMetadata newMD : bindings)
         {
            ServiceBindingMetadata old = byFQN.get(newMD.getFullyQualifiedName());
            if (old != null)
            {
               ServiceBindingValueSource source = old.getServiceBindingValueSource();
               if (source != null)
               {
                  newMD.setServiceBindingValueSource(source);
               }
               else
               {
                  String sourceClass = old.getServiceBindingValueSourceClassName();
                  if (sourceClass != null)
                  {
                     newMD.setServiceBindingValueSourceClassName(sourceClass);
                  }
               }
               
               newMD.setServiceBindingValueSourceConfig(old.getServiceBindingValueSourceConfig());
            }
         }
      }
   }

//   /** 
//    * Add a ServiceBinding to all binding sets in the store. For each binding 
//    * set, a new ServiceBinding is added whose serviceName and bindingName
//    * properties match the passed binding. If <the given <code>binding</code>'s
//    * <code>fixeHostName</code> property is <code>false</code>, the new binding's 
//    * hostName matches the target set's {@link #getDefaultHostName(String) default host name}.
//    * If <code>binding</code>'s <code>fixedPort</code> property is <code>false</code>, 
//    * the new binding's port is derived by taking the port from the passed binding 
//    * and incrementing it by the target set's 
//    * {@link #getDefaultPortOffset(String) default port offset}.
//    *
//    * @param serviceName the name of the service. Cannot be <code>null</code>
//    * @param bindingName name qualifier for the binding within the service.
//    *                    May be <code>null</code>
//    * @param description helpful description of the binding; may be <code>null</code>
//    * @param hostName hostname or IP address to which the binding should be
//    *                 bound. Often <code>null</code> since the host name typically
//    *                 comes from the default host name for each binding set
//    * @param port  port the binding should use
//    * @param fixedHostName <code>true</code> if the binding's <code>hostName</code>
//    *              should remain fixed when added to each binding set; 
//    *              <code>false</code> if it should be changed to the binding set's 
//    *              default host name
//    * @param fixedPort <code>true</code> if the binding's port should remain fixed
//    *              when added to each binding set; <code>false</code> if it 
//    *              should be offset by the binding set's port offset
//    * 
//    * @throws DuplicateServiceException thrown if a configuration for the
//    *    <serverName, serviceName> pair already exists.
//    */
//   @ManagementOperation(description="adds a service binding to all binding sets in the store", impact=Impact.WriteOnly,
//                        params={@ManagementParameter(name="serviceName", 
//                                      description="the name of the service; cannot be null"),
//                                @ManagementParameter(name="bindingName", 
//                                      description="name qualifier for the binding within the service; may be null"),
//                                @ManagementParameter(name="description",
//                                      description="helpful description of the binding; may be null"),
//                                @ManagementParameter(name="hostName", 
//                                      description="hostname or IP address designating " +
//                                            "the interface to which the binding " +
//                                            "should be bound; Often null since the host name " +
//                                            "typically comes from the default host name for each binding set"),
//                                @ManagementParameter(name="port", 
//                                      description="port the binding should use"),
//                                @ManagementParameter(name="fixedHostName", 
//                                      description="true if the value of the hostName " +
//                                            "param must be respected; false if it " +
//                                            "can be altered to the default value " +
//                                            "for each binding set"),
//                                @ManagementParameter(name="fixedPort", 
//                                      description="true if the the binding's port " +
//                                            "should remain fixed when added to each " +
//                                            "binding set; false if it should be " +
//                                            "offset by the binding set's port offset")})
//   public void addStandardBinding(String serviceName, String bindingName, String description, String hostName, int port, boolean fixedHostName, boolean fixedPort) 
//      throws DuplicateServiceException, UnknownHostException
//   {
//      bindingStore.addServiceBinding(serviceName, bindingName, description, hostName, port, fixedHostName, fixedPort);
//   }
//
//   /** 
//    * Remove a service configuration from all binding sets in the store.
//    *
//    * @param serviceName the name of the service. Cannot be <code>null</code>
//    * @param bindingName name qualifier for the binding within the service.
//    *                    May be <code>null</code>
//    */
//   @ManagementOperation(description="removes a service binding", impact=Impact.WriteOnly,
//         params={@ManagementParameter(name="serviceName"),
//                 @ManagementParameter(name="bindingName")})
//   public void removeStandardBinding(String serviceName, String bindingName)
//   {
//      bindingStore.removeServiceBinding(serviceName, bindingName); 
//   }
   
   public void start() throws Exception
   {      
      bindingStore.start();
   }
   
   public void stop() throws Exception
   {
      bindingStore.stop();
   }
}
