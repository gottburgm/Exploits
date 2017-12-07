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
package org.jboss.aspects.versioned;

import org.jboss.aop.Advised;
import org.jboss.aop.ClassAdvisor;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.aop.proxy.ClassProxyFactory;
import org.jboss.aop.util.reference.MethodPersistentReference;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
/**
 * Invoke on a target object
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 80997 $
 */
public class VersionedObjectForwardingInterceptor implements org.jboss.aop.advice.Interceptor
{
   public VersionedObjectForwardingInterceptor()
   {
   }

   public String getName() { return "VersionedObjectForwardingInterceptor"; }

   public Object invoke(org.jboss.aop.joinpoint.Invocation invocation) throws Throwable
   {
      Object target = invocation.getTargetObject();
      if (target instanceof Advised)
      {
         ClassAdvisor advisor = (ClassAdvisor)((Advised)target)._getAdvisor();
         // We will have already gone through the interceptor chain so just invoke on the
         // object.
         return advisor.invoke(invocation);
      }
      else
      {
         if (invocation instanceof MethodInvocation)
         {
            // For non-advised methods, we can only do public method invocations
            org.jboss.aop.joinpoint.MethodInvocation methodInvocation = (org.jboss.aop.joinpoint.MethodInvocation)invocation;
            long methodHash = methodInvocation.getMethodHash();
            HashMap methodMap = ClassProxyFactory.getMethodMap(target.getClass());
            MethodPersistentReference ref = (MethodPersistentReference)methodMap.get(new Long(methodHash));
            Method method = (Method)ref.get();
            Object[] args = methodInvocation.getArguments();
            try
            {
               return method.invoke(target, args);
            }
            catch (InvocationTargetException ex)
            {
               throw ex.getTargetException();
            }
         }
         else
         {
            throw new RuntimeException("should only be METHOD invocations on a non-advised target object: " + invocation.getClass().getName());
         }
      }
   }
}
