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
package org.jboss.test.cmp2.jbas3541;


import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;
import javax.ejb.CreateException;


/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 85945 $</tt>
 */
public abstract class ABean implements EntityBean
{
   // CMP accessors --------------------------------------------
   /**
    * @ejb.pk-field
    * @ejb.persistent-field
    * @ejb.interface-method
    */
   public abstract Long getId();

   public abstract void setId(Long id);

   /**
    * @ejb.persistent-field
    * @ejb.interface-method
    */
   public abstract Integer getIntField();

  /**
   * @ejb.interface-method
   */
   public abstract void setIntField(Integer i);

   /**
    * @throws javax.ejb.CreateException
    * @ejb.create-method
    */
   public Long ejbCreate(Long id, Integer i)
      throws CreateException
   {
      setId(id);
      setIntField(i);
      return null;
   }

   public void ejbPostCreate(Long id, Integer i)
   {
   }

   public void setEntityContext(EntityContext ctx)
   {
   }

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
