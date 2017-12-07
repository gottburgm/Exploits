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
package org.jboss.mx.interceptor;

import org.jboss.mx.server.Invocation;
import org.jboss.mx.server.MBeanInvoker;

/**
 * Interceptor that provides access to the org.jboss.mx.server.Interceptable hooks
 * for dynamically adding and removing interceptors to an MBean.
 *
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>.
 * @version $Revision: 81026 $
 */
public class DynamicInterceptor extends AbstractInterceptor
{
   /** methods implemented from org.jboss.mx.server.Interceptable */
   public static final String ADD_INTERCEPTOR = "addOperationInterceptor";
   public static final String REMOVE_INTERCEPTOR = "removeOperationInterceptor";

   /** the invoker implementing Interceptable */
   MBeanInvoker invoker;
   
   /**
    * CTOR
    */
   public DynamicInterceptor(MBeanInvoker invoker)
   {
      // initialize logger and name
      super();
      setName("DynamicInterceptor");
      
      this.invoker = invoker;
   }

   /**
    * Do the trick
    */
   public Object invoke(Invocation invocation)
      throws Throwable
   {
      String type = invocation.getType();
      
      // implement Interceptable by delegating to MBeanInvoker
      if (type.equals(Invocation.OP_INVOKE))
      {
         String name = invocation.getName();
         
         if (name.equals(ADD_INTERCEPTOR))
         {
            Object args[] = invocation.getArgs();
            Object retn = invocation.getReturnTypeClass();
            
            if ((args.length == 1) && (args[0] instanceof Interceptor) && (retn == null))
            {
               invoker.addOperationInterceptor((Interceptor)args[0]);
               return null;
            }
         }
         else if (name.equals(REMOVE_INTERCEPTOR))
         {
            Object args[] = invocation.getArgs();
            Object retn = invocation.getReturnTypeClass();
            
            if ((args.length == 1) && (args[0] instanceof Interceptor) && (retn == null))
            {
               invoker.removeOperationInterceptor((Interceptor)args[0]);
               return null;
            }            
         }
      }

      // call the next in the interceptor chain,
      // if nobody follows dispatch the call
      Interceptor next = invocation.nextInterceptor();
      if (next != null)
      {
         return next.invoke(invocation);
      }
      else
      {
         return invocation.dispatch();
      }
   }
}
