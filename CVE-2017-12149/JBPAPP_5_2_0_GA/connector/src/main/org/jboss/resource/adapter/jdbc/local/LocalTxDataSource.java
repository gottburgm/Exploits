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
package org.jboss.resource.adapter.jdbc.local;

import java.util.Hashtable;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.transaction.TransactionManager;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.resource.connectionmanager.CachedConnectionManager;
import org.jboss.resource.connectionmanager.InternalManagedConnectionPool;
import org.jboss.resource.connectionmanager.JBossManagedConnectionPool;
import org.jboss.resource.connectionmanager.TxConnectionManager;
import org.jboss.resource.connectionmanager.CachedConnectionManagerReference;
import org.jboss.util.naming.NonSerializableFactory;
import org.jboss.util.naming.Util;

/**
 * This is a pojo that instantiates a Local tx connection pool.
 * It provides same functionality as ds.xml files
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 77478 $
 */
public class LocalTxDataSource
{
   protected static Logger log = Logger.getLogger(LocalTxDataSource.class.getName());


   public LocalTxDataSource()
   {
   }

   private CachedConnectionManager cachedConnectionManager;
   private TransactionManager transactionManager;
   private String jndiName;

   private InternalManagedConnectionPool.PoolParams poolParams = new InternalManagedConnectionPool.PoolParams();
   private LocalManagedConnectionFactory mcf = new LocalManagedConnectionFactory();

   private JBossManagedConnectionPool.OnePool pool = new JBossManagedConnectionPool.OnePool(mcf, poolParams, false, log);
   private TxConnectionManager connectionManager;
   private Object datasource;
   protected Hashtable initialContextProperties;
   protected InitialContext initialContext;

   public class ConnectionManagerDelegate implements ConnectionManager
   {
      private static final long serialVersionUID = 1L;

      public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cxRequestInfo) throws ResourceException
      {
         return connectionManager.allocateConnection(mcf, cxRequestInfo);
      }
   }

   public void setInitialContextProperties(Hashtable initialContextProperties)
   {
      this.initialContextProperties = initialContextProperties;
   }

   public void start() throws Exception
   {
      if (initialContextProperties == null) initialContext = new InitialContext();
      else initialContext = new InitialContext(initialContextProperties);
      
      connectionManager = new TxConnectionManager(cachedConnectionManager, pool, transactionManager);
      connectionManager.setLocalTransactions(true);
      connectionManager.setInterleaving(false);
      pool.setConnectionListenerFactory(connectionManager);
      datasource = connectionManager.getPoolingStrategy().getManagedConnectionFactory().createConnectionFactory(new ConnectionManagerDelegate());
      bindConnectionFactory();
   }


   /**
    * Bind the connection factory into jndi
    */
   protected void bindConnectionFactory() throws Exception
   {
      InitialContext ctx = initialContext;
      try
      {
         Name name = ctx.getNameParser("").parse(jndiName);
         String key = name.toString();
         if( true == true && name.size() > 1 )
         {
            int size = name.size() - 1;
            Util.createSubcontext(initialContext, name.getPrefix(size));
         }
         NonSerializableFactory.rebind(initialContext, key, datasource);
         log.info("Bound datasource to JNDI name '" + jndiName + "'");
      }
      catch (NamingException ne)
      {
         throw new DeploymentException("Could not bind ConnectionFactory into jndi: " + jndiName, ne);
      }
      finally
      {
         ctx.close();
      }
   }

   protected void unbindConnectionFactory() throws Exception
   {
      InitialContext ctx = initialContext;
      try
      {                                              
         ctx.unbind(jndiName);
         NonSerializableFactory.unbind(jndiName);
         log.info("Unbound datasource for JNDI name '" + jndiName + "'");
      }
      catch (NamingException ne)
      {
         log.error("Could not unbind datasource from jndi: " + jndiName, ne);
      }
      finally
      {
         ctx.close();
      }
   }

   public Object getDatasource()
   {
      return datasource;
   }

   public void setCachedConnectionManager(CachedConnectionManagerReference cachedConnectionManager)
   {
      this.cachedConnectionManager = cachedConnectionManager.getCachedConnectionManager();
   }

   public TransactionManager getTransactionManager()
   {
      return transactionManager;
   }

   public void setTransactionManager(TransactionManager transactionManager)
   {
      this.transactionManager = transactionManager;
   }

   public String getJndiName()
   {
      return jndiName;
   }

   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   public int getMinSize()
   {
      return poolParams.minSize;
   }

   public void setMinSize(int minSize)
   {
      poolParams.minSize = minSize;
   }

   public int getMaxSize()
   {
      return poolParams.maxSize;
   }

   public void setMaxSize(int maxSize)
   {
      poolParams.maxSize = maxSize;
   }

   public int getBlockingTimeout()
   {
      return poolParams.blockingTimeout;
   }

   public void setBlockingTimeout(int blockingTimeout)
   {
      poolParams.blockingTimeout = blockingTimeout;
   }

   public long getIdleTimeout()
   {
      return poolParams.idleTimeout;
   }

   public void setIdleTimeout(long idleTimeout)
   {
      poolParams.idleTimeout = idleTimeout;
   }

   public String getDriverClass()
   {
      return mcf.getDriverClass();
   }

   public void setDriverClass(final String driverClass)
   {
      mcf.setDriverClass(driverClass);
   }

   public String getConnectionURL()
   {
      return mcf.getConnectionURL();
   }

   public void setConnectionURL(final String connectionURL)
   {
      mcf.setConnectionURL(connectionURL);
   }

   public void setUserName(final String userName)
   {
      mcf.setUserName(userName);
   }

   public void setPassword(final String password)
   {
      mcf.setPassword(password);
   }

   public void setPreparedStatementCacheSize(int size)
   {
      mcf.setPreparedStatementCacheSize(size);
   }

   public int getPreparedStatementCacheSize()
   {
      return mcf.getPreparedStatementCacheSize();
   }

   public boolean getSharePreparedStatements()
   {
      return mcf.getSharePreparedStatements();
   }

   public void setSharePreparedStatements(boolean sharePS)
   {
      mcf.setSharePreparedStatements(sharePS);
   }

   public boolean getTxQueryTimeout()
   {
      return mcf.isTransactionQueryTimeout();
   }

   public void setTxQueryTimeout(boolean qt)
   {
      mcf.setTransactionQueryTimeout(qt);
   }

   public String getTransactionIsolation()
   {
      return mcf.getTransactionIsolation();
   }

   public void setTransactionIsolation(String transactionIsolation)
   {
      mcf.setTransactionIsolation(transactionIsolation);
   }

   public String getNewConnectionSQL()
   {
      return mcf.getNewConnectionSQL();
   }

   public void setNewConnectionSQL(String newConnectionSQL)
   {
      mcf.setNewConnectionSQL(newConnectionSQL);
   }

   public String getCheckValidConnectionSQL()
   {
      return mcf.getCheckValidConnectionSQL();
   }

   public void setCheckValidConnectionSQL(String checkValidConnectionSQL)
   {
      mcf.setCheckValidConnectionSQL(checkValidConnectionSQL);
   }

   public String getTrackStatements()
   {
      return mcf.getTrackStatements();
   }

   public void setTrackStatements(String value)
   {
      mcf.setTrackStatements(value);
   }

   public String getExceptionSorterClassName()
   {
      return mcf.getExceptionSorterClassName();
   }

   public void setExceptionSorterClassName(String exceptionSorterClassName)
   {
      mcf.setExceptionSorterClassName(exceptionSorterClassName);
   }

   public String getValidConnectionCheckerClassName()
   {
      return mcf.getValidConnectionCheckerClassName();
   }

   public void setValidConnectionCheckerClassName(String value)
   {
      mcf.setValidConnectionCheckerClassName(value);
   }

}
