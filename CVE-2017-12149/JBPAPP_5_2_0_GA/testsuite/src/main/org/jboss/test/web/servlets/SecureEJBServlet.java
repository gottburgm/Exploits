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
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.test.web.interfaces.StatelessSession;
import org.jboss.test.web.interfaces.StatelessSessionHome;

/** 
 *
 * @author  Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class SecureEJBServlet extends HttpServlet
{
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        String echoMsg = null;
        boolean testPropagation = false;
        boolean includeHead = true;
        String param = request.getParameter("testPropagation");
        if( param != null )
            testPropagation = Boolean.valueOf(param).booleanValue();
        param = request.getParameter("includeHead");
        if( param != null )
            includeHead = Boolean.valueOf(param).booleanValue();

        try
        {
            InitialContext ctx = new InitialContext();
            StatelessSessionHome home = null;
            if( testPropagation == true )
            {
                home = (StatelessSessionHome) ctx.lookup("java:comp/env/ejb/UnsecuredEJB");
                StatelessSession bean = home.create();
                echoMsg = bean.forward("SecureEJBServlet called UnsecuredEJB.forward");
            }
            else
            {
                home = (StatelessSessionHome) ctx.lookup("java:comp/env/ejb/SecuredEJB");
                StatelessSession bean = home.create();
                echoMsg = bean.echo("SecureEJBServlet called SecuredEJB.echo");
            }
        }
        catch(Exception e)
        {
            throw new ServletException("Failed to call SecuredEJB.echo", e);
        }
        Principal user = request.getUserPrincipal();
        PrintWriter out = response.getWriter();
        if( includeHead == true )
        {
           response.setContentType("text/html");
           out.println("<html>");
           out.println("<head><title>ENCServlet</title></head><body>");
        }
        out.println("<h1>SecureServlet Accessed</h1>");
        out.println("<pre>You have accessed this servlet as user: "+user);
        out.println("You have accessed SecuredEJB as user: "+echoMsg);
        out.println("</pre>");
        if( includeHead == true )
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
