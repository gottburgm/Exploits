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
package org.jboss.resource.adapter.jdbc.jdbc4;

import org.jboss.resource.adapter.jdbc.WrappedCallableStatement;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;

/**
 * WrappedCallableStatementJDBC4.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 112944 $
 */
public abstract class WrappedCallableStatementJDBC4 extends WrappedCallableStatement
{
   public WrappedCallableStatementJDBC4(WrappedConnectionJDBC4 lc, CallableStatement s)
   {
      super(lc, s);
   }
   
   public boolean isClosed() throws SQLException
   {
      try
      {
         PreparedStatement wrapped = getWrappedObject();
         if (wrapped == null)
            return true;
         return wrapped.isClosed();
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public boolean isPoolable() throws SQLException
   {
      PreparedStatement statement = getUnderlyingStatement();
      try
      {
         return statement.isPoolable();
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setPoolable(boolean poolable) throws SQLException
   {
      PreparedStatement statement = getUnderlyingStatement();
      try
      {
         statement.setPoolable(poolable);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException
   {
      PreparedStatement statement = getUnderlyingStatement();
      try
      {
         statement.setAsciiStream(parameterIndex, x, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException
   {
      PreparedStatement statement = getUnderlyingStatement();
      try
      {
         statement.setAsciiStream(parameterIndex, x);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException
   {
      PreparedStatement statement = getUnderlyingStatement();
      try
      {
         statement.setBinaryStream(parameterIndex, x, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException
   {
      PreparedStatement statement = getUnderlyingStatement();
      try
      {
         statement.setBinaryStream(parameterIndex, x);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException
   {
      PreparedStatement statement = getUnderlyingStatement();
      try
      {
         statement.setBlob(parameterIndex, inputStream, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException
   {
      PreparedStatement statement = getUnderlyingStatement();
      try
      {
         statement.setBlob(parameterIndex, inputStream);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException
   {
      PreparedStatement statement = getUnderlyingStatement();
      try
      {
         statement.setCharacterStream(parameterIndex, reader, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException
   {
      PreparedStatement statement = getUnderlyingStatement();
      try
      {
         statement.setCharacterStream(parameterIndex, reader);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setClob(int parameterIndex, Reader reader, long length) throws SQLException
   {
      PreparedStatement statement = getUnderlyingStatement();
      try
      {
         statement.setClob(parameterIndex, reader, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setClob(int parameterIndex, Reader reader) throws SQLException
   {
      PreparedStatement statement = getUnderlyingStatement();
      try
      {
         statement.setClob(parameterIndex, reader);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException
   {
      PreparedStatement statement = getUnderlyingStatement();
      try
      {
         statement.setNCharacterStream(parameterIndex, value, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException
   {
      PreparedStatement statement = getUnderlyingStatement();
      try
      {
         statement.setNCharacterStream(parameterIndex, value);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setNClob(int parameterIndex, NClob value) throws SQLException
   {
      PreparedStatement statement = getUnderlyingStatement();
      try
      {
         statement.setNClob(parameterIndex, value);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException
   {
      PreparedStatement statement = getUnderlyingStatement();
      try
      {
         statement.setNClob(parameterIndex, reader, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setNClob(int parameterIndex, Reader reader) throws SQLException
   {
      PreparedStatement statement = getUnderlyingStatement();
      try
      {
         statement.setNClob(parameterIndex, reader);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setNString(int parameterIndex, String value) throws SQLException
   {
      PreparedStatement statement = getUnderlyingStatement();
      try
      {
         statement.setNString(parameterIndex, value);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setRowId(int parameterIndex, RowId x) throws SQLException
   {
      PreparedStatement statement = getUnderlyingStatement();
      try
      {
         statement.setRowId(parameterIndex, x);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException
   {
      PreparedStatement statement = getUnderlyingStatement();
      try
      {
         statement.setSQLXML(parameterIndex, xmlObject);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public Reader getCharacterStream(int parameterIndex) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         return statement.getCharacterStream(parameterIndex);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public Reader getCharacterStream(String parameterName) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         return statement.getCharacterStream(parameterName);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public Reader getNCharacterStream(int parameterIndex) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         return statement.getNCharacterStream(parameterIndex);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public Reader getNCharacterStream(String parameterName) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         return statement.getCharacterStream(parameterName);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public NClob getNClob(int parameterIndex) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         return statement.getNClob(parameterIndex);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public NClob getNClob(String parameterName) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         return statement.getNClob(parameterName);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public String getNString(int parameterIndex) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         return statement.getNString(parameterIndex);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public String getNString(String parameterName) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         return statement.getNString(parameterName);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public RowId getRowId(int parameterIndex) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         return statement.getRowId(parameterIndex);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public RowId getRowId(String parameterName) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         return statement.getRowId(parameterName);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public SQLXML getSQLXML(int parameterIndex) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         return statement.getSQLXML(parameterIndex);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public SQLXML getSQLXML(String parameterName) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         return statement.getSQLXML(parameterName);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.setAsciiStream(parameterName, x, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setAsciiStream(String parameterName, InputStream x) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.setAsciiStream(parameterName, x);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.setBinaryStream(parameterName, x, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setBinaryStream(String parameterName, InputStream x) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.setBinaryStream(parameterName, x);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setBlob(String parameterName, Blob x) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.setBlob(parameterName, x);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.setBlob(parameterName, inputStream, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setBlob(String parameterName, InputStream inputStream) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.setBlob(parameterName, inputStream);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.setCharacterStream(parameterName, reader, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setCharacterStream(String parameterName, Reader reader) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.setCharacterStream(parameterName, reader);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setClob(String parameterName, Clob x) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.setClob(parameterName, x);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setClob(String parameterName, Reader reader, long length) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.setClob(parameterName, reader, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setClob(String parameterName, Reader reader) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.setClob(parameterName, reader);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.setNCharacterStream(parameterName, value, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setNCharacterStream(String parameterName, Reader value) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.setNCharacterStream(parameterName, value);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setNClob(String parameterName, NClob value) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.setNClob(parameterName, value);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setNClob(String parameterName, Reader reader, long length) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.setNClob(parameterName, reader, length);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setNClob(String parameterName, Reader reader) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.setNClob(parameterName, reader);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setNString(String parameterName, String value) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.setNString(parameterName, value);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setRowId(String parameterName, RowId x) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.setRowId(parameterName, x);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

   public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException
   {
      CallableStatement statement = getUnderlyingStatement();
      try
      {
         statement.setSQLXML(parameterName, xmlObject);
      }
      catch (Throwable t)
      {
         throw checkException(t);
      }
   }

}
