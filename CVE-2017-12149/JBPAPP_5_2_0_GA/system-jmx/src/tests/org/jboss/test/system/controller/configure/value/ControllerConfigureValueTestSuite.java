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
package org.jboss.test.system.controller.configure.value;

import org.jboss.test.system.controller.configure.value.depends.test.DependencyValueNewUnitTestCase;
import org.jboss.test.system.controller.configure.value.depends.test.DependencyValueOldUnitTestCase;
import org.jboss.test.system.controller.configure.value.dependslist.test.DependencyListValueNewUnitTestCase;
import org.jboss.test.system.controller.configure.value.dependslist.test.DependencyListValueOldUnitTestCase;
import org.jboss.test.system.controller.configure.value.element.test.ElementValueNewUnitTestCase;
import org.jboss.test.system.controller.configure.value.element.test.ElementValueOldUnitTestCase;
import org.jboss.test.system.controller.configure.value.inject.test.NewInjectionValueUnitTestCase;
import org.jboss.test.system.controller.configure.value.javabean.test.JavaBeanValueNewUnitTestCase;
import org.jboss.test.system.controller.configure.value.javabean.test.JavaBeanValueOldUnitTestCase;
import org.jboss.test.system.controller.configure.value.jbxb.test.JBXBValueNewUnitTestCase;
import org.jboss.test.system.controller.configure.value.jbxb.test.JBXBValueOldUnitTestCase;
import org.jboss.test.system.controller.configure.value.text.test.TextValueNewUnitTestCase;
import org.jboss.test.system.controller.configure.value.text.test.TextValueOldUnitTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Controller Configure Value Test Suite.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class ControllerConfigureValueTestSuite extends TestSuite
{
   public static void main(String[] args)
   {
      TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("Controller Configure Value Tests");

      suite.addTest(TextValueOldUnitTestCase.suite());
      suite.addTest(TextValueNewUnitTestCase.suite());
      suite.addTest(DependencyValueOldUnitTestCase.suite());
      suite.addTest(DependencyValueNewUnitTestCase.suite());
      suite.addTest(DependencyListValueOldUnitTestCase.suite());
      suite.addTest(DependencyListValueNewUnitTestCase.suite());
      suite.addTest(ElementValueOldUnitTestCase.suite());
      suite.addTest(ElementValueNewUnitTestCase.suite());
      suite.addTest(JavaBeanValueOldUnitTestCase.suite());
      suite.addTest(JavaBeanValueNewUnitTestCase.suite());
      suite.addTest(JBXBValueOldUnitTestCase.suite());
      suite.addTest(JBXBValueNewUnitTestCase.suite());
      suite.addTest(NewInjectionValueUnitTestCase.suite());
      
      return suite;
   }
}
