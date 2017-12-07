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
package org.jboss.test.mx.mxbean.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.jboss.mx.mxbean.MXBeanSupport;

/**
 * CollectionsMXBeanSupport.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class CollectionsMXBeanSupport extends MXBeanSupport implements CollectionsMXBeanSupportMXBean
{
   String[] array;
   Collection<String> collection;
   Set<String> set;
   List<String> list;
   Map<String, Integer> map;
   TestEnum enumeration;
   
   public CollectionsMXBeanSupport()
   {
   }
   
   public CollectionsMXBeanSupport(String[] array, Collection<String> collection, Set<String> set, List<String> list, Map<String, Integer> map, TestEnum enumeration)
   {
      this.array = array;
      this.collection = collection;
      this.set = set;
      this.list = list;
      this.map = map;
      this.enumeration = enumeration;
   }

   public String[] getArray()
   {
      return array;
   }
   
   public Collection<String> getCollection()
   {
      return collection;
   }

   public List<String> getList()
   {
      return list;
   }

   public Set<String> getSet()
   {
      return set;
   }

   public Map<String, Integer> getMap()
   {
      return map;
   }
   
   public TestEnum getEnum()
   {
      return enumeration;
   }

   public void setEnum(TestEnum enumeration)
   {
      this.enumeration = enumeration;
   }

   public void setArray(String[] array)
   {
      this.array = array;
   }

   public void setCollection(Collection<String> collection)
   {
      this.collection = collection;
   }

   public void setList(List<String> list)
   {
      this.list = list;
   }

   public void setMap(Map<String, Integer> map)
   {
      this.map = map;
   }

   public void setSet(Set<String> set)
   {
      this.set = set;
   }

   public List<String> echoReverse(List<String> list)
   {
      ArrayList<String> result = new ArrayList<String>(list.size());
      for (ListIterator<String> i = list.listIterator(list.size()); i.hasPrevious();)
         result.add(i.previous());
      return result;
   }
}
