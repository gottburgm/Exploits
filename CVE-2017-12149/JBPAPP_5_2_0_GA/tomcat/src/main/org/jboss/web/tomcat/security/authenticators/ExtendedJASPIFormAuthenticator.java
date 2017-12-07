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
package org.jboss.web.tomcat.security.authenticators;

import javax.servlet.http.HttpSession;

import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.jboss.logging.Logger; 

//$Id: ExtendedJASPIFormAuthenticator.java 81037 2008-11-14 13:40:33Z dimitris@jboss.org $

/**
 * An extension of the form authenticator that associates the j_username with
 * the session under the attribute name j_username for use by form login/error
 * pages. If the includePassword attribute is true, the j_password value is
 * also included in the session under the attribute name j_password. In
 * addition, it maps any authentication exception found in the
 * SecurityAssociation to the session attribute name j_exception.
 * Based on the JASPIFormAuthenticator
 * 
 * @author Scott.Stark@jboss.org
 * @author Anil.Saldhana@jboss.org
 * @version $Revision: 81037 $
 */
public class ExtendedJASPIFormAuthenticator extends JASPIFormAuthenticator
{
   private static Logger log = Logger.getLogger(ExtendedJASPIFormAuthenticator.class);
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

   /**
    * Dispatch to the form error-page
    * 
    * @param request Request we are processing
    * @param response Response we are creating
    * @param config Login configuration describing how authentication should
    * be performed
    */
   protected void forwardToErrorPage(Request request, Response response,LoginConfig config)
   {
      if( trace )
         log.trace("forwardToErrorPage");
      populateSession(request);
      super.forwardToErrorPage(request, response,config);
      SecurityAssociationActions.clearAuthException();
   }

   /**
    * Dispatch to the form login-page
    * 
    * @param request Request we are processing
    * @param response Response we are creating
    * @param config Login configuration describing how authentication should
    * be performed
    */
   protected void forwardToLoginPage(LoginConfig config,
      Request request, Response response)
   {
      if( trace )
         log.trace("forwardToLoginPage");
      populateSession(request);
      super.forwardToLoginPage(request, response,config);
   }

   protected void populateSession(Request request)
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
