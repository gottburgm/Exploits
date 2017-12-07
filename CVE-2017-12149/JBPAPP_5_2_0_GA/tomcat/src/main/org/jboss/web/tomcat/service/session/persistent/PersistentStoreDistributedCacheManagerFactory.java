/**
 * 
 */
package org.jboss.web.tomcat.service.session.persistent;

import org.jboss.web.tomcat.service.session.distributedcache.spi.ClusteringNotSupportedException;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheManagerFactory;
import org.jboss.web.tomcat.service.session.distributedcache.spi.LocalDistributableSessionManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;

/**
 *
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class PersistentStoreDistributedCacheManagerFactory implements DistributedCacheManagerFactory
{
   private final PersistentStore store;
   
   /**
    * 
    */
   public PersistentStoreDistributedCacheManagerFactory(PersistentStore store)
   {
      if (store == null)
      {
         throw new IllegalArgumentException("Null store");
      }
      this.store = store;
   }

   @SuppressWarnings("unchecked")
   public <T extends OutgoingDistributableSessionData> DistributedCacheManager<T> getDistributedCacheManager(
         LocalDistributableSessionManager localManager) throws ClusteringNotSupportedException
   {
      return (DistributedCacheManager<T>) new PersistentStoreDistributedCacheManager(store);
   }

}
