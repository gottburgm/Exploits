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
package org.jboss.test.aop.bean;

import org.jboss.tm.TxUtils;

import javax.naming.InitialContext;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 81036 $
 */
public class TxPOJO
{
   TransactionManager tm;

   public TxPOJO() throws Exception
   {
      tm = (TransactionManager)new InitialContext().lookup("java:/TransactionManager");
   }

   public void never() {}

   public void callNever() throws Exception
   {
      boolean exceptionThrown = false;
      tm.begin();
      try
      {
         never();
      }
      catch (Exception ex)
      {
         exceptionThrown = true;
      }
      tm.commit();
      if (!exceptionThrown) throw new Exception("failed on mandatory no tx call");
   }

   public void notsupported() throws Exception
   {
      if (tm.getTransaction() != null) throw new Exception("notsupported() method has tx set");
   }

   public void callNotSupported() throws Exception
   {
      tm.begin();
      notsupported();
      tm.commit();
   }

   public void supports(Transaction tx) throws Exception
   {
      Transaction tmTx = tm.getTransaction();
      if (tx != tmTx) throw new Exception("supports didn't work");
   }

   public boolean hasActiveTransaction() throws Exception
   {
      Transaction tx = tm.getTransaction();
      if (tx == null)
      {
         System.out.println("Transaction: is null");
      } // end of if ()
      else
      {
         System.out.println("Transaction: status " + tx.getStatus() + " of tx" + tx);
      } // end of else

      return TxUtils.isActive(tx);
   }

   public void callSupportsWithTx() throws Exception
   {
      tm.begin();
      Transaction tx = tm.getTransaction();
      supports(tx);
      tm.commit();
   }

   public void callSupportsWithoutTx() throws Exception
   {
      supports(null);
   }

   public void required() throws Exception
   {
      if (tm.getTransaction() == null) throw new Exception("rquired() method has no tx set");
   }


   public void requiresNew(Transaction tx) throws Exception
   {
      Transaction tmTx = tm.getTransaction();
      if (tx == tmTx
          || (tx != null && tx.equals(tmTx))) throw new Exception("transactions shouldn't be equal");
      if (tmTx == null) throw new Exception("tx is null in RequiresNew");
   }
   public void callRequiresNew() throws Exception
   {
      tm.begin();
      Transaction tx = tm.getTransaction();
      requiresNew(tx);
      tm.commit();
   }

   public void mandatory() {}

   public void callMandatoryNoTx() throws Exception
   {
      boolean exceptionThrown = false;
      try
      {
         mandatory();
      }
      catch (Exception ex)
      {
         exceptionThrown = true;
      }
      if (!exceptionThrown) throw new Exception("failed on mandatory no tx call");
   }

   public void callMandatoryWithTx() throws Exception
   {
      tm.begin();
      mandatory();
      tm.commit();
   }


}

