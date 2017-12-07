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
package org.jboss.spring.interceptor;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.ConstructorInvocation;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.spring.support.SpringInjectionSupport;

/**
 * Injects Spring beans on ConstructorInterceptor invocation.
 * Should be the last interceptor in chain since it actually constructs EJB object
 * (in order to inject beans).
 *
 * @author <a href="mailto:ales.justin@genera-lynx.com">Ales Justin</a>
 * @see SpringInjectionSupport
 */
public class SpringInjectionInterceptor extends SpringInjectionSupport implements Interceptor
{
   public SpringInjectionInterceptor()
   {
      log.info("Instantiating " + getName());
   }

   public String getName()
   {
      return "SpringInjectionInterceptor";
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      if (!(invocation instanceof ConstructorInvocation))
      {
         throw new IllegalArgumentException("This interceptor is meant to be applied" +
               " only on new instantiation of @Spring annotated objects");
      }
      Object target = invocation.invokeNext();
      inject(target);
      return target;
   }
}
