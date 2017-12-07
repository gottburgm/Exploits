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
package org.jboss.tm.usertx.server;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import java.rmi.RemoteException;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.Status;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;

import org.jboss.logging.Logger;
import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.tm.TransactionPropagationContextUtil;
import org.jboss.tm.usertx.interfaces.UserTransactionSession;
import org.jboss.util.collection.WeakValueHashMap;

/** A UserTransaction session implementation.
 *  It handles transactions on behalf of a single client.
 * @author Ole Husgaard
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a> 
 * @version $Revision: 81030 $
 */
public class UserTransactionSessionImpl
   implements UserTransactionSession
{
   /** Cache a reference to the TM. */
   private static TransactionManager tm = null;
   private static Logger log = Logger.getLogger(UserTransactionSessionImpl.class);
   /**
    *  Maps the TPCs of all active transactions to their transactions.
    */
   private static Map activeTx = Collections.synchronizedMap(new WeakValueHashMap());
   private static UserTransactionSessionImpl instance = new UserTransactionSessionImpl();

   public static UserTransactionSession getInstance()
   {
      return instance;
   }

   /**
    *  Get a reference to the transaction manager.
    */
   protected synchronized static TransactionManager getTransactionManager()
   {
      if (tm == null)
      {
         try
         {
            Context ctx = new InitialContext();
            tm = (TransactionManager)ctx.lookup("java:/TransactionManager");
         }
         catch (NamingException ex)
         {
            log.error("java:/TransactionManager lookup failed", ex);
         }
      }
      return tm;
   }

   /** Cache a reference to the TPC Factory. */
   private static TransactionPropagationContextFactory tpcFactory = TransactionPropagationContextUtil.getTPCFactory();
   
   /**
    *  Get a reference to the TPC Factory
    */
   protected static TransactionPropagationContextFactory getTPCFactory()
   {
      return tpcFactory;
   }  

   //
   // implements interface UserTransactionSession
   //
   
   /**
    *  Destroy this session.
    */
   public void destroy()
      throws RemoteException
   {
      unreferenced();
   }
   
   /**
    *  Start a new transaction, and return its TPC.
    *
    *  @param timeout The timeout value for the new transaction, in seconds.
    *
    *  @return The transaction propagation context for the new transaction.
    */
   public Object begin(int timeout)
      throws RemoteException,
      NotSupportedException,
      SystemException
   {
      TransactionManager tm = getTransactionManager();
      // Set timeout value
      tm.setTransactionTimeout(timeout);
      // Start tx, and get its TPC.
      tm.begin();
      Object tpc = getTPCFactory().getTransactionPropagationContext();
      // Suspend thread association.
      Transaction tx = tm.suspend();
      // Remember that a new tx is now active.
      activeTx.put(tpc, tx);
      // return the TPC
      return tpc;
   }
   
   /**
    *  Commit the transaction.
    *
    *  @param tpc The transaction propagation context for the transaction.
    */
   public void commit(Object tpc)
      throws RemoteException,
      RollbackException,
      HeuristicMixedException,
      HeuristicRollbackException,
      SecurityException,
      IllegalStateException,
      SystemException
   {
      Transaction tx = (Transaction)activeTx.get(tpc);
      
      if (tx == null)
         throw new IllegalStateException("No transaction.");

      // Resume thread association
      TransactionManager tm = getTransactionManager();
      tm.resume(tx);

      boolean finished = true;
      
      try
      {
         tm.commit();
      }
      catch (java.lang.SecurityException ex)
      {
         finished = false;
         throw ex;
      }
      catch (java.lang.IllegalStateException ex)
      {
         finished = false;
         throw ex;
      }
      finally
      {
         activeTx.remove(tpc);
      }
   }
   
   /**
    *  Rollback the transaction.
    *
    *  @param tpc The transaction propagation context for the transaction.
    */
   public void rollback(Object tpc)
      throws RemoteException,
      SecurityException,
      IllegalStateException,
      SystemException
   {
      Transaction tx = (Transaction)activeTx.get(tpc);
      
      if (tx == null)
         throw new IllegalStateException("No transaction.");
      
      // Resume thread association
      TransactionManager tm = getTransactionManager();
      tm.resume(tx);

      tm.rollback();
      activeTx.remove(tpc);
   }
   
   /**
    *  Mark the transaction for rollback only.
    *
    *  @param tpc The transaction propagation context for the transaction.
    */
   public void setRollbackOnly(Object tpc)
      throws RemoteException,
      IllegalStateException,
      SystemException
   {
      Transaction tx = (Transaction)activeTx.get(tpc);
      
      if (tx == null)
         throw new IllegalStateException("No transaction.");
      
      tx.setRollbackOnly();
   }
   
   /**
    *  Return status of the transaction.
    *
    *  @param tpc The transaction propagation context for the transaction.
    */
   public int getStatus(Object tpc)
      throws RemoteException,
      SystemException
   {
      Transaction tx = (Transaction)activeTx.get(tpc);
      
      if (tx == null)
         return Status.STATUS_NO_TRANSACTION;
      
      return tx.getStatus();
   }
   
   
   //
   // implements interface Unreferenced
   //
   
   /**
    *  When no longer referenced, be sure to rollback any
    *  transactions that are still active.
    */
   public void unreferenced()
   {
      log.debug("Lost connection to UserTransaction client.");
      
      synchronized (activeTx)
      {      
         if (!activeTx.isEmpty())
         {
            log.error("Lost connection to UserTransaction clients: " +
            "Rolling back " + activeTx.size() +
            " active transaction(s).");
            Collection txs = activeTx.values();
            Iterator iter = txs.iterator();
            while (iter.hasNext())
            {
               Transaction tx = (Transaction)iter.next();
               try
               {
                  tx.rollback();
               }
               catch (Exception ex)
               {
                  log.error("rollback failed", ex);
               }
            }
         }
      }
   }
   
}
