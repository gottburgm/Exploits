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
package org.jboss.test.jbossmx.compliance.standard;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.jbossmx.compliance.TestCase;

import org.jboss.test.jbossmx.compliance.standard.support.StandardDerived1;
import org.jboss.test.jbossmx.compliance.standard.support.StandardDerived2;
import org.jboss.test.jbossmx.compliance.standard.support.DynamicDerived1;
import org.jboss.test.jbossmx.compliance.standard.support.StandardDerived3;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;

/**
 * Beat the heck out of the server's standard MBeanInfo inheritance handling
 *
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 */
public class InheritanceTestCase
   extends TestCase
{
   private static int attributeTestCount = 0;
   private static int operationTestCount = 0;
   private static int constructorTestCount = 0;

   public InheritanceTestCase(String s)
   {
      super(s);
   }

   public static Test suite()
   {
      TestSuite testSuite = new TestSuite("All MBeanInfo Torture Tests for Standard MBeans");

      Object mbean = new StandardDerived1();
      MBeanInfo info = InfoUtil.getMBeanInfo(mbean, "test:type=mbeaninfo");

      addConstructorTest(testSuite, info, StandardDerived1.class.getName(), new String[0]);
      testSuite.addTest(new TestCoverageTEST("StandardDerived1 constructor list length", constructorTestCount, info.getConstructors().length));
      addAttributeTest(testSuite, info, "ParentValue", String.class.getName(), false, true, false);
      addAttributeTest(testSuite, info, "Available", boolean.class.getName(), false, true, false);
      testSuite.addTest(new TestCoverageTEST("StandardDerived1 attribute list length", attributeTestCount, info.getAttributes().length));
      testSuite.addTest(new TestCoverageTEST("StandardDerived1 operation list length", operationTestCount, info.getOperations().length));

      resetCounters();

      mbean = new StandardDerived2();
      info = InfoUtil.getMBeanInfo(mbean, "test:type=mbeaninfo");

      addConstructorTest(testSuite, info, StandardDerived2.class.getName(), new String[0]);
      testSuite.addTest(new TestCoverageTEST("StandardDerived2 constructor list length", constructorTestCount, info.getConstructors().length));
      addAttributeTest(testSuite, info, "DerivedValue", String.class.getName(), false, true, false);
      addAttributeTest(testSuite, info, "ParentValue", String.class.getName(), true, false, false);
      addSpuriousAttributeTest(testSuite, info, "Available");
      testSuite.addTest(new TestCoverageTEST("StandardDerived2 attribute list length", attributeTestCount, info.getAttributes().length));
      testSuite.addTest(new TestCoverageTEST("StandardDerived2 operation list length", operationTestCount, info.getOperations().length));

      resetCounters();

      mbean = new StandardDerived3();
      info = InfoUtil.getMBeanInfo(mbean, "test:type=mbeaninfo");

      addConstructorTest(testSuite, info, StandardDerived3.class.getName(), new String[0]);
      testSuite.addTest(new TestCoverageTEST("StandardDerived3 constructor list length", constructorTestCount, info.getConstructors().length));
      addAttributeTest(testSuite, info, "ArbitraryValue", String.class.getName(), false, true, false);
      testSuite.addTest(new TestCoverageTEST("StandardDerived3 attribute list length", attributeTestCount, info.getAttributes().length));
      testSuite.addTest(new TestCoverageTEST("StandardDerived3 operation list length", operationTestCount, info.getOperations().length));

      resetCounters();

      mbean = new DynamicDerived1();
      info = InfoUtil.getMBeanInfo(mbean, "test:type=mbeaninfo");

      testSuite.addTest(new TestCoverageTEST("DynamicDerived1 constructor list length", constructorTestCount, info.getConstructors().length));
      testSuite.addTest(new TestCoverageTEST("DynamicDerived1 attribute list length", attributeTestCount, info.getAttributes().length));
      testSuite.addTest(new TestCoverageTEST("DynamicDerived1 operation list length", operationTestCount, info.getOperations().length));

      return testSuite;
   }

   public static void resetCounters()
   {
      constructorTestCount = 0;
      attributeTestCount = 0;
      operationTestCount = 0;
   }

   public static void addConstructorTest(TestSuite testSuite, MBeanInfo info, String name, String[] signature)
   {
      testSuite.addTest(new ConstructorInfoTEST("InheritanceSUITE constructor", info, name, signature));
      constructorTestCount++;
   }

   public static void addSpuriousAttributeTest(TestSuite testSuite, MBeanInfo info, String name)
   {
      testSuite.addTest(new SpuriousAttributeTEST("InheritanceSUITE spuriousAttribute", info, name));
   }

   public static void addAttributeTest(TestSuite testSuite, MBeanInfo info, String name, String type, boolean read, boolean write, boolean is)
   {
      testSuite.addTest(new AttributeInfoTEST("InheritanceSUITE attribute", info, name, type, read, write, is));
      attributeTestCount++;
   }

   public static void addOperationTest(TestSuite testSuite, MBeanInfo info, String name, int impact, String returnType, String[] signature)
   {
      testSuite.addTest(new OperationInfoTEST("InheritanceSUITE operation", info, name, impact, returnType, signature));
      operationTestCount++;
   }

   public static class TestCoverageTEST extends TestCase
   {
      private String msg;
      private int expected;
      private int got;

      public TestCoverageTEST(String msg, int expected, int got)
      {
         super("testAdequateCoverage");
         this.msg = msg;
         this.expected = expected;
         this.got = got;
      }

      public void testAdequateCoverage()
      {
         assertEquals(msg, expected, got);
      }
   }

}
