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
package org.jboss.test.iiopperf.ejb;

import javax.ejb.EJBException;

import java.util.Collection;
import java.util.Map;
import java.rmi.Remote;

import org.jboss.test.util.ejb.SessionSupport;
import org.jboss.test.iiopperf.interfaces.CMFoo;
import org.jboss.test.iiopperf.interfaces.Foo;
import org.jboss.test.iiopperf.interfaces.TestException;
import org.jboss.test.iiopperf.interfaces.Session;
import org.jboss.test.iiopperf.interfaces.Zoo;

/**
 *   @author Francisco.Reverbel@jboss.org
 *   @version $Revision: 81036 $
 */
public class SessionBean
   extends SessionSupport
{
   private String str = null;
   private Remote rem = null;
   private Session session = null;
   private Foo foo;
   private CMFoo cmfoo;
   private Zoo zoo;
   private int[] intArray;
   private String[] strArray;
   private Foo[] fooArray;
   private Collection coll;
   private Map map;

   public void ejbCreate()
   {
   }

   public void sendReceiveNothing()
   {
   }

   public void sendBoolean(boolean flag)
   {
   }

   public boolean receiveBoolean()
   {
      return true;
   }

   public boolean sendReceiveBoolean(boolean flag)
   {
      return flag;
   }

   public void sendChar(char c)
   {
   }

   public char receiveChar()
   {
      return Character.MAX_VALUE;
   }

   public char sendReceiveChar(char c)
   {
      return c;
   }

   public void sendByte(byte b)
   {
   }

   public byte receiveByte()
   {
      return Byte.MAX_VALUE;
   }

   public byte sendReceiveByte(byte b)
   {
      return b;
   }

   public void sendShort(short s)
   {
   }

   public short receiveShort()
   {
      return Short.MAX_VALUE;
   }

   public short sendReceiveShort(short s)
   {
      return s;
   }

   public void sendInt(int i)
   {
   }

   public int receiveInt()
   {
      return Integer.MAX_VALUE;
   }

   public int sendReceiveInt(int i)
   {
      return i;
   }

   public void sendLong(long l)
   {
   }

   public long receiveLong()
   {
      return Long.MAX_VALUE;
   }

   public long sendReceiveLong(long l)
   {
      return l;
   }

   public void sendFloat(float f)
   {
   }

   public float receiveFloat()
   {
      return Float.MAX_VALUE;
   }

   public float sendReceiveFloat(float f)
   {
      return f;
   }

   public void sendDouble(double d)
   {
   }

   public double receiveDouble()
   {
      return Double.MAX_VALUE;
   }

   public double sendReceiveDouble(double d)
   {
      return d;
   }

   public void sendString(String str)
   {
      this.str = str;
   }

   public String receiveString()
   {
      return this.str;
   }

   public String sendReceiveString(String s)
   {
      return s;
   }

   public void sendRemote(Remote rem)
   {
      this.rem = rem;
   }

   public Remote receiveRemote()
   {
      return this.rem;
   }

   public Remote sendReceiveRemote(Remote r)
   {
      return r;
   }

   public void sendSessionRef(Session session)
   {
     this.session = session;
   }

   public Session receiveSessionRef()
   {
      return this.session;
   }

   public Session sendReceiveSessionRef(Session s)
   {
      return s;
   }

   public void sendSimpleSerializable(Foo foo)
   {
      this.foo = foo;
   }

   public Foo receiveSimpleSerializable()
   {
      return this.foo;
   }

   public Foo sendReceiveSimpleSerializable(Foo foo)
   {
      return foo;
   }

   public void sendSimpleCustomMarshalledSerializable(CMFoo cmfoo)
   {
      this.cmfoo = cmfoo;
   }

   public CMFoo receiveSimpleCustomMarshalledSerializable()
   {
      return this.cmfoo;
   }

   public CMFoo sendReceiveSimpleCustomMarshalledSerializable(CMFoo cmfoo)
   {
      return cmfoo;
   }

   public void sendNestedSerializable(Zoo zoo)
   {
      this.zoo = zoo;
   }

   public Zoo receiveNestedSerializable()
   {
      return this.zoo;
   }

   public Zoo sendReceiveNestedSerializable(Zoo zoo)
   {
      return zoo;
   }

   public void sendIntArray(int[] a)
   {
      this.intArray = a;
   }

   public int[] receiveIntArray()
   {
      return this.intArray;
   }

   public int[] sendReceiveIntArray(int[] a)
   {
      return a;
   }

   public void sendStringArray(String[] a)
   {
      this.strArray = a;
   }

   public String[] receiveStringArray()
   {
      return this.strArray;
   }

   public String[] sendReceiveStringArray(String[] a)
   {
      return a;
   }

   public void sendArrayOfSerializables(Foo[] a)
   {
      this.fooArray = a;
   }

   public Foo[] receiveArrayOfSerializables()
   {
      return fooArray;
   }

   public Foo[] sendReceiveArrayOfSerializables(Foo[] a)
   {
      return a;
   }

   public void sendCollection(Collection c)
   {
      this.coll = c;
   }

   public Collection receiveCollection()
   {
      return this.coll;
   }

   public Collection sendReceiveCollection(Collection c)
   {
      return c;
   }

   public void sendMap(Map m)
   {
      this.map = m;
   }

   public Map receiveMap()
   {
      return this.map;
   }

   public Map sendReceiveMap(Map m)
   {
      return m;
   }

   public void throwException()
      throws TestException
   {
      throw new TestException();
   }
   
}
