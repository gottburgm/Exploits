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
package org.jboss.test.web.test.ssl;

import java.net.HttpURLConnection;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;

/**
 * Unit Test the CLIENT-CERT JASPI integration
 * @author Anil.Saldhana@redhat.com
 * @since May 18, 2009
 */
public class ClientCertJaspiWebUnitTestCase extends JBossTestCase
{ 
   private String baseHttpsNoAuth; 

   private static String login_config = 
      "security/jaspi/jaspi-webssl-jboss-beans.xml";
   
   
   public ClientCertJaspiWebUnitTestCase(String name)
   {
      super(name); 
   }
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      baseHttpsNoAuth = "https://" + getServerHost() + ":" + Integer.getInteger("secureweb.port", 8443) + "/"; 
   }
   
   /** Test CLIENT-CERT
    * 
    * @throws Exception
    */ 
   public void testJASPIClientCert() throws Exception
   {
      log.info("+++ testJASPIClientCert");
      doHttps(baseHttpsNoAuth);
   }
   
   public void doHttps(String httpsNoAuth) throws Exception
   {
      log.info("+++ testJASPIClientCert, httpsNoAuth="+httpsNoAuth);
      // Start by accessing the secured index.html of war1
      HttpClient httpConn = new HttpClient();
      String url = httpsNoAuth+"clientcert-jaspi/unrestricted/SecureServlet";
      log.info("Accessing: "+url);
      GetMethod get = new GetMethod(url);
      int responseCode = httpConn.executeMethod(get);
      String status = get.getStatusText();
      log.debug(status);
      assertTrue("Get OK("+responseCode+")", responseCode == HttpURLConnection.HTTP_OK);
   } 
    
   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(ClientCertJaspiWebUnitTestCase.class));

      // Create an initializer for the test suite
      TestSetup wrapper = new JBossTestSetup(suite)
      {
         @Override
         protected void setUp() throws Exception
         {
            super.setUp(); 
            redeploy("clientcert-jaspi.war");
            redeploy(getResourceURL(login_config));
            flushAuthCache();
         }

         @Override
         protected void tearDown() throws Exception
         {
            undeploy(getResourceURL(login_config));
            undeploy("clientcert-jaspi.war");
            super.tearDown();
         }
      };
      return wrapper;
   } 
}