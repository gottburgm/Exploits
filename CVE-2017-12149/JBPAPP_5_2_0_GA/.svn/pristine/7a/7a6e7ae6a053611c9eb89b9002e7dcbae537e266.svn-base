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

/**
 * Enum implementations for animals.
 *
 * @author <a href="mailto:gturner@unzane.com">Gerald Turner</a>
 */
public abstract class AnimalEnum
{
   private static int nextOrdinal = 0;

   // Constants

   private static final AnimalEnum VALUES[] = new AnimalEnum[3];

   public static final AnimalEnum DOG = new Dog("DOG");
   public static final AnimalEnum CAT = new Cat("CAT");
   public static final AnimalEnum PENGUIN = new Penguin("PENGUIN");

   // Attributes

   private final Integer ordinal;
   private final transient String name;

   // Constructor

   private AnimalEnum(String name)
   {
      this.name = name;
      this.ordinal = new Integer(nextOrdinal++);
      VALUES[ordinal.intValue()] = this;
   }

   // Package

   // Public

   public Integer getOrdinal()
   {
      return ordinal;
   }

   public String toString()
   {
      return name;
   }

   public AnimalEnum valueOf(int ordinal)
   {
      return VALUES[ordinal];
   }

   // Inner

   private static final class Dog extends AnimalEnum
   {
      public Dog(String name)
      {
         super(name);
      }
   }

   private static final class Cat extends AnimalEnum
   {
      public Cat(String name)
      {
         super(name);
      }
   }

   private static final class Penguin extends AnimalEnum
   {
      public Penguin(String name)
      {
         super(name);
      }
   }
}
