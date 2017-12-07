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
package org.jboss.console.twiddle.command;

import javax.management.MBeanParameterInfo;

/** A representation of an mbean operation that compares ops based on
 * the name and the operation parameters.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81010 $
 */
public class MBeanOp
{
   private String name;
   private String[] sig;

   public MBeanOp(String name, MBeanParameterInfo[] params)
   {
      this.name = name;
      int count = params != null ? params.length : 0;
      sig = new String[count];
      for(int n = 0; n < count; n ++)
      {
         MBeanParameterInfo p = params[n];
         sig[n] = p.getType();
      }
   }
   public MBeanOp(String name, int count)
   {
      this.name = name;
      sig = new String[count];
      for(int n = 0; n < count; n ++)
      {
         sig[n] = String.class.getName();
      }
   }

   public String getName()
   {
      return name;
   }
   public String[] getSignature()
   {
      return sig;
   }
   public int getArgCount()
   {
      return sig.length;
   }
   public String getArgType(int n)
   {
      return sig[n];
   }

   public boolean equals(Object obj)
   {
      MBeanOp op = (MBeanOp) obj;
      if( op.name.equals(name) == false || sig.length != op.sig.length )
         return false;
      for(int n = 0; n < sig.length; n ++)
      {
         if( sig[n].equals(op.sig[n]) == false )
            return false;
      }
      return true;
   }

   public int hashCode()
   {
      int hashCode = name.hashCode();
      for(int n = 0; n < sig.length; n ++)
      {
         hashCode += sig[n].hashCode();
      }
      return hashCode;
   }
}
