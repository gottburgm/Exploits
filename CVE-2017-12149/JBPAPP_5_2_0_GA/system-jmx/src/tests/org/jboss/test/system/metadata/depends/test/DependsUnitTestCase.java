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
package org.jboss.test.system.metadata.depends.test;

import javax.management.ObjectName;

import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.test.system.metadata.test.AbstractMetaDataTest;

/**
 * DependsUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class DependsUnitTestCase extends AbstractMetaDataTest
{
   public DependsUnitTestCase(String name)
   {
      super(name);
   }

   public void testDependsNone() throws Exception
   {
      dependsNone();
   }

   public void testDependsOne() throws Exception
   {
      dependsOne();
   }

   public void testDependsTwo() throws Exception
   {
      dependsTwo();
   }

   public void testDependsNestedMBean() throws Exception
   {
      dependsOne();
   }

   // @review shouldn't an empty depends-list be an error?
   public void testDependsListNone() throws Exception
   {
      dependsNone();
   }

   public void testDependsListOne() throws Exception
   {
      dependsOne();
   }

   public void testDependsListTwo() throws Exception
   {
      dependsTwo();
   }

   public void testDependsListNestedMBean() throws Exception
   {
      dependsOne();
   }

   protected void dependsNone() throws Exception
   {
      ServiceMetaData metaData = unmarshalSingleMBean();
      assertNoDependencies(metaData);
      assertOthers(metaData);
   }

   protected void dependsOne() throws Exception
   {
      ServiceMetaData metaData = unmarshalSingleMBean();
      ObjectName[] expected = { TEST1 };
      assertDependencies(metaData, expected);
      assertOthers(metaData);
   }

   protected void dependsTwo() throws Exception
   {
      ServiceMetaData metaData = unmarshalSingleMBean();
      ObjectName[] expected = { TEST1, TEST2 };
      assertDependencies(metaData, expected);
      assertOthers(metaData);
   }

   protected void assertOthers(ServiceMetaData metaData) throws Exception
   {
      assertEquals(testBasicMBeanName, metaData.getObjectName());
      assertEquals(testBasicMBeanCode, metaData.getCode());
      assertNull(metaData.getInterfaceName());
      assertDefaultConstructor(metaData);
      assertNoAttributes(metaData);
      assertNoXMBean(metaData);
   }
}
