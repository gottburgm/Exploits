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

package org.jboss.system.server.profileservice.repository.clustered.metadata;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for objects that maintain a sorted collection of child 
 * {@link Identifiable} metadata and also provide efficient lookup capability
 * based on a child item's {@link Identifiable#getId() id}.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public abstract class AbstractSortedMetadataContainer<K, T extends Identifiable<K>>
   implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 7130572488073615772L;
   
   protected SortedSet<T> sortedItems = new TreeSet<T>();
   private transient Map<K, T> itemMap = new ConcurrentHashMap<K, T>();
   private transient Collection<T> exposedCollection = new MetadataCollection();
   
   // --------------------------------------------------------------- Protected
   
   /**
    * Gets a collection that can be exposed to external callers. Modifications
    * to the collection affect the internal state of this object. The
    * iterator exposed by this collection will provide items ordered by
    * the natural ordering of <code>T</code>. The returned collection is not 
    * thread safe.
    */
   protected Collection<T> getExposedCollection()
   {
      return exposedCollection;
   }
   
   /**
    * Gets the metadata object identified by <code>key</code>.
    * 
    * @param key the key
    * @return the metadata, or <code>null</code> if <code>key</code> is unknown.
    */
   protected T getContainedMetadata(K key)
   {
      return itemMap.get(key);
   }
   
   /**
    * Gets an unmodifiable view of the {@link Identifiable#getId() ids} of the
    * metadata stored in this container. 
    * 
    * @return the ids. Will not be <code>null</code>.
    */
   protected Set<K> getContainedMetadataIds()
   {
      return Collections.unmodifiableSet(itemMap.keySet());
   }
   
   // ----------------------------------------------------------------- Private
   
   private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      exposedCollection = new MetadataCollection();
      itemMap = new ConcurrentHashMap<K, T>();
      for (T item : sortedItems)
      {
         itemMap.put(item.getId(), item);
      }
   }
   
   private class MetadataCollection implements Collection<T>
   {    
      public boolean add(T toAdd)
      {      
         K id = toAdd.getId();
         T existing = itemMap.put(id, toAdd);
         boolean change = (toAdd.equals(existing) == false);
         if (change)
         {
            if (existing != null)
            {
               sortedItems.remove(existing);
            }
            sortedItems.add(toAdd);
         }
         return change;
      }

      public boolean addAll(Collection<? extends T> c)
      {
         boolean mod = false;
         for (T t : c)
         {
            if (add(t))
            {
               mod = true;
            }
         }
         return mod;
      }

      public void clear()
      {
         itemMap.clear();
         sortedItems.clear();
      }

      public boolean contains(Object o)
      {
         return sortedItems.contains(o);
      }

      public boolean containsAll(Collection<?> c)
      {
         return sortedItems.containsAll(c);
      }

      public boolean isEmpty()
      {
         return sortedItems.isEmpty();
      }

      public Iterator<T> iterator()
      {
         return new MetadataCollectionIterator();
      }

      public boolean remove(Object toRemove)
      {
         boolean result = sortedItems.remove(toRemove);
         if (result)
         {
            @SuppressWarnings("unchecked")
            T item = (T) toRemove;
            itemMap.remove(item.getId());
         }
         return result;
      }

      public boolean removeAll(Collection<?> c)
      {
         boolean mod = false;
         for (Object o : c)
         {
            if (remove(o))
            {
               mod = true;
            }
         }
         return mod;
      }

      public boolean retainAll(Collection<?> c)
      {
         throw new UnsupportedOperationException("retainAll is not supported");
      }

      public int size()
      {
         return sortedItems.size();
      }

      public Object[] toArray()
      {
         return sortedItems.toArray();
      }

      public <E> E[] toArray(E[] a)
      {
         return sortedItems.toArray(a);
      }
      
      
      public boolean equals(Object other)
      {
         if (other == this)
         {
            return true;
         }
         
         if (other instanceof Collection)
         {
            Collection<?> o = (Collection<?>) other;
            return sortedItems.size() == o.size() && sortedItems.containsAll(o); 
         }
         return false;
      }
      
      public int hashCode()
      {
         return sortedItems.hashCode();
      }
   }
   
   private class MetadataCollectionIterator implements Iterator<T>
   {
      private final Iterator<T> delegate = AbstractSortedMetadataContainer.this.sortedItems.iterator();
      private T lastReturned;
      
      public boolean hasNext()
      {
         return delegate.hasNext();
      }
      
      public T next()
      {
         this.lastReturned = delegate.next();
         return this.lastReturned;
      }
      
      public void remove()
      {
         this.delegate.remove();
         AbstractSortedMetadataContainer.this.itemMap.remove(this.lastReturned.getId());         
      }
   }

}
