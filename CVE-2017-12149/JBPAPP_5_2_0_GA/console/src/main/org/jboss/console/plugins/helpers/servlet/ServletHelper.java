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
package org.jboss.console.plugins.helpers.servlet;

/**
 * Helper class
 * 
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andredis</a>
 * @version $Revision: 81010 $
 */
public class ServletHelper
{
   /**
    * Encode '<', '>', '"', '&' characters to their html equivalent
    */
   public static String filter(String input)
   {
      if (input == null)
      {
         return null;
      }
      else
      {
         StringBuffer filtered = new StringBuffer(input.length() * 2);
         char c;
         for (int i = 0; i < input.length(); i++)
         {
            c = input.charAt(i);
            switch (c)
            {
               case '<':
                  filtered.append("&lt;");
                  break;
               case '>':
                  filtered.append("&gt;");
                  break;
               case '"':
                  filtered.append("&quot;");
                  break;
               case '&':
                  filtered.append("&amp;");
                  break;
               default:
                  filtered.append(c);
                  break;
            }
         }
         return filtered.toString();
      }
   }
}
