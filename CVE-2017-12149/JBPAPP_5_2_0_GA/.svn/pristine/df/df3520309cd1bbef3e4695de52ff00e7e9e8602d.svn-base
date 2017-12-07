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
package org.jboss.test.tm.test;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;
import org.jboss.test.util.ejb.EJBTestCase;
import org.jboss.tm.TransactionLocal;
import org.jboss.tm.TransactionManagerLocator;

public class TransactionLocalUnitTestCase extends EJBTestCase
{

   protected TransactionManager tm;

   @Override
   public void setUp()
   {
      tm = TransactionManagerLocator.getInstance().locate();
   }
   
   public void testSimpleSetGet() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm);
      tm.begin();
      try
      {
         local.set("Simple");
         assertEquals("Simple", local.get());
      }
      finally
      {
         tm.commit();
      }
   }
   
   public void testSimpleSetNull() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm);
      tm.begin();
      try
      {
         local.set(null);
      }
      finally
      {
         tm.commit();
      }
   }
   
   public void testSimpleGetWithNoInitial() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm);
      tm.begin();
      try
      {
         assertEquals(null, local.get());
      }
      finally
      {
         tm.commit();
      }
   }
   
   public void testSimpleGetWithInitial() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm)
      {
         protected Object initialValue()
         {
            return "Initial";
         }
      };
      tm.begin();
      try
      {
         assertEquals("Initial", local.get());
      }
      finally
      {
         tm.commit();
      }
   }
   
   public void testGetNoTx() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm);
      assertEquals(null, local.get(null));
   }
   
   public void testGetNoTxInitial() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm)
      {
         protected Object initialValue()
         {
            return "Initial";
         }
      };
      assertEquals("Initial", local.get());
   }
   
   public void testSetNoTx() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm);
      try
      {
         local.set("Something");
         fail("Should not be here");
      }
      catch (IllegalStateException expected)
      {
      }
   }
   
   public void testSimpleSetGetExplicit() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm);
      tm.begin();
      Transaction tx = tm.suspend();
      try
      {
         local.set(tx, "Simple");
         assertEquals("Simple", local.get(tx));
      }
      finally
      {
         tm.resume(tx);
         tm.commit();
      }
   }
   
   public void testSimplePutNullExplicit() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm);
      tm.begin();
      Transaction tx = tm.suspend();
      try
      {
         local.set(tx, null);
      }
      finally
      {
         tm.resume(tx);
         tm.commit();
      }
   }
   
   public void testSimpleGetWithNoInitialExplicit() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm);
      tm.begin();
      Transaction tx = tm.suspend();
      try
      {
         assertEquals(null, local.get(tx));
      }
      finally
      {
         tm.resume(tx);
         tm.commit();
      }
   }
   
   public void testSimpleGetWithInitialExplicit() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm)
      {
         protected Object initialValue()
         {
            return "Initial";
         }
      };
      tm.begin();
      Transaction tx = tm.suspend();
      try
      {
         assertEquals("Initial", local.get(tx));
      }
      finally
      {
         tm.resume(tx);
         tm.commit();
      }
   }
   
   public void testGetNoTxExplicit() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm);
      assertEquals(null, local.get(null));
   }
   
   public void testGetNoTxInitialExplicit() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm)
      {
         protected Object initialValue()
         {
            return "Initial";
         }
      };
      assertEquals("Initial", local.get(null));
   }
   
   public void testSetNoTxExplicit() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm);
      try
      {
         local.set(null, "Something");
         fail("Should not be here");
      }
      catch (IllegalStateException expected)
      {
      }
   }
   
   public void testGetAfterCommit() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm);
      tm.begin();
      try
      {
         local.set("Something");
      }
      finally
      {
         tm.commit();
      }
      assertEquals(null, local.get());
   }
   
   public void testGetInitialAfterCommit() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm)
      {
         protected Object initialValue()
         {
            return "Initial";
         }
      };
      tm.begin();
      try
      {
         local.set("Something");
         assertEquals("Something", local.get());
      }
      finally
      {
         tm.commit();
      }
      assertEquals("Initial", local.get());
   }
   
   public void testGetMarkedRolledBack() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm);
      tm.begin();
      tm.setRollbackOnly();
      try
      {
         assertEquals(null, local.get());
      }
      finally
      {
         tm.rollback();
      }
   }
   
   public void testGetInitialMarkedRolledBack() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm)
      {
         protected Object initialValue()
         {
            return "Initial";
         }
      };
      tm.begin();
      tm.setRollbackOnly();
      try
      {
         assertEquals("Initial", local.get());
      }
      finally
      {
         tm.rollback();
      }
   }
   
   public void testSetMarkedRolledBack() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm);
      tm.begin();
      tm.setRollbackOnly();
      try
      {
         local.set("Something");
         assertEquals("Something", local.get());
      }
      finally
      {
         tm.rollback();
      }
   }
   
   public void testGetAfterComplete() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm);
      tm.begin();
      Transaction tx = tm.getTransaction();
      try
      {
         local.set("Something");
      }
      finally
      {
         tx.commit();
      }
      assertEquals(null, local.get());
      tm.suspend();
   }
   
   public void testGetInitialAfterComplete() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm)
      {
         protected Object initialValue()
         {
            return "Initial";
         }
      };
      tm.begin();
      Transaction tx = tm.getTransaction();
      try
      {
         local.set("Something");
         assertEquals("Something", local.get());
      }
      finally
      {
         tx.commit();
      }
      assertEquals("Initial", local.get());
      tm.suspend();
   }
   
   public void testSuspendResume() throws Exception
   {
      TransactionLocal local = new TransactionLocal(tm);
      tm.begin();
      Transaction tx1 = tm.getTransaction();
      try
      {
         local.set("Something");
         assertEquals("Something", local.get());
         tm.suspend();
         tm.begin();
         try
         {
            Transaction tx2 = tm.getTransaction();
            assertEquals(null, local.get());
            assertEquals("Something", local.get(tx1));
            tm.suspend();
            tm.resume(tx1);
            assertEquals("Something", local.get());
            assertEquals(null, local.get(tx2));
            tm.suspend();
            tm.resume(tx2);
         }
         finally
         {
            tm.commit();
         }
      }
      finally
      {
         tm.resume(tx1);
         tm.commit();
      }
   }
   
   public TransactionLocalUnitTestCase(String name)
   {
      super(name);
   }

   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(TransactionLocalUnitTestCase.class, "transaction-test.jar");
   }
}
