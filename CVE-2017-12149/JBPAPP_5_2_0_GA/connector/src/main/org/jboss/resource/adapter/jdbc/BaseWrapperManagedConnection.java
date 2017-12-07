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

import org.jboss.logging.Logger;
import org.jboss.resource.JBossResourceException;
import org.jboss.resource.statistic.JBossConnectionStatistics;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * BaseWrapperManagedConnection
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="mailto:weston.price@jboss.com>Weston Price</a>
 * @version $Revision: 112944 $
 */

public abstract class BaseWrapperManagedConnection implements ManagedConnection , JBossConnectionStatistics
{
   private static final WrappedConnectionFactory wrappedConnectionFactory;
   
   protected final BaseWrapperManagedConnectionFactory mcf;

   protected final Connection con;

   protected final Properties props;

   private final int transactionIsolation;

   private final boolean readOnly;

   private ReentrantLock lock = new ReentrantLock(true);

   private final Collection cels = new ArrayList();

   private final Set handles = new HashSet();

   private PreparedStatementCache psCache = null;

   protected final Object stateLock = new Object();

   protected boolean inManagedTransaction = false;

   protected AtomicBoolean inLocalTransaction = new AtomicBoolean(false);

   protected boolean jdbcAutoCommit = true;

   protected boolean underlyingAutoCommit = true;

   protected boolean jdbcReadOnly;

   protected boolean underlyingReadOnly;

   protected int jdbcTransactionIsolation;

   protected boolean destroyed = false;
   
   static
   {
      Class connectionFactory = null;
      ClassNotFoundException ex = null;
      for (String impl : WrappedConnectionFactory.IMPLEMENTATIONS)
      {
         try
         {
            connectionFactory = WrappedConnectionFactory.class.forName(impl);
            break;
         }
         catch (ClassNotFoundException e)
         {
            // ignore
            ex = e;
         }
      }
      if (connectionFactory == null)
      {
         throw new RuntimeException("Unabled to load wrapped connection factory", ex);
      }
      try
      {
         wrappedConnectionFactory = (WrappedConnectionFactory) connectionFactory.newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error initializign connection factory", e);
      }
   }

   public BaseWrapperManagedConnection(final BaseWrapperManagedConnectionFactory mcf, final Connection con,
         final Properties props, final int transactionIsolation, final int psCacheSize) throws SQLException
   {
      this.mcf = mcf;
      this.con = con;
      this.props = props;

      if (psCacheSize > 0)
         psCache = new PreparedStatementCache(psCacheSize);

      if (transactionIsolation == -1)
         this.transactionIsolation = con.getTransactionIsolation();

      else
      {
         this.transactionIsolation = transactionIsolation;
         con.setTransactionIsolation(transactionIsolation);
      }

      readOnly = con.isReadOnly();

      if (mcf.getNewConnectionSQL() != null)
      {
         Statement s = con.createStatement();
         try
         {
            s.execute(mcf.getNewConnectionSQL());
         }
         finally
         {
            s.close();
         }
      }

      underlyingReadOnly = readOnly;
      jdbcReadOnly = readOnly;
      jdbcTransactionIsolation = this.transactionIsolation;
   }

   public void addConnectionEventListener(ConnectionEventListener cel)
   {
      synchronized (cels)
      {
         cels.add(cel);
      }
   }

   public void removeConnectionEventListener(ConnectionEventListener cel)
   {
      synchronized (cels)
      {
         cels.remove(cel);
      }
   }

   public void associateConnection(Object handle) throws ResourceException
   {
      if (!(handle instanceof WrappedConnection))
         throw new JBossResourceException("Wrong kind of connection handle to associate" + handle);
      ((WrappedConnection) handle).setManagedConnection(this);
      synchronized (handles)
      {
         handles.add(handle);
      }
   }

   public PrintWriter getLogWriter() throws ResourceException
   {
      // TODO: implement this javax.resource.spi.ManagedConnection method
      return null;
   }

   public ManagedConnectionMetaData getMetaData() throws ResourceException
   {
      // TODO: implement this javax.resource.spi.ManagedConnection method
      return null;
   }

   public void setLogWriter(PrintWriter param1) throws ResourceException
   {
      // TODO: implement this javax.resource.spi.ManagedConnection method
   }

   public void cleanup() throws ResourceException
   {
      boolean isActive = false;

      if (lock.hasQueuedThreads())
      {
         Collection<Thread> threads = lock.getQueuedThreads();
         for (Thread thread : threads)
         {
            Throwable t = new Throwable("Thread waiting for lock during cleanup");
            t.setStackTrace(thread.getStackTrace());

            mcf.log.warn(t.getMessage(), t);
         }

         isActive = true;
      }

      if (lock.isLocked())
      {
         Throwable t = new Throwable("Lock owned during cleanup");
         t.setStackTrace(lock.getOwner().getStackTrace());

         mcf.log.warn(t.getMessage(), t);
         
         isActive = true;
      }

      synchronized (handles)
      {
         for (Iterator i = handles.iterator(); i.hasNext();)
         {
            WrappedConnection lc = (WrappedConnection) i.next();
            lc.setManagedConnection(null);
         }

         handles.clear();
      }

      //reset all the properties we know about to defaults.
      synchronized (stateLock)
      {
         jdbcAutoCommit = true;
         jdbcReadOnly = readOnly;
         if (jdbcTransactionIsolation != transactionIsolation)
         {
            try
            {
               con.setTransactionIsolation(transactionIsolation);
               jdbcTransactionIsolation = transactionIsolation;
            }
            catch (SQLException e)
            {
               mcf.log.warn("Error resetting transaction isolation ", e);
            }
         }
      }

      if (isActive)
      {
         // There are active lock - make sure that the JCA container kills
         // this handle by throwing an exception

         throw new ResourceException("Still active locks for " + this);
      }
   }

   protected void lock()
   {
      lock.lock();
   }

   protected void tryLock() throws SQLException
   {
      int tryLock = mcf.getUseTryLock().intValue();
      if (tryLock <= 0)
      {
         lock();
         return;
      }
      try
      {
         if (lock.tryLock(tryLock, TimeUnit.SECONDS) == false)
            throw new SQLException("Unable to obtain lock in " + tryLock + " seconds: " + this);
      }
      catch (InterruptedException e)
      {
         Thread.currentThread().interrupt();
         throw new SQLException("Interrupted attempting lock: " + this);
      }
   }
   
   protected void unlock()
   {
      if (lock.isLocked())
      {
         lock.unlock();
      }
      else
      {
         mcf.log.warn("Owner is null");            
         
         Throwable t = new Throwable("Thread trying to unlock");
         t.setStackTrace(Thread.currentThread().getStackTrace());

         mcf.log.warn(t.getMessage(), t);
      }
   }

   public Object getConnection(Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      checkIdentity(subject, cri);
      WrappedConnection lc = wrappedConnectionFactory.createWrappedConnection(this);
      synchronized (handles)
      {
         handles.add(lc);
      }
      return lc;
   }

   public void destroy() throws ResourceException
   {
      synchronized (stateLock)
      {
         destroyed = true;
      }

      cleanup();
      try
      {
         // See JBAS-5678
         if (underlyingAutoCommit == false)
            con.rollback();
      }
      catch (SQLException ignored)
      {
         getLog().trace("Ignored error during rollback: ", ignored);
      }
      try
      {
         con.close();
      }
      catch (SQLException ignored)
      {
         getLog().trace("Ignored error during close: ", ignored);
      }
   }

   public boolean checkValid()
   {
      SQLException e = mcf.isValidConnection(con);

      if (e == null)
         // It's ok
         return true;
      else
      {
         getLog().warn("Destroying connection that is not valid, due to the following exception: " + con, e);
         broadcastConnectionError(e);
         return false;
      }
   }

   public Properties getProperties()
   {
      return this.props;

   }

   void closeHandle(WrappedConnection handle)
   {
      synchronized (stateLock)
      {
         if (destroyed)
            return;
      }

      synchronized (handles)
      {
         handles.remove(handle);
      }
      ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
      ce.setConnectionHandle(handle);
      Collection copy = null;
      synchronized (cels)
      {
         copy = new ArrayList(cels);
      }
      for (Iterator i = copy.iterator(); i.hasNext();)
      {
         ConnectionEventListener cel = (ConnectionEventListener) i.next();
         cel.connectionClosed(ce);
      }
   }

   Throwable connectionError(Throwable t)
   {
      if(t instanceof SQLException)
      {
         boolean stale = mcf.isStaleConnection((SQLException)t);

         if(stale)
         {
            t = new StaleConnectionException((SQLException)t);           
         
         }else
         {
            boolean fatalException = mcf.isExceptionFatal((SQLException)t);

            if(fatalException)
            {
               broadcastConnectionError(t);
            }
         }
      }
      else
      {
         broadcastConnectionError(t);         
      }

      return t;
   }


   protected void broadcastConnectionError(Throwable e)
   {
      synchronized (stateLock)
      {
         if (destroyed)
         {
            Logger log = getLog();
            if (log.isTraceEnabled())
               log.trace("Not broadcasting error, already destroyed " + this, e);
            return;
         }
      }

      // We need to unlock() before sending the connection error to the
      // event listeners. Otherwise the lock won't be in sync once
      // cleanup() is called
      unlock();

      Exception ex = null;
      if (e instanceof Exception)
         ex = (Exception) e;
      else
         ex = new ResourceAdapterInternalException("Unexpected error", e);
      ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, ex);
      Collection copy = null;
      synchronized (cels)
      {
         copy = new ArrayList(cels);
      }
      for (Iterator i = copy.iterator(); i.hasNext();)
      {
         ConnectionEventListener cel = (ConnectionEventListener) i.next();
         try
         {
            cel.connectionErrorOccurred(ce);
         }
         catch (Throwable t)
         {
            getLog().warn("Error notifying of connection error for listener: " + cel, t);
         }
      }
   }

   Connection getConnection() throws SQLException
   {
      if (con == null)
         throw new SQLException("Connection has been destroyed!!!");
      return con;
   }

   PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
   {
      if (psCache != null)
      {
         PreparedStatementCache.Key key = new PreparedStatementCache.Key(sql,
               PreparedStatementCache.Key.PREPARED_STATEMENT, resultSetType, resultSetConcurrency);
         CachedPreparedStatement cachedps = (CachedPreparedStatement) psCache.get(key);
         if (cachedps != null)
         {
            if (canUse(cachedps))
               cachedps.inUse();
            else
               return doPrepareStatement(sql, resultSetType, resultSetConcurrency);
         }
         else
         {
            PreparedStatement ps = doPrepareStatement(sql, resultSetType, resultSetConcurrency);
            cachedps = wrappedConnectionFactory.createCachedPreparedStatement(ps);
            psCache.insert(key, cachedps);
         }
         return cachedps;
      }
      else
         return doPrepareStatement(sql, resultSetType, resultSetConcurrency);
   }

   PreparedStatement doPrepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
   {
      return con.prepareStatement(sql, resultSetType, resultSetConcurrency);
   }

   CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
   {
      if (psCache != null)
      {
         PreparedStatementCache.Key key = new PreparedStatementCache.Key(sql,
               PreparedStatementCache.Key.CALLABLE_STATEMENT, resultSetType, resultSetConcurrency);
         CachedCallableStatement cachedps = (CachedCallableStatement) psCache.get(key);
         if (cachedps != null)
         {
            if (canUse(cachedps))
               cachedps.inUse();
            else
               return doPrepareCall(sql, resultSetType, resultSetConcurrency);
         }
         else
         {
            CallableStatement cs = doPrepareCall(sql, resultSetType, resultSetConcurrency);
            cachedps = wrappedConnectionFactory.createCachedCallableStatement(cs);
            psCache.insert(key, cachedps);
         }
         return cachedps;
      }
      else
         return doPrepareCall(sql, resultSetType, resultSetConcurrency);
   }

   CallableStatement doPrepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
   {
      return con.prepareCall(sql, resultSetType, resultSetConcurrency);
   }

   boolean canUse(CachedPreparedStatement cachedps)
   {
      // Nobody is using it so we are ok
      if (cachedps.isInUse() == false)
         return true;

      // Cannot reuse prepared statements in auto commit mode
      // if will close the previous usage of the PS
      if (underlyingAutoCommit == true)
         return false;

      // We have been told not to share
      return mcf.sharePS;
   }

   protected Logger getLog()
   {
      return mcf.log;
   }

   private void checkIdentity(Subject subject, ConnectionRequestInfo cri) throws ResourceException
   {
      Properties newProps = mcf.getConnectionProperties(subject, cri);
      if (!props.equals(newProps))
      {
         throw new JBossResourceException("Wrong credentials passed to getConnection!");
      } // end of if ()
   }

   /**
    * The <code>checkTransaction</code> method makes sure the adapter follows the JCA
    * autocommit contract, namely all statements executed outside a container managed transaction
    * or a component managed transaction should be autocommitted. To avoid continually calling
    * setAutocommit(enable) before and after container managed transactions, we keep track of the state
    * and check it before each transactional method call.
    */
   void checkTransaction() throws SQLException
   {
      synchronized (stateLock)
      {
         if (inManagedTransaction)
            return;

         // Check autocommit
         if (jdbcAutoCommit != underlyingAutoCommit)
         {
            con.setAutoCommit(jdbcAutoCommit);
            underlyingAutoCommit = jdbcAutoCommit;
         }
      }

      if (jdbcAutoCommit == false && inLocalTransaction.getAndSet(true) == false)
      {
         ArrayList copy;
         synchronized (cels)
         {
            copy = new ArrayList(cels);
         }
         ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_STARTED);
         for (int i = 0; i < copy.size(); ++i)
         {
            ConnectionEventListener cel = (ConnectionEventListener) copy.get(i);
            try
            {
               cel.localTransactionStarted(ce);
            }
            catch (Throwable t)
            {
               getLog().trace("Error notifying of connection committed for listener: " + cel, t);
            }
         }
      }

      checkState();
   }

   protected void checkState() throws SQLException
   {
      synchronized (stateLock)
      {
         // Check readonly
         if (jdbcReadOnly != underlyingReadOnly)
         {
            con.setReadOnly(jdbcReadOnly);
            underlyingReadOnly = jdbcReadOnly;
         }
      }
   }

   boolean isJdbcAutoCommit()
   {
      return inManagedTransaction ? false : jdbcAutoCommit;
   }

   void setJdbcAutoCommit(final boolean jdbcAutoCommit) throws SQLException
   {
      synchronized (stateLock)
      {
         if (inManagedTransaction)
            throw new SQLException("You cannot set autocommit during a managed transaction!");
         this.jdbcAutoCommit = jdbcAutoCommit;
      }

      if (jdbcAutoCommit && inLocalTransaction.getAndSet(false))
      {
         ArrayList copy;
         synchronized (cels)
         {
            copy = new ArrayList(cels);
         }
         ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_COMMITTED);
         for (int i = 0; i < copy.size(); ++i)
         {
            ConnectionEventListener cel = (ConnectionEventListener) copy.get(i);
            try
            {
               cel.localTransactionCommitted(ce);
            }
            catch (Throwable t)
            {
               getLog().trace("Error notifying of connection committed for listener: " + cel, t);
            }
         }
      }
   }

   boolean isJdbcReadOnly()
   {
      return jdbcReadOnly;
   }

   void setJdbcReadOnly(final boolean readOnly) throws SQLException
   {
      synchronized (stateLock)
      {
         if (inManagedTransaction)
            throw new SQLException("You cannot set read only during a managed transaction!");
         this.jdbcReadOnly = readOnly;
      }
   }

   int getJdbcTransactionIsolation()
   {
      return jdbcTransactionIsolation;
   }

   void setJdbcTransactionIsolation(final int isolationLevel) throws SQLException
   {
      synchronized (stateLock)
      {
         this.jdbcTransactionIsolation = isolationLevel;
         con.setTransactionIsolation(jdbcTransactionIsolation);
      }
   }

   void jdbcCommit() throws SQLException
   {
      synchronized (stateLock)
      {
         if (inManagedTransaction)
            throw new SQLException("You cannot commit during a managed transaction!");
         if (jdbcAutoCommit)
            throw new SQLException("You cannot commit with autocommit set!");
      }
      con.commit();

      if (inLocalTransaction.getAndSet(false))
      {
         ArrayList copy;
         synchronized (cels)
         {
            copy = new ArrayList(cels);
         }
         ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_COMMITTED);
         for (int i = 0; i < copy.size(); ++i)
         {
            ConnectionEventListener cel = (ConnectionEventListener) copy.get(i);
            try
            {
               cel.localTransactionCommitted(ce);
            }
            catch (Throwable t)
            {
               getLog().trace("Error notifying of connection committed for listener: " + cel, t);
            }
         }
      }
   }

   void jdbcRollback() throws SQLException
   {
      synchronized (stateLock)
      {
         if (inManagedTransaction)
            throw new SQLException("You cannot rollback during a managed transaction!");
         if (jdbcAutoCommit)
            throw new SQLException("You cannot rollback with autocommit set!");
      }
      con.rollback();

      if (inLocalTransaction.getAndSet(false))
      {
         ArrayList copy;
         synchronized (cels)
         {
            copy = new ArrayList(cels);
         }
         ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK);
         for (int i = 0; i < copy.size(); ++i)
         {
            ConnectionEventListener cel = (ConnectionEventListener) copy.get(i);
            try
            {
               cel.localTransactionRolledback(ce);
            }
            catch (Throwable t)
            {
               getLog().trace("Error notifying of connection rollback for listener: " + cel, t);
            }
         }
      }
   }

   void jdbcRollback(Savepoint savepoint) throws SQLException
   {
      synchronized (stateLock)
      {
         if (inManagedTransaction)
            throw new SQLException("You cannot rollback during a managed transaction!");
         if (jdbcAutoCommit)
            throw new SQLException("You cannot rollback with autocommit set!");
      }
      con.rollback(savepoint);
   }

   int getTrackStatements()
   {
      return mcf.trackStatements;
   }

   boolean isTransactionQueryTimeout()
   {
      return mcf.isTransactionQueryTimeout;
   }

   int getQueryTimeout()
   {
      return mcf.getQueryTimeout();
   }

   protected void checkException(SQLException e) throws ResourceException
   {
      connectionError(e);
      throw new JBossResourceException("SQLException", e);
   }
   
   public Object listConnectionStats()
   {
	   if(psCache != null)
		   return psCache.toString();
	   else
		   return "-1"; //-1 indicates NoCache
   }
}
