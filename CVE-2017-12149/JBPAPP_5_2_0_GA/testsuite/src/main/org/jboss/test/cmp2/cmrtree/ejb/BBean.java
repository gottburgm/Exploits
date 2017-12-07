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
package org.jboss.test.cmp2.cmrtree.ejb;

import org.jboss.logging.Logger;

import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.ejb.CreateException;
import java.util.Collection;


/**
 * @ejb.bean
 *    name="B"
 *    type="CMP"
 *    cmp-version="2.x"
 *    view-type="local"
 *    reentrant="false"
 * @ejb.pk generate="true"
 * @ejb.util  generate="physical"
 * @ejb.persistence  table-name="CMRTREEB"
 * @jboss.persistence
 * create-table="true"
 * remove-table="true"
 * @ejb:transaction type="Required"
 * @ jboss.container-configuration name="custom container"
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81036 $</tt>
 */
public abstract class BBean
   implements EntityBean
{
   // Attributes -----------------------------------------------
   Logger log = Logger.getLogger(BBean.class);
   private EntityContext ctx;

   // CMP accessors --------------------------------------------
   /**
    * @ejb.pk-field
    * @ejb.persistent-field
    * @ejb.interface-method
    */
   public abstract int getMajorId();

   public abstract void setMajorId(int id);

   /**
    * @ejb.pk-field
    * @ejb.persistent-field
    * @ejb.interface-method
    */
   public abstract String getMinorId();

   public abstract void setMinorId(String id);

   /**
    * @ejb.persistent-field
    * @ejb.interface-method
    */
   public abstract String getName();

   /**
    * @ejb.interface-method
    */
   public abstract void setName(String id);

   /**
    * @ejb.persistent-field
    * @ejb.interface-method
    */
   public abstract String getAMinorId();
   /**
    * @ejb.interface-method
    */
   public abstract void setAMinorId(String aid);

   /**
    * @ejb.persistent-field
    * @ejb.interface-method
    */
   public abstract String getParentBMinorId();
   /**
    * @ejb.interface-method
    */
   public abstract void setParentBMinorId(String bid);

   /**
    * @ejb.interface-method
    * @ejb.relation name="B-Children"
    *    role-name="B-has-children"
    */
   public abstract Collection getChildren();

   /**
    * @ejb.interface-method
    */
   public abstract void setChildren(Collection c);

   /**
    * @ejb.interface-method
    * @ejb.relation
    *    name="B-Children"
    *    role-name="child-has-Parent"
    *    cascade-delete="true"
    * @jboss.relation
    *    fk-constraint="false"
    *    related-pk-field="majorId"
    *    fk-column="majorId"
    * @jboss.relation
    *    fk-constraint="false"
    *    related-pk-field="minorId"
    *    fk-column="parentBMinorId"
    */
   public abstract BLocal getParent();
   /**
    * @ejb.interface-method
    */
   public abstract void setParent(BLocal a);

   /**
    * @ejb.interface-method
    * @ejb.relation
    *    name="A-B"
    *    role-name="B-has-A"
    *    cascade-delete="true"
    * @jboss.relation
    *    fk-constraint="false"
    *    related-pk-field="majorId"
    *    fk-column="majorId"
    * @jboss.relation
    *    fk-constraint="false"
    *    related-pk-field="minorId"
    *    fk-column="AMinorId"
    */
   public abstract ALocal getA();
   /**
    * @ejb.interface-method
    */
   public abstract void setA(ALocal a);

   /**
    * @ejb.create-method
    * @throws CreateException
    */
   public BPK ejbCreate(int majorId, String minorId, String name)
      throws CreateException
   {
      setMajorId(majorId);
      setMinorId(minorId);
      setName(name);
      return null;
   }

   public void ejbPostCreate(int majorId, String minorId, String name)
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
