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

import org.jboss.test.mx.mxbean.support.TestEnum;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * CompositeDataArrayUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class CompositeDataArrayUnitTestCase extends CompositeDataTest
{
   public static Test suite()
   {
      return new TestSuite(CompositeDataArrayUnitTestCase.class);
   }
   
   public CompositeDataArrayUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testArrayString() throws Exception
   {
      String[] array = { "one", "two", "three" };
      constructReconstructTest(array);
   }
   
   public void testMultiArrayString() throws Exception
   {
      String[][] array = { { "one", "two", "three" }, { "four", "five", "six" } };
      constructReconstructTest(array);
   }
   
   public void testArrayEnum() throws Exception
   {
      TestEnum[] array = { TestEnum.FIRST, TestEnum.SECOND };
      String[] openData = { TestEnum.FIRST.name(), TestEnum.SECOND.name() };
      constructReconstructTest(array, openData);
   }
   
   public void testMultiArrayEnum() throws Exception
   {
      TestEnum[][] array = { { TestEnum.FIRST, TestEnum.SECOND }, { TestEnum.SECOND } };
      String[][] openData = { { TestEnum.FIRST.name(), TestEnum.SECOND.name() }, { TestEnum.SECOND.name() } };
      constructReconstructTest(array, openData);
   }
   
   public void testArrayNull() throws Exception
   {
      constructReconstructTest(null, new String[0].getClass());
   }
   
   public void testArrayElementNull() throws Exception
   {
      TestEnum[] array = { TestEnum.FIRST, null };
      String[] openData = { TestEnum.FIRST.name(), null };
      constructReconstructTest(array, openData);
   }
   
   public void testMultiArrayElementNull() throws Exception
   {
      TestEnum[][] array = { { TestEnum.FIRST, null }, { TestEnum.SECOND } };
      String[][] openData = { { TestEnum.FIRST.name(), null }, { TestEnum.SECOND.name() } };
      constructReconstructTest(array, openData);
   }
   
   public void testMultiArrayNull() throws Exception
   {
      TestEnum[][] array = { { TestEnum.FIRST, TestEnum.SECOND }, null, { TestEnum.SECOND } };
      String[][] openData = { { TestEnum.FIRST.name(), TestEnum.SECOND.name() }, null, { TestEnum.SECOND.name() } };
      constructReconstructTest(array, openData);
   }
   
   public void checkEquals(Object expected, Object actual)
   {
      checkArrayEquals(expected, actual);
   }
}
