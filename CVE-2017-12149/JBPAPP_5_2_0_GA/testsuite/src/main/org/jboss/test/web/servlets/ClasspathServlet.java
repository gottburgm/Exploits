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
import java.io.StringWriter;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.AccessControlException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Enumeration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;

import org.jboss.test.web.util.EJBManifestClass;
import org.jboss.test.web.util.EarLibUser;
import org.jboss.test.util.Debug;

/** A servlet that accesses classes in WEB-INF/classes using Class.forName
 * during its initialization.
 *
 * @author  Scott.Scott@jboss.org
 * @version $Revision: 81036 $
 */
public class ClasspathServlet extends HttpServlet
{
   private static final long serialVersionUID = 1;

   private static Logger log = Logger.getLogger(ClasspathServlet.class);

   private StringBuffer initInfo = new StringBuffer();
   private boolean failOnError = true;

   public void init(ServletConfig config) throws ServletException
   {
      String param = config.getInitParameter("failOnError");

      if( param != null && Boolean.valueOf(param).booleanValue() == false )
         failOnError = false;
      log.info("init, failOnError="+failOnError);
      try
      {
         // Check for a
         Class clazz = Class.forName("org.jboss.test.web.util.ClassInClasses");
         initInfo.append("Successfully loaded class: "+clazz.getName());
         ClassLoader cl = clazz.getClassLoader();
         ProtectionDomain pd = clazz.getProtectionDomain();
         CodeSource cs = pd.getCodeSource();
         initInfo.append("\n  ClassLoader : "+cl.getClass().getName()+':'+cl.hashCode());
         initInfo.append("\n  CodeSource.location : "+cs.getLocation());

         // Load a resource bundle
         URL jbprops = cl.getResource("/org/jboss/resources/JBoss.properties");
         log.info("JBoss.properties: "+jbprops);
         ResourceBundle rb = ResourceBundle.getBundle("org.jboss.resources.JBoss");
         log.info("Found JBoss resources: "+rb);
         Enumeration keys = rb.getKeys();
         while( keys.hasMoreElements() )
            log.info(keys.nextElement());
      }
      catch(AccessControlException e)
      {
         log.error("Failed to init, ignoring security exception", e);         
      }
      catch(Exception e)
      {
         log.error("Failed to init", e);
         if( failOnError == true )
            throw new ServletException("Failed to init ClasspathServlet", e);
         else
         {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            initInfo.append("\nFailed to init\n");
            initInfo.append(sw.toString());
         }
      }
   }

   public void destroy()
   {
   }

   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println("<html>");
      out.println("<head><title>ClasspathServlet</title></head>");
      out.println("<body><h1>Initialization Info</h1>");
      out.println("<pre>\n");
      out.println(initInfo.toString());
      out.println("</pre>\n");
      try
      {
         out.println("<h1>EJBManifestClass Info</h1>");
         EJBManifestClass mfClass = new EJBManifestClass();
         StringBuffer results = new StringBuffer("EJBManifestClass Info:");
         Debug.displayClassInfo(mfClass.getClass(), results);
         out.println("<pre>");
         out.println(results.toString());
         out.println("</pre>");
      }
      catch(Exception e)
      {
         if( failOnError == true )
            throw new ServletException("Failed to load EJBManifestClass", e);
         else
         {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            out.println("<pre>");
            out.println(sw.toString());
            out.println("</pre>");
         }
      }

      try
      {
         out.println("<h1>EarLibUser Info</h1>");
         String info = EarLibUser.getClassInfo();
         out.println("<pre>");
         out.println(info);
         out.println("</pre>");
      }
      catch(Exception e)
      {
         if( failOnError == true )
            throw new ServletException("Failed to load EarLibUser", e);
         else
         {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            out.println("<pre>");
            out.println(sw.toString());
            out.println("</pre>");
         }
      }

      out.println("</html>");
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
