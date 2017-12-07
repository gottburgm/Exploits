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

import org.jboss.managed.api.ManagedObject;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;

/**
 * The default ManagedObject persistence plugin. 
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class DefaultManagedObjectPersistencePlugin implements ManagedObjectPersistencePlugin
{

   /** The default persistence delegate. */
   private final DefaultPersistenceDelegate persistenceDelegate;
   
   /** The default recreation delegate. */
   private final DefaultRecreationDelegate recreationDelegate;
   
   public DefaultManagedObjectPersistencePlugin(AbstractValuePersistence valuePersistence,
         AbstractValueRecreation valueRecreation, ManagedObjectRecreationHelper helper)
   {
      // Create the delegates
      this.persistenceDelegate = new DefaultPersistenceDelegate(valuePersistence);
      this.recreationDelegate = new DefaultRecreationDelegate(valueRecreation, helper);
   }
   
   public String getType()
   {
      return Object.class.getName();
   }
   
   public AbstractValuePersistence getValuePersistence()
   {
      return persistenceDelegate.getValuePersistence();
   }
   
   public AbstractValueRecreation getValueRecreation()
   {
      return recreationDelegate.getValueRecreation();
   }
   
   public PersistedManagedObject createPersistedManagedObject(ManagedObject mo)
   {
      return persistenceDelegate.createPersistedManagedObject(mo);
   }
   
   public PersistedManagedObject createPersistedManagedObject(PersistedManagedObject persisted, ManagedObject mo)
   {
      return persistenceDelegate.createPersistedManagedObject(persisted, mo);
   }
   
   public ManagedObject createManagedObject(PersistedManagedObject persisted)
   {
      return recreationDelegate.createManagedObject(persisted);
   }
   
   public ManagedObject updateManagedObject(PersistedManagedObject persisted, ManagedObject mo)
   {
      return recreationDelegate.updateManagedObject(persisted, mo);
   }
   
}

