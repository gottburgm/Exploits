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

import org.jboss.resource.adapter.jdbc.BaseWrapperManagedConnection;
import org.jboss.resource.adapter.jdbc.jdbc4.WrappedConnectionJDBC4;

import java.sql.SQLException;
import java.util.concurrent.Executor;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public abstract class WrappedConnectionJDBC4_1 extends WrappedConnectionJDBC4
{
   protected WrappedConnectionJDBC4_1(BaseWrapperManagedConnection mc)
   {
      super(mc);
   }

   @Override
   public void setSchema(String schema) throws SQLException
   {
      lock();
      try
      {
         getUnderlyingConnection().setSchema(schema);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
      finally
      {
         unlock();
      }
   }

   @Override
   public String getSchema() throws SQLException
   {
      lock();
      try
      {
         return getUnderlyingConnection().getSchema();
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
      finally
      {
         unlock();
      }
   }

   @Override
   public void abort(Executor executor) throws SQLException
   {
      lock();
      try
      {
         getUnderlyingConnection().abort(executor);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
      finally
      {
         unlock();
      }
   }

   @Override
   public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
   {
      lock();
      try
      {
         getUnderlyingConnection().setNetworkTimeout(executor, milliseconds);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
      finally
      {
         unlock();
      }
   }

   @Override
   public int getNetworkTimeout() throws SQLException
   {
      lock();
      try
      {
         return getUnderlyingConnection().getNetworkTimeout();
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
      finally
      {
         unlock();
      }
   }
}
