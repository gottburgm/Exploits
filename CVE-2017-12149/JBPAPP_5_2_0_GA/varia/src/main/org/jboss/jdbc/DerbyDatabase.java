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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.system.ServiceMBeanSupport;

/**
 * Integration with <a href="http://incubator.apache.org/derby/index.html">Derby</a>.
 * 
 * <p>Starts Derby database in-VM.
 * 
 * @jmx.mbean name="jboss:service=Derby"
 *				  extends="org.jboss.system.ServiceMBean"
 * 
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version $Revision: 81038 $
 */
public class DerbyDatabase
   extends ServiceMBeanSupport
   implements DerbyDatabaseMBean, MBeanRegistration
{
   /**
    * Default password: <code>empty string</code>.
    */
   private static final String DEFAULT_PASSWORD = "";
   
   /**
    * Default user: <code>sa</code>.
    */
   private static final String DEFAULT_USER = "sa";
   
   /**
    * JDBC Driver class: <code>org.apache.derby.jdbc.EmbeddedDriver</code>.
    */   
   private static final String JDBC_DRIVER_CLASS = "org.apache.derby.jdbc.EmbeddedDriver";
   
   /**
    * JDBC URL common prefix: <code>jdbc:derby:</code>.
    */
   private static final String JDBC_URL_PREFIX = "jdbc:derby:";

   /**
    * Default data subdir: <code>derby</code>.
    */
   private static final String DERBY_DATA_DIR = "derby";
   
   /**
    * Default database name: <code>default</code>.
    */
   private static final String DEFAULT_DATABASE_NAME = "default";
   
   /**
    * Database name.
    */
   String name = DEFAULT_DATABASE_NAME;

   /**
    * Database user.
    */
   private String user = DEFAULT_USER;
   
   /**
    * Database password.
    */
   private String password = DEFAULT_PASSWORD;
   
   /**
    * Hold a connection for in process derby.
    */
   private Connection connection;

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

   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws MalformedObjectNameException
   {
      return name == null ? OBJECT_NAME : name;
   }

   protected void startService() throws Exception
   {
      String dbURL = JDBC_URL_PREFIX + System.getProperty("jboss.server.data.dir") +
         '/' + DerbyDatabase.DERBY_DATA_DIR +
         '/' + name + ";create=true";
      log.info("starting derby " + dbURL);

      // hold a connection so hypersonic does not close the database
      connection = getConnection(dbURL);
   }

   protected void stopService() throws Exception
   {
      try
      {
         getConnection("jdbc:derby:;shutdown=true");
         log.error("According to the docs, should have caught an exception!");
      }
      catch(SQLException e)
      {
         log.info("Derby shutdown successfully.", e);
      }

      connection = null;
   }

   /**
    * Get the connection.
    * 
    * @param dbURL jdbc url.
    * 
    * @return the connection, allocate one if needed.
    * 
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
