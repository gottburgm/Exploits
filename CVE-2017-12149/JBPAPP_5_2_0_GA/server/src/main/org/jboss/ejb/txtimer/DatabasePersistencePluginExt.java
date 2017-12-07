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

// $Id: DatabasePersistencePluginExt.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

import java.sql.SQLException;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * An extension of DatabasePersistencePlugin that allows setting of the
 * timers table name during initialization.
 *
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 85945 $
 */
public interface DatabasePersistencePluginExt extends DatabasePersistencePlugin
{
   /**
    * Initialize the plugin and set also the timers tablename
    */
   void init(MBeanServer server, ObjectName dataSource, String tableName) throws SQLException;
}