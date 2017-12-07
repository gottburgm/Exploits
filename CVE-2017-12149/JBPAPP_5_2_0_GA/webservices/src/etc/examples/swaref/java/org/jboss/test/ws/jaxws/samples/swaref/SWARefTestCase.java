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
package org.jboss.test.ws.jaxws.samples.swaref;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

import javax.xml.ws.Service;
import javax.xml.namespace.QName;
import javax.activation.DataHandler;
import java.net.URL;

/**
 * Test SwARef with different binding styles and @XmlAttachmentRef locations. 
 *
 * @author Heiko.Braun@jboss.com
 */
public class SWARefTestCase extends JBossWSTest
{
   private String bareEndpointURL = "http://" + getServerHost() + ":8080/jaxws-swaref/BareEndpointImpl";
   private String wrappedEndpointURL = "http://" + getServerHost() + ":8080/jaxws-swaref/WrappedEndpointImpl";
   private String rpclitEndpointURL = "http://" + getServerHost() + ":8080/jaxws-swaref/RpcLitEndpointImpl";

   private QName bareServiceQName = new QName("http://swaref.samples.jaxws.ws.test.jboss.org/", "BareEndpointService");
   private QName wrappedServiceQName = new QName("http://swaref.samples.jaxws.ws.test.jboss.org/", "WrappedEndpointService");
   private QName rpcLitServiceQName = new QName("http://swaref.samples.jaxws.ws.test.jboss.org/", "RpcLitEndpointService");

   private static DataHandler data = new DataHandler("Client data", "text/plain");

   public static Test suite()
   {
      return new JBossWSTestSetup(SWARefTestCase.class, "jaxws-samples-swaref.jar");
   }
   
   public void testBeanAnnotationWithBare() throws Exception
   {
      Service service = Service.create(new URL(bareEndpointURL +"?wsdl"), bareServiceQName);
      BareEndpoint port = service.getPort(BareEndpoint.class);
      DocumentPayload response = port.beanAnnotation(new DocumentPayload(data));
      assertTrue(response.getData().getContent().equals("Server data"));
   }
   
   public void testBeanAnnotationWithWrapped() throws Exception
   {
      Service service = Service.create(new URL(wrappedEndpointURL+"?wsdl"), wrappedServiceQName);
      WrappedEndpoint port = service.getPort(WrappedEndpoint.class);

      DocumentPayload response = port.beanAnnotation(new DocumentPayload(data), "Wrapped test");
      assertTrue(response.getData().getContent().equals("Server data"));

   }

   public void testParameterAnnotationWithWrapped() throws Exception
   {
      Service service = Service.create(new URL(wrappedEndpointURL+"?wsdl"), wrappedServiceQName);
      WrappedEndpoint port = service.getPort(WrappedEndpoint.class);

      DataHandler response = port.parameterAnnotation(new DocumentPayload(data), "Wrapped test", data);
      assertNotNull("Response as null", response);
      assertTrue("Contents are not equal", response.getContent().equals("Server data"));
   }

   public void testBeanAnnotationWithRPC() throws Exception
   {
      Service service = Service.create(new URL(rpclitEndpointURL+"?wsdl"), rpcLitServiceQName);
      RpcLitEndpoint port = service.getPort(RpcLitEndpoint.class);

      DocumentPayload response = port.beanAnnotation( new DocumentPayload(data));
      assertNotNull("Response was null", response);
      assertTrue(response.getData().getContent().equals("Server data"));

   }
}
