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
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;
import org.jboss.test.server.profileservice.persistence.support.CollectionMetaData;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 88573 $
 */
public class CollectionValueUnitTestCase extends AbstractPersistenceFormatTest
{

   public CollectionValueUnitTestCase(String name)
   {
      super(name);
   }
   
   public void test() throws Throwable
   {
      ManagedObject mo = initMO();
      PersistedManagedObject moElement = restore(mo);
      assertNotNull(moElement);
      
      ManagedObject restored = update(new CollectionMetaData(), moElement);
      
      assertNotNull(restored);

      assertNotNull(restored.getProperty("collection"));
      
      List<String> restoredList = ((CollectionMetaData) restored.getAttachment()).getCollection(); 
      assertNotNull(restoredList);
      assertEquals(3, restoredList.size());
      assertTrue(restoredList.containsAll(initList()));

   }

   protected CollectionMetaData createMetaData()
   {
      CollectionMetaData instance = new CollectionMetaData();
      instance.setCollection(initList());
      return instance;
   }
   
   protected ManagedObject initMO()
   {
      return getMOF().initManagedObject(createMetaData(), null);
   }
   
   protected List<String> initList()
   {
      List<String> list = new ArrayList<String>();
      list.add("string1");
      list.add("string2");
      list.add("string3");
      return list;
   }
}

