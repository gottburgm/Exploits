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
package org.jboss.test.system.controller.instantiate.test;

import org.jboss.test.system.controller.instantiate.plain.test.PlainMBeanNewUnitTestCase;
import org.jboss.test.system.controller.instantiate.plain.test.PlainMBeanOldUnitTestCase;
import org.jboss.test.system.controller.instantiate.plain.test.PlainMBeanRedeployAfterErrorNewUnitTestCase;
import org.jboss.test.system.controller.instantiate.plain.test.PlainMBeanRedeployAfterErrorOldUnitTestCase;
import org.jboss.test.system.controller.instantiate.standard.test.StandardMBeanNewUnitTestCase;
import org.jboss.test.system.controller.instantiate.standard.test.StandardMBeanOldUnitTestCase;
import org.jboss.test.system.controller.instantiate.standard.test.StandardMBeanRedeployAfterErrorNewUnitTestCase;
import org.jboss.test.system.controller.instantiate.standard.test.StandardMBeanRedeployAfterErrorOldUnitTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Controller Instantiate Test Suite.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class ControllerInstantiateTestSuite extends TestSuite
{
   public static void main(String[] args)
   {
      TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("Controller Instantiate Tests");

      suite.addTest(NewNoMetaDataInstantiateUnitTestCase.suite());
      suite.addTest(ConstructorArgsOldUnitTestCase.suite());
      suite.addTest(ConstructorArgsNewUnitTestCase.suite());
      suite.addTest(PlainMBeanOldUnitTestCase.suite());
      suite.addTest(PlainMBeanNewUnitTestCase.suite());
      suite.addTest(PlainMBeanRedeployAfterErrorOldUnitTestCase.suite());
      suite.addTest(PlainMBeanRedeployAfterErrorNewUnitTestCase.suite());
      suite.addTest(StandardMBeanOldUnitTestCase.suite());
      suite.addTest(StandardMBeanNewUnitTestCase.suite());
      suite.addTest(StandardMBeanRedeployAfterErrorOldUnitTestCase.suite());
      suite.addTest(StandardMBeanRedeployAfterErrorNewUnitTestCase.suite());
      suite.addTest(ConstructorArgsErrorOldUnitTestCase.suite());
      suite.addTest(ConstructorArgsErrorNewUnitTestCase.suite());
      
      return suite;
   }
}
