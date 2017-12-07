/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.ejb3Integration.injection;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.test.ws.jaxws.ejb3Integration.injection.webservice.EndpointIface;
import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;

/**
 * [JBWS-2634] Implement support for @EJB annotations in WS components
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
public final class InjectionTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(InjectionTestCase.class, "jaxws-injection.ear");
   }

   public void testPojoEndpoint() throws Exception
   {
      QName serviceName = new QName("http://jbossws.org/injection", "POJOService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-injection-pojo/POJOService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      EndpointIface proxy = (EndpointIface)service.getPort(EndpointIface.class);
      assertEquals("Hello World!:Inbound:TestHandler:POJOBean:Outbound:TestHandler", proxy.echo("Hello World!"));
   }

   public void testEjb3Endpoint() throws Exception
   {
      QName serviceName = new QName("http://jbossws.org/injection", "EJB3Service");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-injection-ejb3/EJB3Service?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      EndpointIface proxy = (EndpointIface)service.getPort(EndpointIface.class);
      assertEquals("Hello World!:Inbound:TestHandler:EJB3Bean:Outbound:TestHandler", proxy.echo("Hello World!"));
   }
}
