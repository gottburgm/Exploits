/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.profileservice.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.managed.api.ManagedProperty;

/**
 * Map<String, ManagedProperty> that filters out ManagedPropertys that have
 * isRemoved == true.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 86710 $
 */
public class RemovedPropertyMap
   implements Map<String, ManagedProperty>
{
   private Map<String, ManagedProperty> delegate;
   private Set<String> keySet;

   public RemovedPropertyMap(Map<String, ManagedProperty> delegate)
   {
      this.delegate = delegate;
      // Filter out the removed property keys
      keySet = new HashSet<String>();
      for(String key : delegate.keySet())
      {
         ManagedProperty mp = delegate.get(key);
         if(mp != null && mp.isRemoved() == false)
            keySet.add(key);
      }
   }

   public void clear()
   {
      delegate.clear();
      keySet.clear();
   }

   public boolean containsKey(Object key)
   {
      return keySet.contains(key);
   }

   public boolean containsValue(Object value)
   {
      ManagedProperty mp = (ManagedProperty) value;
      return keySet.contains(mp.getName());
   }

   public Set<Entry<String, ManagedProperty>> entrySet()
   {
      return delegate.entrySet();
   }

   public boolean equals(Object o)
   {
      return delegate.equals(o);
   }

   public ManagedProperty get(Object key)
   {
      ManagedProperty mp = delegate.get(key);
      if(mp != null && mp.isRemoved())
         mp = null;
      return mp;
   }

   public int hashCode()
   {
      return delegate.hashCode();
   }

   public boolean isEmpty()
   {
      return keySet.isEmpty();
   }

   public Set<String> keySet()
   {
      return keySet;
   }

   public ManagedProperty put(String key, ManagedProperty value)
   {
      if(value.isRemoved())
         keySet.add(key);
      return delegate.put(key, value);
   }

   public void putAll(Map<? extends String, ? extends ManagedProperty> t)
   {
      delegate.putAll(t);
   }

   public ManagedProperty remove(Object key)
   {
      if(keySet.contains(key))
         keySet.remove(key);
      return delegate.remove(key);
   }

   public int size()
   {
      return keySet.size();
   }

   public Collection<ManagedProperty> values()
   {
      ArrayList<ManagedProperty> values = new ArrayList<ManagedProperty>();
      for(ManagedProperty mp : delegate.values())
      {
         if(mp.isRemoved() == false)
            values.add(mp);
      }
      return values;
   }
}
