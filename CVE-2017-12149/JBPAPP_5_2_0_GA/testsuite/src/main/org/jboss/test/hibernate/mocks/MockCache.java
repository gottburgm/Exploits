/**
 * 
 */
package org.jboss.test.hibernate.mocks;

import java.util.Map;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;

/**
 * A MockCache.
 * 
 * @author Brian Stansberry
 * @version $Revision: 1.1 $
 */
public class MockCache implements Cache
{
   public static final MockCache instance = new MockCache();

   /* (non-Javadoc)
    * @see org.hibernate.cache.Cache#clear()
    */
   public void clear() throws CacheException
   {
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Cache#destroy()
    */
   public void destroy() throws CacheException
   {
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Cache#get(java.lang.Object)
    */
   public Object get(Object key) throws CacheException
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Cache#getElementCountInMemory()
    */
   public long getElementCountInMemory()
   {
      return 0;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Cache#getElementCountOnDisk()
    */
   public long getElementCountOnDisk()
   {
      return 0;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Cache#getRegionName()
    */
   public String getRegionName()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Cache#getSizeInMemory()
    */
   public long getSizeInMemory()
   {
      return 0;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Cache#getTimeout()
    */
   public int getTimeout()
   {
      return 0;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Cache#lock(java.lang.Object)
    */
   public void lock(Object key) throws CacheException
   {
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Cache#nextTimestamp()
    */
   public long nextTimestamp()
   {
      return 0;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Cache#put(java.lang.Object, java.lang.Object)
    */
   public void put(Object key, Object value) throws CacheException
   {
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Cache#read(java.lang.Object)
    */
   public Object read(Object key) throws CacheException
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Cache#remove(java.lang.Object)
    */
   public void remove(Object key) throws CacheException
   {
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Cache#toMap()
    */
   public Map toMap()
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Cache#unlock(java.lang.Object)
    */
   public void unlock(Object key) throws CacheException
   {
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Cache#update(java.lang.Object, java.lang.Object)
    */
   public void update(Object key, Object value) throws CacheException
   {
   }

}
