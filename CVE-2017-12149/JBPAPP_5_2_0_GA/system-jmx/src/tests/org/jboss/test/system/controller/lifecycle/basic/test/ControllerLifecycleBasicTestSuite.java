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
package org.jboss.test.system.controller.lifecycle.basic.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Controller Lifecycle Basic Test Suite.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class ControllerLifecycleBasicTestSuite extends TestSuite
{
   public static void main(String[] args)
   {
      TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("Controller Lifecycle Basic Tests");

      suite.addTest(BasicLifecycleOldUnitTestCase.suite());
      suite.addTest(BasicLifecycleNewUnitTestCase.suite());
      suite.addTest(BasicDependencyDependsOldUnitTestCase.suite());
      suite.addTest(BasicDependencyDependsNewUnitTestCase.suite());
      suite.addTest(BasicDependencyDependsNestedOldUnitTestCase.suite());
      suite.addTest(BasicDependencyDependsNestedNewUnitTestCase.suite());
      suite.addTest(BasicDependencyDependsListOldUnitTestCase.suite());
      suite.addTest(BasicDependencyDependsListNewUnitTestCase.suite());
      suite.addTest(BasicDependencyDependsListNestedOldUnitTestCase.suite());
      suite.addTest(BasicDependencyDependsListNestedNewUnitTestCase.suite());
      suite.addTest(BasicDependencyDependsOptionalAttributeOldUnitTestCase.suite());
      suite.addTest(BasicDependencyDependsOptionalAttributeNewUnitTestCase.suite());
      suite.addTest(BasicDependencyDependsOptionalAttributeNestedOldUnitTestCase.suite());
      suite.addTest(BasicDependencyDependsOptionalAttributeNestedNewUnitTestCase.suite());
      suite.addTest(BasicDependencyDependsOptionalAttributeListOldUnitTestCase.suite());
      suite.addTest(BasicDependencyDependsOptionalAttributeListNewUnitTestCase.suite());
      suite.addTest(BasicDependencyDependsOptionalAttributeListNestedOldUnitTestCase.suite());
      suite.addTest(BasicDependencyDependsOptionalAttributeListNestedNewUnitTestCase.suite());
      
      return suite;
   }
}
