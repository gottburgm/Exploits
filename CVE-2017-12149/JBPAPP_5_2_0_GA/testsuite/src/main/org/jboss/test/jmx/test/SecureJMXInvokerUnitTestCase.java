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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import junit.framework.Test;
import junit.framework.TestSuite;

/** Tests for the jmx invoker adaptor with a secured xmbean.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class SecureJMXInvokerUnitTestCase extends JMXInvokerUnitTestCase
{
   public SecureJMXInvokerUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      // JBAS-3605, the execution order of tests in this test case is important
      // so it must be defined explicitly when running under some JVMs
      TestSuite suite = new TestSuite();
      suite.addTest(new SecureJMXInvokerUnitTestCase("testGetSomething"));
      suite.addTest(new SecureJMXInvokerUnitTestCase("testGetCustom"));
      suite.addTest(new SecureJMXInvokerUnitTestCase("testGetCustomXMBean"));
      suite.addTest(new SecureJMXInvokerUnitTestCase("testGetXMBeanInfo"));
      suite.addTest(new SecureJMXInvokerUnitTestCase("testXMBeanDoSomething"));
      suite.addTest(new SecureJMXInvokerUnitTestCase("testSetCustom"));
      suite.addTest(new SecureJMXInvokerUnitTestCase("testClassNotFoundException"));
      suite.addTest(new SecureJMXInvokerUnitTestCase("testNotification"));
      suite.addTest(new SecureJMXInvokerUnitTestCase("testNotificationWithBadListener"));
      
      return getDeploySetup(suite, "invoker-adaptor-test.ear");
   }

   ObjectName getObjectName() throws MalformedObjectNameException
   {
      return new ObjectName("jboss.test:service=InvokerTest,secured=true");
   }
   
   protected void setUp() throws Exception
   {
      super.setUp();
      delegate.login();
   }
   
   protected void tearDown() throws Exception
   {
      super.tearDown();
      delegate.logout();
   }
}
