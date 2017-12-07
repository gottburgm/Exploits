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

/**
 * DependsAttributeUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class DependsAttributeUnitTestCase extends AbstractAttributeTest
{
   public DependsAttributeUnitTestCase(String name)
   {
      super(name);
   }

   public void testDependsAttributeNone() throws Exception
   {
      noAttributes();
   }

   public void testDependsAttributeOne() throws Exception
   {
      ServiceAttributeMetaData attribute = unmarshallSingleAttribute();
      assertDependsAttribute(attribute);
   }

   public void testDependsAttributeTwo() throws Exception
   {
      List<ServiceAttributeMetaData> attributes = unmarshallMultipleAttributes(2);
      ServiceAttributeMetaData test = attributes.get(0);
      assertDependsAttribute(test, "Attribute1", TEST1);
      test = attributes.get(1);
      assertDependsAttribute(test, "Attribute2", TEST2);
   }

   public void testDependsAttributeNoName() throws Exception
   {
      // REVIEW shouldn't this be an error?
      noAttributes();
   }
}
