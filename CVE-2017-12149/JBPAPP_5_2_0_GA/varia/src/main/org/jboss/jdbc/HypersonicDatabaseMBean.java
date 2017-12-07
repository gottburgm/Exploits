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
package org.jboss.jdbc;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;

/**
 * MBean interface.
 * 
 * In all cases we run Hypersonic in the same VM with JBoss.
 * A few notes on Hypersonic running modes:
 * 
 * remote (server) mode
 *    hsqldb will listen for connections from local/remote clients
 * 
 * in-process (standalone) mode
 *    hsqldb can only be contacted from in-vm clients
 * 
 * memory-only mode
 *    hsqldb will only keep tables in memory, no persistence of data
 * 
 * @version $Revision: 81038 $
 */
public interface HypersonicDatabaseMBean extends ServiceMBean
{
   /** The default ObjectName */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss:service=Hypersonic");

   // Attributes ----------------------------------------------------

   /** The silent flag, default is 'true' */
   boolean getSilent();
   void setSilent(boolean silent);

   /** The trace flag, default is 'false' */
   boolean getTrace();
   void setTrace(boolean trace);
   
   /** The database name, default is 'default' */
   String getDatabase();   
   void setDatabase(String name);

   /** The listening port when in remove server mode, default is '1701' */
   int getPort();
   void setPort(int port);

   /** The binding address, default is '0.0.0.0' */
   String getBindAddress();
   void setBindAddress(String address);
   
   /** Whether remote server mode hypersonic should avoid calling System.exit() on shutdown, default is 'true'
       By far, the worse mbean attribute name */
   boolean getNo_system_exit();
   void setNo_system_exit(boolean no_system_exit);

   /** Whether DB is persisted, default is 'true'. A false value will activate memory only mode. */
   boolean getPersist();
   void setPersist(boolean persist);

   /** Whether DB is in in-process mode or remote server mode, default is 'false' */
   boolean isInProcessMode();
   void setInProcessMode(boolean b);
   
   /** The default user to use when connecting to the DB, default is "sa" */
   String getUser();   
   void setUser(String user);
   
   /** The default password to use when connecting to the DB, default is "" */
   String getPassword();
   void setPassword(String password);
   
   /** The shutdown command to use when stopping the DB */
   String getShutdownCommand();   
   void setShutdownCommand(String string);

   /** The database manager (UI) class, default is 'org.hsqldb.util.DatabaseManagerSwing' */
   String getDatabaseManagerClass();
   void setDatabaseManagerClass(String databaseManagerClass);
   
   /** The full database path */
   String getDatabasePath();

   // Operations ----------------------------------------------------
   
   /**
    * Start DatabaseManager accessible from the management console.
    */
   void startDatabaseManager();

}
