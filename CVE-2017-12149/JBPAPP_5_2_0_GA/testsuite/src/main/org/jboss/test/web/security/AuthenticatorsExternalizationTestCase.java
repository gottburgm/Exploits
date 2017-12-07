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
package org.jboss.test.web.security;

// $Id: AuthenticatorsExternalizationTestCase.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
import java.net.HttpURLConnection;
import javax.management.MBeanServerConnection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.test.JBossTestCase;

/**
 * JBAS-2481: Externalization of Tomcat Authenticators
 * 
 * @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 * @since Dec 1, 2005
 */
public class AuthenticatorsExternalizationTestCase extends JBossTestCase
{
   MBeanServerConnection server = null;

   private String baseURLNoAuth;

   public AuthenticatorsExternalizationTestCase(String name)
   {
      super(name);
   }

   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      baseURLNoAuth = "http://" + getServerHost() + ":" + Integer.getInteger("web.port", 8080) + "/";
      this.serverFound();
      this.deploy("auth-ext-header-web.ear");
      server = getServer();
      assertNotNull("Obtained MBeanServerConnection?", server);
   }

   @Override
   public void tearDown() throws Exception
   {
      if (server != null)
         server = null;
      this.undeploy("auth-ext-header-web.ear");
   }

   /**
    * Test custom header based authentication
    * 
    * @throws Exception
    */
   public void testHeaderBasedAuthentication() throws Exception
   {
      String location = baseURLNoAuth + "header-auth/index.jsp";
      int responseCode = 0;
      HttpClient httpConn = new HttpClient();
      GetMethod indexGet = null;
      try
      {
         indexGet = new GetMethod(location);
         indexGet.setFollowRedirects(false);
         responseCode = httpConn.executeMethod(indexGet);
         assertEquals(HttpURLConnection.HTTP_FORBIDDEN, responseCode);
      }
      finally
      {
         indexGet.releaseConnection();
      }
      indexGet = null;
      try
      {
         indexGet = new GetMethod(location);
         indexGet.setFollowRedirects(false);
         // Add the request headers
         indexGet.addRequestHeader("JBOSS_TEST_USER_NAME", "jduke");
         indexGet.addRequestHeader("JBOSS_TEST_CREDENTIAL", "theduke");
         responseCode = httpConn.executeMethod(indexGet);
         assertEquals(HttpURLConnection.HTTP_OK, responseCode);
      }
      finally
      {
         indexGet.releaseConnection();
      }
   }
}
