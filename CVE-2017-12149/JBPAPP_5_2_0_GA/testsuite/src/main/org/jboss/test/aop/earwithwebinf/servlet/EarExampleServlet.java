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
package org.jboss.test.aop.earwithwebinf.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.test.aop.earwithwebinf.interceptor.EJBInterceptor;
import org.jboss.test.aop.earwithwebinf.interceptor.WebInterceptor;
import org.jboss.test.aop.earwithwebinf.webinf.classes.ClassesClass;
import org.jboss.test.aop.earwithwebinf.webinf.lib.LibClass;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 85945 $
 */
public class EarExampleServlet extends HttpServlet
{
   public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      try 
      {
         EJBInterceptor.invoked = false;
         WebInterceptor.invoked = false;
         ClassesClass classesClass = new ClassesClass();
         if (EJBInterceptor.invoked) throw new RuntimeException("Should not have invoked EJBInterceptor");
         if (!WebInterceptor.invoked) throw new RuntimeException("Should have invoked WebInterceptor");
         
         EJBInterceptor.invoked = false;
         WebInterceptor.invoked = false;
         String ret = classesClass.invokeEjb("Test");
         if (!EJBInterceptor.invoked) throw new RuntimeException("Should have invoked EJBInterceptor");
         if (!WebInterceptor.invoked) throw new RuntimeException("Should have invoked WebInterceptor");
         if (!ret.equals("#Test#")) throw new RuntimeException("Did not reach EJBLayer");
         
         EJBInterceptor.invoked = false;
         WebInterceptor.invoked = false;
         LibClass libClass = new LibClass();
         if (EJBInterceptor.invoked) throw new RuntimeException("Should not have invoked EJBInterceptor");
         if (!WebInterceptor.invoked) throw new RuntimeException("Should have invoked WebInterceptor");
         
         EJBInterceptor.invoked = false;
         WebInterceptor.invoked = false;
         ret = libClass.invokeEjb("Test");
         if (!EJBInterceptor.invoked) throw new RuntimeException("Should have invoked EJBInterceptor");
         if (!WebInterceptor.invoked) throw new RuntimeException("Should have invoked WebInterceptor");
         if (!ret.equals("#Test#")) throw new RuntimeException("Did not reach EJBLayer");
         
         //It worked
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
   
}
