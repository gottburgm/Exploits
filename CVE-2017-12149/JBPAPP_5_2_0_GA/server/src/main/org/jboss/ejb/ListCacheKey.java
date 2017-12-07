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
package org.jboss.ejb;

import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.IOException;

/**
 * ListCacheKey extends {@link CacheKey} and holds info about the List that the entity belongs to,
 * it is used with CMP 2.0 for reading ahead.
 *
 * @author <a href="mailto:on@ibis.odessa.ua">Oleg Nitz</a>
 * @version $Revision: 81030 $
 */
public final class ListCacheKey
extends CacheKey
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /**
    * The list id.
    */
   private long listId;

   /**
    * The index of this entity in the list.
    */
   private int index;

   // Static --------------------------------------------------------

   // Public --------------------------------------------------------

   public ListCacheKey() {
      // For externalization only
   }

   /**
    * @param listId The list id.
    * @param index The index of this entity in the list.
    */
   public ListCacheKey(Object id, long listId, int index) {
      super(id);
      this.listId = listId;
      this.index = index;
   }

   public long getListId()
   {
      return listId;
   }

   public int getIndex()
   {
      return index;
   }

   // Z implementation ----------------------------------------------

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   public void writeExternal(ObjectOutput out)
      throws IOException
   {
      super.writeExternal(out);
      out.writeLong(listId);
      out.writeInt(index);
   }

   public void readExternal(ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      listId = in.readLong();
      index = in.readInt();
   }

   // Inner classes -------------------------------------------------
}
