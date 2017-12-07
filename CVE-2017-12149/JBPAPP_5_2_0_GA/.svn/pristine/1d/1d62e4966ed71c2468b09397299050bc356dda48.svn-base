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
package org.jboss.test.jca.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * TestConnection.java
 *
 *
 * Created: Fri Feb 14 13:19:39 2003
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class TestConnection implements InvocationHandler
{

   private TestDriver driver;

   private boolean autocommit;

   private boolean closed;

   private boolean rolledBack;

   public boolean isClosed()
   {
      return closed;
   }

   public boolean isRolledBack()
   {
      return rolledBack;
   }

   public TestConnection(TestDriver driver)
   {
      this.driver = driver;
   }
   
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      String name = method.getName();
      if ("getHoldability".equals(name))
         return 0;
      if ("getTransactionIsolation".equals(name))
         return 0;
      if ("isReadOnly".equals(name))
         return false;
      if ("getAutoCommit".equals(name))
         return autocommit;
      if ("setAutoCommit".equals(name))
         autocommit = (Boolean) args[0];
      if ("isClosed".equals(name))
         return closed;
      if ("close".equals(name))
         close();
      if ("nativeSQL".equals(name))
         nativeSQL((String) args[0]);
      if ("rollback".equals(name))
         rollback();
      if ("createStatement".equals(name))
         return createStatement();
      if ("createPreparedStatement".equals(name))
         return createPreparedStatement();
      return null;
   }

   public void setFail(boolean fail)
   {
      driver.setFail(fail);
   }

   public int getClosedCount()
   {
      return driver.getClosedCount();
   }

   // Implementation of java.sql.Connection

   public Statement createStatement(int n, int n1, int n2) throws SQLException {
      return null;
   }

   public void close()
   {
      closed = true;
      driver.connectionClosed();
   }

   public Statement createStatement() throws SQLException
   {
      return (Statement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { Statement.class }, new TestStatement(driver));
   }

   public PreparedStatement createPreparedStatement()
   {
      return (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { PreparedStatement.class }, new TestPreparedStatement(driver));
   }

   public String nativeSQL(String sql)
   {
      if ("ERROR".equals(sql))
      {
         rolledBack = false;
         throw new RuntimeException(sql);
      }
      return sql;
   }

   public void rollback()
   {
      rolledBack = true;
   }
}
