/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.adapter.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.util.NestedSQLException;

/**
 * A wrapper for a connection.
 * 
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 113038 $
 */
public abstract class WrappedConnection extends JBossWrapper implements Connection
{
   private static final Logger log = Logger.getLogger(WrappedConnection.class);

   private volatile BaseWrapperManagedConnection mc;
   private BaseWrapperManagedConnection lockedMC;
   private int lockCount;

   private WrapperDataSource dataSource;
   
   private HashMap<WrappedStatement, Throwable> statements;

   private boolean closed = false;

   private int trackStatements;
   
   public WrappedConnection(final BaseWrapperManagedConnection mc)
   {
      setManagedConnection(mc);
   }

   void setManagedConnection(final BaseWrapperManagedConnection mc)
   {
      this.mc = mc;
      this.lockCount = 0;

      if (mc != null)
      {
         trackStatements = mc.getTrackStatements();
      }
      else
      {
         // Reset the lockedMC reference when returned to the pool
         lockedMC = null;
      }
   }

   protected void lock() throws SQLException
   {
      BaseWrapperManagedConnection mc = this.mc;
      if (mc != null)
      {
         mc.tryLock();
         if (lockedMC == null)
            lockedMC = mc ;

         lockCount++;
      }
      else
         throw new SQLException("Connection is not associated with a managed connection." + this);
   }

   protected void unlock()
   {
      BaseWrapperManagedConnection mc = this.lockedMC;
      if (--lockCount == 0)
         lockedMC = null;

      if (mc != null)
         mc.unlock();
   }

   public WrapperDataSource getDataSource()
   {
      return dataSource;
   }
   
   protected void setDataSource(WrapperDataSource dataSource)
   {
      this.dataSource = dataSource;
   }
   
   public void setReadOnly(boolean readOnly) throws SQLException
   {
      lock();
      try
      {
         checkStatus();
         mc.setJdbcReadOnly(readOnly);
      }
      finally
      {
         unlock();
      }
   }

   public boolean isReadOnly() throws SQLException
   {
      checkStatus();
      return mc.isJdbcReadOnly();
   }

   public void close() throws SQLException
   {
      closed = true;
      if (mc != null)
      {
         if (trackStatements != BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_FALSE_INT)
         {
            synchronized (this)
            {
               if (statements != null)
               {
                  for (Iterator<Map.Entry<WrappedStatement, Throwable>> i = statements.entrySet().iterator(); i.hasNext(); )
                  {
                     Map.Entry<WrappedStatement, Throwable> entry = i.next();
                     WrappedStatement ws = entry.getKey();
                     if (trackStatements == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_TRUE_INT)
                     {
                        Throwable stackTrace = entry.getValue();
                        log.warn("Closing a statement you left open, please do your own housekeeping", stackTrace);
                     }
                     try
                     {
                        ws.internalClose();
                     }
                     catch (Throwable t)
                     {
                        log.warn("Exception trying to close statement:", t);
                     }
                  }
               }
            }
         }
         mc.closeHandle(this);
      }
      mc = null;
      dataSource = null;
   }

   public boolean isClosed() throws SQLException
   {
      return closed;
   }

   protected abstract WrappedStatement wrapStatement(Statement statement);
   
   public Statement createStatement() throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            return wrapStatement(mc.getConnection().createStatement());
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            return wrapStatement(mc.getConnection().createStatement(resultSetType, resultSetConcurrency));
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
         throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            return wrapStatement(mc.getConnection()
                  .createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   protected abstract WrappedPreparedStatement wrapPreparedStatement(PreparedStatement statement);

   public PreparedStatement prepareStatement(String sql) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            return wrapPreparedStatement(mc.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY));
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
         throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            return wrapPreparedStatement(mc.prepareStatement(sql, resultSetType, resultSetConcurrency));
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
         int resultSetHoldability) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            return wrapPreparedStatement(mc.getConnection()
                  .prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            return wrapPreparedStatement(mc.getConnection().prepareStatement(sql, autoGeneratedKeys));
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            return wrapPreparedStatement(mc.getConnection().prepareStatement(sql, columnIndexes));
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            return wrapPreparedStatement(mc.getConnection().prepareStatement(sql, columnNames));
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   protected abstract WrappedCallableStatement wrapCallableStatement(CallableStatement statement);

   public CallableStatement prepareCall(String sql) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            return wrapCallableStatement(mc.prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY));
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            return wrapCallableStatement(mc.prepareCall(sql, resultSetType, resultSetConcurrency));
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
         int resultSetHoldability) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            return wrapCallableStatement(mc.getConnection()
                  .prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public String nativeSQL(String sql) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            return mc.getConnection().nativeSQL(sql);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setAutoCommit(boolean autocommit) throws SQLException
   {
      lock();
      try
      {
         checkStatus();
         mc.setJdbcAutoCommit(autocommit);
      }
      finally
      {
         unlock();
      }
   }

   public boolean getAutoCommit() throws SQLException
   {
      lock();
      try
      {
         checkStatus();
         return mc.isJdbcAutoCommit();
      }
      finally
      {
         unlock();
      }
   }

   public void commit() throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         mc.jdbcCommit();
      }
      finally
      {
         unlock();
      }
   }

   public void rollback() throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         mc.jdbcRollback();
      }
      finally
      {
         unlock();
      }
   }

   public void rollback(Savepoint savepoint) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         mc.jdbcRollback(savepoint);
      }
      finally
      {
         unlock();
      }
   }

   public DatabaseMetaData getMetaData() throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            return mc.getConnection().getMetaData();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setCatalog(String catalog) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            mc.getConnection().setCatalog(catalog);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public String getCatalog() throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            return mc.getConnection().getCatalog();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setTransactionIsolation(int isolationLevel) throws SQLException
   {
      lock();
      try
      {
         checkStatus();
         mc.setJdbcTransactionIsolation(isolationLevel);
      }
      finally
      {
         unlock();
      }
   }

   public int getTransactionIsolation() throws SQLException
   {
      lock();
      try
      {
         checkStatus();
         return mc.getJdbcTransactionIsolation();
      }
      finally
      {
         unlock();
      }
   }

   public SQLWarning getWarnings() throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            return mc.getConnection().getWarnings();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void clearWarnings() throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            mc.getConnection().clearWarnings();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   @SuppressWarnings("unchecked")
   public Map getTypeMap() throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            return mc.getConnection().getTypeMap();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   @SuppressWarnings("unchecked")
   public void setTypeMap(Map typeMap) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            mc.getConnection().setTypeMap(typeMap);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setHoldability(int holdability) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            mc.getConnection().setHoldability(holdability);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public int getHoldability() throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            return mc.getConnection().getHoldability();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public Savepoint setSavepoint() throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            return mc.getConnection().setSavepoint();
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public Savepoint setSavepoint(String name) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            return mc.getConnection().setSavepoint(name);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void releaseSavepoint(Savepoint savepoint) throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try
         {
            mc.getConnection().releaseSavepoint(savepoint);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public Connection getUnderlyingConnection() throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         return mc.getConnection();
      }
      finally
      {
         unlock();
      }
   }

   protected Connection getWrappedObject() throws SQLException
   {
      return getUnderlyingConnection();
   }

   protected void checkTransaction() throws SQLException
   {
      checkStatus();
      mc.checkTransaction();
   }

   void checkTransactionActive() throws SQLException
   {
      if (dataSource == null)
         return;
      dataSource.checkTransactionActive();
   }

   /**
    * The checkStatus method checks that the handle has not been closed and
    * that it is associated with a managed connection.
    * 
    * @exception SQLException if an error occurs
    */
   protected void checkStatus() throws SQLException
   {
      if (closed)
         throw new SQLException("Connection handle has been closed and is unusable");
      if (mc == null)
         throw new SQLException("Connection handle is not currently associated with a ManagedConnection");
      checkTransactionActive();
   }

   /**
    * The base checkException method rethrows the supplied exception, informing
    * the ManagedConnection of the error. Subclasses may override this to
    * filter exceptions based on their severity.
    * 
    * @param t a throwable
    * @return the sql exception
    * @exception SQLException if an error occurs
    */
   protected SQLException checkException(Throwable t) throws SQLException
   {
      Throwable result = null;
      if (t instanceof AbstractMethodError)
      {
         t = new SQLFeatureNotSupportedException("Method is not implemented by JDBC driver", t);
      }

      if (mc != null)
         result = mc.connectionError(t);
      else
         result = t;

      if (result instanceof SQLException)
      {
         throw (SQLException) result;
      }
      else
      {
         throw new NestedSQLException("Error", result);
      }
      
   }

   int getTrackStatements()
   {
      return trackStatements;
   }
   
   void registerStatement(WrappedStatement ws)
   {
      if (trackStatements == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_FALSE_INT)
         return;
      
      synchronized (this)
      {
         if (statements == null)
            statements = new HashMap<WrappedStatement, Throwable>();
         
         if (trackStatements == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_TRUE_INT)
            statements.put(ws, new Throwable("STACKTRACE"));
         else
            statements.put(ws, null);
      }
   }

   void unregisterStatement(WrappedStatement ws)
   {
      if (trackStatements == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_FALSE_INT)
         return;
      synchronized (this)
      {
         if (statements != null)
            statements.remove(ws);
      }
   }

   void checkConfiguredQueryTimeout(WrappedStatement ws, int explicitTimeout) throws SQLException
   {
      if (mc == null || dataSource == null)
         return;

      int timeout = 0; 
      
      // Use the transaction timeout
      if (mc.isTransactionQueryTimeout())
      {
         timeout = dataSource.getTimeLeftBeforeTransactionTimeout();
         if (timeout > 0 && explicitTimeout > 0 && timeout > explicitTimeout)
            timeout = explicitTimeout;
      }
      
      // Look for a configured value
      if (timeout <= 0 && explicitTimeout <= 0)
         timeout = mc.getQueryTimeout();
      
      if (timeout > 0)
         ws.setQueryTimeout(timeout);
   }
   
   Logger getLogger()
   {
      return log;
   }
}
