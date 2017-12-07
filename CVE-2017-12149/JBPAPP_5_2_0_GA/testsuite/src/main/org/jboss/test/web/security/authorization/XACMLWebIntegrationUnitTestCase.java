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
package org.jboss.test.web.security.authorization;

import java.net.HttpURLConnection;
import java.net.URL;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.util.web.HttpUtils;

//$Id: XACMLWebIntegrationUnitTestCase.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/**
 *  Test the XACML Integration in the Web layer
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Jun 20, 2006 
 *  @version $Revision: 81036 $
 */
public class XACMLWebIntegrationUnitTestCase extends JBossTestCase
{ 
   public XACMLWebIntegrationUnitTestCase(String name)
   {
      super(name); 
   }
   
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(XACMLWebIntegrationUnitTestCase.class));
      // Create an initializer for the test suite
      TestSetup wrapper = new JBossTestSetup(suite)
      { 
         protected void setUp() throws Exception
         {
            super.setUp(); 
            deploy("xacml-requestattrib.war");
            deploy("xacml-subjectrole.war");
            String url = getResourceURL("web/xacml/app-policy-service.xml");
            deploy(url); 
         }
         protected void tearDown() throws Exception
         { 
            String url = getResourceURL("web/xacml/app-policy-service.xml");
            undeploy(url);
            undeploy("xacml-requestattrib.war");
            undeploy("xacml-subjectrole.war");
            super.tearDown(); 
         }
      };
      return wrapper; 
   } 
   
   public void testRequestAttributePresence() throws Throwable
   {
      doTestRequestAttributePresence();
   }
   
   public void testSubjectRBAC() throws Throwable
   {
      doTestSubjectRBAC();
   }
   
   /**
    * Tests if redeploying causes any side effects.
    * It runs the tests again after a redeploy.
    */
   public void testRedeploy() throws Throwable
   {
      //undeploy
      String url = getResourceURL("web/xacml/app-policy-service.xml");
      undeploy(url);
      undeploy("xacml-requestattrib.war");
      undeploy("xacml-subjectrole.war");
      //deploy again
      deploy("xacml-requestattrib.war");
      deploy("xacml-subjectrole.war");
      deploy(url);
      
      doTestRequestAttributePresence();
      doTestSubjectRBAC();
   }
   
   private void doTestRequestAttributePresence() throws Throwable
   {
      URL url = new URL(HttpUtils.getBaseURL()+"xacml-requestattrib/test?status=employed");
      HttpUtils.accessURL(url, "JBoss XACML Test", HttpURLConnection.HTTP_OK);
      url = new URL(HttpUtils.getBaseURL()+"xacml-requestattrib/test");
      HttpUtils.accessURL(url, "JBoss XACML Test", HttpURLConnection.HTTP_FORBIDDEN);
   }
   
   private void doTestSubjectRBAC() throws Throwable
   {
      URL url = new URL(HttpUtils.getBaseURL()+"xacml-subjectrole/test");
      HttpUtils.accessURL(url, "JBoss XACML Test", HttpURLConnection.HTTP_OK);
   }
}
