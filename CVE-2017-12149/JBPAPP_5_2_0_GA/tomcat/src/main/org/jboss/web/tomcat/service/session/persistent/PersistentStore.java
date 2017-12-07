/**
 * 
 */
package org.jboss.web.tomcat.service.session.persistent;

import java.util.Set;

import org.jboss.web.tomcat.service.session.distributedcache.spi.IncomingDistributableSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingSessionGranularitySessionData;

/**
 * Interface exposed by a cluster-wide store for distributable web sessions.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public interface PersistentStore
{
   /** Gets ids of session associated with the application that are in the store. */
   Set<String> getSessionIds();
   
   /**
    * Gets the stored information about a particular session.
    * 
    * @param realId the portion of the session id that is consistent across the
    *               lifetime of the session, i.e. with mutable elements like an
    *               appended jvmRoute removed.
    *               
    * @param includeAttributes <code>true</code> if the returned data needs to
    *                          include the session attribute map; <code>false</code>
    *                          if that is unnecessary
    * 
    * @return data about the session, or <code>null</code> if no data exists
    *         in the store
    */
   IncomingDistributableSessionData getSessionData(String realId, boolean includeAttributes);
   
   /**
    * Gets the last accessed timestamp for the session with the given id
    * @param realId the portion of the session id that is consistent across the
    *               lifetime of the session, i.e. with mutable elements like an
    *               appended jvmRoute removed.
    *               
    * @return the timestamp, or <code>null</code> if the session is not stored
    */
   Long getSessionTimestamp(String realId);
   
   /**
    * Gets the session version for the session with the given id
    * 
    * @param realId the portion of the session id that is consistent across the
    *               lifetime of the session, i.e. with mutable elements like an
    *               appended jvmRoute removed.
    *               
    * @return the version, or <code>null</code> if the session is not stored
    */
   Integer getSessionVersion(String realId);
   
   /**
    * Stores this session in the persistent store.
    * 
    * @param sessionData the data to store
    */
   void storeSessionData(OutgoingSessionGranularitySessionData sessionData);
   
   /**
    * Removes any information about this session from the persistent store.
    * 
    * @param realId the portion of the session id that is consistent across the
    *               lifetime of the session, i.e. with mutable elements like an
    *               appended jvmRoute removed.
    */
   void remove(String realId);
   
   /**
    * Perform processing to remove outdated sessions from the store.
    */
   void processExpires();
   
   /**
    * Brings the store to the state where it is able to handle invocations of
    * the other methods in this interface. 
    */
   void start();
   
   /**
    * Removes the store from the state where it is able to handle invocations of
    * the other methods in this interface, performs any needed cleanup work. 
    */
   void stop();
}
