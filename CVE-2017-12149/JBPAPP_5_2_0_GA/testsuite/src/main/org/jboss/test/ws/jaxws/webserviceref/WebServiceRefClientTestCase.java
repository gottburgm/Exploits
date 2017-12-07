/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.ws.jaxws.webserviceref;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ejb3.client.ClientLauncher;

/**
 * Test the JAXWS annotation: javax.xml.ws.WebServiceref
 *
 * @author Thomas.Diesler@jboss.com
 * @since 23-Oct-2005
 */
public class WebServiceRefClientTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-webserviceref";
   
   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(WebServiceRefClientTestCase.class, "jaxws-webserviceref.war, jaxws-webserviceref-client.jar");
   }

   public void testDynamicProxy() throws Exception
   {
      URL wsdlURL = getResource("ws/jaxws/webserviceref/META-INF/wsdl/TestEndpoint.wsdl");
      QName qname = new QName("http://org.jboss.ws/wsref", "TestEndpointService");
      Service service = Service.create(wsdlURL, qname);
      TestEndpoint port = (TestEndpoint)service.getPort(TestEndpoint.class);

      String helloWorld = "Hello World!";
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void testApplicationClient() throws Throwable
   {
      String helloWorld = "Hello World!";
      new ClientLauncher().launch(ApplicationClient.class.getName(), "jbossws-client",  new String[]{helloWorld});
   }
}
