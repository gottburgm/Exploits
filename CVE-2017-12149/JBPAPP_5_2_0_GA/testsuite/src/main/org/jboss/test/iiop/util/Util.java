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
package org.jboss.test.iiop.util;

import org.jboss.test.iiop.interfaces.Boo;
import org.jboss.test.iiop.interfaces.Foo;

public class Util
{
   public final static String STRING =
      "the quick brown fox jumps over the lazy dog";
   
   public static String primitiveTypesToString(boolean flag, char c, byte b,
                                               short s, int i, long l, 
                                               float f, double d)
   {
      String str = "flag:\t" + flag + "\n"
                 + "c:\t" + c + "\n"
                 + "b:\t" + b + "\n"
                 + "s:\t" + s + "\n"
                 + "i:\t" + i + "\n"
                 + "l:\t" + l + "\n"
                 + "f:\t" + f + "\n"
                 + "d:\t" + d + "\n";
      return str;
    }
   
   public static String echo(String s)
   {
      return s + " (echoed back)";
   }
   
   public static Foo echoFoo(Foo f)
   {
      Foo newFoo = new Foo(f.i, f.s);
      newFoo.i++;
      newFoo.s += " <";
      return newFoo;
   }
   
   public static Boo echoBoo(Boo f)
   {
      Boo newBoo = new Boo(f.id, f.name);
      newBoo.id += "+";
      newBoo.name += " <";
      return newBoo;
   }
   
}
