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

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jboss.test.cluster.testutil.DBSetupDelegate;
import org.jboss.test.cluster.testutil.DelegatingClusteredTestCase;
import org.jboss.test.cluster.testutil.TestSetupDelegate;
import org.jboss.test.cluster.testutil.WebTestBase;
import org.jboss.test.cluster.web.persistent.PersistentStoreSetupDelegate;

/**
 * Test for JBPAPP-5171
 * 
 * @author Brian Stansberry
 * @version $Id: UndeployTestCase.java 81084 2008-11-14 17:30:43Z dimitris@jboss.org $
 */
public class PersistentManagerFormAuthTestCase extends WebTestBase
{
   private String baseURLNoAuth;
   
   public PersistentManagerFormAuthTestCase(String name)
   {
      super(name);
   }
   
   protected void setUp() throws Exception
   {
      super.setUp();
      baseURLNoAuth = getHttpURLs()[0]; 
   }
   
   protected String getContextPath()
   {
      return "/http-formauth-persistent/";
   }
   
   protected String getWarName()
   {
      return "http-formauth-persistent.war";
   }

   public static Test suite() throws Exception
   {
      String dbAddress = System.getProperty(DBSetupDelegate.DBADDRESS_PROPERTY, DBSetupDelegate.DEFAULT_ADDRESS);
      TestSetupDelegate dbDelegate = new DBSetupDelegate(dbAddress, DBSetupDelegate.DEFAULT_PORT);
      TestSetupDelegate storeDelegate = new PersistentStoreSetupDelegate(dbAddress, DBSetupDelegate.DEFAULT_PORT);
      List<TestSetupDelegate> list = Arrays.asList(new TestSetupDelegate[]{dbDelegate, storeDelegate});
      return DelegatingClusteredTestCase.getDeploySetup(PersistentManagerFormAuthTestCase.class,
                                                      "httpsession-ds.xml, disable-manager-override.beans, " +
                                                      "http-formauth-persistent.war", list);
   }
   
   public void testFormAuthentication() throws Exception {

      String url = baseURLNoAuth + "/http-formauth-persistent/";
      
      // Start by accessing the secured index.html of war1
      HttpClient httpConn = new HttpClient();
      
      // Try to access protected resource
      GetMethod indexGet = new GetMethod(url + "index.jsp");
      int responseCode = httpConn.executeMethod(indexGet);
      String body = indexGet.getResponseBodyAsString();
      assertTrue("Get OK", responseCode == HttpURLConnection.HTTP_OK);
      assertTrue("Redirected to login page", body.indexOf("j_security_check") > 0 );

      // Submit the login form
    
      PostMethod formPost = new PostMethod(url + "j_security_check");
      formPost.addRequestHeader("Referer", url + "login.html");
      formPost.addParameter("j_username", "admin");
      formPost.addParameter("j_password", "admin");
      responseCode = httpConn.executeMethod(formPost.getHostConfiguration(),
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
      body = warIndex.getResponseBodyAsString();
      if( body.indexOf("j_security_check") > 0 )
         fail("get of "+indexURI+" redirected to login page");
   }

}
