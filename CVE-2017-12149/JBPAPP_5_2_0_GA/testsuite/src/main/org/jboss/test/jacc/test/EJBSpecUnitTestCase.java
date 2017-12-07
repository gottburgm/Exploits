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

import javax.security.auth.login.Configuration;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.TestSetup;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.util.jms.JMSDestinationsUtil;
import org.jboss.security.auth.login.XMLLoginConfigImpl;

/** Test of EJB spec conformace using the security-spec.jar
 * deployment unit when running under a JACC authorization manager.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 105762 $
 */
public class EJBSpecUnitTestCase
   extends org.jboss.test.security.test.EJBSpecUnitTestCase
{

   public EJBSpecUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      // JBAS-3602, the execution order of tests in this test case is important
      // so it must be defined explicitly when running under some JVMs      
      TestSuite suite = new TestSuite();
      
      // @todo this fails under JACC because there is no automatic granting
      // of the correct create permissions needed for the handle activation. This
      // is due to the implementation detail of the handle calling back into
      // the container on activation.
      //suite.addTest(new EJBSpecUnitTestCase("testStatefulHandle"));
      
      // Needs to be excluded because for JACC, the role "Role2"
      // is not defined as a security-role-ref
      //suite.addTest(new EJBSpecUnitTestCase("testDomainInteraction"));

      // All are baseclass tests
      //JBAS-6944 suite.addTest(new EJBSpecUnitTestCase("testSecurityDomain"));      
      suite.addTest(new EJBSpecUnitTestCase("testStatefulCreateCaller"));  
      suite.addTest(new EJBSpecUnitTestCase("testGetCallerPrincipal"));  
      suite.addTest(new EJBSpecUnitTestCase("testPrincipalPropagation"));  
      suite.addTest(new EJBSpecUnitTestCase("testMethodAccess"));  
      suite.addTest(new EJBSpecUnitTestCase("testDomainMethodAccess"));  
      suite.addTest(new EJBSpecUnitTestCase("testMethodAccess2"));  
      suite.addTest(new EJBSpecUnitTestCase("testLocalMethodAccess"));  
      suite.addTest(new EJBSpecUnitTestCase("testUncheckedRemote"));  
      suite.addTest(new EJBSpecUnitTestCase("testRemoteUnchecked"));  
      suite.addTest(new EJBSpecUnitTestCase("testUnchecked"));  
      suite.addTest(new EJBSpecUnitTestCase("testUncheckedWithLogin"));  
      suite.addTest(new EJBSpecUnitTestCase("testExcluded"));  
      suite.addTest(new EJBSpecUnitTestCase("testRunAs"));  
      suite.addTest(new EJBSpecUnitTestCase("testDeepRunAs"));  
      suite.addTest(new EJBSpecUnitTestCase("testRunAsSFSB"));  
      suite.addTest(new EJBSpecUnitTestCase("testJBAS1852"));  
      suite.addTest(new EJBSpecUnitTestCase("testMDBRunAs"));  
      suite.addTest(new EJBSpecUnitTestCase("testMDBDeepRunAs"));  
      suite.addTest(new EJBSpecUnitTestCase("testRunAsWithRoles"));  
      suite.addTest(new EJBSpecUnitTestCase("testHandle"));  
      suite.addTest(new EJBSpecUnitTestCase("testStress"));  
      suite.addTest(new EJBSpecUnitTestCase("testStressNoJaasCache"));  
      
      // Create an initializer for the test suite
      TestSetup wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            Configuration.setConfiguration(XMLLoginConfigImpl.getInstance());
            JMSDestinationsUtil.setupBasicDestinations();
            JMSDestinationsUtil.deployQueue("QueueA");
            JMSDestinationsUtil.deployQueue("QueueB");
            JMSDestinationsUtil.deployQueue("QueueC");
            JMSDestinationsUtil.deployQueue("QueueD");
            redeploy("security-spec.jar");
            flushAuthCache();
         }
         protected void tearDown() throws Exception
         {
            undeploy("security-spec.jar");
            JMSDestinationsUtil.destroyDestinations();
            super.tearDown();
         
         }
      };
      return wrapper;
   }

}
