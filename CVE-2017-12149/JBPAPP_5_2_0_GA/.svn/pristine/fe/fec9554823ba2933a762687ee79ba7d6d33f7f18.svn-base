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
package org.jboss.test.cluster.web.persistent;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.catalina.core.StandardContext;
import org.jboss.metadata.web.jboss.ReplicationGranularity;
import org.jboss.metadata.web.jboss.ReplicationTrigger;
import org.jboss.metadata.web.jboss.SnapshotMode;
import org.jboss.test.cluster.testutil.DBSetupDelegate;
import org.jboss.test.cluster.web.mocks.MockEngine;
import org.jboss.test.cluster.web.mocks.MockHost;
import org.jboss.web.tomcat.service.session.persistent.AbstractPersistentManager;
import org.jboss.web.tomcat.service.session.persistent.DataSourcePersistentManager;
import org.jboss.web.tomcat.service.session.persistent.RDBMSStoreBase;

/**
 * Utilities for session testing.
 * 
 * @author <a href="mailto://brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 81705 $
 */
public class PersistentSessionTestUtil
{  
//   private static final String[] STRING_ONLY_TYPES = { String.class.getName() };
   private static DataSource datasource;
   
   public synchronized static DataSource getDataSource()
   {
      if (datasource == null)
      {
         try
         {
            Driver driver = org.hsqldb.jdbcDriver.class.newInstance();
            String host = System.getProperty(DBSetupDelegate.DBADDRESS_PROPERTY, DBSetupDelegate.DEFAULT_ADDRESS);
            String jdbcURL = "jdbc:hsqldb:hsql://" + host + ":" + DBSetupDelegate.DEFAULT_PORT;
            datasource = new MockDataSource(driver, jdbcURL, "sa", null);
         }
         catch (InstantiationException e)
         {
            throw new RuntimeException("Failed to create DataSource", e);
         }
         catch (IllegalAccessException e)
         {
            throw new RuntimeException("Failed to create DataSource", e);
         }
      }
      return datasource;
   }
   
   public static DataSourcePersistentManager createManager(String warName, int maxInactiveInterval, 
                                                 String jvmRoute)
   {      
      DataSourcePersistentManager mgr = new DataSourcePersistentManager(getDataSource());
      mgr.setSnapshotMode(SnapshotMode.INSTANT);
      
      MockEngine engine = new MockEngine();
      engine.setJvmRoute(jvmRoute);
      MockHost host = new MockHost();
      engine.addChild(host);
      host.setName("localhost");
      StandardContext container = new StandardContext();
      container.setName(warName);
      host.addChild(container);
      container.setManager(mgr);
      
      // Do this after assigning the manager to the container, or else
      // the container's setting will override ours
      // Can't just set the container as their config is per minute not per second
      mgr.setMaxInactiveInterval(maxInactiveInterval);
   
      return mgr;      
   }
   
   public static void configureManager(AbstractPersistentManager mgr, int maxSessions)
   {
      configureManager(mgr,
                       ReplicationGranularity.SESSION,
                       ReplicationTrigger.SET_AND_NON_PRIMITIVE_GET,
                       maxSessions, false, -1, -1, false, 0);
   } 
   
   public static void configureManager(AbstractPersistentManager mgr, int maxSessions, boolean passivation,
         int maxIdle, int minIdle)
   {
      configureManager(mgr,
            ReplicationGranularity.SESSION,
                               ReplicationTrigger.SET_AND_NON_PRIMITIVE_GET,
                               maxSessions, passivation, maxIdle, minIdle, false, 60);
   } 
   
   public static void configureManager(AbstractPersistentManager mgr, ReplicationGranularity granularity,
                                                    ReplicationTrigger trigger,boolean batchMode,
                                                    int maxUnreplicated)
   {
      configureManager(mgr, granularity, trigger, -1, false, 
                       -1, -1, batchMode, maxUnreplicated);
   } 
   
   public static void configureManager(AbstractPersistentManager mgr, 
                                       ReplicationGranularity granularity,
                                       ReplicationTrigger trigger,
                                       int maxSessions, boolean passivation,
                                       int maxIdle, int minIdle,
                                       boolean batchMode,
                                       int maxUnreplicated)
   {
      mgr.setMaxActiveAllowed(maxSessions);
      mgr.setUseSessionPassivation(passivation);
      mgr.setPassivationMaxIdleTime(maxIdle);
      mgr.setPassivationMinIdleTime(minIdle);
      mgr.setReplicationGranularity(granularity);
      mgr.setReplicationTrigger(trigger);
      mgr.setReplicationFieldBatchMode(batchMode);
      mgr.setMaxUnreplicatedInterval(maxUnreplicated);
   }
   
   public static Integer getSessionVersion(DataSource ds, String realId, String appName) throws Exception
   {
      String versionSql = "SELECT " + RDBMSStoreBase.DEFAULT_VERSION_COL + 
            " FROM " + RDBMSStoreBase.DEFAULT_TABLE + 
            " WHERE " + RDBMSStoreBase.DEFAULT_ID_COL + " = ? AND " + RDBMSStoreBase.DEFAULT_APP_COL + " = ?";
      PreparedStatement stmt = null;
      ResultSet rs = null;
      Connection conn = ds.getConnection();
      try
      {
         conn.setAutoCommit(true);
         stmt = conn.prepareStatement(versionSql);
         stmt.setString(1, realId);
         stmt.setString(2, appName);
         rs = stmt.executeQuery();
         if (rs.next())
         {
            return Integer.valueOf(rs.getInt(1));
         }
         
         return null;
      }
      finally
      {
         if (rs != null)
         {
            try
            {
               rs.close();
            }
            catch (Exception e)
            {
            }
         }
         if (stmt != null)
         {
            try
            {
               stmt.close();
            }
            catch (Exception e)
            {
            }
         }
         conn.close();
      }
      
   }
   
   @SuppressWarnings("unchecked")
   public static Set<String> getSessionIds(DataSource ds, String warName) throws Exception
   {
      String keysSql = "SELECT " + RDBMSStoreBase.DEFAULT_ID_COL + " FROM " + 
      RDBMSStoreBase.DEFAULT_TABLE + " WHERE " + RDBMSStoreBase.DEFAULT_APP_COL + " = ?";
      PreparedStatement stmt = null;
      ResultSet rs = null;
      Connection conn = ds.getConnection();
      try
      {
         conn.setAutoCommit(true);
         stmt = conn.prepareStatement(keysSql);
         stmt.setString(1, warName);
         rs = stmt.executeQuery();
         Set<String> result = new HashSet<String>();
         while (rs.next())
         {
            result.add((String) rs.getString(1));
         }
         
         return result;
      }
      finally
      {
         if (rs != null)
         {
            try
            {
               rs.close();
            }
            catch (Exception e)
            {
            }
         }
         if (stmt != null)
         {
            try
            {
               stmt.close();
            }
            catch (Exception e)
            {
            }
         }
         conn.close();
      }
   }

   private PersistentSessionTestUtil() {}
}
