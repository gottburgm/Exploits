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

import javax.ejb.EntityContext;
import javax.ejb.EntityBean;
import javax.ejb.EJBException;
import javax.ejb.RemoveException;
import javax.ejb.CreateException;
import java.rmi.RemoteException;
import java.util.Collection;

/**
 * @ejb.bean
 *    name="Department"
 *    type="CMP"
 *    cmp-version="2.x"
 *    view-type="local"
 *    reentrant="false"
 *    local-jndi-name="Department"
 * @ejb.pk generate="true"
 * @ejb.util generate="physical"
 * @ejb.persistence table-name="DEPARTMENT"
 * @jboss.persistence
 *    create-table="true"
 *    remove-table="true"
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 */
public abstract class DepartmentEntityBean
   implements EntityBean
{
   // Attributes ---------------------------------------------------
   private EntityContext ctx;

   // CMP accessors
   /**
    * @ejb.pk-field
    * @ejb.persistent-field
    * @ejb.interface-method
    * @ejb.persistence column-name="DEPT_CODE"
    */
   public abstract String getDepartmentCode();
   public abstract void setDepartmentCode(String deptCode);

   /**
    * @ejb.pk-field
    * @ejb.persistent-field
    * @ejb.interface-method
    * @ejb.persistence column-name="DEPT_CODE2"
    */
   public abstract String getDepartmentCode2();
   public abstract void setDepartmentCode2(String deptCode);

   /**
    * @ejb.persistent-field
    * @ejb.interface-method
    * @ejb.persistence column-name="DESCR"
    */
   public abstract String getDescription();
   public abstract void setDescription(String description);

   // CMR accessors
   /**
    * @ejb.interface-method
    * @ejb.relation
    *    name="Institute-Department-StandaloneFK"
    *    role-name="Department-has-Institute"
    * @jboss.relation
    *    fk-column="INST_ID_FK"
    *    related-pk-field="instituteId"
    */
   public abstract InstituteLocal getInstitute();
   /**
    * @ejb.interface-method
    */
   public abstract void setInstitute(InstituteLocal institute);

   /**
    * @ejb.interface-method
    * @ejb.relation
    *    name="Department-Group-CompleteFKToPK"
    *    role-name="Department-has-Groups"
    */
   public abstract Collection getGroups();
   /**
    * @ejb.interface-method
    */
   public abstract void setGroups(Collection groups);

   /**
    * @ejb.interface-method
    * @ejb.relation
    *    name="Department-Student-CompleteFKToPK"
    *    role-name="Department-has-Students"
    */
   public abstract Collection getStudents();
   /**
    * @ejb.interface-method
    */
   public abstract void setStudents(Collection students);

   // EntityBean implementation ------------------------------------
   /**
    * @ejb.create-method
    */
   public DepartmentPK ejbCreate(String deptCode, String descr)
      throws CreateException
   {
      setDepartmentCode(deptCode);
      setDepartmentCode2("X"+deptCode);
      setDescription(descr);
      return null;
   }

   public void ejbPostCreate(String deptCode, String descr) {}

   public void ejbActivate() throws EJBException, RemoteException {}
   public void ejbLoad() throws EJBException, RemoteException {}
   public void ejbPassivate() throws EJBException, RemoteException {}
   public void ejbRemove() throws RemoveException, EJBException, RemoteException {}
   public void ejbStore() throws EJBException, RemoteException {}
   public void setEntityContext(EntityContext ctx) throws EJBException, RemoteException
   {
      this.ctx = ctx;
   }
   public void unsetEntityContext() throws EJBException, RemoteException
   {
      this.ctx = null;
   }
}
