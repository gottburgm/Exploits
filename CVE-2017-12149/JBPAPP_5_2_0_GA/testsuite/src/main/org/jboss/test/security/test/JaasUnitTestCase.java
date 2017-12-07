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

import java.net.URL;
import javax.rmi.PortableRemoteObject;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;

import org.jboss.security.auth.login.XMLLoginConfigImpl;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.security.interfaces.CustomPrincipalHome;
import org.jboss.test.security.interfaces.CustomPrincipal;
import org.jboss.test.security.ejb.CustomPrincipalImpl;
import org.jboss.test.util.AppCallbackHandler;
import org.jboss.test.util.web.HttpUtils;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;


/** JAAS specific tests.
 
 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class JaasUnitTestCase
   extends JBossTestCase
{
   static String username = "jduke";
   static char[] password = "theduke".toCharArray();
   
   LoginContext lc;
   boolean loggedIn;

   public JaasUnitTestCase(String name)
   {
      super(name);
   }

   /** Test return of a custom principal from getCallerPrincipal.
    */
   public void testCustomEJBPrincipal() throws Exception
   {
      login();
      log.debug("+++ testCustomEJBPrincipal()");
      Object obj = getInitialContext().lookup("jaas.CustomPrincipalHome");
      obj = PortableRemoteObject.narrow(obj, CustomPrincipalHome.class);
      CustomPrincipalHome home = (CustomPrincipalHome) obj;
      log.debug("Found CustomPrincipalHome");
      CustomPrincipal bean = home.create();
      log.debug("Created CustomPrincipal");
      
      boolean isCustomType = bean.validateCallerPrincipal(CustomPrincipalImpl.class);
      bean.remove();
      logout();
      assertTrue("CustomPrincipalImpl was seen", isCustomType);
   }

   /** Test return of a custom principal from getCallerPrincipal coming from
    * a custom login module.
    */
   public void testCustomEJBPrincipal2() throws Exception
   {
      login();
      log.debug("+++ testCustomEJBPrincipal()");
      Object obj = getInitialContext().lookup("jaas.CustomPrincipal2Home");
      obj = PortableRemoteObject.narrow(obj, CustomPrincipalHome.class);
      CustomPrincipalHome home = (CustomPrincipalHome) obj;
      log.debug("Found CustomPrincipalHome");
      CustomPrincipal bean = home.create();
      log.debug("Created CustomPrincipal");
      
      boolean isCustomType = bean.validateCallerPrincipal(CustomPrincipalImpl.class);
      bean.remove();
      logout();
      assertTrue("CustomPrincipalImpl was seen", isCustomType);
   }

   public void testCustomWebPrincipal() throws Exception
   {
      log.debug("+++ testCustomWebPrincipal()");
      String base = HttpUtils.getBaseURL();
      URL testURL = new URL(base + "jaas/CustomPrincipalServlet"
         +"?type="+CustomPrincipalImpl.class.getName());
      HttpUtils.accessURL(testURL);
   }

   /** Login as user scott using the conf.name login config or
    'jaas-test' if conf.name is not defined.
    */
   private void login() throws Exception
   {
      login(username, password);
   }
   private void login(String username, char[] password) throws Exception
   {
      if( loggedIn )
         return;
      
      lc = null;
      String confName = System.getProperty("conf.name", "jaas-test");
      AppCallbackHandler handler = new AppCallbackHandler(username, password);
      log.debug("Creating LoginContext("+confName+")");
      lc = new LoginContext(confName, handler);
      lc.login();
      log.debug("Created LoginContext, subject="+lc.getSubject());
      loggedIn = true;
   }
   private void logout() throws Exception
   {
      if( loggedIn )
      {
         loggedIn = false;
         lc.logout();
      }
   }


   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(JaasUnitTestCase.class));

      // Create an initializer for the test suite
      TestSetup wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            Configuration.setConfiguration(XMLLoginConfigImpl.getInstance());
            deploy("security-jaas.ear");
            flushAuthCache("jaas-test");
         }
         protected void tearDown() throws Exception
         {
            undeploy("security-jaas.ear");
            super.tearDown();
         
         }
      };
      return wrapper;
   }

}
