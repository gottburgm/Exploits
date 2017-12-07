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
import java.net.URL;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.util.web.HttpUtils;

/**
 * JSR-196: Web Basic Auth Unit Test Case
 * @author Anil.Saldhana@redhat.com
 * @since Oct 8, 2008
 */
public class WebJASPIBasicUnitTestCase extends JBossTestCase
{
   private static String login_config = "security/jaspi/jaspi-webbasic-jboss-beans.xml";
   private String realm = "JASPI";
   
   private String username = "anil";
   private String password = "cricket";

   public WebJASPIBasicUnitTestCase(String name)
   {
      super(name); 
   } 
   
   public void testBasicAuthSuccess() throws Exception
   {   
      String baseURL = HttpUtils.getBaseURL(username, password); 
      URL url = new URL(baseURL+"jaspi-web-basic/");
      HttpUtils.accessURL(url, realm, HttpURLConnection.HTTP_OK, HttpUtils.GET);
   }
   
   public void testBasicAuthFailure() throws Exception
   {
      String baseURL = HttpUtils.getBaseURL(username, "BAD"); 
      URL url = new URL(baseURL+"jaspi-web-basic/");
      HttpUtils.accessURL(url, realm, HttpURLConnection.HTTP_UNAUTHORIZED, HttpUtils.GET); 
   }  
   
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(WebJASPIBasicUnitTestCase.class));
      // Create an initializer for the test suite
      TestSetup wrapper = new JBossTestSetup(suite)
      { 
         protected void setUp() throws Exception
         {
            super.setUp();
            deploy(getResourceURL(login_config)); 
            deploy("jaspi-web-basic.war");
         }
         protected void tearDown() throws Exception
         {   
            undeploy("jaspi-web-basic.war");
            undeploy(getResourceURL(login_config));
            super.tearDown(); 
         }
      };
      return wrapper; 
   }  
}