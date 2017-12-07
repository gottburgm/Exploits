/**
 * 
 */
package org.jboss.web.tomcat.service.session.persistent;

import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;

/**
 * Temporary extension to DistributedCacheManager to expose some further 
 * session information.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public interface ExtendedDistributedCacheManager<T extends OutgoingDistributableSessionData> 
      extends DistributedCacheManager<T>
{
   Integer getSessionVersion(String realId);
   
   Long getSessionTimestamp(String realId);
}
