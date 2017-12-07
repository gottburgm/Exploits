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
package org.jboss.mx.util;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.management.ObjectName;

/**
 * ObjectName pattern matching Helper.<p>
 *
 * Contains various routines for matching domains and properties.<p>
 *
 * Routines based on work done Trevor in the registry.
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @author  <a href="mailto:trevor@protocool.com">Trevor Squires</a>.
 * @version $Revision: 81019 $
 */
public class ObjectNamePatternHelper
{
   // Static --------------------------------------------------------

   /**
    * Compare an object name against a pattern.
    *
    * @param test the string to test
    * @param pattern the pattern to match
    */
   public static boolean patternMatch(ObjectName test, ObjectName pattern)
   {
      if (pattern.equals("*:*"))
         return true;

      if (patternMatch(test.getDomain(), pattern.getDomain()))
      {
         PropertyPattern propertyPattern = new PropertyPattern(pattern);
         return propertyPattern.patternMatch(test);
      }
      return false;
   }

   /**
    * Compare strings where ? and * chars are significant.
    *
    * @param test the string to test
    * @param pattern the pattern to match
    */
   public static boolean patternMatch(String test, String pattern)
   {
      if (pattern.equals("*"))
         return true;
      return patternMatch(test.toCharArray(), 0, pattern.toCharArray(), 0);
   }

   /**
    * Compare  where ? and * chars are significant.<p>
    *
    * I arrived at this solution after quite a bit of trial and error - it's
    * all a bit interwoven.  Obviously I'm no good at parsers and there must
    * be a clearer or more elegant way to do this.  I'm suitably in awe of
    * the perl regex hackers now.
    *
    * @param test the string to test
    * @param tpos the start of the test string
    * @param pattern the pattern to match
    * @param ppos the start of the pattern string
    */
   public static boolean patternMatch(char[] test, int tpos, char[] pattern, int ppos)
   {
      int tlen = test.length;
      int plen = pattern.length;

      while (ppos < plen)
      {
         char c = pattern[ppos++];
         if ('?' == c)
         {
            // eat a test character and make sure we're not
            // already at the end
            if (tpos++ == tlen)
               return false;
         }
         else if ('*' == c)
         {
            if (ppos == plen) // shortcut - * at the end of the pattern
               return true;

            // Fell off the end
            if (tpos == tlen)
               return false;

            // hammer the test chars recursively until we
            // get a match or we drop off the end of test
            do
            {
               if (patternMatch(test, tpos, pattern, ppos))
                  return true;
            }
            while (++tpos < tlen);
         }
         else if (tpos == tlen || c != test[tpos++])
            return false;
      }
      // fell through with no falses so make sure all of test was examined
      return (tpos == tlen);
   }

   /**
    * Encapsulation of property information
    */
   public static class PropertyPattern
   {
      /**
       * Are these properties a pattern?
       */
      boolean isPropertyPattern;

      /**
       * The keys for the properties
       */
      Object[] propertyKeys;

      /**
       * The keys for the properties
       */
      Object[] propertyValues;

      /**
       * The canonical key property string
       */
      String canonicalKeyPropertyString;

      /**
       * Construct a new property pattern
       *
       * @param pattern the object name that might be a pattern
       */
      public PropertyPattern(ObjectName pattern)
      {
         isPropertyPattern = pattern.isPropertyPattern();
         if (isPropertyPattern)
         {
            Hashtable patternKPList = pattern.getKeyPropertyList();
            int length = patternKPList.size();
            propertyKeys = new Object[length];
            propertyValues = new Object[length];

            int i = 0;
            for (Iterator iterator = patternKPList.entrySet().iterator(); iterator.hasNext(); i++)
            {
               Map.Entry entry = (Map.Entry) iterator.next();
               propertyKeys[i] = entry.getKey();
               propertyValues[i] = entry.getValue();
            }
         }
         else
            canonicalKeyPropertyString = pattern.getCanonicalKeyPropertyListString();
      }

      /**
       * Test whether the object name matches the pattern
       *
       * @param name the name to test
       */
      public boolean patternMatch(ObjectName name)
      {
         if (isPropertyPattern)
         {
            // "*" matches everything
            if (propertyKeys.length == 0)
               return true;

            Hashtable kplist = name.getKeyPropertyList();

            for (int i = 0; i < propertyKeys.length; i++)
            {
               if (propertyValues[i].equals(kplist.get(propertyKeys[i])) == false)
                  return false;
            }
            return true;
         }
         else
            return canonicalKeyPropertyString.equals(name.getCanonicalKeyPropertyListString());
      }
   }
}
