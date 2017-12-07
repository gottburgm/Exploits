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
package org.jboss.test.jca.ejb;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.jboss.logging.Logger;
import org.jboss.test.jca.interfaces.CachedConnectionSessionLocal;

/**
 * CachedConnectionSessionBean.java
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version <tt>$Revision: 81036 $</tt>
 *
 * @ejb:bean   name="CachedConnectionSession"
 *             jndi-name="CachedConnectionSession"
 *             local-jndi-name="CachedConnectionSessionBean"
 *             view-type="both"
 *             type="Stateless"
 *
 */

public class CachedConnectionSessionBean implements SessionBean  {

   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
   private Connection conn;
   private Logger log = Logger.getLogger(getClass().getName());
   private SessionContext ctx;

   /**
    * Describe <code>createTable</code> method here.
    *
    * @ejb:interface-method
    */
   public void createTable()
   {
      try
      {
         dropTable();
      }
      catch (Exception e)
      {
      }

      try
      {
         Statement s = getConn().createStatement();
         try
         {
            s.execute("CREATE TABLE TESTCACHEDCONN (ID NUMERIC(18,0) NOT NULL PRIMARY KEY, VAL VARCHAR(255))");
         }
         finally
         {
            s.close();
         }
      }
      catch (SQLException e)
      {
         log.error("sql exception in create table", e);
      }
   }

   /**
    * Describe <code>dropTable</code> method here.
    *
    * @ejb:interface-method
    */
   public void dropTable()
   {
      try
      {
         Statement s = getConn().createStatement();
         try
         {
            s.execute("DROP TABLE TESTCACHEDCONN");
         }
         finally
         {
            s.close();
         }
      }
      catch (SQLException e)
      {
         log.error("sql exception in drop", e);
      }
   }

   /**
    * Describe <code>insert</code> method here.
    *
    * @param id a <code>String</code> value
    * @param value a <code>String</code> value
    *
    * @ejb:interface-method
    */
   public void insert(long id, String value)
   {
      try
      {
         PreparedStatement p = getConn().prepareStatement("INSERT INTO TESTCACHEDCONN (ID, VAL) VALUES (?, ?)");
         try
         {
            p.setLong(1, id);
            p.setString(2, value);
            p.execute();
         }
         finally
         {
            p.close();
         }
      }
      catch (SQLException e)
      {
         log.error("sql exception in insert", e);
      } 
   }

   /**
    * Describe <code>fetch</code> method here.
    *
    * @param id a <code>String</code> value
    *
    * @ejb:interface-method
    */
   public String fetch(long id)
   {
      try
      {
         PreparedStatement p = getConn().prepareStatement("SELECT VAL FROM TESTCACHEDCONN WHERE ID = ?");
         ResultSet rs = null;
         try
         {
            p.setLong(1, id);
            rs = p.executeQuery();
            if (rs.next())
            {
               return rs.getString(1);
            }
            return null;
         }
         finally
         {
            rs.close();
            p.close();
         }
      }
      catch (SQLException e)
      {
         log.error("sql exception in fetch", e);
         return null;
      }
   }
   private Connection getConn()
   {
      if (conn == null)
      {
         log.info("ejb activate never called, conn == null");
         ejbActivate();
      }
      if (conn == null)
      {
         throw new IllegalStateException("could not get a connection");
      }

      return conn;
   }

   /**
    * Invoke another bean that opens a thread local connection,
    * we close it.
    *
    * @ejb:interface-method
    */
   public void firstTLTest()
   {
      try
      {
         CachedConnectionSessionLocal other = (CachedConnectionSessionLocal) ctx.getEJBLocalObject();
         other.secondTLTest();
         ThreadLocalDB.close();
      }
      catch (Exception e)
      {
         log.info("Error", e);
         throw new EJBException(e.toString());
      }
   }

   /**
    * @ejb:interface-method
    */
   public void secondTLTest()
   {
      try
      {
         Connection c = ThreadLocalDB.open();
         c.createStatement().close();
      }
      catch (Exception e)
      {
         log.info("Error", e);
         throw new EJBException(e.toString());
      }
   }

   public void ejbCreate()
   {
   }

   public void ejbActivate()
   {
      log  = Logger.getLogger(getClass());
      try
      {
         //DataSource ds = (DataSource)new InitialContext().lookup("java:/comp/env/datasource");
         DataSource ds = (DataSource)new InitialContext().lookup("java:DefaultDS");
         conn = ds.getConnection();
      }
      catch (NamingException e)
      {
         log.error("naming exception in activate", e);
      }
      catch (SQLException e)
      {
         log.error("sql exception in activate", e);
      }

    }

   public void ejbPassivate() throws RemoteException
   {
      try
      {
         conn.close();
      }
      catch (SQLException e)
      {
         log.error("sql exception in passivate", e);
      }
      conn = null;
      log = null;
   }

   public void ejbRemove() throws RemoteException
   {
   }

   public void setSessionContext(SessionContext ctx) throws RemoteException
   {
      this.ctx = ctx;
   }

   public void unsetSessionContext() throws RemoteException
   {
   }
}
