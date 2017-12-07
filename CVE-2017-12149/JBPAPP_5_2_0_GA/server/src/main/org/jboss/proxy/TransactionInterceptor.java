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

import javax.transaction.TransactionManager;
import javax.transaction.Transaction;

import org.jboss.invocation.Invocation;
import org.jboss.proxy.Interceptor;

/**
* The client-side proxy for an EJB Home object.
*      
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 81030 $
*/
public class TransactionInterceptor
   extends Interceptor
{
   /** Serial Version Identifier. @since 1.4.2.1 */
   private static final long serialVersionUID = 371972342995600888L;

   public static TransactionManager tm;

   /**
   * No-argument constructor for externalization.
   */
   public TransactionInterceptor()
   {
   }

   // Public --------------------------------------------------------
   
   public Object invoke(Invocation invocation) 
   throws Throwable
   {
      if (tm != null)
      {
         Transaction tx = tm.getTransaction();
         if (tx != null) invocation.setTransaction(tx);
      }
      return getNext().invoke(invocation);
   }
   
   
   /** Transaction manager. */
   public static void setTransactionManager(TransactionManager tmx)
   {
      tm = tmx;
   }
}
