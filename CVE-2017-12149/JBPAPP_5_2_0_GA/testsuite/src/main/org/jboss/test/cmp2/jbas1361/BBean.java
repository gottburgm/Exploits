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
package org.jboss.test.cmp2.jbas1361;


import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.ejb.CreateException;


/**
 * @ejb.bean
 *    name="B"
 *    type="CMP"
 *    cmp-version="2.x"
 *    view-type="local"
 *    reentrant="false"
 * @ejb.pk generate="true"
 * @ejb.util  generate="physical"
 * @ejb.persistence  table-name="B"
 * @jboss.persistence
 *    datasource="${ds.name}"
 *    datasource-mapping="${ds.mapping}"
 *    create-table="${jboss.create.table}"
 *    remove-table="${jboss.remove.table}"
 * @ejb:transaction type="Required"
 */
public abstract class BBean
   implements EntityBean
{
   // CMP accessors --------------------------------------------
   /**
    * @ejb.pk-field
    * @ejb.persistent-field
    * @ejb.interface-method
    */
   public abstract Integer getId();

   public abstract void setId(Integer id);

   /**
    * @ejb.pk-field
    * @ejb.persistent-field
    * @ejb.interface-method
    */
   public abstract String getName();

   /**
    * @ejb.interface-method
    */
   public abstract void setName(String id);

   /**
    * @ejb.interface-method
    * @ejb.relation
    *    name="A-B"
    *    role-name="B-has-A"
    * @jboss.relation
    *    fk-constraint="false"
    *    related-pk-field="id"
    *    fk-column="a_id"
    */
   public abstract ALocal getA();
   /**
    * @ejb.interface-method
    */
   public abstract void setA(ALocal a);

   /**
    * @ejb.create-method
    * @throws javax.ejb.CreateException
    */
   public BPK ejbCreate(Integer id, String name)
      throws CreateException
   {
      setId(id);
      setName(name);
      return null;
   }

   public void ejbPostCreate(Integer id, String name)
   {
   }

   /**
    * @param  ctx The new entityContext value
    */
   public void setEntityContext(EntityContext ctx)
   {
   }

   /**
    * Unset the associated entity context.
    */
   public void unsetEntityContext()
   {
   }

   public void ejbActivate()
   {
   }

   public void ejbLoad()
   {
   }

   public void ejbPassivate()
   {
   }

   public void ejbRemove() throws RemoveException
   {
   }

   public void ejbStore()
   {
   }
}
