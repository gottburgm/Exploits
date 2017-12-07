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
package org.jboss.test.hibernate.timers.interfaces;

import java.io.Serializable;

/**
 Example ejb key associated with a timer.

 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class Key implements Serializable
{
   private static final long serialVersionUID = 1L;

   private String key1;
   private long key2;

   public Key()
   {
   }
   public Key(String key1, long key2)
   {
      this.key1 = key1;
      this.key2 = key2;
   }

   public String getKey1()
   {
      return key1;
   }
   public void setKey1(String key1)
   {
      this.key1 = key1;
   }

   public long getKey2()
   {
      return key2;
   }
   public void setKey2(long key2)
   {
      this.key2 = key2;
   }

   public int hashCode()
   {
      int hc = (int) key2;
      return key1.hashCode() + hc;
   }
   public boolean equals(Object o)
   {
      Key k = (Key) o;
      return key1.equals(k.key1) && key2 == k.key2;
   }
}
