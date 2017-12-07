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
package org.jboss.resource.adapter.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * A wrapper for a prepared statement.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71788 $
 */
public abstract class WrappedPreparedStatement extends WrappedStatement implements PreparedStatement 
{
   private final PreparedStatement ps;

   public WrappedPreparedStatement(final WrappedConnection lc, final PreparedStatement ps) 
   {
      super(lc, ps);
      this.ps = ps;
   }

   public PreparedStatement getUnderlyingStatement() throws SQLException
   {
      lock();
      try
      {
         checkState();
         if (ps instanceof CachedPreparedStatement)
         {
            return ((CachedPreparedStatement)ps).getUnderlyingPreparedStatement();
         }
         else
         {
            return ps;
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setBoolean(int parameterIndex, boolean value) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setBoolean(parameterIndex, value);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setByte(int parameterIndex, byte value) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setByte(parameterIndex, value);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setShort(int parameterIndex, short value) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setShort(parameterIndex, value);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setInt(int parameterIndex, int value) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setInt(parameterIndex, value);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setLong(int parameterIndex, long value) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setLong(parameterIndex, value);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setFloat(int parameterIndex, float value) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setFloat(parameterIndex, value);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setDouble(int parameterIndex, double value) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setDouble(parameterIndex, value);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setURL(int parameterIndex, URL value) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setURL(parameterIndex, value);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setTime(int parameterIndex, Time value) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setTime(parameterIndex, value);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setTime(int parameterIndex, Time value, Calendar calendar) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setTime(parameterIndex, value, calendar);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public boolean execute() throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try 
         {
            checkConfiguredQueryTimeout();
            return ps.execute();         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public ResultSetMetaData getMetaData() throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            return ps.getMetaData();         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public ResultSet executeQuery() throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try 
         {
            checkConfiguredQueryTimeout();
            ResultSet resultSet = ps.executeQuery();
            return registerResultSet(resultSet);
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public int executeUpdate() throws SQLException
   {
      lock();
      try
      {
         checkTransaction();
         try 
         {
            checkConfiguredQueryTimeout();
            return ps.executeUpdate();         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void addBatch() throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.addBatch();         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setNull(int parameterIndex, int sqlType) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setNull(parameterIndex, sqlType);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setNull(parameterIndex, sqlType, typeName);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setBigDecimal(int parameterIndex, BigDecimal value) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setBigDecimal(parameterIndex, value);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setString(int parameterIndex, String value) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setString(parameterIndex, value);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setBytes(int parameterIndex, byte[] value) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setBytes(parameterIndex, value);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setDate(int parameterIndex, Date value) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setDate(parameterIndex, value);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setDate(int parameterIndex, Date value, Calendar calendar) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setDate(parameterIndex, value, calendar);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setTimestamp(int parameterIndex, Timestamp value) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setTimestamp(parameterIndex, value);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setTimestamp(int parameterIndex, Timestamp value, Calendar calendar) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setTimestamp(parameterIndex, value, calendar);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   @Deprecated
   public void setAsciiStream(int parameterIndex, InputStream stream, int length) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setAsciiStream(parameterIndex, stream, length);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   @Deprecated
   public void setUnicodeStream(int parameterIndex, InputStream stream, int length) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setUnicodeStream(parameterIndex, stream, length);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setBinaryStream(int parameterIndex, InputStream stream, int length) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setBinaryStream(parameterIndex, stream, length);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void clearParameters() throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.clearParameters();         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setObject(int parameterIndex, Object value, int sqlType, int scale) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setObject(parameterIndex, value, sqlType, scale);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setObject(int parameterIndex, Object value, int sqlType) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setObject(parameterIndex, value, sqlType);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setObject(int parameterIndex, Object value) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setObject(parameterIndex, value);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setCharacterStream(parameterIndex, reader, length);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setRef(int parameterIndex, Ref value) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setRef(parameterIndex, value);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setBlob(int parameterIndex, Blob value) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setBlob(parameterIndex, value);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setClob(int parameterIndex, Clob value) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setClob(parameterIndex, value);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public void setArray(int parameterIndex, Array value) throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            ps.setArray(parameterIndex, value);         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   public ParameterMetaData getParameterMetaData() throws SQLException
   {
      lock();
      try
      {
         checkState();
         try 
         {
            return ps.getParameterMetaData();         
         }
         catch (Throwable t)
         {
            throw checkException(t);
         }
      }
      finally
      {
         unlock();
      }
   }

   protected PreparedStatement getWrappedObject() throws SQLException
   {
      return (PreparedStatement) super.getWrappedObject();
   }
}
