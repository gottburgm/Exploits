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


import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.metadata.ConfigurationMetaData;

/**
 * @deprecated this interceptor was used with Instance Per Transaction containers which do not use a global cache
 * but cache instances per transaction and always passivate instances at commit time (commit option C).
 * The only difference from the EntityInstanceInterceptor is that it uses specific instance Synchronization implementation
 * which always passivates the instance at commit time which is equivalent to commit option C in standard container.
 * Now, the differences between IPT and standard container are:
 * <ul>
 *    <li>org.jboss.ejb.plugins.PerTxEntityInstanceCache as the cache implementation;</li>
 *    <li>NoLock as the locking policy;</li>
 *    <li>empty container-cache-conf element.</li>
 * </ul>
 * (alex@jboss.org)
 * 
 * The role of this interceptor is to synchronize the state of the cache with
 * the underlying storage.  It does this with the ejbLoad and ejbStore
 * semantics of the EJB specification.  In the presence of a transaction this
 * is triggered by transaction demarcation. It registers a callback with the
 * underlying transaction monitor through the JTA interfaces.  If there is no
 * transaction the policy is to store state upon returning from invocation.
 * The synchronization polices A,B,C of the specification are taken care of
 * here.
 *
 * <p><b>WARNING: critical code</b>, get approval from senior developers
 *    before changing.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 81030 $
 */
public class EntityMultiInstanceSynchronizationInterceptor
        extends EntitySynchronizationInterceptor
{
   public void create()
           throws Exception
   {
      super.create();
   }

   public void start()
   {
      // This is in here so that EntityMultiInstanceInterceptor can avoid doing a lock.sync().  
      // 
      if (!container.getLockManager().lockClass.equals(org.jboss.ejb.plugins.lock.NoLock.class)
          && !container.getLockManager().lockClass.equals(org.jboss.ejb.plugins.lock.JDBCOptimisticLock.class)
          && !container.getLockManager().lockClass.equals(org.jboss.ejb.plugins.lock.MethodOnlyEJBLock.class)
          )
      {
         throw new IllegalStateException("the <locking-policy> must be org.jboss.ejb.plugins.lock.NoLock, JDBCOptimisticLock, or MethodOnlyEJBLock for Instance Per Transaction:"
                                          + container.getLockManager().lockClass.getName());
      }
   }

   protected Synchronization createSynchronization(Transaction tx, EntityEnterpriseContext ctx)
   {
      return new MultiInstanceSynchronization(tx, ctx);
   }
   // Protected  ----------------------------------------------------

   // Inner classes -------------------------------------------------

   protected class MultiInstanceSynchronization implements Synchronization
   {
      /**
       *  The transaction we follow.
       */
      protected Transaction tx;

      /**
       *  The context we manage.
       */
      protected EntityEnterpriseContext ctx;

      /**
       *  Create a new instance synchronization instance.
       */
      MultiInstanceSynchronization(Transaction tx, EntityEnterpriseContext ctx)
      {
         this.tx = tx;
         this.ctx = ctx;
      }

      // Synchronization implementation -----------------------------

      public void beforeCompletion()
      {
         //synchronization is handled by GlobalTxEntityMap.
      }

      public void afterCompletion(int status)
      {
         boolean trace = log.isTraceEnabled();

         // This is an independent point of entry. We need to make sure the
         // thread is associated with the right context class loader
         ClassLoader oldCl = SecurityActions.getContextClassLoader();
         SecurityActions.setContextClassLoader(container.getClassLoader());
         container.pushENC();
         ctx.hasTxSynchronization(false);
         ctx.setTransaction(null);
         try
         {
            try
            {
               // If rolled back -> invalidate instance
               if (status != Status.STATUS_ROLLEDBACK)
               {
                  switch (commitOption)
                  {
                     // Keep instance cached after tx commit
                     case ConfigurationMetaData.A_COMMIT_OPTION:
                        throw new IllegalStateException("Commit option A not allowed with this Interceptor");
                        // Keep instance active, but invalidate state
                     case ConfigurationMetaData.B_COMMIT_OPTION:
                        break;
                        // Invalidate everything AND Passivate instance
                     case ConfigurationMetaData.C_COMMIT_OPTION:
                        break;
                     case ConfigurationMetaData.D_COMMIT_OPTION:
                        throw new IllegalStateException("Commit option D not allowed with this Interceptor");
                  }
               }
               try
               {
                  if (ctx.getId() != null)
                     container.getPersistenceManager().passivateEntity(ctx);
               }
               catch (Exception ignored)
               {
               }
               container.getInstancePool().free(ctx);
            }
            finally
            {
               if (trace)
                  log.trace("afterCompletion, clear tx for ctx=" + ctx + ", tx=" + tx);

            }
         } // synchronized(lock)
         finally
         {
            container.popENC();
            SecurityActions.setContextClassLoader(oldCl);
         }
      }

   }

}
