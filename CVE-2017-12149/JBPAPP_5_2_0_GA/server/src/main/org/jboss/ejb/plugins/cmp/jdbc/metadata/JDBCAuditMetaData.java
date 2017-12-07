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

import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.MetaData;
import org.jboss.logging.Logger;
import org.w3c.dom.Element;

/**
 * Audit field meta data
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 81030 $
 */
public final class JDBCAuditMetaData
{
   // Constants ---------------------------------------

   // Attributes --------------------------------------

   /** The created by principal field */
   final private JDBCCMPFieldMetaData createdPrincipalField;

   /** The created by time field */
   final private JDBCCMPFieldMetaData createdTimeField;

   /** The last update by principal field */
   final private JDBCCMPFieldMetaData updatedPrincipalField;

   /** The last update time time field */
   final private JDBCCMPFieldMetaData updatedTimeField;

   /** logger */
   final private Logger log;

   // Constructors ------------------------------------

   /**
    * Constructs audit metadata reading
    * audit XML element
    */
   public JDBCAuditMetaData(JDBCEntityMetaData entityMetaData, Element element)
      throws DeploymentException
   {
      log = Logger.getLogger(entityMetaData.getName());

      Element workElement;

      if ((workElement = MetaData.getOptionalChild(element, "created-by")) != null)
      {
         createdPrincipalField = constructAuditField(entityMetaData, workElement, "audit_created_by");

         log.debug("created-by: " + createdPrincipalField);
      }
      else
         createdPrincipalField = null;

      if ((workElement = MetaData.getOptionalChild(element, "created-time")) != null)
      {
         createdTimeField = constructAuditField(entityMetaData, workElement, "audit_created_time");

         log.debug("created-time: " + createdTimeField);
      }
      else
         createdTimeField = null;

      if ((workElement = MetaData.getOptionalChild(element, "updated-by")) != null)
      {
         updatedPrincipalField = constructAuditField(entityMetaData, workElement, "audit_updated_by");

         log.debug("updated-by: " + updatedPrincipalField);
      }
      else
         updatedPrincipalField = null;

      if ((workElement = MetaData.getOptionalChild(element, "updated-time")) != null)
      {
         updatedTimeField = constructAuditField(entityMetaData, workElement, "audit_updated_time");

         log.debug("updated-time: " + updatedTimeField);
      }
      else
         updatedTimeField = null;
   }

   // Public ------------------------------------------

   public JDBCCMPFieldMetaData getCreatedPrincipalField()
   {
      return createdPrincipalField;
   }

   public JDBCCMPFieldMetaData getCreatedTimeField()
   {
      return createdTimeField;
   }

   public JDBCCMPFieldMetaData getUpdatedPrincipalField()
   {
      return updatedPrincipalField;
   }

   public JDBCCMPFieldMetaData getUpdatedTimeField()
   {
      return updatedTimeField;
   }

   // Private -----------------------------------------

   /**
    * Constructs an audit locking field metadata from
    * XML element
    */
   private static JDBCCMPFieldMetaData constructAuditField(
      JDBCEntityMetaData entity,
      Element element,
      String defaultName)
      throws DeploymentException
   {
      // field name
      String fieldName = MetaData.getOptionalChildContent(element, "field-name");
      if (fieldName == null || fieldName.trim().length() < 1)
         fieldName = defaultName;

      // column name
      String columnName = MetaData.getOptionalChildContent(element, "column-name");
      if (columnName == null || columnName.trim().length() < 1)
         columnName = defaultName;

      // field type
      Class fieldType;
      String fieldTypeStr = MetaData.getOptionalChildContent(element, "field-type");
      if (fieldTypeStr != null)
      {
         try
         {
            fieldType = GetTCLAction.getContextClassLoader().loadClass(fieldTypeStr);
         }
         catch(ClassNotFoundException e)
         {
            throw new DeploymentException(
               "Could not load field type for audit field "
               + fieldName + ": " + fieldTypeStr);
         }
      }
      else
      {
         if (defaultName.endsWith("by"))
            fieldType = String.class;
         else
            fieldType = java.util.Date.class;
      }

      // JDBC/SQL Type
      int jdbcType;
      String sqlType;
      String jdbcTypeName = MetaData.getOptionalChildContent(element, "jdbc-type");
      if (jdbcTypeName != null)
      {
         jdbcType = JDBCMappingMetaData.getJdbcTypeFromName(jdbcTypeName);
         sqlType = MetaData.getUniqueChildContent(element, "sql-type");
      }
      else
      {
         jdbcType = Integer.MIN_VALUE;
         sqlType = null;
      }

      // Is the field exposed?
      JDBCCMPFieldMetaData result = entity.getCMPFieldByName(fieldName);

      if (result == null)
         result = new JDBCCMPFieldMetaData(entity, fieldName, fieldType, columnName, jdbcType, sqlType);

      return result;
   }
}
