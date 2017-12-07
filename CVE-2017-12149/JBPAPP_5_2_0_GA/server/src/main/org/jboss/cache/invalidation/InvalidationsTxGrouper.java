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
package org.jboss.cache.invalidation;

import javax.transaction.Transaction;

import org.jboss.logging.Logger;
import org.jboss.tm.TransactionLocal;

import java.util.HashMap;
import javax.transaction.Synchronization;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Utility class that can be used to group invalidations in a set of
 * BatchInvalidations structure and only commit them alltogether at
 * transaction commit-time.
 * The invalidations are grouped (in this order):
 * - by transaction
 * - by InvalidationManager instance
 * - by InvalidationGroup
 * <p/>
 * This object will manage the transaction registering by itself if not
 * already done.
 * Thus, once a transaction commits, it will prepare a set of BatchInvalidation
 * collections (one for each InvalidationManager involved): on BI instance
 * for each InvalidationGroup. Then it will call the IM.batchInvalidation
 * method.
 *
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81030 $
 * @see InvalidationManagerMBean
 * @see BatchInvalidation
 * @see InvalidatorSynchronization
 */
public class InvalidationsTxGrouper
{

   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   private static final TransactionLocal synchLocal = new TransactionLocal();
   static Logger log = Logger.getLogger(InvalidationsTxGrouper.class);

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   public static void registerInvalidationSynchronization(Transaction tx, InvalidationGroup group, Serializable key)
      throws Exception
   {
      InvalidatorSynchronization synch = (InvalidatorSynchronization) synchLocal.get(tx);
      if(synch == null)
      {
         synch = new InvalidatorSynchronization(tx);
         synchLocal.set(tx, synch);
         // If there is no tx don't try to use it
         if( tx != null )
         tx.registerSynchronization(synch);
      }
      synch.addInvalidation(group, key);
      // If there is no tx call afterCompletion
      if( tx == null )
         synch.afterCompletion(javax.transaction.Status.STATUS_NO_TRANSACTION);
   }
}

class InvalidatorSynchronization
   implements Synchronization
{
   /**
    * The transaction we follow.
    */
   protected Transaction tx;

   /**
    * The context we manage.
    */
   protected HashMap ids = new HashMap();

   /**
    * Create a new isynchronization instance.
    */
   InvalidatorSynchronization(Transaction tx)
   {
      this.tx = tx;
   }

   public void addInvalidation(InvalidationGroup group, Serializable key)
   {
      InvalidationManagerMBean im = group.getInvalidationManager();

      // the grouping is (in order): by InvalidationManager, by InvalidationGroup
      //

      Map relatedInvalidationMgr;
      synchronized(ids)
      {
         relatedInvalidationMgr = (HashMap) ids.get(im);
         if(relatedInvalidationMgr == null)
         {
            relatedInvalidationMgr = new HashMap();
            ids.put(im, relatedInvalidationMgr);
         }
      }

      Set relatedInvalidations;
      synchronized(relatedInvalidationMgr)
      {
         relatedInvalidations = (HashSet) relatedInvalidationMgr.get(group);
         if(relatedInvalidations == null)
         {
            relatedInvalidations = new HashSet();
            relatedInvalidationMgr.put(group, relatedInvalidations);
         }
      }

      relatedInvalidations.add(key);
   }

   // Synchronization implementation -----------------------------

   public void beforeCompletion()
   {
   }


   public void afterCompletion(int status)
   {
      // This is an independent point of entry. We need to make sure the
      // thread is associated with the right context class loader
      //
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

      try
      {
         try
         {
            sendBatchInvalidations();
         }
         catch(Exception ex)
         {
            InvalidationsTxGrouper.log.warn("Failed sending invalidations messages", ex);
         }
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   protected void sendBatchInvalidations()
   {
      boolean trace = InvalidationsTxGrouper.log.isTraceEnabled();
      if(trace)
      {
         InvalidationsTxGrouper.log.trace("Begin sendBatchInvalidations, tx=" + tx);
      }
      // we iterate over all InvalidationManager involved
      //
      Iterator imIter = ids.keySet().iterator();
      while(imIter.hasNext())
      {
         InvalidationManagerMBean im = (InvalidationManagerMBean) imIter.next();

         // get associated groups
         //
         HashMap relatedInvalidationMgr = (HashMap) ids.get(im);

         BatchInvalidation[] bomb = new BatchInvalidation[relatedInvalidationMgr.size()];

         Iterator groupsIter = relatedInvalidationMgr.keySet().iterator();
         int i = 0;
         while(groupsIter.hasNext())
         {
            InvalidationGroup group = (InvalidationGroup) groupsIter.next();
            HashSet sourceIds = (HashSet) relatedInvalidationMgr.get(group);
            String groupName = group.getGroupName();
            if(trace)
            {
               InvalidationsTxGrouper.log.trace("Adding ids to bomb(" + groupName + "): " + sourceIds);
            }
            Serializable[] ids = new Serializable[sourceIds.size()];
            sourceIds.toArray(ids);
            BatchInvalidation batch = new BatchInvalidation(ids, groupName);

            bomb[i] = batch;

            i++;
         }

         // do the batch-invalidation for this IM
         //
         im.batchInvalidate(bomb);
      }
      if(trace)
      {
         InvalidationsTxGrouper.log.trace("End sendBatchInvalidations, tx=" + tx);
      }

      // Help the GC to remove this big structure
      //
      this.ids = null;
   }
}
