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
package org.jboss.test.cluster.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;

import org.jboss.logging.Logger;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class StatefulSessionServlet extends HttpServlet
{
   private static Logger log = Logger.getLogger(StatefulSessionServlet.class);
   private boolean synchSessionAccess;

   public void init(ServletConfig servletConfig) throws ServletException
   {
      super.init(servletConfig);
      String flag = servletConfig.getInitParameter("synchSessionAccess");
      synchSessionAccess = Boolean.valueOf(flag).booleanValue();
   }

   protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      handleRequest(request, response);
   }

   protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      handleRequest(request, response);
   }

   private void handleRequest(HttpServletRequest request, HttpServletResponse response)
      throws IOException
   {
      HttpSession session = request.getSession();
      log.info("handleRequest, session="+session.getId());
      SessionValue value = null;
      if( synchSessionAccess )
      {
         synchronized( session )
         {
            value = (SessionValue) session.getAttribute("TheSessionKey");
         }
      }
      else
      {
         value = (SessionValue) session.getAttribute("TheSessionKey");
      }

      SessionValue prevValue = null;
      if( value == null )
      {
         value = new SessionValue();
      }
      else
      {
         prevValue = new SessionValue();
         prevValue.accessCount = value.accessCount;
         prevValue.username = value.username;
         prevValue.lastAccessHost = value.lastAccessHost;
      }
      value.accessCount ++;
      value.username = request.getRemoteUser();
      value.lastAccessHost = request.getServerName();
      if( synchSessionAccess )
      {
         synchronized( session )
         {
            session.setAttribute("TheSessionKey", value);
         }
      }
      else
      {
         session.setAttribute("TheSessionKey", value);
      }
      log.info(value);

      PrintWriter pw = response.getWriter();
      response.setContentType("text/html");
      response.addIntHeader("X-AccessCount", value.accessCount);
      pw.write("<html>\n");
      String node = InetAddress.getLocalHost().getHostName();
      pw.write("<head><title>StatefulSessionServlet on: "+node+"</title></head>\n");
      pw.write("<body><h1>StatefulSessionServlet on: "+node+"</h1>\n");
      pw.write("<h2>SessionID: "+session.getId()+"</h2>\n");
      pw.write("<pre>\n");
      pw.write(value.toString());
      pw.write("\n</pre>\n");
      if( prevValue != null )
      {
         pw.write("<pre>\n");
         pw.write(prevValue.toString());
         pw.write("\n</pre>\n");         
      }
      pw.write("</body>\n");
      pw.write("</html>\n");
   }
}
