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

import javax.ejb.CreateException;
import javax.ejb.EntityContext;

import org.jboss.test.perf.interfaces.EntityPK;

public abstract class EntityBean implements javax.ejb.EntityBean
{
   private EntityContext context;
   private transient boolean isDirty;
   
   public abstract int getTheKey();
   public abstract void setTheKey(int theKey);
   public abstract int getTheValue();
   public abstract void setTheValue(int theValue);

   public int read()
   {
      setModified(false); // to avoid writing
      return getTheValue();
   }
   
   public void write(int theValue)
   {
      setModified(true); // to force writing
      setTheValue(theValue);
   }
   
   public EntityPK ejbCreate(int theKey, int theValue)
      throws CreateException
   {
      setTheKey(theKey);
      setTheValue(theValue);
      return null;
   }

   public void ejbPostCreate(int theKey, int theValue)
   {
   }

   public void ejbRemove()
   {
   }

   public void setEntityContext(EntityContext context)
   {
      this.context = context;
   }

   public void unsetEntityContext()
   {
      this.context = null;
   }
   
   public void ejbActivate()
   {
   }
   
   public void ejbPassivate()
   {
   }
   
   public void ejbLoad()
   {
      setModified(false); // to avoid writing
   }
   
   public void ejbStore()
   {
      setModified(false); // to avoid writing
   }
   
   public String toString()
   {
      return "EntityBean[theKey=" + getTheKey() + ",theValue=" + getTheValue() +"]";
   }

   public boolean isModified()
   {
      return isDirty;
   }

   public void setModified(boolean flag)
   {
      isDirty = flag;
   }
   
}

