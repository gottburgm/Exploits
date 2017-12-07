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
package org.jboss.test.javassist.test.support;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */

public class AThing implements IThing
{
   private int method1Count;
   private int method2Count;
   private int method3Count;

   public int getMethod1Count()
   {
      return method1Count;
   }

   public int getMethod2Count()
   {
      return method2Count;
   }

   public int getMethod3Count()
   {
      return method3Count;
   }

   public void method1()
   {
      System.out.println("AThing.method1");
      method1Count ++;
   }

   public String method2(String arg)
   {
      method2Count ++;
      return "AThing.method2#"+method2Count;
   }

   public String method3(IThing arg)
   {
      method3Count ++;
      String result = arg.method2("Athing.method3");
      result += ":Athing.method3#"+method3Count;
      return result;
   }

}
