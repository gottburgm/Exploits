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
 *    name="ChildUPK"
 *    type="CMP"
 *    cmp-version="2.x"
 *    view-type="local"
 *    reentrant="false"
 * @ejb.pk
 *    class="java.lang.Object"
 *    generate="false"
 * @ejb.util  generate="physical"
 * @ejb.persistence  table-name="CHILD_UPK"
 * @jboss.persistence
 *    create-table="true"
 *    remove-table="true"
 * @ejb:transaction-type  type="Container"
 * @jboss.container-configuration name="INSERT after ejbPostCreate Container"
 *
 * @jboss.unknown-pk class="java.lang.String"
 * @jboss.entity-command name="key-generator"
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 */
public abstract class ChildCMPUnknownPKBean
   implements EntityBean
{
   // Attributes -----------------------------------------------
   private EntityContext ctx;

   // CMP accessors --------------------------------------------
   /**
    * @ejb.persistent-field
    * @ejb.interface-method
    * @ejb.persistence column-name="FIRST_NAME"
    */
   public abstract String getFirstName();
   public abstract void setFirstName(String name);

   /**
    * @ejb.interface-method
    * @ejb.relation
    *    name="Father-Child-upk"
    *    role-name="Child-has-Father"
    *    target-ejb="Parent"
    *    target-role-name="Father-has-Child"
    *    target-multiple="yes"
    *    cascade-delete="no"
    * @jboss.relation
    *    related-pk-field="id"
    *    fk-column="FATHER_ID"
    * @jboss.relation
    *    related-pk-field="firstName"
    *    fk-column="FATHER_NAME"
    */
   public abstract ParentLocal getFather();
   /**
    * @ejb.interface-method
    */
   public abstract void setFather(ParentLocal parent);

   /**
    * @ejb.interface-method
    * @ejb.relation
    *    name="Mother-Child-upk"
    *    role-name="Child-has-Mother"
    *    target-ejb="Parent"
    *    target-role-name="Mother-has-Child"
    *    target-multiple="yes"
    *    cascade-delete="no"
    * @jboss.relation
    *    related-pk-field="id"
    *    fk-column="MOTHER_ID"
    * @jboss.relation
    *    related-pk-field="firstName"
    *    fk-column="MOTHER_NAME"
    */
   public abstract ParentLocal getMother();
   /**
    * @ejb.interface-method
    */
   public abstract void setMother(ParentLocal parent);

   // EntityBean implementation -------------------------------------
   /**
    * @ejb.create-method
    */
   public Object ejbCreate(String firstName)
      throws CreateException
   {
      setFirstName(firstName);
      return null;
   }

   public void ejbPostCreate(Long id, String firstName) {}

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
