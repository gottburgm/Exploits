/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat Inc., and individual contributors as indicated
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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.deployers.spi.management.ContextStateMapper;
import org.jboss.deployers.spi.management.RuntimeComponentDispatcher;
import org.jboss.logging.Logger;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.DeploymentState;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.MutableManagedComponent;
import org.jboss.managed.api.MutableManagedObject;
import org.jboss.managed.api.RunState;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementObjectID;
import org.jboss.managed.api.annotation.ManagementObjectRef;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.managed.plugins.ManagedComponentImpl;
import org.jboss.managed.plugins.ManagedDeploymentImpl;
import org.jboss.managed.plugins.factory.AbstractManagedObjectFactory;
import org.jboss.metatype.api.types.ArrayMetaType;
import org.jboss.metatype.api.types.CollectionMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.values.ArrayValue;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.GenericValue;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.profileservice.spi.ProfileKey;

/**
 * Maintains the current state of the mgmt components in a view.
 * 
 * @author Emanuel Muckenhuber
 */
class ManagementViewState
{

   /** The logger. */
   private static Logger log = Logger.getLogger(ManagementViewImpl.class);

   /** An index of ManagedComponent by ComponentType */
   private final HashMap<ComponentType, Set<ManagedComponent>> compByCompType = new HashMap<ComponentType, Set<ManagedComponent>>();

   /** id/type key to ManagedObject map */
   private final Map<String, ManagedObject> moRegistry = new HashMap<String, ManagedObject>();

   /** The ManagedPropertys with unresolved ManagementObjectRefs */
   private final Map<String, Set<ManagedProperty>> unresolvedRefs = new HashMap<String, Set<ManagedProperty>>();

   /** A map of runtime ManagedObjects needing to be merged with their matching ManagedObject. */
   private final Map<String, ManagedObject> runtimeMOs = new HashMap<String, ManagedObject>();

   /** The deployment name to ManagedDeployment map */
   private final Map<String, ManagedDeployment> managedDeployments = new HashMap<String, ManagedDeployment>();

   /** The root deployments to resolve the deployment name. */
   private final List<String> rootDeployments = new ArrayList<String>();

   /** The dispatcher handles ManagedOperation dispatches */
   private final RuntimeComponentDispatcher dispatcher;

   /** The managed operation proxy factory. */
   private final ManagedOperationProxyFactory proxyFactory;

   /** A proxy for pure JMX dispatch */
   private final ManagedOperationProxyFactory mbeanProxyFactory;

   private final ContextStateMapper<RunState> runStateMapper;
   private final ContextStateMapper<DeploymentState> deploymentStateMapper;

   protected ManagementViewState(RuntimeComponentDispatcher dispatcher, ManagedOperationProxyFactory proxyFactory,
         ManagedOperationProxyFactory mbeanProxyFactory, ContextStateMapper<RunState> runStateMapper,
         ContextStateMapper<DeploymentState> deploymentStateMapper)
   {
      this.dispatcher = dispatcher;
      this.proxyFactory = proxyFactory;
      this.mbeanProxyFactory = mbeanProxyFactory;
      this.runStateMapper = runStateMapper;
      this.deploymentStateMapper = deploymentStateMapper;
   }

   void release()
   {
      // Cleanup... actually at this point all should be GCed
      this.compByCompType.clear();
      this.managedDeployments.clear();
      this.moRegistry.clear();
      this.runtimeMOs.clear();
      this.unresolvedRefs.clear();
      this.rootDeployments.clear();
   }

   /**
    * Get the mgmt components by the component type
    * 
    * @return the components map
    */
   public HashMap<ComponentType, Set<ManagedComponent>> getCompByCompType()
   {
      return compByCompType;
   }

   /**
    * Get the managed deployments.
    * 
    * @return the managed deployments
    */
   public Map<String, ManagedDeployment> getManagedDeployments()
   {
      return managedDeployments;
   }

   /**
    * Get all the root deployments.
    * 
    * @return the root deployments
    */
   public List<String> getRootDeployments()
   {
      return rootDeployments;
   }

   /**
    * Check if the merging the runtime MOs worked.
    *
    */
   protected void checkRuntimeMOs()
   {
      if (this.runtimeMOs.size() > 0)
      {
         log.warn("Failed to merged the following runtime ManagedObjects: " + runtimeMOs);
      }
   }

   /**
    * Process the root managed deployment. This gets
    * the deployment state for this deployment, which will
    * get populated to the child deployments as well.
    *
    * @param md the managed deployment
    * @param profile the associated profile key
    * @param trace is trace enabled
    * @throws Exception for any error
    */
   protected void processRootManagedDeployment(ManagedDeployment md, ProfileKey profile, boolean trace)
         throws Exception
   {
      DeploymentState state = getDeploymentState(md);
      processManagedDeployment(md, profile, state, 0, trace);
   }

   /**
    * Process managed deployment.
    *
    * @param md the managed deployment
    * @param profile the associated profile key
    * @param state the deployment state
    * @param level depth level
    * @param trace is trace enabled
    * @throws Exception for any error
    */
   protected void processManagedDeployment(ManagedDeployment md, ProfileKey profile, DeploymentState state, int level,
         boolean trace) throws Exception
   {
      String name = md.getName();
      if (trace)
         log.trace(name + " ManagedDeployment_" + level + ": " + md);
      Map<String, ManagedObject> mos = md.getManagedObjects();
      if (trace)
         log.trace(name + " ManagedObjects_ " + level + ": " + mos);

      // Set the deployment state
      if (state != null && md instanceof ManagedDeploymentImpl)
         ((ManagedDeploymentImpl) md).setDeploymentState(state);

      // Map any existing ManagedComponent types
      for (ManagedComponent comp : md.getComponents().values())
      {
         ComponentType type = comp.getType();
         Set<ManagedComponent> typeComps = compByCompType.get(type);
         if (typeComps == null)
         {
            typeComps = new HashSet<ManagedComponent>();
            compByCompType.put(type, typeComps);
         }
         typeComps.add(comp);
      }

      for (ManagedObject mo : mos.values())
      {
         processManagedObject(mo, md);
      }
      managedDeployments.put(name, md);

      // Associate profile with the deployment
      if (profile != null)
      {
         md.setAttachment(ProfileKey.class.getName(), profile);
      }

      // Add root deployments
      if (level == 0)
         this.rootDeployments.add(name);

      // Process children
      List<ManagedDeployment> mdChildren = md.getChildren();
      if (mdChildren != null && mdChildren.isEmpty() == false)
      {
         for (ManagedDeployment mdChild : mdChildren)
         {
            // process the child deployments, with the state of the parent.
            processManagedDeployment(mdChild, profile, state, level + 1, trace);
         }
      }
   }

   /**
    * Process managed object.
    *
    * @param mo the managed object
    * @param md the managed deployment
    */
   protected void processManagedObject(ManagedObject mo, ManagedDeployment md) throws Exception
   {
      String key = mo.getName() + "/" + mo.getNameType();
      log.trace("ID for ManagedObject: " + key + ", attachmentName: " + mo.getAttachmentName());

      // See if this is a runtime ManagedObject
      Map<String, Annotation> moAnns = mo.getAnnotations();

      // Create ManagedComponents for ManagedObjects annotated with ManagementComponent
      ManagementComponent mc = (ManagementComponent) moAnns.get(ManagementComponent.class.getName());
      if (mc != null && md.getComponent(mo.getName()) == null)
      {
         ComponentType type = new ComponentType(mc.type(), mc.subtype());
         MutableManagedComponent comp = new ManagedComponentImpl(type, md, mo);

         if (mo.getTransientAttachment(MBeanRuntimeComponentDispatcher.class.getName()) == null)
            comp = this.proxyFactory.createComponentProxy(comp);

         md.addComponent(mo.getName(), comp);
         log.trace("Processing ManagementComponent(" + mo.getName() + "): " + comp);
         Set<ManagedComponent> typeComps = compByCompType.get(type);
         if (typeComps == null)
         {
            typeComps = new HashSet<ManagedComponent>();
            compByCompType.put(type, typeComps);
         }
         typeComps.add(comp);
         RunState state = updateRunState(mo, comp);
      }

      ManagementObject managementObject = (ManagementObject) moAnns.get(ManagementObject.class.getName());
      if (managementObject != null && managementObject.isRuntime())
      {
         boolean merged = false;
         ManagementComponent component = managementObject.componentType();
         boolean isMC = !(component.type().length() == 0 && component.subtype().length() == 0);

         // Merge this with the ManagedObject
         ManagedObject parentMO = moRegistry.get(key);
         if (parentMO == null && isMC == false)
         {
            log.trace("Deferring resolution of runtime ManagedObject: " + managementObject);
            // Save the runtime mo for merging
            runtimeMOs.put(key, mo);
         }
         else
         {
            mergeRuntimeMO(parentMO, mo);
            merged = true;
            runtimeMOs.remove(key);
         }
         // Update the runtime state of any ManagedComponent associated with this runtime mo
         ManagedComponent comp = md.getComponent(mo.getName());
         if (comp != null)
         {
            RunState state = updateRunState(mo, comp);
            log.trace("Updated component: " + comp + " run state to: " + state);
         }
         // There is no further processing of runtime ManagedObjects, unless its marked as a component
         if (isMC == false)
            return;
         //
         else if (merged == false)
         {
            Set<ManagedOperation> runtimeOps = mo.getOperations();
            runtimeOps = createOperationProxies(mo, runtimeOps);
            MutableManagedObject moi = (MutableManagedObject) mo;
            moi.setOperations(runtimeOps);
         }
      }
      else
      {
         // See if there is runtime info to merge
         ManagedObject runtimeMO = runtimeMOs.get(key);
         if (runtimeMO != null)
         {
            mergeRuntimeMO(mo, runtimeMO);
            runtimeMOs.remove(key);
            // Update the runtime state of any ManagedComponent associated with this runtime mo
            ManagedComponent comp = md.getComponent(mo.getName());
            if (comp != null)
            {
               RunState state = updateRunState(runtimeMO, comp);
               log.trace("Updated component: " + comp + " run state to: " + state);
            }
         }
      }

      // Update the MO registry
      // TODO - does this make sense? In case of a MetaType.isCollection we could get different results then
      //      ManagedObject prevMO = moRegistry.put(key, mo);
      //      if( prevMO != null )
      //      {
      //         // This should only matter for ManagedObjects that have a ManagementObjectID
      //         log.trace("Duplicate mo for key: "+key+", prevMO: "+prevMO);
      //         return;
      //      }
      // Check for unresolved refs
      checkForReferences(key, mo);

      // Scan for @ManagementObjectRef
      for (ManagedProperty prop : mo.getProperties().values())
      {
         log.trace("Checking property: " + prop);
         // See if this is a ManagementObjectID
         Map<String, Annotation> pannotations = prop.getAnnotations();
         if (pannotations != null && pannotations.isEmpty() == false)
         {
            ManagementObjectID id = (ManagementObjectID) pannotations.get(ManagementObjectID.class.getName());
            if (id != null)
            {
               Object refName = getRefName(prop.getValue());
               if (refName == null)
                  refName = id.name();
               String propKey = refName + "/" + id.type();
               log.trace("ManagedProperty level ID for ManagedObject: " + propKey + ", attachmentName: "
                     + mo.getAttachmentName());
               moRegistry.put(propKey, mo);
               checkForReferences(propKey, mo);
            }
            // See if this is a ManagementObjectRef
            ManagementObjectRef ref = (ManagementObjectRef) pannotations.get(ManagementObjectRef.class.getName());
            if (ref != null)
            {
               // The reference key is the prop value + ref.type()
               log.trace("Property(" + prop.getName() + ") references: " + ref);
               Object refName = getRefName(prop.getValue());
               if (refName == null)
                  refName = ref.name();
               String targetKey = refName + "/" + ref.type();
               ManagedObject target = moRegistry.get(targetKey);
               if (target != null)
               {
                  log.trace("Resolved property(" + prop.getName() + ") reference to: " + targetKey);
                  prop.setTargetManagedObject(target);
               }
               else
               {
                  Set<ManagedProperty> referers = unresolvedRefs.get(targetKey);
                  if (referers == null)
                  {
                     referers = new HashSet<ManagedProperty>();
                     unresolvedRefs.put(targetKey, referers);
                  }
                  referers.add(prop);
               }
            }
         }

         MetaType propType = prop.getMetaType();
         if (propType == AbstractManagedObjectFactory.MANAGED_OBJECT_META_TYPE)
         {
            processGenericValue((GenericValue) prop.getValue(), md);
         }
         else if (propType.isArray())
         {
            ArrayMetaType amt = (ArrayMetaType) propType;
            MetaType etype = amt.getElementType();
            if (etype == AbstractManagedObjectFactory.MANAGED_OBJECT_META_TYPE)
            {
               ArrayValue avalue = (ArrayValue) prop.getValue();
               int length = avalue != null ? avalue.getLength() : 0;
               for (int n = 0; n < length; n++)
                  processGenericValue((GenericValue) avalue.getValue(n), md);
            }
         }
         else if (propType.isCollection())
         {
            CollectionMetaType amt = (CollectionMetaType) propType;
            MetaType etype = amt.getElementType();
            if (etype == AbstractManagedObjectFactory.MANAGED_OBJECT_META_TYPE)
            {
               CollectionValue avalue = (CollectionValue) prop.getValue();
               if (avalue != null)
               {
                  MetaValue[] elements = avalue.getElements();
                  for (int n = 0; n < avalue.getSize(); n++)
                  {
                     GenericValue gv = (GenericValue) elements[n];
                     ManagedObject propMO = (ManagedObject) gv.getValue();
                     if (propMO != null)
                        processManagedObject(propMO, md);
                  }
               }
            }
         }
      }
   }

   /**
    * Get ref name.
    *
    * @param value property value
    * @return plain value
    */
   protected Object getRefName(Object value)
   {
      if (value instanceof MetaValue)
      {
         MetaValue metaValue = (MetaValue) value;
         if (metaValue.getMetaType().isSimple() == false)
            throw new IllegalArgumentException("Can only get ref from simple value: " + value);
         SimpleValue svalue = (SimpleValue) metaValue;
         return svalue.getValue();
      }
      return value;
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
            state = getComponentMappedState(comp, runtimeMO, name, runStateMapper);
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
      if (state == DeploymentState.UNKNOWN && dispatcher != null)
      {
         Object name = md.getName();
         if (name != null)
         {
            state = getMappedState(name, deploymentStateMapper);
         }
      }
      return state;
   }

   protected <T extends Enum<?>> T getMappedState(Object name, ContextStateMapper<T> mapper)
   {
      T state = mapper.getErrorState();
      try
      {
         if (dispatcher != null)
         {
            state = dispatcher.mapControllerState(name, mapper);
         }
      }
      catch (Exception e)
      {
         log.debug("Failed to get controller state", e);
      }
      return state;
   }

   protected <T extends Enum<?>> T getComponentMappedState(ManagedComponent comp, ManagedObject mo, Object name,
         ContextStateMapper<T> mapper)
   {
      T state = mapper.getErrorState();
      try
      {
         RuntimeComponentDispatcher dispatcher;
         if (mo != null && mo.getTransientAttachment(MBeanRuntimeComponentDispatcher.class.getName()) != null)
         {
            dispatcher = mbeanProxyFactory.getDispatcher();
         }
         else
         {
            dispatcher = this.dispatcher;
         }

         if (dispatcher != null)
         {
            state = dispatcher.mapControllerState(name, mapper);
         }
      }
      catch (Exception e)
      {
         log.debug("Failed to get controller state", e);
      }
      return state;
   }

   /**
    * Process generic value.
    *
    * @param genericValue the generic value
    * @param md the managed deployment
    * @throws Exception for any error
    */
   protected void processGenericValue(GenericValue genericValue, ManagedDeployment md) throws Exception
   {
      // TODO: a null is probably an error condition
      if (genericValue != null)
      {
         ManagedObject propMO = (ManagedObject) genericValue.getValue();
         // TODO: a null is probably an error condition
         if (propMO != null)
            processManagedObject(propMO, md);
      }
   }

   /**
    * Get the component name from managed property.
    *
    * @param property the managed property
    * @return component name or null if no coresponding component
    */
   protected Object getComponentName(ManagedProperty property)
   {
      // first check target
      ManagedObject targetObject = property.getTargetManagedObject();
      if (targetObject != null)
         return targetObject.getComponentName();

      // check owner
      targetObject = property.getManagedObject();
      return targetObject != null ? targetObject.getComponentName() : null;
   }

   protected void checkForReferences(String key, ManagedObject mo)
   {
      Set<ManagedProperty> referers = unresolvedRefs.get(key);
      log.trace("checkForReferences, " + key + " has referers: " + referers);
      if (referers != null)
      {
         for (ManagedProperty prop : referers)
         {
            prop.setTargetManagedObject(mo);
         }
         unresolvedRefs.remove(key);
      }
   }

   /**
    * Merge the and proxy runtime props and ops
    *
    * @param mo - the parent managed object to merge into. May be null if the
    * runtimeMO is a self contained managed object as is the case for runtime
    * components.
    * @param runtimeMO - the managed object with isRuntime=true to merge/proxy
    * properties and operations for.
    */
   protected void mergeRuntimeMO(ManagedObject mo, ManagedObject runtimeMO) throws Exception
   {
      Map<String, ManagedProperty> runtimeProps = runtimeMO.getProperties();
      Set<ManagedOperation> runtimeOps = runtimeMO.getOperations();
      // Get the runtime MO component name
      Object componentName = runtimeMO.getComponentName();
      log.debug("Merging runtime: " + runtimeMO.getName() + ", compnent name: " + componentName);
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

      boolean trace = log.isTraceEnabled();
      if (runtimeProps != null && runtimeProps.size() > 0)
      {
         if (trace)
            log.trace("Properties before:" + props);
         // We need to pull the runtime values for stats
         for (ManagedProperty prop : runtimeProps.values())
         {
            if (prop.hasViewUse(ViewUse.STATISTIC))
            {
               String propName = prop.getMappedName();
               try
               {
                  AbstractRuntimeComponentDispatcher.setActiveProperty(prop);
                  MetaValue propValue = dispatcher.get(componentName, propName);
                  if (propValue != null)
                     prop.setValue(propValue);
               }
               catch (Throwable t)
               {
                  log.debug("Failed to get stat value, " + componentName + ":" + propName);
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

         if (trace)
            log.trace("Properties after:" + props);
      }
      if (runtimeOps != null && runtimeOps.size() > 0)
      {
         if (trace)
            log.trace("Ops before:" + ops);
         runtimeOps = createOperationProxies(runtimeMO, runtimeOps);
         ops.addAll(runtimeOps);
         if (trace)
            log.trace("Ops after:" + ops);
      }

      MutableManagedObject moi = (MutableManagedObject) mo;
      moi.setProperties(props);
      moi.setOperations(ops);
   }

   /**
    * Create ManagedOperation wrapper to intercept
    * its invocation, pushing the actual invocation
    * to runtime component.
    *
    * @param mo the managed object
    * @param ops the managed operations
    * @return set of wrapped managed operations
    * @throws Exception for any error
    * @see #
    */
   protected Set<ManagedOperation> createOperationProxies(ManagedObject mo, Set<ManagedOperation> ops) throws Exception
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

   private ManagedProperty createPropertyProxy(ManagedProperty prop) throws Exception
   {
      if (proxyFactory == null)
         throw new IllegalArgumentException("Missing RuntimeComponentDispatcher.");

      // Create the delegate property
      Object componentName = prop.getManagedObject().getComponentName();

      if (prop.getManagedObject().getTransientAttachment(MBeanRuntimeComponentDispatcher.class.getName()) != null)
         return mbeanProxyFactory.createPropertyProxy(prop, componentName);

      return proxyFactory.createPropertyProxy(prop, componentName);
   }

   /**
    * Create a defensive copy of this state.
    * 
    * @return the view state copy
    */
   protected ManagementViewState copy() {
      final ManagementViewState state = new ManagementViewState(dispatcher, proxyFactory, mbeanProxyFactory, runStateMapper, deploymentStateMapper);
      state.compByCompType.putAll(this.compByCompType);
      state.managedDeployments.putAll(this.managedDeployments);
      state.moRegistry.putAll(this.moRegistry);
      state.rootDeployments.addAll(this.rootDeployments);
      return state;
   }
   
}
