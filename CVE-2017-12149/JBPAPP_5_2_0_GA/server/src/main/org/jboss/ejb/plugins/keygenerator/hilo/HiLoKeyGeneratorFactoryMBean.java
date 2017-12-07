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
package org.jboss.ejb.plugins.keygenerator.hilo;

/**
 * MBean interface.
 */
public interface HiLoKeyGeneratorFactoryMBean extends org.jboss.system.ServiceMBean {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss.system:service=KeyGeneratorFactory,type=HiLo");

  void setFactoryName(java.lang.String factoryName) ;

  java.lang.String getFactoryName() ;

  void setDataSource(javax.management.ObjectName dataSource) throws java.lang.Exception;

  javax.management.ObjectName getDataSource() ;

  java.lang.String getTableName() ;

  void setTableName(java.lang.String tableName) throws java.lang.Exception;

  java.lang.String getSequenceColumn() ;

  void setSequenceColumn(java.lang.String sequenceColumn) ;

  java.lang.String getSequenceName() ;

  void setSequenceName(java.lang.String sequenceName) ;

  java.lang.String getIdColumnName() ;

  void setIdColumnName(java.lang.String idColumnName) ;

  java.lang.String getCreateTableDdl() ;

  void setCreateTableDdl(java.lang.String createTableDdl) ;

  java.lang.String getSelectHiSql();

  void setSelectHiSql(String selectHiSql);

  long getBlockSize() ;

  void setBlockSize(long blockSize) ;

   boolean isCreateTable();

   void setCreateTable(boolean createTable);

   boolean isDropTable();

   void setDropTable(boolean dropTable);
}
