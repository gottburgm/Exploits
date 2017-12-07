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
package org.jboss.test.remoting.interceptor;

import java.io.Serializable;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.InvocationBase;

/**
 * Just extending InvocationBase and using implementation from
 * MethodCallByMethodInvocation for the getWrapper() and copy() methods.
 * Just need a simple invocation instance for testing.
 *
 * @author <a href="mailto:telrod@e2technologies.net">Tom Elrod</a>
 */
public class TestInvocation extends InvocationBase implements Serializable
{
   private Object arg;

   public TestInvocation(Interceptor[] interceptorStack)
   {
      super(interceptorStack);
   }

   /**
    * Get a wrapper invocation object that can insert a new chain of interceptors
    * at runtime to the invocation flow.  CFlow makes use of this.
    * When the wrapper object finishes its invocation chain it delegates back to
    * the wrapped invocation.
    *
    * @param newchain
    * @return
    */
   public Invocation getWrapper(Interceptor[] newchain)
   {
      int size = interceptors.length + newchain.length;
      Interceptor[] newInterceptors = new Interceptor[size];
      size = 0;
      for(int i = 0; i < interceptors.length; i++, size++)
      {
         newInterceptors[i] = interceptors[i];
      }
      for(int x = 0; x < newchain.length; x++, size++)
      {
         newInterceptors[x] = newchain[x];
      }
      TestInvocation newInvocation = new TestInvocation(newInterceptors);
      newInvocation.setMetaData(getMetaData());
      newInvocation.setArgument(getArgument());
      return newInvocation;
   }

   /**
    * Copies complete state of Invocation object so that it could possibly
    * be reused in a spawned thread.
    *
    * @return
    */
   public Invocation copy()
   {
      return getWrapper(new Interceptor[0]);
   }

   public void setArgument(Object testTarget)
   {
      this.arg = testTarget;
   }

   public Object getArgument()
   {
      return arg;
   }
}
