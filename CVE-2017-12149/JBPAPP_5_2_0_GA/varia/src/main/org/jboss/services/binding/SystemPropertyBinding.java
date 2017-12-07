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

package org.jboss.services.binding;

import java.net.InetAddress;
import java.net.URL;

/**
 * Encapsulates the key and value for a system property. Basic function is to
 * perform type conversions in its constructor for values returned by
 * {@link ServiceBindingManager} so {@link SystemPropertyBinder} can bind the
 * values. 
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public class SystemPropertyBinding
{
   private final String property;
   private final String value;
   
   public SystemPropertyBinding(String property, int value)
   {
      this(property, String.valueOf(value));
   }
   
   public SystemPropertyBinding(String property, InetAddress value)
   {
      this(property, value.getHostAddress());
   }
   
   public SystemPropertyBinding(String property, URL value)
   {
      this(property, value.toExternalForm());
   }
   
   public SystemPropertyBinding(String property, String value)
   {
      this.property = property;
      this.value = value;
   }
   
   public String getProperty()
   {
      return property;
   }
   public String getValue()
   {
      return value;
   }
   
   
}
