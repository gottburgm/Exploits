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

import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.WeakHashMap;

import javax.management.ObjectName;

import org.jboss.beans.info.spi.BeanInfo;
import org.jboss.beans.info.spi.PropertyInfo;
import org.jboss.config.plugins.property.PropertyConfiguration;
import org.jboss.config.spi.Configuration;
import org.jboss.logging.Logger;
import org.jboss.managed.api.Fields;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.managed.plugins.factory.AbstractManagedObjectFactory;
import org.jboss.managed.spi.factory.InstanceClassFactory;
import org.jboss.metatype.api.types.CollectionMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.GenericValue;
import org.jboss.metatype.api.values.InstanceFactory;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.MetaValueFactory;
import org.jboss.metatype.plugins.values.ListInstanceFactory;
import org.jboss.metatype.plugins.values.SetInstanceFactory;
import org.jboss.metatype.plugins.values.SortedSetInstanceFactory;
import org.jboss.metatype.spi.values.MetaMapper;
import org.jboss.reflect.spi.ClassInfo;

/**
 * The AttachmentPropertyPopulator, this writes the values of a 
 * ManagedProperty to the attachment using a registered ICF.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 88716 $
 */
public class AttachmentPropertyPopulator
{

   /** The logger. */
   private static final Logger log = Logger.getLogger(AttachmentPropertyPopulator.class);
   
   /** The meta value factory. */
   private static final MetaValueFactory metaValueFactory = MetaValueFactory.getInstance();
   
   /** The configuration */
   private static final Configuration configuration;
   
   /** The managed object factory. */
   private final ManagedObjectFactory managedObjectFactory;
   
   /** The helper. */
   private final ManagedObjectRecreationHelper helper;
   
   /** The instance factory builders */
   private Map<Class<?>, InstanceFactory<?>> instanceFactoryMap = new WeakHashMap<Class<?>, InstanceFactory<?>>();

   static
   {
      configuration = AccessController.doPrivileged(new PrivilegedAction<Configuration>()
      {
         public Configuration run()
         {
            return new PropertyConfiguration();
         }
      });
   }
   
   public <T> void setInstanceFactory(Class<T> clazz, InstanceFactory<T> factory)
   {
      synchronized(instanceFactoryMap)
      {
         if (factory == null)
            instanceFactoryMap.remove(clazz);
         else
            instanceFactoryMap.put(clazz, factory);
      }
   }
   
   public AttachmentPropertyPopulator(ManagedObjectFactory managedObjectFactory,
         ManagedObjectRecreationHelper helper)
   {
      if(managedObjectFactory == null)
         throw new IllegalArgumentException("null managed object factory.");
      if(helper == null)
         throw new IllegalArgumentException("null helper");
      
      this.managedObjectFactory = managedObjectFactory;
      this.helper = helper;
      // set default collection instance factories
      setInstanceFactory(List.class, ListInstanceFactory.INSTANCE);
      setInstanceFactory(Set.class, SetInstanceFactory.INSTANCE);
      setInstanceFactory(SortedSet.class, SortedSetInstanceFactory.INSTANCE);
   }
   
   /**
    * Process a ManagedProperty.
    * 
    * @param propertyElement the persisted xml meta data.
    * @param name the property name.
    * @param property the managed property.
    * @param attachment the managed object attachment.
    */
   public void processManagedProperty(String name, ManagedProperty property, Object attachment) throws Throwable
   {
      boolean trace = log.isTraceEnabled();
      PropertyInfo propertyInfo = property.getField(Fields.PROPERTY_INFO, PropertyInfo.class);     
      // Skip not writable properties
      if (propertyInfo == null || propertyInfo.isWritable() == false)
      {
         if (trace)
            log.debug("Skipping not writable property " + propertyInfo);
         return;
      }

      // Get the meta data information
      MetaType metaType = property.getMetaType();
      MetaValue value = property.getValue();
      if(value != null)
         metaType = value.getMetaType();

      MetaMapper<?> mapper = property.getTransientAttachment(MetaMapper.class);
      if (mapper == null)
      {
         if(metaType.isComposite())
         {
            // FIXME skip CompositeValueInvocationHandler
            if(metaType.getTypeName().equals(ObjectName.class.getName()) == false)
            {
               // unwrap
               Object unwrapped = metaValueFactory.unwrap(value, propertyInfo.getType());
               if(unwrapped != null)
               {
                  if (Proxy.isProxyClass(unwrapped.getClass()))
                     throw new IllegalStateException("cannot unwrap composite value for property " + property.getName()
                           + " maybe missing @MetaMapping? " + metaType.getTypeName());                  
               }
            }
         }
         else if(metaType.isCollection())
         {
            CollectionMetaType collectionMetaType = (CollectionMetaType) metaType;
            if(collectionMetaType.getElementType() == AbstractManagedObjectFactory.MANAGED_OBJECT_META_TYPE)
            {
               // FIXME
               Collection<?> newCollection = unwrapGenericCollection((CollectionValue) value, propertyInfo);
               propertyInfo.set(attachment, newCollection);
               return;
            }
         }
      }
      
      // Set value
      InstanceClassFactory icf = managedObjectFactory.getInstanceClassFactory(attachment.getClass(), null);
      BeanInfo beanInfo = propertyInfo.getBeanInfo();
      icf.setValue(beanInfo, property, attachment, value);
   }
   
   @SuppressWarnings("unchecked")
   protected Collection<?> unwrapGenericCollection(CollectionValue collection, PropertyInfo propertyInfo) throws Throwable
   {
      // Create a new collection        
      BeanInfo beanInfo = configuration.getBeanInfo(propertyInfo.getType());
      Collection newCollection = (Collection) createNewInstance(beanInfo);
      
      // unwrap generic collection
      for(MetaValue value : collection.getElements())
      {
         ManagedObject o = (ManagedObject) ((GenericValue)value).getValue();
         newCollection.add(o.getAttachment());
      }
      return newCollection;
   }
   
   @SuppressWarnings("deprecation")
   protected Object createNewInstance(BeanInfo beanInfo) throws Throwable
   {
      ClassInfo classInfo = beanInfo.getClassInfo();
      if (classInfo.isInterface())
      {
         InstanceFactory<?> instanceFactory = instanceFactoryMap.get(classInfo.getType());
         if (instanceFactory == null)
            throw new IllegalArgumentException("Cannot instantiate interface BeanInfo, missing InstanceFactory: " + classInfo);

         return instanceFactory.instantiate(beanInfo);
      }
      return beanInfo.newInstance();
   }

}
