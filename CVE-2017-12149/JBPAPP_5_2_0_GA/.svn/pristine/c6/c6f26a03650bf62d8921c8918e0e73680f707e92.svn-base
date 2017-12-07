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
public class TimestampBasedOutdatedSessionChecker
      implements  OutdatedSessionChecker
{
   private final ExtendedDistributedCacheManager<? extends OutgoingDistributableSessionData> manager;
   
   public TimestampBasedOutdatedSessionChecker(ExtendedDistributedCacheManager<? extends OutgoingDistributableSessionData> manager)
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
      Long timestamp = manager.getSessionTimestamp(realId);
      if (timestamp != null)
      {
         result = session.getLastAccessedTimeInternal() < timestamp.longValue();
      }
      else {
         // JBPAPP-5171 fix -- session that has never been replicated can't be outdated
         result = session.getLastReplicated() > 0;
      }
      return result;
   }

}
