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

// $Id: OracleDatabasePersistencePlugin.java 107116 2010-07-27 15:41:59Z jaikiran $

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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

import javax.management.ObjectName;

import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;
import org.jboss.logging.Logger;

/**
 * This DatabasePersistencePlugin uses getBinaryStream/setBinaryStream to persist the
 * serializable objects associated with the timer.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 107116 $
 * @since 23-Sep-2004
 */
public class OracleDatabasePersistencePlugin extends GeneralPurposeDatabasePersistencePlugin
{
   // logging support
   private static Logger log = Logger.getLogger(OracleDatabasePersistencePlugin.class);

   /**
    * Insert a timer object
    */
   public void insertTimer(
         String timerId,
         TimedObjectId timedObjectId,
         Date initialExpiration,
         long intervalDuration,
         Serializable info)
      throws SQLException
   {
      Connection con = null;
      PreparedStatement st = null;
      try
      {
         con = ds.getConnection();

         String sql = "insert into " + getTableName() + " " +
                 "(" + getColumnTimerID() + "," + getColumnTargetID() +
                 "," + getColumnInitialDate() + "," + getColumnTimerInterval() +
                 "," + getColumnInstancePK() + "," + getColumnInfo() + "," + getColumnNextDate() +  ") " +
                 "values (?,?,?,?,?,?,?)";
         st = con.prepareStatement(sql);

         st.setString(1, timerId);
         st.setString(2, timedObjectId.toString());
         st.setTimestamp(3, new Timestamp(initialExpiration.getTime()));
         st.setLong(4, intervalDuration);

         byte[] pkArr = serialize(timedObjectId.getInstancePk());
         if (pkArr != null)
         {
            InputStream is = new ByteArrayInputStream(pkArr);
            st.setBinaryStream(5, is, pkArr.length);
         }
         else
         {
            st.setBytes(5, null);
         }

         byte[] infoArr = serialize(info);
         if (infoArr != null)
         {
            InputStream is = new ByteArrayInputStream(infoArr);
            st.setBinaryStream(6, is, infoArr.length);
         }
         else
         {
            st.setBytes(6, null);
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

   /**
    * Select a list of currently persisted timer handles
    * 
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
               
               InputStream isPk = rs.getBinaryStream(getColumnInstancePK());
               Serializable pKey = (Serializable)deserialize(isPk);
               Serializable info = null;
               try
               {
                  InputStream isInfo = rs.getBinaryStream(getColumnInfo());
                  info = (Serializable)deserialize(isInfo);
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
}

