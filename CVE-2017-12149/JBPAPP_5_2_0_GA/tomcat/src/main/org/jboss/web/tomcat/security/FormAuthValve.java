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
package org.jboss.web.tomcat.security;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.jboss.logging.Logger;

/** A valve that associates the j_username with the session under the attribute
 * name j_username for use by form login/error pages. If the includePassword
 * attribute is true, the j_password value is also included in the session
 * under the attribute name j_password. In addition, it maps any
 * authentication exception found in the SecurityAssociation to the session
 * attribute name j_exception.
 *  
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81037 $
 */
public class FormAuthValve
   extends ValveBase
{
   private static Logger log = Logger.getLogger(FormAuthValve.class);
   private static boolean trace = log.isTraceEnabled();
   private boolean includePassword;

   public boolean isIncludePassword()
   {
      return includePassword;
   }
   public void setIncludePassword(boolean includePassword)
   {
      this.includePassword = includePassword;
   }

   public void invoke(Request request, Response response)
      throws IOException, ServletException
   {
      String username = request.getParameter("j_username");
      HttpSession session = request.getSession(false);
      if( trace )
         log.trace("Enter, j_username="+username);
      if( session != null )
      {
         if( username != null )
            session.setAttribute("j_username", username);
         if( includePassword )
         {
            Object pass = request.getParameter("j_password");
            if( pass != null )
               session.setAttribute("j_password", pass);
         }
      }

      getNext().invoke(request, response);

      username = request.getParameter("j_username");
      session = request.getSession(false);
      if( session != null )
      {
         if( trace )
           log.trace("SessionID: "+session.getId());
         if( username != null )
            session.setAttribute("j_username", username);
         // Check the SecurityAssociation context exception
         Throwable t = (Throwable) SecurityAssociationActions.getAuthException();
         if( trace )
           log.trace("SecurityAssociation.exception: "+t);
         if( t != null )
            session.setAttribute("j_exception", t);
      }
      if( trace )
         log.trace("Exit, username: "+username);
   }
}
