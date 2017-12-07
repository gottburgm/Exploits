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

import java.net.HttpURLConnection;
import java.net.URL;

import junit.framework.Test;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.web.HttpUtils;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;

//$Id: UserInRoleUnitTestCase.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/** Tests of the servlet request isUserInRole call.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class UserInRoleUnitTestCase extends JBossTestCase
{
   private String baseURL = HttpUtils.getBaseURL();
   
   private static Boolean jacc = Boolean.valueOf(System.getProperty("jboss.security.jacc", "false"));;

   public static Test suite() throws Exception
   {
      return getDeploySetup(UserInRoleUnitTestCase.class, "userinrole.ear");
   }

   public UserInRoleUnitTestCase(String name)
   {
      super(name);
   }

   /** Test that the custom 404 error page is seen
    * 
    * @throws Exception
    */
   public void testRoleWithLink() throws Exception
   {
      log.info("+++ testRoleWithLink");
      URL url = new URL(baseURL+"userinrole/testRoleWithLink");
      HttpUtils.accessURL(url, "UserInRoleRealm", HttpURLConnection.HTTP_OK);
   }
   public void testRoleWithoutLink() throws Exception
   {
      log.info("+++ testUnreferencedRole");
      URL url = new URL(baseURL+"userinrole/testUnreferencedRole");
      HttpUtils.accessURL(url, "UserInRoleRealm", HttpURLConnection.HTTP_OK);
   }

   /**
    * Test that two wars from different security domains with common principal
    * names do not conflict in terms of isUserInRole results.
    * http://jira.jboss.com/jira/browse/JBAS-3043
    * 
    * This is the non-jacc version where the programmatic security of isUserInRole
    * will work off of the roles populated in the subject, irrespective of whether
    * the roles are fully defined in the web.xml
    * @throws Exception
    */
   public void testConflictingUserInRole() throws Exception
   {
      if(jacc == Boolean.TRUE)
         return;
      log.info("+++ testConflictingUserInRole");
      String base = HttpUtils.getBaseURL("sa", "sa");

      // Hit the first web app and validate isUserInRole calls
      URL url1 = new URL(base+"userinrole1");
      HttpMethodBase request = HttpUtils.accessURL(url1, "JBAS-3043-1", HttpURLConnection.HTTP_OK);
      Header X = request.getResponseHeader("X-isUserInRole-X");
      log.info("X "+X);
      assertEquals("X-isUserInRole-X("+X+") is true", "true", X.getValue());
      Header Y = request.getResponseHeader("X-isUserInRole-Y");
      log.info("Y "+Y);
      assertEquals("X-isUserInRole-Y("+Y+") is false" , "false", Y.getValue());
      Header Z = request.getResponseHeader("X-isUserInRole-Z");
      log.info("Z "+Z);
      assertEquals("X-isUserInRole-Z("+Z+") is true", "true", Z.getValue());

      // Hit the second web app and validate isUserInRole calls
      URL url2 = new URL(base+"userinrole2");
      request = HttpUtils.accessURL(url2, "JBAS-3043-2", HttpURLConnection.HTTP_OK);
      X = request.getResponseHeader("X-isUserInRole-X");
      log.info("X "+X);
      assertEquals("X-isUserInRole-X("+X+") is false", "false", X.getValue());
      Y = request.getResponseHeader("X-isUserInRole-Y");
      log.info("Y "+Y);
      assertEquals("X-isUserInRole-Y("+Y+") is true", "true", Y.getValue());
      Z = request.getResponseHeader("X-isUserInRole-Z");
      log.info("Z "+Z);
      assertEquals("X-isUserInRole-Z("+Z+") is true", "true", Z.getValue());

      request = HttpUtils.accessURL(url1, "JBAS-3043-1", HttpURLConnection.HTTP_OK);
      X = request.getResponseHeader("X-isUserInRole-X");
      log.info("X "+X);
      assertEquals("X-isUserInRole-X("+X+") is true", "true", X.getValue());
      Y = request.getResponseHeader("X-isUserInRole-Y");
      log.info("Y "+Y);
      assertEquals("X-isUserInRole-Y("+Y+") is false", "false", Y.getValue());
      Z = request.getResponseHeader("X-isUserInRole-Z");
      log.info("Z "+Z);
      assertEquals("X-isUserInRole-Z("+Z+") is true", "true", Z.getValue());
   }
   
   /**
    * Test that two wars from different security domains with common principal
    * names do not conflict in terms of isUserInRole results.
    * http://jira.jboss.com/jira/browse/JBAS-3043 
    * 
    * This is the jacc version where the programmatic security of isUserInRole
    * will work only of the roles are fully defined in the web.xml
    * @throws Exception
    */
   public void testConflictingUserInRoleJaccVersion() throws Exception
   {
      if(jacc == Boolean.FALSE)
         return;
      log.info("+++ testConflictingUserInRole");
      String base = HttpUtils.getBaseURL("sa", "sa");

      // Hit the first web app and validate isUserInRole calls
      URL url1 = new URL(base+"userinrole1");
      HttpMethodBase request = HttpUtils.accessURL(url1, "JBAS-3043-1", HttpURLConnection.HTTP_OK);
      Header X = request.getResponseHeader("X-isUserInRole-X");
      log.info("X "+X);
      assertEquals("X-isUserInRole-X("+X+") is false", "false", X.getValue());
      Header Y = request.getResponseHeader("X-isUserInRole-Y");
      log.info("Y "+Y);
      assertEquals("X-isUserInRole-Y("+Y+") is false" , "false", Y.getValue());
      Header Z = request.getResponseHeader("X-isUserInRole-Z");
      log.info("Z "+Z);
      assertEquals("X-isUserInRole-Z("+Z+") is true", "true", Z.getValue());

      // Hit the second web app and validate isUserInRole calls
      URL url2 = new URL(base+"userinrole2");
      request = HttpUtils.accessURL(url2, "JBAS-3043-2", HttpURLConnection.HTTP_OK);
      X = request.getResponseHeader("X-isUserInRole-X");
      log.info("X "+X);
      assertEquals("X-isUserInRole-X("+X+") is false", "false", X.getValue());
      Y = request.getResponseHeader("X-isUserInRole-Y");
      log.info("Y "+Y);
      assertEquals("X-isUserInRole-Y("+Y+") is false", "false", Y.getValue());
      Z = request.getResponseHeader("X-isUserInRole-Z");
      log.info("Z "+Z);
      assertEquals("X-isUserInRole-Z("+Z+") is true", "true", Z.getValue());

      request = HttpUtils.accessURL(url1, "JBAS-3043-1", HttpURLConnection.HTTP_OK);
      X = request.getResponseHeader("X-isUserInRole-X");
      log.info("X "+X);
      assertEquals("X-isUserInRole-X("+X+") is false", "false", X.getValue());
      Y = request.getResponseHeader("X-isUserInRole-Y");
      log.info("Y "+Y);
      assertEquals("X-isUserInRole-Y("+Y+") is false", "false", Y.getValue());
      Z = request.getResponseHeader("X-isUserInRole-Z");
      log.info("Z "+Z);
      assertEquals("X-isUserInRole-Z("+Z+") is true", "true", Z.getValue());
   } 
}
