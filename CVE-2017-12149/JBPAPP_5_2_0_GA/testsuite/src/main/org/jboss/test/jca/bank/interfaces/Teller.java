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
 * Remote interface for Teller.
 */
public interface Teller extends javax.ejb.EJBObject
{
   /**
    * Describe <code>setUp</code> method here.
    * @exception EJBException if an error occurs
    */
   public void setUp() throws java.rmi.RemoteException;

   /**
    * Describe <code>tearDown</code> method here.
    * @exception EJBException if an error occurs
    */
   public void tearDown() throws java.rmi.RemoteException;

   /**
    * Describe <code>transfer</code> method here.
    * @param from an <code>Account</code> value
    * @param to an <code>Account</code> value
    * @param amount a <code>float</code> value
    * @exception EJBException if an error occurs
    */
   public void transfer(org.jboss.test.jca.bank.interfaces.Account from, org.jboss.test.jca.bank.interfaces.Account to,
         int amount) throws java.rmi.RemoteException;

   /**
    * Describe <code>createAccount</code> method here.
    * @param id a <code>Integer</code> value, id of account
    * @return an <code>Account</code> value
    * @exception EJBException if an error occurs
    */
   public org.jboss.test.jca.bank.interfaces.Account createAccount(java.lang.Integer id)
         throws java.rmi.RemoteException;

   /**
    * Describe <code>getAccountBalance</code> method here.
    * @param id a <code>integer</code> value, id of account
    * @return an <code>int</code> value, balbance of account
    * @exception EJBException if an error occurs
    */
   public int getAccountBalance(java.lang.Integer id) throws java.rmi.RemoteException;

   /**
    * Describe <code>transferTest</code> method here.
    * @param from an <code>AccountLocal</code> value
    * @param to an <code>AccountLocal</code> value
    * @param amount a <code>float</code> value
    * @param iter an <code>int</code> value
    * @exception java.rmi.RemoteException if an error occurs
    * @exception EJBException if an error occurs
    */
   public void transferTest(org.jboss.test.jca.bank.interfaces.AccountLocal from,
         org.jboss.test.jca.bank.interfaces.AccountLocal to, int amount, int iter) throws java.rmi.RemoteException;

}
