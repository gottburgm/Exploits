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
package org.jboss.test.ws.jaxws.samples.webmethod;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.soap.SOAPFaultException;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test the JSR-181 annotation: javax.jws.webmethod
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Oct-2005
 */
public class WebMethodTestCase extends JBossWSTest
{
   private String endpointURL = "http://" + getServerHost() + ":8080/jaxws-samples-webmethod/TestService";
   private String targetNS = "http://webmethod.samples.jaxws.ws.test.jboss.org/";

   public static Test suite()
   {
      return new JBossWSTestSetup(WebMethodTestCase.class, "jaxws-samples-webmethod.war");
   }

   public void testLegalAccess() throws Exception
   {
      URL wsdlURL = new URL(endpointURL + "?wsdl");
      QName serviceName = new QName(targetNS, "EndpointService");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);

      Object retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }

   public void testLegalMessageAccess() throws Exception
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();

      String reqEnv = 
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" + 
         " <env:Header/>" + 
         " <env:Body>" + 
         "  <ns1:echoString xmlns:ns1='" + targetNS + "'>" + 
         "   <arg0>Hello</arg0>" + 
         "  </ns1:echoString>" + 
         " </env:Body>" + 
         "</env:Envelope>";
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));

      URL epURL = new URL(endpointURL);
      SOAPMessage resMsg = con.call(reqMsg, epURL);

      QName qname = new QName(targetNS, "echoStringResponse");
      SOAPElement soapElement = (SOAPElement)resMsg.getSOAPBody().getChildElements(qname).next();
      soapElement = (SOAPElement)soapElement.getChildElements(new QName("return")).next();
      assertEquals("Hello", soapElement.getValue());
   }

   public void testIllegalMessageAccess() throws Exception
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();

      String reqEnv = 
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" + 
         " <env:Header/>" + 
         " <env:Body>" + 
         "  <ns1:noWebMethod xmlns:ns1='" + targetNS + "'>" + 
         "   <String_1>Hello</String_1>" + 
         "  </ns1:noWebMethod>" + 
         " </env:Body>" + 
         "</env:Envelope>";
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));

      URL epURL = new URL(endpointURL);
      SOAPMessage resMsg = con.call(reqMsg, epURL);
      SOAPFault soapFault = resMsg.getSOAPBody().getFault();
      assertNotNull("Expected SOAPFault", soapFault);

      String faultString = soapFault.getFaultString();
      assertTrue(faultString, faultString.indexOf("noWebMethod") > 0);
   }

   public void testIllegalDispatchAccess() throws Exception
   {
      URL wsdlURL = new URL(endpointURL + "?wsdl");
      QName serviceName = new QName(targetNS, "EndpointService");
      QName portName = new QName(targetNS, "EndpointPort");

      String reqPayload = 
         "<ns1:noWebMethod xmlns:ns1='" + targetNS + "'>" + 
         " <String_1>Hello</String_1>" + 
         "</ns1:noWebMethod>";

      String expPayload = 
         "<env:Fault xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" + 
         " <faultcode>env:Client</faultcode>" + 
         " <faultstring>Endpoint {http://webmethod.samples.jaxws.ws.test.jboss.org/}EndpointPort does not contain operation meta data for: {http://webmethod.samples.jaxws.ws.test.jboss.org/}noWebMethod</faultstring>" + 
         "</env:Fault>";

      Service service = Service.create(wsdlURL, serviceName);
      Dispatch dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD);
      try
      {
         dispatch.invoke(new StreamSource(new StringReader(reqPayload)));
         fail("SOAPFaultException expected");
      }
      catch (SOAPFaultException ex)
      {
         // ignore
      }
   }
}
