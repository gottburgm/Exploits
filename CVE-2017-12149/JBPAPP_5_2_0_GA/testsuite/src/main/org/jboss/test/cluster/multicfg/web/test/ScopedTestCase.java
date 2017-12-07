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
package org.jboss.test.cluster.multicfg.web.test;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.management.ObjectName;

import junit.framework.Test;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jboss.test.cluster.testutil.SessionTestUtil;
import org.jboss.test.cluster.testutil.WebTestBase;
import org.jboss.test.cluster.web.JBossClusteredWebTestCase;

/**
 * Clustering test case of get/set under scoped class loader.
 *
 * @author Ben Wang
 * @version $Revision: 1.0
 */
public class ScopedTestCase
      extends WebTestBase
{
   protected String setUrl;
   protected String getUrl;
   protected String modifyUrl;
   protected String modifyNoSetUrl;
   protected String removeUrl;
   protected String invalidateUrl;
   protected String clearUrl;
   protected String attrListUrl;
   protected String bindUrl_;
   protected String protectedUrl_;
   protected String securityCheckUrl_;
   protected String loginFormUrl_;
   protected String setSecuritySubjectUrl_;
   protected String getSecuritySubjectUrl_;
   protected String warName_;
   protected String setUrlBase_;
   protected String getUrlBase_;
   protected String modifyUrlBase_;
   protected String modifyNoSetUrlBase_;
   protected String removeUrlBase_;
   protected String invalidateUrlBase_;
   protected String clearUrlBase_;
   protected String bindUrlBase_;
   protected String attrListUrlBase_;
   protected String protectedUrlBase_;
   protected String securityCheckUrlBase_;
   protected String loginFormUrlBase_;
   protected String setSecuritySubjectUrlBase_;
   protected String getSecuritySubjectUrlBase_;
   
   private ObjectName warObjectName;

   public ScopedTestCase(String name)
   {
      super(name);
      warName_ = "/http-scoped/";
      setUrlBase_ = "setSession.jsp";
      getUrlBase_ = "getAttribute.jsp";
      modifyUrlBase_ = "modifyAttribute.jsp";
      modifyNoSetUrlBase_ = "modifyAttributeNoSet.jsp";
      removeUrlBase_ = "removeAttribute.jsp";
      invalidateUrlBase_ = "invalidateSession.jsp";
      clearUrlBase_ = "clearCache.jsp";
      bindUrlBase_ = "bindSession.jsp?Binding=";
      protectedUrlBase_ = "index.html";
      securityCheckUrlBase_ = "j_security_check";
      loginFormUrlBase_ = "login.html";
      attrListUrlBase_ = "attributeNames.jsp";
      setSecuritySubjectUrlBase_  = "setSecuritySubject.jsp";
      getSecuritySubjectUrlBase_ =  "getSecuritySubject.jsp";

      concatenate();
   }

   protected void concatenate()
   {
      setUrl = warName_ +setUrlBase_;
      getUrl = warName_ +getUrlBase_;
      modifyUrl = warName_ +modifyUrlBase_;
      modifyNoSetUrl = warName_ +modifyNoSetUrlBase_;
      removeUrl = warName_ +removeUrlBase_;
      invalidateUrl = warName_ +invalidateUrlBase_;
      clearUrl = warName_ +clearUrlBase_;
      bindUrl_ = warName_ + bindUrlBase_;
      protectedUrl_ = warName_ + protectedUrlBase_;
      securityCheckUrl_ = warName_ + securityCheckUrlBase_;
      loginFormUrl_ = warName_ + loginFormUrlBase_;
      attrListUrl = warName_ + attrListUrlBase_;
      setSecuritySubjectUrl_ = warName_ + setSecuritySubjectUrlBase_;
      getSecuritySubjectUrl_ = warName_ + getSecuritySubjectUrlBase_;
   }
   
   protected String getWarName()
   {
      return "http-scoped";
   }

   public static Test suite() throws Exception
   {
      return JBossClusteredWebTestCase.getDeploySetup(ScopedTestCase.class,
                                                   "http-scoped.war");
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      
      if (warObjectName == null)
      {
         String oname = "jboss.web:J2EEApplication=none,J2EEServer=none," +
                        "j2eeType=WebModule,name=//localhost/" + getWarName();
         warObjectName = new ObjectName(oname);
      }
   }

   protected void tearDown() throws Exception
   {
//      clear();
      super.tearDown();
   }

   /**
    * Main method that deals with the Http Session Replication Test
    *
    * @throws Exception
    */
   public void testNonPrimitiveGet()
         throws Exception
   {
      String attr = "";
      getLog().debug("Enter testNonPrimitiveGet");

      getLog().debug(setUrl + ":::::::" + getUrl);

      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Set the session attribute first
      makeGet(client, baseURL0_ +setUrl);

      // Create a method instance.
      // Get the Attribute set
      attr = makeGet(client, baseURL0_ +getUrl);

      sleepThread(DEFAULT_SLEEP);

      // Make connection to server 1 and get
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[1]);
      String attr2 = makeGet(client, baseURL1_ +getUrl);

      assertEquals("Get attribute should be but is ", attr, attr2);
      getLog().debug("Exit testNonPrimitiveGet");
   }

   /**
    * Test session modify with non-primitive get/modify.
    *
    * @throws Exception
    */
   public void testNonPrimitiveModify()
         throws Exception
   {
      String attr = "";
      getLog().debug("Enter testNonPrimitiveModify");

      getLog().debug(setUrl + ":::::::" + getUrl);

      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Set the session attribute first
      makeGet(client, baseURL0_ +setUrl);

      // Get the Attribute set
      String attrOld = makeGet(client, baseURL0_ +getUrl);

      // Modify a method instance.
      makeGet(client, baseURL0_ +modifyUrl);

      // Get the Attribute set
      attr = makeGet(client, baseURL0_ +getUrl);

      sleepThread(DEFAULT_SLEEP);

      // Make connection to server 1 and get
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[1]);
      String attr2 = makeGet(client, baseURL1_ +getUrl);

      // Check the result
      assertFalse("Old attribute should be different from new one.",
                  attrOld.equals(attr));
      assertEquals("Attributes should be the same", attr, attr2);
      getLog().debug("Exit testNonPrimitiveModify");
   }

   /**
    * Test session modify with non-primitive get/modify.
    *
    * @throws Exception
    */
   public void testNonPrimitiveRepeatedModify()
         throws Exception
   {
      String attr = "";
      getLog().info("Enter testNonPrimitiveRepeatedModify");

      getLog().debug(setUrl + ":::::::" + getUrl);

      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Set the session attribute first
      makeGet(client, baseURL0_ +setUrl);

      // Get the Attribute set
      String attrOld = makeGetWithState(client, baseURL0_ +getUrl);

      // Modify a method instance.
      makeGet(client, baseURL0_ +modifyUrl);

      // Get the Attribute set
      attr = makeGetWithState(client, baseURL0_ +getUrl);

      sleepThread(DEFAULT_SLEEP);

      // Make connection to server 1 and get
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[1]);
      String attr2 = makeGetWithState(client, baseURL1_ +getUrl);

      // Check the result
      assertFalse("Old attribute should be different from new one.",
                  attrOld.equals(attr));
      assertEquals("Attributes should be the same", attr, attr2);


      // Modify a method instance.
      makeGet(client, baseURL1_ +modifyUrl);

      // Get the Attribute set
      attr = makeGetWithState(client, baseURL1_ +getUrl);

      sleepThread(DEFAULT_SLEEP);

      // Make connection to server 1 and get
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[0]);
      attr2 = makeGetWithState(client, baseURL0_ +getUrl);

      // Check the result
      assertEquals("Attributes should be the same after second modify", attr, attr2);
      getLog().debug("Exit testNonPrimitiveRepeatedModify");
   }

   /**
    * Test session modify with non-primitive remove.
    *
    * @throws Exception
    */
   public void testNonPrimitiveRemove()
         throws Exception
   {
      getLog().debug("Enter testNonPrimitiveRemove");

      getLog().debug(setUrl + ":::::::" + getUrl);

      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Set the session attribute first
      makeGet(client, baseURL0_ +setUrl);

      // Modify a method instance.
      makeGet(client, baseURL0_ +modifyUrl);

      // Get the Attribute set
      makeGet(client, baseURL0_ +getUrl);

      // Get the list of attributes
      String attrList = makeGet(client, baseURL0_ +attrListUrl);
      
      assertTrue("TEST_PERSON is an attribute", attrList.indexOf("TEST_PERSON") > -1);

      sleepThread(DEFAULT_SLEEP);

      // Make connection to server 1 and get
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[1]);
      // Get the Attribute set
      makeGet(client, baseURL1_ +getUrl);      
      // Get the list of attributes
      String attrList1 = makeGet(client, baseURL1_ +attrListUrl);
      
      assertTrue("TEST_PERSON is an attribute on server1", attrList1.indexOf("TEST_PERSON") > -1);
      
      // Remove the attribute
      makeGet(client, baseURL1_ +removeUrl);
      // Attribute is now null. Should have not OK response.
      makeGetFailed(client, baseURL1_ +getUrl);

      // Confirm the attribute is gone from the list
      attrList1 = makeGet(client, baseURL1_ +attrListUrl);
      
      assertTrue("TEST_PERSON should not be an attribute", attrList1.indexOf("TEST_PERSON") == -1);
      
      sleepThread(DEFAULT_SLEEP);
      // Make connection to server 1 and get
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[0]);
      // Attribute is now null. Should have not OK response.
      makeGetFailed(client, baseURL0_ +getUrl);

      // Confirm the attribute is gone from the list
      attrList = makeGet(client, baseURL0_ +attrListUrl);
      
      assertTrue("TEST_PERSON is not an attribute on server0", attrList.indexOf("TEST_PERSON") == -1);

      getLog().debug("Exit testNonPrimitiveRemove");
   }

   /**
    * Test session modify with non-primitive get/modify from node2 and see if it replicates correctly
    * on node1 or not.
    *
    * @throws Exception
    */
   public void testNonPrimitiveModifyFromAlternativeNode()
         throws Exception
   {
      String attr = "";
      getLog().debug("Enter testNonPrimitiveModifyFromAlternativeNode");

      getLog().debug(setUrl + ":::::::" + getUrl);

      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Set the session attribute first
      makeGet(client, baseURL0_ +setUrl);

      // Get the Attribute set
      String attrOld = makeGet(client, baseURL0_ +getUrl);
      
      sleepThread(DEFAULT_SLEEP);
      
      // Switch to server 1 and get the attribute
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[1]);
      String attrOld1 = makeGet(client, baseURL1_ +getUrl);
      
      assertEquals("Attributes should be the same", attrOld, attrOld1);

      // Modify the attribute.
      makeGet(client, baseURL1_ +modifyUrl);
      String attr2 = makeGet(client, baseURL1_ +getUrl);

      sleepThread(400);
      
      // Switch back to server 0 and get the attribute
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[0]);
      attr = makeGet(client, baseURL0_ +getUrl);

      assertEquals("Attributes should be the same", attr2, attr);
      getLog().debug("Exit testNonPrimitiveModifyModifyFromAlternativeNode");
   }

   /**
    * Test invalidate session
    *
    * @throws Exception
    */
   public void testInvalidate()
         throws Exception
   {
      getLog().debug("Enter testInvalidate");

      getLog().debug(setUrl + ":::::::" + getUrl);

      invalidate();
      
      getLog().debug("Exit testInvalidate");  
   }
   
   public void testSessionBindingEvent()
      throws Exception
   {
      String attr = "";
      getLog().debug("Enter testSessionBindingEvent");

      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();
      
      // Bind a new HttpSessionListener to the session
      // and check that there is a valueBound() event
      attr = makeGet(client, baseURL0_ + bindUrl_ + "new");
      assertTrue("Got OK when binding a new listener", 
                 (attr != null && attr.indexOf("OK") >= 0 ) );
      
      // Rebind the same HttpSessionListener to the session
      // and check that there is no valueUnbound()
      attr = makeGet(client, baseURL0_ + bindUrl_ + "rebind");
      assertTrue("Got OK when rebinding an existing listener", 
                 (attr != null && attr.indexOf("OK") >= 0 ) );
      
      // Replace the HttpSessionListener with another one
      // and check that there is a valueUnbound()
      attr = makeGet(client, baseURL0_ + bindUrl_ + "replace");
      assertTrue("Got OK when replacing a listener", 
                 (attr != null && attr.indexOf("OK") >= 0 ) );
      
      // Remove the same HttpSessionListener
      // and check that there is a valueUnbound()
      attr = makeGet(client, baseURL0_ + bindUrl_ + "remove");
      assertTrue("Got OK when removing a listener", 
                 (attr != null && attr.indexOf("OK") >= 0 ) );
   }
   
   public void testExcludeSecuritySubject() throws Exception
   {
      getLog().debug("Enter testExcludeSecuritySubject");
   
      getLog().debug(setSecuritySubjectUrl_ + ":::::::" + getSecuritySubjectUrl_);
   
      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();
   
      // Set the session attribute first
      makeGet(client, baseURL0_ +setSecuritySubjectUrl_);
   
      // Confirm the attribute is available from the server where it was set
      String attrOrig = makeGet(client, baseURL0_ +getSecuritySubjectUrl_);
      assertTrue("javax.security.auth.subject available locally", 
            attrOrig.indexOf("javax.security.auth.Subject") > -1);
      
      sleepThread(DEFAULT_SLEEP);
      
      // Check if the attribute replicated
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[1]);
      String attrRepl = makeGet(client, baseURL1_ +getSecuritySubjectUrl_);
      assertTrue("javax.security.Subject did not replicate", attrRepl.indexOf("java.lang.String") > -1);
   }
   
   /**
    * Test for JBAS-3528 (http://jira.jboss.com/jira/browse/JBAS-3528).
    * 
    * @throws Exception
    */
   public void testIsNew() throws Exception
   {
      getLog().debug("Enter testIsNew");

      getLog().debug(setUrl + ":::::::" + getUrl);

      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Set the session attribute first
      makeGet(client, baseURL0_ +setUrl);
      
      sleepThread(DEFAULT_SLEEP);

      // Let's switch to server 2 to retrieve the session attribute.
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[1]);
      assertFalse("Session is not new", checkNew(client, baseURL1_ + getUrl));
      
      sleepThread(DEFAULT_SLEEP);

      // Let's switch to server 1 to ensure its correct there as well.
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[0]);
      assertFalse("Session is not new", checkNew(client, baseURL0_ + getUrl));      
   }


   /**
    * Makes a http call to the given url and confirms that a non-null
    * header X-SessionIsNew is returned.  Converts the value
    * of the header to a boolean and returns it.
    *
    * @param client
    * @param url
    */
   protected boolean checkNew(HttpClient client, String url)
   {
      getLog().info("checkNew(): trying to get from url " +url);

      GetMethod method = new GetMethod(url);
      int responseCode = 0;
      try
      {
         responseCode = client.executeMethod(method);
      } catch (IOException e)
      {
         e.printStackTrace();
         fail("HttpClient executeMethod fails." +e.toString());
      }
      assertTrue("Get OK with url: " +url + " responseCode: " +responseCode
        , responseCode == HttpURLConnection.HTTP_OK);

      Header hdr = method.getResponseHeader("X-SessionIsNew");
      assertNotNull("Got X-SessionIsNew header", hdr);
      String value = hdr.getValue();
      assertNotNull("Got non-nullX-SessionIsNew header", value);
      
      return Boolean.valueOf(value).booleanValue();
   }

   protected void invalidate() throws Exception
   {
      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Set the session attribute first
      makeGet(client, baseURL0_ +setUrl);

      // Get the Attribute set
      String attr0 = makeGet(client, baseURL0_ +getUrl);

      sleepThread(DEFAULT_SLEEP);
      
      // Make connection to server 1 and get
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[1]);
      String attr1 = makeGet(client, baseURL1_ + getUrl);
      
      assertEquals("attributes match", attr0, attr1);
      
      // Invalidate the session
      makeGet(client, baseURL1_ +invalidateUrl);

      sleepThread(DEFAULT_SLEEP + 200); // wait a bit longer to propagate

      // Make connection to server 0 and get
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[0]);
      // Session is invalidated. Should have not OK response.
      makeGetFailed(client, baseURL0_ + getUrl);
   }

   protected void clear() throws Exception
   {
      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Set the session attribute first
      makeGet(client, baseURL0_ +clearUrl);
   }

   /** 
    * Test ability for a FORM auth based app to have failover without requiring
    * a new sign-on
    * 
    * This test will not pass until a FormAuthenticator that makes
    * use of cached usernames and passwords is available.
    * 
    * @throws Exception
    */ 
   public void badtestFormAuthFailover() throws Exception
   {
      log.info("+++ testFormAuthFailover");
      
      // Start by accessing the war's protected url
      HttpClient client = new HttpClient();
      
      String body = makeGet(client, baseURL0_ + protectedUrl_);
      if( body.indexOf("j_security_check") < 0 )
         fail("get of "+protectedUrl_+" not redirected to login page");
      
      HttpState state = client.getState();
      Cookie[] cookies = state.getCookies();
      String sessionID = null;
      for(int c = 0; c < cookies.length; c ++)
      {
         Cookie k = cookies[c];
         if( k.getName().equalsIgnoreCase("JSESSIONID") )
            sessionID = k.getValue();
      }
      log.debug("Saw JSESSIONID="+sessionID);

      // Submit the login form
      PostMethod formPost = new PostMethod(baseURL0_ + securityCheckUrl_);
      formPost.addRequestHeader("Referer", baseURL0_ + loginFormUrl_);
      formPost.addParameter("j_username", "admin");
      formPost.addParameter("j_password", "admin");
      int responseCode = client.executeMethod(formPost.getHostConfiguration(),
         formPost, state);
      String response = formPost.getStatusText();
      log.debug("responseCode="+responseCode+", response="+response);
      assertTrue("Saw HTTP_MOVED_TEMP("+responseCode+")",
         responseCode == HttpURLConnection.HTTP_MOVED_TEMP);

      //  Follow the redirect to the index.html page
      body = makeGet(client, baseURL0_ + protectedUrl_);
      if( body.indexOf("j_security_check") > 0 )
         fail("get of "+baseURL0_ + protectedUrl_+" redirected to login page");

      // Switch to the second server
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[1]);

      sleepThread(DEFAULT_SLEEP);
      
      // Now try getting the protected url on the second server 
      body = makeGet(client, baseURL1_ + protectedUrl_);
      if( body.indexOf("j_security_check") > 0 )
         fail("get of "+baseURL1_ + protectedUrl_+" redirected to login page");       
   }
}
