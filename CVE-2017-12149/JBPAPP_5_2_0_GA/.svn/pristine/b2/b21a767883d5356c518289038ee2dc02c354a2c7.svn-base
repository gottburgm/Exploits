/**
 * 
 */
package org.jboss.web.tomcat.service.session.persistent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.ha.framework.server.SimpleCachableMarshalledValue;
import org.jboss.logging.Logger;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributableSessionMetadata;
import org.jboss.web.tomcat.service.session.distributedcache.spi.IncomingDistributableSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingSessionGranularitySessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.SessionSerializationFactory;

/**
 * Abstract superclass for {@link PersistentStore} implementations that store in a
 * relational database.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public abstract class RDBMSStoreBase implements PersistentStore
{
   private static final Logger LOG = Logger.getLogger(RDBMSStoreBase.class);

   public static final String DEFAULT_TABLE = "httpsessions";
   public static final String DEFAULT_APP_COL = "app";
   public static final String DEFAULT_ID_COL = "id";
   public static final String DEFAULT_FULLID_COL = "fullid";
   public static final String DEFAULT_ATTRIBUTE_COL = "attributes";
   public static final String DEFAULT_METADATA_COL = "metadata";
   public static final String DEFAULT_ISNEW_COL = "isnew";
   public static final String DEFAULT_ISVALID_COL = "valid";
   public static final String DEFAULT_CREATION_TIME_COL = "creationtime";
   public static final String DEFAULT_LAST_ACCESSED_COL = "lastaccess";
   public static final String DEFAULT_MAX_INACTIVE_COL = "maxinactive";
   public static final String DEFAULT_VERSION_COL = "version";
   
   public static final int DEFAULT_CLEANUP_INTERVAL = 4 * 60 * 60;
   
   // --------------------------------------------------------- Instance Fields

   /** Any inject logger */
   private Logger logger = null;
   
   /**
    * Has this component been started yet?
    */
   private boolean started = false;

   /**
    * The string manager for this package.
    */
//   protected final StringManager sm = StringManager.getManager(Constants.Package);

   /**
    * Context name associated with this Store
    */
   private String name = null;
   
   /**
    * How often to execute the processExpires cleanup
    */
   private int cleanupInterval = DEFAULT_CLEANUP_INTERVAL;
   
   /** When we last executed the processExpires cleanup */
   private long lastCleanup = 0;
   
   private int maxUnreplicatedInterval = -1;

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
   private String sessionTable = DEFAULT_TABLE;

   /**
    * Column to use for /Engine/Host/Context name
    */
   private String sessionAppCol = DEFAULT_APP_COL;

   /**
    * Id column to use.
    */
   private String sessionIdCol = DEFAULT_ID_COL;

   /**
    * Full Id (e.g. including jvmRoute) column to use.
    */
   private String sessionFullIdCol = DEFAULT_FULLID_COL;

   /**
    * Creation time column to use
    */
   private String sessionCreationTimeCol = DEFAULT_CREATION_TIME_COL;

   /**
    * Max Inactive column to use.
    */
   private String sessionMaxInactiveCol = DEFAULT_MAX_INACTIVE_COL;

   /**
    * Version column to use.
    */
   private String sessionVersionCol = DEFAULT_VERSION_COL;

   /**
    * Last Accessed column to use.
    */
   private String sessionLastAccessedCol = DEFAULT_LAST_ACCESSED_COL;

   /**
    * Is New column to use
    */
   private String sessionNewCol = DEFAULT_ISNEW_COL;

   /**
    * Is Valid column to use.
    */
   private String sessionValidCol = DEFAULT_ISVALID_COL;

   /**
    * Column to use for misc metadata.
    */
   private String sessionMetadataCol = DEFAULT_METADATA_COL;

   /**
    * Attribute column to use.
    */
   private String sessionAttributeCol = DEFAULT_ATTRIBUTE_COL;

   private String clearSql;

   private String sizeSql;

   private String insertSql;

   private String fullUpdateSql;

   private String keysSql;

   private String fullLoadSql;

   private String partialLoadSql;
   
   private String reinsertSql;

   private String removeSql;

   private String versionSql;

   private String timestampSql;

   private final Map<Connection, Set<Statement>> statementsByConnection = new ConcurrentHashMap<Connection, Set<Statement>>();

   private String simpleUpdateSql;

   private String attributeUpdateSql;

   private String metadataUpdateSql;

   private String cleanupSql;
   
   private final byte[] emptyAttributes;

   // -------------------------------------------------------- Constructors
   
   protected RDBMSStoreBase()
   {
      try
      {
         @SuppressWarnings("unchecked")
         Object empty = new SimpleCachableMarshalledValue(new HashMap());
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(empty);
         oos.close();
         emptyAttributes = baos.toByteArray();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Cannot serialize simple HashMap");
      }      
   }
   
   // ------------------------------------------------------------- Properties

   /**
    * Return the name for this Store, used for logging.
    */
   public abstract String getStoreName();

   /**
    * Return the info for this Store.
    */
   public abstract String getInfo();

   /**
    * Return the name for this instance (built from container name)
    */
   public String getName()
   {
      if (name == null)
      {
         throw new IllegalStateException("Must configure a name for PersistentStore");
      }
      return name;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * Return the username to use to connect to the database.
    *
    */
   public String getConnectionName()
   {
      return (this.connectionName);
   }

   /**
    * Set the username to use to connect to the database.
    *
    * @param connectionName Username
    */
   public void setConnectionName(String connectionName)
   {
      this.connectionName = connectionName;
   }

   /**
    * Return the password to use to connect to the database.
    *
    */
   public String getConnectionPassword()
   {
      return (this.connectionPassword);
   }

   /**
    * Set the password to use to connect to the database.
    *
    * @param connectionPassword User password
    */
   public void setConnectionPassword(String connectionPassword)
   {
      this.connectionPassword = connectionPassword;
   }

   /**
    * Set the table for this Store.
    *
    * @param sessionTable The new table
    */
   public void setSessionTable(String sessionTable)
   {
      this.sessionTable = sessionTable;
   }

   /**
    * Return the table for this Store.
    */
   public String getSessionTable()
   {
      return (this.sessionTable);
   }

   /**
    * Set the App column for the table.
    *
    * @param sessionAppCol the column name
    */
   public void setSessionAppCol(String sessionAppCol)
   {
      this.sessionAppCol = sessionAppCol;
   }

   /**
    * Return the web application name column for the table.
    */
   public String getSessionAppCol()
   {
      return (this.sessionAppCol);
   }

   /**
    * Return the name of the session id column for the table. This is where the 
    * core, immutable part of the session id is stored.
    */
   public String getSessionIdCol()
   {
      return (this.sessionIdCol);
   }

   /**
    * Set the name of the session id column for the table. This is where the 
    * core, immutable part of the session id is stored.
    *
    * @param sessionIdCol the column name
    */
   public void setSessionIdCol(String sessionIdCol)
   {
      this.sessionIdCol = sessionIdCol;
   }

   /**
    * Returns the name of the full session id column for the table. This is where
    * the full id including any mutable element (e.g. a jvmRoute) that is added 
    * to the {@link #getSessionIdCol() core session id} is stored.
    */
   public String getSessionFullIdCol()
   {
      return (this.sessionFullIdCol);
   }

   /**
    * Set the name of the full session id column for the table. This is where
    * the full id including any mutable element (e.g. a jvmRoute) that is added 
    * to the {@link #getSessionIdCol() core session id} is stored.
    *
    * @param sessionFullIdCol the column name
    */
   public void setSessionFullIdCol(String sessionFullIdCol)
   {
      this.sessionFullIdCol = sessionFullIdCol;
   }

   /**
    * Gets the name of the column where the session creation time is stored.
    * 
    * @return the column name
    */
   public String getSessionCreationTimeCol()
   {
      return (this.sessionCreationTimeCol);
   }

   /**
    * Sets the name of the column where the session creation time is stored.
    * 
    * param sessionCreationTimeCol the column name
    */
   public void setSessionCreationTimeCol(String sessionCreationTimeCol)
   {
      this.sessionCreationTimeCol = sessionCreationTimeCol;
   }

   /**
    * Return the Max Inactive column
    */
   public String getSessionMaxInactiveCol()
   {
      return (this.sessionMaxInactiveCol);
   }

   /**
    * Set the Max Inactive column for the table
    *
    * @param sessionMaxInactiveCol The column name
    */
   public void setSessionMaxInactiveCol(String sessionMaxInactiveCol)
   {
      this.sessionMaxInactiveCol = sessionMaxInactiveCol;
   }

   /**
    * Gets the name of "session is new" marker column
    * 
    * @return the column name
    */
   public String getSessionNewCol()
   {
      return (this.sessionNewCol);
   }

   /**
    * Sets the name of "session is new" marker column
    * 
    * @param sessionNewCol the column name
    */
   public void setSessionNewCol(String sessionNewCol)
   {
      this.sessionNewCol = sessionNewCol;
   }

   /**
    * Return the name of the session version column
    */
   public String getSessionVersionCol()
   {
      return (this.sessionVersionCol);
   }

   /**
    * Set the name of the session version column for the table
    *
    * @param sessionVersionCol The column name
    */
   public void setSessionVersionCol(String sessionVersionCol)
   {
      this.sessionVersionCol = sessionVersionCol;
   }

   /**
    * Return the name of the session last access timestamp column
    */
   public String getSessionLastAccessedCol()
   {
      return (this.sessionLastAccessedCol);
   }

   /**
    * Set the name of the session last access timestamp column for the table
    *
    * @param sessionLastAccessedCol The column name
    */
   public void setSessionLastAccessedCol(String sessionLastAccessedCol)
   {
      this.sessionLastAccessedCol = sessionLastAccessedCol;
   }

   /**
    * Return the Iname of the session validity marker column
    */
   public String getSessionValidCol()
   {
      return (this.sessionValidCol);
   }

   /**
    * Set the name of the session validity marker column for the table
    *
    * @param sessionValidCol The column name
    */
   public void setSessionValidCol(String sessionValidCol)
   {
      this.sessionValidCol = sessionValidCol;
   }

   /**
    * Return the name of the misc misc metadata storage column
    */
   public String getSessionMetadataCol()
   {
      return (this.sessionMetadataCol);
   }

   /**
    * Set the name of the misc metadata storage column for the table
    *
    * @param sessionValidCol The column name
    */
   public void setSessionMetadataCol(String sessionMetadataCol)
   {
      this.sessionMetadataCol = sessionMetadataCol;
   }

   /**
    * Return the attribute storage column for the table
    */
   public String getSessionAttributeCol()
   {
      return (this.sessionAttributeCol);
   }

   /**
    * Set the attribute storage column for the table
    *
    * @param sessionAttributeCol the column name
    */
   public void setSessionAttributeCol(String sessionAttributeCol)
   {
      this.sessionAttributeCol = sessionAttributeCol;
   }

   public int getCleanupInterval()
   {
      return cleanupInterval;
   }

   public void setCleanupInterval(int cleanupInterval)
   {
      this.cleanupInterval = cleanupInterval;
   }
   
   public int getMaxUnreplicatedInterval()
   {
      return maxUnreplicatedInterval;
   }

   public void setMaxUnreplicatedInterval(int maxUnreplicatedInterval)
   {
      this.maxUnreplicatedInterval = maxUnreplicatedInterval;
   }

   public boolean isStarted()
   {
      return started;
   }

   // --------------------------------------------------------- Public Methods

   
   // -------------------------------------------------------  PersistentStore

   public void clear()
   {
      RuntimeException exception = null;
      int numberOfTries = 2;
      while (numberOfTries-- > 0)
      {
         Connection _conn = safeGetConnection();
         boolean success = false;
         try
         {
            PreparedStatement preparedClearSql = prepareStatement(_conn, getClearSql());
            preparedClearSql.setString(1, getName());
            preparedClearSql.execute();

            _conn.commit();
            success = true;
            exception = null;
            break;
         }
         catch (SQLException e)
         {
            if (exception == null)
            {
               exception = new RuntimeException("Caught SQLException executing store clear", e);
            }
         }
         catch (RuntimeException e)
         {
            if (exception == null)
            {
               exception = e;
            }
         }
         finally
         {
            try
            {
               if (!success)
               {
                  cleanup(_conn, null, true);
               }
            }
            finally
            {
               releaseConnection(_conn);
            }
         }
      }
      
      if (exception != null)
      {
         throw exception;
      }
   }

   public int getSize()
   {
      int size = 0;
      ResultSet rst = null;
      RuntimeException exception = null;
      
      int numberOfTries = 2;
      while (numberOfTries-- > 0)
      {
         Connection _conn = safeGetConnection();
         boolean success = false;
         try
         {
            PreparedStatement preparedSizeSql = prepareStatement(_conn, getSizeSql());
            preparedSizeSql.setString(1, getName());
            rst = preparedSizeSql.executeQuery();
            if (rst.next())
            {
               size = rst.getInt(1);
            }

            _conn.commit();
            success = true;
            exception = null;
            break;
         }
         catch (SQLException e)
         {
            if (exception == null)
            {
               exception = new RuntimeException("Caught SQLException getting store size", e);
            }
         }
         catch (RuntimeException e)
         {
            if (exception == null)
            {
               exception = e;
            }
         }
         finally
         {
            try
            {
               if (!success)
               {
                  cleanup(_conn, rst, true);
               }
               else if (rst != null)
               {
                  rst.close();
               }
            }
            catch (SQLException e)
            {
               ;
            }
            finally
            {
               releaseConnection(_conn);
            }
         }
      }
      
      if (exception != null)
      {
         throw exception;
      }
      
      return (size);
   }

   public Set<String> getSessionIds()
   {
      ResultSet rst = null;
      Set<String> keys = null;
      RuntimeException exception = null;
      int numberOfTries = 2;
      while (numberOfTries-- > 0)
      {
         Connection _conn = safeGetConnection();
         boolean success = true;
         try
         {
            PreparedStatement preparedKeysSql = prepareStatement(_conn, getKeysSql());
            preparedKeysSql.setString(1, getName());
            rst = preparedKeysSql.executeQuery();
            keys = new HashSet<String>();
            if (rst != null)
            {
               while (rst.next())
               {
                  keys.add(rst.getString(1));
               }
            }

            _conn.commit();
            success = true;
            exception = null;
            
            break;
         }
         catch (SQLException e)
         {
            if (exception == null)
            {
               exception = new RuntimeException("Caught SQLException getting session ids", e);
            }
         }
         catch (RuntimeException e)
         {
            if (exception == null)
            {
               exception = e;
            }
         }
         finally
         {
            try
            {
               if (!success)
               {
                  cleanup(_conn, rst, true);
               }
               else if (rst != null)
               {
                  rst.close();
               }
            }
            catch (SQLException e)
            {
               ;
            }
            finally
            {
               releaseConnection(_conn);
            }
         }
      }
      
      if (exception != null)
      {
         throw exception;
      }

      return (keys);
   }

   public IncomingDistributableSessionData getSessionData(String realId, boolean includeAttributes)
   {
      ResultSet rst = null;
      IncomingDistributableSessionData incomingSession = null;
      ObjectInputStream attributes_ois = null;

      RuntimeException exception = null;
      
      int numberOfTries = 2;
      while (numberOfTries-- > 0)
      {
         Connection _conn = safeGetConnection();
         boolean success = false;
         try
         {
            String sql = includeAttributes ? getFullLoadSql() : getPartialLoadSql();
            PreparedStatement preparedLoadSql = prepareStatement(_conn, sql);
            preparedLoadSql.setString(1, realId);
            preparedLoadSql.setString(2, getName());
            rst = preparedLoadSql.executeQuery();
            if (rst.next())
            {
               if (getLogger().isTraceEnabled())
               {
                  getLogger().trace("Loading session " + maskId(realId));
               }

               DistributableSessionMetadata metadata = new DistributableSessionMetadata();

               metadata.setId(rst.getString(1));
               metadata.setCreationTime(rst.getLong(2));
               String isNew = rst.getString(3);
               metadata.setNew("1".equals(isNew));
               metadata.setMaxInactiveInterval(rst.getInt(4));
               String valid = rst.getString(7);
               metadata.setValid("1".equals(valid));
//               metadata.setValid(true);

               Integer version = Integer.valueOf(rst.getInt(5));
               Long timestamp = Long.valueOf(rst.getLong(6));

               Map<String, Object> attributes = null;
               if (includeAttributes)
               {
                  BufferedInputStream attributes_bis = new BufferedInputStream(rst.getBinaryStream(8));
                  attributes_ois = new ObjectInputStream(attributes_bis);                  
                  SimpleCachableMarshalledValue mv = (SimpleCachableMarshalledValue) attributes_ois.readObject();
                  mv.setObjectStreamSource(SessionSerializationFactory.getObjectStreamSource());

                  attributes = uncheckedCast(mv.get());
               }

               incomingSession = new IncomingDistributableSessionDataImpl(version, timestamp, metadata, attributes);

            }
            else if (getLogger().isTraceEnabled())
            {
               getLogger().trace(getStoreName() + ": No persisted data object found");
            }

            _conn.commit();
            success = true;
            exception = null;
            break;
         }
         catch (SQLException e)
         {
            if (exception == null)
            {
               exception = new RuntimeException("Caught SQLException loading session " + maskId(realId), e);
            }
         }
         catch (IOException e)
         {
            if (exception == null)
            {
               exception = new RuntimeException("Caught IOException loading session " + maskId(realId), e);
            }
         }
         catch (ClassNotFoundException e)
         {
            if (exception == null)
            {
               exception = new RuntimeException("Caught ClassNotFoundException loading session " + maskId(realId), e);
            }
         }
         catch (RuntimeException e)
         {
            if (exception == null)
            {
               exception = e;
            }
         }
         finally
         {
            try
            {
               if (!success)
               {
                  cleanup(_conn, rst, true);
               }
               else if (rst != null)
               {
                  rst.close();
               }

               if (attributes_ois != null)
               {
                  try
                  {
                     attributes_ois.close();
                  }
                  catch (IOException e)
                  {
                     ;
                  }
               }
            }
            catch (SQLException e)
            {
               ;
            }
            finally
            {
               releaseConnection(_conn);
            }
         }
      }
      
      if (exception != null)
      {
         throw exception;
      }

      return (incomingSession);
   }

   public void remove(String realId)
   {
      if (getLogger().isTraceEnabled())
      {
         getLogger().trace("Loading session " + maskId(realId));
      }
      
      RuntimeException exception = null;
      int numberOfTries = 2;
      while (numberOfTries-- > 0)
      {
         Connection _conn = safeGetConnection();
         boolean success = false;
         try
         {
            executeRemove(realId, _conn);

            _conn.commit();
            success = true;
            exception = null;
            break;
         }
         catch (SQLException e)
         {
            if (exception == null)
            {
               exception = new RuntimeException("Caught SQLException removing session " + maskId(realId), e);
            }
         }
         catch (RuntimeException e)
         {
            if (exception == null)
            {
               exception = e;
            }
         }
         finally
         {
            try
            {
               if (!success)
               {
                  cleanup(_conn, null, true);
               }
            }
            finally
            {
               releaseConnection(_conn);
            }
         }
      }
      
      if (exception != null)
      {
         throw exception;
      }
   }

   public void storeSessionData(OutgoingSessionGranularitySessionData sessionData)
   {
      if (getLogger().isTraceEnabled())
      {
         getLogger().trace("Storing session " + maskId(sessionData));
      }
      
      RuntimeException exception = null;
      ObjectOutputStream oos = null;
      int numberOfTries = 2;
      while (numberOfTries-- > 0)
      {
         boolean success = false;
         Connection _conn = safeGetConnection();
         if (_conn == null)
         {
            return;
         }

         try
         {
            byte[] obs = writeSessionAttributes(sessionData);

            DistributableSessionMetadata metadata = sessionData.getMetadata();
            if (metadata != null && metadata.isNew())
            {
               try
               {
                  executeInsert(sessionData, obs, _conn);
               }
               catch (SQLException e)
               {                  
                  // See if this is due to pre-existing record
                  if (getLogger().isTraceEnabled())
                  {
                     getLogger().trace("Caught SQLException inserting session " + maskId(sessionData), e);
                  }
                  
                  // The existing connection is no good now; need a new one
                  cleanup(_conn, null, true);
                  _conn = null;
                  _conn = safeGetConnection();
                  if (obs != null && executeGetSessionVersion(_conn, sessionData.getRealId()) != null)
                  {
                     executeReInsert(sessionData, obs, _conn);
                  }
                  else
                  {
                     throw e;
                  }
               }
            }
            else
            {
               int count = executeUpdate(sessionData, obs, _conn);
               if (count < 1)
               {
                  // For whatever reason this doesn't exist                  
                  if (metadata != null && obs != null)
                  {
                     executeInsert(sessionData, obs, _conn);
                  }
                  else
                  {
                     // Hmm, we don't have enough data for a full insert
                     throw new IllegalStateException("Cannot insert session " + maskId(sessionData) + " as session metadata is not available");
                  }
               }
            }

            _conn.commit();
            success = true;
            exception = null;
            break;
         }
         catch (SQLException e)
         {
            if (exception == null)
            {
               exception = new RuntimeException("Caught SQLException storing session " +  maskId(sessionData), e);
            }
         }
         catch (IOException e)
         {
            if (exception == null)
            {
               exception = new RuntimeException("Caught IOException storing session " +  maskId(sessionData), e);
            }
         }
         catch (RuntimeException e)
         {
            if (exception == null)
            {
               exception = e;
            }
         }
         finally
         {
            try
            {
               if (!success)
               {
                  cleanup(_conn, null, true);
               }

               if (oos != null)
               {
                  try
                  {
                     oos.close();
                  }
                  catch (IOException ignored)
                  {
                     ;
                  }
               }
            }
            finally
            {
               releaseConnection(_conn);
            }
         }
      }
      
      if (exception != null)
      {
         throw exception;
      }
   }
   
   private static String maskId(OutgoingSessionGranularitySessionData sessionData)
   {
      String realId = (sessionData == null ? null : sessionData.getRealId());
      return maskId(realId);
   }
   
   private static String maskId(String realId)
   {
      if (realId == null)
      {
         return null;
      }
      else
      {
         int length = realId.length();
         if (length <= 8)
         {
            return realId;
         }
         StringBuilder sb = new StringBuilder(realId.substring(0, 2));
         sb.append("****");
         sb.append(realId.substring(length - 6, length));
         return sb.toString();
      }
   }

   public Long getSessionTimestamp(String realId)
   {
      ResultSet rst = null;
      Long result = null;
      RuntimeException exception = null;
      int numberOfTries = 2;   
      while (numberOfTries-- > 0)
      {
         boolean success = false;
         Connection _conn = safeGetConnection();
         try
         {
            PreparedStatement preparedTimestampSql = prepareStatement(_conn, getTimestampSql());
            preparedTimestampSql.setString(1, realId);
            preparedTimestampSql.setString(2, getName());
            rst = preparedTimestampSql.executeQuery();
            if (rst.next())
            {
               result = Long.valueOf(rst.getLong(1));
            }

            _conn.commit();
            success = true;
            exception = null;
            break;
         }
         catch (SQLException e)
         {
            if (exception == null)
            {
               exception = new RuntimeException("Caught SQLException getting timestamp for session " +  maskId(realId), e);
            }
         }
         catch (RuntimeException e)
         {
            if (exception == null)
            {
               exception = e;
            }
         }
         finally
         {
            try
            {
               if (!success)
               {
                  cleanup(_conn, rst, true);
               }
               else if (rst != null)
               {
                  try
                  {
                     rst.close();
                  }
                  catch (SQLException e)
                  {
                     ;
                  }
               }
            }
            finally
            {
               releaseConnection(_conn);
            }
         }
      }
      
      if (exception != null)
      {
         throw exception;
      }
      
      return result;
   }

   public Integer getSessionVersion(String realId)
   {
      
      Integer result = null;
      RuntimeException exception = null;
      int numberOfTries = 2;   
      while (numberOfTries-- > 0)
      {
         boolean success = false;
         Connection _conn = safeGetConnection();   
         try
         {
            result = executeGetSessionVersion(_conn, realId);
            _conn.commit();
            success = true;
            exception = null;
            break;
         }
         catch (SQLException e)
         {
            if (exception == null)
            {
               exception = new RuntimeException("Caught SQLException getting version for session " +  maskId(realId), e);
            }
         }
         catch (RuntimeException e)
         {
            if (exception == null)
            {
               exception = e;
            }
         }
         finally
         {
            try
            {
               if (!success)
               {
//                  cleanup(_conn, rst, true);
                  cleanup(_conn, null, true);
               }
//               else if (rst != null)
//               {
//                  try
//                  {
//                     rst.close();
//                  }
//                  catch (SQLException e)
//                  {
//                     ;
//                  }
//               }
            }
            finally
            {
               releaseConnection(_conn);
            }
         }
      }
      
      if (exception != null)
      {
         throw exception;
      }
      
      return result;
   }

   public void processExpires()
   {
      long now = System.currentTimeMillis();
      long interval = cleanupInterval * 1000;
      long earliest = now - interval;
      if (earliest > lastCleanup)
      {
         long maxUnrep = this.maxUnreplicatedInterval < 0 ? 60000 : this.maxUnreplicatedInterval * 1000;
         Connection _conn = safeGetConnection();
         boolean success = false;
         try
         {
            PreparedStatement preparedCleanupSql = prepareStatement(_conn, getCleanupSql());
            preparedCleanupSql.setString(1, getName());
            preparedCleanupSql.setLong(2, earliest);
            preparedCleanupSql.setLong(3, (now - maxUnrep));
            preparedCleanupSql.execute();

            _conn.commit();
            lastCleanup = now;
            success = true;
         }
         catch (Exception e)
         {
            getLogger().error("Caught exception cleaning out expired sessions", e); 
         }
         finally
         {
            try
            {
               if (!success)
               {
                  cleanup(_conn, null, true);
               }
            }
            finally
            {
               releaseConnection(_conn);
            }
         }        
      }
   }

   public void start()
   {
      // Validate and update our current component state
      if (started)
         throw new IllegalStateException(getStoreName() + " is already started");

      getName();

      createSql();

      startStore();

      started = true;
   }

   public void stop()
   {
      // Validate and update our current component state
      if (!started)
      {
         throw new IllegalStateException(getStoreName() + " is not started");
      }

      started = false;

   }

   // --------------------------------------------------------------  Protected

   /** 
    * Hook for subclasses to perform any needed startup work.
    */
   protected abstract void startStore();

   /**
    * Returns a connection. Calls to this method
    * must be paired (typically via a try/finally block) with a call
    * to {@link #releaseConnection(Connection)}.
    * 
    * @return the connection
    * 
    * @throws SQLException if a database access error occurs
    * @throws RuntimeException if a connection could not be obtained
    */
   protected abstract Connection getConnection() throws SQLException;

   /**
    * Releases a connection obtained from {@link #getConnection()}.
    * 
    * @param conn the connection
    */
   protected abstract void releaseConnection(Connection conn);

   /**
    * Clean up a connection, any associated statements, and an associated
    * result set.
    * 
    * @param conn the connection. Null is handled but isn't sensible
    * @param resultSet the result set, which may be null
    * @param rollback whether {@link Connection#rollback()} should be invoked on the connection
    */
   protected void cleanup(Connection conn, ResultSet resultSet, boolean rollback)
   {
      if (conn != null)
      {
         if (resultSet != null)
         {
            try
            {
               resultSet.close();
            }
            catch (SQLException e)
            {
               getLogger().warn("Caught SQLException closing a result set -- " + e.getLocalizedMessage()); // Just log it here
            }
         }

         if (rollback)
         {
            try
            {
               conn.rollback();
            }
            catch (SQLException e)
            {
               if (getLogger().isTraceEnabled())
               {
                  getLogger().trace("Caught SQLException rolling back connection -- " + e.getLocalizedMessage(), e);
               }
            }
         }

         Set<Statement> stmts = statementsByConnection.remove(conn);
         if (stmts != null)
         {
            for (Statement stmt : stmts)
            {
               try
               {
                  stmt.close();
               }
               catch (SQLException e)
               {
                  getLogger().debug("Caught SQLException closing statement -- " + e.getLocalizedMessage());
               }
            }
         }

         // Close this database connection, and log any errors
         try
         {
            conn.close();
         }
         catch (SQLException e)
         {
            getLogger().error("Caught SQLException closing connection -- " + e.getLocalizedMessage()); // Just log it here
         }
      }
   }
   
   protected Logger getLogger()
   {
      return logger == null ? LOG : logger;
   }

   // ----------------------------------------------------------------  Private

   /**
    * Establishes the SQL strings returned by the various <code>getXyzSql()</code>
    * methods.
    */
   private void createSql()
   {
      this.clearSql = "DELETE FROM " + getSessionTable() + " WHERE " + getSessionAppCol() + " = ?";

      this.keysSql = "SELECT " + getSessionIdCol() + " FROM " + getSessionTable() + " WHERE " + getSessionAppCol()
            + " = ?";
      
      this.sizeSql = "SELECT COUNT(" + getSessionIdCol() + ") " +
      		"        FROM " + getSessionTable() + 
      		        " WHERE " + getSessionAppCol() + " = ?";

      this.fullLoadSql = "SELECT " + getSessionFullIdCol() + ", " + getSessionCreationTimeCol() + ", "
            + getSessionNewCol() + ", " + getSessionMaxInactiveCol() + ", " + getSessionVersionCol() + ", "
            + getSessionLastAccessedCol() + ", " + getSessionValidCol() + ", " + getSessionAttributeCol() + " FROM "
            + getSessionTable() + " WHERE " + getSessionIdCol() + " = ? AND " + getSessionAppCol() + " = ?";

      this.partialLoadSql = "SELECT " + getSessionFullIdCol() + ", " + getSessionCreationTimeCol() + ", "
            + getSessionNewCol() + ", " + getSessionMaxInactiveCol() + ", " + getSessionVersionCol() + ", "
            + getSessionLastAccessedCol() + ", " + getSessionValidCol() + " FROM " + getSessionTable() + " WHERE "
            + getSessionIdCol() + " = ? AND " + getSessionAppCol() + " = ?";

      this.removeSql = "DELETE FROM " + getSessionTable() + " WHERE " + getSessionIdCol() + " = ? AND "
            + getSessionAppCol() + " = ?";

      this.insertSql = "INSERT INTO " + getSessionTable() + " (" + getSessionAppCol() + ", " + getSessionIdCol() + ", "
            + getSessionFullIdCol() + ", " + getSessionCreationTimeCol() + ", " + getSessionNewCol() + ", "
            + getSessionMaxInactiveCol() + ", " + getSessionVersionCol() + ", " + getSessionLastAccessedCol() + ", "
            + getSessionValidCol() + ", " + getSessionAttributeCol() + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

      this.simpleUpdateSql = "UPDATE " + getSessionTable() + " SET " + getSessionVersionCol() + " = ?, "
            + getSessionLastAccessedCol() + " = ?" + " WHERE " + getSessionIdCol() + " = ? AND " + getSessionAppCol()
            + " = ?";

      this.attributeUpdateSql = "UPDATE " + getSessionTable() + " SET " + getSessionVersionCol() + " = ?, "
            + getSessionLastAccessedCol() + " = ?, " + getSessionAttributeCol() + " = ?" + " WHERE "
            + getSessionIdCol() + " = ? AND " + getSessionAppCol() + " = ?";

      this.metadataUpdateSql = "UPDATE " + getSessionTable() + " SET " + getSessionVersionCol() + " = ?, "
            + getSessionLastAccessedCol() + " = ?, " + getSessionFullIdCol() + " = ?, " + getSessionNewCol() + " = ?, "
            + getSessionMaxInactiveCol() + " = ?, " + getSessionValidCol() + " = ?" + " WHERE " + getSessionIdCol()
            + " = ? AND " + getSessionAppCol() + " = ?";

      this.fullUpdateSql = "UPDATE " + getSessionTable() + " SET " + getSessionVersionCol() + " = ?, "
            + getSessionLastAccessedCol() + " = ?, " + getSessionFullIdCol() + " = ?, " + getSessionNewCol() + " = ?, "
            + getSessionMaxInactiveCol() + " = ?, " + getSessionValidCol() + " = ?, " + getSessionAttributeCol()
            + " = ?" + " WHERE " + getSessionIdCol() + " = ? AND " + getSessionAppCol() + " = ?";

      this.reinsertSql = "UPDATE " + getSessionTable() + " SET " + getSessionVersionCol() + " = ?, "
            + getSessionLastAccessedCol() + " = ?, " + getSessionFullIdCol() + " = ?, " + getSessionNewCol() + " = ?, "
            + getSessionMaxInactiveCol() + " = ?, " + getSessionValidCol() + " = ?, " + getSessionAttributeCol()
            + " = ?, " +  getSessionCreationTimeCol() + " = ? WHERE " + getSessionIdCol() + " = ? AND " + getSessionAppCol() + " = ?";

      this.timestampSql = "SELECT " + getSessionLastAccessedCol() + " FROM " + getSessionTable() + " WHERE "
            + getSessionIdCol() + " = ? AND " + getSessionAppCol() + " = ?";

      this.versionSql = "SELECT " + getSessionVersionCol() + " FROM " + getSessionTable() + " WHERE "
            + getSessionIdCol() + " = ? AND " + getSessionAppCol() + " = ?";
      
      this.cleanupSql = "DELETE FROM " + getSessionTable() + " WHERE " + getSessionAppCol() + " = ?" +
                   " AND " + getSessionLastAccessedCol() + " < ? AND " + getSessionLastAccessedCol() + " < (? - (" +
                   getSessionMaxInactiveCol() + " * 1000))";
   }

   private void executeRemove(String id, Connection _conn) throws SQLException
   {
      PreparedStatement preparedRemoveSql = prepareStatement(_conn, getRemoveSql());
      preparedRemoveSql.setString(1, id);
      preparedRemoveSql.setString(2, getName());
      preparedRemoveSql.execute();
   }

   private void executeInsert(OutgoingSessionGranularitySessionData session, byte[] obs, Connection conn)
         throws SQLException, IOException
   {
      if (obs == null)
      {
         obs = this.emptyAttributes;
      }

      DistributableSessionMetadata metadata = session.getMetadata();
      if (metadata == null)
      {
         throw new IllegalStateException("Cannot insert session " + maskId(session) + " as session metadata is missing");
      }

      int size = obs.length;
      ByteArrayInputStream bis = new ByteArrayInputStream(obs);
      InputStream in = new BufferedInputStream(bis, size);

      try
      {
         PreparedStatement preparedInsertSql = prepareStatement(conn, getInsertSql());
         preparedInsertSql.setString(1, getName());
         preparedInsertSql.setString(2, session.getRealId());
         preparedInsertSql.setString(3, metadata.getId());
         preparedInsertSql.setLong(4, metadata.getCreationTime());
         preparedInsertSql.setString(5, metadata.isNew() ? "1" : "0");
         preparedInsertSql.setInt(6, metadata.getMaxInactiveInterval());
         preparedInsertSql.setInt(7, session.getVersion());
         preparedInsertSql.setLong(8, session.getTimestamp());
         preparedInsertSql.setString(9, metadata.isValid() ? "1" : "0");
         preparedInsertSql.setBinaryStream(10, in, size);
         preparedInsertSql.execute();
      }
      finally
      {
         in.close();
      }
   }

   private int executeReInsert(OutgoingSessionGranularitySessionData session, byte[] obs, Connection conn)
         throws SQLException, IOException
   {
      DistributableSessionMetadata metadata = session.getMetadata();
      int size = obs.length;
      InputStream in = null;
      if (obs != null)
      {
         ByteArrayInputStream bis = new ByteArrayInputStream(obs);
         in = new BufferedInputStream(bis, size);
      }

      try
      {
         PreparedStatement preparedUpdateSql = prepareStatement(conn, getReInsertSql());
         preparedUpdateSql.setInt(1, session.getVersion());
         preparedUpdateSql.setLong(2, session.getTimestamp());
         preparedUpdateSql.setString(3, metadata.getId());
         preparedUpdateSql.setString(4, metadata.isNew() ? "1" : "0");
         preparedUpdateSql.setInt(5, metadata.getMaxInactiveInterval());
         preparedUpdateSql.setString(6, metadata.isValid() ? "1" : "0");
         preparedUpdateSql.setBinaryStream(7, in, size);
         preparedUpdateSql.setLong(8, metadata.getCreationTime());

         // Add in the WHERE clause params
         preparedUpdateSql.setString(9, session.getRealId());
         preparedUpdateSql.setString(10, getName());
         int count = preparedUpdateSql.executeUpdate();

         return count;
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }
   }

   private int executeUpdate(OutgoingSessionGranularitySessionData session, byte[] obs, Connection conn)
         throws SQLException, IOException
   {
      DistributableSessionMetadata metadata = session.getMetadata();
      int size = obs == null ? -1 : obs.length;
      InputStream in = null;
      if (obs != null)
      {
         ByteArrayInputStream bis = new ByteArrayInputStream(obs);
         in = new BufferedInputStream(bis, size);
      }

      try
      {
         PreparedStatement preparedUpdateSql = null;
         int idParam = -1; // first parameter in the WHERE clause
         if (metadata != null)
         {
            if (obs != null)
            {
               preparedUpdateSql = prepareStatement(conn, getFullUpdateSql());
               preparedUpdateSql.setBinaryStream(7, in, size);
               idParam = 8;
            }
            else
            {
               preparedUpdateSql = prepareStatement(conn, getMetadataUpdateSql());
               idParam = 7;
            }

            preparedUpdateSql.setString(3, metadata.getId());
            preparedUpdateSql.setString(4, metadata.isNew() ? "1" : "0");
            preparedUpdateSql.setInt(5, metadata.getMaxInactiveInterval());
            preparedUpdateSql.setString(6, metadata.isValid() ? "1" : "0");
         }
         else if (obs != null)
         {
            preparedUpdateSql = prepareStatement(conn, getAttributeUpdateSql());
            preparedUpdateSql.setBinaryStream(3, in, size);
            idParam = 4;
         }
         else
         {
            preparedUpdateSql = prepareStatement(conn, getSimpleUpdateSql());
            idParam = 3;
         }

         // Add in the version and timestamp
         preparedUpdateSql.setInt(1, session.getVersion());
         preparedUpdateSql.setLong(2, session.getTimestamp());
         // Add in the WHERE clause params
         preparedUpdateSql.setString(idParam, session.getRealId());
         preparedUpdateSql.setString(idParam + 1, getName());
         int count = preparedUpdateSql.executeUpdate();

         return count;
      }
      finally
      {
         if (in != null)
         {
            in.close();
         }
      }
   }
   
   private Integer executeGetSessionVersion(Connection _conn, String realId) throws SQLException
   {
      PreparedStatement preparedTimestampSql = prepareStatement(_conn, getVersionSql());
      preparedTimestampSql.setString(1, realId);
      preparedTimestampSql.setString(2, getName());
      ResultSet rst = null;
      try
      {
         Integer result = null;
         rst = preparedTimestampSql.executeQuery();
         if (rst.next())
         {
            result = Integer.valueOf(rst.getInt(1));
         }
         return result;
      }
      finally
      {
         if (rst != null)
         {
            rst.close();
         }
      }
   }

   private String getCleanupSql()
   {
      return cleanupSql;
   }

   private String getClearSql()
   {
      return clearSql;
   }

   private String getInsertSql()
   {
      return insertSql;
   }

   private String getFullUpdateSql()
   {
      return fullUpdateSql;
   }

   private String getReInsertSql()
   {
      return reinsertSql;
   }

   private String getSimpleUpdateSql()
   {
      return simpleUpdateSql;
   }

   private String getMetadataUpdateSql()
   {
      return metadataUpdateSql;
   }

   private String getAttributeUpdateSql()
   {
      return attributeUpdateSql;
   }

   private String getKeysSql()
   {
      return keysSql;
   }

   private String getFullLoadSql()
   {
      return fullLoadSql;
   }

   private String getPartialLoadSql()
   {
      return this.partialLoadSql;
   }

   private String getRemoveSql()
   {
      return removeSql;
   }

   private String getSizeSql()
   {
      return sizeSql;
   }

   private String getVersionSql()
   {
      return versionSql;
   }

   private String getTimestampSql()
   {
      return timestampSql;
   }

   private Connection safeGetConnection()
   {
      try
      {
         return getConnection();
      }
      catch (SQLException e)
      {
         throw new RuntimeException("Caught SQLException getting a connection", e);
      }      
   }

   private PreparedStatement prepareStatement(Connection conn, String sql) throws SQLException
   {
      Set<Statement> stmts = statementsByConnection.get(conn);
      if (stmts == null)
      {
         stmts = new HashSet<Statement>();
         statementsByConnection.put(conn, stmts);
      }
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmts.add(stmt);
      return stmt;
   }

   private byte[] writeSessionAttributes(OutgoingSessionGranularitySessionData session) throws IOException
   {
      Map<String, Object> attrs = session.getSessionAttributes();
      if (attrs == null)
      {
         return null;
      }

      ObjectOutputStream oos = null;
      ByteArrayOutputStream bos = null;

      try
      {
         bos = new ByteArrayOutputStream();
         oos = new ObjectOutputStream(new BufferedOutputStream(bos));

         oos.writeObject(new SimpleCachableMarshalledValue((Serializable) attrs));
         oos.close();
         return bos.toByteArray();
      }
      finally
      {
         if (oos != null)
         {
            oos.close();
         }
         if (bos != null)
         {
            bos.close();
         }
      }
   }

   @SuppressWarnings("unchecked")
   private static <T> T uncheckedCast(Object obj)
   {
      return (T) obj;
   }
}
