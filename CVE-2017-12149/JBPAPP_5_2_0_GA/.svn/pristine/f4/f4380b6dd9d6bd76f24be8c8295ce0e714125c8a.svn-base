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
package org.jboss.iiop.test;

import java.rmi.RemoteException;

/**
 */
//public interface Test extends AbstractTestBase, TestBase {
public interface Test extends TestBase {
        public final String const2 = "abc";

   public Test aa1() throws RemoteException;
   public TestValue aa2() throws RemoteException;
   public Object aa3() throws RemoteException;
   public java.io.Serializable aa4() throws RemoteException;
   public java.io.Externalizable aa5() throws RemoteException;
   public java.rmi.Remote aa6() throws RemoteException;
   
   public String jack(String arg) throws RemoteException;
   public String Jack(String arg) throws RemoteException;
   public String jAcK(String arg) throws RemoteException;
   
   /**
    * Gets the current value of the autonumber.
    */
   public int getValue() throws RemoteException;
   
   /**
    * Sets the current value of the autonumber.
    */
   public void setValue(int value) throws RemoteException;
   
   /**
    * A test operation.
    */
   public TestValue[][] addNumbers(int[] numbers,
                                   boolean b, char c, byte by, short s,
                                   int i, long l, float f, double d,
                                   java.rmi.Remote rem,
                                   TestValue val, Test intf,
                                   String str, Object obj, Class cls,
                                   java.io.Serializable ser,
                                   java.io.Externalizable ext)
      throws TestException, RemoteException;
}
