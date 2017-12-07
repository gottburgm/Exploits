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
 * @ejb.bean name="A"
 * type="CMP"
 * cmp-version="2.x"
 * view-type="local"
 * reentrant="false"
 * @ejb.pk generate="true"
 * @ejb.util generate="physical"
 * @ejb.persistence table-name="CMRTREEA"
 * @jboss.persistence
 * create-table="true"
 * remove-table="true"
 * @ejb:transaction type="Required"
 * @ jboss.container-configuration name="custom container"
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 81036 $</tt>
 */
public abstract class ABean implements EntityBean
{
   // Attributes -----------------------------------------------
   Logger log = Logger.getLogger(ABean.class);
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
    * @ejb.interface-method
    * @ejb.persistent-field
    */
   public abstract String getName();

   /**
    * @ejb.interface-method
    */
   public abstract void setName(String name);

   /**
    * @ejb.interface-method
    * @ejb.relation name="A-B"
    *    role-name="A-has-B"
    */
   public abstract Collection getB();

   /**
    * @ejb.interface-method
    */
   public abstract void setB(Collection c);

   /**
    * @throws javax.ejb.CreateException
    * @ejb.create-method
    */
   public APK ejbCreate(int id, String id2, String name)
      throws CreateException
   {
      setMajorId(id);
      setMinorId(id2);
      setName(name);
      return null;
   }

   public void ejbPostCreate(int id, String id2, String name)
   {
   }

   /**
    * @param ctx The new entityContext value
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
