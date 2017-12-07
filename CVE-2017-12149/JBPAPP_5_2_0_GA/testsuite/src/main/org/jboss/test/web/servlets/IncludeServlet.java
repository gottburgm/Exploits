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
package org.jboss.test.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author  Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class IncludeServlet extends HttpServlet
{
   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      Principal user = request.getUserPrincipal();
      boolean isSecure = request.getRemoteUser() != null;
      response.setBufferSize(2048);
      PrintWriter out = response.getWriter();
      response.setContentType("text/html");
      out.println("<html>");
      out.println("<head><title>IncludeServlet</title></head>");
      out.println("<h1>IncludeServlet Accessed</h1>");
      out.println("<body>You have accessed this servlet as user:"+user);
      try
      {
         out.println("Accessing /restricted/SecureEJBAccess?includeHead=false<br>");
         RequestDispatcher rd = request.getRequestDispatcher("/restricted/SecureEJBAccess?includeHead=false");
         rd.include(request, response);
      }
      catch(ServletException e)
      {
         if( isSecure == true )
            throw e;
         out.println("Access to /restricted/SecureEJBAccess failed as expected<br>");
      }

      out.println("Accessing /UnsecureEJBAccess?includeHead=false<br>");
      RequestDispatcher rd = request.getRequestDispatcher("/UnsecureEJBAccess?includeHead=false");
      rd.include(request, response);
      out.println("</body></html>");
      out.close();
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
   
}
