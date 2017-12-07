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
package org.jboss.test.cluster.testutil;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.test.cluster.web.JBossClusteredWebTestCase;

/**
 * Base class to test HttpSessionReplication.
 *
 * @author Ben Wang
 * @version $Revision: 1.0
 */
public abstract class WebTestBase
      extends JBossClusteredWebTestCase
{
   /** 
    * Standard number of ms to pause between http requests
    * to give session time to replicate
    */
   public static final long DEFAULT_SLEEP = 300;
   
   protected String[] servers_ = null;
   protected String baseURL0_;
   protected String baseURL1_;

   public WebTestBase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      servers_ = super.getServers();
      assertEquals("Server size " , servers_.length, 2);

      String[] httpURLs  = super.getHttpURLs();
      assertEquals("Url size " , httpURLs.length, 2);
      baseURL0_ = httpURLs[0];
      baseURL1_ = baseURL0_;
      if( servers_.length > 1 )
      {
        baseURL1_ = httpURLs[1];
      }
   }

   /**
    * Sleep for specified time
    *
    * @param msecs
    */
   protected void sleepThread(long msecs)
   {
      try {
         Thread.sleep(msecs);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
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
   protected String makeGet(HttpClient client, String url)
      throws IOException
   {
      getLog().debug("makeGet(): trying to get from url " +url);

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

      // Read the response body.
      byte[] responseBody = method.getResponseBody();

      // Release the connection.
//      method.releaseConnection();

      // Deal with the response.
      // Use caution: ensure correct character encoding and is not binary data
      return new String(responseBody);
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
   protected String makeGetFailed(HttpClient client, String url)
      throws IOException
   {
      getLog().debug("makeGetFailed(): trying to get from url " +url);

      GetMethod method = new GetMethod(url);
      int responseCode = 0;
      try
      {
         responseCode = client.executeMethod(method);
      } catch (IOException e)
      {
         e.printStackTrace();
      }
      assertTrue("Should not be OK code with url: " +url + " responseCode: " +responseCode
        , responseCode != HttpURLConnection.HTTP_OK);

      // Read the response body.
      byte[] responseBody = method.getResponseBody();

      // Release the connection.
//      method.releaseConnection();

      // Deal with the response.
      // Use caution: ensure correct character encoding and is not binary data
      return new String(responseBody);
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
   protected String makeGetWithState(HttpClient client, String url)
      throws IOException
   {
      getLog().debug("makeGetWithState(): trying to get from url " +url);
      GetMethod method = new GetMethod(url);
      int responseCode = 0;
      try
      {
         HttpState state = client.getState();
         responseCode = client.executeMethod(method.getHostConfiguration(),
            method, state);
      } catch (IOException e)
      {
         e.printStackTrace();
         fail("HttpClient executeMethod fails." +e.toString());
      }
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);

      // Read the response body.
      byte[] responseBody = method.getResponseBody();
      /* Validate that the attribute was actually seen. An absence of the
         header is treated as true since there are pages used that done't
         add it.
      */
      Header hdr = method.getResponseHeader("X-SawTestHttpAttribute");
      Boolean sawAttr = hdr != null ? Boolean.valueOf(hdr.getValue()) : Boolean.TRUE;
      String attr = null;
      if( sawAttr.booleanValue() )
         attr = new String(responseBody);
      // Release the connection.
//      method.releaseConnection();

      // Deal with the response.
      // Use caution: ensure correct character encoding and is not binary data
      return attr;
   }
   
   protected String getSessionID(HttpClient client, String server)
   {
      Cookie sessionID = getSessionCookie(client, server);
      return sessionID.getValue();
   }
   
   protected Cookie getSessionCookie(HttpClient client, String server)
   {
      Cookie sessionID = SessionTestUtil.getSessionCookie(client);
      if(sessionID == null)
      {
         fail("setCookieDomainToThisServer(): fail to find session id. Server name: " +server);
      }
      log.info("Saw JSESSIONID="+sessionID);
      return sessionID;
   }
   
   protected String stripJvmRoute(String id)
   {
      int index = id.indexOf(".");
      if (index > 0)
      {
         return id.substring(0, index);
      }
      else
      {
         return id;
      }
   }
}
