/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.system.server.profileservice.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.system.server.profileservice.persistence.component.ComponentMapper;
import org.jboss.system.server.profileservice.persistence.component.ComponentMapperRegistry;
import org.jboss.system.server.profileservice.persistence.xml.ModificationInfo;
import org.jboss.system.server.profileservice.persistence.xml.PersistedComponent;
import org.jboss.system.server.profileservice.persistence.xml.PersistenceRoot;

/**
 * The abstract persistence factory.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class AbstractPersistenceFactory extends PersistenceFactory 
{

   /** The component mapper registry. */
   private final ComponentMapperRegistry componentMapper = ComponentMapperRegistry.getInstance();

   /** The delegating persistence plugin. */
   private final DelegatingPersistencePlugin persistencePlugin;
   
   /** The recreation helper. */
   private final ManagedObjectRecreationHelper recreationHelper;
   
   public AbstractPersistenceFactory()
   {
      this(ManagedObjectFactory.getInstance());
   }
   
   public AbstractPersistenceFactory(ManagedObjectFactory managedObjectFactory)
   {
      super(managedObjectFactory);
      this.recreationHelper = new ManagedObjectRecreationHelper(managedObjectFactory);
      this.persistencePlugin = new DelegatingPersistencePlugin(recreationHelper);
   }
   
   /**
    * {@inheritDoc}
    */
   public ManagedObjectPersistencePlugin getPersistencePlugin()
   {
      return persistencePlugin;
   }
   
   /**
    * Apply the persisted information to a attachment.
    * 
    * @param root the persistence root
    * @param attachment the root attachment
    * @param classLoader the classloader
    */
   @Override
   public void restorePersistenceRoot(PersistenceRoot root, Object parentAttachment, ClassLoader classLoader)
   {
      if(root == null)
         throw new IllegalArgumentException("null root");
      if(parentAttachment == null)
         throw new IllegalArgumentException("null attachment");
      
      if(root.getComponents() != null && root.getComponents().isEmpty() == false)
      {
         // Set the classloader
         recreationHelper.setLoader(classLoader);
         try
         {
            // Restore the components
            ComponentMapper mapper = getComponentMapper(root);
            for(PersistedComponent component : root.getComponents())
            {
               mapper.restoreComponent(parentAttachment, component);
            }
         }
         finally
         {
            recreationHelper.setLoader(null);
         }
      }
   }
   
   /**
    * Add a ManagedComponent.
    * 
    * @param root the persistence root
    * @param parent the parent managed object
    * @param component the managed component
    * @return the updated persistence root
    */
   @Override
   public PersistenceRoot addComponent(PersistenceRoot root, ManagedObject parent, ManagedComponent component)
   {
      if(root == null)
         throw new IllegalArgumentException("null persistence root");
      ComponentMapper mapper = getComponentMapper(parent);
      PersistedComponent persistedComponent = mapper.addComponent(parent.getAttachment(), component);
      return addPersistedComponent(root, persistedComponent);
   }

   /**
    * Remove a ManagedComponent.
    * 
    * @param root the persistence root
    * @param parent the parent managed object
    * @param component the managed component
    * @return the updated persistence root
    */
   @Override
   public PersistenceRoot removeComponent(PersistenceRoot root, ManagedObject parent, ManagedComponent component)
   {
      if(root == null)
         throw new IllegalArgumentException("null persistence root");
      
      ComponentMapper mapper = getComponentMapper(parent);
      PersistedComponent persistedComponent = mapper.removeComponent(parent.getAttachment(), component);
      return addPersistedComponent(root, persistedComponent);
   }

   /**
    * Update a ManagedComponent.
    * 
    * @param root the persistence root
    * @param parent the parent managed object
    * @param component the managed component
    * @return the updated persistence root
    */
   @Override
   public PersistenceRoot updateComponent(PersistenceRoot root, ManagedObject parent, ManagedComponent component)
   {
      if(root == null)
         throw new IllegalArgumentException("null persistence root");
      
      ComponentMapper mapper = getComponentMapper(parent);
      PersistedComponent persistedComponent = mapper.updateComponent(parent.getAttachment(), component);
      return addPersistedComponent(root, persistedComponent);
   }

   /**
    * Reset a component. This will remove the persisted information.
    * 
    * @param root the persistence root
    * @param parent the parent managed object
    * @param component the managed component
    * @return the updated persistence root
    */
   @Override
   public PersistenceRoot resetComponent(PersistenceRoot root, ManagedObject parent, ManagedComponent component)
   {
      ComponentMapper mapper = getComponentMapper(parent);
      PersistedComponent persistedComponent = mapper.updateComponent(parent.getAttachment(), component);
      // Map the components
      Map<String, PersistedComponent> components = mapComponents(root);
      // Remove the component
      PersistedComponent previous = components.remove(persistedComponent.getOriginalName());
      if(previous == null)
         previous = components.remove(persistedComponent.getName());
      // Set the new values
      root.setComponents(new ArrayList<PersistedComponent>(components.values()));
      return root;
   }
   
   /**
    * Add a persisted component to the root. This will map and override
    * existing components.
    * 
    * @param root the persistence root
    * @param component the persisted component
    * @return the update persistence root
    */
   protected PersistenceRoot addPersistedComponent(PersistenceRoot root, PersistedComponent component)
   {
      Map<String, PersistedComponent> components = mapComponents(root);
      PersistedComponent previous = components.remove(component.getOriginalName());
      if(previous == null)
         previous = components.remove(component.getName());
      // Add the persisted component
      components.put(component.getName(), component);
      
      // Override with some previous information
      if(previous != null)
      {
         // A added component should remain on added
         if(previous.getModificationInfo() == ModificationInfo.ADDED
               && component.getModificationInfo() == ModificationInfo.MODIFIED)
            component.setModificationInfo(ModificationInfo.ADDED);
         
         // Just remove a previously added component
         if(previous.getModificationInfo() == ModificationInfo.ADDED
               && component.getModificationInfo() == ModificationInfo.REMOVED)
            components.remove(component.getName());
         
         // Override the name
         if(previous.getOriginalName() != null)
            component.setOriginalName(previous.getOriginalName());         
      }
      
      root.setComponents(new ArrayList<PersistedComponent>(components.values()));
      return root;
   }

   /**
    * Get the component mapper for a given ManagedObject.
    * 
    * @param parent the managed object
    * @return the component mapper
    * @throws IllegalStateException if no mapper is registered for this type
    */
   protected ComponentMapper getComponentMapper(ManagedObject parent)
   {
      ComponentMapper mapper = null;
      if(parent.getAttachmentName() != null)
         mapper = getComponentMapper(parent.getAttachmentName());
      if(mapper == null && parent.getAttachment() != null)
         mapper = getComponentMapper(parent.getAttachment().getClass().getName());

      if(mapper == null)
         throw new IllegalStateException("no mapper registered for type: " + parent.getAttachmentName());
      
      return mapper;
   }
   
   /**
    * Get the component mapper for a persistence root.
    * 
    * @param root the persistence root
    * @return the component mapper
    * @throws IllegalStateException if no mapper is registered for this type
    */
   protected ComponentMapper getComponentMapper(PersistenceRoot root)
   {
      ComponentMapper mapper = null;
      if(root.getName() != null)
         mapper = getComponentMapper(root.getName());
      if(root.getClassName() != null)
         mapper = getComponentMapper(root.getClassName());
      
      if(mapper == null)
         throw new IllegalStateException("no mapper registered for type: " + root);
      
      return mapper;
   }
   
   /**
    * Get the component mapper for a given type.
    * 
    * @param type the type
    * @return the component mapper, null if not registered
    * @throw IllegalArgumentException for a null type
    */
   protected ComponentMapper getComponentMapper(String type)
   {
      if(type == null)
         throw new IllegalArgumentException("null type");

      return componentMapper.getMapper(type);
   }
   
   /**
    * Map the components based on their names.
    * 
    * @param root the persistence root
    * @return a map of persisted components
    */
   protected static Map<String, PersistedComponent> mapComponents(PersistenceRoot root)
   {
      Map<String, PersistedComponent> map = new HashMap<String, PersistedComponent>();
      if(root.getComponents() != null && root.getComponents().isEmpty() == false)
      {
         for(PersistedComponent component : root.getComponents())
         {
            // Map this based on the name, as this should 
            // match the original name of the new component
            map.put(component.getName(), component);
         }
      }
      return map;
   }

   /**
    * InstallCallback for adding a component mapper.
    * 
    * @param mapper the component mapper
    */
   public void addComponentMapper(ComponentMapper mapper)
   {
      componentMapper.addMapper(mapper);
   }
   
   /**
    * UnInstallCallback for removing a component mapper.
    * 
    * @param mapper the component mapper
    * @return the removed component mapper
    */
   public ComponentMapper removeComponentMapper(ComponentMapper mapper)
   {
      return componentMapper.removeComponentMapper(mapper);
   }
   
   /**
    * InstallCallback for adding a persistence plugin.
    * 
    * @param plugin the persistence plugin
    */
   public void addPersistencePlugin(ManagedObjectPersistencePlugin plugin)
   {
      persistencePlugin.addPlugin(plugin);
   }
   
   /**
    * UnInstallCallback for removing a persistence plugin.
    * 
    * @param plugin the plugin
    */
   public void removePersistencePlugin(ManagedObjectPersistencePlugin plugin)
   {
      persistencePlugin.removePlugin(plugin);
   }
   
}

