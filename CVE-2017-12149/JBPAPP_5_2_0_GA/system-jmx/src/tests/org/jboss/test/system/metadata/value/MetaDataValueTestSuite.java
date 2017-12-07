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
package org.jboss.test.system.metadata.value;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jboss.test.system.metadata.value.depends.test.DependencyValueUnitTestCase;
import org.jboss.test.system.metadata.value.dependslist.test.DependencyListValueUnitTestCase;
import org.jboss.test.system.metadata.value.element.test.ElementValueUnitTestCase;
import org.jboss.test.system.metadata.value.inject.test.InjectionValueUnitTestCase;
import org.jboss.test.system.metadata.value.javabean.test.JavaBeanValueUnitTestCase;
import org.jboss.test.system.metadata.value.jbxb.test.JBXBValueUnitTestCase;
import org.jboss.test.system.metadata.value.text.test.TextValueUnitTestCase;
import org.jboss.test.system.metadata.value.alias.test.AliasValueUnitTestCase;

/**
 * MetaData Value Test Suite.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class MetaDataValueTestSuite extends TestSuite
{
   public static void main(String[] args)
   {
      TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("MetaData Value Tests");

      suite.addTest(new TestSuite(TextValueUnitTestCase.class));
      suite.addTest(new TestSuite(DependencyValueUnitTestCase.class));
      suite.addTest(new TestSuite(DependencyListValueUnitTestCase.class));
      suite.addTest(new TestSuite(ElementValueUnitTestCase.class));
      suite.addTest(new TestSuite(JavaBeanValueUnitTestCase.class));
      suite.addTest(new TestSuite(JBXBValueUnitTestCase.class));
      suite.addTest(new TestSuite(InjectionValueUnitTestCase.class));
      suite.addTest(new TestSuite(AliasValueUnitTestCase.class));

      return suite;
   }
}
