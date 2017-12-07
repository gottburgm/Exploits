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

import java.net.MalformedURLException;
import org.jboss.aop.advice.Interceptor;
import org.jboss.aop.util.PayloadKey;
import org.jboss.aspects.remoting.interceptors.invoker.InvokerInterceptor;
import org.jboss.aspects.remoting.interceptors.invoker.RemotingInterceptorFactory;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.marshal.serializable.SerializableMarshaller;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class ClientInterceptorTest extends TestCase
{
   private boolean passed = false;

   public void runTest() throws MalformedURLException
   {
      // Create interceptor stack
      Interceptor[] interceptorStack = new Interceptor[]{new InvokerInterceptor()};

      TestInvocation invocation = new TestInvocation(interceptorStack);
      invocation.setArgument(new TestTarget());

      int port = 8081;
      String transport = "socket";

      InvokerLocator locator = new InvokerLocator(transport + "://localhost:" + port + "/?" +
                                                  InvokerLocator.DATATYPE + "=" + SerializableMarshaller.DATATYPE);
      invocation.getMetaData().addMetaData(RemotingInterceptorFactory.REMOTING,
                                           RemotingInterceptorFactory.INVOKER_LOCATOR, locator, PayloadKey.TRANSIENT);

      try
      {
         Object ret = invocation.invokeNext();
         if(ret instanceof Boolean && ((Boolean) ret).booleanValue())
         {
            assertTrue("Simple interceptor test passed.  Value returned was not true.", true);
            System.out.println("Test PASSED.");
            passed = true;
         }
         else
         {
            assertTrue("Simple interceptor test passed.  Value returned was not true.", false);
            System.out.println("Test FAILED!!!");
            passed = false;
         }
      }
      catch(Throwable throwable)
      {
         throwable.printStackTrace();
      }
   }

   public boolean isTestPassing()
   {
      return passed;
   }

   public static void main(String[] args)
   {
      try
      {
         new ClientInterceptorTest().runTest();
      }
      catch(MalformedURLException e)
      {
         e.printStackTrace();
      }
   }

}