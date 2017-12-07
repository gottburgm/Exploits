/**
 * 
 */
package org.jboss.web.tomcat.service.session.persistent;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.catalina.Store;

/**
 * A {@link Store} that uses a {@link DataSource} to obtain connections
 * for persisting sessions to a relational database.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class DataSourcePersistentStore extends RDBMSStoreBase
{
   /**
    * The descriptive information about this implementation.
    */
   private static final String info = "DataSourceStore/1.0";

   private static final String storeName = "DataSourceStore";
   
   // --------------------------------------------------------- Instance Fields
   
   private String jndiName;


   private DataSource injecteddatasource;
   private DataSource datasource;

   /**
    * Creates a new DataSourcePersistentStore.
    */
   public DataSourcePersistentStore()
   {
      
   }

   /**
    * Creates a new DataSourcePersistentStore that uses the given DataSource.
    * This constructor is intended for testing.
    * 
    * @param datasource the datasource
    */
   public DataSourcePersistentStore(DataSource datasource)
   {
      this.injecteddatasource = datasource;
   }
   
   
   // ------------------------------------------------------------- Properties

   public String getDataSourceJndiName()
   {
      return jndiName;
   }

   public void setDataSourceJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   @Override
   public String getStoreName()
   {
      return (storeName);
   }

   // -------------------------------------------------------------- Protected

   @Override
   protected Connection getConnection() throws SQLException
   {
      try
      {
         Connection conn = null;
         if (getConnectionName() != null)
         {
            conn = datasource.getConnection(getConnectionName(), getConnectionPassword());
         }
         else
         {
            conn = datasource.getConnection();
         }
         
         conn.setAutoCommit(false);
         return conn;
      }
      catch (SQLException e)
      {
         // perhaps our datasource has been restarted? Reacquire in case
         try
         {
            findDataSource();
         }
         catch (Exception e1)
         {
            getLogger().error("Caught exception reacquiring datasource", e1);
         }
         throw e;
      }      
   }

   @Override
   public String getInfo()
   {
      return info;
   }

   @Override
   protected void releaseConnection(Connection conn)
   {
      cleanup(conn, null, false);
   }

   @Override
   protected void startStore()
   {
      findDataSource();
   }
   
   private void findDataSource()
   {
      if (injecteddatasource == null)
      {
         if (jndiName == null)
         {
            throw new IllegalStateException("No jndiName has been configured");
         }
   
         try
         {
            datasource = (DataSource) new InitialContext().lookup(jndiName);
            
            getLogger().debug("DataSource found at " + jndiName);
         }
         catch (NamingException e)
         {
            throw new IllegalStateException("Caught NamingException looking up DataSource at " + jndiName + " -- " + e
                  .getLocalizedMessage());
         }
      }
      else
      {
         datasource = injecteddatasource;
      }
      
   }

}
