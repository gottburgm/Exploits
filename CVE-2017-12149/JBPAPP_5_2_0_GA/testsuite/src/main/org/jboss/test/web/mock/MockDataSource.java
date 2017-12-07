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
package org.jboss.test.web.mock;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * A noop DataSource implementation
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 112944 $
 */
public class MockDataSource implements DataSource, Serializable
{
   private static final long serialVersionUID = 1;

   public MockDataSource()
   {
     System.err.println("MockDataSource");
   }

   public boolean isWrapperFor(Class<?> iface) throws SQLException
   {
      return false;
   }

   public <T> T unwrap(Class<T> iface) throws SQLException
   {
      throw new SQLException("No wrapper");
   }

   public Connection getConnection() throws SQLException
   {
      return null;
   }

   public Connection getConnection(String arg0, String arg1) throws SQLException
   {
      return null;
   }

   public int getLoginTimeout() throws SQLException
   {
      return 0;
   }

   public Logger getParentLogger() throws SQLFeatureNotSupportedException
   {
      throw new SQLFeatureNotSupportedException("NYI: org.jboss.test.web.mock.MockDataSource.getParentLogger");
   }

   public PrintWriter getLogWriter() throws SQLException
   {
      return null;
   }

   public void setLoginTimeout(int arg0) throws SQLException
   {
      
   }

   public void setLogWriter(PrintWriter arg0) throws SQLException
   {
      
   }

}
