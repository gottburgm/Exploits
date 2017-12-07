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
import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;

/** A JUnit TestCase for the JAAS LoginContext usage.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class LoginContextUnitTestCase
   extends junit.framework.TestCase
{
   Subject subject1;
   Subject subject2;

   static class MyConfig extends Configuration
   {
      AppConfigurationEntry[] entry;
      MyConfig()
      {
         entry = new AppConfigurationEntry[1];
         HashMap opt0 = new HashMap();
         opt0.put("principal", "starksm");
         entry[0] = new AppConfigurationEntry("org.jboss.security.auth.spi.IdentityLoginModule", AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, opt0);
         //entry[1] = new AppConfigurationEntry("org.jboss.security.plugins.samples.RolesLoginModule", AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, new HashMap());
      }

      public AppConfigurationEntry[] getAppConfigurationEntry(String appName)
      {
         return entry;
      }
      public void refresh()
      {
      }
   }

   public LoginContextUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      Configuration.setConfiguration(new MyConfig());
   }

   public void testLogin1() throws Exception
   {
      subject1 = new Subject();
      LoginContext lc = new LoginContext("LoginContext", subject1);
      lc.login();
      Subject lcSubject = lc.getSubject();
      assertTrue("subject == lcSubject",  subject1 == lcSubject );
   }
   public void testLogin2() throws Exception
   {
      subject2 = new Subject();
      LoginContext lc = new LoginContext("LoginContext", subject2);
      lc.login();
      Subject lcSubject = lc.getSubject();
      assertTrue("subject == lcSubject",  subject2 == lcSubject );
   }
}
