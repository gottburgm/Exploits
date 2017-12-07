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

import junit.framework.Test;

import org.apache.commons.httpclient.HttpClient;
import org.jboss.test.cluster.testutil.SessionTestUtil;
import org.jboss.test.cluster.testutil.WebTestBase;
import org.jboss.test.cluster.web.JBossClusteredWebTestCase;

/**
 * Tests handling of the session timestamp.
 *
 * @author Ben Wang
 * @author Brian Stansberry
 * @version $Revision: 1.0
 */
public class SessionTimestampTestCase
      extends WebTestBase
{

   public SessionTimestampTestCase(String name)
   {
      super(name);

   }

   public static Test suite() throws Exception
   {
      return JBossClusteredWebTestCase.getDeploySetup(SessionTimestampTestCase.class,
                                                      "http-sr.war");
   }
   
   /**
    * Tests that sessions time out properly and that activity
    * on one cluster node prevents timeout on another.
    * 
    * The session is configured by testsessionreplication.jsp to expire
    * after 10 seconds. It is configured by jboss-web.xml with a 
    * maxUnreplicatedInterval of 8 secs.
    */
   public void testSessionTimeout()
      throws Exception
   {
      getLog().debug("Enter testSessionTimeout");

      String setURLName = "/http-sr/testsessionreplication.jsp";
      String getURLName = "/http-sr/getattribute.jsp";

      getLog().debug(setURLName + ":::::::" + getURLName);

      // Create first session on server0
      HttpClient clientA = new HttpClient();
      makeGet(clientA, baseURL0_ +setURLName);

      sleepThread(3000);
      
      // Update the first session to ensure timestamp replication occurs
      makeGetWithState(clientA, baseURL0_ +setURLName);
      // Get the Attribute set by testsessionreplication.jsp
      String attrA0 = makeGetWithState(clientA, baseURL0_ +getURLName);
      assertNotNull("Http session get", attrA0);

      sleepThread(1100);
      
      // Create a 2nd session
      HttpClient clientB = new HttpClient();
      makeGet(clientB, baseURL0_ +setURLName);
      // Get the Attribute set by testsessionreplication.jsp
      String attrB0 = makeGetWithState(clientB, baseURL0_ +getURLName);
      assertNotNull("Http session get", attrB0);
      
      // Sleep 7 secs.  This plus the previous 1.1+3 secs is enough to expire
      // first session on server1 if replication failed to keep it alive      
      sleepThread(7000);
      
      // Switch to the other server and check if 1st session is alive
      // This session has a create time > 8 secs ago, so this will trigger
      // replication of the timestamp
      SessionTestUtil.setCookieDomainToThisServer(clientA, servers_[1]);
      String attrA1 = makeGetWithState(clientA, baseURL1_ +getURLName);
      assertEquals("Http session replication attributes retrieved from both servers ", attrA0, attrA1);
      
      // Switch to the other server and check if 2nd session is alive
      // This session has a create time < 8 secs ago, so this should not
      // trigger replication of the timestamp      
      SessionTestUtil.setCookieDomainToThisServer(clientB, servers_[1]);
      String attrB1 = makeGetWithState(clientB, baseURL1_ +getURLName);
      assertEquals("Http session replication attributes retrieved from both servers ", attrB0, attrB1);
      
      getLog().debug("Replication has kept the sessions alive");
      
      // sleep 2 more seconds so 1st session will expire on server0 if the
      // get didn't replicate the timestamp
      sleepThread(2000);  
      
      // Confirm first session is alive on node 0
      SessionTestUtil.setCookieDomainToThisServer(clientA, servers_[0]);
      attrA0 = makeGetWithState(clientA, baseURL0_ +getURLName);
      assertTrue("Original session A is present", attrA1.equals(attrA0));  
      
      // sleep 1.1 more secs. Last activity for second session on server 0
      // will have been 1.1 + 2 + 7 = 10.1 secs ago, so it should be expired
      // on server 0
      sleepThread(1100);
      
      // Confirm 2nd session is not alive on node 0
      SessionTestUtil.setCookieDomainToThisServer(clientB, servers_[0]);
      attrB0 = makeGetWithState(clientB, baseURL0_ +getURLName);
      assertFalse("Original session B not alive", attrB1.equals(attrB0));
      
      getLog().debug("Exit testSessionTimeout");
   }
   
   /**
    * Tests that a request before maxUnreplicatedInterval has passed doesn't
    * trigger replication.
    */
   public void testMaxUnreplicatedInterval() throws IOException
   {
      getLog().debug("Enter testMaxUnreplicatedInterval");

      String setURLName = "/http-sr/testsessionreplication.jsp";
      String getURLName = "/http-sr/getattribute.jsp";
      String versionURLName = "/http-sr/version.jsp";

      // Create an instance of HttpClient.
      HttpClient client = new HttpClient();

      // Set the session attribute first
      makeGet(client, baseURL0_ +setURLName);
      // Get the Attribute set by testsessionreplication.jsp
      String attr = makeGetWithState(client, baseURL0_ +getURLName);
      
      // Sleep a bit, but less than the 16 sec maxUnreplicatedInterval
      sleepThread(500);
      
      // Access the session without touching any attribute
      String ver = makeGetWithState(client, baseURL0_ +versionURLName);
      
      // Sleep some more, long enough for the session replication to complete
      // if the last request incorrectly caused replication 
      sleepThread(2000);
      
      // Switch servers
      SessionTestUtil.setCookieDomainToThisServer(client, servers_[1]);      
      String ver1 = makeGetWithState(client, baseURL1_ +versionURLName);
      
      assertEquals("Session version count unchanged", ver, ver1);
      
      // Get the Attribute set by testsessionreplication.jsp
      String attr1 = makeGetWithState(client, baseURL1_ +getURLName);
      
      assertEquals("Session still present", attr, attr1);
   }

}
