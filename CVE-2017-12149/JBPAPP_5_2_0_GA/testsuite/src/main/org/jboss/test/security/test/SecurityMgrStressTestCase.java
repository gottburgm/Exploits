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
import java.util.HashSet;
import java.util.Properties;

import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.jacc.PolicyContext;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.jboss.logging.Logger;
import org.jboss.security.AuthorizationManager;
import org.jboss.security.SecurityConstants;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SecurityContextFactory;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.callback.SecurityAssociationHandler;
import org.jboss.security.config.ApplicationPolicy;
import org.jboss.security.config.SecurityConfiguration;
import org.jboss.security.jacc.SubjectPolicyContextHandler;
import org.jboss.security.plugins.JBossAuthorizationManager;
import org.jboss.security.plugins.JaasSecurityManager;
import org.jboss.util.TimedCachePolicy;

/** Stress testing of the JaasSecurityManager
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class SecurityMgrStressTestCase extends TestCase
{
   static final int Nusers = 10;
   static final Logger log = Logger.getLogger(SecurityMgrStressTestCase.class);
   
   /**
    * Constructor for the SimpleUnitTestCase object
    *
    * @param name  Test name
    */
   public SecurityMgrStressTestCase(String name)
   {
      super(name);
   }

   /** Test concurrent access to the isValid and doesUserHaveRole security
    * mgr methods.
    *
    * @exception Exception thrown on any failure
    */
   public void testMTAuthentication() throws Exception
   {
      //SecurityAssociation.setServer();
      int count = Integer.getInteger("jbosstest.threadcount", 10).intValue();
      int iterations = Integer.getInteger("jbosstest.iterationcount", 5000).intValue();
      log.info("Creating "+count+" threads doing "+iterations+" iterations");
      PolicyContext.registerHandler(SecurityConstants.SUBJECT_CONTEXT_KEY, 
    		  new SubjectPolicyContextHandler(), false);
      AuthorizationManager am = new JBossAuthorizationManager("testIdentity");
      JaasSecurityManager secMgr = new JaasSecurityManager("testIdentity", new SecurityAssociationHandler());
      TimedCachePolicy cache = new TimedCachePolicy(3, false, 100);
      cache.create();
      cache.start();
      secMgr.setCachePolicy(cache);
      Thread[] testThreads = new Thread[count];
      AuthTester[] testers = new AuthTester[count];
      for(int t = 0; t < count; t ++)
      {
         int userID = t % Nusers;
         AuthTester test = new AuthTester(secMgr, am, iterations, userID);
         if( t == count - 2 )
            test.failAuthentication();
         if( t == count - 1 )
            test.failAuthorization();
         testers[t] = test;
         Thread thr = new Thread(test, "Tester#"+t);
         thr.start();
         testThreads[t] = thr;
      }

      for(int t = 0; t < count; t ++)
      {
         Thread thr = testThreads[t];
         thr.join();
         AuthTester test = testers[t];
         if( test.failAuthentication == true || test.failAuthorization == true )
            assertTrue("Failure test has an error", test.error != null);
         else if( test.error != null )
            fail("Unexpected error seen by : "+test.error.getLocalizedMessage());
      }
   }

   protected void setUp()
   {
      // Install the custom JAAS configuration
      Configuration.setConfiguration(new TestConfig());
   }

   /** Used to run the testcase from the command line
    *
    * @param args  The command line arguments
    */
   public static void main(String[] args)
   {
      TestRunner.run(SecurityMgrStressTestCase.class);
   }

   /** Hard coded login configurations for the test cases. The configuration
    name corresponds to the unit test function that uses the configuration.
    */
   private static class TestConfig extends Configuration
   {
      private AppConfigurationEntry[] theEntry;

      TestConfig()
      {
         String name = "org.jboss.security.auth.spi.MemoryUsersRolesLoginModule";
         Properties users = new Properties();
         Properties roles = new Properties();
         for(int i = 0; i < Nusers; i ++)
         {
            String username = "jduke"+i;
            users.setProperty(username, "theduke"+i);
            StringBuffer roleNames = new StringBuffer();
            for(int j = 0; j < 3; j ++)
            {
               if( j > 0 )
                  roleNames.append(',');
               roleNames.append(username+"-Role"+j);
            }
            roles.setProperty(username, roleNames.toString());
         }

         HashMap options = new HashMap();
         options.put("users", users);
         options.put("roles", roles);
         AppConfigurationEntry ace = new AppConfigurationEntry(name,
            AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
         theEntry = new AppConfigurationEntry[]{ace};
      }
      public AppConfigurationEntry[] getAppConfigurationEntry(String name)
      {
         return theEntry;
      }
      public void refresh()
      {
      }
   }

   private static class AuthTester implements Runnable
   {
      JaasSecurityManager secMgr;
      int iterations;
      String username;
      String password;
      Throwable error;
      boolean failAuthentication;
      boolean failAuthorization;
	  AuthorizationManager am;

      AuthTester(JaasSecurityManager secMgr, AuthorizationManager am, int iterations, int id)
      {
         this.iterations = iterations;
         this.secMgr = secMgr;
         this.am = am;
         this.username = "jduke"+id;
         this.password = "theduke"+id;
      }

      void failAuthentication()
      {
         failAuthentication = true;
      }
      void failAuthorization()
      {
         failAuthorization = true;
      }

      public void run()
      {
         log.info("Begin run, t="+Thread.currentThread());
         String thePassword = password;
         if( failAuthentication == true )
             thePassword += "-fail";
         SimplePrincipal user = new SimplePrincipal(username);
         HashSet roleSet = new HashSet();
         for(int j = 0; j < 3; j ++)
         {
            String role = username+"-Role"+j;
            if( failAuthorization == true )
               role += "-fail";
            roleSet.add(new SimplePrincipal(role));
         }

         try
         {
            for(int i = 0; i < iterations; i ++)
            {
               String securityDomain = secMgr.getSecurityDomain();
               Subject subject = new Subject();
               boolean authenticated = secMgr.isValid(user, thePassword, subject);
               if( authenticated == false )
                  throw new SecurityException("Failed to authenticate: "+user);
               SecurityConfiguration.addApplicationPolicy(new ApplicationPolicy(securityDomain));
               SecurityContext sc = SecurityContextFactory.createSecurityContext(user, 
            		   thePassword, subject, securityDomain);
               SecurityContextAssociation.setSecurityContext(sc);
               //SecurityAssociation.pushSubjectContext(subject, user, "any");
               boolean authorized = am.doesUserHaveRole(user, roleSet); 
               SecurityContextAssociation.clearSecurityContext();
               if( authorized == false )
               {
                  Subject s = secMgr.getActiveSubject();
                  throw new SecurityException("Failed to authorize, subject="+s);
               }
            }
         }
         catch(Throwable t)
         {
            error = t;
            if( failAuthentication == false && failAuthorization == false )
            log.error("Security failure", t);
         }
         log.info("End run, t="+Thread.currentThread());
      }
   }

}