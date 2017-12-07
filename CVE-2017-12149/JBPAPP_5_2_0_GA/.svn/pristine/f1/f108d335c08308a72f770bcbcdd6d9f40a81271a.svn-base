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
package org.jboss.test.ws.jaxws.samples.webresult;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test the JSR-181 annotation: javax.jws.webresult
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Oct-2005
 */
public class WebResultTestCase extends JBossWSTest
{
   private String targetNS = "http://webresult.samples.jaxws.ws.test.jboss.org/";
   
   private static CustomerService port;

   public static Test suite()
   {
      return new JBossWSTestSetup(WebResultTestCase.class, "jaxws-samples-webresult.war");
   }

   public void setUp() throws Exception
   {
      if (port == null)
      {
         QName serviceName = new QName(targetNS, "CustomerServiceService");
         URL wsdlURL = getResourceURL("META-INF/wsdl/CustomerService.wsdl");

         Service service = Service.create(wsdlURL, serviceName);
         port = service.getPort(CustomerService.class);
      }
   }

   public void testLocateCustomer() throws Exception
   {
      USAddress addr = new USAddress();
      addr.setAddress("Wall Street");

      CustomerRecord retObj = port.locateCustomer("Mickey", "Mouse", addr);
      assertEquals("Mickey", retObj.getFirstName());
      assertEquals("Mouse", retObj.getLastName());
      assertEquals("Wall Street", retObj.getAddress().getAddress());
   }

   public void testMessageAccess() throws Exception
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();

      String reqEnv =
      "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
      " <env:Header/>" +
      " <env:Body>" +
      "  <ns1:locateCustomer xmlns:ns1='" + targetNS + "'>" +
      "   <FirstName>Mickey</FirstName>" +
      "   <LastName>Mouse</LastName>" +
      "   <Address>" +
      "     <address>Wall Street</address>" +
      "   </Address>" +
      "  </ns1:locateCustomer>" +
      " </env:Body>" +
      "</env:Envelope>";
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));

      URL epURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-webresult");

      SOAPMessage resMsg = con.call(reqMsg, epURL);

      QName qname = new QName(targetNS, "locateCustomerResponse");
      SOAPElement soapElement = (SOAPElement)resMsg.getSOAPBody().getChildElements(qname).next();
      soapElement = (SOAPElement)soapElement.getChildElements(new QName("CustomerRecord")).next();
      assertNotNull("Expected CustomerRecord", soapElement);
   }
}
