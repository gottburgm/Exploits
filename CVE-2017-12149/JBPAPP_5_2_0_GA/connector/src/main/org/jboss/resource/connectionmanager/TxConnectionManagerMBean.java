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

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;

/**
 * MBean interface.
 * 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 77478 $
 */
public interface TxConnectionManagerMBean extends BaseConnectionManager2MBean
{
   public static final ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.jca:service=BaseConnectionManager");

   /**
    * mbean get-set pair for field transactionManagerService Get the value of transactionManagerService
    * 
    * @return value of transactionManagerService
    */
   ObjectName getTransactionManagerService();

   /**
    * Set the value of transactionManagerService
    * 
    * @param transactionManagerService Value to assign to transactionManagerService
    */
   void setTransactionManagerService(ObjectName transactionManagerService);

   /**
    * The TransactionManager attribute contains the jndi name of the TransactionManager. This is normally java:/TransactionManager.
    * 
    * @param tmName an <code>String</code> value
    * @deprecated use the ObjectName TransactionManagerService instead
    */
   void setTransactionManager(String tmName);

   /**
    * Describe <code>getTransactionManager</code> method here.
    * 
    * @return an <code>String</code> value
    * @deprecated use the ObjectName TransactionManagerService instead
    */
   String getTransactionManager();

   /**
    * mbean get-set pair for field trackConnectionByTx Get the value of trackConnectionByTx
    * Deprecated in favor of isInterleaving()
    * 
    * @return value of trackConnectionByTx
    */
   @Deprecated
   boolean isTrackConnectionByTx();

   /**
    * Set the value of trackConnectionByTx
    * Deprecated in favor of setInterleaving(boolean value)
    * 
    * @param trackConnectionByTx Value to assign to trackConnectionByTx
    */
   @Deprecated
   void setTrackConnectionByTx(boolean trackConnectionByTx);

   /**
    * Whether interleaving transactions are allowed in case of XA
    * @return the value of interleaving
    */
   boolean isInterleaving();

   /**
    * Whether interleaving transaction are allowed in case of XA
    * 
    * @param value  the value of interleaving
    */
   void setInterleaving(boolean value);
   
   /**
    * mbean get-set pair for field localTransactions Get the value of localTransactions
    * 
    * @return value of localTransactions
    */
   boolean isLocalTransactions();

   /**
    * Set the value of localTransactions
    * 
    * @param localTransactions Value to assign to localTransactions
    */
   void setLocalTransactions(boolean localTransactions);

   /**
    * FIXME Comment this
    * 
    * @return
    */
   int getXAResourceTransactionTimeout();
   
   /**
    * FIXME Comment this
    * 
    * @param timeout
    */
   void setXAResourceTransactionTimeout(int timeout);

   Boolean getIsSameRMOverrideValue();
   
   void setIsSameRMOverrideValue(Boolean value);
   
   public boolean getWrapXAResource();
   
   public void setWrapXAResource(boolean useXAWrapper);
   
   public void setPadXid(boolean padXid);
   
   public boolean getPadXid();
   

}
