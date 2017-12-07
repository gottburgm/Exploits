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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import org.jboss.logging.Logger;
import org.jboss.resource.adapter.jdbc.WrappedConnection;

/**
 * LocalWrapperCleanupTestSessionBean.java
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 81036 $
 *
 * @ejb:bean   name="LocalWrapperCleanupTestSession"
 *             jndi-name="LocalWrapperCleanupTestSession"
 *             view-type="remote"
 *             type="Stateless"
 *
 */
public class LocalWrapperCleanupTestSessionBean implements SessionBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   /** The log */
   private static final Logger log = Logger.getLogger(LocalWrapperCleanupTestSessionBean.class);
   
   public void testAutoCommitInReturnedConnection()
   {
      Connection c = null;
      try
      {
         DataSource ds = (DataSource) new InitialContext().lookup("java:/SingleConnectionDS");
         c = ds.getConnection();
         if (c.getAutoCommit() == false)
         {
            throw new EJBException("Initial autocommit state false!");
         }
         c.setAutoCommit(false);
         c.commit();
         c.close();
         c = null;
         c = ds.getConnection();
         if (c.getAutoCommit() == false)
         {
            throw new EJBException("Returned and reaccessed autocommit state false!");
         }
         c.close();
         c = null;
      }
      catch (EJBException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         log.error("Error", e);
         throw new EJBException("Untested problem in test: " + e);
      }
      finally
      {
         try
         {
            if (c != null)
               c.close();
         }
         catch (Throwable ignored)
         {
         }
      }
   }

   public void testAutoCommit()
   {
      Connection c1 = null;
      Connection c2 = null;
      try
      {
         DataSource ds = (DataSource) new InitialContext().lookup("java:/DefaultDS");
         c1 = ds.getConnection();
         Connection uc1 = ((WrappedConnection) c1).getUnderlyingConnection();
         if (c1.getAutoCommit() == false)
         {
            throw new EJBException("Initial autocommit state false!");
         }

         c2 = ds.getConnection();
         if (c2.getAutoCommit() == false)
         {
            throw new EJBException("Initial autocommit state false!");
         } 
         Statement s1 = c1.createStatement();
         Statement s2 = c2.createStatement();
         s1.execute("create table autocommittest (id integer)");
         try
         {
            s1.execute("insert into autocommittest values (1)");
            uc1.rollback();
            ResultSet rs2 = s2.executeQuery("select * from autocommittest where id = 1");
            if (!rs2.next())
            {
               throw new EJBException("Row not visible to other connection, autocommit failed");
            } 
            rs2.close();

         }
         finally
         {
            s1.execute("drop table autocommittest");
         }
      }
      catch (EJBException e)
      {
         throw e;
      } 
      catch (Exception e)
      {
         log.error("Error", e);
         throw new EJBException("Untested problem in test: " + e);
      } 
      finally
      {
         try
         {
            if (c1 != null)
               c1.close();
         }
         catch (Throwable ignored)
         {
         }
         try
         {
            if (c2 != null)
               c2.close();
         }
         catch (Throwable ignored)
         {
         }
      }
   }

   public void testAutoCommitOffInUserTx()
   {
      Connection c1 = null;
      try
      {
         DataSource ds = (DataSource) new InitialContext().lookup("java:/DefaultDS");
         c1 = ds.getConnection();
         Connection uc1 = ((WrappedConnection) c1).getUnderlyingConnection();
         if (c1.getAutoCommit() == false)
         {
            throw new EJBException("Initial autocommit state false!");
         }

         Statement s1 = c1.createStatement();
         s1.execute("create table autocommittest (id integer)");
         try
         {
            UserTransaction ut = (UserTransaction) new InitialContext().lookup("UserTransaction");
            ut.begin();
            s1.execute("insert into autocommittest values (1)");
            if (uc1.getAutoCommit())
            {
               throw new EJBException("Underlying autocommit is true in user tx!");
            } 

            ut.rollback();
            ResultSet rs1 = s1.executeQuery("select * from autocommittest where id = 1");
            if (rs1.next())
            {
               throw new EJBException("Row committed, autocommit still on!");
            } 
            rs1.close();

         }
         finally
         {
            s1.execute("drop table autocommittest");
         } 
      }
      catch (EJBException e)
      {
         throw e;
      } 
      catch (Exception e)
      {
         log.error("Error", e);
         throw new EJBException("Untested problem in test: " + e);
      } 
      finally
      {
         try
         {
            if (c1 != null)
               c1.close();
         }
         catch (Throwable ignored)
         {
         }
      }
   }

   public void testAutoCommitOffInUserTx2()
   {
      try
      {
         createTable();
         UserTransaction ut = (UserTransaction) new InitialContext().lookup("UserTransaction");
         ut.begin();
         insertAndCheckAutoCommit();
         ut.rollback();
      }
      catch (EJBException e)
      {
         throw e;
      } 
      catch (Exception e)
      {
         log.error("Error", e);
         throw new EJBException("Untested problem in test: " + e);
      } 
      finally
      {
         checkRowAndDropTable();
      }

   }

   public void testReadOnly()
   {
      Connection c1 = null;
      try
      {
         DataSource ds = (DataSource) new InitialContext().lookup("java:/DefaultDS");
         c1 = ds.getConnection();
         Connection uc1 = ((WrappedConnection) c1).getUnderlyingConnection();

         if (uc1.isReadOnly() == true)
            throw new EJBException("Initial underlying readonly true!");
         if (c1.isReadOnly() == true)
            throw new EJBException("Initial readonly true!");

         c1.setReadOnly(true);

         if (uc1.isReadOnly() == true)
            throw new EJBException("Read Only should be lazy!");
         if (c1.isReadOnly() == false)
            throw new EJBException("Changed readonly false!");

         c1.createStatement();

         if (uc1.isReadOnly() == false)
            throw new EJBException("Lazy read only failed!");
         if (c1.isReadOnly() == false)
            throw new EJBException("Read only changed unexpectedly!");
      }
      catch (EJBException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         log.error("Error", e);
         throw new EJBException("Untested problem in test: " + e);
      }
      finally
      {
         try
         {
            if (c1 != null)
               c1.close();
         }
         catch (Throwable ignored)
         {
         }
      }
   }

   public void createTable()
   {
      try
      {
         DataSource ds = (DataSource) new InitialContext().lookup("java:/DefaultDS");
         Connection c1 = ds.getConnection();
         try
         {
            if (c1.getAutoCommit() == false)
               throw new EJBException("Initial autocommit state false!");
            Statement s1 = c1.createStatement();
            s1.execute("create table autocommittest (id integer)");
         }
         finally
         {
            c1.close();
         } 
      }
      catch (EJBException e)
      {
         throw e;
      } 
      catch (Exception e)
      {
         log.error("Error", e);
         throw new EJBException("Untested problem in test: " + e);
      } 
   }

   public void insertAndCheckAutoCommit()
   {
      try
      {
         DataSource ds = (DataSource) new InitialContext().lookup("java:/DefaultDS");
         Connection c1 = ds.getConnection();
         Connection uc1 = ((WrappedConnection) c1).getUnderlyingConnection();
         try
         {
            Statement s1 = c1.createStatement();
            s1.execute("insert into autocommittest values (1)");
            if (uc1.getAutoCommit())
               throw new EJBException("Underlying autocommit is true in user tx!");
         }
         finally
         {
            c1.close();
         } 
      }
      catch (EJBException e)
      {
         throw e;
      } 
      catch (Exception e)
      {
         log.error("Error", e);
         throw new EJBException("Untested problem in test: " + e);
      } 

   }

   public void testManualNoCommitRollback()
   {
      try
      {
         DataSource ds = (DataSource) new InitialContext().lookup("java:/DefaultDS");
         Connection c1 = ds.getConnection();
         try
         {
            c1.setAutoCommit(false);
            Statement s1 = c1.createStatement();
            s1.execute("insert into autocommittest values (1)");
         }
         finally
         {
            c1.close();
         } 
      }
      catch (EJBException e)
      {
         throw e;
      } 
      catch (Exception e)
      {
         log.error("Error", e);
         throw new EJBException("Untested problem in test: " + e);
      } 
   }

   public void testManualSecondNoCommitRollback()
   {
      try
      {
         DataSource ds = (DataSource) new InitialContext().lookup("java:/DefaultDS");
         Connection c1 = ds.getConnection();
         try
         {
            c1.setAutoCommit(false);
            Statement s1 = c1.createStatement();
            s1.execute("insert into autocommittest values (0)");
            c1.commit();
            s1.execute("insert into autocommittest values (1)");
         }
         finally
         {
            c1.close();
         } 
      }
      catch (EJBException e)
      {
         throw e;
      } 
      catch (Exception e)
      {
         log.error("Error", e);
         throw new EJBException("Untested problem in test: " + e);
      } 
   }

   public void checkRowAndDropTable()
   {
      try
      {
         DataSource ds = (DataSource) new InitialContext().lookup("java:/DefaultDS");
         Connection c1 = ds.getConnection();
         try
         {
            if (c1.getAutoCommit() == false)
               throw new EJBException("Initial autocommit state false!");

            Statement s1 = c1.createStatement();
            ResultSet rs1 = s1.executeQuery("select * from autocommittest where id = 1");
            if (rs1.next())
               throw new EJBException("Row committed, autocommit still on!");
         }
         finally
         {
            try
            {
               Statement s1 = c1.createStatement();
               s1.execute("drop table autocommittest");
            }
            catch (Throwable t)
            {
               log.warn("Ignored", t);
            }
            c1.close();
         } 

      }
      catch (EJBException e)
      {
         throw e;
      } 
      catch (Exception e)
      {
         log.error("Error", e);
         throw new EJBException("Untested problem in test: " + e);
      }

   }

   public void addRowCheckAndDropTable()
   {
      try
      {
         DataSource ds = (DataSource) new InitialContext().lookup("java:/DefaultDS");
         Connection c1 = ds.getConnection();
         try
         {
            if (c1.getAutoCommit() == false)
               throw new EJBException("Initial autocommit state false!");
            c1.setAutoCommit(false);

            Statement s1 = c1.createStatement();
            s1.execute("insert into autocommittest values (2)");
            c1.commit();

            ResultSet rs1 = s1.executeQuery("select * from autocommittest where id = 1");
            if (rs1.next())
               throw new EJBException("Row committed, didn't rollback!");
         }
         finally
         {
            try
            {
               Statement s1 = c1.createStatement();
               s1.execute("drop table autocommittest");
            }
            catch (Throwable ignored)
            {
            }
            c1.close();
         } 

      }
      catch (EJBException e)
      {
         throw e;
      } 
      catch (Exception e)
      {
         log.error("Error", e);
         throw new EJBException("Untested problem in test: " + e);
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
   }

   public void unsetSessionContext()
   {
   }
}
