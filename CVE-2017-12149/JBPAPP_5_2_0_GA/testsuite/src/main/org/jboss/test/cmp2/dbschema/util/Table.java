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

import java.util.Map;


/**
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 */
public class Table
{
   private final String table;
   private final Map columnsByName;

   public Table(String table, Map columnsByName)
   {
      this.table = table;
      this.columnsByName = columnsByName;
   }

   public Column getColumn(String name) throws Exception
   {
      Column column = (Column)columnsByName.get(name);
      if(column == null)
         throw new Exception("Column " + name + " not found in table " + table);
      return column;
   }

   public int getColumnsNumber()
   {
      return columnsByName.size();
   }
}
