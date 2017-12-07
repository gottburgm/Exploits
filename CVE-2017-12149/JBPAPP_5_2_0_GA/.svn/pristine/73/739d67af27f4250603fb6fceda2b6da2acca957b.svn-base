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
package org.jboss.aspects.versioned;
import org.jboss.aop.InstanceAdvised;
import org.jboss.util.id.GUID;

public class DistributedFieldUpdate implements java.io.Serializable
{
   private static final long serialVersionUID = -8249659475610689146L;
   
   protected Object val;
   protected long version;
   protected int index;

   public DistributedFieldUpdate() {}
   public DistributedFieldUpdate(Object val, long version, int index)
   {
      setValue(val);
      this.version = version;
      this.index = index;
   }
   public long getVersionId() { return version; }
   public void setVersionId(long newId) { version = newId; }
   public int getFieldIndex() { return index; }


   public boolean equals(Object obj)
   {
      DistributedFieldUpdate update = (DistributedFieldUpdate)obj;
      return update.index == this.index;
   }

   public int hashCode()
   {
      return index;
   }


   public Object getValue() 
   { 
      if (val instanceof VersionReference)
      {
         return ((VersionReference)val).get();
      }
      return val; 
   }
   public void setValue(Object newVal) 
   { 
      if (newVal instanceof InstanceAdvised)
      {
         InstanceAdvised advised = (InstanceAdvised)newVal;
         GUID guid = VersionManager.getGUID(advised);
         if (guid != null)
         {
            // we are versioned
            val = new VersionReference(guid, advised);
            return;
         }
         
      }
      val = newVal; 
   }
   public Object getNonDereferencedValue()
   {
      return val;
   }
}
