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
package org.jboss.web.tomcat.security.jaspi;

import java.io.IOException;
import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.PasswordValidationCallback;
import javax.servlet.http.Cookie;

import org.apache.catalina.Session;
import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.jboss.logging.Logger;
import org.jboss.security.ServerAuthenticationManager;
import org.jboss.security.auth.message.GenericMessageInfo;
import org.jboss.security.plugins.auth.JASPIServerAuthenticationManager;

/**
 * Tomcat authenticator that does JSR-196 (JASPI) authentication
 * @author Anil.Saldhana@redhat.com
 * @since Oct 7, 2008
 */
public class TomcatJASPIAuthenticator extends AuthenticatorBase
{
   private static Logger log = Logger.getLogger(TomcatJASPIAuthenticator.class);
   
   private String messageLayer = "HttpServlet";
   
   protected String serverAuthenticationManagerClass = JASPIServerAuthenticationManager.class.getName();
 
   @Override
   protected boolean authenticate(Request request, Response response, LoginConfig config) throws IOException
   { 
      boolean result = false;
      
      String authMethod = config.getAuthMethod(); 

      // Have we already authenticated someone?
      Principal principal = request.getUserPrincipal();
      String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);
      if (principal != null) {
         log.trace("Already authenticated '" + principal.getName() + "'");
         // Associate the session with any existing SSO session
         if (ssoId != null)
            associate(ssoId, request.getSessionInternal(true));
         return (true);
      }

      if("BASIC".equalsIgnoreCase(authMethod) ||
            "FORM".equalsIgnoreCase(authMethod) )
      {
         // Is there an SSO session against which we can try to reauthenticate?
         if (ssoId != null) {
            log.trace("SSO Id " + ssoId + " set; attempting " +
               "reauthentication");
            /* Try to reauthenticate using data cached by SSO.  If this fails,
                either the original SSO logon was of DIGEST or SSL (which
                we can't reauthenticate ourselves because there is no
                cached username and password), or the realm denied
                the user's reauthentication for some reason.
                In either case we have to prompt the user for a logon */
            if (reauthenticateFromSSO(ssoId, request))
               return true;
         }
      }      

      GenericMessageInfo messageInfo = new GenericMessageInfo();
      messageInfo.setRequestMessage(request);
      messageInfo.setResponseMessage(response);
      
      //Put bits of information needed by tomcat server auth modules
      messageInfo.getMap().put("CACHE", cache); 
      
      TomcatJASPICallbackHandler cbh = new TomcatJASPICallbackHandler();
      
      ServerAuthenticationManager sam = getServerAuthenticationManager();
      if(sam != null)
      {
         result = sam.isValid(messageInfo, new Subject(), messageLayer, cbh);
      } 
      
      //The Authentication process has been a success. We need to register
      //the principal, username, password with the container
      if(result)
      {
         PasswordValidationCallback pvc = cbh.getPasswordValidationCallback();
         CallerPrincipalCallback cpcb = cbh.getCallerPrincipalCallback();
         this.register(request, response, cpcb.getPrincipal(), authMethod, 
               pvc.getUsername(), new String(pvc.getPassword()));
      }
      
      return result; 
   }

   /**
    * Get the FQN of the class that implements
    * the org.jboss.security.ServerAuthenticationManager intepasswordrface
    * @return
    */
   public String getServerAuthenticationManagerClass()
   {
      return serverAuthenticationManagerClass;
   }

   /**
    * Set the FQN of the class that implements
    * the org.jboss.security.ServerAuthenticationManager interface
    * @param serverAuthenticationManagerClass
    */
   public void setServerAuthenticationManagerClass(String serverAuthenticationManagerClass)
   {
      this.serverAuthenticationManagerClass = serverAuthenticationManagerClass;
   } 
   
   protected ServerAuthenticationManager getServerAuthenticationManager()
   {
      ServerAuthenticationManager sam = null;
      Class<?> clazz;
      try
      {
         clazz = SecurityActions.loadClass(serverAuthenticationManagerClass);
         sam = (ServerAuthenticationManager) clazz.newInstance();
      }
      catch (Exception e)
      {
         log.error("Exception in obtaining ServerAuthenticationManager:", e);
      } 
      
      return sam;
   }
   
   /**
    * Register an authenticated Principal and authentication type in our
    * request, in the current session (if there is one), and with our
    * SingleSignOn valve, if there is one.  Set the appropriate cookie
    * to be returned.
    *
    * @param request The servlet request we are processing
    * @param response The servlet response we are generating
    * @param principal The authenticated Principal to be registered
    * @param authType The authentication type to be registered
    * @param username Username used to authenticate (if any)
    * @param password Password used to authenticate (if any)
    */
   protected void register(Request request, Response response,
                           Principal principal, String authType,
                           String username, String password) {

       if (log.isTraceEnabled()) {
           // Bugzilla 39255: http://issues.apache.org/bugzilla/show_bug.cgi?id=39255
           String name = (principal == null) ? "none" : principal.getName();
           log.trace("Authenticated '" + name + "' with type '"
               + authType + "'");
       }

       // Cache the authentication information in our request
       request.setAuthType(authType);
       request.setUserPrincipal(principal);

       Session session = request.getSessionInternal(false);
       // Cache the authentication information in our session, if any
       if (cache) {
           if (session != null) {
               session.setAuthType(authType);
               session.setPrincipal(principal);
               if (username != null)
                   session.setNote(Constants.SESS_USERNAME_NOTE, username);
               else
                   session.removeNote(Constants.SESS_USERNAME_NOTE);
               if (password != null)
                   session.setNote(Constants.SESS_PASSWORD_NOTE, password);
               else
                   session.removeNote(Constants.SESS_PASSWORD_NOTE);
           }
       }

       // Construct a cookie to be returned to the client
       if (sso == null)
           return;

       // Only create a new SSO entry if the SSO did not already set a note
       // for an existing entry (as it would do with subsequent requests
       // for DIGEST and SSL authenticated contexts)
       String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);
       if (ssoId == null) {
           // Construct a cookie to be returned to the client
           ssoId = generateSessionId();
           Cookie cookie = new Cookie(Constants.SINGLE_SIGN_ON_COOKIE, ssoId);
           cookie.setMaxAge(-1);
           cookie.setPath("/");
           
           // Bugzilla 41217
           cookie.setSecure(request.isSecure());
           
           // Bugzilla 34724
           String ssoDomain = sso.getCookieDomain();
           if(ssoDomain != null) {
               cookie.setDomain(ssoDomain);
           }

           response.addCookie(cookie);

           // Register this principal with our SSO valve
           sso.register(ssoId, principal, authType, username, password);
           request.setNote(Constants.REQ_SSOID_NOTE, ssoId);

       } else {
           // Update the SSO session with the latest authentication data
           sso.update(ssoId, principal, authType, username, password);
       }

       // Fix for Bug 10040
       // Always associate a session with a new SSO reqistration.
       // SSO entries are only removed from the SSO registry map when
       // associated sessions are destroyed; if a new SSO entry is created
       // above for this request and the user never revisits the context, the
       // SSO entry will never be cleared if we don't associate the session
       if (session == null)
           session = request.getSessionInternal(true);
       sso.associate(ssoId, session); 
   }
}