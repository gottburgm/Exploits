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
package org.jboss.test.cmp2.ageout.test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;
import org.jboss.ejb.plugins.cmp.jdbc2.schema.Cache;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.ejb.EJBTestCase;
import org.jboss.test.cmp2.ejbselect.ALocal;
import org.jboss.test.cmp2.ejbselect.ALocalHome;
import org.jboss.test.cmp2.ejbselect.AUtil;
import org.jboss.mx.util.MBeanServerLocator;
import junit.framework.Test;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81036 $</tt>
 */
public class JDBC2PmAgeOutUnitTestCase
   extends EJBTestCase
{
   private int maxAgeMs = 3000;
   private int overagerPeriodMs = 1000;
   private DataSource ds;
   private TransactionManager tm;
   private CacheListener cacheListener;

   public JDBC2PmAgeOutUnitTestCase(String methodName)
   {
      super(methodName);
   }

   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(JDBC2PmAgeOutUnitTestCase.class, "cmp2-jdbc2pm-ageout.jar");
   }

   public void setUpEJB(Properties  props) throws Exception
   {
      MBeanServer server = MBeanServerLocator.locateJBoss();
      cacheListener = new CacheListener();
      server.invoke(new ObjectName("jboss.cmp:ejbname=A,service=tablecache,table=TEST_A"),
         "registerListener", new Object[]{
            cacheListener
         }, new String[]{Cache.Listener.class.getName()});
   }

   public void testAgeOut() throws Exception
   {
      Transaction tx = suspendTx();
      String id = "a1";

      try
      {
         beginTx();
         ALocalHome ah = AUtil.getLocalHome();
         ALocal a = ah.create(id);
         a.setIntField(1);
         commitTx();
         long lastUpdated = System.currentTimeMillis();

         checkAge(lastUpdated);
         assertValue(id, 1);

         checkAge(lastUpdated);
         jdbcUpdate(id, 2);

         checkAge(lastUpdated);
         assertValue(id, 1);

         sleepUntilEvicted();
         long lastEvicted = cacheListener.lastEvicted;
         assertValue(id, 2);

         // test ejb update
         try
         {
            Thread.sleep(1000);
         }
         catch(InterruptedException e)
         {
            e.printStackTrace();
         }

         beginTx();
         a = ah.findByPrimaryKey(id);
         a.setIntField(3);
         commitTx();

         sleepUntilEvicted();
         assertTrue(cacheListener.lastEvicted - lastEvicted >= maxAgeMs + 1000);
      }
      finally
      {
         resumeTx(tx);
      }
   }

   private void sleepUntilEvicted()
   {
      int sleepTime = 0;
      while(!cacheListener.evicted)
      {
         try
         {
            Thread.sleep(1000);
            sleepTime += 1000;
         }
         catch(InterruptedException e)
         {
            e.printStackTrace();
         }

         if(!cacheListener.evicted && sleepTime > maxAgeMs + overagerPeriodMs)
         {
            fail("The instance must have been evicted!");
         }
      }
      cacheListener.evicted = false;      
   }

   private void checkAge(long lastUpdated)
      throws Exception
   {
      if(System.currentTimeMillis() - lastUpdated > maxAgeMs)
      {
         throw new Exception("maxAgeMs should be increased for this test to work.");
      }
   }

   private void assertValue(String id, int value) throws Exception
   {
      beginTx();
      try
      {
         ALocalHome ah = AUtil.getLocalHome();
         ALocal a = ah.findByPrimaryKey(id);
         assertEquals(value, a.getIntField());
      }
      finally
      {
         commitTx();
      }
   }

   private void jdbcUpdate(String id, int value) throws Exception
   {
      DataSource ds = getDS();
      Connection con = null;
      Statement st = null;
      try
      {
         con = ds.getConnection();
         st = con.createStatement();
         int rows = st.executeUpdate("update TEST_A set INT_FIELD=" +
            value + " where ID='" + id + "'"
         );
         if(rows != 1)
         {
            throw new Exception("Expected one updated row but got " + rows);
         }
      }
      finally
      {
         JDBCUtil.safeClose(st);
         JDBCUtil.safeClose(con);
      }
   }

   private DataSource getDS() throws NamingException
   {
      if(ds == null)
      {
         ds = (DataSource)new InitialContext().lookup("java:/DefaultDS");
      }
      return ds;
   }

   private Transaction suspendTx() throws Exception
   {
      return getTM().suspend();
   }

   private void resumeTx(Transaction tx) throws Exception
   {
      getTM().resume(tx);
   }

   private Transaction beginTx() throws Exception
   {
      getTM().begin();
      return getTM().getTransaction();
   }

   private void commitTx() throws Exception
   {
      getTM().commit();
   }

   private TransactionManager getTM()
      throws NamingException
   {
      if(tm == null)
      {
         tm = (TransactionManager)new InitialContext().lookup("java:/TransactionManager");
      }
      return tm;
   }

   private static class CacheListener implements Cache.Listener
   {
      public long lastEvicted;
      public boolean evicted;

      public void contention(int partitionIndex, long time)
      {
      }

      public void eviction(int partitionIndex, Object pk, int size)
      {
         lastEvicted = System.currentTimeMillis();
         evicted = true;
      }

      public void hit(int partitionIndex)
      {
      }

      public void miss(int partitionIndex)
      {
      }
   }
}
