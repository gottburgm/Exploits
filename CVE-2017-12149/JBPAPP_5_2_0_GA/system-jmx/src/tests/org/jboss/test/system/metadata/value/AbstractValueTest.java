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

import java.util.List;

import org.jboss.system.metadata.ServiceAttributeMetaData;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.metadata.ServiceValueMetaData;
import org.jboss.test.system.metadata.test.AbstractMetaDataTest;

/**
 * AbstractValueTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class AbstractValueTest extends AbstractMetaDataTest
{
   public AbstractValueTest(String name)
   {
      super(name);
   }

   protected ServiceAttributeMetaData unmarshallSingleAttribute() throws Exception
   {
      ServiceMetaData metaData = unmarshalSingleMBean();
      assertOthers(metaData);
      List<ServiceAttributeMetaData> attributes = metaData.getAttributes();
      assertNotNull(attributes);
      assertEquals(1, attributes.size());
      return attributes.get(0);
   }

   protected ServiceValueMetaData unmarshallSingleValue() throws Exception
   {
      ServiceMetaData metaData = unmarshalSingleMBean();
      assertOthers(metaData);
      List<ServiceAttributeMetaData> attributes = metaData.getAttributes();
      assertNotNull(attributes);
      assertEquals(1, attributes.size());
      ServiceAttributeMetaData attribute = attributes.get(0);
      assertAttributeName(attribute, "Attribute");
      ServiceValueMetaData result = attribute.getValue();
      assertNotNull(result);
      return result;
   }

   protected void assertOthers(ServiceMetaData metaData) throws Exception
   {
      assertEquals(testBasicMBeanName, metaData.getObjectName());
      assertEquals(testBasicMBeanCode, metaData.getCode());
      assertNull(metaData.getInterfaceName());
      assertDefaultConstructor(metaData);
      assertNoXMBean(metaData);
   }
}
