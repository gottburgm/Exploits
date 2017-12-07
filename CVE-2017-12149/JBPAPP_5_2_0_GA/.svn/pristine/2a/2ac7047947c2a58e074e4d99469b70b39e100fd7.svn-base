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
package org.jboss.system.server.profileservice.persistence.component;

import java.util.ArrayList;

import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.system.server.profileservice.persistence.ManagedObjectPersistencePlugin;
import org.jboss.system.server.profileservice.persistence.PersistenceFactory;
import org.jboss.system.server.profileservice.persistence.xml.ModificationInfo;
import org.jboss.system.server.profileservice.persistence.xml.PersistedComponent;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;
import org.jboss.system.server.profileservice.persistence.xml.PersistedProperty;

/**
 * The abstract component mapper.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public abstract class AbstractComponentMapper implements ComponentMapper
{

   /** The persistence factory. */
   private final PersistenceFactory persistenceFactory;
   
   public AbstractComponentMapper(PersistenceFactory persistenceFactory)
   {
      if(persistenceFactory == null)
         throw new IllegalArgumentException("null persistence factory");
      
      this.persistenceFactory = persistenceFactory;
   }
   
   public ManagedObjectPersistencePlugin getPersistencePlugin()
   {
      return persistenceFactory.getPersistencePlugin();
   }
   
   public ManagedObjectFactory getMOF()
   {
      return persistenceFactory.getManagedObjectFactory();
   }
  
   public void restoreComponent(Object attachment, PersistedComponent component)
   {
      if(attachment == null)
         throw new IllegalArgumentException("null attachment");
      if(component == null)
         throw new IllegalArgumentException("null component");

      // Check what we need to do
      ModificationInfo modification = component.getModificationInfo();
      if(modification == null)
         modification = ModificationInfo.MODIFIED;
      switch(modification)
      {
         case ADDED:
            updateComponent(attachment, component, true);
            break;
         case MODIFIED:
            updateComponent(attachment, component, false);
            break;
         case REMOVED:
            removeComponent(attachment, component);
            break;
      }
   }

   public PersistedComponent addComponent(Object attachment, ManagedComponent component)
   {
      return createPersistedComponent(attachment, component, ModificationInfo.ADDED);
   }
   
   public PersistedComponent updateComponent(Object attachment, ManagedComponent component)
   {
      return createPersistedComponent(attachment, component, ModificationInfo.MODIFIED);
   }
   
   public PersistedComponent removeComponent(Object attachment, ManagedComponent component)
   {
      PersistedComponent persisted = createPersistedComponent(attachment, component, ModificationInfo.REMOVED);
      // FIXME cleanup properties, as they are not need for removed components
      persisted.setProperties(new ArrayList<PersistedProperty>());
      return persisted;
   }
   
   /**
    * Remove a component from the attachment.
    * 
    * @param attachment the attachment
    * @param component the component to remove
    */
   protected abstract void removeComponent(Object attachment, PersistedComponent component);

   /**
    * Get a ManagedObject for a given component, based on the information
    * in the attachment descriptor.
    * 
    * @param attachment the attachment
    * @param component the component
    * @param create whether to create a non existing component or not
    * @return the managed object for the component
    */
   protected abstract ManagedObject getComponent(Object attachment, PersistedComponent component, boolean create);
   
   /**
    * Create the persisted information for a given ManagedComponent.
    * 
    * @param attachment the attachment
    * @param component the managed component
    * @return the persisted component
    */
   protected PersistedComponent createComponent(Object attachment, ManagedComponent component)
   {
      // ManagedObject mo = component.getDeployment().getManagedObject(component.getName());
      ManagedObject mo = (ManagedObject) component.getParent();
      PersistedManagedObject persisted = getPersistencePlugin().createPersistedManagedObject(mo);
      PersistedComponent persistedComponent = new PersistedComponent(persisted);
      setComponentName(persistedComponent, mo);
      return persistedComponent;
   }
   
   /**
    * Set the current name for this component.
    * 
    * @param component the persisted component
    * @param mo the managed object
    */
   protected abstract void setComponentName(PersistedComponent component, ManagedObject mo);
   
   /**
    * Update a attachment descripto based on the persisted
    * component information.
    * 
    * @param attachment the attachment
    * @param component the persisted component
    * @param added whether this component was added or not
    */
   protected void updateComponent(Object attachment, PersistedComponent component, boolean added)
   {
      ManagedObject mo = getComponent(attachment, component, added);
      getPersistencePlugin().updateManagedObject(component, mo);
   }

   /**
    * Create a persisted component for a given ManagedComponent.
    * 
    * @param attachment the attachment
    * @param component the managed component
    * @param modification the modification info
    * @return the persisted component
    */
   protected PersistedComponent createPersistedComponent(Object attachment, ManagedComponent component, ModificationInfo modification)
   {
      if(attachment == null)
         throw new IllegalArgumentException("null attachment");
      if(component == null)
         throw new IllegalArgumentException("null component");
      if(modification == null)
         throw new IllegalArgumentException("null modification info");
      // Create the component and set the modification information
      PersistedComponent persisted = createComponent(attachment, component);
      persisted.setModificationInfo(modification);
      return persisted;      
   }

}
