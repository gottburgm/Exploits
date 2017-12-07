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
package org.jboss.resource.adapter.jdbc.jdbc4_1;

import org.jboss.resource.adapter.jdbc.jdbc4.WrappedCallableStatementJDBC4;
import org.jboss.resource.adapter.jdbc.jdbc4.WrappedConnectionJDBC4;

import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public abstract class WrappedCallableStatementJDBC4_1 extends WrappedCallableStatementJDBC4
{
   protected WrappedCallableStatementJDBC4_1(WrappedConnectionJDBC4 lc, CallableStatement s)
   {
      super(lc, s);
   }

   @Override
   public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException
   {
      final CallableStatement statement = getUnderlyingStatement();
      try
      {
         return statement.getObject(parameterIndex, type);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   @Override
   public <T> T getObject(String parameterName, Class<T> type) throws SQLException
   {
      final CallableStatement statement = getUnderlyingStatement();
      try
      {
         return statement.getObject(parameterName, type);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   @Override
   public void closeOnCompletion() throws SQLException
   {
      final CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.closeOnCompletion();
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   @Override
   public boolean isCloseOnCompletion() throws SQLException
   {
      final CallableStatement statement = getUnderlyingStatement();
      try
      {
         return statement.isCloseOnCompletion();
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }
}
