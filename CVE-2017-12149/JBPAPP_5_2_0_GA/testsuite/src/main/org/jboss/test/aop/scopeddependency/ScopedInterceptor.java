/*
* JBoss, Home of Professional Open Source.
* Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.aop.scopeddependency;

import org.jboss.aop.AspectManager;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.joinpoint.Invocation;
import org.jboss.aop.joinpoint.MethodInvocation;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 85945 $
 */
public class ScopedInterceptor implements Interceptor
{
   private GlobalDependency globalDependency;
   
   private ScopedDependency scopedDependency;
   
   private static GlobalDependency global;
   
   private static ScopedDependency scoped;
   
   private static String lastMethod; 

   public GlobalDependency getGlobalDependency()
   {
      return globalDependency;
   }

   public void setGlobalDependency(GlobalDependency globalDependency)
   {
      this.globalDependency = globalDependency;
   }

   public ScopedDependency getScopedDependency()
   {
      return scopedDependency;
   }

   public void setScopedDependency(ScopedDependency scopedDependency)
   {
      this.scopedDependency = scopedDependency;
   }

   public static GlobalDependency getGlobal()
   {
      return global;
   }

   public static ScopedDependency getScoped()
   {
      return scoped;
   }

   public static String getLastMethod()
   {
      return lastMethod;
   }
   
   public static void reset()
   {
      global = null;
      scoped = null;
   }
   
   public String getName()
   {
      return this.getClass().getName();
   }

   public Object invoke(Invocation invocation) throws Throwable
   {
      global = globalDependency;
      scoped = scopedDependency;
      if (invocation instanceof MethodInvocation)
      {
         String methodName = ((MethodInvocation)invocation).getMethod().getName();
         if (!methodName.equals("checkIntercepted"))
         {
            lastMethod = methodName;
         }
      }
      if (invocation.getAdvisor().getManager().getClass() == AspectManager.class)
      {
         throw new Exception("Not scoped domain");
      }
      Object target = invocation.getTargetObject();
      if (target instanceof ScopedTester)
      {
         ((ScopedTester)target).invoked = "true";
      }

      return invocation.invokeNext();
   }
}
