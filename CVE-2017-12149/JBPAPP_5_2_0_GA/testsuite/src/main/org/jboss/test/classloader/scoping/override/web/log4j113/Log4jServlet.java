/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.classloader.scoping.override.web.log4j113;


import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

/** A servlet that validates that it sees the log4j 1.1.3 version of the
 * Category class on initialization.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class Log4jServlet extends HttpServlet
{
   private Category log;

   /**
    *
    * @param servletConfig
    * @throws ServletException
    */
   public void init(ServletConfig servletConfig) throws ServletException
   {
      super.init(servletConfig);
      // Validate the log4j env against the 1.1.3 classes
      try
      {
         Class categoryClass = Category.class;
         // Check that the 1.1.3 assert(boolean, String) method exists
         Class[] sig = {boolean.class, String.class};
         Method m = categoryClass.getDeclaredMethod("assert", sig);
         System.out.println("found assert method: "+m);
         // Find the log4j.properties file
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         URL resURL = loader.getResource("log4j.properties");
         System.out.println("found log4j.properties: "+resURL);
         PropertyConfigurator config = new PropertyConfigurator();
         log = Category.getInstance(Log4jServlet.class);
         config.configure(resURL);

         // Try to access the java:comp/env context
         InitialContext ctx = new InitialContext();
         Context enc = (Context) ctx.lookup("java:comp/env");
         System.out.println("Was able to lookup ENC");
      }
      catch(Throwable t)
      {
         throw new ServletException("Log4jServlet init failed", t);
      }
   }

   protected void doGet(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
   {
      processRequest(request, response);
   }

   protected void doPost(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
   {
      processRequest(request, response);
   }

   private void processRequest(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
   {
      // Try to access the java:comp/env context
      try
      {
         InitialContext ctx = new InitialContext();
         Context enc = (Context) ctx.lookup("java:comp/env");
         System.out.println("Was able to lookup ENC");
      }
      catch(NamingException e)
      {
         throw new ServletException("Failed to lookup ENC", e);
      }

      log.info("processRequest, path="+request.getPathInfo());
      response.setContentType("text/html");
      PrintWriter pw = response.getWriter();
      pw.println("<html><head><title>Log4j1.1.3 test servlet</title></head>");
      pw.println("<body><h1>Log4j1.1.3 test servlet</h1></body>");
      pw.println("</html>");
   }
}
