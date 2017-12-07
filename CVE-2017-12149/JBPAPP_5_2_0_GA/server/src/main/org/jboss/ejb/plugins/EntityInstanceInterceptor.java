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

import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.transaction.Transaction;

import org.jboss.ejb.AllowedOperationsAssociation;
import org.jboss.ejb.BeanLock;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.GlobalTxEntityMap;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.InstancePool;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;
import org.jboss.util.NestedRuntimeException;

/**
 * The instance interceptors role is to acquire a context representing the
 * target object from the cache.
 *
 * <p>This particular container interceptor implements pessimistic locking on
 * the transaction that is associated with the retrieved instance.  If there is
 * a transaction associated with the target component and it is different from
 * the transaction associated with the Invocation coming in then the policy is
 * to wait for transactional commit.
 *
 * <p>We also implement serialization of calls in here (this is a spec
 * requirement). This is a fine grained notify, notifyAll mechanism. We notify
 * on ctx serialization locks and notifyAll on global transactional locks.
 *
 * <p><b>WARNING: critical code</b>, get approval from senior developers before
 * changing.
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:mkgarnek@hotmail.com">Jamie Burns</a>
 * @version $Revision: 81030 $
 */
public class EntityInstanceInterceptor
   extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
	
   // Attributes ----------------------------------------------------
	
   protected EntityContainer container;

   // Static --------------------------------------------------------

   /** A reference to {@link javax.ejb.TimedObject#ejbTimeout}. */
   protected static final Method ejbTimeout;

   static
   {
      try
      {
         ejbTimeout = TimedObject.class.getMethod("ejbTimeout", new Class[]{Timer.class});
      }
      catch (Exception e)
      {
         throw new ExceptionInInitializerError(e);
      }
   }

   // Constructors --------------------------------------------------
	
   // Public --------------------------------------------------------
	
   public void setContainer(Container container)
   {
      this.container = (EntityContainer) container;
   }

   public Container getContainer()
   {
      return container;
   }

   // Interceptor implementation --------------------------------------

   public Object invokeHome(Invocation mi)
      throws Exception
   {
      // Get context
      EntityContainer container = (EntityContainer) getContainer();
      EntityEnterpriseContext ctx = (EntityEnterpriseContext) container.getInstancePool().get();
      ctx.setTxAssociation(GlobalTxEntityMap.NOT_READY);
      InstancePool pool = container.getInstancePool();

      // Pass it to the method invocation
      mi.setEnterpriseContext(ctx);
   
      // Give it the transaction
      ctx.setTransaction(mi.getTransaction());
   
      // Set the current security information
      ctx.setPrincipal(mi.getPrincipal());

      AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_HOME);
      
      // Invoke through interceptors
      
      Object obj = null;
      Exception exception = null;

      try
      {
         obj = getNext().invokeHome(mi);
          
         // Is the context now with an identity? in which case we need to insert
         if (ctx.getId() != null)
         {
            BeanLock lock = container.getLockManager().getLock(ctx.getCacheKey());
            lock.sync(); // lock all access to BeanLock
            try
            {
               // Check there isn't a context already in the cache
               // e.g. commit-option B where the entity was
               // created then removed externally
               InstanceCache cache = container.getInstanceCache();
               cache.remove(ctx.getCacheKey());
       
               // marcf: possible race on creation and usage
               // insert instance in cache,
               cache.insert(ctx);
            }
            finally
            {
               lock.releaseSync();
               container.getLockManager().removeLockRef(ctx.getCacheKey());
            }
             
            // we are all done             
            return obj;
         }
      }
      catch (Exception e)
      {
         exception = e;
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }

      ctx.setTransaction(null);
      // EntityCreateInterceptor will access ctx if it is not null and call postCreate
      mi.setEnterpriseContext(null);
      
      // if we get to here with a null exception then our invocation is
      // just a home invocation. Return our instance to the instance pool   
      if (exception == null)
      {
         container.getInstancePool().free(ctx);
         return obj;
      }
      
      if (exception instanceof RuntimeException)
      {
         // if we get to here with a RuntimeException, we have a system exception.
         // EJB 2.1 section 18.3.1 says we need to discard our instance.
         pool.discard(ctx);
      }
      else
      {
         // if we get to here with an Exception, we have an application exception.
         // EJB 2.1 section 18.3.1 says we can keep the instance. We need to return 
         // our instance to the instance pool so we dont get a memory leak.  
         pool.free(ctx);         
      }
      
      throw exception;
   }


   public Object invoke(Invocation mi)
      throws Exception
   {
      boolean trace = log.isTraceEnabled();

      // The key
      Object key = mi.getId();

      // The context
      EntityEnterpriseContext ctx;
      try
      {
         ctx = (EntityEnterpriseContext) container.getInstanceCache().get(key);
      }
      catch (NoSuchObjectException e)
      {
         if (mi.isLocal())
            throw new NoSuchObjectLocalException(e.getMessage());
         else
            throw e;
      }
      catch (EJBException e)
      {
         throw e;
      }
      catch (RemoteException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         InvocationType type = mi.getType();
         boolean isLocal = (type == InvocationType.LOCAL || type == InvocationType.LOCALHOME);
         if (isLocal)
            throw new EJBException("Unable to get an instance from the pool/cache", e);
         else
            throw new RemoteException("Unable to get an intance from the pool/cache", e);
      }

      if (trace) log.trace("Begin invoke, key=" + key);

      // Associate transaction, in the new design the lock already has the transaction from the
      // previous interceptor

      // Don't set the transction if a read-only method.  With a read-only method, the ctx can be shared
      // between multiple transactions.
      Transaction tx = mi.getTransaction();
      if (!container.isReadOnly())
      {
         Method method = mi.getMethod();
         if (method == null ||
            !container.getBeanMetaData().isMethodReadOnly(method.getName()))
         {
            ctx.setTransaction(tx);
         }
      } 

      // Set the current security information
      ctx.setPrincipal(mi.getPrincipal());
      // Set the JACC EnterpriseBean PolicyContextHandler data
      EnterpriseBeanPolicyContextHandler.setEnterpriseBean(ctx.getInstance());

      // Set context on the method invocation
      mi.setEnterpriseContext(ctx);

      if (ejbTimeout.equals(mi.getMethod()))
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_TIMEOUT);
      else
         AllowedOperationsAssociation.pushInMethodFlag(IN_BUSINESS_METHOD);

      Throwable exceptionThrown = null;
      boolean discardContext = false;
      try
      {
         Object obj = getNext().invoke(mi);
         return obj;
      }
      catch (RemoteException e)
      {
         exceptionThrown = e;
         discardContext = true;
         throw e;
      }
      catch (RuntimeException e)
      {
         exceptionThrown = e;
         discardContext = true;
         throw e;
      }
      catch (Error e)
      {
         exceptionThrown = e;
         discardContext = true;
         throw e;
      }
      catch (Exception e)
      {
         exceptionThrown = e;
         throw e;
      }
      catch (Throwable e)
      {
         exceptionThrown = e;
         discardContext = true;
         throw new NestedRuntimeException(e);
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();

         // Make sure we clear the transaction on an error before synchronization.
         // But avoid a race with a transaction rollback on a synchronization
         // that may have moved the context onto a different transaction
         if (exceptionThrown != null && tx != null)
         {
            Transaction ctxTx = ctx.getTransaction();
            if (tx.equals(ctxTx) && ctx.hasTxSynchronization() == false)
               ctx.setTransaction(null);
         }

         // If an exception has been thrown,
         if (exceptionThrown != null &&
            // if tx, the ctx has been registered in an InstanceSynchronization.
            // that will remove the context, so we shouldn't.
            // if no synchronization then we need to do it by hand
            // But not for application exceptions
            !ctx.hasTxSynchronization() && discardContext)
         {
            // Discard instance
            // EJB 1.1 spec 12.3.1
            container.getInstanceCache().remove(key);

            if (trace) log.trace("Ending invoke, exceptionThrown, ctx=" + ctx, exceptionThrown);
         }
         else if (ctx.getId() == null)
         {
            // The key from the Invocation still identifies the right cachekey
            container.getInstanceCache().remove(key);

            if (trace) log.trace("Ending invoke, cache removal, ctx=" + ctx);
            // no more pool return
         }
         
         EnterpriseBeanPolicyContextHandler.setEnterpriseBean(null);

         if (trace) log.trace("End invoke, key=" + key + ", ctx=" + ctx);
      }// end finally
   }
}

