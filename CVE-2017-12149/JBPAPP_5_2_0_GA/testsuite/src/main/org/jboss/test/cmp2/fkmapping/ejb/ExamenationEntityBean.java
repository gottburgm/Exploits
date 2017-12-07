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

/**
 * @ejb.bean
 *    name="Examenation"
 *    type="CMP"
 *    cmp-version="2.x"
 *    view-type="local"
 *    reentrant="false"
 *    local-jndi-name="Examenation"
 * @ejb.pk generate="true"
 * @ejb.util generate="physical"
 * @ejb.persistence table-name="EXAM"
 * @jboss.persistence
 *    create-table="true"
 *    remove-table="true"
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 */
public abstract class ExamenationEntityBean
   implements EntityBean
{
   // Attributes ---------------------------------------------------
   private EntityContext ctx;

   // CMP accessors
   /**
    * @ejb.pk-field
    * @ejb.persistent-field
    * @ejb.interface-method
    * @ejb.persistence column-name="EXAM_ID"
    */
   public abstract String getExamId();
   public abstract void setExamId(String examId);

   /**
    * @ejb.persistent-field
    * @ejb.interface-method
    * @ejb.persistence column-name="SUBJECT"
    */
   public abstract String getSubject();
   /**
    * @ejb.interface-method
    */
   public abstract void setSubject(String subject);

   /**
    * @ejb.persistent-field
    * @ejb.interface-method
    * @ejb.persistence column-name="DEPT_CODE"
    */
   public abstract String getDepartmentCode();
   /**
    * @ejb.interface-method
    */
   public abstract void setDepartmentCode(String deptCode);

   /**
    * @ejb.persistent-field
    * @ejb.interface-method
    * @ejb.persistence column-name="DEPT_CODE2"
    */
   public abstract String getDepartmentCode2();
   /**
    * @ejb.interface-method
    */
   public abstract void setDepartmentCode2(String deptCode);

   /**
    * @ejb.persistent-field
    * @ejb.interface-method
    * @ejb.persistence column-name="GROUP_NUM"
    */
   public abstract long getGroupNumber();
   /**
    * @ejb.interface-method
    */
   public abstract void setGroupNumber(long groupNum);

   // CMR accessors
   /**
    * @ejb.interface-method
    * @ejb.relation
    *    name="Group-Exam-FKToCMP"
    *    role-name="Exam-has-Group"
    * @jboss.relation
    *    fk-column="DEPT_CODE"
    *    related-pk-field="departmentCode"
    * @jboss.relation
    *    fk-column="DEPT_CODE2"
    *    related-pk-field="departmentCode2"
    * @jboss.relation
    *    fk-column="GROUP_NUM"
    *    related-pk-field="groupNumber"
    */
   public abstract GroupLocal getGroup();
   /**
    * @ejb.interface-method
    */
   public abstract void setGroup(GroupLocal group);

   // EntityBean implementation ------------------------------------
   /**
    * @ejb.create-method
    */
   public ExamenationPK ejbCreate(String examId, String subject, String deptCode, long groupNum)
      throws CreateException
   {
      setExamId(examId);
      setSubject(subject);
      setDepartmentCode(deptCode);
      setDepartmentCode2("X"+deptCode);
      setGroupNumber(groupNum);
      return null;
   }

   public void ejbPostCreate(String examId, String subject, String deptCode, long groupNum) {}

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
