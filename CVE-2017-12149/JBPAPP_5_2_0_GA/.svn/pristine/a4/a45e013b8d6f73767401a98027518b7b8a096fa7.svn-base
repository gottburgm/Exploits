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

import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.CreateException;


/**
 * @ejb.bean
 *    name="Child"
 *    type="CMP"
 *    cmp-version="2.x"
 *    view-type="local"
 *    reentrant="false"
 *    primkey-field="id"
 * @ejb.pk generate="false"
 * @ejb.util  generate="physical"
 * @ejb.persistence  table-name="CHILD"
 * @jboss.persistence
 *    datasource="${ds.name}"
 *    datasource-mapping="${ds.mapping}"
 *    create-table="${jboss.create.table}"
 *    remove-table="${jboss.remove.table}"
 * @ejb:transaction-type type="Container"
 *
 * @ejb.finder
 *    signature="org.jboss.test.cmp2.ejb.ChildLocal findByColor(org.jboss.test.cmp2.enums.ejb.ColorEnum color)"
 *    query="select object(o) from Child o where o.color=?1"
 *
 * @ejb.finder
 *    signature="org.jboss.test.cmp2.ejb.ChildLocal findByColorDeclaredSql(org.jboss.test.cmp2.enums.ejb.ColorEnum color)"
 *    query="select object(o) from Child o where o.color=?1"
 *
 * @ejb.finder
 *    signature="java.util.Collection findLowColor(org.jboss.test.cmp2.enums.ejb.ColorEnum color)"
 *    query="select object(o) from Child o where o.color<?1"
 * @jboss.query
 *    signature="java.util.Collection findLowColor(org.jboss.test.cmp2.enums.ejb.ColorEnum color)"
 *    query="select object(o) from Child o where o.color<?1"
 *
 * @ejb.finder
 *    signature="org.jboss.test.cmp2.ejb.ChildLocal findAndOrderByColor(org.jboss.test.cmp2.enums.ejb.ColorEnum color)"
 *    query="select object(o) from Child o where o.color = ?1"
 * @jboss.query
 *    signature="org.jboss.test.cmp2.ejb.ChildLocal findAndOrderByColor(org.jboss.test.cmp2.enums.ejb.ColorEnum color)"
 *    query="select object(o) from Child o where o.color = ?1 order by o.color"
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 * @author <a href="mailto:gturner@unzane.com">Gerald Turner</a>
 */
public abstract class ChildCMPBean
   implements EntityBean
{
   // Attributes -----------------------------------------------
   private EntityContext ctx;

   // CMP accessors --------------------------------------------
   /**
    * @ejb.pk-field
    * @ejb.persistent-field
    * @ejb.interface-method
    * @ejb.persistence  column-name="CHILD_ID"
    */
   public abstract IDClass getId();

   public abstract void setId(IDClass id);

   /**
    * @ejb.persistent-field
    * @ejb.interface-method
    * @ejb.persistence column-name="COLOR_ID"
    */
   public abstract ColorEnum getColor();

   /**
    * @ejb.interface-method
    */
   public abstract void setColor(ColorEnum color);

   /**
    * @ejb.persistent-field
    * @ejb.interface-method
    * @ejb.persistence column-name="ANIMAL_ID"
    */
   public abstract AnimalEnum getAnimal();

   /**
    * @ejb.interface-method
    */
   public abstract void setAnimal(AnimalEnum animal);

   public abstract ColorEnum ejbSelectMinColor() throws FinderException;

   public ColorEnum ejbHomeSelectMinColor() throws FinderException
   {
      return ejbSelectMinColor();
   }

   public abstract ColorEnum ejbSelectMaxColor() throws FinderException;

   public ColorEnum ejbHomeSelectMaxColor() throws FinderException
   {
      return ejbSelectMaxColor();
   }

   public abstract ColorEnum ejbSelectAvgColor() throws FinderException;

   public ColorEnum ejbHomeSelectAvgColor() throws FinderException
   {
      return ejbSelectAvgColor();
   }

   /**
    * @ejb.select
    * @ejb.interface-method view-type="local"
    * @jboss.query query="SELECT c.color FROM Child c where c.id=?1"
    */
   public abstract ColorEnum ejbSelectColor(IDClass id) throws FinderException;

   public ColorEnum ejbHomeSelectColor(IDClass id) throws FinderException
   {
      return ejbSelectColor(id);
   }

   // EntityBean implementation -------------------------------------
   /**
    * @ejb.create-method
    * @throws CreateException
    */
   public IDClass ejbCreate(IDClass childId)
      throws CreateException
   {
      setId(childId);
      setColor(ColorEnum.RED);
      setAnimal(AnimalEnum.PENGUIN);
      return null;
   }

   public void ejbPostCreate(IDClass childId)
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
