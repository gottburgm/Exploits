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
package org.jboss.system.server.profileservice.persistence;

import org.jboss.managed.api.ManagedObject;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;

/**
 * The managed object persistence plugin, handling the persistence
 * of a ManagedObject.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public interface ManagedObjectPersistencePlugin
{

   /**
    * Get the plugin type.
    * 
    * @return the type
    */
   String getType();
   
   /**
    * Get the value persistence.
    * 
    * @return the value persistence
    */
   AbstractValuePersistence getValuePersistence();
   
   /**
    * Get the value recreation.
    * 
    * @return the value recreation
    */
   AbstractValueRecreation getValueRecreation();
   
   /**
    * Create a persisted managed object
    * 
    * @param mo the managed object
    * @return the persistence xml meta data for managed object
    */
   PersistedManagedObject createPersistedManagedObject(ManagedObject mo);
 
   /**
    * Create a persisted managed object
    * 
    * @param persisted the persisted managed object
    * @param mo the managed object
    * @return the persistence xml meta data for managed object
    */
   PersistedManagedObject createPersistedManagedObject(PersistedManagedObject persisted, ManagedObject mo);
   
   /**
    * Create a managed object, based on the persisted information.
    * 
    * @param persisted the persisted managed object
    * @return the managed object
    */
   ManagedObject createManagedObject(PersistedManagedObject persisted);
   
   /**
    * Update a managed object, based on the persisted information.
    * 
    * @param persisted the persisted managed object
    * @param mo the managed object
    * @return the managed object
    */
   ManagedObject updateManagedObject(PersistedManagedObject persisted, ManagedObject mo);
   
}
