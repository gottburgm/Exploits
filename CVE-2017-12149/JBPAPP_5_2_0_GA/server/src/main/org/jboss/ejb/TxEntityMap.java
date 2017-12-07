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
package org.jboss.ejb;

import java.util.HashMap;
import javax.transaction.Transaction;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.jboss.tm.TransactionLocal;


/**
 * @deprecated this class was useful only for Instance Per Transaction containers and was used to check whether
 * the instance is associated with the tx. There is org.jboss.ejb.plugins.PerTxEntityInstanceCache which is
 * a per tx implementation of org.jboss.ejb.EntityCache which should be used with IPT containers.
 * (alex@jboss.org)
 *
 * This class provides a way to find out what entities of a certain type that are contained in
 * within a transaction.  It is attached to a specific instance of a container.
 *<no longer - global only holds possibly dirty> This class interfaces with the static GlobalTxEntityMap.  EntitySynchronizationInterceptor
 * registers tx/entity pairs through this class.
 * Used in EntitySynchronizationInterceptor.
 *
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 81030 $
 *
 * Revisions:
 *
 * <p><b>Revisions:</b><br>
 * <p><b>2001/08/06: billb</b>
 * <ol>
 *   <li>Got rid of disassociate and added a javax.transaction.Synchronization.  The sync will clean up the map now.
 *   <li>This class now interacts with GlobalTxEntityMap available.
 * </ol>
 */
public class TxEntityMap
{
   protected TransactionLocal m_map = new TransactionLocal();

   /**
    * associate entity with transaction
    */
   public void associate(Transaction tx, EntityEnterpriseContext entity)
      throws RollbackException, SystemException
   {
      HashMap entityMap = (HashMap) m_map.get(tx);
      if(entityMap == null)
      {
         entityMap = new HashMap();
         m_map.set(tx, entityMap);
      }
      //EntityContainer.getGlobalTxEntityMap().associate(tx, entity);
      entityMap.put(entity.getCacheKey(), entity);
   }

   public EntityEnterpriseContext getCtx(Transaction tx, Object key)
   {
      HashMap entityMap = (HashMap) m_map.get(tx);
      if(entityMap == null) return null;
      return (EntityEnterpriseContext) entityMap.get(key);
   }

   public EntityEnterpriseContext getCtx(Object key)
   {
      HashMap entityMap = (HashMap) m_map.get();
      if(entityMap == null) return null;
      return (EntityEnterpriseContext) entityMap.get(key);
   }
}
