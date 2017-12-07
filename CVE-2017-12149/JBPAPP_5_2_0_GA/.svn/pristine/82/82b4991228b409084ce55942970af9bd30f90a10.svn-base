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
package org.jboss.proxy;

import org.jboss.invocation.Invocation;

/**
 * The ClientUserTransactionStickyInterceptor should be used as an interceptor
 * for the HA proxy in the ClientUserTransactionService. The aim of this 
 * interceptor is twofold: First, when UserTransaction.begin() is called, the 
 * result of the invocation, which is the transaction propagation context of 
 * the transaction started, is stored together with the target server used, so 
 * that future invocations can make use of the sticky target. Secondly, for the
 * rest of UserTransaction invocations, i.e. commit(), getStatus()...etc, the 
 * current tpc is retrieved and the sticky target is located and added to the 
 * transient payload of the invocation.
 * 
 * @author <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 */
public class ClientUserTransactionStickyInterceptor extends AbstractTransactionStickyInterceptor
{
   @Override
   public Object invoke(Invocation invocation) throws Throwable
   {
      putIfExistsTransactionTarget(invocation);
      Object response = getNext().invoke(invocation); 
      invocationHasReachedAServer(invocation, response);
      return response;
   }

   public void invocationHasReachedAServer(Invocation invocation, Object response)
   {
      Object tpc = getTransactionPropagationContext();
      if (tpc == null)
      {
         /* If tpc is null when invoking a UserTransaction operation, begin() 
          * is being called, so we remember the target where the transaction 
          * was started.
          */
         rememberTransactionTarget(invocation, response);
      }
   }
}
