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
package org.jboss.test.perf.ejb;
import org.jboss.test.perf.interfaces.Entity2PK;

public class Entity2Bean implements javax.ejb.EntityBean
{
   private javax.ejb.EntityContext _context;
   private transient boolean isDirty;

   public int key1;
   public String key2;
   public Double key3;
   public int the_value;

   public int read()
   {
      setModified(false); // to avoid writing
      return the_value;
   }
   
   public void write(int the_value)
   {
      setModified(true); // to force writing
      this.the_value = the_value;
   }
   
   public Entity2PK ejbCreate(int key1, String key2, Double key3, int value) 
   {
      this.key1 = key1;
      this.key2 = key2;
      this.key3 = key3;
      this.the_value = value;
      return null;
   }
   
   public void ejbPostCreate(int key1, String key2, Double key3, int value) 
   {
   }
   
   public void ejbRemove()
   {
   }
   
   public void setEntityContext(javax.ejb.EntityContext context)
   {
      _context = context;
   }
   
   public void unsetEntityContext()
   {
      _context = null;
   }
   
   public void ejbActivate()
   {
   }
   
   public void ejbPassivate()
   {
   }
   
   public void ejbLoad()
   {
      // WL
      setModified(false); // to avoid writing
   }
   
   public void ejbStore()
   {
      // WL
      setModified(false); // to avoid writing
   }
   
   public String toString()
   {
      return "EntityBean[key=(" + key1 + ',' + key2 + ',' + key3 +  "), the_value=" + the_value +"]";
   }

   // WL
   public boolean isModified()
   {
      return isDirty;
   }
   // WL
   public void setModified(boolean flag)
   {
      isDirty = flag;
   }
   
   
}
