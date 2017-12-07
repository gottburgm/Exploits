/**
 * 
 */
package org.jboss.test.hibernate.mocks;

import java.util.Properties;

import org.hibernate.cache.CacheDataDescription;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CollectionRegion;
import org.hibernate.cache.EntityRegion;
import org.hibernate.cache.QueryResultsRegion;
import org.hibernate.cache.RegionFactory;
import org.hibernate.cache.TimestampsRegion;
import org.hibernate.cfg.Settings;

/**
 * A MockRegionFactory.
 * 
 * @author Brian Stansberry
 * @version $Revision: 1.1 $
 */
public class MockRegionFactory implements RegionFactory
{

   public MockRegionFactory(Properties ignored)
   {
      super();
   }
   
   /* (non-Javadoc)
    * @see org.hibernate.cache.RegionFactory#buildCollectionRegion(java.lang.String, java.util.Properties, org.hibernate.cache.CacheDataDescription)
    */
   public CollectionRegion buildCollectionRegion(String regionName, Properties properties, CacheDataDescription metadata)
         throws CacheException
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.RegionFactory#buildEntityRegion(java.lang.String, java.util.Properties, org.hibernate.cache.CacheDataDescription)
    */
   public EntityRegion buildEntityRegion(String regionName, Properties properties, CacheDataDescription metadata)
         throws CacheException
   {
      return null;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.RegionFactory#buildQueryResultsRegion(java.lang.String, java.util.Properties)
    */
   public QueryResultsRegion buildQueryResultsRegion(String regionName, Properties properties) throws CacheException
   {
      return MockRegion.instance;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.RegionFactory#buildTimestampsRegion(java.lang.String, java.util.Properties)
    */
   public TimestampsRegion buildTimestampsRegion(String regionName, Properties properties) throws CacheException
   {
      return MockRegion.instance;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.RegionFactory#isMinimalPutsEnabledByDefault()
    */
   public boolean isMinimalPutsEnabledByDefault()
   {
      return false;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.RegionFactory#nextTimestamp()
    */
   public long nextTimestamp()
   {
      return 0;
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.RegionFactory#start(org.hibernate.cfg.Settings, java.util.Properties)
    */
   public void start(Settings settings, Properties properties) throws CacheException
   {
   }

   /* (non-Javadoc)
    * @see org.hibernate.cache.RegionFactory#stop()
    */
   public void stop()
   {
   }

}
