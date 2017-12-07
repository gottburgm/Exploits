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

import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.system.server.profileservice.persistence.xml.PersistenceRoot;

/**
 * The persistence factory.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public abstract class PersistenceFactory
{

   /** The managed object factory. */
   private final ManagedObjectFactory managedObjectFactory;
   
   protected PersistenceFactory(ManagedObjectFactory managedObjectFactory)
   {
      if(managedObjectFactory == null)
         throw new IllegalArgumentException("null managed object factory");
      //
      this.managedObjectFactory = managedObjectFactory;
   }
   
   /**
    * Get the managed object factory.
    * 
    * @return the manged object factory
    */
   public ManagedObjectFactory getManagedObjectFactory()
   {
      return managedObjectFactory;
   }
   
   /**
    * Get the persistence plugin.
    * 
    * @return the persistence plugin
    */
   public abstract ManagedObjectPersistencePlugin getPersistencePlugin();

   /**
    * Apply the persisted information to a attachment.
    * 
    * @param root the persistence root
    * @param attachment the root attachment
    * @param classLoader the classloader
    */
   public abstract void restorePersistenceRoot(PersistenceRoot root, Object attachment, ClassLoader classLoader);
   
   /**
    * Add a ManagedComponent.
    * 
    * @param parent the parent managed object
    * @param component the managed component
    * @return the updated persistence root
    */
   public PersistenceRoot addComponent(ManagedObject parent, ManagedComponent component)
   {
      if(parent == null)
         throw new IllegalArgumentException("null parent managed object");
      if(component == null)
         throw new IllegalArgumentException("null managed component");
      
      PersistenceRoot root = new PersistenceRoot();
      return addComponent(root, parent, component);
   }
   
   /**
    * Add a ManagedComponent.
    * 
    * @param root the persistence root
    * @param parent the parent managed object
    * @param component the managed component
    * @return the updated persistence root
    */
   public abstract PersistenceRoot addComponent(PersistenceRoot root, ManagedObject parent, ManagedComponent component);
   
   /**
    * Update a ManagedComponent.
    * 
    * @param parent the parent managed object
    * @param component the managed component
    * @return the updated persistence root
    */
   public PersistenceRoot updateComponent(ManagedObject parent, ManagedComponent component)
   {
      if(parent == null)
         throw new IllegalArgumentException("null parent managed object");
      if(component == null)
         throw new IllegalArgumentException("null managed component");
      
      PersistenceRoot root = new PersistenceRoot();
      return updateComponent(root, parent, component);
   }
   
   /**
    * Update a ManagedComponent.
    * 
    * @param root the persistence root
    * @param parent the parent managed object
    * @param component the managed component
    * @return the updated persistence root
    */
   public abstract PersistenceRoot updateComponent(PersistenceRoot root, ManagedObject parent, ManagedComponent component);

   /**
    * Remove a ManagedComponent.
    * 
    * @param parent the parent managed object
    * @param component the managed component
    * @return the updated persistence root
    */
   public PersistenceRoot removeComponent(ManagedObject parent, ManagedComponent component)
   {
      if(parent == null)
         throw new IllegalArgumentException("null parent managed object");
      if(component == null)
         throw new IllegalArgumentException("null managed component");
      
      PersistenceRoot root = new PersistenceRoot();
      return removeComponent(root, parent, component); 
   }
   
   /**
    * Remove a ManagedComponent.
    * 
    * @param root the persistence root
    * @param parent the parent managed object
    * @param component the managed component
    * @return the updated persistence root
    */
   public abstract PersistenceRoot removeComponent(PersistenceRoot root, ManagedObject parent, ManagedComponent component);

   /**
    * Reset a component. This will remove the persisted information.
    * 
    * @param root the persistence root
    * @param parent the parent managed object
    * @param component the managed component
    * @return the updated persistence root
    */
   public abstract PersistenceRoot resetComponent(PersistenceRoot root, ManagedObject parent, ManagedComponent component);
   
}

