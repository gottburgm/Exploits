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
package org.jboss.test.system.controller.configure.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jboss.test.system.controller.configure.attribute.test.AttributeNewUnitTestCase;
import org.jboss.test.system.controller.configure.attribute.test.AttributeOldUnitTestCase;
import org.jboss.test.system.controller.configure.attribute.test.DependsAttributeNewUnitTestCase;
import org.jboss.test.system.controller.configure.attribute.test.DependsAttributeOldUnitTestCase;
import org.jboss.test.system.controller.configure.attribute.test.DependsListAttributeNewUnitTestCase;
import org.jboss.test.system.controller.configure.attribute.test.DependsListAttributeOldUnitTestCase;
import org.jboss.test.system.controller.configure.value.ControllerConfigureValueTestSuite;

/**
 * Controller Configure Test Suite.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class ControllerConfigureTestSuite extends TestSuite
{
   public static void main(String[] args)
   {
      TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("Controller Configure Tests");

      suite.addTest(AttributeOldUnitTestCase.suite());
      suite.addTest(AttributeNewUnitTestCase.suite());
      suite.addTest(DependsAttributeOldUnitTestCase.suite());
      suite.addTest(DependsAttributeNewUnitTestCase.suite());
      suite.addTest(DependsListAttributeOldUnitTestCase.suite());
      suite.addTest(DependsListAttributeNewUnitTestCase.suite());
      suite.addTest(ControllerConfigureValueTestSuite.suite());
      suite.addTest(ConfigureRedeployAfterErrorOldUnitTestCase.suite());
      suite.addTest(ConfigureRedeployAfterErrorNewUnitTestCase.suite());
      
      return suite;
   }
}
