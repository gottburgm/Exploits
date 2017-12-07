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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.types.TableMetaType;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.SimpleValueSupport;
import org.jboss.metatype.api.values.TableValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;
import org.jboss.test.server.profileservice.persistence.support.SimpleTableMetaData;


/**
 * Simple attachment persistence test for table values.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 88573 $
 */
public class TableValueUnitTestCase extends AbstractPersistenceFormatTest
{

   public TableValueUnitTestCase(String name)
   {
      super(name);
   }

   public void testSimpleTableValue() throws Throwable
   {
      ManagedObject mo = initMO();
      assertNotNull(mo);

      // Restore
      PersistedManagedObject moElement = restore(mo);
      assertNotNull(moElement);
      assertNotNull(moElement.getProperties());

      // Recreate MO
      ManagedObject restored = update(new SimpleTableMetaData(), moElement);

      // Assert
      ManagedProperty p = restored.getProperty("map");
      assertTrue(p.getValue() instanceof TableValue);
      assertTrue(p.getMetaType() instanceof TableMetaType);
      
      TableValue table = (TableValue) p.getValue();
      assertRow(table, 1, "one");
      assertRow(table, 2, "two");
      assertRow(table, 3, "three");
      
      SimpleTableMetaData metaData = (SimpleTableMetaData) restored.getAttachment();
      assertTrue(metaData.getMap().equals(initMap()));
   }
   
   protected void assertRow(TableValue table, Serializable key, Serializable value)
   {
      CompositeValue v = (CompositeValue) table.get(new MetaValue[] { SimpleValueSupport.wrap(key) });
      assertNotNull(v);
      // Assert value
      SimpleValue simple = (SimpleValue) v.get("value");
      assertNotNull(simple);
      assertEquals(value, simple.getValue());      
   }
   
   protected SimpleTableMetaData createTestMetaData()
   {
      SimpleTableMetaData t = new SimpleTableMetaData();
      t.setMap(initMap());
      return t;
   }

   protected ManagedObject initMO()
   {
      return getMOF().initManagedObject(createTestMetaData(), null);
   }
   
   protected Map<Integer, String> initMap()
   {
      Map<Integer, String> map = new HashMap<Integer, String>();
      map.put(1, "one");
      map.put(2, "two");
      map.put(3, "three");
      return map;
   }
   
}
