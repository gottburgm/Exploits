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
package org.jboss.test.server.profileservice.persistence.test;

import javax.management.ObjectName;

import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.PropertiesMetaValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedCompositeValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;
import org.jboss.system.server.profileservice.persistence.xml.PersistedPropertiesValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedProperty;
import org.jboss.system.server.profileservice.persistence.xml.PersistedSimpleValue;
import org.jboss.test.server.profileservice.persistence.support.ObjectNameMetaData;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 88573 $
 */
public class ObjectNameUnitTestCase extends AbstractPersistenceFormatTest
{

   public ObjectNameUnitTestCase(String name)
   {
      super(name);
   }
   
   public void test() throws Throwable
   {
      ManagedObject mo = getMOF().initManagedObject(createMD(), null);
      assertNotNull(mo);
      
      ManagedProperty p = mo.getProperty("name");
      CompositeValue c = (CompositeValue) p.getValue();
      assertNotNull(c);
      PropertiesMetaValue properties = (PropertiesMetaValue) c.get("keyPropertyList");
      properties.put("v", "value5");

      // Assert xml information
      PersistedManagedObject restoredElement = restore(mo);
      assertNotNull(restoredElement);
      
      
      // The objectName
      PersistedProperty pp = restoredElement.getProperties().get(1);
      assertNotNull(pp);
      
      PersistedCompositeValue pcv = (PersistedCompositeValue) pp.getValue();
      assertNotNull(pcv);
      
      PersistedSimpleValue psv = (PersistedSimpleValue) pcv.getValues().get(0);
      assertEquals("domain", psv.getName());
      assertEquals("org.jboss", psv.getValue());
      
      PersistedPropertiesValue ppv = (PersistedPropertiesValue) pcv.getValues().get(1); 
      assertEquals("keyPropertyList", ppv.getName());
      assertEquals(2, ppv.getEntries().size());
      
      // The other object name
      PersistedProperty po = restoredElement.getProperties().get(0);
      assertNotNull(po);
      assertTrue(po.getValue() instanceof PersistedSimpleValue);
      
      //
      ManagedObject restored = update(new ObjectNameMetaData(), restoredElement);
      assertNotNull(restored);
      ObjectNameMetaData metaData = (ObjectNameMetaData) restored.getAttachment();
      assertEquals(createObjectName("value5"), metaData.getName());

   }
   
   
   protected ObjectNameMetaData createMD() throws Exception
   {
      ObjectNameMetaData md = new ObjectNameMetaData();
      // Set objectName
      md.setName(createObjectName("value1"));
      md.setOtherName(createObjectName("value2"));
      
      return md;
   }
   
   protected ObjectName createObjectName(String v) throws Exception
   {
      return new ObjectName("org.jboss:test=test,v="+v);
   }
}

