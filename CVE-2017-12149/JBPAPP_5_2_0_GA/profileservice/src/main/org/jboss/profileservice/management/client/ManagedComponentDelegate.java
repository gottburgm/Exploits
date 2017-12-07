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
package org.jboss.profileservice.management.client;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import org.jboss.deployers.spi.management.DelegatingComponentDispatcher;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedCommon;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.MutableManagedComponent;
import org.jboss.managed.api.RunState;

/**
 * A ManagedComponent used to proxy and dispatch the RunState on request.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class ManagedComponentDelegate implements MutableManagedComponent
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;   
   private Object componentName;
   private MutableManagedComponent delegate;
   private DelegatingComponentDispatcher dispatcher;
   
   public ManagedComponentDelegate(Object componentName, MutableManagedComponent delegate, DelegatingComponentDispatcher dispatcher)
   {
      this.componentName = componentName;
      this.delegate = delegate;
      this.dispatcher = dispatcher;
   }
   
   public Map<String, Annotation> getAnnotations()
   {
      return delegate.getAnnotations();
   }

   public ManagedDeployment getDeployment()
   {
      return delegate.getDeployment();
   }

   public RunState getRunState()
   {
      return dispatcher.updateRunState(componentName);
   }

   public void setRunState(RunState runState)
   {
      delegate.setRunState(runState);
   }
   
   public ComponentType getType()
   {
      return delegate.getType();
   }

   public boolean update()
   {
      return delegate.update();
   }

   public String getAttachmentName()
   {
      return delegate.getAttachmentName();
   }

   public Object getComponentName()
   {
      return delegate.getComponentName();
   }

   public String getName()
   {
      return delegate.getName();
   }

   public String getNameType()
   {
      return delegate.getNameType();
   }

   public Set<ManagedOperation> getOperations()
   {
      return delegate.getOperations();
   }

   public void setOperations(Set<ManagedOperation> operations)
   {
      delegate.setOperations(operations);
   }
   
   public ManagedCommon getParent()
   {
      return delegate.getParent();
   }
   
   public ManagedProperty getProperty(String name)
   {
      return delegate.getProperty(name);
   }
   
   public Map<String, ManagedProperty> getProperties()
   {
      return delegate.getProperties();
   }

   public void setProperties(Map<String, ManagedProperty> properties)
   {
      delegate.setProperties(properties);
   }

   public Set<String> getPropertyNames()
   {
      return delegate.getPropertyNames();
   }

}
