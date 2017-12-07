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

import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.test.system.metadata.test.AbstractMetaDataTest;

/**
 * DependsOptionalAttributeUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class DependsOptionalAttributeUnitTestCase extends AbstractMetaDataTest
{
   public DependsOptionalAttributeUnitTestCase(String name)
   {
      super(name);
   }

   public void testDependsOptionalAttribute() throws Exception
   {
      dependsOne();
   }

   public void testDependsOptionalAttributeNestedMBean() throws Exception
   {
      dependsOne();
   }

   public void testDependsListOptionalAttribute() throws Exception
   {
      dependsOne();
   }

   public void testDependsListOptionalAttributeNestedMBean() throws Exception
   {
      dependsOne();
   }

   protected void dependsOne() throws Exception
   {
      ServiceMetaData metaData = unmarshalSingleMBean();
      assertAttribute(metaData, "AttributeName");
      assertOthers(metaData);
   }

   protected void dependsTwo() throws Exception
   {
      ServiceMetaData metaData = unmarshalSingleMBean();
      assertAttributes(metaData, new String[] { "AttributeName1", "AttributeName2" });
      assertOthers(metaData);
   }

   protected void assertOthers(ServiceMetaData metaData) throws Exception
   {
      assertEquals(testBasicMBeanName, metaData.getObjectName());
      assertEquals(testBasicMBeanCode, metaData.getCode());
      assertNull(metaData.getInterfaceName());
      assertDefaultConstructor(metaData);
      assertNoDependencies(metaData);
      assertNoXMBean(metaData);
   }
}
