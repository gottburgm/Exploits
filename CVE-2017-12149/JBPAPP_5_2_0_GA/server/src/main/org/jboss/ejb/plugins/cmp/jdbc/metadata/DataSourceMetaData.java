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

import org.jboss.system.ServiceMBeanSupport;

import javax.management.ObjectName;

/**
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81030 $</tt>
 */
public class DataSourceMetaData
   extends ServiceMBeanSupport
   implements DataSourceMetaDataMBean
{
   private ObjectName metadataLibrary;
   private String typeMapping;

   /**
    * @jmx.managed-attribute
    */
   public String getTypeMapping()
   {
      return typeMapping;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setTypeMapping(String typeMapping)
   {
      this.typeMapping = typeMapping;
   }

   /**
    * @jmx.managed-attribute
    */
   public ObjectName getMetadataLibrary()
   {
      return metadataLibrary;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setMetadataLibrary(ObjectName metadataLibrary)
   {
      this.metadataLibrary = metadataLibrary;
   }

   /**
    * @jmx.managed-attribute
    */
   public JDBCTypeMappingMetaData getTypeMappingMetaData() throws Exception
   {
      return (JDBCTypeMappingMetaData)server.invoke(
         metadataLibrary,
         "findTypeMappingMetaData",
         new Object[]{typeMapping},
         new String[]{String.class.getName()}
      );
   }
}
