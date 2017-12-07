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

import org.jboss.logging.Logger;
import org.jboss.mx.server.Invocation;

/**
 * This interface defines MBean interceptors. All MBean interceptors must
 * implement this interface.
 *
 * @see org.jboss.mx.interceptor.AbstractInterceptor
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 81026 $   
 */
public interface Interceptor
{

   /**
    * Returns the name of this interceptor. Notice that for shared interceptors
    * this name must be unique among the shared interceptors in the MBean
    * server.
    */
   String getName();
   
   /**
    * Returns true if this interceptor is shared by multiple invokers;
    * false otherwise. Non-shared interceptors should always return false.
    * Shared interceptors return false if they have not been registered
    * to the MBean server yet. Shared interceptors must always return true
    * after they have been registered to the server.
    *
    * @return  true if shared;false otherwise
    */
   boolean isShared();
      
   /**
    * The <tt>invoke</tt> method is called when the invocation object passes
    * this interceptor.
    */
   Object invoke(Invocation invocation) throws Throwable;

   /**
    * Called by the {@link org.jboss.mx.server.MBeanInvoker MBeanInvoker} on
    * a non-shared interceptors to set a logger reference for this interceptor.
    * The interceptor implementation may use the invoker's logger for recording
    * log information.   <p>
    *
    * Shared interceptors should set up their log facility through other means
    * as they are invoked by several different MBean invokers. To access the
    * log implementation of the originating invoker for a particular invocation,
    * an interceptor may query they invocation context for invoker reference.
    */
   void setLogger(Logger log);

   /**
    * This method is part of the interceptor's lifecycle. It is
    * called by the invoker during the initialization of the interceptor
    * instance.        <p>
    *
    * For shared interceptors the lifecycle is driven by the MBean registration.
    * This method is called before the MBean is registered to the server.  <p>
    *
    * Concrete interceptor implementations can override this method to provide
    * initialization code that should be executed before the interceptor
    * is registered.   <p>
    *
    * Any exception that is propagated from this method to its caller will
    * cancel the interceptor registration.
    *
    * @throws Exception if you want to cancel the interceptor registration
    */
   void init() throws Exception;

   /**
    * This method is part of the interceptor's lifecycle. It is
    * called by the invoker during the interceptor initialization process. <p>
    *
    * For shared interceptors the lifecycle is driven by the MBean registration.
    * This method is called after the MBean is registered to the
    * server as part of the 
    * {@link javax.management.MBeanRegistration#postRegister} execution.   <p>
    *
    * Concrete interceptor implementations can override this method to provide
    * initialization code that should be executed once the MBean server and
    * object name references for this interceptor have been resolved.
    */
   void start();

   /**
    * This method is part of the interceptor lifecycle. It is 
    * called by the invoker during the interceptor removal.   <p>
    *
    * For shared interceptors the lifecycle is driven by the MBean
    * unregistration. This method is called after the MBean is registered to the
    * server as part of the 
    * {@link javax.management.MBeanRegistration#preDeregister} execution.  <p>
    *
    * Concrete interceptor implementations can override this
    * method to provide cleanup code that should be executed before the
    * interceptor is unregistered.   <p>
    *
    * Any exception that is propagated from this method to its caller will
    * cancel the interceptor unregistration.
    *
    * @throws Exception if you want to cancel the interceptor unregistration
    */
   void stop() throws Exception;

   /**
    * This method is part of the interceptor lifecycle. It is called by the
    * invoker durin the interceptor removal.   <p>
    *
    * For shared interceptors the lifecycle is driven by the MBean
    * unregistration. This method is called after the MBean is registered to the
    * server as part of the 
    * {@link javax.management.MBeanRegistration#postDeregister} execution.  <p>
    *
    * Concrete interceptor implementations can override this method to provide
    * cleanup code that should be executed once the interceptor is no longer
    * registered to the MBean server.
    */
   void destroy();
   
}


