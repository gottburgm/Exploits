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
package org.jboss.managed.plugins.advice;

import java.util.Arrays;

import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;
import org.jboss.logging.Logger;

/**
 * TraceAdvice.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85526 $
 */
public class TraceAdvice
{
   /** The log */
   private static final Logger log = Logger.getLogger(TraceAdvice.class);
   
   /**
    * Interceptor
    * 
    * @param invocation the invocation
    * @return the result
    * @throws Throwable for any problem
    */
   public Object invoke(Invocation invocation) throws Throwable
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         logMethod(false, invocation, null, null);
      
      Throwable e = null;
      Object result = null;
      try
      {
         result = invocation.invokeNext();
         return result;
      }
      catch (Throwable t)
      {
         e = t;
         throw t;
      }
      finally
      {
         logMethod(true, invocation, result, e);
      }
   }
   
   private void logMethod(boolean beforeAfter, Invocation invocation, Object result, Throwable t)
   {
      MethodInvocation mi = (MethodInvocation) invocation;
      StringBuilder builder = new StringBuilder();
      Object target = mi.getTargetObject();
      builder.append(target.getClass().getSimpleName());
      builder.append('@');
      builder.append(System.identityHashCode(target));
      if (beforeAfter == false)
         builder.append(" before ");
      else
         builder.append(" after  ");
      builder.append(mi.getActualMethod().getName());
      if (beforeAfter == false)
      {
         builder.append(" params=");
         builder.append(Arrays.asList(mi.getArguments()));
      }
      else if (t == null)
      {
         builder.append(" result=");
         builder.append(result);
      }
      if (t != null)
         builder.append(" ended in error:");
      log.trace(builder.toString(), t);
   }
}
