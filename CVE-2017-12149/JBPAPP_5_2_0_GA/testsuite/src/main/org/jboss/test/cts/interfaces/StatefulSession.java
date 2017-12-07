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
import javax.ejb.CreateException;
import javax.ejb.EJBObject;
import javax.ejb.RemoveException;
import org.jboss.test.cts.keys.AccountPK;

/**
 * Interface StatefulSession
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public interface StatefulSession extends EJBObject
{
   public String getTestName() throws RemoteException;

   public String method1(String msg) throws RemoteException;

   public void incCounter() throws RemoteException;

   public void decCounter() throws RemoteException;

   public int getCounter() throws RemoteException;

   public void setCounter(int value) throws RemoteException;

   public BeanContextInfo getBeanContextInfo() throws RemoteException;

   public void loopbackTest() throws RemoteException;

   public void loopbackTest(EJBObject obj) throws RemoteException;

   /** Has ejbActivate been called */
   public boolean getWasActivated() throws RemoteException;

   /** Has ejbPassivate been called */
   public boolean getWasPassivated() throws RemoteException;

   public void createLocalEntity(AccountPK pk, String personsName) throws CreateException, RemoteException;

   public String readAndRemoveEntity() throws RemoveException, RemoteException;

   public void createSessionHandle() throws RemoteException;

   public String useSessionHandle(String arg) throws RemoteException;

   public void createStatefulSessionHandle(String testName) throws RemoteException;

   public void useStatefulSessionHandle() throws RemoteException;

   public void createSessionRef() throws RemoteException;

   public String useSessionRef() throws RemoteException;

   public void ping() throws RemoteException;

   public void sleep(long wait) throws RemoteException;
   
   void testBadUserTx() throws RemoteException;
}
