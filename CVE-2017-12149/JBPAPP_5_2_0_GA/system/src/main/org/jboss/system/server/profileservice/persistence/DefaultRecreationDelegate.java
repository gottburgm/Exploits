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
import org.jboss.managed.api.ManagedProperty;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;

/**
 * The default ManagedObject recreation plugin. This will process and update
 * the ManagedObject and it's properties.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class DefaultRecreationDelegate extends AbstractManagedObjectRecreation
{
   
   /** The recreation helper. */
   private final ManagedObjectRecreationHelper helper;
   
   public DefaultRecreationDelegate(AbstractValueRecreation valueRecreation, ManagedObjectRecreationHelper helper)
   {
      super(valueRecreation);
      if(helper == null)
         throw new IllegalArgumentException("null helper");
      
      this.helper = helper;
   }

   public ManagedObject createManagedObject(PersistedManagedObject persisted)
   {
      if(persisted == null)
         throw new IllegalArgumentException("null persisted managed object.");
      
      String className = persisted.getTemplateName();
      if(className == null)
         className = persisted.getClassName();

      ManagedObject mo = createManagedObjectSkeleton(className);
      if(mo == null)
         throw new RuntimeException("could not recreate managed object for class " + className);
      return updateManagedObject(persisted, mo);
   }

   public ManagedObject updateManagedObject(PersistedManagedObject persisted, ManagedObject mo)
   {
      if(persisted == null)
         throw new IllegalArgumentException("null persisted managed object");
      if(mo == null)
         throw new IllegalArgumentException("null managed object");
      
      // Process properties
      processProperties(persisted, mo);
      
      return mo;
   }
   
   protected ManagedObject createManagedObjectSkeleton(String className)
   {
      try
      {
         return this.helper.createManagedObjectSkeleton(className);
      }
      catch(ClassNotFoundException cnfe)
      {
         throw new RuntimeException("could not recreate attachment", cnfe);
      }
   }
   
   @Override
   protected void setValue(String name, ManagedProperty property, Object attachment)
   {
      try
      {
         this.helper.setValue(name, property, attachment);
      }
      catch (Throwable e)
      {
         throw new RuntimeException("Could not set value for property: " + property, e);
      }
   }
}

