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

import org.jboss.aop.joinpoint.FieldReadInvocation;
import org.jboss.aop.joinpoint.FieldWriteInvocation;

/**
 *
 *  @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 *  @version $Revision: 80997 $
 */
public class StateChangeInterceptor implements org.jboss.aop.advice.Interceptor
{
   protected DistributedPOJOState manager;

   public StateChangeInterceptor(DistributedPOJOState manager)
   {
      this.manager = manager;
   }

   public String getName() { return "StateChangeInterceptor"; }

   /**
    *
    */
   public Object invoke(org.jboss.aop.joinpoint.Invocation invocation) throws Throwable
   {
      if (!(invocation instanceof FieldWriteInvocation) && !(invocation instanceof FieldReadInvocation)) return invocation.invokeNext();

      // Don't need to worry if we're static or not because this is an instance interceptor

      if (invocation instanceof FieldReadInvocation)
      {
         return manager.fieldRead(invocation);
      }
      else
      {
         return manager.fieldWrite(invocation);
      }
   }

}
