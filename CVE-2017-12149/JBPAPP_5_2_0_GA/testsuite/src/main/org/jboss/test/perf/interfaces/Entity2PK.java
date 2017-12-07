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
package org.jboss.test.perf.interfaces;
public class Entity2PK implements java.io.Serializable
{
   public int key1;
   public String key2;
   public Double key3;

   public Entity2PK()
   {
   }

   public Entity2PK(int key1, String key2, Double key3)
   {
      this.key1 = key1;
      this.key2 = key2;
      this.key3 = key3;
   }

   public boolean equals(Object obj)
   {
      Entity2PK key = (Entity2PK) obj;
      boolean equals = key1 == key.key1
         && key2.equals(key.key2) && key3.equals(key.key3);
      return equals;
   }
   public int hashCode()
   {
      return key1 + key2.hashCode() + key3.hashCode();
   }

   public String toString()
   {
      return "Entity2PK[" + key1 + ',' + key2 + ',' + key3 + "]";
   }

}
