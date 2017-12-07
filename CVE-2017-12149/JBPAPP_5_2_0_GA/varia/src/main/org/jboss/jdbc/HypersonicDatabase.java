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

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;

/**
 * Integration with <a href="http://sourceforge.net/projects/hsqldb">HSQLDB</a>
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 * @author <a href="mailto:pf@iprobot.com">Peter Fagerlund</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:vesco.claudio@previnet.it">Claudio Vesco</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public class HypersonicDatabase extends ServiceMBeanSupport
   implements HypersonicDatabaseMBean
{
   /** Default password: <code>empty string</code>. */
   private static final String DEFAULT_PASSWORD = "";
   
   /** Default user: <code>sa</code>. */
   private static final String DEFAULT_USER = "sa";
   
   /** JDBC Driver class: <code>org.hsqldb.jdbcDriver</code>. */   
   private static final String JDBC_DRIVER_CLASS = "org.hsqldb.jdbcDriver";
   
   /** JDBC URL common prefix: <code>jdbc:hsqldb:</code>. */
   private static final String JDBC_URL_PREFIX = "jdbc:hsqldb:";
   
   /** Default shutdown command for remote hypersonic: <code>SHUTDOWN COMPACT</code>. */
   private static final String DEFAULT_REMOTE_SHUTDOWN_COMMAND = "SHUTDOWN COMPACT";
   
   /** Default shutdown command for in process persist hypersonic: <code>SHUTDOWN COMPACT</code>. */
   private static final String DEFAULT_IN_PROCESS_SHUTDOWN_COMMAND = "SHUTDOWN COMPACT";
   
   /** Default shutdown command for in process only memory hypersonic: <code>SHUTDOWN IMMEDIATELY</code>. */
   private static final String DEFAULT_IN_MEMORY_SHUTDOWN_COMMAND = "SHUTDOWN IMMEDIATELY";
   
   /** Default data subdir: <code>hypersonic</code>. */
   private static final String HYPERSONIC_DATA_DIR = "hypersonic";
   
   /** Default port for remote hypersonic: <code>1701</code>. */
   private static final int DEFAULT_PORT = 1701;
   
   /** Default address for remote hypersonic: <code>0.0.0.0</code>. */
   private static final String DEFAULT_ADDRESS = "0.0.0.0";
   
   /** Default database name: <code>default</code>. */
   private static final String DEFAULT_DATABASE_NAME = "default";
   
   /** Database name for memory-only hypersonic: <code>.</code>. */
   private static final String IN_MEMORY_DATABASE = ".";
   
   /** Default database manager (UI) class: <code>org.hsqldb.util.DatabaseManagerSwing</code>. */
   private static final String DEFAULT_DATABASE_MANAGER_CLASS = "org.hsqldb.util.DatabaseManagerSwing";
   
   /** Default server class for remote hypersonic: <code>org.hsqldb.Server</code>. */
   private static final String DEFAULT_SERVER_CLASS = "org.hsqldb.Server";

   // Private Data --------------------------------------------------
   
   /** Full path to db/hypersonic. */
   private File dbPath;

   /** Database name. */
   private String name = DEFAULT_DATABASE_NAME;

   /** Default port. */
   private int port = DEFAULT_PORT;

   /** Default address. */
   private String address = DEFAULT_ADDRESS;
   
   /** Default silent. */
   private boolean silent = true;

   /** Default trace. */
   private boolean trace = false;

   /** Default no_system_exit, new embedded support in 1.7 */
   private boolean no_system_exit = true;
   
   /** Default persisted DB */
   private boolean persist = true;

   /** Shutdown command. */
   private String shutdownCommand;
   
   /** In process/remote mode. */
   private boolean inProcessMode = false;
   
   /** Database user. */
   private String user = DEFAULT_USER;
   
   /** Database password. */
   private String password = DEFAULT_PASSWORD;
   
   /** Database manager (UI) class. */
   private String databaseManagerClass = DEFAULT_DATABASE_MANAGER_CLASS;
   
   /** Server class for remote hypersonic. */
   private String serverClass = DEFAULT_SERVER_CLASS;
   
   /** Server thread for remote hypersonic. */
   private Thread serverThread;
   
   /** Hold a connection for in process hypersonic. */
   private Connection connection;

   private String dbDataDir;

   // Constructors --------------------------------------------------
   
   /**
    * Costructor, empty.
    */
   public HypersonicDatabase()
   {
      // empty
   }

   // Attributes ----------------------------------------------------

   public String getDbDataDir()
   {
      return dbDataDir;
   }

   public void setDbDataDir(String dbDataDir)
   {
      this.dbDataDir = dbDataDir;
   }

   /**
    * Set the database name.
    * 
    * @jmx.managed-attribute
    */
   public void setDatabase(String name)
   {
      if (name == null)
      {
         name = DEFAULT_DATABASE_NAME;
      }
      this.name = name;
   }

   /**
    * Get the database name.
    * 
    * @jmx.managed-attribute
    */
   public String getDatabase()
   {
      return name;
   }

   /**
    * Set the port for remote hypersonic.
    * 
    * @jmx.managed-attribute
    */
   public void setPort(final int port)
   {
      this.port = port;
   }

   /**
    * Get the port for remote hypersonic.
    * 
    * @jmx.managed-attribute
    */
   public int getPort()
   {
      return port;
   }

   /**
    * Set the bind address for remote hypersonic.
    * 
    * @jmx.managed-attribute
    */
   public void setBindAddress(final String address)
   {
      this.address = address;
   }
   
   /**
    * Get the bind address for remote hypersonic.
    * 
    * @jmx.managed-attribute
    */
   public String getBindAddress()
   {
      return address;
   }
   
   /**
    * Set silent flag.
    * 
    * @jmx.managed-attribute
    */
   public void setSilent(final boolean silent)
   {
      this.silent = silent;
   }

   /**
    * Get silent flag.
    * 
    * @jmx.managed-attribute
    */
   public boolean getSilent()
   {
      return silent;
   }

   /**
    * Set trace flag.
    * 
    * @jmx.managed-attribute
    */
   public void setTrace(final boolean trace)
   {
      this.trace = trace;
   }

   /**
    * Get trace flag.
    * 
    * @jmx.managed-attribute
    */
   public boolean getTrace()
   {
      return trace;
   }

   /**
    * If <b>true</b> the server thread for remote hypersonic does no call <code>System.exit()</code>.
    * 
    * @jmx.managed-attribute
    */
   public void setNo_system_exit(final boolean no_system_exit)
   {
      this.no_system_exit = no_system_exit;
   }

   /**
    * Get the <code>no_system_exit</code> flag.
    * 
    * @jmx.managed-attribute
    */
   public boolean getNo_system_exit()
   {
      return no_system_exit;
   }

   /**
    * Set persist flag.
    * 
    * @deprecated use {@link #setInProcessMode(boolean)(boolean) inProcessMode}.
    * 
    * @jmx.managed-attribute
    */
   public void setPersist(final boolean persist)
   {
      this.persist = persist;
   }

   /**
    * Get persist flag.
    * 
    * @deprecated use {@link #setInProcessMode(boolean)(boolean) inProcessMode}.
    * 
    * @jmx.managed-attribute
    */
   public boolean getPersist()
   {
      return persist;
   }

   /**
    * Get the full database path.
    * 
    * @jmx.managed-attribute
    */
   public String getDatabasePath()
   {
      if (dbPath != null)
      {
         return dbPath.toString();
      }
      else
      {
         return null;
      }
   }

   /**
    * @return the <code>inProcessMode</code> flag.
    * 
    * @jmx.managed-attribute 
    */
   public boolean isInProcessMode()
   {
      return inProcessMode;
   }

   /**
    * @return the shutdown command.
    * 
    * @jmx.managed-attribute
    */
   public String getShutdownCommand()
   {
      return shutdownCommand;
   }

   /**
    * If <b>true</b> the hypersonic is in process mode otherwise hypersonic is in server or remote mode.
    * 
    * @param b in process mode or remote mode.
    * 
    * @jmx.managed-attribute
    */
   public void setInProcessMode(boolean b)
   {
      inProcessMode = b;
   }

   /**
    * @param string the shutdown command
    * 
    * @jmx.managed-attribute 
    */
   public void setShutdownCommand(String string)
   {
      shutdownCommand = string;
   }

   /**
    * @return the password
    * 
    * @jmx.managed-attribute 
    */
   public String getPassword()
   {
      return password;
   }

   /**
    * @return the user
    * 
    * @jmx.managed-attribute 
    */
   public String getUser()
   {
      return user;
   }

   /**
    * @param password
    * 
    * @jmx.managed-attribute 
    */
   public void setPassword(String password)
   {
      if (password == null)
      {
         password = DEFAULT_PASSWORD;
      }
      this.password = password;
   }

   /**
    * @param user
    * 
    * @jmx.managed-attribute 
    */
   public void setUser(String user)
   {
      if (user == null)
      {
         user = DEFAULT_USER;
      }
      this.user = user;
   }

   /**
    * @return
    * 
    * @jmx.managed-attribute 
    */
   public String getDatabaseManagerClass()
   {
      return databaseManagerClass;
   }

   /**
    * Set the database manager (UI) class.
    * 
    * @param databaseManagerClass
    * 
    * @jmx.managed-attribute 
    */
   public void setDatabaseManagerClass(String databaseManagerClass)
   {
      if (databaseManagerClass == null)
      {
         databaseManagerClass = DEFAULT_DATABASE_MANAGER_CLASS;
      }
      this.databaseManagerClass = databaseManagerClass;
   }

   /**
    * @return server class for remote hypersonic.
    */
   public String getServerClass()
   {
      return serverClass;
   }

   /**
    * Set the server class for remote hypersonic.
    * 
    * @param serverClass
    */
   public void setServerClass(String serverClass)
   {
      if (serverClass == null)
      {
         serverClass = DEFAULT_SERVER_CLASS;
      }
      this.serverClass = serverClass;
   }

   // Operations ----------------------------------------------------
   
   /** 
    * Start of DatabaseManager accesible from the management console.
    *
    * @jmx.managed-operation
    */
   public void startDatabaseManager()
   {
      // Start DBManager in new thread
      new Thread()
      {
         public void run()
         {
            try
            {
               // If bind address is the default 0.0.0.0, use localhost
               String connectHost = DEFAULT_ADDRESS.equals(address) ? "localhost" : address;               
               String driver = JDBC_DRIVER_CLASS;
               String[] args;
               if (!inProcessMode)
               {
                  args =
                     new String[] {
                        "-noexit",
                        "-driver", driver,
                        "-url", JDBC_URL_PREFIX + "hsql://" + connectHost + ":" + port,
                        "-user", user,
                        "-password", password,
                        "-dir", getDatabasePath()
                        };
               }
               else if (IN_MEMORY_DATABASE.equals(name))
               {
                  args =
                     new String[] {
                        "-noexit",
                        "-driver", driver,
                        "-url", JDBC_URL_PREFIX + IN_MEMORY_DATABASE,
                        "-user", user,
                        "-password", password
                        };
               }
               else
               {
                  args =
                     new String[] {
                        "-noexit",
                        "-driver", driver,
                        "-url", JDBC_URL_PREFIX + getDatabasePath(),
                        "-user", user,
                        "-password", password,
                        "-dir", getDatabasePath()
                        };
               }

               // load (and link) the class only if needed
               ClassLoader cl = Thread.currentThread().getContextClassLoader();
               Class clazz = Class.forName(databaseManagerClass, true, cl);
               Method main = clazz.getMethod("main", new Class[] { args.getClass() });
               main.invoke(null, new Object [] { args });
            }
            catch (HeadlessException e)
            {
               log.error("Failed to start database manager because this is an headless configuration (no display, mouse or keyword)");
            }
            catch (Exception e)
            {
               log.error("Failed to start database manager", e);
            }
         }
      }
      .start();
   }

   // Lifecycle -----------------------------------------------------
   
   /**
    * Start the database
    */
   protected void startService() throws Exception
   {
      // check persist for old compatibility
      if (!persist)
      {
         inProcessMode = true;
         name = IN_MEMORY_DATABASE;
      }
      
      // which database?
      if (!inProcessMode)
      {
         startRemoteDatabase();
      }
      else if (IN_MEMORY_DATABASE.equals(name))
      {
         startInMemoryDatabase();
      }
      else
      {
         startStandaloneDatabase();
      }
   }

   /**
    * We now close the connection clean by calling the
    * serverSocket throught jdbc. The MBeanServer calls this 
    * method at closing time.
    */
   protected void stopService() throws Exception
   {
      // which database?
      if (!inProcessMode)
      {
         stopRemoteDatabase();
      }
      else if (IN_MEMORY_DATABASE.equals(name))
      {
         stopInMemoryDatabase();
      }
      else
      {
         stopStandaloneDatabase();
      }
   }
   
   // Private -------------------------------------------------------
   
   /**
    * Start the standalone (in process) database.
    */
   private void startStandaloneDatabase() throws Exception
   {
      // Get the server data directory
      File dataDir = null;

      if (dbDataDir == null) dataDir = ServerConfigLocator.locate().getServerDataDir();
      else dataDir = new File(dbDataDir);

      // Get DB directory
      File hypersoniDir = new File(dataDir, HYPERSONIC_DATA_DIR);

      if (!hypersoniDir.exists())
      {
         hypersoniDir.mkdirs();
      }

      if (!hypersoniDir.isDirectory())
      {
         throw new IOException("Failed to create directory: " + hypersoniDir);
      }
      
      dbPath = new File(hypersoniDir, name);

      String dbURL = JDBC_URL_PREFIX + getDatabasePath();

      // hold a connection so hypersonic does not close the database
      connection = getConnection(dbURL);
   }

   /**
    * Start the only in memory database.
    */
   private void startInMemoryDatabase() throws Exception
   {
      String dbURL = JDBC_URL_PREFIX + IN_MEMORY_DATABASE;

      // hold a connection so hypersonic does not close the database
      connection = getConnection(dbURL);
   }

   /**
    * Start the remote database.
    */
   private void startRemoteDatabase() throws Exception
   {
      // Get the server data directory
      File dataDir = null;
      if (dbDataDir == null) dataDir = ServerConfigLocator.locate().getServerDataDir();
      else dataDir = new File(dbDataDir);

      // Get DB directory
      File hypersoniDir = new File(dataDir, HYPERSONIC_DATA_DIR);

      if (!hypersoniDir.exists())
      {
         hypersoniDir.mkdirs();
      }

      if (!hypersoniDir.isDirectory())
      {
         throw new IOException("Failed to create directory: " + hypersoniDir);
      }
      
      dbPath = new File(hypersoniDir, name);

      // Start DB in new thread, or else it will block us
      serverThread = new Thread("hypersonic-" + name)
      {
         public void run()
         {
            try
            {
               // Create startup arguments
               String[] args =
                  new String[] {
                     "-database", dbPath.toString(),
                     "-port", String.valueOf(port),
                     "-address", address,
                     "-silent", String.valueOf(silent),
                     "-trace", String.valueOf(trace),
                     "-no_system_exit", String.valueOf(no_system_exit),
                     };

               // Start server
               ClassLoader cl = Thread.currentThread().getContextClassLoader();
               Class clazz = Class.forName(serverClass, true, cl);
               Method main = clazz.getMethod("main", new Class[] { args.getClass() });
               main.invoke(null, new Object[] { args } );
            }
            catch (Exception e)
            {
               log.error("Failed to start database", e);
            }
         }
      };
      serverThread.start();
   }

   /**
    * Stop the standalone (in process) database.
    */
   private void stopStandaloneDatabase() throws Exception
   {
      String dbURL = JDBC_URL_PREFIX + getDatabasePath();

      Connection connection = getConnection(dbURL);
      Statement statement = connection.createStatement();
      
      String shutdownCommand = this.shutdownCommand;
      if (shutdownCommand == null)
      {
         shutdownCommand = DEFAULT_IN_PROCESS_SHUTDOWN_COMMAND;
      }
      
      statement.executeQuery(shutdownCommand);
      this.connection = null;
      log.info("Database standalone closed clean");
   }

   /**
    * Stop the in memory database.
    */
   private void stopInMemoryDatabase() throws Exception
   {
      String dbURL = JDBC_URL_PREFIX + IN_MEMORY_DATABASE;

      Connection connection = getConnection(dbURL);
      Statement statement = connection.createStatement();
      
      String shutdownCommand = this.shutdownCommand;
      if (shutdownCommand == null)
      {
         shutdownCommand = DEFAULT_IN_MEMORY_SHUTDOWN_COMMAND;
      }
      
      statement.executeQuery(shutdownCommand);
      this.connection = null;
      log.info("Database in memory closed clean");
   }

   /**
    * Stop the remote database.
    */
   private void stopRemoteDatabase() throws Exception
   {
      // If bind address is the default 0.0.0.0, use localhost
      String connectHost = DEFAULT_ADDRESS.equals(address) ? "localhost" : address;
      String dbURL = JDBC_URL_PREFIX + "hsql://" + connectHost + ":" + port;

      Connection connection = getConnection(dbURL);
      Statement statement = connection.createStatement();
      
      String shutdownCommand = this.shutdownCommand;
      if (shutdownCommand == null)
      {
         shutdownCommand = DEFAULT_REMOTE_SHUTDOWN_COMMAND;
      }
      
      statement.executeQuery(shutdownCommand);
      // TODO: join thread?
      serverThread = null;
      this.connection = null;
      log.info("Database remote closed clean");
   }
   
   /**
    * Get the connection.
    * 
    * @param dbURL jdbc url.
    * @return the connection, allocate one if needed.
    * @throws Exception
    */
   private synchronized Connection getConnection(String dbURL) throws Exception
   {
      if (connection == null)
      {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         Class.forName(JDBC_DRIVER_CLASS, true, cl).newInstance();
         connection = DriverManager.getConnection(dbURL, user, password);
      }
      return connection;
   }

}
