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
package org.jboss.embedded.adapters;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.jboss.logging.Logger;
import org.jboss.tm.TransactionManagerLocator;

/**
 * Simple, local-only UserTransaction adapter
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author adrian@jboss.org
 * @version $Revision: 85945 $
 */
public final class LocalOnlyUserTransaction implements UserTransaction, java.io.Externalizable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -5998336139321082314L;

   protected static Logger log = Logger.getLogger(LocalOnlyUserTransaction.class);

   private boolean trace = log.isTraceEnabled();
   
   /**
    * Timeout value in seconds for new transactions started
    * by this bean instance.
    */
   private TransactionManager tm;

   public LocalOnlyUserTransaction()
   {
   }

   public void start()
   {
      if (trace)
         log.trace("new UserTx: " + this);
      this.tm = TransactionManagerLocator.locateTransactionManager();
   }

   public void stop()
   {
      this.tm = null;
   }

   public void begin()
           throws NotSupportedException, SystemException
   {
      // Start the transaction
      tm.begin();

      Transaction tx = tm.getTransaction();
      if (trace)
         log.trace("UserTx begin: " + tx);
   }

   public void commit()
           throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
                  SecurityException, IllegalStateException, SystemException
   {
      Transaction tx = tm.getTransaction();
      if (trace)
         log.trace("UserTx commit: " + tx);

      tm.commit();
   }

   public void rollback()
           throws IllegalStateException, SecurityException, SystemException
   {
      Transaction tx = tm.getTransaction();
      if (trace)
         log.trace("UserTx rollback: " + tx);

      tm.rollback();
   }

   public void setRollbackOnly()
           throws IllegalStateException, SystemException
   {
      Transaction tx = tm.getTransaction();
      if (trace)
         log.trace("UserTx setRollbackOnly: " + tx);

      tm.setRollbackOnly();
   }

   public int getStatus()
           throws SystemException
   {
      return tm.getStatus();
   }

   /**
    * Set the transaction timeout value for new transactions
    * started by this instance.
    */
   public void setTransactionTimeout(int seconds)
           throws SystemException
   {
      tm.setTransactionTimeout(seconds);
   }

   public void writeExternal(ObjectOutput out) throws IOException
   {
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      this.tm = TransactionManagerLocator.locateTransactionManager();
   }
}
