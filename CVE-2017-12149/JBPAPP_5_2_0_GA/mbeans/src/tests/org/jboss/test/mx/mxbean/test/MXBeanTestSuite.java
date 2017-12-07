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
package org.jboss.test.mx.mxbean.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * MXBean Test Suite.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class MXBeanTestSuite extends TestSuite
{
   public static void main(String[] args)
   {
      TestRunner.run(suite());
   }

   public static Test suite()
   {
      TestSuite suite = new TestSuite("MXBean Tests");

      suite.addTest(CompositeDataInvocationHandlerUnitTestCase.suite());
      suite.addTest(CompositeTypeMetaDataFactoryUnitTestCase.suite());
      suite.addTest(CompositeDataBigDecimalUnitTestCase.suite());
      suite.addTest(CompositeDataBigIntegerUnitTestCase.suite());
      suite.addTest(CompositeDataBooleanUnitTestCase.suite());
      suite.addTest(CompositeDataByteUnitTestCase.suite());
      suite.addTest(CompositeDataCharacterUnitTestCase.suite());
      suite.addTest(CompositeDataDateUnitTestCase.suite());
      suite.addTest(CompositeDataDoubleUnitTestCase.suite());
      suite.addTest(CompositeDataFloatUnitTestCase.suite());
      suite.addTest(CompositeDataIntegerUnitTestCase.suite());
      suite.addTest(CompositeDataLongUnitTestCase.suite());
      suite.addTest(CompositeDataObjectNameUnitTestCase.suite());
      suite.addTest(CompositeDataShortUnitTestCase.suite());
      suite.addTest(CompositeDataStringUnitTestCase.suite());
      suite.addTest(CompositeDataEnumUnitTestCase.suite());
      suite.addTest(CompositeDataArrayUnitTestCase.suite());
      suite.addTest(CompositeDataCollectionUnitTestCase.suite());
      suite.addTest(CompositeDataMapUnitTestCase.suite());
      suite.addTest(CompositeDataCompositeUnitTestCase.suite());
      suite.addTest(MXBeanSupportUnitTestCase.suite());
      suite.addTest(MXBeanInvocationHandlerUnitTestCase.suite());
      
      return suite;
   }
}
