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
import org.jboss.aop.InstanceAdvised;
import org.jboss.util.id.GUID;

import javax.transaction.Transaction;

public interface DistributedState
{
   public GUID getGUID();
   public DistributedUpdate createTxUpdate(Transaction tx);
   public InstanceAdvised buildObject(SynchronizationManager manager, DistributedVersionManager versionManager) throws Exception;
   public void mergeState(DistributedUpdate update) throws Exception;
   public void mergeState(Transaction tx) throws Exception;
   public void checkOptimisticLock(Transaction tx);
   public InstanceAdvised getObject();
   public void acquireWriteLock();
   public void acquireReadLock();
   public void releaseReadLock();
   public void releaseWriteLock();
}
