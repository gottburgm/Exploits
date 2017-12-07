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
package org.jboss.test.cmp2.enums.ejb;

import java.io.Serializable;
import java.io.ObjectStreamException;


/**
 * Enum implementations for colors.
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 */
public abstract class ColorEnum
   implements Serializable
{
   private static int nextOrdinal = 0;

   // Constants

   private static final ColorEnum VALUES[] =  new ColorEnum[3];

   public static final ColorEnum RED = new Red("RED");
   public static final ColorEnum GREEN = new Green("GREEN");
   public static final ColorEnum BLUE = new Blue("BLUE");

   // Attributes

   private final Integer ordinal;
   private final transient String name;

   // Constructor

   private ColorEnum(String name)
   {
      this.name = name;
      this.ordinal = new Integer(nextOrdinal++);
      VALUES[ordinal.intValue()] = this;
   }

   // Package

   Object readResolve()
      throws ObjectStreamException
   {
      return VALUES[ordinal.intValue()];
   }

   // Public

   public Integer getOrdinal()
   {
      return ordinal;
   }

   public String toString()
   {
      return name;
   }

   public ColorEnum valueOf(int ordinal)
   {
      return VALUES[ordinal];
   }

   // Inner

   private static final class Red extends ColorEnum
   {
      public Red(String name)
      {
         super(name);
      }
   }

   private static final class Green extends ColorEnum
   {
      public Green(String name)
      {
         super(name);
      }
   }

   private static final class Blue extends ColorEnum
   {
      public Blue(String name)
      {
         super(name);
      }
   }
}
