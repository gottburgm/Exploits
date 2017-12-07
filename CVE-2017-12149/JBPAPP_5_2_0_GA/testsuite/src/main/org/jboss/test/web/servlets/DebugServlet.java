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
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Principal;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.test.web.util.Util;

/** A servlet that dumps out debugging information about its environment.
 * @author  Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class DebugServlet extends HttpServlet
{
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>ENCServlet</title></head>");
        out.println("<h1>Debug Accessed</h1>");
        out.println("<body>");
        out.println("<h2>Call Stack</h2>");
        out.println("<pre>");
        Throwable t = new Throwable("Trace");
        t.printStackTrace(out);
        out.println("</pre>");
        out.println("<h2>ClassLoaders</h2>");
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        out.println("<pre>");
        Util.dumpClassLoader(cl, out);
        out.println("</pre>");
        out.println("<h2>JNDI</h2>");
        out.println("<pre>");
        try
        {
            InitialContext iniCtx = new InitialContext();
            super.log("InitialContext.env: "+iniCtx.getEnvironment());
            out.println("InitialContext.env: "+iniCtx.getEnvironment());
            out.println("</pre><h3>java:comp</h3><pre>");
            Util.showTree(" ", (Context) iniCtx.lookup("java:comp"), out);
        }
        catch(Exception e)
        {
            super.log("Failed to create InitialContext", e);
            e.printStackTrace(out);
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
}
