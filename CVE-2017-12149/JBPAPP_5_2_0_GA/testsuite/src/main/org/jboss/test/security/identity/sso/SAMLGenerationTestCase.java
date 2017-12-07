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
package org.jboss.test.security.identity.sso;

import org.jboss.security.identity.sso.AuthResponse;
import org.jboss.security.identity.sso.JBossSingleSignOnProcessor;
import org.jboss.security.identity.sso.SSOUser;
import org.jboss.security.identity.sso.SingleSignOnProcessor;
import org.jboss.test.JBossTestCase;

//$Id: SAMLGenerationTestCase.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/**
 *  Tests generation of saml by the thin library in the security module
 *  based on opensaml
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @author <a href="mailto:Sohil.Shah@jboss.org">Sohil Shah</a>
 *  @since  Apr 10, 2006 
 *  @version $Revision: 81036 $
 */
public class SAMLGenerationTestCase extends JBossTestCase
{ 
   /**
    * Better to be injected rather than a static class
    */
   private SingleSignOnProcessor processor = null;
   
   private static final String USERNAME = "saml_user";
   private static final String PASSWORD = "saml_pwd"; 
   private static final String ASSERTING_PARTY = "jboss";
   
   public SAMLGenerationTestCase(String name)
   {
      super(name); 
   }
   
   protected void setUp()
   {
      processor = new JBossSingleSignOnProcessor();
   }
   
   protected void tearDown()
   {
      processor = null;
   }
   
   public void testRequestGeneration() throws Exception
   {
      assertNotNull("processor != null");
      String request = processor.generateAuthRequest(USERNAME, PASSWORD); 
      assertNotNull("request != null", request); 
      SSOUser user = processor.parseAuthRequest(request);
      assertNotNull("user != null", user);
      assertTrue("user ==" + USERNAME, user.getUserName().equals(USERNAME));
      assertTrue("pwd ==" + PASSWORD, user.getPassword().equals(PASSWORD));
   } 
   
   public void testResponseGeneration() throws Exception
   {
      assertNotNull("processor != null");
      String response = processor.generateAuthResponse(ASSERTING_PARTY,USERNAME, true); 
      assertNotNull("response != null", response); 
      AuthResponse authResponse = processor.parseAuthResponse(response);
      assertNotNull("authResponse != null", authResponse);
      assertTrue("AP ==" + ASSERTING_PARTY,
            authResponse.getAssertingParty().equals(ASSERTING_PARTY));
      assertTrue(" USERNAME==" + USERNAME, 
            authResponse.getUser().getUserName().equals(USERNAME));
      assertNotNull("Asserting token != null", authResponse.getAssertToken()); 
      assertTrue("authenticated?", authResponse.isAuthenticated());
   } 
}
