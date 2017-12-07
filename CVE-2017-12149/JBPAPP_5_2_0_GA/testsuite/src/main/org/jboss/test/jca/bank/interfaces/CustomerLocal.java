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
 * Local interface for Customer.
 */
public interface CustomerLocal extends javax.ejb.EJBLocalObject
{
   /**
    * field get-set pair for field id Get the value of id
    * @return value of id
    */
   public java.lang.Integer getId();

   /**
    * Set the value of id
    * @param id Value to assign to id
    */
   public void setId(java.lang.Integer id);

   /**
    * field get-set pair for field name Get the value of name
    * @return value of name
    */
   public java.lang.String getName();

   /**
    * Set the value of name
    * @param name Value to assign to name
    */
   public void setName(java.lang.String name);

   /**
    * field get-set pair for field accounts Get the value of accounts
    * @return value of accounts
    */
   public java.util.Collection getAccounts();

   /**
    * Describe <code>addAccountLocal</code> method here.
    * @param account an <code>AccountLocal</code> value
    */
   public void addAccount(org.jboss.test.jca.bank.interfaces.AccountLocal account);

   /**
    * Describe <code>removeAccount</code> method here.
    * @param acct an <code>AccountLocal</code> value
    */
   public void removeAccount(org.jboss.test.jca.bank.interfaces.AccountLocal account);

}
