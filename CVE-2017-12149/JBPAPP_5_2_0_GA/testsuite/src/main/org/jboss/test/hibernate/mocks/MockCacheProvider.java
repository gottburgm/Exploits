/**
 * 
 */
package org.jboss.test.hibernate.mocks;

import java.util.Properties;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;

/**
 * A MockCacheProvider.
 * 
 * @author Brian Stansberry
 * @version $Revision: 1.1 $
 */
public class MockCacheProvider implements CacheProvider
{

   /* (non-Javadoc)
    * @see org.hibernate.cache.CacheProvider#buildCache(java.lang.String, java.util.Properties)
    */
   public Cache buildCache(String regionName, Properties properties) throws CacheException
   {
      return MockCache.instance;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.CacheProvider#isMinimalPutsEnabledByDefault()
    */
   public boolean isMinimalPutsEnabledByDefault()
   {
      return false;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.CacheProvider#nextTimestamp()
    */
   public long nextTimestamp()
   {
      return 0;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.CacheProvider#start(java.util.Properties)
    */
   public void start(Properties properties) throws CacheException
   {

   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.CacheProvider#stop()
    */
   public void stop()
   {
   }

}
