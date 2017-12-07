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
package org.jboss.test;

import java.util.Iterator;
import java.util.Set;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.Subject;

import junit.framework.TestCase;

import org.jboss.security.auth.login.XMLLoginConfigImpl;
import org.jboss.security.SimplePrincipal;

public class LoginContextTestCase extends TestCase
{

   public LoginContextTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      System.setOut(System.err);
      XMLLoginConfigImpl config = XMLLoginConfigImpl.getInstance();
      config.setConfigResource("login-config.xml");
      config.loadConfig();
      Configuration.setConfiguration(config);
   }

   private void validateSuccessfulLogin(LoginContext lc) throws LoginException
   {
      Subject subject = lc.getSubject();
      assertTrue("case5 subject != null", subject != null);
      boolean hasGuest = subject.getPrincipals().contains(new SimplePrincipal("guest"));
      assertTrue("subject has guest principal", hasGuest);
      lc.logout();
      hasGuest = subject.getPrincipals().contains(new SimplePrincipal("guest"));
      assertTrue("subject has guest principal", hasGuest == false);
      Set publicCreds = subject.getPublicCredentials();
      assertTrue("public creds has 'A public credential'",
         publicCreds.contains("A public credential"));
      Set privateCreds = subject.getPrivateCredentials();
      assertTrue("private creds has 'A private credential'",
         privateCreds.contains("A private credential"));
      Iterator iter = privateCreds.iterator();
      int count = 0;
      while( iter.hasNext() )
      {
         iter.next();
         count ++;
      }
      assertTrue("private creds has 1 entry", count == 1);
   }

   public void testCase1() throws Exception
   {
      LoginContext lc = new LoginContext("case1");
      lc.login();
      validateSuccessfulLogin(lc);
   }

   public void testCase2() throws Exception
   {
      LoginContext lc = new LoginContext("case2");
      lc.login();
      validateSuccessfulLogin(lc);
   }

   public void testCase3() throws Exception
   {
      LoginContext lc = new LoginContext("case3");
      try
      {
         lc.login();
         fail("LoginContext.login3 did not thrown an exception");
      }
      catch(LoginException e)
      {
         e.printStackTrace();
      }
   }

   /** This should fail because no login module succeeds
    *
    * @throws Exception
    */
   public void testCase4() throws Exception
   {
      LoginContext lc = new LoginContext("case4");
      try
      {
         lc.login();
         fail("LoginContext.login4 did not thrown an exception");
      }
      catch(LoginException e)
      {
         e.printStackTrace();
      }
   }

   public void testCase5() throws Exception
   {
      LoginContext lc = new LoginContext("case5");
      lc.login();
      validateSuccessfulLogin(lc);
   }
   public void testCase6() throws Exception
   {
      LoginContext lc = new LoginContext("case6");
      lc.login();
      validateSuccessfulLogin(lc);
   }
   public void testCase7() throws Exception
   {
      LoginContext lc = new LoginContext("case7");
      lc.login();
      validateSuccessfulLogin(lc);
   }

   public void testCase8() throws Exception
   {
      LoginContext lc = new LoginContext("case8");
      try
      {
         lc.login();
         fail("LoginContext.login8 did not thrown an exception");
      }
      catch(LoginException e)
      {
         e.printStackTrace();
      }
   }

   public void testCase9() throws Exception
   {
      LoginContext lc = new LoginContext("case9");
      lc.login();
      validateSuccessfulLogin(lc);
   }

   public void testCase10() throws Exception
   {
      LoginContext lc = new LoginContext("case10");
      try
      {
         lc.login();
         fail("LoginContext.login10 did not thrown an exception");
      }
      catch(LoginException e)
      {
         e.printStackTrace();
      }
   }

   public void testCase11() throws Exception
   {
      LoginContext lc = new LoginContext("case11");
      lc.login();
      validateSuccessfulLogin(lc);
   }
   public void testCase12() throws Exception
   {
      LoginContext lc = new LoginContext("case12");
      lc.login();
      validateSuccessfulLogin(lc);
   }

   public void testCase13() throws Exception
   {
      LoginContext lc = new LoginContext("case13");
      try
      {
         lc.login();
         fail("LoginContext.login13 did not thrown an exception");
      }
      catch(LoginException e)
      {
         e.printStackTrace();
      }
   }

   public void testCase14() throws Exception
   {
      LoginContext lc = new LoginContext("case14");
      try
      {
         lc.login();
         fail("LoginContext.login14 did not thrown an exception");
      }
      catch(LoginException e)
      {
         e.printStackTrace();
      }
   }

   public void testCase15() throws Exception
   {
      LoginContext lc = new LoginContext("case15");
      try
      {
         lc.login();
         fail("LoginContext.login15 did not thrown an exception");
      }
      catch(LoginException e)
      {
         e.printStackTrace();
      }
   }

   public void testCase16() throws Exception
   {
      LoginContext lc = new LoginContext("case16");
      lc.login();
      validateSuccessfulLogin(lc);
   }

   public void testCase17() throws Exception
   {
      LoginContext lc = new LoginContext("case17");
      lc.login();
      validateSuccessfulLogin(lc);
   }

   public void testCase18() throws Exception
   {
      LoginContext lc = new LoginContext("case18");
      try
      {
         lc.login();
         fail("LoginContext.login18 did not thrown an exception");
      }
      catch(LoginException e)
      {
         e.printStackTrace();
      }
   }

   public void testCase19() throws Exception
   {
      LoginContext lc = new LoginContext("case19");
      try
      {
         lc.login();
         fail("LoginContext.login19 did not thrown an exception");
      }
      catch(LoginException e)
      {
         e.printStackTrace();
      }
   }

   public void testCase20() throws Exception
   {
      LoginContext lc = new LoginContext("case20");
      try
      {
         lc.login();
         fail("LoginContext.login20 did not thrown an exception");
      }
      catch(LoginException e)
      {
         e.printStackTrace();
      }
   }

   public void testCase21() throws Exception
   {
      LoginContext lc = new LoginContext("case21");
      try
      {
         lc.login();
      }
      catch(LoginException e)
      {
         e.printStackTrace();
      }
      Subject subject = lc.getSubject();
      assertTrue("case21 subject == null", subject == null);
   }

   public void testCase22() throws Exception
   {
      LoginContext lc = new LoginContext("case22");
      try
      {
         lc.login();
         fail("LoginContext.login22 did not thrown an exception");
      }
      catch(LoginException e)
      {
         e.printStackTrace();
      }
   }

}
