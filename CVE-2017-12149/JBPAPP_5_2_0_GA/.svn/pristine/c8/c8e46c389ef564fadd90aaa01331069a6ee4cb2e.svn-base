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
package org.jboss.test.testbean.bean;

import java.rmi.*;
import javax.ejb.*;
import javax.naming.InitialContext;

import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;

import javax.transaction.UserTransaction;
import javax.transaction.Status;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import javax.sql.DataSource;
import java.sql.SQLException;

public class BMTStatefulBean implements SessionBean
{
   static org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(BMTStatefulBean.class);

   private SessionContext sessionContext;

   public void ejbCreate() throws RemoteException, CreateException
   {
      log.debug("BMTStatefulBean.ejbCreate() called");
   }


   public void ejbCreate(String caca) throws RemoteException, CreateException
   {
   };
	
   public void ejbCreate(String caca, String cacaprout) throws RemoteException, CreateException
   {
   };
	
   public void ejbActivate() throws RemoteException
   {
      log.debug("BMTStatefulBean.ejbActivate() called");
   }

   public void ejbPassivate() throws RemoteException
   {
      log.debug("BMTStatefulBean.ejbPassivate() called");
   }

   public void ejbRemove() throws RemoteException
   {
      log.debug("BMTStatefulBean.ejbRemove() called");
   }

   public void setSessionContext(SessionContext context) throws RemoteException
   {
      sessionContext = context;
   }

   public String txExists() throws RemoteException
   {
      String result = "";
      try
      {
         UserTransaction ut1 = sessionContext.getUserTransaction();
         result += "Got UserTransaction via sessionContext.getUserTransaction(): " + statusName(ut1.getStatus()) + "\n";

         UserTransaction ut2 = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
         result += "Got UserTransaction via lookup(java:comp/UserTransaction): " + statusName(ut2.getStatus()) + "\n";

         return result;
      }
      catch(Exception e)
      {
         log.debug("failed", e);
         throw new RemoteException(e.getMessage());
      }
   }


   public String txCommit() throws RemoteException
   {
      try
      {
         UserTransaction tx = sessionContext.getUserTransaction();

         String result = "Got transaction : " + statusName(tx.getStatus()) + "\n";
         tx.begin();
         result += "tx.begin(): " + statusName(tx.getStatus()) + "\n";
         tx.commit();
         result += "tx.commit(): " + statusName(tx.getStatus()) + "\n";

         return result;

      }
      catch(Exception e)
      {
         log.debug("failed", e);
         throw new RemoteException(e.getMessage());
      }

   }

   public String txRollback() throws RemoteException
   {
      try
      {
         UserTransaction tx = sessionContext.getUserTransaction();

         String result = "Got transaction : " + statusName(tx.getStatus()) + "\n";
         tx.begin();
         result += "tx.begin(): " + statusName(tx.getStatus()) + "\n";
         tx.rollback();
         result += "tx.rollback(): " + statusName(tx.getStatus()) + "\n";

         return result;

      }
      catch(Exception e)
      {
         log.debug("failed", e);
         throw new RemoteException(e.getMessage());
      }
   }


   public String txBegin() throws RemoteException
   {
      try
      {
         UserTransaction tx = sessionContext.getUserTransaction();

         tx.begin();
         return "status: " + statusName(tx.getStatus());
      }
      catch(Exception e)
      {
         log.debug("failed", e);
         throw new RemoteException(e.getMessage());
      }

   }

   public String txEnd() throws RemoteException
   {
      try
      {
         UserTransaction tx = sessionContext.getUserTransaction();

         tx.commit();
         return "status: " + statusName(tx.getStatus());
      }
      catch(Exception e)
      {
         log.debug("failed", e);
         throw new RemoteException(e.getMessage());
      }

   }

   public void createTable() throws RemoteException
   {
      Connection connection = null;
      Statement stm = null;
      try
      {
         connection = ((DataSource) new InitialContext().lookup("java:comp/env/jdbc/myDatabase")).getConnection();
         stm = connection.createStatement();
         try
         {
            stm.executeUpdate("CREATE TABLE bmttest (field VARCHAR(256))");
         }
         catch(SQLException e)
         {
            // ignore, table probably already exists
         }
         finally
         {
            JDBCUtil.safeClose(stm);
         }

         stm = connection.createStatement();
         stm.executeUpdate("INSERT INTO bmttest VALUES ('initial value')");
      }
      catch(Exception e)
      {
         log.debug("failed", e);
         throw new RemoteException(e.getMessage());
      }
      finally
      {
         JDBCUtil.safeClose(stm);
         JDBCUtil.safeClose(connection);
      }
   }

   public void dropTable() throws RemoteException
   {
      Connection connection = null;
      Statement stm = null;
      try
      {
         connection = ((DataSource) new InitialContext().lookup("java:comp/env/jdbc/myDatabase")).getConnection();
         stm = connection.createStatement();
         stm.executeUpdate("DROP TABLE bmttest");
      }
      catch(Exception e)
      {
         log.debug("failed", e);
         throw new RemoteException(e.getMessage());
      }
      finally
      {
         JDBCUtil.safeClose(stm);
         JDBCUtil.safeClose(connection);
      }
   }


   public String dbCommit() throws RemoteException
   {
      try
      {
         UserTransaction tx = sessionContext.getUserTransaction();

         tx.begin();

         Connection connection = null;
         Statement stm = null;
         try
         {
            connection = ((DataSource) new InitialContext().lookup("java:comp/env/jdbc/myDatabase")).getConnection();
            stm = connection.createStatement();
            stm.executeUpdate("UPDATE bmttest SET field = 'updated via dbCommit'");
         }
         finally
         {
            JDBCUtil.safeClose(stm);
            JDBCUtil.safeClose(connection);
         }

         tx.commit();
         return statusName(tx.getStatus());

      }
      catch(Exception e)
      {
         log.debug("failed", e);
         throw new RemoteException(e.getMessage());
      }
   }


   public String dbRollback() throws RemoteException
   {
      try
      {
         UserTransaction tx = sessionContext.getUserTransaction();

         tx.begin();

         Connection connection = null;
         Statement stm = null;
         try
         {
            connection = ((DataSource) new InitialContext().lookup("java:comp/env/jdbc/myDatabase")).getConnection();
            stm = connection.createStatement();
            stm.executeUpdate("UPDATE bmttest SET field = 'updated via dbRollback'");
         }
         finally
         {
            JDBCUtil.safeClose(stm);
            JDBCUtil.safeClose(connection);
         }

         tx.rollback();
         return statusName(tx.getStatus());

      }
      catch(Exception e)
      {
         log.debug("failed", e);
         throw new RemoteException(e.getMessage());
      }
   }


   public String getDbField() throws RemoteException
   {
      Connection connection = null;
      Statement stm = null;
      ResultSet rs = null;
      try
      {
         connection = ((DataSource) new InitialContext().lookup("java:comp/env/jdbc/myDatabase")).getConnection();
         stm = connection.createStatement();
         rs = stm.executeQuery("SELECT field FROM bmttest ");
         String result = "not found";
         if(rs.next())
         {
            result = rs.getString(1);
         }
         return result;
      }
      catch(Exception e)
      {
         log.debug("failed", e);
         throw new RemoteException(e.getMessage());
      }
      finally
      {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(stm);
         JDBCUtil.safeClose(connection);
      }
   }


   private String statusName(int s)
   {
      switch(s)
      {
         case Status.STATUS_ACTIVE:
            return "STATUS_ACTIVE";
         case Status.STATUS_COMMITTED:
            return "STATUS_COMMITED";
         case Status.STATUS_COMMITTING:
            return "STATUS_COMMITTING";
         case Status.STATUS_MARKED_ROLLBACK:
            return "STATUS_MARKED_ROLLBACK";
         case Status.STATUS_NO_TRANSACTION:
            return "STATUS_NO_TRANSACTION";
         case Status.STATUS_PREPARED:
            return "STATUS_PREPARED";
         case Status.STATUS_PREPARING:
            return "STATUS_PREPARING";
         case Status.STATUS_ROLLEDBACK:
            return "STATUS_ROLLEDBACK";
         case Status.STATUS_ROLLING_BACK:
            return "STATUS_ROLLING_BACK";
         case Status.STATUS_UNKNOWN:
            return "STATUS_UNKNOWN";
      }
      return "REALLY_UNKNOWN";
   }
}
