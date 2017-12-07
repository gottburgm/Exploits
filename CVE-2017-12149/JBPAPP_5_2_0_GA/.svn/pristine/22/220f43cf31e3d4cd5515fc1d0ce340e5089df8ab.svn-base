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
package org.jboss.test.ejb3.webservice.unit;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.test.ejb3.common.EJB3TestCase;
import org.jboss.test.ejb3.webservice.EchoServiceRef;
import org.jboss.test.ejb3.webservice.WebServiceContextEndpoint;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class WebServiceUnitTestCase extends EJB3TestCase
{
   public WebServiceUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testJBPAPP613() throws Exception
   {
      EchoServiceRef bean = lookup("EchoServiceRefBean/remote", EchoServiceRef.class);
      
      String response = bean.echo("JBPAPP-613");
      assertNotNull(response);
   }
   
   public void testJBPAPP614() throws Exception
   {
      Service service = Service.create(
            new URL("http://"+getServerHost()+":8080/ejb3-webservice/WebServiceContextEndpointBean?wsdl"),
            new QName("http://webservice.ejb3.test.jboss.org/","WebServiceContextEndpointBeanService")
          );

      WebServiceContextEndpoint port = service.getPort(WebServiceContextEndpoint.class);
      String response = port.report();
      assertNotNull(response);
   }
   
   public void testJBPAPP620() throws Exception
   {
      Service service = Service.create(
            new URL("http://"+getServerHost()+":8080/ejb3-webservice/WebServiceContextEndpointBean?wsdl"),
            new QName("http://webservice.ejb3.test.jboss.org/","WebServiceContextEndpointBeanService")
          );

      String msg = "JBPAPP-620";
      WebServiceContextEndpoint port = service.getPort(WebServiceContextEndpoint.class);
      String response = port.echo(msg);
      assertNotNull(response);
   }
   
   public static Test suite() throws Exception
   {
      return getDeploySetup(WebServiceUnitTestCase.class, "ejb3-webservice.jar");
   }

}
