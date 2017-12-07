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
package org.jboss.ejb.plugins;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.XmlLoadable;
import org.jboss.tm.JBossTransactionRolledbackException;
import org.jboss.tm.JBossTransactionRolledbackLocalException;
import org.jboss.tm.TransactionTimeoutConfiguration;
import org.jboss.util.NestedException;
import org.jboss.util.deadlock.ApplicationDeadlockException;
import org.w3c.dom.Element;

import javax.ejb.EJBException;
import javax.ejb.TransactionRequiredLocalException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionRequiredException;
import javax.transaction.TransactionRolledbackException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Iterator;
import java.util.ArrayList;

/**
 *  This interceptor handles transactions for CMT beans.
 *
 *  @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *  @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *  @author <a href="mailto:akkerman@cs.nyu.edu">Anatoly Akkerman</a>
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 *  @version $Revision: 81030 $
 */
public class TxInterceptorCMT extends AbstractTxInterceptor implements XmlLoadable
{

   // Constants -----------------------------------------------------


   public static int MAX_RETRIES = 5;
   public static Random random = new Random();

   // Attributes ----------------------------------------------------

   /** 
    * Whether an exception should be thrown if the transaction is not
    * active, even though the application doesn't throw an exception
    */
   private boolean exceptionRollback = true;
   
   private TxRetryExceptionHandler[] retryHandlers = null;

   // Static --------------------------------------------------------


   /**
    * Detects exception contains is or a ApplicationDeadlockException.
    */
   public static ApplicationDeadlockException isADE(Throwable t)
   {
      while (t!=null)
      {
         if (t instanceof ApplicationDeadlockException)
         {
            return (ApplicationDeadlockException)t;
         }
         else if (t instanceof RemoteException)
         {
            t = ((RemoteException)t).detail;
         }
         else if (t instanceof EJBException)
         {
            t = ((EJBException)t).getCausedByException();
         }
         else
         {
            return null;
         }
      }
      return null;
   }
   
   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // XmlLoadable implementation ------------------------------------

   public void importXml(Element ielement)
   {
      try
      {
         Element element = MetaData.getOptionalChild(ielement, "retry-handlers");
         if (element == null) return;
         ArrayList list = new ArrayList();
         Iterator handlers = MetaData.getChildrenByTagName(element, "handler");
         while (handlers.hasNext())
         {
            Element handler = (Element)handlers.next();
            String className = MetaData.getElementContent(handler).trim();
            Class clazz = SecurityActions.getContextClassLoader().loadClass(className);
            list.add(clazz.newInstance());
         }
         retryHandlers = (TxRetryExceptionHandler[])list.toArray(new TxRetryExceptionHandler[list.size()]);
      }
      catch (Exception ex)
      {
         log.warn("Unable to importXml for the TxInterceptorCMT", ex);
      }
   }

   // Interceptor implementation ------------------------------------

   public void create() throws Exception
   {
      super.create();
      BeanMetaData bmd = getContainer().getBeanMetaData();
      exceptionRollback = bmd.getExceptionRollback();
      if (exceptionRollback == false)
         exceptionRollback = bmd.getApplicationMetaData().getExceptionRollback();
   }

   public Object invokeHome(Invocation invocation) throws Exception
   {
      Transaction oldTransaction = invocation.getTransaction();
      for (int i = 0; i < MAX_RETRIES; i++)
      {
         try
         {
            return runWithTransactions(invocation);
         }
         catch (Exception ex)
         {
            checkRetryable(i, ex, oldTransaction);
         }
      }
      throw new RuntimeException("Unreachable");
   }

   /**
    *  This method does invocation interpositioning of tx management
    */
   public Object invoke(Invocation invocation) throws Exception
   {
      Transaction oldTransaction = invocation.getTransaction();
      for (int i = 0; i < MAX_RETRIES; i++)
      {
         try
         {
            return runWithTransactions(invocation);
         }
         catch (Exception ex)
         {
            checkRetryable(i, ex, oldTransaction);
         }
      }
      throw new RuntimeException("Unreachable");
   }

   private void checkRetryable(int i, Exception ex, Transaction oldTransaction) throws Exception
   {
      // if oldTransaction != null this means tx was propagated over the wire
      // and we cannot retry it
      if (i + 1 >= MAX_RETRIES || oldTransaction != null) throw ex;
      // Keep ADE check for backward compatibility
      ApplicationDeadlockException deadlock = isADE(ex);
      if (deadlock != null)
      {
         if (!deadlock.retryable()) throw deadlock;
         log.debug(deadlock.getMessage() + " retrying tx " + (i + 1));
      }
      else if (retryHandlers != null)
      {
         boolean retryable = false;
         for (int j = 0; j < retryHandlers.length; j++)
         {
            retryable = retryHandlers[j].retry(ex);
            if (retryable) break;
         }
         if (!retryable) throw ex;
         log.debug(ex.getMessage() + " retrying tx " + (i + 1));
      }
      else
      {
         throw ex;
      }
      Thread.sleep(random.nextInt(1 + i), random.nextInt(1000));
   }

   // Private  ------------------------------------------------------

   private void printMethod(Method m, byte type)
   {
      String txName;
      switch(type)
      {
         case MetaData.TX_MANDATORY:
            txName = "TX_MANDATORY";
            break;
         case MetaData.TX_NEVER:
            txName = "TX_NEVER";
            break;
         case MetaData.TX_NOT_SUPPORTED:
            txName = "TX_NOT_SUPPORTED";
            break;
         case MetaData.TX_REQUIRED:
            txName = "TX_REQUIRED";
            break;
         case MetaData.TX_REQUIRES_NEW:
            txName = "TX_REQUIRES_NEW";
            break;
         case MetaData.TX_SUPPORTS:
            txName = "TX_SUPPORTS";
            break;
         default:
            txName = "TX_UNKNOWN";
      }

      String methodName;
      if(m != null)
         methodName = m.getName();
      else
         methodName ="<no method>";

      if (log.isTraceEnabled())
      {
         if (m != null && (type == MetaData.TX_REQUIRED || type == MetaData.TX_REQUIRES_NEW))
            log.trace(txName + " for " + methodName + " timeout=" + container.getBeanMetaData().getTransactionTimeout(methodName));
         else
            log.trace(txName + " for " + methodName);
      }
   }

    /*
     *  This method does invocation interpositioning of tx management.
     *
     *  This is where the meat is.  We define what to do with the Tx based
     *  on the declaration.
     *  The Invocation is always the final authority on what the Tx
     *  looks like leaving this interceptor.  In other words, interceptors
     *  down the chain should not rely on the thread association with Tx but
     *  on the Tx present in the Invocation.
     *
     *  @param remoteInvocation If <code>true</code> this is an invocation
     *                          of a method in the remote interface, otherwise
     *                          it is an invocation of a method in the home
     *                          interface.
     *  @param invocation The <code>Invocation</code> of this call.
     */
   private Object runWithTransactions(Invocation invocation) throws Exception
   {
      // Old transaction is the transaction that comes with the MI
      Transaction oldTransaction = invocation.getTransaction();
      // New transaction is the new transaction this might start
      Transaction newTransaction = null;

      boolean trace = log.isTraceEnabled();
      if( trace )
         log.trace("Current transaction in MI is " + oldTransaction);

      InvocationType type = invocation.getType();
      byte transType = container.getBeanMetaData().getTransactionMethod(invocation.getMethod(), type);

      if ( trace )
         printMethod(invocation.getMethod(), transType);

      // Thread arriving must be clean (jboss doesn't set the thread
      // previously). However optimized calls come with associated
      // thread for example. We suspend the thread association here, and
      // resume in the finally block of the following try.
      Transaction threadTx = tm.suspend();
      if( trace )
         log.trace("Thread came in with tx " + threadTx);
      try
      {
         switch (transType)
         {
            case MetaData.TX_NOT_SUPPORTED:
            {
               // Do not set a transaction on the thread even if in MI, just run
               try
               {
                  invocation.setTransaction(null);
                  return invokeNext(invocation, false);
               }
               finally
               {
                  invocation.setTransaction(oldTransaction);
               }
            }
            case MetaData.TX_REQUIRED:
            {
               int oldTimeout = 0;
               Transaction theTransaction = oldTransaction;
               if (oldTransaction == null)
               { // No tx running
                  // Create tx
                  oldTimeout = startTransaction(invocation);

                  // get the tx
                  newTransaction = tm.getTransaction();
                  if( trace )
                     log.trace("Starting new tx " + newTransaction);

                  // Let the method invocation know
                  invocation.setTransaction(newTransaction);
                  theTransaction = newTransaction;
               }
               else
               {
                  // We have a tx propagated
                  // Associate it with the thread
                  tm.resume(oldTransaction);
               }

               // Continue invocation
               try
               {
                  Object result = invokeNext(invocation, oldTransaction != null);
                  checkTransactionStatus(theTransaction, type);
                  return result;
               }
               finally
               {
                  if( trace )
                     log.trace("TxInterceptorCMT: In finally");

                  // Only do something if we started the transaction
                  if (newTransaction != null)
                     endTransaction(invocation, newTransaction, oldTransaction, oldTimeout);
                  else
                     tm.suspend();
               }
            }
            case MetaData.TX_SUPPORTS:
            {
               // Associate old transaction with the thread
               // Some TMs cannot resume a null transaction and will throw
               // an exception (e.g. Tyrex), so make sure it is not null
               if (oldTransaction != null)
               {
                  tm.resume(oldTransaction);
               }

               try
               {
                  Object result = invokeNext(invocation, oldTransaction != null);
                  if (oldTransaction != null)
                     checkTransactionStatus(oldTransaction, type);
                  return result;
               }
               finally
               {
                  tm.suspend();
               }

               // Even on error we don't do anything with the tx,
               // we didn't start it
            }
            case MetaData.TX_REQUIRES_NEW:
            {
               // Always begin a transaction
               int oldTimeout = startTransaction(invocation);

               // get it
               newTransaction = tm.getTransaction();

               // Set it on the method invocation
               invocation.setTransaction(newTransaction);
               // Continue invocation
               try
               {
                  Object result = invokeNext(invocation, false);
                  checkTransactionStatus(newTransaction, type);
                  return result;
               }
               finally
               {
                  // We started the transaction for sure so we commit or roll back
                  endTransaction(invocation, newTransaction, oldTransaction, oldTimeout);
               }
            }
            case MetaData.TX_MANDATORY:
            {
               if (oldTransaction == null)
               {
                  if (type == InvocationType.LOCAL ||
                        type == InvocationType.LOCALHOME)
                  {
                     throw new TransactionRequiredLocalException(
                           "Transaction Required");
                  }
                  else
                  {
                     throw new TransactionRequiredException(
                           "Transaction Required");
                  }
               }

               // Associate it with the thread
               tm.resume(oldTransaction);
               try
               {
                  Object result = invokeNext(invocation, true);
                  checkTransactionStatus(oldTransaction, type);
                  return result;
               }
               finally
               {
                  tm.suspend();
               }
            }
            case MetaData.TX_NEVER:
            {
               if (oldTransaction != null)
               {
                  throw new EJBException("Transaction not allowed");
               }
               return invokeNext(invocation, false);
            }
            default:
                log.error("Unknown TX attribute "+transType+" for method"+invocation.getMethod());
         }
      }
      finally
      {
         // IN case we had a Tx associated with the thread reassociate
         if (threadTx != null)
            tm.resume(threadTx);
      }

      return null;
   }

   private int startTransaction(final Invocation invocation) throws Exception
   {
      // Get the old timeout and set any new timeout
      int oldTimeout = -1;
      if (tm instanceof TransactionTimeoutConfiguration)
      {
         oldTimeout = ((TransactionTimeoutConfiguration) tm).getTransactionTimeout();
         int newTimeout = container.getBeanMetaData().getTransactionTimeout(invocation.getMethod());
         tm.setTransactionTimeout(newTimeout);
      }
      tm.begin();
      return oldTimeout;
   }

   private void endTransaction(final Invocation invocation, final Transaction tx, final Transaction oldTx, final int oldTimeout) 
      throws TransactionRolledbackException, SystemException
   {
      // Assert the correct transaction association
      Transaction current = tm.getTransaction();
      if ((tx == null && current != null) || tx.equals(current) == false)
         throw new IllegalStateException("Wrong transaction association: expected " + tx + " was " + current);

      try
      {
         // Marked rollback
         if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK)
         {
            tx.rollback();
         }
         else
         {
            // Commit tx
            // This will happen if
            // a) everything goes well
            // b) app. exception was thrown
            tx.commit();
         }
      }
      catch (RollbackException e)
      {
         throwJBossException(e, invocation.getType());
      }
      catch (HeuristicMixedException e)
      {
         throwJBossException(e, invocation.getType());
      }
      catch (HeuristicRollbackException e)
      {
         throwJBossException(e, invocation.getType());
      }
      catch (SystemException e)
      {
         throwJBossException(e, invocation.getType());
      }
      catch (IllegalStateException e)
      {
         throwJBossException(e, invocation.getType());
      }
      finally
      {
         // reassociate the oldTransaction with the Invocation (even null)
         invocation.setTransaction(oldTx);
         // Always drop thread association even if committing or
         // rolling back the newTransaction because not all TMs
         // will drop thread associations when commit() or rollback()
         // are called through tx itself (see JTA spec that seems to
         // indicate that thread assoc is required to be dropped only
         // when commit() and rollback() are called through TransactionManager
         // interface)
         //tx has committed, so we can't throw txRolledbackException.
         tm.suspend();
         // Reset the transaction timeout (unless we didn't set it)
         if (oldTimeout != -1)
            tm.setTransactionTimeout(oldTimeout);
      }
   }


   // Protected  ----------------------------------------------------

   /**
    * Rethrow the exception as a rollback or rollback local
    *
    * @param e the exception
    * @param type the invocation type
    */
   protected void throwJBossException(Exception e, InvocationType type)
      throws TransactionRolledbackException
   {
      // Unwrap a nested exception if possible.  There is no
      // point in the extra wrapping, and the EJB spec should have
      // just used javax.transaction exceptions
      if (e instanceof NestedException)
         {
            NestedException rollback = (NestedException) e;
            if(rollback.getCause() instanceof Exception)
            {
               e = (Exception) rollback.getCause();
            }
         }
         if (type == InvocationType.LOCAL
             || type == InvocationType.LOCALHOME)
         {
            throw new JBossTransactionRolledbackLocalException(e);
         }
         else
         {
            throw new JBossTransactionRolledbackException(e);
         }
   }

   /**
    * The application has not thrown an exception, but...
    * When exception-on-rollback is true,
    * check whether the transaction is not active.
    * If it did not throw an exception anyway.
    * 
    * @param tx the transaction
    * @param type the invocation type
    * @throws TransactionRolledbackException if transaction is no longer active
    */
   protected void checkTransactionStatus(Transaction tx, InvocationType type)
      throws TransactionRolledbackException
   {
      if (exceptionRollback)
      {
         if (log.isTraceEnabled())
            log.trace("No exception from ejb, checking transaction status: " + tx);
         int status = Status.STATUS_UNKNOWN;
         try
         {
            status = tx.getStatus();
         }
         catch (Throwable t)
         {
            log.debug("Ignored error trying to retrieve transaction status", t);
         }
         if (status != Status.STATUS_ACTIVE)
         {
            Exception e = new Exception("Transaction cannot be committed (probably transaction timeout): " + tx);
            throwJBossException(e, type);
         }
      }
   }
   
   // Inner classes -------------------------------------------------

   // Monitorable implementation ------------------------------------
   public void sample(Object s)
   {
      // Just here to because Monitorable request it but will be removed soon
   }
   public Map retrieveStatistic()
   {
      return null;
   }
   public void resetStatistic()
   {
   }
}
