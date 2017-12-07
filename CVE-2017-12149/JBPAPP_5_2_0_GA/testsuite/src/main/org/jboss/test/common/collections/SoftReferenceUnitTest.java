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
package org.jboss.test.common.collections;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import junit.framework.TestCase;
import org.jboss.util.collection.SoftSet;
import org.jboss.util.collection.SoftValueHashMap;

/**
 * Tests of the SoftReference based jboss common collection classes
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class SoftReferenceUnitTest extends TestCase
{
   public SoftReferenceUnitTest(String name)
   {
      super(name);
   }

   /**
    * Tests of the SoftSet
    * @throws Exception
    */
   public void testSoftValueSet() throws Exception
   {
      SoftSet set = new SoftSet();
      StringBuffer akey = new StringBuffer("Key#");
      for(int n = 0; n < 1000; n ++)
      {
         String key = "Key#"+n;
         set.add(key);
         assertTrue("set has key", set.contains(key));
         akey.setLength(4);
         akey.append(""+n);
         String key2 = akey.toString();
         assertEquals(key, key2);
         assertEquals(key.hashCode(), key2.hashCode());
         assertTrue("set has akey", set.contains(key2));
      }
      assertEquals("Size == 1000", 1000, set.size());
      assertEquals("Set.isEmpty is false", false, set.isEmpty());
      String[] keys = new String[1000];
      set.toArray(keys);
      for(int n = 0; n < 1000; n ++)
      {
         String key = keys[n];
         assertTrue("set has key", set.contains(key));
      }

      HashSet set2 = new HashSet();
      set2.add("Key#1000");
      assertFalse("set has not Key#1000", set.contains("Key#1000"));
      assertTrue("Key#1000 was added", set.addAll(set2));
      assertEquals("Size == 1001", 1001, set.size());
      assertTrue("Key#1000 was removed", set.removeAll(set2));
      assertEquals("Size == 1000", 1000, set.size());
      set.add("Key#1000");
      assertTrue("Key#1000 was removed", set.retainAll(set2));
      assertEquals("Size == 1", 1, set.size());
      assertTrue("set contains [Key#1000]", set.containsAll(set2));
      
      set.clear();
      assertEquals("Size == 0", 0, set.size());
      assertEquals("Size is empty", true, set.isEmpty());
      for(int n = 0; n < 1000; n ++)
      {
         String key = keys[n];
         set.add(key);
         assertTrue("set has key", set.contains(key));
      }

      for(int n = 0; n < 1000; n ++)
      {
         String key = "Key#"+n;
         set.remove(key);
         assertFalse("key was removed", set.contains(key));
      }

      for(int n = 0; n < 1000; n ++)
      {
         String key = "Key#"+n;
         set.add(key);
         assertTrue("set has key", set.contains(key));
      }
      Iterator iter = set.iterator();
      while( iter.hasNext() )
      {
         Object o = iter.next();
         assertTrue("o instanceof String", o instanceof String);
      }

      forceSoftRefCollection();
      assertEquals("Size == 0 after gc", 0, set.size());
   }

   /**
    * Tests of the SoftValueHashMap
    * @throws Exception
    */
   public void testSoftValueHashMap() throws Exception
   {
      SoftValueHashMap map = new SoftValueHashMap();
      for(int n = 0; n < 1000; n ++)
      {
         String key = "Key#"+n;
         String value = "Value#"+n;
         map.put(key, value);
      }
      assertEquals("Size == 1000", 1000, map.size());
      forceSoftRefCollection();
      assertEquals("Size == 0 after gc", 0, map.size());
   }

   private void forceSoftRefCollection()
   {
      ReferenceQueue queue = new ReferenceQueue();
      SoftReference reference = new SoftReference(new Object(), queue);

      ArrayList list = new ArrayList();
      try
      {
         Random rnd = new Random();
         for(int i = 0; true; i ++)
         {
            BigInteger bi = new BigInteger(16384, rnd);
            list.add(bi);
            if (i%1000==0)
            {
               Reference ref;
               if ( (ref = queue.poll()) != null)
               {
                  System.out.println("Break as the soft reference has been queued: "+ref);
                  break;
               }
            }
         }
      }
      catch (Throwable e)
      {
      }
   }
}
