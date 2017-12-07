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
package org.jboss.test.ws.jaxws.samples.logicalhandler;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test JAXWS logical handlers
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-Aug-2006
 */
public class LogicalHandlerSourceTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(LogicalHandlerSourceTestCase.class, "jaxws-samples-logicalhandler-source.war");
   }

   public void testSourceDoc() throws Exception
   {
      URL endpointAddress = new URL("http://" + getServerHost() + ":8080/jaxws-samples-logicalhandler-source/doc?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jaxws/samples/logicalhandler", "SOAPEndpointDocService");
      Service service = new SOAPEndpointSourceService(endpointAddress, serviceName);
      SOAPEndpointSourceDoc port = (SOAPEndpointSourceDoc)service.getPort(SOAPEndpointSourceDoc.class);
      
      String retStr = port.echo("hello");
      assertResponse(retStr);
   }

   public void testSourceRpc() throws Exception
   {
      URL endpointAddress = new URL("http://" + getServerHost() + ":8080/jaxws-samples-logicalhandler-source/rpc?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jaxws/samples/logicalhandler", "SOAPEndpointRpcService");
      Service service = new SOAPEndpointSourceService(endpointAddress, serviceName);
      SOAPEndpointSourceRpc port = (SOAPEndpointSourceRpc)service.getPort(SOAPEndpointSourceRpc.class);
      
      String retStr = port.echo("hello");
      assertResponse(retStr);
   }
   
   private void assertResponse(String retStr)
   {
      StringBuffer expStr = new StringBuffer("hello");
      expStr.append(":Outbound:LogicalSourceHandler");
      expStr.append(":Outbound:ProtocolHandler");
      expStr.append(":Outbound:PortHandler");
      expStr.append(":Inbound:PortHandler");
      expStr.append(":Inbound:ProtocolHandler");
      expStr.append(":Inbound:LogicalSourceHandler");
      expStr.append(":endpoint");
      expStr.append(":Outbound:LogicalSourceHandler");
      expStr.append(":Outbound:ProtocolHandler");
      expStr.append(":Outbound:PortHandler");
      expStr.append(":Inbound:PortHandler");
      expStr.append(":Inbound:ProtocolHandler");
      expStr.append(":Inbound:LogicalSourceHandler");
      assertEquals(expStr.toString(), retStr);
   }
}
