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
package org.jboss.test.jmx.test;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;

//$Id: MBeanServiceRegistrationUnitTestCase.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

/**
 *  Deploy/undeploy a mbean service
 *  few times to validate mbean service registration/deregistation
 *  @author Anil.Saldhana@redhat.com
 *  @since  Nov 30, 2007 
 *  @version $Revision: 85945 $
 */
public class MBeanServiceRegistrationUnitTestCase extends JBossTestCase
{ 
   public MBeanServiceRegistrationUnitTestCase(String name)
   {
      super(name); 
   }
   
   public void testMBeanRegistration()
   {   
   }

   /**
    * Setup the test suite.
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(MBeanServiceRegistrationUnitTestCase.class));

      // Create an initializer for the test suite
      TestSetup wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            undeploy(getResourceURL("security-spi/deploymentlevel/deploymentlevel-test-service.xml"));
            deploy(getResourceURL("security-spi/deploymentlevel/deploymentlevel-test-service.xml"));
            undeploy(getResourceURL("security-spi/deploymentlevel/deploymentlevel-test-service.xml"));
            deploy(getResourceURL("security-spi/deploymentlevel/deploymentlevel-test-service.xml")); 
         }
         protected void tearDown() throws Exception
         {
            undeploy(getResourceURL("security-spi/deploymentlevel/deploymentlevel-test-service.xml"));  
            super.tearDown(); 
         }
      };
      return wrapper;
   } 
}
