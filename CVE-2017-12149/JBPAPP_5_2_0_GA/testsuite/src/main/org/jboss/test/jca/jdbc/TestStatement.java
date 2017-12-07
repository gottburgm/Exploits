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
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * TestStatement.java
 *
 *
 * Created: Sat Apr 20 14:29:19 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class TestStatement implements InvocationHandler
{
   private final TestDriver driver;

   int queryTimeout = 0;
   
   public TestStatement(final TestDriver driver)
   {
      this.driver = driver;
   }
   
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      String name = method.getName();
      if ("execute".equals(name))
         return execute((String) args[0]);
      if ("executeQuery".equals(name))
         return executeQuery((String) args[0]);
      if ("executeUpdate".equals(name))
         return executeUpdate((String) args[0]);
      if ("getQueryTimeout".equals(name))
         return getQueryTimeout();
      if ("setQueryTimeout".equals(name))
         setQueryTimeout((Integer) args[0]);
      return null;
   }

   public boolean execute(String sql) throws SQLException
   {
      if (driver.getFail())
         throw new SQLException("asked to fail");
      return false;

   }

   public ResultSet executeQuery(String sql) throws SQLException
   {
      execute(sql);
      return null;
   }

   public int executeUpdate(String sql) throws SQLException
   {
      execute(sql);
      return 0;
   }

   public int getQueryTimeout() throws SQLException
   {
      return queryTimeout;
   }

   public void setQueryTimeout(int timeout) throws SQLException
   {
      this.queryTimeout = timeout;
   }
}
