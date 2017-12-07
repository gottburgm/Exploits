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
package org.jboss.test.webservice.endpoint;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.http.HttpContext;
import org.jboss.wsf.spi.http.HttpServer;
import org.jboss.wsf.spi.http.HttpServerFactory;

/**
 * Test Endpoint deployment
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-Jul-2006
 */
public class EndpointServlet extends HttpServlet
{
   private Endpoint endpoint;
   
   @Override
   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      
      // Create the endpoint
      EndpointBean epImpl = new EndpointBean();
      endpoint = Endpoint.create(SOAPBinding.SOAP11HTTP_BINDING, epImpl);

      // Create and start the HTTP server
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      HttpServer httpServer = spiProvider.getSPI(HttpServerFactory.class).getHttpServer();
      httpServer.start();
      
      // Create the context and publish the endpoint
      HttpContext context = httpServer.createContext("/jaxws-endpoint");
      endpoint.publish(context);
   }
   
   @Override
   public void destroy()
   {
      // Stop the endpoint
      endpoint.stop();
      
      super.destroy();
   }

   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      // Create the port
      URL wsdlURL = getServletContext().getResource("/WEB-INF/wsdl/TestService.wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/endpoint", "EndpointService");
      Service service = Service.create(wsdlURL, qname);
      EndpointInterface port = (EndpointInterface)service.getPort(EndpointInterface.class);

      // Invoke the endpoint
      String param = req.getParameter("param");
      String retStr = port.echo(param);

      // Return the result
      PrintWriter pw = new PrintWriter(res.getWriter());
      pw.print(retStr);
   }
}
