/**
 * 
 */
package org.jboss.web.tomcat.service.session.persistent;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class DriverManagerPersistentStore extends RDBMSStoreBase
{
   
   private static final String storeName = DriverManagerPersistentStore.class.getSimpleName();
   /**
       * The descriptive information about this implementation.
       */
   private static final String info = storeName + "/1.0";

   // ------------------------------------------------------ Instance Fields

   /**
    * Connection string to use when connecting to the DB.
    */
   private String connectionURL = null;

   private Lock lock = new ReentrantLock();
   
   /**
    * The database connection.
    */
   private volatile Connection dbConnection = null;

   /**
    * Instance of the JDBC Driver class we use as a connection factory.
    */
   private Driver driver = null;

   /**
    * Driver to use.
    */
   private String driverName = null;

   // ------------------------------------------------------------ Properties
   
   public String getStoreName()
   {
      return storeName;
   }
   
   public String getConnectionURL()
   {
      return connectionURL;
   }

   public void setConnectionURL(String connectionURL)
   {
      this.connectionURL = connectionURL;
   }

   public String getDriverName()
   {
      return driverName;
   }

   public void setDriverName(String driverName)
   {
      this.driverName = driverName;
   }

   @Override
   protected Connection getConnection() throws SQLException
   {
      try
      {
         lock.lockInterruptibly();
      }
      catch (InterruptedException e)
      {
         Thread.currentThread().interrupt();
         throw new RuntimeException("Interrupted while acquiring connection lock");
      }
      
      // Do nothing if there is a database connection already open
      if (dbConnection == null)
      {
         open();
      }
      
      return dbConnection;
   }

   @Override
   protected void cleanup(Connection conn, ResultSet resultSet, boolean rollback)
   {
      this.dbConnection = null;
      super.cleanup(conn, resultSet, rollback);
   }

   @Override
   protected void releaseConnection(Connection dbConnection)
   {
      lock.unlock();
   }

   @Override
   public String getInfo()
   {
      return info;
   }

   @Override
   protected void startStore()
   {
      try
      {
         Class<?> clazz = Class.forName(driverName);
         driver = (Driver) clazz.newInstance();
      }
      catch (RuntimeException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         throw new RuntimeException("Caught exception creating driver of class " + driverName, ex);
      }
      
      try
      {
         lock.lockInterruptibly();
      }
      catch (InterruptedException e)
      {
         Thread.currentThread().interrupt();
         throw new RuntimeException("Interrupted while acquiring connection lock");
      }
      
      try
      {
         open();
      }
      catch (SQLException e)
      {
         throw new RuntimeException("Caught SQLException while opening database connection -- " + e.toString());
      }
      finally
      {
         lock.unlock();
      }

   }
   
   private Connection open() throws SQLException {

      synchronized (this)
      {
         // Double-checked locking is ok since dbConnection is volatile
         if (dbConnection != null)
            return (dbConnection);
         
         // Open a new connection
         Properties props = new Properties();
         if (getConnectionName() != null)
             props.put("user", getConnectionName());
         if (getConnectionPassword() != null)
             props.put("password", getConnectionPassword());
         dbConnection = driver.connect(connectionURL, props);
         dbConnection.setAutoCommit(true);
         return (dbConnection);
      }
  }

}
