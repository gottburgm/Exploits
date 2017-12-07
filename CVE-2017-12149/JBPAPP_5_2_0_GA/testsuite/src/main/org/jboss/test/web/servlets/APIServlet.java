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
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jboss.test.web.util.Util;

/** A servlet that tests use of various servlet API calls that can be affected
 by the web container integration layer.
 
 @author  Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class APIServlet extends HttpServlet
{
   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println("<html>");
      out.println("<head><title>APIServlet</title></head><body><pre>");
      String op = request.getParameter("op");
      if( op.equals("testGetRealPath") )
      {
         String realPath = testGetRealPath();
         out.println("testGetRealPath ok, realPath="+realPath+"\n");
      }
      else if( op.equals("testSessionListener") )
      {
         testSessionListener(request);
      }
      else
      {
         throw new ServletException("Unknown operation called, op="+op);
      }
      out.println("</pre></body></html>");
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

   private String testGetRealPath()
      throws ServletException
   {
      String realPath = getServletContext().getRealPath("/");
      if( realPath == null )
         throw new ServletException("getServletContext().getRealPath(/) returned null");
      return realPath;
   }

   private void testSessionListener(HttpServletRequest request)
      throws ServletException
   {
      // Create/get the session
      HttpSession session = request.getSession(true);
      String sessionID = session.getId();
      boolean created = TestSessionListener.wasCreated(sessionID);
      if( created == false )
         throw new ServletException("No session create event seen");
      // Invalidate the session to test the destroy event
      session.invalidate();
      boolean destroyed = TestSessionListener.wasDestroyed(sessionID);
      if( destroyed == false )
         throw new ServletException("No session destroy event seen");
   }
}
