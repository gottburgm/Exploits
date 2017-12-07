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
package org.jboss.test.security.test.authorization;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.rmi.PortableRemoteObject;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.HttpMethodBase;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.security.interfaces.ACLSession;
import org.jboss.test.util.web.HttpUtils;

/**
 * <p>
 * This {@code TestCase} tests the integration of the ACL layer with the application server. Modules define their ACL
 * constraints in the {@code jboss-acl-policy.xml} configuration file and then call the {@code AuthorizationManager} at
 * runtime to enforce their ACL policies.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class ACLIntegrationUnitTestCase extends JBossTestCase
{

   /**
    * <p>
    * Creates an instance of {@code ACLIntegrationUnitTestCase} with the specified name.
    * </p>
    * 
    * @param name a {@code String} representing the name of the {@code TestCase}.
    */
   public ACLIntegrationUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * <p>
    * Tests the results of calling {@code AuthorizationManager#getEntitlements} from within a web component (a servlet).
    * </p>
    * 
    * @throws Exception if an error occurs while running the test.
    */
   public void testGetEntitlementsFromServlet() throws Exception
   {
      // call the ACLServlet using the identity "Administrator" as a parameter.
      URL url = new URL(HttpUtils.getBaseURL() + "acl-integration/acl?identity=Administrator");
      HttpMethodBase response = HttpUtils.accessURL(url, "JBoss ACL Test", HttpURLConnection.HTTP_OK);
      // each line of the response has the following format: resource_id:permissions
      List<String> entitlements = this.readEntitlementsFromResponse(response);
      assertEquals("ACLServlet retrieved an invalid number of entitlement entries", 2, entitlements.size());
      // Administrator should have CREATE,READ,UPDATE and DELETE permissions on both resources (id=1 and id=2).
      assertTrue("Invalid entitlement entry found", entitlements.contains("1:CREATE,READ,UPDATE,DELETE"));
      assertTrue("Invalid entitlement entry found", entitlements.contains("2:CREATE,READ,UPDATE,DELETE"));

      // now repeat the process, this time using the identity "Guest".
      url = new URL(HttpUtils.getBaseURL() + "acl-integration/acl?identity=Guest");
      response = HttpUtils.accessURL(url, "JBoss ACL Test", HttpURLConnection.HTTP_OK);
      entitlements = this.readEntitlementsFromResponse(response);
      assertEquals("ACLServlet retrieved an invalid number of entitlement entries", 2, entitlements.size());
      // Guest should have READ permission on resource 1 and READ,UPDATE permissions on resource 2.
      assertTrue("Invalid entitlement entry found", entitlements.contains("1:READ"));
      assertTrue("Invalid entitlement entry found", entitlements.contains("2:READ,UPDATE"));
   }

   /**
    * <p>
    * Tests the results of calling {@code AuthorizationManager#getEntitlements} from within an EJB3 component.
    * </p>
    * 
    * @throws Exception
    */
   public void testGetEntitlementsFromEJB() throws Exception
   {
      // lookup the test session.
      Object obj = getInitialContext().lookup("ACLSessionImpl/remote");
      ACLSession session = (ACLSession) PortableRemoteObject.narrow(obj, ACLSession.class);

      // get the entitlements for the Administrator identity.
      Map<Integer, String> entitlementsMap = session.getEntitlementsForIdentity("Administrator");
      assertEquals("ACLSession retrieved an invalid number of entitlement entries", 2, entitlementsMap.size());
      // Administrator should have CREATE,READ and UPDATE permissions on both resources (id=10 and id=11).
      assertEquals("Invalid entitlement entry found", "CREATE,READ,UPDATE", entitlementsMap.get(10));
      assertEquals("Invalid entitlement entry found", "CREATE,READ,UPDATE", entitlementsMap.get(11));

      // now repeat the process, this time using the identity "Guest".
      entitlementsMap = session.getEntitlementsForIdentity("Guest");
      assertEquals("ACLSession retrieved an invalid number of entitlement entries", 2, entitlementsMap.size());
      // Guest should have CREATE, READ and UPDATE permissions on resource 10 and READ permission on resource 11.
      assertEquals("Invalid entitlement entry found", "CREATE,READ,UPDATE", entitlementsMap.get(10));
      assertEquals("Invalid entitlement entry found", "READ", entitlementsMap.get(11));
   }

   /**
    * <p>
    * Reads the response contents and create a {@code List<String>} where each component corresponds to one line of the
    * response body.
    * </p>
    * 
    * @param response the {@code HttpServletResponse} that contains the response from the {@code ACLServlet}.
    * @return a {@code List<String>}, where each element corresponds to one line of the response body.
    * @throws Exception
    */
   private List<String> readEntitlementsFromResponse(HttpMethodBase response) throws Exception
   {
      BufferedReader reader = new BufferedReader(new InputStreamReader(response.getResponseBodyAsStream()));
      List<String> entitlements = new ArrayList<String>();
      String line = reader.readLine();
      while (line != null)
      {
         entitlements.add(line);
         line = reader.readLine();
      }
      return entitlements;
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(ACLIntegrationUnitTestCase.class));

      TestSetup wrapper = new JBossTestSetup(suite)
      {
         /*
          * (non-Javadoc)
          * 
          * @see org.jboss.test.JBossTestSetup#setUp()
          */
         @Override
         protected void setUp() throws Exception
         {
            super.setUp();
            // deploy the application policy that specifies an ACL module.
            String url = getResourceURL("security/authorization/aclpolicy-jboss-beans.xml");
            deploy(url);
            // deploy the web application that calls the ACL module.
            deploy("acl-integration.war");
            // deploy the ejb application that calls the ACL module.
            deploy("acl-integration.jar");
         }

         /*
          * (non-Javadoc)
          * 
          * @see org.jboss.test.JBossTestSetup#tearDown()
          */
         @Override
         protected void tearDown() throws Exception
         {
            // undeploy the test ejb application.
            undeploy("acl-integration.jar");
            // undeploy the test web application.
            undeploy("acl-integration.war");
            // undeploy the application policy.
            String url = getResourceURL("security/authorization/aclpolicy-jboss-beans.xml");
            undeploy(url);
            super.tearDown();
         }
      };
      return wrapper;
   }
}
