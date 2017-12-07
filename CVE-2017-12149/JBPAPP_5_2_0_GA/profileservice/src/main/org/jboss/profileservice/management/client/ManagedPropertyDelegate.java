/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.profileservice.management.client;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jboss.deployers.spi.management.DelegatingComponentDispatcher;
import org.jboss.managed.api.Fields;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.annotation.ActivationPolicy;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.values.MetaValue;

/**
 * A ManagedProperty delegate used as the target of the ManagedProperty
 * proxies used for runtime managed objects statistics.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 90948 $
 */
public class ManagedPropertyDelegate implements ManagedProperty
{
   private final static long serialVersionUID = 1;
   private long propID;
   private Object componentName;
   private ManagedProperty delegate;
   private DelegatingComponentDispatcher dispatcher;
   
   public ManagedPropertyDelegate(long propID, ManagedProperty delegate,
         Object componentName, DelegatingComponentDispatcher dispatcher)
   {
      super();
      this.propID = propID;
      this.delegate = delegate;
      this.dispatcher = dispatcher;
      this.componentName = componentName;
   }

   public String checkValidValue(MetaValue value)
   {
      return delegate.checkValidValue(value);
   }

   public ManagedProperty copy()
   {
      return delegate.copy();
   }

   public Map<String, Annotation> getAnnotations()
   {
      return delegate.getAnnotations();
   }

   public String getDescription()
   {
      return delegate.getDescription();
   }

   public <T> T getField(String fieldName, Class<T> expected)
   {
      return delegate.getField(fieldName, expected);
   }

   public Fields getFields()
   {
      return delegate.getFields();
   }

   public Set<MetaValue> getLegalValues()
   {
      return delegate.getLegalValues();
   }

   public MetaValue getDefaultValue()
   {
      return delegate.getDefaultValue();
   }

   public ManagedObject getManagedObject()
   {
      return delegate.getManagedObject();
   }

   public String getMappedName()
   {
      return delegate.getMappedName();
   }

   public Comparable<? extends MetaValue> getMaximumValue()
   {
      return delegate.getMaximumValue();
   }

   public MetaType getMetaType()
   {
      return delegate.getMetaType();
   }

   public Comparable<? extends MetaValue> getMinimumValue()
   {
      return delegate.getMinimumValue();
   }

   public String getName()
   {
      return delegate.getName();
   }

   public ManagedObject getTargetManagedObject()
   {
      return delegate.getTargetManagedObject();
   }

   public <T> T getTransientAttachment(Class<T> expectedType)
   {
      return delegate.getTransientAttachment(expectedType);
   }

   public Object getTransientAttachment(String name)
   {
      return delegate.getTransientAttachment(name);
   }

   public MetaValue getValue()
   {
      return dispatcher.get(propID, componentName, getMappedName());
   }

   public Collection<String> getAdminViewUses()
   {
      return delegate.getAdminViewUses();
   }

   public boolean hasAnnotation(String key)
   {
      return delegate.hasAnnotation(key);
   }

   public boolean hasViewUse(ViewUse use)
   {
      return delegate.hasViewUse(use);
   }

   public ActivationPolicy getActivationPolicy()
   {
      return delegate.getActivationPolicy();
   }

   public void setModified(boolean flag)
   {
      delegate.setModified(flag);
   }

   public boolean isMandatory()
   {
      return delegate.isMandatory();
   }

   public boolean isReadOnly()
   {
      return delegate.isReadOnly();
   }

   public boolean isModified()
   {
      return delegate.isModified();
   }

   public boolean isRemoved()
   {
      return delegate.isRemoved();
   }

   public void setField(String fieldName, Serializable value)
   {
      delegate.setField(fieldName, value);
   }

   public void setManagedObject(ManagedObject managedObject)
   {
      delegate.setManagedObject(managedObject);
   }

   public void setRemoved(boolean flag)
   {
      delegate.setRemoved(flag);
   }

   public void setTargetManagedObject(ManagedObject target)
   {
      delegate.setTargetManagedObject(target);
   }

   public void setTransientAttachment(String name, Object attachment)
   {
      delegate.setTransientAttachment(name, attachment);
   }

   public void setValue(MetaValue value)
   {
      delegate.setValue(value);
   }
   
}
