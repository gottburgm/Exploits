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
package org.jboss.test.classloader.scoping.override.web.comlog;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.util.Date;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/** A servlet that validates that it sees the application specific log4j output
 * in the ${jboss.server.log.dir}/cl-test.log increasing. The logging interface
 * used is the commons-logging LogFactory/Log.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class Log4jServlet extends HttpServlet
{
   private static Log log = LogFactory.getLog(Log4jServlet.class);

   /**
    *
    * @param servletConfig
    * @throws javax.servlet.ServletException
    */
   public void init(ServletConfig servletConfig) throws ServletException
   {
      super.init(servletConfig);
      log.info("init, servletConfig="+servletConfig);
      System.out.println("Log class: "+log.getClass());
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

   private void processRequest(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
   {
      log.info("processRequest, path="+request.getPathInfo());
      try
      {
         InitialContext ctx = new InitialContext();
         Context enc = (Context) ctx.lookup("java:comp/env");
         log.info("Was able to lookup ENC, "+enc);
      }
      catch(NamingException e)
      {
         throw new ServletException("Failed to lookup ENC", e);
      }

      // Validate that the cl-test.log
      String logDir = System.getProperty("jboss.server.log.dir");
      File logFile = new File(logDir, "cl-test.log");
      if( logFile.exists() == false )
         throw new ServletException(logFile+" does not exist");

      long length = logFile.length();
      log.info("Current length = "+length);
      for(int n = 0; n < 100; n ++)
         log.info("Msg #"+n);
      long lastModified = logFile.lastModified();
      long length2 = logFile.length();
      if( !(length2 > length) )
         throw new ServletException(logFile+" length is not increasing");
      response.setContentType("text/html");
      PrintWriter pw = response.getWriter();
      pw.println("<html><head><title>Commons logging test servlet</title></head>");
      pw.println("<body><h1>Commons logging test servlet</h1>");
      pw.println("Log length: "+length2);
      pw.println(", LastModified: "+new Date(lastModified));
      pw.println("</body></html>");
      pw.flush();
   }
}
