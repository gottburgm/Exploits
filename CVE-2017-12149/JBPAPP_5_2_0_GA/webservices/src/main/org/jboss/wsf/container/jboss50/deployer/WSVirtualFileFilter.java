/*
 * JBoss, Home of Professional Open Source Copyright 2009, Red Hat Middleware
 * LLC, and individual contributors by the @authors tag. See the copyright.txt
 * in the distribution for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.jboss.wsf.container.jboss50.deployer;

import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileFilterWithAttributes;
import org.jboss.virtual.VisitorAttributes;

/**
 * VirtualFileFilter that can be used to search/filter files with the 
 * suffix ".wsdl" and ".xsd".
 * <p/>
 * 
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 *
 */
public class WSVirtualFileFilter implements VirtualFileFilterWithAttributes
{
   /** The attributes */
   private VisitorAttributes attributes;

   /**
    * No-args constructor. 
    * 
    * Will create a recursive filter by setting {@link VisitorAttributes#RECURSE_LEAVES_ONLY}.
    */
   public WSVirtualFileFilter()
   {
      this(VisitorAttributes.RECURSE_LEAVES_ONLY);
   }

   /**
    * 
    * @param attributes The {@link VisitorAttributes} value which determines the recursive behaviour of this filter.
    */
   public WSVirtualFileFilter(final VisitorAttributes attributes)
   {
      this.attributes = attributes;
   }

   /**
    * Retrieves the VisitorAttribute for this instance.
    */
   public VisitorAttributes getAttributes()
   {
      return attributes;
   }

   /**
    * Accepts files that end with .wsdl and .xsd.
    * 
    * @return {@code true} If the file name ends with either .wsdl or .xsd. Otherwise returns false.
    */
   public boolean accepts(final VirtualFile file)
   {
      if (file == null)
         return false;

      final String fileName = file.getName();
      return fileName.endsWith(".wsdl") || fileName.endsWith(".xsd") || fileName.endsWith(".xml");
   }

}
