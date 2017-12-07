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
import java.security.Principal;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.test.web.interfaces.StatelessSession;
import org.jboss.test.web.interfaces.StatelessSessionHome;

/** A servlet that spawns a thread to perform a long running task that
interacts with a secure EJB.

@author  Scott.Stark@jboss.org
@version $Revision: 81036 $
*/
public class SecureEJBServletMT extends HttpServlet
{
   static org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(SecureEJBServletMT.class);
   
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        HttpSession session = request.getSession();
        Principal user = request.getUserPrincipal();
        Object result = session.getAttribute("request.result");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head><title>SecureEJBServletMT</title></head>");
        if( result == null )
            out.println("<meta http-equiv='refresh' content='5'>");
        out.println("<h1>SecureEJBServletMT Accessed</h1>");
        out.println("<body><pre>You have accessed this servlet as user: "+user);

        if( result == null )
        {
            Worker worker = new Worker(session);
            out.println("Started worker thread...");
            Thread t = new Thread(worker, "Worker");
            t.start();
        }
        else if( result instanceof Exception )
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            Exception e = (Exception) result;
            e.printStackTrace(pw);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, sw.toString());
        }
        else
        {
            out.println("Finished request, result = "+result);
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

    static class Worker implements Runnable
    {
        HttpSession session;
        Worker(HttpSession session)
        {
            this.session = session;
        }
        public void run()
        {
            try
            {
                log.debug("Worker, start: "+System.currentTimeMillis());
                Thread.currentThread().sleep(2500);
                InitialContext ctx = new InitialContext();
                StatelessSessionHome home = home = (StatelessSessionHome) ctx.lookup("java:comp/env/ejb/SecuredEJB");
                StatelessSession bean = home.create();
                String echoMsg = bean.echo("SecureEJBServlet called SecuredEJB.echo");
                session.setAttribute("request.result", echoMsg);
            }
            catch(Exception e)
            {
                session.setAttribute("request.result", e);
            }
            finally
            {
                log.debug("Worker, end: "+System.currentTimeMillis());
            }
        }
    }
}
