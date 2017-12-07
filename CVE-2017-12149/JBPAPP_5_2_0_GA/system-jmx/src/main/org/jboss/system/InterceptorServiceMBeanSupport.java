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
package org.jboss.system;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.mx.interceptor.AbstractInterceptor;
import org.jboss.mx.interceptor.DynamicInterceptor;
import org.jboss.mx.interceptor.Interceptor;
import org.jboss.mx.server.Invocation;

/**
 * Helper class that can be used for writing MBean Services
 * that dynamically hook-up an Interceptor to other (X)MBeans
 * that have been configured as Interceptable.
 * 
 * In a nutshell, call attach()/detach() from your
 * createService()/destroyService() or startService()/stopService()
 * pair methods to attach/detach an interceptor to the target mbean(s),
 * then override invoke() to do something with the invocations.
 * 
 * You may also provide your own Interceptor, in which case
 * you should call attach(Interceptor).
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81033 $
 */
public abstract class InterceptorServiceMBeanSupport extends ServiceMBeanSupport
   implements InterceptorServiceMBean
{
   // Private Data --------------------------------------------------
   
   /** The Interceptables to attach to */
   private List interceptables;
   
   /** The attached interceptor */
   private Interceptor interceptor;
   
   // Constructors -------------------------------------------------
    
   /**
    * Constructs an <tt>InterceptorServiceMBeanSupport</tt>.
    */
   public InterceptorServiceMBeanSupport()
   {
        super();
   }

   /**
    * Constructs an <tt>InterceptorServiceMBeanSupport</tt>.
    *
    * Pass-through to ServiceMBeanSupport.
    *
    * @param type   The class type to determine Logger name from.
    */
   public InterceptorServiceMBeanSupport(final Class type)
   {
      super(type);
   }
   
   /**
    * Constructs an <tt>InterceptorServiceMBeanSupport</tt>.
    *
    * Pass-through to ServiceMBeanSupport.
    *
    * @param category   The logger category name.
    */
   public InterceptorServiceMBeanSupport(final String category)
   {
      super(category);
   }

   /**
    * Constructs an <tt>InterceptorServiceMBeanSupport</tt>.
    *
    * Pass-through to ServiceMBeanSupport.
    *
    * @param log   The logger to use.
    */
   public InterceptorServiceMBeanSupport(final Logger log)
   {
      super(log);
   }    
    
   // InterceptorServiceMBean ---------------------------------------
   
   public void setInterceptables(List interceptables)
   {
      // copy
      if (interceptables != null)
      {
         this.interceptables = new ArrayList(interceptables);
      }
   }
   
   public List getInterceptables()
   {
      // return a copy
      if (interceptables != null)
      {
         return new ArrayList(interceptables);
      }
      return null;
   }
   
   // Protected API -------------------------------------------------
   
   /**
    * Add our interceptor to the target Interceptables.
    * 
    * Override invoke(Invocation) to handle the calls.
    * 
    * @throws Exception thrown on any interceptor registration error
    */
   protected void attach() throws Exception
   {
      if (interceptor == null)
      {
         attach(new XMBeanInterceptor());
      }
   }

   /**
    * Add the provided interceptor to the target Interceptables.
    * 
    * @param interceptor the interceptor to add
    * @throws Exception thrown on any interceptor registration error
    */
   protected void attach(Interceptor interceptor) throws Exception
   {
      if (interceptor == null)
      {
         throw new IllegalArgumentException("Null interceptor");
      }
      
      // check we haven't attached already
      if (this.interceptor != null)
      {
         throw new IllegalStateException("Interceptor already attached");
      }
      
      log.debug("Attaching interceptor: " + interceptor.getName());
      
      // remember the interceptor
      this.interceptor = interceptor;
      
      // add the interceptor to the Interceptables; an exception
      // will be thrown if any of them is not Interceptable,
      // in which case detach() should be called.
      if (interceptables != null)
      {
         Object[] params = new Object[] { interceptor };
         String[] signature = new String[] { Interceptor.class.getName() };
         
         for (Iterator i = interceptables.iterator(); i.hasNext(); )
         {
            ObjectName target = (ObjectName)i.next();
            super.server.invoke(target,
               DynamicInterceptor.ADD_INTERCEPTOR,
               params,
               signature);
            
            log.debug("Interceptor attached to: '" + target + "'");
         }
      }
   }
   
   /**
    * Remove the interceptor from the target Interceptables
    */
   protected void detach()
   {
      if (interceptor != null)
      {
         log.debug("Detaching interceptor: " + interceptor.getName());         
         if (interceptables != null)
         {
            Object[] params = new Object[] { interceptor };
            String[] signature = new String[] { Interceptor.class.getName() };
            
            for (Iterator i = interceptables.iterator(); i.hasNext(); )            
            {
               ObjectName target = (ObjectName)i.next();               
               try
               {
                  super.server.invoke(target,
                     DynamicInterceptor.REMOVE_INTERCEPTOR,
                     params,
                     signature);
                  
                  log.debug("Interceptor detached from: '" + target + "'");                  
               }
               catch (Exception e)
               {
                  log.debug("Caught exception while removing interceptor from '" +
                        target + "'", e);
               }
            }
         }
         interceptor = null;
      }
   }
   
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
   
   // Override ------------------------------------------------------
   
   /**
    * Override
    */
   protected Object invoke(Invocation invocation) throws Throwable
   {
      return invokeNext(invocation);
   }
   
   // Private Inner Class -------------------------------------------
   
   /**
    * Simple Interceptor delegating to
    * the invoke(Invocation) callback
    */   
   private class XMBeanInterceptor extends AbstractInterceptor
   {
      public XMBeanInterceptor()
      {
         super("XMBeanInterceptor('" + InterceptorServiceMBeanSupport.this.getServiceName() + "')");
      }
      
      public Object invoke(Invocation invocation) throws Throwable
      {
         // delegate
         return InterceptorServiceMBeanSupport.this.invoke(invocation);
      }
   }
}    
