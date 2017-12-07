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

import org.jboss.beans.info.spi.PropertyInfo;
import org.jboss.logging.Logger;
import org.jboss.managed.api.Fields;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;
import org.jboss.system.server.profileservice.persistence.xml.PersistedProperty;
import org.jboss.system.server.profileservice.persistence.xml.PersistedValue;

/**
 * A abstract ManagedObject recreation helper.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public abstract class AbstractManagedObjectRecreation
{

   /** The logger. */
   private static final Logger log = Logger.getLogger(AbstractManagedObjectRecreation.class);
   
   /** The value recreation. */
   private final AbstractValueRecreation valueRecreation;

   public AbstractManagedObjectRecreation(AbstractValueRecreation valueRecreation)
   {
      this.valueRecreation = valueRecreation;
   }
   
   public AbstractValueRecreation getValueRecreation()
   {
      return this.valueRecreation;
   }
   
   /**
    * Process the properties of the persisted ManagedObject.
    * 
    * @param persisted the persisted managed object
    * @param mo the ManagedObject
    */
   protected void processProperties(PersistedManagedObject persisted, ManagedObject mo)
   {
      if(persisted == null)
         throw new IllegalArgumentException("null persisted managed object");
      if(mo == null)
         throw new IllegalArgumentException("null managed object");
      
      // Get the xml to see what we need to update
      if(persisted.getProperties() != null && persisted.getProperties().isEmpty() == false)
      {
         for(PersistedProperty propertyElement : persisted.getProperties())
         {
            ManagedProperty property = mo.getProperty(propertyElement.getName());
            if(property == null)
               throw new IllegalStateException("unable to find propery: "+ propertyElement.getName());

            // getProperty
            processManagedProperty(propertyElement, property, mo.getAttachment());
         }  
      }
   }

   /**
    * Process a managed property and set it's values to the attachment.
    * 
    * @param persisted the persisted property
    * @param property the managed property
    * @param attachment the attachment
    * @return the meta value of this property
    */
   protected MetaValue processManagedProperty(PersistedProperty persisted, ManagedProperty property, Object attachment)
   {
      if(attachment == null)
         throw new IllegalArgumentException("null attachment");
      
      // Get the property name
      String name = property.getMappedName() != null ? property.getMappedName() : property.getName();
      // Process the property
      MetaValue metaValue = processManagedProperty(persisted, property);
      // Set value
      setValue(name, property, attachment);
      // Return
      return metaValue;
   }
   
   /**
    * Set the value to the managed property/
    * 
    * @param name the property name
    * @param property the managed property itself
    * @param attachment the attachment
    */
   protected abstract void setValue(String name, ManagedProperty property, Object attachment);
   
   /**
    * Process the managed property.
    * 
    * @param persisted the persisted property
    * @param property the managed property
    * @return the value
    */
   protected MetaValue processManagedProperty(PersistedProperty persisted, ManagedProperty property)
   {
      return processManagedProperty(persisted, property, false);
   }
   
   /**
    * Process the managed property.
    * 
    * @param persisted the persisted property
    * @param property the managed property
    * @param trace enable trace logs
    * @return the value
    */
   protected MetaValue processManagedProperty(PersistedProperty persisted, ManagedProperty property, boolean trace)
   {
      if(persisted == null)
         throw new IllegalArgumentException("null persisted property");
      if(property == null)
         throw new IllegalArgumentException("null managed property");

      MetaValue metaValue = property.getValue();
      PersistedValue persistedValue = persisted.getValue();
      if(persistedValue != null && isProcessProperty(property, trace))
      {
         MetaType metaType = property.getMetaType();
         if(metaValue != null)
            metaType = metaValue.getMetaType();

         // Create the value based on the persisted information
         metaValue = createValue(persistedValue, metaType);

         // Update property
         property.setField(Fields.VALUE, metaValue);
         if(metaValue != null)
            property.setField(Fields.META_TYPE, metaValue.getMetaType());
      }
      return metaValue;
   }
   
   /**
    * Call the value recreation to recreate a persisted value.
    * 
    * @param value the persisted value
    * @param metaType the meta type
    * @return the meta value
    */
   protected MetaValue createValue(PersistedValue value, MetaType metaType)
   {
      return getValueRecreation().createMetaValue(value, metaType);
   }
   
   /**
    * Does this property needs to be processed.
    * 
    * @param property the managed property
    * @param trace enable trace logs
    * @return false if the property does not need to be processed otherwise true
    */
   protected boolean isProcessProperty(ManagedProperty property, boolean trace)
   {
      // 
      boolean process = false;
      if(property == null)
         return process;
      
      // Skip non configuration properties
      if(property.hasViewUse(ViewUse.CONFIGURATION) == false)
      {
         if(trace)
            log.trace("Skip non configuration property: " + property.getName());
         return process;
      }
      // Skip read only properties
      if(property.isReadOnly())
      {
         if(trace)
            log.trace("Skip readOnly property: " + property.getName());
         return process;
      }
      // Skip removed properties
      if(property.isRemoved())
      {
         if(trace)
            log.trace("Skip removed property: " + property.getName());
         return process;
      }
      // Skip read only properties
      PropertyInfo propertyInfo = property.getField(Fields.PROPERTY_INFO, PropertyInfo.class);
      if(propertyInfo != null && propertyInfo.isReadable() == false)
      {
         if(trace)
            log.trace("Skip non readable property: " + property.getName());
         return process;
      }
      return true;
   }
   
}

