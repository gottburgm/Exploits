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
package org.jboss.test.jca.test;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.security.auth.login.LoginContext;
import javax.sql.DataSource;

import junit.framework.Test;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.AppCallbackHandler;
import org.jboss.test.jca.securedejb.CallerIdentity;
import org.jboss.test.jca.securedejb.CallerIdentityHome;

/**
 * Tests of how security context interact with the JCA layer.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class SecurityContextUnitTestCase extends JBossTestCase
{

   public SecurityContextUnitTestCase(String name)
   {
      super(name);
   }
   
   public static Test suite() throws Exception
   {
      // Clear any default login behavior
      System.setProperty("jbosstest.secure", "false");
      Test t1 = getDeploySetup(SecurityContextUnitTestCase.class, "jca-securedejb.jar");
      return t1;
   }

   public void testCallerIdentityPropagation() throws Throwable
   {
      log.info("+++ testCallerIdentityPropagation");
      InitialContext ctx = super.getInitialContext();
      try
      {
         log.info("Lookup CallerIdentityDS");
         DataSource ds = (DataSource) ctx.lookup("CallerIdentityDS");
         Connection conn = ds.getConnection("sa", "");
         Statement stmt = conn.createStatement();
         stmt.execute("CREATE USER ejbcaller PASSWORD ejbcallerpw");
         stmt.close();
         conn.close();
      }
      catch(SQLException ignore)
      {
         log.debug("ejbcaller user setup failed", ignore);
      }

      LoginContext lc = login("ejbcaller", "ejbcallerpw".toCharArray());
      CallerIdentityHome home = (CallerIdentityHome) ctx.lookup("jca-test/CallerIdentity");
      CallerIdentity bean = home.create();
      bean.useCallerForAuth();
      lc.logout();
   }

   public void testConfiguredIdentityPropagation() throws Throwable
   {
      InitialContext ctx = super.getInitialContext();

      LoginContext lc = login("ejbcaller", "ejbcallerpw".toCharArray());
      CallerIdentityHome home = (CallerIdentityHome) ctx.lookup("jca-test/CallerIdentity");
      CallerIdentity bean = home.create();
      bean.useConfiguredForAuth();
      lc.logout();
   }

   public void testRunAsIdentityPropagationFS() throws Throwable
   { 
      InitialContext ctx = super.getInitialContext();
      LoginContext lc = login("ejbcaller", "ejbcallerpw".toCharArray());
      CallerIdentityHome home = (CallerIdentityHome) ctx.lookup("jca-test/RunAsIdentityFS");
      CallerIdentity bean = home.create();
      bean.useRunAsForAuthFS();
      lc.logout();
   }

   public void testRunAsIdentityPropagationDS() throws Throwable
   {
      InitialContext ctx = super.getInitialContext();
      LoginContext lc = login("ejbcaller", "ejbcallerpw".toCharArray());
      CallerIdentityHome home = (CallerIdentityHome) ctx.lookup("jca-test/RunAsIdentityDS");
      CallerIdentity bean = home.create();
      bean.useRunAsForAuthDS();
      lc.logout();
   }

   private LoginContext login(String username, char[] password) throws Exception
   {
      String confName = System.getProperty("conf.name", "other");
      AppCallbackHandler handler = new AppCallbackHandler(username, password);
      log.debug("Creating LoginContext("+confName+")");
      LoginContext lc = new LoginContext(confName, handler);
      lc.login();
      log.debug("Created LoginContext, subject="+lc.getSubject());
      return lc;
   }
}
