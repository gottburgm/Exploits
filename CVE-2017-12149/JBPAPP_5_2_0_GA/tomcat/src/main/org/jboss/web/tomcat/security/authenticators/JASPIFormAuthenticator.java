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

import java.io.IOException;
import java.security.Principal;

import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Session;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.authenticator.FormAuthenticator;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.jboss.logging.Logger;
import org.jboss.web.tomcat.security.ExtendedRealm;

//$Id: JASPIFormAuthenticator.java 81037 2008-11-14 13:40:33Z dimitris@jboss.org $

/**
 *  Form Authenticator that delegates to an extended Realm
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  May 24, 2006 
 *  @version $Revision: 81037 $
 */
public class JASPIFormAuthenticator extends FormAuthenticator
{ 
   private static Logger log = Logger.getLogger(JASPIFormAuthenticator.class);
   
   public JASPIFormAuthenticator()
   { 
   }

   /**
    * @see FormAuthenticator#authenticate(org.apache.catalina.connector.Request, 
    *    org.apache.catalina.connector.Response, org.apache.catalina.deploy.LoginConfig)
    */
   public boolean authenticate(Request request, Response response, 
         LoginConfig config) throws IOException
   {
      //References to objects we will need later
      Session session = null;

      // Have we already authenticated someone?
      Principal principal = request.getUserPrincipal();
      String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);
      if (principal != null) {
          if (log.isDebugEnabled())
              log.debug("Already authenticated '" +
                  principal.getName() + "'");
          // Associate the session with any existing SSO session
          if (ssoId != null)
              associate(ssoId, request.getSessionInternal(true));
          return (true);
      }

      // Is there an SSO session against which we can try to reauthenticate?
      if (ssoId != null) {
          if (log.isDebugEnabled())
              log.debug("SSO Id " + ssoId + " set; attempting " +
                        "reauthentication");
          // Try to reauthenticate using data cached by SSO.  If this fails,
          // either the original SSO logon was of DIGEST or SSL (which
          // we can't reauthenticate ourselves because there is no
          // cached username and password), or the realm denied
          // the user's reauthentication for some reason.
          // In either case we have to prompt the user for a logon */
          if (reauthenticateFromSSO(ssoId, request))
              return true;
      }

      // Have we authenticated this user before but have caching disabled?
      if (!cache) {
          session = request.getSessionInternal(true);
          if (log.isDebugEnabled())
              log.debug("Checking for reauthenticate in session " + session);
          String username =
              (String) session.getNote(Constants.SESS_USERNAME_NOTE);
          String password =
              (String) session.getNote(Constants.SESS_PASSWORD_NOTE);
          if ((username != null) && (password != null)) {
              if (log.isDebugEnabled())
                  log.debug("Reauthenticating username '" + username + "'");
             // principal =
                 // context.getRealm().authenticate(username, password); 
              ExtendedRealm realm = (ExtendedRealm)context.getRealm();
              try
              {
                 principal = realm.authenticate(request, response, config);
              }
              catch(Exception e)
              {
                 log.error("Exception in realm authenticate:",e);
              }
              if (principal != null) {
                  session.setNote(Constants.FORM_PRINCIPAL_NOTE, principal);
                  if (!matchRequest(request)) {
                      register(request, response, principal,
                               Constants.FORM_METHOD,
                               username, password);
                      return (true);
                  }
              }
              if (log.isDebugEnabled())
                  log.debug("Reauthentication failed, proceed normally");
          }
      }

      // Is this the re-submit of the original request URI after successful
      // authentication?  If so, forward the *original* request instead.
      if (matchRequest(request)) {
          session = request.getSessionInternal(true);
          if (log.isDebugEnabled())
              log.debug("Restore request from session '"
                        + session.getIdInternal() 
                        + "'");
          principal = (Principal)
              session.getNote(Constants.FORM_PRINCIPAL_NOTE);
          register(request, response, principal, Constants.FORM_METHOD,
                   (String) session.getNote(Constants.SESS_USERNAME_NOTE),
                   (String) session.getNote(Constants.SESS_PASSWORD_NOTE));
          // If we're caching principals we no longer need the username
          // and password in the session, so remove them
          if (cache) {
              session.removeNote(Constants.SESS_USERNAME_NOTE);
              session.removeNote(Constants.SESS_PASSWORD_NOTE);
          }
          if (restoreRequest(request, session)) {
              if (log.isDebugEnabled())
                  log.debug("Proceed to restored request");
              return (true);
          } else {
              if (log.isDebugEnabled())
                  log.debug("Restore of original request failed");
              response.sendError(HttpServletResponse.SC_BAD_REQUEST);
              return (false);
          }
      }

      // Acquire references to objects we will need to evaluate
      MessageBytes uriMB = MessageBytes.newInstance();
      CharChunk uriCC = uriMB.getCharChunk();
      uriCC.setLimit(-1);
      String contextPath = request.getContextPath();
      String requestURI = request.getDecodedRequestURI();
      response.setContext(request.getContext());

      // Is this the action request from the login page?
      boolean loginAction =
          requestURI.startsWith(contextPath) &&
          requestURI.endsWith(Constants.FORM_ACTION);

      // No -- Save this request and redirect to the form login page
      if (!loginAction) {
          session = request.getSessionInternal(true);
          if (log.isDebugEnabled())
              log.debug("Save request in session '" + session.getIdInternal() + "'");
          try {
              saveRequest(request, session);
          } catch (IOException ioe) {
              log.debug("Request body too big to save during authentication");
              response.sendError(HttpServletResponse.SC_FORBIDDEN,
                      sm.getString("authenticator.requestBodyTooBig"));
              return (false);
          }
          forwardToLoginPage(request, response, config);
          return (false);
      }

      // Yes -- Validate the specified credentials and redirect
      // to the error page if they are not correct
      ExtendedRealm realm = (ExtendedRealm)context.getRealm();
      if (characterEncoding != null) {
          request.setCharacterEncoding(characterEncoding);
      }
      String username = request.getParameter(Constants.FORM_USERNAME);
      String password = request.getParameter(Constants.FORM_PASSWORD);
      if (log.isDebugEnabled())
          log.debug("Authenticating username '" + username + "'");
      //principal = realm.authenticate(username, password);
      try
      {
         principal = realm.authenticate(request, response, config);
      }
      catch(Exception e)
      {
         log.error("Exception in realm authenticate:",e);
      }
      
      if (principal == null) {
          forwardToErrorPage(request, response, config);
          return (false);
      }

      if (log.isDebugEnabled())
          log.debug("Authentication of '" + username + "' was successful");

      if (session == null)
          session = request.getSessionInternal(false);
      if (session == null) {
          if (containerLog.isDebugEnabled())
              containerLog.debug
                  ("User took so long to log on the session expired");
          response.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT,
                             sm.getString("authenticator.sessionExpired"));
          return (false);
      }

      // Save the authenticated Principal in our session
      session.setNote(Constants.FORM_PRINCIPAL_NOTE, principal);

      // Save the username and password as well
      session.setNote(Constants.SESS_USERNAME_NOTE, username);
      session.setNote(Constants.SESS_PASSWORD_NOTE, password);

      // Redirect the user to the original request URI (which will cause
      // the original request to be restored)
      requestURI = savedRequestURL(session);
      if (log.isDebugEnabled())
          log.debug("Redirecting to original '" + requestURI + "'");
      if (requestURI == null)
          response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                             sm.getString("authenticator.formlogin"));
      else
          response.sendRedirect(response.encodeRedirectURL(requestURI));
      return (false);
   }  
}
