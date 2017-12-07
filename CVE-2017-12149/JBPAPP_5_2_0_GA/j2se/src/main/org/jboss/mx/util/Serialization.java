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

/**
 * Serialization Helper.<p>
 *
 * Contains static constants and attributes to help is serialization
 * versioning.
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @version $Revision: 81019 $
 */
public class Serialization
{
   // Static --------------------------------------------------------

   /**
    * The latest version of serialization
    */
   public static final int LATEST = 0;

   /**
    * The serialization for the 1.0 specified in the spec 1.1
    */
   public static final int V1R0 = 10;

   /**
    * The serialization version to use
    */
   public static int version = LATEST;

   /**
    * Determine the serialization version
    */
   static
   {
      try
      {
         String property = PropertyAccess.getProperty("jmx.serial.form");
         if (property != null && property.equals("1.0"))
            version = V1R0;
      }
      catch (java.security.AccessControlException ace)
      {
         // required for applets
      }
   }
}

