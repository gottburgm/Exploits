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

// $Id: GeneralPurposeDatabasePersistencePlugin.java 110101 2010-12-21 16:57:49Z bmaxwell $

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;
import org.jboss.ejb.plugins.cmp.jdbc.SQLUtil;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCFunctionMappingMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCTypeMappingMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCMappingMetaData;
import org.jboss.invocation.MarshalledValueInputStream;
import org.jboss.logging.Logger;
import org.jboss.mx.util.ObjectNameFactory;

/**
 * This DatabasePersistencePlugin uses getBytes/setBytes to persist the
 * serializable objects associated with the timer.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 110101 $
 * @since 23-Sep-2004
 */
public class GeneralPurposeDatabasePersistencePlugin implements UpdateableDatabasePersistencePlugin
{
   /** logging support */
   private static Logger log = Logger.getLogger(GeneralPurposeDatabasePersistencePlugin.class);

   /** The mbean server */
   protected MBeanServer server;
   
   /** The service attributes */
   protected ObjectName dataSourceName;

   /** The timers table name */
   protected String tableName;
   
   /** The data source the timers will be persisted to */
   protected DataSource ds;
   
   /** datasource meta data */
   protected ObjectName metaDataName;
   
   // default JDBC type code for binary data
   private int binarySqlType;

   /**
    * Initialize the plugin and set also the timers tablename
    */
   public void init(MBeanServer server, ObjectName dataSource, String tableName) throws SQLException
   {
      if (tableName == null)
         throw new IllegalArgumentException("Timers tableName is null");
      if (tableName.length() == 0)
         throw new IllegalArgumentException("Timers tableName is empty");

      this.tableName = tableName;
      init(server, dataSource);
   }
   
   /** Initialize the plugin */
   public void init(MBeanServer server, ObjectName dataSourceName) throws SQLException
   {
      this.server = server;
      this.dataSourceName = dataSourceName;

      // Get the DataSource from JNDI
      try
      {
         String dsJndiTx = (String)server.getAttribute(dataSourceName, "BindName");
         ds = (DataSource)new InitialContext().lookup(dsJndiTx);
      }
      catch (Exception e)
      {
         throw new SQLException("Failed to lookup data source: " + dataSourceName);
      }

      // Get the DataSource meta data
      String dsName = dataSourceName.getKeyProperty("name");
      metaDataName = ObjectNameFactory.create("jboss.jdbc:datasource=" + dsName + ",service=metadata");
      if (this.server.isRegistered(metaDataName) == false)
         throw new IllegalStateException("Cannot find datasource meta data: " + metaDataName);
   }

   /** Create the timer table if it does not exist already */
   public void createTableIfNotExists()
           throws SQLException
   {
      Connection con = null;
      Statement st = null;
      try
      {        
         JDBCTypeMappingMetaData typeMapping = (JDBCTypeMappingMetaData)server.getAttribute(metaDataName, "TypeMappingMetaData");
         if (typeMapping == null)
            throw new IllegalStateException("Cannot obtain type mapping from: " + metaDataName);

         JDBCMappingMetaData objectMetaData = typeMapping.getTypeMappingMetaData(Object.class);
         binarySqlType = objectMetaData.getJdbcType();
         
         if (!SQLUtil.tableExists(getTableName(), ds))
         {
            con = ds.getConnection();

            String dateType = typeMapping.getTypeMappingMetaData(Timestamp.class).getSqlType();
            String longType = typeMapping.getTypeMappingMetaData(Long.class).getSqlType();
            String objectType = objectMetaData.getSqlType();

            // The create table DDL
            StringBuffer createTableDDL = new StringBuffer("create table " + getTableName() + " (" +
                    " " + getColumnTimerID() + " varchar(80) not null," +
                    " " + getColumnTargetID() + " varchar(250) not null," +
                    " " + getColumnInitialDate() + " " + dateType + " not null," +
                    " " + getColumnNextDate() + " " + dateType + "," +
                    " " + getColumnTimerInterval() + " " + longType + "," +
                    " " + getColumnInstancePK() + " " + objectType + "," +
                    " " + getColumnInfo() + " " + objectType + ", ");

            // Add the primary key constraint using the pk-constraint-template
            JDBCFunctionMappingMetaData pkConstraint = typeMapping.getPkConstraintTemplate();
            String name = SQLUtil.unquote(getTableName(), ds) + "_PK";
            name = SQLUtil.fixConstraintName(name, ds); 
            String[] templateParams = new String[] {
                  name,
                  getColumnTimerID() + ", " + getColumnTargetID()
                  };
            pkConstraint.getFunctionSql(templateParams, createTableDDL);
            
            // Complete the statement
            createTableDDL.append(" )");

            log.debug("Executing DDL: " + createTableDDL);

            st = con.createStatement();
            st.executeUpdate(createTableDDL.toString());
         }
         else if (this.isNextDateColumnPresent() == false) // the timer table exists but the next timeout date column is absent, so create the column
         {
        	 // JBPAPP-4681 https://jira.jboss.org/browse/JBPAPP-4681 introduces a new column
        	 // in the table. Here, we make sure to create the column in already existing DBs
        	 
        	 con = ds.getConnection();

             String dateType = typeMapping.getTypeMappingMetaData(Timestamp.class).getSqlType();

             // The alter table DDL
             StringBuffer alterTableDDL = new StringBuffer("alter table " + getTableName() + " ADD " +
                     " " + getColumnNextDate() + " " + dateType);

				log.debug("Adding new column " + getColumnNextDate()
						+ " to table " + getTableName() + " - executing DDL: "
						+ alterTableDDL);

             st = con.createStatement();
             st.executeUpdate(alterTableDDL.toString());
        	 
         }
      }
      catch (SQLException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         log.error("Cannot create timer table", e);
      }
      finally
      {
         JDBCUtil.safeClose(st);
         JDBCUtil.safeClose(con);
      }
   }

   /** Insert a timer object */
   public void insertTimer(String timerId, TimedObjectId timedObjectId, Date initialExpiration, long intervalDuration, Serializable info)
           throws SQLException
   {
      Connection con = null;
      PreparedStatement st = null;
      try
      {
         con = ds.getConnection();

         String sql = "insert into " + getTableName() + " " +
                 "(" + getColumnTimerID() + "," + getColumnTargetID() + "," + getColumnInitialDate() + "," + getColumnTimerInterval() + "," + getColumnInstancePK() + "," + getColumnInfo() + ","  + getColumnNextDate() + ") " +
                 "values (?,?,?,?,?,?,?)";
         st = con.prepareStatement(sql);

         st.setString(1, timerId);
         st.setString(2, timedObjectId.toString());
         st.setTimestamp(3, new Timestamp(initialExpiration.getTime()));
         st.setLong(4, intervalDuration);

         byte[] bytes = serialize(timedObjectId.getInstancePk());
         if(bytes == null)
         {
            st.setNull(5, binarySqlType);
         }
         else
         {
            st.setBytes(5, bytes);
         }

         bytes = serialize(info);
         if(bytes == null)
         {
            st.setNull(6, binarySqlType);
         }
         else
         {
            st.setBytes(6, bytes);
         }
         // set the next timeout date, which when the timer is being created, is equal to the initial
         // date of expiry.
         st.setTimestamp(7, new Timestamp(initialExpiration.getTime()));
         
         int rows = st.executeUpdate();
         if (rows != 1)
            log.error("Unable to insert timer for: " + timedObjectId);
      }
      finally
      {
         JDBCUtil.safeClose(st);
         JDBCUtil.safeClose(con);
      }
   }

   /** Select a list of currently persisted timer handles
    * @return List<TimerHandleImpl>
    */
   public List selectTimers(ObjectName containerId) throws SQLException
   {
      Connection con = null;
      Statement st = null;
      ResultSet rs = null;
      try
      {
         con = ds.getConnection();

         List list = new ArrayList();

         st = con.createStatement();
         rs = st.executeQuery("select * from " + getTableName());
         while (rs.next())
         {
            String timerId = rs.getString(getColumnTimerID());
            TimedObjectId targetId = TimedObjectId.parse(rs.getString(getColumnTargetID()));
            
            // add this handle to the returned list, if a null containerId was used
            // or the containerId filter matches 
            if (containerId == null || containerId.equals(targetId.getContainerId()))
            {
               Date initialDate = rs.getTimestamp(getColumnInitialDate());
               Date nextTimeout = rs.getTimestamp(getColumnNextDate());
               long interval = rs.getLong(getColumnTimerInterval());
               Serializable pKey = (Serializable)deserialize(rs.getBytes(getColumnInstancePK()));
               Serializable info = null;
               try
               {
                  info = (Serializable)deserialize(rs.getBytes(getColumnInfo()));
               }
               catch (Exception e)
               {
                  // may happen if listing all handles (containerId is null)
                  // with a stored custom info object coming from a scoped
                  // deployment.
                  log.warn("Cannot deserialize custom info object", e);
               }
               // is this really needed? targetId encapsulates pKey as well!
               targetId = new TimedObjectId(targetId.getContainerId(), pKey);
               TimerHandleImpl handle = new TimerHandleImpl(timerId, targetId, initialDate, nextTimeout, interval, info);
               list.add(handle);
            }
         }

         return list;
      }
      finally
      {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(st);
         JDBCUtil.safeClose(con);
      }
   }

   /** Delete a timer. */
   public void deleteTimer(String timerId, TimedObjectId timedObjectId)
           throws SQLException
   {
      Connection con = null;
      PreparedStatement st = null;
      ResultSet rs = null;

      try
      {
         con = ds.getConnection();

         String sql = "delete from " + getTableName() + " where " + getColumnTimerID() + "=? and " + getColumnTargetID() + "=?";
         st = con.prepareStatement(sql);

         st.setString(1, timerId);
         st.setString(2, timedObjectId.toString());

         int rows = st.executeUpdate();
         
         // This appears when a timer is created & persisted inside a tx,
         // but then the tx is rolled back, at which point we go back
         // to remove the entry, but no entry is found.
         // Is this because we are "enlisting" the datasource in the tx, too?
         if (rows != 1)
         {
            log.debug("Unable to remove timer for: " + timerId);
         }
      }
      finally
      {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(st);
         JDBCUtil.safeClose(con);
      }
   }

   /** Clear all persisted timers */
   public void clearTimers()
           throws SQLException
   {
      Connection con = null;
      PreparedStatement st = null;
      ResultSet rs = null;
      try
      {
         con = ds.getConnection();
         st = con.prepareStatement("delete from " + getTableName());
         st.executeUpdate();
      }
      finally
      {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(st);
         JDBCUtil.safeClose(con);
      }
   }
   
   /**
    * {@inheritDoc}
    * @throws NullPointerException If either <code>timerId</code> or <code>timedObjectId</code> or both are null
    */
   @Override
   public void updateNextTimeout(String timerId, TimedObjectId timedObjectId, Date nextTimeout) throws SQLException
   {
      if (timerId == null || timedObjectId == null)
      {
         throw new NullPointerException();
      }
      
      log.debug("Updating next timeout date to " + nextTimeout + " for timer: " + timerId + " ,timedObjectId: "
            + timedObjectId.toString());
      
      Connection con = null;
      PreparedStatement st = null;

      try
      {
         con = ds.getConnection();

         // update the next timeout date column value
         String sql = "update " + getTableName() +
         " " + " SET " + getColumnNextDate() + "=?" +
         " where " + getColumnTimerID() + "=? and " + getColumnTargetID() + "=?";
         
         st = con.prepareStatement(sql);

         if (nextTimeout == null)
         {
            JDBCTypeMappingMetaData typeMapping = (JDBCTypeMappingMetaData)server.getAttribute(metaDataName, "TypeMappingMetaData");
            if (typeMapping == null)
            {
               throw new IllegalStateException("Cannot obtain type mapping from: " + metaDataName);
            }
            int dateType = typeMapping.getTypeMappingMetaData(Timestamp.class).getJdbcType();
            // set the next timeout as null
            st.setNull(1, dateType);
         }
         else
         {
            st.setTimestamp(1, new Timestamp(nextTimeout.getTime()));
         }
         st.setString(2, timerId);
         st.setString(3, timedObjectId.toString());

         // run the update sql
         int rows = st.executeUpdate();
         
         if (rows != 1)
         {
            log.debug("Unexpected update row count: " + rows + " for timer: " + timerId + " timedObjectId: "
                  + timedObjectId.toString());
         }
      }
      catch (SQLException sqle)
      {
         throw sqle;
      }
      catch (Exception e)
      {
         log.error("Could not update next timeout date for timerId: " + timerId + " timedObjectId: "
               + timedObjectId.toString(), e);
      }
      finally
      {
         JDBCUtil.safeClose(st);
         JDBCUtil.safeClose(con);
      }
   }

   /** Get the timer table name */
   public String getTableName()
   {
      return tableName;
   }

   /** Get the timer ID column name */
   public String getColumnTimerID()
   {
      return "TIMERID";
   }

   /** Get the target ID column name */
   public String getColumnTargetID()
   {
      return "TARGETID";
   }

   /** Get the initial date column name */
   public String getColumnInitialDate()
   {
      return "INITIALDATE";
   }

   /** Returns the column name of the next timeout date */
   public String getColumnNextDate()
   {
	   return "NEXTDATE";
   }
   
   /** Get the timer interval column name */
   public String getColumnTimerInterval()
   {
      // Note 'INTERVAL' is a reserved word in MySQL
      return "TIMERINTERVAL";
   }

   /** Get the instance PK column name */
   public String getColumnInstancePK()
   {
      return "INSTANCEPK";
   }

   /** Get the info column name */
   public String getColumnInfo()
   {
      return "INFO";
   }

   /** Serialize an object */
   protected byte[] serialize(Object obj)
   {
      if (obj == null)
         return null;

      ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
      try
      {
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(obj);
         oos.close();
      }
      catch (IOException e)
      {
         log.error("Cannot serialize: " + obj, e);
      }
      return baos.toByteArray();
   }

   /** Deserialize an object */
   protected Object deserialize(byte[] bytes)
   {
      if (bytes == null)
         return null;

      ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
      try
      {
         // Use an ObjectInputStream that instantiates objects
         // using the Thread Context ClassLoader (TCL)
         ObjectInputStream oos = new MarshalledValueInputStream(bais);
         return oos.readObject();
      }
      catch (Exception e)
      {
         log.error("Cannot deserialize", e);
         return null;
      }
   }

   /** Deserialize an object */
   protected Object deserialize(InputStream input)
   {

      if (input == null)
         return null;

      byte[] barr = new byte[1024];
      ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
      try
      {
         for (int b = 0; (b = input.read(barr)) > 0;)
         {
            baos.write(barr, 0, b);
         }
         return deserialize(baos.toByteArray());
      }
      catch (Exception e)
      {
         log.error("Cannot deserialize", e);
         return null;
      }
   }
   
   /**
    * Returns true if the timer table already contains the next timeout date column (represented
    * by {@link #getColumnNextDate()}). Else returns false.
    * @return
    */
   // JBPAPP-4681
   private boolean isNextDateColumnPresent()
   {
      // Just fire a query on the timer table with the next timeout date column in the
      // select clause. If the query fails, then the column in considered absent.
      // I don't like this implementation, but this is the simplest. And since this method
      // is only there to take care of already existing Timer table, this impl should be OK for now
      Connection con = null;
      PreparedStatement st = null;
      ResultSet rs = null;
      try
      {
         con = ds.getConnection();

         String sql = "select " + getColumnNextDate() + " from " + getTableName() + " where " + getColumnTimerID()
               + "=?";

         st = con.prepareStatement(sql);
         st.setString(1, "");

         rs = st.executeQuery();
         return true;

      }
      catch (SQLException sqle)
      {
         // consider any sqlexception as an indication of column absence.
         // I don't like this, but this is the simplest way to figure out the presence of the 
         // column, instead of having to go via the DatabaseMetaData JDBC API. 
         // After all, this method comes into picture only when the timer table is already present
         // (i.e. on existing deployments)
         return false;
      }
      finally
      {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(st);
         JDBCUtil.safeClose(con);
      }
   }
}

