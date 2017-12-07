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

import java.util.HashMap;
import java.util.Map;

import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;
import org.jboss.test.server.profileservice.persistence.support.SimpleStringMapMetaData;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 88573 $
 */
public class MapCompositeUnitTestCase extends AbstractPersistenceFormatTest
{

   public MapCompositeUnitTestCase(String name)
   {
      super(name);
   }

   public void test() throws Throwable
   {
      ManagedObject mo = initMO();
      
      ManagedProperty p = mo.getProperty("map");
      assertNotNull(p);
      MapCompositeValueSupport value = (MapCompositeValueSupport) p.getValue();
      assertNotNull(value);
      assertNotNull(value.get("test1"));
      getLog().debug("keys: " + value.getMetaType().itemSet());
      
      enableTrace("org.jboss.system");
      PersistedManagedObject moElement = restore(mo);
      assertNotNull(moElement);
      
      ManagedObject restored = update(new SimpleStringMapMetaData(), moElement);
      assertNotNull(restored);
      SimpleStringMapMetaData metadata = (SimpleStringMapMetaData) restored.getAttachment();
      assertTrue(metadata.getMap().equals(initMap()));
   }
   
   protected SimpleStringMapMetaData createTestMetaData()
   {
      SimpleStringMapMetaData instance = new SimpleStringMapMetaData();
      instance.setMap(initMap());
      return instance;
   }
   
   protected ManagedObject initMO()
   {
      return getMOF().initManagedObject(createTestMetaData(), null);
   }
   
   protected Map<String, String> initMap()
   {
      Map<String, String> map = new HashMap<String, String>();
      map.put("test1", "one");
      map.put("test2", "two");
      map.put("test3", "three");
      return map;
   }
   
}

