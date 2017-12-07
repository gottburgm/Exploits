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
package org.jboss.test.security.test.mapping;
 
import java.util.Map;

import org.jboss.security.auth.container.config.AuthModuleEntry;
import org.jboss.security.auth.login.BaseAuthenticationInfo;
import org.jboss.security.auth.login.JASPIAuthenticationInfo;
import org.jboss.security.authorization.config.SecurityConfigObjectModelFactory;
import org.jboss.security.config.ApplicationPolicy;
import org.jboss.security.config.MappingInfo;
import org.jboss.security.config.PolicyConfig;
import org.jboss.security.mapping.config.MappingModuleEntry;
import org.jboss.test.security.container.auth.config.JASPIConfigurationTestCase;

//$Id: RoleMappingConfigUnitTestCase.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

/**
 *  Test the RoleMapping Framework Configuration
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  August 24, 2006 
 *  @version $Revision: 85945 $
 */
public class RoleMappingConfigUnitTestCase extends JASPIConfigurationTestCase
{ 
   public RoleMappingConfigUnitTestCase(String name)
   {
      super(name); 
   } 
   
   public void testRoleMappingInfo() throws Exception
   {
      String loc = "security/authorization/config/rolemapping-config.xml";
      PolicyConfig config = getPolicyConfig(loc,new SecurityConfigObjectModelFactory());
      assertNotNull("Returned PolicyConfig is != null ?", config);
      
      ApplicationPolicy aPolicy = config.get("TestRoleMapping"); 
      //Test Authentication
      BaseAuthenticationInfo infoBase = aPolicy.getAuthenticationInfo();
      assertTrue("infoBase==AuthenticationJaspiInfo", infoBase instanceof JASPIAuthenticationInfo);
      JASPIAuthenticationInfo info = (JASPIAuthenticationInfo)infoBase; 
      assertTrue("jaspi != null", info != null); 
      AuthModuleEntry[] authEntry = info.getAuthModuleEntry();
      //Get the first AuthModule
      AuthModuleEntry aEntry1 = authEntry[0];
      validateAuthModule1(aEntry1); 
      //Get the second AuthModule
      AuthModuleEntry aEntry2 = authEntry[1];
      validateAuthModule2(aEntry2);
      
      //Test RoleMapping 
      MappingInfo authzInfo = aPolicy.getRoleMappingInfo();
      MappingModuleEntry[] authzEntries = authzInfo.getMappingModuleEntry();
      assertTrue("AuthzInfo != null", authzInfo != null);
      assertTrue("authzEntries has 1 element", authzEntries.length == 1);
      // Get the first AuthorizationModuleEntry
      MappingModuleEntry azEntry1 = authzEntries[0];
      validateRoleMappingModuleEntry(azEntry1);  
   }
   
   private void validateRoleMappingModuleEntry(MappingModuleEntry ame)
   {
      assertEquals("policy.module1.class.name", ame.getMappingModuleName());
      Map aEntry1Options = ame.getOptions();
      assertNotNull("Options in the first AuthModule != null", aEntry1Options);
      assertTrue( "Length of options == 3", aEntry1Options.size() == 3); 
      String usersProperties = (String) aEntry1Options.get("usersProperties");
      assertNotNull("options.usersProperties exists", usersProperties);
      assertTrue("options.usersProperties == props/jbossws-users.properties",
            usersProperties.equals("props/jbossws-users.properties"));
      String rolesProperties = (String) aEntry1Options.get("rolesProperties");
      assertNotNull("options.rolesProperties exists", rolesProperties);
      assertTrue("options.rolesProperties == props/jbossws-roles.properties",
            rolesProperties.equals("props/jbossws-roles.properties"));
   } 
}
