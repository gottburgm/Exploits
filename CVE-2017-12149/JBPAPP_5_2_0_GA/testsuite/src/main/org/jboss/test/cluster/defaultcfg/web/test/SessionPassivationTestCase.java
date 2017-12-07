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
package org.jboss.test.cluster.defaultcfg.web.test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.Set;

import javax.management.MBeanServerConnection;

import junit.framework.Test;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.test.cluster.testutil.SessionTestUtil;
import org.jboss.test.cluster.testutil.WebTestBase;
import org.jboss.test.cluster.web.CacheHelper;
import org.jboss.test.cluster.web.JBossClusteredWebTestCase;

/**
 * Session passivation tests.
 *
 * @author Brian Stansberry
 * @version $Revision: 1.0
 */
public class SessionPassivationTestCase
      extends WebTestBase
{
   private static boolean deployed0 = true;
   private static boolean deployed1 = true;
   
   protected String setUrl;
   protected String getUrl;
   protected String invalidateUrl;
   protected String warName_;
   protected String setUrlBase_;
   protected String getUrlBase_;
   protected String invalidateUrlBase_;
   private String warFqn_;
   private boolean usingBuddyReplication_;
   
   private Set<HttpClient> clients = new HashSet<HttpClient>();

   public SessionPassivationTestCase(String name)
   {
      super(name);
      warName_ = "/http-session-pass/";
      setUrlBase_ = "setSession.jsp";
      getUrlBase_ = "getAttribute.jsp";
      invalidateUrlBase_ = "invalidateSession.jsp";
      
      concatenate();
   }

   protected void concatenate()
   {
      setUrl = warName_ + setUrlBase_;
      getUrl = warName_ + getUrlBase_;
      invalidateUrl = warName_ + invalidateUrlBase_;
   }
   
   protected String getWarName()
   {
      return "http-session-pass";
   }

   public static Test suite() throws Exception
   {
      return JBossClusteredWebTestCase.getDeploySetup(SessionPassivationTestCase.class,
                                                      "http-session-pass.war");
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      
      MBeanServerConnection[] adaptors = getAdaptors();      
      if (!deployed0)
      {
         deploy(adaptors[0]);
         deployed0 = true;
      }
      if (!deployed1)
      {
         deploy(adaptors[1]);
         deployed1 = true;
      }
      
      String br = System.getProperty("jbosstest.cluster.web.cache.br");
      usingBuddyReplication_ = Boolean.parseBoolean(br);
   }

   protected void tearDown() throws Exception
   {
      super.tearDown();
      
      // Invalidate any sessions to leave the cache clean
      for (HttpClient client : clients)
      {
         try
         {
            SessionTestUtil.setCookieDomainToThisServer(client, servers_[0]);
            makeGet(client, baseURL0_ +invalidateUrl);
         }
         catch (Exception e)
         {
            log.error("Failed clearing client " + client, e);
         }
      }
      
      clients.clear();
   }
   
   protected boolean isCleanCacheOnRedeploy()
   {
      return usingBuddyReplication_;
   }
   
   /**
    * Tests the ability to passivate session when max idle for session is reached
    * 
    * @throws Exception
    */
   public void testSessionPassivationWMaxIdle() throws Exception
   {
      getLog().debug("Enter testSessionPassivationWMaxIdle");

      getLog().debug(setUrl + ":::::::" + getUrl);

      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();
      clients.add(client);
      // Set the session attribute first
      makeGet(client, baseURL0_ +setUrl);
      
      // Get the Attribute set
      String attr0 = checkDeserialization(client, baseURL0_ +getUrl, false);
      
      // sleep up to 16 secs to allow max idle to be reached 
      // and tomcat background process to run
      // assuming that max idle in jboss-web.xml = 5 secs
      // and tomcat background process is using the default = 10 secs
      sleepThread(16000);
      
      // activate the session by requesting the attribute
      // Make connection to server 0 and get
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[0]);
      String attr2 = checkDeserialization(client, baseURL0_ + getUrl, true);
      
      assertEquals("attribute match after activation", attr0, attr2);
   }
   
   /**
    * Tests the ability to passivate session when max number of active sessions reached
    * 
    * @throws Exception
    */
   public void testSessionPassivationWMaxActive() throws Exception
   {
      getLog().debug("Enter testSessionPassivationWMaxActive");

      getLog().debug(setUrl + ":::::::" + getUrl);

      MBeanServerConnection[] adaptors = getAdaptors();
      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();
      clients.add(client);
      // Set the session attribute first
      makeGet(client, baseURL0_ +setUrl);
      
      // Get the Attribute set; confirm session wasn't deserialized
      String attr0 = checkDeserialization(client, baseURL0_ +getUrl, false);
      
      // passivation-min-idle-time is set to 2 secs, so sleep that
      // long so our session can be passivated
      sleepThread(2100);
      
      // Create enough sessions on server0 to trigger passivation
      // assuming that max-active-sessions is set to 10 in jboss-web.xml
      for (int i = 0; i < 10; i++)
      {
         HttpClient newClient = new HttpClient();
         clients.add(newClient);
         makeGet(newClient, baseURL0_ +setUrl);
         makeGet(newClient, baseURL0_ +getUrl);
      }
      
      // access the session and confirm that it was deserialized (passivated/activated)      
      String attr2 = checkDeserialization(client, baseURL0_ + getUrl, true);
      
      assertEquals("attribute match after activation", attr0, attr2);      
   }
   
   /**
    * Tests the ability to passivate sessions on undeployment of an application
    * and to activate it on restart or redeployment of an application
    * @throws Exception
    */
   public void testRedeploy() throws Exception
   {
       getLog().info("Enter testRedeploy");
       
       getLog().debug(setUrl + ":::::::" + getUrl);
       
       // Undeploy from server1 to ensure that a redeploy on server0
       // results in sessions coming from disk, not server1
       MBeanServerConnection[] adaptors = getAdaptors();
       deployed1 = false;
       undeploy(adaptors[1]);
       
       if (!isCleanCacheOnRedeploy())
       {
          // Make sure the cache is not stopped during redeploy due to
          // no services using it. Tell CacheHelper to hold a ref.
          String cacheConfigName = System.getProperty(CacheHelper.CACHE_CONFIG_PROP, "standard-session-cache");
          String usePojoCache = System.getProperty(CacheHelper.CACHE_TYPE_PROP, "false");
          SessionTestUtil.setCacheConfigName(adaptors[0], cacheConfigName, Boolean.parseBoolean(usePojoCache));
       }
       
       sleep(2000);
          
       // Create an instance of HttpClient.
       HttpClient client = new HttpClient();
       clients.add(client);
       
       // Set the session attribute first
       makeGet(client, baseURL0_ +setUrl);
       
       //   Get the Attribute set
       String attr = makeGetWithState(client, baseURL0_ +getUrl);
       
       // undeploy server0, which passivates the session to the distributed store
       deployed0 = false;
       undeploy(adaptors[0]);       

       sleep(2000);    
       // redeploy the application on server 0
       deploy(adaptors[0]);
       deployed0 = true;

       sleep(2000);
       
       // Get the Attribute using the same session ID
       SessionTestUtil.setCookieDomainToThisServer(client, servers_[0]);
       
       if (isCleanCacheOnRedeploy())
       {
          // We don't expect data to survive a restart
          makeGetFailed(client, baseURL0_ +getUrl);
       }
       else
       {                 
          String attr0 = makeGet(client, baseURL0_ +getUrl);          
          assertEquals("attributeMatches after activation", attr0, attr);
       }
       
       getLog().debug("Exit testRedeploy");
       
   }
   
   private void deploy(MBeanServerConnection adaptor) throws Exception
   {
      deploy(adaptor, getWarName() + ".war");   
   }
   
   private void undeploy(MBeanServerConnection adaptor) throws Exception
   {
      undeploy(adaptor, getWarName() + ".war");   
   }
   
   /**
    * Makes a http call to the jsp that retrieves the attribute stored on the
    * session. When the attribute values mathes with the one retrieved earlier,
    * we have HttpSessionReplication.
    * Makes use of commons-httpclient library of Apache
    *
    * @param client
    * @param url
    * @return session attribute
    */
   protected String checkDeserialization(HttpClient client, String url, boolean expectDeserialized)
      throws IOException
   {
      getLog().debug("checkDeserialization(): trying to get from url " +url);

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
      
      Header hdr = method.getResponseHeader("X-SessionDeserialzied");
      Boolean sawAttr = hdr != null ? Boolean.valueOf(hdr.getValue()) : Boolean.FALSE;
      assertEquals("Session deserialization as expected", expectDeserialized, sawAttr.booleanValue());
      
      // Read the response body.
      byte[] responseBody = method.getResponseBody();

      // Release the connection.
//      method.releaseConnection();

      // Deal with the response.
      // Use caution: ensure correct character encoding and is not binary data
      return new String(responseBody);
   }
}
