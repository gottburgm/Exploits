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

import org.jboss.ejb.plugins.cmp.jdbc.Mapper;

import java.io.Serializable;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81036 $</tt>
 */
public class IDClass
   implements Serializable, Mapper
{
   public long id;

   public IDClass()
   {
   }

   public IDClass(long id)
   {
      this.id = id;
   }

   public long getId()
   {
      return id;
   }

   public void setId(long id)
   {
      this.id = id;
   }

   public boolean equals(Object o)
   {
      if(this == o) return true;
      if(!(o instanceof IDClass)) return false;

      final IDClass idClass = (IDClass) o;

      if(id != idClass.id) return false;

      return true;
   }

   public int hashCode()
   {
      return (int) (id ^ (id >>> 32));
   }

   public String toString()
   {
      return "[" + id + ']';
   }

   // Mapper implementation

   public Object toColumnValue(Object fieldValue)
   {
      return fieldValue == null ? null : new Long(((IDClass)fieldValue).id);
   }

   public Object toFieldValue(Object columnValue)
   {
      return columnValue == null ? null : new IDClass(((Long)columnValue).longValue());
   }
}
