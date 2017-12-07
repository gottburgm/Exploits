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
package org.jboss.test.cmp2.jbas3541;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCParameterSetter;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCResultSetReader;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 85945 $</tt>
 */
public class IntJDBCAdaptor
   implements JDBCParameterSetter, JDBCResultSetReader
{
   public void set(PreparedStatement ps, int index, int jdbcType, Object value, Logger log) throws SQLException
   {
      if(value == null)
      {
         ps.setNull(index, jdbcType);
      }
      else
      {
         ps.setInt(index, 2);
      }
   }

   public Object get(ResultSet rs, int index, Class destination, Logger log) throws SQLException
   {
      return rs.wasNull() ? null : new Integer(rs.getInt(index));
   }
}
