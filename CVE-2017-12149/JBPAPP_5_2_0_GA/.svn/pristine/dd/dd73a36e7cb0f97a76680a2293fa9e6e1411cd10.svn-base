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
package org.jboss.test.ws.jaxws.samples.soapbinding;

import java.io.ByteArrayInputStream;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test the JSR-181 annotation: javax.jws.SOAPBinding
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @since 17-Oct-2005
 */
public class SOAPBindingTestCase extends JBossWSTest
{
   private String targetNS = "http://soapbinding.samples.jaxws.ws.test.jboss.org/";

   public static Test suite()
   {
      return new JBossWSTestSetup(SOAPBindingTestCase.class, "jaxws-samples-soapbinding.war");
   }

   public void testExampleService() throws Exception
   {
      QName serviceName = new QName(targetNS, "ExampleService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-soapbinding/ExampleService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      ExampleSEI port = (ExampleSEI)service.getPort(ExampleSEI.class);

      Object retObj = port.concat("first", "second", "third");
      assertEquals("first|second|third", retObj);
   }

   public void testDocBareService() throws Exception
   {
      QName serviceName = new QName(targetNS, "DocBareService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-soapbinding/DocBareService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      DocBare port = (DocBare)service.getPort(DocBare.class);

      SubmitBareRequest poReq = new SubmitBareRequest("Ferrari");
      SubmitBareResponse poRes = port.submitPO(poReq);
      assertEquals("Ferrari", poRes.getProduct());
   }

   public void testDocBareDispatchService() throws Exception
   {
      QName serviceName = new QName(targetNS, "DocBareService");
      QName portName = new QName(targetNS, "DocBarePort");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-soapbinding/DocBareService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      JAXBContext jbc = JAXBContext.newInstance(new Class[] { SubmitBareRequest.class, SubmitBareResponse.class });
      Dispatch dispatch = service.createDispatch(portName, jbc, Mode.PAYLOAD);

      SubmitBareRequest poReq = new SubmitBareRequest("Ferrari");
      SubmitBareResponse poRes = (SubmitBareResponse)dispatch.invoke(poReq);
      assertEquals("Ferrari", poRes.getProduct());
   }

   public void testDocBareServiceMessageAccess() throws Exception
   {
      QName serviceName = new QName(targetNS, "DocBareService");
      QName portName = new QName(targetNS, "DocBarePort");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-soapbinding/DocBareService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      Dispatch dispatch = service.createDispatch(portName, SOAPMessage.class, Mode.MESSAGE);

      String reqEnv =
      "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
      " <env:Header/>" +
      " <env:Body>" +
      "  <ns1:SubmitPO xmlns:ns1='" + targetNS + "'>" +
      "   <ns1:product>Ferrari</ns1:product>" +
      "  </ns1:SubmitPO>" +
      " </env:Body>" +
      "</env:Envelope>";

      SOAPMessage reqMsg = MessageFactory.newInstance().createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));
      SOAPMessage resMsg = (SOAPMessage)dispatch.invoke(reqMsg);

      QName qname = new QName(targetNS, "SubmitPOResponse");
      SOAPElement soapElement = (SOAPElement)resMsg.getSOAPBody().getChildElements(qname).next();
      soapElement = (SOAPElement)soapElement.getChildElements(new QName(targetNS, "product")).next();
      assertEquals("Ferrari", soapElement.getValue());
   }

   public void testNamespacedDocBareServiceMessageAccess() throws Exception
   {
      QName serviceName = new QName(targetNS, "DocBareService");
      QName portName = new QName(targetNS, "DocBarePort");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-soapbinding/DocBareService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      Dispatch dispatch = service.createDispatch(portName, SOAPMessage.class, Mode.MESSAGE);

      String requestNamespace = "http://namespace/request";
      String resultNamespace = "http://namespace/result";

      String reqEnv =
      "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
      " <env:Header/>" +
      " <env:Body>" +
      "  <ns1:SubmitNamespacedPO xmlns:ns1='" + requestNamespace+ "'>" +
      "   <ns2:product xmlns:ns2='" + targetNS + "'>Ferrari</ns2:product>" +
      "  </ns1:SubmitNamespacedPO>" +
      " </env:Body>" +
      "</env:Envelope>";

      SOAPMessage reqMsg = MessageFactory.newInstance().createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));
      SOAPMessage resMsg = (SOAPMessage)dispatch.invoke(reqMsg);

      QName qname = new QName(resultNamespace, "SubmitBareResponse");
      SOAPElement soapElement = (SOAPElement)resMsg.getSOAPBody().getChildElements(qname).next();
      soapElement = (SOAPElement)soapElement.getChildElements(new QName(targetNS, "product")).next();
      assertEquals("Ferrari", soapElement.getValue());
   }

   public void testDocWrappedService() throws Exception
   {
      QName serviceName = new QName(targetNS, "DocWrappedService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-soapbinding/DocWrappedService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      DocWrapped port = (DocWrapped)service.getPort(DocWrapped.class);

      String poRes = port.submitPO("Ferrari");
      assertEquals("Ferrari", poRes);
      
      poRes = port.submitNamespacedPO("Ferrari", "message");
      assertEquals("Ferrari", poRes);
   }

   public void testDocWrappedServiceMessageAccess() throws Exception
   {
      MessageFactory msgFactory = MessageFactory.newInstance();

      String reqEnv =
      "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
      " <env:Header/>" +
      " <env:Body>" +
      "  <ns1:SubmitPO xmlns:ns1='" + targetNS + "'>" +
      "   <PurchaseOrder>Ferrari</PurchaseOrder>" +
      "  </ns1:SubmitPO>" +
      " </env:Body>" +
      "</env:Envelope>";
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));

      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-soapbinding/DocWrappedService?wsdl");
      QName serviceName = new QName(targetNS, "DocWrappedService");
      QName portName = new QName(targetNS, "DocWrappedPort");
      Service service = Service.create(wsdlURL, serviceName);
      Dispatch dispatch = service.createDispatch(portName, SOAPMessage.class, Mode.MESSAGE);

      SOAPMessage resMsg = (SOAPMessage) dispatch.invoke(reqMsg);

      QName qname = new QName(targetNS, "SubmitPOResponse");
      SOAPElement soapElement = (SOAPElement)resMsg.getSOAPBody().getChildElements(qname).next();
      soapElement = (SOAPElement)soapElement.getChildElements(new QName("PurchaseOrderAck")).next();
      assertEquals("Ferrari", soapElement.getValue());
   }

   public void testNamespacedDocWrappedServiceMessageAccess() throws Exception
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();

      String purchaseNamespace = "http://namespace/purchase";
      String resultNamespace = "http://namespace/result";
      String stringNamespace = "http://namespace/string";

      String reqEnv =
      "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
      " <env:Header/>" +
      " <env:Body>" +
      "  <ns1:SubmitNamespacedPO xmlns:ns1='" + targetNS + "'>" +
      "   <ns2:NamespacedPurchaseOrder xmlns:ns2='" + purchaseNamespace + "'>Ferrari</ns2:NamespacedPurchaseOrder>" +
      "   <ns3:NamespacedString xmlns:ns3='" + stringNamespace + "'>message</ns3:NamespacedString>" +
      "  </ns1:SubmitNamespacedPO>" +
      " </env:Body>" +
      "</env:Envelope>";
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));
      URL epURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-soapbinding/DocWrappedService");

      SOAPMessage resMsg = con.call(reqMsg, epURL);

      QName qname = new QName(targetNS, "SubmitNamespacedPOResponse");
      SOAPElement soapElement = (SOAPElement)resMsg.getSOAPBody().getChildElements(qname).next();
      soapElement = (SOAPElement)soapElement.getChildElements(new QName(resultNamespace, "NamespacedPurchaseOrderAck")).next();
      assertEquals("Ferrari", soapElement.getValue());
   }
}
