/**
 * 
 */
package org.jboss.web.tomcat.service.session.persistent;

import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.LifecycleException;
import org.jboss.logging.Logger;
import org.jboss.metadata.web.jboss.ReplicationGranularity;
import org.jboss.metadata.web.jboss.ReplicationTrigger;
import org.jboss.metadata.web.jboss.SnapshotMode;
import org.jboss.web.tomcat.service.session.JBossCacheManager;
import org.jboss.web.tomcat.service.session.OutdatedSessionChecker;
import org.jboss.web.tomcat.service.session.OwnedSessionUpdate;
import org.jboss.web.tomcat.service.session.distributedcache.spi.IncomingDistributableSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;

/**
 *
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public abstract class AbstractPersistentManager<P extends RDBMSStoreBase, O extends OutgoingDistributableSessionData> 
   extends JBossCacheManager<O>
{
   // ----------------------------------------------------- Instance Variables


   /**
    * The descriptive information about this implementation.
    */
   private static final String info = "AbstractPersistentManager/1.0";

   /**
    * The descriptive name of this Manager implementation (for logging).
    */
   protected static String name = "AbstractPersistentManager";
   
   private final P store;

   /**
    * The connection username to use when trying to connect to the database.
    */
   private String connectionName = null;

   /**
    * The connection URL to use when trying to connect to the database.
    */
   private String connectionPassword = null;

   /**
    * Table to use.
    */
   private String sessionTable = null;

   /**
    * Column to use for /Engine/Host/Context name
    */
   private String sessionAppCol = null;

   /**
    * Id column to use.
    */
   private String sessionIdCol = null;

   /**
    * Full Id (e.g. including jvmRoute) column to use.
    */
   private String sessionFullIdCol = null;

   /**
    * Creation time column to use
    */
   private String sessionCreationTimeCol = null;

   /**
    * Max Inactive column to use.
    */
   private String sessionMaxInactiveCol = null;

   /**
    * Version column to use.
    */
   private String sessionVersionCol = null;

   /**
    * Last Accessed column to use.
    */
   private String sessionLastAccessedCol = null;

   /**
    * Is New column to use
    */
   private String sessionNewCol = null;

   /**
    * Is Valid column to use.
    */
   private String sessionValidCol = null;

   /**
    * Column to use for misc metadata.
    */
   private String sessionMetadataCol = null;

   /**
    * Attribute column to use.
    */
   private String sessionAttributeCol = null;
   
   private Integer cleanupInterval = null;

   private Logger log = Logger.getLogger(getClass());
   
   // ------------------------------------------------------------ Constructors

   /**
    * 
    */
   public AbstractPersistentManager(P store)
   {
      super(new PersistentStoreDistributedCacheManagerFactory(store));
      
      this.store = store;
   }


   // ------------------------------------------------------------- Properties


   /**
    * Return descriptive information about this Manager implementation and
    * the corresponding version number, in the format
    * <code>&lt;description&gt;/&lt;version&gt;</code>.
    */
   public String getInfo() {

       return (info);

   }

   /**
    * Return the descriptive short name of this Manager implementation.
    */
   public String getName() {

       return (name);

   }

   public String getConnectionName()
   {
      return connectionName;
   }


   public void setConnectionName(String connectionName)
   {
      this.connectionName = connectionName;
   }


   public String getConnectionPassword()
   {
      return connectionPassword;
   }


   public void setConnectionPassword(String connectionPassword)
   {
      this.connectionPassword = connectionPassword;
   }


   public String getSessionTable()
   {
      return sessionTable;
   }


   public void setSessionTable(String sessionTable)
   {
      this.sessionTable = sessionTable;
   }


   public String getSessionAppCol()
   {
      return sessionAppCol;
   }


   public void setSessionAppCol(String sessionAppCol)
   {
      this.sessionAppCol = sessionAppCol;
   }


   public String getSessionIdCol()
   {
      return sessionIdCol;
   }


   public void setSessionIdCol(String sessionIdCol)
   {
      this.sessionIdCol = sessionIdCol;
   }


   public String getSessionFullIdCol()
   {
      return sessionFullIdCol;
   }


   public void setSessionFullIdCol(String sessionFullIdCol)
   {
      this.sessionFullIdCol = sessionFullIdCol;
   }


   public String getSessionCreationTimeCol()
   {
      return sessionCreationTimeCol;
   }


   public void setSessionCreationTimeCol(String sessionCreationTimeCol)
   {
      this.sessionCreationTimeCol = sessionCreationTimeCol;
   }


   public String getSessionMaxInactiveCol()
   {
      return sessionMaxInactiveCol;
   }


   public void setSessionMaxInactiveCol(String sessionMaxInactiveCol)
   {
      this.sessionMaxInactiveCol = sessionMaxInactiveCol;
   }


   public String getSessionVersionCol()
   {
      return sessionVersionCol;
   }


   public void setSessionVersionCol(String sessionVersionCol)
   {
      this.sessionVersionCol = sessionVersionCol;
   }


   public String getSessionLastAccessedCol()
   {
      return sessionLastAccessedCol;
   }


   public void setSessionLastAccessedCol(String sessionLastAccessedCol)
   {
      this.sessionLastAccessedCol = sessionLastAccessedCol;
   }


   public String getSessionNewCol()
   {
      return sessionNewCol;
   }


   public void setSessionNewCol(String sessionNewCol)
   {
      this.sessionNewCol = sessionNewCol;
   }


   public String getSessionValidCol()
   {
      return sessionValidCol;
   }


   public void setSessionValidCol(String sessionValidCol)
   {
      this.sessionValidCol = sessionValidCol;
   }


   public String getSessionMetadataCol()
   {
      return sessionMetadataCol;
   }


   public void setSessionMetadataCol(String sessionMetadataCol)
   {
      this.sessionMetadataCol = sessionMetadataCol;
   }


   public String getSessionAttributeCol()
   {
      return sessionAttributeCol;
   }


   public void setSessionAttributeCol(String sessionAttributeCol)
   {
      this.sessionAttributeCol = sessionAttributeCol;
   }  
   
   public Integer getCleanupInterval()
   {
      return cleanupInterval;
   }

   public void setCleanupInterval(Integer cleanupInterval)
   {
      this.cleanupInterval = cleanupInterval;
   }
   
   
   
   
   
   // --------------------------------------------------------------  Public


   @Override
   public void setReplicationGranularity(ReplicationGranularity granularity)
   {
      switch (granularity)
      {
         case SESSION:
            super.setReplicationGranularity(granularity);
            break;
         default:
            log.warn("Ignoring call to set replication granularity to " + 
                  granularity + " -- only " + ReplicationGranularity.SESSION + " is supported");
      }
   }


   @Override
   public void start() throws LifecycleException
   {
      configureStore();
      super.start();
   }


   // --------------------------------------------------------------  Protected

   protected P getPersistentStore()
   {
      return store;
   }
   
   @Override
   protected void configureUnembedded() throws LifecycleException
   {
      // Only set replication attributes if they were not
      // already set via a <Manager> element in an XML config file
      
      if (getReplicationGranularity() == null) 
      {
         setReplicationGranularity(ReplicationGranularity.SESSION);
      }
      
      if (getReplicationTrigger() == null) 
      {
         setReplicationTrigger(ReplicationTrigger.SET_AND_NON_PRIMITIVE_GET);
      }
      
      if (isReplicationFieldBatchMode() == null)
      {
         setReplicationFieldBatchMode(false);
      }
      
      if (getSnapshotMode() == null)
      {
         setSnapshotMode(SnapshotMode.INSTANT);
      }   
   
   }


   protected void configureStore()
   {
      store.setName(this.getContextName());
      store.setMaxUnreplicatedInterval(getMaxUnreplicatedInterval());
      
      if (getConnectionName() != null)
      {
         store.setConnectionName(getConnectionName());
      }
      if (getConnectionPassword() != null)
      {
         store.setConnectionPassword(getConnectionPassword());
      }
      if (getSessionAppCol() != null)
      {
         store.setSessionAppCol(getSessionAppCol());
      }
      if (getSessionAttributeCol() != null)
      {
         store.setSessionAttributeCol(getSessionAttributeCol());
      }
      if (getSessionCreationTimeCol() != null)
      {
         store.setSessionCreationTimeCol(getSessionCreationTimeCol());
      }
      if (getSessionFullIdCol() != null)
      {
         store.setSessionFullIdCol(getSessionFullIdCol());
      }
      if (getSessionIdCol() != null)
      {
         store.setSessionIdCol(getSessionIdCol());
      }
      if (getSessionLastAccessedCol() != null)
      {
         store.setSessionLastAccessedCol(getSessionLastAccessedCol());
      }
      if (getSessionMaxInactiveCol() != null)
      {
         store.setSessionMaxInactiveCol(getSessionMaxInactiveCol());
      }
      if (getSessionMetadataCol() != null)
      {
         store.setSessionMetadataCol(getSessionMetadataCol());
      }
      if (getSessionNewCol() != null)
      {
         store.setSessionNewCol(getSessionNewCol());
      }
      if (getSessionTable() != null)
      {
         store.setSessionTable(getSessionTable());
      }
      if (getSessionValidCol() != null)
      {
         store.setSessionValidCol(getSessionValidCol());
      }
      if (getSessionVersionCol() != null)
      {
         store.setSessionVersionCol(getSessionVersionCol());
      }
      if (getCleanupInterval() != null)
      {
         store.setCleanupInterval(getCleanupInterval().intValue());
      }
   }


   /**
    * Overrides superclass to update the contents of OwnedSessionUpdate values
    * to reflect the current state of the {@link PersistentStore}.
    */
   @Override
   protected Map<String, OwnedSessionUpdate> getUnloadedSessions()
   {
      Map<String, OwnedSessionUpdate> map =  super.getUnloadedSessions();
      Map<String, OwnedSessionUpdate> processed = new HashMap<String, OwnedSessionUpdate>();
      for (Map.Entry<String, OwnedSessionUpdate> entry : map.entrySet())
      {
         String realId = entry.getKey();
         OwnedSessionUpdate existing = entry.getValue();
         
         Long timestamp = store.getSessionTimestamp(realId);
         if (timestamp != null && existing.getUpdateTime() != timestamp.longValue())
         {
            // Timestamp change -- pull in the data
            IncomingDistributableSessionData data = store.getSessionData(entry.getKey(), false);
            if (data != null)
            {
                OwnedSessionUpdate updated = new OwnedSessionUpdate(existing.getOwner(), 
                      data.getTimestamp(), 
                      data.getMetadata().getMaxInactiveInterval(), 
                      existing.isPassivated());
                
                processed.put(realId, updated);
            }
            else
            {
               // Session has been deleted; just keep existing and let
               // the caller clean up
               processed.put(realId, existing);
            }
         }
         else
         {
            // Session has been deleted; just keep existing and let
            // the caller clean up
            processed.put(realId, existing);
         }
      }
      
      return processed;
   }


   @Override
   protected void initializeUnloadedSessions()
   {
      // no-op
   }

   

   @Override
   protected OutdatedSessionChecker initOutdatedSessionChecker()
   {
      // TODO make this configurable
      return new VersionBasedOutdatedSessionChecker((ExtendedDistributedCacheManager<? extends OutgoingDistributableSessionData>) getDistributedCacheManager());
   }


   @Override
   protected void processExpirationPassivation()
   {
      super.processExpirationPassivation();
      
      this.store.processExpires();
   }
   
   

   
}
