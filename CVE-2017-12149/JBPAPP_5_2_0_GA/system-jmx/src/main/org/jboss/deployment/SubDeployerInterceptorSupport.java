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

import org.jboss.logging.Logger;
import org.jboss.mx.server.Invocation;
import org.jboss.system.InterceptorServiceMBeanSupport;

/**
 * Base class that can be used for writing services
 * that dynamically hook to other interceptable deployers
 * in order to add functionality in the deployment cycle.
 *
 * We override attach() to install a different interceptor
 * from that of the base class that understands SubDeployer
 * calls. Note that the baseclass invoke(Invocation) won't be
 * called, so no need to override it.
 * 
 * Simply call attach()/detach() from createService()/destroyService()
 * or startService()/stopService() pair methods to attach/detach the
 * interceptor to the configured Interceptable SubDeployer(s).
 * Then override any of the init/create/start/stop/destroy methods to
 * apply the extra interception functionality. Inside those methods
 * don't forget to forward the call using invokeNext().
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81033 $
 */
public abstract class SubDeployerInterceptorSupport extends InterceptorServiceMBeanSupport
   implements SubDeployerInterceptorMBean
{

   // Constructors -------------------------------------------------
    
   /**
    * Constructs an <tt>SubDeployerInterceptorSupport</tt>.
    */
   public SubDeployerInterceptorSupport()
   {
        super();
   }

   /**
    * Constructs an <tt>SubDeployerInterceptorSupport</tt>.
    *
    * Pass-through to InterceptorServiceMBeanSupport.
    *
    * @param type   The class type to determine Logger name from.
    */
   public SubDeployerInterceptorSupport(final Class type)
   {
      super(type);
   }
   
   /**
    * Constructs an <tt>SubDeployerInterceptorSupport</tt>.
    *
    * Pass-through to InterceptorServiceMBeanSupport.
    *
    * @param category   The logger category name.
    */
   public SubDeployerInterceptorSupport(final String category)
   {
      super(category);
   }

   /**
    * Constructs an <tt>SubDeployerInterceptorSupport</tt>.
    *
    * Pass-through to InterceptorServiceMBeanSupport.
    *
    * @param log   The logger to use.
    */
   public SubDeployerInterceptorSupport(final Logger log)
   {
      super(log);
   }    
    
   // Protected API -------------------------------------------------
   
   /**
    * We override attach() from InterceptorServiceMBeanSupport
    * to attach a different interceptor that knows how to switch
    * init/create/start/stop/destroy SubDeployer calls.
    * 
    * @throws Exception thrown on any interceptor registration error
    */
   protected void attach() throws Exception
   {
      super.attach(new XMBeanInterceptor());
   }
   
   // Override ------------------------------------------------------
   
   /**
    * Override
    */   
   protected Object init(Invocation invocation, DeploymentInfo di) throws Throwable
   {
      return invokeNext(invocation);
   }

   /**
    * Override
    */
   protected Object create(Invocation invocation, DeploymentInfo di) throws Throwable
   {
      return invokeNext(invocation);
   }

   /**
    * Override
    */
   protected Object start(Invocation invocation, DeploymentInfo di) throws Throwable
   {
      return invokeNext(invocation);
   }

   /**
    * Override
    */
   protected Object stop(Invocation invocation, DeploymentInfo di) throws Throwable
   {
      return invokeNext(invocation);
   }

   /**
    * Override
    */
   protected Object destroy(Invocation invocation, DeploymentInfo di) throws Throwable
   {
      return invokeNext(invocation);
   }

   // Private Inner Class -------------------------------------------
   
   /**
    * Simple SubDeployerInterceptor delegating to
    * the SubDeployerInterceptorSupport callbacks
    */
   private class XMBeanInterceptor extends SubDeployerInterceptor
   {
      public XMBeanInterceptor()
      {
         super("XMBeanInterceptor('" + SubDeployerInterceptorSupport.this.getServiceName() + "')");
      }
      
      protected Object init(Invocation invocation, DeploymentInfo di) throws Throwable
      {
         logSubDeployerInvocation(invocation, di);
         
         // delegate
         return SubDeployerInterceptorSupport.this.init(invocation, di);
      }
      
      protected Object create(Invocation invocation, DeploymentInfo di) throws Throwable
      {
         logSubDeployerInvocation(invocation, di);
         
         // delegate
         return SubDeployerInterceptorSupport.this.create(invocation, di);
      }
      
      protected Object start(Invocation invocation, DeploymentInfo di) throws Throwable
      {
         logSubDeployerInvocation(invocation, di);

         // delegate
         return SubDeployerInterceptorSupport.this.start(invocation, di);
      }
      
      protected Object stop(Invocation invocation, DeploymentInfo di) throws Throwable
      {
         logSubDeployerInvocation(invocation, di);

         // delegate
         return SubDeployerInterceptorSupport.this.stop(invocation, di);
      }
      
      protected Object destroy(Invocation invocation, DeploymentInfo di) throws Throwable
      {
         logSubDeployerInvocation(invocation, di);

         // delegate
         return SubDeployerInterceptorSupport.this.destroy(invocation, di);
      }
      
      protected void logSubDeployerInvocation(Invocation invocation, DeploymentInfo di)
      {
         if (SubDeployerInterceptorSupport.this.log.isTraceEnabled())
         {
            StringBuffer sbuf = new StringBuffer();
            sbuf.append("intercepting ").append(invocation.getName())
               .append("(), url=").append(di.url.toString())
               .append(", state=").append(di.state.toString());
            
            SubDeployerInterceptorSupport.this.log.trace(sbuf.toString());
         }
      }
   }
}    
