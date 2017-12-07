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
package org.jboss.web.tomcat.security.login;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.naming.NamingException;
import javax.servlet.http.Cookie;

import org.apache.catalina.Container;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Session;
import org.apache.catalina.Valve;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.authenticator.SingleSignOn;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.jboss.web.tomcat.service.request.ActiveRequestResponseCacheValve;

//$Id: WebAuthentication.java 112773 2012-03-23 21:19:38Z dehort $

/**
 *  JBAS-4077: Programmatic Web Login
 *  @author Anil.Saldhana@redhat.com
 *  @since  Mar 12, 2007 
 *  @version $Revision: 112773 $
 */
public class WebAuthentication
{
   public static final String AUTH_TYPE = "PROGRAMMATIC_WEB_LOGIN";

   public WebAuthentication()
   {
   }

   /**
    * Login an user via the CLIENT-CERT method
    * @param certs X509 certificates
    * @return Authenticated User Principal
    */
   public boolean login(X509Certificate[] certs)
   {
      //Get the active request
      Request request = ActiveRequestResponseCacheValve.activeRequest.get();
      if (request == null)
         throw new IllegalStateException("request is null");
      Principal p = request.getContext().getRealm().authenticate(certs);
      if (p != null)
      {
         register(request, p, null, null);
      }
      return p != null;
   }

   /**
    * Login an user via the BASIC, FORM, DIGEST methods
    * @param username
    * @param credential
    * @return
    * @throws NamingException
    */
   public boolean login(String username, Object credential)
   {
      //Get the active request
      Request request = ActiveRequestResponseCacheValve.activeRequest.get();
      if (request == null)
         throw new IllegalStateException("request is null");

      Principal p = null;
      if (credential instanceof String)
      {
         p = request.getContext().getRealm().authenticate(username, (String) credential);
      }
      else if (credential instanceof byte[])
      {
         p = request.getContext().getRealm().authenticate(username, (byte[]) credential);
      }
      if (p != null)
      {
         register(request, p, username, credential);
      }
      return p != null;
   }

   /**
    * Log the user out
    *
    */
   public void logout()
   {
      //Get the active request
      Request request = ActiveRequestResponseCacheValve.activeRequest.get();
      if (request == null)
         throw new IllegalStateException("request is null");
      unregister(request);
   }

   /**
    * Register the principal with the request, session etc just the way AuthenticatorBase does
    * @param request Catalina Request
    * @param principal User Principal generated via authentication
    * @param username username passed by the user (null for client-cert)
    * @param credential Password (null for client-cert and digest)
    */
   protected void register(Request request, Principal principal, String username, Object password)
   {
      request.setAuthType(AUTH_TYPE);
      request.setUserPrincipal(principal);

      //Cache the authentication principal in the session
      Session session = request.getSessionInternal(false);
      if (session != null)
      {
         session.setAuthType(AUTH_TYPE);
         session.setPrincipal(principal);
         if (username != null)
            session.setNote(Constants.SESS_USERNAME_NOTE, username);
         else
            session.removeNote(Constants.SESS_USERNAME_NOTE);
         if (password != null)
            session.setNote(Constants.SESS_PASSWORD_NOTE, getPasswordAsString(password));
         else
            session.removeNote(Constants.SESS_PASSWORD_NOTE);
      }

      // JBAS-4424: Programmatic web authentication with SSO
      SingleSignOn sso = this.getSingleSignOn(request);
      if (sso == null)
         return;

      // Only create a new SSO entry if the SSO did not already set a note
      // for an existing entry (as it would do with subsequent requests
      // for DIGEST and SSL authenticated contexts)
      String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);
      if (ssoId == null)
      {
         // Construct a cookie to be returned to the client
         ssoId = generateSessionId();
         Cookie cookie = new Cookie(Constants.SINGLE_SIGN_ON_COOKIE, ssoId);
         cookie.setMaxAge(-1);
         cookie.setPath("/");

         // Bugzilla 41217
         cookie.setSecure(request.isSecure());

         // Bugzilla 34724
         String ssoDomain = sso.getCookieDomain();
         if (ssoDomain != null)
         {
            cookie.setDomain(ssoDomain);
         }

         Response response = ActiveRequestResponseCacheValve.activeResponse.get();
         response.addCookie(cookie);

         // Register this principal with our SSO valve
         sso.register(ssoId, principal, AUTH_TYPE, username, this.getPasswordAsString(password));
         request.setNote(Constants.REQ_SSOID_NOTE, ssoId);

      }
      else
      {
         // Update the SSO session with the latest authentication data
         sso.update(ssoId, principal, AUTH_TYPE, username, this.getPasswordAsString(password));
      }

      // Always associate a session with a new SSO reqistration.
      // SSO entries are only removed from the SSO registry map when
      // associated sessions are destroyed; if a new SSO entry is created
      // above for this request and the user never revisits the context, the
      // SSO entry will never be cleared if we don't associate the session
      if (session == null)
         session = request.getSessionInternal(true);
      sso.associate(ssoId, session);
   }

   /**
    * Log the user out
    * @param request
    */
   protected void unregister(Request request)
   {
      request.setAuthType(null);
      request.setUserPrincipal(null);

      // Cache the authentication principal in the session.
      Session session = request.getSessionInternal(false);
      if (session != null)
      {
         session.setAuthType(null);
         session.setPrincipal(null);
         session.removeNote(Constants.SESS_USERNAME_NOTE);
         session.removeNote(Constants.SESS_PASSWORD_NOTE);
      }
      // Unregister the SSOID.
      SingleSignOn sso = this.getSingleSignOn(request);
      if (sso != null)
      {
         String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);
         sso.deregister(ssoId);
      }
   }

   private String getPasswordAsString(Object cred)
   {
      String p = null;

      if (cred instanceof String)
      {
         p = (String) cred;
      }
      else if (cred instanceof byte[])
      {
         p = new String((byte[]) cred);
      }
      return p;
   }

   /**
    * <p>
    * Generate and return a new session identifier for the cookie that identifies an SSO principal.
    * </p>
    * 
    * @return a <code>String</code> representing the generated identifier.
    */
   private String generateSessionId()
   {
      UUID uid = UUID.randomUUID();
      String higherBits = Long.toHexString(uid.getMostSignificantBits());
      String lowerBits = Long.toHexString(uid.getLeastSignificantBits());

      return (higherBits + lowerBits).toUpperCase();
   }

   /**
    * <p>
    * Obtain a reference to the <code>SingleSignOn</code> valve, if one was configured.
    * </p>
    * 
    * @param request    the <code>Request</code> object used to look up the SSO valve.
    * @return   a reference to the <code>SingleSignOn</code> valve, or <code>null</code> if no SSO valve
    * has been configured.
    */
   private SingleSignOn getSingleSignOn(Request request)
   {
      SingleSignOn sso = null;
      Container parent = request.getContext().getParent();
      while ((sso == null) && (parent != null))
      {
         if (!(parent instanceof Pipeline))
         {
            parent = parent.getParent();
            continue;
         }
         Valve valves[] = ((Pipeline) parent).getValves();
         for (int i = 0; i < valves.length; i++)
         {
            if (valves[i] instanceof SingleSignOn)
            {
               sso = (SingleSignOn) valves[i];
               break;
            }
         }
         if (sso == null)
            parent = parent.getParent();
      }
      return sso;
   }
}
