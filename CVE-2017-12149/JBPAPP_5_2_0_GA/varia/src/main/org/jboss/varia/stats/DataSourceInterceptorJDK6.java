/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.varia.stats;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.CallableStatement;
import java.sql.SQLClientInfoException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.Struct;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;

import java.sql.Savepoint;
import java.sql.ParameterMetaData;

import java.io.PrintWriter;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.Calendar;
import java.util.Properties;
import java.math.BigDecimal;
import java.net.URL;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 85945 $</tt>
 * @@jmx:mbean name="jboss.stats:name=DataSourceInterceptor"
 * extends="org.jboss.system.ServiceMBean"
 */
public class DataSourceInterceptorJDK6 extends DataSourceInterceptor
{

   public <T>T unwrap(Class<T> iface) throws SQLException
   {
      return target.unwrap(iface);
   }
   
   public boolean isWrapperFor(Class<?> iface) throws SQLException
   {
      return target.isWrapperFor(iface);
   }

   public Connection getConnection() throws SQLException
   {
      return new ConnectionInterceptor(target.getConnection());
   }

   public Connection getConnection(String username, String password) throws SQLException
   {
      return new ConnectionInterceptor(target.getConnection());
   }

   // Inner

   public class ConnectionInterceptor
      implements Connection
   {
      private final Connection target;

      public ConnectionInterceptor(Connection target)
      {
         this.target = target;
      }

      
      public int getHoldability() throws SQLException
      {
         return target.getHoldability();
      }
      

      public int getTransactionIsolation() throws SQLException
      {
         return target.getTransactionIsolation();
      }

      public void clearWarnings() throws SQLException
      {
         target.clearWarnings();
      }

      public void close() throws SQLException
      {
         target.close();
      }

      public void commit() throws SQLException
      {
         target.commit();
      }

      public void rollback() throws SQLException
      {
         target.rollback();
      }

      public boolean getAutoCommit() throws SQLException
      {
         return target.getAutoCommit();
      }

      public boolean isClosed() throws SQLException
      {
         return target.isClosed();
      }

      public boolean isReadOnly() throws SQLException
      {
         return target.isReadOnly();
      }

      
      public void setHoldability(int holdability) throws SQLException
      {
         target.setHoldability(holdability);
      }
      

      public void setTransactionIsolation(int level) throws SQLException
      {
         target.setTransactionIsolation(level);
      }

      public void setAutoCommit(boolean autoCommit) throws SQLException
      {
         target.setAutoCommit(autoCommit);
      }

      public void setReadOnly(boolean readOnly) throws SQLException
      {
         target.setReadOnly(readOnly);
      }

      public String getCatalog() throws SQLException
      {
         return target.getCatalog();
      }

      public void setCatalog(String catalog) throws SQLException
      {
         target.setCatalog(catalog);
      }

      public DatabaseMetaData getMetaData() throws SQLException
      {
         return target.getMetaData();
      }

      public SQLWarning getWarnings() throws SQLException
      {
         return target.getWarnings();
      }

      
      public Savepoint setSavepoint() throws SQLException
      {
         return target.setSavepoint();
      }
      

      
      public void releaseSavepoint(Savepoint savepoint) throws SQLException
      {
         target.releaseSavepoint(savepoint);
      }
      

      
      public void rollback(Savepoint savepoint) throws SQLException
      {
         target.rollback(savepoint);
      }
      

      public Statement createStatement() throws SQLException
      {
         return new StatementInterceptor(this, target.createStatement());
      }

      public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
      {
         return new StatementInterceptor(this, target.createStatement(resultSetType, resultSetConcurrency));
      }

      
      public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
         throws SQLException
      {
         return new StatementInterceptor(this,
            target.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
      }
      

      public Map getTypeMap() throws SQLException
      {
         return target.getTypeMap();
      }

      public void setTypeMap(Map map) throws SQLException
      {
         target.setTypeMap(map);
      }

      public String nativeSQL(String sql) throws SQLException
      {
         return target.nativeSQL(sql);
      }

      public CallableStatement prepareCall(String sql) throws SQLException
      {
         return target.prepareCall(sql);
      }

      public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
         throws SQLException
      {
         return target.prepareCall(sql, resultSetType, resultSetConcurrency);
      }

      
      public CallableStatement prepareCall(String sql,
                                           int resultSetType,
                                           int resultSetConcurrency,
                                           int resultSetHoldability) throws SQLException
      {
         return target.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
      }
      

      public PreparedStatement prepareStatement(String sql) throws SQLException
      {
         logSql(sql);
         return new PreparedStatementInterceptor(this, target.prepareStatement(sql));
      }

      
      public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
      {
         logSql(sql);
         return new PreparedStatementInterceptor(this, target.prepareStatement(sql, autoGeneratedKeys));
      }
      

      public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
         throws SQLException
      {
         logSql(sql);
         return new PreparedStatementInterceptor(this,
            target.prepareStatement(sql, resultSetType, resultSetConcurrency));
      }

      
      public PreparedStatement prepareStatement(String sql,
                                                int resultSetType,
                                                int resultSetConcurrency,
                                                int resultSetHoldability) throws SQLException
      {
         logSql(sql);
         return new PreparedStatementInterceptor(this,
            target.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
      }
      

      
      public PreparedStatement prepareStatement(String sql, int columnIndexes[]) throws SQLException
      {
         logSql(sql);
         return new PreparedStatementInterceptor(this, target.prepareStatement(sql, columnIndexes));
      }
      

      
      public Savepoint setSavepoint(String name) throws SQLException
      {
         return target.setSavepoint(name);
      }
      

      
      public PreparedStatement prepareStatement(String sql, String columnNames[]) throws SQLException
      {
         logSql(sql);
         return new PreparedStatementInterceptor(this, target.prepareStatement(sql, columnNames));
      }

      public <T>T unwrap(Class<T> iface) throws SQLException
      {
         return target.unwrap(iface);
      }

      public boolean isWrapperFor(Class<?> iface) throws SQLException
      {
         return target.isWrapperFor(iface);
      }

      public Clob createClob() throws SQLException
      {
         return target.createClob();
      }

      public Blob createBlob() throws SQLException
      {
         return target.createBlob();
      }

      public NClob createNClob() throws SQLException
      {
         return target.createNClob();
      }

      public SQLXML createSQLXML() throws SQLException
      {
         return target.createSQLXML();
      }

      public boolean isValid(int timeout) throws SQLException
      {
         return target.isValid(timeout);
      }

      public void setClientInfo(String name, String value) throws SQLClientInfoException
      {
         target.setClientInfo(name, value);
      }

      public void setClientInfo(Properties properties) throws SQLClientInfoException
      {
         target.setClientInfo(properties);
      }

      public String getClientInfo(String name) throws SQLException
      {
         return target.getClientInfo(name);
      }

      public Properties getClientInfo() throws SQLException
      {
         return target.getClientInfo();
      }

      public Array createArrayOf(String typeName, Object[] elements) throws SQLException
      {
         return target.createArrayOf(typeName, elements);
      }

      public Struct createStruct(String typeName, Object[] attributes) throws SQLException
      {
         return target.createStruct(typeName, attributes);
      }
   }

   public class StatementInterceptor
      implements Statement
   {
      private final Connection con;
      private final Statement target;

      public StatementInterceptor(Connection con, Statement target)
      {
         this.con = con;
         this.target = target;
      }

      public int getFetchDirection() throws SQLException
      {
         return target.getFetchDirection();
      }

      public int getFetchSize() throws SQLException
      {
         return target.getFetchSize();
      }

      public int getMaxFieldSize() throws SQLException
      {
         return target.getMaxFieldSize();
      }

      public int getMaxRows() throws SQLException
      {
         return target.getMaxRows();
      }

      public int getQueryTimeout() throws SQLException
      {
         return target.getQueryTimeout();
      }

      public int getResultSetConcurrency() throws SQLException
      {
         return target.getResultSetConcurrency();
      }

      
      public int getResultSetHoldability() throws SQLException
      {
         return target.getResultSetHoldability();
      }
      

      public int getResultSetType() throws SQLException
      {
         return target.getResultSetType();
      }

      public int getUpdateCount() throws SQLException
      {
         return target.getUpdateCount();
      }

      public void cancel() throws SQLException
      {
         target.cancel();
      }

      public void clearBatch() throws SQLException
      {
         target.clearBatch();
      }

      public void clearWarnings() throws SQLException
      {
         target.clearWarnings();
      }

      public void close() throws SQLException
      {
         target.close();
      }

      public boolean getMoreResults() throws SQLException
      {
         return target.getMoreResults();
      }

      public int[] executeBatch() throws SQLException
      {
         return target.executeBatch();
      }

      public void setFetchDirection(int direction) throws SQLException
      {
         target.setFetchDirection(direction);
      }

      public void setFetchSize(int rows) throws SQLException
      {
         target.setFetchSize(rows);
      }

      public void setMaxFieldSize(int max) throws SQLException
      {
         target.setMaxFieldSize(max);
      }

      public void setMaxRows(int max) throws SQLException
      {
         target.setMaxRows(max);
      }

      public void setQueryTimeout(int seconds) throws SQLException
      {
         target.setQueryTimeout(seconds);
      }

      
      public boolean getMoreResults(int current) throws SQLException
      {
         return target.getMoreResults(current);
      }
      

      public void setEscapeProcessing(boolean enable) throws SQLException
      {
         target.setEscapeProcessing(enable);
      }

      public int executeUpdate(String sql) throws SQLException
      {
         logSql(sql);
         return target.executeUpdate(sql);
      }

      public void addBatch(String sql) throws SQLException
      {
         logSql(sql);
         target.addBatch(sql);
      }

      public void setCursorName(String name) throws SQLException
      {
         target.setCursorName(name);
      }

      public boolean execute(String sql) throws SQLException
      {
         logSql(sql);
         return target.execute(sql);
      }

      
      public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException
      {
         logSql(sql);
         return target.executeUpdate(sql, autoGeneratedKeys);
      }
      

      
      public boolean execute(String sql, int autoGeneratedKeys) throws SQLException
      {
         logSql(sql);
         return target.execute(sql, autoGeneratedKeys);
      }
      

      
      public int executeUpdate(String sql, int columnIndexes[]) throws SQLException
      {
         logSql(sql);
         return target.executeUpdate(sql, columnIndexes);
      }
      

      
      public boolean execute(String sql, int columnIndexes[]) throws SQLException
      {
         logSql(sql);
         return target.execute(sql, columnIndexes);
      }
      

      public Connection getConnection() throws SQLException
      {
         return con;
      }

      
      public ResultSet getGeneratedKeys() throws SQLException
      {
         return target.getGeneratedKeys();
      }
      

      public ResultSet getResultSet() throws SQLException
      {
         return target.getResultSet();
      }

      public SQLWarning getWarnings() throws SQLException
      {
         return target.getWarnings();
      }

      
      public int executeUpdate(String sql, String columnNames[]) throws SQLException
      {
         logSql(sql);
         return target.executeUpdate(sql, columnNames);
      }
      

      
      public boolean execute(String sql, String columnNames[]) throws SQLException
      {
         logSql(sql);
         return target.execute(sql, columnNames);
      }
      

      public ResultSet executeQuery(String sql) throws SQLException
      {
         logSql(sql);
         return target.executeQuery(sql);
      }

      public <T>T unwrap(Class<T> iface) throws SQLException
      {
         return target.unwrap(iface);
      }

      public boolean isWrapperFor(Class<?> iface) throws SQLException
      {
         return target.isWrapperFor(iface);
      }

      public boolean isClosed() throws SQLException
      {
         return target.isClosed();
      }

      public void setPoolable(boolean poolable) throws SQLException
      {
         target.setPoolable(poolable);
      }

      public boolean isPoolable() throws SQLException
      {
         return target.isPoolable();
      }
   }

   public class PreparedStatementInterceptor
      extends StatementInterceptor
      implements PreparedStatement
   {
      private final PreparedStatement target;

      public PreparedStatementInterceptor(Connection con, PreparedStatement target)
      {
         super(con, target);
         this.target = target;
      }

      public int executeUpdate() throws SQLException
      {
         return target.executeUpdate();
      }

      public void addBatch() throws SQLException
      {
         target.addBatch();
      }

      public void clearParameters() throws SQLException
      {
         target.clearParameters();
      }

      public boolean execute() throws SQLException
      {
         return target.execute();
      }

      public void setByte(int parameterIndex, byte x) throws SQLException
      {
         target.setByte(parameterIndex, x);
      }

      public void setDouble(int parameterIndex, double x) throws SQLException
      {
         target.setDouble(parameterIndex, x);
      }

      public void setFloat(int parameterIndex, float x) throws SQLException
      {
         target.setFloat(parameterIndex, x);
      }

      public void setInt(int parameterIndex, int x) throws SQLException
      {
         target.setInt(parameterIndex, x);
      }

      public void setNull(int parameterIndex, int sqlType) throws SQLException
      {
         target.setNull(parameterIndex, sqlType);
      }

      public void setLong(int parameterIndex, long x) throws SQLException
      {
         target.setLong(parameterIndex, x);
      }

      public void setShort(int parameterIndex, short x) throws SQLException
      {
         target.setShort(parameterIndex, x);
      }

      public void setBoolean(int parameterIndex, boolean x) throws SQLException
      {
         target.setBoolean(parameterIndex, x);
      }

      public void setBytes(int parameterIndex, byte x[]) throws SQLException
      {
         target.setBytes(parameterIndex, x);
      }

      public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException
      {
         target.setAsciiStream(parameterIndex, x, length);
      }

      public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException
      {
         target.setBinaryStream(parameterIndex, x, length);
      }

      public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException
      {
         target.setUnicodeStream(parameterIndex, x, length);
      }

      public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException
      {
         target.setCharacterStream(parameterIndex, reader, length);
      }

      public void setObject(int parameterIndex, Object x) throws SQLException
      {
         target.setObject(parameterIndex, x);
      }

      public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException
      {
         target.setObject(parameterIndex, x, targetSqlType);
      }

      public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException
      {
         target.setObject(parameterIndex, x, targetSqlType, scale);
      }

      public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException
      {
         target.setNull(paramIndex, sqlType, typeName);
      }

      public void setString(int parameterIndex, String x) throws SQLException
      {
         target.setString(parameterIndex, x);
      }

      public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException
      {
         target.setBigDecimal(parameterIndex, x);
      }

      
      public void setURL(int parameterIndex, URL x) throws SQLException
      {
         target.setURL(parameterIndex, x);
      }
      

      public void setArray(int i, Array x) throws SQLException
      {
         target.setArray(i, x);
      }

      public void setBlob(int i, Blob x) throws SQLException
      {
         target.setBlob(i, x);
      }

      public void setClob(int i, Clob x) throws SQLException
      {
         target.setClob(i, x);
      }

      public void setDate(int parameterIndex, Date x) throws SQLException
      {
         target.setDate(parameterIndex, x);
      }

      
      public ParameterMetaData getParameterMetaData() throws SQLException
      {
         return target.getParameterMetaData();
      }
      

      public void setRef(int i, Ref x) throws SQLException
      {
         target.setRef(i, x);
      }

      public ResultSet executeQuery() throws SQLException
      {
         return target.executeQuery();
      }

      public ResultSetMetaData getMetaData() throws SQLException
      {
         return target.getMetaData();
      }

      public void setTime(int parameterIndex, Time x) throws SQLException
      {
         target.setTime(parameterIndex, x);
      }

      public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException
      {
         target.setTimestamp(parameterIndex, x);
      }

      public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException
      {
         target.setDate(parameterIndex, x, cal);
      }

      public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException
      {
         target.setTime(parameterIndex, x, cal);
      }

      public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException
      {
         target.setTimestamp(parameterIndex, x, cal);
      }

      public void setRowId(int parameterIndex, RowId x) throws SQLException
      {
         target.setRowId(parameterIndex, x);
      }

      public void setNString(int parameterIndex, String value) throws SQLException
      {
         target.setNString(parameterIndex, value);
      }

      public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException
      {
         target.setNCharacterStream(parameterIndex, value, length);
      }

      public void setNClob(int parameterIndex, NClob value) throws SQLException
      {
         target.setNClob(parameterIndex, value);
      }

      public void setClob(int parameterIndex, Reader reader, long length) throws SQLException
      {
         target.setClob(parameterIndex, reader, length);
      }

      public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException
      {
         target.setBlob(parameterIndex, inputStream, length);
      }

      public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException
      {
         target.setNClob(parameterIndex, reader, length);
      }

      public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException
      {
         target.setSQLXML(parameterIndex, xmlObject);
      }

      public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException
      {
         target.setAsciiStream(parameterIndex, x, length);
      }

      public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException
      {
         target.setBinaryStream(parameterIndex, x, length);
      }

      public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException
      {
         target.setCharacterStream(parameterIndex, reader, length);
      }

      public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException
      {
         target.setAsciiStream(parameterIndex, x);
      }

      public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException
      {
         target.setBinaryStream(parameterIndex, x);
      }

      public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException
      {
         target.setCharacterStream(parameterIndex, reader);
      }

      public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException
      {
         target.setNCharacterStream(parameterIndex, value);
      }

      public void setClob(int parameterIndex, Reader reader) throws SQLException
      {
         target.setClob(parameterIndex, reader);
      }

      public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException
      {
         target.setBlob(parameterIndex, inputStream);
      }

      public void setNClob(int parameterIndex, Reader reader) throws SQLException
      {
         target.setNClob(parameterIndex, reader);
      }
   }
}
