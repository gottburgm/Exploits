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
package org.jboss.profileservice.management.views;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.deployers.spi.management.ContextStateMapper;
import org.jboss.deployers.spi.management.RuntimeComponentDispatcher;
import org.jboss.logging.Logger;
import org.jboss.managed.api.DeploymentState;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.MutableManagedComponent;
import org.jboss.managed.api.MutableManagedObject;
import org.jboss.managed.api.RunState;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.profileservice.management.AbstractRuntimeComponentDispatcher;
import org.jboss.profileservice.management.ManagedOperationProxyFactory;
import org.jboss.profileservice.spi.Profile;
import org.jboss.profileservice.spi.ProfileKey;

/**
 * A abstract profile view. 
 * 
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @author ales.justin@jboss.org
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * 
 * @version $Revision$
 */
public abstract class AbstractProfileView extends AbstractManagedDeploymentView
{
   
   /** The logger. */
   private static final Logger log = Logger.getLogger(AbstractProfileView.class);
   
   /** The state mappings. */
   private static final ContextStateMapper<RunState> runStateMapper;
   private static final ContextStateMapper<DeploymentState> deploymentStateMapper;

   /** The runtime component dispatcher. */
   private RuntimeComponentDispatcher dispatcher;
   
   /** The proxy factory. */
   private ManagedOperationProxyFactory proxyFactory;
   
   static
   {
      // Set default run state mappings for mc beans/mbeans
      Map<String, RunState> runStateMappings = new HashMap<String, RunState>();
      runStateMappings.put("**ERROR**", RunState.FAILED);
      runStateMappings.put("Not Installed", RunState.STOPPED);
      runStateMappings.put("PreInstall", RunState.STOPPED);
      runStateMappings.put("Described", RunState.STOPPED);
      runStateMappings.put("Instantiated", RunState.STOPPED);
      runStateMappings.put("Configured", RunState.STOPPED);
      runStateMappings.put("Create", RunState.STOPPED);
      runStateMappings.put("Start", RunState.STOPPED);
      runStateMappings.put("Installed", RunState.RUNNING);

      runStateMapper = new ContextStateMapper<RunState>(runStateMappings,
            RunState.STARTING, RunState.STOPPED, RunState.FAILED, RunState.UNKNOWN);

      Map<String, DeploymentState> deploymentMappings = new HashMap<String, DeploymentState>();
      deploymentMappings.put("**ERROR**", DeploymentState.FAILED);
      deploymentMappings.put("Not Installed", DeploymentState.STOPPED);
      deploymentMappings.put("Installed", DeploymentState.STARTED);

      deploymentStateMapper = new ContextStateMapper<DeploymentState>(deploymentMappings,
            DeploymentState.STARTING, DeploymentState.STOPPING, DeploymentState.FAILED, DeploymentState.UNKNOWN);         
   }
   
   public AbstractProfileView(ManagedOperationProxyFactory proxyFactory)
   {
      if(proxyFactory == null)
         throw new IllegalArgumentException("null proxy factory");
      if(proxyFactory.getDispatcher() == null)
         throw new IllegalArgumentException("null runtime component dispatcher");
      
      this.proxyFactory = proxyFactory;
      this.dispatcher = proxyFactory.getDispatcher();      
   }

   public abstract boolean hasBeenModified(Profile profile);
   
   public abstract ProfileKey getProfileKey();
   
   protected void processRootManagedDeployment(ManagedDeployment md, boolean trace) throws Exception
   {
      DeploymentState state = getDeploymentState(md);
      processManagedDeployment(md, state, 0, trace);      
   }

   @Override
   protected void mergeRuntimeMO(ManagedObject mo, ManagedObject runtimeMO)
      throws Exception
   {
      Map<String, ManagedProperty> runtimeProps = runtimeMO.getProperties();
      Set<ManagedOperation> runtimeOps = runtimeMO.getOperations();
      // Get the runtime MO component name
      Object componentName = runtimeMO.getComponentName();
      log.debug("Merging runtime: "+runtimeMO.getName()+", compnent name: "+componentName);
      Map<String, ManagedProperty> moProps = null;
      Set<ManagedOperation> moOps = null;
      HashMap<String, ManagedProperty> props = null;
      HashSet<ManagedOperation> ops = null;
      // If mo is null, the merge target is the runtimeMO
      if (mo == null)
      {
         // Just proxy the runtime props/ops
         mo = runtimeMO;
         moProps = mo.getProperties();
         moOps = mo.getOperations();
         // These will be updated with the proxied values, don't duplicate props/ops
         props = new HashMap<String, ManagedProperty>();
         ops = new HashSet<ManagedOperation>();
      }
      else
      {
         // Merge the runtime props/ops
         moProps = mo.getProperties();
         moOps = mo.getOperations();
         props = new HashMap<String, ManagedProperty>(moProps);
         ops = new HashSet<ManagedOperation>(moOps);
      }
   
      if (runtimeProps != null && runtimeProps.size() > 0)
      {
         log.debug("Properties before:"+props);
         // We need to pull the runtime values for stats
         for(ManagedProperty prop : runtimeProps.values())
         {
            if(prop.hasViewUse(ViewUse.STATISTIC))
            {
               String propName = prop.getMappedName();
               try
               {
                  AbstractRuntimeComponentDispatcher.setActiveProperty(prop);
                  MetaValue propValue = dispatcher.get(componentName, propName);
                  if(propValue != null)
                     prop.setValue(propValue);
               }
               catch(Throwable t)
               {
                  log.debug("Failed to get stat value, "+componentName+":"+propName);
               }
               ManagedProperty proxiedProp = createPropertyProxy(prop);
               props.put(prop.getName(), proxiedProp);
            }
            else
            {
               props.put(prop.getName(), prop);
            }
            // Keep the property associated with the runtime MO for invocations/updates
            if (prop.getTargetManagedObject() == null)
               prop.setTargetManagedObject(runtimeMO);
         }
         
         log.debug("Properties after:"+props);
      }
      if (runtimeOps != null && runtimeOps.size() > 0)
      {
         log.debug("Ops before:"+ops);
         runtimeOps = createOperationProxies(runtimeMO, runtimeOps);
         ops.addAll(runtimeOps);
         log.debug("Ops after:"+ops);
      }
   
      MutableManagedObject moi = (MutableManagedObject) mo;
      moi.setProperties(props);
      moi.setOperations(ops);
   }

   @Override
   protected Set<ManagedOperation> createOperationProxies(ManagedObject mo, Set<ManagedOperation> ops)
      throws Exception
   {
      if (proxyFactory == null)
         throw new IllegalArgumentException("Missing RuntimeComponentDispatcher.");
   
      Object componentName = mo.getComponentName();
      return createOperationProxies(ops, componentName);
   }
   
   protected Set<ManagedOperation> createOperationProxies(Set<ManagedOperation> ops, Object componentName)
      throws Exception
   {
      // Create the delegate operation
      return proxyFactory.createOperationProxies(ops, componentName);
   }

   private ManagedProperty createPropertyProxy(ManagedProperty prop)
      throws Exception
   {
      if (proxyFactory == null)
         throw new IllegalArgumentException("Missing RuntimeComponentDispatcher.");
      
      // Create the delegate property
      Object componentName = prop.getManagedObject().getComponentName();
      return proxyFactory.createPropertyProxy(prop, componentName);
   }
   
   protected RunState updateRunState(ManagedObject runtimeMO, ManagedComponent comp)
   {
      RunState state = comp.getRunState();
      if (state == RunState.UNKNOWN && dispatcher != null)
      {
         Object name = comp.getComponentName();
         if (name == null && runtimeMO != null)
            name = runtimeMO.getComponentName();
         if (name != null)
         {
            state = getMappedState(name, runStateMapper);
            if (comp instanceof MutableManagedComponent)
            {
               MutableManagedComponent mcomp = MutableManagedComponent.class.cast(comp);
               mcomp.setRunState(state);
            }
         }
      }
      return state;
   }
   
   protected DeploymentState getDeploymentState(ManagedDeployment md)
   {
      DeploymentState state = md.getDeploymentState();
      if(state == DeploymentState.UNKNOWN && dispatcher != null)
      {
         Object name = md.getName();
         if(name != null)
         { 
            state = getMappedState(name, deploymentStateMapper);
         }
      }
      return state;
   }
   
   protected <T extends Enum<?>> T getMappedState(Object name, ContextStateMapper<T> mapper)
   {
      T state = null;
      if(dispatcher != null)
      {
         try
         {
            //TODO, update RuntimeComponentDispatcher
            AbstractRuntimeComponentDispatcher xdispatcher = (AbstractRuntimeComponentDispatcher) dispatcher;
            state = xdispatcher.mapControllerState(name, mapper);            
         }
         catch(Exception e)
         {
            state = mapper.getErrorState();
         }
      }
      return state;      
   }

}

