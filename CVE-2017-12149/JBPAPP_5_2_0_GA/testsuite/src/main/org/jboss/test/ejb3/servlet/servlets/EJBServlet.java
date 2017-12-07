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
package org.jboss.test.ejb3.servlet.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jboss.test.ejb3.servlet.Session30BusinessLocal;
import org.jboss.test.ejb3.servlet.Session30BusinessRemote;
import org.jboss.test.ejb3.servlet.Session30Home;
import org.jboss.test.ejb3.servlet.Session30Local;
import org.jboss.test.ejb3.servlet.Session30LocalHome;
import org.jboss.test.ejb3.servlet.Session30Remote;
import org.jboss.test.ejb3.servlet.StatelessLocal;
import org.jboss.test.ejb3.servlet.TestObject;
import org.jboss.test.ejb3.servlet.WarTestObject;

/** A servlet that accesses an EJB and tests whether the call argument
 is serialized.

 @author  Scott.Stark@jboss.org
 @version $Revision: 91045 $
 */
public class EJBServlet extends HttpServlet
{
   private static final Logger log = Logger.getLogger(EJBServlet.class);

   @EJB
   Session30BusinessRemote injectedSession;

   @EJB
   StatelessLocal injectedStateful;

   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
   {
      try
      {
         SecurityClient client = SecurityClientFactory.getSecurityClient();
         client.setSimple("somebody", "password");
         client.login();

         injectedSession.hello();
         injectedSession.goodbye();

         injectedStateful.hello();
         injectedStateful.goodbye();

         InitialContext ctx = new InitialContext();

         Session30BusinessRemote remote = (Session30BusinessRemote)ctx.lookup("ejb/Session30");

         remote.hello();
         remote.goodbye();

         TestObject o = new TestObject();
         remote.access(o);
         o = remote.createTestObject();

         Session30BusinessLocal local = (Session30BusinessLocal)ctx.lookup("ejb/Session30Local");
         o = new TestObject();
         local.access(o);
         o = local.createTestObject();

         WarTestObject warObject = (WarTestObject)local.getWarTestObject();

         Session30Home home = (Session30Home)ctx.lookup("ejb/Session30Home");
         Session30Remote remote21 = home.create();
         remote21.access(o);

         Session30LocalHome localHome = (Session30LocalHome)ctx.lookup("ejb/Session30LocalHome");
         Session30Local local21 = localHome.create();
         local21.access(o);

         home = (Session30Home)ctx.lookup("java:comp/env/ejb/remote/Session30");
         remote21 = home.create();
         remote21.access(o);

         localHome = (Session30LocalHome)ctx.lookup("java:comp/env/ejb/local/Session30");
         local21 = localHome.create();
         local21.access(o);
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new ServletException("Failed to call OptimizedEJB/Session30 through remote and local interfaces", e);
      }
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println("<html>");
      out.println("<head><title>EJBServlet</title></head>");
      out.println("<body>Tests passed<br></body>");
      out.println("</html>");
      out.close();
   }

   private void lookup(String name)
   {
      log.info("lookup " + name);
      try {
         InitialContext jndiContext = new InitialContext();
         NamingEnumeration names = jndiContext.list(name);
         if (names != null){
            while (names.hasMore()){
               log.info("  " + names.next());
            }
         }
      } catch (Exception e){
      }
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
