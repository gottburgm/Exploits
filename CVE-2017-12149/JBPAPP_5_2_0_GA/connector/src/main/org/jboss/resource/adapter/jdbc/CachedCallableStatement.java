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
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * A cache wrapper for java.sql.CallableStatement
 *
 * @author <a href="mailto:andrewarro@mail.ru">Andrew Belomutskiy</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 112944 $
 */
@SuppressWarnings("deprecation")
public abstract class CachedCallableStatement extends CachedPreparedStatement
   implements CallableStatement
{
   protected final CallableStatement cs;

   public CachedCallableStatement(CallableStatement ps) throws SQLException
   {
      super(ps);
      this.cs = ps;
   }

   public boolean wasNull() throws SQLException
   {
      return cs.wasNull();
   }

   public byte getByte(int parameterIndex) throws SQLException
   {
      return cs.getByte(parameterIndex);
   }

   public double getDouble(int parameterIndex) throws SQLException
   {
      return cs.getDouble(parameterIndex);
   }

   public float getFloat(int parameterIndex) throws SQLException
   {
      return cs.getFloat(parameterIndex);
   }

   public int getInt(int parameterIndex) throws SQLException
   {
      return cs.getInt(parameterIndex);
   }

   public long getLong(int parameterIndex) throws SQLException
   {
      return cs.getLong(parameterIndex);
   }

   public short getShort(int parameterIndex) throws SQLException
   {
      return cs.getShort(parameterIndex);
   }

   public boolean getBoolean(int parameterIndex) throws SQLException
   {
      return cs.getBoolean(parameterIndex);
   }

   public byte[] getBytes(int parameterIndex) throws SQLException
   {
      return cs.getBytes(parameterIndex);
   }

   public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException
   {
      cs.registerOutParameter(parameterIndex, sqlType);
   }

   public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException
   {
      cs.registerOutParameter(parameterIndex, sqlType, scale);
   }

   public Object getObject(int parameterIndex) throws SQLException
   {
      return cs.getObject(parameterIndex);
   }

   public String getString(int parameterIndex) throws SQLException
   {
      return cs.getString(parameterIndex);
   }

   public void registerOutParameter(int paramIndex, int sqlType, String typeName) throws SQLException
   {
      cs.registerOutParameter(paramIndex, sqlType, typeName);
   }

   public byte getByte(String parameterName) throws SQLException
   {
      return cs.getByte(parameterName);
   }

   public double getDouble(String parameterName) throws SQLException
   {
      return cs.getDouble(parameterName);
   }

   public float getFloat(String parameterName) throws SQLException
   {
      return cs.getFloat(parameterName);
   }

   public int getInt(String parameterName) throws SQLException
   {
      return cs.getInt(parameterName);
   }

   public long getLong(String parameterName) throws SQLException
   {
      return cs.getLong(parameterName);
   }

   public short getShort(String parameterName) throws SQLException
   {
      return cs.getShort(parameterName);
   }

   public boolean getBoolean(String parameterName) throws SQLException
   {
      return cs.getBoolean(parameterName);
   }

   public byte[] getBytes(String parameterName) throws SQLException
   {
      return cs.getBytes(parameterName);
   }

   public void setByte(String parameterName, byte x) throws SQLException
   {
      cs.setByte(parameterName, x);
   }

   public void setDouble(String parameterName, double x) throws SQLException
   {
      cs.setDouble(parameterName, x);
   }

   public void setFloat(String parameterName, float x) throws SQLException
   {
      cs.setFloat(parameterName, x);
   }

   public void registerOutParameter(String parameterName, int sqlType) throws SQLException
   {
      cs.registerOutParameter(parameterName, sqlType);
   }

   public void setInt(String parameterName, int x) throws SQLException
   {
      cs.setInt(parameterName, x);
   }

   public void setNull(String parameterName, int sqlType) throws SQLException
   {
      cs.setNull(parameterName, sqlType);
   }

   public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException
   {
      cs.registerOutParameter(parameterName, sqlType, scale);
   }

   public void setLong(String parameterName, long x) throws SQLException
   {
      cs.setLong(parameterName, x);
   }

   public void setShort(String parameterName, short x) throws SQLException
   {
      cs.setShort(parameterName, x);
   }

   public void setBoolean(String parameterName, boolean x) throws SQLException
   {
      cs.setBoolean(parameterName, x);
   }

   public void setBytes(String parameterName, byte[] x) throws SQLException
   {
      cs.setBytes(parameterName, x);
   }

   public BigDecimal getBigDecimal(int parameterIndex) throws SQLException
   {
      return cs.getBigDecimal(parameterIndex);
   }

   public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException
   {
      return cs.getBigDecimal(parameterIndex, scale);
   }

   public URL getURL(int parameterIndex) throws SQLException
   {
      return cs.getURL(parameterIndex);
   }

   public Array getArray(int i) throws SQLException
   {
      return cs.getArray(i);
   }

   public Blob getBlob(int i) throws SQLException
   {
      return cs.getBlob(i);
   }

   public Clob getClob(int i) throws SQLException
   {
      return cs.getClob(i);
   }

   public Date getDate(int parameterIndex) throws SQLException
   {
      return cs.getDate(parameterIndex);
   }

   public Ref getRef(int i) throws SQLException
   {
      return cs.getRef(i);
   }

   public Time getTime(int parameterIndex) throws SQLException
   {
      return cs.getTime(parameterIndex);
   }

   public Timestamp getTimestamp(int parameterIndex) throws SQLException
   {
      return cs.getTimestamp(parameterIndex);
   }

   public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException
   {
      cs.setAsciiStream(parameterName, x, length);
   }

   public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException
   {
      cs.setBinaryStream(parameterName, x, length);
   }

   public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException
   {
      cs.setCharacterStream(parameterName, reader, length);
   }

   public Object getObject(String parameterName) throws SQLException
   {
      return cs.getObject(parameterName);
   }

   public void setObject(String parameterName, Object x) throws SQLException
   {
      cs.setObject(parameterName, x);
   }

   public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException
   {
      cs.setObject(parameterName, x, targetSqlType);
   }

   public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException
   {
      cs.setObject(parameterName, x, targetSqlType, scale);
   }

   @SuppressWarnings("unchecked")
   public Object getObject(int i, Map map) throws SQLException
   {
      return cs.getObject(i, map);
   }

   public String getString(String parameterName) throws SQLException
   {
      return cs.getString(parameterName);
   }

   public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException
   {
      cs.registerOutParameter(parameterName, sqlType, typeName);
   }

   public void setNull(String parameterName, int sqlType, String typeName) throws SQLException
   {
      cs.setNull(parameterName, sqlType, typeName);
   }

   public void setString(String parameterName, String x) throws SQLException
   {
      cs.setString(parameterName, x);
   }

   public BigDecimal getBigDecimal(String parameterName) throws SQLException
   {
      return cs.getBigDecimal(parameterName);
   }

   public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException
   {
      cs.setBigDecimal(parameterName, x);
   }

   public URL getURL(String parameterName) throws SQLException
   {
      return cs.getURL(parameterName);
   }

   public void setURL(String parameterName, URL val) throws SQLException
   {
      cs.setURL(parameterName, val);
   }

   public Array getArray(String parameterName) throws SQLException
   {
      return cs.getArray(parameterName);
   }

   public Blob getBlob(String parameterName) throws SQLException
   {
      return cs.getBlob(parameterName);
   }

   public Clob getClob(String parameterName) throws SQLException
   {
      return cs.getClob(parameterName);
   }

   public Date getDate(String parameterName) throws SQLException
   {
      return cs.getDate(parameterName);
   }

   public void setDate(String parameterName, Date x) throws SQLException
   {
      cs.setDate(parameterName, x);
   }

   public Date getDate(int parameterIndex, Calendar cal) throws SQLException
   {
      return cs.getDate(parameterIndex, cal);
   }

   public Ref getRef(String parameterName) throws SQLException
   {
      return cs.getRef(parameterName);
   }

   public Time getTime(String parameterName) throws SQLException
   {
      return cs.getTime(parameterName);
   }

   public void setTime(String parameterName, Time x) throws SQLException
   {
      cs.setTime(parameterName, x);
   }

   public Time getTime(int parameterIndex, Calendar cal) throws SQLException
   {
      return cs.getTime(parameterIndex, cal);
   }

   public Timestamp getTimestamp(String parameterName) throws SQLException
   {
      return cs.getTimestamp(parameterName);
   }

   public void setTimestamp(String parameterName, Timestamp x) throws SQLException
   {
      cs.setTimestamp(parameterName, x);
   }

   public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException
   {
      return cs.getTimestamp(parameterIndex, cal);
   }

   @SuppressWarnings("unchecked")
   public Object getObject(String parameterName, Map map) throws SQLException
   {
      return cs.getObject(parameterName, map);
   }

   public Date getDate(String parameterName, Calendar cal) throws SQLException
   {
      return cs.getDate(parameterName, cal);
   }

   public Time getTime(String parameterName, Calendar cal) throws SQLException
   {
      return cs.getTime(parameterName, cal);
   }

   public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException
   {
      return cs.getTimestamp(parameterName, cal);
   }

   public void setDate(String parameterName, Date x, Calendar cal) throws SQLException
   {
      cs.setDate(parameterName, x, cal);
   }

   public void setTime(String parameterName, Time x, Calendar cal) throws SQLException
   {
      cs.setTime(parameterName, x, cal);
   }

   public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException
   {
      cs.setTimestamp(parameterName, x, cal);
   }

   @Deprecated
   protected CallableStatement getWrappedObject() throws SQLException
   {
      return cs;
   }
}
