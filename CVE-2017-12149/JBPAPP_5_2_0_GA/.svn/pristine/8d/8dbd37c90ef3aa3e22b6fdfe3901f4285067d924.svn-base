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
package org.jboss.deployment.scanner;

import org.jboss.deployment.DefaultDeploymentSorter;

import java.util.Comparator;
import java.net.URL;

/**
 * This is simialr to the PrefixDeploymentSorter in that it will order
 * files that do not start with a numeric value before those that do.
 * If two files begin with a number, will compare the numeric values.
 * However, if the two files do not have numeric prefixes, will
 * compare them using compareToIgnoreCase.
 *
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class AlphaNumericDeploymentSorter implements Comparator, DefaultDeploymentSorter
{

   private PrefixDeploymentSorter sorter = new PrefixDeploymentSorter();

   public String[] getSuffixOrder()
   {
      return sorter.getSuffixOrder();
   }

   public void setSuffixOrder(String[] suffixOrder)
   {
      sorter.setSuffixOrder(suffixOrder);
   }

   public int compare(Object o1, Object o2)
   {
      int comp = sorter.compare(o1, o2);

      return comp == 0 ? alphaCompare(o1, o2) : comp;
   }

   private String convertURLToString(URL url)
   {
      String path = url.getPath();
      int nameEnd = path.length() - 1;
      if (nameEnd <= 0) {
          return "";
      }

      // ignore a trailing '/'
      if (path.charAt(nameEnd) == '/') {
          nameEnd--;
      }

      // find the previous URL separator: '/'
      int nameStart = path.lastIndexOf('/', nameEnd) + 1;

      return path.substring(nameStart);

   }

   public int alphaCompare(Object o1, Object o2)
   {
      String s1 = convertURLToString((URL)o1);
      boolean s1IsDigit = Character.isDigit(s1.charAt(0));
      String s2 = convertURLToString((URL)o2);
      boolean s2IsDigit = Character.isDigit(s2.charAt(0));

      if(s1IsDigit && !s2IsDigit)
      {
         return 1;  // o1 is greater than o2, since has number and o2 does not
      }
      else if(!s1IsDigit && s2IsDigit)
      {
         return -1; //o1 is less than o2, since does not have number and o2 does
      }
      if(s1IsDigit && s2IsDigit)  // numeric comapre
      {
         int num1 = getNumericPrefix(s1);
         int num2 = getNumericPrefix(s2);
         int diff = num1 - num2;
         if(diff != 0) // do not begin with same number
         {
            return diff;
         }
         else //numbers are the same, so have to compare rest of the string
         {
            String s1Suf = getAlphaSuffix(s1);
            String s2Sef = getAlphaSuffix(s2);
            return s1Suf.compareToIgnoreCase(s2Sef);
         }
      }
      else // alpha compare
      {
         return s1.compareToIgnoreCase(s2);
      }
   }

   private String getAlphaSuffix(String s1)
   {
      int x = 0;
      while(Character.isDigit(s1.charAt(x++)));
      return s1.substring(x);
   }

   private int getNumericPrefix(String s1)
   {
      int x = 0;
      String numS1 = null;
      while(Character.isDigit(s1.charAt(x++)))
      {
         numS1 = s1.substring(0, x);
      }
      int number = Integer.parseInt(numS1);
      return number;
   }


}
