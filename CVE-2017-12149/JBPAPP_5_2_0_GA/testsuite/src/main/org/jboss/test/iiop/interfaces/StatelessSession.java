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
package org.jboss.test.iiop.interfaces;

import java.rmi.RemoteException;
import javax.ejb.EJBObject;

/**
 *   @author reverbel@ime.usp.br
 *   @version $Revision: 81036 $
 */
public interface StatelessSession
   extends EJBObject
{
   public String getString()
      throws java.rmi.RemoteException;
   
   public String testPrimitiveTypes(boolean flag, char c, byte b,
                                    short s, int i, long l, float f, double d)
      throws java.rmi.RemoteException;
   
   public String testString(String s)
      throws java.rmi.RemoteException;
   
   public StatelessSession testStatelessSession(String s, StatelessSession t)
      throws java.rmi.RemoteException;
   
   public java.rmi.Remote testRemote(String s, java.rmi.Remote t)
      throws java.rmi.RemoteException;
   
   public Foo testSerializable(Foo foo)
      throws java.rmi.RemoteException;
   
   public int[] testIntArray(int[] a)
      throws java.rmi.RemoteException;
   
   public Foo[] testValueArray(Foo[] a)
      throws java.rmi.RemoteException;
   
   public String testException(int i)
      throws NegativeArgumentException, java.rmi.RemoteException;
   
   public Object fooValueToObject(Foo foo)
      throws java.rmi.RemoteException;
   
   public Object booValueToObject(Boo boo)
      throws java.rmi.RemoteException;
   
   public java.util.Vector valueArrayToVector(Foo[] a)
      throws java.rmi.RemoteException;
   
   public Foo[] vectorToValueArray(java.util.Vector v)
      throws java.rmi.RemoteException;
   
   public Object getException()
      throws java.rmi.RemoteException;
   
   public Object getZooValue()
      throws java.rmi.RemoteException;
   
   public Object[] testReferenceSharingWithinArray(Object[] a)
      throws java.rmi.RemoteException;
   
   public java.util.Collection testReferenceSharingWithinCollection(
                                                        java.util.Collection c)
      throws java.rmi.RemoteException;

   public org.omg.CORBA.Object testCorbaObject(org.omg.CORBA.Object obj)
      throws java.rmi.RemoteException;

   public IdlInterface testIdlInterface(IdlInterface ref)
      throws java.rmi.RemoteException;

}
