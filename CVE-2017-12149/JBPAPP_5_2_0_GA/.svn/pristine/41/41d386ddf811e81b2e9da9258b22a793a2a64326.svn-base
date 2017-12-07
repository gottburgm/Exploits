/**
 * 
 */
package org.jboss.test.cluster.web.persistent;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.Statement;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 *
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public class PersistentStoreTableSetup
{
   private static final String DEFAULT_DS = "java:DefaultDS";
   
   private static final String DEFAULT_DROP_DDL = "DROP TABLE httpsessions IF EXISTS";
   
   private static final String DEFAULT_CREATE_DDL = "CREATE TABLE httpsessions (" +
   		"app VARCHAR(255) NOT NULL, " +
   		"id VARCHAR(255) NOT NULL, " +
   		"fullId VARCHAR(255) NOT NULL, " +
   		"creationtime BIGINT NOT NULL, " +
   		"maxinactive BIGINT NOT NULL, " +
   		"version INT NOT NULL, " +
   		"lastaccess BIGINT NOT NULL, " +
   		"isnew CHAR(1) NOT NULL, " +
   		"valid CHAR(1) NOT NULL, " +
   		"metadata VARBINARY NULL, " +
   		"attributes LONGVARBINARY NOT NULL " +
   		", CONSTRAINT app_id PRIMARY KEY (app, id)" +
   		")";
   // --------------------------------------------------------- Instance Fields
   
   private String jdbcURL;
   
   private String jndiName;
   
   private String createTableDDL;
   
   private String dropTableDDL;

   // ------------------------------------------------------------- Properties

   public String getDataSourceJndiName()
   {
      return jndiName == null ? DEFAULT_DS : jndiName;
   }

   public void setDataSourceJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   public String getJdbcURL()
   {
      return jdbcURL;
   }

   public void setJdbcURL(String jdbcURL)
   {
      this.jdbcURL = jdbcURL;
   }

   public String getCreateTableDDL()
   {
      return createTableDDL == null ? DEFAULT_CREATE_DDL : createTableDDL;
   }

   public void setCreateTableDDL(String createTableDDL)
   {
      this.createTableDDL = createTableDDL;
   }

   public String getDropTableDDL()
   {
      return dropTableDDL == null ? DEFAULT_DROP_DDL : dropTableDDL;
   }

   public void setDropTableDDL(String dropTableDDL)
   {
      this.dropTableDDL = dropTableDDL;
   }
   
   
   
   // ------------------------------------------------------------------ Public

   public void start() throws Exception
   {
      Connection conn = getConnection();
      conn.setAutoCommit(false);
      Statement stmt = null;
      boolean success = false;
      try
      {
         stmt = conn.createStatement();
         stmt.execute(getDropTableDDL());
         stmt.close();
         stmt = conn.createStatement();
         stmt.execute(getCreateTableDDL());
         conn.commit();
         success = true;
      }
      finally
      {
         if (!success)
         {
            conn.rollback();
         }
         if (stmt != null)
         {
            stmt.close();
         }
         conn.close();
      }

   }

   private Connection getConnection() throws Exception
   {
      Connection conn = null;
      if (jdbcURL != null)
      {
         Driver driver = org.hsqldb.jdbcDriver.class.newInstance();
         Properties props = new Properties();
         props.put("user", "sa");
         conn = driver.connect(jdbcURL, props);
      }
      else
      {
         DataSource datasource = (DataSource) new InitialContext().lookup(getDataSourceJndiName());
         conn = datasource.getConnection();
      }
      return conn;
   }

}
