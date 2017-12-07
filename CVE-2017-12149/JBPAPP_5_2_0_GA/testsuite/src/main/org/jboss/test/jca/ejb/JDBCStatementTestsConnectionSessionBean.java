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

import javax.ejb.SessionBean;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.jboss.resource.adapter.jdbc.WrappedConnection;
import org.jboss.test.jca.jdbc.TestConnection;

/**
 * JDBCStatementTestsConnectionSessionBean
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 *
 * @ejb:bean   name="JDBCStatementTestsConnectionSession"
 *             jndi-name="JDBCStatementTestsConnectionSession"
 *             local-jndi-name="JDBCStatementTestsConnectionSessionLocal"
 *             view-type="both"
 *             type="Stateless"
 */
public class JDBCStatementTestsConnectionSessionBean implements SessionBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private static final Logger log = Logger.getLogger(JDBCStatementTestsConnectionSessionBean.class);
   
   private SessionContext sessionContext;
   
   public JDBCStatementTestsConnectionSessionBean()
   {

   }

   /**
    * The <code>testConnectionObtainable</code> method gets
    * connections from the TestDriver after setting fail to true.
    * This causes the test sql to throw an exception when the
    * connection is retrieved from a pool, which closes the
    * connection, forcing the connectionmanager to get a new one.  We
    * check this by counting how many connections have been closed.
    *
    *
    * @ejb:interface-method
    * @ejb:transaction type="NotSupported"
    */
   public void testConnectionObtainable()
   {
      TestConnection tc = null;
      try
      {
         DataSource ds = (DataSource) new InitialContext().lookup("java:StatementTestsConnectionDS");
         Connection c = ds.getConnection();
         WrappedConnection wc = (WrappedConnection) c;
         Connection uc = wc.getUnderlyingConnection();
         tc = (TestConnection) Proxy.getInvocationHandler(uc);
         c.close();
         tc.setFail(true);
         int closeCount1 = tc.getClosedCount();
         c = ds.getConnection();
         if (closeCount1 == tc.getClosedCount())
         {
            throw new EJBException("no connections closed!, closedCount: " + closeCount1);
         }
         c.close();
         for (int i = 0; i < 10; i++)
         {

            int closeCount = tc.getClosedCount();
            c = ds.getConnection();
            if (closeCount == tc.getClosedCount())
            {
               throw new EJBException("no connections closed! at iteration: " + i + ", closedCount: " + closeCount);
            }
            c.close();
         }

      }
      catch (SQLException e)
      {
         throw new EJBException(e);
      }
      catch (NamingException e)
      {
         throw new EJBException(e);
      }
      finally
      {
         if (tc != null)
            tc.setFail(false);
      }
   }

   /**
    * @ejb:interface-method
    * @ejb:transaction type="NotSupported"
    */
   public void testConfiguredQueryTimeout()
   {
      try
      {
         DataSource ds = (DataSource) new InitialContext().lookup("java:StatementTestsConnectionDS");
         Connection c = ds.getConnection();
         try
         {
            Statement s = c.createStatement();
            s.execute("blah");
            if (s.getQueryTimeout() != 100)
               throw new EJBException("Configured query timeout not set");
         }
         finally
         {
            c.close();
         }
      }
      catch (SQLException e)
      {
         throw new EJBException(e);
      }
      catch (NamingException e)
      {
         throw new EJBException(e);
      }
   }

   /**
    * @ejb:interface-method
    * @ejb:transaction type="Required"
    */
   public void testTransactionQueryTimeout()
   {
      try
      {
         DataSource ds = (DataSource) new InitialContext().lookup("java:StatementTestsConnectionDS");
         Connection c = ds.getConnection();
         try
         {
            Statement s = c.createStatement();
            s.execute("blah");
            if (s.getQueryTimeout() == 0)
               throw new EJBException("Tranaction query timeout not set");
         }
         finally
         {
            c.close();
         }
      }
      catch (SQLException e)
      {
         throw new EJBException(e);
      }
      catch (NamingException e)
      {
         throw new EJBException(e);
      }
   }

   /**
    * @ejb:interface-method
    * @ejb:transaction type="Required"
    */
   public void testTransactionQueryTimeoutMarkedRollback()
   {
      try
      {
         DataSource ds = (DataSource) new InitialContext().lookup("java:StatementTestsConnectionDS");
         Connection c = ds.getConnection();
         try
         {
            Statement s = c.createStatement();
            sessionContext.setRollbackOnly();
            try
            {
               s.execute("blah");
               throw new EJBException("Should not be here!");
            }
            catch (SQLException expected)
            {
               log.info("Got expected sql exception", expected);
            }
         }
         finally
         {
            c.close();
         }
      }
      catch (SQLException e)
      {
         throw new EJBException(e);
      }
      catch (NamingException e)
      {
         throw new EJBException(e);
      }
   }

   /**
    * @ejb:interface-method
    * @ejb:transaction type="NotSupported"
    */
   public void testLazyAutoCommit()
   {
      try
      {
         DataSource ds = (DataSource) new InitialContext().lookup("java:NoTxStatementTestsConnectionDS");
         Connection c = ds.getConnection();
         try
         {
            c.setAutoCommit(false);
            c.rollback();
         }
         finally
         {
            c.close();
         }
         c = ds.getConnection();
         try
         {
            c.setAutoCommit(false);
            c.commit();
         }
         finally
         {
            c.close();
         }
         c = ds.getConnection();
         try
         {
            c.setAutoCommit(false);
            c.rollback(null);
         }
         finally
         {
            c.close();
         }
      }
      catch (SQLException e)
      {
         throw new EJBException(e);
      }
      catch (NamingException e)
      {
         throw new EJBException(e);
      }
   }
   
   public void testRollbackOnCloseNoTx()
   {
      TestConnection tc = null;
      try
      {
         DataSource ds = (DataSource) new InitialContext().lookup("java:NoTxStatementTestsConnectionDS");
         Connection c = ds.getConnection();
         try
         {
            c.setAutoCommit(false);
            WrappedConnection wc = (WrappedConnection) c;
            Connection uc = wc.getUnderlyingConnection();
            tc = (TestConnection) Proxy.getInvocationHandler(uc);

            try
            {
               c.nativeSQL("ERROR");
            }
            catch (SQLException expected)
            {
            }
         }
         finally
         {
            try
            {
               c.close();
            }
            catch (SQLException ignored)
            {
            }
         }
         
         if (tc.isClosed() == false)
            throw new RuntimeException("Connection was not closed");
         if (tc.isRolledBack() == false)
            throw new RuntimeException("Connection was not rolled back");
      }
      catch (SQLException e)
      {
         throw new EJBException(e);
      }
      catch (NamingException e)
      {
         throw new EJBException(e);
      }
   }
   
   public void testRollbackOnCloseManagedTx()
   {
      TestConnection tc = null;
      try
      {
         DataSource ds = (DataSource) new InitialContext().lookup("java:StatementTestsConnectionDS");
         Connection c = ds.getConnection();
         try
         {
            WrappedConnection wc = (WrappedConnection) c;
            Connection uc = wc.getUnderlyingConnection();
            tc = (TestConnection) Proxy.getInvocationHandler(uc);

            try
            {
               c.nativeSQL("ERROR");
            }
            catch (SQLException expected)
            {
            }
         }
         finally
         {
            try
            {
               c.close();
            }
            catch (SQLException ignored)
            {
            }
         }
         
         if (tc.isClosed() == false)
            throw new RuntimeException("Connection was not closed");
         if (tc.isRolledBack() == false)
            throw new RuntimeException("Connection was not rolled back");
      }
      catch (SQLException e)
      {
         throw new EJBException(e);
      }
      catch (NamingException e)
      {
         throw new EJBException(e);
      }
   }

   public void ejbCreate()
   {
   }

   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }

   public void ejbRemove()
   {
   }

   public void setSessionContext(SessionContext ctx)
   {
      sessionContext = ctx;
   }

   public void unsetSessionContext()
   {
   }
}
