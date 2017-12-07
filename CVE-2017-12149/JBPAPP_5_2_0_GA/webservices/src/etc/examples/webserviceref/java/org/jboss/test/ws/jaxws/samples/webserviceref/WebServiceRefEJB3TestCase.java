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
package org.jboss.test.ws.jaxws.samples.webserviceref;

import java.net.URL;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.test.ws.jaxws.samples.webserviceref.EJB3Remote;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test the JAXWS annotation: javax.xml.ws.WebServiceref
 *
 * @author Thomas.Diesler@jboss.com
 * @since 23-Oct-2005
 */
public class WebServiceRefEJB3TestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-samples-webserviceref";

   public static Test suite()
   {
      return new JBossWSTestSetup(WebServiceRefEJB3TestCase.class, "jaxws-samples-webserviceref.war");
   }

   public void testDynamicProxy() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName qname = new QName("http://org.jboss.ws/wsref", "EndpointService");
      Service service = Service.create(wsdlURL, qname);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);

      String helloWorld = "Hello World!";
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void testEJB3Client() throws Exception
   {
      deploy("jaxws-samples-webserviceref-ejb3-client.jar");
      try
      {
         InitialContext iniCtx = getInitialContext();
         EJB3Remote ejb3Remote = (EJB3Remote)iniCtx.lookup("/EJB3Client/remote");

         String helloWorld = "Hello World!";
         Object retObj = ejb3Remote.echo(helloWorld);
         assertEquals(helloWorld, retObj);
      }
      finally
      {
         undeploy("jaxws-samples-webserviceref-ejb3-client.jar");
      }
   }
}
