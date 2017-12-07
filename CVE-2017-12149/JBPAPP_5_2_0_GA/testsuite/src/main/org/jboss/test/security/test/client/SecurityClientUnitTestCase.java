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
package org.jboss.test.security.test.client;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.security.auth.login.Configuration;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.security.auth.callback.AppCallbackHandler;
import org.jboss.security.auth.login.XMLLoginConfigImpl;
import org.jboss.security.client.JBossSecurityClient;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup; 
import org.jboss.test.security.interfaces.CalledSession;
import org.jboss.test.security.interfaces.CalledSessionHome;

//$Id: SecurityClientUnitTestCase.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

/**
 *  Unit tests for the Security Client SPI
 *  @author Anil.Saldhana@redhat.com
 *  @since  May 1, 2007 
 *  @version $Revision: 85945 $
 */
public class SecurityClientUnitTestCase extends JBossTestCase
{ 
   public SecurityClientUnitTestCase(String name)
   {
      super(name); 
   }

   public void testSecurityClientFactory() throws Exception
   {
      SecurityClient sc = null;
      sc = SecurityClientFactory.getSecurityClient();
      assertNotNull("SecurityClient != null",sc);
      sc = SecurityClientFactory.getSecurityClient("org.jboss.security.client.JBossSecurityClient");
      assertNotNull("SecurityClient != null",sc);
      sc = SecurityClientFactory.getSecurityClient(JBossSecurityClient.class);
      assertNotNull("SecurityClient != null",sc);
   }

   /** Test a user with Echo and EchoLocal roles can access the CalleeBean
       through its local interface by calling the CallerBean and that a user
       with only a EchoLocal cannot call the CallerBean.
    */
   public void testLocalEJBMethodAccessWithSimpleLogin() throws Exception
   {
      log.debug("+++ testLocalEJBMethodAccessWithSimpleLogin"); 
      SecurityClient sc = SecurityClientFactory.getSecurityClient(JBossSecurityClient.class);
      sc.setSimple("scott", "echoman".toCharArray()); 
      sc.login();
      InitialContext jndiContext = new InitialContext();
      Object obj = jndiContext.lookup("spec.CallerBean");
      obj = PortableRemoteObject.narrow(obj, CalledSessionHome.class);
      CalledSessionHome home = (CalledSessionHome) obj;
      log.debug("Found spec.CallerBean Home");
      CalledSession bean = home.create();
      log.debug("Created spec.CallerBean");
      log.debug("Bean.invokeEcho('testLocalMethodAccess') -> "+bean.invokeEcho("testLocalMethodAccess"));
      bean.remove();
      sc.logout();
   }

   /** Test a user with Echo and EchoLocal roles can access the CalleeBean
       through its local interface by calling the CallerBean and that a user
       with only a EchoLocal cannot call the CallerBean.
    */
   public void testLocalEJBMethodAccessWithJaasLogin() throws Exception
   {
      log.debug("+++ testLocalEJBMethodAccessWithJaasLogin");
      String confName = System.getProperty("conf.name", "spec-test");

      SecurityClient sc = SecurityClientFactory.getSecurityClient(JBossSecurityClient.class);
      AppCallbackHandler acbh = new AppCallbackHandler("scott","echoman".toCharArray()); 
      sc.setJAAS(confName, acbh);
      sc.login();
      InitialContext jndiContext = new InitialContext();
      Object obj = jndiContext.lookup("spec.CallerBean");
      obj = PortableRemoteObject.narrow(obj, CalledSessionHome.class);
      CalledSessionHome home = (CalledSessionHome) obj;
      log.debug("Found spec.CallerBean Home");
      CalledSession bean = home.create();
      log.debug("Created spec.CallerBean");
      log.debug("Bean.invokeEcho('testLocalMethodAccess') -> "+bean.invokeEcho("testLocalMethodAccess"));
      bean.remove();
      sc.logout();
   }

   /** Test a user with Echo and EchoLocal roles can access the CalleeBean
       through its local interface by calling the CallerBean and that a user
       with only a EchoLocal cannot call the CallerBean.
    */
   public void testLocalEJBMethodAccessWithlogout() throws Exception
   {
      log.debug("+++ testLocalEJBMethodAccessWithlogout"); 

      SecurityClient sc = SecurityClientFactory.getSecurityClient(JBossSecurityClient.class); 
      sc.logout(); 
      InitialContext jndiContext = new InitialContext();
      Object obj = jndiContext.lookup("spec.CallerBean");
      obj = PortableRemoteObject.narrow(obj, CalledSessionHome.class);
      CalledSessionHome home = (CalledSessionHome) obj; 
      try
      { 
         log.debug("Found spec.CallerBean Home");
         home.create();
         fail("home.create Should have failed");
      }
      catch(Exception e)
      { 
         log.debug("Got the expected exception",e);
      } 
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(SecurityClientUnitTestCase.class));

      // Create an initializer for the test suite
      TestSetup wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            Configuration.setConfiguration(XMLLoginConfigImpl.getInstance());
            redeploy("security-spec.jar");
            flushAuthCache();
         }
         protected void tearDown() throws Exception
         {
            undeploy("security-spec.jar");
            super.tearDown();

         }
      };
      return wrapper;
   } 
}
