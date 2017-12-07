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

import java.io.IOException;
import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;

import org.jboss.logging.Logger;

//Test on type with wsdlLocation
@WebServiceRef(name = "service1", value = EndpointService.class, wsdlLocation = "WEB-INF/wsdl/Endpoint.wsdl")

// Test multiple on type
@WebServiceRefs( { 
   @WebServiceRef(name = "service2", value = EndpointService.class),
   @WebServiceRef(name = "port1", value = EndpointService.class, type = Endpoint.class) })
public class ServletClient extends HttpServlet
{
   // Provide logging
   private static Logger log = Logger.getLogger(ServletClient.class);
   
   // Test on field with name
   @WebServiceRef(name = "EndpointService3")
   public EndpointService service3;

   // Test on field without name
   @WebServiceRef
   public EndpointService service4;

   // Test on method with value
   @WebServiceRef(name = "EndpointService5")
   public void setService5(EndpointService service)
   {
      this.service5 = service;
   }
   private EndpointService service5;
   
   // Test on method without name
   @WebServiceRef
   public void setService6(EndpointService service)
   {
      this.service6 = service;
   }
   private EndpointService service6;
   
   // Test on field with name and value
   @WebServiceRef(name = "Port2", value = EndpointService.class)
   public Endpoint port2;

   // Test on field with value
   @WebServiceRef(value = EndpointService.class)
   public Endpoint port3;

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      String inStr = req.getParameter("echo");
      log.info("doGet: " + inStr);
      
      ArrayList<Endpoint> ports = new ArrayList<Endpoint>();
      try
      {
         InitialContext iniCtx = new InitialContext();
         ports.add(((EndpointService)iniCtx.lookup("java:comp/env/service1")).getEndpointPort());
         ports.add(((EndpointService)iniCtx.lookup("java:comp/env/service2")).getEndpointPort());
         ports.add((Endpoint)service3.getPort(Endpoint.class));
         ports.add(((EndpointService)iniCtx.lookup("java:comp/env/EndpointService3")).getEndpointPort());
         ports.add((Endpoint)service4.getPort(Endpoint.class));
         ports.add(((EndpointService)iniCtx.lookup("java:comp/env/" + getClass().getName() + "/service4")).getEndpointPort());
         ports.add((Endpoint)service5.getPort(Endpoint.class));
         ports.add(((EndpointService)iniCtx.lookup("java:comp/env/EndpointService5")).getEndpointPort());
         ports.add((Endpoint)service6.getPort(Endpoint.class));
         ports.add(((EndpointService)iniCtx.lookup("java:comp/env/" + getClass().getName() + "/service6")).getEndpointPort());
         ports.add((Endpoint)iniCtx.lookup("java:comp/env/port1"));
         ports.add(port2);
         ports.add((Endpoint)iniCtx.lookup("java:comp/env/Port2"));
         ports.add(port3);
         ports.add((Endpoint)iniCtx.lookup("java:comp/env/" + getClass().getName() + "/port3"));
      }
      catch (Exception ex)
      {
         log.error("Cannot add port", ex);
         throw new WebServiceException(ex);
      }
      
      for (Endpoint port : ports)
      {
         String outStr = port.echo(inStr);
         if (inStr.equals(outStr) == false)
            throw new WebServiceException("Invalid echo return: " + inStr);
      }
      
      res.getWriter().print(inStr);
   }
}
