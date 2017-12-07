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
package org.jboss.test.cmp2.lob;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.ResultSet;
import java.sql.Blob;
import java.io.IOException;

import org.jboss.ejb.plugins.cmp.jdbc.JDBCParameterSetter;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCResultSetReader;
import org.jboss.logging.Logger;
import org.jboss.invocation.MarshalledValue;

/**
 @author Scott.Stark@jboss.org
 @version $Revision: 85945 $
 */
public class GetSetObjectParameter
   implements JDBCParameterSetter, JDBCResultSetReader
{
   public Object get(ResultSet rs, int index, Class destination, Logger log) throws SQLException
   {
      Object value = rs.getObject(index);
      if(log.isTraceEnabled())
         log.trace("get: i=" + index + ", destination="+destination+", value=" + value);
      Object result = value;
      if(rs.wasNull())
      {
         result = null;
      }
      else if( value instanceof MarshalledValue )
      {
         MarshalledValue mv = (MarshalledValue) value;
         try
         {
            result = mv.get();
         }
         catch (Exception e)
         {
            SQLException sqe = new SQLException("Unable to extract MarshalledValue");
            sqe.initCause(e);
            throw sqe;
         }
      }
      return result;
   }

   public void set(PreparedStatement ps, int index, int jdbcType, Object value,
      Logger log) throws SQLException
   {
      if(log.isTraceEnabled())
         log.trace("set: i=" + index + ", jdbcType="+jdbcType+", value=" + value);
      if( value instanceof MarshalledValue )
      {
         MarshalledValue mv = (MarshalledValue) value;
         try
         {
            value = mv.get();
         }
         catch (Exception e)
         {
            SQLException sqe = new SQLException("Unable to extract MarshalledValue");
            sqe.initCause(e);
            throw sqe;
         }
      }

      switch( jdbcType )
      {
         case Types.BLOB:
         {
            ps.setObject(index, value, jdbcType);
            break;
         }
         case Types.OTHER:
            ps.setObject(index, value);
            break;
         default:
            throw new SQLException("Unsupported jdbcType: "+jdbcType);
      }
   }
}
