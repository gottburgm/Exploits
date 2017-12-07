/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2012, Red Hat, Inc., and individual contributors
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
abstract class AbstractDataSourceInterceptorJDBC4_1 extends AbstractDataSourceInterceptorJDBC4
{
   public Logger getParentLogger() throws SQLFeatureNotSupportedException
   {
      return target.getParentLogger();
   }

   // Inner

   protected abstract class ConnectionInterceptor extends AbstractDataSourceInterceptorJDBC4.ConnectionInterceptor
   {
      protected ConnectionInterceptor(Connection target)
      {
         super(target);
      }

      public void setSchema(String schema) throws SQLException
      {
         target.setSchema(schema);
      }

      public String getSchema() throws SQLException
      {
         return target.getSchema();
      }

      public void abort(Executor executor) throws SQLException
      {
         target.abort(executor);
      }

      public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
      {
         target.setNetworkTimeout(executor, milliseconds);
      }

      public int getNetworkTimeout() throws SQLException
      {
         return target.getNetworkTimeout();
      }
   }

   protected abstract class StatementInterceptor extends AbstractDataSourceInterceptorJDBC4.StatementInterceptor
   {
      protected StatementInterceptor(Connection con, Statement target)
      {
         super(con, target);
      }

      public void closeOnCompletion() throws SQLException
      {
         target.closeOnCompletion();
      }

      public boolean isCloseOnCompletion() throws SQLException
      {
         return target.isCloseOnCompletion();
      }
   }

   protected abstract class PreparedStatementInterceptor extends AbstractDataSourceInterceptorJDBC4.PreparedStatementInterceptor
           implements PreparedStatement
   {
      public PreparedStatementInterceptor(Connection con, PreparedStatement target)
      {
         super(con, target);
      }

      public void closeOnCompletion() throws SQLException
      {
         target.closeOnCompletion();
      }

      public boolean isCloseOnCompletion() throws SQLException
      {
         return target.isCloseOnCompletion();
      }
   }
}
