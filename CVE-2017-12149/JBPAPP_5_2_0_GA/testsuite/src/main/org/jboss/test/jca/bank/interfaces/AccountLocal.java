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
package org.jboss.test.jca.bank.interfaces;

/**
 * Local interface for Account.
 */
public interface AccountLocal extends javax.ejb.EJBLocalObject
{
   /**
    * Abstract cmp2 field get-set pair for field id Get the value of id
    * @return value of id
    */
   public java.lang.Integer getId();

   /**
    * Set the value of id
    * @param id Value to assign to id
    */
   public void setId(java.lang.Integer id);

   /**
    * field get-set pair for field balance Get the value of balance
    * @return value of balance
    */
   public int getBalance();

   /**
    * Set the value of balance
    * @param balance Value to assign to balance
    */
   public void setBalance(int balance);

   /**
    * field get-set pair for field customerId Get the value of customerId
    * @return value of customerId
    */
   public java.lang.Integer getCustomerId();

   /**
    * Set the value of customerId
    * @param customerId Value to assign to customerId
    */
   public void setCustomerId(java.lang.Integer customerId);

   /**
    * Describe <code>deposit</code> method here.
    * @param amount an <code>int</code> value
    */
   public void deposit(int amount);

   /**
    * Describe <code>withdraw</code> method here.
    * @param amount an <code>int</code> value
    */
   public void withdraw(int amount);

}
