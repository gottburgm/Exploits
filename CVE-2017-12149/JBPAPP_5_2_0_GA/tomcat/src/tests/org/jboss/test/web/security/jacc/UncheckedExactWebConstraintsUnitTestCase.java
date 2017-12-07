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


/**
 * Test 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81037 $
 */
public class UncheckedExactWebConstraintsUnitTestCase extends TestCase
{
   private PolicyConfiguration pc;

   public void testUncheckedExact() throws Exception
   {
      Policy p = Policy.getPolicy();
      SimplePrincipal[] caller = null;
      ProtectionDomain pd = new ProtectionDomain(null, null, null, caller);

      WebResourcePermission wrp = new WebResourcePermission("/protected/exact/get/roleA", "GET");
      assertFalse("/protected/exact/get/roleA GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/protected/exact/get/roleA", "POST");
      assertFalse("/protected/exact/get/roleA POST", p.implies(pd, wrp));

      caller = new SimplePrincipal[]{new SimplePrincipal("RoleA")};
      wrp = new WebResourcePermission("/protected/exact/get/roleA", "GET");
      assertFalse("/protected/exact/get/roleA GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/protected/exact/get/roleA", "POST");
      assertFalse("/protected/exact/get/roleA POST", p.implies(pd, wrp));

      caller = new SimplePrincipal[]{new SimplePrincipal("RoleB")};
      pd = new ProtectionDomain(null, null, null, caller);
      wrp = new WebResourcePermission("/protected/exact/get/roleA", "GET");
      assertFalse("/protected/exact/get/roleA GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/protected/exact/get/roleA", "POST");
      assertTrue("/protected/exact/get/roleA POST", p.implies(pd, wrp));
   }

   protected void setUp() throws Exception
   {
      JBossWebMetaData metaData = new JBossWebMetaData();
      WebMetaData specMetaData = new WebMetaData();
      metaData.merge(null, specMetaData);
      List<SecurityConstraintMetaData> securityContraints = new ArrayList<SecurityConstraintMetaData>();

      addProtectedASC(securityContraints);
      addProtectedBSC(securityContraints);
      specMetaData.setSecurityContraints(securityContraints);

      DelegatingPolicy policy = new DelegatingPolicy();
      Policy.setPolicy(policy);
      JBossPolicyConfigurationFactory pcf = new JBossPolicyConfigurationFactory();
      pc = pcf.getPolicyConfiguration("UncheckedWebConstraintsUnitTestCase", true);
      WebPermissionMapping.createPermissions(metaData, pc);
      pc.commit();
      System.out.println(policy.listContextPolicies());
      PolicyContext.setContextID("UncheckedWebConstraintsUnitTestCase");
   }

   /*
   <security-constraint>
       <web-resource-collection>
           <web-resource-name>exact, get method, roleA</web-resource-name>
           <url-pattern>/protected/exact/get/roleA</url-pattern>
           <http-method>GET</http-method>
       </web-resource-collection>
       <auth-constraint>
           <role-name>RoleA</role-name>
       </auth-constraint>
       <user-data-constraint>
           <transport-guarantee>NONE</transport-guarantee>
       </user-data-constraint>
   </security-constraint>
   */
   private void addProtectedASC(List securityContraints)
   {
      // security-constraint/ display-name = ASC
      SecurityConstraintMetaData sc1 = new SecurityConstraintMetaData();
      sc1.setDisplayName("ASC");
      WebResourceCollectionMetaData wrc = new WebResourceCollectionMetaData();
      wrc.setName("exact, get method, roleA");
      sc1.getResourceCollections().add(wrc);
      securityContraints.add(sc1);

      // web-resource-collection/web-resource-name = exact, get method, roleA
      wrc.getUrlPatterns().add("/protected/exact/get/roleA");
      wrc.getHttpMethods().add("GET");

      // auth-constraint/role-name = RoleA
      AuthConstraintMetaData ac = new AuthConstraintMetaData();
      ArrayList<String> roles = new ArrayList<String>();
      roles.add("RoleA");
      ac.setRoleNames(roles);
      sc1.setAuthConstraint(ac);

      // user-data-constraint/transport-guarantee
      UserDataConstraintMetaData none = new UserDataConstraintMetaData();
      none.setTransportGuarantee(TransportGuaranteeType.NONE);
      sc1.setUserDataConstraint(none);
   }

   /*
   <security-constraint>
       <web-resource-collection>
           <web-resource-name>exact, get method, roleA verifier</web-resource-name>
           <url-pattern>/protected/exact/get/roleA</url-pattern>
           <http-method>POST</http-method>
           <http-method>PUT</http-method>
           <http-method>HEAD</http-method>
           <http-method>TRACE</http-method>
           <http-method>OPTIONS</http-method>
           <http-method>DELETE</http-method>
       </web-resource-collection>
       <auth-constraint>
           <role-name>RoleB</role-name>
       </auth-constraint>
   </security-constraint> 
   */
   private void addProtectedBSC(List securityContraints)
   {
      // security-constraint/ display-name = ASC
      SecurityConstraintMetaData sc1 = new SecurityConstraintMetaData();
      sc1.setDisplayName("ASC");
      WebResourceCollectionMetaData wrc = new WebResourceCollectionMetaData();
      wrc.setName("exact, get method, roleA verifier");
      sc1.getResourceCollections().add(wrc);
      securityContraints.add(sc1);

      // web-resource-collection/web-resource-name = exact, get method, roleA verifier
      wrc.getUrlPatterns().add("/protected/exact/get/roleA");
      wrc.getHttpMethods().add("POST");
      wrc.getHttpMethods().add("PUT");
      wrc.getHttpMethods().add("HEAD");
      wrc.getHttpMethods().add("TRACE");
      wrc.getHttpMethods().add("OPTIONS");
      wrc.getHttpMethods().add("DELETE");

      // auth-constraint/role-name = RoleB
      AuthConstraintMetaData ac = new AuthConstraintMetaData();
      ArrayList<String> roles = new ArrayList<String>();
      roles.add("RoleB");
      ac.setRoleNames(roles);
      sc1.setAuthConstraint(ac);
   }
}
