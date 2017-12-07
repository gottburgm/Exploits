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
package org.jboss.ejb.txtimer;

// $Id: DatabasePersistencePlugin.java 81030 2008-11-14 12:59:42Z dimitris@jboss.org $

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * An implementation of of this interface provides database specific JDBC access that is
 * not portable accros RDBMS systems.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 81030 $
 * @since 23-Sep-2004
 */
public interface DatabasePersistencePlugin
{
   /** Initialize the plugin */
   void init(MBeanServer server, ObjectName dataSource) throws SQLException;

   /** Create the timers table if it does not exist already */
   void createTableIfNotExists() throws SQLException;

   /** Insert a timer object */
   void insertTimer(String timerId, TimedObjectId timedObjectId, Date initialExpiration, long intervalDuration, Serializable info) throws SQLException;

   /** Select a list of currently persisted timer handles
    * @return List<TimerHandleImpl>
    */
   List selectTimers(ObjectName containerId) throws SQLException;

   /** Delete a timer. */
   void deleteTimer(String timerId, TimedObjectId timedObjectId) throws SQLException;

   /** Clear all persisted timers */
   void clearTimers() throws SQLException;

   /** Get the timer table name */
   String getTableName();

   /** Get the timer ID column name */
   String getColumnTimerID();

   /** Get the target ID column name */
   String getColumnTargetID();

   /** Get the initial date column name */
   String getColumnInitialDate();

   /** Get the timer interval column name */
   String getColumnTimerInterval();

   /** Get the instance PK column name */
   String getColumnInstancePK();

   /** Get the info column name */
   String getColumnInfo();
}

