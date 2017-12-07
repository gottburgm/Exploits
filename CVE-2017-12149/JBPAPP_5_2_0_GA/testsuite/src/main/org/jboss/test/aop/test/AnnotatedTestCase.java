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

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;

/**
 * 
 * @author <a href="stale.pedersen@jboss.org">Stale W. Pedersen</a>
 * @version $Revision: 85945 $
 */
public class AnnotatedTestCase extends JBossTestCase
{

   public AnnotatedTestCase(String name)
   {
      super(name);
   }
   
   public void testBinding() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AnnotatedTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testBinding", params, sig);
   }

   public void testCompostition() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AnnotatedTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testCompostition", params, sig);
   }

   public void testMixin() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AnnotatedTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testMixin", params, sig);
   }

   public void testIntroduction() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AnnotatedTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testIntroduction", params, sig);
   }

   public void testInterceptorDef() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AnnotatedTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testInterceptorDef", params, sig);
   }

   public void testTypedef() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AnnotatedTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testTypedef", params, sig);
   }

   public void testCFlow() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AnnotatedTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testCFlow", params, sig);
   }

   public void testPrepare() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AnnotatedTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testPrepare", params, sig);
   }

   public void testPrepareAtClassLevel() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AnnotatedTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testPrepareAtClassLevel", params, sig);
   }

   public void testDynamicCFlow() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AnnotatedTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testDynamicCFlow", params, sig);
   }

   public void testAnnotationIntroduction() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AnnotatedTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testAnnotationIntroduction", params, sig);
   }
   public void testPrecedence() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AnnotatedTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testPrecedence", params, sig);
   }
   public void testAspectFactory() throws Exception
   {
      MBeanServerConnection server = getServer();
      ObjectName testerName = new ObjectName("jboss.aop:name=AnnotatedTester");
      Object[] params = {};
      String[] sig = {};
      server.invoke(testerName, "testAspectFactory", params, sig);
   }
      
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(AnnotatedTestCase.class));

      AOPTestSetup setup = new AOPTestSetup(suite, "aop-annotated.sar");
      return setup;
   }

}
