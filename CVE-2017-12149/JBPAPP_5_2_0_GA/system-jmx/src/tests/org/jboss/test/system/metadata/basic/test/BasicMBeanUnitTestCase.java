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
package org.jboss.test.system.metadata.basic.test;

import javax.management.MalformedObjectNameException;

import org.jboss.dependency.spi.ControllerMode;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.test.system.metadata.test.AbstractMetaDataTest;

/**
 * BasicMBeanUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class BasicMBeanUnitTestCase extends AbstractMetaDataTest
{
   public BasicMBeanUnitTestCase(String name)
   {
      super(name);
   }

   public void testBasicMBean() throws Exception
   {
      ServiceMetaData metaData = unmarshalSingleMBean();
      assertEquals(testBasicMBeanName, metaData.getObjectName());
      assertEquals(testBasicMBeanCode, metaData.getCode());
      assertNull(metaData.getInterfaceName());
      assertNull(metaData.getMode());
      assertOthers(metaData);
   }

   public void testBasicMBeanNoName() throws Exception
   {
      assertFailUnmarshal(RuntimeException.class);
   }

   public void testBasicMBeanEmptyName() throws Exception
   {
      assertFailUnmarshal(RuntimeException.class);
   }

   public void testBasicMBeanInvalidName() throws Exception
   {
      assertFailUnmarshal(MalformedObjectNameException.class);
   }

   public void testBasicMBeanInterface() throws Exception
   {
      ServiceMetaData metaData = unmarshalSingleMBean();
      assertEquals(testBasicMBeanName, metaData.getObjectName());
      assertEquals(testBasicMBeanCode, metaData.getCode());
      assertEquals(testBasicMBeanInterface, metaData.getInterfaceName());
      assertNull(metaData.getMode());
      assertOthers(metaData);
   }

   public void testBasicMBeanMode() throws Exception
   {
      ServiceMetaData metaData = unmarshalSingleMBean();
      assertEquals(testBasicMBeanName, metaData.getObjectName());
      assertEquals(testBasicMBeanCode, metaData.getCode());
      assertEquals(ControllerMode.ON_DEMAND, metaData.getMode());
      assertOthers(metaData);
   }
   
   protected void assertOthers(ServiceMetaData metaData) throws Exception
   {
      assertDefaultConstructor(metaData);
      assertNoAttributes(metaData);
      assertNoDependencies(metaData);
      assertNoXMBean(metaData);
   }
}
