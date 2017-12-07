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
package org.jboss.test.naming.test;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.JBossTestSuite;
import org.jboss.test.naming.ejb.NamingTests;
import org.jboss.test.util.jms.JMSDestinationsUtil;

/** Stress tests for the JNDI naming layer
 *
 *  @author Scott.Stark@jboss.org
 *  @version $Revision: 105321 $
 */
public class NamingStressTestCase extends JBossTestCase
{
   public NamingStressTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      Properties props = new Properties();
      props.setProperty("ejbRunnerJndiName", "EJBTestRunnerHome");
      props.setProperty("encBeanJndiName", "ENCBean");
      props.setProperty("encIterations", "1000");

      JBossTestSuite testSuite = new JBossTestSuite(props);
      
      JBossTestSetup test = new JBossTestSetup(new TestSuite(NamingTests.class))
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            JMSDestinationsUtil.setupBasicDestinations();
            deploy ("naming.jar");
         }
         protected void tearDown() throws Exception
         {
            super.tearDown();
            undeploy ("naming.jar");
            JMSDestinationsUtil.destroyDestinations();
         }
      };
      
      testSuite.addTest(test);
      
      return testSuite;
      
      
   }

}
