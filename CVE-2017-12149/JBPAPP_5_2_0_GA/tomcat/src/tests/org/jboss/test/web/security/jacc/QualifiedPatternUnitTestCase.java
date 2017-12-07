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
import javax.security.jacc.WebUserDataPermission;

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
public class QualifiedPatternUnitTestCase extends TestCase
{
   private PolicyConfiguration pc;

   public void testUnchecked() throws Exception
   {
      Policy p = Policy.getPolicy();
      SimplePrincipal[] caller = null;
      ProtectionDomain pd = new ProtectionDomain(null, null, null, caller);

      WebResourcePermission wrp = new WebResourcePermission("/a", "GET");
      assertTrue("/a GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/a", "POST");
      assertTrue("/a POST", p.implies(pd, wrp));

      caller = new SimplePrincipal[]{new SimplePrincipal("R1")};
      pd = new ProtectionDomain(null, null, null, caller);
      wrp = new WebResourcePermission("/a/x", "GET");
      assertTrue("/a/x GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/a/x", "POST");
      boolean implied = p.implies(pd, wrp);
      assertTrue("/a/x POST", implied);
      wrp = new WebResourcePermission("/b/x", "GET");
      assertTrue("/b/x GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/b/x", "POST");
      assertTrue("/b/x POST", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/b/x", "DELETE");
      assertFalse("/b/x DELETE", p.implies(pd, wrp));

      wrp = new WebResourcePermission("/a/x.asp", "GET");
      assertTrue("/a/x.asp GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/a/x.asp", "POST");
      assertTrue("/a/x.asp POST", p.implies(pd, wrp));

      WebUserDataPermission wudp = new WebUserDataPermission("/a/*:/a", "GET:CONFIDENTIAL");
      assertTrue("/a/*:/a GET:CONFIDENTIAL", p.implies(pd, wudp));
      wudp = new WebUserDataPermission("/a/*:/a", "GET:CONFIDENTIAL");
      assertTrue("/b/*:/b GET,POST:CONFIDENTIAL", p.implies(pd, wudp));
      
   }

   protected void setUp() throws Exception
   {
      JBossWebMetaData metaData = new JBossWebMetaData();
      WebMetaData specMetaData = new WebMetaData();
      metaData.merge(null, specMetaData);
      List<SecurityConstraintMetaData> securityContraints = new ArrayList<SecurityConstraintMetaData>();

      addSC1(securityContraints);
      addSC2(securityContraints);
      specMetaData.setSecurityContraints(securityContraints);

      DelegatingPolicy policy = new DelegatingPolicy();
      Policy.setPolicy(policy);
      JBossPolicyConfigurationFactory pcf = new JBossPolicyConfigurationFactory();
      pc = pcf.getPolicyConfiguration("QualifiedPatternUnitTestCase", true);
      WebPermissionMapping.createPermissions(metaData, pc);
      pc.commit();
      System.out.println(policy.listContextPolicies());
      PolicyContext.setContextID("QualifiedPatternUnitTestCase");
   }

   /*
   <security-constraint>
      <web-resource-collection>
         <web-resource-name>sc1.c1</web-resource-name>
         <url-pattern>/a/*</url-pattern>
         <url-pattern>/b/*</url-pattern>
         <url-pattern>/a</url-pattern>
         <url-pattern>/b</url-pattern>
         <http-method>DELETE</http-method>
         <http-method>PUT</http-method>
      </web-resource-collection>
      <web-resource-collection>
      <web-resource-name>sc1.c2</web-resource-name>
         <url-pattern>*.asp</url-pattern>
      </web-resource-collection>
      <auth-constraint/>
   </security-constraint>
   */
   private void addSC1(List securityContraints)
   {
      // security-constraint/ display-name = SC1
      SecurityConstraintMetaData sc1 = new SecurityConstraintMetaData();
      sc1.setDisplayName("SC1");
      WebResourceCollectionMetaData wrcsc1c1 = new WebResourceCollectionMetaData();
      wrcsc1c1.setName("sc1.c1");
      sc1.getResourceCollections().add(wrcsc1c1);

      // web-resource-collection/web-resource-name = sc1.c1
      wrcsc1c1.getUrlPatterns().add("/a/*");
      wrcsc1c1.getUrlPatterns().add("/b/*");
      wrcsc1c1.getUrlPatterns().add("/a");
      wrcsc1c1.getUrlPatterns().add("/b");
      wrcsc1c1.getHttpMethods().add("DELETE");
      wrcsc1c1.getHttpMethods().add("PUT");

      WebResourceCollectionMetaData wrcsc1c2 = new WebResourceCollectionMetaData();
      wrcsc1c2.setName("sc1.c2");
      sc1.getResourceCollections().add(wrcsc1c2);
      wrcsc1c2.getUrlPatterns().add("*.asp");

      sc1.setAuthConstraint(new AuthConstraintMetaData());
      assertTrue(sc1.isExcluded());
      securityContraints.add(sc1);
   }

   /*
   <security-constraint>
      <web-resource-collection>
         <web-resource-name>sc2.c1</web-resource-name>
         <url-pattern>/a/*</url-pattern>
         <url-pattern>/b/*</url-pattern>
         <http-method>GET</http-method>
      </web-resource-collection>
      <web-resource-collection>
         <web-resource-name>sc2.c2</web-resource-name>
         <url-pattern>/b/*</url-pattern>
         <http-method>POST</http-method>
      </web-resource-collection>
      <auth-constraint>
         <role-name>R1</role-name>
      </auth-constraint>
      <user-data-constraint>
         <transport-guarantee>CONFIDENTIAL</transport-guarantee>
      </user-data-constraint>
   </security-constraint>
   */
   private void addSC2(List securityContraints)
   {
      // security-constraint/ display-name = SC2
      SecurityConstraintMetaData sc2 = new SecurityConstraintMetaData();
      sc2.setDisplayName("SC2");
      WebResourceCollectionMetaData wrcsc2c1 = new WebResourceCollectionMetaData();
      wrcsc2c1.setName("sc2.c1");
      sc2.getResourceCollections().add(wrcsc2c1);

      // web-resource-collection/web-resource-name = sc2.c1
      wrcsc2c1.getUrlPatterns().add("/a/*");
      wrcsc2c1.getUrlPatterns().add("/b/*");
      wrcsc2c1.getHttpMethods().add("GET");

      // web-resource-collection/web-resource-name = sc2.c2
      WebResourceCollectionMetaData wrcsc2c2 = new WebResourceCollectionMetaData();
      wrcsc2c2.setName("sc2.c2");
      sc2.getResourceCollections().add(wrcsc2c2);
      wrcsc2c2.getUrlPatterns().add("/b/*");
      wrcsc2c2.getHttpMethods().add("POST");

      AuthConstraintMetaData ac = new AuthConstraintMetaData();
      ArrayList<String> roles = new ArrayList<String>();
      roles.add("R1");
      ac.setRoleNames(roles);
      sc2.setAuthConstraint(ac);
      UserDataConstraintMetaData udc = new UserDataConstraintMetaData();
      udc.setTransportGuarantee(TransportGuaranteeType.CONFIDENTIAL);
      sc2.setUserDataConstraint(udc);
      securityContraints.add(sc2);
   }
}
