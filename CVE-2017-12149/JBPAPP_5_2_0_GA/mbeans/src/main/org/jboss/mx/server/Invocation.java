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
package org.jboss.mx.server;

import java.util.Arrays;

import org.jboss.mx.interceptor.AbstractInterceptor;


/**
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81026 $
 *   
 */
public class Invocation
   extends InvocationContext   
{
   private Object[] args;
   
   private InvocationContext ctx;

   // Constructors --------------------------------------------------
   public Invocation() {}
   
   public Invocation(InvocationContext ic)
   {
      addContext(ic);
   }
   
   
   // Public --------------------------------------------------------
   
   public void addContext(final InvocationContext ctx)
   {
      super.copy(ctx);
      this.ctx = ctx;
   }

   public void setArgs(Object[] args)
   {
      this.args = args;
   }

   public Object[] getArgs()
   {
      return args;
   }
   
   int ic_counter = 0;
   
   public AbstractInterceptor nextInterceptor()
   {
      if (interceptors == null)
         return null;
         
      if (ic_counter < interceptors.size())
          return (AbstractInterceptor)interceptors.get(ic_counter++);
      else
         return null;
   }
      
      
   public Object invoke() throws Throwable
   {
      AbstractInterceptor ic = nextInterceptor();
      
      if (ic == null)
         return dispatch();
      else
         return ic.invoke(this);
         
   }
   
   public Object dispatch() throws Throwable
   {
      return dispatcher.invoke(this);
   }
   
   Object retVal = null;

   
   
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append(getName());
      String[] sig = getSignature();
      if (sig != null)
         buffer.append(Arrays.asList(sig));
      buffer.append(' ').append(getType());
      return buffer.toString();
   }
   
   public Class getAttributeTypeClass() throws ClassNotFoundException
   {
      return ctx.getAttributeTypeClass();
   }

   public Class getReturnTypeClass() throws ClassNotFoundException
   {
      return ctx.getReturnTypeClass();
   }

   public Class[] getSignatureClasses() throws ClassNotFoundException
   {
      return ctx.getSignatureClasses();
   }
}
      



