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

import java.util.Comparator;

/** A comparator that compares ops based on name and argument count.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81010 $
 */
public class OpCountComparator implements Comparator
{
   public int compare(Object o1, Object o2)
   {
      MBeanOp op1 = (MBeanOp) o1;
      MBeanOp op2 = (MBeanOp) o2;
      int compare = op1.getName().compareTo(op2.getName());
      if( compare == 0 )
         compare = op1.getArgCount() - op2.getArgCount();
      return compare;
   }
}
