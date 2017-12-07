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

import java.io.Serializable;
import java.sql.ParameterMetaData;
import java.sql.SQLException;

import org.jboss.resource.adapter.jdbc.JBossWrapper;

/**
 * @author <a href="mailto:telrod@e2technologies.net">Tom Elrod</a>
 * @version $Revision: 71554 $
 */
public class SerializableParameterMetaData extends JBossWrapper implements ParameterMetaData, Serializable
{
   /** @since 1.1 */
   static final long serialVersionUID = -6601828413479683906L;
   int parameterCount = 0;

   public SerializableParameterMetaData(ParameterMetaData pMetaData) throws SQLException
   {
      this.parameterCount = pMetaData.getParameterCount();
   }

   /**
    * Retrieves the number of parameters in the <code>PreparedStatement</code>
    * object for which this <code>ParameterMetaData</code> object contains
    * information.
    *
    * @return the number of parameters
    * @throws java.sql.SQLException if a database access error occurs
    * @since 1.4
    */
   public int getParameterCount() throws SQLException
   {
      return parameterCount;
   }

   /**
    * Retrieves the designated parameter's mode.
    *
    * @param param the first parameter is 1, the second is 2, ...
    * @return mode of the parameter; one of
    *         <code>ParameterMetaData.parameterModeIn</code>,
    *         <code>ParameterMetaData.parameterModeOut</code>, or
    *         <code>ParameterMetaData.parameterModeInOut</code>
    *         <code>ParameterMetaData.parameterModeUnknown</code>.
    * @throws java.sql.SQLException if a database access error occurs
    * @since 1.4
    */
   public int getParameterMode(int param) throws SQLException
   {
      return 0;
   }

   /**
    * Retrieves the designated parameter's SQL type.
    *
    * @param param the first parameter is 1, the second is 2, ...
    * @return SQL type from <code>java.sql.Types</code>
    * @throws java.sql.SQLException if a database access error occurs
    * @see java.sql.Types
    * @since 1.4
    */
   public int getParameterType(int param) throws SQLException
   {
      return 0;
   }

   /**
    * Retrieves the designated parameter's number of decimal digits.
    *
    * @param param the first parameter is 1, the second is 2, ...
    * @return precision
    * @throws java.sql.SQLException if a database access error occurs
    * @since 1.4
    */
   public int getPrecision(int param) throws SQLException
   {
      return 0;
   }

   /**
    * Retrieves the designated parameter's number of digits to right of the decimal point.
    *
    * @param param the first parameter is 1, the second is 2, ...
    * @return scale
    * @throws java.sql.SQLException if a database access error occurs
    * @since 1.4
    */
   public int getScale(int param) throws SQLException
   {
      return 0;
   }

   /**
    * Retrieves whether null values are allowed in the designated parameter.
    *
    * @param param the first parameter is 1, the second is 2, ...
    * @return the nullability status of the given parameter; one of
    *         <code>ParameterMetaData.parameterNoNulls</code>,
    *         <code>ParameterMetaData.parameterNullable</code>, or
    *         <code>ParameterMetaData.parameterNullableUnknown</code>
    * @throws java.sql.SQLException if a database access error occurs
    * @since 1.4
    */
   public int isNullable(int param) throws SQLException
   {
      return 0;
   }

   /**
    * Retrieves whether values for the designated parameter can be signed numbers.
    *
    * @param param the first parameter is 1, the second is 2, ...
    * @return <code>true</code> if so; <code>false</code> otherwise
    * @throws java.sql.SQLException if a database access error occurs
    * @since 1.4
    */
   public boolean isSigned(int param) throws SQLException
   {
      return false;
   }

   /**
    * Retrieves the fully-qualified name of the Java class whose instances
    * should be passed to the method <code>PreparedStatement.setObject</code>.
    *
    * @param param the first parameter is 1, the second is 2, ...
    * @return the fully-qualified name of the class in the Java programming
    *         language that would be used by the method
    *         <code>PreparedStatement.setObject</code> to set the value
    *         in the specified parameter. This is the class name used
    *         for custom mapping.
    * @throws java.sql.SQLException if a database access error occurs
    * @since 1.4
    */
   public String getParameterClassName(int param) throws SQLException
   {
      return null;
   }

   /**
    * Retrieves the designated parameter's database-specific type name.
    *
    * @param param the first parameter is 1, the second is 2, ...
    * @return type the name used by the database. If the parameter type is
    *         a user-defined type, then a fully-qualified type name is returned.
    * @throws java.sql.SQLException if a database access error occurs
    * @since 1.4
    */
   public String getParameterTypeName(int param) throws SQLException
   {
      return null;
   }
}


