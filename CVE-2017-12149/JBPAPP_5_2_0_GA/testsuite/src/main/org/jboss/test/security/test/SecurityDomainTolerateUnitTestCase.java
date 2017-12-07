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

import javax.rmi.PortableRemoteObject;
import javax.security.auth.login.Configuration;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jboss.security.auth.login.XMLLoginConfigImpl;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.security.interfaces.StatelessSession;
import org.jboss.test.security.interfaces.StatelessSessionHome;

/**
 * JBAS-6060: Tolerate security domain in web and ejb2
 * deployment descriptors with no java:/jaas prefix
 * @author Anil.Saldhana@redhat.com
 * @since Oct 20, 2008
 */
public class SecurityDomainTolerateUnitTestCase extends JBossTestCase
{
   private static String login_config = "security/sdtolerate/sdtolerate-jboss-beans.xml";
   
   private String username = "harry";
   private String password = "potter";
   
   public SecurityDomainTolerateUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testEJB() throws Exception
   { 
      log.debug("+++ testEJB");
      Object obj = getInitialContext().lookup("null.StatelessSession");
      obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
      StatelessSessionHome home = (StatelessSessionHome) obj;
      log.debug("Found null.StatelessSession Home");
      StatelessSession bean = null;
      try
      {
         bean = home.create(); 
         fail("Invoking create() should fail");
      }
      catch (Exception e)
      {
         Throwable t = e.getCause();
         if (t instanceof SecurityException)
         {
            log.debug("Invoking create() was correctly denied by a SecurityException:", e); 
         }
         else
         {
            log.debug("Invoking create() failed by an unexpected reason:", e);
            fail("Unexpected exception");
         }
      }
      SecurityClient client = SecurityClientFactory.getSecurityClient();
      client.setSimple(username, password);
      client.login();
      try
      {
         bean = home.create(); 
         bean.echo("hi");
      }
      catch(Exception e)
      {
         fail(e.getLocalizedMessage());
      }
   }
   
   public void testWeb() throws Exception
   {
      String baseURLNoAuth = "http://" + getServerHost() + 
                     ":" + Integer.getInteger("web.port", 8080) + "/";
      HttpClient httpConn = new HttpClient();
      GetMethod indexGet = new GetMethod(baseURLNoAuth + "sdtolerate/");
      int responseCode = httpConn.executeMethod(indexGet);
      String body = indexGet.getResponseBodyAsString();
      assertTrue("Get OK(" + responseCode + ")", responseCode == HttpURLConnection.HTTP_OK);
      assertTrue("Redirected to login page", body.indexOf("j_security_check") > 0);
      HttpState state = httpConn.getState();
      Cookie[] cookies = state.getCookies();
      String sessionID = null;
      for (int c = 0; c < cookies.length; c++)
      {
         Cookie k = cookies[c];
         if (k.getName().equalsIgnoreCase("JSESSIONID"))
            sessionID = k.getValue();
      }
      getLog().debug("Saw JSESSIONID=" + sessionID);
      // Submit the login form
      PostMethod formPost = new PostMethod(baseURLNoAuth + "sdtolerate/j_security_check");
      formPost.addRequestHeader("Referer", baseURLNoAuth + "sdtolerate/login.jsp");
      formPost.addParameter("j_username", this.username);
      formPost.addParameter("j_password", new String(password));
      responseCode = httpConn.executeMethod(formPost);
      String loginResult = formPost.getResponseBodyAsString();
      if( loginResult.indexOf("Encountered a login error") > 0 )
         fail("Login Failed"); 

      String response = formPost.getStatusText();
      log.debug("responseCode="+responseCode+", response="+response);
      assertTrue("Saw HTTP_MOVED_TEMP", responseCode == HttpURLConnection.HTTP_MOVED_TEMP);

      //  Follow the redirect to the index.jsp
      Header location = formPost.getResponseHeader("Location");
      String indexURI = location.getValue();
      GetMethod war1Index = new GetMethod(indexURI);
      responseCode = httpConn.executeMethod(war1Index);
      response = war1Index.getStatusText();
      log.debug("responseCode="+responseCode+", response="+response);
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK); 
   }
   
   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(SecurityDomainTolerateUnitTestCase.class));

      // Create an initializer for the test suite
      TestSetup wrapper = new JBossTestSetup(suite)
      {
         @Override
         protected void setUp() throws Exception
         {
            super.setUp();
            Configuration.setConfiguration(XMLLoginConfigImpl.getInstance());
            redeploy("sdtolerate.ear");
            redeploy(getResourceURL(login_config));
            flushAuthCache();
         }

         @Override
         protected void tearDown() throws Exception
         {
            undeploy(getResourceURL(login_config));
            undeploy("sdtolerate.ear");
            super.tearDown();
         }
      };
      return wrapper;
   }

}