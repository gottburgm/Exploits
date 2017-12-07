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
package org.jboss.resource.metadata;

import java.io.Serializable;

/**
 * Transaction Support meta data
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public class TransactionSupportMetaData implements Serializable
{
   static final long serialVersionUID = 691622093079125748L;

   /** No transaction */
   public static final int NoTransaction = 0;

   /** Local transaction */
   public static final int LocalTransaction = 1;

   /** XA transaction */
   public static final int XATransaction = 2;
   
   /** The transaction support */
   private int transactionSupport;

   /**
    * Get the transaction support
    * 
    * @return the transaction support
    */
   public int getTransactionSupport()
   {
      return transactionSupport;
   }

   /**
    * Set the transaction support
    * 
    * @param transactionSupport the transaction support
    */
   public void setTransactionSupport(int transactionSupport)
   {
      this.transactionSupport = transactionSupport;
   }
   
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("TransactionSupportMetaData").append('@');
      buffer.append(Integer.toHexString(System.identityHashCode(this)));
      if (transactionSupport == NoTransaction)
         buffer.append("[transactionSupport=NoTransaction]");
      else if (transactionSupport == LocalTransaction)
         buffer.append("[transactionSupport=LocalTransaction]");
      else if (transactionSupport == XATransaction)
         buffer.append("[transactionSupport=XATransaction]");
      else 
         buffer.append("[transactionSupport=Unknown/Error]");
      return buffer.toString();
   }
}
