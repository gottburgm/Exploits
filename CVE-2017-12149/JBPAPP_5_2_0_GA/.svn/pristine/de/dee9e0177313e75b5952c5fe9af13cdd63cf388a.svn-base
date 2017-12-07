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
package org.jboss.test.cmp2.fkmapping.ejb;


import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.ejb.CreateException;


/**
 * @ejb.bean
 *    name="Parent"
 *    type="CMP"
 *    cmp-version="2.x"
 *    view-type="local"
 *    reentrant="false"
 * @ejb.pk generate="true"
 * @ejb.util  generate="physical"
 * @ejb.persistence  table-name="PARENT"
 * @jboss.persistence
 *    create-table="true"
 *    remove-table="true"
 * @ejb:transaction-type  type="Container"
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 */
public abstract class ParentCMPBean
   implements EntityBean
{
   // Attributes -----------------------------------------------
   private EntityContext ctx;

   // CMP accessors --------------------------------------------
   /**
    * @ejb.pk-field
    * @ejb.persistent-field
    * @ejb.interface-method
    * @ejb.persistence  column-name="PARENT_ID"
    */
   public abstract Long getId();
   public abstract void setId(Long id);

   /**
    * @ejb.pk-field
    * @ejb.persistent-field
    * @ejb.interface-method
    * @ejb.persistence column-name="FIRST_NAME"
    */
   public abstract String getFirstName();
   public abstract void setFirstName(String name);

   // EntityBean implementation -------------------------------------
   /**
    * @ejb.create-method
    * @throws CreateException
    */
   public ParentPK ejbCreate(Long parentId, String firstName)
      throws CreateException
   {
      setId(parentId);
      setFirstName(firstName);
      return null;
   }

   public void ejbPostCreate(Long parentId, String firstName)
   {
   }

   /**
    * @param  ctx The new entityContext value
    */
   public void setEntityContext(EntityContext ctx)
   {
      this.ctx = ctx;
   }

   /**
    * Unset the associated entity context.
    */
   public void unsetEntityContext()
   {
      this.ctx = null;
   }

   public void ejbActivate() {}
   public void ejbLoad() {}
   public void ejbPassivate() {}
   public void ejbRemove() throws RemoveException {}
   public void ejbStore() {}
}
