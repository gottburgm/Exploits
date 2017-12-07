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
public interface OutdatedSessionChecker
{
   boolean isSessionOutdated(ClusteredSession<? extends OutgoingDistributableSessionData> session);
}
