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
package org.jboss.test.cluster.ejb2.ustxsticky;

import javax.transaction.Transaction;

import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.logging.Logger;
import org.jboss.tm.TransactionPropagationContextImporter;
import org.jboss.tm.TransactionPropagationContextUtil;

/**
 * StickinessVerifierInterceptor.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class UserTransactionStickinessVerifierInterceptor extends AbstractInterceptor
{
   private static final Logger log = Logger.getLogger(UserTransactionStickinessVerifierInterceptor.class);
   
   @Override
   public Object invoke(Invocation inv) throws Exception
   {
      MarshalledInvocation mi = (MarshalledInvocation)inv;      
      Object tpc = mi.getTransactionPropagationContext();
      TransactionPropagationContextImporter tpcImporter = TransactionPropagationContextUtil.getTPCImporter();
      Transaction tx = tpcImporter.importTransactionPropagationContext(tpc);
      log.debug("Tpc " + tpc + " is associated with tx " + tx);
      
      /* If a tpc is retrieved on the server side but matches no transaction 
       * there, then it means that invocation was not sticky. An invocation 
       * containing a tpc must, under transaction stikcy rules, hit the server
       * where the tpc was generated from and where the transaction is running.
       */
      if (tx == null && tpc != null)
      {
         throw new IllegalStateException("Tpc " + tpc + " does not match a transaction on this node, invocation not sticky!");
      }
      
      return getNext().invoke(inv);
   }
}
