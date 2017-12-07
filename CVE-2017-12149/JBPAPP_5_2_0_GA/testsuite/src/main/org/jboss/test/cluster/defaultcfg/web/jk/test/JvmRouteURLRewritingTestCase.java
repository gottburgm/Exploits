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

package org.jboss.test.cluster.defaultcfg.web.jk.test;

import java.io.IOException;
import java.net.HttpURLConnection;

import junit.framework.Test;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.test.cluster.testutil.WebTestBase;
import org.jboss.test.cluster.web.JBossClusteredWebTestCase;

/**
 * Tests that failover works properly, with an updated jvmRoute portion
 * of the session id, when JK is used but session cookies are not.
 * 
 * @author Brian Stansberry
 */
public class JvmRouteURLRewritingTestCase extends WebTestBase
{

   public JvmRouteURLRewritingTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return JBossClusteredWebTestCase.getDeploySetup(JvmRouteURLRewritingTestCase.class,
                                                      "http-jk.war");
   }
   
   public void testJkFailoverWithURLRewriting() throws Exception
   {
      getLog().debug("Enter testJkFailoverWithURLRewriting()");
      
      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();
      
      String sessId0 = makeGetWithRequestCount(client, baseURL0_, null, "1");
      
      // Fail over to node1
      String sessId1 = makeGetWithRequestCount(client, baseURL1_, sessId0, "2");
      
      // Fail back over to node0
      makeGetWithRequestCount(client, baseURL0_, sessId1, "3");
   }
   
   private String makeGetWithRequestCount(HttpClient client, 
                                          String urlBase,
                                          String sessionId,
                                          String expectedCount)
      throws IOException
   {
      String url = urlBase + "/http-jk/accessSession.jsp";
      if (sessionId != null)
      {
         url = url + ";jsessionid=" + sessionId;
      }
      
      GetMethod method = new GetMethod(url);
      int responseCode = 0;
      try
      {
         responseCode = client.executeMethod(method);
      } 
      catch (IOException e)
      {
         e.printStackTrace();
         fail("HttpClient executeMethod fails." +e.toString());
      }
      assertTrue("Get OK with url: " +url + " responseCode: " +responseCode
        , responseCode == HttpURLConnection.HTTP_OK);      
      
      // Validate that the request count is as expected,
      // proving that state replication happened
      Header hdr = method.getResponseHeader("X-TestRequestCount");
      assertNotNull("Got the X-TestRequestCount header", hdr);
      assertEquals("X-TestRequestCount header is correct", expectedCount, hdr.getValue());
      
      // Find out the session id
      hdr = method.getResponseHeader("X-TestJSessionID");
      assertNotNull("Got the X-TestJSessionID header", hdr);      
      String id = hdr.getValue();
      
      if (sessionId != null)
      {
         // Check the real session id is correct
         assertEquals("Real session id is correct", stripJvmRoute(sessionId), 
                                                    stripJvmRoute(id));
         // Check the session id has changed
         assertFalse("Session id has changed", sessionId.equals(id));
      }
      
      // Read the response body and confirm the URL was properly encoded.
      byte[] responseBody = method.getResponseBody();
      String body = new String(responseBody);      
      String expectedURL = "accessSession.jsp;jsessionid=" + id;
      assertTrue("URL encoded properly: " + body, body.indexOf(expectedURL) > -1);
      
      return id;
   }

}
