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

import java.util.ArrayList;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;

import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.logging.Logger;

// standard EJB3 annotations
@Remote(EJB3Remote.class)
@RemoteBinding(jndiBinding = "/ejb3/EJB3Client")
@Stateless

// Test on type with wsdlLocation
@WebServiceRef(name = "service1", value = TestEndpointService.class, wsdlLocation = "META-INF/wsdl/TestEndpoint.wsdl")

// Test multiple on type
@WebServiceRefs( { 
   @WebServiceRef(name = "service2", value = TestEndpointService.class),
   @WebServiceRef(name = "port1", value = TestEndpointService.class, type = TestEndpoint.class) })
public class EJB3Client implements EJB3Remote
{
   // Provide logging
   private static Logger log = Logger.getLogger(EJB3Client.class);

   // Test on field with name
   @WebServiceRef(name = "TestEndpointService3")
   public TestEndpointService service3;

   // Test on field without name
   @WebServiceRef
   public TestEndpointService service4;

   // Test on method with value
   @WebServiceRef(name = "TestEndpointService5")
   public void setServiceSetter5(TestEndpointService service)
   {
      this.service5 = service;
   }
   private TestEndpointService service5;
   
   // Test on method without name
   @WebServiceRef
   public void setServiceSetter6(TestEndpointService service)
   {
      this.service6 = service;
   }
   private TestEndpointService service6;
   
   // Test on field with name and value
   @WebServiceRef(name = "Port2", value = TestEndpointService.class)
   public TestEndpoint port2;

   // Test on field with value
   @WebServiceRef(value = TestEndpointService.class)
   public TestEndpoint port3;

   public String echo(String inStr)
   {
      log.info("echo: " + inStr);

      ArrayList<TestEndpoint> ports = new ArrayList<TestEndpoint>();
      try
      {
         InitialContext iniCtx = new InitialContext();
         ports.add(((TestEndpointService)iniCtx.lookup("java:comp/env/service1")).getTestEndpointPort());
         ports.add(((TestEndpointService)iniCtx.lookup("java:comp/env/service2")).getTestEndpointPort());
         ports.add((TestEndpoint)service3.getPort(TestEndpoint.class));
         ports.add(((TestEndpointService)iniCtx.lookup("java:comp/env/TestEndpointService3")).getTestEndpointPort());
         ports.add((TestEndpoint)service4.getPort(TestEndpoint.class));
         ports.add(((TestEndpointService)iniCtx.lookup("java:comp/env/" + getClass().getName() + "/service4")).getTestEndpointPort());
         ports.add((TestEndpoint)service5.getPort(TestEndpoint.class));
         ports.add(((TestEndpointService)iniCtx.lookup("java:comp/env/TestEndpointService5")).getTestEndpointPort());
         ports.add((TestEndpoint)service6.getPort(TestEndpoint.class));
         ports.add(((TestEndpointService)iniCtx.lookup("java:comp/env/" + getClass().getName() + "/serviceSetter6")).getTestEndpointPort());
         ports.add((TestEndpoint)iniCtx.lookup("java:comp/env/port1"));
         ports.add(port2);
         ports.add((TestEndpoint)iniCtx.lookup("java:comp/env/Port2"));
         ports.add(port3);
         ports.add((TestEndpoint)iniCtx.lookup("java:comp/env/" + getClass().getName() + "/port3"));
      }
      catch (Exception ex)
      {
         log.error("Cannot add port", ex);
         throw new WebServiceException(ex);
      }

      for (TestEndpoint port : ports)
      {
         String outStr = port.echo(inStr);
         if (inStr.equals(outStr) == false)
            throw new WebServiceException("Invalid echo return: " + inStr);
      }

      return inStr;
   }
}
