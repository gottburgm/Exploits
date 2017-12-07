/**
 * 
 */
package org.jboss.web.tomcat.service.session.persistent;

import org.jboss.web.tomcat.service.session.ClusteredSession;
import org.jboss.web.tomcat.service.session.OutdatedSessionChecker;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;

/**
 *
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class VersionBasedOutdatedSessionChecker
      implements  OutdatedSessionChecker
{
   private final ExtendedDistributedCacheManager<? extends OutgoingDistributableSessionData> manager;
   
   public VersionBasedOutdatedSessionChecker(ExtendedDistributedCacheManager<? extends OutgoingDistributableSessionData> manager)
   {
      if (manager == null)
      {
         throw new IllegalArgumentException("Null manager");
      }
      this.manager = manager;
   }
   
   public boolean isSessionOutdated(ClusteredSession<? extends OutgoingDistributableSessionData> session)
   {
      boolean result = true;
      String realId = session.getRealId();
      Integer version = manager.getSessionVersion(realId);
      if (version != null)
      {
         session.setVersionFromDistributedCache(version.intValue());
         result = session.isOutdated();
      }
      else {
         // JBPAPP-5171 fix -- session that has never been replicated can't be outdated
         result = session.getLastReplicated() > 0;
      }
      return result;
   }

}
