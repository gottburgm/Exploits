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
import org.jboss.metadata.web.spec.SecurityConstraintMetaData;
import org.jboss.metadata.web.spec.TransportGuaranteeType;
import org.jboss.metadata.web.spec.UserDataConstraintMetaData;
import org.jboss.metadata.web.spec.WebMetaData;
import org.jboss.metadata.web.spec.WebResourceCollectionMetaData;
import org.jboss.metadata.web.spec.WebResourceCollectionsMetaData;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.jacc.DelegatingPolicy;
import org.jboss.security.jacc.JBossPolicyConfigurationFactory;
import org.jboss.web.WebPermissionMapping;

/**

 * @author Scott.Stark@jboss.org
 * @version $Revision: 81037 $
 */
public class DataWebConstraintsUnitTestCase extends TestCase
{
   public void testUncheckedExact() throws Exception
   {
      Policy p = Policy.getPolicy();
      SimplePrincipal[] caller = null;
      ProtectionDomain pd = new ProtectionDomain(null, null, null, caller);

      WebResourcePermission wrp = new WebResourcePermission("/", "GET");
      assertTrue("/ GET", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/", "POST");
      assertTrue("/ POST", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/any", "POST");
      assertTrue("/any POST", p.implies(pd, wrp));
      wrp = new WebResourcePermission("/", "DELETE");
      assertTrue("/any DELETE", p.implies(pd, wrp));

   }

   protected void setUp() throws Exception
   {
      PolicyConfiguration pc;
      JBossWebMetaData metaData = new JBossWebMetaData();
      WebMetaData specMetaData = new WebMetaData();
      metaData.merge(null, specMetaData);
      SecurityConstraintMetaData sc = new SecurityConstraintMetaData();
      addSC(sc);
      List<SecurityConstraintMetaData> securityContraints = new ArrayList<SecurityConstraintMetaData>();
      securityContraints.add(sc);
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
           <web-resource-name>SSL Only</web-resource-name>
           <url-pattern>/*</url-pattern>
       </web-resource-collection>
       <user-data-constraint>
            <transport-guarantee>CONFIDENTIAL</transport-guarantee>
       </user-data-constraint>
   </security-constraint>
   */
   private void addSC(SecurityConstraintMetaData securityContraints)
   {
      WebResourceCollectionMetaData wsmd = new WebResourceCollectionMetaData();
      securityContraints.getResourceCollections().add(wsmd);
      // web-resource-collection/web-resource-name = exact, get method, roleA
      wsmd.setName("SSL Only");
      wsmd.getUrlPatterns().add("/*");
      // A null set of roles is unchecked
      securityContraints.getAuthConstraint().setRoleNames(null);
      UserDataConstraintMetaData udc = new UserDataConstraintMetaData();
      udc.setTransportGuarantee(TransportGuaranteeType.CONFIDENTIAL);
      securityContraints.setUserDataConstraint(udc);
   }

}
