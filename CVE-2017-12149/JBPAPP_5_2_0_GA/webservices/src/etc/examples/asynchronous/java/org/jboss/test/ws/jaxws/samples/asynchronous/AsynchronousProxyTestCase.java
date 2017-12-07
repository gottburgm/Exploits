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
package org.jboss.test.ws.jaxws.samples.asynchronous;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test JAXWS asynchrous proxy
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-Aug-2006
 */
public class AsynchronousProxyTestCase extends JBossWSTest
{
   private String targetNS = "http://org.jboss.ws/jaxws/asynchronous";
   private Exception handlerException;
   private boolean asyncHandlerCalled;

   public static Test suite()
   {
      return new JBossWSTestSetup(AsynchronousProxyTestCase.class, "jaxws-samples-asynchronous.war");
   }

   public void testInvokeSync() throws Exception
   {
      Endpoint port = createProxy();
      String retStr = port.echo("Hello");
      assertEquals("Hello", retStr);
   }

   public void testInvokeAsync() throws Exception
   {
      Endpoint port = createProxy();
      Response response = port.echoAsync("Async");

      // access future
      String retStr = (String) response.get();
      assertEquals("Async", retStr);
   }

   public void testInvokeAsyncHandler() throws Exception
   {
      AsyncHandler<String> handler = new AsyncHandler<String>()
      {
         public void handleResponse(Response response)
         {
            try
            {
               System.out.println("AsyncHandler.handleResponse() method called");
               String retStr = (String) response.get(5000, TimeUnit.MILLISECONDS);
               assertEquals("Hello", retStr);
               asyncHandlerCalled = true;
            }
            catch (Exception ex)
            {
               handlerException = ex;
            }
         }
      };

      Endpoint port = createProxy();
      Future future = port.echoAsync("Hello", handler);
      long start = System.currentTimeMillis();
      future.get(5000, TimeUnit.MILLISECONDS);
      long end = System.currentTimeMillis();
      System.out.println("Time spent in future.get() was " + (end - start) + "milliseconds");

      if (handlerException != null)
         throw handlerException;

      assertTrue("Async handler called", asyncHandlerCalled);
   }

   private Endpoint createProxy() throws MalformedURLException
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-asynchronous?wsdl");
      QName serviceName = new QName(targetNS, "EndpointBeanService");
      Service service = Service.create(wsdlURL, serviceName);
      return (Endpoint)service.getPort(Endpoint.class);
   }
}
