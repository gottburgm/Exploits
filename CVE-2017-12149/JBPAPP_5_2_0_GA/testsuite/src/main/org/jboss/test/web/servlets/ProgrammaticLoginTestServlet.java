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
 
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.web.tomcat.security.login.WebAuthentication;

//$Id: ProgrammaticLoginTestServlet.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

/**
 *  JBAS-4077: Programmatic Web Login
 *  Servlet picks up the username, password from the request parameters
 *  and then does the web authentication
 *  @author Anil.Saldhana@redhat.com
 *  @since  Mar 12, 2007 
 *  @version $Revision: 85945 $
 */
public class ProgrammaticLoginTestServlet extends HttpServlet
{ 
   private static final long serialVersionUID = 1L;

   protected void service(HttpServletRequest request, HttpServletResponse response)
   throws ServletException, IOException
   {
      String operation = request.getParameter("operation");
      String username = request.getParameter("username");
      String pass = request.getParameter("pass");

      if("login".equals(operation))
         this.login(request, username, pass);
      else if("logout".equals(operation))
         this.logout(request);
      else
         throw new ServletException("Unrecognized operation: " + operation);
   }

   private void login(HttpServletRequest request, String username, String pass)
   throws ServletException
   {
      if(username == null || pass == null)
         throw new RuntimeException("username or password is null");
      WebAuthentication pwl = new WebAuthentication();
      pwl.login(username, pass);

      //Only when there is web login, does the principal become visible
      log("User Principal=" + request.getUserPrincipal());
      log("isUserInRole(Authorized User)=" + request.isUserInRole("AuthorizedUser"));
      if(request.getUserPrincipal() == null || !request.isUserInRole("AuthorizedUser"))
         throw new ServletException("User is not authenticated or the isUserInRole check failed");
   }
   
   private void logout(HttpServletRequest request) throws ServletException
   {
      //Log the user out
      new WebAuthentication().logout();
      if(request.getUserPrincipal() != null || request.isUserInRole("AuthorizedUser"))
         throw new ServletException("User is still authenticated or pass: isUserInRole(Authorized User)");
   }
}
