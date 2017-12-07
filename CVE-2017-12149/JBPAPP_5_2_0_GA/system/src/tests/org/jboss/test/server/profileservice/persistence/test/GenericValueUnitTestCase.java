/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.server.profileservice.persistence.test;

import java.util.ArrayList;
import java.util.List;

import org.jboss.managed.api.ManagedObject;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.GenericValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;
import org.jboss.test.server.profileservice.persistence.support.GenericSupportMetaData;
import org.jboss.test.server.profileservice.persistence.support.SimpleGenericMetaData;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 88716 $
 */
public class GenericValueUnitTestCase extends AbstractPersistenceFormatTest
{

   public GenericValueUnitTestCase(String name)
   {
      super(name);
   }

   public void test() throws Throwable
   {
      ManagedObject mo = createTestMO();

      CollectionValue collection = (CollectionValue) mo.getProperty("list").getValue();
      ManagedObject child2 = (ManagedObject) ((GenericValue) collection.getElements()[1]).getValue();
      child2.getProperty("string").setValue(SimpleValueSupport.wrap("changedName"));
      // TODO test generic array
      
      PersistedManagedObject moElement = restore(mo);

      ManagedObject restored = update(new GenericSupportMetaData(), moElement);

      GenericSupportMetaData md = (GenericSupportMetaData) restored.getAttachment();
      assertNotNull(md);
      
      SimpleGenericMetaData child = md.getList().get(1);
      assertEquals(child.getString(), "changedName");
   }

   private ManagedObject createTestMO()
   {
      return getMOF().initManagedObject(createTestMetaData(), null);
   }

   private GenericSupportMetaData createTestMetaData()
   {
      GenericSupportMetaData metaData = new GenericSupportMetaData();

      List<SimpleGenericMetaData> list = new ArrayList<SimpleGenericMetaData>();

      SimpleGenericMetaData child1 = createSImple("child1", 11, null);
      SimpleGenericMetaData child2 = createSImple("child2", 21, null);

      list.add(createSImple("parent1", 1, child1));
      list.add(createSImple("parent2", 2, child2));

      metaData.setList(list);

      return metaData;
   }
   
   protected SimpleGenericMetaData[][] createArray()
   {

      SimpleGenericMetaData child1 = createSImple("array11", 11, null);
      SimpleGenericMetaData child2 = createSImple("array12", 12, null);
      SimpleGenericMetaData child3 = createSImple("array21", 21, null);
      SimpleGenericMetaData child4 = createSImple("array22", 22, null);      
      return new SimpleGenericMetaData[][] {{child1, child2}, {child3, child4}};
   }

   private SimpleGenericMetaData createSImple(String string, int integer, SimpleGenericMetaData child)
   {
      return new SimpleGenericMetaData(string, integer, child);
   }
}
