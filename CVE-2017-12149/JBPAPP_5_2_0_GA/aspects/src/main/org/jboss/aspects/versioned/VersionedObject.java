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
package org.jboss.aspects.versioned;

import EDU.oswego.cs.dl.util.concurrent.FIFOSemaphore;
import org.jboss.aop.Advised;
import org.jboss.aop.util.MarshalledValue;
import org.jboss.tm.TransactionLocal;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;

/**
 *  Manages transactional versions of an object
 *  versioned object must be Serializable or Cloneable
 *
 *  @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 *  @version $Revision: 80997 $
 */
public class VersionedObject
{
   FIFOSemaphore lock = new FIFOSemaphore(1);
   TransactionLocal txLocal = new TransactionLocal();
   long currentId = 0;
   Object currentObject;
   long versionIdGenerator = 0;

   public VersionedObject(Object obj)
   {
      currentObject = obj;
   }

   public Object getVersion(Transaction tx)
   {
      if (tx == null) return currentObject;
      return txLocal.get(tx);
   }

   public Object createVersion(Transaction tx) throws Exception
   {
      lock.acquire();
      Object version = null;
      long versionId;
      try
      {
         version = new MarshalledValue(currentObject).get();
         if (version instanceof Advised)
         {
            // REVISIT: Currently share the InstanceAdvisor between versions
            Advised versionAdvised = (Advised)version;
            Advised currentAdvised = (Advised)currentObject;
            versionAdvised._setInstanceAdvisor(currentAdvised._getInstanceAdvisor());
         }
         versionId = ++versionIdGenerator;
      }
      finally
      {
         lock.release();
      }

      tx.registerSynchronization(new VersionSynchronization(tx, versionId, version));
      txLocal.set(tx, version);
      return version;
   }

   private final class VersionSynchronization implements Synchronization
   {
      long versionId;
      Object version;
      Transaction tx;

      public VersionSynchronization(Transaction tx, long versionId, Object version)
      {
         this.tx = tx;
         this.versionId = versionId;
         this.version = version;
      }
      public void beforeCompletion()
      {
         try
         {
            lock.acquire();
         }
         catch (InterruptedException ignored)
         {
            throw new RuntimeException(ignored);
         }
         if (currentId >= versionId)
         {
            lock.release();
            throw new OptimisticLockFailure();
         }
      }

      public void afterCompletion(int status)
      {
         //possible status is committed and rolledback
         if (status != Status.STATUS_ROLLEDBACK)
         {
            currentId = versionId;
            currentObject = version;
            lock.release();
         }
      }
   }
}
