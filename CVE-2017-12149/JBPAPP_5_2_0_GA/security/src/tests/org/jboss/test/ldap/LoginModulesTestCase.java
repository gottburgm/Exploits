/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ldap;

import java.lang.reflect.Method;
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level; 
import java.util.logging.ConsoleHandler;
import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jboss.logging.Logger;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.callback.UsernamePasswordHandler;
import org.jboss.security.plugins.JaasSecurityDomain;

/** Tests of the LoginModule classes.

 @author Scott.Stark@jboss.org
 @version $Revision: 85945 $
 */
public class LoginModulesTestCase extends TestCase
{
   static
   {
      try
      {
         Configuration.setConfiguration(new TestConfig());
         System.out.println("Installed TestConfig as JAAS Configuration");
         Logger.setPluginClassName("org.jboss.logging.JDK14LoggerPlugin");
         java.util.logging.Logger security = java.util.logging.Logger.getLogger("org.jboss.security");
         security.setLevel(Level.FINEST);
         ConsoleHandler console = new ConsoleHandler();
         console.setLevel(Level.FINEST);
         security.addHandler(console);
         Logger log = Logger.getLogger("org.jboss.security");
         log.trace("Configured JDK trace logging");
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
   /** Hard coded login configurations for the test cases. The configuration
    name corresponds to the unit test function that uses the configuration.
    */
   static class TestConfig extends Configuration
   {
      public void refresh()
      {
      }

      public AppConfigurationEntry[] getAppConfigurationEntry(String name)
      {
         AppConfigurationEntry[] entry = null;
         try
         {
            Class[] parameterTypes = {};
            Method m = getClass().getDeclaredMethod(name, parameterTypes);
            Object[] args = {};
            entry = (AppConfigurationEntry[]) m.invoke(this, args);
         }
         catch(Exception e)
         {
         }
         return entry;
      }
      AppConfigurationEntry[] testLdapExample1()
      {
         String name = "org.jboss.security.auth.spi.LdapLoginModule";
         HashMap options = new HashMap();
         options.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
         options.put("java.naming.provider.url", "ldap://lamia/");
         options.put("java.naming.security.authentication", "simple");
         options.put("principalDNPrefix", "uid=");
         options.put("principalDNSuffix", ",ou=People,dc=jboss,dc=org");
         options.put("rolesCtxDN", "ou=Roles,dc=jboss,dc=org");
         options.put("uidAttributeID", "member");
         options.put("matchOnUserDN", "true");
         options.put("roleAttributeID", "cn");
         options.put("roleAttributeIsDN", "false");
         options.put("searchTimeLimit", "5000");
         options.put("searchScope", "ONELEVEL_SCOPE");
         AppConfigurationEntry ace = new AppConfigurationEntry(name,
         AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
         AppConfigurationEntry[] entry = {ace};
         return entry;
      }
      AppConfigurationEntry[] testLdapExample11()
      {
         String name = "org.jboss.security.auth.spi.LdapLoginModule";
         HashMap options = new HashMap();
         options.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
         options.put("java.naming.provider.url", "ldap://lamia/");
         options.put("java.naming.security.authentication", "simple");
         options.put("java.naming.security.principal", "cn=Root,dc=jboss,dc=org");
         options.put("java.naming.security.credentials", "secret1");

         options.put("principalDNPrefix", "uid=");
         options.put("principalDNSuffix", ",ou=People,dc=jboss,dc=org");
         options.put("rolesCtxDN", "ou=Roles,dc=jboss,dc=org");
         options.put("uidAttributeID", "member");
         options.put("matchOnUserDN", "true");
         options.put("roleAttributeID", "cn");
         options.put("roleAttributeIsDN", "false");
         AppConfigurationEntry ace = new AppConfigurationEntry(name,
         AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
         AppConfigurationEntry[] entry = {ace};
         return entry;
      }
      AppConfigurationEntry[] testLdapExample11Encrypt()
      {
         String name = "org.jboss.security.auth.spi.LdapLoginModule";
         HashMap options = new HashMap();
         options.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
         options.put("java.naming.provider.url", "ldap://lamia/");
         options.put("java.naming.security.authentication", "simple");
         options.put("java.naming.security.principal", "cn=Root,dc=jboss,dc=org");
         // secret1 encrypted
         options.put("java.naming.security.credentials", "7hInTB4HCBL");

         options.put("jaasSecurityDomain", "jboss.test:service=JaasSecurityDomain,domain=testLdapExample11Encrypt");
         options.put("principalDNPrefix", "uid=");
         options.put("principalDNSuffix", ",ou=People,dc=jboss,dc=org");
         options.put("rolesCtxDN", "ou=Roles,dc=jboss,dc=org");
         options.put("uidAttributeID", "member");
         options.put("matchOnUserDN", "true");
         options.put("roleAttributeID", "cn");
         options.put("roleAttributeIsDN", "false");
         AppConfigurationEntry ace = new AppConfigurationEntry(name,
         AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
         AppConfigurationEntry[] entry = {ace};
         return entry;
      }
      AppConfigurationEntry[] testLdapExample2()
      {
         String name = "org.jboss.security.auth.spi.LdapLoginModule";
         HashMap options = new HashMap();
         options.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
         options.put("java.naming.provider.url", "ldap://lamia/");
         options.put("java.naming.security.authentication", "simple");
         options.put("principalDNPrefix", "uid=");
         options.put("principalDNSuffix", ",ou=People,o=example2,dc=jboss,dc=org");
         options.put("rolesCtxDN", "ou=Roles,o=example2,dc=jboss,dc=org");
         options.put("uidAttributeID", "uid");
         options.put("matchOnUserDN", "false");
         options.put("roleAttributeID", "memberOf");
         options.put("roleAttributeIsDN", "true");
         options.put("roleNameAttributeID", "cn");

         AppConfigurationEntry ace = new AppConfigurationEntry(name,
         AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
         AppConfigurationEntry[] entry = {ace};
         return entry;
      }

      /**
      testLdapExample21 {
         org.jboss.security.auth.spi.LdapExtLoginModule
            java.naming.factory.initial=com.sun.jndi.ldap.LdapCtxFactory
            java.naming.provider.url="ldap://lamia/"
            java.naming.security.authentication=simple
            bindDN="cn=Root,dc=jboss,dc=org"
            bindCredential=secret1
            baseCtxDN="ou=People,dc=jboss,dc=org"
            baseFilter="(uid={0})"
            rolesCtxDN="ou=Roles,dc=jboss,dc=org";
            roleFilter="(member={1})"
            roleAttributeID="cn"
            roleRecursion=0
      };
      */
      AppConfigurationEntry[] testLdapExample21()
      {
         String name = "org.jboss.security.auth.spi.LdapExtLoginModule";
         HashMap options = new HashMap();
         options.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
         options.put("java.naming.provider.url", "ldap://lamia/");
         options.put("java.naming.security.authentication", "simple");

         options.put("bindDN", "cn=Root,dc=jboss,dc=org");
         options.put("bindCredential", "secret1");
         options.put("baseCtxDN", "ou=People,dc=jboss,dc=org");
         options.put("baseFilter", "(uid={0})");

         options.put("rolesCtxDN", "ou=Roles,dc=jboss,dc=org");
         options.put("roleFilter", "(member={1})");
         options.put("roleAttributeID", "cn");
         options.put("roleRecursion", "0");

         AppConfigurationEntry ace = new AppConfigurationEntry(name,
         AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
         AppConfigurationEntry[] entry = {ace};
         return entry;
      }
      AppConfigurationEntry[] testLdapExample21Encrypt()
      {
         String name = "org.jboss.security.auth.spi.LdapExtLoginModule";
         HashMap options = new HashMap();
         options.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
         options.put("java.naming.provider.url", "ldap://lamia/");
         options.put("java.naming.security.authentication", "simple");

         options.put("jaasSecurityDomain", "jboss.test:service=JaasSecurityDomain,domain=testLdapExample21Encrypt");
         options.put("bindDN", "cn=Root,dc=jboss,dc=org");
         // secret1 encrypted
         options.put("bindCredential", "7hInTB4HCBL");
         options.put("baseCtxDN", "ou=People,dc=jboss,dc=org");
         options.put("baseFilter", "(uid={0})");

         options.put("rolesCtxDN", "ou=Roles,dc=jboss,dc=org");
         options.put("roleFilter", "(member={1})");
         options.put("roleAttributeID", "cn");
         options.put("roleRecursion", "0");

         AppConfigurationEntry ace = new AppConfigurationEntry(name,
         AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
         AppConfigurationEntry[] entry = {ace};
         return entry;
      }
      /**
      testLdapExample23 {
         org.jboss.security.auth.spi.LdapExtLoginModule
            java.naming.factory.initial=com.sun.jndi.ldap.LdapCtxFactory
            java.naming.provider.url="ldap://lamia/"
            java.naming.security.authentication=simple
            bindDN="cn=Root,dc=jboss,dc=org"
            bindCredential=secret1
            baseCtxDN="ou=People,o=example3,dc=jboss,dc=org"
            baseFilter="(cn={0})"
            rolesCtxDN="ou=Roles,o=example3,dc=jboss,dc=org";
            roleFilter="(member={1})"
            roleAttributeID="cn"
            roleRecursion=0
      };
      */
      AppConfigurationEntry[] testLdapExample23()
      {
         String name = "org.jboss.security.auth.spi.LdapExtLoginModule";
         HashMap options = new HashMap();
         options.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
         options.put("java.naming.provider.url", "ldap://lamia/");
         options.put("java.naming.security.authentication", "simple");


         options.put("bindDN", "cn=Root,dc=jboss,dc=org");
         options.put("bindCredential", "secret1");
         options.put("baseCtxDN", "ou=People,o=example3,dc=jboss,dc=org");
         options.put("baseFilter", "(cn={0})");

         options.put("rolesCtxDN", "ou=Roles,o=example3,dc=jboss,dc=org");
         options.put("roleFilter", "(member={1})");
         options.put("roleAttributeID", "cn");
         options.put("roleRecursion", "0");

         AppConfigurationEntry ace = new AppConfigurationEntry(name,
         AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
         AppConfigurationEntry[] entry = {ace};
         return entry;
      }
      /**
      testLdapExample22 {
         org.jboss.security.auth.spi.LdapExtLoginModule
            java.naming.factory.initial=com.sun.jndi.ldap.LdapCtxFactory
            java.naming.provider.url="ldap://lamia/"
            java.naming.security.authentication=simple
            bindDN="cn=Root,dc=jboss,dc=org"
            bindCredential=secret1
            baseCtxDN="ou=People,o=example2,dc=jboss,dc=org"
            baseFilter="(uid={0})"
            rolesCtxDN="ou=Roles,o=example2,dc=jboss,dc=org";
            roleFilter="(uid={0})"
            roleAttributeID="memberOf"
            roleAttributeIsDN="true"
            roleNameAttributeID="cn"
            roleRecursion=0
      };
      */
      AppConfigurationEntry[] testLdapExample22()
      {
         String name = "org.jboss.security.auth.spi.LdapExtLoginModule";
         HashMap options = new HashMap();
         options.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
         options.put("java.naming.provider.url", "ldap://lamia/");
         options.put("java.naming.security.authentication", "simple");


         options.put("bindDN", "cn=Root,dc=jboss,dc=org");
         options.put("bindCredential", "secret1");
         options.put("baseCtxDN", "ou=People,o=example2,dc=jboss,dc=org");
         options.put("baseFilter", "(uid={0})");

         options.put("rolesCtxDN", "ou=Roles,o=example2,dc=jboss,dc=org");
         options.put("roleFilter", "(uid={0})");
         options.put("roleAttributeID", "memberOf");
         options.put("roleAttributeIsDN", "true");
         options.put("roleNameAttributeID", "cn");
         options.put("roleRecursion", "0");

         AppConfigurationEntry ace = new AppConfigurationEntry(name,
         AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
         AppConfigurationEntry[] entry = {ace};
         return entry;
      }
      /**
      testLdapExample24 {
         org.jboss.security.auth.spi.LdapExtLoginModule
            java.naming.factory.initial=com.sun.jndi.ldap.LdapCtxFactory
            java.naming.provider.url="ldap://lamia/"
            java.naming.security.authentication=simple
            bindDN="cn=Root,dc=jboss,dc=org"
            bindCredential=secret1
            baseCtxDN="ou=People,o=example4,dc=jboss,dc=org"
            baseFilter="(cn={0})"
            rolesCtxDN="ou=Roles,o=example4,dc=jboss,dc=org";
            roleFilter="(member={1})"
            roleAttributeID="memberOf"
            roleRecursion=1
      };
      */
      AppConfigurationEntry[] testLdapExample24()
      {
         String name = "org.jboss.security.auth.spi.LdapExtLoginModule";
         HashMap options = new HashMap();
         options.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
         options.put("java.naming.provider.url", "ldap://lamia/");
         options.put("java.naming.security.authentication", "simple");

         options.put("bindDN", "cn=Root,dc=jboss,dc=org");
         options.put("bindCredential", "secret1");
         options.put("baseCtxDN", "ou=People,o=example4,dc=jboss,dc=org");
         options.put("baseFilter", "(cn={0})");

         options.put("rolesCtxDN", "ou=Roles,o=example4,dc=jboss,dc=org");
         options.put("roleFilter", "(member={1})");
         options.put("roleAttributeID", "cn");
         options.put("roleRecursion", "1");

         AppConfigurationEntry ace = new AppConfigurationEntry(name,
         AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
         AppConfigurationEntry[] entry = {ace};
         return entry;
      }

      AppConfigurationEntry[] testJBAS3312()
      {
         String name = "org.jboss.security.auth.spi.LdapExtLoginModule";
         HashMap options = new HashMap();
         options.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
         options.put("java.naming.provider.url", "ldap://lamia/");
         options.put("java.naming.security.authentication", "simple");

         options.put("bindDN", "cn=Root,DC=uz,DC=kuleuven,DC=ac,DC=be");
         options.put("bindCredential", "root");
         options.put("baseCtxDN", "ou=People,dc=uz,dc=kuleuven,dc=ac,dc=be");
         options.put("baseFilter", "(sAMAccountName={0})");

         options.put("rolesCtxDN", "OU=Informatiesystemen,OU=Groups,DC=uz,DC=kuleuven,DC=ac,DC=be");
         options.put("roleFilter", "(member={1})");
         options.put("roleAttributeID", "memberOf");
         options.put("roleAttributeIsDN", "true");
         options.put("roleNameAttributeID", "cn");
         options.put("roleRecursion", "5");
         options.put("searchScope", "ONELEVEL_SCOPE");

         AppConfigurationEntry ace = new AppConfigurationEntry(name,
         AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
         AppConfigurationEntry[] entry = {ace};
         return entry;
      }

   }

   public LoginModulesTestCase(String testName)
   {
      super(testName);
   }

   public void testLdapExample1() throws Exception
   {
      System.out.println("testLdapExample1");
      UsernamePasswordHandler handler = new UsernamePasswordHandler("jduke", "theduke".toCharArray());
      LoginContext lc = new LoginContext("testLdapExample1", handler);
      lc.login();

      Subject subject = lc.getSubject();
      System.out.println("Subject: "+subject);

      Set groups = subject.getPrincipals(Group.class);
      assertTrue("Principals contains jduke", subject.getPrincipals().contains(new SimplePrincipal("jduke")));
      assertTrue("Principals contains Roles", groups.contains(new SimplePrincipal("Roles")));
      Group roles = (Group) groups.iterator().next();
      assertTrue("Echo is a role", roles.isMember(new SimplePrincipal("Echo")));
      assertTrue("TheDuke is a role", roles.isMember(new SimplePrincipal("TheDuke")));

      lc.logout();
   }
   public void testLdapExample11() throws Exception
   {
      System.out.println("testLdapExample11");
      UsernamePasswordHandler handler = new UsernamePasswordHandler("jduke", "theduke".toCharArray());
      LoginContext lc = new LoginContext("testLdapExample11", handler);
      lc.login();

      Subject subject = lc.getSubject();
      System.out.println("Subject: "+subject);

      Set groups = subject.getPrincipals(Group.class);
      assertTrue("Principals contains jduke", subject.getPrincipals().contains(new SimplePrincipal("jduke")));
      assertTrue("Principals contains Roles", groups.contains(new SimplePrincipal("Roles")));
      Group roles = (Group) groups.iterator().next();
      assertTrue("Echo is a role", roles.isMember(new SimplePrincipal("Echo")));
      assertTrue("TheDuke is a role", roles.isMember(new SimplePrincipal("TheDuke")));

      lc.logout();
   }
   public void testLdapExample11Encrypt() throws Exception
   {
      System.out.println("testLdapExample11Encrypt");
      MBeanServer server = MBeanServerFactory.createMBeanServer("jboss");
      JaasSecurityDomain secDomain = new JaasSecurityDomain("testLdapExample11Encrypt");
      secDomain.setSalt("abcdefgh");
      secDomain.setIterationCount(13);
      secDomain.setKeyStorePass("master");
      secDomain.setManagerServiceName(null);
      secDomain.start();
      ObjectName name = new ObjectName("jboss.test:service=JaasSecurityDomain,domain=testLdapExample11Encrypt");
      server.registerMBean(secDomain, name);

      // secret1 encrypts to 7hInTB4HCBL
      UsernamePasswordHandler handler = new UsernamePasswordHandler("jduke", "theduke".toCharArray());
      LoginContext lc = new LoginContext("testLdapExample11Encrypt", handler);
      lc.login();

      Subject subject = lc.getSubject();
      System.out.println("Subject: "+subject);

      Set groups = subject.getPrincipals(Group.class);
      assertTrue("Principals contains jduke", subject.getPrincipals().contains(new SimplePrincipal("jduke")));
      assertTrue("Principals contains Roles", groups.contains(new SimplePrincipal("Roles")));
      Group roles = (Group) groups.iterator().next();
      assertTrue("Echo is a role", roles.isMember(new SimplePrincipal("Echo")));
      assertTrue("TheDuke is a role", roles.isMember(new SimplePrincipal("TheDuke")));

      lc.logout();
      MBeanServerFactory.releaseMBeanServer(server);
   }
   /*
version: 1
dn: o=example2,dc=jboss,dc=org
objectClass: top
objectClass: dcObject
objectClass: organization
dc: jboss
o: JBoss

dn: ou=People,o=example2,dc=jboss,dc=org
objectClass: top
objectClass: organizationalUnit
ou: People

dn: uid=jduke,ou=People,o=example2,dc=jboss,dc=org
objectClass: top
objectClass: uidObject
objectClass: person
objectClass: inetOrgPerson
cn: Java Duke
employeeNumber: judke-123
sn: Duke
uid: jduke
userPassword:: dGhlZHVrZQ==

dn: uid=jduke2,ou=People,o=example2,dc=jboss,dc=org
objectClass: top
objectClass: uidObject
objectClass: person
objectClass: inetOrgPerson
cn: Java Duke2
employeeNumber: judke2-123
sn: Duke2
uid: jduke2
userPassword:: dGhlZHVrZTI=

dn: ou=Roles,o=example2,dc=jboss,dc=org
objectClass: top
objectClass: organizationalUnit
ou: Roles

dn: uid=jduke,ou=Roles,o=example2,dc=jboss,dc=org
objectClass: top
objectClass: groupUserEx
memberOf: cn=Echo,ou=Roles,o=example2,dc=jboss,dc=org
memberOf: cn=TheDuke,ou=Roles,o=example2,dc=jboss,dc=org
uid: jduke

dn: uid=jduke2,ou=Roles,o=example2,dc=jboss,dc=org
objectClass: top
objectClass: groupUserEx
memberOf: cn=Echo2,ou=Roles,o=example2,dc=jboss,dc=org
memberOf: cn=TheDuke2,ou=Roles,o=example2,dc=jboss,dc=org
uid: jduke2

dn: cn=Echo,ou=Roles,o=example2,dc=jboss,dc=org
objectClass: top
objectClass: groupOfNames
cn: Echo
description: the echo role
member: uid=jduke,ou=People,dc=jboss,dc=org

dn: cn=TheDuke,ou=Roles,o=example2,dc=jboss,dc=org
objectClass: groupOfNames
objectClass: top
cn: TheDuke
description: the duke role
member: uid=jduke,ou=People,o=example2,dc=jboss,dc=org

dn: cn=Echo2,ou=Roles,o=example2,dc=jboss,dc=org
objectClass: top
objectClass: groupOfNames
cn: Echo2
description: the Echo2 role
member: uid=jduke2,ou=People,dc=jboss,dc=org

dn: cn=TheDuke2,ou=Roles,o=example2,dc=jboss,dc=org
objectClass: groupOfNames
objectClass: top
cn: TheDuke2
description: the duke2 role
member: uid=jduke2,ou=People,o=example2,dc=jboss,dc=org

dn: cn=JBossAdmin,ou=Roles,o=example2,dc=jboss,dc=org
objectClass: top
objectClass: groupOfNames
cn: JBossAdmin
description: the JBossAdmin group
member: uid=jduke,ou=People,dc=jboss,dc=org   
   */
   public void testLdapExample2() throws Exception
   {
      System.out.println("testLdapExample2");
      UsernamePasswordHandler handler = new UsernamePasswordHandler("jduke", "theduke".toCharArray());
      LoginContext lc = new LoginContext("testLdapExample2", handler);
      lc.login();

      Subject subject = lc.getSubject();
      System.out.println("Subject: "+subject);

      Set groups = subject.getPrincipals(Group.class);
      assertTrue("Principals contains jduke", subject.getPrincipals().contains(new SimplePrincipal("jduke")));
      assertTrue("Principals contains Roles", groups.contains(new SimplePrincipal("Roles")));
      Group roles = (Group) groups.iterator().next();
      assertTrue("Echo is a role", roles.isMember(new SimplePrincipal("Echo")));
      assertTrue("TheDuke is a role", roles.isMember(new SimplePrincipal("TheDuke")));
      assertFalse("Echo2 is NOT a role", roles.isMember(new SimplePrincipal("Echo2")));
      assertFalse("TheDuke2 is NOT a role", roles.isMember(new SimplePrincipal("TheDuke2")));

      lc.logout();
   }
   public void testLdapExample21() throws Exception
   {
      System.out.println("testLdapExample21");
      UsernamePasswordHandler handler = new UsernamePasswordHandler("jduke",
         "theduke".toCharArray());
      LoginContext lc = new LoginContext("testLdapExample21", handler);
      lc.login();

      Subject subject = lc.getSubject();
      System.out.println("Subject: "+subject);

      Set groups = subject.getPrincipals(Group.class);
      Set principals = subject.getPrincipals();
      assertTrue("Principals contains jduke", principals.contains(new SimplePrincipal("jduke")));
      assertTrue("Principals contains Roles", groups.contains(new SimplePrincipal("Roles")));
      Group roles = (Group) groups.iterator().next();
      assertTrue("Echo is a role", roles.isMember(new SimplePrincipal("Echo")));
      assertTrue("TheDuke is a role", roles.isMember(new SimplePrincipal("TheDuke")));

      lc.logout();
   }
   public void testLdapExample21Encrypt() throws Exception
   {
      System.out.println("testLdapExample21Encrypt");
      MBeanServer server = MBeanServerFactory.createMBeanServer("jboss");
      JaasSecurityDomain secDomain = new JaasSecurityDomain("testLdapExample21Encrypt");
      secDomain.setSalt("abcdefgh");
      secDomain.setIterationCount(13);
      secDomain.setKeyStorePass("master");
      secDomain.setManagerServiceName(null);
      secDomain.start();
      ObjectName name = new ObjectName("jboss.test:service=JaasSecurityDomain,domain=testLdapExample21Encrypt");
      server.registerMBean(secDomain, name);

      UsernamePasswordHandler handler = new UsernamePasswordHandler("jduke",
         "theduke".toCharArray());
      LoginContext lc = new LoginContext("testLdapExample21Encrypt", handler);
      lc.login();

      Subject subject = lc.getSubject();
      System.out.println("Subject: "+subject);

      Set groups = subject.getPrincipals(Group.class);
      Set principals = subject.getPrincipals();
      assertTrue("Principals contains jduke", principals.contains(new SimplePrincipal("jduke")));
      assertTrue("Principals contains Roles", groups.contains(new SimplePrincipal("Roles")));
      Group roles = (Group) groups.iterator().next();
      assertTrue("Echo is a role", roles.isMember(new SimplePrincipal("Echo")));
      assertTrue("TheDuke is a role", roles.isMember(new SimplePrincipal("TheDuke")));

      lc.logout();
      MBeanServerFactory.releaseMBeanServer(server);
   }
   public void testLdapExample23() throws Exception
   {
      System.out.println("testLdapExample23");
      UsernamePasswordHandler handler = new UsernamePasswordHandler("Java Duke",
         "theduke".toCharArray());
      LoginContext lc = new LoginContext("testLdapExample23", handler);
      lc.login();

      Subject subject = lc.getSubject();
      System.out.println("Subject: "+subject);

      Set groups = subject.getPrincipals(Group.class);
      Set principals = subject.getPrincipals();
      assertTrue("Principals contains Java Duke", principals.contains(new SimplePrincipal("Java Duke")));
      assertTrue("Principals contains Roles", groups.contains(new SimplePrincipal("Roles")));
      Group roles = (Group) groups.iterator().next();
      assertTrue("Echo is a role", roles.isMember(new SimplePrincipal("Echo")));
      assertTrue("TheDuke is a role", roles.isMember(new SimplePrincipal("TheDuke")));

      lc.logout();
   }
   public void testLdapExample22() throws Exception
   {
      System.out.println("testLdapExample22");
      UsernamePasswordHandler handler = new UsernamePasswordHandler("jduke",
         "theduke".toCharArray());
      LoginContext lc = new LoginContext("testLdapExample22", handler);
      lc.login();

      Subject subject = lc.getSubject();
      System.out.println("Subject: "+subject);

      Set groups = subject.getPrincipals(Group.class);
      Set principals = subject.getPrincipals();
      assertTrue("Principals contains jduke", principals.contains(new SimplePrincipal("jduke")));
      assertTrue("Principals contains Roles", groups.contains(new SimplePrincipal("Roles")));
      Group roles = (Group) groups.iterator().next();
      assertTrue("Echo is a role", roles.isMember(new SimplePrincipal("Echo")));
      assertTrue("TheDuke is a role", roles.isMember(new SimplePrincipal("TheDuke")));

      lc.logout();
   }
   public void testLdapExample24() throws Exception
   {
      System.out.println("testLdapExample24");
      UsernamePasswordHandler handler = new UsernamePasswordHandler("Java Duke",
         "theduke".toCharArray());
      LoginContext lc = new LoginContext("testLdapExample24", handler);
      lc.login();

      Subject subject = lc.getSubject();
      System.out.println("Subject: "+subject);

      Set groups = subject.getPrincipals(Group.class);
      Set principals = subject.getPrincipals();
      assertTrue("Principals contains Java Duke", principals.contains(new SimplePrincipal("Java Duke")));
      assertTrue("Principals contains Roles", groups.contains(new SimplePrincipal("Roles")));
      Group roles = (Group) groups.iterator().next();
      assertTrue("RG2 is a role", roles.isMember(new SimplePrincipal("RG2")));
      assertTrue("R1 is a role", roles.isMember(new SimplePrincipal("R1")));
      assertTrue("R2 is a role", roles.isMember(new SimplePrincipal("R2")));
      assertTrue("R3 is a role", roles.isMember(new SimplePrincipal("R3")));
      assertFalse("R4 is NOT a role", roles.isMember(new SimplePrincipal("R4")));
      assertTrue("R5 is a role", roles.isMember(new SimplePrincipal("R5")));

      lc.logout();
   }

   /* JBAS-3312 testcase
dn: DC=uz,DC=kuleuven,DC=ac,DC=be
objectClass: top

dn: ou=People,dc=uz,dc=kuleuven,dc=ac,dc=be
objectClass: organizationalUnit
ou: People

dn: CN=jduke,ou=People,dc=uz,dc=kuleuven,dc=ac,dc=be
memberOf: ou=People,dc=uz,dc=kuleuven,dc=ac,dc=be
objectClass: top
objectClass: person
objectClass: organizationalPerson
objectClass: user
cn: JDuke
name: Java Duke
sn: TheDuke
sAMAccountName: jduke
userPrincipalName: jduke@jboss.org
userPassword: theduke

dn: OU=Groups,DC=uz,DC=kuleuven,DC=ac,DC=be
objectClass: top
objectClass: organizationalUnit
objectClass: orgUnitEx
ou: Groups
objectCategory: CN=Organizational-Unit,CN=Schema,CN=Configuration,DC=uz,DC=kuleuven,DC=ac,DC=be


dn: OU=Informatiesystemen,OU=Groups,DC=uz,DC=kuleuven,DC=ac,DC=be
objectClass: top
objectClass: organizationalUnit
objectClass: orgUnitEx
ou: Informatiesystemen
objectCategory: CN=Organizational-Unit,CN=Schema,CN=Configuration,DC=uz,DC=kuleuven,DC=ac,DC=be


dn: CN=inf_map_informatiesystemen_lijst,OU=Informatiesystemen,OU=Groups,DC=uz,DC=kuleuven,DC=ac,DC=be
objectClass: top
objectClass: group
cn: inf_map_informatiesystemen_lijst
member: CN=inf_map_vmware_Lijst,OU=Informatiesystemen,OU=Groups,DC=uz,DC=kuleuven,DC=ac,DC=be
member: CN=inf_map_carenet_Lijst,OU=Informatiesystemen,OU=Groups,DC=uz,DC=kuleuven,DC=ac,DC=be
sAMAccountName: inf_map_informatiesystemen_lijst
objectCategory: CN=Group,CN=Schema,CN=Configuration,DC=uz,DC=kuleuven,DC=ac,DC=be


dn: CN=inf_map_vmware_Lijst,OU=Informatiesystemen,OU=Groups,DC=uz,DC=kuleuven,DC=ac,DC=be
objectClass: top
objectClass: group
cn: inf_map_vmware_Lijst
description: \\uz\data\Admin\VMWare Lijst
member: CN=inf_map_vmware_iso_S,OU=Informatiesystemen,OU=Groups,DC=uz,DC=kuleuven,DC=ac,DC=be
member: CN=inf_map_vmware_iso_L,OU=Informatiesystemen,OU=Groups,DC=uz,DC=kuleuven,DC=ac,DC=be
memberOf: CN=inf_map_informatiesystemen_lijst,OU=Informatiesystemen,OU=Groups,DC=uz,DC=kuleuven,DC=ac,DC=be
sAMAccountName: inf_map_vmware_Lijst
objectCategory: CN=Group,CN=Schema,CN=Configuration,DC=uz,DC=kuleuven,DC=ac,DC=be


dn: CN=inf_map_vmware_iso_S,OU=Informatiesystemen,OU=Groups,DC=uz,DC=kuleuven,DC=ac,DC=be
objectClass: top
objectClass: group
cn: inf_map_vmware_iso_S
description: \\uz\data\Admin\VMWare\ISO Schrijven
member: CN=markv,OU=People,DC=uz,DC=kuleuven,DC=ac,DC=be
member: CN=jduke,OU=People,DC=uz,DC=kuleuven,DC=ac,DC=be
memberOf: CN=inf_map_informatiesystemen_lijst,OU=Informatiesystemen,OU=Groups,DC=uz,DC=kuleuven,DC=ac,DC=be
memberOf: CN=inf_map_vmware_Lijst,OU=Informatiesystemen,OU=Groups,DC=uz,DC=kuleuven,DC=ac,DC=be
sAMAccountName: inf_map_vmware_iso_S
objectCategory: CN=Group,CN=Schema,CN=Configuration,DC=uz,DC=kuleuven,DC=ac,DC=be
    */
   public void testJBAS3312() throws Exception
   {
      System.out.println("testJBAS3312");
      UsernamePasswordHandler handler = new UsernamePasswordHandler("jduke",
         "theduke".toCharArray());
      LoginContext lc = new LoginContext("testJBAS3312", handler);
      lc.login();

      Subject subject = lc.getSubject();
      System.out.println("Subject: "+subject);

      Set groups = subject.getPrincipals(Group.class);
      Set principals = subject.getPrincipals();
      assertTrue("Principals contains Java Duke", principals.contains(new SimplePrincipal("jduke")));
      assertTrue("Principals contains Roles", groups.contains(new SimplePrincipal("Roles")));
      Group roles = (Group) groups.iterator().next();
      Enumeration names = roles.members();
      while( names.hasMoreElements() )
      {
         System.out.println(names.nextElement());
      }
      assertTrue("inf_map_vmware_iso_S is a role", roles.isMember(new SimplePrincipal("inf_map_vmware_iso_S")));
      assertTrue("inf_map_informatiesystemen_lijst is a role", roles.isMember(new SimplePrincipal("inf_map_informatiesystemen_lijst")));
      assertTrue("inf_map_vmware_Lijst is a role", roles.isMember(new SimplePrincipal("inf_map_vmware_Lijst")));

      lc.logout();
   }

   public static void main(java.lang.String[] args)
   {
      System.setErr(System.out);
      TestSuite suite = new TestSuite(LoginModulesTestCase.class);
      junit.textui.TestRunner.run(suite);
   }

}
