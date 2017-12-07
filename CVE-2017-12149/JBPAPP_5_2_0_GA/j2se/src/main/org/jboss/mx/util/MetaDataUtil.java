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

import java.util.HashSet;

/**
 * Utilities for handling meta data
 * 
 * Based on Strings from common (should jbossmx use common?)
 * 
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="Scott.Stark@jboss.org">Scott Stark</a>
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 81019 $
 */
public final class MetaDataUtil
{
   // Constants -----------------------------------------------------

   public static final String BOOLEAN_TYPE_NAME = Boolean.TYPE.getName();
   public static final String BYTE_TYPE_NAME = Byte.TYPE.getName();
   public static final String CHAR_TYPE_NAME = Character.TYPE.getName();
   public static final String DOUBLE_TYPE_NAME = Double.TYPE.getName();
   public static final String FLOAT_TYPE_NAME = Float.TYPE.getName();
   public static final String INT_TYPE_NAME = Integer.TYPE.getName();
   public static final String LONG_TYPE_NAME = Long.TYPE.getName();
   public static final String SHORT_TYPE_NAME = Short.TYPE.getName();
   public static final String VOID_TYPE_NAME = Void.TYPE.getName();

   private static final HashSet reserved = new HashSet();

   static
   {
      reserved.add("assert");
      reserved.add("abstract");
      reserved.add("boolean");
      reserved.add("break");
      reserved.add("byte");
      reserved.add("case");
      reserved.add("catch");
      reserved.add("char");
      reserved.add("class");
      reserved.add("const");
      reserved.add("continue");
      reserved.add("default");
      reserved.add("do");
      reserved.add("double");
      reserved.add("else");
      reserved.add("extends");
      reserved.add("false");
      reserved.add("final");
      reserved.add("finally");
      reserved.add("float");
      reserved.add("for");
      reserved.add("goto");
      reserved.add("if");
      reserved.add("implements");
      reserved.add("import");
      reserved.add("instanceof");
      reserved.add("int");
      reserved.add("interface");
      reserved.add("long");
      reserved.add("native");
      reserved.add("new");
      reserved.add("null");
      reserved.add("package");
      reserved.add("private");
      reserved.add("protected");
      reserved.add("public");
      reserved.add("return");
      reserved.add("short");
      reserved.add("static");
      reserved.add("strictfp");
      reserved.add("super");
      reserved.add("switch");
      reserved.add("synchronized");
      reserved.add("this");
      reserved.add("throw");
      reserved.add("throws");
      reserved.add("transient");
      reserved.add("true");
      reserved.add("try");
      reserved.add("void");
      reserved.add("volatile");
      reserved.add("while");
   }

   // Static --------------------------------------------------------

   /**
    * Tests whether the passed string is a valid java identifier
    *
    * @param string the string to test
    * @return true when it is valid
    */
   public static final boolean isValidJavaIdentifier(String string)
   {
      // Null or empty
      if (string == null || string.length() == 0)
         return false;

      final char[] chars = string.toCharArray();

      // Invalid start character
      if (Character.isJavaIdentifierStart(chars[0]) == false)
         return false;

      // Invalid part character
      for (int i = 1; i < chars.length; ++i)
      {
         if (Character.isJavaIdentifierPart(chars[i]) == false)
            return false;
      }

        if (reserved.contains(string))
           return false;

      // Yippee!
      return true;
   }

   /**
    * Tests whether the passed string is a valid java type
    *
    * @param string the string to test
    * @return true when it is valid
    */
   public static final boolean isValidJavaType(String string)
   {
      // Null or empty
      if (string == null || string.length() == 0)
         return false;

      // Looks like an array
      if (string.charAt(0) == '[')
      {
         String baseClassName = getBaseClassName(string);
         // But it is not valid
         if (baseClassName == null)
            return false;

         string = baseClassName;
      }

      // Check for a primitive
      if (isPrimitive(string))
         return true;

      final char[] chars = string.toCharArray();

      int start = 0;

      for (int i = 0; i < chars.length; ++i)
      {
         // Found a dot
         if (chars[i] == '.')
         {
            // But it as the start or straight after a previous dot
            if (i == start)
               return false;

            // Is what is before the dot a valid identifier?
            if (isValidJavaIdentifier(string.substring(start, i)) == false)
               return false;

            start = i+1;
         }
      }

      // Check the trailing characters
      if (start < chars.length &&
          isValidJavaIdentifier(string.substring(start, chars.length)) == false)
         return false;

      // Yippee!
      return true;
   }

   /**
    * Gets the base class name, either the passed class name
    * or the underlying class name if it is an array.<p>
    *
    * NOTE: The class is not check for validity.<p>
    *
    * Null is returned when the array declaration is invalid.
    *
    * @param string the string to test
    * @return the underlying class name or null
    */
   public static String getBaseClassName(String className)
   {
      final int length = className.length();
      final int last = length - 1;
      int i = 0;

      // Eat the array dimensions
      while (i < length && className.charAt(i) == '[')
         ++i;

      // It looks like an array
      if (i > 0)
      {
         // But is it valid
         char type = className.charAt(i);
         // Primitive array
         if (type == 'B' || type == 'C' || type == 'D' || type == 'F' ||
             type == 'I' || type == 'J' || type == 'S' || type == 'Z' || type == 'V')
         {
            if (i != last)
               return null;
            return className.substring(last, length);
         }
         // Object Array
         else if (className.charAt(i) != 'L' ||
             i >= last-1 ||
             className.charAt(last) != ';')
            return null;

         // Potentially valid array, class name might be rubbish
         return className.substring(i+1, last);
      }

      // Not an array
      return className;
   }

   /**
    * Checks whether a string is primitive
    *
    * @param string the string to test
    * @return true if it is primitive
    */
   public static boolean isPrimitive(String string)
   {
      if (string.equals(INT_TYPE_NAME))
         return true;
      if (string.equals(LONG_TYPE_NAME))
         return true;
      if (string.equals(BOOLEAN_TYPE_NAME))
         return true;
      if (string.equals(BYTE_TYPE_NAME))
         return true;
      if (string.equals(CHAR_TYPE_NAME))
         return true;
      if (string.equals(SHORT_TYPE_NAME))
         return true;
      if (string.equals(FLOAT_TYPE_NAME))
         return true;
      if (string.equals(DOUBLE_TYPE_NAME))
         return true;
      if (string.equals(VOID_TYPE_NAME))
         return true;
      return false;
   }
}