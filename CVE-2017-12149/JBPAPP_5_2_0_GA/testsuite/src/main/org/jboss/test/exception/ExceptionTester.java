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
package org.jboss.test.exception;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import javax.ejb.EJBObject;


/** A remote interface used for testing propagation of exceptions back to a
 * remote client.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */ 
public interface ExceptionTester extends EJBObject
{
   public void applicationExceptionInTx() 
         throws ApplicationException, RemoteException;
   public void applicationErrorInTx() throws RemoteException;
   public void ejbExceptionInTx() throws RemoteException;
   public void runtimeExceptionInTx() throws RemoteException;
   public void remoteExceptionInTx() throws RemoteException;
   
   public void applicationExceptionNewTx() 
         throws ApplicationException, RemoteException;
   public void applicationErrorNewTx() throws RemoteException;
   public void ejbExceptionNewTx() throws RemoteException;
   public void runtimeExceptionNewTx() throws RemoteException;
   public void remoteExceptionNewTx() throws RemoteException;

   public void applicationExceptionNoTx() 
         throws ApplicationException, RemoteException;
   public void applicationErrorNoTx() throws RemoteException;
   public void ejbExceptionNoTx() throws RemoteException;
   public void runtimeExceptionNoTx() throws RemoteException;
   public void remoteExceptionNoTx() throws RemoteException;

   public void securityExceptionNoTx() 
         throws RemoteException;
   public void securityExceptionByAppNoTx() 
         throws InvalidKeyException, RemoteException;
}
