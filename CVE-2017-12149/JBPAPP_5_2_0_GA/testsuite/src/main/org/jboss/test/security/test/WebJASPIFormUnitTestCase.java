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
package org.jboss.test.security.test;

import java.net.HttpURLConnection;

import junit.extensions.TestSetup;
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
 * JSR-196: Web Form Auth Unit Test Case
 * @author Anil.Saldhana@redhat.com
 * @since Oct 8, 2008
 */
public class WebJASPIFormUnitTestCase extends JBossTestCase
{
   private static String login_config = "security/jaspi/jaspi-webform-jboss-beans.xml";
   private String baseURLNoAuth ; 
   private HttpClient httpConn = new HttpClient();
   
   private String username = "anil";
   private String password = "cricket";

   public WebJASPIFormUnitTestCase(String name)
   {
      super(name); 
   }
   
   protected void setUp() throws Exception
   {
      super.setUp();
      baseURLNoAuth = "http://" + getServerHost() + ":" + Integer.getInteger("web.port", 8080) + "/"; 
   }
   
   public void testFormAuthSuccess() throws Exception
   {
      GetMethod indexGet = new GetMethod(baseURLNoAuth+"jaspi-web-form/");
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
      PostMethod formPost = new PostMethod(baseURLNoAuth+"jaspi-web-form/j_security_check");
      formPost.addRequestHeader("Referer", baseURLNoAuth+"jaspi-web-form/login.jsp");
      formPost.addParameter("j_username", username);
      formPost.addParameter("j_password", password);
      responseCode = httpConn.executeMethod(formPost);
      String response = formPost.getStatusText();
      log.debug("responseCode="+responseCode+", response="+response);
      assertTrue("Saw HTTP_MOVED_TEMP", responseCode == HttpURLConnection.HTTP_MOVED_TEMP);

      //  Follow the redirect to the SecureServlet
      Header location = formPost.getResponseHeader("Location");
      String indexURI = location.getValue();
      GetMethod war1Index = new GetMethod(indexURI); 
      responseCode = httpConn.executeMethod(war1Index);
      response = war1Index.getStatusText();
      log.debug("responseCode="+responseCode+", response="+response);
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
      body = war1Index.getResponseBodyAsString();
      if( body.indexOf("j_security_check") > 0 )
         fail("get of "+indexURI+" redirected to login page"); 
      if( body.indexOf("Hi Anil") < 0 )
         fail("index.html not seen");
   }
   
   public void testFormAuthFailure() throws Exception
   {
      log.info("+++ testFormAuthFailure");
      GetMethod indexGet = new GetMethod(baseURLNoAuth+"jaspi-web-form/");
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
      PostMethod formPost = new PostMethod(baseURLNoAuth+"jaspi-web-form/j_security_check");
      formPost.addRequestHeader("Referer", baseURLNoAuth+"jaspi-web-form/login.jsp");
      formPost.addParameter("j_username", "baduser");
      formPost.addParameter("j_password", "badpass");
      responseCode = httpConn.executeMethod(formPost);
      String response = formPost.getStatusText();
      log.debug("responseCode="+responseCode+", response="+response);
      String responseBody = formPost.getResponseBodyAsString();
      if(responseBody.indexOf("Errored") < 0)
         fail("Error page not seen");  
   } 
   
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(WebJASPIFormUnitTestCase.class));
      // Create an initializer for the test suite
      TestSetup wrapper = new JBossTestSetup(suite)
      { 
         protected void setUp() throws Exception
         {
            super.setUp();
            deploy(getResourceURL(login_config)); 
            deploy("jaspi-web-form.war");
         }
         protected void tearDown() throws Exception
         {   
            undeploy("jaspi-web-form.war");
            undeploy(getResourceURL(login_config));
            super.tearDown(); 
         }
      };
      return wrapper; 
   }  
}
