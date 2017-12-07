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

import java.util.HashMap;
import java.security.acl.Group;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.LoginException;

import org.jboss.security.plugins.JaasSecurityManager;
import org.jboss.security.auth.callback.SecurityAssociationHandler;
import org.jboss.security.auth.spi.UsernamePasswordLoginModule;
import org.jboss.security.SimplePrincipal;
import org.jboss.util.TimedCachePolicy;
import org.jboss.logging.Logger;
import junit.framework.TestCase;

/** Tests of the JaasSecurityManager implementation.

 @author Scott.Stark@jboss.org
 @version $Revision: 113241 $
 */
public class JaasSecurityManagerUnitTestCase
   extends TestCase
{
   static Logger log = Logger.getLogger(JaasSecurityManagerUnitTestCase.class);

   public JaasSecurityManagerUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Setup the JAAS configuration
    * @throws Exception
    */
   protected void setUp() throws Exception
   {
      super.setUp();
      Configuration.setConfiguration(new MyConfig());
   }

   /**
    * Validate that using of String/char[] representing the same
    * credential do not cause thrashing of the domain cache.
    */
   public void testStringCharArrayCredential()
   {
      SimplePrincipal jduke = new SimplePrincipal("jduke");
      CallbackHandler handler = new SecurityAssociationHandler(jduke, "theduke".toCharArray());
      JaasSecurityManager sm = new JaasSecurityManager("testStringCharArrayCredential", handler);
      TimedCachePolicy cache = new TimedCachePolicy(600, true, 10);
      cache.create();
      cache.start();
      sm.setCachePolicy(cache);
      // Reset the validation count
      CountedLoginModule.validateCount = 0;
      // Initial validation to populate the cache
      assertTrue(sm.isValid(jduke, "theduke"));
      // Validate that the String credential form uses the cache
      assertTrue(sm.isValid(jduke, "theduke"));
      // Validate that the char[] credential form uses the cache
      assertTrue(sm.isValid(jduke, "theduke".toCharArray()));
   }
   /**
    * Validate that using of char[]/String representing the same
    * credential do not cause thrashing of the domain cache.
    */
   public void testCharArrayStringCredential()
   {
      SimplePrincipal jduke = new SimplePrincipal("jduke");
      CallbackHandler handler = new SecurityAssociationHandler(jduke, "theduke".toCharArray());
      JaasSecurityManager sm = new JaasSecurityManager("testStringCharArrayCredential", handler);
      TimedCachePolicy cache = new TimedCachePolicy(600, true, 10);
      cache.create();
      cache.start();
      sm.setCachePolicy(cache);

      // Reset the validation count
      CountedLoginModule.validateCount = 0;
      // Initial validation to populate the cache
      assertTrue(sm.isValid(jduke, "theduke".toCharArray()));
      // Validate that the char[] credential form uses the cache
      assertTrue(sm.isValid(jduke, "theduke".toCharArray()));
      // Validate that the String credential form uses the cache
      assertTrue(sm.isValid(jduke, "theduke"));
   }

   /**
    * Implementation of JAAS configuration for this testcase
    */
   static class MyConfig extends Configuration
   {
      AppConfigurationEntry[] entry;
      MyConfig()
      {
         entry = new AppConfigurationEntry[1];
         HashMap opts = new HashMap();
         entry[0] = new AppConfigurationEntry(CountedLoginModule.class.getName(),
            AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, opts);
      }

      public AppConfigurationEntry[] getAppConfigurationEntry(String appName)
      {
         return entry;
      }
      public void refresh()
      {
      }
   }

   /**
    * UsernamePasswordLoginModule extenstion that only allows a single
    * validation attempt.
    */
   public static class CountedLoginModule extends UsernamePasswordLoginModule
   {
      static int validateCount = 0;

      protected boolean validatePassword(String inputPassword, String expectedPassword)
      {
         validateCount ++;
         log.info("validatePassword, validateCount="+validateCount);
         if( validateCount > 1 )
         {
            IllegalStateException ex = new IllegalStateException("Too many validation calls: "+validateCount);
            super.setValidateError(ex);
            return false;
         }
         return super.validatePassword(inputPassword, expectedPassword);
      }

      protected String getUsersPassword() throws LoginException
      {
         return "theduke";
      }

      protected Group[] getRoleSets() throws LoginException
      {
         return new Group[0];
      }
   }
}
