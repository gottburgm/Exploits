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
package org.jboss.resource.adapter.jdbc.jdk7;

import org.jboss.resource.adapter.jdbc.BaseWrapperManagedConnection;
import org.jboss.resource.adapter.jdbc.CachedCallableStatement;
import org.jboss.resource.adapter.jdbc.CachedPreparedStatement;
import org.jboss.resource.adapter.jdbc.WrappedCallableStatement;
import org.jboss.resource.adapter.jdbc.WrappedConnection;
import org.jboss.resource.adapter.jdbc.WrappedConnectionFactory;
import org.jboss.resource.adapter.jdbc.WrappedPreparedStatement;
import org.jboss.resource.adapter.jdbc.WrappedResultSet;
import org.jboss.resource.adapter.jdbc.WrappedStatement;
import org.jboss.resource.adapter.jdbc.jdbc4_1.CachedCallableStatementJDBC4_1;
import org.jboss.resource.adapter.jdbc.jdbc4_1.CachedPreparedStatementJDBC4_1;
import org.jboss.resource.adapter.jdbc.jdbc4_1.WrappedCallableStatementJDBC4_1;
import org.jboss.resource.adapter.jdbc.jdbc4_1.WrappedConnectionJDBC4_1;
import org.jboss.resource.adapter.jdbc.jdbc4_1.WrappedPreparedStatementJDBC4_1;
import org.jboss.resource.adapter.jdbc.jdbc4_1.WrappedResultSetJDBC4_1;
import org.jboss.resource.adapter.jdbc.jdbc4_1.WrappedStatementJDBC4_1;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class WrappedConnectionFactoryJDK7 implements WrappedConnectionFactory
{
   @Override
   public WrappedConnection createWrappedConnection(BaseWrapperManagedConnection mc)
   {
      return new WrappedConnectionJDBC4_1(mc)
      {
         @Override
         protected WrappedStatement wrapStatement(Statement statement)
         {
            return new WrappedStatementJDBC4_1(this, statement)
            {
               @Override
               protected WrappedResultSet wrapResultSet(ResultSet resultSet)
               {
                  return new WrappedResultSetJDBC4_1(this, resultSet) {};
               }
            };
         }

         @Override
         protected WrappedPreparedStatement wrapPreparedStatement(PreparedStatement statement)
         {
            return new WrappedPreparedStatementJDBC4_1(this, statement)
            {
               @Override
               protected WrappedResultSet wrapResultSet(ResultSet resultSet)
               {
                  return new WrappedResultSetJDBC4_1(this, resultSet) {};
               }
            };
         }

         @Override
         protected WrappedCallableStatement wrapCallableStatement(CallableStatement statement)
         {
            return new WrappedCallableStatementJDBC4_1(this, statement)
            {
               @Override
               protected WrappedResultSet wrapResultSet(ResultSet resultSet)
               {
                  return new WrappedResultSetJDBC4_1(this, resultSet) {};
               }
            };
         }
      };
   }

   @Override
   public CachedPreparedStatement createCachedPreparedStatement(PreparedStatement ps) throws SQLException
   {
      return new CachedPreparedStatementJDBC4_1(ps) {};
   }

   @Override
   public CachedCallableStatement createCachedCallableStatement(CallableStatement cs) throws SQLException
   {
      return new CachedCallableStatementJDBC4_1(cs) {};
   }
}
