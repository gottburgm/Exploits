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
package org.jboss.test.web.test;

import java.net.HttpURLConnection;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.security.plugins.JaasSecurityManagerServiceMBean;
import junit.framework.Test;
import junit.framework.TestSuite;

/** Tests of form authentication
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class FormAuthUnitTestCase extends JBossTestCase
{
   private String baseURLNoAuth ; 
   private HttpClient httpConn = new HttpClient();

   public FormAuthUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      baseURLNoAuth = "http://" + getServerHost() + ":" + Integer.getInteger("web.port", 8080) + "/"; 
   }

   /** Test form authentication of a secured servlet
    * 
    * @throws Exception
    */ 
   public void testFormAuth() throws Exception
   {
      log.info("+++ testFormAuth");
      doSecureGetWithLogin("form-auth/restricted/SecuredServlet");
      /* Access the resource without attempting a login to validate that the
         session is valid and that any caching on the server is working as
         expected.
      */
      doSecureGet("form-auth/restricted/SecuredServlet");
   }

   /**
    * Test that a bad login is redirected to the errors.jsp and that the
    * session j_exception is not null.
    * 
    * @throws Exception
    */ 
   public void testFormAuthException() throws Exception
   {
      log.info("+++ testFormAuthException");
      GetMethod indexGet = new GetMethod(baseURLNoAuth+"form-auth/restricted/SecuredServlet");
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
      PostMethod formPost = new PostMethod(baseURLNoAuth+"form-auth/j_security_check");
      formPost.addRequestHeader("Referer", baseURLNoAuth+"form-auth/restricted/login.html");
      formPost.addParameter("j_username", "baduser");
      formPost.addParameter("j_password", "badpass");
      responseCode = httpConn.executeMethod(formPost.getHostConfiguration(),
         formPost, state);
      String response = formPost.getStatusText();
      log.debug("responseCode="+responseCode+", response="+response);
      Header jex = formPost.getResponseHeader("X-JException");
      log.debug("Saw X-JException, "+jex);
      assertNotNull("X-JException != null", jex);
   }

   /** Test form authentication of a secured servlet and validate that there is
    * a SecurityAssociation setting Subject. 
    * 
    * @throws Exception
    */ 
   public void testFormAuthSubject() throws Exception
   {
      log.info("+++ testFormAuthSubject");
      // Start by accessing the secured index.html of war1
      HttpClient httpConn = new HttpClient();
      GetMethod indexGet = new GetMethod(baseURLNoAuth+"form-auth/restricted/SecuredServlet");
      indexGet.setQueryString("validateSubject=true");
      int responseCode = httpConn.executeMethod(indexGet);
      String body = indexGet.getResponseBodyAsString();
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
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
      PostMethod formPost = new PostMethod(baseURLNoAuth+"form-auth/j_security_check");
      formPost.addRequestHeader("Referer", baseURLNoAuth+"form-auth/restricted/login.html");
      formPost.addParameter("j_username", "jduke");
      formPost.addParameter("j_password", "theduke");
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

   /** Test that a post from an unsecured form to a secured servlet does not
    * loose its data during the redirct to the form login.
    * 
    * @throws Exception
    */ 
   public void testPostDataFormAuth() throws Exception
   {
      log.info("+++ testPostDataFormAuth");
      // Start by accessing the secured index.html of war1
      HttpClient httpConn = new HttpClient();
      GetMethod indexGet = new GetMethod(baseURLNoAuth+"form-auth/unsecure_form.html");
      int responseCode = httpConn.executeMethod(indexGet);
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
      // Submit the form to /restricted/SecuredPostServlet
      PostMethod servletPost = new PostMethod(baseURLNoAuth+"form-auth/restricted/SecuredPostServlet");
      servletPost.addParameter("checkParam", "123456");
      responseCode = httpConn.executeMethod(servletPost);

      String body = servletPost.getResponseBodyAsString();
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
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
      PostMethod formPost = new PostMethod(baseURLNoAuth+"form-auth/j_security_check");
      formPost.addRequestHeader("Referer", baseURLNoAuth+"form-auth/unsecure_form.html");
      formPost.addParameter("j_username", "jduke");
      formPost.addParameter("j_password", "theduke");
      responseCode = httpConn.executeMethod(formPost.getHostConfiguration(),
         formPost, state);
      String response = formPost.getStatusText();
      getLog().debug("responseCode="+responseCode+", response="+response);
      assertTrue("Saw HTTP_MOVED_TEMP", responseCode == HttpURLConnection.HTTP_MOVED_TEMP);

      //  Follow the redirect to the SecureServlet
      Header location = formPost.getResponseHeader("Location");
      String indexURI = location.getValue();
      GetMethod war1Index = new GetMethod(indexURI);
      responseCode = httpConn.executeMethod(war1Index.getHostConfiguration(),
         war1Index, state);
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
      body = war1Index.getResponseBodyAsString();
      if( body.indexOf("j_security_check") > 0 )
         fail("get of "+indexURI+" redirected to login page");
   }

   /** Test that the war which uses <security-domain flushOnSessionInvalidation="true">
    * in the jboss-web.xml does not have any jaas security domain cache entries
    * after the web session has been invalidated.
    */ 
   public void testFlushOnSessionInvalidation() throws Exception
   {
      log.info("+++ testFlushOnSessionInvalidation");
      MBeanServerConnection conn = (MBeanServerConnection) getServer();
      ObjectName name = new ObjectName("jboss.security:service=JaasSecurityManager");
      JaasSecurityManagerServiceMBean secMgrService = (JaasSecurityManagerServiceMBean)
         MBeanServerInvocationHandler.newProxyInstance(conn, name, JaasSecurityManagerServiceMBean.class, false);

      // Access a secured servlet to create a session and jaas cache entry
      doSecureGetWithLogin("form-auth/restricted/SecuredServlet");

      // Validate that the jaas cache has 1 principal
      List principals = secMgrService.getAuthenticationCachePrincipals("jbossweb-form-auth");
      assertTrue("jbossweb-form-auth has one principal", principals.size() == 1);

      // Logout to clear the cache
      doSecureGet("form-auth/Logout");
      principals = secMgrService.getAuthenticationCachePrincipals("jbossweb-form-auth");
      log.info("jbossweb-form-auth principals = "+principals);
      assertTrue("jbossweb-form-auth has no cache principals", principals.size() == 0);
   }

   public PostMethod doSecureGetWithLogin(String path) throws Exception
   {
      return doSecureGetWithLogin(path, "jduke", "theduke");
   }
   public PostMethod doSecureGetWithLogin(String path, String username, String password)
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
      PostMethod formPost = new PostMethod(baseURLNoAuth+"form-auth/j_security_check");
      formPost.addRequestHeader("Referer", baseURLNoAuth+"form-auth/restricted/login.html");
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
   public void doSecureGet(String path) throws Exception
   {
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
      GetMethod indexGet = new GetMethod(baseURLNoAuth+path);
      int responseCode = httpConn.executeMethod(indexGet.getHostConfiguration(),
         indexGet, state);
      assertTrue("Get OK("+responseCode+")", responseCode == HttpURLConnection.HTTP_OK);
   }

   /** One time setup for all SingleSignOnUnitTestCase unit tests
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(FormAuthUnitTestCase.class));

      // Create an initializer for the test suite
      Test wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            deploy("form-auth.ear");
            // Make sure the security cache is clear
            flushAuthCache();
            //Make sure the ExtendedFormAuthenticator is registered in tomcat
            //Note Tomcat always binds to localhost
            String oname = "jboss.web:host=localhost,name=ExtendedFormAuthenticator,path=/form-auth,type=Valve";
            ObjectName formAuth = new ObjectName(oname);
            //We have a form-auth war with FORM authenticator and that is not overriden at the webapp level
//            assertNotNull("Authenticator for FORM on host=localhost exists?", getServer().getObjectInstance(formAuth));
         
         }
         protected void tearDown() throws Exception
         {
            undeploy("form-auth.ear");
            super.tearDown();
         }
      };
      return wrapper;
   }
}
