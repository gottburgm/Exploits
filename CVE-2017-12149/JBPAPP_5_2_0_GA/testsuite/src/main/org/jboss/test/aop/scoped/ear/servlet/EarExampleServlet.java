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
package org.jboss.test.aop.scoped.ear.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.test.aop.scoped.ear.ejb.ExampleSession;
import org.jboss.test.aop.scoped.ear.ejb.ExampleSessionHome;
import org.jboss.test.aop.scoped.ear.interceptor.TestInterceptor;

/** 
 *  
 * @author <a href="mailto:kabirkhan@bigfoot.com">Kabir Khan</a>
 *
 */
public class EarExampleServlet extends HttpServlet {

   String scope;
   public void init(ServletConfig cfg) throws ServletException
   {
      super.init(cfg);
      scope = super.getInitParameter("scope");
   }

   public void service(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {
      try 
      {
         InitialContext ctx = new InitialContext();
         Object obj = ctx.lookup("ExampleSession" + scope);
         
         System.out.println("Expected loader " + ExampleSessionHome.class.getClassLoader());
         System.out.println("Interfaces");
         Class[] ifs = obj.getClass().getInterfaces();
         for (int i = 0 ; i < ifs.length ; i++)
         {
            System.out.println(i + " " + ifs[i].getName() + " " + ifs[i].getClassLoader());
         }
         
         ExampleSessionHome home = (ExampleSessionHome)PortableRemoteObject.narrow(obj, ExampleSessionHome.class);
         ExampleSession exSess = home.create();
         
         TestInterceptor.intercepted = 0;
         exSess.getValue("hello"); 
         if (TestInterceptor.intercepted != 1)
         {
            throw new ServletException("Wrong TestInterceptor.intercepted for bean, expected=1; actual=" + TestInterceptor.intercepted);
         }

         //When using generated advisors the interceptors are not actually created until a joinpoint requiring it has been accessed 
         String expected = "scope" + scope;
         String actual = TestInterceptor.scope;
         if (!actual.equals(expected))
         {
            throw new ServletException("Wrong TestInterceptor.scope, expected=" + expected + "; actual=" + TestInterceptor.scope);
         }
         
         TestInterceptor.intercepted = 0;
         testServlet();
         if (TestInterceptor.intercepted != 1)
         {
            throw new ServletException("Wrong TestInterceptor.intercepted for servlet, expected=1; actual=" + TestInterceptor.intercepted);
         }

         response.setContentType("text/html");
         PrintWriter out = response.getWriter();
         out.println("<html>");
         out.println("<head><title>EarExampleServlet</title></head>");
         out.println("<body>Tests passed<br></body>");
         out.println("</html>");
         out.close();

      } 
      catch (Exception e) 
      {
         e.printStackTrace();
         
         StringBuffer err = new StringBuffer(e.getClass().getName() + "\n");
         err.append(e.getMessage() + "\n");
         
         StackTraceElement[] elements = e.getStackTrace();
         for (int i = 0 ; i < elements.length ; i++)
         {
            err.append(elements[i].toString() + "\n");
         }
         
         response.sendError(500, err.toString());
      } 
   }
   
   private void testServlet()
   {
      
   }
}
