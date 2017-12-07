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

import java.io.IOException;
import java.net.HttpURLConnection;

import junit.framework.TestCase;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jboss.logging.Logger;

/** 
 * Base class for tests of web app single sign-on
 * 
 * @author Brian Stansberry
 * @version $Revision: 76164 $
 */
public abstract class SSOBaseCase extends TestCase
{ 
   /** Test single sign-on across two web apps using form based auth
    * 
    * @throws Exception
    */ 
   protected static void executeFormAuthSingleSignOnTest(String serverA, 
                                                  String serverB,
                                                  Logger log) 
         throws Exception
   {
      String warA1 = serverA + "/war1/";
      String warB2 = serverB + "/war2/";
      
      // Start by accessing the secured index.html of war1
      HttpClient httpConn = new HttpClient();
      
      checkAccessDenied(httpConn, warA1 + "index.html");

      HttpState state = httpConn.getState();
      
      String sessionID = getSessionIdFromState(state);
      log.debug("Saw JSESSIONID="+sessionID);

      // Submit the login form
      executeFormLogin(httpConn, warA1);

      String ssoID = processSSOCookie(state, serverA, serverB);
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
      checkAccessAllowed(httpConn, warB2 + "index.html");

      /* Access a secured servlet that calls a secured ejb in war2 to test
      propagation of the SSO identity to the ejb container. */
      checkAccessAllowed(httpConn, warB2 + "EJBServlet");

      // Now try logging out of war2 
      executeLogout(httpConn, warB2);
      
      // Again, pause before switching wars
      if (!serverA.equals(serverB))
         Thread.sleep(500);
      
      // Try accessing war1 again      
      checkAccessDenied(httpConn, warA1 + "index.html");
      
      // Try accessing war2 again      
      checkAccessDenied(httpConn, warB2 + "index.html");      
      
   }

   protected static void executeNoAuthSingleSignOnTest(String serverA, String serverB, Logger log)
         throws Exception
   {
      String warA1 = serverA + "/war1/";
      String warB2 = serverB + "/war2/";
      String warB6 = serverB + "/war6/";
      
      // Start by accessing the secured index.html of war1
      HttpClient httpConn = new HttpClient();

      checkAccessDenied(httpConn, warA1 + "index.html");

      HttpState state = httpConn.getState();

      String sessionID = getSessionIdFromState(state);
      log.debug("Saw JSESSIONID=" + sessionID);

      // Submit the login form
      executeFormLogin(httpConn, warA1);

      String ssoID = processSSOCookie(state, serverA, serverB);
      log.debug("Saw JSESSIONIDSSO=" + ssoID);

      // Pause a moment before switching wars to better simulate real life
      // use cases.  Otherwise, the test case can "outrun" the async
      // replication in the TreeCache used by the clustered SSO
      // 500 ms is a long time, but this isn't a test of replication speed
      // and we don't want spurious failures.
      if (!serverA.equals(serverB))
         Thread.sleep(500);

      // Now try getting the war2 index using the JSESSIONIDSSO cookie 
      log.debug("Prepare /war2/index.html get");
      checkAccessAllowed(httpConn, warB2 + "index.html");

      /* Access a secured servlet that calls a secured ejb in war2 to test
       propagation of the SSO identity to the ejb container. */
      checkAccessAllowed(httpConn, warB2 + "EJBServlet");
      
      /* do the same test on war6 to test SSO auth replication with no 
         auth configured war */ 
      checkAccessAllowed(httpConn, warB6 + "index.html");
      
      checkAccessAllowed(httpConn, warB2 + "EJBServlet");

   }

   public static void executeLogout(HttpClient httpConn, String warURL) throws IOException, HttpException
   {
      GetMethod logout = new GetMethod(warURL + "Logout");
      logout.setFollowRedirects(false);
      int responseCode = httpConn.executeMethod(logout.getHostConfiguration(),
         logout, httpConn.getState());
      assertTrue("Logout: Saw HTTP_MOVED_TEMP("+responseCode+")",
         responseCode == HttpURLConnection.HTTP_MOVED_TEMP);
      Header location = logout.getResponseHeader("Location");
      String indexURI = location.getValue();
      if( indexURI.indexOf("index.html") < 0 )
         fail("get of " + warURL + "Logout not redirected to login page");
   }

   public static void checkAccessAllowed(HttpClient httpConn, String url) throws IOException, HttpException
   {
      GetMethod war2Index = new GetMethod(url);
      int responseCode = httpConn.executeMethod(war2Index.getHostConfiguration(),
         war2Index, httpConn.getState());
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
      String body = war2Index.getResponseBodyAsString();
      if( body.indexOf("j_security_check") > 0 )
         fail("get of " + url + " redirected to login page");
   }

   public static void executeFormLogin(HttpClient httpConn, String warURL) throws IOException, HttpException
   {      
      PostMethod formPost = new PostMethod(warURL + "j_security_check");
      formPost.addRequestHeader("Referer", warURL + "login.html");
      formPost.addParameter("j_username", "jduke");
      formPost.addParameter("j_password", "theduke");
      int responseCode = httpConn.executeMethod(formPost.getHostConfiguration(),
         formPost, httpConn.getState());
      assertTrue("Saw HTTP_MOVED_TEMP("+responseCode+")",
         responseCode == HttpURLConnection.HTTP_MOVED_TEMP);

      //  Follow the redirect to the index.html page
      Header location = formPost.getResponseHeader("Location");
      String indexURI = location.getValue();
      GetMethod warIndex = new GetMethod(indexURI);
      responseCode = httpConn.executeMethod(warIndex.getHostConfiguration(),
         warIndex, httpConn.getState());
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
      String body = warIndex.getResponseBodyAsString();
      if( body.indexOf("j_security_check") > 0 )
         fail("get of "+indexURI+" redirected to login page");
   }

   public static void checkAccessDenied(HttpClient httpConn, String url) throws IOException, HttpException
   {
      GetMethod indexGet = new GetMethod(url);
      int responseCode = httpConn.executeMethod(indexGet);
      String body = indexGet.getResponseBodyAsString();
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
      assertTrue("Redirected to login page", body.indexOf("j_security_check") > 0 );
   }

   public static String processSSOCookie(HttpState state, String serverA, String serverB)
   {
      Cookie[] cookies = state.getCookies();
      String ssoID = null;
      for(int c = 0; c < cookies.length; c ++)
      {
         if( "JSESSIONIDSSO".equalsIgnoreCase(cookies[c].getName()) )
         {
            ssoID = cookies[c].getValue();
            if (serverA.equals(serverB) == false) 
            {
               // Make an sso cookie to send to serverB
               Cookie copy = copyCookie(cookies[c], serverB);
               state.addCookie(copy);
            }
         }
      }
      
      assertNotNull("Saw JSESSIONIDSSO", ssoID);
      
      return ssoID;
   }
   
   public static Cookie copyCookie(Cookie toCopy, String targetServer)
   {
      // Parse the target server down to a domain name
      int index = targetServer.indexOf("://");
      if (index > -1)
      {
         targetServer = targetServer.substring(index + 3);
      }
      index = targetServer.indexOf(":");
      if (index > -1)
      {
         targetServer = targetServer.substring(0, index);         
      }
      index = targetServer.indexOf("/");
      if (index > -1)
      {
         targetServer = targetServer.substring(0, index);
      }
      
      Cookie copy = new Cookie(targetServer,
                               toCopy.getName(),
                               toCopy.getValue(),
                               "/",
                               null,
                               false);
      return copy;
   }
   
   public static String getSessionIdFromState(HttpState state)
   {
      Cookie[] cookies = state.getCookies();
      String sessionID = null;
      for(int c = 0; c < cookies.length; c ++)
      {
         if( "JSESSIONID".equalsIgnoreCase(cookies[c].getName()) )
            sessionID = cookies[c].getName();
      }
      return sessionID;
      
   }
}
