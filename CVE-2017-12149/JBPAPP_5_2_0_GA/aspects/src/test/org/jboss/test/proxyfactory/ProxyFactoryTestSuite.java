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
package org.jboss.test.proxyfactory;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jboss.test.proxyfactory.test.DataSourceTestCase;
import org.jboss.test.proxyfactory.test.HollowInterfaceAndMixinTestCase;
import org.jboss.test.proxyfactory.test.HollowTestCase;
import org.jboss.test.proxyfactory.test.HollowWithMixinTestCase;
import org.jboss.test.proxyfactory.test.InstanceAdvisedTestCase;
import org.jboss.test.proxyfactory.test.InterceptedMixinTestCase;
import org.jboss.test.proxyfactory.test.InterfaceAndMixinTestCase;
import org.jboss.test.proxyfactory.test.NonInterfaceAdvisedTestCase;
import org.jboss.test.proxyfactory.test.NonInterfaceNotAdvisedTestCase;
import org.jboss.test.proxyfactory.test.ObjectTestCase;
import org.jboss.test.proxyfactory.test.ProxyCacheTestCase;
import org.jboss.test.proxyfactory.test.SerializableTestCase;
import org.jboss.test.proxyfactory.test.SimpleMetaDataTestCase;
import org.jboss.test.proxyfactory.test.SimpleMixinTestCase;
import org.jboss.test.proxyfactory.test.SimpleTestCase;
import org.jboss.test.proxyfactory.test.TaggingInterfaceTestCase;

/**
 * ProxyFactory Test Suite.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 80997 $
 */
public class ProxyFactoryTestSuite extends TestSuite
{
   public static void main(String[] args)
   {
      TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("ProxyFactory Tests");

      suite.addTest(SimpleTestCase.suite());
      suite.addTest(ProxyCacheTestCase.suite());
      suite.addTest(NonInterfaceAdvisedTestCase.suite());
      suite.addTest(NonInterfaceNotAdvisedTestCase.suite());
      suite.addTest(TaggingInterfaceTestCase.suite());
      suite.addTest(InstanceAdvisedTestCase.suite());
      suite.addTest(SimpleMetaDataTestCase.suite());
      suite.addTest(SimpleMixinTestCase.suite());
      suite.addTest(InterceptedMixinTestCase.suite());
      suite.addTest(HollowWithMixinTestCase.suite());
      suite.addTest(InterfaceAndMixinTestCase.suite());
      suite.addTest(HollowInterfaceAndMixinTestCase.suite());
      suite.addTest(ObjectTestCase.suite());
      suite.addTest(HollowTestCase.suite());
      suite.addTest(SerializableTestCase.suite());
      suite.addTest(DataSourceTestCase.suite());
      
      return suite;
   }
}
