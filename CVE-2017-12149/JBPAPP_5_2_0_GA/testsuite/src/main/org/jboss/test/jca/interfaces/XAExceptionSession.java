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
 * Remote interface for XAExceptionSession.
 */
public interface XAExceptionSession extends javax.ejb.EJBObject
{
   /**
    * Describe <code>ejbCreate</code> method here.
    */
   public void ejbCreate() throws java.rmi.RemoteException;

   /**
    * Describe <code>testXAException</code> method here.
    */
   public void testXAExceptionToTransactionRolledbackException() throws java.rmi.RemoteException;

   /**
    * Describe <code>testXAException</code> method here.
    */
   public void testRMERRInOnePCToTransactionRolledbackException() throws java.rmi.RemoteException;

   /**
    * Similate a connection failure
    */
   public void simulateConnectionError() throws java.rmi.RemoteException;

   /**
    * Similate a connection failure
    */
   public void simulateConnectionErrorWithTwoHandles() throws java.rmi.RemoteException;

   /**
    * Similate an exception
    */
   public void simulateError(java.lang.String failure, int count) throws java.rmi.RemoteException;

   /**
    * Similate an exception
    */
   public void simulateFactoryError(java.lang.String failure, int count) throws java.rmi.RemoteException;

}
