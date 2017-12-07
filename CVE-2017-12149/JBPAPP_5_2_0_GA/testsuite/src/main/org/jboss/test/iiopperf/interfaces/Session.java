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
package org.jboss.test.iiopperf.interfaces;

import java.util.Collection;
import java.util.Map;
import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.ejb.EJBObject;

/**
 *   @author Francisco.Reverbel@jboss.org
 *   @version $Revision: 81036 $
 */
public interface Session
   extends EJBObject
{
   public void sendReceiveNothing()
      throws RemoteException;

   public void sendBoolean(boolean flag)
      throws RemoteException;

   public boolean receiveBoolean()
      throws RemoteException;

   public boolean sendReceiveBoolean(boolean flag)
      throws RemoteException;

   public void sendChar(char c)
      throws RemoteException;

   public char receiveChar()
      throws RemoteException;

   public char sendReceiveChar(char c)
      throws RemoteException;

   public void sendByte(byte b)
      throws RemoteException;

   public byte receiveByte()
      throws RemoteException;

   public byte sendReceiveByte(byte b)
      throws RemoteException;

   public void sendShort(short s)
      throws RemoteException;

   public short receiveShort()
      throws RemoteException;

   public short sendReceiveShort(short s)
      throws RemoteException;

   public void sendInt(int i)
      throws RemoteException;

   public int receiveInt()
      throws RemoteException;

   public int sendReceiveInt(int i)
      throws RemoteException;

   public void sendLong(long l)
      throws RemoteException;

   public long receiveLong()
      throws RemoteException;

   public long sendReceiveLong(long l)
      throws RemoteException;

   public void sendFloat(float f)
      throws RemoteException;

   public float receiveFloat()
      throws RemoteException;

   public float sendReceiveFloat(float f)
      throws RemoteException;

   public void sendDouble(double d)
      throws RemoteException;

   public double receiveDouble()
      throws RemoteException;

   public double sendReceiveDouble(double d)
      throws RemoteException;

   public void sendString(String s)
      throws RemoteException;

   public String receiveString()
      throws RemoteException;

   public String sendReceiveString(String s)
      throws RemoteException;

   public void sendRemote(Remote r)
      throws RemoteException;

   public Remote receiveRemote()
      throws RemoteException;

   public Remote sendReceiveRemote(Remote r)
      throws RemoteException;

   public void sendSessionRef(Session s)
      throws RemoteException;

   public Session receiveSessionRef()
      throws RemoteException;

   public Session sendReceiveSessionRef(Session s)
      throws RemoteException;

   public void sendSimpleSerializable(Foo foo)
      throws RemoteException;

   public Foo receiveSimpleSerializable()
      throws RemoteException;

   public Foo sendReceiveSimpleSerializable(Foo r)
      throws RemoteException;

   public void sendSimpleCustomMarshalledSerializable(CMFoo cmfoo)
      throws RemoteException;

   public CMFoo receiveSimpleCustomMarshalledSerializable()
      throws RemoteException;

   public CMFoo sendReceiveSimpleCustomMarshalledSerializable(CMFoo cmfoo)
      throws RemoteException;

   public void sendNestedSerializable(Zoo zoo)
      throws RemoteException;

   public Zoo receiveNestedSerializable()
      throws RemoteException;

   public Zoo sendReceiveNestedSerializable(Zoo zoo)
      throws RemoteException;

   public void sendIntArray(int[] a)
      throws RemoteException;

   public int[] receiveIntArray()
      throws RemoteException;

   public int[] sendReceiveIntArray(int[] a)
      throws RemoteException;

   public void sendStringArray(String[] a)
      throws RemoteException;

   public String[] receiveStringArray()
      throws RemoteException;

   public String[] sendReceiveStringArray(String[] a)
      throws RemoteException;

   public void sendArrayOfSerializables(Foo[] a)
      throws RemoteException;

   public Foo[] receiveArrayOfSerializables()
      throws RemoteException;

   public Foo[] sendReceiveArrayOfSerializables(Foo[] a)
      throws RemoteException;

   public void sendCollection(Collection c)
      throws RemoteException;

   public Collection receiveCollection()
      throws RemoteException;

   public Collection sendReceiveCollection(Collection c)
      throws RemoteException;

   public void sendMap(Map m)
      throws RemoteException;

   public Map receiveMap()
      throws RemoteException;

   public Map sendReceiveMap(Map m)
      throws RemoteException;

   public void throwException()
      throws TestException, RemoteException;
   
}
