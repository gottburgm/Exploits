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
package org.jboss.test.jca.adapter;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 112944 $</tt>
 */
public class MockedXADataSource
   implements XADataSource
{
   private static final Map instances = new HashMap();

   public static MockedXADataSource getInstance(String url)
   {
      return (MockedXADataSource)instances.get(url);
   }

   public static void stop(String url)
   {
      getInstance(url).stopped = true;
   }

   public static void start(String url)
   {
      getInstance(url).stopped = false;
   }

   public static String[] getUrls()
   {
      return (String[])instances.keySet().toArray(new String[instances.size()]);
   }

   private String url;
   private boolean stopped;
   private int loginTimeout;
   private PrintWriter logWriter;

   public String getURL()
   {
      return url;
   }

   public void setURL(String url)
   {
      this.url = url;
      instances.put(url, this);
   }

   public int getLoginTimeout() throws SQLException
   {
      return loginTimeout;
   }

   public Logger getParentLogger() throws SQLFeatureNotSupportedException
   {
      throw new SQLFeatureNotSupportedException("NYI: org.jboss.test.jca.adapter.MockedXADataSource.getParentLogger");
   }

   public void setLoginTimeout(int seconds) throws SQLException
   {
      this.loginTimeout = seconds;
   }

   public PrintWriter getLogWriter() throws SQLException
   {
      return logWriter;
   }

   public void setLogWriter(PrintWriter out) throws SQLException
   {
      this.logWriter = out;
   }

   public XAConnection getXAConnection() throws SQLException
   {
      return (XAConnection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { XAConnection.class }, new MockedXAConnection());
   }

   public XAConnection getXAConnection(String user, String password) throws SQLException
   {
      return getXAConnection();
   }

   // Inner

   public class MockedXAConnection implements InvocationHandler
   {
      private boolean closed;
      private Connection con;
      private XAResource xaResource = new MockedXAResource();

      public MockedXAConnection()
      {
         con = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { Connection.class }, new MockedConnection());
      }
      
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
      {
         String name = method.getName();
         if ("getXAResource".equals(name))
            return xaResource;
         if ("getConnection".equals(name))
            return con;
         if ("close".equals(name))
            closed = true;
         return null;
      }

      class MockedConnection implements InvocationHandler
      {
         private int holdability;
         private int txIsolation;
         private boolean autoCommit;
         private boolean readOnly;
         private String catalog;
         
         public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
         {
            String name = method.getName();
            if ("getUrl".equals(name))
               return url;
            check();
            if ("getHoldability".equals(name))
               return holdability;
            if ("setHoldability".equals(name))
               holdability = (Integer) args[0];
            if ("getTransactionIsolation".equals(name))
               return txIsolation;
            if ("setTransactionIsolation".equals(name))
               txIsolation = (Integer) args[0];
            if ("getAutoCommit".equals(name))
               return autoCommit;
            if ("setAutoCommit".equals(name))
               autoCommit = (Boolean) args[0];
            if ("isClosed".equals(name))
               return closed;
            if ("isReadOnly".equals(name))
               return readOnly;
            if ("setReadOnly".equals(name))
               readOnly = (Boolean) args[0];
            if ("close".equals(name))
               closed = true;
            if ("getCatalog".equals(name))
               return catalog;
            if ("setCatalog".equals(name))
               catalog = (String) args[0];
            if ("getMetaData".equals(name))
               return getMetaData();
            if ("createStatement".equals(name))
               return createStatement();
            return null;
         }

         public DatabaseMetaData getMetaData() throws SQLException
         {
            check();
            return (DatabaseMetaData)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
               new Class[]{DatabaseMetaData.class},
               new InvocationHandler()
               {
                  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                  {
                     if("getURL".equals(method.getName()))
                     {
                        return url;
                     }

                     return new UnsupportedOperationException(
                        "Not implemented: method=" +
                        method.getName() +
                        ", args=" +
                        (args == null ? (Object)"null" : Arrays.asList(args))
                     );
                  }
               }
            );
         }

         public Statement createStatement() throws SQLException
         {
            check();
            return (Statement)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
               new Class[]{Statement.class},
               new InvocationHandler()
               {
                  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                  {
                     String methodName = method.getName();
                     if("execute".equals(methodName))
                     {
                        // let's suppose it went well!
                        return Boolean.FALSE;
                     }

                     return new UnsupportedOperationException(
                        "Not implemented: method=" +
                        methodName +
                        ", args=" +
                        (args == null ? (Object)"null" : Arrays.asList(args))
                     );
                  }
               }
            );
         }

         // Private

         private void check() throws SQLException
         {
            if(stopped)
            {
               throw new SQLException("The database is not available: " + url);
            }
         }
      }
   }

   class MockedXAResource
      implements XAResource
   {
      private int txTimeOut;

      public int getTransactionTimeout() throws XAException
      {
         return txTimeOut;
      }

      public boolean setTransactionTimeout(int i) throws XAException
      {
         this.txTimeOut = i;
         return true;
      }

      public boolean isSameRM(XAResource xaResource) throws XAException
      {
         return xaResource instanceof MockedXAResource;
      }

      public Xid[] recover(int i) throws XAException
      {
         throw new UnsupportedOperationException("recover is not implemented.");
      }

      public int prepare(Xid xid) throws XAException
      {
         return XAResource.XA_OK;
      }

      public void forget(Xid xid) throws XAException
      {
      }

      public void rollback(Xid xid) throws XAException
      {
      }

      public void end(Xid xid, int i) throws XAException
      {
      }

      public void start(Xid xid, int i) throws XAException
      {
      }

      public void commit(Xid xid, boolean b) throws XAException
      {
      }
   }
}
