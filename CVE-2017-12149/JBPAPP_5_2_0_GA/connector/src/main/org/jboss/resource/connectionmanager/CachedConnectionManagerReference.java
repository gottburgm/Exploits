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
package org.jboss.resource.connectionmanager;

import javax.transaction.TransactionManager;
import org.jboss.tm.usertx.client.ServerVMClientUserTransaction;
import org.jboss.ejb.EnterpriseContext;

/**
 * This should be removed when JCA is refactored as POJOs
 * This is just to avoid the start/stop sequence of CachedConnectionManager
 * when it is created by a container as CachedConnectionManager tries to locate things
 * through MBEan calls.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 71554 $
 */
public class CachedConnectionManagerReference
{
   private CachedConnectionManager cachedConnectionManager = new CachedConnectionManager();
   private TransactionManager transactionManager;

   public CachedConnectionManager getCachedConnectionManager()
   {
      return cachedConnectionManager;
   }

   public void setCachedConnectionManager(CachedConnectionManager cachedConnectionManager)
   {
      this.cachedConnectionManager = cachedConnectionManager;
   }

   public TransactionManager getTransactionManager()
   {
      return transactionManager;
   }

   public void setTransactionManager(TransactionManager transactionManager)
   {
      this.transactionManager = transactionManager;
   }

   public void start()
   {
      TransactionSynchronizer.setTransactionManager(transactionManager);
      ServerVMClientUserTransaction.getSingleton().registerTxStartedListener(cachedConnectionManager);
      EnterpriseContext.setUserTransactionStartedListener(cachedConnectionManager);
   }

   public void stop()
   {
      ServerVMClientUserTransaction.getSingleton().unregisterTxStartedListener(cachedConnectionManager);
      EnterpriseContext.setUserTransactionStartedListener(null);
   }
}
