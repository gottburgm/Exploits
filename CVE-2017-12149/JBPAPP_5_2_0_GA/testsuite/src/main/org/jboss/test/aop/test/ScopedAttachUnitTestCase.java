/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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


import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Kabir Khan</a>
 * @version $Id: ScopedAttachUnitTestCase.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $
 */
public class ScopedAttachUnitTestCase extends JBossTestCase
{
   Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;
   static AOPClassLoaderHookTestSetup setup;

   public ScopedAttachUnitTestCase(String name)
   {
      super(name);
   }

   public void testPOJOAdvised1() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester1");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "checkPOJOAdvised", params, sig);
   }
   
   public void testPOJOAdvised2() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester2");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "checkPOJOAdvised", params, sig);
   }
   
   public void testExpectedValues1() throws Exception
   {
      deploy("aop-scopedattachtest1.aop");
      try
      {
         MBeanServerConnection server = getServer();
         ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester1");
         Integer iv = (Integer)server.getAttribute(testerName, "ExpectedInterceptorValue");
         Integer ia = (Integer)server.getAttribute(testerName, "ExpectedAspectValue");
         assertEquals(11, iv.intValue());
         assertEquals(21, ia.intValue());
      }
      finally
      {
         undeploy("aop-scopedattachtest1.aop");
      }
   }
   
   public void testExpectedValues2() throws Exception
   {
      deploy("aop-scopedattachtest2.aop");
      try
      {
         MBeanServerConnection server = getServer();
         ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester2");
         Integer iv = (Integer)server.getAttribute(testerName, "ExpectedInterceptorValue");
         Integer ia = (Integer)server.getAttribute(testerName, "ExpectedAspectValue");
         assertEquals(12, iv.intValue());
         assertEquals(22, ia.intValue());
      }
      finally
      {
         undeploy("aop-scopedattachtest2.aop");
      }
   }
   
   public void testScoped1() throws Exception
   {
      deploy("aop-scopedattachtest1.aop");
      try
      {
         MBeanServerConnection server = getServer();
         ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester1");
         Object[] params = {};
         String[] sig = {};
         server.invoke(testerName, "testScoped", params, sig);
      }
      finally
      {
         undeploy("aop-scopedattachtest1.aop");
      }
   }

   public void testScoped2() throws Exception
   {
      deploy("aop-scopedattachtest2.aop");
      try
      {
         MBeanServerConnection server = getServer();
         ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester2");
         Object[] params = {};
         String[] sig = {};
         server.invoke(testerName, "testScoped", params, sig);
      }
      finally
      {
         undeploy("aop-scopedattachtest2.aop");
      }
   }
   
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(ScopedAttachUnitTestCase.class));

      setup = new AOPClassLoaderHookTestSetup(suite, "aop-scopedattachtest1.sar,aop-scopedattachtest2.sar");
      //Since this test relies on some of the aspects from base-aspects.xml, deploy those for this test
      setup.setUseBaseXml(true);
      return setup;
   }
}