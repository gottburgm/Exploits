/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * A TestSuite equivalent to the smoke-tests build.xml target
org.jboss.test.aop.test.AOPUnitTestCase
org.jboss.test.classloader.test.ScopingUnitTestCase
org.jboss.test.cts.test.BmpUnitTestCase
org.jboss.test.cts.test.CmpUnitTestCase
org.jboss.test.cts.test.CtsCmp2OptionDUnitTestCase
org.jboss.test.cts.test.CtsCmp2UnitTestCase
org.jboss.test.cts.test.IndependentJarsUnitTestCase
org.jboss.test.cts.test.MDBUnitTestCase
org.jboss.test.cts.test.StatelessSessionBrokenCreateUnitTestCase
org.jboss.test.cts.test.StatelessSessionUnitTestCase
org.jboss.test.ejb3.test.SimpleSessionUnitTestCase
org.jboss.test.jbossmessaging.test.JBossMessagingJoramUnitTestCase
org.jboss.test.jca.test.BaseConnectionManagerUnitTestCase
org.jboss.test.jca.test.PoolingUnitTestCase
org.jboss.test.jca.test.XADSUnitTestCase
org.jboss.test.jmsra.test.RaJMSSessionUnitTestCase
org.jboss.test.jmsra.test.RaQueueUnitTestCase
org.jboss.test.jmsra.test.RaSyncRecUnitTestCase
org.jboss.test.jmsra.test.RaTopicUnitTestCase
org.jboss.test.naming.test.SimpleUnitTestCase
org.jboss.test.tm.test.TransactionManagerUnitTestCase
org.jboss.test.web.test.WebIntegrationUnitTestCase
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 105321 $
 */
public class SmokeTestSuite extends TestSuite
{
   public static void main(String[] args)
      throws Exception
   {
      TestRunner.run(suite());
   }
   public static Test suite()
      throws Exception
   {
      TestSuite suite = new TestSuite("Smoke Tests");

      suite.addTest(org.jboss.test.aop.test.AOPUnitTestCase.suite());
      suite.addTestSuite(org.jboss.test.classloader.test.ScopingUnitTestCase.class);
      suite.addTest(org.jboss.test.cts.test.BmpUnitTestCase.suite());
      suite.addTest(org.jboss.test.cts.test.CmpUnitTestCase.suite());
      suite.addTest(org.jboss.test.cts.test.CmpUnitTestCase.suite());
      suite.addTest(org.jboss.test.cts.test.CtsCmp2OptionDUnitTestCase.suite());
      suite.addTest(org.jboss.test.cts.test.CtsCmp2UnitTestCase.suite());
      suite.addTest(org.jboss.test.cts.test.IndependentJarsUnitTestCase.suite());
      suite.addTest(org.jboss.test.cts.test.MDBUnitTestCase.suite());
      suite.addTest(org.jboss.test.cts.test.StatelessSessionBrokenCreateUnitTestCase.suite());
      suite.addTest(org.jboss.test.cts.test.StatelessSessionUnitTestCase.suite());
      suite.addTest(org.jboss.test.ejb3.test.SimpleSessionUnitTestCase.suite());
      suite.addTest(org.jboss.test.jbossmessaging.test.JoramUnitTestCase.suite());
      suite.addTestSuite(org.jboss.test.jca.test.BaseConnectionManagerUnitTestCase.class);
      suite.addTest(org.jboss.test.jca.test.PoolingUnitTestCase.suite());
      suite.addTest(org.jboss.test.jca.test.XADSUnitTestCase.suite());
      suite.addTest(org.jboss.test.jmsra.test.RaJMSSessionUnitTestCase.suite());
      suite.addTest(org.jboss.test.jmsra.test.RaQueueUnitTestCase.suite());
      suite.addTest(org.jboss.test.jmsra.test.RaSyncRecUnitTestCase.suite());
      suite.addTest(org.jboss.test.jmsra.test.RaTopicUnitTestCase.suite());
      suite.addTestSuite(org.jboss.test.naming.test.SimpleUnitTestCase.class);
      suite.addTest(org.jboss.test.tm.test.TransactionManagerUnitTestCase.suite());
      suite.addTest(org.jboss.test.web.test.WebIntegrationUnitTestCase.suite());
      
      return suite;
   }

   

}
