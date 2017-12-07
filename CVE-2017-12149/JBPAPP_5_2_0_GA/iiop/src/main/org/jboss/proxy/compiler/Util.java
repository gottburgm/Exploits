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
package org.jboss.proxy.compiler;

/**
 * Runtime utility class used by IIOP stub classes.
 *
 * @author Unknown
 * @author <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 81018 $
 */
public class Util
{
   public static final Object[] NOARGS = {};

   public static final ClassLoader NULLCL = null;

   public static Boolean wrap(boolean x) 
   {
      return new Boolean(x);
   }
   
   public static Byte wrap(byte x) 
   {
      return new Byte(x);
   }
   
   public static Character wrap(char x) 
   {
      return new Character(x);
   }
   
   public static Short wrap(short x) 
   {
      return new Short(x);
   }
   
   public static Integer wrap(int x) 
   {
      return new Integer(x);
   }
   
   public static Long wrap(long x) 
   {
      return new Long(x);
   }
   
   public static Float wrap(float x) 
   {
      return new Float(x);
   }
   
   public static Double wrap(double x) 
   {
      return new Double(x);
   }
   
}
