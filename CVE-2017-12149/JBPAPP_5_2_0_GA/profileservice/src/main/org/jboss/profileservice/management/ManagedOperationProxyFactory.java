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
package org.jboss.profileservice.management;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.aop.Dispatcher;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.aspects.remoting.MergeMetaDataInterceptor;
import org.jboss.aspects.remoting.Remoting;
import org.jboss.aspects.security.SecurityClientInterceptor;
import org.jboss.deployers.spi.management.DelegatingComponentDispatcher;
import org.jboss.deployers.spi.management.RuntimeComponentDispatcher;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.MutableManagedComponent;
import org.jboss.profileservice.management.DelegatingComponentDispatcherImpl.ProxyRegistry;
import org.jboss.profileservice.management.client.ManagedComponentDelegate;
import org.jboss.profileservice.management.client.ManagedOperationDelegate;
import org.jboss.profileservice.management.client.ManagedPropertyDelegate;
import org.jboss.remoting.InvokerLocator;
import org.jboss.util.collection.ConcurrentReferenceHashMap;
import org.jboss.util.collection.ConcurrentReferenceHashMap.ReferenceType;

/**
 * Factory generating managed operations and properties delegating
 * requests to a RuntimeComponentDispatcher proxy.
 *
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class ManagedOperationProxyFactory implements Serializable, ProxyRegistry
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1343224268002757169L;
   private AtomicLong operationID = new AtomicLong(0);
   private AtomicLong propertyID = new AtomicLong(0);
   private RuntimeComponentDispatcher dispatcher;
   private InvokerLocator locator;
   private Map<Long, ManagedProperty> properties = new ConcurrentReferenceHashMap<Long, ManagedProperty>(ReferenceType.STRONG, ReferenceType.WEAK);
   private Map<Long, ManagedOperation> operations = new ConcurrentReferenceHashMap<Long, ManagedOperation>(ReferenceType.STRONG, ReferenceType.WEAK);
   
   /** The runtime component dispatcher proxy. */
   private DelegatingComponentDispatcher dispatcherProxy;

   /** The dispatch name. */
   private String dispatchName;

   public String getDispatchName()
   {
      return dispatchName;
   }

   public void setDispatchName(String dispatchName)
   {
      this.dispatchName = dispatchName;
   }

   public RuntimeComponentDispatcher getDispatcher()
   {
      return dispatcher;
   }
   public void setDispatcher(RuntimeComponentDispatcher dispatcher)
   {
      this.dispatcher = dispatcher;
   }

   public InvokerLocator getLocator()
   {
      return locator;
   }

   public void setLocator(InvokerLocator locator)
   {
      this.locator = locator;
   }

   public DelegatingComponentDispatcher getDispatcherProxy()
   {
      return dispatcherProxy;
   }

   public void start() throws Exception
   {
      if(this.dispatcher == null)
         throw new IllegalStateException("Null dispatcher.");
      if(this.locator == null)
         throw new IllegalStateException("Null locator.");

      this.dispatcherProxy = createDispatcherProxy();
   }

   public void clear()
   {
      //
   }

   /**
    * Get the managed operation delegate
    * 
    * @param the operation id
    * @return the operation
    * @throws IllegalStateException if the operation is not registered
    */
   public ManagedOperation getManagedOperation(Long opID)
   {
      ManagedOperation op = this.operations.get(opID);
      if(op == null)
         throw new IllegalStateException("operation not found for id " + opID);
      return op;
   }

   /**
    * Get the managed property delegate
    * 
    * @param the property id
    * @return the property
    * @throws IllegalStateException if the property is not registered
    */
   public ManagedProperty getManagedProperty(Long propID)
   {
      ManagedProperty prop = this.properties.get(propID);
      if(prop == null)
         throw new IllegalStateException("property not found for id " + propID);
      return prop;
   }

   /**
    * Create a delegating managed component.
    * 
    * @param comp the managed component
    * @return the delegating managed component
    */
   public MutableManagedComponent createComponentProxy(MutableManagedComponent comp)
   {
      // 
      if(comp.getComponentName() == null)
         return comp;
      
      // Check if this is a delegate component already
      if(comp instanceof ManagedComponentDelegate)
         return comp;
      
      return new ManagedComponentDelegate(comp.getComponentName(), comp, dispatcherProxy);
   }
   
   /**
    * Create a delegating managed property. This is used for ViewUse.STATISTIC properties,
    * delegating the getValue() request to the dispatcher proxy.
    *
    * @param delegate the original property
    * @param componentName the component name
    * @return the delegate managed property
    */
   public ManagedProperty createPropertyProxy(ManagedProperty delegate, Object componentName)
   {
      if(delegate instanceof ManagedPropertyDelegate)
      {
         // Ignore ManagedPropertyDelegate
         return delegate;
      }
      else
      {
         long propID = propertyID.incrementAndGet();
         ManagedPropertyDelegate proxy = new ManagedPropertyDelegate(propID, delegate, componentName, dispatcherProxy);
         this.properties.put(propID, proxy);
         return proxy;
      }
   }
   
   /**
    * Create managed operations for runtime components, where the invoke() can
    * be invoked on the client side.
    *
    * @param ops the managed operations
    * @param componentName the component name
    * @return a set of runtime operations
    * @throws Exception
    */
   public Set<ManagedOperation> createOperationProxies(Set<ManagedOperation> ops, Object componentName)
      throws Exception
   {
      HashSet<ManagedOperation> opProxies = new HashSet<ManagedOperation>();
      for (ManagedOperation op : ops)
      {
         if(op instanceof ManagedOperationDelegate)
         {
            // Ignore ManagedOperationDelegate
            opProxies.add(op);
            continue;
         }
         else
         {
            long opID = operationID.incrementAndGet();
            ManagedOperationDelegate proxy = new ManagedOperationDelegate(opID, op, componentName, dispatcherProxy);
            this.operations.put(opID, proxy);
            opProxies.add(proxy);               
         }
      }
      return opProxies;
   }

   /**
    * Create a remoting DelegatingComponentDispatcher proxy.
    *
    * @return the proxy
    * @throws Exception
    */
   protected DelegatingComponentDispatcher createDispatcherProxy() throws Exception
   {
      // The interceptors
      ArrayList<Interceptor> interceptors = new ArrayList<Interceptor>();
      interceptors.add(SecurityClientInterceptor.singleton);
      interceptors.add(MergeMetaDataInterceptor.singleton);
      interceptors.add(InvokeRemoteInterceptor.singleton);

      Class<?>[] ifaces = {DelegatingComponentDispatcher.class};

      DelegatingComponentDispatcher delegate = new DelegatingComponentDispatcherImpl(this, this.dispatcher);

      String dispatchName = "ProfileService-" + this.dispatchName;
      Dispatcher.singleton.registerTarget(dispatchName, delegate);
      return (DelegatingComponentDispatcher) Remoting.createRemoteProxy(dispatchName,
            getClass().getClassLoader(), ifaces, locator, interceptors, "ProfileService");
   }

}
