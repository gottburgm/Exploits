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
package org.jboss.varia.scheduler;

/**
 * MBean interface.
 */
public interface DBScheduleProviderMBean extends org.jboss.varia.scheduler.AbstractScheduleProviderMBean {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss:service=DBScheduleProvider");

  java.lang.String getDataSourceName() ;

   /**
    * Sets the JNDI name of the Data Source. You have to ensure that the DataSource is available when this service is started.
    */
  void setDataSourceName(java.lang.String pDataSourceName) ;

  java.lang.String getSQLStatement() ;

   /**
    * Sets the SQL Statement used to retrieve the data from the Database
    */
  void setSQLStatement(java.lang.String pSQLStatement) ;

   /**
    * Add the Schedule to the Schedule Manager
    */
  void startProviding() throws java.lang.Exception;

   /**
    * Stops the Provider from providing causing the provider to remove the Schedule
    */
  void stopProviding() ;

}
