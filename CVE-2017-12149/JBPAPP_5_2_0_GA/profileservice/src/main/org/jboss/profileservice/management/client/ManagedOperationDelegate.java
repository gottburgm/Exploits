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

import org.jboss.deployers.spi.management.DelegatingComponentDispatcher;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedParameter;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.values.MetaValue;

/**
 * <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 90913 $
 */
public class ManagedOperationDelegate
   implements ManagedOperation
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 2031110731596810579L;
   private long opID;
   private Object componentName;
   private ManagedOperation delegate;
   private DelegatingComponentDispatcher dispatcherProxy;

   public ManagedOperationDelegate(long opID, ManagedOperation delegate, Object componentName,
         DelegatingComponentDispatcher dispatcherProxy)
   {
      if (delegate == null)
         throw new IllegalArgumentException("Null delegate.");
      this.opID = opID;
      this.delegate = delegate;
      this.componentName = componentName;
      this.dispatcherProxy = dispatcherProxy;
   }

   public long getOpID()
   {
      return opID;
   }

   public MetaValue invoke(MetaValue... metaValues)
   {
      return dispatcherProxy.invoke(opID, componentName, delegate.getName(), metaValues);
   }

   public String getDescription()
   {
      return delegate.getDescription();
   }

   public String getName()
   {
      return delegate.getName();
   }

   public Impact getImpact()
   {
      return delegate.getImpact();
   }

   public MetaType getReturnType()
   {
      return delegate.getReturnType();
   }

   public ManagedParameter[] getParameters()
   {
      return delegate.getParameters();
   }
   
   public String[] getReflectionSignature()
   {
      return delegate.getReflectionSignature();
   }

   public <T> T getTransientAttachment(Class<T> expectedType)
   {
      return delegate.getTransientAttachment(expectedType);
   }

   public Object getTransientAttachment(String name)
   {
      return delegate.getTransientAttachment(name);
   }

   public void setTransientAttachment(String name, Object attachment)
   {
      delegate.setTransientAttachment(name, attachment);
   }
}
