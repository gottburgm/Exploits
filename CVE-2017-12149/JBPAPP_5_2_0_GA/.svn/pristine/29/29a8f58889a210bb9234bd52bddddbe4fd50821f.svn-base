/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.cmp2.dbschema.util;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


/**
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 */
public class DBSchemaHelper
{
   private static final String TABLE_NAME = "TABLE_NAME";

   public static Table getTable(DatabaseMetaData dbMD, String tableName) throws SQLException
   {
      ResultSet rs = dbMD.getColumns(null, null, tableName, null);
      Map columns = new HashMap();
      while(rs.next())
      {
         final Column column = new Column(rs);
         columns.put(column.getName(), column);
      }
      safeClose(rs);
      return new Table(tableName, columns);
   }

   public static List getTableNames(DatabaseMetaData dbMD) throws SQLException
   {
      ResultSet rs = dbMD.getTables(null, null, null, null);
      List results = new ArrayList();
      while(rs.next())
      {
         results.add(rs.getString(TABLE_NAME));
      }
      safeClose(rs);
      return results;
   }

   public static Connection getConnection(String url, String user, String pwd) throws SQLException
   {
      return DriverManager.getConnection(url, user, pwd);
   }

   public static void safeClose(Connection con)
   {
      if(con != null)
         try
         {
            con.close();
         }
         catch(Exception e){}
   }

   public static void safeClose(ResultSet rs)
   {
      if(rs != null)
         try
         {
            rs.close();
         }
         catch(Exception e){}
   }
}
