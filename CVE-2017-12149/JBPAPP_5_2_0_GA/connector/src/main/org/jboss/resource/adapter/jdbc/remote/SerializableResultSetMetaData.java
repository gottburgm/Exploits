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
package org.jboss.resource.adapter.jdbc.remote;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.Serializable;

import org.jboss.resource.adapter.jdbc.JBossWrapper;

/** A wrapper to marshall ResultSetMetaData remotely.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 74771 $
 */
public class   SerializableResultSetMetaData extends JBossWrapper
   implements ResultSetMetaData, Serializable
{
   /** @since 1.3 */
   static final long serialVersionUID = -6663485432752348789L;

   private ColumnData[] columnData;

   private static class ColumnData implements Serializable
   {
      static final long serialVersionUID = 5060626133767712300L;
      String className;
      String label;
      String name;
      int type;
      String typeName;
   }

   SerializableResultSetMetaData(ResultSetMetaData metaData) throws SQLException
   {
      int count = metaData.getColumnCount();
      columnData = new ColumnData[count+1];
      for(int c = 1; c <= count; c ++)
      {
         ColumnData data = new ColumnData();
         columnData[c] = data;
         data.label = metaData.getColumnLabel(c);
         data.name = metaData.getColumnName(c);
         data.type = metaData.getColumnType(c);
         data.className = metaData.getColumnClassName(c);
      }
   }

   public int getColumnCount() throws SQLException
   {
      // Adjust the usable count by 1 for the 1 base index
      return columnData.length - 1;
   }

   public boolean isAutoIncrement(int column) throws SQLException
   {
      return false;
   }

   public boolean isCaseSensitive(int column) throws SQLException
   {
      return false;
   }

   public boolean isSearchable(int column) throws SQLException
   {
      return false;
   }

   public boolean isCurrency(int column) throws SQLException
   {
      return false;
   }

   public int isNullable(int column) throws SQLException
   {
      return 0;
   }

   public boolean isSigned(int column) throws SQLException
   {
      return false;
   }

   public int getColumnDisplaySize(int column) throws SQLException
   {
      return 0;
   }

   public String getColumnLabel(int column) throws SQLException
   {
      return columnData[column].label;
   }

   public String getColumnName(int column) throws SQLException
   {
      return columnData[column].name;
   }

   public String getSchemaName(int column) throws SQLException
   {
      return null;
   }

   public int getPrecision(int column) throws SQLException
   {
      return 0;
   }

   public int getScale(int column) throws SQLException
   {
      return 0;
   }

   public String getTableName(int column) throws SQLException
   {
      return "";
   }

   public String getCatalogName(int column) throws SQLException
   {
      return "";
   }

   public int getColumnType(int column) throws SQLException
   {
      return columnData[column].type;
   }

   public String getColumnTypeName(int column) throws SQLException
   {
      return columnData[column].typeName;
   }

   public boolean isReadOnly(int column) throws SQLException
   {
      return false;
   }

   public boolean isWritable(int column) throws SQLException
   {
      return false;
   }

   public boolean isDefinitelyWritable(int column) throws SQLException
   {
      return false;
   }

   public String getColumnClassName(int column) throws SQLException
   {
      return columnData[column].className;
   }
}
