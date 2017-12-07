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
package org.jboss.proxy.ejb;

import java.rmi.RemoteException;

import javax.transaction.Transaction;

import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.invocation.Invocation;
import org.jboss.metadata.MetaData;

/**
 * A NoTxPropagationInterceptor for throwing remote exceptions
 * according to the spec.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81018 $
 */
public class NoTxPropagationInterceptor extends AbstractInterceptor
{
   public Object invokeHome(Invocation mi) throws Exception
   {
      checkNoTxPropagation(mi);
      return getNext().invokeHome(mi);
   }

   public Object invoke(Invocation mi) throws Exception
   {
      checkNoTxPropagation(mi);
      return getNext().invoke(mi);
   }
   
   protected void checkNoTxPropagation(Invocation mi) throws Exception
   {
      // No problem for local
      if (mi.isLocal())
         return;
      
      // Do we have a foreign transaction context?
      Transaction tx = mi.getTransaction();
      if (tx == null || (tx instanceof ForeignTransaction) == false)
         return;
      
      byte txType = container.getBeanMetaData().getTransactionMethod(mi.getMethod(), mi.getType());
      if (txType != MetaData.TX_NOT_SUPPORTED && txType != MetaData.TX_REQUIRES_NEW)
         throw new RemoteException("TxPropagation is not supported: " + container.getJmxName() + " method=" + mi.getMethod());
      
      // The propogation is not a problem
      mi.setTransaction(null);
   }
}
