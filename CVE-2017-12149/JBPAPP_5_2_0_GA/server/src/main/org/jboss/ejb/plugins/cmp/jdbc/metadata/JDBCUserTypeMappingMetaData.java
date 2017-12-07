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
import org.w3c.dom.Element;


/**
 * Immutable class, instances of which represent user type mappings.
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 */
public class JDBCUserTypeMappingMetaData
{
   /** Fully qualified Java type name being mapped */
   private final String javaType;
   /** Fully qualified Java type name <code>javaType</code> is mapped to */
   private final String mappedType;
   /** Fully qualified Java type name of Mapper implementation */
   private final String mapper;
   /** Check a field of this type for dirty state after the getter: null, true or false (can be overriden on the field level) */
   private final byte checkDirtyAfterGet;
   /** CMP field state factory class that should be used for fields of this type unless overriden on the field level */
   private final String stateFactory;

   public JDBCUserTypeMappingMetaData(Element userMappingEl)
      throws DeploymentException
   {
      javaType = MetaData.getUniqueChildContent(userMappingEl, "java-type");
      mappedType = MetaData.getUniqueChildContent(userMappingEl, "mapped-type");
      mapper = MetaData.getUniqueChildContent(userMappingEl, "mapper");
      checkDirtyAfterGet = JDBCCMPFieldMetaData.readCheckDirtyAfterGet(userMappingEl);
      stateFactory = MetaData.getOptionalChildContent(userMappingEl, "state-factory");
   }

   public String getJavaType()
   {
      return javaType;
   }

   public String getMappedType()
   {
      return mappedType;
   }

   public String getMapper()
   {
      return mapper;
   }

   public byte checkDirtyAfterGet()
   {
      return checkDirtyAfterGet;
   }

   public String getStateFactory()
   {
      return stateFactory;
   }
}
