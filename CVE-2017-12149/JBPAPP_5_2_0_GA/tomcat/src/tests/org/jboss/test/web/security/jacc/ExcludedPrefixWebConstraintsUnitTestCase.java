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

import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.WebResourcePermission;

import junit.framework.TestCase;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.AuthConstraintMetaData;
import org.jboss.metadata.web.spec.SecurityConstraintMetaData;
import org.jboss.metadata.web.spec.TransportGuaranteeType;
import org.jboss.metadata.web.spec.UserDataConstraintMetaData;
import org.jboss.metadata.web.spec.WebMetaData;
import org.jboss.metadata.web.spec.WebResourceCollectionMetaData;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.jacc.DelegatingPolicy;
import org.jboss.security.jacc.JBossPolicyConfigurationFactory;
import org.jboss.web.WebPermissionMapping;

/** Test of the unchecked permission
 
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
      <web-resource-collection>
         <web-resource-name>Excluded</web-resource-name>
         <url-pattern>/restricted/post-only/excluded/*</url-pattern>
         <url-pattern>/*</url-pattern>
      </web-resource-collection>
      <auth-constraint />
      <user-data-constraint>
         <transport-guarantee>NONE</transport-guarantee>
      </user-data-constraint>
   </security-constraint>

   <security-constraint>
      <web-resource-collection>
         <web-resource-name>Restricted POST</web-resource-name>
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
      <web-resource-collection>
         <web-resource-name>Excluded POST</web-resource-name>
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
public class ExcludedPrefixWebConstraintsUnitTestCase extends TestCase
{
   private PolicyConfiguration pc;

   public void testUncheckedPrefix() throws Exception
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

   protected void setUp() throws Exception
   {
      JBossWebMetaData metaData = new JBossWebMetaData();
      WebMetaData specMetaData = new WebMetaData();
      metaData.merge(null, specMetaData);
      List<SecurityConstraintMetaData> securityContraints = new ArrayList<SecurityConstraintMetaData>();
      addSC(securityContraints);
      specMetaData.setSecurityContraints(securityContraints);

      DelegatingPolicy policy = new DelegatingPolicy();
      Policy.setPolicy(policy);
      JBossPolicyConfigurationFactory pcf = new JBossPolicyConfigurationFactory();
      pc = pcf.getPolicyConfiguration("UncheckedPrefixWebConstraintsUnitTestCase", true);
      WebPermissionMapping.createPermissions(metaData, pc);
      pc.commit();
      System.out.println(policy.listContextPolicies());
      PolicyContext.setContextID("UncheckedPrefixWebConstraintsUnitTestCase");
   }

   private void addSC(List<SecurityConstraintMetaData> securityContraints)
   {
      // security-constraint/ display-name = excluded
      SecurityConstraintMetaData sc1 = new SecurityConstraintMetaData();
      sc1.setDisplayName("excluded");
      WebResourceCollectionMetaData wrcex = new WebResourceCollectionMetaData();
      wrcex.setName("excluded");
      sc1.getResourceCollections().add(wrcex);
      // web-resource-collection/web-resource-name = No Access
      wrcex.getUrlPatterns().add("/excluded/*");
      wrcex.getUrlPatterns().add("/restricted/get-only/excluded/*");
      wrcex.getUrlPatterns().add("/restricted/post-only/excluded/*");
      wrcex.getUrlPatterns().add("/restricted/any/excluded/*");
      wrcex.getUrlPatterns().add("/excluded/*");

      // web-resource-collection/web-resource-name = No Access
      WebResourceCollectionMetaData wrcna = new WebResourceCollectionMetaData();
      wrcna.setName("No Access");
      sc1.getResourceCollections().add(wrcna);
      wrcna.getUrlPatterns().add("/restricted/*");
      wrcna.getHttpMethods().add("DELETE");
      wrcna.getHttpMethods().add("PUT");
      wrcna.getHttpMethods().add("HEAD");
      wrcna.getHttpMethods().add("OPTIONS");
      wrcna.getHttpMethods().add("TRACE");
      wrcna.getHttpMethods().add("GET");
      wrcna.getHttpMethods().add("POST");

      AuthConstraintMetaData excluded = new AuthConstraintMetaData();
      sc1.setAuthConstraint(excluded);
      UserDataConstraintMetaData none = new UserDataConstraintMetaData();
      none.setTransportGuarantee(TransportGuaranteeType.NONE);
      sc1.setUserDataConstraint(none);

      // security-constraint/ display-name = unchecked
      SecurityConstraintMetaData sc2 = new SecurityConstraintMetaData();
      sc1.setDisplayName("unchecked");
      WebResourceCollectionMetaData wrcun = new WebResourceCollectionMetaData();
      wrcun.setName("Unchecked");
      sc2.getResourceCollections().add(wrcun);
      wrcun.getUrlPatterns().add("/unchecked/*");
      wrcun.getUrlPatterns().add("/restricted/not/*");
      wrcun.getHttpMethods().add("DELETE");
      wrcun.getHttpMethods().add("PUT");
      wrcun.getHttpMethods().add("HEAD");
      wrcun.getHttpMethods().add("OPTIONS");
      wrcun.getHttpMethods().add("TRACE");
      wrcun.getHttpMethods().add("GET");
      wrcun.getHttpMethods().add("POST");

      // no auth-constraint
      sc2.setAuthConstraint(null);
      // user-data-constraint/transport-guarantee
      sc2.setUserDataConstraint(none);
   }

}
