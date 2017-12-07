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
import javax.ejb.EJBObject;


/** A session bean that calls another bean in a seperate deployment
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public interface CallerSession
   extends EJBObject
{
   /** A call that looks up the cts2.jar CalleeSessionHome from JNDI each
    * time, creates an instance and calls simpleCall(false) to test type
    * isolation between jars.
    * @param isCaller
    * @throws RemoteException
    */
   public CalleeData simpleCall(boolean isCaller) throws RemoteException;
   /** A call that looks up the cts2.jar CalleeSessionHome from JNDI once
    * and resuses the home on subsequent calls, creates an instance and calls
    * simpleCall(false) to test type isolation between jars.
    * @param isCaller
    * @throws RemoteException
    */
   public CalleeData simpleCall2(boolean isCaller) throws RemoteException;

   /** An entry point
    *
    * @throws RemoteException
    */
   public void callByValueInSameJar() throws RemoteException;
   /** Make a call that requires is test argument to be passed by value
    *
    * @param test argument used to test call marshalling
    * @throws RemoteException
    */
   public void validateValueMarshalling(ReferenceTest test)
      throws RemoteException;

   public void callAppEx()
      throws RemoteException, CalleeException;

   /** A method that throws an application exception
    */
   public void appEx()
      throws RemoteException, CalleeException;
}
