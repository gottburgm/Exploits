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

import junit.framework.Test;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.test.JBossTestCase;

/** Tests of ssl and CLIENT-CERT auth
 * 
 * @author Scott.Stark@jboss.org
 * @author Anil.Saldhana@jboss.org
 * @version $Revision: 89013 $
 */
public class SSLUnitTestCase extends JBossTestCase
{
   private String baseHttpNoAuth; 
   private String baseHttpsNoAuth; 

   public SSLUnitTestCase(String name)
   {
      super(name);
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      baseHttpNoAuth = "http://" + getServerHost() + ":" + Integer.getInteger("web.port", 8080) + "/"; 
      baseHttpsNoAuth = "https://" + getServerHost() + ":" + Integer.getInteger("secureweb.port", 8443) + "/"; 
   }

   /** Test that access of the transport constrained redirects to the ssl connector
    * 
    * @throws Exception
    */ 
   public void testHttpRedirect() throws Exception
   {
      log.info("+++ testHttpRedirect");
      doHttpRedirect(baseHttpNoAuth);
   }
   /** Test that access of the transport constrained redirects to the ssl connector
    * when using the SecurityDomain based connector config.
    * 
    * @throws Exception
    */ 
   public void testHttpRedirectSecurityDomain() throws Exception
   {
      log.info("+++ testHttpRedirectSecurityDomain");
      int port = Integer.getInteger("web.port", 8080).intValue();
      port += 1000;
      String httpNoAuth = "http://" + getServerHost() + ":" + port + "/";
      doHttpRedirect(httpNoAuth);
   }

   /** Test that access of the transport constrained 
    * 
    * @throws Exception
    */ 
   public void testHttps() throws Exception
   {
      log.info("+++ testHttps");
      doHttps(baseHttpsNoAuth);
   }
   
   public void testHttpsSecurityDomain() throws Exception
   {
      log.info("+++ testHttps");
      int port = Integer.getInteger("secureweb.port", 8443).intValue();
      port += 1000;
      String httpsNoAuth = "https://" + getServerHost() + ":" + port + "/";
      doHttps(httpsNoAuth);
   }
   
   /**
    * Test masking of Keystore password via encryption
    * @throws Exception
    */
   public void testEncryptPassword() throws Exception
   {
      log.info("+++ testHttps");
      int port = Integer.getInteger("secureweb.port", 8443).intValue();
      port += 1500;
      String httpsNoAuth = "https://" + getServerHost() + ":" + port + "/";
      doHttps(httpsNoAuth);
   }
   

   private void doHttpRedirect(String httpNoAuth) throws Exception
   {
      log.info("+++ testHttpRedirect, httpNoAuth="+httpNoAuth);
      // Start by accessing the secured index.html of war1
      HttpClient httpConn = new HttpClient();
      String url = httpNoAuth+"clientcert-auth/unrestricted/SecureServlet";
      log.info("Accessing: "+url);
      GetMethod get = new GetMethod(url);
      int responseCode = httpConn.executeMethod(get);
      String status = get.getStatusText();
      log.debug(status);
      assertTrue("Get OK("+responseCode+")", responseCode == HttpURLConnection.HTTP_OK);
   }
   public void doHttps(String httpsNoAuth) throws Exception
   {
      log.info("+++ doHttps, httpsNoAuth="+httpsNoAuth);
      // Start by accessing the secured index.html of war1
      HttpClient httpConn = new HttpClient();
      String url = httpsNoAuth+"clientcert-auth/unrestricted/SecureServlet";
      log.info("Accessing: "+url);
      GetMethod get = new GetMethod(url);
      int responseCode = httpConn.executeMethod(get);
      String status = get.getStatusText();
      log.debug(status);
      assertTrue("Get OK("+responseCode+")", responseCode == HttpURLConnection.HTTP_OK);
   } 
   
   /** One time setup for all SingleSignOnUnitTestCase unit tests
    */
   public static Test suite() throws Exception
   {
      Test suite = getDeploySetup(SSLUnitTestCase.class, "clientcert-auth.war");
      return suite;
   }
}