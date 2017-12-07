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
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.sql.Types;

import org.jboss.deployment.DeploymentException;
import org.jboss.logging.Logger;

import org.jboss.metadata.MetaData;
import org.w3c.dom.Element;

/**
 * Imutable class which holds a mapping between a Java Class and a JDBC type
 * and a SQL type.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @version $Revision: 81030 $
 */
public final class JDBCMappingMetaData
{
   private static Logger log = Logger.getLogger(JDBCMappingMetaData.class.getName());

   /**
    * Gets the JDBC type constant int for the name. The mapping from name to jdbc
    * type is contained in java.sql.Types.
    *
    * @param name the name for the jdbc type
    * @return the int type constant from java.sql.Types
    * @see java.sql.Types
    */
   public static int getJdbcTypeFromName(String name) throws DeploymentException
   {
      if(name == null)
      {
         throw new DeploymentException("jdbc-type cannot be null");
      }

      try
      {
         Integer constant = (Integer)Types.class.getField(name).get(null);
         return constant.intValue();

      }
      catch(Exception e)
      {
         log.warn("Unrecognized jdbc-type: " + name + ", using Types.OTHER", e);
         return Types.OTHER;
      }
   }

   /** fully qualified Java type name */
   private final String javaType;
   /** JDBC type according to java.sql.Types */
   private final int jdbcType;
   /** SQL type */
   private final String sqlType;
   /** parameter setter */
   private final String paramSetter;
   /** result set reader */
   private final String resultReader;

   /**
    * Constructs a mapping with the data contained in the mapping xml element
    * from a jbosscmp-jdbc xml file.
    *
    * @param element the xml Element which contains the metadata about
    *      this mapping
    * @throws DeploymentException if the xml element is not semantically correct
    */
   public JDBCMappingMetaData(Element element) throws DeploymentException
   {
      javaType = MetaData.getUniqueChildContent(element, "java-type");
      jdbcType = getJdbcTypeFromName(MetaData.getUniqueChildContent(element, "jdbc-type"));
      sqlType = MetaData.getUniqueChildContent(element, "sql-type");
      paramSetter = MetaData.getOptionalChildContent(element, "param-setter");
      resultReader = MetaData.getOptionalChildContent(element, "result-reader");
   }

   /**
    * Gets the java type of this mapping. The java type is used to differentiate
    * this mapping from other mappings.
    *
    * @return the java type of this mapping
    */
   public String getJavaType()
   {
      return javaType;
   }

   /**
    * Gets the jdbc type of this mapping. The jdbc type is used to retrieve data
    * from a result set and to set parameters in a prepared statement.
    *
    * @return the jdbc type of this mapping
    */
   public int getJdbcType()
   {
      return jdbcType;
   }

   /**
    * Gets the sql type of this mapping. The sql type is the sql column data
    * type, and is used in CREATE TABLE statements.
    *
    * @return the sql type String of this mapping
    */
   public String getSqlType()
   {
      return sqlType;
   }

   public String getParamSetter()
   {
      return paramSetter;
   }

   public String getResultReader()
   {
      return resultReader;
   }
}
