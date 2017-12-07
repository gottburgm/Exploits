/**
 * 
 */
package org.jboss.test.cluster.web.persistent;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Trivial DataSource impl that doesn't pool connections, simply creates them
 * from a {@link Driver}.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision$
 */
public class MockDataSource implements DataSource
{
   private PrintWriter logWriter;
   private int loginTimeout;
   
   private final Driver driver;
   private final String jdbcUrl;
   private final String userName;
   private final String password;
   
   public MockDataSource(Driver driver, String url, String username, String password)
   {
      this.driver = driver;
      this.jdbcUrl = url;
      this.userName = username;
      this.password = password;
   }
   public Connection getConnection() throws SQLException
   {
      return getConnection(this.userName, this.password);
   }

   public Connection getConnection(String username, String password) throws SQLException
   {
      Properties props = new Properties();
      if (username != null)
      {
         props.put("user", username);
      }
      if (password != null)
      {
         props.put("password", password);
      }
      Connection conn = driver.connect(this.jdbcUrl, props);
      return conn;
   }

   public PrintWriter getLogWriter() throws SQLException
   {
      return this.logWriter;
   }

   public int getLoginTimeout() throws SQLException
   {
      return this.loginTimeout;
   }

   public Logger getParentLogger() throws SQLFeatureNotSupportedException
   {
      throw new SQLFeatureNotSupportedException("NYI: org.jboss.test.cluster.web.persistent.MockDataSource.getParentLogger");
   }

   public void setLogWriter(PrintWriter out) throws SQLException
   {
      this.logWriter = out;
   }

   public void setLoginTimeout(int seconds) throws SQLException
   {
      this.loginTimeout = seconds;
   }
   
   public boolean isWrapperFor(Class<?> iface) throws SQLException
   {
      return false;
   }

   public <T> T unwrap(Class<T> iface) throws SQLException
   {
      throw new SQLException("No wrapper");
   }

}
