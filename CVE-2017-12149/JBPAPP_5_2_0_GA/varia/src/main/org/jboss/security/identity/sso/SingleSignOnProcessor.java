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
package org.jboss.security.identity.sso;

//$Id: SingleSignOnProcessor.java 81038 2008-11-14 13:43:27Z dimitris@jboss.org $

/**
 *  Interface for SAML based SSO processor
 *  @author <a href="mailto:Sohil.Shah@jboss.org">Sohil Shah</a>
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Apr 10, 2006 
 *  @version $Revision: 81038 $
 */
public interface SingleSignOnProcessor
{
   /**
    * SSO constants
    */
   String SSO_TOKEN = "token"; //name of the SSO domain level cookie
   String LOGOUT_TOKEN = "logoutToken"; //name of the logoutInProgress cookie
   String LOGOUT_DEST = "logoutDest"; //name of the logout destination cookie
   String SSO_USERNAME = "jboss_sso_username"; //username request attribute
   String SSO_PASSWORD = "jboss_sso_password"; //password request attribute
   String SSO_SESSION = "jboss_sso_session"; //SSOSession session attribute
   //authentication type of the SSOAuthenticator being plugged in 
   String SSO_AUTH_TYPE = "JBOSS-FEDERATED-SSO"; 
   /**
    * This method generates a SAML authentication request based on the supplied username and password
    * 
    * @param username
    * @param password
    * @return
    * @throws SSOException
    */
   public String generateAuthRequest(String username,String password) 
   throws SSOException; 
   
   /**
    * This method generates a SAML authentication response based on the supplied username, password, and the 
    * status of the authentication process
    * 
    * @param assertingParty
    * @param username
    * @param password
    * @param success
    * @return
    * @throws SSOException
    */
   public String generateAuthResponse(String assertingParty,String username,boolean success) 
   throws SSOException;
   
   /**
    * This method parses a SAML authentication request into a SSOUser domain object
    * 
    * @param request
    * @return
    * @throws SSOException
    */
   public SSOUser parseAuthRequest(String request) throws SSOException;
   
   /**
    * This method parses a SAML authentication response and produces an AuthResponse domain object
    * 
    * @param response
    * @return
    * @throws SSOException
    */
   public AuthResponse parseAuthResponse(String response)
   throws SSOException; 
}
