/**
 * 
 */
package org.jboss.web.tomcat.service.session;

import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;

/**
 *
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class AskSessionOutdatedSessionChecker
      implements  OutdatedSessionChecker
{
   public boolean isSessionOutdated(ClusteredSession<? extends OutgoingDistributableSessionData> session)
   {
      return session.isOutdated();
   }

}
