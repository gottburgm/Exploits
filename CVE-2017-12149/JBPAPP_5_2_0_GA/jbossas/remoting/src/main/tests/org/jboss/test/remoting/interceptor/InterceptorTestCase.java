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
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.marshal.serializable.SerializableUnMarshaller;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:telrod@e2technologies.net">Tom Elrod</a>
 */
public class InterceptorTestCase extends TestCase
{
   private boolean continueRun = true;

   public InterceptorTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      continueRun = true;
      new Thread()
      {
         public void run()
         {
            startServer();
         }
      }.start();
   }

   public void testClient() throws Exception
   {
      try
      {
         org.jboss.test.remoting.interceptor.ClientInterceptorTest test = new org.jboss.test.remoting.interceptor.ClientInterceptorTest();
         test.runTest();
         boolean passed = test.isTestPassing();
         assertTrue("ClientInterceptorTest failed.", passed);
      }
      catch(MalformedURLException e)
      {
         e.printStackTrace();
      }

   }

   protected void tearDown() throws Exception
   {
      super.tearDown();
      continueRun = false;

   }


   public void startServer()
   {
      try
      {
         int port = 8081;
         String transport = "socket";
//         InvokerLocator locator = new InvokerLocator(transport + "://localhost:" + port + "/?" +
//               InvokerLocator.DATATYPE + "=" + SerializableUnMarshaller.DATATYPE);
         InvokerLocator locator = new InvokerLocator(transport + "://localhost:" + port + "/?" +
                                                     InvokerLocator.DATATYPE + "=" + SerializableUnMarshaller.DATATYPE);

         ServerInterceptorTest server = new ServerInterceptorTest();
         server.setLocator(locator);
         server.setup();

         while(continueRun)
         {
            Thread.sleep(1000);
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

}