/**
 * 
 */
package org.jboss.test.hibernate.mocks;

import java.util.Map;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.QueryResultsRegion;
import org.hibernate.cache.TimestampsRegion;

/**
 * A MockQueryResultsRegion.
 * 
 * @author Brian Stansberry
 * @version $Revision: 1.1 $
 */
public class MockRegion implements QueryResultsRegion, TimestampsRegion
{
   public static final MockRegion instance = new MockRegion();

   /* (non-Javadoc)
    * @see org.hibernate.cache.GeneralDataRegion#evict(java.lang.Object)
    */
   public void evict(Object key) throws CacheException
   {
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.GeneralDataRegion#evictAll()
    */
   public void evictAll() throws CacheException
   {
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.GeneralDataRegion#get(java.lang.Object)
    */
   public Object get(Object key) throws CacheException
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.GeneralDataRegion#put(java.lang.Object, java.lang.Object)
    */
   public void put(Object key, Object value) throws CacheException
   {
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Region#destroy()
    */
   public void destroy() throws CacheException
   {
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Region#getElementCountInMemory()
    */
   public long getElementCountInMemory()
   {
      return 0;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Region#getElementCountOnDisk()
    */
   public long getElementCountOnDisk()
   {
      return 0;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Region#getName()
    */
   public String getName()
   {
      return "MockRegion";
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Region#getSizeInMemory()
    */
   public long getSizeInMemory()
   {
      return 0;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Region#getTimeout()
    */
   public int getTimeout()
   {
      return 0;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Region#nextTimestamp()
    */
   public long nextTimestamp()
   {
      return 0;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.Region#toMap()
    */
   public Map<?,?> toMap()
   {
      return null;
   }

   public boolean contains(Object key)
   {
      return false;
   }

}
