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
package org.jboss.test.mx.mxbean.test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jboss.test.mx.mxbean.support.TestParameterizedType;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * CompositeDataCollectionUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class CompositeDataCollectionUnitTestCase extends CompositeDataTest
{
   private static final TestParameterizedType COLLECTION_OF_STRINGS = new TestParameterizedType(Collection.class, new Type[] { String.class });
   private static final TestParameterizedType LIST_OF_STRINGS = new TestParameterizedType(List.class, new Type[] { String.class });
   private static final TestParameterizedType SET_OF_STRINGS = new TestParameterizedType(Set.class, new Type[] { String.class });
   
   public static Test suite()
   {
      return new TestSuite(CompositeDataCollectionUnitTestCase.class);
   }
   
   public CompositeDataCollectionUnitTestCase(String name)
   {
      super(name);
   }
   
   public void testCollection() throws Exception
   {
      Collection<String> c = new ArrayList<String>();
      c.add("one");
      c.add("two");
      c.add("three");
      String[] openData = { "one", "two", "three" };
      constructReconstructTest(c, COLLECTION_OF_STRINGS, openData);
   }
   
   public void testCollectionNull() throws Exception
   {
      constructReconstructTest(null, COLLECTION_OF_STRINGS);
   }
   
   public void testCollectionContainsNull() throws Exception
   {
      Collection<String> c = new ArrayList<String>();
      c.add("one");
      c.add(null);
      c.add("three");
      String[] openData = { "one", null, "three" };
      constructReconstructTest(c, COLLECTION_OF_STRINGS, openData);
   }
   
   public void testList() throws Exception
   {
      ArrayList<String> c = new ArrayList<String>();
      c.add("one");
      c.add("two");
      c.add("three");
      String[] openData = { "one", "two", "three" };
      constructReconstructTest(c, LIST_OF_STRINGS, openData);
   }
   
   public void testListNull() throws Exception
   {
      constructReconstructTest(null, LIST_OF_STRINGS);
   }
   
   public void testListContainsNull() throws Exception
   {
      ArrayList<String> c = new ArrayList<String>();
      c.add("one");
      c.add(null);
      c.add("three");
      String[] openData = { "one", null, "three" };
      constructReconstructTest(c, LIST_OF_STRINGS, openData);
   }
   
   public void testSet() throws Exception
   {
      LinkedHashSet<String> c = new LinkedHashSet<String>();
      c.add("one");
      c.add("two");
      c.add("three");
      String[] openData = { "one", "two", "three" };
      constructReconstructTest(c, SET_OF_STRINGS, openData);
   }
   
   public void testSetNull() throws Exception
   {
      constructReconstructTest(null, SET_OF_STRINGS);
   }
   
   public void testSetContainsNull() throws Exception
   {
      LinkedHashSet<String> c = new LinkedHashSet<String>();
      c.add("one");
      c.add(null);
      c.add("three");
      String[] openData = { "one", null, "three" };
      constructReconstructTest(c, SET_OF_STRINGS, openData);
   }

   protected void checkOpenDataEquals(Object expected, Object actual)
   {
      checkArrayEquals(expected, actual);
   }
}
