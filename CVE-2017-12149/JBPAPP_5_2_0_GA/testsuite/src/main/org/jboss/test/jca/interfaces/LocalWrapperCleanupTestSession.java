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
package org.jboss.test.jca.interfaces;

import java.rmi.RemoteException;

/**
 * Remote interface for LocalWrapperCleanupTestSession.
 */
public interface LocalWrapperCleanupTestSession extends javax.ejb.EJBObject
{
   /**
    * Describe <code>testAutoCommitInReturnedConnection</code> method here.
    * @exception EJBException if an error occurs
    */
   public void testAutoCommitInReturnedConnection() throws java.rmi.RemoteException;

   /**
    * Describe <code>testAutoCommit</code> method here.
    * @exception EJBException if an error occurs
    */
   public void testAutoCommit() throws java.rmi.RemoteException;

   /**
    * Describe <code>testAutoCommitOffInUserTx</code> method here.
    * @exception EJBException if an error occurs
    */
   public void testAutoCommitOffInUserTx() throws java.rmi.RemoteException;

   /**
    * Describe <code>testAutoCommitOffInUserTx2</code> method here.
    * @exception EJBException if an error occurs
    */
   public void testAutoCommitOffInUserTx2() throws java.rmi.RemoteException;

   public void testReadOnly() throws java.rmi.RemoteException;

   void testManualNoCommitRollback() throws RemoteException;
   void testManualSecondNoCommitRollback() throws RemoteException;

   /**
    * Describe <code>createTable</code> method here.
    * @exception EJBException if an error occurs
    */
   public void createTable() throws java.rmi.RemoteException;

   /**
    * Describe <code>insertAndCheckAutoCommit</code> method here.
    * @exception EJBException if an error occurs
    */
   public void insertAndCheckAutoCommit() throws java.rmi.RemoteException;

   /**
    * Describe <code>checkRowAndDropTable</code> method here.
    * @exception EJBException if an error occurs
    */
   public void checkRowAndDropTable() throws java.rmi.RemoteException;

   void addRowCheckAndDropTable() throws RemoteException;
}
