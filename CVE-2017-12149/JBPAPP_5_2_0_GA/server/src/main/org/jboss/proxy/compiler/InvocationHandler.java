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

import java.lang.reflect.Method;

/**
 * An invoker is a target of method calls, where the calls are expressed
 * not as primitive Java method invocations, but according to the conventions
 * of the Core Reflection API.
 * Invokers are designed to be used along with the Core Reflection API.
 * <p>
 * The Invoker.invoke operation is similar to java.lang.reflect.Method.invoke,
 * except that the object (or objects) which receives the message is hidden
 * behind the invoker.  Also, unlike Method.invoke, the action of the
 * Invoker.invoke operation is completely under programmer control,
 * because Invoker.invoke is an interface method, not a native method.
 * <p>
 * You can wrap an invoker around an object so that the invoker passes
 * all method calls down to the object.  Such an invoker is called a
 * <em>proxy invoker</em> for that object.
 * <p>
 * You can also wrap a new object around an invoker, so that the object
 * implements some given interface (or interfaces), and passes all method
 * calls up to the invoker.
 * Such an object is called a <em>proxy target object</em> for that invoker.
 * <p>
 * You can do more complex tasks with invokers, such as passing each method
 * call through a network connection before it reaches its target object.
 * You can also filter or replicate method invocations.  You can even
 * execute the the invocations interpretively, without ever calling
 * the method on a "real" Java object.
 *
 * @see Method#invoke
 * @see Invoker#invoke
 * @see Proxies#newInvoker
 * @see Proxies#newTarget
 *
 * @author Unknown
 * @version $Revision: 81030 $
 */
public interface InvocationHandler
{
   /**
    * Processes a method invocation on a proxy instance and returns the
    * result.
    *
    * @param dummy     ???
    * @param method    The method instance corresponding to the interface
    *                  method invoked on the proxy instance.
    * @param args      An array of objects containing the values of the
    *                  arguments passed in the method invocation on the proxy
    *                  instance, or null if interface method takes no
    *                  arguments.
    * @return          The value to return from the method invocation on
    *                  the proxy instance. 
    *
    * @throws Throwable    The exception to throw from the method invocation
    *                      on the proxy instance.
    */
   Object invoke(Object dummy, Method method, Object[] args)
      throws Throwable;
}
