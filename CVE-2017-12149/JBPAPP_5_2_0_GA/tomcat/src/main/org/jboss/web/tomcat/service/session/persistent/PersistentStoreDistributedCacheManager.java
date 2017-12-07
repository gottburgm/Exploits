/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.web.tomcat.service.session.persistent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.metadata.web.jboss.ReplicationGranularity;
import org.jboss.web.tomcat.service.session.distributedcache.spi.BatchingManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributableSessionMetadata;
import org.jboss.web.tomcat.service.session.distributedcache.spi.IncomingDistributableSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingSessionGranularitySessionData;

/**
 *
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class PersistentStoreDistributedCacheManager
      implements ExtendedDistributedCacheManager<OutgoingSessionGranularitySessionData>
{   
   private static final BatchingManager BATCH_MGR = new NoOpBatchingManager();
   
   private final PersistentStore store;
   
   /**
    * 
    */
   public PersistentStoreDistributedCacheManager(PersistentStore store)
   {
      if (store == null)
      {
         throw new IllegalArgumentException("Null store");
      }
      this.store = store;
   }
   
   public BatchingManager getBatchingManager()
   {
      return BATCH_MGR;
   }

   public boolean isPassivationEnabled()
   {
      return true;
   }

   public void evictSession(String realId, String dataOwner)
   {
      // no-op -- we don't keep anything in memory
   }

   public void evictSession(String realId)
   {
      // no-op -- we don't keep anything in memory
   }

   public IncomingDistributableSessionData getSessionData(String realId, boolean initialLoad)
   {
      return getSessionData(realId, null, true);
   }

   public IncomingDistributableSessionData getSessionData(String realId, String dataOwner, boolean includeAttributes)
   {
      return store.getSessionData(realId, includeAttributes);
   }

   public Map<String, String> getSessionIds()
   {
      Map<String, String> result = new HashMap<String, String>();
      Set<String> keys = store.getSessionIds();
      if (keys != null)
      {
         for (String key : keys)
         {
            result.put(key, null);
         }
      }
      
      return result;
   }
    

   public void removeSession(String realId)
   {
      store.remove(realId);
   }

   public void removeSessionLocal(String realId, String dataOwner)
   {
      removeSessionLocal(realId);
   }

   public void removeSessionLocal(String realId)
   {
      // we ignore this, as it's logically equivalent to an evict
   }

   public void sessionCreated(String realId)
   {
      // no-op
   }

   public Long getSessionTimestamp(String realId)
   {
      return store.getSessionTimestamp(realId);
   }

   public Integer getSessionVersion(String realId)
   {
      return store.getSessionVersion(realId);
   }

   public void start()
   {
      store.start();            
   }

   public void stop()
   {
      store.stop();       
   }

   public void storeSessionData(OutgoingSessionGranularitySessionData sessionData)
   {
      store.storeSessionData(sessionData);    
   } 

   public boolean getSupportsAttributeOperations()
   {
      return false;
   }

   public Object getAttribute(String realId, String key)
   {
      throw new UnsupportedOperationException("Attribute operations not supported " +
            "with ReplicationGranularity " + ReplicationGranularity.SESSION);
   }

   public Set<String> getAttributeKeys(String realId)
   {
      throw new UnsupportedOperationException("Attribute operations not supported " +
            "with ReplicationGranularity " + ReplicationGranularity.SESSION);
   }

   public Map<String, Object> getAttributes(String realId)
   {
      throw new UnsupportedOperationException("Attribute operations not supported " +
            "with ReplicationGranularity " + ReplicationGranularity.SESSION);
   }

   public void putAttribute(String realId, Map<String, Object> map)
   {
      throw new UnsupportedOperationException("Attribute operations not supported " +
            "with ReplicationGranularity " + ReplicationGranularity.SESSION);
   }

   public void putAttribute(String realId, String key, Object value)
   {
      throw new UnsupportedOperationException("Attribute operations not supported " +
            "with ReplicationGranularity " + ReplicationGranularity.SESSION);
   }

   public Object removeAttribute(String realId, String key)
   {
      throw new UnsupportedOperationException("Attribute operations not supported " +
            "with ReplicationGranularity " + ReplicationGranularity.SESSION);
   }

   public void removeAttributeLocal(String realId, String key)
   {
      throw new UnsupportedOperationException("Attribute operations not supported " +
            "with ReplicationGranularity " + ReplicationGranularity.SESSION);
   }
   
   public static class NoOpBatchingManager implements BatchingManager
   {
      public boolean isBatchInProgress() throws Exception
      {
         return false;
      }

      public void startBatch() throws Exception
      {
         // no-op
      }

      public void endBatch()
      {
         // no-op
      }

      public void setBatchRollbackOnly() throws Exception
      {
         // no-op
      }

   }
   
   public static class IncomingDistributableSessionDataImpl implements IncomingDistributableSessionData
   {
      private final int version;
      private final long timestamp;
      private final DistributableSessionMetadata metadata;
      private final Map<String, Object> attributes;
      
      public IncomingDistributableSessionDataImpl(Integer version, Long timestamp, 
                                                 DistributableSessionMetadata metadata,
                                                 Map<String, Object> attributes)
      {
         if (version == null)
            throw new IllegalStateException("version is null");
         if (timestamp == null)
            throw new IllegalStateException("timestamp is null");
         if (metadata == null)
            throw new IllegalStateException("metadata is null");
         
         this.version = version.intValue();
         this.timestamp = timestamp.longValue();
         this.metadata = metadata;
         this.attributes = attributes;
      }

      public boolean providesSessionAttributes()
      {
         return attributes != null;
      }

      public Map<String, Object> getSessionAttributes()
      {
         if (attributes == null)
         {
            throw new IllegalStateException("Not configured to provide session attributes");
         }
         return attributes;
      }   
      
      public DistributableSessionMetadata getMetadata()
      {
         return metadata;
      }

      public long getTimestamp()
      {
         return timestamp;
      }

      public int getVersion()
      {
         return version;
      }
      
   }

}
