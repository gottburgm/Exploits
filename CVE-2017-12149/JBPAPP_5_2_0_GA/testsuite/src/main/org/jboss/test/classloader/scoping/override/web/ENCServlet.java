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
package org.jboss.test.classloader.scoping.override.web;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.naming.InitialContext;
import javax.naming.Context;

/** A servlet that validates the context java:comp/env context when scoping
 * is enabled.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class ENCServlet extends HttpServlet
{
   /**
    *
    * @param servletConfig
    * @throws ServletException
    */
   public void init(ServletConfig servletConfig) throws ServletException
   {
      super.init(servletConfig);
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
      response.setContentType("text/html");
      PrintWriter pw = response.getWriter();
      pw.println("<html><head><title>ENCServlet Scoping Test</title></head>");
      pw.println("<body><h1>ENCServlet Scoping Test</h1>");

      try
      {
         InitialContext ctx = new InitialContext();
         Context env = (Context) ctx.lookup("java:comp/env");
         pw.println("env = "+env);
         String value1 = (String) env.lookup("prop1");
         if( value1.equals("value1") == false )
            throw new IllegalStateException("prop1 != value1, "+value1);
      }
      catch (Exception e)
      {
         throw new ServletException("Failed to validate ENC", e);
      }
      pw.println("</body></html>");
   }
}
