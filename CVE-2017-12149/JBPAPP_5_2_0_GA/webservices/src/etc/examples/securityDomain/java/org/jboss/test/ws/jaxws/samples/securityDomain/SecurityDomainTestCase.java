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
package org.jboss.test.ws.jaxws.samples.securityDomain;

import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Secure endpoint using
 * 
 * @SecurityDomain
 * 
 * @author alessio.soldano@jboss.com
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
public class SecurityDomainTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-securityDomain";

   public static Test suite()
   {
      return new JBossWSTestSetup(SecurityDomainTestCase.class, "jaxws-samples-securityDomain.jar");
   }

   private SecureEndpoint getPort() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/securityDomain", "SecureEndpointService");
      SecureEndpoint port = Service.create(wsdlURL, serviceName).getPort(SecureEndpoint.class);
      return port;
   }

   public void testNegative() throws Exception
   {
      SecureEndpoint port = getPort();
      try
      {
         port.echo("Hello");
         fail("Expected: Invalid HTTP server response [401] - Unauthorized");
      }
      catch (WebServiceException ex)
      {
         // all good
      }
   }

   public void testPositive() throws Exception
   {
      SecureEndpoint port = getPort();

      Map<String, Object> reqContext = ((BindingProvider)port).getRequestContext();
      reqContext.put(BindingProvider.USERNAME_PROPERTY, "kermit");
      reqContext.put(BindingProvider.PASSWORD_PROPERTY, "thefrog");

      String retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }
}
