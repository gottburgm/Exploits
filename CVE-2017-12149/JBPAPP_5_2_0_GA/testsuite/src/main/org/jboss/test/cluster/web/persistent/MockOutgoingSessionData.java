/**
 * 
 */
package org.jboss.test.cluster.web.persistent;

import java.util.Map;

import org.jboss.web.tomcat.service.session.OutgoingDistributableSessionDataImpl;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributableSessionMetadata;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingSessionGranularitySessionData;

/**
 *
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class MockOutgoingSessionData 
      extends OutgoingDistributableSessionDataImpl
      implements OutgoingSessionGranularitySessionData
{
   private final Map<String, Object> attributes;
   
   public MockOutgoingSessionData(String realId, int version, 
         Long timestamp, DistributableSessionMetadata metadata,
         Map<String, Object> attributes)
   {
      super(realId, version, timestamp, metadata);
      this.attributes = attributes;
   }

   public Map<String, Object> getSessionAttributes()
   {
      return attributes;
   }

}
