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
package org.jboss.test.system.metadata.attributes.test;

import java.util.List;

import org.jboss.system.metadata.ServiceAttributeMetaData;
import org.jboss.system.metadata.ServiceMetaData;

/**
 * AttributeUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class AttributeUnitTestCase extends AbstractAttributeTest
{
   public AttributeUnitTestCase(String name)
   {
      super(name);
   }

   public void testAttributeNone() throws Exception
   {
      noAttributes();
   }

   public void testAttributeOne() throws Exception
   {
      ServiceAttributeMetaData attribute = unmarshallSingleAttribute();
      assertTextAttribute(attribute);
   }

   public void testAttributeTwo() throws Exception
   {
      List<ServiceAttributeMetaData> attributes = unmarshallMultipleAttributes(2);
      ServiceAttributeMetaData test = attributes.get(0);
      assertTextAttribute(test, "Attribute1", "value1");
      test = attributes.get(1);
      assertTextAttribute(test, "Attribute2", "value2");
   }

   public void testAttributeNoValue() throws Exception
   {
      noAttributes();
   }

   public void testAttributeEmptyValue() throws Exception
   {
      noAttributes();
   }

   public void testAttributeNoTrim() throws Exception
   {
      ServiceAttributeMetaData attribute = unmarshallSingleAttribute();
      assertTextAttribute(attribute, false, true);
   }

   public void testAttributeNoReplace() throws Exception
   {
      ServiceAttributeMetaData attribute = unmarshallSingleAttribute();
      assertTextAttribute(attribute, true, false);
   }

   protected void assertOthers(ServiceMetaData metaData) throws Exception
   {
      super.assertOthers(metaData);
      assertNoDependencies(metaData);
   }
}
