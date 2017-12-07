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
package org.jboss.tm.usertx.client;

import java.io.Serializable;

import java.rmi.RemoteException;

import java.util.LinkedList;
import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.NamingException;

import javax.transaction.UserTransaction;
import javax.transaction.Transaction;
import javax.transaction.Status;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;

import org.jboss.remoting.CannotConnectException;
import org.jboss.tm.TransactionPropagationContextFactory;

import org.jboss.tm.usertx.interfaces.UserTransactionSession;
import org.jboss.tm.usertx.interfaces.UserTransactionSessionFactory;
import org.jboss.logging.Logger;
import org.jboss.naming.NamingContextFactory;

/**
 * The client-side UserTransaction implementation. This will delegate all
 * UserTransaction calls to the server.
 *
 * <em>Warning:</em> This is only for stand-alone clients that do not have their
 * own transaction service. No local work is done in the context of transactions
 * started here, only work done in beans at the server. Instantiating objects of
 * this class outside the server will change the JRMP GenericProxy so that
 * outgoing calls use the propagation contexts of the transactions started
 * here.
 * @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @version $Revision: 99841 $
 */
public class ClientUserTransaction
   implements UserTransaction,
   TransactionPropagationContextFactory,
   Referenceable,
   Serializable
{
   // Static --------------------------------------------------------
   /** @since at least jboss-3.2.0 */
   private static final long serialVersionUID = 1747989355209242872L;

   /**
    * Our singleton instance.
    */
   private static ClientUserTransaction singleton = new ClientUserTransaction();

   private static final Logger log = Logger.getLogger(ClientUserTransaction.class);

   private static boolean trace = log.isTraceEnabled();   
   
   /**
    * Return a reference to the singleton instance.
    * 
    * @return the singleton
    */
   public static ClientUserTransaction getSingleton()
   {
      return singleton;
   }


   // Constructors --------------------------------------------------

   /**
    * Create a new instance.
    */
   private ClientUserTransaction()
   {
   }

   // Public --------------------------------------------------------

   //
   // implements interface UserTransaction
   //

   public void begin()
      throws NotSupportedException, SystemException
   {
      if(getStatus() != Status.STATUS_NO_TRANSACTION)
         throw new NotSupportedException("Attempt to start a nested transaction (the transaction started previously hasn't been ended yet).");

      ThreadInfo info = getThreadInfo();
      trace = log.isTraceEnabled(); // Only check for trace enabled once per transaction
      
      if (trace)
      {
         log.trace("Calling UserTransaction.begin()");
      }
      
      try
      {
         Object tpc = getSession().begin(info.getTimeout());
         info.push(tpc);
      }
      catch (SystemException e)
      {
         throw e;
      }
      catch (RemoteException e)
      {
         // destroy session gone bad.
         destroySession();
         logCauseException(e);
         throw new SystemException(e.toString());
      }
      catch (Exception e)
      {
         logCauseException(e);
         throw new SystemException(e.toString());
      }
   }

   public void commit()
      throws RollbackException,
      HeuristicMixedException,
      HeuristicRollbackException,
      SecurityException,
      IllegalStateException,
      SystemException
   {
      ThreadInfo info = getThreadInfo();
      Object tpc = info.getTpc();

      if (trace)
      {
         log.trace("Calling UserTransaction.commit(" + tpc + ")");
      }      
      
      try
      {
         getSession().commit(tpc);
      }
      catch (RollbackException e)
      {
         throw e;
      }
      catch (HeuristicMixedException e)
      {
         throw e;
      }
      catch (HeuristicRollbackException e)
      {
         throw e;
      }
      catch (SecurityException e)
      {
         throw e;
      }
      catch (SystemException e)
      {
         throw e;
      }
      catch (IllegalStateException e)
      {
         throw e;
      }
      catch (RemoteException e)
      {
         // destroy session gone bad.
         destroySession();
         logCauseException(e);
         throw new SystemException(e.toString());
      }
      catch(CannotConnectException e)
      {
         // destroy session gone bad.
         destroySession();
         logCauseException(e);
         throw new SystemException(e.toString());
      }      
      catch (Exception e)
      {
         logCauseException(e);
         throw new SystemException(e.toString());
      }
      finally
      {
         info.pop();
      }
   }

   public void rollback()
      throws SecurityException,
      IllegalStateException,
      SystemException
   {
      ThreadInfo info = getThreadInfo();
      Object tpc = info.getTpc();      

      if (trace)
      {
         log.trace("Calling UserTransaction.rollback(" + tpc + ")");
      }
      
      try
      {
         getSession().rollback(tpc);
      }
      catch (SecurityException e)
      {
         throw e;
      }
      catch (SystemException e)
      {
         throw e;
      }
      catch (IllegalStateException e)
      {
         throw e;
      }
      catch (RemoteException e)
      {
         // destroy session gone bad.
         destroySession();
         logCauseException(e);
         throw new SystemException(e.toString());
      }
      catch(CannotConnectException e)
      {
         // destroy session gone bad.
         destroySession();
         logCauseException(e);
         throw new SystemException(e.toString());
      }      
      catch (Exception e)
      {
         logCauseException(e);
         throw new SystemException(e.toString());
      }
      finally
      {
         info.pop();
      }
   }

   public void setRollbackOnly()
      throws IllegalStateException,
      SystemException
   {
      ThreadInfo info = getThreadInfo();
      Object tpc = info.getTpc();      

      if (trace)
      {
         log.trace("Calling UserTransaction.setRollbackOnly(" + tpc + ")");
      }      
      
      try
      {
         getSession().setRollbackOnly(tpc);
      }
      catch (SystemException e)
      {
         throw e;
      }
      catch (IllegalStateException e)
      {
         throw e;
      }
      catch (RemoteException e)
      {
         // destroy session gone bad.
         destroySession();
         logCauseException(e);
         throw new SystemException(e.toString());
      }
      catch (Exception e)
      {
         logCauseException(e);
         throw new SystemException(e.toString());
      }
   }

   public int getStatus()
      throws SystemException
   {
      ThreadInfo info = getThreadInfo();
      Object tpc = info.getTpc();

      if (log.isTraceEnabled())
      {
         log.trace("Calling UserTransaction.getStatus(" + tpc + ")");
      }      
      
      if (tpc == null)
      {
         return Status.STATUS_NO_TRANSACTION;
      }

      try
      {
         return getSession().getStatus(tpc);
      }
      catch (SystemException e)
      {
         throw e;
      }
      catch (RemoteException e)
      {
         // destroy session gone bad.
         destroySession();
         logCauseException(e);
         throw new SystemException(e.toString());
      }
      catch (Exception e)
      {
         logCauseException(e);
         throw new SystemException(e.toString());
      }
   }

   public void setTransactionTimeout(int seconds)
      throws SystemException
   {
      getThreadInfo().setTimeout(seconds);
   }


   //
   // implements interface TransactionPropagationContextFactory
   //

   public Object getTransactionPropagationContext()
   {
      return getThreadInfo().getTpc();
   }

   public Object getTransactionPropagationContext(Transaction tx)
   {
      // No need to implement in a stand-alone client.
      throw new InternalError("Should not have been used.");
   }
 

   //
   // implements interface Referenceable
   //

   public Reference getReference()
      throws NamingException
   {
      Reference ref = new Reference("org.jboss.tm.usertx.client.ClientUserTransaction",
         "org.jboss.tm.usertx.client.ClientUserTransactionObjectFactory",
         null);

      return ref;
   }


   // Private -------------------------------------------------------

   /**
    * The RMI remote interface to the real tx service session at the server.
    */
   private UserTransactionSession session = null;

   /**
    * Storage of per-thread information used here.
    */
   private transient ThreadLocal threadInfo = new ThreadLocal();


   /**
    * Create a new session.
    */
   private synchronized void createSession()
   {
      // Destroy any old session.
      if (session != null)
         destroySession();

      try
      {
         // Get a reference to the UT session factory.
         UserTransactionSessionFactory factory;
         Hashtable env = (Hashtable) NamingContextFactory.lastInitialContextEnv.get();
         InitialContext ctx = new InitialContext(env);
         factory = (UserTransactionSessionFactory) ctx.lookup("UserTransactionSessionFactory");
         // Call factory to get a UT session.
         session = factory.newInstance();
      }
      catch (Exception ex)
      {
         throw new RuntimeException("UT factory lookup failed", ex);
      }
   }

   /**
    * Destroy the current session.
    */
   private synchronized void destroySession()
   {
      if (session != null)
      {
         try
         {
            session.destroy();
         }
         catch (RemoteException ex)
         {
            // Ignore.
         }
         session = null;
      }
   }

   /**
    * Get the session. This will create a session, if one does not already
    * exist.
    */
   private synchronized UserTransactionSession getSession()
   {
      if (session == null)
         createSession();
      return session;
   }


   /**
    * Return the per-thread information, possibly creating it if needed.
    */
   private ThreadInfo getThreadInfo()
   {
      ThreadInfo ret = (ThreadInfo) threadInfo.get();

      if (ret == null)
      {
         ret = new ThreadInfo();
         threadInfo.set(ret);
      }
      
      if (trace)
      {
         log.trace("Thread local: " + threadInfo); 
         log.trace("Thread info holder: " + ret);
      }

      return ret;
   }

   private void logCauseException(Exception e)
   {
      if (trace)
      {
         log.trace("Logging cause exception", e);         
      }
   }

   // Inner classes -------------------------------------------------

   /**
    * Per-thread data holder class. This stores the stack of TPCs for the
    * transactions started by this thread.
    */
   private class ThreadInfo
   {
      /**
       * A stack of TPCs for transactions started by this thread. If the
       * underlying service does not support nested transactions, its size is
       * never greater than 1. Last element of the list denotes the stack top.
       */
      private LinkedList tpcStack = new LinkedList();

      /**
       * The timeout value (in seconds) for new transactions started by this
       * thread.
       */
      private int timeout = 0;

      /**
       * Override to terminate any transactions that the thread may have
       * forgotten.
       */
      protected void finalize()
         throws Throwable
      {
         if (trace)
         {
            log.trace("Tpc stack: finalize " + this);
         }
         
         try
         {
            while (!tpcStack.isEmpty())
            {
               Object tpc = getTpc();
               pop();

               try
               {
                  getSession().rollback(tpc);
               }
               catch (Exception ex)
               {
                  // ignore
               }
            }
         }
         catch (Throwable t)
         {
            // ignore
         }
         super.finalize();
      }

      /**
       * Push the TPC of a newly started transaction on the stack.
       */
      void push(Object tpc)
      {
         tpcStack.addLast(tpc);
         if (trace)
         {
            log.trace("Tpc stack: added " + this + " tpc=" + tpc);
         }
      }

      /**
       * Pop the TPC of a newly terminated transaction from the stack.
       */
      void pop()
      {
         Object tpc = tpcStack.removeLast();
         if (trace)
         {
            log.trace("Tpc stack: removed " + this + " tpc=" + tpc);
         }
      }

      /**
       * Get the TPC at the top of the stack.
       */
      Object getTpc()
      {
         Object tpc = (tpcStack.isEmpty()) ? null : tpcStack.getLast();
         if (trace)
         {
            log.trace("Tpc stack: peek " + this + " tpc=" + tpc);
         }
         return tpc;
      }

      /**
       * Return the default transaction timeout in seconds to use for new
       * transactions started by this thread. A value of <code>0</code> means
       * that a default timeout value should be used.
       */
      int getTimeout()
      {
         return timeout;
      }

      /**
       * Set the default transaction timeout in seconds to use for new
       * transactions started by this thread.
       */
      void setTimeout(int seconds)
      {
         timeout = seconds;
      }
   }

}
