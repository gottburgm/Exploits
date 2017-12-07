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

import org.jboss.resource.adapter.jdbc.jdbc4.WrappedConnectionJDBC4;
import org.jboss.resource.adapter.jdbc.jdbc4.WrappedStatementJDBC4;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public abstract class WrappedStatementJDBC4_1 extends WrappedStatementJDBC4
{
   protected WrappedStatementJDBC4_1(WrappedConnectionJDBC4 lc, Statement s)
   {
      super(lc, s);
   }

   @Override
   public void closeOnCompletion() throws SQLException
   {
      final Statement statement = getUnderlyingStatement();
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
      final Statement statement = getUnderlyingStatement();
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
