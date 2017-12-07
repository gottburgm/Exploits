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
package org.jboss.embedded.tomcat.security;

import java.io.IOException;
import java.security.Principal;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.apache.catalina.Container;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

/**
 * comment
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class SecurityAssociationValve extends ValveBase
{
   public SecurityAssociationValve()
   {
   }

   public SecurityAssociationValve(Container container)
   {
      this.setContainer(container);
   }

   public void invoke(Request request, Response response) throws IOException, ServletException
   {
      Principal caller = request.getPrincipal();
      System.out.println("******* caller principal: " + caller);

      Principal userPrincipal = request.getUserPrincipal();
      System.out.println("******* user principal: " + userPrincipal);

      HttpSession hsession = request.getSession(false);
      Session session = null;

      // If there is a session, get the tomcat session for the principal
      Manager manager = container.getManager();
      if (manager != null && hsession != null)
      {
         try
         {
            session = manager.findSession(hsession.getId());
         }
         catch (IOException ignore)
         {
         }
      }

      if (session != null)
      {
         System.out.println("***** principal from session: " + session.getPrincipal());
      }
      else
      {
         System.out.println("Session was null");
      }
      Wrapper servlet = request.getWrapper();
      System.out.println("RUNAS: " + servlet.getRunAs());
      if (servlet.getRealm() == null)
         System.out.println("Servlet realm was null");
      getNext().invoke(request, response);
   }
}
