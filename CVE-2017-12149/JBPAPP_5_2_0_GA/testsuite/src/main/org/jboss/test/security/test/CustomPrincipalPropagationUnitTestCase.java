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
package org.jboss.test.security.test;

import java.net.HttpURLConnection;
import java.util.HashMap;

import javax.rmi.PortableRemoteObject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;

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
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.security.ejb.CustomPrincipalImpl;
import org.jboss.test.security.interfaces.CustomPrincipal;
import org.jboss.test.security.interfaces.CustomPrincipalHome; 
import org.jboss.test.util.AppCallbackHandler;

//$Id: CustomPrincipalPropagationUnitTestCase.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

/**
 *  Test propagation of Custom Principal
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Sep 22, 2006 
 *  @version $Revision: 85945 $
 */
public class CustomPrincipalPropagationUnitTestCase extends JBossTestCase
{ 
   static String username = "jduke";
   static char[] password = "theduke".toCharArray();
   
   LoginContext lc;
   boolean loggedIn;
   
   public CustomPrincipalPropagationUnitTestCase(String name)
   {
      super(name); 
   }
   
   /**
    * Custom Principal from outside the Application Server VM
    * @throws Exception
    */
   public void testCustomPrincipalTransmission() throws Exception
   {
      Configuration.setConfiguration(new MyConfig()); 
      login();
      Object obj = getInitialContext().lookup("jaas.CustomPrincipalHome");
      obj = PortableRemoteObject.narrow(obj, CustomPrincipalHome.class);
      CustomPrincipalHome home = (CustomPrincipalHome) obj;
      log.debug("Found CustomPrincipalHome");
      CustomPrincipal bean = home.create();
      log.debug("Created CustomPrincipal");

      boolean isCustomType = bean.validateCallerPrincipal(CustomPrincipalImpl.class);
      bean.remove();
      logout();
      assertTrue("CustomPrincipalImpl was seen", isCustomType);
   }  
   
   /**
    * A web-app has a welcome jsp (called as index.jsp). Inside this jsp,
    * there is a call made out to an ejb
    * 
    * @throws Exception
    */
   public void testCustomPrincipalTransmissionInVM() throws Exception
   { 
      String baseURLNoAuth = "http://" + getServerHost() + ":" + Integer.getInteger("web.port", 8080) + "/";
      HttpClient httpConn = new HttpClient();
      GetMethod indexGet = new GetMethod(baseURLNoAuth + "custom-principal/");
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
      PostMethod formPost = new PostMethod(baseURLNoAuth + "custom-principal/j_security_check");
      formPost.addRequestHeader("Referer", baseURLNoAuth + "custom-principal/login.jsp");
      formPost.addParameter("j_username", this.username);
      formPost.addParameter("j_password", new String(password));
      responseCode = httpConn.executeMethod(formPost.getHostConfiguration(), formPost, state);
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
      responseCode = httpConn.executeMethod(war1Index.getHostConfiguration(),
            war1Index, state);
      response = war1Index.getStatusText();
      log.debug("responseCode="+responseCode+", response="+response);
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
      body = war1Index.getResponseBodyAsString();
      log.debug("Final result obtained:"+body);
      if( body.indexOf("j_security_check") > 0 )
         fail("get of "+indexURI+" redirected to login page"); 
      if( body.indexOf("Propagation Success") < 0 )
         fail("Propagation of custom principal within VM failed");  
   }

   /** Login as user scott using the conf.name login config or
   'jaas-test' if conf.name is not defined.
   */
  private void login() throws Exception
  {
     login(username, password);
  }
  private void login(String username, char[] password) throws Exception
  {
     if( loggedIn )
        return;
     
     lc = null;
     String confName = System.getProperty("conf.name", "jaas-test");
     AppCallbackHandler handler = new AppCallbackHandler(username, password);
     log.debug("Creating LoginContext("+confName+")");
     lc = new LoginContext(confName, handler);
     lc.login();
     log.debug("Created LoginContext, subject="+lc.getSubject());
     loggedIn = true;
  }
  private void logout() throws Exception
  {
     if( loggedIn )
     {
        loggedIn = false;
        lc.logout();
     }
  }
  
  /**
   * Setup the test suite.
   */
  public static Test suite() throws Exception
  {
     TestSuite suite = new TestSuite();
     suite.addTest(new TestSuite(CustomPrincipalPropagationUnitTestCase.class));

     // Create an initializer for the test suite
     TestSetup wrapper = new JBossTestSetup(suite)
     {
        protected void setUp() throws Exception
        {
           super.setUp();
           Configuration.setConfiguration(XMLLoginConfigImpl.getInstance());
           deploy("security-jaas.ear");
           flushAuthCache("jaas-test");
           flushAuthCache("jaas-testpropagation");
        }
        protected void tearDown() throws Exception
        {
           undeploy("security-jaas.ear");
           super.tearDown();
        
        }
     };
     return wrapper;
  }
  
  static class MyConfig extends Configuration
  {
     AppConfigurationEntry[] entry;
     MyConfig()
     {
        entry = new AppConfigurationEntry[2];
        HashMap opt0 = new HashMap();
        opt0.put("principal", new CustomPrincipalImpl(username));
        opt0.put("credential", password);
        opt0.put("password-stacking", "useFirstPass"); 
        entry[0] = new AppConfigurationEntry("org.jboss.test.security.ejb.CustomPrincipalLoginModule", 
              AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, opt0);
        entry[1] = new AppConfigurationEntry("org.jboss.security.ClientLoginModule", 
              AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, opt0);
     }

     public AppConfigurationEntry[] getAppConfigurationEntry(String appName)
     {
        return entry;
     }
     public void refresh()
     {
     }
  }

}
