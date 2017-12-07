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
package org.jboss.test.web.security.authenticators;

import java.io.IOException;
import java.security.Principal;

import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.jboss.logging.Logger;
import javax.servlet.http.HttpServletResponse;

//$Id: HeaderAuthenticator.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/**
 *  Test Authenticator that can authenticate based on headers.
 *  username = JBOSS_TEST_USER_NAME
 *  credential = JBOSS_TEST_CREDENTIAL
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Mar 6, 2006 
 *  @version $Revision: 81036 $
 */
public class HeaderAuthenticator extends AuthenticatorBase
{ 
   private static Logger log = Logger.getLogger(HeaderAuthenticator.class);
   
   /**
    * Create a new HeaderAuthenticator. 
    */
   public HeaderAuthenticator()
   {
      super(); 
   }

   /**
    * Authenticate the user making this request, based on the specified
    * login configuration.  Return <code>true</code> if any specified
    * constraint has been satisfied, or <code>false</code> if we have
    * created a response challenge already.
    *
    * @param request Request we are processing
    * @param response Response we are creating
    * @param config    Login configuration describing how authentication
    *              should be performed
    *
    * @exception IOException if an input/output error occurs
    */
   protected boolean authenticate(Request request,
                                           Response response,
                                           LoginConfig config)
       throws IOException
   {  
      Realm realm = context.getRealm();
      /**
       * You can get the userid/credential from the header
       */
      Session session = request.getSessionInternal(true);
      String username = request.getHeader("JBOSS_TEST_USER_NAME");
      String password = request.getHeader("JBOSS_TEST_CREDENTIAL");
      log.debug("Test UserName =" + username);
      log.debug("Test cred present?:" + (password != null));
      Principal principal = realm.authenticate(username,password);
      if(principal == null)
      {
         response.sendError(HttpServletResponse.SC_FORBIDDEN);
         return false;
      }
         
      //Save the authenticated Principal in our session
      session.setNote(Constants.SESS_USERNAME_NOTE, principal);
      request.setUserPrincipal(principal);
      return true;
   }

}
