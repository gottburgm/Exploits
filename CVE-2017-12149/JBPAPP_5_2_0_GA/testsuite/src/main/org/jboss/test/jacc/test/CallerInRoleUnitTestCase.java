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
package org.jboss.test.jacc.test;

import javax.rmi.PortableRemoteObject;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.security.auth.login.XMLLoginConfigImpl;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup; 
import org.jboss.test.security.interfaces.UsefulStatelessSession;
import org.jboss.test.security.interfaces.UsefulStatelessSessionHome; 
import org.jboss.test.util.AppCallbackHandler;

//$Id: CallerInRoleUnitTestCase.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/**
 *  JBAS-2661:EJB context isCallerInRole not delegating to JACC when installed
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Apr 20, 2006
 *  @version $Revision: 81036 $
 */
public class CallerInRoleUnitTestCase extends JBossTestCase
{ 
   LoginContext lc;
   public CallerInRoleUnitTestCase(String name)
   {
      super(name); 
   }
   
   public void testCallerInRoleForBean1() throws Exception
   {
      login("anil","opensource".toCharArray()); 
      Object obj = getInitialContext().lookup("bean1.UsefulStatelessSessionBean");
      obj = PortableRemoteObject.narrow(obj, UsefulStatelessSessionHome.class);
      UsefulStatelessSessionHome home = (UsefulStatelessSessionHome) obj; 
      UsefulStatelessSession bean = home.create();  
      assertEquals("NiceUser is true", "true", ""+bean.isCallerInRole("NiceUser") ); 
      assertEquals("BadRole is false", "false", ""+bean.isCallerInRole("BadRole") );
      bean.remove();
      logout();
   } 
   
   private void login(String username, char[] password) throws Exception
   {  
      lc = null;
      String confName = System.getProperty("conf.name", "spec-test");
      AppCallbackHandler handler = new AppCallbackHandler(username, password);
      log.debug("Creating LoginContext("+confName+")");
      lc = new LoginContext(confName, handler);
      lc.login();
      log.debug("Created LoginContext, subject="+lc.getSubject()); 
   }
   
   private void logout() throws Exception
   { 
         lc.logout(); 
   }
   
   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(CallerInRoleUnitTestCase.class));

      // Create an initializer for the test suite
      TestSetup wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            Configuration.setConfiguration(XMLLoginConfigImpl.getInstance());
            redeploy("security-jacc-callerinrole.jar"); 
         }
         protected void tearDown() throws Exception
         {
            undeploy("security-jacc-callerinrole.jar");
            super.tearDown(); 
         }
      };
      return wrapper;
   }
}
