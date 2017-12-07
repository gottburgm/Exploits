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
package test.dbc.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision: 80997 $
*/
@org.jboss.aspects.dbc.Dbc
public class Sorter
{
   /**
    * Returns the original array with all the elements sorted incrementally
    */
    @org.jboss.aspects.dbc.PostCond ({"$rtn.length == $0.length", "java: for (int i = 0 ; i < $rtn.length ; i++){if (i > 0){if ($rtn[i] < $rtn[i - 1])return false;}}return true;"})
   public static int[] sort(int[] unsorted)
   {
      //Not the most efficient sorting algorithm in the world, but hey
      ArrayList list = new ArrayList();

      for (int i = 0 ; i < unsorted.length ; i++)
      {
         list.add(new Integer(unsorted[i]));
      }

      Collections.sort(list,
            new Comparator()
            {
         		public boolean equals(Object obj)
         		{
         		   return false;
         		}

         		public int compare(Object o1, Object o2)
         		{
         		   int i1 = ((Integer)o1).intValue();
         		   int i2 = ((Integer)o2).intValue();

         		   if (i1 < i2)return -1;
         		   if (i1 == i2)return 0;
         		   return 1;
         		}
            });

      int[] sorted = new int[unsorted.length];
      for (int i = 0 ; i < list.size() ; i++)
      {
         sorted[i] = ((Integer)list.get(i)).intValue();
      }
      return sorted;
   }

   /**
    * This will break the post condition
    */
    @org.jboss.aspects.dbc.PostCond ({"java: for (int i = 0 ; i < $rtn.length ; i++){if (i > 0){if ($rtn[i] < $rtn[i - 1])return false;}}return true;"})
   public static int[] brokenSort(int[] unsorted)
   {
      return unsorted;
   }
}
