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

import junit.framework.Test;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.test.cluster.multicfg.web.test.ScopedTestCase;
import org.jboss.test.cluster.web.JBossClusteredWebTestCase;

public class CrossContextCallsTestCase extends ScopedTestCase
{

   public CrossContextCallsTestCase(String name)
   {
      super(name);
      warName_ = "/http-cross-ctx-first/";
      
      concatenate();
   }

   public static Test suite() throws Exception
   {
      return JBossClusteredWebTestCase.getDeploySetup(CrossContextCallsTestCase.class,
                                                      "http-cross-ctx.ear");
   }
   
   
   /**
    * Disabled; no-op.
    */
   public void testExcludeSecuritySubject() throws Exception
   {
      return;
   }

   /**
    * Disabled; no-op.
    */
   public void testSessionBindingEvent() throws Exception
   {
      return;
   }

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
      
      String response = extractResponse(method);
   
      // Release the connection.
      // method.releaseConnection();
      
      return response;
   }
   
   protected String getWarName()
   {
      return "http-cross-ctx-first";
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
      assertTrue("Get OK with url: " +url + " responseCode: " +responseCode, 
                 responseCode == HttpURLConnection.HTTP_OK);
      
      String response = extractResponse(method);
   
      // Release the connection.
      // method.releaseConnection();
      
      return response;
   }
   
   private String extractResponse(GetMethod method)
      throws IOException
   {
      Header header = method.getResponseHeader("FIRST");
   
      assertNotNull("Received FIRST header", header);
      
      String result = header.getValue();
      
      assertNotNull("FIRST header not null", result);
      
      header = method.getResponseHeader("SECOND");
      
      assertNotNull("Received SECOND header", header);
      
      String second = header.getValue();
      
      assertNotNull("FIRST header not null", second);
      
      result.concat(second);
      
      // Read the response body.
      byte[] responseBody = method.getResponseBody();
      // Use caution: ensure correct character encoding and is not binary data
      result.concat(new String(responseBody));
   
      // Release the connection.
      //   method.releaseConnection();
      
      return result;
      
   }

}
