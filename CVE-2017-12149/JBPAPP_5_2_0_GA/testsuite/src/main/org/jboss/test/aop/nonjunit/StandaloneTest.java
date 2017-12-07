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
package org.jboss.test.aop.nonjunit;

import javax.naming.InitialContext;

import org.jboss.test.JBossTestCase;
import junit.framework.Test;
import java.net.InetAddress;
import javax.management.ObjectName;
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.jboss.test.aop.bean.AOPTester;

import junit.framework.*;
/**
 * Sample client for the jboss container.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Id: StandaloneTest.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $
 */

public class StandaloneTest 
   extends TestCase 
{
   static boolean deployed = false;
   static int test = 0;
   AOPTester tester = new AOPTester();

   public StandaloneTest(String name) {

      super(name);
      
   }

   public void testMethodInterception() throws Exception
   {
      tester.testMethodInterception();
   }


   public void testFieldInterception() throws Exception
   {
      tester.testFieldInterception();
   }

   public void testBasic() throws Exception
   {
      tester.testBasic();
   }


   public void testAspect() throws Exception
   {
      tester.testAspect();
   }

   public void testCallerPointcut() throws Exception
   {
      tester.testCallerPointcut();
   }

   public void testInheritance() throws Exception
   {
      tester.testInheritance();
   }
   
   public void testMetadata() throws Exception
   {
      tester.testMetadata();
   }
   
   public void testDynamicInterceptors() throws Exception
   {
      tester.testDynamicInterceptors();
   }
   
   public void testMixin() throws Exception
   {
      tester.testMixin();
   }
   
   
   public void testConstructorInterception() throws Exception
   {
      tester.testConstructorInterception();
   }	

   public void testExceptions() throws Exception
   {
      tester.testExceptions();
   }	

   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(StandaloneTest.class));
      return suite;
   }

}
