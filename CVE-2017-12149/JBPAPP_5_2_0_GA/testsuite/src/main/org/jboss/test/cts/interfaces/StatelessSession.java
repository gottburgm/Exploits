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
package org.jboss.test.cts.interfaces;

import java.rmi.RemoteException;
import java.lang.reflect.InvocationTargetException;
import javax.ejb.EJBObject;


/** Interface for tests of stateless sessions
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public interface StatelessSession
   extends EJBObject
{
   public String method1 (String msg)
      throws RemoteException;

   void breakCreate() throws RemoteException;

   public void loopbackTest ()
      throws RemoteException;

   public void loopbackTest(EJBObject obj)
      throws RemoteException;

   public void callbackTest(ClientCallback callback, String data)
      throws RemoteException;

   public void npeError() throws RemoteException;

   /** Obtain the session local home and create a local proxy to test the
    * ability to access the local home through the local interface
    * @throws RemoteException
    */
   public void testLocalHome() throws InvocationTargetException, 
         RemoteException;

   public void testPassivationByTimeLocal() throws RemoteException;
}
