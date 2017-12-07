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
package org.jboss.test.web.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A simple servlet that spits out the request info.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class RequestInfoServlet extends HttpServlet
{
   private static final long serialVersionUID = 1;

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
   {
      PrintWriter out = res.getWriter();
      out.println("<html><head><title>RequestInfoServlet</title></head><body>");
      out.println("<h1>Request Info</h1>");
      out.println("getCharacterEncoding"+req.getCharacterEncoding()+"<br>");
      out.println("getContextPath"+req.getContextPath()+"<br>");
      out.println("getContentLength"+req.getContentLength()+"<br>");
      out.println("getContentType"+req.getContentType()+"<br>");
      out.println("</body></html>");
      out.flush();
   }
   
}
