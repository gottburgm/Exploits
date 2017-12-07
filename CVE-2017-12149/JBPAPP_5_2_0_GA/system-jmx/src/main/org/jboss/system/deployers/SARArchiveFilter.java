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
package org.jboss.system.deployers;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilter;

/**
 * SARArchiveFilter.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class SARArchiveFilter implements VirtualFileFilter
{
   /** The patterns */
   private final Set<String> patterns;
   
   /** Whether there is the accept all wildcard */
   private final boolean allowAll;
   
   /**
    * Create a new SARArchiveFilter.
    * 
    * @param patternsString the pattern string
    * @throws IllegalArgumentException for a null string
    */
   public SARArchiveFilter(String patternsString)
   {
      if (patternsString == null)
         throw new IllegalArgumentException("Null patternsString");

      StringTokenizer tokens = new StringTokenizer (patternsString, ",");
      patterns = new HashSet<String>(tokens.countTokens());
      for (int i=0; tokens.hasMoreTokens (); ++i)
      {
         String token = tokens.nextToken();
         patterns.add(token.trim());
      }
      allowAll = patterns.contains("*");
   }
   
   public boolean accepts(VirtualFile file)
   {
      if (allowAll)
         return true;
      return patterns.contains(file.getName());
   }
}
