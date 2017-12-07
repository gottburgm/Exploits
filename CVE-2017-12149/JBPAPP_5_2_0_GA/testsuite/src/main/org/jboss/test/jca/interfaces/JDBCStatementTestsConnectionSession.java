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

/**
 * Remote interface for JDBCStatementTestsConnectionSession.
 */
public interface JDBCStatementTestsConnectionSession extends javax.ejb.EJBObject
{
   /**
    * The <code>testConnectionObtainable</code> method gets connections from the TestDriver after setting fail to true. This causes the test sql to throw an exception when the connection is retrieved from a pool, which closes the connection, forcing the connectionmanager to get a new one. We check this by counting how many connections have been closed.
    */
   public void testConnectionObtainable() throws java.rmi.RemoteException;

   public void testConfiguredQueryTimeout() throws java.rmi.RemoteException;

   public void testTransactionQueryTimeout() throws java.rmi.RemoteException;

   public void testTransactionQueryTimeoutMarkedRollback() throws java.rmi.RemoteException;

   public void testLazyAutoCommit() throws java.rmi.RemoteException;

   public void testRollbackOnCloseNoTx() throws java.rmi.RemoteException;

   public void testRollbackOnCloseManagedTx() throws java.rmi.RemoteException;
}
