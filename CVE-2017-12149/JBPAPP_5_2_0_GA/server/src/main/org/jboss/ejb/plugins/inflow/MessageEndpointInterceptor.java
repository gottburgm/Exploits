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
package org.jboss.ejb.plugins.inflow;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.ResourceException;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.jboss.ejb.MessageDrivenContainer;
import org.jboss.invocation.Invocation;
import org.jboss.logging.Logger;
import org.jboss.proxy.Interceptor;

/**
 * Implements the application server message endpoint requirements.
 * 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 67780 $
 */
public class MessageEndpointInterceptor extends Interceptor
{
   /** The serialVersionUID */
   private static final long serialVersionUID =  -8740717288847385688L;
   
   /** The log */
   private static final Logger log = Logger.getLogger(MessageEndpointInterceptor.class);
   
   /** The key for the factory */
   public static final String MESSAGE_ENDPOINT_FACTORY = "MessageEndpoint.Factory";

   /** The key for the xa resource */
   public static final String MESSAGE_ENDPOINT_XARESOURCE = "MessageEndpoint.XAResource";
   
   /** Whether trace is enabled */
   private boolean trace = log.isTraceEnabled(); 
   
   /** Cached version of our proxy string */
   private String cachedProxyString = null;
   
   /** Whether this proxy has been released */
   protected AtomicBoolean released = new AtomicBoolean(false);
   
   /** Whether we have delivered a message */
   protected AtomicBoolean delivered = new AtomicBoolean(false);
   
   /** The in use thread */
   protected Thread inUseThread = null;
   
   /** The old classloader of the thread */
   protected ClassLoader oldClassLoader = null;
   
   /** Any transaction we started */
   protected Transaction transaction = null;
   
   /** Any suspended transaction */
   protected Transaction suspended = null;

   /** The beforeDeliveryInvoked used to identify sequence of before/after invocation*/
   protected boolean beforeDeliveryInvoked = false;
   
   /** The message endpoint factory */
   private JBossMessageEndpointFactory endpointFactory;
   
   public MessageEndpointInterceptor()
   {
   }

   public Object invoke(Invocation mi) throws Throwable
   {
      // Are we still useable?
      if (released.get())
         throw new IllegalStateException("This message endpoint + " + getProxyString(mi) + " has been released");

      // Concurrent invocation?
      synchronized (this)
      {
         Thread currentThread = Thread.currentThread();
         if (inUseThread != null && inUseThread.equals(currentThread) == false)
            throw new IllegalStateException("This message endpoint + " + getProxyString(mi) + " is already in use by another thread " + inUseThread);
         inUseThread = currentThread;
      }
      
      String method = mi.getMethod().getName();
      if (trace)
         log.trace("MessageEndpoint " + getProxyString(mi) + " in use by " + method + " " + inUseThread);
      
      // Which operation?
      if (method.equals("release"))
      {
         release(mi);
         return null;
      }
      else if (method.equals("beforeDelivery"))
      {
         before(mi);
         return null;
      }
      else if (method.equals("afterDelivery"))
      {
         after(mi);
         return null;
      }
      else
         return delivery(mi);
   }
   
   /**
    * Release this message endpoint.
    * 
    * @param mi the invocation
    * @throws Throwable for any error
    */
   protected void release(Invocation mi) throws Throwable
   {
      // We are now released
      released.set(true);

      if (trace)
         log.trace("MessageEndpoint " + getProxyString(mi) + " released");
      
      // Tidyup any outstanding delivery
      if (getOldClassLoader() != null)
      {
         try
         {
            finish("release", mi, false);
         }
         catch (Throwable t)
         {
            log.warn("Error in release ", t);
         }
      }
   }
   
   /**
    * Before delivery processing.
    * 
    * @param mi the invocation
    * @throws Throwable for any error
    */
   protected void before(Invocation mi) throws Throwable
   {
      // Called out of sequence
      if (getBeforeDeliveryInvoke())
         throw new IllegalStateException("Missing afterDelivery from the previous beforeDelivery for message endpoint " + getProxyString(mi));

      // Set the classloader
      MessageDrivenContainer container = getContainer(mi);
      synchronized (this)
      {
         oldClassLoader = GetTCLAction.getContextClassLoader(inUseThread);
         SetTCLAction.setContextClassLoader(inUseThread, container.getClassLoader());
      }
      if (trace)
         log.trace("MessageEndpoint " + getProxyString(mi) + " set context classloader to " + container.getClassLoader());

      // start any transaction
      try
      {
         startTransaction("beforeDelivery", mi, container);
         setBeforeDeliveryInvoke(true);
      }
      catch (Throwable t)
      {
         setBeforeDeliveryInvoke(false);
         resetContextClassLoader(mi);
         throw new ResourceException(t);
      }
   }
   
   /**
    * After delivery processing.
    * 
    * @param mi the invocation
    * @throws Throwable for any error
    */
   protected void after(Invocation mi) throws Throwable
   {
      // Called out of sequence
      if(!getBeforeDeliveryInvoke())
      {
         throw new IllegalStateException("afterDelivery without a previous beforeDelivery for message endpoint " + getProxyString(mi));
         
      }

      // Finish this delivery committing if we can
      try
      {
         finish("afterDelivery", mi, true);
      }
      catch (Throwable t)
      {
         throw new ResourceException(t);
      
      }
   }
   
   /**
    * Delivery.
    * 
    * @param mi the invocation
    * @return the result of the delivery
    * @throws Throwable for any error
    */
   protected Object delivery(Invocation mi) throws Throwable
   {
      // Have we already delivered a message?
      if (delivered.get())
         throw new IllegalStateException("Multiple message delivery between before and after delivery is not allowed for message endpoint " + getProxyString(mi));

      if (trace)
         log.trace("MessageEndpoint " + getProxyString(mi) + " delivering");
      
      // Mark delivery if beforeDelivery was invoked
      if (getOldClassLoader() != null)
         delivered.set(true);
     
      MessageDrivenContainer container = getContainer(mi);
      boolean commit = true;
      try
      {
         // Check for starting a transaction
         if (getOldClassLoader() == null)
            startTransaction("delivery", mi, container);
         return getNext().invoke(mi);
      }
      catch (Throwable t)
      {
         if (trace)
            log.trace("MessageEndpoint " + getProxyString(mi) + " delivery error", t);
         if (t instanceof Error || t instanceof RuntimeException)
         {
            Transaction transaction = getTransaction();
            if (transaction != null)
               transaction.setRollbackOnly();
            commit = false;
         }
         throw t;
      }
      finally
      {
         // No before/after delivery, end any transaction and release the lock
         if (getOldClassLoader() == null)
         {
            try
            {
               // Finish any transaction we started
               endTransaction(mi, commit);
            }
            finally
            {
               releaseThreadLock(mi);
            }
         }
      }
   }
   
   /**
    * Finish the current delivery
    * 
    * @param context the lifecycle method
    * @param mi the invocation
    * @param commit whether to commit
    * @throws Throwable for any error
    */
   protected void finish(String context, Invocation mi, boolean commit) throws Throwable
   {
      try
      {
         endTransaction(mi, commit);
      }
      finally
      {
         setBeforeDeliveryInvoke(false);
         // Reset delivered flag
         delivered.set(false);
         // Change back to the original context classloader
         resetContextClassLoader(mi);
         // We no longer hold the lock
         releaseThreadLock(mi);
      }
   }

   /**
    * Start a transaction
    *  
    * @param context the lifecycle method
    * @param mi the invocation
    * @param container the container
    * @throws Throwable for any error
    */
   protected void startTransaction(String context, Invocation mi, MessageDrivenContainer container) throws Throwable
   {
      // Get any passed resource
      XAResource resource = (XAResource) mi.getInvocationContext().getValue(MESSAGE_ENDPOINT_XARESOURCE);

      Method method = null;

      // Normal delivery      
      if ("delivery".equals(context))
         method = mi.getMethod();
      // Before delivery
      else
         method = (Method) mi.getArguments()[0];

      // Is the delivery transacted?
      boolean isTransacted = getMessageEndpointFactory(mi).isDeliveryTransacted(method);

      if (trace)
         log.trace("MessageEndpoint " + getProxyString(mi) + " " + context + " method=" + method + " xaResource=" + resource + " transacted=" + isTransacted);

      // Get the transaction status
      TransactionManager tm = container.getTransactionManager();
      Transaction tx = tm.suspend();
      synchronized (this)
      {
         suspended = tx;
      }

      if (trace)
         log.trace("MessageEndpoint " + getProxyString(mi) + " " + context + " currentTx=" + suspended);

      // Delivery is transacted
      if (isTransacted)
      {
         // No transaction means we start a new transaction and enlist the resource
         if (suspended == null)
         {
            tm.begin();
            tx = tm.getTransaction();
            synchronized (this)
            {
               transaction = tx;
            }
            if (trace)
               log.trace("MessageEndpoint " + getProxyString(mi) + " started transaction=" + transaction);
      
            // Enlist the XAResource in the transaction
            if (resource != null)
            {
               transaction.enlistResource(resource);
               if (trace)
                  log.trace("MessageEndpoint " + getProxyString(mi) + " enlisted=" + resource);
            }
         }
         else
         {
            // If there is already a transaction we ignore the XAResource (by spec 12.5.9)
            try
            {
               tm.resume(suspended);
            }
            finally
            {
               synchronized (this)
               {
                  suspended = null;
               }
               if (trace)
                  log.trace("MessageEndpoint " + getProxyString(mi) + " transaction=" + suspended + " already active, IGNORED=" + resource);
            }
         }
      }
   }
   
   /**
    * End the transaction
    * 
    * @param mi the invocation
    * @param commit whether to try to commit
    * @throws Throwable for any error
    */
   protected void endTransaction(Invocation mi, boolean commit) throws Throwable
   {
      TransactionManager tm = null;
      Transaction currentTx = null;
      try
      {
         // If we started the transaction, commit it
         Transaction transaction = getTransaction();
         if (transaction != null)
         {
            tm = getContainer(mi).getTransactionManager();
            currentTx = tm.getTransaction();
            
            // Suspend any bad transaction - there is bug somewhere, but we will try to tidy things up
            if (currentTx != null && currentTx.equals(transaction) == false)
            {
               log.warn("Current transaction " + currentTx + " is not the expected transaction.");
               tm.suspend();
               tm.resume(transaction);
            }
            else
            {
               // We have the correct transaction
               currentTx = null;
            }
            
            // Commit or rollback depending on the status
            if (commit == false || transaction.getStatus() == Status.STATUS_MARKED_ROLLBACK)
            {
               if (trace)
                  log.trace("MessageEndpoint " + getProxyString(mi) + " rollback");
               tm.rollback();
            }
            else
            {
               if (trace)
                  log.trace("MessageEndpoint " + getProxyString(mi) + " commit");
               tm.commit();
            }
         }

         // If we suspended the incoming transaction, resume it
         Transaction suspended = getSuspended();
         if (suspended != null)
         {
            try
            {
               tm = getContainer(mi).getTransactionManager();
               tm.resume(suspended);
            }
            finally
            {
               synchronized (this)
               {
                  this.suspended = null;
               }
            }
         }
      }
      finally
      {
         synchronized (this)
         {
            transaction = null;
         }
      
         // Resume any suspended transaction
         if (currentTx != null)
         {
            try
            {
               tm.resume(currentTx);
            }
            catch (Throwable t)
            {
               log.warn("MessageEndpoint " + getProxyString(mi) + " failed to resume old transaction " + currentTx);
            }
         }
      }
   }
   
   /**
    * Reset the context classloader
    * 
    * @param mi the invocation
    */
   protected void resetContextClassLoader(Invocation mi)
   {
      synchronized (this)
      {
         if (trace)
            log.trace("MessageEndpoint " + getProxyString(mi) + " reset classloader " + oldClassLoader);
         SetTCLAction.setContextClassLoader(inUseThread, oldClassLoader);
         oldClassLoader = null;
      }
   }
   
   protected void setBeforeDeliveryInvoke(boolean bdi)
   {
      this.beforeDeliveryInvoked = bdi;
      
   }
   
   protected boolean getBeforeDeliveryInvoke()
   {
      return this.beforeDeliveryInvoked;
      
   }
   /**
    * Release the thread lock
    * 
    * @param mi the invocation
    */
   protected void releaseThreadLock(Invocation mi)
   {
      synchronized (this)
      {
         if (trace)
            log.trace("MessageEndpoint " + getProxyString(mi) + " no longer in use by " + inUseThread);
         inUseThread = null;
      }
   }
   
   /**
    * Get our proxy's string value.
    * 
    * @param mi the invocation
    * @return the string
    */
   protected String getProxyString(Invocation mi)
   {
      if (cachedProxyString == null)
         cachedProxyString = mi.getInvocationContext().getCacheId().toString();
      return cachedProxyString;
   }

   /**
    * Get the message endpoint factory
    *
    * @param mi the invocation
    * @return the message endpoint factory
    */
   protected JBossMessageEndpointFactory getMessageEndpointFactory(Invocation mi)
   {
      if (endpointFactory == null)
         endpointFactory = (JBossMessageEndpointFactory) mi.getInvocationContext().getValue(MESSAGE_ENDPOINT_FACTORY);
      if (endpointFactory == null)
         throw new IllegalStateException("No message endpoint factory in " + mi.getInvocationContext().context);
      return endpointFactory;
   }
   
   /**
    * Get the container
    *
    * @param mi the invocation
    * @return the container
    */
   protected MessageDrivenContainer getContainer(Invocation mi)
   {
      JBossMessageEndpointFactory messageEndpointFactory = getMessageEndpointFactory(mi);
      MessageDrivenContainer container = messageEndpointFactory.getContainer();
      if (container == null)
         throw new IllegalStateException("No container associated with message endpoint factory: " + messageEndpointFactory.getServiceName());
      return container;
   }

   protected synchronized ClassLoader getOldClassLoader()
   {
      return oldClassLoader;
   }

   protected synchronized Transaction getTransaction()
   {
      return transaction;
   }

   protected synchronized Transaction getSuspended()
   {
      return suspended;
   }
}
