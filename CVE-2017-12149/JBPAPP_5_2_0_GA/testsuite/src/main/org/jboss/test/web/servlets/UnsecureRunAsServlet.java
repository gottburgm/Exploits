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
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.test.web.interfaces.RunAsTargetLocalHome;
import org.jboss.test.web.interfaces.RunAsTargetLocal;
import org.jboss.logging.Logger;

/** A servlet deployed under an unrestricted path that invokes the checkRunAs
 * method on a secured RunAsTargetLocal EJB.
 *
 * @author  Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class UnsecureRunAsServlet extends HttpServlet
{
   Logger log = Logger.getLogger(UnsecureRunAsServlet.class);

   /**
    * Test that init sees the run-as role
    * @throws ServletException
    */ 
   public void init() throws ServletException
   {
      String ejbName = super.getInitParameter("ejbName");
      try
      {
         InitialContext ctx = new InitialContext();
         RunAsTargetLocalHome home = null;
         Context enc = (Context) ctx.lookup("java:comp/env");
         home = (RunAsTargetLocalHome) enc.lookup(ejbName);
         RunAsTargetLocal bean = home.create();
         bean.checkRunAs();
      }
      catch(Exception e)
      {
         throw new ServletException("Access to failed to method: checkRunAs", e);
      }
   }

   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      String ejbName = request.getParameter("ejbName");
      try
      {
         InitialContext ctx = new InitialContext();
         RunAsTargetLocalHome home = null;
         Context enc = (Context) ctx.lookup("java:comp/env");
         home = (RunAsTargetLocalHome) enc.lookup(ejbName);
         RunAsTargetLocal bean = home.create();
         bean.checkRunAs();
      }
      catch(Exception e)
      {
         log.error("Access to checkRunAs failed", e);
         throw new ServletException("Access to checkRunAs failed", e);
      }

      Principal user = request.getUserPrincipal();
      PrintWriter out = response.getWriter();
      response.setContentType("text/html");
      out.println("<html>");
      out.println("<head><title>UnsecureRunAsServlet</title></head><body>");
      out.println("<h1>UnsecureRunAsServlet Accessed</h1>");
      out.println("<pre>You have accessed this servlet as user: "+user+"<br>");
      out.println("</pre>");
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

}
