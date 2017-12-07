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
package org.jboss.test.ws.jaxws.samples.xop.doclit;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTestSetup;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;
import javax.activation.DataHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;

/**
 * Test service endpoint capability to process inlined and optimized
 * requests transparently. Both client and service endpoint do have handlers in place.
 * This means that an additional transition to a conceptually inlined message (handler view)
 * will happen as well.
 *
 * <ul>
 * <li>Client and service endpoint have MTOM enabled (roundtrip)
 * <li>Client send inlined requests (MTOM disabled), service answers with an optimized response.
 * </ul>
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @since 05.12.2006
 */
public class XOPHandlerTestCase extends XOPBase
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-samples-xop-doclit/bare";

   public static Test suite()
   {
      return new JBossWSTestSetup(XOPHandlerTestCase.class, "jaxws-samples-xop-doclit.war");
   }

   protected void setUp() throws Exception
   {

      QName serviceName = new QName("http://doclit.xop.samples.jaxws.ws.test.jboss.org/", "MTOMService");
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      port = service.getPort(MTOMEndpoint.class);
      binding = (SOAPBinding)((BindingProvider)port).getBinding();      

      List<Handler> handlerChain = new ArrayList<Handler>();
      handlerChain.addAll(binding.getHandlerChain());
      handlerChain.add(new MTOMProtocolHandler());
      binding.setHandlerChain(handlerChain);
   }

    /**
    * Consumption of inlined data should will always result on 'application/octet-stream'
    * @throws Exception
    */
   public void testDataHandlerRoundtrip() throws Exception
   {
      getBinding().setMTOMEnabled(true);

      DataHandler dh = new DataHandler("Client Data", "text/plain");
      DHResponse response = getPort().echoDataHandler(new DHRequest(dh));
      assertNotNull(response);
      assertEquals("application/octet-stream", response.getDataHandler().getContentType());
      assertTrue("Wrong java type returned", response.getDataHandler().getContent() instanceof InputStream);
   }

   /**
    * Consumption of inlined data should will always result on 'application/octet-stream'
    * @throws Exception
    */
   public void testDataHandlerResponseOptimzed() throws Exception
   {
      getBinding().setMTOMEnabled(false);

      DataHandler dh = new DataHandler("Client data", "text/plain");
      DHResponse response = getPort().echoDataHandler(new DHRequest(dh));
      assertNotNull(response);
      assertEquals("application/octet-stream", response.getDataHandler().getContentType());
      assertTrue("Wrong java type returned", response.getDataHandler().getContent() instanceof InputStream);
   }
}
