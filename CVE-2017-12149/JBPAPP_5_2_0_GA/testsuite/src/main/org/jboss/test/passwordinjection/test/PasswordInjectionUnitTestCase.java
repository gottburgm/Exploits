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
package org.jboss.test.passwordinjection.test;

import javax.naming.InitialContext;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.security.beans.TestPasswordInjectedBean;

/**
 * JBAS-6710: Password masking in xml
 * @author Anil.Saldhana@redhat.com
 * @since Apr 17, 2009
 */
public class PasswordInjectionUnitTestCase extends JBossTestCase
{
   public PasswordInjectionUnitTestCase(String name)
   {
      super(name); 
   }
   
   public void testPasswordInjection() throws Exception
   {
      InitialContext ic = new InitialContext();
      TestPasswordInjectedBean tp = (TestPasswordInjectedBean) ic.lookup("testJNDIBean");
      assertNotNull("Password Bean is in JNDI", tp);
      assertTrue("Password has been injected", tp.isPasswordSet());
   }
   
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(PasswordInjectionUnitTestCase.class));

      // Create an initializer for the test suite
      TestSetup wrapper = new JBossTestSetup(suite)
      { 
         String passBeans = "test-password-jboss-beans.xml";
         String jarName = "passwordbean.jar";
         
         protected void setUp() throws Exception
         {
            super.setUp();

            deploy(jarName);
            
            // deploy the Password Beans
            String url1 = getResourceURL("security/password-mask/" + passBeans);
            deploy(url1); 
            
         }
         protected void tearDown() throws Exception
         {
            undeploy(jarName);
            
            // undeploy the Password Beans
            String url1 = getResourceURL("security/password-mask/" + passBeans);
            undeploy(url1); 
            super.tearDown(); 
         }
      };
      return wrapper;
   }
}