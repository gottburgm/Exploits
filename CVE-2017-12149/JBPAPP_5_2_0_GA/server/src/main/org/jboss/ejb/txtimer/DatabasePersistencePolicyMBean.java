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
package org.jboss.ejb.txtimer;

import java.sql.SQLException;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;

/**
 * MBean interface.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 107116 $
 * @since 09-Sep-2004
 */
public interface DatabasePersistencePolicyMBean extends ServiceMBean, PersistencePolicyExt
{
   /** The default object name */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.ejb:service=EJBTimerService,persistencePolicy=database");

   // Attributes ----------------------------------------------------
   
   /** The used datasource */
   void setDataSource(ObjectName dataSource);   
   ObjectName getDataSource();

   /** The used database persistence plugin class */
   void setDatabasePersistencePlugin(String dbpPluginClass);   
   String getDatabasePersistencePlugin();
   
   /** The timers table name */
   void setTimersTable(String tableName);
   String getTimersTable();
   
   // Operations ----------------------------------------------------
   
   /**
    * Re-read the current persistent timers list,
    * clear the db of timers, and restore the timers.
    */
   void resetAndRestoreTimers() throws SQLException;

}
