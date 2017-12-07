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

import java.net.URL;

import javax.management.Attribute; 
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.util.web.HttpUtils;

/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Kabir Khan</a>
 * @version $Id: ScopedUnitTestCase.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $
 */
public class ScopedUnitTestCase
        extends JBossTestCase
{
   org.jboss.logging.Logger log = getLog();

   static boolean deployed = false;
   static int test = 0;
   static AOPClassLoaderHookTestSetup setup;

   public ScopedUnitTestCase(String name)
   {
      super(name);
   }

   public void testExpectedValues1() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester1");
      Integer iv = (Integer)server.getAttribute(testerName, "ExpectedInterceptorValue");
      Integer ia = (Integer)server.getAttribute(testerName, "ExpectedAspectValue");
      assertEquals(11, iv.intValue());
      assertEquals(21, ia.intValue());
   }
   
   public void testExpectedValues2() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester2");
      Integer iv = (Integer)server.getAttribute(testerName, "ExpectedInterceptorValue");
      Integer ia = (Integer)server.getAttribute(testerName, "ExpectedAspectValue");
      assertEquals(12, iv.intValue());
      assertEquals(22, ia.intValue());
   }
   
   public void testScoped1() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester1");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testScoped", params, sig);
   }

   public void testScoped2() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester2");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testScoped", params, sig);
   }
   
   public void testAnnotations1() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester1");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testAnnotatedScopedAnnotationsDeployed", params, sig);
   }
   
   public void testAnnotations2() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester2");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testAnnotatedScopedAnnotationsNotDeployed", params, sig);
   }
   
   public void testIntroduction1() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester1");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testIntroduction1", params, sig);
   }
   
   public void testIntroduction2() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester2");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testIntroduction2", params, sig);
   }

   public void testInclude1() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester1");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testInclude", params, sig);
   }
   
   public void testInclude2() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester2");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testInclude", params, sig);
   }
   
   public void testExclude1() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester1");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testExclude", params, sig);
   }
   
   public void testExclude2() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester2");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testExclude", params, sig);
   }
   
   public void testIgnore1() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester1");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testIgnore", params, sig);
   }
   
   public void testIgnore2() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=ScopedTester2");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testIgnore", params, sig);
   }
   
   public void testEar1() throws Exception
   {
      URL url = new URL(HttpUtils.getBaseURL() + "aop-scopedear1/srv");
      HttpUtils.accessURL(url);      
   }

   public void testEar2() throws Exception
   {
      URL url = new URL(HttpUtils.getBaseURL() + "aop-scopedear2/srv");
      HttpUtils.accessURL(url);
   }

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(ScopedUnitTestCase.class));

      setup = new AOPClassLoaderHookTestSetup(suite, new MySetupHook(), "aop-scopedtest1.sar,aop-scopedtest2.sar,aop-scopedear1.ear,aop-scopedear2.ear");
      //Since this test relies on some of the aspects from base-aspects.xml, deploy those for this test
      setup.setUseBaseXml(true);

      return setup;
   }

   static class MySetupHook implements SetupHook
   {
      String originalIgnore;
      String originalInclude;
      String originalExclude;

      public void setup(MBeanServerConnection server) throws Exception
      {
         ObjectName aspectManager = new ObjectName(AOPClassLoaderHookTestSetup.ASPECT_MANAGER_NAME);
         
         originalIgnore = (String)server.getAttribute(aspectManager, "Ignore");
         server.setAttribute(aspectManager, new Attribute("Ignore", "*$$Ignored$$*"));
         
         originalInclude = (String)server.getAttribute(aspectManager, "Include");
         server.setAttribute(aspectManager, new Attribute("Include", "org.jboss.test.aop.scoped.excluded.included."));
         
         originalExclude = (String)server.getAttribute(aspectManager, "Exclude");
         server.setAttribute(aspectManager, new Attribute("Exclude", "org.jboss.test.aop.scoped.excluded."));
      }
   
      public void teardown(MBeanServerConnection server) throws Exception
      {
         ObjectName aspectManager = new ObjectName(AOPClassLoaderHookTestSetup.ASPECT_MANAGER_NAME);
   
         server.setAttribute(aspectManager, new Attribute("Ignore", originalIgnore));
         server.setAttribute(aspectManager, new Attribute("Include", originalInclude));
         server.setAttribute(aspectManager, new Attribute("Exclude", originalExclude));
      }
   }
}
