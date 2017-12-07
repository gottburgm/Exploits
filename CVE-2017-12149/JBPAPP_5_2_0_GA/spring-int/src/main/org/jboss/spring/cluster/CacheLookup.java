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
package org.jboss.spring.cluster;

import org.jboss.cache.CacheException;
import org.jboss.cache.pojo.PojoCache;

/**
 * Cache lookup helper class.
 *
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
public abstract class CacheLookup
{
   private String rootName = "/pojos/";
   protected PojoCache pojoCache;

   protected CacheLookup(PojoCache pojoCache)
   {
      this.pojoCache = pojoCache;
   }

   /**
    * Get the object out of pojo cache.
    *
    * @param name the name to look for
    * @return cached object or null for some cache exception
    */
   protected Object get(String name)
   {
      try
      {
         return pojoCache.find(rootName + name);
      }
      catch (CacheException e)
      {
         System.err.println("Exception getting object from PojoCache:" + e);
      }
      return null;
   }

   /**
    * Put the object into cache.
    *
    * @param name the name to put it under
    * @param object the object to put
    * @return result of cache put
    */
   protected Object put(String name, Object object)
   {
      try
      {
         return pojoCache.attach(rootName + name, object);
      }
      catch (CacheException e)
      {
         throw new IllegalArgumentException("Unable to put object to PojoCache: " + e);
      }
   }

   /**
    * Remove object with name param from cache.
    *
    * @param name the object's name
    * @return removed object or null for any cache error
    */
   public Object remove(String name)
   {
      try
      {
         return pojoCache.detach(rootName + name);
      }
      catch (CacheException e)
      {
         System.err.println("Exception removing object from PojoCache:" + e);
         return null;
      }
   }

   /**
    * Set the cache root name.
    *
    * @param rootName the root name
    */
   public void setRootName(String rootName)
   {
      this.rootName = rootName;
   }
}
