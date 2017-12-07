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
package org.jboss.test.web.security.jacc;

import java.util.ArrayList;
import java.util.List;
import java.security.Policy;
import java.security.ProtectionDomain;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.PolicyContext;

import junit.framework.TestCase;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.AuthConstraintMetaData;
import org.jboss.metadata.web.spec.SecurityConstraintMetaData;
import org.jboss.metadata.web.spec.TransportGuaranteeType;
import org.jboss.metadata.web.spec.UserDataConstraintMetaData;
import org.jboss.metadata.web.spec.WebMetaData;
import org.jboss.metadata.web.spec.WebResourceCollectionMetaData;
import org.jboss.web.WebPermissionMapping;
import org.jboss.security.jacc.DelegatingPolicy;
import org.jboss.security.jacc.JBossPolicyConfigurationFactory;
import org.jboss.security.SimplePrincipal;

/** Test

 <?xml version="1.0" encoding="UTF-8"?>
 <web-app version="2.4"
    xmlns="http://java.sun.com/xml/ns/j2ee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee
    http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd">

    <description>Tests of various security-constraints</description>

    <servlet>
       <servlet-name>ConstraintsServlet</servlet-name>
       <servlet-class>org.jboss.test.security.servlets.ConstraintsServlet</servlet-class>
    </servlet>

    <servlet-mapping>
       <servlet-name>ConstraintsServlet</servlet-name>
       <url-pattern>/*</url-pattern>
    </servlet-mapping>

    <security-constraint>
       <display-name>excluded</display-name>
       <web-resource-collection>
          <web-resource-name>No Access</web-resource-name>
          <url-pattern>/excluded/*</url-pattern>
          <url-pattern>/restricted/get-only/excluded/*</url-pattern>
          <url-pattern>/restricted/post-only/excluded/*</url-pattern>
          <url-pattern>/restricted/any/excluded/*</url-pattern>
       </web-resource-collection>
       <web-resource-collection>
          <web-resource-name>No Access</web-resource-name>
          <url-pattern>/restricted/*</url-pattern>
          <http-method>DELETE</http-method>
          <http-method>PUT</http-method>
          <http-method>HEAD</http-method>
          <http-method>OPTIONS</http-method>
          <http-method>TRACE</http-method>
          <http-method>GET</http-method>
          <http-method>POST</http-method>
       </web-resource-collection>
       <auth-constraint />
       <user-data-constraint>
          <transport-guarantee>NONE</transport-guarantee>
       </user-data-constraint>
    </security-constraint>

    <security-constraint>
       <display-name>unchecked</display-name>
       <web-resource-collection>
          <web-resource-name>All Access</web-resource-name>
          <url-pattern>/unchecked/*</url-pattern>
          <http-method>DELETE</http-method>
          <http-method>PUT</http-method>
          <http-method>HEAD</http-method>
          <http-method>OPTIONS</http-method>
          <http-method>TRACE</http-method>
          <http-method>GET</http-method>
          <http-method>POST</http-method>
       </web-resource-collection>
       <user-data-constraint>
          <transport-guarantee>NONE</transport-guarantee>
       </user-data-constraint>
    </security-constraint>

    <security-constraint>
       <display-name>Restricted GET</display-name>
       <web-resource-collection>
          <web-resource-name>Restricted Access - Get Only</web-resource-name>
          <url-pattern>/restricted/get-only/*</url-pattern>
          <http-method>GET</http-method>
       </web-resource-collection>
       <auth-constraint>
          <role-name>GetRole</role-name>
       </auth-constraint>
       <user-data-constraint>
          <transport-guarantee>NONE</transport-guarantee>
       </user-data-constraint>
    </security-constraint>
    <security-constraint>
       <display-name>Excluded GET</display-name>
       <web-resource-collection>
          <web-resource-name>Restricted Access - Get Only</web-resource-name>
          <url-pattern>/restricted/get-only/*</url-pattern>
          <http-method>DELETE</http-method>
          <http-method>PUT</http-method>
          <http-method>HEAD</http-method>
          <http-method>OPTIONS</http-method>
          <http-method>TRACE</http-method>
          <http-method>POST</http-method>
       </web-resource-collection>
       <auth-constraint />
       <user-data-constraint>
          <transport-guarantee>NONE</transport-guarantee>
       </user-data-constraint>
    </security-constraint>

    <security-constraint>
       <display-name>Restricted POST</display-name>
       <web-resource-collection>
          <web-resource-name>Restricted Access - Post Only</web-resource-name>
          <url-pattern>/restricted/post-only/*</url-pattern>
          <http-method>POST</http-method>
       </web-resource-collection>
       <auth-constraint>
          <role-name>PostRole</role-name>
       </auth-constraint>
       <user-data-constraint>
          <transport-guarantee>NONE</transport-guarantee>
       </user-data-constraint>
    </security-constraint>
    <security-constraint>
       <display-name>Excluded POST</display-name>
       <web-resource-collection>
          <web-resource-name>Restricted Access - Post Only</web-resource-name>
          <url-pattern>/restricted/post-only/*</url-pattern>
          <http-method>DELETE</http-method>
          <http-method>PUT</http-method>
          <http-method>HEAD</http-method>
          <http-method>OPTIONS</http-method>
          <http-method>TRACE</http-method>
          <http-method>GET</http-method>
       </web-resource-collection>
       <auth-constraint />
       <user-data-constraint>
          <transport-guarantee>NONE</transport-guarantee>
       </user-data-constraint>
    </security-constraint>

    <security-constraint>
       <display-name>Restricted ANY</display-name>
       <web-resource-collection>
          <web-resource-name>Restricted Access - Any</web-resource-name>
          <url-pattern>/restricted/any/*</url-pattern>
          <http-method>DELETE</http-method>
          <http-method>PUT</http-method>
          <http-method>HEAD</http-method>
          <http-method>OPTIONS</http-method>
          <http-method>TRACE</http-method>
          <http-method>GET</http-method>
          <http-method>POST</http-method>
       </web-resource-collection>
       <auth-constraint>
          <role-name>*</role-name>
       </auth-constraint>
       <user-data-constraint>
          <transport-guarantee>NONE</transport-guarantee>
       </user-data-constraint>
    </security-constraint>

    <security-constraint>
       <display-name>Unrestricted</display-name>
       <web-resource-collection>
          <web-resource-name>Restricted Access - Any</web-resource-name>
          <url-pattern>/restricted/not/*</url-pattern>
          <http-method>DELETE</http-method>
          <http-method>PUT</http-method>
          <http-method>HEAD</http-method>
          <http-method>OPTIONS</http-method>
          <http-method>TRACE</http-method>
          <http-method>GET</http-method>
          <http-method>POST</http-method>
       </web-resource-collection>
       <user-data-constraint>
          <transport-guarantee>NONE</transport-guarantee>
       </user-data-constraint>
    </security-constraint>

    <security-role>
       <role-name>GetRole</role-name>
    </security-role>
    <security-role>
       <role-name>PostRole</role-name>
    </security-role>

    <login-config>
       <auth-method>BASIC</auth-method>
       <realm-name>WebConstraintsUnitTestCase</realm-name>
    </login-config>
 </web-app>

 @author Scott.Stark@jboss.org
 @version $Revision: 81037 $
 */
public class WebConstraintsUnitTestCase extends TestCase
{
   private PolicyConfiguration pc;

   public void testUnchecked() throws Exception
   {
      Policy p = Policy.getPolicy();
      SimplePrincipal[] caller = null;
      ProtectionDomain pd = new ProtectionDomain(null, null, null, caller);
      // Test /unchecked
      WebResourcePermission wrp = new WebResourcePermission("/unchecked", "GET");
      assertTrue("/unchecked GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/unchecked/x", "GET");
      assertTrue("/unchecked/x GET", p.implies(pd, wrp));

      // Test the Unrestricted security-constraint
      wrp = new WebResourcePermission("/restricted/not", "GET");
      assertTrue("/restricted/not GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/restricted/not/x", "GET");
      assertTrue("/restricted/not/x GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/restricted/not/x", "HEAD");
      assertTrue("/restricted/not/x HEAD", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/restricted/not/x", "POST");
      assertTrue("/restricted/not/x POST", p.implies(pd, wrp));

      wrp = new WebResourcePermission("/", "GET");
      assertTrue("/ GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/other", "GET");
      assertTrue("/other GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/other", "HEAD");
      assertTrue("/other HEAD", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/other", "POST");
      assertTrue("/other POST", p.implies(pd, wrp));
   }

   public void testGetAccess() throws Exception
   {
      Policy p = Policy.getPolicy();
      SimplePrincipal[] caller = {new SimplePrincipal("GetRole")};
      ProtectionDomain pd = new ProtectionDomain(null, null, null, caller);

      // Test the Restricted GET security-constraint
      WebResourcePermission wrp = new WebResourcePermission("/restricted/get-only", "GET");
      assertTrue("/restricted/get-only GET", p.implies(pd, wrp));

      wrp = new WebResourcePermission("/restricted/get-only/x", "GET");
      assertTrue("/restricted/get-only/x GET", p.implies(pd, wrp));

      // Test the Restricted ANY security-constraint
      wrp = new WebResourcePermission("/restricted/any/x", "GET");
      assertTrue("/restricted/any/x GET", p.implies(pd, wrp));

      // Test that a POST to the Restricted GET security-constraint fails
      wrp = new WebResourcePermission("/restricted/get-only/x", "POST");
      assertFalse("/restricted/get-only/x POST", p.implies(pd, wrp));

      // Test that Restricted POST security-constraint fails
      wrp = new WebResourcePermission("/restricted/post-only/x", "GET");
      assertFalse("/restricted/post-only/x GET", p.implies(pd, wrp));

      // Validate that the excluded subcontext if not accessible
      wrp = new WebResourcePermission("/restricted/get-only/excluded/x", "GET");
      assertFalse("/restricted/get-only/excluded/x GET", p.implies(pd, wrp));

      caller = new SimplePrincipal[]{new SimplePrincipal("OtherRole")};
      pd = new ProtectionDomain(null, null, null, caller);
      // Test the Restricted GET security-constraint 
      wrp = new WebResourcePermission("/restricted/get-only", "GET");
      assertFalse("/restricted/get-only GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/restricted/get-only/x", "GET");
      assertFalse("/restricted/get-only/x GET", p.implies(pd, wrp));

      /* Test the Restricted ANY security-constraint. Note that this would be
      allowed by the non-JACC and standalone tomcat as they interpret the "*"
      role-name to mean any role while the JACC mapping simply replaces "*" with
      the web.xml security-role/role-name values.
      */
      wrp = new WebResourcePermission("/restricted/any/x", "GET");
      assertFalse("/restricted/any/x GET", p.implies(pd, wrp));
   }

   /** Test that the excluded paths are not accessible by anyone
    */
   public void testExcludedAccess() throws Exception
   {
      Policy p = Policy.getPolicy();
      SimplePrincipal[] caller = {new SimplePrincipal("GetRole")};
      ProtectionDomain pd = new ProtectionDomain(null, null, null, caller);

      WebResourcePermission wrp = new WebResourcePermission("/excluded/x", "GET");
      assertFalse("/excluded/x GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/excluded/x", "OPTIONS");
      assertFalse("/excluded/x OPTIONS", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/excluded/x", "HEAD");
      assertFalse("/excluded/x HEAD", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/excluded/x", "POST");
      assertFalse("/excluded/x POST", p.implies(pd, wrp));

      wrp = new WebResourcePermission("/restricted/", "GET");
      assertFalse("/restricted/ GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/restricted/", "OPTIONS");
      assertFalse("/restricted/ OPTIONS", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/restricted/", "HEAD");
      assertFalse("/restricted/ HEAD", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/restricted/", "POST");
      assertFalse("/restricted/ POST", p.implies(pd, wrp));

      wrp = new WebResourcePermission("/restricted/get-only/excluded/x", "GET");
      assertFalse("/restricted/get-only/excluded/x GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/restricted/get-only/excluded/x", "OPTIONS");
      assertFalse("/restricted/get-only/excluded/x OPTIONS", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/restricted/get-only/excluded/x", "HEAD");
      assertFalse("/restricted/get-only/excluded/x HEAD", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/restricted/get-only/excluded/x", "POST");
      assertFalse("/restricted/get-only/excluded/x POST", p.implies(pd, wrp));

      wrp = new WebResourcePermission("/restricted/post-only/excluded/x", "GET");
      assertFalse("/restricted/post-only/excluded/x GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/restricted/post-only/excluded/x", "OPTIONS");
      assertFalse("/restricted/post-only/excluded/x OPTIONS", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/restricted/post-only/excluded/x", "HEAD");
      assertFalse("/restricted/post-only/excluded/x HEAD", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/restricted/post-only/excluded/x", "POST");
      assertFalse("/restricted/post-only/excluded/x POST", p.implies(pd, wrp));

      wrp = new WebResourcePermission("/restricted/any/excluded/x", "GET");
      assertFalse("/restricted/any/excluded/x GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/restricted/any/excluded/x", "OPTIONS");
      assertFalse("/restricted/any/excluded/x OPTIONS", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/restricted/any/excluded/x", "HEAD");
      assertFalse("/restricted/any/excluded/x HEAD", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/restricted/any/excluded/x", "POST");
      assertFalse("/restricted/any/excluded/x POST", p.implies(pd, wrp));
   }

   /** Test POSTs against URLs that only allows the POST method and required
    * the PostRole role
    */
   public void testPostAccess() throws Exception
   {
      Policy p = Policy.getPolicy();
      SimplePrincipal[] caller = {new SimplePrincipal("PostRole")};
      ProtectionDomain pd = new ProtectionDomain(null, null, null, caller);

      WebResourcePermission wrp = new WebResourcePermission("/restricted/post-only/", "POST");
      assertTrue("/restricted/post-only/ POST", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/restricted/post-only/x", "POST");
      assertTrue("/restricted/post-only/x POST", p.implies(pd, wrp));

      // Test the Restricted ANY security-constraint
      wrp = new WebResourcePermission("/restricted/any/x", "POST");
      assertTrue("/restricted/any/x POST", p.implies(pd, wrp));

      // Validate that the excluded subcontext if not accessible
      wrp = new WebResourcePermission("/restricted/post-only/excluded/x", "POST");
      assertFalse("/restricted/post-only/excluded/x POST", p.implies(pd, wrp));

      // Test that a GET to the Restricted POST security-constraint fails
      wrp = new WebResourcePermission("/restricted/post-only/x", "GET");
      assertFalse("/restricted/post-only/excluded/x GET", p.implies(pd, wrp));
      // Test that Restricted POST security-constraint fails
      wrp = new WebResourcePermission("/restricted/get-only/x", "POST");
      assertFalse("/restricted/get-only/x POST", p.implies(pd, wrp));

      // Change to otherUser to test failure
      caller = new SimplePrincipal[]{new SimplePrincipal("OtherRole")};
      pd = new ProtectionDomain(null, null, null, caller);

      // Test the Restricted Post security-constraint 
      wrp = new WebResourcePermission("/restricted/post-only", "POST");
      assertFalse("/restricted/post-only POST", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/restricted/post-only/x", "POST");
      assertFalse("/restricted/post-only/x POST", p.implies(pd, wrp));

   }

   protected void setUp() throws Exception
   {
      JBossWebMetaData metaData = new JBossWebMetaData();
      WebMetaData specMetaData = new WebMetaData();
      metaData.merge(null, specMetaData);
      List<SecurityConstraintMetaData> securityContraints = new ArrayList<SecurityConstraintMetaData>();

      addExcluded(securityContraints);
      addAllAccessSC(securityContraints);
      addRestrictedGetSC(securityContraints);
      addExcludedGetSC(securityContraints);
      addRestrictedPostSC(securityContraints);
      addExcludedPostSC(securityContraints);
      addRestrictedAnySC(specMetaData, securityContraints);
      addUnrestrictedSC(securityContraints);
      specMetaData.setSecurityContraints(securityContraints);

      DelegatingPolicy policy = new DelegatingPolicy();
      Policy.setPolicy(policy);
      JBossPolicyConfigurationFactory pcf = new JBossPolicyConfigurationFactory();
      pc = pcf.getPolicyConfiguration("WebConstraintsUnitTestCase", true);
      WebPermissionMapping.createPermissions(metaData, pc);
      pc.commit();
      System.out.println(policy.listContextPolicies());
      PolicyContext.setContextID("WebConstraintsUnitTestCase");
   }

   private void addExcluded(List<SecurityConstraintMetaData> securityContraints)
   {
      // security-constraint/ display-name = excluded
      SecurityConstraintMetaData sc1 = new SecurityConstraintMetaData();
      sc1.setDisplayName("excluded");
      // web-resource-collection/web-resource-name = No Access
      WebResourceCollectionMetaData wrc1 = new WebResourceCollectionMetaData();
      wrc1.setName("No Access");
      sc1.getResourceCollections().add(wrc1);

      wrc1.getUrlPatterns().add("/excluded/*");
      wrc1.getUrlPatterns().add("/restricted/get-only/excluded/*");
      wrc1.getUrlPatterns().add("/restricted/post-only/excluded/*");
      wrc1.getUrlPatterns().add("/restricted/any/excluded/*");
      wrc1.getUrlPatterns().add("/excluded/*");

      // web-resource-collection/web-resource-name = No Access
      WebResourceCollectionMetaData wrc2 = new WebResourceCollectionMetaData();
      wrc2.setName("No Access");
      sc1.getResourceCollections().add(wrc2);
      wrc2.getUrlPatterns().add("/restricted/*");
      wrc2.getUrlPatterns().add("DELETE");
      wrc2.getUrlPatterns().add("PUT");
      wrc2.getUrlPatterns().add("HEAD");
      wrc2.getUrlPatterns().add("OPTIONS");
      wrc2.getUrlPatterns().add("TRACE");
      wrc2.getUrlPatterns().add("GET");
      wrc2.getUrlPatterns().add("POST");

      // <auth-constraint />
      AuthConstraintMetaData excluded = new AuthConstraintMetaData();
      sc1.setAuthConstraint(excluded);

      // user-data-constraint/transport-guarantee
      UserDataConstraintMetaData none = new UserDataConstraintMetaData();
      none.setTransportGuarantee(TransportGuaranteeType.NONE);
      sc1.setUserDataConstraint(none);
      securityContraints.add(sc1);     
   }

   private void addAllAccessSC(List<SecurityConstraintMetaData> securityContraints)
   {
      // security-constraint/ display-name = AllAccessSC
      SecurityConstraintMetaData sc1 = new SecurityConstraintMetaData();
      sc1.setDisplayName("AllAccessSC");
      // web-resource-collection/web-resource-name = All Access
      WebResourceCollectionMetaData wrc1 = new WebResourceCollectionMetaData();
      wrc1.setName("All Access");
      sc1.getResourceCollections().add(wrc1);

      // All Access
      wrc1.getUrlPatterns().add("/unchecked/*");
      wrc1.getHttpMethods().add("DELETE");
      wrc1.getHttpMethods().add("PUT");
      wrc1.getHttpMethods().add("HEAD");
      wrc1.getHttpMethods().add("OPTIONS");
      wrc1.getHttpMethods().add("TRACE");
      wrc1.getHttpMethods().add("GET");
      wrc1.getHttpMethods().add("POST");

      // user-data-constraint/transport-guarantee
      UserDataConstraintMetaData none = new UserDataConstraintMetaData();
      none.setTransportGuarantee(TransportGuaranteeType.NONE);
      sc1.setUserDataConstraint(none);
      securityContraints.add(sc1);     
   }

   private void addRestrictedGetSC(List<SecurityConstraintMetaData> securityContraints)
   {
      // security-constraint/ display-name = RestrictedGetSC
      SecurityConstraintMetaData sc1 = new SecurityConstraintMetaData();
      sc1.setDisplayName("RestrictedGetSC");
      // web-resource-collection/web-resource-name = Restricted Access - Get Only
      WebResourceCollectionMetaData wrc1 = new WebResourceCollectionMetaData();
      wrc1.setName("Restricted Access - Get Only");
      sc1.getResourceCollections().add(wrc1);

      // All Access
      wrc1.getUrlPatterns().add("/restricted/get-only/*");
      wrc1.getHttpMethods().add("GET");

      // auth-constraint/role-name = GetRole
      AuthConstraintMetaData ac1 = new AuthConstraintMetaData();
      ac1.getRoleNames().add("GetRole");
      sc1.setAuthConstraint(ac1);

      // user-data-constraint/transport-guarantee
      UserDataConstraintMetaData none = new UserDataConstraintMetaData();
      none.setTransportGuarantee(TransportGuaranteeType.NONE);
      sc1.setUserDataConstraint(none);
      securityContraints.add(sc1);     
   }

   private void addExcludedGetSC(List<SecurityConstraintMetaData> securityContraints)
   {
      // security-constraint/ display-name = ExcludedGetSC
      SecurityConstraintMetaData sc1 = new SecurityConstraintMetaData();
      sc1.setDisplayName("ExcludedGetSC");
      // web-resource-collection/web-resource-name = Restricted Access - Get Only
      WebResourceCollectionMetaData wrc1 = new WebResourceCollectionMetaData();
      wrc1.setName("Restricted Access - Get Only");
      sc1.getResourceCollections().add(wrc1);

      // All Access
      wrc1.getUrlPatterns().add("/restricted/get-only/*");
      wrc1.getHttpMethods().add("DELETE");
      wrc1.getHttpMethods().add("PUT");
      wrc1.getHttpMethods().add("HEAD");
      wrc1.getHttpMethods().add("OPTIONS");
      wrc1.getHttpMethods().add("TRACE");
      wrc1.getHttpMethods().add("POST");

      // <auth-constraint />
      AuthConstraintMetaData excluded = new AuthConstraintMetaData();
      sc1.setAuthConstraint(excluded);

      // user-data-constraint/transport-guarantee
      UserDataConstraintMetaData none = new UserDataConstraintMetaData();
      none.setTransportGuarantee(TransportGuaranteeType.NONE);
      sc1.setUserDataConstraint(none);
      securityContraints.add(sc1);
   }

   private void addRestrictedPostSC(List<SecurityConstraintMetaData> securityContraints)
   {
      // security-constraint/ display-name = RestrictedGetSC
      SecurityConstraintMetaData sc1 = new SecurityConstraintMetaData();
      sc1.setDisplayName("RestrictedGetSC");
      // web-resource-collection/web-resource-name = Restricted Access - Post Only
      WebResourceCollectionMetaData wrc1 = new WebResourceCollectionMetaData();
      wrc1.setName("Restricted Access - Post Only");
      sc1.getResourceCollections().add(wrc1);

      // All Access
      wrc1.getUrlPatterns().add("/restricted/post-only/*");
      wrc1.getHttpMethods().add("POST");

      // auth-constraint/role-name = GetRole
      AuthConstraintMetaData ac1 = new AuthConstraintMetaData();
      ac1.getRoleNames().add("PostRole");
      sc1.setAuthConstraint(ac1);

      // user-data-constraint/transport-guarantee
      UserDataConstraintMetaData none = new UserDataConstraintMetaData();
      none.setTransportGuarantee(TransportGuaranteeType.NONE);
      sc1.setUserDataConstraint(none);
      securityContraints.add(sc1);     
   }

   private void addExcludedPostSC(List<SecurityConstraintMetaData> securityContraints)
   {
      // security-constraint/ display-name = ExcludedPostSC
      SecurityConstraintMetaData sc1 = new SecurityConstraintMetaData();
      sc1.setDisplayName("ExcludedPostSC");
      // web-resource-collection/web-resource-name = Restricted Access - Post Only
      WebResourceCollectionMetaData wrc1 = new WebResourceCollectionMetaData();
      wrc1.setName("Restricted Access - Post Only");
      sc1.getResourceCollections().add(wrc1);

      // All Access
      wrc1.getUrlPatterns().add("/restricted/post-only/*");
      wrc1.getHttpMethods().add("DELETE");
      wrc1.getHttpMethods().add("PUT");
      wrc1.getHttpMethods().add("HEAD");
      wrc1.getHttpMethods().add("OPTIONS");
      wrc1.getHttpMethods().add("TRACE");
      wrc1.getHttpMethods().add("GET");

      // <auth-constraint />
      AuthConstraintMetaData excluded = new AuthConstraintMetaData();
      sc1.setAuthConstraint(excluded);

      // user-data-constraint/transport-guarantee
      UserDataConstraintMetaData none = new UserDataConstraintMetaData();
      none.setTransportGuarantee(TransportGuaranteeType.NONE);
      sc1.setUserDataConstraint(none);
      securityContraints.add(sc1);
   }

   private void addRestrictedAnySC(WebMetaData wmd, List<SecurityConstraintMetaData> securityContraints)
   {
      // security-constraint/ display-name = RestrictedAnySC
      SecurityConstraintMetaData sc1 = new SecurityConstraintMetaData();
      sc1.setDisplayName("RestrictedAnySC");
      // web-resource-collection/web-resource-name = Restricted Access - Any
      WebResourceCollectionMetaData wrc1 = new WebResourceCollectionMetaData();
      wrc1.setName("Restricted Access - Any");
      sc1.getResourceCollections().add(wrc1);

      // All Access
      wrc1.getUrlPatterns().add("/restricted/any/*");
      wrc1.getHttpMethods().add("DELETE");
      wrc1.getHttpMethods().add("PUT");
      wrc1.getHttpMethods().add("HEAD");
      wrc1.getHttpMethods().add("OPTIONS");
      wrc1.getHttpMethods().add("TRACE");
      wrc1.getHttpMethods().add("GET");
      wrc1.getHttpMethods().add("POST");

      // auth-constraint/role-name = GetRole
      AuthConstraintMetaData ac1 = new AuthConstraintMetaData();
      ac1.getRoleNames().add("GetRole");
      ac1.getRoleNames().add("PostRole");
      sc1.setAuthConstraint(ac1);

      // user-data-constraint/transport-guarantee
      UserDataConstraintMetaData none = new UserDataConstraintMetaData();
      none.setTransportGuarantee(TransportGuaranteeType.NONE);
      sc1.setUserDataConstraint(none);
      securityContraints.add(sc1);     
   }

   private void addUnrestrictedSC(List<SecurityConstraintMetaData> securityContraints)
   {
      // security-constraint/ display-name = UnrestrictedSC
      SecurityConstraintMetaData sc1 = new SecurityConstraintMetaData();
      sc1.setDisplayName("UnrestrictedSC");
      // web-resource-collection/web-resource-name = Restricted Access - Any
      WebResourceCollectionMetaData wrc1 = new WebResourceCollectionMetaData();
      wrc1.setName("Restricted Access - Any");
      sc1.getResourceCollections().add(wrc1);

      // All Access
      wrc1.getUrlPatterns().add("/restricted/not/*");
      wrc1.getHttpMethods().add("DELETE");
      wrc1.getHttpMethods().add("PUT");
      wrc1.getHttpMethods().add("HEAD");
      wrc1.getHttpMethods().add("OPTIONS");
      wrc1.getHttpMethods().add("TRACE");
      wrc1.getHttpMethods().add("GET");
      wrc1.getHttpMethods().add("POST");

      // auth-constraint/role-name = unchecked
      sc1.setAuthConstraint(null);

      // user-data-constraint/transport-guarantee
      UserDataConstraintMetaData none = new UserDataConstraintMetaData();
      none.setTransportGuarantee(TransportGuaranteeType.NONE);
      sc1.setUserDataConstraint(none);
      securityContraints.add(sc1);
   }
}
