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
package org.jboss.profileservice.remoting;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;

/**
 * A poji proxy invocation handler that implements a simple security
 * container using aop interceptors.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision:$
 */
public class SecurityContainer implements InvocationHandler
{
   private static ThreadLocal<Invocation> activeInvocation
      = new ThreadLocal<Invocation>();
   private List<Interceptor> interceptors;
   private Object target;

   SecurityContainer(List<Interceptor> interceptors, Object target)
   {
      this.interceptors = interceptors;
      this.target = target;
   }

   public static void setInvocation(Invocation inv)
   {
      activeInvocation.set(inv);
   }

   public Object invoke(Object proxy, Method method, Object[] args)
         throws Throwable
   {
      // Get the invocation the handler set
      Invocation inv = activeInvocation.get();
      if(inv instanceof MethodInvocation)
      {
         // Need to set the target since this is not a true aop proxy
         MethodInvocation mi = (MethodInvocation) inv;
         mi.setTargetObject(target);
         // Override the method to the poji proxy method to return the invocation method
         inv = new PojiMethodInvocation(mi, method);
      }

      // run through the interceptors
      for(Interceptor i : interceptors)
      {
         i.invoke(inv);
      }

      // Perform the invocation on the target
      try
      {
         return method.invoke(target, args);
      }
      catch(InvocationTargetException ite)
      {
         throw ite.getTargetException();
      }
   }

}
