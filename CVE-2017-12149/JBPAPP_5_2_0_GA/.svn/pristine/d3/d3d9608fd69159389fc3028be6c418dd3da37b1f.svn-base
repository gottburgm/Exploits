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

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;

import junit.framework.Test;

import org.jboss.wsf.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.w3c.dom.Element;

/**
 * Test JAXWS asynchrous dispatch
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-Aug-2006
 */
public class AsynchronousDispatchTestCase extends JBossWSTest
{
   private String targetNS = "http://org.jboss.ws/jaxws/asynchronous";
   private String reqPayload = "<ns2:echo xmlns:ns2='" + targetNS + "'><String_1>Hello</String_1></ns2:echo>";
   private Exception handlerException;
   private boolean asyncHandlerCalled;

   public static Test suite()
   {
      return new JBossWSTestSetup(AsynchronousDispatchTestCase.class, "jaxws-samples-asynchronous.war");
   }

   public void testInvokeAsynch() throws Exception
   {
      Source reqObj = new StreamSource(new StringReader(reqPayload));
      Response response = createDispatch().invokeAsync(reqObj);
      verifyResponse((Source)response.get(3000, TimeUnit.MILLISECONDS));
   }

   public void testInvokeAsynchHandler() throws Exception
   {
      AsyncHandler handler = new AsyncHandler()
      {
         public void handleResponse(Response response)
         {
            try
            {
               verifyResponse((Source)response.get());
               asyncHandlerCalled = true;
            }
            catch (Exception ex)
            {
               handlerException = ex;
            }
         }
      };
      StreamSource reqObj = new StreamSource(new StringReader(reqPayload));
      Future future = createDispatch().invokeAsync(reqObj, handler);
      future.get(1000, TimeUnit.MILLISECONDS);

      if (handlerException != null)
         throw handlerException;

      assertTrue("Async handler called", asyncHandlerCalled);
   }

   private Dispatch createDispatch() throws MalformedURLException
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-asynchronous?wsdl");
      QName serviceName = new QName(targetNS, "EndpointBeanService");
      QName portName = new QName(targetNS, "EndpointPort");
      Service service = Service.create(wsdlURL, serviceName);
      Dispatch dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD);
      return dispatch;
   }

   private void verifyResponse(Source result) throws IOException
   {
      Element docElement = DOMUtils.sourceToElement(result);
      Element retElement = DOMUtils.getFirstChildElement(docElement);
      assertEquals("result", retElement.getNodeName());
      assertEquals("Hello", retElement.getTextContent());
   }
}
