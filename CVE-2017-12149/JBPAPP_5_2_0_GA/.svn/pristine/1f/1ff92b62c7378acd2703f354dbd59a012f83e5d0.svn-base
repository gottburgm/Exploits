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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.managed.api.ManagedObject;
import org.jboss.metatype.api.values.MetaValueFactory;
import org.jboss.metatype.plugins.values.MetaValueFactoryBuilder;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;

/**
 * A delegating persistence plugin.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class DelegatingPersistencePlugin implements ManagedObjectPersistencePlugin
{

   /** The meta data plugins. */
   private Map<String, ManagedObjectPersistencePlugin> plugins = new ConcurrentHashMap<String, ManagedObjectPersistencePlugin>();

   /** The default plugin. */
   private final ManagedObjectPersistencePlugin defaultPlugin;
   
   /** The meta value factory. */
   private final MetaValueFactory metaValueFactory = MetaValueFactoryBuilder.create();
   
   /** The value persistence. */
   private final AbstractValuePersistence valuePersistence;
   
   /** The value recreation. */
   private AbstractValueRecreation valueRecreation;
   
   public DelegatingPersistencePlugin(ManagedObjectRecreationHelper helper)
   {
      this.valuePersistence = new AbstractValuePersistence(this, metaValueFactory);
      this.valueRecreation = new AbstractValueRecreation(this);
      this.defaultPlugin = new DefaultManagedObjectPersistencePlugin(valuePersistence, valueRecreation, helper);
   }
   
   public String getType()
   {
      return Object.class.getName();
   }
   
   /**
    * Get the value recreation.
    * 
    * @return the value recreation
    */
   public AbstractValuePersistence getValuePersistence()
   {
      return this.valuePersistence;
   }
   
   /**
    * Create a persisted managed object.
    * 
    * @param mo the managed object
    * @return the persistence xml meta data for managed object
    */
   public PersistedManagedObject createPersistedManagedObject(ManagedObject mo)
   {
      if(mo == null)
         throw new IllegalArgumentException("Null managed object.");
      
      ManagedObjectPersistencePlugin plugin = getPlugin(mo);
      return plugin.createPersistedManagedObject(mo);
   }

   /**
    * Create a persisted managed object
    * 
    * @param persisted the persisted managed object
    * @param mo the managed object
    * @return the persistence xml meta data for managed object
    */
   public PersistedManagedObject createPersistedManagedObject(PersistedManagedObject persisted, ManagedObject mo)
   {
      if(persisted == null)
         throw new IllegalArgumentException("Null persisted managed object.");
      if(mo == null)
         throw new IllegalArgumentException("Null managed object.");
      
      ManagedObjectPersistencePlugin plugin = getPlugin(mo);
      return plugin.createPersistedManagedObject(persisted, mo);
   }
   
   /**
    * Get the value persistence.
    * 
    * @return the value persistence
    */
   public AbstractValueRecreation getValueRecreation()
   {
      return this.valueRecreation;
   }
   
   /**
    * Create a persisted managed object
    * 
    * @param mo the managed object
    * @return the persistence xml meta data for managed object
    */
   public ManagedObject createManagedObject(PersistedManagedObject persisted)
   {
      if(persisted == null)
         throw new IllegalArgumentException("null persisted managed object");
      
      ManagedObjectPersistencePlugin plugin = getPlugin(persisted);
      return plugin.createManagedObject(persisted);
   }

   /**
    * Create a persisted managed object
    * 
    * @param persisted the persisted managed object
    * @param mo the managed object
    * @return the persistence xml meta data for managed object
    */
   public ManagedObject updateManagedObject(PersistedManagedObject persisted, ManagedObject mo)
   {
      if(persisted == null)
         throw new IllegalArgumentException("null persisted managed object");
      if(mo == null)
         throw new IllegalArgumentException("null managed object");
      
      ManagedObjectPersistencePlugin plugin = getPlugin(persisted);
      return plugin.updateManagedObject(persisted, mo);
   }
   
   /**
    * Extract the type name and get the plugin.
    * 
    * @param mo the managed object
    * @return the managed object plugin
    */
   protected ManagedObjectPersistencePlugin getPlugin(ManagedObject mo)
   {
      String type = mo.getAttachmentName();
      if(type == null && mo.getAttachment() != null)
         type = mo.getAttachment().getClass().getName();

      return getPlugin(type);
   }
   
   /**
    * Get the plugin registered for persisted managed object.
    * 
    * @param persisted the persisted managed object
    * @return the ManagedObject recreation plugin
    */
   protected ManagedObjectPersistencePlugin getPlugin(PersistedManagedObject persisted)
   {
      if(persisted == null)
         throw new IllegalArgumentException("null persisted managed object");
      
      String className = persisted.getTemplateName();
      if(className == null)
         className = persisted.getClassName();
      return getPlugin(className);
   }
   
   /**
    * Get the plugin.
    * 
    * @param type the managed object type
    * @return the managed object plugin
    */
   public ManagedObjectPersistencePlugin getPlugin(String type)
   {
      if(type == null)
         throw new IllegalArgumentException("Null type.");
      
      ManagedObjectPersistencePlugin plugin = this.plugins.get(type);
      if(plugin == null)
         plugin = defaultPlugin;
      return plugin;
   }
   
   /**
    * Add a managed object plugin.
    * 
    * @param plugin the plugin to add
    */
   public void addPlugin(ManagedObjectPersistencePlugin plugin)
   {
      if(plugin == null)
         throw new IllegalArgumentException("Null plugin.");
      if(plugin.getType() == null)
         throw new IllegalArgumentException("Null plugin type.");

      this.plugins.put(plugin.getType(), plugin);
   }

   /**
    * Remove a managed object plugin.
    * 
    * @param plugin the plugin to remove
    */
   public void removePlugin(ManagedObjectPersistencePlugin plugin)
   {
      if(plugin == null)
         throw new IllegalArgumentException("Null plugin.");
      if(plugin.getType() == null)
         throw new IllegalArgumentException("Null plugin type.");

      this.plugins.remove(plugin.getType());
   }

}
