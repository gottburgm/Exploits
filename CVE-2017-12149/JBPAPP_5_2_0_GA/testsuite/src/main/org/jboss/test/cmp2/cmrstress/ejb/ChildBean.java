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
package org.jboss.test.cmp2.cmrstress.ejb;

import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;

import org.jboss.logging.Logger;

/**
 * The problem child.
 * 
 * This code is based upon the original test case provided by Andrew May.
 *
 * @version <tt>$Revision: 81036 $</tt>
 * @author  <a href="mailto:steve@resolvesw.com">Steve Coy</a>.
 *
 * @ejb.bean name="Child"
 *           type="CMP"
 *           cmp-version="2.x"
 *           view-type="local"
 *           jndi-name="cmrstress/Child"
 *           primkey-field="id"
 *           schema="Child"
 * 
 * @ejb.pk class="java.lang.String"
 *         generate="false"
 * 
 * @ejb.persistence table-name="StressedChild"
 * 
 * @ejb.home generate="both"
 * @ejb.interface generate="local"
 * 
 * @ejb.transaction type="Supports"
 * 
 * @jboss.persistence
 *       create-table="true"
 *       remove-table="true"
 * @jboss.tuned-updates tune="true"
 */
public abstract class ChildBean implements EntityBean
{
   /**
    * CMP get method for Id attribute.
    * @ejb.interface-method view-type="local"
    * @ejb.persistent-field
    * @jboss.column-name name="id"
    * jboss.method-attributes read-only="true"
    */
   public abstract String getId();

   /**
    * CMP set method for Id attribute.
    * @ejb.interface-method view-type="local"
    * @ejb.transaction type="Mandatory"
    */
   public abstract void setId(String id);
   
   /**
    * CMP get method for Name attribute.
    * @ejb.interface-method view-type="local"
    * @ejb.persistent-field
    * @jboss.column-name name="name"
    * jboss.method-attributes read-only="true"
    */
   public abstract String getName();

   /**
    * CMP set method for Name attribute.
    * @ejb.interface-method view-type="local"
    * @ejb.transaction type="Mandatory"
    */
   public abstract void setName(String name);

   /**
    * CMP get method for Value attribute.
    * @ejb.interface-method view-type="local"
    * @ejb.persistent-field
    * @jboss.column-name name="value"
    * jboss.method-attributes read-only="true"
    */
   public abstract String getValue();

   /**
    * CMP set method for Value attribute.
    * @ejb.interface-method view-type="local"
    * @ejb.transaction type="Mandatory"
    */
   public abstract void setValue(String value);
   
   /**
    * Create method for Entity.
    * @ejb.create-method view-type="local"
    * @ejb.transaction type="Mandatory"
    */
   public String ejbCreate(String id, String name, String value) throws javax.ejb.CreateException
   {
      msLog.debug("Created with pk: " + id);
      setId(id);
      setName(name);
      setValue(value);
      return null;
   }

   public void ejbPostCreate(String id, String name, String value)
   {
   }
   
   /**
    * @see javax.ejb.EntityBean#ejbActivate()
    */
   public void ejbActivate()
   {
   }

   /**
    * @see javax.ejb.EntityBean#ejbLoad()
    */
   public void ejbLoad()
   {
   }

   /**
    * @see javax.ejb.EntityBean#ejbPassivate()
    */
   public void ejbPassivate()
   {
   }

   /**
    * @see javax.ejb.EntityBean#ejbRemove()
    */
   public void ejbRemove() throws RemoveException
   {
   }

   /**
    * @see javax.ejb.EntityBean#ejbStore()
    */
   public void ejbStore()
   {
   }

   /**
    * @see javax.ejb.EntityBean#setEntityContext(javax.ejb.EntityContext)
    */
   public void setEntityContext(EntityContext arg0)
   {
   }

   /**
    * @see javax.ejb.EntityBean#unsetEntityContext()
    */
   public void unsetEntityContext()
   {
   }

   private static final Logger   msLog = Logger.getLogger(ChildBean.class);

}
