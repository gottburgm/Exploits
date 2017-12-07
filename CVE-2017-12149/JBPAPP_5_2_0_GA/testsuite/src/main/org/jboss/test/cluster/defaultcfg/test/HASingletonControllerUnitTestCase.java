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
package org.jboss.test.cluster.defaultcfg.test;

import javax.management.ObjectName;

import junit.framework.TestCase;

import org.jboss.test.cluster.hasingleton.HASingletonControllerTester;

/**
 * HASingletonController tests
 * 
 * @author  Ivelin Ivanov <ivelin@jboss.org>
 * @version $Revision: 85945 $
 */
public class HASingletonControllerUnitTestCase extends TestCase
{
   private HASingletonControllerTester singletonControllerTester = null;

   public HASingletonControllerUnitTestCase(String name)
   {
      super(name);
   }

   public void setUp()
   {
      singletonControllerTester = new HASingletonControllerTester();
   }


   public void tearDown()
   {
      singletonControllerTester = null;
   }

   public void testSetValidTargetName() throws Exception
   {
      ObjectName someSingletonService = new ObjectName("jboss:service=HASingletonMBeanExample");
      singletonControllerTester.setTargetName(someSingletonService);

      assertEquals("setTargetName() failed", singletonControllerTester.getTargetName(), someSingletonService);
   }

   public void testSetTargetStartMethod()
   {
      String someMethod = "startTheSingleton";
      singletonControllerTester.setTargetStartMethod(someMethod);

      assertEquals("setTargetStartMethod() failed", singletonControllerTester.getTargetStartMethod(), someMethod);
   }

   public void testSetTargetStartMethodArgument()
   {
       String someArgument = "aStartValue";
       singletonControllerTester.setTargetStartMethodArgument(someArgument);
  
       assertEquals("setTargetStartMethodArgument() failed", singletonControllerTester.getTargetStartMethodArgument(), someArgument);
   }

   public void testSetTargetStopMethodArgument()
   {
      String someArgument = "aSopValue";
      singletonControllerTester.setTargetStopMethodArgument(someArgument);
  
      assertEquals("setTargetStopMethodArgument() failed", singletonControllerTester.getTargetStopMethodArgument(), someArgument);
   } 

   /* HASingletonController can now start without a target/method set.
    * We can use the produced notifications to start/stop other services
    * by setting a dependency on a Barrier mbean, see JBAS-2626
   public void testSetNullOrBlankStartTargetName()
   {
      String someMethod = "";
      singletonControllerTester.setTargetStartMethod(someMethod);

      assertEquals("setTargetStartMethod() failed to set default value", singletonControllerTester.getTargetStartMethod(), "startSingleton");

      someMethod = null;
      singletonControllerTester.setTargetStartMethod(someMethod);

      assertEquals("setTargetStartMethod() failed to set default value", singletonControllerTester.getTargetStartMethod(), "startSingleton");
   }
   */

   public void testSetTargetStopMethod()
   {
      String someMethod = "stopTheSingleton";
      singletonControllerTester.setTargetStopMethod(someMethod);

      assertEquals("setTargetStartMethod() failed", singletonControllerTester.getTargetStopMethod(), someMethod);
   }

   /* HASingletonController can now start without a target/method set.
    * We can use the produced notifications to start/stop other services
    * by setting a dependency on a Barrier mbean, see JBAS-2626
   public void testSetNullOrBlankStopTargetName()
   {
      String someMethod = "";
      singletonControllerTester.setTargetStopMethod(someMethod);

      assertEquals("setTargetStartMethod() failed to set default value", singletonControllerTester.getTargetStopMethod(), "stopSingleton");

      someMethod = null;
      singletonControllerTester.setTargetStopMethod(someMethod);

      assertEquals("setTargetStartMethod() failed to set default value", singletonControllerTester.getTargetStopMethod(), "stopSingleton");
   }
   */

   public void testStartSingleton() throws Exception
   {
      ObjectName serviceName = new ObjectName("jboss:service=HASingletonMBeanExample");
      singletonControllerTester.setTargetName(serviceName);
      singletonControllerTester.setTargetStartMethod("startTheSingleton");

      singletonControllerTester.startSingleton();

      assertEquals("method not invoked as expected",
         singletonControllerTester.__invokationStack__.pop(), "invokeMBeanMethod:jboss:service=HASingletonMBeanExample.startTheSingleton");
   }

   public void testStopSingleton() throws Exception
   {
      ObjectName serviceName = new ObjectName("jboss:service=HASingletonMBeanExample");
      singletonControllerTester.setTargetName(serviceName);
      singletonControllerTester.setTargetStopMethod("stopTheSingleton");

      singletonControllerTester.stopSingleton();

      assertEquals("method not invoked as expected",
         singletonControllerTester.__invokationStack__.pop(), "invokeMBeanMethod:jboss:service=HASingletonMBeanExample.stopTheSingleton");
   }

}
