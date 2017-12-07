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
package org.jboss.test.jca.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import junit.framework.Test;

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.ejb.EJBTestCase;
import org.jboss.tm.TransactionManagerLocator;
import org.jboss.tm.TxUtils;

/**
 * Abstract concurrent stress test.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class ExecuteSQLDuringRollbackStressTestCase extends EJBTestCase
{
   protected final Logger log = Logger.getLogger(getClass());
   
   private DataSource dataSource;
   
   private CountDownLatch latch;

   private TransactionManager tm;
   
   private AtomicReference<Transaction> transaction = new AtomicReference<Transaction>();

   public void testExecuteSQLDuringRollback() throws Throwable
   {
      dataSource = (DataSource) new InitialContext().lookup("java:DefaultDS");
      createDatabase();
      for (int i = 0; i < getIterationCount(); ++i)
      {
         latch = new CountDownLatch(2);

         ExecuteSQL executeSQL = new ExecuteSQL();
         Thread thread1 = new Thread(executeSQL);
         thread1.start();

         Rollback rollback = new Rollback();
         Thread thread2 = new Thread(rollback);
         thread2.start();
         
         thread1.join();
         thread2.join();
         if (executeSQL.error != null)
            throw executeSQL.error;
         if (rollback.error != null)
            throw rollback.error;
         checkDatabase();
      }
   }
   
   public class ExecuteSQL extends TestRunnable
   {
      private Connection c;
      private Statement stmt;
      
      public void setup() throws Throwable
      {
         tm.begin();
         transaction.set(tm.getTransaction());
         
         try
         {
            c = dataSource.getConnection();
            stmt = c.createStatement();
            stmt.executeUpdate("insert into JCA_EXECUTE_ROLLBACK values ('101')");
         }
         catch (Throwable t)
         {
            try
            {
               if (c != null)
                  c.close();
            }
            catch (Exception ignored)
            {
            }
            try
            {
               tm.rollback();
            }
            catch (Exception ignored)
            {
               log.warn("Ignored", ignored);
            }
            throw t;
         }
      }

      public void test() throws Throwable
      {
         try
         {
            stmt.executeUpdate("delete from JCA_EXECUTE_ROLLBACK where name='100'");
         }
         catch (SQLException expected)
         {
         }
         finally
         {
            try
            {
               if (c != null)
                  c.close();
            }
            catch (Exception ignored)
            {
            }
            try
            {
               synchronized (transaction)
               {
                  if (TxUtils.isActive(tm))
                     tm.rollback();
                  else
                     tm.suspend();
               }
            }
            catch (Exception ignored)
            {
            }
         }
         
         tm.begin();
         try
         {
            Connection c = dataSource.getConnection();
            try
            {
               Statement s = c.createStatement();
               s.executeQuery("select * from JCA_EXECUTE_ROLLBACK");
            }
            finally
            {
               try
               {
                  c.close();
               }
               catch (Exception ignored)
               {
               }
            }
         }
         finally
         {
            tm.commit();
         }
      }
   }
   
   public class Rollback extends TestRunnable
   {
      public void test() throws Throwable
      {
         Transaction tx = transaction.get();
         if (tx != null)
         {
            try
            {
               synchronized (transaction)
               {
                  if (TxUtils.isActive(tx))
                     tx.rollback();
               }
            }
            catch (Exception ignored)
            {
            }
         }
      }
   }
   
   protected void createDatabase() throws Throwable
   {
      Connection c = dataSource.getConnection();
      try
      {
         Statement stmt = c.createStatement();
         try
         {
            stmt.executeUpdate("create table JCA_EXECUTE_ROLLBACK (name varchar(100))");
         }
         catch (SQLException ignored)
         {
         }
         stmt.executeUpdate("delete from JCA_EXECUTE_ROLLBACK");
         stmt.executeUpdate("insert into JCA_EXECUTE_ROLLBACK values ('100')");
      }
      finally
      {
         try
         {
            c.close();
         }
         catch (Exception ignored)
         {
         }
      }
   }
   
   protected void checkDatabase() throws Throwable
   {
      Connection c = dataSource.getConnection();
      try
      {
         Statement stmt = c.createStatement();
         ResultSet rs = stmt.executeQuery("select name from JCA_EXECUTE_ROLLBACK");
         if (rs.next() == false)
            throw new RuntimeException("Expected a first row");
         String value = rs.getString(1);
         if ("100".equals(value) == false)
            throw new RuntimeException("Expected first row to be 100 got " + value);
         if (rs.next())
            throw new RuntimeException("Expected only one row");
      }
      catch (Throwable t)
      {
         log.error("Error checking database", t);
         throw t;
      }
      finally
      {
         try
         {
            c.close();
         }
         catch (Exception ignored)
         {
         }
      }
   }
   
   public class TestRunnable implements Runnable
   {
      public Throwable error;
      
      public void setup() throws Throwable
      {
      }
      
      public void test() throws Throwable
      {
      }
      
      public void run()
      {
         try
         {
            setup();
         }
         catch (Throwable t)
         {
            error = t;
            latch.countDown();
            return;
         }
         latch.countDown();
         try
         {
            latch.await();
         }
         catch (InterruptedException e)
         {
            log.warn("Ignored", e);
         }
         try
         {
            test();
         }
         catch (Throwable t)
         {
            error = t;
         }
      }
   }
   
   protected void setUp() throws Exception
   {
      tm = TransactionManagerLocator.getInstance().locate();
   }
   
   public ExecuteSQLDuringRollbackStressTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(ExecuteSQLDuringRollbackStressTestCase.class, "jca-tests.jar");
   }
}
