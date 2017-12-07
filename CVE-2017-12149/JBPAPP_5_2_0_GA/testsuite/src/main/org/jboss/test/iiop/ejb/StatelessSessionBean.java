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
package org.jboss.test.iiop.ejb;

import javax.ejb.EJBException;

import org.jboss.test.util.ejb.SessionSupport;
import org.jboss.test.iiop.interfaces.Boo;
import org.jboss.test.iiop.interfaces.Foo;
import org.jboss.test.iiop.interfaces.IdlInterface;
import org.jboss.test.iiop.interfaces.NegativeArgumentException;
import org.jboss.test.iiop.interfaces.StatelessSession;
import org.jboss.test.iiop.interfaces.Zoo;
import org.jboss.test.iiop.util.Util;

/**
 *   @author reverbel@ime.usp.br
 *   @version $Revision: 81036 $
 */
public class StatelessSessionBean
   extends SessionSupport
{
   public String getString()
   {
      return Util.STRING;
   }
   
   public String testPrimitiveTypes(boolean flag, char c, byte b,
                                    short s, int i, long l, float f, double d)
   {
      return Util.primitiveTypesToString(flag, c, b, s, i, l, f, d);
   }
   
   public String testString(String s)
   {
      return Util.echo(s);
   }
   
   public StatelessSession testStatelessSession(String s, StatelessSession t)
   {
      return t;
   }
   
   public java.rmi.Remote testRemote(String s, java.rmi.Remote t)
   {
      return t;
   }
   
   public Foo testSerializable(Foo foo)
   {
      return Util.echoFoo(foo);
   }
   
   public int[] testIntArray(int[] a)
   {
      for (int i = 0; i < a.length; i++) 
         a[i]++;
      return a;
   }
   
   public Foo[] testValueArray(Foo[] a)
   {
      for (int i = 0; i < a.length; i++)
         a[i] = Util.echoFoo(a[i]);
      return a;
   }
   
   public String testException(int i)
      throws NegativeArgumentException
   {
      if (i >= 0)
         return "#" + i;
      else
         throw new NegativeArgumentException(i);
   }
   
   public Object fooValueToObject(Foo foo)
   {
      return Util.echoFoo(foo);
   }
   
   public Object booValueToObject(Boo boo)
   {
      return Util.echoBoo(boo);
   }
   
   public java.util.Vector valueArrayToVector(Foo[] a)
   {
      java.util.Vector v = new java.util.Vector();
      
      for (int i = 0; i < a.length; i++) 
         v.add(Util.echoFoo(a[i]));
      return v;
   }
   
   public Foo[] vectorToValueArray(java.util.Vector v)
   {
      Foo a[] = new Foo[v.size()];
      
      for (int i = 0; i < a.length; i++) 
         a[i] = Util.echoFoo((Foo)v.elementAt(i));
      return a;
   }
   
   public Object getException()
   {
      Object obj = null;
      try 
         {
            NegativeArgumentException e = new NegativeArgumentException(-7777);
            throw e;
         }
      catch (NegativeArgumentException e)
         {
            obj = e;
         }
      return obj;
   }
   
   public Object getZooValue()
   {
      return new Zoo("outer_zoo",
                     "returned by getZooValue",
                     new Zoo("inner_zoo", "inner"));
   }
   
   public Object[] testReferenceSharingWithinArray(Object[] a)
   {
      int n = a.length;
      Object[] b = new Object[2 * n];
      for (int i = 0; i < n; i++)
         b[i + n] = b[i] = a[i];
      return b;
   }
   
   public java.util.Collection testReferenceSharingWithinCollection(
                                                      java.util.Collection cin)
   {
      java.util.Collection cout = new java.util.ArrayList(cin);
      java.util.Iterator i = cin.iterator();
      while (i.hasNext())
         cout.add(i.next());
      return cout;
   }
   
   public org.omg.CORBA.Object testCorbaObject(org.omg.CORBA.Object obj)
   {
      return obj;
   }

   public IdlInterface testIdlInterface(IdlInterface ref)
   {
      return ref;
   }

}
