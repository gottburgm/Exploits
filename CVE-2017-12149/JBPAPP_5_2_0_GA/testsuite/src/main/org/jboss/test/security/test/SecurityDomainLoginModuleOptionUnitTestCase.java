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
package org.jboss.test.security.test;

import java.util.Map;

import javax.security.auth.login.Configuration;
import javax.security.auth.login.AppConfigurationEntry;

import org.jboss.logging.Logger;

import org.jboss.logging.XLevel;
import org.jboss.security.SecurityConstants;
import org.jboss.security.auth.login.XMLLoginConfigImpl;
import org.jboss.test.JBossTestCase;

/**
 * For changes made on JBAS-1477, the security domain name is added to every login
 * module option map by the ApplicationInfo object.  When TRACE logging is enable,
 * a login module will then display this option value for trouble shooting.  The
 * first part of test, displays how the security domain option is properly set
 * in a security domain that exists in Configuration.  The second test shows
 * how the "other" security domain is displayed when the original domain does not
 * exist in Configuration.
 *
 * @author chris griffith
 * @version $Revision: 81036 $
 */
public class SecurityDomainLoginModuleOptionUnitTestCase extends JBossTestCase
{

   public SecurityDomainLoginModuleOptionUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      // Setup the replacement properties
      System.setProperty("users.properties", "/security/config/users.properites");
      System.setProperty("roles.properties", "/security/config/roles.properites");

      // Install the custom JAAS configuration
      XMLLoginConfigImpl config = XMLLoginConfigImpl.getInstance();
      config.setConfigResource("security/login-config.xml");
      config.loadConfig();
      Configuration.setConfiguration(config);
   }

   public void testSecurityDomainLoginModuleOption() throws Exception
   {
      Configuration config = Configuration.getConfiguration();
      String validSecurityDomain = "testUsersRoles";
      String invalidSecurityDomain = "doesNotExist"; 

      getLog().info("testSecurityDomainLoginModuleOption");

      //get the app configuration for a valid security domain...
      AppConfigurationEntry[] entries = config.getAppConfigurationEntry(validSecurityDomain);
      assertTrue("Entries not null",entries != null);

      //for each login module configured in domain, check that the option is set as expected.
      for (int i=0;i<entries.length;i++)
      {
	 String loginModuleClass = entries[i].getLoginModuleName();
         String flag = entries[i].getControlFlag().toString();
	 Map options = entries[i].getOptions();

	 getLog().info(loginModuleClass + " is " + flag + "\nWith options...\n" + options);
	 
	 String option = (String)options.get(SecurityConstants.SECURITY_DOMAIN_OPTION);
	 assertTrue("Security domain option has value \"" + option + 
		    "\", it should be \"" + validSecurityDomain + "\"",
		    option.equals(validSecurityDomain));
      }

      //now get the app configuration for a domain that does not exist.
      entries = config.getAppConfigurationEntry(invalidSecurityDomain);
      assertTrue("Entries not null", entries != null);

      //for each login module config'ed in domain, check that the option is set as "other"
      for (int i=0;i<entries.length;i++)
      {
	 String loginModuleClass = entries[i].getLoginModuleName();
         String flag = entries[i].getControlFlag().toString();
	 Map options = entries[i].getOptions();

	 getLog().info(loginModuleClass + " is " + flag + "\nWith options...\n" + options);
	 
	 String option = (String)options.get(SecurityConstants.SECURITY_DOMAIN_OPTION);
	 assertTrue("Security domain option has value \"" + option + 
		    "\", it should be \"" + SecurityConstants.DEFAULT_APPLICATION_POLICY + "\"",
		    option.equals(SecurityConstants.DEFAULT_APPLICATION_POLICY));
      }
   }
}
