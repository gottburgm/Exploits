/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.profileservice.management;

import java.util.HashMap;
import java.util.Map;

import org.jboss.deployers.spi.management.ContextStateMapper;
import org.jboss.deployers.spi.management.DelegatingComponentDispatcher;
import org.jboss.deployers.spi.management.RuntimeComponentDispatcher;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.RunState;
import org.jboss.metatype.api.values.MetaValue;

/**
 * A delegating runtime component dispatcher, used as the proxy for
 * ManagedProperty gets and ManagedOperation invokes.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 94860 $
 */
public class DelegatingComponentDispatcherImpl
   implements DelegatingComponentDispatcher
{
  
   /** The state mappings. */
   private static final ContextStateMapper<RunState> runStateMapper;
   
   private RuntimeComponentDispatcher dispatcher;
   private ProxyRegistry registry;

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
   }
   
   public DelegatingComponentDispatcherImpl(ProxyRegistry registry, RuntimeComponentDispatcher dispatcher)
   {
      this.registry = registry;
      this.dispatcher = dispatcher;
   }

   public MetaValue get(Long propID, Object componentName, String propertyName)
   {
      ManagedProperty mp = this.registry.getManagedProperty(propID);
      AbstractRuntimeComponentDispatcher.setActiveProperty(mp);
      try
      {
         return dispatcher.get(componentName, propertyName);
      }
      finally
      {
         AbstractRuntimeComponentDispatcher.setActiveProperty(null);
      }
   }

   public MetaValue invoke(Long opID, Object componentName, String methodName, MetaValue... param)
   {
      ManagedOperation op = this.registry.getManagedOperation(opID);
      AbstractRuntimeComponentDispatcher.setActiveOperation(op);
      try
      {
         if(param == null)
            param = new MetaValue[0];
         
         MetaValue result = null;
         if (componentName != null)
         {
            result = (MetaValue) dispatcher.invoke(componentName, methodName, param);
         }
         return result;
      }
      finally
      {
         AbstractRuntimeComponentDispatcher.setActiveOperation(null);
      }
   }
   
   public RunState updateRunState(Object componentName)
   {
      RunState state = RunState.UNKNOWN;
      try
      {
         state = dispatcher.mapControllerState(componentName, runStateMapper);
      }
      catch(Exception ignore) { }
      return state;
   }

   public static interface ProxyRegistry
   {
    
      ManagedProperty getManagedProperty(Long propID);
      ManagedOperation getManagedOperation(Long opID);

   }
   
}
