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
package org.jboss.test.classloader.leak.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.test.classloader.leak.ejb3.Ejb3StatefulSession;
import org.jboss.test.classloader.leak.ejb3.Ejb3StatelessSession;

/**
 * Servlet that invokes on EJB3 Session beans.
 * 
 * @author Brian Stansberry
 */
public class Ejb3Servlet extends HttpServlet
{
   private static final long serialVersionUID = 1L;

   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      try
      {
          InitialContext ctx = new InitialContext();
          Ejb3StatelessSession ejb3slsb = (Ejb3StatelessSession) ctx.lookup("Ejb3StatelessSession/remote");
          ejb3slsb.log("EJB");
          Ejb3StatefulSession ejb3sfsb = (Ejb3StatefulSession) ctx.lookup("Ejb3StatefulSession/remote");
          ejb3sfsb.log("EJB");
          Ejb3StatelessSession tlpejb3slsb = (Ejb3StatelessSession) ctx.lookup("ThreadLocalPoolEjb3StatelessSession/remote");
          tlpejb3slsb.log("EJB");
      }
      catch (Exception e)
      {
          throw new javax.servlet.ServletException(e);
      }
      
      res.setContentType("text/text");
      PrintWriter writer = res.getWriter();
      writer.println("EJB");
      writer.flush();
   }
   
}
