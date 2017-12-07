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

import java.net.HttpURLConnection;

import junit.framework.Test;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.test.cluster.testutil.WebTestBase;
import org.jboss.test.cluster.web.JBossClusteredWebTestCase;

/** Tests of http session replication
 *
 * @author  Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class WebSessionTestCase extends JBossClusteredWebTestCase
{
   /** 
    * Standard number of ms to pause between http requests
    * to give session time to replicate
    */
   public static final long DEFAULT_SLEEP = WebTestBase.DEFAULT_SLEEP;
   
   public WebSessionTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(WebSessionTestCase.class, "dist-ss.war");
   }

   /** This makes 2 requests to the jbosstest.cluster.node0 /dist-ss/StatefulSessionServlet
    * followed by 2 requests to the jbosstest.cluster.node1 /dist-ss/StatefulSessionServlet
    * using the same session ID to validate that the session is replicated
    * with the current value and updated correctly. The session AccessCount
    * value is returned via the X-AccessCount header which should be 4 after
    * the last request.
    * 
    * @throws Exception
    */ 
   public void testServletSessionFailover()
      throws Exception
   {
      getLog().debug("+++ testServletSessionFailover");

      String[] servers = super.getServers();
      String[] httpURLs  = super.getHttpURLs();
      // Access the StatefulSessionServlet of dist-ss.war@server0 twice
      String baseURL0 = httpURLs[0];
      HttpClient httpConn = new HttpClient();
      GetMethod servletGet0 = new GetMethod(baseURL0+"/dist-ss/StatefulSessionServlet/");
      int responseCode = httpConn.executeMethod(servletGet0);
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
      Header accessCount = servletGet0.getResponseHeader("X-AccessCount");
      int count = Integer.parseInt(accessCount.getValue());
      assertEquals("X-AccessCount ", 1, count);
      // Get the state for the JSESSIONID
      HttpState state = httpConn.getState();
      servletGet0 = new GetMethod(baseURL0+"/dist-ss/StatefulSessionServlet/");
      responseCode = httpConn.executeMethod(servletGet0.getHostConfiguration(),
         servletGet0, state);
      accessCount = servletGet0.getResponseHeader("X-AccessCount");
      count = Integer.parseInt(accessCount.getValue());
      assertEquals("X-AccessCount ", 2, count);
      // Get the JSESSIONID so we can reset the host
      Cookie[] cookies = state.getCookies();
      Cookie sessionID = null;
      for(int c = 0; c < cookies.length; c ++)
      {
         Cookie k = cookies[c];
         if( k.getName().equalsIgnoreCase("JSESSIONID") )
            sessionID = k;
      }
      log.info("Saw JSESSIONID="+sessionID);
      // Reset the domain so that the cookie will be sent to server1
      sessionID.setDomain(servers[1]);
      state.addCookie(sessionID);
      _sleep(DEFAULT_SLEEP);

      // Access the StatefulSessionServlet of dist-ss.war@server1 twice
      String baseURL1 = httpURLs[1];
      GetMethod servletGet1 = new GetMethod(baseURL1+"/dist-ss/StatefulSessionServlet/");
      responseCode = httpConn.executeMethod(servletGet1.getHostConfiguration(),
         servletGet1, state);
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
      accessCount = servletGet1.getResponseHeader("X-AccessCount");
      count = Integer.parseInt(accessCount.getValue());
      assertEquals("X-AccessCount ", 3, count);
      servletGet1 = new GetMethod(baseURL1+"/dist-ss/StatefulSessionServlet/");
      responseCode = httpConn.executeMethod(servletGet1.getHostConfiguration(),
         servletGet1, state);
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
      accessCount = servletGet1.getResponseHeader("X-AccessCount");
      count = Integer.parseInt(accessCount.getValue());
      assertEquals("X-AccessCount ", 4, count);
   }

   /** This makes 4 requests alternating between the jbosstest.cluster.node0
    * /dist-ss/StatefulSessionServlet and jbosstest.cluster.node1 /dist-ss/StatefulSessionServlet
    * using the same session ID to validate that the session is replicated
    * with the current value and updated correctly. The session AccessCount
    * value is returned via the X-AccessCount header which should be 4 after
    * the last request.
    * 
    * Note: this test is not currently working since current http session
    * replication assumes sticky session. It does not support random load
    * balancing.
    * bwang.
    *
    * @throws Exception
    */ 
   public void testServletSessionLoadBalancing()
      throws Exception
   {
      getLog().debug("+++ testServletSessionLoadBalancing");

      String[] servers = getServers();
      String[] httpURLs  = super.getHttpURLs();
      String baseURL0 = httpURLs[0];
      String baseURL1 = baseURL0;
      if( servers.length > 1 )
      {
        baseURL1 = httpURLs[1];
      }
      // Access the StatefulSessionServlet of dist-ss.war@server0 twice
      HttpClient httpConn = new HttpClient();
      GetMethod servletGet0 = new GetMethod(baseURL0+"/dist-ss/StatefulSessionServlet/");
      int responseCode = httpConn.executeMethod(servletGet0);
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
      Header accessCount = servletGet0.getResponseHeader("X-AccessCount");
      int count = Integer.parseInt(accessCount.getValue());
      assertEquals("X-AccessCount ", 1, count);
      // Get the state for the JSESSIONID
      HttpState state = httpConn.getState();
      // Get the JSESSIONID so we can reset the host
      Cookie[] cookies = state.getCookies();
      Cookie sessionID = null;
      for(int c = 0; c < cookies.length; c ++)
      {
         Cookie k = cookies[c];
         if( k.getName().equalsIgnoreCase("JSESSIONID") )
            sessionID = k;
      }
      log.info("Saw JSESSIONID="+sessionID);
      // Reset the domain so that the cookie will be sent to server1
      sessionID.setDomain(servers[1]);
      state.addCookie(sessionID);
      _sleep(DEFAULT_SLEEP);
      // Access the StatefulSessionServlet of dist-ss.war@server1 twice
      GetMethod servletGet1 = new GetMethod(baseURL1+"/dist-ss/StatefulSessionServlet/");
      responseCode = httpConn.executeMethod(servletGet1.getHostConfiguration(),
         servletGet1, state);
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
      accessCount = servletGet1.getResponseHeader("X-AccessCount");
      count = Integer.parseInt(accessCount.getValue());
      assertEquals("X-AccessCount ", 2, count);

      _sleep(DEFAULT_SLEEP);
      // Reset the domain so that the cookie will be sent to server0
      sessionID.setDomain(servers[0]);
      state.addCookie(sessionID);
      servletGet0 = new GetMethod(baseURL0+"/dist-ss/StatefulSessionServlet/");
      responseCode = httpConn.executeMethod(servletGet0.getHostConfiguration(),
         servletGet0, state);
      accessCount = servletGet0.getResponseHeader("X-AccessCount");
      count = Integer.parseInt(accessCount.getValue());
      assertEquals("X-AccessCount ", 3, count);

      _sleep(DEFAULT_SLEEP);
      // Reset the domain so that the cookie will be sent to server1
      sessionID.setDomain(servers[1]);
      state.addCookie(sessionID);
      servletGet1 = new GetMethod(baseURL1+"/dist-ss/StatefulSessionServlet/");
      responseCode = httpConn.executeMethod(servletGet1.getHostConfiguration(),
         servletGet1, state);
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
      accessCount = servletGet1.getResponseHeader("X-AccessCount");
      count = Integer.parseInt(accessCount.getValue());
      assertEquals("X-AccessCount ", 4, count);
   }


   /**
    * Sleep for specified time
    *
    * @param msecs
    */
   protected void _sleep(long msecs)
   {
      try {
         Thread.sleep(msecs);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
   }

}
