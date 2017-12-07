/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.classloader.sharing.staticarray.common;

import java.util.Arrays;

/**
 * Class with a static public int array
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class Sequencer
   implements SequencerMBean
{
   public static Integer[] info = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

   public synchronized static void setInfo(int[] array)
   {
      info = new Integer[array.length];
      for(int n = 0; n < array.length; n ++)
         info[n] = array[n];
   }

   public synchronized void randomize()
   {
   }

   public synchronized void reverse()
   {
      Integer[] tmp = new Integer[info.length];
      for(int n = 0; n < info.length; n ++)
         tmp[n] = info[n];
      info = tmp;
   }

   public synchronized void set(int[] array)
   {
      setInfo(array);
   }

   public synchronized void sort()
   {
      Arrays.sort(info);
   }

   
}
