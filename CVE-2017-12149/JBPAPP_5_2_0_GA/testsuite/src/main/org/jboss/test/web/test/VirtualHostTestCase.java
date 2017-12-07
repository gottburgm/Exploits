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
package org.jboss.test.web.test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import junit.framework.Test;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.web.HttpUtils;

import com.meterware.httpunit.HttpException;

/**
 * Test virtual hosts. 
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85945 $
 */
public class VirtualHostTestCase extends JBossTestCase
{
   /** The base URL. */
   private String baseURL = HttpUtils.getBaseURL();
   
   /** The web ctx url. */
   private static final String webURL = "jbossweb-virtual-host/index.html";

   /**
    * @return the Test
    * @throws Exception
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(VirtualHostTestCase.class, "jbossweb-virtual-host.war");
   }

   /**
    * Create a new VirtualHostUnitTestCase.
    * 
    * @param name the name
    */
   public VirtualHostTestCase(String name)
   {
      super(name);
   }

   /**
    * Test the virtual.host
    * 
    * @throws Exception
    */
   public void testVirtualHost() throws Exception
   {
      URL url = new URL(baseURL + webURL);
      testHost(url, "virtual.host");
   }
   
   /**
    * Test the virtual.alias
    * 
    * @throws Exception
    */
   public void testVirtualAlias() throws Exception
   {
      URL url = new URL(baseURL + webURL);
      testHost(url, "virtual.alias");      
   }
   
   /**
    * Check the response of the server based on a virtual host.
    * 
    * @param url the url
    * @param virtualHost the virtual host
    * @throws Exception
    */
   protected void testHost(URL url, String virtualHost) throws Exception
   {
      HttpClient httpConn = new HttpClient();
      HttpMethodBase request = new OverrideGetMethod(url.toString(), virtualHost);
      int responseCode = httpConn.executeMethod(request);
      
      if( responseCode != HttpURLConnection.HTTP_OK )
      {
         throw new IOException("Expected reply code:"+ HttpURLConnection.HTTP_OK
            +", actual="+responseCode);
      }
   }
   
   /**
    * Test the non existing web app.
    * 
    * @throws Exception
    */
   public void testNormalHost() throws Exception
   {
      URL url = new URL(baseURL + webURL);
      Header[] hdrs = new Header[0];
      HttpUtils.accessURL(url, null, HttpURLConnection.HTTP_NOT_FOUND, hdrs, HttpUtils.GET);
   }
   
   /**
    * Override the GetMethod to add a specific host to the http header.
    */
   private static class OverrideGetMethod extends GetMethod
   {
      /** . */
      private final String virtualHost;
      
      public OverrideGetMethod(String url, String virtualHost)
      {
         super(url);
         this.virtualHost = virtualHost;
      }
      
      /**
       * Override the host header in the http request.
       */
      @Override
      protected void addHostRequestHeader(HttpState state, HttpConnection conn)
         throws IOException, HttpException
      {
         setRequestHeader("Host", virtualHost);
      }
   }
}