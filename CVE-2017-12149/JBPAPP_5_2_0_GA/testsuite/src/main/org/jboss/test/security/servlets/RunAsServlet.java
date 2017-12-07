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
package org.jboss.test.security.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.Principal;
import java.util.HashSet;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import org.jboss.test.security.interfaces.RunAsServiceRemoteHome;
import org.jboss.test.security.interfaces.RunAsServiceRemote;
import org.jboss.test.security.interfaces.CallerInfo;
import org.jboss.security.SimplePrincipal;

/**
 * The target of the web constraints security tests
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class RunAsServlet extends HttpServlet
{
   protected void doRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      Principal caller = request.getUserPrincipal();

      InputStream is = request.getInputStream();
      ObjectInputStream ois = new ObjectInputStream(is);
      try
      {
         String method = (String) ois.readObject();
         if( method == null )
            throw new ServletException("No method parameter given");
         CallerInfo info = (CallerInfo) ois.readObject();
         InitialContext ctx = new InitialContext();
         Object obj = ctx.lookup("jacc/RunAs");
         obj = PortableRemoteObject.narrow(obj, RunAsServiceRemoteHome.class);
         RunAsServiceRemoteHome home = (RunAsServiceRemoteHome) obj;
         System.out.println("Found RunAsServiceRemoteHome");
         RunAsServiceRemote bean = home.create();
         System.out.println("Created RunAsServiceRemote");
         Class[] sig = {CallerInfo.class};
         Method methodCall = bean.getClass().getMethod(method, sig);
         Object[] args = {info};
         methodCall.invoke(bean, args);
         bean.remove();
      }
      catch(Exception e)
      {
         e.printStackTrace();
         throw new ServletException(e);
      }

      PrintWriter pw = response.getWriter();
      pw.write("<html>\n");
      pw.write("<br>Saw UserPrincipal: "+caller);
      pw.write("<br>PathInfo: "+request.getPathInfo());
      pw.write("</html>\n");
   }

   protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      doRequest(request, response);
   }

   protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      doRequest(request, response);
   }
}
