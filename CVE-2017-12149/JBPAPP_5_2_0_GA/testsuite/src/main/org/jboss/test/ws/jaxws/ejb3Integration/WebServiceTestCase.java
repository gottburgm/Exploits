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
package org.jboss.test.ws.jaxws.ejb3Integration;

import junit.framework.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.Hashtable;
import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.test.ws.jaxws.webserviceref.WebServiceRefServletTestCase;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision: 85945 $
 */
public class WebServiceTestCase extends JBossWSTest
{
	public static Test suite()
   {
      //return JBossWSTestSetup.newTestSetup(WebServiceTestCase.class, "webservices-ejb3.jar, webservices-ejb3-client.jar");
	   return JBossWSTestSetup.newTestSetup(WebServiceTestCase.class, "webservices-ejb3.jar");
   }

   public void testRemoteAccess() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      Ejb3WSEndpoint ejb3Remote = (Ejb3WSEndpoint)iniCtx.lookup("/test-webservices/SimpleEndpoint");

      String helloWorld = "Hello world!";
      Object retObj = ejb3Remote.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   /**
    * Simple web service test coverage
    * @throws Exception
    */
   public void testWebService() throws Exception
   {
      Service service = Service.create(
        new URL("http://"+getServerHost()+":8080/webservices-ejb3/SimpleEndpoint?wsdl"),
        new QName("http://ejb3Integration.jaxws.ws.test.jboss.org/","SimpleEndpointService")
      );

      String msg = "testWebService";
      Ejb3WSEndpoint port = service.getPort(Ejb3WSEndpoint.class);
      String response = port.echo(msg);
      assertEquals(msg, response);
   }

   /**
    * Test web service context injection
    * @throws Exception
    */
   public void testWebServiceContext() throws Exception
   {
      Service service = Service.create(
        new URL("http://"+getServerHost()+":8080/webservices-ejb3/WebServiceContextEndpoint?wsdl"),
        new QName("http://ejb3Integration.jaxws.ws.test.jboss.org/","WebServiceContextEndpointService")
      );

      String msg = "testWebServiceContext";
      Ejb3WSEndpoint port = service.getPort(Ejb3WSEndpoint.class);
      String response = port.echo(msg);
      assertNotNull(response);
   }

   public void testWebServiceRef() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      BusinessInterface ejb3Remote = (BusinessInterface)iniCtx.lookup("/test-webservices/WebServiceRefBean");

      String msg = "testWebServiceRef";
      Object retObj = ejb3Remote.echo(msg);
      assertEquals(msg, retObj);
   }

   /**
    * Test web service context injection into JAX-WS handler
    * @throws Exception
    */
   public void testHandlerContext() throws Exception
   {
      Service service = Service.create(
        new URL("http://"+getServerHost()+":8080/webservices-ejb3/HandlerContextEndpoint?wsdl"),
        new QName("http://ejb3Integration.jaxws.ws.test.jboss.org/","HandlerContextEndpointService")
      );

      String msg = "testHandlerContext";
      Ejb3WSEndpoint port = service.getPort(Ejb3WSEndpoint.class);
      String response = port.echo(msg);
      assertNotNull(response);
   }
}
