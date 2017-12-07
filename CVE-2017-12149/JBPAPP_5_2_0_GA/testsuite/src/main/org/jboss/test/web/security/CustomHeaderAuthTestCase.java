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
package org.jboss.test.web.security;

import java.net.HttpURLConnection;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient; 
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup; 

//$Id: CustomHeaderAuthTestCase.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

/**
 *  JBAS-2283: Custom Header based authentication
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Sep 11, 2006 
 *  @version $Revision: 85945 $
 */
public class CustomHeaderAuthTestCase extends JBossTestCase
{ 
   private String baseURLNoAuth;
   private HttpClient httpConn = new HttpClient();
   
   private String path = "header-form-auth/restricted/SecuredServlet";
   
   public CustomHeaderAuthTestCase(String name)
   {
      super(name); 
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      baseURLNoAuth = "http://" + getServerHost() + ":" + Integer.getInteger("web.port", 8080) + "/"; 
   }
   
   /**
    * Ensure that in the absence of headers, there is regular 
    * form based authentication
    * @throws Exception
    */
   public void testRegularFormAuth() throws Exception
   {
      doSecureGetWithLogin(path, "jduke", "theduke");
   }
   
   /**
    * Test usecases where the userid is sent via header and the
    * session key is used as the password.  To simplify testing,
    * we pass a password as part of the session key. In reality,
    * there needs to be a login module that can take the username 
    * and session key and validate.
    * @throws Exception
    */
   public void testCustomHeaderBaseAuth() throws Exception
   { 
      String serverHost = getServerHost();
      //Siteminder usecase
      performCustomAuth("sm_ssoid", new Cookie(serverHost, 
                  "SMSESSION", "theduke", "/", null, false), "SiteMinder");
      
      //Cleartrust usecase
      performCustomAuth("ct-remote-user", new Cookie(serverHost, 
            "CTSESSION", "theduke", "/", null, false), "Cleartrust");
      
      //Oblix usecase
      performCustomAuth("HTTP_OBLIX_UID", new Cookie(serverHost, 
            "ObSSOCookie", "theduke", "/", null, false), "Oblix"); 
   }
   
   private void performCustomAuth(String headerId, Cookie cookie,
         String usecase) throws Exception
   {
      GetMethod indexGet = new GetMethod(baseURLNoAuth+path);
      indexGet.addRequestHeader(headerId, "jduke"); 
      httpConn.getState().addCookie(cookie); 
      int responseCode = httpConn.executeMethod(indexGet); 
      String response = indexGet.getStatusText();
      log.debug("Response from " + usecase + " case:"+response);
      Header jex = indexGet.getResponseHeader("X-JException");
      log.debug("Saw X-JException, "+jex);
      assertNull("X-JException == null", jex);
      assertTrue("Get OK("+responseCode+")", responseCode == HttpURLConnection.HTTP_OK);
   }
   
   private PostMethod doSecureGetWithLogin(String path, String username, String password)
   throws Exception
   {
      GetMethod indexGet = new GetMethod(baseURLNoAuth+path);
      int responseCode = httpConn.executeMethod(indexGet);
      String body = indexGet.getResponseBodyAsString();
      assertTrue("Get OK("+responseCode+")", responseCode == HttpURLConnection.HTTP_OK);
      assertTrue("Redirected to login page", body.indexOf("j_security_check") > 0 );

      HttpState state = httpConn.getState();
      Cookie[] cookies = state.getCookies();
      String sessionID = null;
      for(int c = 0; c < cookies.length; c ++)
      {
         Cookie k = cookies[c];
         if( k.getName().equalsIgnoreCase("JSESSIONID") )
            sessionID = k.getValue();
      }
      getLog().debug("Saw JSESSIONID="+sessionID);

      // Submit the login form
      PostMethod formPost = new PostMethod(baseURLNoAuth+"header-form-auth/j_security_check");
      formPost.addRequestHeader("Referer", baseURLNoAuth+"header-form-auth/restricted/login.html");
      formPost.addParameter("j_username", username);
      formPost.addParameter("j_password", password);
      responseCode = httpConn.executeMethod(formPost.getHostConfiguration(),
            formPost, state);
      String response = formPost.getStatusText();
      log.debug("responseCode="+responseCode+", response="+response);
      assertTrue("Saw HTTP_MOVED_TEMP", responseCode == HttpURLConnection.HTTP_MOVED_TEMP);

      //  Follow the redirect to the SecureServlet
      Header location = formPost.getResponseHeader("Location");
      String indexURI = location.getValue();
      GetMethod war1Index = new GetMethod(indexURI);
      responseCode = httpConn.executeMethod(war1Index.getHostConfiguration(),
            war1Index, state);
      response = war1Index.getStatusText();
      log.debug("responseCode="+responseCode+", response="+response);
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
      body = war1Index.getResponseBodyAsString();
      if( body.indexOf("j_security_check") > 0 )
         fail("get of "+indexURI+" redirected to login page");
      return formPost;
   }

   /** One time setup for all SingleSignOnUnitTestCase unit tests
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(CustomHeaderAuthTestCase.class));

      // Create an initializer for the test suite
      Test wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            deploy("header-form-auth.ear");
            // Make sure the security cache is clear
            flushAuthCache();
         }
         protected void tearDown() throws Exception
         {
            undeploy("header-form-auth.ear");
            super.tearDown();
         }
      };
      return wrapper;
   }
}
