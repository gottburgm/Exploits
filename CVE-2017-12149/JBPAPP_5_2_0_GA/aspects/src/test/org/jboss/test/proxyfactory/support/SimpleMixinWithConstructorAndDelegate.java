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
package org.jboss.test.proxyfactory.support;

import org.jboss.aop.proxy.container.ContainerProxyCacheKey;
import org.jboss.aop.proxy.container.Delegate;
import org.jboss.aop.proxy.container.MarshalledContainerProxy;
import org.jboss.aop.proxy.container.MarshalledProxyAdvisor;

/**
 * If using a constructor and passing "this" as the parameters, the proxy gets used. The delegate (instance wrapped by proxy) is not 
 * set in the proxy until later, and if the mixin implements Delegate we will get set with the "real" instance.
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 80997 $
 */
public class SimpleMixinWithConstructorAndDelegate implements Simple, Delegate
{
   public static Object proxy;
   public static Object delegate;
   public static boolean invoked;
   
   public SimpleMixinWithConstructorAndDelegate(Object bean)
   {
      proxy = bean;
   }

   public void doSomething()
   {
      invoked = true;
   }

   public Object getDelegate()
   {
      return delegate;
   }

   public void setDelegate(Object delegate)
   {
      SimpleMixinWithConstructorAndDelegate.delegate = delegate;
   }

   public void localUnmarshal(MarshalledContainerProxy proxy)
   {
   }

   public void remoteUnmarshal(MarshalledContainerProxy proxy, MarshalledProxyAdvisor advisor)
   {
   }

   public void setContainerProxyCacheKey(ContainerProxyCacheKey key)
   {
   }
}
