/*
* JBoss, a division of Red Hat
* Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

import javax.management.MBeanServerConnection;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.cluster.web.CacheHelper;


/** Tests of web app single sign-on in a clustered environment
 * 
 * @author Brian Stansberry
 * @version $Revision: 76165 $
 */
public class ClusteredSingleSignOnUnitTestCase 
      extends JBossClusteredTestCase
{   
   // NOTE: these variables must be static as apparently a separate instance
   // of this class is created for each test
   private static boolean deployed0 = true;
   private static boolean deployed1 = true;
   
   private MBeanServerConnection[] adaptors = null;
   
   public ClusteredSingleSignOnUnitTestCase(String name)
   {
      super(name);
   }

   /** One time setup for all ClusteredSingleSignOnUnitTestCase unit tests
    */
   public static Test suite() throws Exception
   {
      // Have to build the suite in detail, as testSessionExpiration must come first
      TestSuite suite = new TestSuite();
      suite.addTest(new ClusteredSingleSignOnUnitTestCase("testSessionExpiration"));
      suite.addTest(new ClusteredSingleSignOnUnitTestCase("testFormAuthSingleSignOn"));
      suite.addTest(new ClusteredSingleSignOnUnitTestCase("testNoAuthSingleSignOn"));
      suite.addTest(new ClusteredSingleSignOnUnitTestCase("testUndeployNonClusteredWebapp"));
      suite.addTest(new ClusteredSingleSignOnUnitTestCase("testUndeployClusteredWebapp"));
      
      return JBossClusteredTestCase.getDeploySetup(suite, "web-sso-clustered.ear");
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      
      log.debug("deployed0 = " + deployed0);
      log.debug("deployed1 = " + deployed1);
      
      adaptors = getAdaptors(); 
      if (!deployed0)
      {
         deploy(adaptors[0], "web-sso-clustered.ear");
         deployed0 = true;
      }
      if (!deployed1)
      {
         deploy(adaptors[1], "web-sso-clustered.ear");
         deployed1 = true;
      }
   }
   
   protected void tearDown() throws Exception
   {
      super.tearDown();
      
      log.debug("deployed0 = " + deployed0);
      log.debug("deployed1 = " + deployed1);
   }
   
   public void testUndeployNonClusteredWebapp() throws Exception
   {
      log.info("+++ testUndeployNonClusteredWebapp");
      
      webappUndeployTest("war4", "war5");
   }
   
   public void testUndeployClusteredWebapp() throws Exception
   {
      log.info("+++ testUndeployClusteredWebapp");
      
      webappUndeployTest("war1", "war2");
   }
   
   /**
    * Tests that undeploying a webapp on one server doesn't kill an sso
    * that also has a session from another webapp associated with it.
    * See JBAS-2429.
    * 
    * TODO create an independently deployable war so we can test this in
    *      a non-clustered environment as well; this isn't a clustering issue 
    *
    * @throws Exception
    */
   private void webappUndeployTest(String firstWar, String secondWar) throws Exception
   {
      String[] httpURLs  = super.getHttpURLs();

      String serverA = httpURLs[0];
      String serverB = httpURLs[1];
      
      String warA1 = serverA + "/" + firstWar +"/";
      String warB1 = serverB + "/" + firstWar +"/";
      String warB2 = serverB + "/" + secondWar +"/";
      
      // Start by accessing the secured index.html of war1
      HttpClient httpConn = new HttpClient();
      SSOBaseCase.checkAccessDenied(httpConn,  warA1 + "index.html");

      HttpState state = httpConn.getState();
      
      String sessionID = SSOBaseCase.getSessionIdFromState(state);
      log.debug("Saw JSESSIONID="+sessionID);
      
      // Submit the login form
      SSOBaseCase.executeFormLogin(httpConn, warA1);

      String ssoID = SSOBaseCase.processSSOCookie(state, serverA, serverB);
      log.debug("Saw JSESSIONIDSSO="+ssoID);

      // Pause a moment before switching wars to better simulate real life
      // use cases.  Otherwise, the test case can "outrun" the async
      // replication in the TreeCache used by the clustered SSO
      // 500 ms is a long time, but this isn't a test of replication speed
      // and we don't want spurious failures.
      if (!serverA.equals(serverB))
         Thread.sleep(500);

      // Now try getting the war2 index using the JSESSIONIDSSO cookie 
      log.debug("Prepare /war2/index.html get");
      SSOBaseCase.checkAccessAllowed(httpConn, warB2 + "index.html");

      // Sleep some more to allow the updated sso to propagate back to serverA
      if (!serverA.equals(serverB))
         Thread.sleep(500);
      
      // We now have a clustered sso context, plus a war1 session on
      // serverA and a war2 session on serverB. No war1 session on serverB,
      // so the only way to access war1 on B without a login is through sso.
      
      //Undeploy the ear from serverA and confirm that it doesn't kill the sso
      undeploy(adaptors[0], "web-sso-clustered.ear");
      deployed0 = false;

      // Sleep some more to allow the updated sso to propagate back to serverB
      if (!serverA.equals(serverB))
         Thread.sleep(500);
      
      // Now try getting the war1 index using the JSESSIONIDSSO cookie 
      log.debug("Prepare /war1/index.html get");
      SSOBaseCase.checkAccessAllowed(httpConn, warB1 + "index.html");
   }


   /** Test single sign-on across two web apps using form based auth
    * 
    * @throws Exception
    */ 
   public void testFormAuthSingleSignOn() throws Exception
   {
      log.info("+++ testFormAuthSingleSignOn");
      String[] httpURLs  = super.getHttpURLs();

      String serverA = httpURLs[0];
      String serverB = httpURLs[1];
      log.info(System.getProperties());
      log.info("serverA: "+serverA);
      log.info("serverB: "+serverB);
      SSOBaseCase.executeFormAuthSingleSignOnTest(serverA, serverB, getLog());
   }
   
   /** Test single sign-on across two web apps using form based auth
    * 
    * @throws Exception
    */ 
   public void testNoAuthSingleSignOn() throws Exception
   {
      log.info("+++ testNoAuthSingleSignOn");
      String[] httpURLs  = super.getHttpURLs();

      String serverA = httpURLs[0];
      String serverB = httpURLs[1];
      log.info(System.getProperties());
      log.info("serverA: "+serverA);
      log.info("serverB: "+serverB);
      SSOBaseCase.executeNoAuthSingleSignOnTest(serverA, serverB, getLog());
   }
   
   /** 
    * Tests that use of transactions in ClusteredSSO does not interfere 
    * with session expiration thread.  See JBAS-2212.
    * 
    * @throws Exception
    */ 
   public void testSessionExpiration() 
         throws Exception
   {
      log.info("+++ testSessionExpiration");
      String[] httpURLs  = super.getHttpURLs();

      String serverA = httpURLs[0];
      String serverB = httpURLs[1];
      log.info(System.getProperties());
      log.info("serverA: "+serverA);
      log.info("serverB: "+serverB);
      
      String warA3 = serverA + "/war3/";
      String warB3 = serverB + "/war3/";
      
      // First create an SSO that we won't use again -- we later test that it
      // gets cleaned up from the cache
      HttpClient httpConn1 = new HttpClient();
      SSOBaseCase.checkAccessDenied(httpConn1, warA3 + "index.jsp");
      SSOBaseCase.executeFormLogin(httpConn1, warA3);
      HttpState state = httpConn1.getState();      
      String sessionID1 = SSOBaseCase.getSessionIdFromState(state);
      
      // Now the standard SSO tests
      HttpClient httpConn = new HttpClient();
      SSOBaseCase.checkAccessDenied(httpConn, warA3 + "index.jsp");
      state = httpConn.getState();      
      String sessionID = SSOBaseCase.getSessionIdFromState(state);
      log.debug("Saw JSESSIONID="+sessionID);
      SSOBaseCase.executeFormLogin(httpConn, warA3);
      String ssoID = SSOBaseCase.processSSOCookie(state, serverA, serverB);
      log.debug("Saw JSESSIONIDSSO="+ssoID);

      // Wait more than 15 secs to let session time out
      // It's life is 5 secs and the processor runs every 10
      try {
         Thread.sleep(15500);
      }
      catch (InterruptedException ie)
      {
         log.debug("Interrupted while waiting for session expiration");
      }
      
      // Try accessing war3 again on both nodes -- should succeed
      SSOBaseCase.checkAccessAllowed(httpConn, warA3 + "index.jsp");
      SSOBaseCase.checkAccessAllowed(httpConn, warB3 + "index.jsp");

      // Wait more than 30 secs to let SSO time out -- 15 for the sessions
      // (as above) and 15 for the SSOs maxEmptyLife
      try {
         Thread.sleep(30500);
      }
      catch (InterruptedException ie)
      {
         log.debug("Interrupted while waiting for SSO expiration");
      }
      
      // Try accessing war3 again on both nodes -- should fail
      SSOBaseCase.checkAccessDenied(httpConn, warA3 + "index.jsp");
      SSOBaseCase.checkAccessDenied(httpConn, warB3 + "index.jsp");
      
      // Confirm that the SSO we created at the start was removed from the cache
      assertFalse("node0 cache does not have SSO " + sessionID1,
                  getCacheHasSSO(adaptors[0], sessionID1));
      assertFalse("node1 cache does not have SSO " + sessionID1,
            getCacheHasSSO(adaptors[1], sessionID1));
   }
   
   private boolean getCacheHasSSO(MBeanServerConnection adaptor ,String ssoId)
         throws Exception
   {
      adaptor.invoke(CacheHelper.OBJECT_NAME, 
                     "setCacheConfigName", 
                     new Object[] { "clustered-sso", Boolean.FALSE }, 
                     new String[] { String.class.getName(), boolean.class.getName() });
      
      Boolean b = (Boolean) adaptor.invoke(CacheHelper.OBJECT_NAME, "getCacheHasSSO", 
                            new Object[] { ssoId }, 
                            new String[] { String.class.getName() });
      return b.booleanValue();
   }
}
