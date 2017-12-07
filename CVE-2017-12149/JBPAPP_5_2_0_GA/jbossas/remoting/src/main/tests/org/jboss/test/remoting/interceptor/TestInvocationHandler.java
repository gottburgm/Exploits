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

import javax.management.MBeanServer;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.callback.InvokerCallbackHandler;
import org.jboss.remoting.ServerInvocationHandler;
import org.jboss.remoting.ServerInvoker;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class TestInvocationHandler implements ServerInvocationHandler
{
   private ServerInvoker invoker;

   private static final Logger log = Logger.getLogger(TestInvocationHandler.class);


   /**
    * set the invoker that owns this handler
    *
    * @param invoker
    */
   public void setInvoker(ServerInvoker invoker)
   {
      this.invoker = invoker;
   }

   /**
    * set the mbean server that the handler can reference
    *
    * @param server
    */
   public void setMBeanServer(MBeanServer server)
   {
   }

   public Object invoke(InvocationRequest invocation)
         throws Throwable
   {
      Object ret = null;
      Object param = invocation.getParameter();
      if(param instanceof org.jboss.test.remoting.interceptor.TestInvocation)
      {
         Object obj = ((org.jboss.test.remoting.interceptor.TestInvocation) param).getArgument();
         if(obj instanceof TestTarget)
         {
            TestTarget testParamVal = (TestTarget) obj;
            ret = new Boolean(new TestTarget().equals(testParamVal));
         }
      }
      else
      {
         log.info("Don't recognize the parameter type, so just returning it.");
         ret = param;
      }
      return ret;
   }

   /**
    * Adds a callback handler that will listen for callbacks from
    * the server invoker handler.
    *
    * @param callbackHandler
    */
   public void addListener(InvokerCallbackHandler callbackHandler)
   {
   }

   /**
    * Removes the callback handler that was listening for callbacks
    * from the server invoker handler.
    *
    * @param callbackHandler
    */
   public void removeListener(InvokerCallbackHandler callbackHandler)
   {
   }
}
