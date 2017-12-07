/*
  * JBoss, Home of Professional Open Source
  * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.test.jacc.test.allstarrole;

import java.net.HttpURLConnection;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;
 
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup; 
import org.jboss.test.util.web.HttpUtils;

//$Id: JBAS1824AllStarRoleJaccTestCase.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

/**
 *  JBAS-1824: <role-name>*</role-name> should create WebResourcePermission(url,null)
 *  if requested.
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Feb 16, 2007 
 *  @version $Revision: 85945 $
 */
public class JBAS1824AllStarRoleJaccTestCase extends JBossTestCase
{   
   private String baseURLAuth = HttpUtils.getBaseURL("jduke", "theduke");
   
   public JBAS1824AllStarRoleJaccTestCase(String name)
   {
      super(name); 
   } 
   
   public void testSuccessfulAuthorizationBypass() throws Exception
   {
      //Try a successful access
      HttpUtils.accessURL(new URL(baseURLAuth + "/jacc-allstarrole/index.html")); 
   }
   
   public void testUnsuccessfulAuthorizationBypass() throws Exception
   {
      //Try a unsuccessful access
      HttpUtils.accessURL(new URL(baseURLAuth + "/jacc-allstarrole-noconfig/index.html") 
           , "JBossTest Servlets", HttpURLConnection.HTTP_FORBIDDEN); 
   } 
    
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(JBAS1824AllStarRoleJaccTestCase.class));

      // Create an initializer for the test suite
      Test wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            deploy("jacc-allstarrole.war");
            deploy("jacc-allstarrole-noconfig.war");
            // Make sure the security cache is clear
            flushAuthCache();
         }
         protected void tearDown() throws Exception
         {
            undeploy("jacc-allstarrole-noconfig.war");
            undeploy("jacc-allstarrole.war");
            super.tearDown();
         }
      };
      return wrapper;
   }
}
