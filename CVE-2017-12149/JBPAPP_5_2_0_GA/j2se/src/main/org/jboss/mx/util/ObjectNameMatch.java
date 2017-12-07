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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.management.ObjectName;

/** JMX ObjectName comparision utility methods
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81019 $
 */
public class ObjectNameMatch
{
   /** Compare two ObjectNames to see if the match via equality or as
    * a pattern.
    * @param n0 An ObjectName or pattern
    * @param n1 An ObjectName or pattern
    * @return true if n0 and n1 match, false otherwise
    */
   public static boolean match(ObjectName n0, ObjectName n1)
   {
      boolean match = n0.equals(n1);
      if( match == true )
         return true;

      // First compare the domains
      String d0 = n0.getDomain();
      String d1 = n1.getDomain();
      int star0 = d0.indexOf('*');
      int star1 = d1.indexOf('*');

      if( star0 >= 0 )
      {
         if( star1 >= 0 )
         {
            match = d0.equals(d1);
         }
         else
         {
            try
            {
               Pattern domainRE = Pattern.compile(d0);
               Matcher m = domainRE.matcher(d1);
               match = m.matches();
            }
            catch(PatternSyntaxException e)
            {
            }
         }
      }
      else if( star1 >= 0 )
      {
         if( star0 >= 0 )
         {
            match = d0.equals(d1);
         }
         else
         {
            try
            {
               Pattern domainRE = Pattern.compile(d1);
               Matcher m = domainRE.matcher(d0);
               match = m.matches();
            }
            catch(PatternSyntaxException e)
            {
            }
         }
      }
      else
      {
         match = d0.equals(d1);
      }

      if( match == false )
         return false;

      // Next compare properties
      if( n0.isPropertyPattern() )
      {
         Hashtable props0 = n0.getKeyPropertyList();
         Hashtable props1 = n1.getKeyPropertyList();
         Iterator iter = props0.keySet().iterator();
         while( match == true && iter.hasNext() )
         {
            String key = (String) iter.next();
            String value = (String) props0.get(key);
            match &= value.equals(props1.get(key));
         }
      }
      else if( n1.isPropertyPattern() )
      {
         Hashtable props0 = n0.getKeyPropertyList();
         Hashtable props1 = n1.getKeyPropertyList();
         Iterator iter = props1.keySet().iterator();
         while( iter.hasNext() )
         {
            String key = (String) iter.next();
            String value = (String) props1.get(key);
            match &= value.equals(props0.get(key));
         }
      }

      return match;
   }

}
