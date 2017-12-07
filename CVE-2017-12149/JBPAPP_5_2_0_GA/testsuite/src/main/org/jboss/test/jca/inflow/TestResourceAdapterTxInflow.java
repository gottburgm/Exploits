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
package org.jboss.test.jca.inflow;

import javax.naming.InitialContext;
import javax.resource.spi.XATerminator;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkManager;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Management interface of TestResourceAdapter.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 83308 $
 */
public class TestResourceAdapterTxInflow
{
   TestResourceAdapter adapter;
   public TestResourceAdapterTxInflow(TestResourceAdapter adapter)
   {
      this.adapter = adapter;
   }
   
   public TestResourceAdapterTxInflowResults run() throws Exception
   {
      TestResourceAdapterTxInflowResults results = new TestResourceAdapterTxInflowResults();
      try
      {
         basicTest();
         results.basicTest.pass();
      }
      catch (Throwable t)
      {
         results.basicTest.fail(t);
      }

      return results;
   }
   
   public TransactionManager getTransactionManager() throws Exception
   {
      InitialContext ctx = new InitialContext();
      return (TransactionManager) ctx.lookup("java:/TransactionManager");
   }
   
   public void basicTest() throws Exception
   {
      XATerminator xt = adapter.ctx.getXATerminator();
      WorkManager wm = adapter.ctx.getWorkManager();
      TestWork work = new TestWork();
      ExecutionContext ec = new ExecutionContext();
      Xid xid = new MyXid(1);
      ec.setXid(xid);

      wm.doWork(work, 0l, ec, null);
      if (work.complete == false)
         throw new Exception("Work was not done");
      if (work.e != null)
         throw work.e;
      if (work.enlisted == false)
         throw new Exception("Not enlisted");
      if (work.delisted)
         throw new Exception("Should not be ended yet");
      if (work.committed)
         throw new Exception("Should not be committed yet");

      xt.commit(xid, true);
      if (work.delisted == false)
         throw new Exception("Should be ended");
      if (work.committed == false)
         throw new Exception("Should be committed");
   }
   
   public class MyXid implements Xid
   {
      int id;
      
      public MyXid(int id)
      {
         this.id = id;
      }
      
      public byte[] getBranchQualifier()
      {
         // TODO getBranchQualifier
         return null;
      }

      public int getFormatId()
      {
         return 666;
      }
      
      public byte[] getGlobalTransactionId()
      {
         return new byte[] { (byte) id }; 
      }
   }
   
   public class TestWork implements Work, XAResource
   {
      public boolean complete = false;
      public Exception e;
      
      public boolean enlisted = false;
      public boolean delisted = false;
      public boolean committed = false;
      
      public void run()
      {
         try
         {
            complete = true;
            TransactionManager tm = getTransactionManager();
            Transaction tx = tm.getTransaction();
            tx.enlistResource(this);
         }
         catch (Exception e)
         {
            this.e = e;
         }
      }

      public void release()
      {
      }
      
      public void commit(Xid arg0, boolean arg1) throws XAException
      {
         committed = true;
      }

      public void end(Xid arg0, int arg1) throws XAException
      {
         delisted = true;
      }
      
      public void forget(Xid arg0) throws XAException
      {
         throw new XAException("NYI");
      }
      
      public int getTransactionTimeout() throws XAException
      {
         // TODO getTransactionTimeout
         return 0;
      }
      
      public boolean isSameRM(XAResource arg0) throws XAException
      {
         // TODO isSameRM
         return false;
      }
      
      public int prepare(Xid arg0) throws XAException
      {
         // JBAS-6407 TestResourceAdapterTxInflow can't assume one phase commit
         return XAResource.XA_OK;
      }

      public Xid[] recover(int arg0) throws XAException
      {
         throw new XAException("NYI");
      }

      public void rollback(Xid arg0) throws XAException
      {
         throw new XAException("NYI");
      }

      public boolean setTransactionTimeout(int arg0) throws XAException
      {
         return false;
      }

      public void start(Xid arg0, int arg1) throws XAException
      {
         enlisted = true;
      }
   }
}
