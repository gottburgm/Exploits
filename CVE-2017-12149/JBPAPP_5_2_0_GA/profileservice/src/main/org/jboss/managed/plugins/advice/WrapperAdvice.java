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
package org.jboss.managed.plugins.advice;

import java.util.Set;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.proxy.container.GeneratedAOPProxyFactory;
import org.jboss.managed.api.Fields;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;

/**
 * WrapperAdvice, intercepts methods that produce objects
 * that require proxies.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85526 $
 */
public class WrapperAdvice
{
   /**
    * Wrap a managed object
    * 
    * @param managedObject the managed object
    * @return the managed object wrapper
    */
   public static ManagedObject wrapManagedObject(ManagedObject managedObject)
   {
      return createProxy(managedObject, ManagedObject.class);
   }
   
   /**
    * Wrap a managed property
    * 
    * @param managedProperty the managed property
    * @return the managed property wrapper
    */
   public static ManagedProperty wrapManagedProperty(ManagedProperty managedProperty)
   {
      return createProxy(managedProperty, ManagedProperty.class);
   }
   
   /**
    * Wrap fields
    * 
    * @param fields the fields
    * @return the fields wrapper
    */
   public static Fields wrapFields(Fields fields)
   {
      return createProxy(fields, Fields.class);
   }

   /**
    * Wrap a returned managed object
    * 
    * @param invocation the invocation
    * @return the wrapped managed object
    * @throws Throwable for any error
    */
   public ManagedObject wrapManagedObject(Invocation invocation) throws Throwable
   {
      ManagedObject result = (ManagedObject) invocation.invokeNext();
      return wrapManagedObject(result);
   }

   /**
    * Wrap a returned managed property
    * 
    * @param invocation the invocation
    * @return the wrapped managed property
    * @throws Throwable for any error
    */
   public ManagedProperty wrapManagedProperty(Invocation invocation) throws Throwable
   {
      ManagedProperty result = (ManagedProperty) invocation.invokeNext();
      return wrapManagedProperty(result);
   }

   /**
    * Wrap a returned managed property set
    * 
    * @param invocation the invocation
    * @return the wrapped managed property set
    * @throws Throwable for any error
    */
   @SuppressWarnings("unchecked")
   public Set<ManagedProperty> wrapManagedPropertySet(Invocation invocation) throws Throwable
   {
      Set<ManagedProperty> result = (Set<ManagedProperty>) invocation.invokeNext();
      return new WrapperSet<ManagedProperty>(result, ManagedProperty.class);
   }

   /**
    * Wrap fields
    * 
    * @param invocation the invocation
    * @return the wrapped managed property
    * @throws Throwable for any error
    */
   public Fields wrapFields(Invocation invocation) throws Throwable
   {
      Fields result = (Fields) invocation.invokeNext();
      return wrapFields(result);
   }
   
   /**
    * Create a proxy 
    * 
    * @param <T> the expected type
    * @param target the target
    * @param interfaceClass the interface class
    * @return the proxy
    */
   static <T> T createProxy(T target, Class<T> interfaceClass)
   {
      return GeneratedAOPProxyFactory.createProxy(target, interfaceClass);
   }
}
