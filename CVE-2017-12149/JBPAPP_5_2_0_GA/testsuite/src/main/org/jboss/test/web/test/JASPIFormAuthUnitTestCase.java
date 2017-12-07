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
package org.jboss.test.web.test;

import javax.management.ObjectName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.security.config.ApplicationPolicy;
import org.jboss.test.JBossTestSetup;

//$Id: JASPIFormAuthUnitTestCase.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/** Tests of form authentication with JASPI integration
 * 
 * @author Anil.Saldhana@jboss.org
 * @version $Revision: 81036 $
 */
public class JASPIFormAuthUnitTestCase extends FormAuthUnitTestCase
{ 
   public JASPIFormAuthUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testFlushOnSessionInvalidation() throws Exception
   {
      //noop
   }
  
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(JASPIFormAuthUnitTestCase.class));

      // Create an initializer for the test suite
      Test wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            deploy("jaspi-form-auth.ear");
            // Make sure the security cache is clear
            flushAuthCache();
            //Make sure the ExtendedFormAuthenticator is registered in tomcat
            String oname = "jboss.web:host="+getServerHost()+",name=ExtendedJASPIFormAuthenticator,path=/form-auth,type=Valve";
            ObjectName formAuth = new ObjectName(oname);
            //We have a form-auth war with FORM authenticator and that is not overriden at the webapp level
            assertNotNull("Authenticator for FORM on host=localhost exists?", getServer().getObjectInstance(formAuth));  
         }
         protected void tearDown() throws Exception
         {
            undeploy("jaspi-form-auth.ear");
            super.tearDown();
         }
      };
      return wrapper;
   }
}
