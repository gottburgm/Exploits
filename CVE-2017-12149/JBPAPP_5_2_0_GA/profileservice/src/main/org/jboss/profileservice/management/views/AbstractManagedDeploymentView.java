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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jboss.logging.Logger;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.DeploymentState;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.MutableManagedObject;
import org.jboss.managed.api.RunState;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementObjectID;
import org.jboss.managed.api.annotation.ManagementObjectRef;
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
import org.jboss.profileservice.spi.NoSuchDeploymentException;

/**
 * A abstract managed deployment view.
 * 
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @author ales.justin@jboss.org
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * 
 * @version $Revision$
 */
public abstract class AbstractManagedDeploymentView
{
   
   /** The logger. */
   private static final Logger log = Logger.getLogger(AbstractManagedDeploymentView.class);

   /** An index of ManagedComponent by ComponentType */
   private Map<ComponentType, Set<ManagedComponent>> compByCompType = new HashMap<ComponentType, Set<ManagedComponent>>();
   
   /** id/type key to ManagedObject map */
   private Map<String, ManagedObject> moRegistry = new HashMap<String, ManagedObject>();
   
   /** The ManagedPropertys with unresolved ManagementObjectRefs */
   private Map<String, Set<ManagedProperty>> unresolvedRefs = new HashMap<String, Set<ManagedProperty>>();
   
   /** A map of runtime ManagedObjects needing to be merged with their matching ManagedObject. */
   private Map<String, ManagedObject> runtimeMOs = new HashMap<String, ManagedObject>();
   
   /** The deployment name to ManagedDeployment map */
   private Map<String, ManagedDeployment> managedDeployments = new TreeMap<String, ManagedDeployment>();   
   
   /** The root deployments to resolve the deployment name. */
   private List<String> rootDeployments = new ArrayList<String>();  

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
   protected void processManagedDeployment(ManagedDeployment md, DeploymentState state, int level, boolean trace) throws Exception
   {
      String name = md.getName();
      if (trace)
         log.trace(name + " ManagedDeployment_" + level + ": " + md);
      Map<String, ManagedObject> mos = md.getManagedObjects();
      if (trace)
         log.trace(name + " ManagedObjects_ " + level + ": " + mos);
      
      // Set the deployment state
      if(state != null && md instanceof ManagedDeploymentImpl)
         ((ManagedDeploymentImpl)md).setDeploymentState(state);
      
      for(ManagedObject mo : mos.values())
      {
         processManagedObject(mo, md);
      }
      managedDeployments.put(name, md);
      
      // Add root deployments
      if(level == 0)
         this.rootDeployments.add(name);
      
      // Process children
      List<ManagedDeployment> mdChildren = md.getChildren();
      if(mdChildren != null && mdChildren.isEmpty() == false)
      {
         for(ManagedDeployment mdChild : mdChildren)
         {
            // process the child deployments, with the state of the parent.
            processManagedDeployment(mdChild, state, level + 1, trace);
         }
      }
   }
   
   /**
    * Get the deployment names for a given type.
    * 
    * @param type the deployment type
    * @return the matching deployments
    */
   public Set<String> getDeploymentNamesForType(String type)
   {
      boolean trace = log.isTraceEnabled();
      HashSet<String> matches = new HashSet<String>();
      for(ManagedDeployment md : managedDeployments.values())
      {
         String name = md.getName();
         Set<String> types = md.getTypes();
         if(types != null)
         {
            if(types.contains(type))
            {
               if(trace)
                  log.trace(name+" matches type: "+type+", types:"+types);
               matches.add(name);
            }
         }
      }
      return matches;
   }
   
   /**
    * Get the deployments for a given type.
    * 
    * @param type the deployment type.
    * @return the matching deployments
    */
   public Set<ManagedDeployment> getDeploymentsForType(String type)
   {
      Set<String> names = getDeploymentNamesForType(type);
      HashSet<ManagedDeployment> mds = new HashSet<ManagedDeployment>();
      for(String name : names)
      {
         ManagedDeployment md = this.managedDeployments.get(name);
         mds.add(md);
      }
      return mds;
   }
   
   public Set<ComponentType> getComponentTypes()
   {
      return compByCompType.keySet();      
   }

   /**
    * Get component for a given type.
    * 
    * @param type the component type
    * @return a set of matching components
    */
   public Set<ManagedComponent> getComponentsForType(ComponentType type)
   {
      Set<ManagedComponent> comps = compByCompType.get(type);
      if(comps == null)
         comps = Collections.emptySet();
      return comps;
   }
   
   /**
    * Get the deployment names.
    * 
    * @return the deployment names
    */
   public Set<String> getDeploymentNames()
   {
      return this.managedDeployments.keySet();
   }
   
   /**
    * Find a deployment.
    * 
    * @param name
    * @return
    * @throws NoSuchDeploymentException
    */
   public Collection<ManagedDeployment> getDeployment(String name) throws NoSuchDeploymentException
   {
      if(name == null)
         throw new IllegalArgumentException("Null deployment name");

      List<ManagedDeployment> deployments = new ArrayList<ManagedDeployment>();
      // Check the file name
      
      if(this.managedDeployments.containsKey(name))
      {
         ManagedDeployment md = this.managedDeployments.get(name);
         if(md != null)
            deployments.add(md);
      }
      else
      {
         // Look for a simple name
         for(String deployment : this.rootDeployments)
         {
            String fixedDeploymentName = deployment;
            if(deployment.endsWith("/"))
               fixedDeploymentName = deployment.substring(0, deployment.length() - 1);

            if(fixedDeploymentName.endsWith(name))
            {
               ManagedDeployment md = this.managedDeployments.get(deployment);
               if(md != null)
                  deployments.add(md);
            }
         }  
      }
      return deployments;
   }
   
   protected abstract void mergeRuntimeMO(ManagedObject mo, ManagedObject runtimeMO) throws Exception;

   protected abstract Set<ManagedOperation> createOperationProxies(ManagedObject mo, Set<ManagedOperation> runtimeOps) throws Exception;

   protected abstract RunState updateRunState(ManagedObject mo, ManagedComponent comp) throws Exception;
   
   /**
    * Process managed object.
    *
    * @param mo the managed object
    * @param md the managed deployment
    */
   protected void processManagedObject(ManagedObject mo, ManagedDeployment md)
      throws Exception
   {
      String key = mo.getName() + "/" + mo.getNameType();
      if(mo.getName().equals("org.jboss.security.plugins.SecurityConfig"))
         log.info("Saw SecurityConfig MO");
      log.debug("ID for ManagedObject: "+key+", attachmentName: "+mo.getAttachmentName());

      // See if this is a runtime ManagedObject
      Map<String, Annotation> moAnns = mo.getAnnotations();
      ManagementObject managementObject = (ManagementObject) moAnns.get(ManagementObject.class.getName());
      if (managementObject.isRuntime())
      {
         boolean merged = false;
         ManagementComponent mc = managementObject.componentType();
         boolean isMC = !(mc.type().length() == 0 && mc.subtype().length() == 0);
         
         // Merge this with the ManagedObject
         ManagedObject parentMO = moRegistry.get(key);
         if (parentMO == null && isMC == false)
         {
            log.debug("Deferring resolution of runtime ManagedObject: "+managementObject);
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
            log.debug("Updated component: "+comp+" run state to: "+state);
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
               log.debug("Updated component: "+comp+" run state to: "+state);
            }
         }
      }

      // Update the MO registry
      // TODO - does this make sense? In case of a MetaType.isCollection we could get different results then
//      ManagedObject prevMO = moRegistry.put(key, mo);
//      if( prevMO != null )
//      {
//         // This should only matter for ManagedObjects that have a ManagementObjectID
//         log.debug("Duplicate mo for key: "+key+", prevMO: "+prevMO);
//         return;
//      }
      // Check for unresolved refs
      checkForReferences(key, mo);

      // Map any existing ManagedComponent types
      for(ManagedComponent comp : md.getComponents().values())
      {
         log.debug("Updating ManagementComponent: "+comp);
         ComponentType type = comp.getType();
         Set<ManagedComponent> typeComps = compByCompType.get(type);
         if (typeComps == null)
         {
            typeComps = new HashSet<ManagedComponent>();
            compByCompType.put(type, typeComps);
         }
         typeComps.add(comp);
      }

      // Create ManagedComponents for ManagedObjects annotated with ManagementComponent
      ManagementComponent mc = (ManagementComponent) moAnns.get(ManagementComponent.class.getName());
      if (mc != null && md.getComponent(mo.getName()) == null)
      {
         ComponentType type = new ComponentType(mc.type(), mc.subtype());
         ManagedComponentImpl comp = new ManagedComponentImpl(type, md, mo);
         md.addComponent(mo.getName(), comp);
         log.debug("Processing ManagementComponent("+mo.getName()+"): "+comp);
         Set<ManagedComponent> typeComps = compByCompType.get(type);
         if (typeComps == null)
         {
            typeComps = new HashSet<ManagedComponent>();
            compByCompType.put(type, typeComps);
         }
         typeComps.add(comp);
         updateRunState(null, comp);
      }

      // Scan for @ManagementObjectRef
      for(ManagedProperty prop : mo.getProperties().values())
      {
         log.debug("Checking property: "+prop);
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
               log.debug("ManagedProperty level ID for ManagedObject: "+propKey+", attachmentName: "+mo.getAttachmentName());
               moRegistry.put(propKey, mo);
               checkForReferences(propKey, mo);
            }
            // See if this is a ManagementObjectRef
            ManagementObjectRef ref = (ManagementObjectRef) pannotations.get(ManagementObjectRef.class.getName());
            if ( ref != null )
            {
               // The reference key is the prop value + ref.type()
               log.debug("Property("+prop.getName()+") references: "+ref);
               Object refName = getRefName(prop.getValue());
               if (refName == null)
                  refName = ref.name();
               String targetKey = refName + "/" + ref.type();
               ManagedObject target = moRegistry.get(targetKey);
               if (target != null)
               {
                  log.debug("Resolved property("+prop.getName()+") reference to: "+targetKey);
                  prop.setTargetManagedObject(target);
               }
               else
               {
                  Set<ManagedProperty> referers =  unresolvedRefs.get(targetKey);
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
            processGenericValue ((GenericValue)prop.getValue(), md);
         }
         else if (propType.isArray())
         {
            ArrayMetaType amt = (ArrayMetaType) propType;
            MetaType etype = amt.getElementType();
            if (etype == AbstractManagedObjectFactory.MANAGED_OBJECT_META_TYPE)
            {
               ArrayValue avalue = (ArrayValue) prop.getValue();
               int length = avalue != null ? avalue.getLength() : 0;
               for(int n = 0; n < length; n ++)
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
               if(avalue != null)
               {
                  MetaValue[] elements = avalue.getElements();
                  for(int n = 0; n < avalue.getSize(); n ++)
                  {
                     GenericValue gv = (GenericValue) elements[n];
                     ManagedObject propMO = (ManagedObject) gv.getValue();
                     if(propMO != null)
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
         MetaValue metaValue = (MetaValue)value;
         if (metaValue.getMetaType().isSimple() == false)
            throw new IllegalArgumentException("Can only get ref from simple value: " + value);
         SimpleValue svalue = (SimpleValue) metaValue;
         return svalue.getValue();
      }
      return value;
   }

   /**
    * Check for references.
    * 
    * @param key the property key
    * @param mo the managed object
    */
   protected void checkForReferences(String key, ManagedObject mo)
   {
      Set<ManagedProperty> referers =  unresolvedRefs.get(key);
      log.debug("checkForReferences, "+key+" has referers: "+referers);
      if (referers != null)
      {
         for(ManagedProperty prop : referers)
         {
            prop.setTargetManagedObject(mo);
         }
         unresolvedRefs.remove(key);
      }      
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
   
}
