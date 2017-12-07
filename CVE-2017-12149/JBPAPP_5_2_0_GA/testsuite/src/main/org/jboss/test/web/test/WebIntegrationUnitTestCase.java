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
import java.net.URL;
import javax.management.ObjectName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.util.jms.JMSDestinationsUtil;
import org.jboss.test.util.web.HttpUtils;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.Header;

/** Tests of servlet container integration into the JBoss server. This test
 requires than a web container be integrated into the JBoss server. The tests
 currently do NOT use the java.net.HttpURLConnection and associated http client
 and  these do not return valid HTTP error codes so if a failure occurs it
 is best to connect the webserver using a browser to look for additional error
 info.

 The secure access tests require a user named 'jduke' with a password of 'theduke'
 with a role of 'AuthorizedUser' in the servlet container.
 
 @author Scott.Stark@jboss.org
 @version $Revision: 105789 $
 */
public class WebIntegrationUnitTestCase extends JBossTestCase
{
   private static String REALM = "JBossTest Servlets";
   private String baseURL = HttpUtils.getBaseURL(); 
   private String baseURLNoAuth = HttpUtils.getBaseURLNoAuth(); 
   
   public WebIntegrationUnitTestCase(String name)
   {
      super(name);
   }
   
   /** Access the http://{host}/jbosstest/APIServlet to test the
    * getRealPath method
    */
   public void testRealPath() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/APIServlet?op=testGetRealPath");
      HttpUtils.accessURL(url);
   }

   /** Access the http://{host}/jbosstest/APIServlet to test the
    * HttpSessionListener events
    */
   public void testHttpSessionListener() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/APIServlet?op=testSessionListener");
      HttpUtils.accessURL(url);
   }

   /** Access the http://{host}/jbosstest/EJBOnStartupServlet
    */
   public void testEJBOnStartupServlet() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/EJBOnStartupServlet");
      HttpUtils.accessURL(url);
   }
   /** Access the http://{host}/jbosstest/ENCServlet
    */
   public void testENCServlet() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/ENCServlet");
      HttpUtils.accessURL(url);
   }

   /** Access the http://{host}/jbosstest/SimpleServlet to test that servlets
    * in the WEB-INF/lib jar.
    * 
    */
   public void testServletInJar() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/SimpleServlet");
      HttpUtils.accessURL(url);      
   }

   /** Access the http://{host}/jbosstest/EJBServlet
    */
   public void testEJBServlet() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/EJBServlet");
      HttpUtils.accessURL(url);
   }
   /** Access the http://{host}/jbosstest/EntityServlet
    */
   public void testEntityServlet() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/EntityServlet");
      HttpUtils.accessURL(url);
   }
   /** Access the http://{host}/jbosstest/StatefulSessionServlet
    */
   public void testStatefulSessionServlet() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/StatefulSessionServlet");
      HttpUtils.accessURL(url);
      // Need a mechanism to force passivation...
      HttpUtils.accessURL(url);
   }
   /** Access the http://{host}/jbosstest/UserTransactionServlet
    */
   public void testUserTransactionServlet() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/UserTransactionServlet");
      HttpUtils.accessURL(url);
   }
   /** Access the http://{host}/jbosstest/SpeedServlet
    */
   public void testSpeedServlet() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/SpeedServlet");
      HttpUtils.accessURL(url);
   }
   /** Access the http://{host}/jbosstest/snoop.jsp
    */
   public void testSnoopJSP() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/snoop.jsp");
      HttpUtils.accessURL(url);
   }
   /** Access the http://{host}/jbosstest/snoop.jsp
    */
   public void testSnoopJSPByPattern() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/test-snoop.snp");
      HttpUtils.accessURL(url);
   }
   /** Access the http://{host}/jbosstest/test-jsp-mapping
    */
   public void testSnoopJSPByMapping() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/test-jsp-mapping");
      HttpUtils.accessURL(url);
   }
   /** Access the http://{host}/jbosstest/classpath.jsp
    */
   public void testJSPClasspath() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/classpath.jsp");
      HttpUtils.accessURL(url);
   }

   /** Access the http://{host}/jbosstest/ClientLoginServlet
    */
   public void testClientLoginServlet() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/ClientLoginServlet");
      HttpUtils.accessURL(url);
   }
   /** Access the http://{host}/jbosstest/restricted/UserInRoleServlet to
    * test isUserInRole.
    */
   public void testUserInRoleServlet() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/restricted/UserInRoleServlet");
      HttpMethodBase request = HttpUtils.accessURL(url);
      Header errors = request.getResponseHeader("X-ExpectedUserRoles-Errors");
      log.info("X-ExpectedUserRoles-Errors: "+errors);
      assertTrue("X-ExpectedUserRoles-Errors("+errors+") is null", errors == null);
      errors = request.getResponseHeader("X-UnexpectedUserRoles-Errors");
      log.info("X-UnexpectedUserRoles-Errors: "+errors);
      assertTrue("X-UnexpectedUserRoles-Errors("+errors+") is null", errors == null);
   }
   /** Access the http://{host}/jbosstest/restricted/SecureServlet
    */
   public void testSecureServlet() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/restricted/SecureServlet");
      HttpUtils.accessURL(url);
   }
   /** Access the http://{host}/jbosstest/restricted2/SecureServlet
    */
   public void testSecureServlet2() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/restricted2/SecureServlet");
      HttpUtils.accessURL(url);
   }
   /** Access the http://{host}/jbosstest/restricted/SubjectServlet
    */
   public void testSubjectServlet() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/restricted/SubjectServlet");
      HttpMethodBase request = HttpUtils.accessURL(url);
      Header hdr = request.getResponseHeader("X-SubjectServlet");
      log.info("X-SubjectServlet: "+hdr);
      assertTrue("X-SubjectServlet("+hdr+") is NOT null", hdr != null);
      hdr = request.getResponseHeader("X-SubjectFilter-ENC");
      log.info("X-SubjectFilter-ENC: "+hdr);
      assertTrue("X-SubjectFilter-ENC("+hdr+") is NOT null", hdr != null);
      hdr = request.getResponseHeader("X-SubjectFilter-SubjectSecurityManager");
      log.info("X-SubjectFilter-SubjectSecurityManager: "+hdr);
      assertTrue("X-SubjectFilter-SubjectSecurityManager("+hdr+") is NOT null", hdr != null);
   }
   /** Access the http://{host}/jbosstest/restricted/SecureServlet
    */
   public void testSecureServletAndUnsecureAccess() throws Exception
   {
      getLog().info("+++ testSecureServletAndUnsecureAccess");
      URL url = new URL(baseURL+"jbosstest/restricted/SecureServlet");
      getLog().info("Accessing SecureServlet with valid login");
      HttpUtils.accessURL(url);
      String baseURL2 = "http://" + getServerHost() + ":" + Integer.getInteger("web.port", 8080) + '/';
      URL url2 = new URL(baseURL2+"jbosstest/restricted/UnsecureEJBServlet");
      getLog().info("Accessing SecureServlet with no login");
      HttpUtils.accessURL(url2, REALM, HttpURLConnection.HTTP_UNAUTHORIZED);
   }
   /** Access the http://{host}/jbosstest/restricted/SecureServlet
    */
   public void testSecureServletWithBadPass() throws Exception
   {
      String baseURL = "http://jduke:badpass@" + getServerHost() + ":" + Integer.getInteger("web.port", 8080) + '/';
      URL url = new URL(baseURL+"jbosstest/restricted/SecureServlet");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_UNAUTHORIZED);
   }
   /** Access the http://{host}/jbosstest/restricted/SecureServlet
    */
   public void testSecureServletWithNoLogin() throws Exception
   {
      String baseURL = "http://" + getServerHost() + ":" + Integer.getInteger("web.port", 8080) + '/';
      URL url = new URL(baseURL+"jbosstest/restricted/SecureServlet");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_UNAUTHORIZED);
   }
   /** Access the http://{host}/jbosstest-not/unrestricted/SecureServlet
    */
   public void testNotJbosstest() throws Exception
   {
      String baseURL = "http://" + getServerHost() + ":" + Integer.getInteger("web.port", 8080) + '/';
      URL url = new URL(baseURL+"jbosstest-not/unrestricted/SecureServlet");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);
   }
   /** Access the http://{host}/jbosstest/restricted/SecuredEntityFacadeServlet
    */
   public void testSecuredEntityFacadeServlet() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/restricted/SecuredEntityFacadeServlet");
      HttpUtils.accessURL(url);
   }
   /** Access the http://{host}/jbosstest/restricted/SecureEJBAccess
    */
   public void testSecureEJBAccess() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/restricted/SecureEJBAccess");
      HttpUtils.accessURL(url);
   }
   /** Access the http://{host}/jbosstest/restricted/include_ejb.jsp
    */
   public void testIncludeEJB() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest/restricted/include_ejb.jsp");
      HttpUtils.accessURL(url);
   } 
   
   /** 
    * JBAS-3279: Authenticated user can bypass declarative role checks for servlets
    */
   public void testUnauthorizedAccess() throws Exception
   {
      URL url = new URL(baseURL+"jbosstest//restricted3//SecureServlet");
      HttpUtils.accessURL(url,REALM, HttpURLConnection.HTTP_FORBIDDEN);
      url = new URL(baseURL+"jbosstest/%2frestricted3//SecureServlet");
      // BES 2007/02/21 -- %xx encoded '/' is verboten so we now expect 400
      //HttpUtils.accessURL(url,REALM, HttpURLConnection.HTTP_FORBIDDEN);
      HttpUtils.accessURL(url,REALM, HttpURLConnection.HTTP_BAD_REQUEST);
   }
   
   /** Access the http://{host}/jbosstest/UnsecureEJBAccess with method=echo
    * to test that an unsecured servlet cannot access a secured EJB method
    * that requires a valid permission. This should fail.
    */
   public void testUnsecureEJBAccess() throws Exception
   {
      URL url = new URL(baseURLNoAuth+"jbosstest/UnsecureEJBAccess?method=echo");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_INTERNAL_ERROR);
   }
   /** Access the http://{host}/jbosstest/UnsecureEJBAccess with method=unchecked
    * to test that an unsecured servlet can access a secured EJB method that
    * only requires an authenticated user. This requires unauthenticated
    * identity support by the web security domain.
    */
   public void testUnsecureAnonEJBAccess() throws Exception
   {
      URL url = new URL(baseURLNoAuth+"jbosstest/UnsecureEJBAccess?method=unchecked");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);
   }

   public void testUnsecureRunAsServlet() throws Exception
   {
      URL url = new URL(baseURLNoAuth+"jbosstest/UnsecureRunAsServlet?method=checkRunAs");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);      
   }

   /** Access the http://{host}/jbosstest/UnsecureRunAsServletWithPrincipalName
    * to test that an unsecured servlet can access a secured EJB method by using
    * a run-as role. This should also have a custom run-as principal name.
    * 
    * @throws Exception
    */ 
   public void testUnsecureRunAsServletWithPrincipalName() throws Exception
   {
      URL url = new URL(baseURLNoAuth+"jbosstest/UnsecureRunAsServletWithPrincipalName?ejbName=ejb/UnsecureRunAsServletWithPrincipalNameTarget");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);      
   }

   /** Access the http://{host}/jbosstest/UnsecureRunAsServletWithPrincipalNameAndRoles
    * to test that an unsecured servlet can access a secured EJB method by using
    * a run-as role. This should also have a custom run-as principal name and
    * additional roles.
    * 
    * @throws Exception
    */ 
   public void testUnsecureRunAsServletWithPrincipalNameAndRoles() throws Exception
   {
      URL url = new URL(baseURLNoAuth+"jbosstest/UnsecureRunAsServletWithPrincipalNameAndRoles?ejbName=ejb/UnsecureRunAsServletWithPrincipalNameAndRolesTarget");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);      
   }
   
   /** Deploy a second ear that include a notjbosstest-web.war to test ears
    with the same war names conflicting.
    Access the http://{host}/jbosstest-not2/unrestricted/SecureServlet
    */
   public void testNotJbosstest2() throws Exception
   {
      try 
      {
         deploy("jbosstest-web2.ear");
         String baseURL = "http://" + getServerHost() + ":" + Integer.getInteger("web.port", 8080) + '/';
         URL url = new URL(baseURL+"jbosstest-not2/unrestricted/SecureServlet");
         HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);
      }
      finally
      {
         undeploy("jbosstest-web2.ear");
      } // end of try-finally
   }

   /** Deploy a bad war and then redploy with a fixed war to test failed war
    * cleanup.
    * Access the http://{host}/redeploy/index.html
    */
   public void testBadWarRedeploy() throws Exception
   {
      try
      {
         deploy("bad-web.war");
         fail("The bad-web.war deployment did not fail");
      }
      catch(Exception e)
      {
         getLog().debug("bad-web.war failed as expected", e);
      }
      finally
      {
         undeploy("bad-web.war");
      } // end of try-finally
      try 
      {
         deploy("good-web.war");
         String baseURL = "http://" + getServerHost() + ":" + Integer.getInteger("web.port", 8080) + '/';
         URL url = new URL(baseURL+"redeploy/index.html");
         HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);
      }
      finally
      {
         undeploy("good-web.war");
      } // end of try-finally
   }

   /** Test of a war that accesses classes referred to via the war manifest
    * classpath. Access the http://{host}/manifest/classpath.jsp
    */
   public void testWarManifest() throws Exception
   {
      deploy("manifest-web.ear");
      try
      {
         String baseURL = "http://" + getServerHost() + ":" + Integer.getInteger("web.port", 8080) + '/';
         URL url = new URL(baseURL+"manifest/classpath.jsp");
         HttpMethodBase request = HttpUtils.accessURL(url);
         Header errors = request.getResponseHeader("X-Exception");
         log.info("X-Exception: "+errors);
         assertTrue("X-Exception("+errors+") is null", errors == null);
      }
      finally
      {
         undeploy("manifest-web.ear");
      }
   }

   public void testBadEarRedeploy() throws Exception
   {
      try
      {
         deploy("jbosstest-bad.ear");
         fail("The jbosstest-bad.ear deployment did not fail");
      }
      catch(Exception e)
      {
         getLog().debug("jbosstest-bad.ear failed as expected", e);
      }
      finally
      {
         undeploy("jbosstest-bad.ear");
      } // end of finally
      try 
      {
         deploy("jbosstest-good.ear");
         String baseURL = "http://" + getServerHost() + ":" + Integer.getInteger("web.port", 8080) + '/';
         URL url = new URL(baseURL+"redeploy/index.html");
         HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);
      }
      finally
      {
         undeploy("jbosstest-good.ear");
      } // end of try-finally
      
   }

   /**
    * Validate a war level override of the 
    * java2ClassLoadingComplianceOverride flag to true.
    * 
    * @throws Exception
    */
   public void testJava2ClassLoadingComplianceOverride() throws Exception
   {
      getLog().info("+++ Begin testJava2ClassLoadingComplianceOverride");
      deploy("class-loading.war");
      try
      {
         String baseURL = "http://" + getServerHost() + ":" + Integer.getInteger("web.port", 8080) + '/';
         // Load a log4j class
         URL url = new URL(baseURL+"class-loading/ClasspathServlet2?class=org.apache.log4j.net.SocketAppender");
         HttpMethodBase request = HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);
         Header cs = request.getResponseHeader("X-CodeSource");
         log.info(cs);
         // Validate it has not come from the war
         assertTrue("X-CodeSource("+cs+") does not contain war",
               cs.getValue().indexOf(".war") < 0 );
         getLog().debug(url+" OK");
      }
      finally
      {
         undeploy("class-loading.war");
         getLog().info("+++ End testJava2ClassLoadingComplianceOverride");
      }
   }
   public void testWARWithServletAPIClasses() throws Exception
   {
      getLog().info("+++ Begin testWARWithServletAPIClasses");
      deploy("servlet-classes.war");
      try
      {
         String baseURL = "http://" + getServerHost() + ":" + Integer.getInteger("web.port", 8080) + '/';
         // Load a servlet class
         URL url = new URL(baseURL+"servlet-classes/ClasspathServlet2?class=javax.servlet.http.HttpServletResponse");
         HttpMethodBase request = HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);
         Header cs = request.getResponseHeader("X-CodeSource");
         log.info(cs);
         // Validate it has not come from the war
         assertTrue("X-CodeSource("+cs+") does not contain war",
               cs.getValue().indexOf(".war") < 0 );
         getLog().debug(url+" OK");
      }
      finally
      {
         undeploy("servlet-classes.war");
         getLog().info("+++ End testWARWithServletAPIClasses");
      }
   }

   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(WebIntegrationUnitTestCase.class));

      // Create an initializer for the test suite
      Test wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            JMSDestinationsUtil.setupBasicDestinations();
            redeploy("jbosstest-web.ear");
            flushAuthCache("jbosstest-web");
         }
         protected void tearDown() throws Exception
         {
            undeploy("jbosstest-web.ear");
            JMSDestinationsUtil.destroyDestinations();
            super.tearDown();
         
         }
      };
      return wrapper;
   }

}
