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
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.util.web.HttpUtils;
import org.jboss.security.plugins.JaasSecurityManagerServiceMBean;
import junit.framework.Test;
import junit.framework.TestSuite;

/** Tests of custom error forwarding 
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class CustomErrorsUnitTestCase extends JBossTestCase
{
   private String baseURLNoAuth = HttpUtils.getBaseURLNoAuth(); 

   public CustomErrorsUnitTestCase(String name)
   {
      super(name);
   }

   /** Test that the custom 404 error page is seen
    * 
    * @throws Exception
    */ 
   public void test404Error() throws Exception
   {
      log.info("+++ test404Error");
      int errorCode = HttpURLConnection.HTTP_NOT_FOUND;
      URL url = new URL(baseURLNoAuth+"error-producer/ErrorGeneratorServlet?errorCode="+errorCode);
      HttpMethodBase request = HttpUtils.accessURL(url, "Realm",
         HttpURLConnection.HTTP_NOT_FOUND);
      Header errors = request.getResponseHeader("X-CustomErrorPage");
      log.info("X-CustomErrorPage: "+errors);
      assertTrue("X-CustomErrorPage("+errors+") is 404.jsp",
         errors.getValue().equals("404.jsp"));
   }

   /** Test that the custom 500 error page is seen
    * 
    * @throws Exception
    */ 
   public void test500Error() throws Exception
   {
      log.info("+++ test500Error");
      int errorCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
      URL url = new URL(baseURLNoAuth+"error-producer/ErrorGeneratorServlet?errorCode="+errorCode);
      HttpMethodBase request = HttpUtils.accessURL(url, "Realm",
         HttpURLConnection.HTTP_INTERNAL_ERROR);
      Header errors = request.getResponseHeader("X-CustomErrorPage");
      log.info("X-CustomErrorPage: "+errors);
      assertTrue("X-CustomErrorPage("+errors+") is 500.jsp",
         errors.getValue().equals("500.jsp"));
   }

   /** Test that the custom 500 error page is seen for an exception
    * 
    * @throws Exception
    */ 
   public void testExceptionError() throws Exception
   {
      log.info("+++ testExceptionError");
      URL url = new URL(baseURLNoAuth+"error-producer/ErrorGeneratorServlet");
      HttpMethodBase request = HttpUtils.accessURL(url, "Realm",
         HttpURLConnection.HTTP_INTERNAL_ERROR);
      Header page = request.getResponseHeader("X-CustomErrorPage");
      log.info("X-CustomErrorPage: "+page);
      assertTrue("X-CustomErrorPage("+page+") is 500.jsp",
         page.getValue().equals("500.jsp"));
      Header errors = request.getResponseHeader("X-ExceptionType");
      log.info("X-ExceptionType: "+errors);
      assertTrue("X-ExceptionType("+errors+") is 500.jsp",
         errors.getValue().equals("java.lang.IllegalStateException"));
   }

   /** One time setup for all SingleSignOnUnitTestCase unit tests
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(CustomErrorsUnitTestCase.class));

      // Create an initializer for the test suite
      Test wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            deploy("custom-errors.war");
            deploy("error-producer.war");
         }
         protected void tearDown() throws Exception
         {
            undeploy("custom-errors.war");
            undeploy("error-producer.war");
            super.tearDown();
         }
      };
      return wrapper;
   }
}
