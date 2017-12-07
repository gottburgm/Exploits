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

import org.jboss.resource.adapter.jdbc.CachedCallableStatement;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;

/**
 * CachedCallableStatementJDBC4.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 112944 $
 */
@SuppressWarnings("deprecation")
public abstract class CachedCallableStatementJDBC4 extends CachedCallableStatement
{
   public CachedCallableStatementJDBC4(CallableStatement cs) throws SQLException
   {
      super(cs);
   }

   public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException
   {
      getWrappedObject().setAsciiStream(parameterIndex, x, length);
   }

   public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException
   {
      getWrappedObject().setAsciiStream(parameterIndex, x);
   }

   public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException
   {
      getWrappedObject().setBinaryStream(parameterIndex, x, length);
   }

   public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException
   {
      getWrappedObject().setBinaryStream(parameterIndex, x);
   }

   public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException
   {
      getWrappedObject().setBlob(parameterIndex, inputStream, length);
   }

   public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException
   {
      getWrappedObject().setBlob(parameterIndex, inputStream);
   }

   public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException
   {
      getWrappedObject().setCharacterStream(parameterIndex, reader, length);
   }

   public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException
   {
      getWrappedObject().setCharacterStream(parameterIndex, reader);
   }

   public void setClob(int parameterIndex, Reader reader, long length) throws SQLException
   {
      getWrappedObject().setClob(parameterIndex, reader, length);
   }

   public void setClob(int parameterIndex, Reader reader) throws SQLException
   {
      getWrappedObject().setClob(parameterIndex, reader);
   }

   public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException
   {
      getWrappedObject().setNCharacterStream(parameterIndex, value, length);
   }

   public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException
   {
      getWrappedObject().setNCharacterStream(parameterIndex, value);
   }

   public void setNClob(int parameterIndex, NClob value) throws SQLException
   {
      getWrappedObject().setNClob(parameterIndex, value);
   }

   public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException
   {
      getWrappedObject().setNClob(parameterIndex, reader, length);
   }

   public void setNClob(int parameterIndex, Reader reader) throws SQLException
   {
      getWrappedObject().setNClob(parameterIndex, reader);
   }

   public void setNString(int parameterIndex, String value) throws SQLException
   {
      getWrappedObject().setNString(parameterIndex, value);
   }

   public void setRowId(int parameterIndex, RowId x) throws SQLException
   {
      getWrappedObject().setRowId(parameterIndex, x);
   }

   public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException
   {
      getWrappedObject().setSQLXML(parameterIndex, xmlObject);
   }

   public boolean isClosed() throws SQLException
   {
      return getWrappedObject().isClosed();
   }

   public boolean isPoolable() throws SQLException
   {
      return getWrappedObject().isPoolable();
   }

   public void setPoolable(boolean poolable) throws SQLException
   {
      getWrappedObject().setPoolable(poolable);
   }

   public Reader getCharacterStream(int parameterIndex) throws SQLException
   {
      return getWrappedObject().getCharacterStream(parameterIndex);
   }

   public Reader getCharacterStream(String parameterName) throws SQLException
   {
      return getWrappedObject().getCharacterStream(parameterName);
   }

   public Reader getNCharacterStream(int parameterIndex) throws SQLException
   {
      return getWrappedObject().getNCharacterStream(parameterIndex);
   }

   public Reader getNCharacterStream(String parameterName) throws SQLException
   {
      return getWrappedObject().getNCharacterStream(parameterName);
   }

   public NClob getNClob(int parameterIndex) throws SQLException
   {
      return getWrappedObject().getNClob(parameterIndex);
   }

   public NClob getNClob(String parameterName) throws SQLException
   {
      return getWrappedObject().getNClob(parameterName);
   }

   public String getNString(int parameterIndex) throws SQLException
   {
      return getWrappedObject().getNString(parameterIndex);
   }

   public String getNString(String parameterName) throws SQLException
   {
      return getWrappedObject().getNString(parameterName);
   }

   public RowId getRowId(int parameterIndex) throws SQLException
   {
      return getWrappedObject().getRowId(parameterIndex);
   }

   public RowId getRowId(String parameterName) throws SQLException
   {
      return getWrappedObject().getRowId(parameterName);
   }

   public SQLXML getSQLXML(int parameterIndex) throws SQLException
   {
      return getWrappedObject().getSQLXML(parameterIndex);
   }

   public SQLXML getSQLXML(String parameterName) throws SQLException
   {
      return getWrappedObject().getSQLXML(parameterName);
   }

   public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException
   {
      getWrappedObject().setAsciiStream(parameterName, x, length);
   }

   public void setAsciiStream(String parameterName, InputStream x) throws SQLException
   {
      getWrappedObject().setAsciiStream(parameterName, x);
   }

   public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException
   {
      getWrappedObject().setBinaryStream(parameterName, x, length);
   }

   public void setBinaryStream(String parameterName, InputStream x) throws SQLException
   {
      getWrappedObject().setBinaryStream(parameterName, x);
   }

   public void setBlob(String parameterName, Blob x) throws SQLException
   {
      getWrappedObject().setBlob(parameterName, x);
   }

   public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException
   {
      getWrappedObject().setBlob(parameterName, inputStream, length);
   }

   public void setBlob(String parameterName, InputStream inputStream) throws SQLException
   {
      getWrappedObject().setBlob(parameterName, inputStream);
   }

   public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException
   {
      getWrappedObject().setCharacterStream(parameterName, reader, length);
   }

   public void setCharacterStream(String parameterName, Reader reader) throws SQLException
   {
      getWrappedObject().setCharacterStream(parameterName, reader);
   }

   public void setClob(String parameterName, Clob x) throws SQLException
   {
      getWrappedObject().setClob(parameterName, x);
   }

   public void setClob(String parameterName, Reader reader, long length) throws SQLException
   {
      getWrappedObject().setClob(parameterName, reader, length);
   }

   public void setClob(String parameterName, Reader reader) throws SQLException
   {
      getWrappedObject().setClob(parameterName, reader);
   }

   public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException
   {
      getWrappedObject().setNCharacterStream(parameterName, value, length);
   }

   public void setNCharacterStream(String parameterName, Reader value) throws SQLException
   {
      getWrappedObject().setNCharacterStream(parameterName, value);
   }

   public void setNClob(String parameterName, NClob value) throws SQLException
   {
      getWrappedObject().setNClob(parameterName, value);
   }

   public void setNClob(String parameterName, Reader reader, long length) throws SQLException
   {
      getWrappedObject().setNClob(parameterName, reader, length);
   }

   public void setNClob(String parameterName, Reader reader) throws SQLException
   {
      getWrappedObject().setNClob(parameterName, reader);
   }

   public void setNString(String parameterName, String value) throws SQLException
   {
      getWrappedObject().setNString(parameterName, value);
   }

   public void setRowId(String parameterName, RowId x) throws SQLException
   {
      getWrappedObject().setRowId(parameterName, x);
   }

   public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException
   {
      getWrappedObject().setSQLXML(parameterName, xmlObject);
   }
}
