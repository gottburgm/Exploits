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
public class UncheckedPrefixWebConstraintsUnitTestCase extends TestCase
{
   private PolicyConfiguration pc;

   public void testUncheckedPrefix() throws Exception
   {
      Policy p = Policy.getPolicy();
      SimplePrincipal[] caller = null;
      ProtectionDomain pd = new ProtectionDomain(null, null, null, caller);

      // There should be no 
      WebResourcePermission wrp = new WebResourcePermission("/restricted/post-only/x", "GET");
      assertFalse("/restricted/post-only/x GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/restricted/post-only/x", "POST");
      assertFalse("/restricted/post-only/x POST", p.implies(pd, wrp));

      caller = new SimplePrincipal[]{new SimplePrincipal("PostRole")};
      pd = new ProtectionDomain(null, null, null, caller);
      wrp = new WebResourcePermission("/restricted/post-only/x", "GET");
      assertFalse("/restricted/post-only/x GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/restricted/post-only/x", "POST");
      assertTrue("/restricted/post-only/x POST", p.implies(pd, wrp));

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

   private void addSC(List securityContraints)
   {
      // security-constraint/ display-name = SC1
      SecurityConstraintMetaData sc1 = new SecurityConstraintMetaData();
      sc1.setDisplayName("SC1");
      // web-resource-collection/web-resource-name = Excluded
      WebResourceCollectionMetaData wrc1 = new WebResourceCollectionMetaData();
      wrc1.setName("Excluded");
      sc1.getResourceCollections().add(wrc1);
      wrc1.getUrlPatterns().add("/restricted/post-only/excluded/*");
      wrc1.getUrlPatterns().add("/*");

      // <auth-constraint />
      AuthConstraintMetaData excluded = new AuthConstraintMetaData();
      sc1.setAuthConstraint(excluded);

      // user-data-constraint/transport-guarantee
      UserDataConstraintMetaData none = new UserDataConstraintMetaData();
      none.setTransportGuarantee(TransportGuaranteeType.NONE);
      sc1.setUserDataConstraint(none);
      securityContraints.add(sc1);

      SecurityConstraintMetaData sc2 = new SecurityConstraintMetaData();
      sc2.setDisplayName("SC2");
      // web-resource-collection/web-resource-name = Restricted POST
      WebResourceCollectionMetaData wrc2 = new WebResourceCollectionMetaData();
      wrc2.setName("Restricted POST");
      sc2.getResourceCollections().add(wrc2);
      wrc2.getUrlPatterns().add("/restricted/post-only/*");
      wrc2.getHttpMethods().add("POST");
      AuthConstraintMetaData ac2 = new AuthConstraintMetaData();
      ac2.getRoleNames().add("PostRole");
      sc2.setAuthConstraint(ac2);
      sc2.setUserDataConstraint(none);
      securityContraints.add(sc2);

      SecurityConstraintMetaData sc3 = new SecurityConstraintMetaData();
      sc3.setDisplayName("SC3");
      // web-resource-collection/web-resource-name = Excluded POST
      WebResourceCollectionMetaData wrc3 = new WebResourceCollectionMetaData();
      wrc3.setName("Excluded POST");
      wrc3.getUrlPatterns().add("/restricted/post-only/*");
      wrc3.getHttpMethods().add("DELETE");
      wrc3.getHttpMethods().add("PUT");
      wrc3.getHttpMethods().add("HEAD");
      wrc3.getHttpMethods().add("OPTIONS");
      wrc3.getHttpMethods().add("TRACE");
      wrc3.getHttpMethods().add("GET");
      sc3.setAuthConstraint(excluded);
      sc3.setUserDataConstraint(none);
      securityContraints.add(sc3);
   }

}
