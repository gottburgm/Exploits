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
package org.jboss.ejb;

import java.rmi.RemoteException;
import java.security.Identity;
import java.security.Principal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.TimerService;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContextException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.jboss.logging.Logger;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SecurityRoleRefMetaData;
import org.jboss.security.RealmMapping;
import org.jboss.security.SecurityContext;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.javaee.SecurityHelperFactory;
import org.jboss.security.javaee.SecurityRoleRef;
import org.jboss.tm.TransactionTimeoutConfiguration;
import org.jboss.tm.TxUtils;
import org.jboss.tm.usertx.client.ServerVMClientUserTransaction;

//$Id: EnterpriseContext.java 110102 2010-12-21 17:08:42Z bmaxwell $

/**
 * The EnterpriseContext is used to associate EJB instances with
 * metadata about it.
 *
 * @see StatefulSessionEnterpriseContext
 * @see StatelessSessionEnterpriseContext
 * @see EntityEnterpriseContext
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:juha@jboss.org">Juha Lindfors</a>
 * @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 * @author <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @author <a href="mailto:anil.saldhana@jboss.org">Anil Saldhana</a>
 * @version $Revision: 110102 $
 */
public abstract class EnterpriseContext
   implements AllowedOperationsFlags
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** Instance logger. */
   protected static Logger log = Logger.getLogger(EnterpriseContext.class);

   /** The EJB instance */
   Object instance;

   /** The container using this context */
   Container con;

   /**
    * Set to the synchronization currently associated with this context.
    * May be null
    */
   Synchronization synch;

   /** The transaction associated with the instance */
   Transaction transaction;

   /** The principal associated with the call */
   private Principal principal;

   /** The principal for the bean associated with the call */
   private Principal beanPrincipal;

   /** Only StatelessSession beans have no Id, stateful and entity do */
   Object id;

   /** The instance is being used.  This locks it's state */
   int locked = 0;

   /** The instance is used in a transaction, synchronized methods on the tx */
   Object txLock = new Object();

   /**
    * Holds one of the IN_METHOD constants, to indicate that we are in an ejb method
    * According to the EJB2.1 spec not all context methods can be accessed at all times
    * For example ctx.getPrimaryKey() should throw an IllegalStateException when called from within ejbCreate()
    */
   private Stack inMethodStack = new Stack();

   // Static --------------------------------------------------------
   //Registration for CachedConnectionManager so our UserTx can notify
   //on tx started.
   private static ServerVMClientUserTransaction.UserTransactionStartedListener tsl;

   /**
    * The <code>setUserTransactionStartedListener</code> method is called by
    * CachedConnectionManager on start and stop.  The tsl is notified on
    * UserTransaction.begin so it (the CachedConnectionManager) can enroll
    * connections that are already checked out.
    *
    * @param newTsl a <code>ServerVMClientUserTransaction.UserTransactionStartedListener</code> value
    */
   public static void setUserTransactionStartedListener(ServerVMClientUserTransaction.UserTransactionStartedListener newTsl)
   {
      tsl = newTsl;
   }

   // Constructors --------------------------------------------------

   public EnterpriseContext(Object instance, Container con)
   {
      this.instance = instance;
      this.con = con;
   }

   // Public --------------------------------------------------------

   public Object getInstance()
   {
      return instance;
   }

   /**
    * Gets the container that manages the wrapped bean.
    */
   public Container getContainer()
   {
      return con;
   }

   public abstract void discard()
   throws RemoteException;

   /**
    * Get the EJBContext object
    */
   public abstract EJBContext getEJBContext();

   public void setId(Object id)
   {
      this.id = id;
   }

   public Object getId()
   {
      return id;
   }

   public Object getTxLock()
   {
      return txLock;
   }

   public void setTransaction(Transaction transaction)
   {
      // DEBUG log.debug("EnterpriseContext.setTransaction "+((transaction == null) ? "null" : Integer.toString(transaction.hashCode())));
      this.transaction = transaction;
   }

   public Transaction getTransaction()
   {
      return transaction;
   }

   public void setPrincipal(Principal principal)
   {
      this.principal = principal;
      /* Clear the bean principal used for getCallerPrincipal and synch with the
      new call principal
      */
      this.beanPrincipal = null;
      if( con.getSecurityManager() != null )
         this.beanPrincipal = getCallerPrincipal();
   } 

   public void lock()
   {
      locked++;
      //new Exception().printStackTrace();
      //DEBUG log.debug("EnterpriseContext.lock() "+hashCode()+" "+locked);
   }

   public void unlock()
   {

      // release a lock
      locked--;

      //new Exception().printStackTrace();
      if (locked < 0)
      {
         // new Exception().printStackTrace();
         log.error("locked < 0", new Throwable());
      }

      //DEBUG log.debug("EnterpriseContext.unlock() "+hashCode()+" "+locked);
   }

   public boolean isLocked()
   {

      //DEBUG log.debug("EnterpriseContext.isLocked() "+hashCode()+" at "+locked);
      return locked != 0;
   }

   public Principal getCallerPrincipal()
   {
      EJBContextImpl ctxImpl = (EJBContextImpl) getEJBContext();
      return ctxImpl.getCallerPrincipalInternal();
   }

   /**
    * before reusing this context we clear it of previous state called
    * by pool.free()
    */
   public void clear()
   {
      this.id = null;
      this.locked = 0;
      this.principal = null;
      this.beanPrincipal = null;
      this.synch = null;
      this.transaction = null;
      this.inMethodStack.clear();
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   protected boolean isContainerManagedTx()
   {
      BeanMetaData md = con.getBeanMetaData();
      return md.isContainerManagedTx();
   }

   protected boolean isUserManagedTx()
   {
      BeanMetaData md = con.getBeanMetaData();
      return md.isContainerManagedTx() == false;
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

   protected class EJBContextImpl implements EJBContext
   {
      /**
       *  A per-bean instance UserTransaction instance cached after the
       *  first call to <code>getUserTransaction()</code>.
       */
      private UserTransactionImpl userTransaction = null;

      private InitialContext ctx;


      private InitialContext getContext()
      {
          if (ctx==null)
          {
              try
              {
                 ctx = new InitialContext();
              }
              catch (NamingException e)
              {
                 throw new RuntimeException(e);
              }
          }

          return ctx;
      }

      protected EJBContextImpl()
      {
      }

      public Object lookup(String name)
      {
         try
         {
            return getContext().lookup(name);
         }
         catch (NamingException ignored)
         {
         }
         return null;
      }

      /**
       * @deprecated
       */
      public Identity getCallerIdentity()
      {
         throw new EJBException("Deprecated");
      }

      public TimerService getTimerService() throws IllegalStateException
      {
         return getContainer().getTimerService(null);
      }

      /**
       * Get the Principal for the current caller. This method
       * cannot return null according to the ejb-spec.
       */
      public Principal getCallerPrincipal()
      {
         return getCallerPrincipalInternal();
      }

      /**
       * The implementation of getCallerPrincipal()
       * @return the caller principal
       */
      Principal getCallerPrincipalInternal()
      {
         if( beanPrincipal == null )
         {    
            RealmMapping rm = con.getRealmMapping(); 
            SecurityContext sc = SecurityActions.getSecurityContext();
            Principal caller = null;
            try
            {
               caller = SecurityHelperFactory.getEJBAuthorizationHelper(sc).getCallerPrincipal();
            }
            catch (Exception e)
            {
               log.error("Error getting callerPrincipal for " + con.getBeanClass(),e);
            }
            
            /* Apply any domain caller mapping. This should really only be
            done for non-run-as callers.
            */
            if (rm != null)
               caller = rm.getPrincipal(caller);
             
            if( caller == null )
            {
               /* Try the incoming request principal. This is needed if a client
               clears the current caller association and and an interceptor calls
               getCallerPrincipal as the call stack unwinds.
               */
               if( principal != null )
               {
                 if( rm != null )
                    caller = rm.getPrincipal(principal);
                 else
                    caller = principal;
               }
               // Check for an unauthenticated principal value
               else
               {
                  ApplicationMetaData appMetaData = con.getBeanMetaData().getApplicationMetaData();
                  String name = appMetaData.getUnauthenticatedPrincipal();
                  if (name != null)
                     caller = new SimplePrincipal(name);
               }
            }
   
            if( caller == null )
            {
               throw new IllegalStateException("No valid security context for the caller identity");
            }
            /* Save caller as the beanPrincipal for reuse if getCallerPrincipal is called as the
               stack unwinds. An example of where this would occur is the cmp2 audit layer.
            */
            beanPrincipal = caller;
         }
         return beanPrincipal;
      }

      public EJBHome getEJBHome()
      {
         EJBProxyFactory proxyFactory = con.getProxyFactory();
         if (proxyFactory == null)
            throw new IllegalStateException("No remote home defined.");

         return (EJBHome) proxyFactory.getEJBHome();
      }

      public EJBLocalHome getEJBLocalHome()
      {
         if (con.getLocalHomeClass() == null)
            throw new IllegalStateException("No local home defined.");

         if (con instanceof EntityContainer)
            return ((EntityContainer) con).getLocalProxyFactory().getEJBLocalHome();
         else if (con instanceof StatelessSessionContainer)
            return ((StatelessSessionContainer) con).getLocalProxyFactory().getEJBLocalHome();
         else if (con instanceof StatefulSessionContainer)
            return ((StatefulSessionContainer) con).getLocalProxyFactory().getEJBLocalHome();

         // Should never get here
         throw new EJBException("No EJBLocalHome available (BUG!)");
      }

      /**
       * @deprecated
       */
      public Properties getEnvironment()
      {
         throw new EJBException("Deprecated");
      }

      public boolean getRollbackOnly()
      {
         // EJB1.1 11.6.1: Must throw IllegalStateException if BMT
         if (con.getBeanMetaData().isBeanManagedTx())
            throw new IllegalStateException("getRollbackOnly() not allowed for BMT beans.");

         try
         {
            TransactionManager tm = con.getTransactionManager();

            // The getRollbackOnly and setRollBackOnly method of the SessionContext interface should be used
            // only in the session bean methods that execute in the context of a transaction.
            if (tm.getTransaction() == null)
               throw new IllegalStateException("getRollbackOnly() not allowed without a transaction.");

            // JBAS-3847, consider an asynchronous rollback due to timeout
            int status = tm.getStatus();
            return TxUtils.isRollback(status);
         }
         catch (SystemException e)
         {
            log.warn("failed to get tx manager status; ignoring", e);
            return true;
         }
      }

      public void setRollbackOnly()
      {
         // EJB1.1 11.6.1: Must throw IllegalStateException if BMT
         if (con.getBeanMetaData().isBeanManagedTx())
            throw new IllegalStateException("setRollbackOnly() not allowed for BMT beans.");

         try
         {
            TransactionManager tm = con.getTransactionManager();

            // The getRollbackOnly and setRollBackOnly method of the SessionContext interface should be used
            // only in the session bean methods that execute in the context of a transaction.
            if (tm.getTransaction() == null)
               throw new IllegalStateException("setRollbackOnly() not allowed without a transaction.");

            tm.setRollbackOnly();
         }
         catch (SystemException e)
         {
            log.warn("failed to set rollback only; ignoring", e);
         }
      }

      /**
       * @deprecated
       */
      public boolean isCallerInRole(Identity id)
      {
         throw new EJBException("Deprecated");
      }

      /**
       * Checks if the current caller has a given role.
       * The current caller is either the principal associated with the method invocation
       * or the current run-as principal.
       */
      public boolean isCallerInRole(String roleName)
      {    
         Container container = getContainer();
         //Generate the SecurityRoleRef set
         Iterator<SecurityRoleRefMetaData> it = container.getBeanMetaData().getSecurityRoleReferences();
         Set<SecurityRoleRef> securityRoleRefs = new HashSet<SecurityRoleRef>();
         while(it.hasNext())
         {
            SecurityRoleRefMetaData meta = (SecurityRoleRefMetaData) it.next();
            securityRoleRefs.add(new SecurityRoleRef(meta.getName(), meta.getLink(),meta.getDescription()));
         } 
         //Get the context subject
         Subject contextSubject = null;
         try
         {
            contextSubject = SecurityActions.getContextSubject();
            if(contextSubject == null)
               log.error("Subject is null for isCallerInRole Check with role="+roleName); 
         }
         catch (PolicyContextException pe)
         {
           if(log.isTraceEnabled())
              log.trace("PolicyContextException in getting caller subject:",pe);
         }
  
         SecurityContext sc = SecurityActions.getSecurityContext(); 
         String ejbName = container.getBeanMetaData().getEjbName();
         
         try
         {
            return SecurityActions.isCallerInRole(sc, roleName, 
                                          ejbName, principal, contextSubject, 
                                          container.getJaccContextID(), securityRoleRefs);
         }
         catch (Exception e)
         {
            log.error("isCallerInRole("+ roleName+") had exception:",e);
         }
         return false; 
      }

      public UserTransaction getUserTransaction()
      {
         if (userTransaction == null)
         {
            if (isContainerManagedTx())
            {
               throw new IllegalStateException
               ("CMT beans are not allowed to get a UserTransaction");
            }

            userTransaction = new UserTransactionImpl();
         }

         return userTransaction;
      } 
   }

   // Inner classes -------------------------------------------------

   protected class UserTransactionImpl
   implements UserTransaction
   {
      /** Timeout value in seconds for new transactions started by this bean instance. */
      private int timeout = 0;

      /** Whether trace is enabled */
      boolean trace;

      public UserTransactionImpl()
      {
         trace = log.isTraceEnabled();
         if (trace)
            log.trace("new UserTx: " + this);
      }

      public void begin()
      throws NotSupportedException, SystemException
      {
         TransactionManager tm = con.getTransactionManager();

         int oldTimeout = -1;
         if (tm instanceof TransactionTimeoutConfiguration)
            oldTimeout = ((TransactionTimeoutConfiguration) tm).getTransactionTimeout();

         // Set the timeout value
         tm.setTransactionTimeout(timeout);

         try
         {
            // Start the transaction
            tm.begin();

            //notify checked out connections
            EJB2UserTransactionProvider.getSingleton().userTransactionStarted();
            if (tsl != null)
               tsl.userTransactionStarted();

            Transaction tx = tm.getTransaction();
            if (trace)
               log.trace("UserTx begin: " + tx);

            // keep track of the transaction in enterprise context for BMT
            setTransaction(tx);
         }
         finally
         {
            // Reset the transaction timeout (if we know what it was)
            if (oldTimeout != -1)
               tm.setTransactionTimeout(oldTimeout);
         }
      }

      public void commit()
      throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
      SecurityException, IllegalStateException, SystemException
      {
         TransactionManager tm = con.getTransactionManager();
         try
         {
            Transaction tx = tm.getTransaction();
            if (trace)
               log.trace("UserTx commit: " + tx);

            int status = tm.getStatus();
            tm.commit();
         }
         finally
         {
            // According to the spec, after commit and rollback was called on
            // UserTransaction, the thread is associated with no transaction.
            // Since the BMT Tx interceptor will associate and resume the tx
            // from the context with the thread that comes in
            // on a subsequent invocation, we must set the context transaction to null
            setTransaction(null);
         }
      }

      public void rollback()
      throws IllegalStateException, SecurityException, SystemException
      {
         TransactionManager tm = con.getTransactionManager();
         try
         {
            Transaction tx = tm.getTransaction();
            if (trace)
               log.trace("UserTx rollback: " + tx);
            tm.rollback();
         }
         finally
         {
            // According to the spec, after commit and rollback was called on
            // UserTransaction, the thread is associated with no transaction.
            // Since the BMT Tx interceptor will associate and resume the tx
            // from the context with the thread that comes in
            // on a subsequent invocation, we must set the context transaction to null
            setTransaction(null);
         }
      }

      public void setRollbackOnly()
      throws IllegalStateException, SystemException
      {
         TransactionManager tm = con.getTransactionManager();
         Transaction tx = tm.getTransaction();
         if (trace)
            log.trace("UserTx setRollbackOnly: " + tx);

         tm.setRollbackOnly();
      }

      public int getStatus()
      throws SystemException
      {
         TransactionManager tm = con.getTransactionManager();
         return tm.getStatus();
      }

      /**
       * Set the transaction timeout value for new transactions
       * started by this instance.
       */
      public void setTransactionTimeout(int seconds)
      throws SystemException
      {
         timeout = seconds;
      }
   }
}
