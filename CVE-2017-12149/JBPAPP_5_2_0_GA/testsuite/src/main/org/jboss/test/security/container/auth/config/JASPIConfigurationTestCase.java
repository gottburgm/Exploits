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
package org.jboss.test.security.container.auth.config;
 
//$Id: JASPIConfigurationTestCase.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;

import org.jboss.security.auth.container.config.AuthModuleEntry;
import org.jboss.security.auth.login.AuthenticationInfo;
import org.jboss.security.auth.login.BaseAuthenticationInfo; 
import org.jboss.security.auth.login.JASPIAuthenticationInfo;
import org.jboss.security.auth.login.LoginConfigObjectModelFactory;
import org.jboss.security.auth.login.LoginModuleStackHolder; 
import org.jboss.security.auth.spi.UsersObjectModelFactory;
import org.jboss.security.config.ApplicationPolicy;
import org.jboss.security.config.PolicyConfig;
import org.jboss.test.JBossTestCase;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;

/**
 *  Tests the new login-config.xml based on JSR-196
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Dec 19, 2005
 */
public class JASPIConfigurationTestCase extends JBossTestCase
{  
   public JASPIConfigurationTestCase(String name)
   {
      super(name); 
   }  
   
   public void testJaasAuthenticationInfo() throws Exception
   {
      PolicyConfig config = getPolicyConfig("security/jaspi/login-config-jaspi.xml",
            new LoginConfigObjectModelFactory());
      assertNotNull("Returned PolicyConfig is != null ?", config);
      ApplicationPolicy aPolicy = config.get("JBossWS");
      BaseAuthenticationInfo infoBase = aPolicy.getAuthenticationInfo();
      assertTrue("infoBase==AuthenticationInfo", infoBase instanceof AuthenticationInfo);
      AuthenticationInfo info = (AuthenticationInfo)infoBase; 
      assertTrue("JBossWS != null", info != null);
      AppConfigurationEntry[] entries = info.getAppConfigurationEntry();
      assertTrue("entries.length == 1", entries.length == 1);
      AppConfigurationEntry ace = entries[0];
      assertTrue("org.jboss.security.auth.spi.UsersRolesLoginModule",
         ace.getLoginModuleName().equals("org.jboss.security.auth.spi.UsersRolesLoginModule"));
      Map options = ace.getOptions();
      assertTrue("Options.size == 3", options.size() == 3);
      String guest = (String) options.get("unauthenticatedIdentity");
      assertTrue("anonymous", guest.equals("anonymous"));
      String users = (String) options.get("usersProperties");
      assertTrue("anonymous", users.equals("props/jbossws-users.properties"));
      String roles = (String) options.get("rolesProperties");
      assertTrue("anonymous", roles.equals("props/jbossws-roles.properties")); 
   }
   
   public void testJaspiAuthenticationInfo() throws Exception
   {
      LoginConfigObjectModelFactory lcomf = new LoginConfigObjectModelFactory(); 
      PolicyConfig config = getPolicyConfig("security/jaspi/login-config-jaspi.xml",lcomf);
      assertNotNull("Returned PolicyConfig is != null ?", config);
      ApplicationPolicy aPolicy = config.get("jaspi");
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
   }
   
   protected void validateAuthModule1(AuthModuleEntry aEntry1)
   { 
      assertEquals("auth.module1.class.name", aEntry1.getAuthModuleName());
      Map aEntry1Options = aEntry1.getOptions();
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
   
   protected void validateAuthModule2(AuthModuleEntry aEntry2)
   { 
      assertEquals("auth.module2.class.name", aEntry2.getAuthModuleName());
      LoginModuleStackHolder lmsh = aEntry2.getLoginModuleStackHolder();
      assertNotNull("LoginModuleStackHolder  != null", lmsh);
      assertEquals("lm-stack", lmsh.getName());
   }
   
   protected PolicyConfig getPolicyConfig(String config,
         LoginConfigObjectModelFactory lcomf ) throws Exception
   {
      UsersObjectModelFactory uomf = new UsersObjectModelFactory();
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(config);
      InputStreamReader xmlReader = new InputStreamReader(is); 
      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      unmarshaller.setNamespaceAware(true);
      unmarshaller.setValidation(true);
      unmarshaller.mapFactoryToNamespace(uomf, "http://www.jboss.org/j2ee/schemas/XMLLoginModule");
      return (PolicyConfig) unmarshaller.unmarshal(xmlReader, lcomf, null); 
   } 
}
