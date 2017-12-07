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
package org.jboss.test.aop.test;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanInfo;
import javax.management.ObjectName;


import junit.framework.Test;
import junit.framework.TestSuite;


import org.jboss.test.JBossTestCase;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id: MicrocontainerJMXUnitTestCase.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $
 */

public class MicrocontainerJMXUnitTestCase
        extends JBossTestCase
{
   static boolean deployed = false;
   static int test = 0;

   public MicrocontainerJMXUnitTestCase(String name)
   {
      super(name);
   }

   public void testAnnotated() throws Exception
   {
      ObjectName testerName = new ObjectName("jboss.aop:name=AnnotatedBean");
      testBean(testerName);
   }

   public void testXml() throws Exception
   {
      ObjectName testerName = new ObjectName("jboss.aop:name=XmlBean");
      testBean(testerName);
   }

   public void testBeanWithCtorMethodCall() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=BeanWithCtorMethodCall");
      MBeanInfo info = server.getMBeanInfo(testerName);
      assertNotNull(info);
      server.setAttribute(testerName, new Attribute("Property", 42));
      assertEquals(42, server.getAttribute(testerName, "Property"));
   }

   public void testBeanWithDependencyFromAspect() throws Exception
   {
      //Do this twice since there was a problem with redeployment
      doTestDeployDependencies();
   }

   public void testRedeployedBeanWithDependencyFromAspect() throws Exception
   {
      //Do this twice since there was a problem with redeployment
      doTestDeployDependencies();
   }

   private void doTestDeployDependencies() throws Exception
   {
      try
      {
         deploy("aop-mc-jmxtest-has-dependency.jar");
      }
      catch(Exception expected)
      {
         //Since the dependencies are not there, we get an exception...
      }
      
      try
      {
         MBeanServerConnection server = getServer();
         ObjectName testerName = new ObjectName("jboss.aop:name=BeanWithDependency");
         try
         {
            server.getMBeanInfo(testerName);
            fail(testerName + " should not have been found");
         }
         catch (InstanceNotFoundException expected)
         {
         }

         deploy("aop-mc-jmxtest-dependency.jar");
         try
         {
            server.setAttribute(testerName, new Attribute("Property", 42));
            assertEquals(42, server.getAttribute(testerName, "Property"));
            String ret = (String)server.invoke(testerName, "someAction", new Object[0], new String[0]);
            assertNotNull(ret);
            assertEquals("true", ret);
         }
         finally
         {
            undeploy("aop-mc-jmxtest-dependency.jar");
         }

         try
         {
            server.getMBeanInfo(testerName);
            fail(testerName + " should not have been found");
         }
         catch (InstanceNotFoundException expected)
         {
         }
      }
      finally
      {
         undeploy("aop-mc-jmxtest-has-dependency.jar");
      }
   }

   private void testBean(ObjectName on) throws Exception
   {
      MBeanServerConnection server = getServer();
      server.setAttribute(on, new Attribute("Property", 42));
      assertEquals(42, server.getAttribute(on, "Property"));

      Object ret = server.invoke(on, "someAction", new Object[0], new String[0]);
      assertEquals("JMX42", ret);
   }


   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(MicrocontainerJMXUnitTestCase.class));

      AOPTestSetup setup = new AOPTestSetup(suite, "aop-mc-jmxtest.jar");
      return setup;
   }

}
