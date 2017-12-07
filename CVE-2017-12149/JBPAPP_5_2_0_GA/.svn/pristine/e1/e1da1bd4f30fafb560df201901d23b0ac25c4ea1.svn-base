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

import java.util.Enumeration;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A simple servlet that checks MC aware servlet context attributes
 *
 * @author Ales.Justin@jboss.org
 */
public class MCAwareServlet extends HttpServlet
{
   private static final long serialVersionUID = 1;

   public void init(ServletConfig servletConfig) throws ServletException
   {
      super.init(servletConfig);
      ServletContext servletContext = servletConfig.getServletContext();
      checkAttribute(servletContext, "org.jboss.deployers.structure.spi.DeploymentUnit");
      checkAttribute(servletContext, "jboss.kernel:service=Kernel");
   }

   protected static void checkAttribute(ServletContext context, String name)
   {
      Object attribute = context.getAttribute(name);
      if (attribute == null)
      {
         Enumeration names = context.getAttributeNames();
         System.out.println("ServletContext attributes = " + context);
         while(names.hasMoreElements())
         {
            System.out.println(names.nextElement());
         }
         throw new IllegalArgumentException("No such attribute: " + name);
      }
   }

   protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
   {
      System.out.println("doGet");
   }

   protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
   {
      System.out.println("doPost");
   }
}