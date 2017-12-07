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
package org.jboss.test.security.test;

import java.net.HttpURLConnection;
import java.net.URL;

import org.jboss.test.util.web.HttpUtils;
import org.jboss.test.JBossTestCase;
import junit.framework.Test;

/** Tests of the web declarative security model

 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class WebConstraintsUnitTestCase extends JBossTestCase
{
   public static String REALM = "WebConstraintsUnitTestCase";
   public String baseURLNoAuth = HttpUtils.getBaseURLNoAuth(); 
   public static final String WAR = "web-constraints.war";
   static String username = "scott";
   static char[] password = "echoman".toCharArray();
   /** A flag indicating if a "*" web-app/auth-constraint/role-name should imply
    * any authenticated user role, or only the security-role/role-name values
    * in the web app. True = only the web-app defined roles.
    */
   private boolean strictStarRolesMode;

   public boolean isStrictStarRolesMode()
   {
      return strictStarRolesMode;
   }
   public void setStrictStarRolesMode(boolean strictStarRolesMode)
   {
      this.strictStarRolesMode = strictStarRolesMode;
   }

   public WebConstraintsUnitTestCase(String name)
   {
      super(name);
   }

   /** Test URLs that should require no authentication for any method
    */
   public void testUnchecked() throws Exception
   {
      log.debug("+++ testUnchecked");
      // Test the unchecked security-constraint
      URL url = new URL(baseURLNoAuth+"web-constraints/unchecked");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);
      url = new URL(baseURLNoAuth+"web-constraints/unchecked/");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);
      url = new URL(baseURLNoAuth+"web-constraints/unchecked/x");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK, HttpUtils.HEAD);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK, HttpUtils.POST);

      // Test the Unrestricted security-constraint
      url = new URL(baseURLNoAuth+"web-constraints/restricted/not");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK, HttpUtils.HEAD);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK, HttpUtils.POST);
      url = new URL(baseURLNoAuth+"web-constraints/restricted/not/x");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK, HttpUtils.HEAD);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK, HttpUtils.POST);

      // Test the unspecified mappings
      url = new URL(baseURLNoAuth+"web-constraints/");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);
      url = new URL(baseURLNoAuth+"web-constraints/other");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK, HttpUtils.HEAD);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK, HttpUtils.POST);
   }

   /** Test GETs against URLs that only allows the GET method and required
    * the GetRole role
    */
   public void testGetAccess() throws Exception
   {
      log.debug("+++ testGetAccess");
      Thread.sleep(10*1000);
      String baseURL = HttpUtils.getBaseURL("getUser", "getUserPass");
      // Test the Restricted GET security-constraint
      URL url = new URL(baseURL+"web-constraints/restricted/get-only");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);
      url = new URL(baseURL+"web-constraints/restricted/get-only/x");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);

      // Test the Restricted ANY security-constraint
      url = new URL(baseURL+"web-constraints/restricted/any/x");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);

      // Test that a POST to the Restricted GET security-constraint fails
      url = new URL(baseURL+"web-constraints/restricted/get-only/x");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN, HttpUtils.POST);
      // Test that Restricted POST security-constraint fails
      url = new URL(baseURL+"web-constraints/restricted/post-only/x");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN);

      // Validate that the excluded subcontext if not accessible
      url = new URL(baseURL+"web-constraints/restricted/get-only/excluded/x");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN);

      // Change to otherUser to test failure
      baseURL = HttpUtils.getBaseURL("otherUser", "otherUserPass");
      
      // Test the Restricted GET security-constraint 
      url = new URL(baseURL+"web-constraints/restricted/get-only");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN);
      url = new URL(baseURL+"web-constraints/restricted/get-only/x");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN);

      if( strictStarRolesMode == false )
      {
         // Test the Restricted ANY security-constraint
         url = new URL(baseURL+"web-constraints/restricted/any/x");
         HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);
      }
   }

   /** Test that the excluded paths are not accessible by anyone
    */
   public void testExcludedAccess() throws Exception
   {
      log.debug("+++ testExcludedAccess");
      String baseURL = HttpUtils.getBaseURL("getUser", "getUserPass");
      // Test the excluded security-constraint
      URL url = new URL(baseURL+"web-constraints/excluded/x");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN, HttpUtils.OPTIONS);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN, HttpUtils.HEAD);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN, HttpUtils.POST);
      url = new URL(baseURL+"web-constraints/restricted/");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN, HttpUtils.OPTIONS);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN, HttpUtils.HEAD);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN, HttpUtils.POST);

      url = new URL(baseURL+"web-constraints/restricted/get-only/excluded/x");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN, HttpUtils.OPTIONS);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN, HttpUtils.HEAD);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN, HttpUtils.POST);

      url = new URL(baseURL+"web-constraints/restricted/put-only/excluded/x");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN, HttpUtils.OPTIONS);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN, HttpUtils.HEAD);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN, HttpUtils.POST);

      url = new URL(baseURL+"web-constraints/restricted/any/excluded/x");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN, HttpUtils.OPTIONS);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN, HttpUtils.HEAD);
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN, HttpUtils.POST);
   }

   /** Test POSTs against URLs that only allows the POST method and required
    * the PostRole role
    */
   public void testPostAccess() throws Exception
   {
      log.debug("+++ testPostAccess");
      String baseURL = HttpUtils.getBaseURL("postUser", "postUserPass");
      // Test the Restricted POST security-constraint
      URL url = new URL(baseURL+"web-constraints/restricted/post-only/");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK, HttpUtils.POST);
      url = new URL(baseURL+"web-constraints/restricted/post-only/x");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK, HttpUtils.POST);

      // Test the Restricted ANY security-constraint
      url = new URL(baseURL+"web-constraints/restricted/any/x");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK, HttpUtils.POST);

      // Validate that the excluded subcontext if not accessible
      url = new URL(baseURL+"web-constraints/restricted/post-only/excluded/x");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN);

      // Test that a GET to the Restricted POST security-constraint fails
      url = new URL(baseURL+"web-constraints/restricted/post-only/x");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN);
      // Test that Restricted POST security-constraint fails
      url = new URL(baseURL+"web-constraints/restricted/get-only/x");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN, HttpUtils.POST);

      // Change to otherUser to test failure
      baseURL = HttpUtils.getBaseURL("otherUser", "otherUserPass");
      
      // Test the Restricted GET security-constraint 
      url = new URL(baseURL+"web-constraints/restricted/post-only");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN, HttpUtils.POST);
      url = new URL(baseURL+"web-constraints/restricted/post-only/x");
      HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_FORBIDDEN, HttpUtils.POST);

      if( strictStarRolesMode == false )
      {
         // Test the Restricted ANY security-constraint
         url = new URL(baseURL+"web-constraints/restricted/any/x");
         HttpUtils.accessURL(url, REALM, HttpURLConnection.HTTP_OK);
      }
   }

   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(WebConstraintsUnitTestCase.class, WAR);
   }
}
