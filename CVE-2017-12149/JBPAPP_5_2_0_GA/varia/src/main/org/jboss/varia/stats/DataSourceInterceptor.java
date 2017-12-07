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
package org.jboss.varia.stats;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.ServiceMBean;
import org.jboss.naming.NonSerializableFactory;

import javax.sql.DataSource;
import javax.management.ObjectName;
import javax.naming.NamingException;
import javax.naming.Name;
import javax.naming.InitialContext;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSetMetaData;
import java.sql.Time;
import java.sql.Timestamp;

import java.sql.Savepoint;
import java.sql.ParameterMetaData;

import java.io.PrintWriter;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.Calendar;
import java.math.BigDecimal;
import java.net.URL;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81038 $</tt>
 */
public abstract class DataSourceInterceptor
   extends ServiceMBeanSupport
   implements DataSource, DataSourceInterceptorMBean
{
   /**
    * JNDI name the service will be bound under
    */
   private String bindName;
   /**
    * target DataSource JNDI name
    */
   private String targetName;
   /**
    * target DataSource
    */
   protected DataSource target;

   private ObjectName statsCollector;

   // MBean implementation

   /**
    * @jmx.managed-attribute
    */
   public ObjectName getStatsCollector()
   {
      return statsCollector;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setStatsCollector(ObjectName statsCollector)
   {
      this.statsCollector = statsCollector;
   }

   /**
    * @jmx.managed-attribute
    */
   public String getBindName()
   {
      return bindName;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setBindName(String bindName) throws NamingException
   {
      this.bindName = bindName;
      if(getState() == ServiceMBean.STARTED)
      {
         bind();
      }
   }

   /**
    * @jmx.managed-attribute
    */
   public String getTargetName()
   {
      return targetName;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setTargetName(String targetName) throws NamingException
   {
      this.targetName = targetName;
      if(getState() == ServiceMBean.STARTED)
      {
         updateTarget();
      }
   }

   public void startService()
      throws Exception
   {
      updateTarget();
      bind();
   }

   public void stopService()
      throws Exception
   {
      unbind();
   }

   // DataSource implementation

   public int getLoginTimeout() throws SQLException
   {
      return target.getLoginTimeout();
   }

   public void setLoginTimeout(int seconds) throws SQLException
   {
      target.setLoginTimeout(seconds);
   }

   public PrintWriter getLogWriter() throws SQLException
   {
      return target.getLogWriter();
   }

   public void setLogWriter(PrintWriter out) throws SQLException
   {
      target.setLogWriter(out);
   }

   public abstract Connection getConnection() throws SQLException;

   public abstract Connection getConnection(String username, String password) throws SQLException;

   // Protected 

   protected void logSql(String sql)
   {
      try
      {
         StatisticalItem item = new TxReport.SqlStats(sql);
         server.invoke(statsCollector, "addStatisticalItem",
            new Object[]{item},
            new String[]{StatisticalItem.class.getName()});
      }
      catch(Exception e)
      {
         log.error("Failed to add invocation.", e);
      }
   }

   // Private

   private void bind()
      throws NamingException
   {
      InitialContext ic = null;
      try
      {
         ic = new InitialContext();
         Name name = ic.getNameParser("").parse(bindName);
         NonSerializableFactory.rebind(name, this, true);
         log.debug("bound to JNDI name " + bindName);
      }
      finally
      {
         if(ic != null)
         {
            ic.close();
         }
      }
   }

   private void unbind()
      throws NamingException
   {
      InitialContext ic = null;
      try
      {
         ic = new InitialContext();
         ic.unbind(bindName);
         NonSerializableFactory.unbind(bindName);
      }
      finally
      {
         if(ic != null)
         {
            ic.close();
         }
      }
   }

   private void updateTarget()
      throws NamingException
   {
      InitialContext ic = null;
      try
      {
         ic = new InitialContext();
         target = (DataSource) ic.lookup(targetName);
         log.debug("target updated to " + targetName);
      }
      finally
      {
         if(ic != null)
         {
            ic.close();
         }
      }
   }
}
