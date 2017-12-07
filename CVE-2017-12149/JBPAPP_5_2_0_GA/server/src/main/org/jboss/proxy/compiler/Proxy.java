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
package org.jboss.proxy.compiler;

import java.io.Serializable;

import org.jboss.util.NestedRuntimeException;

/**
 * A factory for creating proxy objects.
 *      
 * @version <tt>$Revision: 81030 $</tt>
 * @author Unknown
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class Proxy
{
   /**
    * Create a new proxy instance.  
    * 
    * <p>Proxy instances will also implement {@link Serializable}.
    *
    * <p>Delegates the actual creation of the proxy to
    *    {@link Proxies#newTarget}.
    *
    * @param loader       The class loader for the new proxy instance.
    * @param interfaces   A list of classes which the proxy will implement.
    * @param h            The handler for method invocations.
    * @return             A new proxy instance.
    *
    * @throws RuntimeException    Failed to create new proxy target.
    */
   public static Object newProxyInstance(final ClassLoader loader,
                                         final Class[] interfaces,
                                         final InvocationHandler h)
   {
      // Make all proxy instances implement Serializable
      Class[] interfaces2 = new Class[interfaces.length + 1];
      System.arraycopy(interfaces, 0, interfaces2, 0, interfaces.length);
      interfaces2[interfaces2.length - 1] = Serializable.class;

      try {
         // create a new proxy
         return Proxies.newTarget(loader, h, interfaces2);
      }
      catch (Exception e) {
         throw new NestedRuntimeException("Failed to create new proxy target", e);
      }
   }

   public static void forgetProxyForClass(Class clazz)
   {
      Proxies.forgetProxyForClass(clazz);
   }

}

