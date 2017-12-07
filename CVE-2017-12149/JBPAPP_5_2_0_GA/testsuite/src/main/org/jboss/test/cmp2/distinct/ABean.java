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
package org.jboss.test.cmp2.distinct;


import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.ejb.CreateException;


/**
 * @ejb.bean name="A"
 * type="CMP"
 * cmp-version="2.x"
 * view-type="local"
 * reentrant="false"
 * @ejb.pk generate="true"
 * @ejb.util generate="physical"
 * @ejb.persistence table-name="A"
 * @jboss.persistence datasource="${ds.name}"
 * datasource-mapping="${ds.mapping}"
 * create-table="${jboss.create.table}"
 * remove-table="${jboss.remove.table}"
 * pk-constraint="true"
 * @ejb:transaction type="Required"
 * @ejb.finder signature="Collection findByName(java.lang.String i)"
 * query="select distinct object(o) from A o where o.name=?1"
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 */
public abstract class ABean implements EntityBean
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
    * @ejb.persistent-field
    * @ejb.interface-method
    */
   public abstract MyData getMyData();

  /**
   * @ejb.interface-method
   */
   public abstract void setMyData(MyData data);

   /**
    * @ejb.persistent-field
    * @ejb.interface-method
    */
   public abstract String getName();

  /**
   * @ejb.interface-method
   */
   public abstract void setName(String name);

   /**
    * @throws javax.ejb.CreateException
    * @ejb.create-method
    */
   public Integer ejbCreate(Integer id, String name)
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
    * @param ctx The new entityContext value
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
