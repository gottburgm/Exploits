/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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

/**
 * <p>
 * JBAS-4804: Generic Header Based Authentication. This test case addresses the scenarios in which the 
 * <code>GenericHeaderAuthenticator</code> is configured in the <code>WEB-INF/context.xml</code> file of
 * the web application. The <code>HttpHeaderForSSOAuth</code> and <code>SessionCookieForSSOAuth</code>
 * properties are defined as attributes of the <code>GenericHeaderAuthenticator</code>, requiring no
 * further configuration of the application server.
 * </p>
 * <p>
 * The web application's <code>contex.xml</code> file should look like the following:
 * <pre>
 *    &lt;Context&gt;
 *       &lt;Valve className="org.jboss.web.tomcat.security.GenericHeaderAuthenticator" 
 *              httpHeaderForSSOAuth="sm_ssoid,ct-remote-user,HTTP_OBLIX_UID"
 *              sessionCookieForSSOAuth="SMSESSION,CTSESSION,ObSSOCookie"/&gt;
 *    &lt;/Context&gt;
 * </pre>
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 * @since  Oct 18, 2007 
 */
public class GenericHeaderAuthUnitTestCase extends JBossTestCase
{
   private String testAppBaseURL;

   private String securedServletPath;

   private HttpClient httpClient;

   /**
    * <p>
    * Creates an instance of <code>GenericHeaderAuthUnitTestCase</code> with the specified name.
    * </p>
    * 
    * @param name   the name of the test case.
    */
   public GenericHeaderAuthUnitTestCase(String name)
   {
      super(name);
//      this.testAppBaseURL = "http://" + super.getServerHost() + ":" + Integer.getInteger("web.port", 8080)
//            + "/generic-header-auth/";
      this.securedServletPath = "restricted/SecuredServlet";
      this.httpClient = new HttpClient();
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      this.testAppBaseURL = "http://" + super.getServerHost() + ":" + Integer.getInteger("web.port", 8080)
            + "/generic-header-auth/";
   }


   /**
    * <p>
    * Check that, in the absence of headers, regular form authentication takes place.
    * </p>
    * 
    * @throws Exception if an error occurs when running the test.
    */
   public void testRegularFormAuth() throws Exception
   {
      GetMethod getMethod = new GetMethod(this.testAppBaseURL + this.securedServletPath);
      // execute a plain request to the SecureServlet
      try
      {
         int responseCode = this.httpClient.executeMethod(getMethod);
         String body = getMethod.getResponseBodyAsString();
         // check the response code and assert the redirection to the login page
         assertTrue("Unexpected response code received: " + responseCode, responseCode == HttpURLConnection.HTTP_OK);
         assertTrue("Failed to redirect the request to the login page", body.indexOf("j_security_check") > 0);
      }
      finally
      {
         getMethod.releaseConnection();
      }

      HttpState state = this.httpClient.getState();
      // fill in the login form and submit it
      PostMethod postMethod = new PostMethod(this.testAppBaseURL + "j_security_check");
      postMethod.addRequestHeader("Referer", this.testAppBaseURL + "restricted/login.html");
      postMethod.addParameter("j_username", "jduke");
      postMethod.addParameter("j_password", "theduke");
      Header location = null;
      try
      {
         int responseCode = this.httpClient.executeMethod(postMethod.getHostConfiguration(), postMethod, state);
         log.debug("responseCode=" + responseCode + ", response=" + postMethod.getStatusText());
         // check the response code received and the presence of a location header in the response
         assertTrue("Unexpected response code received: " + responseCode,
               responseCode == HttpURLConnection.HTTP_MOVED_TEMP);
         location = postMethod.getResponseHeader("Location");
         assertNotNull("Location header not found in response", location);
      }
      finally
      {
         postMethod.releaseConnection();
      }

      // follow the redirect as defined by the location header
      String indexURI = location.getValue();
      getMethod = new GetMethod(indexURI);
      try
      {
         int responseCode = this.httpClient.executeMethod(getMethod.getHostConfiguration(), getMethod, state);
         log.debug("responseCode=" + responseCode + ", response=" + getMethod.getStatusText());
         // check the reponse code received
         assertTrue("Unexpected response code received: " + responseCode, responseCode == HttpURLConnection.HTTP_OK);
         String body = getMethod.getResponseBodyAsString();
         // assert the redirection of to the SecureServlet
         assertTrue("Redirect to SecureServlet has failed", body.indexOf("SecureServlet") > 0);
      }
      finally
      {
         getMethod.releaseConnection();
      }
   }

   /**
    * <p>
    * Test usecases where the userid is sent via header and the session key is used as the password. To simplify
    * testing, we pass a password as part of the session key. In reality, there needs to be a login module that can
    * take the username and session key and validate.
    * </p>
    * 
    * @throws Exception if an error occurs when running the test.
    */
   public void testGenericHeaderBaseAuth() throws Exception
   {
      String serverHost = super.getServerHost();
      // Siteminder usecase
      this
            .performHeaderAuth("sm_ssoid", new Cookie(serverHost, "SMSESSION", "theduke", "/", null, false),
                  "SiteMinder");
      // Cleartrust usecase
      this.performHeaderAuth("ct-remote-user", new Cookie(serverHost, "CTSESSION", "theduke", "/", null, false),
            "Cleartrust");
      // Oblix usecase
      this.performHeaderAuth("HTTP_OBLIX_UID", new Cookie(serverHost, "ObSSOCookie", "theduke", "/", null, false),
            "Oblix");
   }

   /**
    * <p>
    * Invoke the <code>SecureServlet</code> setting the specified <code>headerId</code> and <code>cookie</code> objects
    * in the request.
    * </p>
    * 
    * @param headerId   a <code>String</code> representing the name of the request header that holds the user id.
    * @param cookie     a <code>Cookie</code> object containing the user's password.
    * @param usecase a <code>String</code> representing the name of the use case being tested.
    * @throws Exception if an error occurs when authenticating the user.
    */
   private void performHeaderAuth(String headerId, Cookie cookie, String usecase) throws Exception
   {
      GetMethod method = new GetMethod(this.testAppBaseURL + this.securedServletPath);
      // add the headerId and cookie objects to the request
      method.addRequestHeader(headerId, "jduke");
      this.httpClient.getState().addCookie(cookie);
      // execute the request
      try
      {
         int responseCode = this.httpClient.executeMethod(method);
         // check the response code received
         log.debug("Response from " + usecase + " case:" + method.getStatusText());
         assertTrue("Unexpected response code received: " + responseCode, responseCode == HttpURLConnection.HTTP_OK);
         // check that access to the secure servlet has been granted
         String body = method.getResponseBodyAsString();
         assertTrue("Access to SecureServlet has not been granted", body.indexOf("SecureServlet") > 0);
      }
      finally
      {
         // release the connection
         method.releaseConnection();
      }
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(GenericHeaderAuthUnitTestCase.class));

      // create an initializer for the test suite
      Test wrapper = new JBossTestSetup(suite)
      {
         /**
          * <p>
          * Deploy the ear file containing the Servlets used by the tests.
          * </p>
          * 
          * @throws Exception if an error occurs when deploying the ear.
          */
         protected void setUp() throws Exception
         {
            super.setUp();
            super.deploy("generic-header-auth.ear");
            // make sure the security cache is clear
            super.flushAuthCache();
         }

         /**
          * <p>
          * Undeploy the ear file containing the Servlets used by the tests.
          * </p>
          * 
          * @throws Exception if an error occurs when undeploying the ear.
          */
         protected void tearDown() throws Exception
         {
            super.undeploy("generic-header-auth.ear");
            super.tearDown();
         }
      };
      return wrapper;
   }
}
