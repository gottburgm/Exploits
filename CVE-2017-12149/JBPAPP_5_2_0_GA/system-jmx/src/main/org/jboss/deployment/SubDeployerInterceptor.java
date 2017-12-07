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
package org.jboss.deployment;

import org.jboss.mx.interceptor.AbstractInterceptor;
import org.jboss.mx.interceptor.Interceptor;
import org.jboss.mx.server.Invocation;

/**
 * Base class for SubDeployer interceptors.
 * 
 * Override one or more of the init(), create(), start(), stop(), destroy()
 * methods to add behaviour. Don't forget to call invokeNext() inside
 * your implementation, if you want the call to be continued.
 *
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81033 $   
 */
public abstract class SubDeployerInterceptor extends AbstractInterceptor
{
   // Constructors --------------------------------------------------
   
   /**
    * Default CTOR
    */
   public SubDeployerInterceptor()
   {
      super();
   }
   
   /**
    * CTOR
    * 
    * @param name - the name to use for this interceptor
    */
   public SubDeployerInterceptor(String name)
   {
      // invoker is unknown
      super(name);
   }
   
   // Interceptor implementation ------------------------------------   
   
   /**
    * This invoke method checks for invocations of interest, .i.e.
    * init(), create(), start(), stop(), destroy() operation calls
    * with a single DeploymentInfo argument and wraps the invocation
    * with calls to corresponding init(), create(), etc. methods.
    */
   public Object invoke(Invocation invocation) throws Throwable
   {
      String type = invocation.getType();
      
      // make sure this is an operation invocation
      if (type.equals(Invocation.OP_INVOKE))
      {
         Object args[] = invocation.getArgs();
         Object retn = invocation.getReturnTypeClass();
         
         // make sure the signature matches -> void <methodName>(DeploymentInfo di)
         if ((args.length == 1) && (args[0] instanceof DeploymentInfo) && (retn == null))
         {         
            String method = invocation.getName();
            DeploymentInfo di = (DeploymentInfo)args[0];
            
            if (method.equals("init"))
            {
               return init(invocation, di);
            }
            else if (method.equals("create"))
            {
               return create(invocation, di);
            }
            else if (method.equals("start"))
            {
               return start(invocation, di);
            }
            else if (method.equals("stop"))
            {
               return stop(invocation, di);
            }
            else if (method.equals("destroy"))
            {
               return destroy(invocation, di);
            }
         }
      }
      // if we reached this point invocation is of no interest
      // to SubDeployerInterceptor so simply forward it
      return invokeNext(invocation);
   }
   
   // Protected -----------------------------------------------------

   /**
    * Use this to forward the call
    */
   protected Object invokeNext(Invocation invocation) throws Throwable
   {
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
   
   // Protected overrides -------------------------------------------
   
   // Override the following methods to add behaviour. Remember
   // to call invokeNext() if you want the call to proceed.
   
   protected Object init(Invocation invocation, DeploymentInfo di) throws Throwable
   {
      return invokeNext(invocation);
   }
   
   protected Object create(Invocation invocation, DeploymentInfo di) throws Throwable
   {
      return invokeNext(invocation);
   }
   
   protected Object start(Invocation invocation, DeploymentInfo di) throws Throwable
   {
      return invokeNext(invocation);
   }
   
   protected Object stop(Invocation invocation, DeploymentInfo di) throws Throwable
   {
      return invokeNext(invocation);
   }
   
   protected Object destroy(Invocation invocation, DeploymentInfo di) throws Throwable
   {
      return invokeNext(invocation);
   }

}