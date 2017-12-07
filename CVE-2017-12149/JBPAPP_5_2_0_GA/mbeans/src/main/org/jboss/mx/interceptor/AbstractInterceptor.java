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

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.mx.server.Invocation;

/**
 * Base class for all interceptors. This class provides some default method
 * implementations for interceptors.
 *
 * @see org.jboss.mx.interceptor.Interceptor
 * @see org.jboss.mx.server.MBeanInvoker
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81026 $   
 */
public abstract class AbstractInterceptor 
   implements Interceptor
{

   // Attributes ----------------------------------------------------

   /**
    * Name for this interceptor.
    */
   protected String name            = "<no name>";
   
   /**
    * Indicates whether this interceptor instance is shared or not.
    */
   protected boolean isShared       = false;
   
   /**
    * Logger reference for interceptor implementations. This reference is
    * set by the invoker for non-shared interceptors after construction.
    * Shared interceptors will should create their own logger instance.
    */
   protected Logger log;
   
   
   // Constructors --------------------------------------------------
   
   /**
    * Constructs a new intereceptor instance. This interceptor is not shared
    * in the MBean server.
    */
   public AbstractInterceptor()
   {
      log = Logger.getLogger(getClass());
   }
   
   /**
    * Constructs a new interceptor instance with a given name. This interceptor
    * is not shared in the MBean server.
    *
    * @param name name of this interceptor
    *
    * @throws IllegalArgumentException if name contains <tt>null</tt> reference
    */
   public AbstractInterceptor(String name)
   {
      if (name == null || name.equals(""))
         throw new IllegalArgumentException("null name");
         
      this.name = name;

      log = Logger.getLogger(getClass());
   }
   

   // Public --------------------------------------------------------

   /**
    * Sets a name for this interceptor.
    *
    * @param   name
    */
   public void setName(String name)
   {
      this.name = name;
   }
   
   
   // Interceptor implementation ------------------------------------   
   
   /**
    * The default invoke implementation queries the invocation object for the
    * next interceptor in the chain. If one exists, it is invoked. Otherwise
    * the invocation is dispatched to the target object.    <p>
    *
    * Concrete implementations should override this method to implement
    * their specific application logic.
    *
    * @see     org.jboss.mx.server.Invocation
    * @see     org.jboss.mx.server.MBeanInvoker
    *
    * @param   invocation  the invocation object send towards the target
    *                      resource by the invoker
    *
    * @return  return value from the target resource
    *
    * @throws InvocationException This exception wraps any exceptions thrown
    *         by either the target method of the resource object, or invocation
    *         interceptors in this interceptor chain. The target exception is
    *         unwrapped at the {@link org.jboss.mx.server.MBeanInvoker} instance.
    */
   public Object invoke(Invocation invocation) throws Throwable
   {
      Interceptor ic = invocation.nextInterceptor();
      
      // if the invocation object does not provide us with more interceptors,
      // invoke the dispatcher that lands the invocation to its final target
      // in the resource object
      if (ic == null)
         return invocation.dispatch();
      
      // see if the next interceptor in the chain is shared
      if (ic.isShared())
      {
         // we require a common interface for all shared interceptors
         SharedInterceptor shared = (SharedInterceptor)ic;
         
         // we invoke shared interceptor it via the MBean server bus, get the
         // interceptors view to its MBean server
         MBeanServer server = shared.getMBeanServer();
         
         // And the object name the interceptor is registered under
         ObjectName name = shared.getObjectName();
         
         return server.invoke(
                  name, "invoke",                              
                  new Object[] { invocation },                    // args
                  new String[] { Invocation.class.getName() }     // signature
         );
      }
      
      // invoke non-shared interceptor directly via Java reference
      else
      {
         return ic.invoke(invocation);
      }
   }

   public String getName()
   {
      return name;
   }
   
   public boolean isShared()
   {
      return isShared;
   }
      
   public void setLogger(Logger log)
   {
      this.log = log;
   }
   
   public void init() throws Exception {}

   public void start() {}

   public void stop() throws Exception {}

   public void destroy() {}

   
   // Object overrides ----------------------------------------------

   /**
    * Returns a string representation of this interceptor instance.
    *
    * @return  string representation
    */
   public String toString()
   {
      String className = getClass().getName();
      int index        = className.lastIndexOf('.');
      
      return className.substring((index < 0) ? 0 : index) + "[name=" + name + "]";
   }
}


