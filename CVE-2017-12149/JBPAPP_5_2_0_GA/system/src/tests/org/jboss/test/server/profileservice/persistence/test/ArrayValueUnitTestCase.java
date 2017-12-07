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

import java.util.Arrays;

import org.jboss.managed.api.ManagedObject;
import org.jboss.metatype.api.values.ArrayValue;
import org.jboss.system.server.profileservice.persistence.xml.PersistedManagedObject;
import org.jboss.test.server.profileservice.persistence.support.SimpleArrayMetaData;

/**
 * Simple attachment persistence test for array values.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 88573 $
 */
public class ArrayValueUnitTestCase extends AbstractPersistenceFormatTest
{

   public ArrayValueUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testSimpleArray() throws Throwable
   {
      ManagedObject mo = initMO();
      PersistedManagedObject moElement = restore(mo);
      
      assertNotNull(moElement);
      ManagedObject restored = update(new SimpleArrayMetaData(), moElement);
      
      assertNotNull(restored.getProperties());
      assertEquals(3, restored.getProperties().size());
      
      // Test 1D
      ArrayValue test1D = (ArrayValue) restored.getProperty("test1D").getValue();
      assertNotNull(test1D);
      
      char[] unwrapped1D = (char[]) getMVF().unwrap(test1D);
      assertNotNull(unwrapped1D);
      assertTrue(Arrays.equals(init1D(), unwrapped1D));
      
      // Test 2D
      ArrayValue test2D = (ArrayValue) restored.getProperty("test2D").getValue();
      assertNotNull(test2D);
      assertEquals(2, test2D.getMetaType().getDimension());
      
      char[][] unwrapped2D = (char[][]) getMVF().unwrap(test2D);
      assertNotNull(unwrapped2D);
      assertEquals(2, unwrapped2D.length);
      assertEquals('a', unwrapped2D[0][0]);
      assertEquals('f', unwrapped2D[1][1]);

      // Test 3D
      ArrayValue test3D = (ArrayValue) restored.getProperty("test3D").getValue();
      assertNotNull(test3D);
      assertEquals(3, test3D.getMetaType().getDimension());

      char[][][] unwrapped3D = (char[][][]) getMVF().unwrap(test3D);
      assertNotNull(unwrapped3D);
      
      assertEquals(3, unwrapped3D[0].length);
      assertEquals(3, unwrapped3D[0][0].length);
      assertEquals(2, unwrapped3D[0][1].length);
      assertEquals(2, unwrapped3D[0][2].length);
      assertEquals('g', unwrapped3D[0][2][0]);
      assertEquals('h', unwrapped3D[0][2][1]);
   }
   
   protected ManagedObject initMO()
   {
      SimpleArrayMetaData instance = new SimpleArrayMetaData();
      instance.setTest1D(init1D());
      instance.setTest2D(init2D());
      instance.setTest3D(init3D());
      
      return getMOF().initManagedObject(instance, null);
   }
   
   protected char[] init1D()
   {
      return new char[] {'a', 'b', 'c'};
   }
   
   protected char[][] init2D()
   {
      return new char[][] {{'a', 'b', 'c'}, { 'e', 'f'}};
   }
   
   protected char[][][] init3D()
   {
      return new char[][][] {{{'a', 'b', 'c'}, { 'e', 'f'}, {'g', 'h'}}};
   }
   
}
