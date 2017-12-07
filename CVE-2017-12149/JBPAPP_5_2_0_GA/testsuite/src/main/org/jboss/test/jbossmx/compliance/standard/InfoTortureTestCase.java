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
package org.jboss.test.jbossmx.compliance.standard;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.jbossmx.compliance.TestCase;
import org.jboss.test.jbossmx.compliance.standard.support.Torture;
import org.jboss.util.platform.Java;

/**
 * Beat the heck out of the server's standard MBeanInfo
 *
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>.
 */
public class InfoTortureTestCase 
   extends TestCase
{
   private static int attributeTestCount = 0;
   private static int operationTestCount = 0;
   private static int constructorTestCount = 0;

   public InfoTortureTestCase(String s)
   {
      super(s);
   }

   public static Test suite()
   {
      TestSuite testSuite = new TestSuite("All MBeanInfo Torture Tests for Standard MBeans");

      Object mbean = new Torture();
      MBeanInfo info = InfoUtil.getMBeanInfo(mbean, "test:type=mbeaninfo");

      // Tests for valid constructors
      addConstructorTest(testSuite, info, Torture.class.getName(), new String[0]);
      addConstructorTest(testSuite, info, Torture.class.getName(), new String[] { String[][].class.getName() });

      // make sure we are testing all exposed constructors (each ValidConstructorTest increments a counter
      // which is used to figure out whether we have adequate test coverage)
      testSuite.addTest(new TestCoverageTEST("Torture constructor list length", constructorTestCount, info.getConstructors().length));

      // Tests for attributes that should not be there
      addSpuriousAttributeTest(testSuite, info, "peachy");
      addSpuriousAttributeTest(testSuite, info, "Peachy");
      addSpuriousAttributeTest(testSuite, info, "suer");
      addSpuriousAttributeTest(testSuite, info, "settlement");
      addSpuriousAttributeTest(testSuite, info, "Result");
      addSpuriousAttributeTest(testSuite, info, "Multi");

      // make sure remaining attributes are correct
      // Args are: Name, Type, Readable, Writable, IsIS
      addAttributeTest(testSuite, info, "NiceString", String.class.getName(), true, true, false);

      // JBAS-3746, conditionally exclude this test on JRockit 1.5
      boolean jrockit = System.getProperty("java.vm.name").indexOf("JRockit") > -1;
      if (Java.isVersion(Java.VERSION_1_5) && jrockit)
      {
         // exclude the test but keep the test coverage count
         ++attributeTestCount;
      }
      else
      {
         // include the test
         addAttributeTest(testSuite, info, "NiceBoolean", boolean.class.getName(), true, true, true);
      }
      
      addAttributeTest(testSuite, info, "Something", String.class.getName(), false, true, false);
      addAttributeTest(testSuite, info, "Int", int.class.getName(), false, true, false);
      addAttributeTest(testSuite, info, "IntArray", int[].class.getName(), false, true, false);
      addAttributeTest(testSuite, info, "NestedIntArray", int[][][].class.getName(), false, true, false);
      addAttributeTest(testSuite, info, "Integer", Integer.class.getName(), false, true, false);
      addAttributeTest(testSuite, info, "IntegerArray", Integer[].class.getName(), false, true, false);
      addAttributeTest(testSuite, info, "NestedIntegerArray", Integer[][][].class.getName(), false, true, false);
      addAttributeTest(testSuite, info, "Myinteger", int.class.getName(), true, false, false);
      addAttributeTest(testSuite, info, "MyintegerArray", int[].class.getName(), true, false, false);
      addAttributeTest(testSuite, info, "MyNestedintegerArray", int[][][].class.getName(), true, false, false);
      addAttributeTest(testSuite, info, "MyInteger", Integer.class.getName(), true, false, false);
      addAttributeTest(testSuite, info, "MyIntegerArray", Integer[].class.getName(), true, false, false);
      addAttributeTest(testSuite, info, "MyNestedIntegerArray", Integer[][][].class.getName(), true, false, false);
      addAttributeTest(testSuite, info, "ready", boolean.class.getName(), true, false, true);

      // this is *not* an attribute, it must appear as an operation
      //addAttributeTest(testSuite, info, "Ready", Boolean.class.getName(), true, false, true);

      // make sure we are testing all exposed attributes (each ValidAttributeTest increments a counter
      // which is used to figure out whether we have adequate test coverage)
      testSuite.addTest(new TestCoverageTEST("Torture attribute list length", attributeTestCount, info.getAttributes().length));

      // validate the operations
      // Args are: Name, impact, returnTypeString, SignatureAsStringArray
      addOperationTest(testSuite, info, "settlement", MBeanOperationInfo.UNKNOWN, int.class.getName(), new String[] { String.class.getName() });
      addOperationTest(testSuite, info, "getSomething", MBeanOperationInfo.UNKNOWN, Void.TYPE.getName(), new String[0]);
      addOperationTest(testSuite, info, "ispeachy", MBeanOperationInfo.UNKNOWN, boolean.class.getName(), new String[] { int.class.getName() });
      addOperationTest(testSuite, info, "isPeachy", MBeanOperationInfo.UNKNOWN, Boolean.class.getName(), new String[] { int.class.getName() });
      addOperationTest(testSuite, info, "setMulti", MBeanOperationInfo.UNKNOWN, Void.TYPE.getName(), new String[] { String.class.getName(), Integer.class.getName() });
      addOperationTest(testSuite, info, "getResult", MBeanOperationInfo.UNKNOWN, String.class.getName(), new String[] { String.class.getName() });
      addOperationTest(testSuite, info, "setNothing", MBeanOperationInfo.UNKNOWN, Void.TYPE.getName(), new String[0]);
      addOperationTest(testSuite, info, "getNothing", MBeanOperationInfo.UNKNOWN, Void.TYPE.getName(), new String[0]);
      addOperationTest(testSuite, info, "doSomethingCrazy", MBeanOperationInfo.UNKNOWN, String[][].class.getName(), new String[] { Object[].class.getName(), String[].class.getName(), int[][][].class.getName() });
      // Hmmm... This fails in the RI (which causes the operation coverage test to fail too.
      // it's odd because in the RI issuer() isn't treated as an attribute and it doesn't
      // appear as an operation - it just disappears!
      addOperationTest(testSuite, info, "issuer", MBeanOperationInfo.UNKNOWN, String.class.getName(), new String[0]);

      // this must appear as an operation, not an attribute
      addOperationTest(testSuite, info, "isReady", MBeanOperationInfo.UNKNOWN, Boolean.class.getName(), new String[0]);
      
      // make sure we are testing all exposed operations (each ValidOperationTest increments a counter
      // which is used to figure out whether we have adequate test coverage)
      testSuite.addTest(new TestCoverageTEST("Torture operation list length", operationTestCount, info.getOperations().length));

      return testSuite;
   }

   public static void addConstructorTest(TestSuite testSuite, MBeanInfo info, String name, String[] signature)
   {
      testSuite.addTest(new ConstructorInfoTEST("InfoTortureSUITE constructor", info, name, signature));
      constructorTestCount++;
   }

   public static void addSpuriousAttributeTest(TestSuite testSuite, MBeanInfo info, String name)
   {
      testSuite.addTest(new SpuriousAttributeTEST("InfoTortureSUITE spuriousAttribute", info, name));
   }

   public static void addAttributeTest(TestSuite testSuite, MBeanInfo info, String name, String type, boolean read, boolean write, boolean is)
   {
      testSuite.addTest(new AttributeInfoTEST("InfoTortureSUITE attribute", info, name, type, read, write, is));
      attributeTestCount++;
   }

   public static void addOperationTest(TestSuite testSuite, MBeanInfo info, String name, int impact, String returnType, String[] signature)
   {
      testSuite.addTest(new OperationInfoTEST("InfoTortureSUITE operation", info, name, impact, returnType, signature));
      operationTestCount++;
   }

   public static class TestCoverageTEST
      extends TestCase
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
