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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLAuthenticationQuery;
import org.opensaml.SAMLAuthenticationStatement;
import org.opensaml.SAMLException;
import org.opensaml.SAMLNameIdentifier;
import org.opensaml.SAMLRequest;
import org.opensaml.SAMLResponse;
import org.opensaml.SAMLSubject;
import org.opensaml.provider.SecureRandomIDProvider;

//$Id: JBossSingleSignOnProcessor.java 81038 2008-11-14 13:43:27Z dimitris@jboss.org $

/**
 *  OpenSAML v1.1 based implementation
 *  @author <a href="mailto:Sohil.Shah@jboss.org">Sohil Shah</a>
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Apr 10, 2006 
 *  @version $Revision: 81038 $
 */
public class JBossSingleSignOnProcessor implements SingleSignOnProcessor
{
   private SecureRandomIDProvider idProvider = new SecureRandomIDProvider();
   
   private static final String LOGIN_FAILED="login_failed";
   
   /**
    * @see SingleSignOnProcessor#generateAuthRequest(String, String)
    */
   public String generateAuthRequest(String username, String password) 
   throws SSOException
   { 
      if(username == null || username.length() == 0)
         throw new IllegalArgumentException("username is null or zero-length");
      if(password == null)
         throw new IllegalArgumentException("password is null");
      try
      {
         String request = null;
         
         //create a SAMLSubject
         SAMLNameIdentifier id = new SAMLNameIdentifier();
         id.setName(username);
         id.setNameQualifier(password);
         id.setFormat(SAMLNameIdentifier.FORMAT_UNSPECIFIED);
         SAMLSubject subject = new SAMLSubject();
         subject.setNameIdentifier(id);      
         SAMLAuthenticationQuery query = new SAMLAuthenticationQuery(subject,
               SAMLAuthenticationStatement.AuthenticationMethod_Password);
         
         SAMLRequest authRequest = new SAMLRequest(query);
         request = authRequest.toString();
         
         return request;
      }
      catch(SAMLException sme)
      {
         throw new SSOException(sme);
      }
   }
   
   /**
    * @see SingleSignOnProcessor#generateAuthResponse(String, String, boolean)
    */
   public String generateAuthResponse(String assertingParty, String username, 
         boolean success) throws SSOException
   { 
      if(assertingParty == null || assertingParty.length() == 0)
         throw new IllegalArgumentException("assertingParty is null or zero-length");
      if(username == null || username.length() == 0)
         throw new IllegalArgumentException("username is null or zero-length");
      try
      {
         String response = null;
         
         //construct the SAML Response
         SAMLResponse authResponse = new SAMLResponse();
         authResponse.setId(this.idProvider.getIdentifier());
         
         if(success)
         {
            //create a successfull authenticationstatment
            SAMLNameIdentifier id = new SAMLNameIdentifier();
            id.setName(username);
            id.setFormat(SAMLNameIdentifier.FORMAT_UNSPECIFIED);
            SAMLSubject subject = new SAMLSubject();
            subject.setNameIdentifier(id);
            
            String methodStr = SAMLAuthenticationStatement.AuthenticationMethod_Password;
            SAMLAuthenticationStatement authStatement = new SAMLAuthenticationStatement();
            
            authStatement.setAuthMethod(methodStr);
            authStatement.setSubject(subject);
            authStatement.setAuthInstant(new Date());
            
            //create an assertion 
            SAMLAssertion authAssertion = new SAMLAssertion();
            authAssertion.setId(this.idProvider.getIdentifier());
            authAssertion.setIssuer(assertingParty);
            authAssertion.addStatement(authStatement);
            
            //create the SAMLResponse            
            authResponse.addAssertion(authAssertion);
         }
         else
         {
            SAMLException loginFailed = new SAMLException(LOGIN_FAILED);
            authResponse.setStatus(loginFailed);
         }
         
         response = authResponse.toString();
         
         return response;
      }
      catch(SAMLException sme)
      {
         throw new SSOException(sme);
      }
   }
   
   /**
    * @see SingleSignOnProcessor#parseAuthRequest(String)
    */
   public SSOUser parseAuthRequest(String request) throws SSOException
   {  
      if(request == null || request.length() == 0)
         throw new IllegalArgumentException("request is null or zero-length");
      ByteArrayInputStream bis = null;
      try
      {
         SSOUser user = null;
         
         bis = new ByteArrayInputStream(request.getBytes());
         SAMLRequest authRequest = new SAMLRequest(bis);
         
         
         SAMLAuthenticationQuery query = (SAMLAuthenticationQuery)authRequest.getQuery();
         SAMLSubject subject = query.getSubject();
         
         //get the SAMLNameIdentifier
         SAMLNameIdentifier id = subject.getNameIdentifier();
         String username = id.getName();
         String password = id.getNameQualifier();
         
         user = new SSOUser(username,password);
         
         return user;
      } 
      catch(SAMLException sme)
      {
         throw new SSOException(sme);
      }
      finally
      {
         if(bis!=null)
         {
            try{bis.close();}catch(IOException e){}
         }
      }
   }
   
   /**
    * @see JBossSingleSignOnProcessor#parseAuthResponse(String)
    */
   public AuthResponse parseAuthResponse(String resp) throws SSOException
   { 
      if(resp == null || resp.length() == 0)
         throw new IllegalArgumentException("response is null or zero-length");
      AuthResponse authResponse = null;
      ByteArrayInputStream bis = null;
      boolean success = false;
      String assertToken = null;
      String assertingParty = null;
      String username = null;
      try
      {                        
          bis = new ByteArrayInputStream(resp.getBytes());
          SAMLResponse response = new SAMLResponse(bis);
          
          Iterator assertions = response.getAssertions();
          if(assertions!=null && assertions.hasNext())
          {
              success = true;
              SAMLAssertion authAssertion = (SAMLAssertion)assertions.next();
              assertToken = authAssertion.getId();
              assertingParty = authAssertion.getIssuer();
              SAMLAuthenticationStatement authStatement = (SAMLAuthenticationStatement)authAssertion.getStatements().next();
              username = authStatement.getSubject().getNameIdentifier().getName();
              
              SSOUser user = new SSOUser(username,null);
              authResponse = new AuthResponse(assertingParty,assertToken,user,success);
          }            
          
          return authResponse;
      }
      catch(SAMLException sme)
      {
          if(sme.getMessage().equals(LOGIN_FAILED))
          {
              success = false;
              authResponse = new AuthResponse(assertingParty,assertToken,null,success);
              return authResponse;
          }
          else
          {
              throw new SSOException(sme);
          }
      }
      finally
      {
          if(bis!=null)
          {
              try{bis.close();}catch(Exception e){}
          }
      }
   } 
}
