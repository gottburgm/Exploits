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
package org.jboss.test.security.test.authorization;

import java.rmi.RemoteException;

import javax.rmi.PortableRemoteObject;
import javax.security.auth.login.LoginContext;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.security.interfaces.StatelessSession;
import org.jboss.test.security.interfaces.StatelessSessionHome;
import org.jboss.test.util.AppCallbackHandler;

// $Id: XACMLEJBIntegrationUnitTestCase.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/**
 * Unit tests for the XACML Integration of the EJB Layer
 * 
 * @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 * @since Jul 6, 2006
 * @version $Revision: 81036 $
 */
public class XACMLEJBIntegrationUnitTestCase extends JBossTestCase
{

   static String username = "scott";

   static char[] password = "echoman".toCharArray();

   LoginContext lc;

   boolean loggedIn;

   private static String login_config = "security/authorization/xacml-ejb/app-policy-service.xml";

   public XACMLEJBIntegrationUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(XACMLEJBIntegrationUnitTestCase.class));
      // Create an initializer for the test suite
      TestSetup wrapper = new JBossTestSetup(suite)
      {
         @Override
         protected void setUp() throws Exception
         {
            super.setUp();
            deploy("xacml-ejb.jar");
            deploy(getResourceURL(login_config));
         }

         @Override
         protected void tearDown() throws Exception
         {
            undeploy(getResourceURL(login_config));
            undeploy("xacml-ejb.jar");
            super.tearDown();
         }
      };
      return wrapper;
   }

   /**
    * Test that the echo method is accessible by an Echo role. Since the noop() method of the StatelessSession bean was
    * not assigned any permissions it should be unchecked.
    */
   public void testMethodAccess() throws Exception
   {
      log.debug("+++ testMethodAccess");
      process();
   }

   /**
    * Test that redeploying the deployment unit does not add another policy. In other words, checks if undeploying
    * removes the policy. Does exactly what testMethodAccess() do.
    */
   public void testJBAS6067() throws Exception
   {
      undeploy(getResourceURL(login_config));
      undeploy("xacml-ejb.jar");
      deploy("xacml-ejb.jar");
      deploy(getResourceURL(login_config));

      log.debug("+++ testJBAS6067");
      process();
   }

   private void process() throws Exception
   {
      login();
      Object obj = getInitialContext().lookup("spec.StatelessSession");
      obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
      StatelessSessionHome home = (StatelessSessionHome) obj;
      log.debug("Found StatelessSessionHome");
      StatelessSession bean = home.create();
      log.debug("Created spec.StatelessSession");
      log.debug("Bean.echo('Hello') -> " + bean.echo("Hello"));

      try
      {
         // This should not be allowed
         bean.noop();
         fail("Was able to call StatelessSession.noop");
      }
      catch (RemoteException e)
      {
         log.debug("StatelessSession.noop failed as expected");
      }
      bean.remove();
      logout();
   }

   /**
    * Login as user scott using the conf.name login config or 'spec-test' if conf.name is not defined.
    */
   private void login() throws Exception
   {
      login(username, password);
   }

   private void login(String username, char[] password) throws Exception
   {
      if (loggedIn)
         return;

      lc = null;
      String confName = System.getProperty("conf.name", "spec-test");
      AppCallbackHandler handler = new AppCallbackHandler(username, password);
      log.debug("Creating LoginContext(" + confName + ")");
      lc = new LoginContext(confName, handler);
      lc.login();
      log.debug("Created LoginContext, subject=" + lc.getSubject());
      loggedIn = true;
   }

   private void logout() throws Exception
   {
      if (loggedIn)
      {
         loggedIn = false;
         lc.logout();
      }
   }
}
