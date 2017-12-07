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
package org.jboss.test.cmp2.ejbselect;

import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

/**
 * @ejb.bean
 *    name="A"
 *    type="CMP"
 *    cmp-version="2.x"
 *    view-type="local"
 *    primkey-field="id"
 * @ejb.pk generate="true"
 * @ejb.util  generate="physical"
 * @ejb.persistence  table-name="TEST_A"
 * @jboss.persistence
 *    create-table="true"
 *    remove-table="true"
 *
 * @jboss.declared-sql
 *    signature="Collection ejbSelectSomeBsDeclaredSQL(org.jboss.test.cmp2.ejbselect.ALocal a)"
 *    ejb-name="B"
 *    alias="b"
 *    from=", TEST_A a"
 *    where="a.ID={0.id} AND b.A_ID=a.ID"
 *
 * @author others + <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 */
public abstract class ABean implements EntityBean
{
   private EntityContext ctx;

   /**
    * @ejb.pk-field
    * @ejb.persistent-field
    * @ejb.interface-method
    * @ejb.persistence  column-name="ID"
    */
   public abstract String getId();
   public abstract void setId(String id);

   /**
    * @ejb.persistent-field
    * @ejb.interface-method
    * @ejb.persistence  column-name="INT_FIELD"
    */
   public abstract int getIntField();
   /**
    * @ejb.interface-method
    */
   public abstract void setIntField(int value);

   /**
    * @ejb.relation
    *    name="A-B"
    *    role-name="A-has-Bs"
    * @ejb.interface-method
    */
   public abstract Collection getBs();
   /**
    * @ejb.interface-method
    */
   public abstract void setBs(Collection Bs);

   // ejbSelect methods

   /**
    * @ejb.select query="SELECT OBJECT(b) FROM B AS b WHERE b.a = ?1"
    */
   public abstract Collection ejbSelectSomeBs(ALocal a) throws FinderException;

   /**
    * @ejb.select query="SELECT DISTINCT OBJECT(a) FROM A AS a WHERE a.bs IS NOT EMPTY"
    */
   public abstract Collection ejbSelectAWithBs() throws FinderException;

   /**
    * Declared SQL
    * @ejb.select query=""
    */
   public abstract Collection ejbSelectSomeBsDeclaredSQL(ALocal a) throws FinderException;

   /**
    * NOTE: -1234 does not exist
    * @ejb.select query="SELECT SUM(a.intField) FROM A AS a WHERE a.intField = -1234"
    */
   public abstract int ejbSelectNullSum() throws FinderException;

   /**
    * Dynamic QL
    * @ejb.select query=""
    */
   public abstract Collection ejbSelectDynamic(String ql, Object[] params) throws FinderException;

   // Home methods

   /**
    * @ejb.home-method
    */
   public Collection ejbHomeSelectDynamic(String ql, Object[] params) throws FinderException
   {
      return ejbSelectDynamic(ql, params);
   }
   
   // Interface methods

   /**
    * @ejb.interface-method
    */
   public Collection getSomeBs() throws FinderException
   {
      return ejbSelectSomeBs((ALocal)ctx.getEJBLocalObject());
   }

   /**
    * @ejb.interface-method
    */
   public Collection getSomeBsDeclaredSQL() throws FinderException
   {
      return ejbSelectSomeBsDeclaredSQL((ALocal)ctx.getEJBLocalObject());
   }

   /**
    * @ejb.interface-method
    */
   public Collection getAWithBs() throws FinderException
   {
      return ejbSelectAWithBs();
   }

   // Home methods

   /**
    * @ejb.home-method
    */
   public Collection ejbHomeGetSomeBs(ALocal a) throws FinderException
   {
      return ejbSelectSomeBs(a);
   }

   /**
    * @ejb.home-method
    */
   public Collection ejbHomeGetSomeBsDeclaredSQL(ALocal a) throws FinderException
   {
      return ejbSelectSomeBsDeclaredSQL(a);
   }

   /**
    * @ejb.home-method
    */
   public void ejbHomeCheckFinderForNull() throws FinderException
   {
      ejbSelectNullSum();
   }

   /**
    * @ejb.create-method
    */
   public String ejbCreate(String id) throws CreateException
   {
      setId(id);
      return null;
   }

   public void ejbPostCreate(String id) {}

   public void setEntityContext(EntityContext ctx)
   {
      this.ctx = ctx;
   }

   public void unsetEntityContext()
   {
      this.ctx = null;
   }

   public void ejbRemove() throws RemoveException {}
   public void ejbActivate() {}
   public void ejbPassivate() {}
   public void ejbLoad() {}
   public void ejbStore() {}
}
