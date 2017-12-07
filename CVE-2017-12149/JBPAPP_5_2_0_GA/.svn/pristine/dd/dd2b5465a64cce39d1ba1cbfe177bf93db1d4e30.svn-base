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
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;

import org.jboss.logging.Logger;

//Test on type with wsdlLocation
@WebServiceRef(name = "service1", value = TestEndpointService.class, wsdlLocation = "META-INF/wsdl/TestEndpoint.wsdl")

// Test multiple on type
@WebServiceRefs( { 
   @WebServiceRef(name = "service2", value = TestEndpointService.class),
   @WebServiceRef(name = "port1", value = TestEndpointService.class, type = TestEndpoint.class) })
public class ApplicationClient
{
   // Provide logging
   private static Logger log = Logger.getLogger(ApplicationClient.class);
   
   // Test on field with name
   @WebServiceRef(name = "TestEndpointService3")
   public static TestEndpointService service3;

   // Test on field without name
   @WebServiceRef
   public static TestEndpointService service4;

   // Test on method with value
   @WebServiceRef(name = "TestEndpointService5")
   public static void setServiceSetter5(TestEndpointService service)
   {
      ApplicationClient.service5 = service;
   }
   private static TestEndpointService service5;
   
   // Test on method without name
   @WebServiceRef
   public static void setServiceSetter6(TestEndpointService service)
   {
      ApplicationClient.service6 = service;
   }
   private static TestEndpointService service6;
   
   // Test on field with name and value
   @WebServiceRef(name = "Port2", value = TestEndpointService.class)
   public static TestEndpoint port2;

   // Test on field with value
   @WebServiceRef(value = TestEndpointService.class)
   public static TestEndpoint port3;

   public static InitialContext iniCtx;
   
   private static void setInitialCtx() throws NamingException
   {
      if (iniCtx == null)
      {
         InitialContext ctx = new InitialContext();
         Hashtable env = ctx.getEnvironment();
         env.put(Context.URL_PKG_PREFIXES, "org.jboss.naming.client");
         env.put("j2ee.clientName", "jbossws-client");
         iniCtx = new InitialContext(env);
      }
   }

   public static void main(String[] args)
   {
      String inStr = args[0];
      log.info("echo: " + inStr);
      
      ArrayList<TestEndpoint> ports = new ArrayList<TestEndpoint>();
      try
      {
         setInitialCtx();
         ports.add(((TestEndpointService)iniCtx.lookup("java:comp/env/service1")).getTestEndpointPort());
         ports.add(((TestEndpointService)iniCtx.lookup("java:comp/env/service2")).getTestEndpointPort());
         ports.add((TestEndpoint)service3.getPort(TestEndpoint.class));
         ports.add(((TestEndpointService)iniCtx.lookup("java:comp/env/TestEndpointService3")).getTestEndpointPort());
         ports.add((TestEndpoint)service4.getPort(TestEndpoint.class));
         ports.add(((TestEndpointService)iniCtx.lookup("java:comp/env/" + ApplicationClient.class.getName() + "/service4")).getTestEndpointPort());
         ports.add((TestEndpoint)service5.getPort(TestEndpoint.class));
         ports.add(((TestEndpointService)iniCtx.lookup("java:comp/env/TestEndpointService5")).getTestEndpointPort());
         ports.add((TestEndpoint)service6.getPort(TestEndpoint.class));
         ports.add(((TestEndpointService)iniCtx.lookup("java:comp/env/" + ApplicationClient.class.getName() + "/serviceSetter6")).getTestEndpointPort());
         ports.add((TestEndpoint)iniCtx.lookup("java:comp/env/port1"));
         ports.add(port2);
         ports.add((TestEndpoint)iniCtx.lookup("java:comp/env/Port2"));
         ports.add(port3);
         ports.add((TestEndpoint)iniCtx.lookup("java:comp/env/" + ApplicationClient.class.getName() + "/port3"));
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
   }

}
