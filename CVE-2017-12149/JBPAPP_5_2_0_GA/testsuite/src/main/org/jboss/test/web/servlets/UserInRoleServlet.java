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
import java.util.ArrayList;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.util.Strings;

/** A servlet that calls isUserInRole for every role name defined in the
 * expectedUserRoles init parameter and validates that each role is assigned
 * to the user. Any role in the expectedUserRoles for which isUserInRole is
 * false is added to the X-ExpectedUserRoles-Errors reply header. If the user
 * has every role from the expectedUserRoles list, the X-ExpectedUserRoles-Errors
 * header will not be in the reply.
 * 
 * This servlet also calls isUserInRole for every role name defined in the
 * unexpectedUserRoles init parameter and validates that each role is NOT 
 * assigned to the user. Any role in the unexpectedUserRoles for which
 * isUserInRole is true is added to the X-UnexpectedUserRoles-Errors reply
 * header. If the user has no roles from the unexpectedUserRoles list, the
 * X-UnexpectedUserRoles-Errors header will not be in the reply.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class UserInRoleServlet extends HttpServlet
{
   /** The roles for which isUserInRole should return true */
   private String[] expectedUserRoles;
   /** The roles for which isUserInRole should return false */
   private String[] unexpectedUserRoles;

   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      String param = config.getInitParameter("expectedUserRoles");
      expectedUserRoles = Strings.split(param, ",");
      param = config.getInitParameter("unexpectedUserRoles");
      unexpectedUserRoles = Strings.split(param, ",");
   }

   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      Principal user = request.getUserPrincipal();
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println("<html>");
      out.println("<head><title>UserInRoleServlet</title></head>");
      out.println("<body>");
      out.println("You have accessed this servlet as user:"+user);

      out.println("<h1>ExpectedUserRoles</h1>");
      out.println("<ul>");
      ArrayList errors = new ArrayList();
      for(int n = 0; n < expectedUserRoles.length; n ++)
      {
         String role = expectedUserRoles[n];
         boolean inRole = request.isUserInRole(role);
         out.println("<li>isUserInRole("+role+") = "+inRole+"</li>");
         if( inRole == false )
            errors.add(role);
      }
      out.println("</ul>");
      if( errors.size() > 0 )
      {
         String value = errors.toString();
         response.addHeader("X-ExpectedUserRoles-Errors", value);
      }

      errors.clear();
      out.println("<h1>UnexpectedUserRoles</h1>");
      out.println("<ul>");
      for(int n = 0; n < unexpectedUserRoles.length; n ++)
      {
         String role = unexpectedUserRoles[n];
         boolean inRole = request.isUserInRole(role);
         out.println("<li>isUserInRole("+role+") = "+inRole+"</li>");
         if( inRole == true )
            errors.add(role);
      }
      if( errors.size() > 0 )
      {
         String value = errors.toString();
         response.addHeader("X-UnexpectedUserRoles-Errors", value);
      }
      out.println("</ul>");

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

