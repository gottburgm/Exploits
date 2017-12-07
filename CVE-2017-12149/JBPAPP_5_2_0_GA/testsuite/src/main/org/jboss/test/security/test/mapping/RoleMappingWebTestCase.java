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
package org.jboss.test.security.test.mapping;

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
 *  Test role mapping logic for the web layer
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @version $Revision: 85945 $
 *  @since  Aug 27, 2006
 */
public class RoleMappingWebTestCase extends JBossTestCase
{ 
   private String baseURLNoAuth;
   private HttpClient httpConn = new HttpClient();

   public RoleMappingWebTestCase(String name)
   {
      super(name); 
   }
   
   /**
    * Test a FORM auth simple webapp. A role of "testRole" will
    * be mapped to Authorized User via the role mapping logic
    */
   public void testWebAccess() throws Exception
   {
      baseURLNoAuth = "http://" + getServerHost() 
            + ":" + Integer.getInteger("web.port", 8080) + "/"; 
      GetMethod indexGet = new GetMethod(baseURLNoAuth+"web-role-map/Secured.jsp");
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
      PostMethod formPost = new PostMethod(baseURLNoAuth+"web-role-map/j_security_check");
      formPost.addRequestHeader("Referer", baseURLNoAuth+"web-role-map/login.html");
      formPost.addParameter("j_username", "user");
      formPost.addParameter("j_password", "pass");
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
   } 
    
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(RoleMappingWebTestCase.class));

      // Create an initializer for the test suite
      Test wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            deploy(getResourceURL("security-spi/rolemapping/rolemapping-test-service.xml")); 
            deploy("web-role-map.war");
         }
         protected void tearDown() throws Exception
         {
            undeploy(getResourceURL("security-spi/rolemapping/rolemapping-test-service.xml"));
            undeploy("web-role-map.war");
            super.tearDown();
         }
      };
      return wrapper;
   }
}
