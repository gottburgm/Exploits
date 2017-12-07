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
package org.jboss.tm.usertx.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.transaction.UserTransaction;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;


/**
 *  The RMI remote UserTransaction session interface.
 *
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 81030 $
 */
public interface UserTransactionSession
   extends Remote
{
   /**
    *  Destroy this session.
    */
   public void destroy()
      throws RemoteException;

   /**
    *  Start a new transaction, and return its TPC.
    *
    *  @param timeout The timeout value for the new transaction, in seconds.
    *
    *  @return The transaction propagation context for the new transaction.
    */
   public Object begin(int timeout)
      throws RemoteException,
             NotSupportedException,
             SystemException;

   /**
    *  Commit the transaction.
    *
    *  @param tpc The transaction propagation context for the transaction.
    */
   public void commit(Object tpc)
      throws RemoteException,
             RollbackException,
             HeuristicMixedException,
             HeuristicRollbackException,
             SecurityException,
             IllegalStateException,
             SystemException;

   /**
    *  Rollback the transaction.
    *
    *  @param tpc The transaction propagation context for the transaction.
    */
   public void rollback(Object tpc)
      throws RemoteException,
             SecurityException,
             IllegalStateException,
             SystemException;

   /**
    *  Mark the transaction for rollback only.
    *
    *  @param tpc The transaction propagation context for the transaction.
    */
   public void setRollbackOnly(Object tpc)
      throws RemoteException,
             IllegalStateException,
             SystemException;
   
   /**
    *  Return status of the transaction.
    *
    *  @param tpc The transaction propagation context for the transaction.
    */
   public int getStatus(Object tpc)
      throws RemoteException,
             SystemException;
}
